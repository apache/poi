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

import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XADES_132_NS;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.OOXMLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.Office2010SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.SignaturePolicyService;
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampServiceValidator;
import org.apache.poi.poifs.crypt.dsig.spi.AddressDTO;
import org.apache.poi.poifs.crypt.dsig.spi.IdentityDTO;
import org.w3c.dom.events.EventListener;

/**
 * This class bundles the configuration options used for the existing
 * signature facets.
 * Apart of the opc-package (thread local) most values will probably be constant, so
 * it might be configured centrally (e.g. by spring) 
 */
public class SignatureConfig {
    
    public static interface SignatureConfigurable {
        void setSignatureConfig(SignatureConfig signatureConfig);        
    }

    private ThreadLocal<OPCPackage> opcPackage = new ThreadLocal<OPCPackage>();
    
    private List<SignatureFacet> signatureFacets = new ArrayList<SignatureFacet>();
    private HashAlgorithm digestAlgo = HashAlgorithm.sha1;
    private Date executionTime = new Date();
    private PrivateKey key;
    private List<X509Certificate> signingCertificateChain;
    private IdentityDTO identity;
    private AddressDTO address;
    private byte[] photo;

    /**
     * the optional signature policy service used for XAdES-EPES.
     */
    private SignaturePolicyService signaturePolicyService;
    private URIDereferencer uriDereferencer = new OOXMLURIDereferencer();
    private String canonicalizationMethod = CanonicalizationMethod.INCLUSIVE;
    
    private boolean includeEntireCertificateChain = true;
    private boolean includeIssuerSerial = false;
    private boolean includeKeyValue = false;
    
    private TimeStampService tspService = new TSPTimeStampService();
    // timestamp service provider URL
    private String tspUrl;
    private boolean tspOldProtocol = false;
    private HashAlgorithm tspDigestAlgo = HashAlgorithm.sha1;
    private String tspUser;
    private String tspPass;
    private TimeStampServiceValidator tspValidator;
    /**
     * the optional TSP request policy OID.
     */
    private String tspRequestPolicy = "1.3.6.1.4.1.13762.3";
    private String userAgent = "POI XmlSign Service TSP Client";
    private String proxyUrl;
    
    /**
     * the optional revocation data service used for XAdES-C and XAdES-X-L.
     * When <code>null</code> the signature will be limited to XAdES-T only.
     */
    private RevocationDataService revocationDataService;
    private HashAlgorithm xadesDigestAlgo = HashAlgorithm.sha1;
    private String xadesRole = null;
    private String xadesSignatureId = null;
    private boolean xadesSignaturePolicyImplied = true;

    /**
     * Work-around for Office 2010 IssuerName encoding.
     */
    private boolean xadesIssuerNameNoReverseOrder = true;

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
    
    /**
     * The process of signing includes the marshalling of xml structures.
     * This also includes the canonicalization. Currently this leads to problems 
     * with certain namespaces, so this EventListener is used to interfere
     * with the marshalling process.
     */
    EventListener signCreationListener = null;

    /**
     * Map of namespace uris to prefix
     * If a mapping is specified, the corresponding elements will be prefixed
     */
    Map<String,String> namespacePrefixes = new HashMap<String,String>();
    
    protected void init(boolean onlyValidation) {
        if (uriDereferencer == null) {
            throw new EncryptedDocumentException("uriDereferencer is null");
        }
        if (opcPackage == null) {
            throw new EncryptedDocumentException("opcPackage is null");
        }
        if (uriDereferencer instanceof SignatureConfigurable) {
            ((SignatureConfigurable)uriDereferencer).setSignatureConfig(this);
        }
        if (namespacePrefixes.isEmpty()) {
            /*
             * OOo doesn't like ds namespaces so per default prefixing is off.
             */
            // namespacePrefixes.put(XML_DIGSIG_NS, "");
            namespacePrefixes.put(OO_DIGSIG_NS, "mdssi");
            namespacePrefixes.put(XADES_132_NS, "xd");
        }
        
        if (onlyValidation) return;

        if (signCreationListener == null) {
            signCreationListener = new SignatureMarshalListener();
        }
        
        if (signCreationListener instanceof SignatureConfigurable) {
            ((SignatureConfigurable)signCreationListener).setSignatureConfig(this);
        }
        
        if (tspService != null) {
            tspService.setSignatureConfig(this);
        }
        
        if (xadesSignatureId == null || xadesSignatureId.isEmpty()) {
            xadesSignatureId = "idSignedProperties";
        }

        if (signatureFacets.isEmpty()) {
            addSignatureFacet(new OOXMLSignatureFacet());
            addSignatureFacet(new KeyInfoSignatureFacet());
            addSignatureFacet(new XAdESSignatureFacet());
            addSignatureFacet(new Office2010SignatureFacet());
        }

        for (SignatureFacet sf : signatureFacets) {
            sf.setSignatureConfig(this);
        }
    }
    
