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
package org.apache.poi.poifs.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public abstract class Decryptor implements Cloneable {
    public static final String DEFAULT_PASSWORD="VelvetSweatshop";
    public static final String DEFAULT_POIFS_ENTRY="EncryptedPackage";
    
    protected EncryptionInfo encryptionInfo;
    private SecretKey secretKey;
    private byte[] verifier, integrityHmacKey, integrityHmacValue;

    protected Decryptor() {
    }
    
    /**
     * Return a stream with decrypted data.
     * <p>
     * Use {@link #getLength()} to get the size of that data that can be safely read from the stream.
     * Just reading to the end of the input stream is not sufficient because there are
     * normally padding bytes that must be discarded
     * </p>
     *
     * @param dir the node to read from
     * @return decrypted stream
     */
    public abstract InputStream getDataStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException;

    /**
     * Wraps a stream for decryption<p>
     * 
     * As we are handling streams and don't know the total length beforehand,
     * it's the callers duty to care for the length of the entries.
     *
     * @param stream the stream to be wrapped
     * @param initialPos initial/current byte position within the stream
     * @return decrypted stream
     */
    public InputStream getDataStream(InputStream stream, int size, int initialPos)
        throws IOException, GeneralSecurityException {
        throw new EncryptedDocumentException("this decryptor doesn't support reading from a stream");
    }

    /**
     * Sets the chunk size of the data stream.
     * Needs to be set before the data stream is requested.
     * When not set, the implementation uses method specific default values
     *
     * @param chunkSize the chunk size, i.e. the block size with the same encryption key
     */
    public void setChunkSize(int chunkSize) {
        throw new EncryptedDocumentException("this decryptor doesn't support changing the chunk size");
    }

    /**
     * Initializes a cipher object for a given block index for encryption
     *
     * @param cipher may be null, otherwise the given instance is reset to the new block index
     * @param block the block index, e.g. the persist/slide id (hslf)
     * @return a new cipher object, if cipher was null, otherwise the reinitialized cipher
     * @throws GeneralSecurityException if the cipher can't be initialized
     */
    public Cipher initCipherForBlock(Cipher cipher, int block)
    throws GeneralSecurityException {
        throw new EncryptedDocumentException("this decryptor doesn't support initCipherForBlock");
    }
    
    public abstract boolean verifyPassword(String password)
        throws GeneralSecurityException;

    /**
     * Returns the length of the encrypted data that can be safely read with
     * {@link #getDataStream(org.apache.poi.poifs.filesystem.DirectoryNode)}.
     * Just reading to the end of the input stream is not sufficient because there are
     * normally padding bytes that must be discarded
     *
     * <p>
     *    The length variable is initialized in {@link #getDataStream(org.apache.poi.poifs.filesystem.DirectoryNode)},
     *    an attempt to call getLength() prior to getDataStream() will result in IllegalStateException.
     * </p>
     *
     * @return length of the encrypted data
     * @throws IllegalStateException if {@link #getDataStream(org.apache.poi.poifs.filesystem.DirectoryNode)}
     * was not called
     */
    public abstract long getLength();

    public static Decryptor getInstance(EncryptionInfo info) {
        Decryptor d = info.getDecryptor();
        if (d == null) {
            throw new EncryptedDocumentException("Unsupported version");
        }
        return d;
    }

    public InputStream getDataStream(POIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }

    // for tests
    public byte[] getVerifier() {
        return verifier;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
    
    public byte[] getIntegrityHmacKey() {
        return integrityHmacKey;
    }

    @SuppressWarnings("unused")
    public byte[] getIntegrityHmacValue() {
        return integrityHmacValue;
    }

    protected void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    protected void setVerifier(byte[] verifier) {
        this.verifier = (verifier == null) ? null : verifier.clone();
    }

    protected void setIntegrityHmacKey(byte[] integrityHmacKey) {
        this.integrityHmacKey = (integrityHmacKey == null) ? null : integrityHmacKey.clone();
    }

    protected void setIntegrityHmacValue(byte[] integrityHmacValue) {
        this.integrityHmacValue = (integrityHmacValue == null) ? null : integrityHmacValue.clone();
    }

    @SuppressWarnings("unused")
    protected int getBlockSizeInBytes() {
        return encryptionInfo.getHeader().getBlockSize();
    }
    
    protected int getKeySizeInBytes() {
        return encryptionInfo.getHeader().getKeySize()/8;
    }
    
    public EncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    public void setEncryptionInfo(EncryptionInfo encryptionInfo) {
        this.encryptionInfo = encryptionInfo;
    }

    @Override
    public Decryptor clone() throws CloneNotSupportedException {
        Decryptor other = (Decryptor)super.clone();
        other.integrityHmacKey = integrityHmacKey.clone();
        other.integrityHmacValue = integrityHmacValue.clone();
        other.verifier = verifier.clone();
        other.secretKey = new SecretKeySpec(secretKey.getEncoded(), secretKey.getAlgorithm());
        // encryptionInfo is set from outside
        return other;
    }
}