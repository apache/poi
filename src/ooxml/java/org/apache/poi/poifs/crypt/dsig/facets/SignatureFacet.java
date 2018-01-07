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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.jcp.xml.dsig.internal.dom.DOMDigestMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig.SignatureConfigurable;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SuppressForbidden;
import org.w3c.dom.Document;

/**
 * JSR105 Signature Facet base class.
 */
public abstract class SignatureFacet implements SignatureConfigurable {

    private static final POILogger LOG = POILogFactory.getLogger(SignatureFacet.class);
    
    public static final String XML_NS = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    public static final String XML_DIGSIG_NS = XMLSignature.XMLNS;
    public static final String OO_DIGSIG_NS = PackageNamespaces.DIGITAL_SIGNATURE;
    public static final String MS_DIGSIG_NS = "http://schemas.microsoft.com/office/2006/digsig";
    public static final String XADES_132_NS = "http://uri.etsi.org/01903/v1.3.2#";
    public static final String XADES_141_NS = "http://uri.etsi.org/01903/v1.4.1#";

    protected SignatureConfig signatureConfig;

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    /**
     * This method is being invoked by the XML signature service engine during
     * pre-sign phase. Via this method a signature facet implementation can add
     * signature facets to an XML signature.
     * 
     * @param document the signature document to be used for imports
     * @param references list of reference definitions
     * @param objects objects to be signed/included in the signature document
     * @throws XMLSignatureException
     */
    public void preSign(
          Document document
        , List<Reference> references
        , List<XMLObject> objects
    ) throws XMLSignatureException {
        // empty
    }

    /**
     * This method is being invoked by the XML signature service engine during
     * the post-sign phase. Via this method a signature facet can extend the XML
     * signatures with for example key information.
     *
     * @param document the signature document to be modified
     * @throws MarshalException
     */
    public void postSign(Document document) throws MarshalException {
        // empty
    }

    protected XMLSignatureFactory getSignatureFactory() {
        return signatureConfig.getSignatureFactory();
    }
    
    protected Transform newTransform(String canonicalizationMethod) throws XMLSignatureException {
        return newTransform(canonicalizationMethod, null);
    }
    
    protected Transform newTransform(String canonicalizationMethod, TransformParameterSpec paramSpec)
    throws XMLSignatureException {
        try {
            return getSignatureFactory().newTransform(canonicalizationMethod, paramSpec);
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException("unknown canonicalization method: "+canonicalizationMethod, e);
        }
    }
    
    protected Reference newReference(String uri, List<Transform> transforms, String type, String id, byte digestValue[])
    throws XMLSignatureException {
        return newReference(uri, transforms, type, id, digestValue, signatureConfig);
    }

    public static Reference newReference(
          String uri
        , List<Transform> transforms
        , String type
        , String id
        , byte digestValue[]
        , SignatureConfig signatureConfig)
    throws XMLSignatureException {
        // the references appear in the package signature or the package object
        // so we can use the default digest algorithm
        String digestMethodUri = signatureConfig.getDigestMethodUri();
        XMLSignatureFactory sigFac = signatureConfig.getSignatureFactory();
        DigestMethod digestMethod;
        try {
            digestMethod = sigFac.newDigestMethod(digestMethodUri, null);
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException("unknown digest method uri: "+digestMethodUri, e);
        }

        Reference reference;
        if (digestValue == null) {
            reference = sigFac.newReference(uri, digestMethod, transforms, type, id);
        } else {
            reference = sigFac.newReference(uri, digestMethod, transforms, type, id, digestValue);
        }
        
        brokenJvmWorkaround(reference);

        return reference;
    }
    
    // helper method ... will be removed soon
    public static void brokenJvmWorkaround(final Reference reference) {
        final DigestMethod digestMethod = reference.getDigestMethod();
        final String digestMethodUri = digestMethod.getAlgorithm();
        
        final Provider bcProv = Security.getProvider("BC");
        if (bcProv != null && !DigestMethod.SHA1.equals(digestMethodUri)) {
            // workaround for https://bugzilla.redhat.com/show_bug.cgi?id=1155012
            // overwrite standard message digest, if a digest <> SHA1 is used
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                @SuppressForbidden("Workaround for a bug, needs access to private JDK members (may fail in Java 9): https://bugzilla.redhat.com/show_bug.cgi?id=1155012")
                public Void run() {
                    try {
                        Method m = DOMDigestMethod.class.getDeclaredMethod("getMessageDigestAlgorithm");
                        m.setAccessible(true);
                        String mdAlgo = (String)m.invoke(digestMethod);
                        MessageDigest md = MessageDigest.getInstance(mdAlgo, bcProv);
                        Field f = DOMReference.class.getDeclaredField("md");
                        f.setAccessible(true);
                        f.set(reference, md);
                    } catch (Exception e) {
                        LOG.log(POILogger.WARN, "Can't overwrite message digest (workaround for https://bugzilla.redhat.com/show_bug.cgi?id=1155012)", e);
                    }
                    return null; // Void
                }
            });
        }
    }
}