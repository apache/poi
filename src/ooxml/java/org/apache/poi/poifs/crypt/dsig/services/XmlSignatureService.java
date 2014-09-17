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

import static org.apache.poi.poifs.crypt.dsig.SignatureInfo.XmlDSigNS;
import static org.apache.poi.poifs.crypt.dsig.SignatureInfo.XmlNS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
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
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureInfoConfig;
import org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Base64;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.xml.sax.SAXException;


/**
 * Abstract base class for an XML Signature Service implementation.
 */
public class XmlSignatureService implements SignatureService {
    private static final POILogger LOG = POILogFactory.getLogger(XmlSignatureService.class);

    protected SignatureInfoConfig signatureConfig;
    
    private String signatureNamespacePrefix;
    private String signatureId = "idPackageSignature";
    
    /**
     * Main constructor.
     */
    public XmlSignatureService(SignatureInfoConfig signatureConfig) {
        this.signatureNamespacePrefix = null;
        this.signatureConfig = signatureConfig;
    }
    
    public SignatureInfoConfig getSignatureConfig() {
        return signatureConfig;
    }

    /**
     * Sets the signature Id attribute value used to create the XML signature. A
     * <code>null</code> value will trigger an automatically generated signature
     * Id.
     * 
     * @param signatureId
     */
    protected void setSignatureId(String signatureId) {
            this.signatureId = signatureId;
    }

    /**
     * Sets the XML Signature namespace prefix to be used for signature
     * creation. A <code>null</code> value will omit the prefixing.
     * 
     * @param signatureNamespacePrefix
     */
    protected void setSignatureNamespacePrefix(String signatureNamespacePrefix) {
        this.signatureNamespacePrefix = signatureNamespacePrefix;
    }

    /**
     * Gives back the human-readable description of what the citizen will be
     * signing. The default value is "XML Document". Override this method to
     * provide the citizen with another description.
     * 
     * @return
     */
    protected String getSignatureDescription() {
        return "Office OpenXML Document";
    }

    /**
     * Gives back the output stream to which to write the signed XML document.
     * 
     * @return
     */
    // protected abstract OutputStream getSignedDocumentOutputStream();
    @Override
    public DigestInfo preSign(Document document, List<DigestInfo> digestInfos)
    throws NoSuchAlgorithmException {
        SignatureInfo.initXmlProvider();

        LOG.log(POILogger.DEBUG, "preSign");
        HashAlgorithm hashAlgo = this.signatureConfig.getDigestAlgo();

        byte[] digestValue;
        try {
            digestValue = getXmlSignatureDigestValue(document, digestInfos);
        } catch (Exception e) {
            throw new RuntimeException("XML signature error: " + e.getMessage(), e);
        }

        String description = getSignatureDescription();
        return new DigestInfo(digestValue, hashAlgo, description);
    }

    @Override
    public void postSign(Document document, byte[] signatureValue)
    throws IOException, MarshalException, ParserConfigurationException, XmlException {
        LOG.log(POILogger.DEBUG, "postSign");
        SignatureInfo.initXmlProvider();

        /*
         * Check ds:Signature node.
         */
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
        for (SignatureFacet signatureFacet : this.signatureConfig.getSignatureFacets()) {
            signatureFacet.postSign(document, this.signatureConfig.getSigningCertificateChain());
        }

        registerIds(document);
        writeDocument(document);
    }

