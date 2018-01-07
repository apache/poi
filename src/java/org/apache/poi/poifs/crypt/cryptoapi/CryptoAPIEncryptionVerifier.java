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

import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.standard.StandardEncryptionVerifier;
import org.apache.poi.util.LittleEndianInput;

public class CryptoAPIEncryptionVerifier extends StandardEncryptionVerifier implements Cloneable {

    protected CryptoAPIEncryptionVerifier(LittleEndianInput is,
            CryptoAPIEncryptionHeader header) {
        super(is, header);
    }

    protected CryptoAPIEncryptionVerifier(CipherAlgorithm cipherAlgorithm,
            HashAlgorithm hashAlgorithm, int keyBits, int blockSize,
            ChainingMode chainingMode) {
        super(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }

    @Override
    protected void setSalt(byte salt[]) {
        super.setSalt(salt);
    }

    @Override
    protected void setEncryptedVerifier(byte encryptedVerifier[]) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    @Override
    protected void setEncryptedVerifierHash(byte encryptedVerifierHash[]) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }

    @Override
    public CryptoAPIEncryptionVerifier clone() throws CloneNotSupportedException {
        return (CryptoAPIEncryptionVerifier)super.clone();
    }
}
