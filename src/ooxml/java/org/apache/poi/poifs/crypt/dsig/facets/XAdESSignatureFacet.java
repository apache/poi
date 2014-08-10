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

import static org.apache.poi.poifs.crypt.dsig.SignatureInfo.setPrefix;

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.apache.poi.poifs.crypt.dsig.spi.Constants;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
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
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3.x2000.x09.xmldsig.X509IssuerSerialType;
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
    
    private final Date clock;

    private final HashAlgorithm hashAlgo;

    private final SignaturePolicyService signaturePolicyService;

    private String idSignedProperties;

    private boolean signaturePolicyImplied;

    private String role;

    private boolean issuerNameNoReverseOrder = false;

    private Map<String, String> dataObjectFormatMimeTypes;

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
    public XAdESSignatureFacet(Date clock, HashAlgorithm hashAlgo,
            SignaturePolicyService signaturePolicyService) {
        this.clock = (clock == null ? new Date() : clock);
        this.hashAlgo = (hashAlgo == null ? HashAlgorithm.sha1 : hashAlgo);
        this.signaturePolicyService = signaturePolicyService;
        this.dataObjectFormatMimeTypes = new HashMap<String, String>();
    }

    public void postSign(SignatureType signatureElement,
            List<X509Certificate> signingCertificateChain) {
        LOG.log(POILogger.DEBUG, "postSign");
    }

    public void preSign(XMLSignatureFactory signatureFactory,
            String signatureId,
            List<X509Certificate> signingCertificateChain,
            List<Reference> references, List<XMLObject> objects)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        LOG.log(POILogger.DEBUG, "preSign");

        // QualifyingProperties
        QualifyingPropertiesDocument qualDoc = QualifyingPropertiesDocument.Factory.newInstance();
        QualifyingPropertiesType qualifyingProperties = qualDoc.addNewQualifyingProperties();
        qualifyingProperties.setTarget("#" + signatureId);
        
        // SignedProperties
        SignedPropertiesType signedProperties = qualifyingProperties.addNewSignedProperties();
        String signedPropertiesId;
        if (null != this.idSignedProperties) {
            signedPropertiesId = this.idSignedProperties;
        } else {
            signedPropertiesId = signatureId + "-xades";
        }
        signedProperties.setId(signedPropertiesId);

        // SignedSignatureProperties
        SignedSignaturePropertiesType signedSignatureProperties = signedProperties.addNewSignedSignatureProperties();

        // SigningTime
        Calendar xmlGregorianCalendar = Calendar.getInstance();
        xmlGregorianCalendar.setTimeZone(TimeZone.getTimeZone("Z"));
        xmlGregorianCalendar.setTime(this.clock);
        xmlGregorianCalendar.clear(Calendar.MILLISECOND);
        signedSignatureProperties.setSigningTime(xmlGregorianCalendar);

        // SigningCertificate
        if (null == signingCertificateChain
                || signingCertificateChain.isEmpty()) {
            throw new RuntimeException("no signing certificate chain available");
        }
        CertIDListType signingCertificates = signedSignatureProperties.addNewSigningCertificate();
        CertIDType certId = signingCertificates.addNewCert();
        X509Certificate signingCertificate = signingCertificateChain.get(0);
        setCertID(certId, signingCertificate, this.hashAlgo, this.issuerNameNoReverseOrder);

        // ClaimedRole
        if (null != this.role && false == this.role.isEmpty()) {
            SignerRoleType signerRole = signedSignatureProperties.addNewSignerRole();
            signedSignatureProperties.setSignerRole(signerRole);
            ClaimedRolesListType claimedRolesList = signerRole.addNewClaimedRoles();
            AnyType claimedRole = claimedRolesList.addNewClaimedRole();
            XmlString roleString = XmlString.Factory.newInstance();
            roleString.setStringValue(this.role);
            SignatureInfo.insertXChild(claimedRole, roleString);
        }

        // XAdES-EPES
        if (null != this.signaturePolicyService) {
            SignaturePolicyIdentifierType signaturePolicyIdentifier =
                signedSignatureProperties.addNewSignaturePolicyIdentifier();
            
            SignaturePolicyIdType signaturePolicyId = signaturePolicyIdentifier.addNewSignaturePolicyId();

            ObjectIdentifierType objectIdentifier = signaturePolicyId.addNewSigPolicyId();
            objectIdentifier.setDescription(this.signaturePolicyService.getSignaturePolicyDescription());
            
            IdentifierType identifier = objectIdentifier.addNewIdentifier();
            identifier.setStringValue(this.signaturePolicyService.getSignaturePolicyIdentifier());

            byte[] signaturePolicyDocumentData = this.signaturePolicyService.getSignaturePolicyDocument();
            DigestAlgAndValueType sigPolicyHash = signaturePolicyId.addNewSigPolicyHash();
            setDigestAlgAndValue(sigPolicyHash, signaturePolicyDocumentData, this.hashAlgo);

            String signaturePolicyDownloadUrl = this.signaturePolicyService
                    .getSignaturePolicyDownloadUrl();
            if (null != signaturePolicyDownloadUrl) {
                SigPolicyQualifiersListType sigPolicyQualifiers = signaturePolicyId.addNewSigPolicyQualifiers(); 
                AnyType sigPolicyQualifier = sigPolicyQualifiers.addNewSigPolicyQualifier();
                XmlString spUriElement = XmlString.Factory.newInstance();
                spUriElement.setStringValue(signaturePolicyDownloadUrl);
                SignatureInfo.insertXChild(sigPolicyQualifier, spUriElement);
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
        List<XMLStructure> xadesObjectContent = new LinkedList<XMLStructure>();
        Element qualDocEl = (Element)qualifyingProperties.getDomNode();
        qualDocEl.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xd", "http://uri.etsi.org/01903/v1.3.2#");
        setPrefix(qualifyingProperties, "http://uri.etsi.org/01903/v1.3.2#", "xd");
        xadesObjectContent.add(new DOMStructure(qualDocEl));
        XMLObject xadesObject = signatureFactory.newXMLObject(xadesObjectContent, null, null, null);
        objects.add(xadesObject);

        // add XAdES ds:Reference
        DigestMethod digestMethod = signatureFactory.newDigestMethod(hashAlgo.xmlSignUri, null);
        List<Transform> transforms = new LinkedList<Transform>();
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

}