    @SuppressWarnings("unchecked")
    private byte[] getXmlSignatureDigestValue(Document document, List<DigestInfo> digestInfos)
        throws ParserConfigurationException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, MarshalException,
        javax.xml.crypto.dsig.XMLSignatureException,
        TransformerFactoryConfigurationError, TransformerException,
        IOException, SAXException, NoSuchProviderException, XmlException, URISyntaxException {

        // it's necessary to explicitly set the mdssi namespace, but the sign() method has no
        // normal way to interfere with, so we need to add the namespace under the hand ...
        final EventTarget et = (EventTarget)document;
        EventListener myModificationListener = new EventListener() {
            @Override
            public void handleEvent(Event e) {
                if (e instanceof MutationEvent) {
                    MutationEvent mutEvt = (MutationEvent)e;
                    if (mutEvt.getTarget() instanceof Element) {
                        Element el = (Element)mutEvt.getTarget();
                        if ("idPackageObject".equals(el.getAttribute("Id"))) {
                            et.removeEventListener("DOMSubtreeModified", this, false);
                            el.setAttributeNS(XmlNS, "xmlns:mdssi", PackageNamespaces.DIGITAL_SIGNATURE);
                        }
                    }
                }
            }
        };
        
        et.addEventListener("DOMSubtreeModified", myModificationListener, false);
        
        /*
         * Signature context construction.
         */
        XMLSignContext xmlSignContext = new DOMSignContext(this.signatureConfig.getKey(), document);
        URIDereferencer uriDereferencer = this.signatureConfig.getUriDereferencer();
        if (null != uriDereferencer) {
            xmlSignContext.setURIDereferencer(uriDereferencer);
        }

        xmlSignContext.putNamespacePrefix(
                "http://schemas.openxmlformats.org/package/2006/digital-signature",
                "mdssi");
        
        if (this.signatureNamespacePrefix != null) {
            /*
             * OOo doesn't like ds namespaces so per default prefixing is off.
             */
            xmlSignContext.putNamespacePrefix(XmlDSigNS, this.signatureNamespacePrefix);
        }

        XMLSignatureFactory signatureFactory = SignatureInfo.getSignatureFactory();

        /*
         * Add ds:References that come from signing client local files.
         */
        List<Reference> references = new ArrayList<Reference>();
        addDigestInfosAsReferences(digestInfos, signatureFactory, references);

        /*
         * Invoke the signature facets.
         */
        String localSignatureId = this.signatureId;
        if (localSignatureId == null) {
            localSignatureId = "xmldsig-" + UUID.randomUUID().toString();
        }
        List<XMLObject> objects = new ArrayList<XMLObject>();
        for (SignatureFacet signatureFacet : this.signatureConfig.getSignatureFacets()) {
            LOG.log(POILogger.DEBUG, "invoking signature facet: " + signatureFacet.getClass().getSimpleName());
            signatureFacet.preSign(document, signatureFactory, localSignatureId, this.signatureConfig.getSigningCertificateChain(), references, objects);
        }

        /*
         * ds:SignedInfo
         */
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(getSignatureMethod(this.signatureConfig.getDigestAlgo()), null);
        CanonicalizationMethod canonicalizationMethod = signatureFactory
            .newCanonicalizationMethod(getCanonicalizationMethod(),
            (C14NMethodParameterSpec) null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(
            canonicalizationMethod, signatureMethod, references);

        /*
         * JSR105 ds:Signature creation
         */
        String signatureValueId = localSignatureId + "-signature-value";
        javax.xml.crypto.dsig.XMLSignature xmlSignature = signatureFactory
            .newXMLSignature(signedInfo, null, objects, localSignatureId,
            signatureValueId);

        /*
         * ds:Signature Marshalling.
         */
        xmlSignContext.setDefaultNamespacePrefix(this.signatureNamespacePrefix);
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

        MessageDigest jcaMessageDigest = CryptoFunctions.getMessageDigest(this.signatureConfig.getDigestAlgo());
        byte[] digestValue = jcaMessageDigest.digest(octets);
        return digestValue;
    }

    /**
     * the resulting document needs to be tweaked before it can be digested -
     * this applies to the verification and signing step
     *
     * @param doc
     */
    public static void registerIds(Document doc) {
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
    
    private void addDigestInfosAsReferences(List<DigestInfo> digestInfos, XMLSignatureFactory signatureFactory, List<Reference> references)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, MalformedURLException {
        for (DigestInfo digestInfo : safe(digestInfos)) {
            byte[] documentDigestValue = digestInfo.digestValue;

            DigestMethod digestMethod = signatureFactory.newDigestMethod(
                            digestInfo.hashAlgo.xmlSignUri, null);

            String uri = new File(digestInfo.description).getName();

            Reference reference = signatureFactory.newReference(uri,
                            digestMethod, null, null, null, documentDigestValue);
            references.add(reference);
        }
    }

    private String getSignatureMethod(HashAlgorithm hashAlgo) {
        if (null == hashAlgo) {
            throw new RuntimeException("digest algo is null");
        }

        switch (hashAlgo) {
        case sha1:   return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
        case sha256: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
        case sha384: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384;
        case sha512: return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512;
        case ripemd160: return XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160;
        default: break;
        }

        throw new RuntimeException("unsupported sign algo: " + hashAlgo);
    }

    protected String getCanonicalizationMethod() {
        return CanonicalizationMethod.INCLUSIVE;
    }

    protected void writeDocument(Document document) throws IOException, XmlException {
        XmlOptions xo = new XmlOptions();
        Map<String,String> namespaceMap = new HashMap<String,String>();
        for (SignatureFacet sf : this.signatureConfig.getSignatureFacets()) {
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
        OPCPackage pkg = this.signatureConfig.getOpcPackage();

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

    @SuppressWarnings("unchecked")
    public static <T> List<T> safe(List<T> other) {
        return other == null ? Collections.EMPTY_LIST : other;
    }
}
