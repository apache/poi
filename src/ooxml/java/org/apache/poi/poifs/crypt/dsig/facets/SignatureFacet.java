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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;

/**
 * JSR105 Signature Facet interface.
 * 
 * @author Frank Cornelis
 * 
 */
public interface SignatureFacet {

    /**
     * This method is being invoked by the XML signature service engine during
     * pre-sign phase. Via this method a signature facet implementation can add
     * signature facets to an XML signature.
     * 
     * @param signatureFactory
     * @param document
     * @param signatureId
     * @param signingCertificateChain
     *            the optional signing certificate chain
     * @param references
     * @param objects
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
    void preSign(
          Document document
        , XMLSignatureFactory signatureFactory
        , String signatureId
        , List<X509Certificate> signingCertificateChain
        , List<Reference> references
        , List<XMLObject> objects
    ) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, URISyntaxException, XmlException;

    /**
     * This method is being invoked by the XML signature service engine during
     * the post-sign phase. Via this method a signature facet can extend the XML
     * signatures with for example key information.
     * 
     * @param signatureElement
     * @param signingCertificateChain
     */
    void postSign(
          Document document
        , List<X509Certificate> signingCertificateChain
    ) throws MarshalException, XmlException;
    
    Map<String,String> getNamespacePrefixMapping();
}