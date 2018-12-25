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
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.dom.DOMSignContext;

import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.POITestCase;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignaturePart;
import org.apache.poi.poifs.crypt.dsig.facets.EnvelopedSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.OOXMLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESXLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampServiceValidator;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3.x2000.x09.xmldsig.ReferenceType;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;

public class TestSignatureInfo {
    private static final POILogger LOG = POILogFactory.getLogger(TestSignatureInfo.class);
    private static final POIDataSamples testdata = POIDataSamples.getXmlDSignInstance();

    private static Calendar cal;
    private KeyPair keyPair;
    private X509Certificate x509;

    @AfterClass
    public static void removeUserLocale() {
        LocaleUtil.resetUserLocale();
    }
    
    @BeforeClass
    public static void initBouncy() {
        CryptoFunctions.registerBouncyCastle();

        // Set cal to now ... only set to fixed date for debugging ...
        LocaleUtil.resetUserLocale();
        LocaleUtil.resetUserTimeZone();
        
        cal = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
        assertNotNull(cal);

        // don't run this test when we are using older Xerces as it triggers an XML Parser backwards compatibility issue 
        // in the xmlsec jar file
        String additionalJar = System.getProperty("additionaljar");
        //System.out.println("Having: " + additionalJar);
        Assume.assumeTrue("Not running TestSignatureInfo because we are testing with additionaljar set to " + additionalJar, 
                additionalJar == null || additionalJar.trim().length() == 0);
        
        System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
        
        // Set line.separator for bug61182
        // System.setProperty("line.separator", "\n");
    }

    @Ignore("This test is very sensitive, it breaks with every little change to the produced XML")
    @Test
    public void bug61182() throws Exception {
        String pfxInput =
            "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";
        
        Calendar cal = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
        cal.clear();
        cal.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        cal.set(2017, Calendar.JULY, 1);
        
        SignatureConfig signatureConfig = prepareConfig("test", "CN=Test", pfxInput);
        signatureConfig.setExecutionTime(cal.getTime());

        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);

        XSSFWorkbook wb1 = new XSSFWorkbook();
        wb1.createSheet().createRow(1).createCell(1).setCellValue("Test");
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
        wb1.write(bos);
        wb1.close();
        
        OPCPackage pkg1 = OPCPackage.open(new ByteArrayInputStream(bos.toByteArray()));
        
        signatureConfig.setOpcPackage(pkg1);
        si.confirmSignature();
        assertTrue(si.verifySignature());
        bos.reset();
        pkg1.save(bos);
        pkg1.close();

        XSSFWorkbook wb2 = new XSSFWorkbook(new ByteArrayInputStream(bos.toByteArray()));
        assertEquals("Test", wb2.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
        OPCPackage pkg2 = wb2.getPackage();
        signatureConfig.setOpcPackage(pkg2);
        assertTrue(si.verifySignature());
        
        // xmlbeans adds line-breaks depending on the system setting, so we get different
        // test results on Unix/Mac/Windows
        // if the xml documents eventually change, this test needs to be run with the
        // separator set to the various system configurations
        String sep = SystemProperties.getProperty( "line.separator" );
        String signExp;
        assumeTrue("Hashes only known for Windows/Unix/Mac", sep == null || "\n".equals(sep) || "\r\n".equals(sep) || "\r".equals(sep));
        if (sep == null || "\n".equals(sep)) {
            // Unix
            signExp =
                "QkqTFQZjXagjRAoOWKpAGa8AR0rKqkSfBtfSWqtjBmTgyjarn+t2POHkpySIpheHAbg+90GKSH88ACMtPHbG7q"+
                "FL4gtgAD9Kjew6j16j0IRBwy145UlPrSLFMfF7YF7UlU1k1LBkIlRJ6Fv4MAJl6XspuzZOZIUmHZrWrdxycUQ=";
        } else if ("\r\n".equals(sep)){
            // Windows
            signExp =
                "GmAlL7+bT1r3FsMHJOp3pKg8betblYieZTjhMIrPZPRBbSzjO7KsYRGNtr0aOE3qr8xzyYJN6/8QdF5X7pUEUc"+
                "2m8ctrm7s5o2vZTkAqk9ENJGDjBPXX7TnuVOiVeL1cJdtjHC2QpjtRwkFR+B54G6b1OXLOFuQpP3vqR3+/XXE=";
        } else {
            // Mac
            signExp =
                "NZedY/LNTYU4nAUEUhIOg5+fKdgVtzRXKmdD3v+47E7Mb84oeiUGv9cCEE91DU3StF/JFIhjOJqavOzKnCsNcz"+
                "NJ4j/inggUl1OJUsicqIGQnA7E8vzWnN1kf5lINgJLv+0PyrrX9sQZbItzxUpgqyOFYcD0trid+31nRt4wtaA=";
        }
        
        String signAct = si.getSignatureParts().iterator().next().
            getSignatureDocument().getSignature().getSignatureValue().getStringValue();
        assertEquals(signExp, signAct);
        
        pkg2.close();
        wb2.close();
    }
    
