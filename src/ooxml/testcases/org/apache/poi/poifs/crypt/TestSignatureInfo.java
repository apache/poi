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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POITestCase;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.crypt.dsig.DigestInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo.SignaturePart;
import org.apache.poi.poifs.crypt.dsig.facets.EnvelopedSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESXLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampServiceValidator;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3.x2000.x09.xmldsig.ReferenceType;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;

public class TestSignatureInfo {
    private static final POILogger LOG = POILogFactory.getLogger(TestSignatureInfo.class);
    private static final POIDataSamples testdata = POIDataSamples.getXmlDSignInstance();

    private static Calendar cal;
    private KeyPair keyPair = null;
    private X509Certificate x509 = null;
    
    @BeforeClass
    public static void initBouncy() throws IOException {
        CryptoFunctions.registerBouncyCastle();

        // Set cal to now ... only set to fixed date for debugging ...
        cal = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
        assertNotNull(cal);
//        cal.set(2014, 7, 6, 21, 42, 12);
//        cal.clear(Calendar.MILLISECOND);

        // don't run this test when we are using older Xerces as it triggers an XML Parser backwards compatibility issue 
        // in the xmlsec jar file
        String additionalJar = System.getProperty("additionaljar");
        //System.out.println("Having: " + additionalJar);
        Assume.assumeTrue("Not running TestSignatureInfo because we are testing with additionaljar set to " + additionalJar, 
                additionalJar == null || additionalJar.trim().length() == 0);
    }
    
