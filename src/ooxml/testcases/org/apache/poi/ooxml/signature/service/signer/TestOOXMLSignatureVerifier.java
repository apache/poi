
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

import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.ooxml.signature.service.signer.ooxml.OOXMLProvider;
import org.apache.poi.ooxml.signature.service.signer.ooxml.OOXMLSignatureVerifier;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.signature.PackageDigitalSignatureManager;



public class TestOOXMLSignatureVerifier extends TestCase {

    private static final Log LOG = LogFactory.getLog(TestOOXMLSignatureVerifier.class);

    static {
        OOXMLProvider.install();
    }

    public void testIsOOXMLDocument() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-unsigned.docx");

        // operate
        boolean result = OOXMLSignatureVerifier.isOOXML(url);

        // verify
        assertTrue(result);
    }

    public void testPOI() throws Exception {
        // setup
        InputStream inputStream = TestOOXMLSignatureVerifier.class.getResourceAsStream("/hello-world-unsigned.docx");

        // operate
        boolean result = POIXMLDocument.hasOOXMLHeader(inputStream);

        // verify
        assertTrue(result);
    }

    public void testOPC() throws Exception {
        // setup
        InputStream inputStream = TestOOXMLSignatureVerifier.class.getResourceAsStream("/hello-world-signed.docx");

        // operate
        OPCPackage opcPackage = OPCPackage.open(inputStream);

        ArrayList<PackagePart> parts = opcPackage.getParts();
        for (PackagePart part : parts) {
            LOG.debug("part name: " + part.getPartName().getName());
            LOG.debug("part content type: " + part.getContentType());
        }

        ArrayList<PackagePart> signatureParts = opcPackage.getPartsByContentType("application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml");
        assertFalse(signatureParts.isEmpty());

        PackagePart signaturePart = signatureParts.get(0);
        LOG.debug("signature part class type: " + signaturePart.getClass().getName());

        PackageDigitalSignatureManager packageDigitalSignatureManager = new PackageDigitalSignatureManager();
        // yeah... POI implementation still missing
    }

    public void testGetSignerUnsigned() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-unsigned.docx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetSignerOffice2010Unsigned() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-office-2010-technical-preview-unsigned.docx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetSignerUnsignedPowerpoint() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-unsigned.pptx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetSignerUnsignedExcel() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-unsigned.xlsx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetSigner() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-signed.docx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertEquals(1, result.size());
        X509Certificate signer = result.get(0);
        LOG.debug("signer: " + signer.getSubjectX500Principal());
    }

    public void testOffice2010TechnicalPreview() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-office-2010-technical-preview.docx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertEquals(1, result.size());
        X509Certificate signer = result.get(0);
        LOG.debug("signer: " + signer.getSubjectX500Principal());
    }

    public void testGetSignerPowerpoint() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-signed.pptx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertEquals(1, result.size());
        X509Certificate signer = result.get(0);
        LOG.debug("signer: " + signer.getSubjectX500Principal());
    }

    public void testGetSignerExcel() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-signed.xlsx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertEquals(1, result.size());
        X509Certificate signer = result.get(0);
        LOG.debug("signer: " + signer.getSubjectX500Principal());
    }

    public void testGetSigners() throws Exception {
        // setup
        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-signed-twice.docx");

        // operate
        List<X509Certificate> result = OOXMLSignatureVerifier.getSigners(url);

        // verify
        assertNotNull(result);
        assertEquals(2, result.size());
        X509Certificate signer1 = result.get(0);
        X509Certificate signer2 = result.get(1);
        LOG.debug("signer 1: " + signer1.getSubjectX500Principal());
        LOG.debug("signer 2: " + signer2.getSubjectX500Principal());
    }

    public void testVerifySignature() throws Exception {

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("org.jcp.xml.dsig.internal.dom");
        logger.log(Level.FINE, "test");

        URL url = TestOOXMLSignatureVerifier.class.getResource("/hello-world-signed.docx");
        boolean validity = OOXMLSignatureVerifier.verifySignature(url);
        assertTrue(validity);
    }

    public void testTamperedFile() throws Exception {

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("org.jcp.xml.dsig.internal.dom");
        logger.log(Level.FINE, "test");

        URL url = TestOOXMLSignatureVerifier.class.getResource("/invalidsig.docx");
        boolean validity = OOXMLSignatureVerifier.verifySignature(url);
        assertFalse(validity);
    }
}
