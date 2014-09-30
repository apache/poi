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
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.w3c.dom.Document;

/**
 * Signature Facet implementation to create enveloped signatures.
 * 
 * @author Frank Cornelis
 * 
 */
public class EnvelopedSignatureFacet implements SignatureFacet {

    private SignatureConfig signatureConfig;

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }
    
    @Override
    public void postSign(Document document) {
        // empty
    }

    @Override
    public void preSign(Document document
        , XMLSignatureFactory signatureFactory
        , List<Reference> references
        , List<XMLObject> objects)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        DigestMethod digestMethod = signatureFactory.newDigestMethod
            (signatureConfig.getDigestMethodUri(), null);

        List<Transform> transforms = new ArrayList<Transform>();
        Transform envelopedTransform = signatureFactory.newTransform
            (CanonicalizationMethod.ENVELOPED, (TransformParameterSpec) null);
        transforms.add(envelopedTransform);
        Transform exclusiveTransform = signatureFactory.newTransform
            (CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
        transforms.add(exclusiveTransform);

        Reference reference = signatureFactory.newReference("", digestMethod,
                transforms, null, null);

        references.add(reference);
    }
}
