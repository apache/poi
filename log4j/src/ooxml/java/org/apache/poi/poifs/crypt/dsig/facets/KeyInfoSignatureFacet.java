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

import java.security.Key;
import java.security.KeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Signature Facet implementation that adds ds:KeyInfo to the XML signature.
 *
 * @author Frank Cornelis
 */
public class KeyInfoSignatureFacet implements SignatureFacet {

    private static final POILogger LOG = POILogFactory.getLogger(KeyInfoSignatureFacet.class);

    @Override
    public void postSign(SignatureInfo signatureInfo, Document document)
    throws MarshalException {
        LOG.log(POILogger.DEBUG, "postSign");

        NodeList nl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "Object");

        /*
         * Make sure we insert right after the ds:SignatureValue element, just
         * before the first ds:Object element.
         */
        Node nextSibling = (nl.getLength() == 0) ? null : nl.item(0);

        /*
         * Construct the ds:KeyInfo element using JSR 105.
         */
        KeyInfoFactory keyInfoFactory = signatureInfo.getKeyInfoFactory();
        List<Object> x509DataObjects = new ArrayList<>();
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        X509Certificate signingCertificate = signatureConfig.getSigningCertificateChain().get(0);

        List<XMLStructure> keyInfoContent = new ArrayList<>();

        if (signatureConfig.isIncludeKeyValue()) {
            KeyValue keyValue;
            try {
                keyValue = keyInfoFactory.newKeyValue(signingCertificate.getPublicKey());
            } catch (KeyException e) {
                throw new RuntimeException("key exception: " + e.getMessage(), e);
            }
            keyInfoContent.add(keyValue);
        }

        if (signatureConfig.isIncludeIssuerSerial()) {
            x509DataObjects.add(keyInfoFactory.newX509IssuerSerial(
                signingCertificate.getIssuerX500Principal().toString(),
                signingCertificate.getSerialNumber()));
        }

        if (signatureConfig.isIncludeEntireCertificateChain()) {
            x509DataObjects.addAll(signatureConfig.getSigningCertificateChain());
        } else {
            x509DataObjects.add(signingCertificate);
        }

        if (!x509DataObjects.isEmpty()) {
            X509Data x509Data = keyInfoFactory.newX509Data(x509DataObjects);
            keyInfoContent.add(x509Data);
        }
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(keyInfoContent);
        DOMKeyInfo domKeyInfo = (DOMKeyInfo)keyInfo;

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

        Element n = document.getDocumentElement();
        DOMSignContext domSignContext = (nextSibling == null)
            ? new DOMSignContext(key, n)
            : new DOMSignContext(key, n, nextSibling);
        signatureConfig.getNamespacePrefixes().forEach(domSignContext::putNamespacePrefix);

        DOMStructure domStructure = new DOMStructure(n);
        domKeyInfo.marshal(domStructure, domSignContext);

        // move keyinfo into the right place
        if (nextSibling != null) {
            NodeList kiNl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "KeyInfo");
            if (kiNl.getLength() != 1) {
                throw new RuntimeException("KeyInfo wasn't set");
            }
            nextSibling.getParentNode().insertBefore(kiNl.item(0), nextSibling);
        }
    }
}