    @Test
    public void office2007prettyPrintedRels() throws Exception {
        OPCPackage pkg = OPCPackage.open(testdata.getFile("office2007prettyPrintedRels.docx"), PackageAccess.READ);
        try {
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            boolean isValid = si.verifySignature();
            assertTrue(isValid);
        } finally {
            pkg.close();
        }
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
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            List<X509Certificate> result = new ArrayList<X509Certificate>();
            for (SignaturePart sp : si.getSignatureParts()) {
                if (sp.validate()) {
                    result.add(sp.getSigner());
                }
            }
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
            try {
                SignatureConfig sic = new SignatureConfig();
                sic.setOpcPackage(pkg);
                SignatureInfo si = new SignatureInfo();
                si.setSignatureConfig(sic);
                List<X509Certificate> result = new ArrayList<X509Certificate>();
                for (SignaturePart sp : si.getSignatureParts()) {
                    if (sp.validate()) {
                        result.add(sp.getSigner());
                    }
                }
    
                assertNotNull(result);
                assertEquals("test-file: "+testFile, 1, result.size());
                X509Certificate signer = result.get(0);
                LOG.log(POILogger.DEBUG, "signer: " + signer.getSubjectX500Principal());
    
                boolean b = si.verifySignature();
                assertTrue("test-file: "+testFile, b);
                pkg.revert();
            } finally {
                pkg.close();
            }
        }
    }

    @Test
    public void getMultiSigners() throws Exception {
        String testFile = "hello-world-signed-twice.docx";
        OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ);
        try {
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            List<X509Certificate> result = new ArrayList<X509Certificate>();
            for (SignaturePart sp : si.getSignatureParts()) {
                if (sp.validate()) {
                    result.add(sp.getSigner());
                }
            }
    
            assertNotNull(result);
            assertEquals("test-file: "+testFile, 2, result.size());
            X509Certificate signer1 = result.get(0);
            X509Certificate signer2 = result.get(1);
            LOG.log(POILogger.DEBUG, "signer 1: " + signer1.getSubjectX500Principal());
            LOG.log(POILogger.DEBUG, "signer 2: " + signer2.getSubjectX500Principal());
    
            boolean b = si.verifySignature();
            assertTrue("test-file: "+testFile, b);
            pkg.revert();
        } finally {
            pkg.close();
        }
    }
    
    @Test
    public void testSignSpreadsheet() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
        sign(pkg, "Test", "CN=Test", 1);
        pkg.close();
    }

    @Test
    public void testManipulation() throws Exception {
        // sign & validate
        String testFile = "hello-world-unsigned.xlsx";
        @SuppressWarnings("resource")
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
        sign(pkg, "Test", "CN=Test", 1);
        
        // manipulate
        XSSFWorkbook wb = new XSSFWorkbook(pkg);
        wb.setSheetName(0, "manipulated");
        // ... I don't know, why commit is protected ...
        POITestCase.callMethod(XSSFWorkbook.class, wb, Void.class, "commit", new Class[0], new Object[0]);

        // todo: test a manipulation on a package part, which is not signed
        // ... maybe in combination with #56164 
        
        // validate
        SignatureConfig sic = new SignatureConfig();
        sic.setOpcPackage(pkg);
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(sic);
        boolean b = si.verifySignature();
        assertFalse("signature should be broken", b);
        
        wb.close();
    }
    
    @Test
    public void testSignSpreadsheetWithSignatureInfo() throws Exception {
        initKeyPair("Test", "CN=Test");
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
        SignatureConfig sic = new SignatureConfig();
        sic.setOpcPackage(pkg);
        sic.setKey(keyPair.getPrivate());
        sic.setSigningCertificateChain(Collections.singletonList(x509));
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(sic);
        // hash > sha1 doesn't work in excel viewer ...
        si.confirmSignature();
        List<X509Certificate> result = new ArrayList<X509Certificate>();
        for (SignaturePart sp : si.getSignatureParts()) {
            if (sp.validate()) {
                result.add(sp.getSigner());
            }
        }
        assertEquals(1, result.size());
        pkg.close();
    }

    @Test
    public void testSignEnvelopingDocument() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);

        initKeyPair("Test", "CN=Test");
        final X509CRL crl = PkiTestUtils.generateCrl(x509, keyPair.getPrivate());
        
        // setup
        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setOpcPackage(pkg);
        signatureConfig.setKey(keyPair.getPrivate());

        /*
         * We need at least 2 certificates for the XAdES-C complete certificate
         * refs construction.
         */
        List<X509Certificate> certificateChain = new ArrayList<X509Certificate>();
        certificateChain.add(x509);
        certificateChain.add(x509);
        signatureConfig.setSigningCertificateChain(certificateChain);
        
        signatureConfig.addSignatureFacet(new EnvelopedSignatureFacet());
        signatureConfig.addSignatureFacet(new KeyInfoSignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESSignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESXLSignatureFacet());
        
        // check for internet, no error means it works
        boolean mockTsp = (getAccessError("http://timestamp.comodoca.com/rfc3161", true, 10000) != null);
        
        // http://timestamping.edelweb.fr/service/tsp
        // http://tsa.belgium.be/connect
        // http://timestamp.comodoca.com/authenticode
        // http://timestamp.comodoca.com/rfc3161
        // http://services.globaltrustfinder.com/adss/tsa
        signatureConfig.setTspUrl("http://timestamp.comodoca.com/rfc3161");
        signatureConfig.setTspRequestPolicy(null); // comodoca request fails, if default policy is set ...
        signatureConfig.setTspOldProtocol(false);
        
        //set proxy info if any
        String proxy = System.getProperty("http_proxy");
        if (proxy != null && proxy.trim().length() > 0) {
            signatureConfig.setProxyUrl(proxy);
        }

        if (mockTsp) {
            TimeStampService tspService = new TimeStampService(){
                @Override
                public byte[] timeStamp(byte[] data, RevocationData revocationData) throws Exception {
                    revocationData.addCRL(crl);
                    return "time-stamp-token".getBytes(LocaleUtil.CHARSET_1252);                
                }
                @Override
                public void setSignatureConfig(SignatureConfig config) {
                    // empty on purpose
                }
            };
            signatureConfig.setTspService(tspService);
        } else {
            TimeStampServiceValidator tspValidator = new TimeStampServiceValidator() {
                @Override
                public void validate(List<X509Certificate> validateChain,
                RevocationData revocationData) throws Exception {
                    for (X509Certificate certificate : validateChain) {
                        LOG.log(POILogger.DEBUG, "certificate: " + certificate.getSubjectX500Principal());
                        LOG.log(POILogger.DEBUG, "validity: " + certificate.getNotBefore() + " - " + certificate.getNotAfter());
                    }
                }
            };
            signatureConfig.setTspValidator(tspValidator);
            signatureConfig.setTspOldProtocol(signatureConfig.getTspUrl().contains("edelweb"));
        }
        
        final RevocationData revocationData = new RevocationData();
        revocationData.addCRL(crl);
        OCSPResp ocspResp = PkiTestUtils.createOcspResp(x509, false,
                x509, x509, keyPair.getPrivate(), "SHA1withRSA", cal.getTimeInMillis());
        revocationData.addOCSP(ocspResp.getEncoded());

        RevocationDataService revocationDataService = new RevocationDataService(){
            @Override
            public RevocationData getRevocationData(List<X509Certificate> revocationChain) {
                return revocationData;
            }
        };
        signatureConfig.setRevocationDataService(revocationDataService);

        // operate
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);
        try {
            si.confirmSignature();
        } catch (RuntimeException e) {
            pkg.close();
            // only allow a ConnectException because of timeout, we see this in Jenkins from time to time...
            if(e.getCause() == null) {
                throw e;
            }
            if((e.getCause() instanceof ConnectException) || (e.getCause() instanceof SocketTimeoutException)) {
                Assume.assumeFalse("Only allowing ConnectException with 'timed out' as message here, but had: " + e,
                        e.getCause().getMessage().contains("timed out"));
            } else if (e.getCause() instanceof IOException) {
                Assume.assumeFalse("Only allowing IOException with 'Error contacting TSP server' as message here, but had: " + e,
                        e.getCause().getMessage().contains("Error contacting TSP server"));
            } else if (e.getCause() instanceof RuntimeException) {
                Assume.assumeFalse("Only allowing RuntimeException with 'This site is cur' as message here, but had: " + e,
                        e.getCause().getMessage().contains("This site is cur"));
            }
            throw e;
        }
        
        // verify
        Iterator<SignaturePart> spIter = si.getSignatureParts().iterator();
        assertTrue("Had: " + si.getSignatureConfig().getOpcPackage().
                        getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN),
                spIter.hasNext());
        SignaturePart sp = spIter.next();
        boolean valid = sp.validate();
        assertTrue(valid);
        
        SignatureDocument sigDoc = sp.getSignatureDocument();
        String declareNS = 
            "declare namespace xades='http://uri.etsi.org/01903/v1.3.2#'; "
          + "declare namespace ds='http://www.w3.org/2000/09/xmldsig#'; ";
        
        String digestValXQuery = declareNS +
            "$this/ds:Signature/ds:SignedInfo/ds:Reference";
        for (ReferenceType rt : (ReferenceType[])sigDoc.selectPath(digestValXQuery)) {
            assertNotNull(rt.getDigestValue());
            assertEquals(signatureConfig.getDigestMethodUri(), rt.getDigestMethod().getAlgorithm());
        }

        String certDigestXQuery = declareNS +
            "$this//xades:SigningCertificate/xades:Cert/xades:CertDigest";
        XmlObject xoList[] = sigDoc.selectPath(certDigestXQuery);
        assertEquals(xoList.length, 1);
        DigestAlgAndValueType certDigest = (DigestAlgAndValueType)xoList[0];
        assertNotNull(certDigest.getDigestValue());

        String qualPropXQuery = declareNS +
            "$this/ds:Signature/ds:Object/xades:QualifyingProperties";
        xoList = sigDoc.selectPath(qualPropXQuery);
        assertEquals(xoList.length, 1);
        QualifyingPropertiesType qualProp = (QualifyingPropertiesType)xoList[0];
        boolean qualPropXsdOk = qualProp.validate();
        assertTrue(qualPropXsdOk);

        pkg.close();
    }

    public static String getAccessError(String destinationUrl, boolean fireRequest, int timeout) {
        URL url;
        try {
            url = new URL(destinationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid destination URL", e);
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();

            // set specified timeout if non-zero
            if(timeout != 0) {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }

            conn.setDoOutput(false);
            conn.setDoInput(true);

            /* if connecting is not possible this will throw a connection refused exception */
            conn.connect();

            if (fireRequest) {
                InputStream is = null;
                try {
                    is = conn.getInputStream();
                } finally {
                    IOUtils.closeQuietly(is);
                }

            }
            /* if connecting is possible we return true here */
            return null;

        } catch (IOException e) {
            /* exception is thrown -> server not available */
            return e.getClass().getName() + ": " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    @Test
    public void testCertChain() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        String password = "test";
        InputStream is = testdata.openResourceAsStream("chaintest.pfx");
        keystore.load(is, password.toCharArray());
        is.close();

        Key key = keystore.getKey("poitest", password.toCharArray());
        Certificate chainList[] = keystore.getCertificateChain("poitest");
        List<X509Certificate> certChain = new ArrayList<X509Certificate>();
        for (Certificate c : chainList) {
            certChain.add((X509Certificate)c);
        }
        x509 = certChain.get(0);
        keyPair = new KeyPair(x509.getPublicKey(), (PrivateKey)key);
        
        String testFile = "hello-world-unsigned.xlsx";
        OPCPackage pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);

        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setKey(keyPair.getPrivate());
        signatureConfig.setSigningCertificateChain(certChain);
        Calendar oldCal = LocaleUtil.getLocaleCalendar(2007, 7, 1);
        signatureConfig.setExecutionTime(oldCal.getTime());
        signatureConfig.setDigestAlgo(HashAlgorithm.sha1);
        signatureConfig.setOpcPackage(pkg);
        
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);

        si.confirmSignature();
        
        for (SignaturePart sp : si.getSignatureParts()){
            assertTrue("Could not validate", sp.validate());
            X509Certificate signer = sp.getSigner();
            assertNotNull("signer undefined?!", signer);
            List<X509Certificate> certChainRes = sp.getCertChain();
            assertEquals(3, certChainRes.size());
        }
        
        pkg.close();
    }

    @Test
    public void testNonSha1() throws Exception {
        String testFile = "hello-world-unsigned.xlsx";
        initKeyPair("Test", "CN=Test");

        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setKey(keyPair.getPrivate());
        signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));

        HashAlgorithm testAlgo[] = { HashAlgorithm.sha224, HashAlgorithm.sha256
            , HashAlgorithm.sha384, HashAlgorithm.sha512, HashAlgorithm.ripemd160 }; 
        
        for (HashAlgorithm ha : testAlgo) {
            OPCPackage pkg = null;
            try {
                signatureConfig.setDigestAlgo(ha);
                pkg = OPCPackage.open(copy(testdata.getFile(testFile)), PackageAccess.READ_WRITE);
                signatureConfig.setOpcPackage(pkg);
                
                SignatureInfo si = new SignatureInfo();
                si.setSignatureConfig(signatureConfig);
        
                si.confirmSignature();
                boolean b = si.verifySignature();
                assertTrue("Signature not correctly calculated for " + ha, b);
            } finally {
                if (pkg != null) {
                    pkg.close();
                }
            }
        }
    }

    @Test
    public void bug58630() throws Exception {
        // test deletion of sheet 0 and signing
        File tpl = copy(testdata.getFile("bug58630.xlsx"));
        SXSSFWorkbook wb1 = new SXSSFWorkbook((XSSFWorkbook)WorkbookFactory.create(tpl), 10);
        wb1.setCompressTempFiles(true);
        wb1.removeSheetAt(0);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb1.write(os);
        wb1.close();
        OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(os.toByteArray()));
        
        initKeyPair("Test", "CN=Test");
        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setKey(keyPair.getPrivate());
        signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
        signatureConfig.setOpcPackage(pkg);
        
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);
        si.confirmSignature();
        assertTrue("invalid signature", si.verifySignature());
        
        pkg.close();
    }
    
    @Test
    public void testMultiSign() throws Exception {
        initKeyPair("KeyA", "CN=KeyA");
        //KeyPair keyPairA = keyPair;
        //X509Certificate x509A = x509;
        initKeyPair("KeyB", "CN=KeyB");
        //KeyPair keyPairB = keyPair;
        //X509Certificate x509B = x509;
        
        File tpl = copy(testdata.getFile("bug58630.xlsx"));
        OPCPackage pkg = OPCPackage.open(tpl);
        try {
            //SignatureConfig signatureConfig = new SignatureConfig();
            assertNotNull(pkg);
        } finally {
            pkg.close();
        }
    }
    
    private void sign(OPCPackage pkgCopy, String alias, String signerDn, int signerCount) throws Exception {
        initKeyPair(alias, signerDn);

        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setKey(keyPair.getPrivate());
        signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
        signatureConfig.setExecutionTime(cal.getTime());
        signatureConfig.setDigestAlgo(HashAlgorithm.sha1);
        signatureConfig.setOpcPackage(pkgCopy);
        
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);

        Document document = DocumentHelper.createDocument();

        // operate
        DigestInfo digestInfo = si.preSign(document, null);

        // verify
        assertNotNull(digestInfo);
        LOG.log(POILogger.DEBUG, "digest algo: " + digestInfo.hashAlgo);
        LOG.log(POILogger.DEBUG, "digest description: " + digestInfo.description);
        assertEquals("Office OpenXML Document", digestInfo.description);
        assertNotNull(digestInfo.hashAlgo);
        assertNotNull(digestInfo.digestValue);

        // setup: key material, signature value
        byte[] signatureValue = si.signDigest(digestInfo.digestValue);
        
        // operate: postSign
        si.postSign(document, signatureValue);

        // verify: signature
        si.getSignatureConfig().setOpcPackage(pkgCopy);
        List<X509Certificate> result = new ArrayList<X509Certificate>();
        for (SignaturePart sp : si.getSignatureParts()) {
            if (sp.validate()) {
                result.add(sp.getSigner());
            }
        }
        assertEquals(signerCount, result.size());
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
            Date notBefore = cal.getTime();
            Calendar cal2 = (Calendar)cal.clone();
            cal2.add(Calendar.YEAR, 1);
            Date notAfter = cal2.getTime();
            KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature);
            
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
        if (extension == null || "".equals(extension)) {
            extension = ".zip";
        }

        // ensure that we create the "build" directory as it might not be existing
        // in the Sonar Maven runs where we are at a different source directory
        File buildDir = new File("build");
        if(!buildDir.exists()) {
            assertTrue("Failed to create " + buildDir.getAbsolutePath(), 
                    buildDir.mkdirs());
        }
        File tmpFile = new File(buildDir, "sigtest"+extension);

        OutputStream fos = new FileOutputStream(tmpFile);
        try {
            InputStream fis = new FileInputStream(input);
            try {
                IOUtils.copy(fis, fos);
            } finally {
                fis.close();
            }
        } finally {
            fos.close();
        }

        return tmpFile;
    }

}
