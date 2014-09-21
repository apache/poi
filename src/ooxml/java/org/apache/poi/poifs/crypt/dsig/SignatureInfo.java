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

package org.apache.poi.poifs.crypt.dsig;

import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160;
import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384;
import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig.SignatureConfigurable;
import org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.Base64;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.xml.sax.SAXException;

public class SignatureInfo implements SignatureConfigurable {

    public static final String XmlNS = "http://www.w3.org/2000/xmlns/";
    public static final String XmlDSigNS = XMLSignature.XMLNS;
    
    // see https://www.ietf.org/rfc/rfc3110.txt
    // RSA/SHA1 SIG Resource Records
    public static final byte[] SHA1_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x04, 0x14 };

    public static final byte[] SHA224_DIGEST_INFO_PREFIX = new byte[] 
        { 0x30, 0x2b, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x04, 0x04, 0x1c };

    public static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };

    public static final byte[] SHA384_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x3f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x04, 0x30 };

    public static final byte[] SHA512_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x4f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x04, 0x40 };

    public static final byte[] RIPEMD128_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x02, 0x04, 0x10 };

    public static final byte[] RIPEMD160_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x01, 0x04, 0x14 };

    public static final byte[] RIPEMD256_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x2b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x03, 0x04, 0x20 };
    
    protected static class SignCreationListener implements EventListener, SignatureConfigurable {
        ThreadLocal<EventTarget> target = new ThreadLocal<EventTarget>();
        SignatureConfig signatureConfig;
        public void setEventTarget(EventTarget target) {
            this.target.set(target);
        }
        public void handleEvent(Event e) {
            if (e instanceof MutationEvent) {
                MutationEvent mutEvt = (MutationEvent)e;
                if (mutEvt.getTarget() instanceof Element) {
                    Element el = (Element)mutEvt.getTarget();
                    String packageId = signatureConfig.getPackageSignatureId();
                    if (packageId.equals(el.getAttribute("Id"))) {
                        target.get().removeEventListener("DOMSubtreeModified", this, false);
                        el.setAttributeNS(XmlNS, "xmlns:mdssi", PackageNamespaces.DIGITAL_SIGNATURE);
                    }
                }
            }
        }
        public void setSignatureConfig(SignatureConfig signatureConfig) {
            this.signatureConfig = signatureConfig;
        }
    }
    
    
    private static final POILogger LOG = POILogFactory.getLogger(SignatureInfo.class);
    private static boolean isInitialized = false;
    
    private SignatureConfig signatureConfig;

    public SignatureConfig getSignatureConfig() {
        return signatureConfig;
    }

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    public boolean verifySignature() {
        initXmlProvider();
        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
        List<X509Certificate> signers = new ArrayList<X509Certificate>();
        return getSignersAndValidate(signers, true);
    }

    public void confirmSignature()
    throws NoSuchAlgorithmException, IOException, MarshalException, ParserConfigurationException, XmlException, InvalidAlgorithmParameterException, NoSuchProviderException, XMLSignatureException, TransformerFactoryConfigurationError, TransformerException, SAXException, URISyntaxException {
        Document document = DocumentHelper.createDocument();
        
        // operate
        DigestInfo digestInfo = preSign(document, null);

        // setup: key material, signature value
        byte[] signatureValue = signDigest(digestInfo.digestValue);
        
        // operate: postSign
        postSign(document, signatureValue);
    }

    public byte[] signDigest(byte digest[]) {
        Cipher cipher = CryptoFunctions.getCipher(signatureConfig.getKey(), CipherAlgorithm.rsa
            , ChainingMode.ecb, null, Cipher.ENCRYPT_MODE, "PKCS1Padding");
            
        try {
            ByteArrayOutputStream digestInfoValueBuf = new ByteArrayOutputStream();
            digestInfoValueBuf.write(getHashMagic());
            digestInfoValueBuf.write(digest);
            byte[] digestInfoValue = digestInfoValueBuf.toByteArray();
            byte[] signatureValue = cipher.doFinal(digestInfoValue);
            return signatureValue;
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
    }
    
    public List<X509Certificate> getSigners() {
        initXmlProvider();
        List<X509Certificate> signers = new ArrayList<X509Certificate>();
        getSignersAndValidate(signers, false);
        return signers;
    }
    
    protected boolean getSignersAndValidate(List<X509Certificate> signers, boolean onlyFirst) {
        signatureConfig.init(true);
        
        boolean allValid = true;
        List<PackagePart> signatureParts = getSignatureParts(onlyFirst);
        if (signatureParts.isEmpty()) {
            LOG.log(POILogger.DEBUG, "no signature resources");
            allValid = false;
        }

        for (PackagePart signaturePart : signatureParts) {
            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();

            try {
                Document doc = DocumentHelper.readDocument(signaturePart.getInputStream());
                registerIds(doc);
                
                DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
                domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);
                domValidateContext.setURIDereferencer(signatureConfig.getUriDereferencer());
    
                XMLSignatureFactory xmlSignatureFactory = getSignatureFactory();
                XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
                boolean validity = xmlSignature.validate(domValidateContext);
                allValid &= validity;
                if (!validity) continue;
                // TODO: check what has been signed.
            } catch (Exception e) {
                LOG.log(POILogger.ERROR, "error in marshalling and validating the signature", e);
                continue;
            }

            X509Certificate signer = keySelector.getCertificate();
            signers.add(signer);
        }
        
        return allValid;
    }

    protected List<PackagePart> getSignatureParts(boolean onlyFirst) {
        List<PackagePart> packageParts = new ArrayList<PackagePart>();
        OPCPackage pkg = signatureConfig.getOpcPackage();
        
        PackageRelationshipCollection sigOrigRels = pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        for (PackageRelationship rel : sigOrigRels) {
            PackagePart sigPart = pkg.getPart(rel);
            LOG.log(POILogger.DEBUG, "Digital Signature Origin part", sigPart);

            try {
                PackageRelationshipCollection sigRels = sigPart.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE);
                for (PackageRelationship sigRel : sigRels) {
                    PackagePart sigRelPart = sigPart.getRelatedPart(sigRel); 
                    LOG.log(POILogger.DEBUG, "XML Signature part", sigRelPart);
                    packageParts.add(sigRelPart);
                    if (onlyFirst) break;
                }
            } catch (InvalidFormatException e) {
                LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
            }
            
            if (onlyFirst && !packageParts.isEmpty()) break;
        }

        return packageParts;
    }
    
    public static XMLSignatureFactory getSignatureFactory() {
        return XMLSignatureFactory.getInstance("DOM", getProvider());
    }

    public static KeyInfoFactory getKeyInfoFactory() {
        return KeyInfoFactory.getInstance("DOM", getProvider());
    }

    // currently classes are linked to Apache Santuario, so this might be superfluous 
    public static Provider getProvider() {
        String dsigProviderNames[] = {
            System.getProperty("jsr105Provider"),
            "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI", // Santuario xmlsec
            "org.jcp.xml.dsig.internal.dom.XMLDSigRI"         // JDK xmlsec
        };
        for (String pn : dsigProviderNames) {
            if (pn == null) continue;
            try {
                return (Provider)Class.forName(pn).newInstance();
            } catch (Exception e) {
                LOG.log(POILogger.DEBUG, "XMLDsig-Provider '"+pn+"' can't be found - trying next.");
            }
        }

        throw new RuntimeException("JRE doesn't support default xml signature provider - set jsr105Provider system property!");
    }
    
    public static void setPrefix(Node el, String ns, String prefix) {
        if (ns.equals(el.getNamespaceURI())) el.setPrefix(prefix);
        NodeList nl = el.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            setPrefix(nl.item(i), ns, prefix);
        }
    }
    
    protected byte[] getHashMagic() {
        switch (signatureConfig.getDigestAlgo()) {
        case sha1: return SHA1_DIGEST_INFO_PREFIX;
        // sha224: return SHA224_DIGEST_INFO_PREFIX;
        case sha256: return SHA256_DIGEST_INFO_PREFIX;
        case sha384: return SHA384_DIGEST_INFO_PREFIX;
        case sha512: return SHA512_DIGEST_INFO_PREFIX;
        case ripemd128: return RIPEMD128_DIGEST_INFO_PREFIX;
        case ripemd160: return RIPEMD160_DIGEST_INFO_PREFIX;
        // case ripemd256: return RIPEMD256_DIGEST_INFO_PREFIX;
        default: throw new EncryptedDocumentException("Hash algorithm "+signatureConfig.getDigestAlgo()+" not supported for signing.");
        }
    }

    protected String getSignatureMethod() {
        switch (signatureConfig.getDigestAlgo()) {
        case sha1:   return ALGO_ID_SIGNATURE_RSA_SHA1;
        case sha256: return ALGO_ID_SIGNATURE_RSA_SHA256;
        case sha384: return ALGO_ID_SIGNATURE_RSA_SHA384;
        case sha512: return ALGO_ID_SIGNATURE_RSA_SHA512;
        case ripemd160: return ALGO_ID_MAC_HMAC_RIPEMD160;
        default: throw new EncryptedDocumentException("Hash algorithm "+signatureConfig.getDigestAlgo()+" not supported for signing.");
        }
    }

    
    
    public static synchronized void initXmlProvider() {
        if (isInitialized) return;
        isInitialized = true;
        
        try {
            Init.init();
            RelationshipTransformService.registerDsigProvider();
            CryptoFunctions.registerBouncyCastle();
        } catch (Exception e) {
            throw new RuntimeException("Xml & BouncyCastle-Provider initialization failed", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public DigestInfo preSign(Document document, List<DigestInfo> digestInfos)
        throws ParserConfigurationException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, MarshalException,
        javax.xml.crypto.dsig.XMLSignatureException,
        TransformerFactoryConfigurationError, TransformerException,
        IOException, SAXException, NoSuchProviderException, XmlException, URISyntaxException {
        SignatureInfo.initXmlProvider();
        signatureConfig.init(false);
        
        // it's necessary to explicitly set the mdssi namespace, but the sign() method has no
        // normal way to interfere with, so we need to add the namespace under the hand ...
        EventTarget target = (EventTarget)document;
        EventListener creationListener = signatureConfig.getSignCreationListener();
        if (creationListener != null) {
            if (creationListener instanceof SignCreationListener) {
                ((SignCreationListener)creationListener).setEventTarget(target);
            }
            target.addEventListener("DOMSubtreeModified", creationListener, false);
        }
        
        /*
         * Signature context construction.
         */
        XMLSignContext xmlSignContext = new DOMSignContext(signatureConfig.getKey(), document);
        URIDereferencer uriDereferencer = signatureConfig.getUriDereferencer();
        if (null != uriDereferencer) {
            xmlSignContext.setURIDereferencer(uriDereferencer);
        }

        xmlSignContext.putNamespacePrefix(
                "http://schemas.openxmlformats.org/package/2006/digital-signature",
                "mdssi");
        
        String sigNsPrefix = signatureConfig.getSignatureNamespacePrefix();
        if (sigNsPrefix != null) {
            /*
             * OOo doesn't like ds namespaces so per default prefixing is off.
             */
            xmlSignContext.putNamespacePrefix(XmlDSigNS, sigNsPrefix);
        }

        XMLSignatureFactory signatureFactory = SignatureInfo.getSignatureFactory();

        /*
         * Add ds:References that come from signing client local files.
         */
        List<Reference> references = new ArrayList<Reference>();
        for (DigestInfo digestInfo : safe(digestInfos)) {
            byte[] documentDigestValue = digestInfo.digestValue;

            DigestMethod digestMethod = signatureFactory.newDigestMethod(
                            digestInfo.hashAlgo.xmlSignUri, null);

            String uri = new File(digestInfo.description).getName();

            Reference reference = signatureFactory.newReference
                (uri, digestMethod, null, null, null, documentDigestValue);
            references.add(reference);
        }

        /*
         * Invoke the signature facets.
         */
        List<XMLObject> objects = new ArrayList<XMLObject>();
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            LOG.log(POILogger.DEBUG, "invoking signature facet: " + signatureFacet.getClass().getSimpleName());
            signatureFacet.preSign(document, signatureFactory, references, objects);
        }

        /*
         * ds:SignedInfo
         */
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(getSignatureMethod(), null);
        CanonicalizationMethod canonicalizationMethod = signatureFactory
            .newCanonicalizationMethod(signatureConfig.getCanonicalizationMethod(),
            (C14NMethodParameterSpec) null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(
            canonicalizationMethod, signatureMethod, references);

        /*
         * JSR105 ds:Signature creation
         */
        String signatureValueId = signatureConfig.getPackageSignatureId() + "-signature-value";
        javax.xml.crypto.dsig.XMLSignature xmlSignature = signatureFactory
            .newXMLSignature(signedInfo, null, objects, signatureConfig.getPackageSignatureId(),
            signatureValueId);

        /*
         * ds:Signature Marshalling.
         */
        xmlSignContext.setDefaultNamespacePrefix(signatureConfig.getSignatureNamespacePrefix());
        // xmlSignContext.putNamespacePrefix(PackageNamespaces.DIGITAL_SIGNATURE, "mdssi");
        xmlSignature.sign(xmlSignContext);

        registerIds(document);
        
        /*
         * Completion of undigested ds:References in the ds:Manifests.
         */
        for (XMLObject object : objects) {
            LOG.log(POILogger.DEBUG, "object java type: " + object.getClass().getName());
            List<XMLStructure> objectContentList = object.getContent();
            for (XMLStructure objectContent : objectContentList) {
                LOG.log(POILogger.DEBUG, "object content java type: " + objectContent.getClass().getName());
                if (!(objectContent instanceof Manifest)) continue;
                Manifest manifest = (Manifest) objectContent;
                List<Reference> manifestReferences = manifest.getReferences();
                for (Reference manifestReference : manifestReferences) {
                    if (manifestReference.getDigestValue() != null) continue;

                    DOMReference manifestDOMReference = (DOMReference)manifestReference;
                    manifestDOMReference.digest(xmlSignContext);
                }
            }
        }

        /*
         * Completion of undigested ds:References.
         */
        List<Reference> signedInfoReferences = signedInfo.getReferences();
        for (Reference signedInfoReference : signedInfoReferences) {
            DOMReference domReference = (DOMReference)signedInfoReference;

            // ds:Reference with external digest value
            if (domReference.getDigestValue() != null) continue;
            
            domReference.digest(xmlSignContext);
        }

        /*
         * Calculation of XML signature digest value.
         */
        DOMSignedInfo domSignedInfo = (DOMSignedInfo)signedInfo;
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        domSignedInfo.canonicalize(xmlSignContext, dataStream);
        byte[] octets = dataStream.toByteArray();

        /*
         * TODO: we could be using DigestOutputStream here to optimize memory
         * usage.
         */

        MessageDigest jcaMessageDigest = CryptoFunctions.getMessageDigest(signatureConfig.getDigestAlgo());
        byte[] digestValue = jcaMessageDigest.digest(octets);
        
        
        String description = signatureConfig.getSignatureDescription();
        return new DigestInfo(digestValue, signatureConfig.getDigestAlgo(), description);
    }

    public void postSign(Document document, byte[] signatureValue)
    throws IOException, MarshalException, ParserConfigurationException, XmlException {
        LOG.log(POILogger.DEBUG, "postSign");
        SignatureInfo.initXmlProvider();

        /*
         * Check ds:Signature node.
         */
        String signatureId = signatureConfig.getPackageSignatureId();
        if (!signatureId.equals(document.getDocumentElement().getAttribute("Id"))) {
            throw new RuntimeException("ds:Signature not found for @Id: " + signatureId);
        }

        /*
         * Insert signature value into the ds:SignatureValue element
         */
        NodeList sigValNl = document.getElementsByTagNameNS(XmlDSigNS, "SignatureValue");
        if (sigValNl.getLength() != 1) {
            throw new RuntimeException("preSign has to be called before postSign");
        }
        sigValNl.item(0).setTextContent(Base64.encode(signatureValue));

        /*
         * Allow signature facets to inject their own stuff.
         */
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            signatureFacet.postSign(document, signatureConfig.getSigningCertificateChain());
        }

        registerIds(document);
        writeDocument(document);
    }

    protected void writeDocument(Document document) throws IOException, XmlException {
        XmlOptions xo = new XmlOptions();
        Map<String,String> namespaceMap = new HashMap<String,String>();
        for (SignatureFacet sf : signatureConfig.getSignatureFacets()) {
            Map<String,String> sfm = sf.getNamespacePrefixMapping();
            if (sfm != null) {
                namespaceMap.putAll(sfm);
            }
        }
        xo.setSaveSuggestedPrefixes(namespaceMap);
        xo.setUseDefaultNamespace();

        LOG.log(POILogger.DEBUG, "output signed Office OpenXML document");

        /*
         * Copy the original OOXML content to the signed OOXML package. During
         * copying some files need to changed.
         */
        OPCPackage pkg = signatureConfig.getOpcPackage();

        PackagePartName sigPartName, sigsPartName;
        try {
            // <Override PartName="/_xmlsignatures/sig1.xml" ContentType="application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml"/>
            sigPartName = PackagingURIHelper.createPartName("/_xmlsignatures/sig1.xml");
            // <Default Extension="sigs" ContentType="application/vnd.openxmlformats-package.digital-signature-origin"/>
            sigsPartName = PackagingURIHelper.createPartName("/_xmlsignatures/origin.sigs");
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }
        
        String sigContentType = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";
        PackagePart sigPart = pkg.getPart(sigPartName);
        if (sigPart == null) {
            sigPart = pkg.createPart(sigPartName, sigContentType);
        }
        
        OutputStream os = sigPart.getOutputStream();
        SignatureDocument sigDoc = SignatureDocument.Factory.parse(document);
        sigDoc.save(os, xo);
        os.close();
        
        String sigsContentType = "application/vnd.openxmlformats-package.digital-signature-origin";
        PackagePart sigsPart = pkg.getPart(sigsPartName);
        if (sigsPart == null) {
            // touch empty marker file
            sigsPart = pkg.createPart(sigsPartName, sigsContentType);
        }
        
        PackageRelationshipCollection relCol = pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        for (PackageRelationship pr : relCol) {
            pkg.removeRelationship(pr.getId());
        }
        pkg.addRelationship(sigsPartName, TargetMode.INTERNAL, PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        
        sigsPart.addRelationship(sigPartName, TargetMode.INTERNAL, PackageRelationshipTypes.DIGITAL_SIGNATURE);
    }
    
    /**
     * the resulting document needs to be tweaked before it can be digested -
     * this applies to the verification and signing step
     *
     * @param doc
     */
    private static void registerIds(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(XmlDSigNS, "Object");
        registerIdAttribute(nl);
        nl = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#", "SignedProperties");
        registerIdAttribute(nl);
    }
    
    public static void registerIdAttribute(NodeList nl) {
        for (int i=0; i<nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            if (el.hasAttribute("Id")) {
                el.setIdAttribute("Id", true);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> safe(List<T> other) {
        return other == null ? Collections.EMPTY_LIST : other;
    }
}
