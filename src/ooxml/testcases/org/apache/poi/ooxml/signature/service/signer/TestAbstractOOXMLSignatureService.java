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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ooxml.signature.service.signer.ooxml.AbstractOOXMLSignatureService;
import org.apache.poi.ooxml.signature.service.signer.ooxml.OOXMLProvider;
import org.apache.poi.ooxml.signature.service.signer.ooxml.OOXMLSignatureVerifier;
import org.apache.poi.ooxml.signature.service.spi.DigestInfo;
import org.bouncycastle.asn1.x509.KeyUsage;



public class TestAbstractOOXMLSignatureService extends TestCase {

    private static final Log LOG = LogFactory.getLog(TestAbstractOOXMLSignatureService.class);

    static {
        OOXMLProvider.install();
    }

    private static class OOXMLTestSignatureService extends AbstractOOXMLSignatureService {

        private final URL _ooxmlUrl;

        private final TemporaryTestDataStorage _temporaryDataStorage;

        private final ByteArrayOutputStream _signedOOXMLOutputStream;

        public OOXMLTestSignatureService(URL ooxmlUrl) {
            _temporaryDataStorage = new TemporaryTestDataStorage();
            _signedOOXMLOutputStream = new ByteArrayOutputStream();
            _ooxmlUrl = ooxmlUrl;
        }

        @Override
        protected URL getOfficeOpenXMLDocumentURL() {
            return _ooxmlUrl;
        }

        @Override
        protected OutputStream getSignedOfficeOpenXMLDocumentOutputStream() {
            return _signedOOXMLOutputStream;
        }

        public byte[] getSignedOfficeOpenXMLDocumentData() {
            return _signedOOXMLOutputStream.toByteArray();
        }

        @Override
        protected TemporaryDataStorage getTemporaryDataStorage() {
            return _temporaryDataStorage;
        }
    }

    public void testPreSign() throws Exception {
        // setup
        URL ooxmlUrl = TestAbstractOOXMLSignatureService.class.getResource("/hello-world-unsigned.docx");
        assertNotNull(ooxmlUrl);

        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);

        // operate
        DigestInfo digestInfo = signatureService.preSign(null, null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        LOG.debug("digest description: " + digestInfo.description);
        assertEquals("Office OpenXML Document", digestInfo.description);
        assertNotNull(digestInfo.digestAlgo);
        assertNotNull(digestInfo.digestValue);

        TemporaryDataStorage temporaryDataStorage = signatureService.getTemporaryDataStorage();
        String preSignResult = IOUtils.toString(temporaryDataStorage.getTempInputStream());
        LOG.debug("pre-sign result: " + preSignResult);
        File tmpFile = File.createTempFile("ooxml-pre-sign-", ".xml");
        FileUtils.writeStringToFile(tmpFile, preSignResult);
        LOG.debug("tmp pre-sign file: " + tmpFile.getAbsolutePath());
    }

    public void testPostSign() throws Exception {
        sign("/hello-world-unsigned.docx");
    }

    public void testSignOffice2010() throws Exception {
        sign("/hello-world-office-2010-technical-preview-unsigned.docx");
    }

    public void testSignTwice() throws Exception {
        sign("/hello-world-signed.docx", 2);
    }

    public void testSignTwiceHere() throws Exception {
        File tmpFile = sign("/hello-world-unsigned.docx", 1);
        sign(tmpFile.toURI().toURL(), "CN=Test2", 2);
    }

    public void testSignPowerpoint() throws Exception {
        sign("/hello-world-unsigned.pptx");
    }

    public void testSignSpreadsheet() throws Exception {
        sign("/hello-world-unsigned.xlsx");
    }

    private void sign(String documentResourceName) throws Exception {
        sign(documentResourceName, 1);
    }

    private File sign(String documentResourceName, int signerCount) throws Exception {
        URL ooxmlUrl = TestAbstractOOXMLSignatureService.class.getResource(documentResourceName);
        return sign(ooxmlUrl, signerCount);
    }

    private File sign(URL ooxmlUrl, int signerCount) throws Exception {
        return sign(ooxmlUrl, "CN=Test", signerCount);
    }

    private File sign(URL ooxmlUrl, String signerDn, int signerCount) throws Exception {
        // setup
        assertNotNull(ooxmlUrl);

        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);

        // operate
        DigestInfo digestInfo = signatureService.preSign(null, null);

        // verify
        assertNotNull(digestInfo);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        LOG.debug("digest description: " + digestInfo.description);
        assertEquals("Office OpenXML Document", digestInfo.description);
        assertNotNull(digestInfo.digestAlgo);
        assertNotNull(digestInfo.digestValue);

        TemporaryDataStorage temporaryDataStorage = signatureService.getTemporaryDataStorage();
        String preSignResult = IOUtils.toString(temporaryDataStorage.getTempInputStream());
        LOG.debug("pre-sign result: " + preSignResult);
        File tmpFile = File.createTempFile("ooxml-pre-sign-", ".xml");
        FileUtils.writeStringToFile(tmpFile, preSignResult);
        LOG.debug("tmp pre-sign file: " + tmpFile.getAbsolutePath());

        // setup: key material, signature value
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), signerDn, null, keyPair.getPrivate(), true, 0,
                                        null, null, new KeyUsage(KeyUsage.nonRepudiation));

        // operate: postSign
        signatureService.postSign(signatureValue, Collections.singletonList(certificate));

        // verify: signature
        byte[] signedOOXMLData = signatureService.getSignedOfficeOpenXMLDocumentData();
        assertNotNull(signedOOXMLData);
        LOG.debug("signed OOXML size: " + signedOOXMLData.length);
        String extension = FilenameUtils.getExtension(ooxmlUrl.getFile());
        tmpFile = File.createTempFile("ooxml-signed-", "." + extension);
        FileUtils.writeByteArrayToFile(tmpFile, signedOOXMLData);
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        List<X509Certificate> signers = OOXMLSignatureVerifier.getSigners(tmpFile.toURI().toURL());
        assertEquals(signerCount, signers.size());
        // assertEquals(certificate, signers.get(0));
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        return tmpFile;
    }
}
