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
import java.security.Provider;
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
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;

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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.events.EventListener;

/**
 * This class bundles the configuration options used for the existing
 * signature facets.
 * Apart of the thread local members (e.g. opc-package) most values will probably be constant, so
 * it might be configured centrally (e.g. by spring) 
 */
public class SignatureConfig {

    private static final POILogger LOG = POILogFactory.getLogger(SignatureConfig.class);
    
    public static interface SignatureConfigurable {
        void setSignatureConfig(SignatureConfig signatureConfig);        
    }

    private ThreadLocal<OPCPackage> opcPackage = new ThreadLocal<OPCPackage>();
    private ThreadLocal<XMLSignatureFactory> signatureFactory = new ThreadLocal<XMLSignatureFactory>();
    private ThreadLocal<KeyInfoFactory> keyInfoFactory = new ThreadLocal<KeyInfoFactory>();
    private ThreadLocal<Provider> provider = new ThreadLocal<Provider>();
    
    private List<SignatureFacet> signatureFacets = new ArrayList<SignatureFacet>();
    private HashAlgorithm digestAlgo = HashAlgorithm.sha1;
    private Date executionTime = new Date();
    private PrivateKey key;
    private List<X509Certificate> signingCertificateChain;

    /**
     * the optional signature policy service used for XAdES-EPES.
     */
    private SignaturePolicyService signaturePolicyService;
    private URIDereferencer uriDereferencer = null;
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
    private String xadesSignatureId = "idSignedProperties";
    private boolean xadesSignaturePolicyImplied = true;
    private String xadesCanonicalizationMethod = CanonicalizationMethod.EXCLUSIVE;

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
    
