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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public abstract class Encryptor implements Cloneable {
    protected static final String DEFAULT_POIFS_ENTRY = Decryptor.DEFAULT_POIFS_ENTRY;
    private EncryptionInfo encryptionInfo;
    private SecretKey secretKey;
    
    /**
     * Return a output stream for encrypted data.
     *
     * @param dir the node to write to
     * @return encrypted stream
     */
    public abstract OutputStream getDataStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException;

    // for tests
    public abstract void confirmPassword(String password, byte[] keySpec, byte[] keySalt, byte[] verifier, byte[] verifierSalt, byte[] integritySalt);
    
    public abstract void confirmPassword(String password);
	
	public static Encryptor getInstance(EncryptionInfo info) {
	    return info.getEncryptor();
    }

    public OutputStream getDataStream(POIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
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
    
    @Override
    public Encryptor clone() throws CloneNotSupportedException {
        Encryptor other = (Encryptor)super.clone();
        other.secretKey = new SecretKeySpec(secretKey.getEncoded(), secretKey.getAlgorithm());
        // encryptionInfo is set from outside
        return other;
    }
}
