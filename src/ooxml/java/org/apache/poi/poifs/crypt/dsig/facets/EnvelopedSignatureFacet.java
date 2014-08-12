package org.apache.poi.poifs.crypt.dsig.facets;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.w3.x2000.x09.xmldsig.SignatureType;

/**
 * Signature Facet implementation to create enveloped signatures.
 * 
 * @author Frank Cornelis
 * 
 */
public class EnvelopedSignatureFacet implements SignatureFacet {

    private final HashAlgorithm hashAlgo;

    /**
     * Default constructor. Digest algorithm will be SHA-1.
     */
    public EnvelopedSignatureFacet() {
        this(HashAlgorithm.sha1);
    }

    /**
     * Main constructor.
     * 
     * @param hashAlgo
     *            the digest algorithm to be used within the ds:Reference
     *            element. Possible values: "SHA-1", "SHA-256, or "SHA-512".
     */
    public EnvelopedSignatureFacet(HashAlgorithm hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    @Override
    public void postSign(SignatureType signatureElement
        , List<X509Certificate> signingCertificateChain) {
        // empty
    }

    @Override
    public void preSign(XMLSignatureFactory signatureFactory,
            String signatureId,
            List<X509Certificate> signingCertificateChain,
            List<Reference> references, List<XMLObject> objects)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        DigestMethod digestMethod = signatureFactory.newDigestMethod(
                this.hashAlgo.xmlSignUri, null);

        List<Transform> transforms = new LinkedList<Transform>();
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