    /**
     * Inits and checks the config object.
     * If not set previously, complex configuration properties also get 
     * created/initialized via this initialization call.
     *
     * @param onlyValidation if true, only a subset of the properties
     * is initialized, which are necessary for validation. If false,
     * also the other properties needed for signing are been taken care of
     */
    protected void init(boolean onlyValidation) {
        if (opcPackage == null) {
            throw new EncryptedDocumentException("opcPackage is null");
        }
        if (uriDereferencer == null) {
            uriDereferencer = new OOXMLURIDereferencer();
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
    
    /**
     * @param signatureFacet the signature facet is appended to facet list 
     */
    public void addSignatureFacet(SignatureFacet signatureFacet) {
        signatureFacets.add(signatureFacet);
    }
    
    /**
     * @return the list of facets, may be empty when the config object is not initialized
     */
    public List<SignatureFacet> getSignatureFacets() {
        return signatureFacets;
    }

    /**
     * @param signatureFacets the new list of facets
     */
    public void setSignatureFacets(List<SignatureFacet> signatureFacets) {
        this.signatureFacets = signatureFacets;
    }

    /**
     * @return the main digest algorithm, defaults to sha-1
     */
    public HashAlgorithm getDigestAlgo() {
        return digestAlgo;
    }

    /**
     * @param digestAlgo the main digest algorithm
     */
    public void setDigestAlgo(HashAlgorithm digestAlgo) {
        this.digestAlgo = digestAlgo;
    }
    
    /**
     * @return the opc package to be used by this thread, stored as thread-local
     */
    public OPCPackage getOpcPackage() {
        return opcPackage.get();
    }
    
    /**
     * @param opcPackage the opc package to be handled by this thread, stored as thread-local
     */
    public void setOpcPackage(OPCPackage opcPackage) {
        this.opcPackage.set(opcPackage);
    }

    /**
     * @return the private key
     */
    public PrivateKey getKey() {
        return key;
    }

    /**
     * @param key the private key
     */
    public void setKey(PrivateKey key) {
        this.key = key;
    }

    /**
     * @return the certificate chain, index 0 is usually the certificate matching
     * the private key
     */
    public List<X509Certificate> getSigningCertificateChain() {
        return signingCertificateChain;
    }

    /**
     * @param signingCertificateChain the certificate chain, index 0 should be
     * the certificate matching the private key
     */
    public void setSigningCertificateChain(
            List<X509Certificate> signingCertificateChain) {
        this.signingCertificateChain = signingCertificateChain;
    }

    /**
     * @return the time at which the document is signed, also used for the timestamp service.
     * defaults to now
     */
    public Date getExecutionTime() {
        return executionTime;
    }

    /**
     * @param executionTime sets the time at which the document ought to be signed
     */
    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }
    
    /**
     * @return the service to be used for XAdES-EPES properties. There's no default implementation
     */
    public SignaturePolicyService getSignaturePolicyService() {
        return signaturePolicyService;
    }

    /**
     * @param signaturePolicyService the service to be used for XAdES-EPES properties
     */
    public void setSignaturePolicyService(SignaturePolicyService signaturePolicyService) {
        this.signaturePolicyService = signaturePolicyService;
    }

    /**
     * @return the dereferencer used for Reference/@URI attributes, defaults to {@link OOXMLURIDereferencer}
     */
    public URIDereferencer getUriDereferencer() {
        return uriDereferencer;
    }

    /**
     * @param uriDereferencer the dereferencer used for Reference/@URI attributes
     */
    public void setUriDereferencer(URIDereferencer uriDereferencer) {
        this.uriDereferencer = uriDereferencer;
    }

    /**
     * @return Gives back the human-readable description of what the citizen
     * will be signing. The default value is "Office OpenXML Document".
     */
    public String getSignatureDescription() {
        return signatureDescription;
    }

    /**
     * @param signatureDescription the human-readable description of
     * what the citizen will be signing.
     */
    public void setSignatureDescription(String signatureDescription) {
        this.signatureDescription = signatureDescription;
    }
    
    /**
     * @return the default canonicalization method, defaults to INCLUSIVE
     */
    public String getCanonicalizationMethod() {
        return canonicalizationMethod;
    }
    
    /**
     * @param canonicalizationMethod the default canonicalization method
     */
    public void setCanonicalizationMethod(String canonicalizationMethod) {
        this.canonicalizationMethod = canonicalizationMethod;
    }

    /**
     * @return The signature Id attribute value used to create the XML signature.
     * Defaults to "idPackageSignature"
     */
    public String getPackageSignatureId() {
        return packageSignatureId;
    }

    /**
     * @param packageSignatureId The signature Id attribute value used to create the XML signature.
     * A <code>null</code> value will trigger an automatically generated signature Id.
     */
    public void setPackageSignatureId(String packageSignatureId) {
        this.packageSignatureId = nvl(packageSignatureId,"xmldsig-"+UUID.randomUUID());
    }

    /**
     * @return the url of the timestamp provider (TSP)
     */
    public String getTspUrl() {
        return tspUrl;
    }

    /**
     * @param tspUrl the url of the timestamp provider (TSP)
     */
    public void setTspUrl(String tspUrl) {
        this.tspUrl = tspUrl;
    }
    
    /**
     * @return if true, uses timestamp-request/response mimetype,
     * if false, timestamp-query/reply mimetype 
     */
    public boolean isTspOldProtocol() {
        return tspOldProtocol;
    }
    
    /**
     * @param tspOldProtocol defines the timestamp-protocol mimetype
     * @see #isTspOldProtocol
     */
    public void setTspOldProtocol(boolean tspOldProtocol) {
        this.tspOldProtocol = tspOldProtocol;
    }
    
    /**
     * @return the hash algorithm to be used for the timestamp entry.
     * Defaults to the hash algorithm of the main entry
     */
    public HashAlgorithm getTspDigestAlgo() {
        return nvl(tspDigestAlgo,digestAlgo);
    }
    
    /**
     * @param tspDigestAlgo the algorithm to be used for the timestamp entry.
     * if <code>null</code>, the hash algorithm of the main entry
     */
    public void setTspDigestAlgo(HashAlgorithm tspDigestAlgo) {
        this.tspDigestAlgo = tspDigestAlgo;
    }

    /**
     * @return the proxy url to be used for all communications.
     * Currently this affects the timestamp service
     */
    public String getProxyUrl() {
        return proxyUrl;
    }
    
    /**
     * @param proxyUrl the proxy url to be used for all communications.
     * Currently this affects the timestamp service
     */
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    
    /**
     * @return the timestamp service. Defaults to {@link TSPTimeStampService}
     */
    public TimeStampService getTspService() {
        return tspService;
    }
    
    /**
     * @param tspService the timestamp service
     */
    public void setTspService(TimeStampService tspService) {
        this.tspService = tspService;
    }
    
    /**
     * @return the user id for the timestamp service - currently only basic authorization is supported
     */
    public String getTspUser() {
        return tspUser;
    }
    
    /**
     * @param tspUser the user id for the timestamp service - currently only basic authorization is supported
     */
    public void setTspUser(String tspUser) {
        this.tspUser = tspUser;
    }
    
    /**
     * @return the password for the timestamp service
     */
    public String getTspPass() {
        return tspPass;
    }
    
    /**
     * @param tspPass the password for the timestamp service
     */
    public void setTspPass(String tspPass) {
        this.tspPass = tspPass;
    }
    
    /**
     * @return the validator for the timestamp service (certificate)
     */
    public TimeStampServiceValidator getTspValidator() {
        return tspValidator;
    }
    
    /**
     * @param tspValidator the validator for the timestamp service (certificate)
     */
    public void setTspValidator(TimeStampServiceValidator tspValidator) {
        this.tspValidator = tspValidator;
    }

    /**
     * @return the optional revocation data service used for XAdES-C and XAdES-X-L.
     * When <code>null</code> the signature will be limited to XAdES-T only.
     */
    public RevocationDataService getRevocationDataService() {
        return revocationDataService;
    }

    /**
     * @param revocationDataService the optional revocation data service used for XAdES-C and XAdES-X-L.
     * When <code>null</code> the signature will be limited to XAdES-T only.
     */
    public void setRevocationDataService(RevocationDataService revocationDataService) {
        this.revocationDataService = revocationDataService;
    }

    /**
     * @return hash algorithm used for XAdES. Defaults to the {@link #getDigestAlgo()}
     */
    public HashAlgorithm getXadesDigestAlgo() {
        return nvl(xadesDigestAlgo,digestAlgo);
    }
    
    /**
     * @param xadesDigestAlgo hash algorithm used for XAdES.
     * When <code>null</code>, defaults to {@link #getDigestAlgo()}
     */
    public void setXadesDigestAlgo(HashAlgorithm xadesDigestAlgo) {
        this.xadesDigestAlgo = xadesDigestAlgo;
    }

    /**
     * @return the user agent used for http communication (e.g. to the TSP)
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * @param userAgent the user agent used for http communication (e.g. to the TSP)
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the asn.1 object id for the tsp request policy.
     * Defaults to <code>1.3.6.1.4.1.13762.3</code>
     */
    public String getTspRequestPolicy() {
        return tspRequestPolicy;
    }
    
    /**
     * @param tspRequestPolicy the asn.1 object id for the tsp request policy.
     */
    public void setTspRequestPolicy(String tspRequestPolicy) {
        this.tspRequestPolicy = tspRequestPolicy;
    }

    /**
     * @return true, if the whole certificate chain is included in the signature.
     * When false, only the signer cert will be included 
     */
    public boolean isIncludeEntireCertificateChain() {
        return includeEntireCertificateChain;
    }

    /**
     * @param includeEntireCertificateChain if true, include the whole certificate chain.
     * If false, only include the signer cert
     */
    public void setIncludeEntireCertificateChain(boolean includeEntireCertificateChain) {
        this.includeEntireCertificateChain = includeEntireCertificateChain;
    }

    /**
     * @return if true, issuer serial number is included
     */
    public boolean isIncludeIssuerSerial() {
        return includeIssuerSerial;
    }

    /**
     * @param includeIssuerSerial if true, issuer serial number is included
     */
    public void setIncludeIssuerSerial(boolean includeIssuerSerial) {
        this.includeIssuerSerial = includeIssuerSerial;
    }

    /**
     * @return if true, the key value of the public key (certificate) is included
     */
    public boolean isIncludeKeyValue() {
        return includeKeyValue;
    }

    /**
     * @param includeKeyValue if true, the key value of the public key (certificate) is included
     */
    public void setIncludeKeyValue(boolean includeKeyValue) {
        this.includeKeyValue = includeKeyValue;
    }

    /**
     * @return the xades role element. If <code>null</code> the claimed role element is omitted.
     * Defaults to <code>null</code>
     */
    public String getXadesRole() {
        return xadesRole;
    }

    /**
     * @param xadesRole the xades role element. If <code>null</code> the claimed role element is omitted.
     */
    public void setXadesRole(String xadesRole) {
        this.xadesRole = xadesRole;
    }

    /**
     * @return the Id for the XAdES SignedProperties element.
     * Defaults to <code>idSignedProperties</code>
     */
    public String getXadesSignatureId() {
        return nvl(xadesSignatureId, "idSignedProperties");
    }

    /**
     * @param xadesSignatureId the Id for the XAdES SignedProperties element.
     * When <code>null</code> defaults to <code>idSignedProperties</code>
     */
    public void setXadesSignatureId(String xadesSignatureId) {
        this.xadesSignatureId = xadesSignatureId;
    }

    /**
     * @return when true, include the policy-implied block.
     * Defaults to <code>true</code>
     */
    public boolean isXadesSignaturePolicyImplied() {
        return xadesSignaturePolicyImplied;
    }

    /**
     * @param xadesSignaturePolicyImplied when true, include the policy-implied block
     */
    public void setXadesSignaturePolicyImplied(boolean xadesSignaturePolicyImplied) {
        this.xadesSignaturePolicyImplied = xadesSignaturePolicyImplied;
    }

    /**
     * Make sure the DN is encoded using the same order as present
     * within the certificate. This is an Office2010 work-around.
     * Should be reverted back.
     * 
     * XXX: not correct according to RFC 4514.
     *
     * @return when true, the issuer DN is used instead of the issuer X500 principal
     */
    public boolean isXadesIssuerNameNoReverseOrder() {
        return xadesIssuerNameNoReverseOrder;
    }

    /**
     * @param xadesIssuerNameNoReverseOrder when true, the issuer DN instead of the issuer X500 prinicpal is used
     */
    public void setXadesIssuerNameNoReverseOrder(boolean xadesIssuerNameNoReverseOrder) {
        this.xadesIssuerNameNoReverseOrder = xadesIssuerNameNoReverseOrder;
    }

    
    /**
     * @return the event listener which is active while xml structure for
     * the signature is created.
     * Defaults to {@link SignatureMarshalListener}
     */
    public EventListener getSignatureMarshalListener() {
        return signatureMarshalListener;
    }

    /**
     * @param signatureMarshalListener the event listener watching the xml structure
     * generation for the signature
     */
    public void setSignatureMarshalListener(EventListener signatureMarshalListener) {
        this.signatureMarshalListener = signatureMarshalListener;
    }

    /**
     * @return the map of namespace uri (key) to prefix (value)
     */
    public Map<String, String> getNamespacePrefixes() {
        return namespacePrefixes;
    }

    /**
     * @param namespacePrefixes the map of namespace uri (key) to prefix (value)
     */
    public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }

