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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_DIGSIG_NS;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;


/**
 * <p>This class is the default entry point for XML signatures and can be used for
 * validating an existing signed office document and signing a office document.</p>
 *
 * <p><b>Validating a signed office document</b></p>
 *
 * <pre>
 * OPCPackage pkg = OPCPackage.open(..., PackageAccess.READ);
 * SignatureConfig sic = new SignatureConfig();
 * sic.setOpcPackage(pkg);
 * SignatureInfo si = new SignatureInfo();
 * si.setSignatureConfig(sic);
 * boolean isValid = si.validate();
 * ...
 * </pre>
 *
 * <p><b>Signing an office document</b></p>
 *
 * <pre>
 * // loading the keystore - pkcs12 is used here, but of course jks &amp; co are also valid
 * // the keystore needs to contain a private key and it's certificate having a
 * // 'digitalSignature' key usage
 * char password[] = "test".toCharArray();
 * File file = new File("test.pfx");
 * KeyStore keystore = KeyStore.getInstance("PKCS12");
 * FileInputStream fis = new FileInputStream(file);
 * keystore.load(fis, password);
 * fis.close();
 *
 * // extracting private key and certificate
 * String alias = "xyz"; // alias of the keystore entry
 * Key key = keystore.getKey(alias, password);
 * X509Certificate x509 = (X509Certificate)keystore.getCertificate(alias);
 *
 * // filling the SignatureConfig entries (minimum fields, more options are available ...)
 * SignatureConfig signatureConfig = new SignatureConfig();
 * signatureConfig.setKey(keyPair.getPrivate());
 * signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
 * OPCPackage pkg = OPCPackage.open(..., PackageAccess.READ_WRITE);
 * signatureConfig.setOpcPackage(pkg);
 *
 * // adding the signature document to the package
 * SignatureInfo si = new SignatureInfo();
 * si.setSignatureConfig(signatureConfig);
 * si.confirmSignature();
 * // optionally verify the generated signature
 * boolean b = si.verifySignature();
 * assert (b);
 * // write the changes back to disc
 * pkg.close();
 * </pre>
 *
 * <p><b>Implementation notes:</b></p>
 *
 * <p>Although there's a XML signature implementation in the Oracle JDKs 6 and higher,
 * compatibility with IBM JDKs is also in focus (... but maybe not thoroughly tested ...).
 * Therefore we are using the Apache Santuario libs (xmlsec) instead of the built-in classes,
 * as the compatibility seems to be provided there.</p>
 *
 * <p>To use SignatureInfo and its sibling classes, you'll need to have the following libs
 * in the classpath:</p>
 * <ul>
 * <li>BouncyCastle bcpkix and bcprov (tested against 1.67)</li>
 * <li>Apache Santuario "xmlsec" (tested against 2.2.0)</li>
 * <li>and slf4j-api (tested against 1.7.30)</li>
 * </ul>
 */
public class SignatureInfo {

    private static final POILogger LOG = POILogFactory.getLogger(SignatureInfo.class);

    private SignatureConfig signatureConfig;
    private OPCPackage opcPackage;
    private Provider provider;
    private XMLSignatureFactory signatureFactory;
    private KeyInfoFactory keyInfoFactory;
    private URIDereferencer uriDereferencer;

    /**
     * @return the signature config
     */
    public SignatureConfig getSignatureConfig() {
        return signatureConfig;
    }

    /**
     * @param signatureConfig the signature config, needs to be set before a SignatureInfo object is used
     */
    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    public void setOpcPackage(OPCPackage opcPackage) {
        this.opcPackage = opcPackage;
    }

    public OPCPackage getOpcPackage() {
        return opcPackage;
    }

    public URIDereferencer getUriDereferencer() {
        return uriDereferencer;
    }

    public void setUriDereferencer(URIDereferencer uriDereferencer) {
        this.uriDereferencer = uriDereferencer;
    }

    /**
     * @return true, if first signature part is valid
     */
    public boolean verifySignature() {
        initXmlProvider();
        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
        // only validate first part
        Iterator<SignaturePart> iter = getSignatureParts().iterator();
        return iter.hasNext() && iter.next().validate();
    }

    /**
     * add the xml signature to the document
     *
     * @throws XMLSignatureException if the signature can't be calculated
     * @throws MarshalException if the document can't be serialized
     */
    public void confirmSignature() throws XMLSignatureException, MarshalException {
        initXmlProvider();
        final Document document = DocumentHelper.createDocument();
        final DOMSignContext xmlSignContext = createXMLSignContext(document);

        // operate
        final DOMSignedInfo signedInfo = preSign(xmlSignContext);

        // setup: key material, signature value
        final String signatureValue = signDigest(xmlSignContext, signedInfo);

        // operate: postSign
        postSign(xmlSignContext, signatureValue);
    }

