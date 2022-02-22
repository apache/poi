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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hpsf.ClassID;
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
import org.apache.poi.poifs.crypt.dsig.services.TimeStampHttpClient;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampServiceValidator;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampSimpleHttpClient;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Removal;
import org.apache.xml.security.signature.XMLSignature;

/**
 * This class bundles the configuration options used for the existing
 * signature facets.
 * Apart from the thread local members (e.g. opc-package) most values will probably be constant, so
 * it might be configured centrally (e.g. by spring)
 */
@SuppressWarnings({"unused","WeakerAccess"})
public class SignatureConfig {
    public static class CRLEntry {
        private final String crlURL;
        private final String certCN;
        private final byte[] crlBytes;

        public CRLEntry(String crlURL, String certCN, byte[] crlBytes) {
            this.crlURL = crlURL;
            this.certCN = certCN;
            this.crlBytes = crlBytes;
        }

        public String getCrlURL() {
            return crlURL;
        }

        public String getCertCN() {
            return certCN;
        }

        public byte[] getCrlBytes() {
            return crlBytes;
        }
    }

    public static final String SIGNATURE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final Logger LOG = LogManager.getLogger(SignatureConfig.class);
    private static final String DigestMethod_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#sha224";
    private static final String DigestMethod_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
    private static final String XMLSEC_SANTUARIO = "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI";
    private static final String XMLSEC_JDK = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";

    private static final List<Supplier<SignatureFacet>> DEFAULT_FACETS = Arrays.asList(
        OOXMLSignatureFacet::new,
        KeyInfoSignatureFacet::new,
        XAdESSignatureFacet::new,
        Office2010SignatureFacet::new
    );


    private final ThreadLocal<OPCPackage> opcPackage = new ThreadLocal<>();
    private final ThreadLocal<XMLSignatureFactory> signatureFactory = new ThreadLocal<>();
    private final ThreadLocal<KeyInfoFactory> keyInfoFactory = new ThreadLocal<>();
    private final ThreadLocal<Provider> provider = new ThreadLocal<>();

    private List<SignatureFacet> signatureFacets = new ArrayList<>();
    private HashAlgorithm digestAlgo = HashAlgorithm.sha256;
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
    private boolean includeIssuerSerial;
    private boolean includeKeyValue;

    /**
     * the time-stamp service used for XAdES-T and XAdES-X.
     */
    private TimeStampService tspService = new TSPTimeStampService();
    private TimeStampHttpClient tspHttpClient = new TimeStampSimpleHttpClient();


    /**
     * timestamp service provider URL
     */
    private String tspUrl;
    private boolean tspOldProtocol;
    /**
     * if not defined, it's the same as the main digest
     */
    private HashAlgorithm tspDigestAlgo;
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
     * When {@code null} the signature will be limited to XAdES-T only.
     */
    private RevocationDataService revocationDataService;
    /**
     * if not defined, it's the same as the main digest
     */
    private HashAlgorithm xadesDigestAlgo;
    private String xadesRole;
    private String xadesSignatureId = "idSignedProperties";
    private boolean xadesSignaturePolicyImplied = true;
    private String xadesCanonicalizationMethod = CanonicalizationMethod.EXCLUSIVE;

    /**
     * Work-around for Office 2010 IssuerName encoding.
     */
    private boolean xadesIssuerNameNoReverseOrder = true;

    /**
     * The signature Id attribute value used to create the XML signature. A
     * {@code null} value will trigger an automatically generated signature Id.
     */
    private String packageSignatureId = "idPackageSignature";

    /**
     * Gives back the human-readable description of what the citizen will be
     * signing. The default value is "Office OpenXML Document".
     */
    private String signatureDescription = "Office OpenXML Document";

    /**
     * Only applies when working with visual signatures:
     * Specifies a GUID which can be cross-referenced with the GUID of the signature line stored in the document content.
     * I.e. the signatureline element id attribute in the document/sheet has to be references in the SetupId element.
     */
    private ClassID signatureImageSetupId;

