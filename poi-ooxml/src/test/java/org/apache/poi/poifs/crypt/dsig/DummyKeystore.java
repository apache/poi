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
package org.apache.poi.poifs.crypt.dsig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RandomSingleton;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
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
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class DummyKeystore {
    public static class KeyCertPair {
        private final PrivateKey key;
        private final List<X509Certificate> x509chain;

        public KeyCertPair(PrivateKey key, Certificate[] x509chain) {
            this.key = key;
            this.x509chain = Stream.of(x509chain).map(X509Certificate.class::cast).collect(Collectors.toList());
        }

        public PrivateKey getKey() {
            return key;
        }

        public X509Certificate getX509() {
            return x509chain.get(0);
        }

        public List<X509Certificate> getX509Chain() {
            return x509chain;
        }
    }

    private static final String DUMMY_ALIAS = "Test";
    private static final String DUMMY_PASS = "test";

    private final KeyStore keystore;

    public DummyKeystore(String storePass) throws GeneralSecurityException, IOException {
        this((File)null, storePass);
    }

    public DummyKeystore(File storeFile, String storePass) throws GeneralSecurityException, IOException {
        CryptoFunctions.registerBouncyCastle();
        keystore = KeyStore.getInstance("PKCS12");
        try (InputStream fis = storeFile != null && storeFile.exists() ? new FileInputStream(storeFile) : null) {
            keystore.load(fis, storePass.toCharArray());
        }
    }

    public DummyKeystore(String pfxInput, String storePass) throws GeneralSecurityException, IOException {
        CryptoFunctions.registerBouncyCastle();
        keystore = KeyStore.getInstance("PKCS12");
        try (InputStream fis = UnsynchronizedByteArrayInputStream.builder().setByteArray(RawDataUtil.decompress(pfxInput)).get()) {
            keystore.load(fis, storePass.toCharArray());
        }
    }

    /**
     * Create dummy key
     * @return the alias of the dummy key
     */
    public KeyCertPair createDummyKey() throws GeneralSecurityException, IOException, OperatorCreationException {
        return addEntry(DUMMY_ALIAS, DUMMY_PASS, 2048, 24);
    }

    public KeyCertPair addEntryFromPEM(File pemFile, String keyPass) throws IOException, CertificateException, KeyStoreException {
        PrivateKey key = null;
        X509Certificate x509 = null;

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

        if (key == null || x509 == null) {
            throw new IOException("Please add private key and certificate in the PEM file.");
        }

        String alias = x509.getSubjectX500Principal().getName();
        keystore.setKeyEntry(alias, key, keyPass.toCharArray(), new Certificate[]{x509});

        return new KeyCertPair(key, new Certificate[]{x509});
    }


    /**
     * Add an entry with password, keySize and expiry values. Ignore if alias is already in keystore
     * @param keySize multiple of 1024, e.g. 1024, 2048
     */
    public KeyCertPair addEntry(String keyAlias, String keyPass, int keySize, int expiryInMonths) throws GeneralSecurityException, IOException, OperatorCreationException {
        if (!keystore.isKeyEntry(keyAlias)) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4), RandomSingleton.getInstance());
            KeyPair pair = keyPairGenerator.generateKeyPair();

            Date notBefore = new Date();
            Calendar cal = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
            cal.add(Calendar.MONTH, expiryInMonths);
            Date notAfter = cal.getTime();
            KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature);

            X509Certificate x509 = generateCertificate(pair.getPublic(), notBefore, notAfter, pair.getPrivate(), keyUsage);
            keystore.setKeyEntry(keyAlias, pair.getPrivate(), keyPass.toCharArray(), new Certificate[]{x509});
            return new KeyCertPair(pair.getPrivate(), new X509Certificate[]{x509});
        } else {
            return new KeyCertPair(getKey(keyAlias, keyPass), keystore.getCertificateChain(keyAlias));
        }
    }

    public KeyCertPair getKeyPair(String keyAlias, String keyPass) throws GeneralSecurityException {
        return new KeyCertPair(getKey(keyAlias, keyPass), keystore.getCertificateChain(keyAlias));
    }

    public KeyCertPair getKeyPair(int index, String keyPass) throws GeneralSecurityException {
        Map.Entry<String, PrivateKey> me = getKeyByIndex(index, keyPass);
        return me != null ?  getKeyPair(me.getKey(), keyPass) : null;
    }

    public PrivateKey getKey(String keyAlias, String keyPass) throws GeneralSecurityException {
        return (PrivateKey)keystore.getKey(keyAlias, keyPass.toCharArray());
    }

    public PrivateKey getKey(int index, String keyPass) throws GeneralSecurityException {
        Map.Entry<String, PrivateKey> me = getKeyByIndex(index, keyPass);
        return me != null ?  me.getValue() : null;
    }

    public X509Certificate getFirstX509(String alias) throws KeyStoreException {
        return (X509Certificate)keystore.getCertificate(alias);
    }

    private Map.Entry<String,PrivateKey> getKeyByIndex(int index, String keyPass) throws GeneralSecurityException {
        for (String a : Collections.list(keystore.aliases())) {
            try {
                PrivateKey pk = (PrivateKey) keystore.getKey(a, keyPass.toCharArray());
                if (pk != null) {
                    return new AbstractMap.SimpleEntry<>(a, pk);
                }
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
                break;
            }
        }
        return null;
    }

    public void save(File storeFile, String storePass) throws IOException, GeneralSecurityException {
        try (FileOutputStream fos = new FileOutputStream(storeFile)) {
            keystore.store(fos, storePass.toCharArray());
        }
    }

    public X509CRL generateCrl(KeyCertPair certPair)
        throws GeneralSecurityException, IOException, OperatorCreationException {

        PrivateKey issuerPrivateKey = certPair.getKey();
        X509Certificate issuer = certPair.getX509();

        X509CertificateHolder holder = new X509CertificateHolder(issuer.getEncoded());
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(holder.getIssuer(), new Date());
        crlBuilder.setNextUpdate(new Date(new Date().getTime() + 100000));
        JcaContentSignerBuilder contentBuilder = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC");

        CRLNumber crlNumber = new CRLNumber(new BigInteger("1234"));

        crlBuilder.addExtension(Extension.cRLNumber, false, crlNumber);
        X509CRLHolder x509Crl = crlBuilder.build(contentBuilder.build(issuerPrivateKey));
        return new JcaX509CRLConverter().setProvider("BC").getCRL(x509Crl);
    }



    private static X509Certificate generateCertificate(PublicKey subjectPublicKey,
        Date notBefore, Date notAfter,
        PrivateKey issuerPrivateKey,
        KeyUsage keyUsage)
        throws IOException, OperatorCreationException, CertificateException {
        final String signatureAlgorithm = "SHA1withRSA";
        final String subjectDn = "CN=Test";
        X500Name issuerName = new X500Name(subjectDn);

        RSAPublicKey rsaPubKey = (RSAPublicKey)subjectPublicKey;
        RSAKeyParameters rsaSpec = new RSAKeyParameters(false, rsaPubKey.getModulus(), rsaPubKey.getPublicExponent());

        SubjectPublicKeyInfo subjectPublicKeyInfo =
            SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(rsaSpec);

        DigestCalculator digestCalc = new JcaDigestCalculatorProviderBuilder()
            .setProvider("BC").build().get(CertificateID.HASH_SHA1);

        X509v3CertificateBuilder certificateGenerator = new X509v3CertificateBuilder(
            issuerName
            , new BigInteger(128, RandomSingleton.getInstance())
            , notBefore
            , notAfter
            , new X500Name(subjectDn)
            , subjectPublicKeyInfo
        );

        X509ExtensionUtils exUtils = new X509ExtensionUtils(digestCalc);
        SubjectKeyIdentifier subKeyId = exUtils.createSubjectKeyIdentifier(subjectPublicKeyInfo);
        AuthorityKeyIdentifier autKeyId = exUtils.createAuthorityKeyIdentifier(subjectPublicKeyInfo);

        certificateGenerator.addExtension(Extension.subjectKeyIdentifier, false, subKeyId);
        certificateGenerator.addExtension(Extension.authorityKeyIdentifier, false, autKeyId);

        BasicConstraints bc = new BasicConstraints(0);
        certificateGenerator.addExtension(Extension.basicConstraints, false, bc);

        if (null != keyUsage) {
            certificateGenerator.addExtension(Extension.keyUsage, true, keyUsage);
        }

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
        signerBuilder.setProvider("BC");

        X509CertificateHolder certHolder =
            certificateGenerator.build(signerBuilder.build(issuerPrivateKey));

        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

    public OCSPResp createOcspResp(KeyCertPair certPair, long nonceTimeinMillis)
    throws OperatorCreationException, OCSPException, CertificateEncodingException, IOException {
        X509Certificate certificate = certPair.getX509();
        X509Certificate issuerCertificate = certPair.getX509();
        X509Certificate ocspResponderCertificate = certPair.getX509();
        PrivateKey ocspResponderPrivateKey = certPair.getKey();


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

    public void importX509(File file) throws CertificateException, KeyStoreException, IOException {
        try (InputStream is = new FileInputStream(file)) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
            keystore.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        }
    }

    public void importKeystore(File file, String storePass, String keyPass, Function<String,String> otherKeyPass) throws GeneralSecurityException, IOException {
        DummyKeystore dk = new DummyKeystore(file, storePass);

        Map<String,X509Certificate> myCerts = new HashMap<>();
        for (String a : Collections.list(keystore.aliases())) {
            Certificate[] chain = keystore.getCertificateChain(a);
            if (chain == null) {
                Certificate cert = keystore.getCertificate(a);
                if (cert == null) {
                    continue;
                }
                chain = new Certificate[]{cert};
            }
            Arrays.stream(chain)
                .map(X509Certificate.class::cast)
                .filter(c -> !myCerts.containsKey(c.getSubjectX500Principal().getName()))
                .forEach(c -> myCerts.put(c.getSubjectX500Principal().getName(), c));
        }

        for (String a : Collections.list(dk.keystore.aliases())) {
            KeyCertPair keyPair = dk.getKeyPair(a, otherKeyPass.apply(a));
            ArrayList<X509Certificate> chain = new ArrayList<>(keyPair.getX509Chain());
            Set<String> names = chain.stream().map(X509Certificate::getSubjectX500Principal).map(X500Principal::getName).collect(Collectors.toSet());
            X509Certificate last = chain.get(chain.size() - 1);
            do {
                String issuer = last.getIssuerX500Principal().getName();
                X509Certificate parent = myCerts.get(issuer);
                if (names.contains(issuer) || parent == null) {
                    break;
                } else {
                    chain.add(parent);
                    names.add(issuer);
                }
                last = parent;
            } while (true);

            keystore.setKeyEntry(a, keyPair.getKey(), keyPass.toCharArray(), chain.toArray(new X509Certificate[0]));
        }
    }
}