    /**
     * Convenience method for creating the signature context
     *
     * @param document the document the signature is based on
     *
     * @return the initialized signature context
     */
    public DOMSignContext createXMLSignContext(final Document document) {
        initXmlProvider();
        return new DOMSignContext(signatureConfig.getKey(), document);
    }


    /**
     * Sign (encrypt) the digest with the private key.
     * Currently only rsa is supported.
     *
     * @return the encrypted hash
     */
    public String signDigest(final DOMSignContext xmlSignContext, final DOMSignedInfo signedInfo) {
        initXmlProvider();
        final PrivateKey key = signatureConfig.getKey();
        final HashAlgorithm algo = signatureConfig.getDigestAlgo();

        // taken from org.apache.xml.security.utils.Base64
        final int BASE64DEFAULTLENGTH = 76;


        if (algo.hashSize*4/3 > BASE64DEFAULTLENGTH && !XMLUtils.ignoreLineBreaks()) {
            throw new EncryptedDocumentException("The hash size of the chosen hash algorithm ("+algo+" = "+algo.hashSize+" bytes), "+
                "will motivate XmlSec to add linebreaks to the generated digest, which results in an invalid signature (... at least "+
                "for Office) - please persuade it otherwise by adding '-Dorg.apache.xml.security.ignoreLineBreaks=true' to the JVM "+
                "system properties.");
        }

        try (final DigestOutputStream dos = getDigestStream(algo, key)) {
            dos.init();

            final Document document = (Document)xmlSignContext.getParent();
            final Element el = getDsigElement(document, "SignedInfo");
            final DOMSubTreeData subTree = new DOMSubTreeData(el, true);
            signedInfo.getCanonicalizationMethod().transform(subTree, xmlSignContext, dos);

            return Base64.getEncoder().encodeToString(dos.sign());
        } catch (GeneralSecurityException|IOException|TransformException e) {
            throw new EncryptedDocumentException(e);
        }
    }

    private static DigestOutputStream getDigestStream(final HashAlgorithm algo, final PrivateKey key) {
        switch (algo) {
            case md2: case md5: case sha1: case sha256: case sha384: case sha512:
                return new SignatureOutputStream(algo, key);
            default:
                return new DigestOutputStream(algo, key);
        }
    }

    /**
     * @return a signature part for each signature document.
     * the parts can be validated independently.
     */
    public Iterable<SignaturePart> getSignatureParts() {
        initXmlProvider();
        return SignaturePartIterator::new;
    }

    private final class SignaturePartIterator implements Iterator<SignaturePart> {
        Iterator<PackageRelationship> sigOrigRels;
        private Iterator<PackageRelationship> sigRels;
        private PackagePart sigPart;

        private SignaturePartIterator() {
            sigOrigRels = opcPackage.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN).iterator();
        }

