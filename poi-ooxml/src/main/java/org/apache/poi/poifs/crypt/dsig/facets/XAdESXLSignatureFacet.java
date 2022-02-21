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

import static java.util.Optional.ofNullable;
import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet.insertXChild;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.crypto.MarshalException;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
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
import org.etsi.uri.x01903.v14.TimeStampValidationDataDocument;
import org.etsi.uri.x01903.v14.ValidationDataType;
import org.w3.x2000.x09.xmldsig.CanonicalizationMethodType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XAdES-X-L v1.4.1 signature facet. This signature facet implementation will
 * upgrade a given XAdES-BES/EPES signature to XAdES-X-L.
 * If no revocation data service is set, only a XAdES-T signature is created.
 *
 * We don't inherit from XAdESSignatureFacet as we also want to be able to use
 * this facet out of the context of a signature creation. This signature facet
 * assumes that the signature is already XAdES-BES/EPES compliant.
 *
 * @see XAdESSignatureFacet
 */
public class XAdESXLSignatureFacet implements SignatureFacet {

    private static final Logger LOG = LogManager.getLogger(XAdESXLSignatureFacet.class);

    private final CertificateFactory certificateFactory;

    public XAdESXLSignatureFacet() {
        try {
            this.certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("X509 JCA error: " + e.getMessage(), e);
        }
    }

    @Override
    public void postSign(SignatureInfo signatureInfo, Document document) throws MarshalException {
        LOG.atDebug().log("XAdES-X-L post sign phase");

        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();

        // check for XAdES-BES
        NodeList qualNl = document.getElementsByTagNameNS(XADES_132_NS, "QualifyingProperties");
        QualifyingPropertiesType qualProps = getQualProps(qualNl);

        // create basic XML container structure
        UnsignedPropertiesType unsignedProps =
            ofNullable(qualProps.getUnsignedProperties()).orElseGet(qualProps::addNewUnsignedProperties);
        UnsignedSignaturePropertiesType unsignedSigProps =
            ofNullable(unsignedProps.getUnsignedSignatureProperties()).orElseGet(unsignedProps::addNewUnsignedSignatureProperties);

        final NodeList nlSigVal = document.getElementsByTagNameNS(XML_DIGSIG_NS, "SignatureValue");
        if (nlSigVal.getLength() != 1) {
            throw new IllegalArgumentException("SignatureValue is not set.");
        }
        final Element sigVal = (Element)nlSigVal.item(0);


        // Without revocation data service we cannot construct the XAdES-C extension.
        RevocationDataService revDataSvc = signatureConfig.getRevocationDataService();
        if (revDataSvc != null) {
            // XAdES-X-L
            addCertificateValues(unsignedSigProps, signatureConfig);
        }

        LOG.atDebug().log("creating XAdES-T time-stamp");

        // xadesv141::TimeStampValidationData
        XAdESTimeStampType signatureTimeStamp;
        try {
            final RevocationData tsaRevocationDataXadesT = new RevocationData();
            signatureTimeStamp = createXAdESTimeStamp(signatureInfo, tsaRevocationDataXadesT, sigVal);
            unsignedSigProps.addNewSignatureTimeStamp().set(signatureTimeStamp);

            if (tsaRevocationDataXadesT.hasRevocationDataEntries()) {
                TimeStampValidationDataDocument validationData = createValidationData(tsaRevocationDataXadesT);
                insertXChild(unsignedSigProps, validationData);
            }
        } catch (CertificateEncodingException e) {
            throw new MarshalException("unable to create XAdES signatrue", e);
        }


        if (revDataSvc != null) {
            // XAdES-C: complete certificate refs
            CompleteCertificateRefsType completeCertificateRefs = completeCertificateRefs(unsignedSigProps, signatureConfig);

            // XAdES-C: complete revocation refs
            RevocationData revocationData = revDataSvc.getRevocationData(signatureConfig.getSigningCertificateChain());
            CompleteRevocationRefsType completeRevocationRefs = unsignedSigProps.addNewCompleteRevocationRefs();
            addRevocationCRL(completeRevocationRefs, signatureConfig, revocationData);
            addRevocationOCSP(completeRevocationRefs, signatureConfig, revocationData);

            RevocationValuesType revocationValues = unsignedSigProps.addNewRevocationValues();
            createRevocationValues(revocationValues, revocationData);

            // XAdES-X Type 1 timestamp
            LOG.atDebug().log("creating XAdES-X time-stamp");
            revocationData = new RevocationData();
            XAdESTimeStampType timeStampXadesX1 = createXAdESTimeStamp(signatureInfo, revocationData,
                    sigVal, signatureTimeStamp.getDomNode(), completeCertificateRefs.getDomNode(), completeRevocationRefs.getDomNode());

            // marshal XAdES-X
            unsignedSigProps.addNewSigAndRefsTimeStamp().set(timeStampXadesX1);
        }




        // marshal XAdES-X-L
        Element n = (Element)document.importNode(qualProps.getDomNode(), true);
        NodeList nl = n.getElementsByTagName("TimeStampValidationData");
        for (int i=0; i<nl.getLength(); i++) {
            ((Element)nl.item(i)).setAttributeNS(XML_NS, "xmlns", "http://uri.etsi.org/01903/v1.4.1#");
        }
        Node qualNL0 = qualNl.item(0);
        qualNL0.getParentNode().replaceChild(n, qualNL0);
    }

