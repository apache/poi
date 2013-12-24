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
package org.apache.poi.poifs.crypt.standard;

import static org.apache.poi.poifs.crypt.CryptoFunctions.getUtf16LeString;

import java.io.IOException;

import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CipherProvider;
import org.apache.poi.poifs.crypt.EncryptionHeader;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianOutput;

public class StandardEncryptionHeader extends EncryptionHeader implements EncryptionRecord {
    // A flag that specifies whether CryptoAPI RC4 or ECMA-376 encryption 
    // [ECMA-376] is used. It MUST be 1 unless fExternal is 1. If fExternal is 1, it MUST be 0.
    private static BitField flagsCryptoAPI = new BitField(0x04);

    // A value that MUST be 0 if document properties are encrypted. The 
    // encryption of document properties is specified in section 2.3.5.4 [MS-OFFCRYPTO].
    @SuppressWarnings("unused")
    private static BitField flagsDocProps = new BitField(0x08);
    
    // A value that MUST be 1 if extensible encryption is used,. If this value is 1, 
    // the value of every other field in this structure MUST be 0.
    @SuppressWarnings("unused")
    private static BitField flagsExternal = new BitField(0x10);
    
    // A value that MUST be 1 if the protected content is an ECMA-376 document 
    // [ECMA-376]. If the fAES bit is 1, the fCryptoAPI bit MUST also be 1.
    private static BitField flagsAES = new BitField(0x20);
    
    protected StandardEncryptionHeader(DocumentInputStream is) throws IOException {
        setFlags(is.readInt());
        setSizeExtra(is.readInt());
        setCipherAlgorithm(CipherAlgorithm.fromEcmaId(is.readInt()));
        setHashAlgorithm(HashAlgorithm.fromEcmaId(is.readInt()));
        setKeySize(is.readInt());
        setBlockSize(getKeySize());
        setCipherProvider(CipherProvider.fromEcmaId(is.readInt()));

        is.readLong(); // skip reserved

        // CSPName may not always be specified
        // In some cases, the salt value of the EncryptionVerifier is the next chunk of data
        is.mark(LittleEndianConsts.INT_SIZE+1);
        int checkForSalt = is.readInt();
        is.reset();
        
        if (checkForSalt == 16) {
            setCspName("");
        } else {
            StringBuilder builder = new StringBuilder();
            while (true) {
                char c = (char) is.readShort();
                if (c == 0) break;
                builder.append(c);
            }
            setCspName(builder.toString());
        }
        
        setChainingMode(ChainingMode.ecb);
        setKeySalt(null);
    }

    protected StandardEncryptionHeader(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setKeySize(keyBits);
        setBlockSize(blockSize);
        setCipherProvider(cipherAlgorithm.provider);
        setFlags(flagsCryptoAPI.setBoolean(0, true)
                | flagsAES.setBoolean(0, cipherAlgorithm.provider == CipherProvider.aes));
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/bb931357(v=vs.85).aspx for a full list
        // setCspName("Microsoft Enhanced RSA and AES Cryptographic Provider");
    }
    
    public void write(LittleEndianByteArrayOutputStream bos) {
        int startIdx = bos.getWriteIndex();
        LittleEndianOutput sizeOutput = bos.createDelayedOutput(LittleEndianConsts.INT_SIZE);
        bos.writeInt(getFlags());
        bos.writeInt(0); // size extra
        bos.writeInt(getCipherAlgorithm().ecmaId);
        bos.writeInt(getHashAlgorithmEx().ecmaId);
        bos.writeInt(getKeySize());
        bos.writeInt(getCipherProvider().ecmaId);
        bos.writeInt(0); // reserved1
        bos.writeInt(0); // reserved2
        if (getCspName() != null) {
            bos.write(getUtf16LeString(getCspName()));
            bos.writeShort(0);
        }
        int headerSize = bos.getWriteIndex()-startIdx-LittleEndianConsts.INT_SIZE;
        sizeOutput.writeInt(headerSize);        
    }
}
