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
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.xmlbeans.XmlObject;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.etsi.uri.x01903.v13.UnsignedPropertiesType;
import org.etsi.uri.x01903.v13.UnsignedSignaturePropertiesType;
import org.w3.x2000.x09.xmldsig.SignatureType;

/**
 * Work-around for Office2010 to accept the XAdES-BES/EPES signature.
 * 
 * xades:UnsignedProperties/xades:UnsignedSignatureProperties needs to be
 * present.
 * 
 * @author Frank Cornelis
 * 
 */
public class Office2010SignatureFacet implements SignatureFacet {

    public void preSign(XMLSignatureFactory signatureFactory,
        String signatureId,
        List<X509Certificate> signingCertificateChain,
        List<Reference> references,
        List<XMLObject> objects
    ) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    }

    public void postSign(SignatureType signatureElement, List<X509Certificate> signingCertificateChain) {
        QualifyingPropertiesType qualProps = null;
        
        // check for XAdES-BES
        String qualPropXQuery =
                "declare namespace xades='http://uri.etsi.org/01903/v1.3.2#'; "
              + "declare namespace ds='http://www.w3.org/2000/09/xmldsig#'; "
              + "$this/ds:Object/xades:QualifyingProperties";
        XmlObject xoList[] = signatureElement.selectPath(qualPropXQuery);
        if (xoList.length == 1) {
            qualProps = (QualifyingPropertiesType)xoList[0];
        }
        
        if (qualProps == null) {
            throw new IllegalArgumentException("no XAdES-BES extension present");
        }

        // create basic XML container structure
        UnsignedPropertiesType unsignedProps = qualProps.getUnsignedProperties();
        if (unsignedProps == null) {
            unsignedProps = qualProps.addNewUnsignedProperties();
        }
        UnsignedSignaturePropertiesType unsignedSigProps = unsignedProps.getUnsignedSignatureProperties();
        if (unsignedSigProps == null) {
            unsignedSigProps = unsignedProps.addNewUnsignedSignatureProperties();
        }
    }
    
    public Map<String,String> getNamespacePrefixMapping() {
        return null;
    }
}