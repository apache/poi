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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
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

import org.apache.poi.poifs.crypt.dsig.HorribleProxy;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.ASN1InputStreamIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.AuthorityInformationAccessIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.AuthorityKeyIdentifierIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.BasicConstraintsIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.BasicOCSPRespGeneratorIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.BasicOCSPRespIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.CRLNumberIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.CRLReasonIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.CertificateIDIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.CertificateStatusIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DERIA5StringIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DERSequenceIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DistributionPointIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DistributionPointNameIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.GeneralNameIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.GeneralNamesIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.KeyUsageIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPReqGeneratorIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPReqIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPRespGeneratorIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPRespIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.ReqIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.RevokedStatusIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.SubjectKeyIdentifierIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.SubjectPublicKeyInfoIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509ExtensionsIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509ObjectIdentifiersIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509PrincipalIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509V2CRLGeneratorIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509V3CertificateGeneratorIf;
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
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    private static SubjectKeyIdentifierIf createSubjectKeyId(PublicKey publicKey)
    throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException
        , IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        ASN1InputStreamIf asnObj = HorribleProxy.newProxy(ASN1InputStreamIf.class, bais);
        SubjectPublicKeyInfoIf info =
            HorribleProxy.newProxy(SubjectPublicKeyInfoIf.class, asnObj.readObject$Sequence());
        SubjectKeyIdentifierIf keyId =  HorribleProxy.newProxy(SubjectKeyIdentifierIf.class, info);
        return keyId;
    }

    private static AuthorityKeyIdentifierIf createAuthorityKeyId(PublicKey publicKey)
    throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException
        , IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        ASN1InputStreamIf asnObj = HorribleProxy.newProxy(ASN1InputStreamIf.class, bais);
        SubjectPublicKeyInfoIf info =
            HorribleProxy.newProxy(SubjectPublicKeyInfoIf.class, asnObj.readObject$Sequence());
        AuthorityKeyIdentifierIf keyId = HorribleProxy.newProxy(AuthorityKeyIdentifierIf.class, info);

        return keyId;
    }

    static X509Certificate generateCertificate(PublicKey subjectPublicKey,
            String subjectDn, Date notBefore, Date notAfter,
            X509Certificate issuerCertificate, PrivateKey issuerPrivateKey,
            boolean caFlag, int pathLength, String crlUri, String ocspUri,
            KeyUsageIf keyUsage)
    throws IOException, InvalidKeyException, IllegalStateException, NoSuchAlgorithmException
        , SignatureException, CertificateException, InvocationTargetException, IllegalAccessException
        , InstantiationException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException
    {
        String signatureAlgorithm = "SHA1withRSA";
        X509V3CertificateGeneratorIf certificateGenerator = HorribleProxy.newProxy(X509V3CertificateGeneratorIf.class);
        certificateGenerator.reset();
        certificateGenerator.setPublicKey(subjectPublicKey);
        certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);
        certificateGenerator.setNotBefore(notBefore);
        certificateGenerator.setNotAfter(notAfter);
        X509PrincipalIf subjectDN = HorribleProxy.newProxy(X509PrincipalIf.class, subjectDn);
        X509PrincipalIf issuerDN;
        if (null != issuerCertificate) {
            issuerDN = HorribleProxy.newProxy(X509PrincipalIf.class, issuerCertificate
                    .getSubjectX500Principal().toString());
        } else {
            issuerDN = subjectDN;
        }
        certificateGenerator.setIssuerDN(issuerDN);
        certificateGenerator.setSubjectDN(subjectDN);
        certificateGenerator.setSerialNumber(new BigInteger(128,
                new SecureRandom()));

        X509ExtensionsIf X509Extensions = HorribleProxy.newProxy(X509ExtensionsIf.class);
        
        certificateGenerator.addExtension(X509Extensions.SubjectKeyIdentifier(),
                false, createSubjectKeyId(subjectPublicKey));
        PublicKey issuerPublicKey;
        issuerPublicKey = subjectPublicKey;
        certificateGenerator.addExtension(
                X509Extensions.AuthorityKeyIdentifier(), false,
                createAuthorityKeyId(issuerPublicKey));

        if (caFlag) {
            BasicConstraintsIf bc;
            
            if (-1 == pathLength) {
                bc = HorribleProxy.newProxy(BasicConstraintsIf.class, true);
            } else {
                bc = HorribleProxy.newProxy(BasicConstraintsIf.class, pathLength);
            }
            certificateGenerator.addExtension(X509Extensions.BasicConstraints(), false, bc);
        }

        if (null != crlUri) {
            GeneralNameIf gn = HorribleProxy.newProxy(GeneralNameIf.class);
            int uri = gn.uniformResourceIdentifier();
            DERIA5StringIf crlUriDer = HorribleProxy.newProxy(DERIA5StringIf.class, crlUri);
            gn = HorribleProxy.newProxy(GeneralNameIf.class, uri, crlUriDer);

            DERSequenceIf gnDer = HorribleProxy.newProxy(DERSequenceIf.class, gn);
            GeneralNamesIf gns = HorribleProxy.newProxy(GeneralNamesIf.class, gnDer);
            
            DistributionPointNameIf dpn = HorribleProxy.newProxy(DistributionPointNameIf.class, 0, gns);
            DistributionPointIf distp = HorribleProxy.newProxy(DistributionPointIf.class, dpn, null, null);
            DERSequenceIf distpDer = HorribleProxy.newProxy(DERSequenceIf.class, distp);
            certificateGenerator.addExtension(X509Extensions.CRLDistributionPoints(), false, distpDer);
        }

        if (null != ocspUri) {
            GeneralNameIf ocspName = HorribleProxy.newProxy(GeneralNameIf.class);
            int uri = ocspName.uniformResourceIdentifier();
            ocspName = HorribleProxy.newProxy(GeneralNameIf.class, uri, ocspUri);
            
            X509ObjectIdentifiersIf X509ObjectIdentifiers = HorribleProxy.newProxy(X509ObjectIdentifiersIf.class);
            AuthorityInformationAccessIf authorityInformationAccess =
                HorribleProxy.newProxy(AuthorityInformationAccessIf.class
                    , X509ObjectIdentifiers.ocspAccessMethod(), ocspName);
            
            certificateGenerator.addExtension(
                    X509Extensions.AuthorityInfoAccess(), false,
                    authorityInformationAccess);
        }

        if (null != keyUsage) {
            certificateGenerator.addExtension(X509Extensions.KeyUsage(), true, keyUsage);
        }

        X509Certificate certificate;
        certificate = certificateGenerator.generate(issuerPrivateKey);

        /*
         * Next certificate factory trick is needed to make sure that the
         * certificate delivered to the caller is provided by the default
         * security provider instead of BouncyCastle. If we don't do this trick
         * we might run into trouble when trying to use the CertPath validator.
         */
        CertificateFactory certificateFactory = CertificateFactory
                .getInstance("X.509");
        certificate = (X509Certificate) certificateFactory
                .generateCertificate(new ByteArrayInputStream(certificate
                        .getEncoded()));
        return certificate;
    }

    static Document loadDocument(InputStream documentInputStream)
            throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(documentInputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);
        return document;
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

    public static X509CRL generateCrl(X509Certificate issuer,
            PrivateKey issuerPrivateKey) throws InvalidKeyException,
            CRLException, IllegalStateException, NoSuchAlgorithmException,
            SignatureException, InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        X509V2CRLGeneratorIf crlGenerator = HorribleProxy.newProxy(X509V2CRLGeneratorIf.class);
        crlGenerator.setIssuerDN(issuer.getSubjectX500Principal());
        Date now = new Date();
        crlGenerator.setThisUpdate(now);
        crlGenerator.setNextUpdate(new Date(now.getTime() + 100000));
        crlGenerator.setSignatureAlgorithm("SHA1withRSA");

        X509ExtensionsIf X509Extensions = HorribleProxy.newProxy(X509ExtensionsIf.class);
        CRLNumberIf crlNumber = HorribleProxy.newProxy(CRLNumberIf.class, new BigInteger("1234"));
        
        crlGenerator.addExtension(X509Extensions.CRLNumber(), false, crlNumber);
        X509CRL x509Crl = crlGenerator.generate(issuerPrivateKey);
        return x509Crl;
    }

    public static OCSPRespIf createOcspResp(X509Certificate certificate,
            boolean revoked, X509Certificate issuerCertificate,
            X509Certificate ocspResponderCertificate,
            PrivateKey ocspResponderPrivateKey, String signatureAlgorithm)
            throws Exception {
        // request
        OCSPReqGeneratorIf ocspReqGenerator = HorribleProxy.newProxy(OCSPReqGeneratorIf.class);
        CertificateIDIf certId = HorribleProxy.newProxy(CertificateIDIf.class);
        certId = HorribleProxy.newProxy(CertificateIDIf.class, certId.HASH_SHA1(),
                issuerCertificate, certificate.getSerialNumber());
        ocspReqGenerator.addRequest(certId);
        OCSPReqIf ocspReq = ocspReqGenerator.generate();

        BasicOCSPRespGeneratorIf basicOCSPRespGenerator = 
            HorribleProxy.newProxy(BasicOCSPRespGeneratorIf.class, ocspResponderCertificate.getPublicKey());

        // request processing
        ReqIf[] requestList = ocspReq.getRequestList();
        for (ReqIf ocspRequest : requestList) {
            CertificateIDIf certificateID = ocspRequest.getCertID();
            CertificateStatusIf certificateStatus;
            if (revoked) {
                CRLReasonIf crlr = HorribleProxy.newProxy(CRLReasonIf.class);
                RevokedStatusIf rs = HorribleProxy.newProxy(RevokedStatusIf.class, new Date(), crlr.unspecified());
                certificateStatus = HorribleProxy.newProxy(CertificateStatusIf.class, rs.getDelegate());
            } else {
                CertificateStatusIf cs = HorribleProxy.newProxy(CertificateStatusIf.class);
                certificateStatus = cs.GOOD();
            }
            basicOCSPRespGenerator
                    .addResponse(certificateID, certificateStatus);
        }

        // basic response generation
        X509Certificate[] chain = null;
        if (!ocspResponderCertificate.equals(issuerCertificate)) {
            chain = new X509Certificate[] { ocspResponderCertificate,
                    issuerCertificate };
        }

        BasicOCSPRespIf basicOCSPResp = basicOCSPRespGenerator.generate(
                signatureAlgorithm, ocspResponderPrivateKey, chain, new Date(),
                "BC");

        // response generation
        OCSPRespGeneratorIf ocspRespGenerator = HorribleProxy.newProxy(OCSPRespGeneratorIf.class);
        OCSPRespIf ocspResp = ocspRespGenerator.generate(
                ocspRespGenerator.SUCCESSFUL(), basicOCSPResp);

        return ocspResp;
    }
}
