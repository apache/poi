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

import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.EncryptedDocumentException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

/**
 * @author Gary King
 */
public class AgileDecryptor extends Decryptor {

    private final EncryptionInfo _info;
    private SecretKey _secretKey;

    private static final byte[] kVerifierInputBlock;
    private static final byte[] kHashedVerifierBlock;
    private static final byte[] kCryptoKeyBlock;

    static {
        kVerifierInputBlock =
            new byte[] { (byte)0xfe, (byte)0xa7, (byte)0xd2, (byte)0x76,
                         (byte)0x3b, (byte)0x4b, (byte)0x9e, (byte)0x79 };
        kHashedVerifierBlock =
            new byte[] { (byte)0xd7, (byte)0xaa, (byte)0x0f, (byte)0x6d,
                         (byte)0x30, (byte)0x61, (byte)0x34, (byte)0x4e };
        kCryptoKeyBlock =
            new byte[] { (byte)0x14, (byte)0x6e, (byte)0x0b, (byte)0xe7,
                         (byte)0xab, (byte)0xac, (byte)0xd0, (byte)0xd6 };
    }

    public boolean verifyPassword(String password) throws GeneralSecurityException {
        EncryptionVerifier verifier = _info.getVerifier();
        int algorithm = verifier.getAlgorithm();
        int mode = verifier.getCipherMode();

        byte[] pwHash = hashPassword(_info, password);
        byte[] iv = generateIv(algorithm, verifier.getSalt(), null);

        SecretKey skey;
        skey = new SecretKeySpec(generateKey(pwHash, kVerifierInputBlock), "AES");
        Cipher cipher = getCipher(algorithm, mode, skey, iv);
        byte[] verifierHashInput = cipher.doFinal(verifier.getVerifier());

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] trimmed = new byte[verifier.getSalt().length];
        System.arraycopy(verifierHashInput, 0, trimmed, 0, trimmed.length);
        byte[] hashedVerifier = sha1.digest(trimmed);

        skey = new SecretKeySpec(generateKey(pwHash, kHashedVerifierBlock), "AES");
        iv = generateIv(algorithm, verifier.getSalt(), null);
        cipher = getCipher(algorithm, mode, skey, iv);
        byte[] verifierHash = cipher.doFinal(verifier.getVerifierHash());
        trimmed = new byte[hashedVerifier.length];
        System.arraycopy(verifierHash, 0, trimmed, 0, trimmed.length);

        if (Arrays.equals(trimmed, hashedVerifier)) {
            skey = new SecretKeySpec(generateKey(pwHash, kCryptoKeyBlock), "AES");
            iv = generateIv(algorithm, verifier.getSalt(), null);
            cipher = getCipher(algorithm, mode, skey, iv);
            byte[] inter = cipher.doFinal(verifier.getEncryptedKey());
            byte[] keyspec = new byte[_info.getHeader().getKeySize() / 8];
            System.arraycopy(inter, 0, keyspec, 0, keyspec.length);
            _secretKey = new SecretKeySpec(keyspec, "AES");
            return true;
        } else {
            return false;
        }
    }

    public InputStream getDataStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
        DocumentInputStream dis = dir.createDocumentInputStream("EncryptedPackage");
        long size = dis.readLong();
        return new ChunkedCipherInputStream(dis, size);
    }

    protected AgileDecryptor(EncryptionInfo info) {
        _info = info;
    }

    private class ChunkedCipherInputStream extends InputStream {
        private int _lastIndex = 0;
        private long _pos = 0;
        private final long _size;
        private final DocumentInputStream _stream;
        private byte[] _chunk;
        private Cipher _cipher;

        public ChunkedCipherInputStream(DocumentInputStream stream, long size)
            throws GeneralSecurityException {
            _size = size;
            _stream = stream;
            _cipher = getCipher(_info.getHeader().getAlgorithm(),
                                _info.getHeader().getCipherMode(),
                                _secretKey, _info.getHeader().getKeySalt());
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b) == 1)
                return b[0];
            return -1;
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int total = 0;

            while (len > 0) {
                if (_chunk == null) {
                    try {
                        _chunk = nextChunk();
                    } catch (GeneralSecurityException e) {
                        throw new EncryptedDocumentException(e.getMessage());
                    }
                }
                int count = (int)(4096L - (_pos & 0xfff));
                count = Math.min(available(), Math.min(count, len));
                System.arraycopy(_chunk, (int)(_pos & 0xfff), b, off, count);
                off += count;
                len -= count;
                _pos += count;
                if ((_pos & 0xfff) == 0)
                    _chunk = null;
                total += count;
            }

            return total;
        }

        public long skip(long n) throws IOException {
            long start = _pos;
            long skip = Math.min(available(), n);

            if ((((_pos + skip) ^ start) & ~0xfff) != 0)
                _chunk = null;
            _pos += skip;
            return skip;
        }

        public int available() throws IOException { return (int)(_size - _pos); }
        public void close() throws IOException { _stream.close(); }
        public boolean markSupported() { return false; }

        private byte[] nextChunk() throws GeneralSecurityException, IOException {
            int index = (int)(_pos >> 12);
            byte[] blockKey = new byte[4];
            LittleEndian.putInt(blockKey, index);
            byte[] iv = generateIv(_info.getHeader().getAlgorithm(),
                                   _info.getHeader().getKeySalt(), blockKey);
            _cipher.init(Cipher.DECRYPT_MODE, _secretKey, new IvParameterSpec(iv));
            if (_lastIndex != index)
                _stream.skip((index - _lastIndex) << 12);

            byte[] block = new byte[Math.min(_stream.available(), 4096)];
            _stream.readFully(block);
            _lastIndex = index + 1;
            return _cipher.doFinal(block);
        }
    }

    private Cipher getCipher(int algorithm, int mode, SecretKey key, byte[] vec)
        throws GeneralSecurityException {
        String name = null;
        String chain = null;

        if (algorithm == EncryptionHeader.ALGORITHM_AES_128 ||
            algorithm == EncryptionHeader.ALGORITHM_AES_192 ||
            algorithm == EncryptionHeader.ALGORITHM_AES_256)
            name = "AES";

        if (mode == EncryptionHeader.MODE_CBC)
            chain = "CBC";
        else if (mode == EncryptionHeader.MODE_CFB)
            chain = "CFB";

        Cipher cipher = Cipher.getInstance(name + "/" + chain + "/NoPadding");
        IvParameterSpec iv = new IvParameterSpec(vec);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher;
    }

    private byte[] getBlock(int algorithm, byte[] hash) {
        byte[] result = new byte[getBlockSize(algorithm)];
        Arrays.fill(result, (byte)0x36);
        System.arraycopy(hash, 0, result, 0, Math.min(result.length, hash.length));
        return result;
    }

    private byte[] generateKey(byte[] hash, byte[] blockKey) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(hash);
        return getBlock(_info.getVerifier().getAlgorithm(), sha1.digest(blockKey));
    }

    protected byte[] generateIv(int algorithm, byte[] salt, byte[] blockKey)
        throws NoSuchAlgorithmException {


        if (blockKey == null)
            return getBlock(algorithm, salt);

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(salt);
        return getBlock(algorithm, sha1.digest(blockKey));
    }
}