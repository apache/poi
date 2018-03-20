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
package org.apache.poi.poifs.crypt;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.crypt.agile.AgileDecryptor;
import org.apache.poi.poifs.crypt.agile.AgileEncryptionVerifier;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/*
import org.junit.BeforeClass;
import java.util.Date;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
*/

/**
 * @see <a href="http://stackoverflow.com/questions/1615871/creating-an-x509-certificate-in-java-without-bouncycastle">creating a self-signed certificate</a> 
 */
public class TestCertificateEncryption {
    /**
     * how many days from now the Certificate is valid for
     */
    static final int days = 1000;
    /**
     * the signing algorithm, eg "SHA1withRSA"
     */
    static final String algorithm = "SHA1withRSA";
    static final String password = "foobaa";
    static final String certAlias = "poitest";
    /**
     * the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     */
    static final String certDN = "CN=poitest";
    // static final File pfxFile = TempFile.createTempFile("poitest", ".pfx");
    static byte pfxFileBytes[];
    
    static class CertData {
        KeyPair keypair;
        X509Certificate x509;
    }
    
    /** 
     * Create a self-signed X.509 Certificate
     * 
     * The keystore generation / loading is split, because normally the keystore would
     * already exist.
     */ 
    /* @BeforeClass
    public static void initKeystore() throws GeneralSecurityException, IOException {
        CertData certData = new CertData();
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        certData.keypair = keyGen.generateKeyPair();
        PrivateKey privkey = certData.keypair.getPrivate();
        PublicKey publkey = certData.keypair.getPublic();
    
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(certDN);
        
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(publkey));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
        
        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        
        // Update the algorith, and resign.
        algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        certData.x509 = cert;
        
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(null, password.toCharArray());
        keystore.setKeyEntry(certAlias, certData.keypair.getPrivate(), password.toCharArray(), new Certificate[]{certData.x509});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keystore.store(bos, password.toCharArray());
        pfxFileBytes = bos.toByteArray();
    } */

    public CertData loadKeystore()
    throws GeneralSecurityException, IOException {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        
        // InputStream fis = new ByteArrayInputStream(pfxFileBytes);
        InputStream fis = POIDataSamples.getPOIFSInstance().openResourceAsStream("poitest.pfx");
        keystore.load(fis, password.toCharArray());
        fis.close();
        
        X509Certificate x509 = (X509Certificate)keystore.getCertificate(certAlias);
        PrivateKey privateKey = (PrivateKey)keystore.getKey(certAlias, password.toCharArray());
        PublicKey publicKey = x509.getPublicKey();

        CertData certData = new CertData();
        certData.keypair = new KeyPair(publicKey, privateKey);
        certData.x509 = x509;
        
        return certData;
    }
    
    @Test
    public void testCertificateEncryption() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem();
        EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile, CipherAlgorithm.aes128, HashAlgorithm.sha1, -1, -1, ChainingMode.cbc);
        AgileEncryptionVerifier aev = (AgileEncryptionVerifier)info.getVerifier();
        CertData certData = loadKeystore();
        aev.addCertificate(certData.x509);
        
        Encryptor enc = info.getEncryptor();
        enc.confirmPassword("foobaa");
        
        File file = POIDataSamples.getDocumentInstance().getFile("VariousPictures.docx");
        InputStream fis = new FileInputStream(file);
        byte byteExpected[] = IOUtils.toByteArray(fis);
        fis.close();
        
        OutputStream os = enc.getDataStream(fs);
        IOUtils.copy(new ByteArrayInputStream(byteExpected), os);
        os.close();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fs.writeFilesystem(bos);
        bos.close();
        
        fs = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        info = new EncryptionInfo(fs);
        AgileDecryptor agDec = (AgileDecryptor)info.getDecryptor();
        boolean passed = agDec.verifyPassword(certData.keypair, certData.x509);
        assertTrue("certificate verification failed", passed);
        
        fis = agDec.getDataStream(fs);
        byte byteActual[] = IOUtils.toByteArray(fis);
        fis.close();
        
        assertThat(byteExpected, equalTo(byteActual));
    }
}
