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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.EncryptedDocumentException;
import org.junit.jupiter.api.Test;

class TestCipherAlgorithm {
    @Test
    void validInputs() {
        assertEquals(128, CipherAlgorithm.aes128.defaultKeySize);

        for(CipherAlgorithm alg : CipherAlgorithm.values()) {
            assertEquals(alg, CipherAlgorithm.valueOf(alg.toString()));
        }

        assertEquals(CipherAlgorithm.aes128, CipherAlgorithm.fromEcmaId(0x660E));
        assertEquals(CipherAlgorithm.aes192, CipherAlgorithm.fromXmlId("AES", 192));
    }

    @Test
    void invalidInputs() {
        assertThrows(EncryptedDocumentException.class, () -> CipherAlgorithm.fromEcmaId(0));
        assertThrows(EncryptedDocumentException.class, () -> CipherAlgorithm.fromXmlId("AES", 1));
        assertThrows(EncryptedDocumentException.class, () -> CipherAlgorithm.fromXmlId("RC1", 0x40));
    }
}
