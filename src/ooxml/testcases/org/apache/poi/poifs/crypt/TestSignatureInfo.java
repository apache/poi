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
package org.apache.poi.poifs.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Cipher;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.dsig.HorribleProxy;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.KeyUsageIf;
import org.apache.poi.poifs.crypt.dsig.services.XmlSignatureService;
import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSignatureInfo {
    private static final POILogger LOG = POILogFactory.getLogger(TestSignatureInfo.class);
    private static final POIDataSamples testdata = POIDataSamples.getXmlDSignInstance();

    private KeyPair keyPair = null;
    private X509Certificate x509 = null;
    

    
    @BeforeClass
    public static void initBouncy() throws MalformedURLException {
        File bcJar = testdata.getFile("bcprov-ext-jdk15on-1.49.jar");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URLClassLoader ucl = new URLClassLoader(new URL[]{bcJar.toURI().toURL()}, cl);
        Thread.currentThread().setContextClassLoader(ucl);
    }
    
    @Test
    public void getSignerUnsigned() throws Exception {
        String testFiles[] = { 
            "hello-world-unsigned.docx",
            "hello-world-unsigned.pptx",
            "hello-world-unsigned.xlsx",
            "hello-world-office-2010-technical-preview-unsigned.docx"
        };
        
        for (String testFile : testFiles) {
            OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ);
            SignatureInfo si = new SignatureInfo(pkg);
            List<X509Certificate> result = si.getSigners();
            pkg.revert();
            pkg.close();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
    
    @Test
    public void getSigner() throws Exception {
        String testFiles[] = { 
            "hyperlink-example-signed.docx",
            "hello-world-signed.docx",
            "hello-world-signed.pptx",
            "hello-world-signed.xlsx",
            "hello-world-office-2010-technical-preview.docx",
            "ms-office-2010-signed.docx",
            "ms-office-2010-signed.pptx",
            "ms-office-2010-signed.xlsx",
            "Office2010-SP1-XAdES-X-L.docx",
            "signed.docx",
        };
        
        for (String testFile : testFiles) {
            OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ);
            SignatureInfo si = new SignatureInfo(pkg);
            List<X509Certificate> result = si.getSigners();

            assertNotNull(result);
            assertEquals("test-file: "+testFile, 1, result.size());
            X509Certificate signer = result.get(0);
            LOG.log(POILogger.DEBUG, "signer: " + signer.getSubjectX500Principal());

            boolean b = si.verifySignature();
            assertTrue("test-file: "+testFile, b);
            pkg.revert();
        }
    }

    @Test
    public void getMultiSigners() throws Exception {
        String testFile = "hello-world-signed-twice.docx";
        OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ);
        SignatureInfo si = new SignatureInfo(pkg);
        List<X509Certificate> result = si.getSigners();

        assertNotNull(result);
        assertEquals("test-file: "+testFile, 2, result.size());
        X509Certificate signer1 = result.get(0);
        X509Certificate signer2 = result.get(1);
        LOG.log(POILogger.DEBUG, "signer 1: " + signer1.getSubjectX500Principal());
        LOG.log(POILogger.DEBUG, "signer 2: " + signer2.getSubjectX500Principal());

        boolean b = si.verifySignature();
        assertTrue("test-file: "+testFile, b);
        pkg.revert();
    }
    
    @Test
    public void testSignSpreadsheet() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
        sign(pkg, "Test", "CN=Test", 1);
        pkg.close();
    }

    @Test
    public void testSignSpreadsheetWithSignatureInfo() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
        SignatureInfo si = new SignatureInfo(pkg);
        initKeyPair("Test", "CN=Test");
        si.confirmSignature(keyPair.getPrivate(), x509, HashAlgorithm.sha1);
        List<X509Certificate> signer = si.getSigners();
        assertEquals(1, signer.size());
        pkg.close();
    }

    
    private OPCPackage sign(OPCPackage pkgCopy, String alias, String signerDn, int signerCount) throws Exception {
        /*** TODO : set cal to now ... only set to fixed date for debugging ... */ 
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(2014, 7, 6, 21, 42, 12);
        
        XmlSignatureService signatureService = new XmlSignatureService(HashAlgorithm.sha1, pkgCopy);
        signatureService.initFacets(cal.getTime());
        initKeyPair(alias, signerDn);

        // operate
        List<X509Certificate> x509Chain = Collections.singletonList(x509);
        DigestInfo digestInfo = signatureService.preSign(null, x509Chain, null, null, null);

        // verify
        assertNotNull(digestInfo);
        LOG.log(POILogger.DEBUG, "digest algo: " + digestInfo.hashAlgo);
        LOG.log(POILogger.DEBUG, "digest description: " + digestInfo.description);
        assertEquals("Office OpenXML Document", digestInfo.description);
        assertNotNull(digestInfo.hashAlgo);
        assertNotNull(digestInfo.digestValue);

        // setup: key material, signature value

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        ByteArrayOutputStream digestInfoValueBuf = new ByteArrayOutputStream();
        digestInfoValueBuf.write(SignatureInfo.SHA1_DIGEST_INFO_PREFIX);
        digestInfoValueBuf.write(digestInfo.digestValue);
        byte[] digestInfoValue = digestInfoValueBuf.toByteArray();
        byte[] signatureValue = cipher.doFinal(digestInfoValue);

        // operate: postSign
        signatureService.postSign(signatureValue, Collections.singletonList(x509));

        // verify: signature
        SignatureInfo si = new SignatureInfo(pkgCopy);
        List<X509Certificate> signers = si.getSigners();
        assertEquals(signerCount, signers.size());

        return pkgCopy;
    }

    private void initKeyPair(String alias, String subjectDN) throws Exception {
        final char password[] = "test".toCharArray();
        File file = new File("build/test.pfx");

        KeyStore keystore = KeyStore.getInstance("PKCS12");

        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            keystore.load(fis, password);
            fis.close();
        } else {
            keystore.load(null, password);
        }

        if (keystore.isKeyEntry(alias)) {
            Key key = keystore.getKey(alias, password);
            x509 = (X509Certificate)keystore.getCertificate(alias);
            keyPair = new KeyPair(x509.getPublicKey(), (PrivateKey)key);
        } else {
            keyPair = PkiTestUtils.generateKeyPair();
            Calendar cal = Calendar.getInstance();
            Date notBefore = cal.getTime();
            cal.add(Calendar.YEAR, 1);
            Date notAfter = cal.getTime();
            KeyUsageIf keyUsage = HorribleProxy.newProxy(KeyUsageIf.class);
            keyUsage = HorribleProxy.newProxy(KeyUsageIf.class, keyUsage.digitalSignature());
            
            x509 = PkiTestUtils.generateCertificate(keyPair.getPublic(), subjectDN
                , notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, keyUsage);

            keystore.setKeyEntry(alias, keyPair.getPrivate(), password, new Certificate[]{x509});
            FileOutputStream fos = new FileOutputStream(file);
            keystore.store(fos, password);
            fos.close();
        }
    }

    private static File copy(File input) throws IOException {
        String extension = input.getName().replaceAll(".*?(\\.[^.]+)?$", "$1");
        if (extension == null || "".equals(extension)) extension = ".zip";
        File tmpFile = new File("build", "sigtest"+extension);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        FileInputStream fis = new FileInputStream(input);
        IOUtils.copy(fis, fos);
        fis.close();
        fos.close();
        return tmpFile;
    }
}
