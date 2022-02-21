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

import static org.apache.logging.log4j.util.Unbox.box;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.x500.X500Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig.CRLEntry;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampHttpClient.TimeStampHttpClientResponse;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
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
 */
public class TSPTimeStampService implements TimeStampService {

    private static final Logger LOG = LogManager.getLogger(TSPTimeStampService.class);

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

    @Override
    @SuppressWarnings({"squid:S2647"})
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

        TimeStampHttpClient httpClient = signatureConfig.getTspHttpClient();
        httpClient.init(signatureConfig);
        httpClient.setContentTypeIn(signatureConfig.isTspOldProtocol() ? "application/timestamp-request" : "application/timestamp-query");
        TimeStampHttpClientResponse response = httpClient.post(signatureConfig.getTspUrl(), request.getEncoded());
        if (!response.isOK()) {
            throw new IOException("Requesting timestamp data failed");
        }

        byte[] responseBytes = response.getResponseBytes();

        if (responseBytes.length == 0) {
            throw new RuntimeException("Content-Length is zero");
        }

        // TSP response parsing and validation
        TimeStampResponse timeStampResponse = new TimeStampResponse(responseBytes);
        timeStampResponse.validate(request);

        if (0 != timeStampResponse.getStatus()) {
            LOG.atDebug().log("status: {}", box(timeStampResponse.getStatus()));
            LOG.atDebug().log("status string: {}", timeStampResponse.getStatusString());
            PKIFailureInfo failInfo = timeStampResponse.getFailInfo();
            if (null != failInfo) {
                LOG.atDebug().log("fail info int value: {}", box(failInfo.intValue()));
                if (/*PKIFailureInfo.unacceptedPolicy*/(1 << 8) == failInfo.intValue()) {
                    LOG.atDebug().log("unaccepted policy");
                }
            }
            throw new RuntimeException("timestamp response status != 0: "
                    + timeStampResponse.getStatus());
        }
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        SignerId signerId = timeStampToken.getSID();
        BigInteger signerCertSerialNumber = signerId.getSerialNumber();
        X500Name signerCertIssuer = signerId.getIssuer();
        LOG.atDebug().log("signer cert serial number: {}", signerCertSerialNumber);
        LOG.atDebug().log("signer cert issuer: {}", signerCertIssuer);

        // TSP signer certificates retrieval
        Map<String, X509CertificateHolder> certificateMap =
            timeStampToken.getCertificates().getMatches(null).stream()
                .collect(Collectors.toMap(h -> h.getSubject().toString(), Function.identity()));


        // TSP signer cert path building
        X509CertificateHolder signerCert = certificateMap.values().stream()
            .filter(h -> signerCertIssuer.equals(h.getIssuer())
                && signerCertSerialNumber.equals(h.getSerialNumber()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("TSP response token has no signer certificate"));

        JcaX509CertificateConverter x509converter = new JcaX509CertificateConverter();
        x509converter.setProvider("BC");

        // complete certificate chain
        X509Certificate child = x509converter.getCertificate(signerCert);
        do {
            revocationData.addCertificate(child);
            X500Principal issuer = child.getIssuerX500Principal();
            if (child.getSubjectX500Principal().equals(issuer)) {
                break;
            }
            X509CertificateHolder parentHolder = certificateMap.get(issuer.getName());
            child = (parentHolder != null)
                ? x509converter.getCertificate(parentHolder)
                : signatureConfig.getCachedCertificateByPrinicipal(issuer.getName());
            if (child != null) {
                retrieveCRL(signatureConfig, child).forEach(revocationData::addCRL);
            }
        } while (child != null);

        // verify TSP signer signature
        BcRSASignerInfoVerifierBuilder verifierBuilder = new BcRSASignerInfoVerifierBuilder(
            new DefaultCMSSignatureAlgorithmNameGenerator(),
            new DefaultSignatureAlgorithmIdentifierFinder(),
            new DefaultDigestAlgorithmIdentifierFinder(),
            new BcDigestCalculatorProvider());
        SignerInformationVerifier verifier = verifierBuilder.build(signerCert);

        timeStampToken.validate(verifier);

        // verify TSP signer certificate
        if (signatureConfig.getTspValidator() != null) {
            signatureConfig.getTspValidator().validate(revocationData.getX509chain(), revocationData);
        }

        LOG.atDebug().log("time-stamp token time: {}", timeStampToken.getTimeStampInfo().getGenTime());

        return timeStampToken.getEncoded();
    }

