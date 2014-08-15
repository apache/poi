package org.apache.poi.poifs.crypt.dsig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;

import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.poi.poifs.crypt.dsig.HorribleProxy.ProxyIf;
import org.w3c.dom.Node;

public interface HorribleProxies {
    public static final String xmlSecBase = "org.jcp.xml.dsig.internal.dom";
    // public static final String xmlSecBase = "org.apache.jcp.xml.dsig.internal.dom";
    
    public interface ASN1InputStreamIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.ASN1InputStream";
        
        ASN1OctetStringIf readObject$ASNString() throws IOException;
        DEROctetStringIf readObject$DERString() throws IOException;
        DERIntegerIf readObject$Integer() throws IOException;
        ASN1SequenceIf readObject$Sequence() throws IOException;
        Object readObject$Object() throws IOException;
    }

    public interface ASN1ObjectIdentifierIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.ASN1ObjectIdentifier";
    }
    
    public interface ASN1OctetStringIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.ASN1OctetString";
        byte[] getOctets();
    }
    
    public interface ASN1SequenceIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.ASN1Sequence";
    }
    
    public interface AuthorityInformationAccessIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.AuthorityInformationAccess";
    }
    
    public interface AuthorityKeyIdentifierIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.AuthorityKeyIdentifier";
        byte[] getKeyIdentifier();
    }
    
    public interface BasicConstraintsIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.BasicConstraints";
    }
    
    public interface BasicOCSPRespIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.BasicOCSPResp";
        Date getProducedAt();
        RespIDIf getResponderId();
    }
    
    public interface BcDigestCalculatorProviderIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.operator.bc.BcDigestCalculatorProvider";
    }

    public interface BcRSASignerInfoVerifierBuilderIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder";
        SignerInformationVerifierIf build(X509CertificateHolderIf holder); 
    }
    
    public interface CanonicalizerIf extends ProxyIf {
        String delegateClass = "com.sun.org.apache.xml.internal.security.c14n.Canonicalizer";
        byte[] canonicalizeSubtree(Node node) throws Exception;
    }
    
    public interface CRLNumberIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.CRLNumber";
    }
    
    public interface DefaultDigestAlgorithmIdentifierFinderIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder";
    }
    
    public interface DistributionPointNameIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.DistributionPointName";
    }
    
    public interface DistributionPointIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.DistributionPoint";
    }
    
    public interface DERIA5StringIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.DERIA5String";
    }
    
    public interface DERIntegerIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.DERInteger";
        BigInteger getPositiveValue();
    }
    
    public interface DEROctetStringIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.DEROctetString";
        byte[] getOctets();
    }
    
    public interface DERTaggedObjectIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.DERTaggedObject";
        int getTagNo();
        ASN1OctetStringIf getObject$String();
        Object getObject$Object();
    }

    public interface DERSequenceIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.DERSequence";
    }
    
    public interface DOMKeyInfoIf extends ProxyIf {
        String delegateClass = xmlSecBase+".DOMKeyInfo";
        void marshal(Node parent, Node nextSibling, String dsPrefix, DOMCryptoContext context) throws MarshalException;
    }
    
    public interface DOMReferenceIf extends ProxyIf {
        String delegateClass = xmlSecBase+".DOMReference";
        void digest(XMLSignContext paramXMLSignContext) throws XMLSignatureException;
        byte[] getDigestValue();
    }
    
    public interface DOMSignedInfoIf extends ProxyIf {
        String delegateClass = xmlSecBase+".DOMSignedInfo";
        void canonicalize(XMLCryptoContext paramXMLCryptoContext, ByteArrayOutputStream paramByteArrayOutputStream);
    }
    
    public interface XMLSignatureIf extends ProxyIf {
        String delegateClass = "com.sun.org.apache.xml.internal.security.signature.XMLSignature";
        String ALGO_ID_SIGNATURE_RSA_SHA1();
        String ALGO_ID_SIGNATURE_RSA_SHA256();
        String ALGO_ID_SIGNATURE_RSA_SHA384();
        String ALGO_ID_SIGNATURE_RSA_SHA512();
        String ALGO_ID_MAC_HMAC_RIPEMD160();
    }
    
    public interface DOMXMLSignatureIf extends ProxyIf {
        String delegateClass = xmlSecBase+".DOMXMLSignature";
        void marshal(Node node, String prefix, DOMCryptoContext context) throws MarshalException;
    }
    
    public interface GeneralNameIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.GeneralName";
        
        int uniformResourceIdentifier();
        
    }
    
    public interface GeneralNamesIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.GeneralNames";
    }
    
    public interface InitIf extends ProxyIf {
        String delegateClass = "com.sun.org.apache.xml.internal.security.Init";
        void init();
    }

    public interface KeyUsageIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.KeyUsage";
        int digitalSignature();
    }
    
    public interface OCSPRespIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.OCSPResp";
        BasicOCSPRespIf getResponseObject();
        byte[] getEncoded() throws IOException;
    }
    
    public interface PKIFailureInfoIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.cmp.PKIFailureInfo";
        int intValue();
    }

    public interface RespIDIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.RespID";
        ResponderIDIf toASN1Object();
    }
    
    public interface ResponderIDIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.ocsp.ResponderID";
        DERTaggedObjectIf toASN1Object();
    }

    public interface SignerIdIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.cms.SignerId";
        BigInteger getSerialNumber();
        X500Principal getIssuer();
    }

    public interface SignerInformationVerifierIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.cms.SignerInformationVerifier";
    }
    
    public interface StoreIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.util.Store";
        Collection<Certificate> getMatches(Object selector) throws Exception;
    }
    
    public interface SubjectKeyIdentifierIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.SubjectKeyIdentifier";
        byte[] getKeyIdentifier();
    }
    
    public interface SubjectPublicKeyInfoIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.SubjectPublicKeyInfo";
    }
    
    public interface TimeStampRequestGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.tsp.TimeStampRequestGenerator";
        void setCertReq(boolean certReq);
        void setReqPolicy(String reqPolicy);
        TimeStampRequestIf generate(String igestAlgorithmOID, byte[] digest, BigInteger nonce);
    }
    
    public interface TimeStampRequestIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.tsp.TimeStampRequest";
        byte[] getEncoded() throws IOException;
    }
    
    public interface TimeStampResponseIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.tsp.TimeStampResponse";
        void validate(TimeStampRequestIf request) throws Exception;
        int getStatus();
        String getStatusString();
        PKIFailureInfoIf getFailInfo();
        TimeStampTokenIf getTimeStampToken();
    }
    
    public interface TimeStampTokenIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.tsp.TimeStampToken";
        SignerIdIf getSID();
        StoreIf getCertificates();
        StoreIf getCRLs();
        TimeStampTokenInfoIf getTimeStampInfo();
        byte[] getEncoded() throws IOException;
        void validate(SignerInformationVerifierIf verifier) throws Exception;
    }
    
    public interface TimeStampTokenInfoIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.tsp.TimeStampTokenInfo";
        Date getGenTime();
    }
    
    public interface X509CertificateHolderIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.cert.X509CertificateHolder";
    }

    public interface X509NameIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.X509Name";
        String toString$delegate();
    }

    public interface X509PrincipalIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.jce.X509Principal";
        String getName();
    }
    
    public interface X509V3CertificateGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.x509.X509V3CertificateGenerator";
        
        void reset();
        void setPublicKey(PublicKey key);
        void setSignatureAlgorithm(String signatureAlgorithm);
        void setNotBefore(Date date);
        void setNotAfter(Date date);
        void setIssuerDN(X509PrincipalIf issuerDN);
        void setSubjectDN(X509PrincipalIf issuerDN);
        void setSerialNumber(BigInteger serialNumber);
        
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, SubjectKeyIdentifierIf value);
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, AuthorityKeyIdentifierIf value);
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, BasicConstraintsIf value);
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, DERSequenceIf value);
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, AuthorityInformationAccessIf value);
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, KeyUsageIf value);
        
        X509Certificate generate(PrivateKey issuerPrivateKey);
    }

    public interface OCSPReqIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.OCSPReq";

        ReqIf[] getRequestList();
    }
    
    public interface OCSPReqGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.OCSPReqGenerator";
        
        void addRequest(CertificateIDIf certId);
        OCSPReqIf generate();
    }

    public interface BasicOCSPRespGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.BasicOCSPRespGenerator";

        void addResponse(CertificateIDIf certificateID, CertificateStatusIf certificateStatus);
        BasicOCSPRespIf generate(String signatureAlgorithm, PrivateKey ocspResponderPrivateKey,
                X509Certificate chain[], Date date, String provider);
    }
    
    public interface CertificateIDIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.CertificateID";
        
        String HASH_SHA1();
    }
    
    public interface X509ExtensionsIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.X509Extensions";
        
        ASN1ObjectIdentifierIf AuthorityKeyIdentifier();
        ASN1ObjectIdentifierIf SubjectKeyIdentifier();
        ASN1ObjectIdentifierIf BasicConstraints();
        ASN1ObjectIdentifierIf CRLDistributionPoints();
        ASN1ObjectIdentifierIf AuthorityInfoAccess();
        ASN1ObjectIdentifierIf KeyUsage();
        ASN1ObjectIdentifierIf CRLNumber();
    }
    
    public interface X509ObjectIdentifiersIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.X509ObjectIdentifiers";
        
        ASN1ObjectIdentifierIf ocspAccessMethod();
    }
    
    public interface X509V2CRLGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.x509.X509V2CRLGenerator";
        
        void setIssuerDN(X500Principal issuerDN);
        void setThisUpdate(Date date);
        void setNextUpdate(Date date);
        void setSignatureAlgorithm(String algorithm);
        
        void addExtension(ASN1ObjectIdentifierIf oid, boolean critical, CRLNumberIf value);
        X509CRL generate(PrivateKey privateKey);
    }
    
    public interface ReqIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.Req";
        
        CertificateIDIf getCertID();
    }
    
    public interface CertificateStatusIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.CertificateStatus";
        
        CertificateStatusIf GOOD();
    }
    
    public interface RevokedStatusIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.RevokedStatus";
    }
    
    public interface CRLReasonIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.asn1.x509.CRLReason";
        int unspecified();
    }

    public interface OCSPRespGeneratorIf extends ProxyIf {
        String delegateClass = "org.bouncycastle.ocsp.OCSPRespGenerator";
        int SUCCESSFUL();
        OCSPRespIf generate(int status, BasicOCSPRespIf basicOCSPResp);
    }
}
