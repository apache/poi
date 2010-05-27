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

import org.apache.poi.poifs.filesystem.DocumentInputStream;

import java.io.IOException;

/**
 *  @author Maxim Valyanskiy
 */
public class EncryptionHeader {
    public static final int ALGORITHM_RC4 = 0x6801;
    public static final int ALGORITHM_AES_128 = 0x660E;
    public static final int ALGORITHM_AES_192 = 0x660F;
    public static final int ALGORITHM_AES_256 = 0x6610;

    public static final int HASH_SHA1 = 0x8004;

    public static final int PROVIDER_RC4 = 1;
    public static final int PROVIDER_AES = 0x18; 

    private final int flags;
    private final int sizeExtra;
    private final int algorithm;
    private final int hashAlgorithm;
    private final int keySize;
    private final int providerType;
    private final String cspName;

    public EncryptionHeader(DocumentInputStream is) throws IOException {
        flags = is.readInt();
        sizeExtra = is.readInt();
        algorithm = is.readInt();
        hashAlgorithm = is.readInt();
        keySize = is.readInt();
        providerType = is.readInt();

        is.readLong(); // skip reserved

        StringBuilder builder = new StringBuilder();

        while (true) {
            char c = (char) is.readShort();

            if (c == 0) {
                break;
            }

            builder.append(c);
        }

        cspName = builder.toString();
    }

    public int getFlags() {
        return flags;
    }

    public int getSizeExtra() {
        return sizeExtra;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public int getHashAlgorithm() {
        return hashAlgorithm;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getProviderType() {
        return providerType;
    }

    public String getCspName() {
        return cspName;
    }
}