    /**
     * Provides a signature image for visual signature lines
     */
    private byte[] signatureImage;
    /**
     * The image shown, when the signature is valid
     */
    private byte[] signatureImageValid;
    /**
     * The image shown, when the signature is invalid
     */
    private byte[] signatureImageInvalid;

    /**
     * The process of signing includes the marshalling of xml structures.
     * This also includes the canonicalization. Currently this leads to problems
     * with certain namespaces, so this EventListener is used to interfere
     * with the marshalling process.
     */
    private SignatureMarshalListener signatureMarshalListener = new SignatureMarshalDefaultListener();

    /**
     * Map of namespace uris to prefix
     * If a mapping is specified, the corresponding elements will be prefixed
     */
    private final Map<String,String> namespacePrefixes = new HashMap<>();

    /**
     * if true, the signature config is updated based on the validated document
     */
    private boolean updateConfigOnValidate = false;

    /**
     * if true, the signature is added to the existing signatures
     *
     * @since POI 4.1.0
     */
    private boolean allowMultipleSignatures = false;

    /**
     * Switch to enable/disable secure validation - see setter for more information
     *
     * @since POI 5.2.0
     */
    private boolean secureValidation = true;

    private String commitmentType = "Created and approved this document";

    /**
     * Swtich to enable/disable automatic CRL download - by default the download is with all https hostname
     * and certificate verifications disabled.
     *
     * @since POI 5.2.1
     */
    private boolean allowCRLDownload = false;

    /**
     * List of cached / saved CRL entries
     */
    private final List<CRLEntry> crlEntries = new ArrayList<>();

    /**
     * Keystore used for cached certificates
     */
    private final KeyStore keyStore = emptyKeyStore();

    public SignatureConfig() {
        // OOo doesn't like ds namespaces so per default prefixing is off.
        // namespacePrefixes.put(XML_DIGSIG_NS, "");
        namespacePrefixes.put(OO_DIGSIG_NS, "mdssi");
        namespacePrefixes.put(XADES_132_NS, "xd");
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
        if (signatureFacets.isEmpty()) {
            return DEFAULT_FACETS.stream().map(Supplier::get).collect(Collectors.toList());
        } else {
            return signatureFacets;
        }
    }

    /**
     * @param signatureFacets the new list of facets
     */
    public void setSignatureFacets(List<SignatureFacet> signatureFacets) {
        this.signatureFacets = signatureFacets;
    }

