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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.KeyUsageIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPRespIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxy;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.facets.EnvelopedSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.SignaturePolicyService;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESXLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.XmlSignatureService;
import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlObject;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3.x2000.x09.xmldsig.SignatureDocument;

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
        CryptoFunctions.registerBouncyCastle();
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
        // hash > sha1 doesn't work in excel viewer ...
        si.confirmSignature(keyPair.getPrivate(), x509, HashAlgorithm.sha1);
        List<X509Certificate> signer = si.getSigners();
        assertEquals(1, signer.size());
        pkg.close();
    }

    @Test
    public void testSignEnvelopingDocument() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);

        // setup
        EnvelopedSignatureFacet envelopedSignatureFacet = new EnvelopedSignatureFacet();
        KeyInfoSignatureFacet keyInfoSignatureFacet = new KeyInfoSignatureFacet(true, false, false);
        SignaturePolicyService signaturePolicyService = null;
        XAdESSignatureFacet xadesSignatureFacet = new XAdESSignatureFacet(null, null, signaturePolicyService);

        
        TimeStampService mockTimeStampService = mock(TimeStampService.class);
        RevocationDataService mockRevocationDataService = mock(RevocationDataService.class);

        XAdESXLSignatureFacet xadesXLSignatureFacet = new XAdESXLSignatureFacet(
                mockTimeStampService, mockRevocationDataService);
        XmlSignatureService testedInstance = new XmlSignatureService(HashAlgorithm.sha1, pkg);
        testedInstance.addSignatureFacet(envelopedSignatureFacet, keyInfoSignatureFacet,
                xadesSignatureFacet, xadesXLSignatureFacet);
        
        initKeyPair("Test", "CN=Test");
        List<X509Certificate> certificateChain = new ArrayList<X509Certificate>();
        /*
         * We need at least 2 certificates for the XAdES-C complete certificate
         * refs construction.
         */
        certificateChain.add(x509);
        certificateChain.add(x509);
        
        RevocationData revocationData = new RevocationData();
        final X509CRL crl = PkiTestUtils.generateCrl(x509, keyPair.getPrivate());
        revocationData.addCRL(crl);
        OCSPRespIf ocspResp = PkiTestUtils.createOcspResp(x509, false,
                x509, x509, keyPair.getPrivate(), "SHA1withRSA");
        revocationData.addOCSP(ocspResp.getEncoded());
        
        when(mockTimeStampService.timeStamp(any(byte[].class), any(RevocationData.class)))
        .thenAnswer(new Answer<byte[]>(){
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                RevocationData revocationData = (RevocationData) arguments[1];
                revocationData.addCRL(crl);
                return "time-stamp-token".getBytes();
            }            
        });
        
        when(mockRevocationDataService.getRevocationData(eq(certificateChain)))
        .thenReturn(revocationData);
        
        // operate
        DigestInfo digestInfo = testedInstance.preSign(null, certificateChain, null, null, null);

        // verify
        assertNotNull(digestInfo);
        assertEquals(HashAlgorithm.sha1, digestInfo.hashAlgo);
        assertNotNull(digestInfo.digestValue);
        
        SignatureDocument sigDoc = testedInstance.getSignatureDocument();
        String certDigestXQuery =
                "declare namespace xades='http://uri.etsi.org/01903/v1.3.2#'; "
              + "declare namespace ds='http://www.w3.org/2000/09/xmldsig#'; "
              + "$this/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:CertDigest";
        XmlObject xoList[] = sigDoc.selectPath(certDigestXQuery);
        assertEquals(xoList.length, 1);
        DigestAlgAndValueType certDigest = (DigestAlgAndValueType)xoList[0];
        assertNotNull(certDigest.getDigestValue());

        // Sign the received XML signature digest value.
        byte[] signatureValue = SignatureInfo.signDigest(keyPair.getPrivate(), HashAlgorithm.sha1, digestInfo.digestValue);

        // Operate: postSign
        testedInstance.postSign(signatureValue, certificateChain);
        
        // verify
        verify(mockTimeStampService, times(2)).timeStamp(any(byte[].class), any(RevocationData.class));
        verify(mockRevocationDataService).getRevocationData(certificateChain);
        
        DOMValidateContext domValidateContext = new DOMValidateContext(
                KeySelector.singletonKeySelector(keyPair.getPublic()),
                testedInstance.getSignatureDocument().getDomNode());
        XMLSignatureFactory xmlSignatureFactory = SignatureInfo.getSignatureFactory();
        XMLSignature xmlSignature = xmlSignatureFactory
                .unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);

        xoList = sigDoc.selectPath(certDigestXQuery);
        assertEquals(xoList.length, 1);
        certDigest = (DigestAlgAndValueType)xoList[0];
        assertNotNull(certDigest.getDigestValue());
        
        String qualPropXQuery =
                "declare namespace xades='http://uri.etsi.org/01903/v1.3.2#'; "
              + "declare namespace ds='http://www.w3.org/2000/09/xmldsig#'; "
              + "$this/ds:Signature/ds:Object/xades:QualifyingProperties";
        xoList = sigDoc.selectPath(qualPropXQuery);
        assertEquals(xoList.length, 1);
        QualifyingPropertiesType qualProp = (QualifyingPropertiesType)xoList[0];
        boolean qualPropXsdOk = qualProp.validate();
        assertTrue(qualPropXsdOk);
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
