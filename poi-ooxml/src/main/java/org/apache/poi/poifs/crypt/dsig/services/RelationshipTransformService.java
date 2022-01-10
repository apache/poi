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

package org.apache.poi.poifs.crypt.dsig.services;

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_NS;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.jcp.xml.dsig.internal.dom.ApacheNodeSetData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.util.SuppressForbidden;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTRelationshipReference;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.RelationshipReferenceDocument;
import org.w3.x2000.x09.xmldsig.TransformDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * JSR105 implementation of the RelationshipTransform transformation.
 *
 * <p>
 * Specs: http://openiso.org/Ecma/376/Part2/12.2.4#26
 * </p>
 */
public class RelationshipTransformService extends TransformService {

    public static final String TRANSFORM_URI = "http://schemas.openxmlformats.org/package/2006/RelationshipTransform";

    private final List<String> sourceIds;

    private static final Logger LOG = LogManager.getLogger(RelationshipTransformService.class);

    /**
     * Relationship Transform parameter specification class.
     */
    public static class RelationshipTransformParameterSpec implements TransformParameterSpec {
        List<String> sourceIds = new ArrayList<>();
        public void addRelationshipReference(String relationshipId) {
                sourceIds.add(relationshipId);
        }
        public boolean hasSourceIds() {
            return !sourceIds.isEmpty();
        }
    }

    @SuppressForbidden("new Provider(String,String,String) is not available in Java 8")
    private static final class POIXmlDsigProvider extends Provider {
        static final long serialVersionUID = 1L;
        private static final String NAME = "POIXmlDsigProvider";

        private POIXmlDsigProvider() {
            super(NAME, 1d, NAME);
            put("TransformService." + TRANSFORM_URI, RelationshipTransformService.class.getName());
            put("TransformService." + TRANSFORM_URI + " MechanismType", "DOM");
        }
    }


    public RelationshipTransformService() {
        super();
        LOG.atDebug().log("constructor");
        this.sourceIds = new ArrayList<>();
    }

    /**
     * Register the provider for this TransformService
     *
     * @see TransformService
     */
    public static synchronized void registerDsigProvider() {
        // the xml signature classes will try to find a special TransformerService,
        // which is of course unknown to JCE before ...
        if (Security.getProvider(POIXmlDsigProvider.NAME) == null) {
            Security.addProvider(new POIXmlDsigProvider());
        }
    }


    @Override
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        LOG.atDebug().log("init(params)");
        if (!(params instanceof RelationshipTransformParameterSpec)) {
            throw new InvalidAlgorithmParameterException();
        }
        RelationshipTransformParameterSpec relParams = (RelationshipTransformParameterSpec) params;
        this.sourceIds.addAll(relParams.sourceIds);
    }

    @Override
    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        LOG.atDebug().log("init(parent,context)");
        LOG.atDebug().log("parent java type: {}", parent.getClass().getName());
        DOMStructure domParent = (DOMStructure) parent;
        Node parentNode = domParent.getNode();

        try {
            TransformDocument transDoc = TransformDocument.Factory.parse(parentNode, DEFAULT_XML_OPTIONS);
            XmlObject[] xoList = transDoc.getTransform().selectChildren(RelationshipReferenceDocument.type.getDocumentElementName());
            if (xoList.length == 0) {
                LOG.atWarn().log("no RelationshipReference/@SourceId parameters present");
            }
            for (XmlObject xo : xoList) {
                String sourceId = ((CTRelationshipReference)xo).getSourceId();
                LOG.atDebug().log("sourceId: {}", sourceId);
                this.sourceIds.add(sourceId);
            }
        } catch (XmlException e) {
            throw new InvalidAlgorithmParameterException(e);
        }
    }

    @Override
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        LOG.atDebug().log("marshallParams(parent,context)");
        DOMStructure domParent = (DOMStructure) parent;
        Element parentNode = (Element)domParent.getNode();
        Document doc = parentNode.getOwnerDocument();

        for (String sourceId : this.sourceIds) {
            Element el = doc.createElementNS(OO_DIGSIG_NS, "mdssi:RelationshipReference");
            el.setAttributeNS(XML_NS, "xmlns:mdssi", OO_DIGSIG_NS);
            el.setAttribute("SourceId", sourceId);
            parentNode.appendChild(el);
        }
    }

    public AlgorithmParameterSpec getParameterSpec() {
        LOG.atDebug().log("getParameterSpec");
        return null;
    }

    /**
     * The relationships transform takes the XML document from the Relationships part
     * and converts it to another XML document.
     *
     * @see <a href="https://www.ecma-international.org/activities/Office%20Open%20XML%20Formats/Draft%20ECMA-376%203rd%20edition,%20March%202011/Office%20Open%20XML%20Part%202%20-%20Open%20Packaging%20Conventions.pdf">13.2.4.24 Relationships Transform Algorithm</a>
     */
    public Data transform(Data data, XMLCryptoContext context) throws TransformException {
        LOG.atDebug().log("transform(data,context)");
        LOG.atDebug().log("data java type: {}", data.getClass().getName());
        OctetStreamData octetStreamData = (OctetStreamData) data;
        LOG.atDebug().log("URI: {}", octetStreamData.getURI());
        InputStream octetStream = octetStreamData.getOctetStream();

        Document doc;
        try {
            doc = DocumentHelper.readDocument(octetStream);
        } catch (Exception e) {
            throw new TransformException(e.getMessage(), e);
        }

        // keep only those relationships which id is registered in the sourceIds
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        TreeMap<String,Element> rsList = new TreeMap<>();
        for (int i=nl.getLength()-1; i>=0; i--) {
            Node n = nl.item(i);
            if ("Relationship".equals(n.getLocalName())) {
                Element el = (Element)n;
                String id = el.getAttribute("Id");
                if (sourceIds.contains(id)) {
                    String targetMode = el.getAttribute("TargetMode");
                    if (targetMode == null || targetMode.isEmpty()) {
                        el.setAttribute("TargetMode", "Internal");
                    }
                    rsList.put(id, el);
                }
            }
            root.removeChild(n);
        }

        for (Element el : rsList.values()) {
            root.appendChild(el);
        }

        LOG.atDebug().log("# Relationship elements: {}", box(rsList.size()));

        return new ApacheNodeSetData(new XMLSignatureInput(root));
    }

    public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
        LOG.atDebug().log("transform(data,context,os)");
        return null;
    }

    public boolean isFeatureSupported(String feature) {
        LOG.atDebug().log("isFeatureSupported(feature)");
        return false;
    }
}
