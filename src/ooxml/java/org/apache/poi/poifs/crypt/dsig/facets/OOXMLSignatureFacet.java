/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.apache.poi.poifs.crypt.dsig.facets;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService.RelationshipTransformParameterSpec;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTSignatureTime;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.SignatureTimeDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.microsoft.schemas.office.x2006.digsig.CTSignatureInfoV1;
import com.microsoft.schemas.office.x2006.digsig.SignatureInfoV1Document;

/**
 * Office OpenXML Signature Facet implementation.
 * 
 * @author fcorneli
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc313071.aspx">[MS-OFFCRYPTO]: Office Document Cryptography Structure</a>
 */
public class OOXMLSignatureFacet extends SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(OOXMLSignatureFacet.class);

    @Override
    public void preSign(
        Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {
        LOG.log(POILogger.DEBUG, "pre sign");
        addManifestObject(document, references, objects);
        addSignatureInfo(document, references, objects);
    }

    protected void addManifestObject(
        Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {

        List<Reference> manifestReferences = new ArrayList<Reference>();
        addManifestReferences(manifestReferences);
        Manifest manifest =  getSignatureFactory().newManifest(manifestReferences);
        
        String objectId = "idPackageObject"; // really has to be this value.
        List<XMLStructure> objectContent = new ArrayList<XMLStructure>();
        objectContent.add(manifest);

        addSignatureTime(document, objectContent);

        XMLObject xo = getSignatureFactory().newXMLObject(objectContent, objectId, null, null);
        objects.add(xo);

        Reference reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);
    }

    @SuppressWarnings("resource")
    protected void addManifestReferences(List<Reference> manifestReferences)
    throws XMLSignatureException {

        OPCPackage ooxml = signatureConfig.getOpcPackage();
        List<PackagePart> relsEntryNames = ooxml.getPartsByContentType(ContentTypes.RELATIONSHIPS_PART);

        Set<String> digestedPartNames = new HashSet<String>();
        for (PackagePart pp : relsEntryNames) {
            String baseUri = pp.getPartName().getName().replaceFirst("(.*)/_rels/.*", "$1");

            PackageRelationshipCollection prc;
            try {
                prc = new PackageRelationshipCollection(ooxml);
                prc.parseRelationshipsPart(pp);
            } catch (InvalidFormatException e) {
                throw new XMLSignatureException("Invalid relationship descriptor: "+pp.getPartName().getName(), e);
            }
            
            RelationshipTransformParameterSpec parameterSpec = new RelationshipTransformParameterSpec();
            for (PackageRelationship relationship : prc) {
                String relationshipType = relationship.getRelationshipType();
                
                /*
                 * ECMA-376 Part 2 - 3rd edition
                 * 13.2.4.16 Manifest Element
                 * "The producer shall not create a Manifest element that references any data outside of the package."
                 */
                if (TargetMode.EXTERNAL == relationship.getTargetMode()) {
                    continue;
                }

                if (!isSignedRelationship(relationshipType)) continue;

                parameterSpec.addRelationshipReference(relationship.getId());

                // TODO: find a better way ...
                String partName = relationship.getTargetURI().toString();
                if (!partName.startsWith(baseUri)) {
                    partName = baseUri + partName;
                }
                try {
                    partName = new URI(partName).normalize().getPath().replace('\\', '/');
                    LOG.log(POILogger.DEBUG, "part name: " + partName);
                } catch (URISyntaxException e) {
                    throw new XMLSignatureException(e);
                }
                
                String contentType;
                try {
                    PackagePartName relName = PackagingURIHelper.createPartName(partName);
                    PackagePart pp2 = ooxml.getPart(relName);
                    contentType = pp2.getContentType();
                } catch (InvalidFormatException e) {
                    throw new XMLSignatureException(e);
                }
                
                if (relationshipType.endsWith("customXml")
                    && !(contentType.equals("inkml+xml") || contentType.equals("text/xml"))) {
                    LOG.log(POILogger.DEBUG, "skipping customXml with content type: " + contentType);
                    continue;
                }
                
                if (!digestedPartNames.contains(partName)) {
                    // We only digest a part once.
                    String uri = partName + "?ContentType=" + contentType;
                    Reference reference = newReference(uri, null, null, null, null);
                    manifestReferences.add(reference);
                    digestedPartNames.add(partName);
                }
            }
            
            if (parameterSpec.hasSourceIds()) {
                List<Transform> transforms = new ArrayList<Transform>();
                transforms.add(newTransform(RelationshipTransformService.TRANSFORM_URI, parameterSpec));
                transforms.add(newTransform(CanonicalizationMethod.INCLUSIVE));
                String uri = pp.getPartName().getName()
                    + "?ContentType=application/vnd.openxmlformats-package.relationships+xml";
                Reference reference = newReference(uri, transforms, null, null, null);
                manifestReferences.add(reference);
            }
        }
    }


    protected void addSignatureTime(Document document, List<XMLStructure> objectContent) {
        /*
         * SignatureTime
         */
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
        fmt.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        String nowStr = fmt.format(signatureConfig.getExecutionTime());
        LOG.log(POILogger.DEBUG, "now: " + nowStr);

        SignatureTimeDocument sigTime = SignatureTimeDocument.Factory.newInstance();
        CTSignatureTime ctTime = sigTime.addNewSignatureTime();
        ctTime.setFormat("YYYY-MM-DDThh:mm:ssTZD");
        ctTime.setValue(nowStr);

        Element n = (Element)document.importNode(ctTime.getDomNode(),true);
        List<XMLStructure> signatureTimeContent = new ArrayList<XMLStructure>();
        signatureTimeContent.add(new DOMStructure(n));
        SignatureProperty signatureTimeSignatureProperty = getSignatureFactory()
            .newSignatureProperty(signatureTimeContent, "#" + signatureConfig.getPackageSignatureId(),
            "idSignatureTime");
        List<SignatureProperty> signaturePropertyContent = new ArrayList<SignatureProperty>();
        signaturePropertyContent.add(signatureTimeSignatureProperty);
        SignatureProperties signatureProperties = getSignatureFactory()
            .newSignatureProperties(signaturePropertyContent,
            "id-signature-time-" + signatureConfig.getExecutionTime());
        objectContent.add(signatureProperties);
    }

    protected void addSignatureInfo(Document document,
        List<Reference> references,
        List<XMLObject> objects)
    throws XMLSignatureException {
        List<XMLStructure> objectContent = new ArrayList<XMLStructure>();

        SignatureInfoV1Document sigV1 = SignatureInfoV1Document.Factory.newInstance();
        CTSignatureInfoV1 ctSigV1 = sigV1.addNewSignatureInfoV1();
        ctSigV1.setManifestHashAlgorithm(signatureConfig.getDigestMethodUri());
        Element n = (Element)document.importNode(ctSigV1.getDomNode(), true);
        n.setAttributeNS(XML_NS, XMLConstants.XMLNS_ATTRIBUTE, MS_DIGSIG_NS);
        
        List<XMLStructure> signatureInfoContent = new ArrayList<XMLStructure>();
        signatureInfoContent.add(new DOMStructure(n));
        SignatureProperty signatureInfoSignatureProperty = getSignatureFactory()
            .newSignatureProperty(signatureInfoContent, "#" + signatureConfig.getPackageSignatureId(),
            "idOfficeV1Details");

        List<SignatureProperty> signaturePropertyContent = new ArrayList<SignatureProperty>();
        signaturePropertyContent.add(signatureInfoSignatureProperty);
        SignatureProperties signatureProperties = getSignatureFactory()
            .newSignatureProperties(signaturePropertyContent, null);
        objectContent.add(signatureProperties);

        String objectId = "idOfficeObject";
        objects.add(getSignatureFactory().newXMLObject(objectContent, objectId, null, null));

        Reference reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);
    }

    protected static String getRelationshipReferenceURI(String zipEntryName) {
        return "/"
            + zipEntryName
            + "?ContentType=application/vnd.openxmlformats-package.relationships+xml";
    }

    protected static String getResourceReferenceURI(String resourceName, String contentType) {
        return "/" + resourceName + "?ContentType=" + contentType;
    }

    protected static boolean isSignedRelationship(String relationshipType) {
        LOG.log(POILogger.DEBUG, "relationship type: " + relationshipType);
        for (String signedTypeExtension : signed) {
            if (relationshipType.endsWith(signedTypeExtension)) {
                return true;
            }
        }
        if (relationshipType.endsWith("customXml")) {
            LOG.log(POILogger.DEBUG, "customXml relationship type");
            return true;
        }
        return false;
    }
    
    public static final String[] contentTypes = {
        /*
         * Word
         */
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml",
        "application/vnd.openxmlformats-officedocument.theme+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml",

        /*
         * Word 2010
         */
        "application/vnd.ms-word.stylesWithEffects+xml",

        /*
         * Excel
         */
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",

        /*
         * Powerpoint
         */
        "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml",

        /*
         * Powerpoint 2010
         */
        "application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"
    };

    /**
     * Office 2010 list of signed types (extensions).
     */
    public static final String[] signed = {
        "powerPivotData", //
        "activeXControlBinary", //
        "attachedToolbars", //
        "connectorXml", //
        "downRev", //
        "functionPrototypes", //
        "graphicFrameDoc", //
        "groupShapeXml", //
        "ink", //
        "keyMapCustomizations", //
        "legacyDiagramText", //
        "legacyDocTextInfo", //
        "officeDocument", //
        "pictureXml", //
        "shapeXml", //
        "smartTags", //
        "ui/altText", //
        "ui/buttonSize", //
        "ui/controlID", //
        "ui/description", //
        "ui/enabled", //
        "ui/extensibility", //
        "ui/helperText", //
        "ui/imageID", //
        "ui/imageMso", //
        "ui/keyTip", //
        "ui/label", //
        "ui/lcid", //
        "ui/loud", //
        "ui/pressed", //
        "ui/progID", //
        "ui/ribbonID", //
        "ui/showImage", //
        "ui/showLabel", //
        "ui/supertip", //
        "ui/target", //
        "ui/text", //
        "ui/title", //
        "ui/tooltip", //
        "ui/userCustomization", //
        "ui/visible", //
        "userXmlData", //
        "vbaProject", //
        "wordVbaData", //
        "wsSortMap", //
        "xlBinaryIndex", //
        "xlExternalLinkPath/xlAlternateStartup", //
        "xlExternalLinkPath/xlLibrary", //
        "xlExternalLinkPath/xlPathMissing", //
        "xlExternalLinkPath/xlStartup", //
        "xlIntlMacrosheet", //
        "xlMacrosheet", //
        "customData", //
        "diagramDrawing", //
        "hdphoto", //
        "inkXml", //
        "media", //
        "slicer", //
        "slicerCache", //
        "stylesWithEffects", //
        "ui/extensibility", //
        "chartColorStyle", //
        "chartLayout", //
        "chartStyle", //
        "dictionary", //
        "timeline", //
        "timelineCache", //
        "aFChunk", //
        "attachedTemplate", //
        "audio", //
        "calcChain", //
        "chart", //
        "chartsheet", //
        "chartUserShapes", //
        "commentAuthors", //
        "comments", //
        "connections", //
        "control", //
        "customProperty", //
        "customXml", //
        "diagramColors", //
        "diagramData", //
        "diagramLayout", //
        "diagramQuickStyle", //
        "dialogsheet", //
        "drawing", //
        "endnotes", //
        "externalLink", //
        "externalLinkPath", //
        "font", //
        "fontTable", //
        "footer", //
        "footnotes", //
        "glossaryDocument", //
        "handoutMaster", //
        "header", //
        "hyperlink", //
        "image", //
        "mailMergeHeaderSource", //
        "mailMergeRecipientData", //
        "mailMergeSource", //
        "notesMaster", //
        "notesSlide", //
        "numbering", //
        "officeDocument", //
        "oleObject", //
        "package", //
        "pivotCacheDefinition", //
        "pivotCacheRecords", //
        "pivotTable", //
        "presProps", //
        "printerSettings", //
        "queryTable", //
        "recipientData", //
        "settings", //
        "sharedStrings", //
        "sheetMetadata", //
        "slide", //
        "slideLayout", //
        "slideMaster", //
        "slideUpdateInfo", //
        "slideUpdateUrl", //
        "styles", //
        "table", //
        "tableSingleCells", //
        "tableStyles", //
        "tags", //
        "theme", //
        "themeOverride", //
        "transform", //
        "video", //
        "viewProps", //
        "volatileDependencies", //
        "webSettings", //
        "worksheet", //
        "xmlMaps", //
        "ctrlProp", //
        "customData", //
        "diagram", //
        "diagramColorsHeader", //
        "diagramLayoutHeader", //
        "diagramQuickStyleHeader", //
        "documentParts", //
        "slicer", //
        "slicerCache", //
        "vmlDrawing" //
    };
}