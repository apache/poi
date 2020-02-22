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
package org.apache.poi.poifs.crypt.agile;

import java.util.Iterator;

import com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptor;
import com.microsoft.schemas.office.x2006.encryption.EncryptionDocument;
import com.microsoft.schemas.office.x2006.encryption.STCipherChaining;
import com.microsoft.schemas.office.x2006.keyEncryptor.password.CTPasswordKeyEncryptor;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.apache.poi.poifs.crypt.HashAlgorithm;

/**
 * Used when checking if a key is valid for a document
 */
public class AgileEncryptionVerifier extends EncryptionVerifier {

    private int keyBits = -1;
    private int blockSize = -1;

    @SuppressWarnings("unused")
    public AgileEncryptionVerifier(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }

    protected AgileEncryptionVerifier(EncryptionDocument ed) {
        Iterator<CTKeyEncryptor> encList = ed.getEncryption().getKeyEncryptors().getKeyEncryptorList().iterator();
        CTPasswordKeyEncryptor keyData;
        try {
            keyData = encList.next().getEncryptedPasswordKey();
            if (keyData == null) {
                throw new NullPointerException("encryptedKey not set");
            }
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse keyData", e);
        }

        int kb = (int)keyData.getKeyBits();
        CipherAlgorithm ca = CipherAlgorithm.fromXmlId(keyData.getCipherAlgorithm().toString(), kb);
        setCipherAlgorithm(ca);

        setKeySize(kb);

        int blockSize = keyData.getBlockSize();
        setBlockSize(blockSize);

        int hashSize = keyData.getHashSize();

        HashAlgorithm ha = HashAlgorithm.fromEcmaId(keyData.getHashAlgorithm().toString());
        setHashAlgorithm(ha);

        if (getHashAlgorithm().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " +
                    keyData.getHashAlgorithm() + " @ " + hashSize + " bytes");
        }

        setSpinCount(keyData.getSpinCount());
        setEncryptedVerifier(keyData.getEncryptedVerifierHashInput());
        setSalt(keyData.getSaltValue());
        setEncryptedKey(keyData.getEncryptedKeyValue());
        setEncryptedVerifierHash(keyData.getEncryptedVerifierHashValue());

        int saltSize = keyData.getSaltSize();
        if (saltSize != getSalt().length) {
            throw new EncryptedDocumentException("Invalid salt size");
        }

        switch (keyData.getCipherChaining().intValue()) {
            case STCipherChaining.INT_CHAINING_MODE_CBC:
                setChainingMode(ChainingMode.cbc);
                break;
            case STCipherChaining.INT_CHAINING_MODE_CFB:
                setChainingMode(ChainingMode.cfb);
                break;
            default:
                throw new EncryptedDocumentException("Unsupported chaining mode - "+ keyData.getCipherChaining());
        }
    }

    public AgileEncryptionVerifier(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setChainingMode(chainingMode);
        setKeySize(keyBits);
        setBlockSize(blockSize);
        setSpinCount(100000); // TODO: use parameter
    }

    public AgileEncryptionVerifier(AgileEncryptionVerifier other) {
        super(other);
        keyBits = other.keyBits;
        blockSize = other.blockSize;
    }

    @Override
    protected void setSalt(byte[] salt) {
        if (salt == null || salt.length != getCipherAlgorithm().blockSize) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setSalt(salt);
    }

    // make method visible for this package
    @Override
    protected void setEncryptedVerifier(byte[] encryptedVerifier) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    // make method visible for this package
    @Override
    protected void setEncryptedVerifierHash(byte[] encryptedVerifierHash) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }

    // make method visible for this package
    @Override
    protected void setEncryptedKey(byte[] encryptedKey) {
        super.setEncryptedKey(encryptedKey);
    }

    @Override
    public AgileEncryptionVerifier copy() {
        return new AgileEncryptionVerifier(this);
    }


    /**
     * The keysize (in bits) of the verifier data. This usually equals the keysize of the header,
     * but only on a few exceptions, like files generated by Office for Mac, can be
     * different.
     *
     * @return the keysize (in bits) of the verifier.
     */
    public int getKeySize() {
        return keyBits;
    }


    /**
     * The blockSize (in bytes) of the verifier data.
     * This usually equals the blocksize of the header.
     *
     * @return the blockSize (in bytes) of the verifier,
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the keysize (in bits) of the verifier
     *
     * @param keyBits the keysize (in bits)
     */
    protected void setKeySize(int keyBits) {
        this.keyBits = keyBits;
        for (int allowedBits : getCipherAlgorithm().allowedKeySize) {
            if (allowedBits == keyBits) {
                return;
            }
        }
        throw new EncryptedDocumentException("KeySize "+keyBits+" not allowed for cipher "+getCipherAlgorithm());
    }


    /**
     * Sets the blockSize (in bytes) of the verifier
     *
     * @param blockSize the blockSize (in bytes)
     */
    protected void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    protected final void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        super.setCipherAlgorithm(cipherAlgorithm);
        if (cipherAlgorithm.allowedKeySize.length == 1) {
            setKeySize(cipherAlgorithm.defaultKeySize);
        }
    }
}