    @Test
    public void office2007prettyPrintedRels() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(testdata.getFile("office2007prettyPrintedRels.docx"), PackageAccess.READ)) {
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            boolean isValid = si.verifySignature();
            assertTrue(isValid);
        }
    }
    
    @Test
    public void getSignerUnsigned() throws Exception {
        String[] testFiles = {
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
            List<X509Certificate> result = new ArrayList<>();
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
        String[] testFiles = {
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
            try (OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ)) {
                SignatureConfig sic = new SignatureConfig();
                sic.setOpcPackage(pkg);
                SignatureInfo si = new SignatureInfo();
                si.setSignatureConfig(sic);
                List<X509Certificate> result = new ArrayList<>();
                for (SignaturePart sp : si.getSignatureParts()) {
                    if (sp.validate()) {
                        result.add(sp.getSigner());
                    }
                }

                assertNotNull(result);
                assertEquals("test-file: " + testFile, 1, result.size());
                X509Certificate signer = result.get(0);
                LOG.log(POILogger.DEBUG, "signer: " + signer.getSubjectX500Principal());

                boolean b = si.verifySignature();
                assertTrue("test-file: " + testFile, b);
                pkg.revert();
            }
        }
    }

    @Test
    public void getMultiSigners() throws Exception {
        String testFile = "hello-world-signed-twice.docx";
        try (OPCPackage pkg = OPCPackage.open(testdata.getFile(testFile), PackageAccess.READ)) {
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            List<X509Certificate> result = new ArrayList<>();
            for (SignaturePart sp : si.getSignatureParts()) {
                if (sp.validate()) {
                    result.add(sp.getSigner());
                }
            }

            assertNotNull(result);
            assertEquals("test-file: " + testFile, 2, result.size());
            X509Certificate signer1 = result.get(0);
            X509Certificate signer2 = result.get(1);
            LOG.log(POILogger.DEBUG, "signer 1: " + signer1.getSubjectX500Principal());
            LOG.log(POILogger.DEBUG, "signer 2: " + signer2.getSubjectX500Principal());

            boolean b = si.verifySignature();
            assertTrue("test-file: " + testFile, b);
            pkg.revert();
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
        List<X509Certificate> result = new ArrayList<>();
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
        File sigCopy = testdata.getFile(testFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(50000);

        final String execTimestr;


        try (OPCPackage pkg = OPCPackage.open(copy(sigCopy), PackageAccess.READ_WRITE)) {

            initKeyPair("Test", "CN=Test");
            final X509CRL crl = generateCrl(x509, keyPair.getPrivate());

            // setup
            SignatureConfig signatureConfig = new SignatureConfig();
            signatureConfig.setOpcPackage(pkg);
            signatureConfig.setKey(keyPair.getPrivate());

            /*
             * We need at least 2 certificates for the XAdES-C complete certificate
             * refs construction.
             */
            List<X509Certificate> certificateChain = new ArrayList<>();
            certificateChain.add(x509);
            certificateChain.add(x509);
            signatureConfig.setSigningCertificateChain(certificateChain);

            signatureConfig.addSignatureFacet(new OOXMLSignatureFacet());
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

            signatureConfig.setXadesDigestAlgo(HashAlgorithm.sha512);
            signatureConfig.setXadesRole("Xades Reviewer");
            signatureConfig.setSignatureDescription("test xades signature");

            execTimestr = signatureConfig.formatExecutionTime();

            //set proxy info if any
            String proxy = System.getProperty("http_proxy");
            if (proxy != null && proxy.trim().length() > 0) {
                signatureConfig.setProxyUrl(proxy);
            }

            if (mockTsp) {
                TimeStampService tspService = new TimeStampService() {
                    @Override
                    public byte[] timeStamp(byte[] data, RevocationData revocationData) {
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
                TimeStampServiceValidator tspValidator = (validateChain, revocationData) -> {
                    for (X509Certificate certificate : validateChain) {
                        LOG.log(POILogger.DEBUG, "certificate: " + certificate.getSubjectX500Principal());
                        LOG.log(POILogger.DEBUG, "validity: " + certificate.getNotBefore() + " - " + certificate.getNotAfter());
                    }
                };
                signatureConfig.setTspValidator(tspValidator);
                signatureConfig.setTspOldProtocol(signatureConfig.getTspUrl().contains("edelweb"));
            }

            final RevocationData revocationData = new RevocationData();
            revocationData.addCRL(crl);
            OCSPResp ocspResp = createOcspResp(x509, false,
                    x509, x509, keyPair.getPrivate(), "SHA1withRSA", cal.getTimeInMillis());
            revocationData.addOCSP(ocspResp.getEncoded());

            RevocationDataService revocationDataService = revocationChain -> revocationData;
            signatureConfig.setRevocationDataService(revocationDataService);

            // operate
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(signatureConfig);
            try {
                si.confirmSignature();
            } catch (RuntimeException e) {
                pkg.close();
                // only allow a ConnectException because of timeout, we see this in Jenkins from time to time...
                if (e.getCause() == null) {
                    throw e;
                }
                if ((e.getCause() instanceof ConnectException) || (e.getCause() instanceof SocketTimeoutException)) {
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
            for (ReferenceType rt : (ReferenceType[]) sigDoc.selectPath(digestValXQuery)) {
                assertNotNull(rt.getDigestValue());
                assertEquals(signatureConfig.getDigestMethodUri(), rt.getDigestMethod().getAlgorithm());
            }

            String certDigestXQuery = declareNS +
                    "$this//xades:SigningCertificate/xades:Cert/xades:CertDigest";
            XmlObject[] xoList = sigDoc.selectPath(certDigestXQuery);
            assertEquals(xoList.length, 1);
            DigestAlgAndValueType certDigest = (DigestAlgAndValueType) xoList[0];
            assertNotNull(certDigest.getDigestValue());

            String qualPropXQuery = declareNS +
                    "$this/ds:Signature/ds:Object/xades:QualifyingProperties";
            xoList = sigDoc.selectPath(qualPropXQuery);
            assertEquals(xoList.length, 1);
            QualifyingPropertiesType qualProp = (QualifyingPropertiesType) xoList[0];
            boolean qualPropXsdOk = qualProp.validate();
            assertTrue(qualPropXsdOk);

            pkg.save(bos);
        }

        try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(bos.toByteArray()))) {
            SignatureConfig signatureConfig = new SignatureConfig();
            signatureConfig.setOpcPackage(pkg);
            signatureConfig.setUpdateConfigOnValidate(true);

            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(signatureConfig);

            assertTrue(si.verifySignature());

            assertEquals(HashAlgorithm.sha512, signatureConfig.getXadesDigestAlgo());
            assertEquals("Xades Reviewer", signatureConfig.getXadesRole());
            assertEquals("test xades signature", signatureConfig.getSignatureDescription());
            assertEquals(execTimestr, signatureConfig.formatExecutionTime());
        }
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
        Certificate[] chainList = keystore.getCertificateChain("poitest");
        List<X509Certificate> certChain = new ArrayList<>();
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

        HashAlgorithm[] testAlgo = {HashAlgorithm.sha224, HashAlgorithm.sha256
                , HashAlgorithm.sha384, HashAlgorithm.sha512, HashAlgorithm.ripemd160};
        
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
            } catch (EncryptedDocumentException e) {
                Assume.assumeTrue(e.getMessage().startsWith("Export Restrictions"));
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
        cal = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
        cal.clear();
        cal.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        cal.set(2018, Calendar.DECEMBER, 14);

        // test signing with separate opened packages
        File tpl = copy(testdata.getFile("hello-world-unsigned.xlsx"));
        try (OPCPackage pkg = OPCPackage.open(tpl)) {
            signPkg63011(pkg, "bug63011_key1.pem", true);
        }

        try (OPCPackage pkg = OPCPackage.open(tpl)) {
            signPkg63011(pkg, "bug63011_key2.pem", true);
        }

        verifyPkg63011(tpl, true);

        // test signing with single opened package
        tpl = copy(testdata.getFile("hello-world-unsigned.xlsx"));
        try (OPCPackage pkg = OPCPackage.open(tpl)) {
            signPkg63011(pkg, "bug63011_key1.pem", true);
            signPkg63011(pkg, "bug63011_key2.pem", true);
        }

        verifyPkg63011(tpl, true);

        try (OPCPackage pkg = OPCPackage.open(tpl)) {
            signPkg63011(pkg, "bug63011_key1.pem", true);
            signPkg63011(pkg, "bug63011_key2.pem", false);
        }

        verifyPkg63011(tpl, false);
    }

    private void verifyPkg63011(File tpl, boolean multi) throws InvalidFormatException, IOException {
        try (OPCPackage pkg = OPCPackage.open(tpl, PackageAccess.READ)) {
            SignatureConfig sic = new SignatureConfig();
            sic.setOpcPackage(pkg);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            List<X509Certificate> result = new ArrayList<>();
            for (SignaturePart sp : si.getSignatureParts()) {
                if (sp.validate()) {
                    result.add(sp.getSigner());
                }
            }

            assertNotNull(result);

            if (multi) {
                assertEquals(2, result.size());
                assertEquals("CN=Muj Klic", result.get(0).getSubjectDN().toString());
                assertEquals("CN=My Second key", result.get(1).getSubjectDN().toString());
            } else {
                assertEquals(1, result.size());
                assertEquals("CN=My Second key", result.get(0).getSubjectDN().toString());
            }

            assertTrue(si.verifySignature());
            pkg.revert();
        }
    }

    private void signPkg63011(OPCPackage pkg, String pemFile, boolean multi)
    throws IOException, CertificateException, XMLSignatureException, MarshalException {
        assertNotNull(pkg);
        initKeyFromPEM(testdata.getFile(pemFile));

        SignatureConfig config = new SignatureConfig();
        config.setKey(keyPair.getPrivate());
        config.setSigningCertificateChain(Collections.singletonList(x509));
        config.setExecutionTime(cal.getTime());
        config.setAllowMultipleSignatures(multi);
        config.setOpcPackage(pkg);

        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(config);
        si.confirmSignature();
    }

    @Test
    public void testRetrieveCertificate() throws InvalidFormatException, IOException {
        SignatureConfig sic = new SignatureConfig();
        final File file = testdata.getFile("PPT2016withComment.pptx");
        try (final OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ)) {
            sic.setOpcPackage(pkg);
            sic.setUpdateConfigOnValidate(true);
            SignatureInfo si = new SignatureInfo();
            si.setSignatureConfig(sic);
            assertTrue(si.verifySignature());
        }

        final List<X509Certificate> certs = sic.getSigningCertificateChain();
        assertEquals(1, certs.size());
        assertEquals("CN=Test", certs.get(0).getSubjectDN().getName());
        assertEquals("SuperDuper-Reviewer", sic.getXadesRole());
        assertEquals("Purpose for signing", sic.getSignatureDescription());
        assertEquals("2018-06-10T09:00:54Z", sic.formatExecutionTime());
        assertEquals(CanonicalizationMethod.INCLUSIVE, sic.getCanonicalizationMethod());
    }

    private SignatureConfig prepareConfig(String alias, String signerDn, String pfxInput) throws Exception {
        initKeyPair(alias, signerDn, pfxInput);

        SignatureConfig signatureConfig = new SignatureConfig();
        signatureConfig.setKey(keyPair.getPrivate());
        signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
        signatureConfig.setExecutionTime(cal.getTime());
        signatureConfig.setDigestAlgo(HashAlgorithm.sha1);

        return signatureConfig;
    }
    
    private void sign(OPCPackage pkgCopy, String alias, String signerDn, int signerCount) throws Exception {
        SignatureConfig signatureConfig = prepareConfig(alias, signerDn, null);
        signatureConfig.setOpcPackage(pkgCopy);
        
        SignatureInfo si = new SignatureInfo();
        si.setSignatureConfig(signatureConfig);

        final Document document = DocumentHelper.createDocument();
        final DOMSignContext xmlSignContext = si.createXMLSignContext(document);

        // operate
        final DOMSignedInfo signedInfo = si.preSign(xmlSignContext);

        // verify
        assertNotNull(signedInfo);
        assertEquals("Office OpenXML Document", signatureConfig.getSignatureDescription());

        // setup: key material, signature value
        final String signatureValue = si.signDigest(xmlSignContext, signedInfo);
        
        // operate: postSign
        si.postSign(xmlSignContext, signatureValue);

        // verify: signature
        si.getSignatureConfig().setOpcPackage(pkgCopy);
        List<X509Certificate> result = new ArrayList<>();
        for (SignaturePart sp : si.getSignatureParts()) {
            if (sp.validate()) {
                result.add(sp.getSigner());
            }
        }
        assertEquals(signerCount, result.size());
    }

    private void initKeyPair(String alias, String subjectDN) throws Exception {
        initKeyPair(alias, subjectDN, null);
    }
    
    private void initKeyPair(String alias, String subjectDN, String pfxInput) throws Exception {
        final char[] password = "test".toCharArray();
        File file = new File("build/test.pfx");

        KeyStore keystore = KeyStore.getInstance("PKCS12");

        if (pfxInput != null) {
            InputStream fis = new ByteArrayInputStream(RawDataUtil.decompress(pfxInput));
            keystore.load(fis, password);
            fis.close();
        } else if (file.exists()) { 
            InputStream fis = new FileInputStream(file);
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
            keyPair = generateKeyPair();
            Date notBefore = cal.getTime();
            Calendar cal2 = (Calendar)cal.clone();
            cal2.add(Calendar.YEAR, 1);
            Date notAfter = cal2.getTime();
            KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature);
            
            x509 = generateCertificate(keyPair.getPublic(), subjectDN
                , notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, keyUsage);

            keystore.setKeyEntry(alias, keyPair.getPrivate(), password, new Certificate[]{x509});
            
            if (pfxInput == null) {
                FileOutputStream fos = new FileOutputStream(file);
                keystore.store(fos, password);
                fos.close();
            }
        }
    }

    private void initKeyFromPEM(File pemFile) throws IOException, CertificateException {
        // see https://stackoverflow.com/questions/11787571/how-to-read-pem-file-to-get-private-and-public-key
        PrivateKey key = null;
        x509 = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pemFile), StandardCharsets.ISO_8859_1))) {
            PEMParser parser = new PEMParser(br);
            for (Object obj; (obj = parser.readObject()) != null; ) {
                if (obj instanceof PrivateKeyInfo) {
                    key = new JcaPEMKeyConverter().setProvider("BC").getPrivateKey((PrivateKeyInfo)obj);
                } else if (obj instanceof X509CertificateHolder) {
                    x509 = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder)obj);
                }
            }
        }

        if (key != null && x509 != null) {
            keyPair = new KeyPair(x509.getPublicKey(), key);
        }
    }

    private static File copy(File input) throws IOException {
        String extension = input.getName().replaceAll(".*?(\\.[^.]+)?$", "$1");
        if (extension == null || extension.isEmpty()) {
            extension = ".zip";
        }

        // ensure that we create the "build" directory as it might not be existing
        // in the Sonar Maven runs where we are at a different source directory
        File buildDir = new File("build");
        if(!buildDir.exists()) {
            assertTrue("Failed to create " + buildDir.getAbsolutePath(), buildDir.mkdirs());
        }
        File tmpFile = new File(buildDir, "sigtest"+extension);

        try (OutputStream fos = new FileOutputStream(tmpFile)) {
            try (InputStream fis = new FileInputStream(input)) {
                IOUtils.copy(fis, fos);
            }
        }

        return tmpFile;
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(1024,
                RSAKeyGenParameterSpec.F4), random);
        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate generateCertificate(PublicKey subjectPublicKey,
                                               String subjectDn, Date notBefore, Date notAfter,
                                               X509Certificate issuerCertificate, PrivateKey issuerPrivateKey,
                                               boolean caFlag, int pathLength, String crlUri, String ocspUri,
                                               KeyUsage keyUsage)
            throws IOException, OperatorCreationException, CertificateException
    {
        String signatureAlgorithm = "SHA1withRSA";
        X500Name issuerName;
        if (issuerCertificate != null) {
            issuerName = new X509CertificateHolder(issuerCertificate.getEncoded()).getIssuer();
        } else {
            issuerName = new X500Name(subjectDn);
        }

        RSAPublicKey rsaPubKey = (RSAPublicKey)subjectPublicKey;
        RSAKeyParameters rsaSpec = new RSAKeyParameters(false, rsaPubKey.getModulus(), rsaPubKey.getPublicExponent());

        SubjectPublicKeyInfo subjectPublicKeyInfo =
                SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(rsaSpec);

        DigestCalculator digestCalc = new JcaDigestCalculatorProviderBuilder()
                .setProvider("BC").build().get(CertificateID.HASH_SHA1);

        X509v3CertificateBuilder certificateGenerator = new X509v3CertificateBuilder(
                issuerName
                , new BigInteger(128, new SecureRandom())
                , notBefore
                , notAfter
                , new X500Name(subjectDn)
                , subjectPublicKeyInfo
        );

        X509ExtensionUtils exUtils = new X509ExtensionUtils(digestCalc);
        SubjectKeyIdentifier subKeyId = exUtils.createSubjectKeyIdentifier(subjectPublicKeyInfo);
        AuthorityKeyIdentifier autKeyId = (issuerCertificate != null)
                ? exUtils.createAuthorityKeyIdentifier(new X509CertificateHolder(issuerCertificate.getEncoded()))
                : exUtils.createAuthorityKeyIdentifier(subjectPublicKeyInfo);

        certificateGenerator.addExtension(Extension.subjectKeyIdentifier, false, subKeyId);
        certificateGenerator.addExtension(Extension.authorityKeyIdentifier, false, autKeyId);

        if (caFlag) {
            BasicConstraints bc;

            if (-1 == pathLength) {
                bc = new BasicConstraints(true);
            } else {
                bc = new BasicConstraints(pathLength);
            }
            certificateGenerator.addExtension(Extension.basicConstraints, false, bc);
        }

        if (null != crlUri) {
            int uri = GeneralName.uniformResourceIdentifier;
            DERIA5String crlUriDer = new DERIA5String(crlUri);
            GeneralName gn = new GeneralName(uri, crlUriDer);

            DERSequence gnDer = new DERSequence(gn);
            GeneralNames gns = GeneralNames.getInstance(gnDer);

            DistributionPointName dpn = new DistributionPointName(0, gns);
            DistributionPoint distp = new DistributionPoint(dpn, null, null);
            DERSequence distpDer = new DERSequence(distp);
            certificateGenerator.addExtension(Extension.cRLDistributionPoints, false, distpDer);
        }

        if (null != ocspUri) {
            int uri = GeneralName.uniformResourceIdentifier;
            GeneralName ocspName = new GeneralName(uri, ocspUri);

            AuthorityInformationAccess authorityInformationAccess =
                    new AuthorityInformationAccess(X509ObjectIdentifiers.ocspAccessMethod, ocspName);

            certificateGenerator.addExtension(Extension.authorityInfoAccess, false, authorityInformationAccess);
        }

        if (null != keyUsage) {
            certificateGenerator.addExtension(Extension.keyUsage, true, keyUsage);
        }

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
        signerBuilder.setProvider("BC");

        X509CertificateHolder certHolder =
                certificateGenerator.build(signerBuilder.build(issuerPrivateKey));

        /*
         * Next certificate factory trick is needed to make sure that the
         * certificate delivered to the caller is provided by the default
         * security provider instead of BouncyCastle. If we don't do this trick
         * we might run into trouble when trying to use the CertPath validator.
         */
//        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//        certificate = (X509Certificate) certificateFactory
//                .generateCertificate(new ByteArrayInputStream(certificate
//                        .getEncoded()));
        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

    private static X509CRL generateCrl(X509Certificate issuer, PrivateKey issuerPrivateKey)
            throws CertificateEncodingException, IOException, CRLException, OperatorCreationException {

        X509CertificateHolder holder = new X509CertificateHolder(issuer.getEncoded());
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(holder.getIssuer(), new Date());
        crlBuilder.setNextUpdate(new Date(new Date().getTime() + 100000));
        JcaContentSignerBuilder contentBuilder = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC");

        CRLNumber crlNumber = new CRLNumber(new BigInteger("1234"));

        crlBuilder.addExtension(Extension.cRLNumber, false, crlNumber);
        X509CRLHolder x509Crl = crlBuilder.build(contentBuilder.build(issuerPrivateKey));
        return new JcaX509CRLConverter().setProvider("BC").getCRL(x509Crl);
    }

    private static OCSPResp createOcspResp(X509Certificate certificate,
                                          boolean revoked, X509Certificate issuerCertificate,
                                          X509Certificate ocspResponderCertificate,
                                          PrivateKey ocspResponderPrivateKey, String signatureAlgorithm,
                                          long nonceTimeinMillis)
            throws Exception {
        DigestCalculator digestCalc = new JcaDigestCalculatorProviderBuilder()
                .setProvider("BC").build().get(CertificateID.HASH_SHA1);
        X509CertificateHolder issuerHolder = new X509CertificateHolder(issuerCertificate.getEncoded());
        CertificateID certId = new CertificateID(digestCalc, issuerHolder, certificate.getSerialNumber());

        // request
        //create a nonce to avoid replay attack
        BigInteger nonce = BigInteger.valueOf(nonceTimeinMillis);
        DEROctetString nonceDer = new DEROctetString(nonce.toByteArray());
        Extension ext = new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, true, nonceDer);
        Extensions exts = new Extensions(ext);

        OCSPReqBuilder ocspReqBuilder = new OCSPReqBuilder();
        ocspReqBuilder.addRequest(certId);
        ocspReqBuilder.setRequestExtensions(exts);
        OCSPReq ocspReq = ocspReqBuilder.build();


        SubjectPublicKeyInfo keyInfo = new SubjectPublicKeyInfo
                (CertificateID.HASH_SHA1, ocspResponderCertificate.getPublicKey().getEncoded());

        BasicOCSPRespBuilder basicOCSPRespBuilder = new BasicOCSPRespBuilder(keyInfo, digestCalc);
        basicOCSPRespBuilder.setResponseExtensions(exts);

        // request processing
        Req[] requestList = ocspReq.getRequestList();
        for (Req ocspRequest : requestList) {
            CertificateID certificateID = ocspRequest.getCertID();
            CertificateStatus certificateStatus = CertificateStatus.GOOD;
            if (revoked) {
                certificateStatus = new RevokedStatus(new Date(), CRLReason.privilegeWithdrawn);
            }
            basicOCSPRespBuilder.addResponse(certificateID, certificateStatus);
        }

        // basic response generation
        X509CertificateHolder[] chain = null;
        if (!ocspResponderCertificate.equals(issuerCertificate)) {
            // TODO: HorribleProxy can't convert array input params yet
            chain = new X509CertificateHolder[] {
                    new X509CertificateHolder(ocspResponderCertificate.getEncoded()),
                    issuerHolder
            };
        }

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA")
                .setProvider("BC").build(ocspResponderPrivateKey);
        BasicOCSPResp basicOCSPResp = basicOCSPRespBuilder.build(contentSigner, chain, new Date(nonceTimeinMillis));


        OCSPRespBuilder ocspRespBuilder = new OCSPRespBuilder();

        return ocspRespBuilder.build(OCSPRespBuilder.SUCCESSFUL, basicOCSPResp);
    }
}