    /**
     * helper method for null/default value handling
     * @param value
     * @param defaultValue
     * @return if value is not null, return value otherwise defaultValue
     */
    protected static <T> T nvl(T value, T defaultValue)  {
        return value == null ? defaultValue : value;
    }

    /**
     * Each digest method has its own IV (initial vector)
     *
     * @return the IV depending on the main digest method
     */
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

    /**
     * @return the uri for the signature method, i.e. currently only rsa is
     * supported, so it's the rsa variant of the main digest
     */
    public String getSignatureMethodUri() {
        switch (getDigestAlgo()) {
        case sha1:   return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
        case sha224: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224;
        case sha256: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
        case sha384: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384;
        case sha512: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512;
        case ripemd160: return XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160;
        default: throw new EncryptedDocumentException("Hash algorithm "
            +getDigestAlgo()+" not supported for signing.");
        }
    }
    
    /**
     * @return the uri for the main digest
     */
    public String getDigestMethodUri() {
        return getDigestMethodUri(getDigestAlgo());
    }
    
    /**
     * @param digestAlgo the digest algo, currently only sha* and ripemd160 is supported 
     * @return the uri for the given digest
     */
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
    
    /**
     * @param signatureFactory the xml signature factory, saved as thread-local
     */
    public void setSignatureFactory(XMLSignatureFactory signatureFactory) {
        this.signatureFactory.set(signatureFactory);
    }
    
