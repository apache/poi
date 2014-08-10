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

package org.apache.poi.poifs.crypt.dsig.facets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.namespace.QName;

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.HorribleProxy;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.ASN1InputStreamIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.ASN1OctetStringIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.BasicOCSPRespIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.CanonicalizerIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DERIntegerIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DERTaggedObjectIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.InitIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.OCSPRespIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.RespIDIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.ResponderIDIf;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.X509NameIf;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.etsi.uri.x01903.v13.CRLIdentifierType;
import org.etsi.uri.x01903.v13.CRLRefType;
import org.etsi.uri.x01903.v13.CRLRefsType;
import org.etsi.uri.x01903.v13.CRLValuesType;
import org.etsi.uri.x01903.v13.CertIDListType;
import org.etsi.uri.x01903.v13.CertIDType;
import org.etsi.uri.x01903.v13.CertificateValuesType;
import org.etsi.uri.x01903.v13.CompleteCertificateRefsType;
import org.etsi.uri.x01903.v13.CompleteRevocationRefsType;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.EncapsulatedPKIDataType;
import org.etsi.uri.x01903.v13.OCSPIdentifierType;
import org.etsi.uri.x01903.v13.OCSPRefType;
import org.etsi.uri.x01903.v13.OCSPRefsType;
import org.etsi.uri.x01903.v13.OCSPValuesType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.etsi.uri.x01903.v13.ResponderIDType;
import org.etsi.uri.x01903.v13.RevocationValuesType;
import org.etsi.uri.x01903.v13.UnsignedPropertiesType;
import org.etsi.uri.x01903.v13.UnsignedSignaturePropertiesType;
import org.etsi.uri.x01903.v13.XAdESTimeStampType;
import org.etsi.uri.x01903.v14.ValidationDataType;
import org.w3.x2000.x09.xmldsig.CanonicalizationMethodType;
import org.w3.x2000.x09.xmldsig.ObjectType;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3.x2000.x09.xmldsig.SignatureValueType;
import org.w3c.dom.Node;

/**
 * XAdES-X-L v1.4.1 signature facet. This signature facet implementation will
 * upgrade a given XAdES-BES/EPES signature to XAdES-X-L.
 * 
 * We don't inherit from XAdESSignatureFacet as we also want to be able to use
 * this facet out of the context of a signature creation. This signature facet
 * assumes that the signature is already XAdES-BES/EPES compliant.
 * 
 * This implementation has been tested against an implementation that
 * participated multiple ETSI XAdES plugtests.
 * 
 * @author Frank Cornelis
 * @see XAdESSignatureFacet
 */
