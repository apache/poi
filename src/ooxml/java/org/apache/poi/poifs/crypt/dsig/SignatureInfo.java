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

import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_DIGSIG_NS;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.xml.sax.SAXException;


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
 * <p><b>Signing a office document</b></p>
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
 * OPCPackage pkg = OPCPackage.open(..., PackageAccess.READ);
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
 * <li>BouncyCastle bcpkix and bcprov (tested against 1.51)</li>
 * <li>Apache Santuario "xmlsec" (tested against 2.0.1)</li>
 * <li>and slf4j-api (tested against 1.7.7)</li>
 * </ul>
 */
public class SignatureInfo implements SignatureConfigurable {

    private static final POILogger LOG = POILogFactory.getLogger(SignatureInfo.class);
    private static boolean isInitialized = false;
    
    private SignatureConfig signatureConfig;

    public class SignaturePart {
        private final PackagePart signaturePart;
        private X509Certificate signer;
        private List<X509Certificate> certChain;
        
        private SignaturePart(PackagePart signaturePart) {
            this.signaturePart = signaturePart;
        }
        
        public PackagePart getPackagePart() {
            return signaturePart;
        }
        
        public X509Certificate getSigner() {
            return signer;
        }
        
        public List<X509Certificate> getCertChain() {
            return certChain;
        }
        
        public SignatureDocument getSignatureDocument() throws IOException, XmlException {
            // TODO: check for XXE
            return SignatureDocument.Factory.parse(signaturePart.getInputStream());
        }
        
