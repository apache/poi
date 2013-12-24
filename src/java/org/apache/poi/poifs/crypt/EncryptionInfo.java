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

import static org.apache.poi.poifs.crypt.EncryptionMode.agile;
import static org.apache.poi.poifs.crypt.EncryptionMode.standard;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 */
public class EncryptionInfo {
    private final int versionMajor;
    private final int versionMinor;
    private final int encryptionFlags;
    
    private final EncryptionHeader header;
    private final EncryptionVerifier verifier;
    private final Decryptor decryptor;
    private final Encryptor encryptor;

    public EncryptionInfo(POIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    
    public EncryptionInfo(NPOIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    
    public EncryptionInfo(DirectoryNode dir) throws IOException {
        DocumentInputStream dis = dir.createDocumentInputStream("EncryptionInfo");
        versionMajor = dis.readShort();
        versionMinor = dis.readShort();
        encryptionFlags = dis.readInt();
        
        EncryptionMode encryptionMode;
        if (versionMajor == agile.versionMajor
            && versionMinor == agile.versionMinor
            && encryptionFlags == agile.encryptionFlags) {
            encryptionMode = agile;
        } else {
            encryptionMode = standard;
        }
        
        EncryptionInfoBuilder eib;
        try {
            eib = getBuilder(encryptionMode);
        } catch (Exception e) {
            throw (IOException)new IOException().initCause(e);
        }

        eib.initialize(this, dis);
        header = eib.getHeader();
        verifier = eib.getVerifier();
        decryptor = eib.getDecryptor();
        encryptor = eib.getEncryptor();
    }

    public EncryptionInfo(POIFSFileSystem fs, EncryptionMode encryptionMode) throws IOException {
        this(fs.getRoot(), encryptionMode);
     }
     
     public EncryptionInfo(NPOIFSFileSystem fs, EncryptionMode encryptionMode) throws IOException {
        this(fs.getRoot(), encryptionMode);
     }
     
    public EncryptionInfo(
          DirectoryNode dir
        , EncryptionMode encryptionMode
    ) throws EncryptedDocumentException {
        this(dir, encryptionMode, null, null, -1, -1, null);
    }
    
    public EncryptionInfo(
        POIFSFileSystem fs
      , EncryptionMode encryptionMode
      , CipherAlgorithm cipherAlgorithm
      , HashAlgorithm hashAlgorithm
      , int keyBits
      , int blockSize
      , ChainingMode chainingMode
    ) throws EncryptedDocumentException {
        this(fs.getRoot(), encryptionMode, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }
    
    public EncryptionInfo(
        NPOIFSFileSystem fs
      , EncryptionMode encryptionMode
      , CipherAlgorithm cipherAlgorithm
      , HashAlgorithm hashAlgorithm
      , int keyBits
      , int blockSize
      , ChainingMode chainingMode
    ) throws EncryptedDocumentException {
        this(fs.getRoot(), encryptionMode, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }
        
    public EncryptionInfo(
          DirectoryNode dir
        , EncryptionMode encryptionMode
        , CipherAlgorithm cipherAlgorithm
        , HashAlgorithm hashAlgorithm
        , int keyBits
        , int blockSize
        , ChainingMode chainingMode
    ) throws EncryptedDocumentException {
        versionMajor = encryptionMode.versionMajor;
        versionMinor = encryptionMode.versionMinor;
        encryptionFlags = encryptionMode.encryptionFlags;

        EncryptionInfoBuilder eib;
        try {
            eib = getBuilder(encryptionMode);
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
        
        eib.initialize(this, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
        
        header = eib.getHeader();
        verifier = eib.getVerifier();
        decryptor = eib.getDecryptor();
        encryptor = eib.getEncryptor();
    }

    protected static EncryptionInfoBuilder getBuilder(EncryptionMode encryptionMode)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        EncryptionInfoBuilder eib;
        eib = (EncryptionInfoBuilder)cl.loadClass(encryptionMode.builder).newInstance();
        return eib;
    }
    
    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public int getEncryptionFlags() {
        return encryptionFlags;
    }

    public EncryptionHeader getHeader() {
        return header;
    }

    public EncryptionVerifier getVerifier() {
        return verifier;
    }
    
    public Decryptor getDecryptor() {
        return decryptor;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }
}
