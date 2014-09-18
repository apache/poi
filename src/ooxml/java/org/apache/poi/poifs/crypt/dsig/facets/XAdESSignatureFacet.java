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

import static org.apache.poi.poifs.crypt.dsig.SignatureInfo.XmlNS;
import static org.apache.poi.poifs.crypt.dsig.SignatureInfo.setPrefix;

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureInfoConfig;
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
 * @see http://en.wikipedia.org/wiki/XAdES
 * 
 */
public class XAdESSignatureFacet implements SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(XAdESSignatureFacet.class);

    private static final String XADES_TYPE = "http://uri.etsi.org/01903#SignedProperties";
    
    private SignatureInfoConfig signatureConfig;
    
    private String idSignedProperties;

    private boolean signaturePolicyImplied;

    private String role;

    private boolean issuerNameNoReverseOrder = false;

    private Map<String, String> dataObjectFormatMimeTypes = new HashMap<String, String>();

    /**
     * Main constructor.
     * 
     * @param clock
     *            the clock to be used for determining the xades:SigningTime,
     *            defaults to now when null
     * @param hashAlgo
     *            the digest algorithm to be used for all required XAdES digest
     *            operations. Possible values: "SHA-1", "SHA-256", or "SHA-512",
     *            defaults to SHA-1 when null
     * @param signaturePolicyService
     *            the optional signature policy service used for XAdES-EPES.
     */
    public XAdESSignatureFacet(SignatureInfoConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    @Override
    public void postSign(Document document, List<X509Certificate> signingCertificateChain) {
        LOG.log(POILogger.DEBUG, "postSign");
    }

    @Override
    public void preSign(Document document,
            XMLSignatureFactory signatureFactory,
            List<Reference> references, List<XMLObject> objects)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        LOG.log(POILogger.DEBUG, "preSign");

        // QualifyingProperties
        QualifyingPropertiesDocument qualDoc = QualifyingPropertiesDocument.Factory.newInstance();
        QualifyingPropertiesType qualifyingProperties = qualDoc.addNewQualifyingProperties();
        qualifyingProperties.setTarget("#" + signatureConfig.getPackageSignatureId());
        
        // SignedProperties
        SignedPropertiesType signedProperties = qualifyingProperties.addNewSignedProperties();
        String signedPropertiesId = this.idSignedProperties;
        if (this.idSignedProperties == null) {
            signedPropertiesId = signatureConfig.getPackageSignatureId() + "-xades";
        }
        signedProperties.setId(signedPropertiesId);

        // SignedSignatureProperties
        SignedSignaturePropertiesType signedSignatureProperties = signedProperties.addNewSignedSignatureProperties();

        // SigningTime
        Calendar xmlGregorianCalendar = Calendar.getInstance();
        xmlGregorianCalendar.setTimeZone(TimeZone.getTimeZone("Z"));
        xmlGregorianCalendar.setTime(this.signatureConfig.getExecutionTime());
        xmlGregorianCalendar.clear(Calendar.MILLISECOND);
        signedSignatureProperties.setSigningTime(xmlGregorianCalendar);

        // SigningCertificate
        if (signatureConfig.getSigningCertificateChain() == null
            || signatureConfig.getSigningCertificateChain().isEmpty()) {
            throw new RuntimeException("no signing certificate chain available");
        }
        CertIDListType signingCertificates = signedSignatureProperties.addNewSigningCertificate();
        CertIDType certId = signingCertificates.addNewCert();
        X509Certificate signingCertificate = signatureConfig.getSigningCertificateChain().get(0);
        setCertID(certId, signingCertificate, this.signatureConfig.getDigestAlgo(), this.issuerNameNoReverseOrder);

        // ClaimedRole
        if (null != this.role && false == this.role.isEmpty()) {
            SignerRoleType signerRole = signedSignatureProperties.addNewSignerRole();
            signedSignatureProperties.setSignerRole(signerRole);
            ClaimedRolesListType claimedRolesList = signerRole.addNewClaimedRoles();
            AnyType claimedRole = claimedRolesList.addNewClaimedRole();
            XmlString roleString = XmlString.Factory.newInstance();
            roleString.setStringValue(this.role);
            insertXChild(claimedRole, roleString);
        }

        // XAdES-EPES
        SignaturePolicyService policyService = this.signatureConfig.getSignaturePolicyService();
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
            setDigestAlgAndValue(sigPolicyHash, signaturePolicyDocumentData, this.signatureConfig.getDigestAlgo());

            String signaturePolicyDownloadUrl = policyService.getSignaturePolicyDownloadUrl();
            if (null != signaturePolicyDownloadUrl) {
                SigPolicyQualifiersListType sigPolicyQualifiers = signaturePolicyId.addNewSigPolicyQualifiers(); 
                AnyType sigPolicyQualifier = sigPolicyQualifiers.addNewSigPolicyQualifier();
                XmlString spUriElement = XmlString.Factory.newInstance();
                spUriElement.setStringValue(signaturePolicyDownloadUrl);
                insertXChild(sigPolicyQualifier, spUriElement);
            }
        } else if (this.signaturePolicyImplied) {
            SignaturePolicyIdentifierType signaturePolicyIdentifier = 
                    signedSignatureProperties.addNewSignaturePolicyIdentifier();
            signaturePolicyIdentifier.addNewSignaturePolicyImplied();
        }

        // DataObjectFormat
        if (false == this.dataObjectFormatMimeTypes.isEmpty()) {
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

        // marshall XAdES QualifyingProperties
        // ((Element)qualifyingProperties.getSignedProperties().getDomNode()).setIdAttribute("Id", true);

        // add XAdES ds:Object
        List<XMLStructure> xadesObjectContent = new ArrayList<XMLStructure>();
        Element qualDocEl = (Element)document.importNode(qualifyingProperties.getDomNode(), true);
        SignatureInfo.registerIdAttribute(qualDocEl.getElementsByTagName("SignedProperties"));
        qualDocEl.setAttributeNS(XmlNS, "xmlns:xd", "http://uri.etsi.org/01903/v1.3.2#");
        setPrefix(qualDocEl, "http://uri.etsi.org/01903/v1.3.2#", "xd");
        xadesObjectContent.add(new DOMStructure(qualDocEl));
        XMLObject xadesObject = signatureFactory.newXMLObject(xadesObjectContent, null, null, null);
        objects.add(xadesObject);

        // add XAdES ds:Reference
        DigestMethod digestMethod = signatureFactory.newDigestMethod(this.signatureConfig.getDigestAlgo().xmlSignUri, null);
        List<Transform> transforms = new ArrayList<Transform>();
        Transform exclusiveTransform = signatureFactory
                .newTransform(CanonicalizationMethod.INCLUSIVE,
                        (TransformParameterSpec) null);
        transforms.add(exclusiveTransform);
        Reference reference = signatureFactory.newReference("#"
                + signedPropertiesId, digestMethod, transforms, XADES_TYPE,
                null);
        references.add(reference);
    }

    /**
     * Gives back the JAXB DigestAlgAndValue data structure.
     * 
     * @param data
     * @param xadesObjectFactory
     * @param xmldsigObjectFactory
     * @param hashAlgo
     * @return
     */
    protected static void setDigestAlgAndValue(
            DigestAlgAndValueType digestAlgAndValue,
            byte[] data,
            HashAlgorithm hashAlgo) {
        DigestMethodType digestMethod = digestAlgAndValue.addNewDigestMethod();
        digestMethod.setAlgorithm(hashAlgo.xmlSignUri);
        
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(hashAlgo);
        byte[] digestValue = messageDigest.digest(data);
        digestAlgAndValue.setDigestValue(digestValue);
    }

    /**
     * Gives back the JAXB CertID data structure.
     * 
     * @param certificate
     * @param xadesObjectFactory
     * @param xmldsigObjectFactory
     * @param digestAlgorithm
     * @return
     */
    protected static void setCertID(
            CertIDType certId,
            X509Certificate certificate,
            HashAlgorithm digestAlgorithm, boolean issuerNameNoReverseOrder) {
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
        setDigestAlgAndValue(certDigest, encodedCertificate, digestAlgorithm);
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

    /**
     * Sets the Id that will be used on the SignedProperties element;
     * 
     * @param idSignedProperties
     */
    public void setIdSignedProperties(String idSignedProperties) {
        this.idSignedProperties = idSignedProperties;
    }

    /**
     * Sets the signature policy to implied.
     * 
     * @param signaturePolicyImplied
     */
    public void setSignaturePolicyImplied(boolean signaturePolicyImplied) {
        this.signaturePolicyImplied = signaturePolicyImplied;
    }

    /**
     * Sets the XAdES claimed role.
     * 
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Work-around for Office 2010 IssuerName encoding.
     * 
     * @param reverseOrder
     */
    public void setIssuerNameNoReverseOrder(boolean reverseOrder) {
        this.issuerNameNoReverseOrder = reverseOrder;
    }


    public Map<String,String> getNamespacePrefixMapping() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("xd", "http://uri.etsi.org/01903/v1.3.2#");
        return map;
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