        @Override
        public boolean hasNext() {
            while (sigRels == null || !sigRels.hasNext()) {
                if (!sigOrigRels.hasNext()) {
                    return false;
                }
                sigPart = opcPackage.getPart(sigOrigRels.next());
                LOG.log(POILogger.DEBUG, "Digital Signature Origin part", sigPart);
                try {
                    sigRels = sigPart.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE).iterator();
                } catch (InvalidFormatException e) {
                    LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
                }
            }
            return true;
        }

        @Override
        public SignaturePart next() {
            PackagePart sigRelPart = null;
            do {
                try {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    sigRelPart = sigPart.getRelatedPart(sigRels.next());
                    LOG.log(POILogger.DEBUG, "XML Signature part", sigRelPart);
                } catch (InvalidFormatException e) {
                    LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
                }
            } while (sigRelPart == null);
            return new SignaturePart(sigRelPart, SignatureInfo.this);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }



    /**
     * Helper method for adding informations before the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     */
    @SuppressWarnings("unchecked")
    public DOMSignedInfo preSign(final DOMSignContext xmlSignContext)
    throws XMLSignatureException, MarshalException {
        final Document document = (Document)xmlSignContext.getParent();

        registerEventListener(document);

        // Signature context construction.
        if (uriDereferencer != null) {
            xmlSignContext.setURIDereferencer(uriDereferencer);
        }

        signatureConfig.getNamespacePrefixes().forEach(xmlSignContext::putNamespacePrefix);

        xmlSignContext.setDefaultNamespacePrefix("");

        /*
         * Add ds:References that come from signing client local files.
         */
        List<Reference> references = new ArrayList<>();

        /*
         * Invoke the signature facets.
         */
        List<XMLObject> objects = new ArrayList<>();
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            LOG.log(POILogger.DEBUG, "invoking signature facet: ", signatureFacet.getClass().getSimpleName());
            signatureFacet.preSign(this, document, references, objects);
        }

        /*
         * ds:SignedInfo
         */
        SignedInfo signedInfo;
        try {
            SignatureMethod signatureMethod = signatureFactory.newSignatureMethod
                (signatureConfig.getSignatureMethodUri(), null);
            CanonicalizationMethod canonicalizationMethod = signatureFactory
                .newCanonicalizationMethod(signatureConfig.getCanonicalizationMethod(),
                (C14NMethodParameterSpec) null);
            signedInfo = signatureFactory.newSignedInfo(
                canonicalizationMethod, signatureMethod, references);
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException(e);
        }

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
        xmlSignature.sign(xmlSignContext);

        /*
         * Completion of undigested ds:References in the ds:Manifests.
         */
        for (XMLObject object : objects) {
            LOG.log(POILogger.DEBUG, "object java type: ", object.getClass().getName());
            List<XMLStructure> objectContentList = object.getContent();
            for (XMLStructure objectContent : objectContentList) {
                LOG.log(POILogger.DEBUG, "object content java type: ", objectContent.getClass().getName());
                if (!(objectContent instanceof Manifest)) {
                    continue;
                }
                Manifest manifest = (Manifest) objectContent;
                List<Reference> manifestReferences = manifest.getReferences();
                for (Reference manifestReference : manifestReferences) {
                    if (manifestReference.getDigestValue() != null) {
                        continue;
                    }

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
            if (domReference.getDigestValue() != null) {
                continue;
            }

            domReference.digest(xmlSignContext);
        }

        return (DOMSignedInfo)signedInfo;
    }

    // it's necessary to explicitly set the mdssi namespace, but the sign() method has no
    // normal way to interfere with, so we need to add the namespace under the hand ...
    protected void registerEventListener(Document document) {
        final SignatureMarshalListener sml = signatureConfig.getSignatureMarshalListener();
        if (sml == null) {
            return;
        }

        final EventListener[] el = { null };
        final EventTarget eventTarget = (EventTarget)document;
        final String eventType = "DOMSubtreeModified";
        final boolean DONT_USE_CAPTURE = false;

        el[0] = (e) -> {
            if (e instanceof MutationEvent && e.getTarget() instanceof Document) {
                eventTarget.removeEventListener(eventType, el[0], DONT_USE_CAPTURE);
                sml.handleElement(this, document, eventTarget, el[0]);
                eventTarget.addEventListener(eventType, el[0], DONT_USE_CAPTURE);
            }
        };

        eventTarget.addEventListener(eventType, el[0], DONT_USE_CAPTURE);
    }

    /**
     * Helper method for adding informations after the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     */
    public void postSign(final DOMSignContext xmlSignContext, final String signatureValue)
    throws MarshalException {
        LOG.log(POILogger.DEBUG, "postSign");

        final Document document = (Document)xmlSignContext.getParent();

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
        final Element signatureNode = getDsigElement(document, "SignatureValue");
        if (signatureNode == null) {
            throw new RuntimeException("preSign has to be called before postSign");
        }
        signatureNode.setTextContent(signatureValue);

        /*
         * Allow signature facets to inject their own stuff.
         */
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            signatureFacet.postSign(this, document);
        }

        writeDocument(document);
    }

    /**
     * Write XML signature into the OPC package
     *
     * @param document the xml signature document
     * @throws MarshalException if the document can't be serialized
     */
    protected void writeDocument(Document document) throws MarshalException {
        XmlOptions xo = new XmlOptions();
        Map<String,String> namespaceMap = new HashMap<>();
        signatureConfig.getNamespacePrefixes().forEach((k,v) -> namespaceMap.put(v,k));
        xo.setSaveSuggestedPrefixes(namespaceMap);
        xo.setUseDefaultNamespace();

        LOG.log(POILogger.DEBUG, "output signed Office OpenXML document");

        /*
         * Copy the original OOXML content to the signed OOXML package. During
         * copying some files need to changed.
         */
        try {
            // <Default Extension="sigs" ContentType="application/vnd.openxmlformats-package.digital-signature-origin"/>
            final DSigRelation originDesc = DSigRelation.ORIGIN_SIGS;
            PackagePartName originPartName = PackagingURIHelper.createPartName(originDesc.getFileName(0));

            PackagePart originPart = opcPackage.getPart(originPartName);
            if (originPart == null) {
                // touch empty marker file
                originPart = opcPackage.createPart(originPartName, originDesc.getContentType());
                opcPackage.addRelationship(originPartName, TargetMode.INTERNAL, originDesc.getRelation());
            }

            // <Override PartName="/_xmlsignatures/sig1.xml" ContentType="application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml"/>
            final DSigRelation sigDesc = DSigRelation.SIG;
            int nextSigIdx = opcPackage.getUnusedPartIndex(sigDesc.getDefaultFileName());

            if (!signatureConfig.isAllowMultipleSignatures()) {
                PackageRelationshipCollection prc = originPart.getRelationshipsByType(sigDesc.getRelation());
                for (int i=2; i<nextSigIdx; i++) {
                    PackagePartName pn = PackagingURIHelper.createPartName(sigDesc.getFileName(i));
                    for (PackageRelationship rel : prc) {
                        PackagePart pp = originPart.getRelatedPart(rel);
                        if (pp.getPartName().equals(pn)) {
                            originPart.removeRelationship(rel.getId());
                            prc.removeRelationship(rel.getId());
                            break;
                        }
                    }

                    opcPackage.removePart(opcPackage.getPart(pn));
                }
                nextSigIdx = 1;
            }


            PackagePartName sigPartName = PackagingURIHelper.createPartName(sigDesc.getFileName(nextSigIdx));
            PackagePart sigPart = opcPackage.getPart(sigPartName);
            if (sigPart == null) {
                sigPart = opcPackage.createPart(sigPartName, sigDesc.getContentType());
                originPart.addRelationship(sigPartName, TargetMode.INTERNAL, sigDesc.getRelation());
            } else {
                sigPart.clear();
            }

            try (OutputStream os = sigPart.getOutputStream()) {
                SignatureDocument sigDoc = SignatureDocument.Factory.parse(document, DEFAULT_XML_OPTIONS);
                sigDoc.save(os, xo);
            }

        } catch (Exception e) {
            throw new MarshalException("Unable to write signature document", e);
        }
    }

    private Element getDsigElement(final Document document, final String localName) {
        NodeList sigValNl = document.getElementsByTagNameNS(XML_DIGSIG_NS, localName);
        if (sigValNl.getLength() == 1) {
            return (Element)sigValNl.item(0);
        }

        LOG.log(POILogger.WARN, "Signature element '", localName, "' was ",
                (sigValNl.getLength() == 0 ? "not found" : "multiple times"));

        return null;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setSignatureFactory(XMLSignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    public XMLSignatureFactory getSignatureFactory() {
        return signatureFactory;
    }

    public void setKeyInfoFactory(KeyInfoFactory keyInfoFactory) {
        this.keyInfoFactory = keyInfoFactory;
    }

    public KeyInfoFactory getKeyInfoFactory() {
        return keyInfoFactory;
    }



    /**
     * Initialize the xml signing environment and the bouncycastle provider
     */
    @SuppressWarnings("deprecation")
    protected void initXmlProvider() {
        if (opcPackage == null) {
            opcPackage = signatureConfig.getOpcPackage();
        }
        if (provider == null) {
            provider = signatureConfig.getProvider();
            if (provider == null) {
                provider = XmlProviderInitSingleton.getInstance().findProvider();
            }
        }
        if (signatureFactory == null) {
            signatureFactory = signatureConfig.getSignatureFactory();
            if (signatureFactory == null) {
                signatureFactory = XMLSignatureFactory.getInstance("DOM", provider);
            }
        }
        if (keyInfoFactory == null) {
            keyInfoFactory = signatureConfig.getKeyInfoFactory();
            if (keyInfoFactory == null) {
                keyInfoFactory = KeyInfoFactory.getInstance("DOM", provider);
            }
        }
        if (uriDereferencer == null) {
            uriDereferencer = signatureConfig.getUriDereferencer();
            if (uriDereferencer == null) {
                uriDereferencer = new OOXMLURIDereferencer();
            }
        }
        if (uriDereferencer instanceof OOXMLURIDereferencer) {
            ((OOXMLURIDereferencer)uriDereferencer).setSignatureInfo(this);
        }
    }

    private static final class XmlProviderInitSingleton {

        // Bill Pugh Singleton
        private static class SingletonHelper {
            private static final XmlProviderInitSingleton INSTANCE = new XmlProviderInitSingleton();
        }

        public static XmlProviderInitSingleton getInstance(){
            return SingletonHelper.INSTANCE;
        }

        private XmlProviderInitSingleton() {
            try {
                Init.init();
                RelationshipTransformService.registerDsigProvider();
                CryptoFunctions.registerBouncyCastle();
            } catch (Exception e) {
                throw new RuntimeException("Xml & BouncyCastle-Provider initialization failed", e);
            }
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
        public Provider findProvider() {
            return
                Stream.of(SignatureConfig.getProviderNames())
                .map(this::getProvider)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(this::providerNotFound);
        }

        private Provider getProvider(String className) {
            try {
                return (Provider)Class.forName(className).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOG.log(POILogger.DEBUG, "XMLDsig-Provider '", className, "' can't be found - trying next.");
                return null;
            }
        }

        private RuntimeException providerNotFound() {
            return new RuntimeException("JRE doesn't support default xml signature provider - set jsr105Provider system property!");
        }
    }

}
