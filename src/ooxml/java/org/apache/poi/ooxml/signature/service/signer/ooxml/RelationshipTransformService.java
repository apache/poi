
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


/*
 * Based on the eID Applet Project code.
 * Original Copyright (C) 2008-2009 FedICT.
 */

package org.apache.poi.ooxml.signature.service.signer.ooxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

    private static final Log LOG = LogFactory.getLog(RelationshipTransformService.class);

    public RelationshipTransformService() {
        super();
        LOG.debug("constructor");
        this.sourceIds = new LinkedList<String>();
    }

    @Override
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        LOG.debug("init(params)");
        if (false == params instanceof RelationshipTransformParameterSpec) {
            throw new InvalidAlgorithmParameterException();
        }
        RelationshipTransformParameterSpec relParams = (RelationshipTransformParameterSpec) params;
        for (String sourceId : relParams.getSourceIds()) {
            this.sourceIds.add(sourceId);
        }
    }

    @Override
    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        LOG.debug("init(parent,context)");
        LOG.debug("parent java type: " + parent.getClass().getName());
        DOMStructure domParent = (DOMStructure) parent;
        Node parentNode = domParent.getNode();
        try {
            LOG.debug("parent: " + toString(parentNode));
        } catch (TransformerException e) {
            throw new InvalidAlgorithmParameterException();
        }
        Element nsElement = parentNode.getOwnerDocument().createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:mdssi", "http://schemas.openxmlformats.org/package/2006/digital-signature");
        NodeList nodeList;
        try {
            nodeList = XPathAPI.selectNodeList(parentNode, "mdssi:RelationshipReference/@SourceId", nsElement);
        } catch (TransformerException e) {
            LOG.error("transformer exception: " + e.getMessage(), e);
            throw new InvalidAlgorithmParameterException();
        }
        if (0 == nodeList.getLength()) {
            LOG.warn("no RelationshipReference/@SourceId parameters present");
        }
        for (int nodeIdx = 0; nodeIdx < nodeList.getLength(); nodeIdx++) {
            Node node = nodeList.item(nodeIdx);
            String sourceId = node.getTextContent();
            LOG.debug("sourceId: " + sourceId);
            this.sourceIds.add(sourceId);
        }
    }

    @Override
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        LOG.debug("marshallParams(parent,context)");
        DOMStructure domParent = (DOMStructure) parent;
        Node parentNode = domParent.getNode();
        Element parentElement = (Element) parentNode;
        parentElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:mdssi", "http://schemas.openxmlformats.org/package/2006/digital-signature");
        Document document = parentNode.getOwnerDocument();
        for (String sourceId : this.sourceIds) {
            Element relationshipReferenceElement = document.createElementNS("http://schemas.openxmlformats.org/package/2006/digital-signature",
                                            "mdssi:RelationshipReference");
            relationshipReferenceElement.setAttribute("SourceId", sourceId);
            parentElement.appendChild(relationshipReferenceElement);
        }
    }

    public AlgorithmParameterSpec getParameterSpec() {
        LOG.debug("getParameterSpec");
        return null;
    }

    public Data transform(Data data, XMLCryptoContext context) throws TransformException {
        LOG.debug("transform(data,context)");
        LOG.debug("data java type: " + data.getClass().getName());
        OctetStreamData octetStreamData = (OctetStreamData) data;
        LOG.debug("URI: " + octetStreamData.getURI());
        InputStream octetStream = octetStreamData.getOctetStream();
        Document relationshipsDocument;
        try {
            relationshipsDocument = loadDocument(octetStream);
        } catch (Exception e) {
            throw new TransformException(e.getMessage(), e);
        }
        try {
            LOG.debug("relationships document: " + toString(relationshipsDocument));
        } catch (TransformerException e) {
            throw new TransformException(e.getMessage(), e);
        }
        Element nsElement = relationshipsDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "http://schemas.openxmlformats.org/package/2006/relationships");
        Element relationshipsElement = relationshipsDocument.getDocumentElement();
        NodeList childNodes = relationshipsElement.getChildNodes();
        for (int nodeIdx = 0; nodeIdx < childNodes.getLength(); nodeIdx++) {
            Node childNode = childNodes.item(nodeIdx);
            if (Node.ELEMENT_NODE != childNode.getNodeType()) {
                LOG.debug("removing node");
                relationshipsElement.removeChild(childNode);
                nodeIdx--;
                continue;
            }
            Element childElement = (Element) childNode;
            String idAttribute = childElement.getAttribute("Id");
            LOG.debug("Relationship id attribute: " + idAttribute);
            if (false == this.sourceIds.contains(idAttribute)) {
                LOG.debug("removing element: " + idAttribute);
                relationshipsElement.removeChild(childNode);
                nodeIdx--;
            }
            /*
             * See: ISO/IEC 29500-2:2008(E) - 13.2.4.24 Relationships Transform
             * Algorithm.
             */
            if (null == childElement.getAttributeNode("TargetMode")) {
                childElement.setAttribute("TargetMode", "Internal");
            }
        }
        LOG.debug("# Relationship elements: " + relationshipsElement.getElementsByTagName("*").getLength());
        sortRelationshipElements(relationshipsElement);
        try {
            return toOctetStreamData(relationshipsDocument);
        } catch (TransformerException e) {
            throw new TransformException(e.getMessage(), e);
        }
    }

    private void sortRelationshipElements(Element relationshipsElement) {
        List<Element> relationshipElements = new LinkedList<Element>();
        NodeList relationshipNodes = relationshipsElement.getElementsByTagName("*");
        int nodeCount = relationshipNodes.getLength();
        for (int nodeIdx = 0; nodeIdx < nodeCount; nodeIdx++) {
            Node relationshipNode = relationshipNodes.item(0);
            Element relationshipElement = (Element) relationshipNode;
            LOG.debug("unsorted Id: " + relationshipElement.getAttribute("Id"));
            relationshipElements.add(relationshipElement);
            relationshipsElement.removeChild(relationshipNode);
        }
        Collections.sort(relationshipElements, new RelationshipComparator());
        for (Element relationshipElement : relationshipElements) {
            LOG.debug("sorted Id: " + relationshipElement.getAttribute("Id"));
            relationshipsElement.appendChild(relationshipElement);
        }
    }

    private String toString(Node dom) throws TransformerException {
        Source source = new DOMSource(dom);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        /*
         * We have to omit the ?xml declaration if we want to embed the
         * document.
         */
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();
    }

    private OctetStreamData toOctetStreamData(Node node) throws TransformerException {
        Source source = new DOMSource(node);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result result = new StreamResult(outputStream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        LOG.debug("result: " + new String(outputStream.toByteArray()));
        return new OctetStreamData(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    private Document loadDocument(InputStream documentInputStream) throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(documentInputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);
        return document;
    }

    public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
        LOG.debug("transform(data,context,os)");
        return null;
    }

    public boolean isFeatureSupported(String feature) {
        LOG.debug("isFeatureSupported(feature)");
        return false;
    }
}
