
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

package org.apache.poi.ooxml.signature.service.signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ooxml.signature.service.spi.DigestInfo;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.jcp.xml.dsig.internal.dom.DOMReference;
import org.jcp.xml.dsig.internal.dom.DOMXMLSignature;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class TestAbstractXmlSignatureService extends TestCase {

    private static final Log LOG = LogFactory.getLog(TestAbstractXmlSignatureService.class);

    private static class XmlSignatureTestService extends AbstractXmlSignatureService {

        private Document envelopingDocument;

        private List<String> referenceUris;

        private TemporaryTestDataStorage temporaryDataStorage;

        private String signatureDescription;

        private ByteArrayOutputStream signedDocumentOutputStream;

        private URIDereferencer uriDereferencer;

        public XmlSignatureTestService() {
            super();
            this.referenceUris = new LinkedList<String>();
            this.temporaryDataStorage = new TemporaryTestDataStorage();
            this.signedDocumentOutputStream = new ByteArrayOutputStream();
        }

        public byte[] getSignedDocumentData() {
            return this.signedDocumentOutputStream.toByteArray();
        }

        public void setEnvelopingDocument(Document envelopingDocument) {
            this.envelopingDocument = envelopingDocument;
        }

        @Override
        protected Document getEnvelopingDocument() {
            return this.envelopingDocument;
        }

        @Override
        protected String getSignatureDescription() {
            return this.signatureDescription;
        }

        public void setSignatureDescription(String signatureDescription) {
            this.signatureDescription = signatureDescription;
        }

        @Override
        protected List<String> getReferenceUris() {
            return this.referenceUris;
        }

        public void addReferenceUri(String referenceUri) {
            this.referenceUris.add(referenceUri);
        }

        @Override
        protected OutputStream getSignedDocumentOutputStream() {
            return this.signedDocumentOutputStream;
        }

        @Override
        protected TemporaryDataStorage getTemporaryDataStorage() {
            return this.temporaryDataStorage;
        }

        public String getFilesDigestAlgorithm() {
            return null;
        }

        @Override
        protected URIDereferencer getURIDereferencer() {
            return this.uriDereferencer;
        }

        public void setUriDereferencer(URIDereferencer uriDereferencer) {
            this.uriDereferencer = uriDereferencer;
        }
    }

    public void testSignEnvelopingDocument() throws Exception {
        // setup
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "tns:root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "urn:test");
        document.appendChild(rootElement);
        Element dataElement = document.createElementNS("urn:test", "tns:data");
        dataElement.setAttributeNS(null, "Id", "id-1234");
        dataElement.setTextContent("data to be signed");
        rootElement.appendChild(dataElement);

        XmlSignatureTestService testedInstance = new XmlSignatureTestService();
        testedInstance.setEnvelopingDocument(document);
        testedInstance.addReferenceUri("#id-1234");
        testedInstance.setSignatureDescription("test-signature-description");

        // operate
        DigestInfo digestInfo = testedInstance.preSign(null, null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest info description: " + digestInfo.description);
        assertEquals("test-signature-description", digestInfo.description);
        assertNotNull(digestInfo.digestValue);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        assertEquals("SHA-1", digestInfo.digestAlgo);

        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);

        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());

        /*
         * Sign the received XML signature digest value.
         */
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true,
                                        0, null, null, new KeyUsage(KeyUsage.nonRepudiation));

        /*
         * Operate: postSign
         */
        testedInstance.postSign(signatureValue, Collections.singletonList(certificate));

        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));

        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);

        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
    }

    public static class UriTestDereferencer implements URIDereferencer {

        private final Map<String, byte[]> resources;

        public UriTestDereferencer() {
            this.resources = new HashMap<String, byte[]>();
        }

        public void addResource(String uri, byte[] data) {
            this.resources.put(uri, data);
        }

        public Data dereference(URIReference uriReference, XMLCryptoContext xmlCryptoContext) throws URIReferenceException {
            String uri = uriReference.getURI();
            byte[] data = this.resources.get(uri);
            if (null == data) {
                return null;
            }
            return new OctetStreamData(new ByteArrayInputStream(data));
        }
    }

    public void testSignExternalUri() throws Exception {
        // setup
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        XmlSignatureTestService testedInstance = new XmlSignatureTestService();
        testedInstance.setEnvelopingDocument(document);
        testedInstance.addReferenceUri("external-uri");
        testedInstance.setSignatureDescription("test-signature-description");
        UriTestDereferencer uriDereferencer = new UriTestDereferencer();
        uriDereferencer.addResource("external-uri", "hello world".getBytes());
        testedInstance.setUriDereferencer(uriDereferencer);

        // operate
        DigestInfo digestInfo = testedInstance.preSign(null, null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest info description: " + digestInfo.description);
        assertEquals("test-signature-description", digestInfo.description);
        assertNotNull(digestInfo.digestValue);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        assertEquals("SHA-1", digestInfo.digestAlgo);

        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);

        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());

        /*
         * Sign the received XML signature digest value.
         */
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true,
                                        0, null, null, new KeyUsage(KeyUsage.nonRepudiation));

        /*
         * Operate: postSign
         */
        testedInstance.postSign(signatureValue, Collections.singletonList(certificate));

        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));

        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);

        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        domValidateContext.setURIDereferencer(uriDereferencer);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
    }

    public void testSignEnvelopingDocumentWithExternalDigestInfo() throws Exception {
        // setup
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "tns:root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "urn:test");
        document.appendChild(rootElement);

        XmlSignatureTestService testedInstance = new XmlSignatureTestService();
        testedInstance.setEnvelopingDocument(document);
        testedInstance.setSignatureDescription("test-signature-description");

        byte[] refData = "hello world".getBytes();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(refData);
        byte[] digestValue = messageDigest.digest();
        DigestInfo refDigestInfo = new DigestInfo(digestValue, "SHA-1", "urn:test:ref");

        // operate
        DigestInfo digestInfo = testedInstance.preSign(Collections.singletonList(refDigestInfo), null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest info description: " + digestInfo.description);
        assertEquals("test-signature-description", digestInfo.description);
        assertNotNull(digestInfo.digestValue);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        assertEquals("SHA-1", digestInfo.digestAlgo);

        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);

        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());

        /*
         * Sign the received XML signature digest value.
         */
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true,
                                        0, null, null, new KeyUsage(KeyUsage.nonRepudiation));

        /*
         * Operate: postSign
         */
        testedInstance.postSign(signatureValue, Collections.singletonList(certificate));

        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));

        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);

        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        URIDereferencer dereferencer = new URITest2Dereferencer();
        domValidateContext.setURIDereferencer(dereferencer);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
    }

    public void testSignExternalDigestInfo() throws Exception {
        // setup
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        XmlSignatureTestService testedInstance = new XmlSignatureTestService();
        testedInstance.setEnvelopingDocument(document);
        testedInstance.setSignatureDescription("test-signature-description");

        byte[] refData = "hello world".getBytes();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(refData);
        byte[] digestValue = messageDigest.digest();
        DigestInfo refDigestInfo = new DigestInfo(digestValue, "SHA-1", "urn:test:ref");

        // operate
        DigestInfo digestInfo = testedInstance.preSign(Collections.singletonList(refDigestInfo), null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest info description: " + digestInfo.description);
        assertEquals("test-signature-description", digestInfo.description);
        assertNotNull(digestInfo.digestValue);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        assertEquals("SHA-1", digestInfo.digestAlgo);

        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);

        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());

        /*
         * Sign the received XML signature digest value.
         */
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true,
                                        0, null, null, new KeyUsage(KeyUsage.nonRepudiation));

        /*
         * Operate: postSign
         */
        testedInstance.postSign(signatureValue, Collections.singletonList(certificate));

        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));

        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);

        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        URIDereferencer dereferencer = new URITest2Dereferencer();
        domValidateContext.setURIDereferencer(dereferencer);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
    }

    private static class URITest2Dereferencer implements URIDereferencer {

        private static final Log LOG = LogFactory.getLog(URITest2Dereferencer.class);

        public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
            LOG.debug("dereference: " + uriReference.getURI());
            return new OctetStreamData(new ByteArrayInputStream("hello world".getBytes()));
        }
    }

    public void testJsr105Signature() throws Exception {
        KeyPair keyPair = PkiTestUtils.generateKeyPair();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "tns:root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "urn:test");
        document.appendChild(rootElement);
        Element dataElement = document.createElementNS("urn:test", "tns:data");
        dataElement.setAttributeNS(null, "Id", "id-1234");
        dataElement.setTextContent("data to be signed");
        rootElement.appendChild(dataElement);

        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());

        XMLSignContext signContext = new DOMSignContext(keyPair.getPrivate(), document.getDocumentElement());
        signContext.putNamespacePrefix(javax.xml.crypto.dsig.XMLSignature.XMLNS, "ds");

        DigestMethod digestMethod = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
        Reference reference = signatureFactory.newReference("#id-1234", digestMethod);
        DOMReference domReference = (DOMReference) reference;
        assertNull(domReference.getCalculatedDigestValue());
        assertNull(domReference.getDigestValue());

        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        CanonicalizationMethod canonicalizationMethod = signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
                                        (C14NMethodParameterSpec) null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, Collections.singletonList(reference));

        javax.xml.crypto.dsig.XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, null);

        DOMXMLSignature domXmlSignature = (DOMXMLSignature) xmlSignature;
        domXmlSignature.marshal(document.getDocumentElement(), "ds", (DOMCryptoContext) signContext);
        domReference.digest(signContext);
        // xmlSignature.sign(signContext);
        // LOG.debug("signed document: " + toString(document));

        Element nsElement = document.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(document, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());
    }
}
