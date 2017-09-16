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

import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.services.SignaturePolicyService;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.etsi.uri.x01903.v13.AnyType;
import org.etsi.uri.x01903.v13.CertIDListType;
import org.etsi.uri.x01903.v13.CertIDType;
import org.etsi.uri.x01903.v13.ClaimedRolesListType;
import org.etsi.uri.x01903.v13.DataObjectFormatType;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.IdentifierType;
import org.etsi.uri.x01903.v13.ObjectIdentifierType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesDocument;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.etsi.uri.x01903.v13.SigPolicyQualifiersListType;
import org.etsi.uri.x01903.v13.SignaturePolicyIdType;
import org.etsi.uri.x01903.v13.SignaturePolicyIdentifierType;
import org.etsi.uri.x01903.v13.SignedDataObjectPropertiesType;
import org.etsi.uri.x01903.v13.SignedPropertiesType;
import org.etsi.uri.x01903.v13.SignedSignaturePropertiesType;
import org.etsi.uri.x01903.v13.SignerRoleType;
import org.w3.x2000.x09.xmldsig.DigestMethodType;
import org.w3.x2000.x09.xmldsig.X509IssuerSerialType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XAdES Signature Facet. Implements XAdES v1.4.1 which is compatible with XAdES
 * v1.3.2. The implemented XAdES format is XAdES-BES/EPES. It's up to another
 * part of the signature service to upgrade the XAdES-BES to a XAdES-X-L.
 * 
 * This implementation has been tested against an implementation that
 * participated multiple ETSI XAdES plugtests.
 * 
 * @author Frank Cornelis
 * @see <a href="http://en.wikipedia.org/wiki/XAdES">XAdES</a>
 * 
 */
