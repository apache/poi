package org.apache.poi.poifs.crypt.dsig.facets;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void postSign(Document document, List<X509Certificate> signingCertificateChain) {
        // empty
    }

    @Override
    public void preSign(Document document
        , XMLSignatureFactory signatureFactory
        , List<Reference> references
        , List<XMLObject> objects)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        DigestMethod digestMethod = signatureFactory.newDigestMethod(signatureConfig.getDigestAlgo().xmlSignUri, null);

        List<Transform> transforms = new ArrayList<Transform>();
        Transform envelopedTransform = signatureFactory
                .newTransform(CanonicalizationMethod.ENVELOPED,
                        (TransformParameterSpec) null);
        transforms.add(envelopedTransform);
        Transform exclusiveTransform = signatureFactory
                .newTransform(CanonicalizationMethod.EXCLUSIVE,
                        (TransformParameterSpec) null);
        transforms.add(exclusiveTransform);

        Reference reference = signatureFactory.newReference("", digestMethod,
                transforms, null, null);

        references.add(reference);
    }

    @Override
    public Map<String,String> getNamespacePrefixMapping() {
        return null;
    }
}
