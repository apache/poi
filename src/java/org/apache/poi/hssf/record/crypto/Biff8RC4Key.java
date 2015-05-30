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

package org.apache.poi.hssf.record.crypto;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public class Biff8RC4Key extends Biff8EncryptionKey {
    // these two constants coincidentally have the same value
    public static final int KEY_DIGEST_LENGTH = 5;
    private static final int PASSWORD_HASH_NUMBER_OF_BYTES_USED = 5;

    private static POILogger log = POILogFactory.getLogger(Biff8RC4Key.class);
    
    Biff8RC4Key(byte[] keyDigest) {
        if (keyDigest.length != KEY_DIGEST_LENGTH) {
            throw new IllegalArgumentException("Expected 5 byte key digest, but got " + HexDump.toHex(keyDigest));
        }

        CipherAlgorithm ca = CipherAlgorithm.rc4;
        _secretKey = new SecretKeySpec(keyDigest, ca.jceId);
    }

    /**
     * Create using the default password and a specified docId
     * @param salt 16 bytes
     */
    public static Biff8RC4Key create(String password, byte[] salt) {
        return new Biff8RC4Key(createKeyDigest(password, salt));
    }
    
    /**
     * @return <code>true</code> if the keyDigest is compatible with the specified saltData and saltHash
     */
    public boolean validate(byte[] verifier, byte[] verifierHash) {
        check16Bytes(verifier, "verifier");
        check16Bytes(verifierHash, "verifierHash");

        // validation uses the RC4 for block zero
        Cipher rc4 = getCipher();
        initCipherForBlock(rc4, 0);
        
        byte[] verifierPrime = verifier.clone();
        byte[] verifierHashPrime = verifierHash.clone();

        try {
            rc4.update(verifierPrime, 0, verifierPrime.length, verifierPrime);
            rc4.update(verifierHashPrime, 0, verifierHashPrime.length, verifierHashPrime);
        } catch (ShortBufferException e) {
            throw new EncryptedDocumentException("buffer too short", e);
        }

        MessageDigest md5 = CryptoFunctions.getMessageDigest(HashAlgorithm.md5);
        md5.update(verifierPrime);
        byte[] finalVerifierResult = md5.digest();

        if (log.check(POILogger.DEBUG)) {
            byte[] verifierHashThatWouldWork = xor(verifierHash, xor(verifierHashPrime, finalVerifierResult));
            log.log(POILogger.DEBUG, "valid verifierHash value", HexDump.toHex(verifierHashThatWouldWork));
        }

        return Arrays.equals(verifierHashPrime, finalVerifierResult);
    }
    
    Cipher getCipher() {
        CipherAlgorithm ca = CipherAlgorithm.rc4;
        Cipher rc4 = CryptoFunctions.getCipher(_secretKey, ca, null, null, Cipher.ENCRYPT_MODE);
        return rc4;
    }
    
    static byte[] createKeyDigest(String password, byte[] docIdData) {
        check16Bytes(docIdData, "docId");
        int nChars = Math.min(password.length(), 16);
        byte[] passwordData = new byte[nChars*2];
        for (int i=0; i<nChars; i++) {
            char ch = password.charAt(i);
            passwordData[i*2+0] = (byte) ((ch << 0) & 0xFF);
            passwordData[i*2+1] = (byte) ((ch << 8) & 0xFF);
        }

        MessageDigest md5 = CryptoFunctions.getMessageDigest(HashAlgorithm.md5);
        md5.update(passwordData);
        byte[] passwordHash = md5.digest();
        md5.reset();

        for (int i=0; i<16; i++) {
            md5.update(passwordHash, 0, PASSWORD_HASH_NUMBER_OF_BYTES_USED);
            md5.update(docIdData, 0, docIdData.length);
        }
        
        byte[] result = CryptoFunctions.getBlock0(md5.digest(), KEY_DIGEST_LENGTH);
        return result;
    }

    void initCipherForBlock(Cipher rc4, int keyBlockNo) {
        byte buf[] = new byte[LittleEndianConsts.INT_SIZE]; 
        LittleEndian.putInt(buf, 0, keyBlockNo);
        
        MessageDigest md5 = CryptoFunctions.getMessageDigest(HashAlgorithm.md5);
        md5.update(_secretKey.getEncoded());
        md5.update(buf);

        SecretKeySpec skeySpec = new SecretKeySpec(md5.digest(), _secretKey.getAlgorithm());
        try {
            rc4.init(Cipher.ENCRYPT_MODE, skeySpec);
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException("Can't rekey for next block", e);
        }
    }
    
    private static byte[] xor(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }
    private static void check16Bytes(byte[] data, String argName) {
        if (data.length != 16) {
            throw new IllegalArgumentException("Expected 16 byte " + argName + ", but got " + HexDump.toHex(data));
        }
    }
    

}