public class XAdESSignatureFacet extends SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(XAdESSignatureFacet.class);

    private static final String XADES_TYPE = "http://uri.etsi.org/01903#SignedProperties";
    
    private Map<String, String> dataObjectFormatMimeTypes = new HashMap<>();


    @Override
    public void preSign(
          Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException {
        LOG.log(POILogger.DEBUG, "preSign");

        // QualifyingProperties
        QualifyingPropertiesDocument qualDoc = QualifyingPropertiesDocument.Factory.newInstance();
        QualifyingPropertiesType qualifyingProperties = qualDoc.addNewQualifyingProperties();
        qualifyingProperties.setTarget("#" + signatureConfig.getPackageSignatureId());
        
        // SignedProperties
        SignedPropertiesType signedProperties = qualifyingProperties.addNewSignedProperties();
        signedProperties.setId(signatureConfig.getXadesSignatureId());

        // SignedSignatureProperties
        SignedSignaturePropertiesType signedSignatureProperties = signedProperties.addNewSignedSignatureProperties();

        // SigningTime
        Calendar xmlGregorianCalendar = Calendar.getInstance(TimeZone.getTimeZone("Z"), Locale.ROOT);
        xmlGregorianCalendar.setTime(signatureConfig.getExecutionTime());
        xmlGregorianCalendar.clear(Calendar.MILLISECOND);
        signedSignatureProperties.setSigningTime(xmlGregorianCalendar);

        // SigningCertificate
        if (signatureConfig.getSigningCertificateChain() == null
            || signatureConfig.getSigningCertificateChain().isEmpty()) {
            throw new RuntimeException("no signing certificate chain available");
        }
        CertIDListType signingCertificates = signedSignatureProperties.addNewSigningCertificate();
        CertIDType certId = signingCertificates.addNewCert();
        X509Certificate certificate = signatureConfig.getSigningCertificateChain().get(0);
        setCertID(certId, signatureConfig, signatureConfig.isXadesIssuerNameNoReverseOrder(), certificate);

        // ClaimedRole
        String role = signatureConfig.getXadesRole();
        if (role != null && !role.isEmpty()) {
            SignerRoleType signerRole = signedSignatureProperties.addNewSignerRole();
            signedSignatureProperties.setSignerRole(signerRole);
            ClaimedRolesListType claimedRolesList = signerRole.addNewClaimedRoles();
            AnyType claimedRole = claimedRolesList.addNewClaimedRole();
            XmlString roleString = XmlString.Factory.newInstance();
            roleString.setStringValue(role);
            insertXChild(claimedRole, roleString);
        }

        // XAdES-EPES
        SignaturePolicyService policyService = signatureConfig.getSignaturePolicyService();
        if (policyService != null) {
            SignaturePolicyIdentifierType signaturePolicyIdentifier =
                signedSignatureProperties.addNewSignaturePolicyIdentifier();
            
            SignaturePolicyIdType signaturePolicyId = signaturePolicyIdentifier.addNewSignaturePolicyId();

            ObjectIdentifierType objectIdentifier = signaturePolicyId.addNewSigPolicyId();
            objectIdentifier.setDescription(policyService.getSignaturePolicyDescription());
            
            IdentifierType identifier = objectIdentifier.addNewIdentifier();
            identifier.setStringValue(policyService.getSignaturePolicyIdentifier());

            byte[] signaturePolicyDocumentData = policyService.getSignaturePolicyDocument();
            DigestAlgAndValueType sigPolicyHash = signaturePolicyId.addNewSigPolicyHash();
            setDigestAlgAndValue(sigPolicyHash, signaturePolicyDocumentData, signatureConfig.getDigestAlgo());

            String signaturePolicyDownloadUrl = policyService.getSignaturePolicyDownloadUrl();
            if (null != signaturePolicyDownloadUrl) {
                SigPolicyQualifiersListType sigPolicyQualifiers = signaturePolicyId.addNewSigPolicyQualifiers(); 
                AnyType sigPolicyQualifier = sigPolicyQualifiers.addNewSigPolicyQualifier();
                XmlString spUriElement = XmlString.Factory.newInstance();
                spUriElement.setStringValue(signaturePolicyDownloadUrl);
                insertXChild(sigPolicyQualifier, spUriElement);
            }
        } else if (signatureConfig.isXadesSignaturePolicyImplied()) {
            SignaturePolicyIdentifierType signaturePolicyIdentifier = 
                    signedSignatureProperties.addNewSignaturePolicyIdentifier();
            signaturePolicyIdentifier.addNewSignaturePolicyImplied();
        }

        // DataObjectFormat
        if (!dataObjectFormatMimeTypes.isEmpty()) {
            SignedDataObjectPropertiesType signedDataObjectProperties =
                signedProperties.addNewSignedDataObjectProperties();

            List<DataObjectFormatType> dataObjectFormats = signedDataObjectProperties
                    .getDataObjectFormatList();
            for (Map.Entry<String, String> dataObjectFormatMimeType : this.dataObjectFormatMimeTypes
                    .entrySet()) {
                DataObjectFormatType dataObjectFormat = DataObjectFormatType.Factory.newInstance();
                dataObjectFormat.setObjectReference("#" + dataObjectFormatMimeType.getKey());
                dataObjectFormat.setMimeType(dataObjectFormatMimeType.getValue());
                dataObjectFormats.add(dataObjectFormat);
            }
        }

        // add XAdES ds:Object
        List<XMLStructure> xadesObjectContent = new ArrayList<>();
        Element qualDocElSrc = (Element)qualifyingProperties.getDomNode();
        Element qualDocEl = (Element)document.importNode(qualDocElSrc, true);
        xadesObjectContent.add(new DOMStructure(qualDocEl));
        XMLObject xadesObject = getSignatureFactory().newXMLObject(xadesObjectContent, null, null, null);
        objects.add(xadesObject);

        // add XAdES ds:Reference
        List<Transform> transforms = new ArrayList<>();
        Transform exclusiveTransform = newTransform(CanonicalizationMethod.INCLUSIVE);
        transforms.add(exclusiveTransform);
        Reference reference = newReference
            ("#"+signatureConfig.getXadesSignatureId(), transforms, XADES_TYPE, null, null);
        references.add(reference);
    }

    /**
     * Gives back the JAXB DigestAlgAndValue data structure.
     *
     * @param digestAlgAndValue the parent for the new digest element 
     * @param data the data to be digested
     * @param digestAlgo the digest algorithm
     */
    protected static void setDigestAlgAndValue(
            DigestAlgAndValueType digestAlgAndValue,
            byte[] data,
            HashAlgorithm digestAlgo) {
        DigestMethodType digestMethod = digestAlgAndValue.addNewDigestMethod();
        digestMethod.setAlgorithm(SignatureConfig.getDigestMethodUri(digestAlgo));
        
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(digestAlgo);
        byte[] digestValue = messageDigest.digest(data);
        digestAlgAndValue.setDigestValue(digestValue);
    }

    /**
     * Gives back the JAXB CertID data structure.
     */
    protected static void setCertID
        (CertIDType certId, SignatureConfig signatureConfig, boolean issuerNameNoReverseOrder, X509Certificate certificate) {
        X509IssuerSerialType issuerSerial = certId.addNewIssuerSerial();
        String issuerName;
        if (issuerNameNoReverseOrder) {
            /*
             * Make sure the DN is encoded using the same order as present
             * within the certificate. This is an Office2010 work-around.
             * Should be reverted back.
             * 
             * XXX: not correct according to RFC 4514.
             */
            // TODO: check if issuerName is different on getTBSCertificate
            // issuerName = PrincipalUtil.getIssuerX509Principal(certificate).getName().replace(",", ", ");
            issuerName = certificate.getIssuerDN().getName().replace(",", ", ");
        } else {
            issuerName = certificate.getIssuerX500Principal().toString();
        }
        issuerSerial.setX509IssuerName(issuerName);
        issuerSerial.setX509SerialNumber(certificate.getSerialNumber());

        byte[] encodedCertificate;
        try {
            encodedCertificate = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("certificate encoding error: "
                    + e.getMessage(), e);
        }
        DigestAlgAndValueType certDigest = certId.addNewCertDigest(); 
        setDigestAlgAndValue(certDigest, encodedCertificate, signatureConfig.getXadesDigestAlgo());
    }

    /**
     * Adds a mime-type for the given ds:Reference (referred via its @URI). This
     * information is added via the xades:DataObjectFormat element.
     * 
     * @param dsReferenceUri
     * @param mimetype
     */
    public void addMimeType(String dsReferenceUri, String mimetype) {
        this.dataObjectFormatMimeTypes.put(dsReferenceUri, mimetype);
    }

    protected static void insertXChild(XmlObject root, XmlObject child) {
        XmlCursor rootCursor = root.newCursor();
        rootCursor.toEndToken();
        XmlCursor childCursor = child.newCursor();
        childCursor.toNextToken();
        childCursor.moveXml(rootCursor);
        childCursor.dispose();
        rootCursor.dispose();
    }

}