    /**
     * @return the main digest algorithm, defaults to sha256
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
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setOpcPackage(OPCPackage)} instead
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public OPCPackage getOpcPackage() {
        return opcPackage.get();
    }

    /**
     * @param opcPackage the opc package to be handled by this thread, stored as thread-local
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setOpcPackage(OPCPackage)} instead
     */
    @Deprecated
    @Removal(version = "5.0.0")
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
     * @return the formatted execution time ({@link #SIGNATURE_TIME_FORMAT})
     *
     * @since POI 4.0.0
     */
    public String formatExecutionTime() {
        final DateFormat fmt = new SimpleDateFormat(SIGNATURE_TIME_FORMAT, Locale.ROOT);
        fmt.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        return fmt.format(getExecutionTime());
    }

    /**
     * Sets the executionTime which is in standard format ({@link #SIGNATURE_TIME_FORMAT})
     * @param executionTime the execution time
     *
     * @since POI 4.0.0
     */
    public void setExecutionTime(String executionTime) {
        if (executionTime != null && !"".equals(executionTime)){
            final DateFormat fmt = new SimpleDateFormat(SIGNATURE_TIME_FORMAT, Locale.ROOT);
            fmt.setTimeZone(LocaleUtil.TIMEZONE_UTC);
            try {
                this.executionTime = fmt.parse(executionTime);
            } catch (ParseException e) {
                LOG.atWarn().log("Illegal execution time: {}. Must be formatted as " + SIGNATURE_TIME_FORMAT, executionTime);
            }
        }
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
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#getUriDereferencer()} instead
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public URIDereferencer getUriDereferencer() {
        return uriDereferencer;
    }

    /**
     * @param uriDereferencer the dereferencer used for Reference/@URI attributes
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setUriDereferencer(URIDereferencer)} instead
     */
    @Deprecated
    @Removal(version = "5.0.0")
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

    public byte[] getSignatureImage() {
        return signatureImage;
    }

    public byte[] getSignatureImageValid() {
        return signatureImageValid;
    }

    public byte[] getSignatureImageInvalid() {
        return signatureImageInvalid;
    }

    public ClassID getSignatureImageSetupId() {
        return signatureImageSetupId;
    }

    public void setSignatureImageSetupId(ClassID signatureImageSetupId) {
        this.signatureImageSetupId = signatureImageSetupId;
    }

    public void setSignatureImage(byte[] signatureImage) {
        this.signatureImage = (signatureImage == null) ? null : signatureImage.clone();
    }

    public void setSignatureImageValid(byte[] signatureImageValid) {
        this.signatureImageValid = (signatureImageValid == null) ? null : signatureImageValid.clone();
    }

    public void setSignatureImageInvalid(byte[] signatureImageInvalid) {
        this.signatureImageInvalid = (signatureImageInvalid == null) ? null : signatureImageInvalid.clone();
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
        this.canonicalizationMethod = verifyCanonicalizationMethod(canonicalizationMethod, CanonicalizationMethod.INCLUSIVE);
    }

    private static String verifyCanonicalizationMethod(String canonicalizationMethod, String defaultMethod) {
        if (canonicalizationMethod == null || canonicalizationMethod.isEmpty()) {
            return defaultMethod;
        }

        switch (canonicalizationMethod) {
            case Transform.ENVELOPED:
            case CanonicalizationMethod.INCLUSIVE:
            case CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS:
            case CanonicalizationMethod.EXCLUSIVE:
            case CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS:
                return canonicalizationMethod;
        }

        throw new EncryptedDocumentException("Unknown CanonicalizationMethod: "+canonicalizationMethod);
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
     * A {@code null} value will trigger an automatically generated signature Id.
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
     * if {@code null}, the hash algorithm of the main entry
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
     * @return the http client used for timestamp server connections
     *
     * @since POI 5.2.1
     */
    public TimeStampHttpClient getTspHttpClient() {
        return tspHttpClient;
    }

    /**
     * @param tspHttpClient the http client used for timestamp server connections
     *
     * @since POI 5.2.1
     */
    public void setTspHttpClient(TimeStampHttpClient tspHttpClient) {
        this.tspHttpClient = tspHttpClient;
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
     * When {@code null} the signature will be limited to XAdES-T only.
     */
    public RevocationDataService getRevocationDataService() {
        return revocationDataService;
    }

    /**
     * @param revocationDataService the optional revocation data service used for XAdES-C and XAdES-X-L.
     * When {@code null} the signature will be limited to XAdES-T only.
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
     * When {@code null}, defaults to {@link #getDigestAlgo()}
     */
    public void setXadesDigestAlgo(HashAlgorithm xadesDigestAlgo) {
        this.xadesDigestAlgo = xadesDigestAlgo;
    }

    /**
     * @param xadesDigestAlgo hash algorithm used for XAdES.
     * When {@code null}, defaults to {@link #getDigestAlgo()}
     *
     * @since POI 4.0.0
     */
    public void setXadesDigestAlgo(String xadesDigestAlgo) {
        this.xadesDigestAlgo = getDigestMethodAlgo(xadesDigestAlgo);
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
     * Defaults to {@code 1.3.6.1.4.1.13762.3}
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
     * @return the xades role element. If {@code null} the claimed role element is omitted.
     * Defaults to {@code null}
     */
    public String getXadesRole() {
        return xadesRole;
    }

    /**
     * @param xadesRole the xades role element. If {@code null} the claimed role element is omitted.
     */
    public void setXadesRole(String xadesRole) {
        this.xadesRole = xadesRole;
    }

    /**
     * @return the Id for the XAdES SignedProperties element.
     * Defaults to {@code idSignedProperties}
     */
    public String getXadesSignatureId() {
        return nvl(xadesSignatureId, "idSignedProperties");
    }

    /**
     * @param xadesSignatureId the Id for the XAdES SignedProperties element.
     * When {@code null} defaults to {@code idSignedProperties}
     */
    public void setXadesSignatureId(String xadesSignatureId) {
        this.xadesSignatureId = xadesSignatureId;
    }

    /**
     * @return when true, include the policy-implied block.
     * Defaults to {@code true}
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
     * @return the event listener which is active while xml structure for the signature is created.
     * Defaults to {@link SignatureMarshalListener}
     */
    public SignatureMarshalListener getSignatureMarshalListener() {
        return signatureMarshalListener;
    }

    /**
     * @param signatureMarshalListener the event listener watching the xml structure
     * generation for the signature
     */
    public void setSignatureMarshalListener(SignatureMarshalListener signatureMarshalListener) {
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
        this.namespacePrefixes.clear();
        this.namespacePrefixes.putAll(namespacePrefixes);
    }

    /**
     * helper method for null/default value handling
     * @param value the value to be tested
     * @param defaultValue the default value
     * @return if value is not null, return value otherwise defaultValue
     */
    private static <T> T nvl(T value, T defaultValue)  {
        return value == null ? defaultValue : value;
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
     * Converts the digest algorithm - currently only sha* and ripemd160 is supported.
     * MS Office only supports sha1, sha256, sha384, sha512.
     *
     * @param digestAlgo the digest algorithm
     * @return the uri for the given digest
     */
    public static String getDigestMethodUri(HashAlgorithm digestAlgo) {
        switch (digestAlgo) {
        case sha1:   return DigestMethod.SHA1;
        case sha224: return DigestMethod_SHA224;
        case sha256: return DigestMethod.SHA256;
        case sha384: return DigestMethod_SHA384;
        case sha512: return DigestMethod.SHA512;
        case ripemd160: return DigestMethod.RIPEMD160;
        default: throw new EncryptedDocumentException("Hash algorithm "
            +digestAlgo+" not supported for signing.");
        }
    }

    /**
     * Converts the digest algorithm ur - currently only sha* and ripemd160 is supported.
     * MS Office only supports sha1, sha256, sha384, sha512.
     *
     * @param digestMethodUri the digest algorithm uri
     * @return the hash algorithm for the given digest
     */
    private static HashAlgorithm getDigestMethodAlgo(String digestMethodUri) {
        if (digestMethodUri == null || digestMethodUri.isEmpty()) {
            return null;
        }
        switch (digestMethodUri) {
            case DigestMethod.SHA1:   return HashAlgorithm.sha1;
            case DigestMethod_SHA224: return HashAlgorithm.sha224;
            case DigestMethod.SHA256: return HashAlgorithm.sha256;
            case DigestMethod_SHA384: return HashAlgorithm.sha384;
            case DigestMethod.SHA512: return HashAlgorithm.sha512;
            case DigestMethod.RIPEMD160: return HashAlgorithm.ripemd160;
            default: throw new EncryptedDocumentException("Hash algorithm "
                    +digestMethodUri+" not supported for signing.");
        }
    }

    /**
     * Set the digest algorithm based on the method uri.
     * This is used when a signature was successful validated and the signature
     * configuration is updated
     *
     * @param signatureMethodUri the method uri
     *
     * @since POI 4.0.0
     */
    public void setSignatureMethodFromUri(final String signatureMethodUri) {
        switch (signatureMethodUri) {
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1:
                setDigestAlgo(HashAlgorithm.sha1);
                break;
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224:
                setDigestAlgo(HashAlgorithm.sha224);
                break;
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256:
                setDigestAlgo(HashAlgorithm.sha256);
                break;
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384:
                setDigestAlgo(HashAlgorithm.sha384);
                break;
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512:
                setDigestAlgo(HashAlgorithm.sha512);
                break;
            case XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160:
                setDigestAlgo(HashAlgorithm.ripemd160);
                break;
            default: throw new EncryptedDocumentException("Hash algorithm "
                    +signatureMethodUri+" not supported.");
        }
    }


    /**
     * @param signatureFactory the xml signature factory, saved as thread-local
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setSignatureFactory(XMLSignatureFactory)}
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public void setSignatureFactory(XMLSignatureFactory signatureFactory) {
        this.signatureFactory.set(signatureFactory);
    }

    /**
     * @return the xml signature factory (thread-local)
     *
     * @deprecated in POI 5.0.0 - will be handled by SignatureInfo internally
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public XMLSignatureFactory getSignatureFactory() {
        return signatureFactory.get();
    }

    /**
     * @param keyInfoFactory the key factory, saved as thread-local
     *
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setKeyInfoFactory(KeyInfoFactory)}
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public void setKeyInfoFactory(KeyInfoFactory keyInfoFactory) {
        this.keyInfoFactory.set(keyInfoFactory);
    }

    /**
     * @return the key factory (thread-local)
     *
     * @deprecated in POI 5.0.0 - will be handled by SignatureInfo internally
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public KeyInfoFactory getKeyInfoFactory() {
        return keyInfoFactory.get();
    }

    /**
     * Helper method to set provider
     * @param provider the provider
     * @deprecated in POI 5.0.0 - use {@link SignatureInfo#setProvider(Provider)}
     */
    @Internal
    @Deprecated
    @Removal(version = "5.0.0")
    public void setProvider(Provider provider) {
        this.provider.set(provider);
    }

    /**
     * @return the cached provider or null if not set before
     *
     * @deprecated in POI 5.0.0 - will be handled by SignatureInfo internally
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public Provider getProvider() {
        return provider.get();
    }

    /**
     * Determine the possible classes for XMLSEC.
     * The order is
     * <ol>
     * <li>the class pointed to by the system property "jsr105Provider"</li>
     * <li>the Santuario xmlsec provider</li>
     * <li>the JDK xmlsec provider</li>
     * </ol>
     *
     * @return a list of possible XMLSEC provider class names
     */
    public static String[] getProviderNames() {
        // need to check every time, as the system property might have been changed in the meantime
        String sysProp = System.getProperty("jsr105Provider");
        return (sysProp == null || "".equals(sysProp))
            ? new String[]{XMLSEC_SANTUARIO, XMLSEC_JDK}
            : new String[]{sysProp, XMLSEC_SANTUARIO, XMLSEC_JDK};
    }


    /**
     * @return the cannonicalization method for XAdES-XL signing.
     * Defaults to {@code EXCLUSIVE}
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
        this.xadesCanonicalizationMethod = verifyCanonicalizationMethod(xadesCanonicalizationMethod, CanonicalizationMethod.EXCLUSIVE);
    }

    /**
     * @return true, if the signature config is to be updated based on the successful validated document
     *
     * @since POI 4.0.0
     */
    public boolean isUpdateConfigOnValidate() {
        return updateConfigOnValidate;
    }

    /**
     * The signature config can be updated if a document is succesful validated.
     * This flag is used for activating this modifications.
     * Defaults to {@code false}
     *
     * @param updateConfigOnValidate if true, update config on validate
     *
     * @since POI 4.0.0
     */
    public void setUpdateConfigOnValidate(boolean updateConfigOnValidate) {
        this.updateConfigOnValidate = updateConfigOnValidate;
    }

    /**
     * @return true, if multiple signatures can be attached
     *
     * @since POI 4.1.0
     */
    public boolean isAllowMultipleSignatures() {
        return allowMultipleSignatures;
    }

    /**
     * Activate multiple signatures
     *
     * @param allowMultipleSignatures if true, the signature will be added,
     *          otherwise all existing signatures will be replaced by the current
     *
     * @since POI 4.1.0
     */
    public void setAllowMultipleSignatures(boolean allowMultipleSignatures) {
        this.allowMultipleSignatures = allowMultipleSignatures;
    }

    /**
     * @return is secure validation enabled?
     *
     * @since POI 5.2.0
     */
    public boolean isSecureValidation() {
        return secureValidation;
    }

    /**
     * Enable or disable secure validation - default is enabled.
     * <p>
     * Starting with xmlsec 2.3.0 larger documents with a lot of document parts started to fail,
     * because a maximum of 30 references were hard-coded allowed for secure validation to succeed.
     * <p>
     * Secure validation has the following features:
     * <ul>
     * <li>Limits the number of Transforms per Reference to a maximum of 5.
     * <li>Does not allow XSLT transforms.
     * <li>Does not allow a RetrievalMethod to reference another RetrievalMethod.
     * <li>Does not allow a Reference to call the ResolverLocalFilesystem or the ResolverDirectHTTP (references to local files and HTTP resources are forbidden).
     * <li>Limits the number of references per Manifest (SignedInfo) to a maximum of 30.
     * <li>MD5 is not allowed as a SignatureAlgorithm or DigestAlgorithm.
     * <li>Guarantees that the Dereferenced Element returned via Document.getElementById is unique by performing a tree-search.
     * <li>Does not allow DTDs
     * </ul>
     *
     * @see <a href="https://santuario.apache.org/faq.html#faq-4.SecureValidation">XmlSec SecureValidation</a>
     *
     * @since POI 5.2.0
     */
    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public String getCommitmentType() {
        return commitmentType;
    }

    /**
     * Set the commitmentType, which is usually one of ...
     * <ul>
     *     <li>"Created and approved this document"
     *     <li>"Approved this document"
     *     <li>"Created this document"
     *     <li>... or any other important sounding statement
     * </ul>
     */
    public void setCommitmentType(String commitmentType) {
        this.commitmentType = commitmentType;
    }


    public CRLEntry addCRL(String crlURL, String certCN, byte[] crlBytes) {
        CRLEntry ce = new CRLEntry(crlURL, certCN, crlBytes);
        crlEntries.add(ce);
        return ce;
    }

    public List<CRLEntry> getCrlEntries() {
        return crlEntries;
    }

    public boolean isAllowCRLDownload() {
        return allowCRLDownload;
    }

    public void setAllowCRLDownload(boolean allowCRLDownload) {
        this.allowCRLDownload = allowCRLDownload;
    }

    /**
     * @return keystore with cached certificates
     */
    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Add certificate into keystore (cache) for further certificate chain lookups
     * @param alias the alias, or null if alias is taken from common name attribute of certificate
     * @param x509 the x509 certificate
     */
    public void addCachedCertificate(String alias, X509Certificate x509) throws KeyStoreException {
        String lAlias = alias;
        if (lAlias == null) {
            lAlias = x509.getSubjectX500Principal().getName();
        }
        if (keyStore != null) {
            synchronized (keyStore) {
                keyStore.setCertificateEntry(lAlias, x509);
            }
        }
    }

    public void addCachedCertificate(String alias, byte[] x509Bytes) throws KeyStoreException, CertificateException {
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");
        X509Certificate x509 = (X509Certificate)certFact.generateCertificate(new ByteArrayInputStream(x509Bytes));
        addCachedCertificate(null, x509);
    }

    public X509Certificate getCachedCertificateByPrinicipal(String principalName) {
        if (keyStore == null) {
            return null;
        }
        // TODO: add synchronized
        try {
            for (String a : Collections.list(keyStore.aliases())) {
                Certificate[] chain = keyStore.getCertificateChain(a);
                if (chain == null) {
                    Certificate cert = keyStore.getCertificate(a);
                    if (cert == null) {
                        continue;
                    }
                    chain = new Certificate[]{cert};
                }
                Optional<X509Certificate> found = Stream.of(chain)
                    .map(X509Certificate.class::cast)
                    .filter(c -> principalName.equalsIgnoreCase(c.getSubjectX500Principal().getName()))
                    .findFirst();
                if (found.isPresent()) {
                    return found.get();
                }
            }
            return null;
        } catch (KeyStoreException e) {
            return null;
        }
    }


    private static KeyStore emptyKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            return ks;
        } catch (IOException | GeneralSecurityException e) {
            LOG.atError().withThrowable(e).log("unable to create PKCS #12 keystore - XAdES certificate chain lookups disabled");
        }
        return null;
    }


}