    /**
     * @return the xml signature factory (thread-local)
     */
    public XMLSignatureFactory getSignatureFactory() {
        XMLSignatureFactory sigFac = signatureFactory.get();
        if (sigFac == null) {
            sigFac = XMLSignatureFactory.getInstance("DOM", getProvider());
            setSignatureFactory(sigFac);
        }
        return sigFac;
    }

    /**
     * @param keyInfoFactory the key factory, saved as thread-local
     */
    public void setKeyInfoFactory(KeyInfoFactory keyInfoFactory) {
        this.keyInfoFactory.set(keyInfoFactory);
    }
    
    /**
     * @return the key factory (thread-local)
     */
    public KeyInfoFactory getKeyInfoFactory() {
        KeyInfoFactory keyFac = keyInfoFactory.get();
        if (keyFac == null) {
            keyFac = KeyInfoFactory.getInstance("DOM", getProvider());
            setKeyInfoFactory(keyFac);
        }
        return keyFac;
    }

    /**
     * This method tests the existence of xml signature provider in the following order:
     * <ul>
     * <li>the class pointed to by the system property "jsr105Provider"</li>
     * <li>the Santuario xmlsec provider</li>
     * <li>the JDK xmlsec provider</li>
     * </ul>
     * 
     * For signing the classes are linked against the Santuario xmlsec, so this might
     * only work for validation (not tested).
     *  
     * @return the xml dsig provider
     */
    public Provider getProvider() {
        Provider prov = provider.get();
        if (prov == null) {
            String dsigProviderNames[] = {
                System.getProperty("jsr105Provider"),
                "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI", // Santuario xmlsec
                "org.jcp.xml.dsig.internal.dom.XMLDSigRI"         // JDK xmlsec
            };
            for (String pn : dsigProviderNames) {
                if (pn == null) continue;
                try {
                    prov = (Provider)Class.forName(pn).newInstance();
                    break;
                } catch (Exception e) {
                    LOG.log(POILogger.DEBUG, "XMLDsig-Provider '"+pn+"' can't be found - trying next.");
                }
            }
        }

        if (prov == null) {
            throw new RuntimeException("JRE doesn't support default xml signature provider - set jsr105Provider system property!");
        }
        
        return prov;
    }

    /**
     * @return the cannonicalization method for XAdES-XL signing.
     * Defaults to <code>EXCLUSIVE</code>
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/javax/xml/crypto/dsig/CanonicalizationMethod.html">javax.xml.crypto.dsig.CanonicalizationMethod</a>
     */
    public String getXadesCanonicalizationMethod() {
        return xadesCanonicalizationMethod;
    }

    /**
     * @param xadesCanonicalizationMethod the cannonicalization method for XAdES-XL signing
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/javax/xml/crypto/dsig/CanonicalizationMethod.html">javax.xml.crypto.dsig.CanonicalizationMethod</a>
     */
    public void setXadesCanonicalizationMethod(String xadesCanonicalizationMethod) {
        this.xadesCanonicalizationMethod = xadesCanonicalizationMethod;
    }
}
