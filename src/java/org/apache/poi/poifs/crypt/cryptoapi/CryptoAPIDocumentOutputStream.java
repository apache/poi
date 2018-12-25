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
package org.apache.poi.poifs.crypt.cryptoapi;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.util.Internal;

/**
 * An OutputStream for the document entries within the encrypted stream
 */
@Internal
/* package */ class CryptoAPIDocumentOutputStream extends ByteArrayOutputStream {
    private final Cipher cipher;
    private final CryptoAPIEncryptor encryptor;
    private final byte[] oneByte = {0};

    public CryptoAPIDocumentOutputStream(CryptoAPIEncryptor encryptor) throws GeneralSecurityException {
        this.encryptor = encryptor;
        cipher = encryptor.initCipherForBlock(null, 0);
    }
    
    public byte[] getBuf() {
        return buf;
    }
    
    public void setSize(int count) {
        this.count = count;
    }
    
    public void setBlock(int block) throws GeneralSecurityException {
        encryptor.initCipherForBlock(cipher, block);
    }
    
    @Override
    public void write(int b) {
        try {
            oneByte[0] = (byte)b;
            cipher.update(oneByte, 0, 1, oneByte, 0);
            super.write(oneByte);
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        try {
            cipher.update(b, off, len, b, off);
            super.write(b, off, len);
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
    }

}