public class XAdESXLSignatureFacet implements SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(XAdESXLSignatureFacet.class);

    public static final String XADES_NAMESPACE = "http://uri.etsi.org/01903/v1.3.2#";

    public static final String XADES141_NAMESPACE = "http://uri.etsi.org/01903/v1.4.1#";

    private final TimeStampService timeStampService;

    private String c14nAlgoId;

    private final RevocationDataService revocationDataService;

    private final CertificateFactory certificateFactory;

    private final HashAlgorithm hashAlgo;

    static {
        try {
            HorribleProxy.createProxy(InitIf.class, "init");
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize JDK xml signature classes - feature unsupported by the this JDK?!", e);
        }
    }

    /**
     * Convenience constructor.
     * 
     * @param timeStampService
     *            the time-stamp service used for XAdES-T and XAdES-X.
     * @param revocationDataService
     *            the optional revocation data service used for XAdES-C and
     *            XAdES-X-L. When <code>null</code> the signature will be
     *            limited to XAdES-T only.
     */
    public XAdESXLSignatureFacet(TimeStampService timeStampService,
            RevocationDataService revocationDataService) {
        this(timeStampService, revocationDataService, HashAlgorithm.sha1);
    }

    /**
     * Main constructor.
     * 
     * @param timeStampService
     *            the time-stamp service used for XAdES-T and XAdES-X.
     * @param revocationDataService
     *            the optional revocation data service used for XAdES-C and
     *            XAdES-X-L. When <code>null</code> the signature will be
     *            limited to XAdES-T only.
     * @param digestAlgorithm
     *            the digest algorithm to be used for construction of the
     *            XAdES-X-L elements.
     */
    public XAdESXLSignatureFacet(TimeStampService timeStampService,
            RevocationDataService revocationDataService,
            HashAlgorithm digestAlgorithm) {
        this.c14nAlgoId = CanonicalizationMethod.EXCLUSIVE;
        this.hashAlgo = digestAlgorithm;
        this.timeStampService = timeStampService;
        this.revocationDataService = revocationDataService;

        try {
            this.certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("X509 JCA error: " + e.getMessage(), e);
        }
    }

    public void setCanonicalizerAlgorithm(String c14nAlgoId) {
        this.c14nAlgoId = c14nAlgoId;
    }

    public void postSign(SignatureType signatureElement,
            List<X509Certificate> signingCertificateChain) {
        LOG.log(POILogger.DEBUG, "XAdES-X-L post sign phase");

        QualifyingPropertiesType qualProps = null;
        
        try {
            // check for XAdES-BES
            for (ObjectType ot : signatureElement.getObjectList()) {
                XmlObject xo[] = ot.selectChildren(new QName(XADES_NAMESPACE, "QualifyingProperties"));
                if (xo != null && xo.length > 0) {
                    qualProps = QualifyingPropertiesType.Factory.parse(xo[0].getDomNode());
                    break;
                }
            }
        } catch (XmlException e) {
            throw new RuntimeException("signature decoding error", e);
        }
        
        if (qualProps == null) {
            throw new IllegalArgumentException("no XAdES-BES extension present");
        }

        // create basic XML container structure
        UnsignedPropertiesType unsignedProps = qualProps.getUnsignedProperties();
        if (unsignedProps == null) {
            unsignedProps = qualProps.addNewUnsignedProperties();
        }
        UnsignedSignaturePropertiesType unsignedSigProps = unsignedProps.getUnsignedSignatureProperties();
        if (unsignedSigProps == null) {
            unsignedSigProps = unsignedProps.addNewUnsignedSignatureProperties();
        }
        

        // create the XAdES-T time-stamp
        SignatureValueType svt = signatureElement.getSignatureValue();
        
        RevocationData tsaRevocationDataXadesT = new RevocationData();
        LOG.log(POILogger.DEBUG, "creating XAdES-T time-stamp");
        XAdESTimeStampType signatureTimeStamp = createXAdESTimeStamp(
                Collections.singletonList(svt.getDomNode()),
                tsaRevocationDataXadesT, this.c14nAlgoId,
                this.timeStampService);

        // marshal the XAdES-T extension
        unsignedSigProps.addNewSignatureTimeStamp().set(signatureTimeStamp);

        // xadesv141::TimeStampValidationData
        if (tsaRevocationDataXadesT.hasRevocationDataEntries()) {
            ValidationDataType validationData = createValidationData(tsaRevocationDataXadesT);
            SignatureInfo.insertXChild(unsignedSigProps, validationData);
        }

        if (null == this.revocationDataService) {
            /*
             * Without revocation data service we cannot construct the XAdES-C
             * extension.
             */
            return;
        }

        // XAdES-C: complete certificate refs
        CompleteCertificateRefsType completeCertificateRefs = 
            unsignedSigProps.addNewCompleteCertificateRefs();

        CertIDListType certIdList = completeCertificateRefs.addNewCertRefs();
        for (int certIdx = 1; certIdx < signingCertificateChain.size(); certIdx++) {
            /*
             * We skip the signing certificate itself according to section
             * 4.4.3.2 of the XAdES 1.4.1 specification.
             */
            X509Certificate certificate = signingCertificateChain.get(certIdx);
            CertIDType certId = certIdList.addNewCert();
            XAdESSignatureFacet.setCertID(certId, certificate, this.hashAlgo, false);
        }

        // XAdES-C: complete revocation refs
        CompleteRevocationRefsType completeRevocationRefs = 
            unsignedSigProps.addNewCompleteRevocationRefs();
        RevocationData revocationData = this.revocationDataService
                .getRevocationData(signingCertificateChain);
        if (revocationData.hasCRLs()) {
            CRLRefsType crlRefs = completeRevocationRefs.addNewCRLRefs();
            completeRevocationRefs.setCRLRefs(crlRefs);

            for (byte[] encodedCrl : revocationData.getCRLs()) {
                CRLRefType crlRef = crlRefs.addNewCRLRef();
                X509CRL crl;
                try {
                    crl = (X509CRL) this.certificateFactory
                            .generateCRL(new ByteArrayInputStream(encodedCrl));
                } catch (CRLException e) {
                    throw new RuntimeException("CRL parse error: "
                            + e.getMessage(), e);
                }

                CRLIdentifierType crlIdentifier = crlRef.addNewCRLIdentifier();
                String issuerName = crl.getIssuerDN().getName().replace(",", ", ");
                crlIdentifier.setIssuer(issuerName);
                Calendar cal = Calendar.getInstance();
                cal.setTime(crl.getThisUpdate());
                crlIdentifier.setIssueTime(cal);
                crlIdentifier.setNumber(getCrlNumber(crl));

                DigestAlgAndValueType digestAlgAndValue = crlRef.addNewDigestAlgAndValue();
                XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, encodedCrl, this.hashAlgo);
            }
        }
        if (revocationData.hasOCSPs()) {
            OCSPRefsType ocspRefs = completeRevocationRefs.addNewOCSPRefs();
            for (byte[] ocsp : revocationData.getOCSPs()) {
                try {
                    OCSPRefType ocspRef = ocspRefs.addNewOCSPRef();
    
                    DigestAlgAndValueType digestAlgAndValue = ocspRef.addNewDigestAlgAndValue();
                    XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, ocsp, this.hashAlgo);
    
                    OCSPIdentifierType ocspIdentifier = ocspRef.addNewOCSPIdentifier();
                    
                    OCSPRespIf ocspResp = HorribleProxy.newProxy(OCSPRespIf.class, ocsp);
                    
                    BasicOCSPRespIf basicOcspResp = ocspResp.getResponseObject();
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(basicOcspResp.getProducedAt());
                    ocspIdentifier.setProducedAt(cal);
    
                    ResponderIDType responderId = ocspIdentifier.addNewResponderID();
    
                    RespIDIf respId = basicOcspResp.getResponderId();
                    ResponderIDIf ocspResponderId = respId.toASN1Object();
                    DERTaggedObjectIf derTaggedObject = ocspResponderId.toASN1Object();
                    if (2 == derTaggedObject.getTagNo()) {
                        ASN1OctetStringIf keyHashOctetString = derTaggedObject.getObject$String();
                        byte key[] = keyHashOctetString.getOctets();
                        responderId.setByKey(key);
                    } else {
                        X509NameIf name = HorribleProxy.createProxy(X509NameIf.class, "getInstance", derTaggedObject.getObject$Object());
                        String nameStr = name.toString$delegate();
                        responderId.setByName(nameStr);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("OCSP decoding error: " + e.getMessage(), e);
                }
            }
        }

        // marshal XAdES-C

        // XAdES-X Type 1 timestamp
        
        
        
        List<Node> timeStampNodesXadesX1 = new LinkedList<Node>();
        timeStampNodesXadesX1.add(signatureElement.getDomNode());
        timeStampNodesXadesX1.add(signatureTimeStamp.getDomNode());
        timeStampNodesXadesX1.add(completeCertificateRefs.getDomNode());
        timeStampNodesXadesX1.add(completeRevocationRefs.getDomNode());

        RevocationData tsaRevocationDataXadesX1 = new RevocationData();
        LOG.log(POILogger.DEBUG, "creating XAdES-X time-stamp");
        XAdESTimeStampType timeStampXadesX1 = createXAdESTimeStamp(
                timeStampNodesXadesX1, tsaRevocationDataXadesX1,
                this.c14nAlgoId, this.timeStampService);
        if (tsaRevocationDataXadesX1.hasRevocationDataEntries()) {
            ValidationDataType timeStampXadesX1ValidationData = createValidationData(tsaRevocationDataXadesX1);
            SignatureInfo.insertXChild(unsignedSigProps, timeStampXadesX1ValidationData);
        }

        // marshal XAdES-X

        // XAdES-X-L
        CertificateValuesType certificateValues = unsignedSigProps.addNewCertificateValues();
        for (X509Certificate certificate : signingCertificateChain) {
            EncapsulatedPKIDataType encapsulatedPKIDataType = certificateValues.addNewEncapsulatedX509Certificate();
            try {
                encapsulatedPKIDataType.setByteArrayValue(certificate.getEncoded());
            } catch (CertificateEncodingException e) {
                throw new RuntimeException("certificate encoding error: " + e.getMessage(), e);
            }
        }
        
        RevocationValuesType revocationValues = unsignedSigProps.addNewRevocationValues();
        createRevocationValues(revocationValues, revocationData);

        // marshal XAdES-X-L
    }

    public static byte[] getC14nValue(List<Node> nodeList, String c14nAlgoId) {
        byte[] c14nValue = null;
        try {
            for (Node node : nodeList) {
                /*
                 * Re-initialize the c14n else the namespaces will get cached
                 * and will be missing from the c14n resulting nodes.
                 */
                CanonicalizerIf c14n = HorribleProxy.createProxy(CanonicalizerIf.class, "newInstance", c14nAlgoId);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(c14nValue);
                bos.write(c14n.canonicalizeSubtree(node));
                c14nValue = bos.toByteArray();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("c14n error: " + e.getMessage(), e);
        }
        return c14nValue;
    }

    public void preSign(XMLSignatureFactory signatureFactory,
            String signatureId,
            List<X509Certificate> signingCertificateChain,
            List<Reference> references, List<XMLObject> objects)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // nothing to do here
    }

    private BigInteger getCrlNumber(X509CRL crl) {
        byte[] crlNumberExtensionValue = crl.getExtensionValue("2.5.29.20" /*CRLNumber*/);
        if (null == crlNumberExtensionValue) {
            return null;
        }
        try {
            ASN1InputStreamIf asn1InputStream = HorribleProxy.newProxy(ASN1InputStreamIf.class, crlNumberExtensionValue);
            ASN1OctetStringIf octetString = asn1InputStream.readObject$ASNString();
            byte[] octets = octetString.getOctets();
            asn1InputStream = HorribleProxy.newProxy(ASN1InputStreamIf.class, octets);
            DERIntegerIf integer =  asn1InputStream.readObject$Integer();
            BigInteger crlNumber = integer.getPositiveValue();
            return crlNumber;
        } catch (Exception e) {
            throw new RuntimeException("I/O error: " + e.getMessage(), e);
        }
    }

    public static XAdESTimeStampType createXAdESTimeStamp(
            List<Node> nodeList,
            RevocationData revocationData,
            String c14nAlgoId,
            TimeStampService timeStampService) {
        byte[] c14nSignatureValueElement = getC14nValue(nodeList, c14nAlgoId);

        return createXAdESTimeStamp(c14nSignatureValueElement, revocationData,
                c14nAlgoId, timeStampService);
    }

    public static XAdESTimeStampType createXAdESTimeStamp(
            byte[] data,
            RevocationData revocationData,
            String c14nAlgoId,
            TimeStampService timeStampService) {
        // create the time-stamp
        byte[] timeStampToken;
        try {
            timeStampToken = timeStampService.timeStamp(data, revocationData);
        } catch (Exception e) {
            throw new RuntimeException("error while creating a time-stamp: "
                    + e.getMessage(), e);
        }

        // create a XAdES time-stamp container
        XAdESTimeStampType xadesTimeStamp = XAdESTimeStampType.Factory.newInstance();
        xadesTimeStamp.setId("time-stamp-" + UUID.randomUUID().toString());
        CanonicalizationMethodType c14nMethod = xadesTimeStamp.addNewCanonicalizationMethod();
        c14nMethod.setAlgorithm(c14nAlgoId);

        // embed the time-stamp
        EncapsulatedPKIDataType encapsulatedTimeStamp = xadesTimeStamp.addNewEncapsulatedTimeStamp();
        encapsulatedTimeStamp.setByteArrayValue(timeStampToken);
        encapsulatedTimeStamp.setId("time-stamp-token-" + UUID.randomUUID().toString());

        return xadesTimeStamp;
    }

    private ValidationDataType createValidationData(
            RevocationData revocationData) {
        ValidationDataType validationData = ValidationDataType.Factory.newInstance();
        RevocationValuesType revocationValues = validationData.addNewRevocationValues();
        createRevocationValues(revocationValues, revocationData);
        return validationData;
    }

    private void createRevocationValues(
            RevocationValuesType revocationValues, RevocationData revocationData) {
        if (revocationData.hasCRLs()) {
            CRLValuesType crlValues = revocationValues.addNewCRLValues();
            for (byte[] crl : revocationData.getCRLs()) {
                EncapsulatedPKIDataType encapsulatedCrlValue = crlValues.addNewEncapsulatedCRLValue();
                encapsulatedCrlValue.setByteArrayValue(crl);
            }
        }
        if (revocationData.hasOCSPs()) {
            OCSPValuesType ocspValues = revocationValues.addNewOCSPValues();
            for (byte[] ocsp : revocationData.getOCSPs()) {
                EncapsulatedPKIDataType encapsulatedOcspValue = ocspValues.addNewEncapsulatedOCSPValue();
                encapsulatedOcspValue.setByteArrayValue(ocsp);
            }
        }
    }

    public Map<String,String> getNamespacePrefixMapping() {
        return null;
    }

}