    private QualifyingPropertiesType getQualProps(NodeList qualNl) throws MarshalException {
        // check for XAdES-BES
        if (qualNl.getLength() != 1) {
            throw new MarshalException("no XAdES-BES extension present");
        }

        try {
            Node first = qualNl.item(0);
            QualifyingPropertiesDocument qualDoc = QualifyingPropertiesDocument.Factory.parse(first, DEFAULT_XML_OPTIONS);
            return qualDoc.getQualifyingProperties();
        } catch (XmlException e) {
            throw new MarshalException(e);
        }
    }

    private CompleteCertificateRefsType completeCertificateRefs(UnsignedSignaturePropertiesType unsignedSigProps, SignatureConfig signatureConfig) {
        CompleteCertificateRefsType completeCertificateRefs = unsignedSigProps.addNewCompleteCertificateRefs();

        CertIDListType certIdList = completeCertificateRefs.addNewCertRefs();
        /*
         * We skip the signing certificate itself according to section
         * 4.4.3.2 of the XAdES 1.4.1 specification.
         */
        List<X509Certificate> certChain = signatureConfig.getSigningCertificateChain();
        certChain.stream().skip(1).forEachOrdered(cert ->
            XAdESSignatureFacet.setCertID(certIdList.addNewCert(), signatureConfig, false, cert)
        );

        return completeCertificateRefs;
    }

