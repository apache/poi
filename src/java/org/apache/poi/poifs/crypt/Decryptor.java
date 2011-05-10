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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.util.LittleEndian;

public abstract class Decryptor {
    public static final String DEFAULT_PASSWORD="VelvetSweatshop";

    public abstract InputStream getDataStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException;

    public abstract boolean verifyPassword(String password)
        throws GeneralSecurityException;

    public static Decryptor getInstance(EncryptionInfo info) {
        int major = info.getVersionMajor();
        int minor = info.getVersionMinor();

        if (major == 4 && minor == 4)
            return new AgileDecryptor(info);
        else if (minor == 2 && (major == 3 || major == 4))
            return new EcmaDecryptor(info);
        else
            throw new EncryptedDocumentException("Unsupported version");
    }

    public InputStream getDataStream(NPOIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }

    public InputStream getDataStream(POIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }

    protected static int getBlockSize(int algorithm) {
        switch (algorithm) {
        case EncryptionHeader.ALGORITHM_AES_128: return 16;
        case EncryptionHeader.ALGORITHM_AES_192: return 24;
        case EncryptionHeader.ALGORITHM_AES_256: return 32;
        }
        throw new EncryptedDocumentException("Unknown block size");
    }

    protected byte[] hashPassword(EncryptionInfo info,
                                  String password) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] bytes;
        try {
            bytes = password.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            throw new EncryptedDocumentException("UTF16 not supported");
        }

        sha1.update(info.getVerifier().getSalt());
        byte[] hash = sha1.digest(bytes);
        byte[] iterator = new byte[4];

        for (int i = 0; i < info.getVerifier().getSpinCount(); i++) {
            sha1.reset();
            LittleEndian.putInt(iterator, i);
            sha1.update(iterator);
            hash = sha1.digest(hash);
        }

        return hash;
    }
}