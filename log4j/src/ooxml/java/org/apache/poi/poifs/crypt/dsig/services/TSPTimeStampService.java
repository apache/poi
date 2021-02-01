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
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
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

    /**
     * Maps the digest algorithm to corresponding OID value.
     */
    public ASN1ObjectIdentifier mapDigestAlgoToOID(HashAlgorithm digestAlgo) {
        switch (digestAlgo) {
        case sha1:   return X509ObjectIdentifiers.id_SHA1;
        case sha256: return NISTObjectIdentifiers.id_sha256;
        case sha384: return NISTObjectIdentifiers.id_sha384;
        case sha512: return NISTObjectIdentifiers.id_sha512;
        default:
            throw new IllegalArgumentException("unsupported digest algo: " + digestAlgo);
        }
    }

    @SuppressWarnings({"unchecked","squid:S2647"})
    public byte[] timeStamp(SignatureInfo signatureInfo, byte[] data, RevocationData revocationData) throws Exception {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();

        // digest the message
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(signatureConfig.getTspDigestAlgo());
        byte[] digest = messageDigest.digest(data);

        // generate the TSP request
        BigInteger nonce = new BigInteger(128, new SecureRandom());
        TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
        requestGenerator.setCertReq(true);
        String requestPolicy = signatureConfig.getTspRequestPolicy();
        if (requestPolicy != null) {
            requestGenerator.setReqPolicy(new ASN1ObjectIdentifier(requestPolicy));
        }
        ASN1ObjectIdentifier digestAlgoOid = mapDigestAlgoToOID(signatureConfig.getTspDigestAlgo());
        TimeStampRequest request = requestGenerator.generate(digestAlgoOid, digest, nonce);
        byte[] encodedRequest = request.getEncoded();

        // create the HTTP POST request
        Proxy proxy = Proxy.NO_PROXY;
        if (signatureConfig.getProxyUrl() != null) {
            URL proxyUrl = new URL(signatureConfig.getProxyUrl());
            String host = proxyUrl.getHost();
            int port = proxyUrl.getPort();
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(host), (port == -1 ? 80 : port)));
        }

        ByteArrayOutputStream bos;
        String contentType;
        HttpURLConnection huc = (HttpURLConnection)new URL(signatureConfig.getTspUrl()).openConnection(proxy);
        try {
            if (signatureConfig.getTspUser() != null) {
                String userPassword = signatureConfig.getTspUser() + ":" + signatureConfig.getTspPass();
                String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes(StandardCharsets.ISO_8859_1));
                huc.setRequestProperty("Authorization", "Basic " + encoding);
            }

            huc.setRequestMethod("POST");
            huc.setConnectTimeout(20000);
            huc.setReadTimeout(20000);
            huc.setDoOutput(true); // also sets method to POST.
            huc.setRequestProperty("User-Agent", signatureConfig.getUserAgent());
            huc.setRequestProperty("Content-Type", signatureConfig.isTspOldProtocol()
                    ? "application/timestamp-request"
                    : "application/timestamp-query"); // "; charset=ISO-8859-1");

            OutputStream hucOut = huc.getOutputStream();
            hucOut.write(encodedRequest);

            // invoke TSP service
            huc.connect();

            int statusCode = huc.getResponseCode();
            if (statusCode != 200) {
                LOG.log(POILogger.ERROR, "Error contacting TSP server ", signatureConfig.getTspUrl(),
                        ", had status code ", statusCode, "/", huc.getResponseMessage());
                throw new IOException("Error contacting TSP server " + signatureConfig.getTspUrl() +
                        ", had status code " + statusCode + "/" + huc.getResponseMessage());
            }

            // HTTP input validation
            contentType = huc.getHeaderField("Content-Type");
            if (null == contentType) {
                throw new RuntimeException("missing Content-Type header");
            }

            bos = new ByteArrayOutputStream();
            IOUtils.copy(huc.getInputStream(), bos);
            LOG.log(POILogger.DEBUG, "response content: ", HexDump.dump(bos.toByteArray(), 0, 0));
        } finally {
            huc.disconnect();
        }

        if (!contentType.startsWith(signatureConfig.isTspOldProtocol()
            ? "application/timestamp-response"
            : "application/timestamp-reply"
        )) {
            throw new RuntimeException("invalid Content-Type: " + contentType +
                    // dump the first few bytes
                    ": " + HexDump.dump(bos.toByteArray(), 0, 0, 200));
        }

        if (bos.size() == 0) {
            throw new RuntimeException("Content-Length is zero");
        }

        // TSP response parsing and validation
        TimeStampResponse timeStampResponse = new TimeStampResponse(bos.toByteArray());
        timeStampResponse.validate(request);

        if (0 != timeStampResponse.getStatus()) {
            LOG.log(POILogger.DEBUG, "status: ", timeStampResponse.getStatus());
            LOG.log(POILogger.DEBUG, "status string: ", timeStampResponse.getStatusString());
            PKIFailureInfo failInfo = timeStampResponse.getFailInfo();
            if (null != failInfo) {
                LOG.log(POILogger.DEBUG, "fail info int value: ", failInfo.intValue());
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
        LOG.log(POILogger.DEBUG, "signer cert serial number: ", signerCertSerialNumber);
        LOG.log(POILogger.DEBUG, "signer cert issuer: ", signerCertIssuer);

        // TSP signer certificates retrieval
        Collection<X509CertificateHolder> certificates = timeStampToken.getCertificates().getMatches(null);

        X509CertificateHolder signerCert = null;
        Map<X500Name, X509CertificateHolder> certificateMap = new HashMap<>();
        for (X509CertificateHolder certificate : certificates) {
            if (signerCertIssuer.equals(certificate.getIssuer())
                && signerCertSerialNumber.equals(certificate.getSerialNumber())) {
                signerCert = certificate;
            }
            certificateMap.put(certificate.getSubject(), certificate);
        }

        // TSP signer cert path building
        if (signerCert == null) {
            throw new RuntimeException("TSP response token has no signer certificate");
        }
        List<X509Certificate> tspCertificateChain = new ArrayList<>();
        JcaX509CertificateConverter x509converter = new JcaX509CertificateConverter();
        x509converter.setProvider("BC");
        X509CertificateHolder certificate = signerCert;
        do {
            LOG.log(POILogger.DEBUG, "adding to certificate chain: ", certificate.getSubject());
            tspCertificateChain.add(x509converter.getCertificate(certificate));
            if (certificate.getSubject().equals(certificate.getIssuer())) {
                break;
            }
            certificate = certificateMap.get(certificate.getIssuer());
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
        if (signatureConfig.getTspValidator() != null) {
            signatureConfig.getTspValidator().validate(tspCertificateChain, revocationData);
        }

        LOG.log(POILogger.DEBUG, "time-stamp token time: ",
                timeStampToken.getTimeStampInfo().getGenTime());

        return timeStampToken.getEncoded();
    }
}