    private void addRevocationCRL(CompleteRevocationRefsType completeRevocationRefs, SignatureConfig signatureConfig, RevocationData revocationData) {
        if (revocationData.hasCRLs()) {
            CRLRefsType crlRefs = completeRevocationRefs.addNewCRLRefs();
            completeRevocationRefs.setCRLRefs(crlRefs);

            for (byte[] encodedCrl : revocationData.getCRLs()) {
                CRLRefType crlRef = crlRefs.addNewCRLRef();
                X509CRL crl;
                try {
                    crl = (X509CRL) this.certificateFactory
                        .generateCRL(new UnsynchronizedByteArrayInputStream(encodedCrl));
                } catch (CRLException e) {
                    throw new RuntimeException("CRL parse error: " + e.getMessage(), e);
                }

                CRLIdentifierType crlIdentifier = crlRef.addNewCRLIdentifier();
                String issuerName = crl.getIssuerX500Principal().getName().replace(",", ", ");
                crlIdentifier.setIssuer(issuerName);
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Z"), Locale.ROOT);
                cal.setTime(crl.getThisUpdate());
                crlIdentifier.setIssueTime(cal);
                crlIdentifier.setNumber(getCrlNumber(crl));

                DigestAlgAndValueType digestAlgAndValue = crlRef.addNewDigestAlgAndValue();
                XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, encodedCrl, signatureConfig.getDigestAlgo());
            }
        }
    }

    private void addRevocationOCSP(CompleteRevocationRefsType completeRevocationRefs, SignatureConfig signatureConfig, RevocationData revocationData) {
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
                        ASN1OctetString keyHashOctetString = (ASN1OctetString)derTaggedObject.getBaseObject();
                        byte[] key = keyHashOctetString.getOctets();
                        responderId.setByKey(key);
                    } else {
                        X500Name name = X500Name.getInstance(derTaggedObject.getBaseObject());
                        String nameStr = name.toString();
                        responderId.setByName(nameStr);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("OCSP decoding error: " + e.getMessage(), e);
                }
            }
        }
    }

    private void addCertificateValues(UnsignedSignaturePropertiesType unsignedSigProps, SignatureConfig signatureConfig) {
        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();
        if (chain.size() < 2) {
            return;
        }
        CertificateValuesType certificateValues = unsignedSigProps.addNewCertificateValues();
        try {
            for (X509Certificate certificate : chain.subList(1, chain.size())) {
                certificateValues.addNewEncapsulatedX509Certificate().setByteArrayValue(certificate.getEncoded());
            }
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("certificate encoding error: " + e.getMessage(), e);
        }
    }

    private static byte[] getC14nValue(List<Node> nodeList, String c14nAlgoId) {
        try (UnsynchronizedByteArrayOutputStream c14nValue = new UnsynchronizedByteArrayOutputStream()) {
            for (Node node : nodeList) {
                /*
                 * Re-initialize the c14n else the namespaces will get cached
                 * and will be missing from the c14n resulting nodes.
                 */
                Canonicalizer c14n = Canonicalizer.getInstance(c14nAlgoId);
                c14n.canonicalizeSubtree(node, c14nValue);
            }
            return c14nValue.toByteArray();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("c14n error: " + e.getMessage(), e);
        }
    }

    private BigInteger getCrlNumber(X509CRL crl) {
        byte[] crlNumberExtensionValue = crl.getExtensionValue(Extension.cRLNumber.getId());
        if (null == crlNumberExtensionValue) {
            return null;
        }

        try (ASN1InputStream asn1IS1 = new ASN1InputStream(crlNumberExtensionValue)) {
            ASN1OctetString octetString = (ASN1OctetString)asn1IS1.readObject();
            byte[] octets = octetString.getOctets();
            try (ASN1InputStream asn1IS2 = new ASN1InputStream(octets)) {
                ASN1Integer integer = (ASN1Integer) asn1IS2.readObject();
                return integer.getPositiveValue();
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error: " + e.getMessage(), e);
        }
    }

    private XAdESTimeStampType createXAdESTimeStamp(
            SignatureInfo signatureInfo,
            RevocationData revocationData,
            Node... nodes) {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        byte[] c14nSignatureValueElement = getC14nValue(Arrays.asList(nodes), signatureConfig.getXadesCanonicalizationMethod());

        // create the time-stamp
        byte[] timeStampToken;
        try {
            timeStampToken = signatureConfig.getTspService().timeStamp(signatureInfo, c14nSignatureValueElement, revocationData);
        } catch (Exception e) {
            throw new RuntimeException("error while creating a time-stamp: "
                + e.getMessage(), e);
        }

        // create a XAdES time-stamp container
        XAdESTimeStampType xadesTimeStamp = XAdESTimeStampType.Factory.newInstance();
        CanonicalizationMethodType c14nMethod = xadesTimeStamp.addNewCanonicalizationMethod();
        c14nMethod.setAlgorithm(signatureConfig.getXadesCanonicalizationMethod());

        // embed the time-stamp
        EncapsulatedPKIDataType encapsulatedTimeStamp = xadesTimeStamp.addNewEncapsulatedTimeStamp();
        encapsulatedTimeStamp.setByteArrayValue(timeStampToken);

        return xadesTimeStamp;
    }

    private TimeStampValidationDataDocument createValidationData(RevocationData revocationData)
    throws CertificateEncodingException {
        TimeStampValidationDataDocument doc = TimeStampValidationDataDocument.Factory.newInstance();
        ValidationDataType validationData = doc.addNewTimeStampValidationData();
        List<X509Certificate> tspChain = revocationData.getX509chain();

        if (tspChain.size() > 1) {
            CertificateValuesType cvals = validationData.addNewCertificateValues();
            for (X509Certificate x509 : tspChain.subList(1, tspChain.size())) {
                byte[] encoded = x509.getEncoded();
                cvals.addNewEncapsulatedX509Certificate().setByteArrayValue(encoded);
            }
        }
        RevocationValuesType revocationValues = validationData.addNewRevocationValues();
        createRevocationValues(revocationValues, revocationData);
        return doc;
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
