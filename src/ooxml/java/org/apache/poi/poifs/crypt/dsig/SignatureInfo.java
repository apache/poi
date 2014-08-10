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

package org.apache.poi.poifs.crypt.dsig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Cipher;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.HorribleProxies.InitIf;
import org.apache.poi.poifs.crypt.dsig.services.RelationshipTransformService;
import org.apache.poi.poifs.crypt.dsig.services.XmlSignatureService;
import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SAXHelper;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SignatureInfo {
    
    public static final byte[] SHA1_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x04, 0x14 };

    public static final byte[] SHA224_DIGEST_INFO_PREFIX = new byte[] 
        { 0x30, 0x2b, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x04, 0x04, 0x1c };

    public static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };

    public static final byte[] SHA384_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x3f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x04, 0x30 };

    public static final byte[] SHA512_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x4f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86
        , 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x04, 0x40 };

    public static final byte[] RIPEMD128_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x02, 0x04, 0x10 };

    public static final byte[] RIPEMD160_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x01, 0x04, 0x14 };

    public static final byte[] RIPEMD256_DIGEST_INFO_PREFIX = new byte[]
        { 0x30, 0x2b, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x24, 0x03, 0x02, 0x03, 0x04, 0x20 };
    
    
    private static final POILogger LOG = POILogFactory.getLogger(SignatureInfo.class);
    private static boolean isInitialized = false;
    
    private final OPCPackage pkg;
    
    public SignatureInfo(OPCPackage pkg) {
        this.pkg = pkg;
    }
    
    public boolean verifySignature() {
        initXmlProvider();
        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
        List<X509Certificate> signers = new LinkedList<X509Certificate>();
        return getSignersAndValidate(signers, true);
    }

    public void confirmSignature(Key key, X509Certificate x509)
    throws NoSuchAlgorithmException, IOException {
        confirmSignature(key, x509, HashAlgorithm.sha1);
    }
    
    public void confirmSignature(Key key, X509Certificate x509, HashAlgorithm hashAlgo)
    throws NoSuchAlgorithmException, IOException {
        XmlSignatureService signatureService = createSignatureService(hashAlgo, pkg);
        
        // operate
        List<X509Certificate> x509Chain = Collections.singletonList(x509);
        DigestInfo digestInfo = signatureService.preSign(null, x509Chain, null, null, null);

        // setup: key material, signature value

        Cipher cipher = CryptoFunctions.getCipher(key, CipherAlgorithm.rsa
            , ChainingMode.ecb, null, Cipher.ENCRYPT_MODE, "PKCS1Padding");
        
        byte[] signatureValue;
        try {
            ByteArrayOutputStream digestInfoValueBuf = new ByteArrayOutputStream();
            digestInfoValueBuf.write(SHA1_DIGEST_INFO_PREFIX);
            digestInfoValueBuf.write(digestInfo.digestValue);
            byte[] digestInfoValue = digestInfoValueBuf.toByteArray();
            signatureValue = cipher.doFinal(digestInfoValue);
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }

        
        // operate: postSign
        signatureService.postSign(signatureValue, Collections.singletonList(x509));
    }

    public XmlSignatureService createSignatureService(HashAlgorithm hashAlgo, OPCPackage pkg) {
        XmlSignatureService signatureService = new XmlSignatureService(hashAlgo, pkg);
        signatureService.initFacets(new Date());
        return signatureService;
    }
    
    public List<X509Certificate> getSigners() {
        initXmlProvider();
        List<X509Certificate> signers = new LinkedList<X509Certificate>();
        getSignersAndValidate(signers, false);
        return signers;
    }
    
    protected boolean getSignersAndValidate(List<X509Certificate> signers, boolean onlyFirst) {
        boolean allValid = true;
        List<PackagePart> signatureParts = getSignatureParts(onlyFirst);
        if (signatureParts.isEmpty()) {
            LOG.log(POILogger.DEBUG, "no signature resources");
            allValid = false;
        }
        
        for (PackagePart signaturePart : signatureParts) {
            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();

            try {
                Document doc = SAXHelper.readSAXDocumentW3C(signaturePart.getInputStream());
                // dummy call to createSignatureService to tweak document afterwards
                createSignatureService(HashAlgorithm.sha1, pkg).registerIds(doc);
                
                DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
                domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);
                OOXMLURIDereferencer dereferencer = new OOXMLURIDereferencer(pkg);
                domValidateContext.setURIDereferencer(dereferencer);
    
                XMLSignatureFactory xmlSignatureFactory = getSignatureFactory();
                XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
                boolean validity = xmlSignature.validate(domValidateContext);
                allValid &= validity;
                if (!validity) continue;
                // TODO: check what has been signed.
            } catch (Exception e) {
                LOG.log(POILogger.ERROR, "error in marshalling and validating the signature", e);
                continue;
            }

            X509Certificate signer = keySelector.getCertificate();
            signers.add(signer);
        }
        
        return allValid;
    }

    protected List<PackagePart> getSignatureParts(boolean onlyFirst) {
        List<PackagePart> packageParts = new LinkedList<PackagePart>();
        
        PackageRelationshipCollection sigOrigRels = pkg.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN);
        for (PackageRelationship rel : sigOrigRels) {
            PackagePart sigPart = pkg.getPart(rel);
            LOG.log(POILogger.DEBUG, "Digital Signature Origin part", sigPart);

            try {
                PackageRelationshipCollection sigRels = sigPart.getRelationshipsByType(PackageRelationshipTypes.DIGITAL_SIGNATURE);
                for (PackageRelationship sigRel : sigRels) {
                    PackagePart sigRelPart = sigPart.getRelatedPart(sigRel); 
                    LOG.log(POILogger.DEBUG, "XML Signature part", sigRelPart);
                    packageParts.add(sigRelPart);
                    if (onlyFirst) break;
                }
            } catch (InvalidFormatException e) {
                LOG.log(POILogger.WARN, "Reference to signature is invalid.", e);
            }
            
            if (onlyFirst && !packageParts.isEmpty()) break;
        }

        return packageParts;
    }
    
    public static XMLSignatureFactory getSignatureFactory() {
        Provider p = Security.getProvider("XMLDSig");
        assert(p != null);
        return XMLSignatureFactory.getInstance("DOM", p);
    }

    public static KeyInfoFactory getKeyInfoFactory() {
        Provider p = Security.getProvider("XMLDSig");
        assert(p != null);
        return KeyInfoFactory.getInstance("DOM", p);
    }

    public static void insertXChild(XmlObject root, XmlObject child) {
        XmlCursor rootCursor = root.newCursor();
        insertXChild(rootCursor, child);
        rootCursor.dispose();
    }

    public static void insertXChild(XmlCursor rootCursor, XmlObject child) {
        rootCursor.toEndToken();
        XmlCursor childCursor = child.newCursor();
        childCursor.toNextToken();
        childCursor.moveXml(rootCursor);
        childCursor.dispose();
    }

    public static void setPrefix(XmlObject xobj, String ns, String prefix) {
        for (XmlCursor cur = xobj.newCursor(); cur.hasNextToken(); cur.toNextToken()) {
            if (cur.isStart()) {
                Element el = (Element)cur.getDomNode();
                if (ns.equals(el.getNamespaceURI())) el.setPrefix(prefix);
            }
        }
    }
    
    public static synchronized void initXmlProvider() {
        if (isInitialized) return;
        isInitialized = true;
        
        try {
            InitIf init = HorribleProxy.newProxy(InitIf.class);
            init.init();

            RelationshipTransformService.registerDsigProvider();
            
            Provider bcProv = Security.getProvider("BC");
            if (bcProv == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class<?> c = cl.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
                bcProv = (Provider)c.newInstance();
                Security.addProvider(bcProv);
            }
        } catch (Exception e) {
            throw new RuntimeException("Xml & BouncyCastle-Provider initialization failed", e);
        }
    }
}
