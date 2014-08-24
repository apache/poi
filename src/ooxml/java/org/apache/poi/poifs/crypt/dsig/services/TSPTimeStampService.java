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

package org.apache.poi.poifs.crypt.dsig.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/**
 * A TSP time-stamp service implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class TSPTimeStampService implements TimeStampService {

    private static final POILogger LOG = POILogFactory.getLogger(TSPTimeStampService.class);

    static {
        CryptoFunctions.registerBouncyCastle();
    }

    public static final String DEFAULT_USER_AGENT = "POI XmlSign Service TSP Client";

    private final String tspServiceUrl;

    private String requestPolicy;

    private final String userAgent;

    private final TimeStampServiceValidator validator;

    private String username;

    private String password;

    private String proxyHost;

    private int proxyPort;

    private HashAlgorithm digestAlgo;

    private String digestAlgoOid;

    private String requestContentType = "application/timestamp-query;charset=ISO-8859-1";

    private String responseContentType = "application/timestamp-reply";
    
    public TSPTimeStampService(String tspServiceUrl,
            TimeStampServiceValidator validator) {
        this(tspServiceUrl, validator, null, null);
    }

    /**
     * Main constructor.
     * 
     * @param tspServiceUrl
     *            the URL of the TSP service.
     * @param validator
     *            the trust validator used to validate incoming TSP response
     *            signatures.
     * @param requestPolicy
     *            the optional TSP request policy.
     * @param userAgent
     *            the optional User-Agent TSP request header value.
     */
    public TSPTimeStampService(String tspServiceUrl,
            TimeStampServiceValidator validator, String requestPolicy,
            String userAgent) {
        if (null == tspServiceUrl) {
            throw new IllegalArgumentException("TSP service URL required");
        }
        this.tspServiceUrl = tspServiceUrl;

        if (null == validator) {
            throw new IllegalArgumentException("TSP validator required");
        }
        this.validator = validator;

        this.requestPolicy = requestPolicy;

        if (null != userAgent) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT;
        }
        
        setDigestAlgo(HashAlgorithm.sha1);
    }

    /**
     * Sets the request policy OID.
     * 
     * @param policyOid
     */
    public void setRequestPolicy(String policyOid) {
        this.requestPolicy = policyOid;
    }

    /**
     * Sets the credentials used in case the TSP service requires
     * authentication.
     * 
     * @param username
     * @param password
     */
    public void setAuthenticationCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Resets the authentication credentials.
     */
    public void resetAuthenticationCredentials() {
        this.username = null;
        this.password = null;
    }

    /**
     * Sets the digest algorithm used for time-stamping data. Example value:
     * "SHA-1".
     * 
     * @param digestAlgo
     */
    public void setDigestAlgo(HashAlgorithm digestAlgo) {
        switch (digestAlgo) {
        case sha1:
            digestAlgoOid = "1.3.14.3.2.26";
            break;
        case sha256:
            digestAlgoOid = "2.16.840.1.101.3.4.2.1";
            break;
        case sha384:
            digestAlgoOid = "2.16.840.1.101.3.4.2.2";
            break;
        case sha512:
            digestAlgoOid = "2.16.840.1.101.3.4.2.3";
            break;
        default:
            throw new IllegalArgumentException("unsupported digest algo: " + digestAlgo);
        }

        this.digestAlgo = digestAlgo;
    }

    /**
     * Configures the HTTP proxy settings to be used to connect to the TSP
     * service.
     * 
     * @param proxyHost
     * @param proxyPort
     */
    public void setProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * Resets the HTTP proxy settings.
     */
    public void resetProxy() {
        this.proxyHost = null;
        this.proxyPort = 0;
    }

    public byte[] timeStamp(byte[] data, RevocationData revocationData)
            throws Exception {
        // digest the message
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(this.digestAlgo);
        byte[] digest = messageDigest.digest(data);

        // generate the TSP request
        BigInteger nonce = new BigInteger(128, new SecureRandom());
        TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
        requestGenerator.setCertReq(true);
        if (null != this.requestPolicy) {
            requestGenerator.setReqPolicy(this.requestPolicy);
        }
        TimeStampRequest request = requestGenerator.generate(this.digestAlgoOid, digest, nonce);
        byte[] encodedRequest = request.getEncoded();

        // create the HTTP POST request
        Proxy proxy = (this.proxyHost != null)
            ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort))
            : Proxy.NO_PROXY;
        HttpURLConnection huc = (HttpURLConnection)new URL(this.tspServiceUrl).openConnection(proxy);
        
        if (null != this.username) {
            String userPassword = this.username + ":" + this.password;
            String encoding = DatatypeConverter.printBase64Binary(userPassword.getBytes(Charset.forName("iso-8859-1")));
            huc.setRequestProperty("Authorization", "Basic " + encoding);
        }

        huc.setDoOutput(true); // also sets method to POST.
        huc.setRequestProperty("User-Agent", this.userAgent);
        huc.setRequestProperty("Content-Type", requestContentType);
        
        OutputStream hucOut = huc.getOutputStream();
        hucOut.write(encodedRequest);
        
        // invoke TSP service
        huc.connect();
        
        int statusCode = huc.getResponseCode();
        if (statusCode != 200) {
            LOG.log(POILogger.ERROR, "Error contacting TSP server ", this.tspServiceUrl);
            throw new Exception("Error contacting TSP server " + this.tspServiceUrl);
        }

        // HTTP input validation
        String contentType = huc.getHeaderField("Content-Type");
        if (null == contentType) {
            throw new RuntimeException("missing Content-Type header");
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(huc.getInputStream(), bos);
        LOG.log(POILogger.DEBUG, "response content: ", bos.toString());
        
        if (!contentType.startsWith(responseContentType)) {
            throw new RuntimeException("invalid Content-Type: " + contentType);
        }
        
        if (bos.size() == 0) {
            throw new RuntimeException("Content-Length is zero");
        }

        // TSP response parsing and validation
        TimeStampResponse timeStampResponse = new TimeStampResponse(bos.toByteArray());
        timeStampResponse.validate(request);

        if (0 != timeStampResponse.getStatus()) {
            LOG.log(POILogger.DEBUG, "status: " + timeStampResponse.getStatus());
            LOG.log(POILogger.DEBUG, "status string: " + timeStampResponse.getStatusString());
            PKIFailureInfo failInfo = timeStampResponse.getFailInfo();
            if (null != failInfo) {
                LOG.log(POILogger.DEBUG, "fail info int value: " + failInfo.intValue());
                if (/*PKIFailureInfo.unacceptedPolicy*/(1 << 8) == failInfo.intValue()) {
                    LOG.log(POILogger.DEBUG, "unaccepted policy");
                }
            }
            throw new RuntimeException("timestamp response status != 0: "
                    + timeStampResponse.getStatus());
        }
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        SignerId signerId = timeStampToken.getSID();
        BigInteger signerCertSerialNumber = signerId.getSerialNumber();
        X500Name signerCertIssuer = signerId.getIssuer();
        LOG.log(POILogger.DEBUG, "signer cert serial number: " + signerCertSerialNumber);
        LOG.log(POILogger.DEBUG, "signer cert issuer: " + signerCertIssuer);

        // TSP signer certificates retrieval
        Collection<X509CertificateHolder> certificates = timeStampToken.getCertificates().getMatches(null);
        JcaX509ExtensionUtils utils = new JcaX509ExtensionUtils();
        
        X509CertificateHolder signerCert = null;
        Map<String, X509CertificateHolder> certificateMap = new HashMap<String, X509CertificateHolder>();
        for (X509CertificateHolder certificate : certificates) {
            if (signerCertIssuer.equals(certificate.getIssuer())
                && signerCertSerialNumber.equals(certificate.getSerialNumber())) {
                signerCert = certificate;
            }
            byte skiBytes[] = utils.createSubjectKeyIdentifier(certificate.getSubjectPublicKeyInfo()).getKeyIdentifier();
            String ski = Hex.encodeHexString(skiBytes);
            certificateMap.put(ski, certificate);
            LOG.log(POILogger.DEBUG, "embedded certificate: "
                    + certificate.getSubject() + "; SKI="
                    + ski);
        }

        // TSP signer cert path building
        if (null == signerCert) {
            throw new RuntimeException(
                    "TSP response token has no signer certificate");
        }
        List<X509Certificate> tspCertificateChain = new ArrayList<X509Certificate>();
        JcaX509CertificateConverter x509converter = new JcaX509CertificateConverter();
        x509converter.setProvider("BC");
        X509CertificateHolder certificate = signerCert;
        do {
            LOG.log(POILogger.DEBUG, "adding to certificate chain: " + certificate.getSubject());
            tspCertificateChain.add(x509converter.getCertificate(certificate));
            if (certificate.getSubject().equals(certificate.getIssuer())) {
                break;
            }
            byte akiBytes[] = utils.createAuthorityKeyIdentifier(certificate.getSubjectPublicKeyInfo()).getKeyIdentifier();
            String aki = Hex.encodeHexString(akiBytes);
            certificate = certificateMap.get(aki);
        } while (null != certificate);

        // verify TSP signer signature
        X509CertificateHolder holder = new X509CertificateHolder(tspCertificateChain.get(0).getEncoded());
        DefaultCMSSignatureAlgorithmNameGenerator nameGen = new DefaultCMSSignatureAlgorithmNameGenerator();
        DefaultSignatureAlgorithmIdentifierFinder sigAlgoFinder = new DefaultSignatureAlgorithmIdentifierFinder();
        DefaultDigestAlgorithmIdentifierFinder hashAlgoFinder = new DefaultDigestAlgorithmIdentifierFinder();
        BcDigestCalculatorProvider calculator = new BcDigestCalculatorProvider();
        BcRSASignerInfoVerifierBuilder verifierBuilder = new BcRSASignerInfoVerifierBuilder(nameGen, sigAlgoFinder, hashAlgoFinder, calculator);
        SignerInformationVerifier verifier = verifierBuilder.build(holder);
        
        timeStampToken.validate(verifier);

        // verify TSP signer certificate
        this.validator.validate(tspCertificateChain, revocationData);

        LOG.log(POILogger.DEBUG, "time-stamp token time: "
                + timeStampToken.getTimeStampInfo().getGenTime());

        byte[] timestamp = timeStampToken.getEncoded();
        return timestamp;
    }

    /**
     * usually the request content type is "application/timestamp-query;charset=ISO-8859-1",
     * but some timestamp server use a different content type
     */
    public void setRequestContentType(String requestContentType) {
        this.requestContentType = requestContentType;
    }

    /**
     * usually the response content type is "application/timestamp-reply",
     * but some timestamp server use a different content type
     */
    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }
}