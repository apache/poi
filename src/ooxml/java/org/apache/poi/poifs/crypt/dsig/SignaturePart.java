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

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ooxml.util.DocumentHelper;
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
    private final SignatureConfig signatureConfig;
    private X509Certificate signer;
    private List<X509Certificate> certChain;
    
    /* package */ SignaturePart(final PackagePart signaturePart, final SignatureConfig signatureConfig) {
        this.signaturePart = signaturePart;
        this.signatureConfig = signatureConfig;
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
        try {
            Document doc = DocumentHelper.readDocument(signaturePart.getInputStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nl = (NodeList)xpath.compile("//*[@Id]").evaluate(doc, XPathConstants.NODESET);
            final int length = nl.getLength();
            for (int i=0; i<length; i++) {
                ((Element)nl.item(i)).setIdAttribute("Id", true);
            }
            
            DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
            domValidateContext.setProperty(XMLSEC_VALIDATE_MANIFEST, Boolean.TRUE);
            domValidateContext.setURIDereferencer(signatureConfig.getUriDereferencer());

            XMLSignatureFactory xmlSignatureFactory = signatureConfig.getSignatureFactory();
            XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
            
            boolean valid = xmlSignature.validate(domValidateContext);

            if (valid) {
                signer = keySelector.getSigner();
                certChain = keySelector.getCertChain();
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
}