        public boolean validate() {
            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
            try {
                Document doc = DocumentHelper.readDocument(signaturePart.getInputStream());
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nl = (NodeList)xpath.compile("//*[@Id]").evaluate(doc, XPathConstants.NODESET);
                for (int i=0; i<nl.getLength(); i++) {
                    ((Element)nl.item(i)).setIdAttribute("Id", true);
                }
                
                DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
                domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);
                domValidateContext.setURIDereferencer(signatureConfig.getUriDereferencer());
    
                XMLSignatureFactory xmlSignatureFactory = getSignatureFactory();
                XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
                boolean valid = xmlSignature.validate(domValidateContext);

                if (valid) {
                    signer = keySelector.getSigner();
                    certChain = keySelector.getCertChain();
                }
                
                return valid;
            } catch (Exception e) {
                LOG.log(POILogger.ERROR, "error in marshalling and validating the signature", e);
                return false;
            }
        }
    }
    
    public SignatureInfo() {
        initXmlProvider();        
    }
    
    public SignatureConfig getSignatureConfig() {
        return signatureConfig;
    }

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    public boolean verifySignature() {
        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
        for (SignaturePart sp : getSignatureParts()){
            // only validate first part
            return sp.validate();
        }
        return false;
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
            digestInfoValueBuf.write(signatureConfig.getHashMagic());
            digestInfoValueBuf.write(digest);
            byte[] digestInfoValue = digestInfoValueBuf.toByteArray();
            byte[] signatureValue = cipher.doFinal(digestInfoValue);
            return signatureValue;
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
    }
    
    public Iterable<SignaturePart> getSignatureParts() {
        signatureConfig.init(true);
        return new Iterable<SignaturePart>() {
            public Iterator<SignaturePart> iterator() {
                return new Iterator<SignaturePart>() {
                    OPCPackage pkg = signatureConfig.getOpcPackage();
                    Iterator<PackageRelationship> sigOrigRels = 
                        pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN).iterator();
                    Iterator<PackageRelationship> sigRels = null;
                    PackagePart sigPart = null;
                    
                    public boolean hasNext() {
                        while (sigRels == null || !sigRels.hasNext()) {
                            if (!sigOrigRels.hasNext()) return false;
                            sigPart = pkg.getPart(sigOrigRels.next());
                            LOG.log(POILogger.DEBUG, "Digital Signature Origin part", sigPart);
                            try {
                                sigRels = sigPart.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE).iterator();
                            } catch (InvalidFormatException e) {
                                LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
                            }
                        }
                        return true;
                    }
                    
                    public SignaturePart next() {
                        PackagePart sigRelPart = null;
                        do {
                            try {
                                if (!hasNext()) throw new NoSuchElementException();
                                sigRelPart = sigPart.getRelatedPart(sigRels.next()); 
                                LOG.log(POILogger.DEBUG, "XML Signature part", sigRelPart);
                            } catch (InvalidFormatException e) {
                                LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
                            }
                        } while (sigPart == null);
                        return new SignaturePart(sigRelPart);
                    }
                    
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
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
    
    protected static synchronized void initXmlProvider() {
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
    
    /**
     * Helper method for adding informations before the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     */
    @SuppressWarnings("unchecked")
    public DigestInfo preSign(Document document, List<DigestInfo> digestInfos)
        throws ParserConfigurationException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, MarshalException,
        javax.xml.crypto.dsig.XMLSignatureException,
        TransformerFactoryConfigurationError, TransformerException,
        IOException, SAXException, NoSuchProviderException, XmlException, URISyntaxException {
        signatureConfig.init(false);
        
        // it's necessary to explicitly set the mdssi namespace, but the sign() method has no
        // normal way to interfere with, so we need to add the namespace under the hand ...
        EventTarget target = (EventTarget)document;
        EventListener creationListener = signatureConfig.getSignatureMarshalListener();
        if (creationListener != null) {
            if (creationListener instanceof SignatureMarshalListener) {
                ((SignatureMarshalListener)creationListener).setEventTarget(target);
            }
            SignatureMarshalListener.setListener(target, creationListener, true);
        }
        
        /*
         * Signature context construction.
         */
        XMLSignContext xmlSignContext = new DOMSignContext(signatureConfig.getKey(), document);
        URIDereferencer uriDereferencer = signatureConfig.getUriDereferencer();
        if (null != uriDereferencer) {
            xmlSignContext.setURIDereferencer(uriDereferencer);
        }

        for (Map.Entry<String,String> me : signatureConfig.getNamespacePrefixes().entrySet()) {
            xmlSignContext.putNamespacePrefix(me.getKey(), me.getValue());
        }
        xmlSignContext.setDefaultNamespacePrefix(""); // signatureConfig.getNamespacePrefixes().get(XML_DIGSIG_NS));
        
        XMLSignatureFactory signatureFactory = SignatureInfo.getSignatureFactory();

        /*
         * Add ds:References that come from signing client local files.
         */
        List<Reference> references = new ArrayList<Reference>();
        for (DigestInfo digestInfo : safe(digestInfos)) {
            byte[] documentDigestValue = digestInfo.digestValue;

            DigestMethod digestMethod = signatureFactory.newDigestMethod
                (signatureConfig.getDigestMethodUri(), null);

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
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod
            (signatureConfig.getSignatureMethod(), null);
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
        xmlSignature.sign(xmlSignContext);

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

        MessageDigest md = CryptoFunctions.getMessageDigest(signatureConfig.getDigestAlgo());
        byte[] digestValue = md.digest(octets);
        
        
        String description = signatureConfig.getSignatureDescription();
        return new DigestInfo(digestValue, signatureConfig.getDigestAlgo(), description);
    }

    /**
     * Helper method for adding informations after the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     */
    public void postSign(Document document, byte[] signatureValue)
    throws IOException, MarshalException, ParserConfigurationException, XmlException {
        LOG.log(POILogger.DEBUG, "postSign");

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
        NodeList sigValNl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "SignatureValue");
        if (sigValNl.getLength() != 1) {
            throw new RuntimeException("preSign has to be called before postSign");
        }
        sigValNl.item(0).setTextContent(Base64.encode(signatureValue));

        /*
         * Allow signature facets to inject their own stuff.
         */
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            signatureFacet.postSign(document);
        }

        writeDocument(document);
    }

    protected void writeDocument(Document document) throws IOException, XmlException {
        XmlOptions xo = new XmlOptions();
        Map<String,String> namespaceMap = new HashMap<String,String>();
        for(Map.Entry<String,String> entry : signatureConfig.getNamespacePrefixes().entrySet()){
            namespaceMap.put(entry.getValue(), entry.getKey());
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
        
        PackagePart sigPart = pkg.getPart(sigPartName);
        if (sigPart == null) {
            sigPart = pkg.createPart(sigPartName, ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART);
        }
        
        OutputStream os = sigPart.getOutputStream();
        SignatureDocument sigDoc = SignatureDocument.Factory.parse(document);
        sigDoc.save(os, xo);
        os.close();
        
        PackagePart sigsPart = pkg.getPart(sigsPartName);
        if (sigsPart == null) {
            // touch empty marker file
            sigsPart = pkg.createPart(sigsPartName, ContentTypes.DIGITAL_SIGNATURE_ORIGIN_PART);
        }
        
        PackageRelationshipCollection relCol = pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        for (PackageRelationship pr : relCol) {
            pkg.removeRelationship(pr.getId());
        }
        pkg.addRelationship(sigsPartName, TargetMode.INTERNAL, PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        
        sigsPart.addRelationship(sigPartName, TargetMode.INTERNAL, PackageRelationshipTypes.DIGITAL_SIGNATURE);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> List<T> safe(List<T> other) {
        return other == null ? Collections.EMPTY_LIST : other;
    }
}
