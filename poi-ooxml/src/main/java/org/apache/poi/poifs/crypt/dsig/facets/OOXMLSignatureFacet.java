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

import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacetHelper.newReference;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacetHelper.newTransform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.crypto.URIReference;
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
import javax.xml.crypto.dsig.XMLSignatureFactory;

import com.microsoft.schemas.office.x2006.digsig.CTSignatureInfoV1;
import com.microsoft.schemas.office.x2006.digsig.SignatureInfoV1Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService.RelationshipTransformParameterSpec;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTSignatureTime;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.SignatureTimeDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Office OpenXML Signature Facet implementation.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc313071.aspx">[MS-OFFCRYPTO]: Office Document Cryptography Structure</a>
 */
public class OOXMLSignatureFacet implements SignatureFacet {

    private static final Logger LOG = LogManager.getLogger(OOXMLSignatureFacet.class);
    private static final String ID_PACKAGE_OBJECT = "idPackageObject";

    @Override
    public void preSign(
          SignatureInfo signatureInfo
        , Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {
        LOG.atDebug().log("pre sign");
        addManifestObject(signatureInfo, document, references, objects);
        addSignatureInfo(signatureInfo, document, references, objects);
    }

    protected void addManifestObject(
          SignatureInfo signatureInfo
        , Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {

        final XMLSignatureFactory sigFac = signatureInfo.getSignatureFactory();

        List<Reference> manifestReferences = new ArrayList<>();
        addManifestReferences(signatureInfo, manifestReferences);
        Manifest manifest = sigFac.newManifest(manifestReferences);

        List<XMLStructure> objectContent = new ArrayList<>();
        objectContent.add(manifest);

        addSignatureTime(signatureInfo, document, objectContent);

        XMLObject xo = sigFac.newXMLObject(objectContent, ID_PACKAGE_OBJECT, null, null);
        objects.add(xo);

        Reference reference = newReference(signatureInfo, "#"+ID_PACKAGE_OBJECT, null, XML_DIGSIG_NS+"Object");
        references.add(reference);
    }

    @SuppressWarnings("resource")
    protected void addManifestReferences(SignatureInfo signatureInfo, List<Reference> manifestReferences)
    throws XMLSignatureException {
        OPCPackage opcPackage = signatureInfo.getOpcPackage();
        List<PackagePart> relsEntryNames = opcPackage.getPartsByContentType(ContentTypes.RELATIONSHIPS_PART);

        Set<String> digestedPartNames = new HashSet<>();
        for (PackagePart pp : relsEntryNames) {
            final String baseUri = pp.getPartName().getName().replaceFirst("(.*)/_rels/.*", "$1");

            PackageRelationshipCollection prc;
            try {
                prc = new PackageRelationshipCollection(opcPackage);
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
                    // only add the relationship but not the reference/data
                    parameterSpec.addRelationshipReference(relationship.getId());
                    continue;
                }

                if (!isSignedRelationship(relationshipType)) {
                    continue;
                }

                parameterSpec.addRelationshipReference(relationship.getId());

                String partName = normalizePartName(relationship.getTargetURI(), baseUri);

                // We only digest a part once.
                if (digestedPartNames.contains(partName)) {
                    continue;
                }
                digestedPartNames.add(partName);

                String contentType;
                try {
                    PackagePartName relName = PackagingURIHelper.createPartName(partName);
                    PackagePart pp2 = opcPackage.getPart(relName);
                    contentType = pp2.getContentType();
                } catch (InvalidFormatException e) {
                    throw new XMLSignatureException(e);
                }

                if (relationshipType.endsWith("customXml")
                    && !(contentType.equals("inkml+xml") || contentType.equals("text/xml"))) {
                    LOG.atDebug().log("skipping customXml with content type: {}", contentType);
                    continue;
                }

                String uri = partName + "?ContentType=" + contentType;
                Reference reference = newReference(signatureInfo, uri, null, null);
                manifestReferences.add(reference);
            }

            if (parameterSpec.hasSourceIds()) {
                List<Transform> transforms = new ArrayList<>();
                transforms.add(newTransform(signatureInfo, RelationshipTransformService.TRANSFORM_URI, parameterSpec));
                transforms.add(newTransform(signatureInfo, CanonicalizationMethod.INCLUSIVE));
                String uri = normalizePartName(pp.getPartName().getURI(), baseUri)
                    + "?ContentType=application/vnd.openxmlformats-package.relationships+xml";
                Reference reference = newReference(signatureInfo, uri, transforms, null);
                manifestReferences.add(reference);
            }
        }

        manifestReferences.sort(Comparator.comparing(URIReference::getURI));
    }

    /**
     * Normalize a URI/part name
     * TODO: find a better way ...
     */
    private static String normalizePartName(URI partName, String baseUri) throws XMLSignatureException {
        String pn = partName.toASCIIString();
        if (!pn.startsWith(baseUri)) {
            pn = baseUri + pn;
        }
        try {
            pn = new URI(pn).normalize().getPath().replace('\\', '/');
            LOG.atDebug().log("part name: {}", pn);
        } catch (URISyntaxException e) {
            throw new XMLSignatureException(e);
        }
        return pn;
    }


    protected void addSignatureTime(SignatureInfo signatureInfo, Document document, List<XMLStructure> objectContent) {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        XMLSignatureFactory sigFac = signatureInfo.getSignatureFactory();
        /*
         * SignatureTime
         */
        SignatureTimeDocument sigTime = SignatureTimeDocument.Factory.newInstance();
        CTSignatureTime ctTime = sigTime.addNewSignatureTime();
        ctTime.setFormat("YYYY-MM-DDThh:mm:ssTZD");
        ctTime.setValue(signatureConfig.formatExecutionTime());
        LOG.atDebug().log("execution time: {}", ctTime.getValue());

        Element n = (Element)document.importNode(ctTime.getDomNode(),true);
        List<XMLStructure> signatureTimeContent = new ArrayList<>();
        signatureTimeContent.add(new DOMStructure(n));
        SignatureProperty signatureTimeSignatureProperty = sigFac
            .newSignatureProperty(signatureTimeContent, "#" + signatureConfig.getPackageSignatureId(),
            "idSignatureTime");
        List<SignatureProperty> signaturePropertyContent = new ArrayList<>();
        signaturePropertyContent.add(signatureTimeSignatureProperty);
        SignatureProperties signatureProperties = sigFac
            .newSignatureProperties(signaturePropertyContent, null);
        objectContent.add(signatureProperties);
    }

    protected void addSignatureInfo(
          SignatureInfo signatureInfo
        , Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        XMLSignatureFactory sigFac = signatureInfo.getSignatureFactory();

        List<XMLStructure> objectContent = new ArrayList<>();

        SignatureInfoV1Document sigV1 = createSignatureInfoV1(signatureInfo);
        Element n = (Element)document.importNode(sigV1.getSignatureInfoV1().getDomNode(), true);
        // n.setAttributeNS(XML_NS, XMLConstants.XMLNS_ATTRIBUTE, MS_DIGSIG_NS);

        List<XMLStructure> signatureInfoContent = new ArrayList<>();
        signatureInfoContent.add(new DOMStructure(n));
        SignatureProperty signatureInfoSignatureProperty = sigFac
            .newSignatureProperty(signatureInfoContent, "#" + signatureConfig.getPackageSignatureId(),
            "idOfficeV1Details");

        List<SignatureProperty> signaturePropertyContent = new ArrayList<>();
        signaturePropertyContent.add(signatureInfoSignatureProperty);
        SignatureProperties signatureProperties = sigFac
            .newSignatureProperties(signaturePropertyContent, null);
        objectContent.add(signatureProperties);

        String objectId = "idOfficeObject";
        objects.add(sigFac.newXMLObject(objectContent, objectId, null, null));

        Reference reference = newReference(signatureInfo, "#" + objectId, null, XML_DIGSIG_NS+"Object");
        references.add(reference);

        Base64.Encoder enc = Base64.getEncoder();
        byte[] imageValid = signatureConfig.getSignatureImageValid();
        if (imageValid != null) {
            objectId = "idValidSigLnImg";
            DOMStructure tn = new DOMStructure(document.createTextNode(enc.encodeToString(imageValid)));
            objects.add(sigFac.newXMLObject(Collections.singletonList(tn), objectId, null, null));

            reference = newReference(signatureInfo, "#" + objectId, null, XML_DIGSIG_NS+"Object");
            references.add(reference);
        }

        byte[] imageInvalid = signatureConfig.getSignatureImageInvalid();
        if (imageInvalid != null) {
            objectId = "idInvalidSigLnImg";
            DOMStructure tn = new DOMStructure(document.createTextNode(enc.encodeToString(imageInvalid)));
            objects.add(sigFac.newXMLObject(Collections.singletonList(tn), objectId, null, null));

            reference = newReference(signatureInfo, "#" + objectId, null, XML_DIGSIG_NS+"Object");
            references.add(reference);
        }
    }

    /**
     * Create SignatureInfoV1 element. This method can be easily extended by subclasses, to fill its other elements.
     */
    protected SignatureInfoV1Document createSignatureInfoV1(SignatureInfo signatureInfo) {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();

        SignatureInfoV1Document sigV1 = SignatureInfoV1Document.Factory.newInstance();
        CTSignatureInfoV1 ctSigV1 = sigV1.addNewSignatureInfoV1();
        if (signatureConfig.getDigestAlgo() != HashAlgorithm.sha1) {
            ctSigV1.setManifestHashAlgorithm(signatureConfig.getDigestMethodUri());
        }

        String desc = signatureConfig.getSignatureDescription();
        if (desc != null) {
            ctSigV1.setSignatureComments(desc);
        }

        byte[] image = signatureConfig.getSignatureImage();
        if (image == null) {
            ctSigV1.setSignatureType(1);
        } else {
            ctSigV1.setSetupID(signatureConfig.getSignatureImageSetupId().toString());
            ctSigV1.setSignatureImage(image);
            ctSigV1.setSignatureType(2);
        }

        return sigV1;
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
        LOG.atDebug().log("relationship type: {}", relationshipType);
        String rt = relationshipType.replaceFirst(".*/relationships/", "");
        return (signed.contains(rt) || rt.endsWith("customXml"));
    }

    /**
     * Office 2010 list of signed types (extensions).
     */
    private static final Set<String> signed = Stream.of(
            "activeXControlBinary", "aFChunk", "attachedTemplate", "attachedToolbars", "audio", "calcChain", "chart", "chartColorStyle",
            "chartLayout", "chartsheet", "chartStyle", "chartUserShapes", "commentAuthors", "comments", "connections", "connectorXml",
            "control", "ctrlProp", "customData", "customData", "customProperty", "customXml", "diagram", "diagramColors",
            "diagramColorsHeader", "diagramData", "diagramDrawing", "diagramLayout", "diagramLayoutHeader", "diagramQuickStyle",
            "diagramQuickStyleHeader", "dialogsheet", "dictionary", "documentParts", "downRev", "drawing", "endnotes", "externalLink",
            "externalLinkPath", "font", "fontTable", "footer", "footnotes", "functionPrototypes", "glossaryDocument", "graphicFrameDoc",
            "groupShapeXml", "handoutMaster", "hdphoto", "header", "hyperlink", "image", "ink", "inkXml", "keyMapCustomizations",
            "legacyDiagramText", "legacyDocTextInfo", "mailMergeHeaderSource", "mailMergeRecipientData", "mailMergeSource", "media",
            "notesMaster", "notesSlide", "numbering", "officeDocument", "officeDocument", "oleObject", "package", "pictureXml",
            "pivotCacheDefinition", "pivotCacheRecords", "pivotTable", "powerPivotData", "presProps", "printerSettings", "queryTable",
            "recipientData", "settings", "shapeXml", "sharedStrings", "sheetMetadata", "slicer", "slicer", "slicerCache", "slicerCache",
            "slide", "slideLayout", "slideMaster", "slideUpdateInfo", "slideUpdateUrl", "smartTags", "styles", "stylesWithEffects",
            "table", "tableSingleCells", "tableStyles", "tags", "theme", "themeOverride", "timeline", "timelineCache", "transform",
            "ui/altText", "ui/buttonSize", "ui/controlID", "ui/description", "ui/enabled", "ui/extensibility", "ui/extensibility",
            "ui/helperText", "ui/imageID", "ui/imageMso", "ui/keyTip", "ui/label", "ui/lcid", "ui/loud", "ui/pressed", "ui/progID",
            "ui/ribbonID", "ui/showImage", "ui/showLabel", "ui/supertip", "ui/target", "ui/text", "ui/title", "ui/tooltip",
            "ui/userCustomization", "ui/visible", "userXmlData", "vbaProject", "video", "viewProps", "vmlDrawing",
            "volatileDependencies", "webSettings", "wordVbaData", "worksheet", "wsSortMap", "xlBinaryIndex",
            "xlExternalLinkPath/xlAlternateStartup", "xlExternalLinkPath/xlLibrary", "xlExternalLinkPath/xlPathMissing",
            "xlExternalLinkPath/xlStartup", "xlIntlMacrosheet", "xlMacrosheet", "xmlMaps"
    ).collect(Collectors.toSet());
}