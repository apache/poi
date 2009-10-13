
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PkiTestUtils {

    public static final byte[] SHA1_DIGEST_INFO_PREFIX = new byte[] { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x04, 0x14 };

    private PkiTestUtils() {
        super();
    }

    static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4), random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(bais).readObject());
        return new SubjectKeyIdentifier(info);
    }

    private static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(bais).readObject());

        return new AuthorityKeyIdentifier(info);
    }

    static X509Certificate generateCertificate(PublicKey subjectPublicKey, String subjectDn, DateTime notBefore, DateTime notAfter,
                                    X509Certificate issuerCertificate, PrivateKey issuerPrivateKey, boolean caFlag, int pathLength, String crlUri,
                                    String ocspUri, KeyUsage keyUsage) throws IOException, InvalidKeyException, IllegalStateException,
                                    NoSuchAlgorithmException, SignatureException, CertificateException {
        String signatureAlgorithm = "SHA1withRSA";
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        certificateGenerator.reset();
        certificateGenerator.setPublicKey(subjectPublicKey);
        certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);
        certificateGenerator.setNotBefore(notBefore.toDate());
        certificateGenerator.setNotAfter(notAfter.toDate());
        X509Principal issuerDN;
        if (null != issuerCertificate) {
            issuerDN = new X509Principal(issuerCertificate.getSubjectX500Principal().toString());
        } else {
            issuerDN = new X509Principal(subjectDn);
        }
        certificateGenerator.setIssuerDN(issuerDN);
        certificateGenerator.setSubjectDN(new X509Principal(subjectDn));
        certificateGenerator.setSerialNumber(new BigInteger(128, new SecureRandom()));

        certificateGenerator.addExtension(X509Extensions.SubjectKeyIdentifier, false, createSubjectKeyId(subjectPublicKey));
        PublicKey issuerPublicKey;
        issuerPublicKey = subjectPublicKey;
        certificateGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier, false, createAuthorityKeyId(issuerPublicKey));

        if (caFlag) {
            if (-1 == pathLength) {
                certificateGenerator.addExtension(X509Extensions.BasicConstraints, false, new BasicConstraints(true));
            } else {
                certificateGenerator.addExtension(X509Extensions.BasicConstraints, false, new BasicConstraints(pathLength));
            }
        }

        if (null != crlUri) {
            GeneralName gn = new GeneralName(GeneralName.uniformResourceIdentifier, new DERIA5String(crlUri));
            GeneralNames gns = new GeneralNames(new DERSequence(gn));
            DistributionPointName dpn = new DistributionPointName(0, gns);
            DistributionPoint distp = new DistributionPoint(dpn, null, null);
            certificateGenerator.addExtension(X509Extensions.CRLDistributionPoints, false, new DERSequence(distp));
        }

        if (null != ocspUri) {
            GeneralName ocspName = new GeneralName(GeneralName.uniformResourceIdentifier, ocspUri);
            AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess(X509ObjectIdentifiers.ocspAccessMethod, ocspName);
            certificateGenerator.addExtension(X509Extensions.AuthorityInfoAccess.getId(), false, authorityInformationAccess);
        }

        if (null != keyUsage) {
            certificateGenerator.addExtension(X509Extensions.KeyUsage, true, keyUsage);
        }

        X509Certificate certificate;
        certificate = certificateGenerator.generate(issuerPrivateKey);

        /*
         * Next certificate factory trick is needed to make sure that the
         * certificate delivered to the caller is provided by the default
         * security provider instead of BouncyCastle. If we don't do this trick
         * we might run into trouble when trying to use the CertPath validator.
         */
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
        return certificate;
    }

    static Document loadDocument(InputStream documentInputStream) throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(documentInputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);
        return document;
    }

    static String toString(Node dom) throws TransformerException {
        Source source = new DOMSource(dom);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        /*
         * We have to omit the ?xml declaration if we want to embed the
         * document.
         */
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();
    }
}
