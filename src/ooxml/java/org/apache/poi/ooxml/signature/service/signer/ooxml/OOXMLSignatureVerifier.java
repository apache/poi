
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.ooxml.signature.service.signer.KeyInfoKeySelector;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 * Signature verifier util class for Office Open XML file format.
 */
public class OOXMLSignatureVerifier {

    private static final Log LOG = LogFactory.getLog(OOXMLSignatureVerifier.class);

    private OOXMLSignatureVerifier() {
        super();
    }

    /**
     * Checks whether the file referred by the given URL is an OOXML document.
     * 
     * @param url
     * @return
     * @throws IOException
     */
    public static boolean isOOXML(URL url) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(url.openStream());
        ZipEntry zipEntry;
        while (null != (zipEntry = zipInputStream.getNextEntry())) {
            if (false == "[Content_Types].xml".equals(zipEntry.getName())) {
                continue;
            }
            if (zipEntry.getSize() > 0) {
                return true;
            }
        }
        return false;
    }

    public static List<X509Certificate> getSigners(URL url) throws IOException, ParserConfigurationException, SAXException, TransformerException,
                                    MarshalException, XMLSignatureException, InvalidFormatException {
        List<X509Certificate> signers = new LinkedList<X509Certificate>();
        List<PackagePart> signatureParts = getSignatureParts(url);
        if (signatureParts.isEmpty()) {
            LOG.debug("no signature resources");
        }
        for (PackagePart signaturePart : signatureParts) {
            Document signatureDocument = loadDocument(signaturePart);
            if (null == signatureDocument) {
                continue;
            }

            NodeList signatureNodeList = signatureDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (0 == signatureNodeList.getLength()) {
                return null;
            }
            Node signatureNode = signatureNodeList.item(0);

            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
            DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, signatureNode);
            domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);
            OOXMLURIDereferencer dereferencer = new OOXMLURIDereferencer(url);
            domValidateContext.setURIDereferencer(dereferencer);

            XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
            XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
            boolean validity = xmlSignature.validate(domValidateContext);

            if (false == validity) {
                continue;
            }
            // TODO: check what has been signed.

            X509Certificate signer = keySelector.getCertificate();
            signers.add(signer);
        }
        return signers;
    }

    public static boolean verifySignature(URL url) throws InvalidFormatException, IOException, ParserConfigurationException, SAXException, MarshalException,
                                    XMLSignatureException {
        PackagePart signaturePart = getSignaturePart(url);
        if (signaturePart == null) {
            LOG.info(url + " does not contain a signature");
            return false;
        }
        LOG.debug("signature resource name: " + signaturePart.getPartName());

        OOXMLProvider.install();

        Document signatureDocument = loadDocument(signaturePart);
        LOG.debug("signature loaded");
        NodeList signatureNodeList = signatureDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        Node signatureNode = signatureNodeList.item(0);
        KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
        DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, signatureNode);
        domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);

        OOXMLURIDereferencer dereferencer = new OOXMLURIDereferencer(url);
        domValidateContext.setURIDereferencer(dereferencer);

        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        return xmlSignature.validate(domValidateContext);
    }

    private static PackagePart getSignaturePart(URL url) throws IOException, InvalidFormatException {
        List<PackagePart> packageParts = getSignatureParts(url);
        if (packageParts.isEmpty()) {
            return null;
        } else {
            return packageParts.get(0);
        }
    }

    private static List<PackagePart> getSignatureParts(URL url) throws IOException, InvalidFormatException {
        List<PackagePart> packageParts = new LinkedList<PackagePart>();
        OPCPackage pkg = POIXMLDocument.openPackage(url.getPath());
        PackageRelationshipCollection sigOrigRels = pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        for (PackageRelationship rel : sigOrigRels) {
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart sigPart = pkg.getPart(relName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Digital Signature Origin part = " + sigPart);
            }

            PackageRelationshipCollection sigRels = sigPart.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE);
            for (PackageRelationship sigRel : sigRels) {
                PackagePartName sigRelName = PackagingURIHelper.createPartName(sigRel.getTargetURI());
                PackagePart sigRelPart = pkg.getPart(sigRelName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("XML Signature part = " + sigRelPart);
                }
                packageParts.add(sigRelPart);
            }
        }
        return packageParts;
    }

    private static Document loadDocument(PackagePart part) throws ParserConfigurationException, SAXException, IOException {
        InputStream documentInputStream = part.getInputStream();
        return loadDocument(documentInputStream);
    }

    private static Document loadDocument(InputStream documentInputStream) throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(documentInputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);
        return document;
    }
}
