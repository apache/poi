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
package org.apache.poi.poifs.crypt.agile;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.LittleEndianInput;
import org.apache.xmlbeans.XmlException;

import com.microsoft.schemas.office.x2006.encryption.EncryptionDocument;

public class AgileEncryptionInfoBuilder implements EncryptionInfoBuilder {
    
    @Override
    public void initialize(EncryptionInfo info, LittleEndianInput dis) throws IOException {
        EncryptionDocument ed = parseDescriptor((InputStream)dis);
        info.setHeader(new AgileEncryptionHeader(ed));
        info.setVerifier(new AgileEncryptionVerifier(ed));
        if (info.getVersionMajor() == EncryptionMode.agile.versionMajor
            && info.getVersionMinor() == EncryptionMode.agile.versionMinor) {
            AgileDecryptor dec = new AgileDecryptor();
            dec.setEncryptionInfo(info);
            info.setDecryptor(dec);
            AgileEncryptor enc = new AgileEncryptor();
            enc.setEncryptionInfo(info);
            info.setEncryptor(enc);
        }
    }

    @Override
    public void initialize(EncryptionInfo info, CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        if (cipherAlgorithm == null) {
            cipherAlgorithm = CipherAlgorithm.aes128;
        }
        if (cipherAlgorithm == CipherAlgorithm.rc4) {
            throw new EncryptedDocumentException("RC4 must not be used with agile encryption.");
        }
        if (hashAlgorithm == null) {
            hashAlgorithm = HashAlgorithm.sha1;
        }
        if (chainingMode == null) {
            chainingMode = ChainingMode.cbc;
        }
        if (!(chainingMode == ChainingMode.cbc || chainingMode == ChainingMode.cfb)) {
            throw new EncryptedDocumentException("Agile encryption only supports CBC/CFB chaining.");
        }
        if (keyBits == -1) {
            keyBits = cipherAlgorithm.defaultKeySize;
        }
        if (blockSize == -1) {
            blockSize = cipherAlgorithm.blockSize;
        }
        boolean found = false;
        for (int ks : cipherAlgorithm.allowedKeySize) {
            found |= (ks == keyBits);
        }
        if (!found) {
            throw new EncryptedDocumentException("KeySize "+keyBits+" not allowed for Cipher "+ cipherAlgorithm);
        }
        info.setHeader(new AgileEncryptionHeader(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        info.setVerifier(new AgileEncryptionVerifier(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        AgileDecryptor dec = new AgileDecryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        AgileEncryptor enc = new AgileEncryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }
    
    protected static EncryptionDocument parseDescriptor(String descriptor) {
        try {
            return EncryptionDocument.Factory.parse(descriptor, DEFAULT_XML_OPTIONS);
        } catch (XmlException e) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
        }
    }

    protected static EncryptionDocument parseDescriptor(InputStream descriptor) {
        try {
            return EncryptionDocument.Factory.parse(descriptor, DEFAULT_XML_OPTIONS);
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
        }
    }
}
