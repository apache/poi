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

package org.apache.poi.poifs.crypt.dsig.facets;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.util.Internal;

@Internal
final class SignatureFacetHelper {
    private SignatureFacetHelper() {}

    static Transform newTransform(SignatureInfo signatureInfo, String canonicalizationMethod) throws XMLSignatureException {
        return newTransform(signatureInfo, canonicalizationMethod, null);
    }

    static Transform newTransform(SignatureInfo signatureInfo, String canonicalizationMethod, TransformParameterSpec paramSpec)
            throws XMLSignatureException {
        try {
            return signatureInfo.getSignatureFactory().newTransform(canonicalizationMethod, paramSpec);
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException("unknown canonicalization method: "+canonicalizationMethod, e);
        }
    }

    static Reference newReference(
            SignatureInfo signatureInfo
            , String uri
            , List<Transform> transforms
            , String type
            , String id
            , byte[] digestValue)
            throws XMLSignatureException {
        // the references appear in the package signature or the package object
        // so we can use the default digest algorithm
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        String digestMethodUri = signatureConfig.getDigestMethodUri();
        XMLSignatureFactory sigFac = signatureInfo.getSignatureFactory();
        DigestMethod digestMethod;
        try {
            digestMethod = sigFac.newDigestMethod(digestMethodUri, null);
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException("unknown digest method uri: "+digestMethodUri, e);
        }

        return (digestValue == null)
                ? sigFac.newReference(uri, digestMethod, transforms, type, id)
                : sigFac.newReference(uri, digestMethod, transforms, type, id, digestValue);
    }
}
