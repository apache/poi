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
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.TempFileCreationStrategy;

public abstract class Encryptor implements GenericRecord {

    protected static final String DEFAULT_POIFS_ENTRY = Decryptor.DEFAULT_POIFS_ENTRY;
    private EncryptionInfo encryptionInfo;
    private SecretKey secretKey;

    protected Encryptor() {}

    protected Encryptor(Encryptor other) {
        encryptionInfo = other.encryptionInfo;
        // secretKey is immutable
        secretKey = other.secretKey;
    }

    /**
     * Return an output stream for encrypted data.
     * <p>
     * Note for implementors: Implementations must override {@link #getDataStream(DirectoryNode, TempFileCreationStrategy)}
     *
     * @param dir the node to write to
     * @return encrypted stream
     */
    public OutputStream getDataStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException {
        return getDataStream(dir, TempFileCreationStrategy.getDefaultStrategy());
    }

    /**
     * Return an output stream for encrypted data.
     * <p>
     * Note for implementors: Implementations of {@code Encryptor} must treat this method as abstract. This
     * method has a default implementation for backwards compatibility only. The default implementation ignores
     * the {@code tmpStrategy} parameter and calls {@link #getDataStream(DirectoryNode)}.
     *
     * @param dir the node to write to
     * @param tmpStrategy the strategy to create temporary files (if needed), may not be null
     * @return encrypted stream
     *
     * @since POI 5.5.0
     */
    public OutputStream getDataStream(DirectoryNode dir, TempFileCreationStrategy tmpStrategy)
            throws IOException, GeneralSecurityException {
        return getDataStream(dir);
    }

    // for tests
    public abstract void confirmPassword(String password, byte[] keySpec, byte[] keySalt, byte[] verifier, byte[] verifierSalt, byte[] integritySalt);

    public abstract void confirmPassword(String password);

    public static Encryptor getInstance(EncryptionInfo info) {
        return info.getEncryptor();
    }

    public OutputStream getDataStream(POIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs, TempFileCreationStrategy.getDefaultStrategy());
    }

    public OutputStream getDataStream(POIFSFileSystem fs, TempFileCreationStrategy tmpStrategy) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot(), tmpStrategy);
    }

    public ChunkedCipherOutputStream getDataStream(OutputStream stream, int initialOffset)
    throws IOException, GeneralSecurityException {
        throw new EncryptedDocumentException("this decryptor doesn't support writing directly to a stream");
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public EncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    public void setEncryptionInfo(EncryptionInfo encryptionInfo) {
        this.encryptionInfo = encryptionInfo;
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

    public abstract Encryptor copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "secretKey", secretKey == null ? () -> null : secretKey::getEncoded
        );
    }
}
