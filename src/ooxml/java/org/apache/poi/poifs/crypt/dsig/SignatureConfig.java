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
import javax.xml.crypto.dsig.DigestMethod;

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

    /**
     * the optional signature policy service used for XAdES-EPES.
     */
    private SignaturePolicyService signaturePolicyService;
    private URIDereferencer uriDereferencer = new OOXMLURIDereferencer();
    private String canonicalizationMethod = CanonicalizationMethod.INCLUSIVE;
    
    private boolean includeEntireCertificateChain = true;
    private boolean includeIssuerSerial = false;
    private boolean includeKeyValue = false;
    
    /**
     * the time-stamp service used for XAdES-T and XAdES-X.
     */
    private TimeStampService tspService = new TSPTimeStampService();
    /**
     * timestamp service provider URL
     */
    private String tspUrl;
    private boolean tspOldProtocol = false;
    /**
     * if not defined, it's the same as the main digest
     */
    private HashAlgorithm tspDigestAlgo = null;
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
    /**
     * if not defined, it's the same as the main digest
     */
    private HashAlgorithm xadesDigestAlgo = null;
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
    EventListener signatureMarshalListener = null;

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

        if (signatureMarshalListener == null) {
            signatureMarshalListener = new SignatureMarshalListener();
        }
        
        if (signatureMarshalListener instanceof SignatureConfigurable) {
            ((SignatureConfigurable)signatureMarshalListener).setSignatureConfig(this);
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
        this.packageSignatureId = nvl(packageSignatureId,"xmldsig-"+UUID.randomUUID());
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
        return nvl(tspDigestAlgo,digestAlgo);
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
        return nvl(xadesDigestAlgo,digestAlgo);
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
    public EventListener getSignatureMarshalListener() {
        return signatureMarshalListener;
    }
    public void setSignatureMarshalListener(EventListener signatureMarshalListener) {
        this.signatureMarshalListener = signatureMarshalListener;
    }
    public Map<String, String> getNamespacePrefixes() {
        return namespacePrefixes;
    }
    public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }
    protected static <T> T nvl(T value, T defaultValue)  {
        return value == null ? defaultValue : value;
    }
    public byte[] getHashMagic() {
        // see https://www.ietf.org/rfc/rfc3110.txt
        // RSA/SHA1 SIG Resource Records
        byte result[];
        switch (getDigestAlgo()) {
        case sha1: result = new byte[]
            { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x0e
            , 0x03, 0x02, 0x1a, 0x04, 0x14 };
            break;
        case sha224: result = new byte[] 
            { 0x30, 0x2b, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
            , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x04, 0x04, 0x1c };
            break;
        case sha256: result = new byte[]
            { 0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
            , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };
            break;
        case sha384: result = new byte[]
            { 0x30, 0x3f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
            , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x04, 0x30 };
            break;
        case sha512: result  = new byte[]
            { 0x30, 0x4f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
            , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x04, 0x40 };
            break;
        case ripemd128: result = new byte[]
            { 0x30, 0x1b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24
            , 0x03, 0x02, 0x02, 0x04, 0x10 };
            break;
        case ripemd160: result = new byte[]
            { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24
            , 0x03, 0x02, 0x01, 0x04, 0x14 };
            break;
        // case ripemd256: result = new byte[]
        //    { 0x30, 0x2b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24
        //    , 0x03, 0x02, 0x03, 0x04, 0x20 };
        //    break;
        default: throw new EncryptedDocumentException("Hash algorithm "
            +getDigestAlgo()+" not supported for signing.");
        }
        
        return result;
    }

    public String getSignatureMethod() {
        switch (getDigestAlgo()) {
        case sha1:   return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
        case sha224: return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224;
        case sha256: return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
        case sha384: return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384;
        case sha512: return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512;
        case ripemd160: return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160;
        default: throw new EncryptedDocumentException("Hash algorithm "
            +getDigestAlgo()+" not supported for signing.");
        }
    }
    
    public String getDigestMethodUri() {
        return getDigestMethodUri(getDigestAlgo());
    }
    
    public static String getDigestMethodUri(HashAlgorithm digestAlgo) {
        switch (digestAlgo) {
        case sha1:   return DigestMethod.SHA1;
        case sha224: return "http://www.w3.org/2001/04/xmldsig-more#sha224";
        case sha256: return DigestMethod.SHA256;
        case sha384: return "http://www.w3.org/2001/04/xmldsig-more#sha384";
        case sha512: return DigestMethod.SHA512;
        case ripemd160: return DigestMethod.RIPEMD160;
        default: throw new EncryptedDocumentException("Hash algorithm "
            +digestAlgo+" not supported for signing.");
        }
    }
    
}
