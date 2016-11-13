/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.poifs.crypt.temp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;

/**
 * EncryptedTempData can be used to buffer binary data in a secure way, by using encrypted temp files.
 */
@Beta
public class EncryptedTempData {
    private static POILogger LOG = POILogFactory.getLogger(EncryptedTempData.class);
 
    private final static CipherAlgorithm cipherAlgorithm = CipherAlgorithm.aes128;
    private final SecretKeySpec skeySpec;
    private final byte[] ivBytes;
    private final File tempFile;
    
    public EncryptedTempData() throws IOException {
        SecureRandom sr = new SecureRandom();
        ivBytes = new byte[16];
        byte[] keyBytes = new byte[16];
        sr.nextBytes(ivBytes);
        sr.nextBytes(keyBytes);
        skeySpec = new SecretKeySpec(keyBytes, cipherAlgorithm.jceId);
        tempFile = TempFile.createTempFile("poi-temp-data", ".tmp");
    }

    public OutputStream getOutputStream() throws IOException {
        Cipher ciEnc = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.ENCRYPT_MODE, null);
        return new CipherOutputStream(new FileOutputStream(tempFile), ciEnc);
    }

    public InputStream getInputStream() throws IOException {
        Cipher ciDec = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.DECRYPT_MODE, null);
        return new CipherInputStream(new FileInputStream(tempFile), ciDec);
    }
    
    public void dispose() {
        if (!tempFile.delete()) {
            LOG.log(POILogger.WARN, tempFile.getAbsolutePath()+" can't be removed (or was already removed.");
        }
    }
}