    public void addSignatureFacet(SignatureFacet sf) {
        signatureFacets.add(sf);
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
        return opcPackage.get();
    }
    public void setOpcPackage(OPCPackage opcPackage) {
        this.opcPackage.set(opcPackage);
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
    public String getTspUrl() {
        return tspUrl;
    }
    public void setTspUrl(String tspUrl) {
        this.tspUrl = tspUrl;
    }
    public boolean isTspOldProtocol() {
        return tspOldProtocol;
    }
    public void setTspOldProtocol(boolean tspOldProtocol) {
        this.tspOldProtocol = tspOldProtocol;
    }
    public HashAlgorithm getTspDigestAlgo() {
        return tspDigestAlgo;
    }
    public void setTspDigestAlgo(HashAlgorithm tspDigestAlgo) {
        this.tspDigestAlgo = tspDigestAlgo;
    }
    public String getProxyUrl() {
        return proxyUrl;
    }
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    public TimeStampService getTspService() {
        return tspService;
    }
    public void setTspService(TimeStampService tspService) {
        this.tspService = tspService;
    }
    public String getTspUser() {
        return tspUser;
    }
    public void setTspUser(String tspUser) {
        this.tspUser = tspUser;
    }
    public String getTspPass() {
        return tspPass;
    }
    public void setTspPass(String tspPass) {
        this.tspPass = tspPass;
    }
    public TimeStampServiceValidator getTspValidator() {
        return tspValidator;
    }
    public void setTspValidator(TimeStampServiceValidator tspValidator) {
        this.tspValidator = tspValidator;
    }
    public RevocationDataService getRevocationDataService() {
        return revocationDataService;
    }
    public void setRevocationDataService(RevocationDataService revocationDataService) {
        this.revocationDataService = revocationDataService;
    }
    public HashAlgorithm getXadesDigestAlgo() {
        return xadesDigestAlgo;
    }
    public void setXadesDigestAlgo(HashAlgorithm xadesDigestAlgo) {
        this.xadesDigestAlgo = xadesDigestAlgo;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getTspRequestPolicy() {
        return tspRequestPolicy;
    }
    public void setTspRequestPolicy(String tspRequestPolicy) {
        this.tspRequestPolicy = tspRequestPolicy;
    }
    public boolean isIncludeEntireCertificateChain() {
        return includeEntireCertificateChain;
    }
    public void setIncludeEntireCertificateChain(boolean includeEntireCertificateChain) {
        this.includeEntireCertificateChain = includeEntireCertificateChain;
    }
    public boolean isIncludeIssuerSerial() {
        return includeIssuerSerial;
    }
    public void setIncludeIssuerSerial(boolean includeIssuerSerial) {
        this.includeIssuerSerial = includeIssuerSerial;
    }
    public boolean isIncludeKeyValue() {
        return includeKeyValue;
    }
    public void setIncludeKeyValue(boolean includeKeyValue) {
        this.includeKeyValue = includeKeyValue;
    }
    public String getXadesRole() {
        return xadesRole;
    }
    public void setXadesRole(String xadesRole) {
        this.xadesRole = xadesRole;
    }
    public String getXadesSignatureId() {
        return xadesSignatureId;
    }
    public void setXadesSignatureId(String xadesSignatureId) {
        this.xadesSignatureId = xadesSignatureId;
    }
    public boolean isXadesSignaturePolicyImplied() {
        return xadesSignaturePolicyImplied;
    }
    public void setXadesSignaturePolicyImplied(boolean xadesSignaturePolicyImplied) {
        this.xadesSignaturePolicyImplied = xadesSignaturePolicyImplied;
    }
    public boolean isXadesIssuerNameNoReverseOrder() {
        return xadesIssuerNameNoReverseOrder;
    }
    public void setXadesIssuerNameNoReverseOrder(boolean xadesIssuerNameNoReverseOrder) {
        this.xadesIssuerNameNoReverseOrder = xadesIssuerNameNoReverseOrder;
    }
    public EventListener getSignCreationListener() {
        return signCreationListener;
    }
    public void setSignCreationListener(EventListener signCreationListener) {
        this.signCreationListener = signCreationListener;
    }
    public Map<String, String> getNamespacePrefixes() {
        return namespacePrefixes;
    }
    public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }
}
