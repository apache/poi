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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet.insertXChild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.crypto.MarshalException;

import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xmlbeans.XmlException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RespID;
import org.etsi.uri.x01903.v13.*;
import org.etsi.uri.x01903.v14.ValidationDataType;
import org.w3.x2000.x09.xmldsig.CanonicalizationMethodType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class XAdESXLSignatureFacet extends SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(XAdESXLSignatureFacet.class);

    private final CertificateFactory certificateFactory;

    public XAdESXLSignatureFacet() {
        try {
            this.certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("X509 JCA error: " + e.getMessage(), e);
        }
    }

    @Override
    public void postSign(Document document) throws MarshalException {
        LOG.log(POILogger.DEBUG, "XAdES-X-L post sign phase");

        QualifyingPropertiesDocument qualDoc = null;
        QualifyingPropertiesType qualProps = null;

        // check for XAdES-BES
        NodeList qualNl = document.getElementsByTagNameNS(XADES_132_NS, "QualifyingProperties");
        if (qualNl.getLength() == 1) {
            try {
                qualDoc = QualifyingPropertiesDocument.Factory.parse(qualNl.item(0), DEFAULT_XML_OPTIONS);
            } catch (XmlException e) {
                throw new MarshalException(e);
            }
            qualProps = qualDoc.getQualifyingProperties();
        } else {
            throw new MarshalException("no XAdES-BES extension present");
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
        NodeList nlSigVal = document.getElementsByTagNameNS(XML_DIGSIG_NS, "SignatureValue");
        if (nlSigVal.getLength() != 1) {
            throw new IllegalArgumentException("SignatureValue is not set.");
        }
        
        RevocationData tsaRevocationDataXadesT = new RevocationData();
        LOG.log(POILogger.DEBUG, "creating XAdES-T time-stamp");
        XAdESTimeStampType signatureTimeStamp = createXAdESTimeStamp
            (Collections.singletonList(nlSigVal.item(0)), tsaRevocationDataXadesT);

        // marshal the XAdES-T extension
        unsignedSigProps.addNewSignatureTimeStamp().set(signatureTimeStamp);

        // xadesv141::TimeStampValidationData
        if (tsaRevocationDataXadesT.hasRevocationDataEntries()) {
            ValidationDataType validationData = createValidationData(tsaRevocationDataXadesT);
            insertXChild(unsignedSigProps, validationData);
        }

        if (signatureConfig.getRevocationDataService() == null) {
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
        /*
         * We skip the signing certificate itself according to section
         * 4.4.3.2 of the XAdES 1.4.1 specification.
         */
        List<X509Certificate> certChain = signatureConfig.getSigningCertificateChain();
        int chainSize = certChain.size();
        if (chainSize > 1) {
            for (X509Certificate cert : certChain.subList(1, chainSize)) {
                CertIDType certId = certIdList.addNewCert();
                XAdESSignatureFacet.setCertID(certId, signatureConfig, false, cert);
            }
        }

        // XAdES-C: complete revocation refs
        CompleteRevocationRefsType completeRevocationRefs = 
            unsignedSigProps.addNewCompleteRevocationRefs();
        RevocationData revocationData = signatureConfig.getRevocationDataService()
            .getRevocationData(certChain);
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
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Z"), Locale.ROOT);
                cal.setTime(crl.getThisUpdate());
                crlIdentifier.setIssueTime(cal);
                crlIdentifier.setNumber(getCrlNumber(crl));

                DigestAlgAndValueType digestAlgAndValue = crlRef.addNewDigestAlgAndValue();
                XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, encodedCrl, signatureConfig.getDigestAlgo());
            }
        }
        if (revocationData.hasOCSPs()) {
            OCSPRefsType ocspRefs = completeRevocationRefs.addNewOCSPRefs();
            for (byte[] ocsp : revocationData.getOCSPs()) {
                try {
                    OCSPRefType ocspRef = ocspRefs.addNewOCSPRef();
    
                    DigestAlgAndValueType digestAlgAndValue = ocspRef.addNewDigestAlgAndValue();
                    XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, ocsp, signatureConfig.getDigestAlgo());
    
                    OCSPIdentifierType ocspIdentifier = ocspRef.addNewOCSPIdentifier();
                    
                    OCSPResp ocspResp = new OCSPResp(ocsp);
                    
                    BasicOCSPResp basicOcspResp = (BasicOCSPResp)ocspResp.getResponseObject();
                    
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Z"), Locale.ROOT);
                    cal.setTime(basicOcspResp.getProducedAt());
                    ocspIdentifier.setProducedAt(cal);
    
                    ResponderIDType responderId = ocspIdentifier.addNewResponderID();
    
                    RespID respId = basicOcspResp.getResponderId();
                    ResponderID ocspResponderId = respId.toASN1Primitive();
                    DERTaggedObject derTaggedObject = (DERTaggedObject)ocspResponderId.toASN1Primitive();
                    if (2 == derTaggedObject.getTagNo()) {
                        ASN1OctetString keyHashOctetString = (ASN1OctetString)derTaggedObject.getObject();
                        byte key[] = keyHashOctetString.getOctets();
                        responderId.setByKey(key);
                    } else {
                        X500Name name = X500Name.getInstance(derTaggedObject.getObject());
                        String nameStr = name.toString();
                        responderId.setByName(nameStr);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("OCSP decoding error: " + e.getMessage(), e);
                }
            }
        }

        // marshal XAdES-C
        
        // XAdES-X Type 1 timestamp
        List<Node> timeStampNodesXadesX1 = new ArrayList<>();
        timeStampNodesXadesX1.add(nlSigVal.item(0));
        timeStampNodesXadesX1.add(signatureTimeStamp.getDomNode());
        timeStampNodesXadesX1.add(completeCertificateRefs.getDomNode());
        timeStampNodesXadesX1.add(completeRevocationRefs.getDomNode());

        RevocationData tsaRevocationDataXadesX1 = new RevocationData();
        LOG.log(POILogger.DEBUG, "creating XAdES-X time-stamp");
        XAdESTimeStampType timeStampXadesX1 = createXAdESTimeStamp
            (timeStampNodesXadesX1, tsaRevocationDataXadesX1);
        if (tsaRevocationDataXadesX1.hasRevocationDataEntries()) {
            ValidationDataType timeStampXadesX1ValidationData = createValidationData(tsaRevocationDataXadesX1);
            insertXChild(unsignedSigProps, timeStampXadesX1ValidationData);
        }

        // marshal XAdES-X
        unsignedSigProps.addNewSigAndRefsTimeStamp().set(timeStampXadesX1);

        // XAdES-X-L
        CertificateValuesType certificateValues = unsignedSigProps.addNewCertificateValues();
        for (X509Certificate certificate : certChain) {
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
        Node n = document.importNode(qualProps.getDomNode(), true);
        qualNl.item(0).getParentNode().replaceChild(n, qualNl.item(0));
    }

    public static byte[] getC14nValue(List<Node> nodeList, String c14nAlgoId) {
        ByteArrayOutputStream c14nValue = new ByteArrayOutputStream();
        try {
            for (Node node : nodeList) {
                /*
                 * Re-initialize the c14n else the namespaces will get cached
                 * and will be missing from the c14n resulting nodes.
                 */
                Canonicalizer c14n = Canonicalizer.getInstance(c14nAlgoId);
                c14nValue.write(c14n.canonicalizeSubtree(node));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("c14n error: " + e.getMessage(), e);
        }
        return c14nValue.toByteArray();
    }

    private BigInteger getCrlNumber(X509CRL crl) {
        byte[] crlNumberExtensionValue = crl.getExtensionValue(Extension.cRLNumber.getId());
        if (null == crlNumberExtensionValue) {
            return null;
        }

        try {
            ASN1InputStream asn1IS1 = null, asn1IS2 = null;
            try {
                asn1IS1 = new ASN1InputStream(crlNumberExtensionValue);
                ASN1OctetString octetString = (ASN1OctetString)asn1IS1.readObject();
                byte[] octets = octetString.getOctets();
                asn1IS2 = new ASN1InputStream(octets);
                ASN1Integer integer = (ASN1Integer)asn1IS2.readObject();
                return integer.getPositiveValue();
            } finally {
                IOUtils.closeQuietly(asn1IS2);
                IOUtils.closeQuietly(asn1IS1);
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error: " + e.getMessage(), e);
        }
    }

    private XAdESTimeStampType createXAdESTimeStamp(
            List<Node> nodeList,
            RevocationData revocationData) {
        byte[] c14nSignatureValueElement = getC14nValue(nodeList, signatureConfig.getXadesCanonicalizationMethod());

        return createXAdESTimeStamp(c14nSignatureValueElement, revocationData);
    }

    private XAdESTimeStampType createXAdESTimeStamp(byte[] data, RevocationData revocationData) {
        // create the time-stamp
        byte[] timeStampToken;
        try {
            timeStampToken = signatureConfig.getTspService().timeStamp(data, revocationData);
        } catch (Exception e) {
            throw new RuntimeException("error while creating a time-stamp: "
                    + e.getMessage(), e);
        }

        // create a XAdES time-stamp container
        XAdESTimeStampType xadesTimeStamp = XAdESTimeStampType.Factory.newInstance();
        xadesTimeStamp.setId("time-stamp-" + UUID.randomUUID());
        CanonicalizationMethodType c14nMethod = xadesTimeStamp.addNewCanonicalizationMethod();
        c14nMethod.setAlgorithm(signatureConfig.getXadesCanonicalizationMethod());

        // embed the time-stamp
        EncapsulatedPKIDataType encapsulatedTimeStamp = xadesTimeStamp.addNewEncapsulatedTimeStamp();
        encapsulatedTimeStamp.setByteArrayValue(timeStampToken);
        encapsulatedTimeStamp.setId("time-stamp-token-" + UUID.randomUUID());

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
}