    /**
     * Check if CRL is to be added, check cached CRLs in config and download if necessary.
     * Can be overriden to suppress the logic
     * @return empty list, if not found or suppressed, otherwise the list of CRLs as encoded bytes
     */
    protected List<byte[]> retrieveCRL(SignatureConfig signatureConfig, X509Certificate holder) throws IOException {
        // TODO: add config, if crls should be added
        final List<CRLEntry> crlEntries = signatureConfig.getCrlEntries();
        byte[] crlPoints = holder.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crlPoints == null) {
            return Collections.emptyList();
        }

        // TODO: check if parse is necessary, or if crlExt.getExtnValue() can be use directly
        ASN1Primitive extVal = JcaX509ExtensionUtils.parseExtensionValue(crlPoints);
        return Stream.of(CRLDistPoint.getInstance(extVal).getDistributionPoints())
            .map(DistributionPoint::getDistributionPoint)
            .filter(Objects::nonNull)
            .filter(dpn -> dpn.getType() == DistributionPointName.FULL_NAME)
            .flatMap(dpn -> Stream.of(GeneralNames.getInstance(dpn.getName()).getNames()))
            .filter(genName -> genName.getTagNo() == GeneralName.uniformResourceIdentifier)
            .map(genName -> ASN1IA5String.getInstance(genName.getName()).getString())
            .flatMap(url -> {
                List<CRLEntry> ul = crlEntries.stream().filter(ce -> matchCRLbyUrl(ce, holder, url)).collect(Collectors.toList());
                Stream<CRLEntry> cl = crlEntries.stream().filter(ce -> matchCRLbyCN(ce, holder, url));
                if (ul.isEmpty()) {
                    CRLEntry ce = downloadCRL(signatureConfig, url);
                    if (ce != null) {
                        ul.add(ce);
                    }
                }
                return Stream.concat(ul.stream(), cl).map(CRLEntry::getCrlBytes);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected boolean matchCRLbyUrl(CRLEntry other, X509Certificate holder, String url) {
        return url.equals(other.getCrlURL());
    }

    protected boolean matchCRLbyCN(CRLEntry other, X509Certificate holder, String url) {
        return holder.getSubjectX500Principal().getName().equals(other.getCertCN());
    }

    /**
     * Convenience method to download a crl in an unsafe way, i.e. without verifying the
     * https certificates.
     * Please provide your own method, if you have imported the TSP server CA certificates
     * in your local keystore
     *
     * @return the bytes of the CRL or null if unsuccessful / download is suppressed
     */
    protected CRLEntry downloadCRL(SignatureConfig signatureConfig, String url) {
        if (!signatureConfig.isAllowCRLDownload()) {
            return null;
        }

        TimeStampHttpClient httpClient = signatureConfig.getTspHttpClient();
        httpClient.init(signatureConfig);
        httpClient.setBasicAuthentication(null, null);
        TimeStampHttpClientResponse response;
        try {
            response = httpClient.get(url);
            if (!response.isOK()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            byte[] crlBytes = response.getResponseBytes();
            // verify the downloaded bytes, throws Exception if invalid
            X509CRL crl = (X509CRL)certFact.generateCRL(new ByteArrayInputStream(crlBytes));
            return signatureConfig.addCRL(url, crl.getIssuerX500Principal().getName(), crlBytes);
        } catch (GeneralSecurityException e) {
            LOG.atWarn().withThrowable(e).log("CRL download failed from {}", url);
            return null;
        }
    }
}