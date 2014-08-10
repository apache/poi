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

package org.apache.poi.poifs.crypt.dsig.facets;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.apache.poi.poifs.crypt.dsig.HorribleProxy;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.DOMKeyInfoIf;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3.x2000.x09.xmldsig.ObjectType;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.Node;

/**
 * Signature Facet implementation that adds ds:KeyInfo to the XML signature.
 * 
 * @author Frank Cornelis
 * 
 */
public class KeyInfoSignatureFacet implements SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(KeyInfoSignatureFacet.class);

    private final boolean includeEntireCertificateChain;

    private final boolean includeIssuerSerial;

    private final boolean includeKeyValue;

    /**
     * Main constructor.
     * 
     * @param includeEntireCertificateChain
     * @param includeIssuerSerial
     * @param includeKeyValue
     */
    public KeyInfoSignatureFacet(boolean includeEntireCertificateChain,
            boolean includeIssuerSerial, boolean includeKeyValue) {
        this.includeEntireCertificateChain = includeEntireCertificateChain;
        this.includeIssuerSerial = includeIssuerSerial;
        this.includeKeyValue = includeKeyValue;
    }

    public void postSign(SignatureType signatureElement,
            List<X509Certificate> signingCertificateChain) {
        LOG.log(POILogger.DEBUG, "postSign");

        List<ObjectType> objList = signatureElement.getObjectList();
        
        /*
         * Make sure we insert right after the ds:SignatureValue element, just
         * before the first ds:Object element.
         */
        Node nextSibling = (objList.isEmpty()) ? null : objList.get(0).getDomNode();

        /*
         * Construct the ds:KeyInfo element using JSR 105.
         */
        String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        Provider xmlDSigProv;
        try {
            xmlDSigProv = (Provider) Class.forName(providerName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("JRE doesn't support default xml signature provider - set jsr105Provider system property!", e);
        }
        
        KeyInfoFactory keyInfoFactory = KeyInfoFactory.getInstance("DOM", xmlDSigProv);
        List<Object> x509DataObjects = new LinkedList<Object>();
        X509Certificate signingCertificate = signingCertificateChain.get(0);

        List<Object> keyInfoContent = new LinkedList<Object>();

        if (this.includeKeyValue) {
            KeyValue keyValue;
            try {
                keyValue = keyInfoFactory.newKeyValue(signingCertificate.getPublicKey());
            } catch (KeyException e) {
                throw new RuntimeException("key exception: " + e.getMessage(), e);
            }
            keyInfoContent.add(keyValue);
        }

        if (this.includeIssuerSerial) {
            x509DataObjects.add(keyInfoFactory.newX509IssuerSerial(
                    signingCertificate.getIssuerX500Principal().toString(),
                    signingCertificate.getSerialNumber()));
        }

        if (this.includeEntireCertificateChain) {
            for (X509Certificate certificate : signingCertificateChain) {
                x509DataObjects.add(certificate);
            }
        } else {
            x509DataObjects.add(signingCertificate);
        }

        if (false == x509DataObjects.isEmpty()) {
            X509Data x509Data = keyInfoFactory.newX509Data(x509DataObjects);
            keyInfoContent.add(x509Data);
        }
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(keyInfoContent);
        DOMKeyInfoIf domKeyInfo;
        try {
            domKeyInfo = HorribleProxy.newProxy(DOMKeyInfoIf.class, keyInfo);
        } catch (Exception e) {
            throw new RuntimeException("DOMKeyInfo instance error: " + e.getMessage(), e);
        }        

        Key key = new Key() {
            private static final long serialVersionUID = 1L;

            public String getAlgorithm() {
                return null;
            }

            public byte[] getEncoded() {
                return null;
            }

            public String getFormat() {
                return null;
            }
        };

        DOMSignContext domSignContext = new DOMSignContext(key, signatureElement.getDomNode());
        DOMCryptoContext domCryptoContext = domSignContext;
        String signatureNamespacePrefix = "xd";
        try {
            domKeyInfo.marshal(signatureElement.getDomNode(), nextSibling,
                signatureNamespacePrefix, domCryptoContext);
        } catch (MarshalException e) {
            throw new RuntimeException("marshall error: " + e.getMessage(), e);
        }
    }

    public void preSign(XMLSignatureFactory signatureFactory,
        String signatureId,
        List<X509Certificate> signingCertificateChain,
        List<Reference> references,
        List<XMLObject> objects
    ) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // empty
    }

    public Map<String,String> getNamespacePrefixMapping() {
        Map<String,String> map = new HashMap<String,String>();
        // map.put("xd", "http://www.w3.org/2000/09/xmldsig#");
        return map;
    }

}