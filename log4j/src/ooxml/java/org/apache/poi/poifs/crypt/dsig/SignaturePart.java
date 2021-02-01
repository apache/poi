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
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_DIGSIG_NS;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlException;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SignaturePart {
    private static final POILogger LOG = POILogFactory.getLogger(SignaturePart.class);
    private static final String XMLSEC_VALIDATE_MANIFEST = "org.jcp.xml.dsig.validateManifests";


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
        // TODO: check for XXE
        return SignatureDocument.Factory.parse(signaturePart.getInputStream(), DEFAULT_XML_OPTIONS);
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
            Document doc = DocumentHelper.readDocument(signaturePart.getInputStream());
            NodeList nl = (NodeList)xpath.compile("//*[@Id]").evaluate(doc, XPathConstants.NODESET);
            final int length = nl.getLength();
            for (int i=0; i<length; i++) {
                ((Element)nl.item(i)).setIdAttribute("Id", true);
            }

            DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
            domValidateContext.setProperty(XMLSEC_VALIDATE_MANIFEST, Boolean.TRUE);

            URIDereferencer uriDereferencer = signatureInfo.getUriDereferencer();
            domValidateContext.setURIDereferencer(uriDereferencer);

            XMLSignatureFactory xmlSignatureFactory = signatureInfo.getSignatureFactory();
            XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);

            boolean valid = xmlSignature.validate(domValidateContext);

            if (valid) {
                signer = keySelector.getSigner();
                certChain = keySelector.getCertChain();
                extractConfig(doc, xmlSignature);
            }

            return valid;
        } catch (IOException e) {
            String s = "error in reading document";
            LOG.log(POILogger.ERROR, s, e);
            throw new EncryptedDocumentException(s, e);
        } catch (SAXException e) {
            String s = "error in parsing document";
            LOG.log(POILogger.ERROR, s, e);
            throw new EncryptedDocumentException(s, e);
        } catch (XPathExpressionException e) {
            String s = "error in searching document with xpath expression";
            LOG.log(POILogger.ERROR, s, e);
            throw new EncryptedDocumentException(s, e);
        } catch (MarshalException e) {
            String s = "error in unmarshalling the signature";
            LOG.log(POILogger.ERROR, s, e);
            throw new EncryptedDocumentException(s, e);
        } catch (XMLSignatureException e) {
            String s = "error in validating the signature";
            LOG.log(POILogger.ERROR, s, e);
            throw new EncryptedDocumentException(s, e);
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
