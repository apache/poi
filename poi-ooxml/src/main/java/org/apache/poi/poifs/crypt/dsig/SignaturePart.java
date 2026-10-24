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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.MS_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_DIGSIG_NS;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.xmlbeans.XmlException;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SignaturePart {
    private static final Logger LOG = PoiLogManager.getLogger(SignaturePart.class);
    private static final String XMLSEC_VALIDATE_MANIFEST = "org.jcp.xml.dsig.validateManifests";
    private static final String XMLSEC_VALIDATE_SECURE = "org.apache.jcp.xml.dsig.secureValidation";


    private final PackagePart signaturePart;
    private final SignatureInfo signatureInfo;
    private X509Certificate signer;
    private List<X509Certificate> certChain;

    /* package */ SignaturePart(final PackagePart signaturePart, final SignatureInfo signatureInfo) {
        this.signaturePart = signaturePart;
        this.signatureInfo = signatureInfo;
    }

    /**
     * @return the package part containing the signature
     */
    public PackagePart getPackagePart() {
        return signaturePart;
    }

    /**
     * @return the signer certificate
     */
    public X509Certificate getSigner() {
        return signer;
    }

    /**
     * @return the certificate chain of the signer
     */
    public List<X509Certificate> getCertChain() {
        return certChain;
    }

    /**
     * Helper method for examining the xml signature
     *
     * @return the xml signature document
     * @throws IOException if the xml signature doesn't exist or can't be read
     * @throws XmlException if the xml signature is malformed
     */
    public SignatureDocument getSignatureDocument() throws IOException, XmlException {
        try (InputStream stream = signaturePart.getInputStream()) {
            return SignatureDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
        }
    }

    /**
     * @return true, when the xml signature is valid, false otherwise
     *
     * @throws EncryptedDocumentException if the signature can't be extracted or if its malformed
     */
    public boolean validate() {
        KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
        XPath xpath = XPathHelper.getFactory().newXPath();
        xpath.setNamespaceContext(new XPathNSContext());

        try {
            Document doc;
            try (InputStream stream = signaturePart.getInputStream()) {
                doc = DocumentHelper.readDocument(stream);
            }

            NodeList nl = (NodeList)xpath.compile("//*[@Id]").evaluate(doc, XPathConstants.NODESET);
            final int length = nl.getLength();
            for (int i=0; i<length; i++) {
                ((Element)nl.item(i)).setIdAttribute("Id", true);
            }

            DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
            domValidateContext.setProperty(XMLSEC_VALIDATE_MANIFEST, Boolean.TRUE);
            domValidateContext.setProperty(XMLSEC_VALIDATE_SECURE, signatureInfo.getSignatureConfig().isSecureValidation());

            URIDereferencer uriDereferencer = signatureInfo.getUriDereferencer();
            domValidateContext.setURIDereferencer(uriDereferencer);

            XMLSignatureFactory xmlSignatureFactory = signatureInfo.getSignatureFactory();
            XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);

            boolean valid = xmlSignature.validate(domValidateContext) && hasSignedRelationships(doc, xmlSignature);

            if (valid) {
                signer = keySelector.getSigner();
                certChain = keySelector.getCertChain();
                extractConfig(doc, xmlSignature);
            }

            return valid;
        } catch (IOException e) {
            String s = "error in reading document";
            LOG.atError().withThrowable(e).log(s);
            throw new EncryptedDocumentException(s, e);
        } catch (SAXException e) {
            String s = "error in parsing document";
            LOG.atError().withThrowable(e).log(s);
            throw new EncryptedDocumentException(s, e);
        } catch (XPathExpressionException e) {
            String s = "error in searching document with xpath expression";
            LOG.atError().withThrowable(e).log(s);
            throw new EncryptedDocumentException(s, e);
        } catch (MarshalException e) {
            String s = "error in unmarshalling the signature";
            LOG.atError().withThrowable(e).log(s);
            throw new EncryptedDocumentException(s, e);
        } catch (XMLSignatureException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                if (t instanceof OOXMLURIDereferencer.ContentTypeMismatchException) {
                    LOG.atWarn().log("the signature is invalid: {}", t.getMessage());
                    return false;
                }
            }
            String s = "error in validating the signature";
            LOG.atError().withThrowable(e).log(s);
            throw new EncryptedDocumentException(s, e);
        }
    }

    /**
     * Checks that no relationship which is not covered by the signature has the same type as a
     * signed relationship of the same source, as the package could resolve that type to it
     */
    private boolean hasSignedRelationships(Document doc, XMLSignature xmlSignature) throws XMLSignatureException {
        // the relationship ids and types per relationships part, which are listed in the transforms
        // of the manifests in the objects referenced by the (validated) SignedInfo
        Map<String,Set<String>> signedIds = new HashMap<>();
        Map<String,Set<String>> signedTypes = new HashMap<>();
        for (Reference ref : xmlSignature.getSignedInfo().getReferences()) {
            String uri = ref.getURI();
            Element obj = (uri == null || !uri.startsWith("#")) ? null : doc.getElementById(uri.substring(1));
            if (obj == null) {
                continue;
            }
            NodeList refs = obj.getElementsByTagNameNS(XML_DIGSIG_NS, "Reference");
            for (int i=0; i<refs.getLength(); i++) {
                Element manifestRef = (Element)refs.item(i);
                if (!"Manifest".equals(manifestRef.getParentNode().getLocalName()) || !hasRelationshipTransform(manifestRef)) {
                    continue;
                }
                String partName = manifestRef.getAttribute("URI").replaceFirst("\\?.*", "").toLowerCase(Locale.ROOT);
                addAttributes(manifestRef, "RelationshipReference", "SourceId", signedIds.computeIfAbsent(partName, k -> new HashSet<>()));
                addAttributes(manifestRef, "RelationshipsGroupReference", "SourceType", signedTypes.computeIfAbsent(partName, k -> new HashSet<>()));
            }
        }

        for (PackagePart pp : signatureInfo.getOpcPackage().getPartsByContentType(ContentTypes.RELATIONSHIPS_PART)) {
            String partName = pp.getPartName().getName().toLowerCase(Locale.ROOT);
            PackageRelationshipCollection prc;
            try {
                prc = new PackageRelationshipCollection(signatureInfo.getOpcPackage());
                prc.parseRelationshipsPart(pp);
            } catch (InvalidFormatException e) {
                throw new XMLSignatureException("Invalid relationship descriptor: " + pp.getPartName().getName(), e);
            }
            Set<String> ids = signedIds.getOrDefault(partName, Set.of());
            Set<String> groupTypes = signedTypes.getOrDefault(partName, Set.of());
            Set<String> types = new HashSet<>();
            for (PackageRelationship rel : prc) {
                if (ids.contains(rel.getId())) {
                    types.add(rel.getRelationshipType());
                }
            }
            for (PackageRelationship rel : prc) {
                String type = rel.getRelationshipType();
                if (!ids.contains(rel.getId()) && !groupTypes.contains(type) && types.contains(type)) {
                    LOG.atWarn().log("relationship {} in {} is not covered by the signature", rel.getId(), pp.getPartName().getName());
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasRelationshipTransform(Element manifestRef) {
        NodeList transforms = manifestRef.getElementsByTagNameNS(XML_DIGSIG_NS, "Transform");
        for (int i=0; i<transforms.getLength(); i++) {
            if (RelationshipTransformService.TRANSFORM_URI.equals(((Element)transforms.item(i)).getAttribute("Algorithm"))) {
                return true;
            }
        }
        return false;
    }

    private static void addAttributes(Element manifestRef, String localName, String attribute, Set<String> values) {
        NodeList nl = manifestRef.getElementsByTagNameNS(OO_DIGSIG_NS, localName);
        for (int i=0; i<nl.getLength(); i++) {
            values.add(((Element)nl.item(i)).getAttribute(attribute));
        }
    }

    private void extractConfig(final Document doc, final XMLSignature xmlSignature) throws XPathExpressionException {
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        if (!signatureConfig.isUpdateConfigOnValidate()) {
            return;
        }

        signatureConfig.setSigningCertificateChain(certChain);
        signatureConfig.setSignatureMethodFromUri(xmlSignature.getSignedInfo().getSignatureMethod().getAlgorithm());

        final XPath xpath = XPathHelper.getFactory().newXPath();
        xpath.setNamespaceContext(new XPathNSContext());

        final Map<String,Consumer<String>> m = new HashMap<>();
        m.put("//mdssi:SignatureTime/mdssi:Value", signatureConfig::setExecutionTime);
        m.put("//xd:ClaimedRole", signatureConfig::setXadesRole);
        m.put("//dsss:SignatureComments", signatureConfig::setSignatureDescription);
        m.put("//xd:QualifyingProperties//xd:SignedSignatureProperties//ds:DigestMethod/@Algorithm", signatureConfig::setXadesDigestAlgo);
        m.put("//ds:CanonicalizationMethod", signatureConfig::setCanonicalizationMethod);
        m.put("//xd:CommitmentTypeId/xd:Description", signatureConfig::setCommitmentType);

        for (Map.Entry<String,Consumer<String>> me : m.entrySet()) {
            String val = (String)xpath.compile(me.getKey()).evaluate(doc, XPathConstants.STRING);
            me.getValue().accept(val);
        }
    }

    private class XPathNSContext implements NamespaceContext {
        final Map<String,String> nsMap = new HashMap<>();

        {
            signatureInfo.getSignatureConfig().getNamespacePrefixes().forEach((k,v) -> nsMap.put(v,k));
            nsMap.put("dsss", MS_DIGSIG_NS);
            nsMap.put("ds", XML_DIGSIG_NS);
        }

        public String getNamespaceURI(String prefix) {
            return nsMap.get(prefix);
        }
        @SuppressWarnings("rawtypes")
        @Override
        public Iterator getPrefixes(String val) {
            return null;
        }
        public String getPrefix(String uri) {
            return null;
        }
    }
}
