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

package org.apache.poi.poifs.crypt.dsig;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.OOXMLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.Office2010SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.SignaturePolicyService;
import org.apache.poi.poifs.crypt.dsig.spi.AddressDTO;
import org.apache.poi.poifs.crypt.dsig.spi.IdentityDTO;

public class SignatureInfoConfig {
    
    private List<SignatureFacet> signatureFacets = new ArrayList<SignatureFacet>();
    private HashAlgorithm digestAlgo = HashAlgorithm.sha1;
    private Date executionTime = new Date();
    private OPCPackage opcPackage;
    private PrivateKey key;
    private List<X509Certificate> signingCertificateChain;
    private IdentityDTO identity;
    private AddressDTO address;
    private byte[] photo;
    private SignaturePolicyService signaturePolicyService;
    private URIDereferencer uriDereferencer;
    private String signatureNamespacePrefix;
    private String canonicalizationMethod = CanonicalizationMethod.INCLUSIVE;

    /**
     * The signature Id attribute value used to create the XML signature. A
     * <code>null</code> value will trigger an automatically generated signature Id.
     */
    private String packageSignatureId = "idPackageSignature";
    
    /**
     * Gives back the human-readable description of what the citizen will be
     * signing. The default value is "Office OpenXML Document".
     */
    private String signatureDescription = "Office OpenXML Document";

    public SignatureInfoConfig() {
        OOXMLURIDereferencer uriDereferencer = new OOXMLURIDereferencer();
        uriDereferencer.setSignatureConfig(this);
        this.uriDereferencer = uriDereferencer;
    }
    
    public void addSignatureFacet(SignatureFacet sf) {
        signatureFacets.add(sf);
    }
    
    public void addDefaultFacets() {
        addSignatureFacet(new OOXMLSignatureFacet(this));
        addSignatureFacet(new KeyInfoSignatureFacet(true, false, false));

        XAdESSignatureFacet xadesSignatureFacet = new XAdESSignatureFacet(this);
        xadesSignatureFacet.setIdSignedProperties("idSignedProperties");
        xadesSignatureFacet.setSignaturePolicyImplied(true);
        /*
         * Work-around for Office 2010.
         */
        xadesSignatureFacet.setIssuerNameNoReverseOrder(true);
        addSignatureFacet(xadesSignatureFacet);
        addSignatureFacet(new Office2010SignatureFacet());
    }
    
    /**
     * Gives back the used XAdES signature facet.
     * 
     * @return
     */
    public XAdESSignatureFacet getXAdESSignatureFacet() {
        for (SignatureFacet sf : getSignatureFacets()) {
            if (sf instanceof XAdESSignatureFacet) {
                return (XAdESSignatureFacet)sf;
            }
        }
        return null;
    }
    
    
    public List<SignatureFacet> getSignatureFacets() {
        return signatureFacets;
    }
    public void setSignatureFacets(List<SignatureFacet> signatureFacets) {
        this.signatureFacets = signatureFacets;
    }
    public HashAlgorithm getDigestAlgo() {
        return digestAlgo;
    }
    public void setDigestAlgo(HashAlgorithm digestAlgo) {
        this.digestAlgo = digestAlgo;
    }
    public OPCPackage getOpcPackage() {
        return opcPackage;
    }
    public void setOpcPackage(OPCPackage opcPackage) {
        this.opcPackage = opcPackage;
    }
    public PrivateKey getKey() {
        return key;
    }
    public void setKey(PrivateKey key) {
        this.key = key;
    }
    public List<X509Certificate> getSigningCertificateChain() {
        return signingCertificateChain;
    }
    public void setSigningCertificateChain(
            List<X509Certificate> signingCertificateChain) {
        this.signingCertificateChain = signingCertificateChain;
    }
    public IdentityDTO getIdentity() {
        return identity;
    }
    public void setIdentity(IdentityDTO identity) {
        this.identity = identity;
    }
    public AddressDTO getAddress() {
        return address;
    }
    public void setAddress(AddressDTO address) {
        this.address = address;
    }
    public byte[] getPhoto() {
        return photo;
    }
    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
    public Date getExecutionTime() {
        return executionTime;
    }
    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }
    public SignaturePolicyService getSignaturePolicyService() {
        return signaturePolicyService;
    }
    public void setSignaturePolicyService(SignaturePolicyService signaturePolicyService) {
        this.signaturePolicyService = signaturePolicyService;
    }
    public URIDereferencer getUriDereferencer() {
        return uriDereferencer;
    }
    public void setUriDereferencer(URIDereferencer uriDereferencer) {
        this.uriDereferencer = uriDereferencer;
    }
    public String getSignatureDescription() {
        return signatureDescription;
    }
    public void setSignatureDescription(String signatureDescription) {
        this.signatureDescription = signatureDescription;
    }
    public String getSignatureNamespacePrefix() {
        return signatureNamespacePrefix;
    }
    public void setSignatureNamespacePrefix(String signatureNamespacePrefix) {
        this.signatureNamespacePrefix = signatureNamespacePrefix;
    }
    public String getCanonicalizationMethod() {
        return canonicalizationMethod;
    }
    public void setCanonicalizationMethod(String canonicalizationMethod) {
        this.canonicalizationMethod = canonicalizationMethod;
    }
    public String getPackageSignatureId() {
        return packageSignatureId;
    }
    public void setPackageSignatureId(String packageSignatureId) {
        this.packageSignatureId = (packageSignatureId != null)
            ? packageSignatureId
            : "xmldsig-" + UUID.randomUUID();
    }
}
