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

package org.apache.poi.poifs.crypt.xor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;

public class XOREncryptor extends Encryptor implements Cloneable {

    protected XOREncryptor() {
    }

    @Override
    public void confirmPassword(String password) {
    }

    @Override
    public void confirmPassword(String password, byte keySpec[],
            byte keySalt[], byte verifier[], byte verifierSalt[],
            byte integritySalt[]) {
    }

    @Override
    public OutputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        OutputStream countStream = new XORCipherOutputStream(dir);
        return countStream;
    }

    protected int getKeySizeInBytes() {
        return -1;
    }

    protected void createEncryptionInfoEntry(DirectoryNode dir) throws IOException {
    }

    @Override
    public XOREncryptor clone() throws CloneNotSupportedException {
        return (XOREncryptor)super.clone();
    }

    protected class XORCipherOutputStream extends ChunkedCipherOutputStream {

        @Override
        protected Cipher initCipherForBlock(Cipher cipher, int block, boolean lastChunk)
        throws GeneralSecurityException {
            return XORDecryptor.initCipherForBlock(cipher, block, getEncryptionInfo(), getSecretKey(), Cipher.ENCRYPT_MODE);
        }

        @Override
        protected void calculateChecksum(File file, int i) {
        }

        @Override
        protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
        throws IOException, GeneralSecurityException {
            XOREncryptor.this.createEncryptionInfoEntry(dir);
        }

        public XORCipherOutputStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException {
            super(dir, 512);
        }
    }
}
