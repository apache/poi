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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Date;

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

import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PkiTestUtils {

    private PkiTestUtils() {
        super();
    }

    static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(1024,
                RSAKeyGenParameterSpec.F4), random);
        return keyPairGenerator.generateKeyPair();
    }

    static X509Certificate generateCertificate(PublicKey subjectPublicKey,
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

    static Document loadDocument(InputStream documentInputStream)
            throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(documentInputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        return documentBuilder.parse(inputSource);
    }

    static String toString(Node dom) throws TransformerException {
        Source source = new DOMSource(dom);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        /*
         * We have to omit the ?xml declaration if we want to embed the
         * document.
         */
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();
    }

    public static X509CRL generateCrl(X509Certificate issuer, PrivateKey issuerPrivateKey)
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

    public static OCSPResp createOcspResp(X509Certificate certificate,
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
