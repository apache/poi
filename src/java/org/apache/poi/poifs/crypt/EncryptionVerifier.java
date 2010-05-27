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

/**
 *  @author Maxim Valyanskiy
 */
public class EncryptionVerifier {
    private final byte[] salt = new byte[16];
    private final byte[] verifier = new byte[16];
    private final byte[] verifierHash;
    private final int verifierHashSize;

    public EncryptionVerifier(DocumentInputStream is, int encryptedLength) {
        int saltSize = is.readInt();

        if (saltSize!=16) {
            throw new RuntimeException("Salt size != 16 !?");
        }

        is.readFully(salt);
        is.readFully(verifier);

        verifierHashSize = is.readInt();

        verifierHash = new byte[encryptedLength];
        is.readFully(verifierHash);
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getVerifier() {
        return verifier;
    }

    public byte[] getVerifierHash() {
        return verifierHash;
    }
}
