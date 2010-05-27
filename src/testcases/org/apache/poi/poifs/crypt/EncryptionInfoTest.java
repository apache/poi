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

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;

/**
 *  @author Maxim Valyanskiy
 */
public class EncryptionInfoTest extends TestCase {
    public void testEncryptionInfo() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        assertEquals(3, info.getVersionMajor());
        assertEquals(2, info.getVersionMinor());

        assertEquals(EncryptionHeader.ALGORITHM_AES_128, info.getHeader().getAlgorithm());
        assertEquals(EncryptionHeader.HASH_SHA1, info.getHeader().getHashAlgorithm());
        assertEquals(128, info.getHeader().getKeySize());
        assertEquals(EncryptionHeader.PROVIDER_AES, info.getHeader().getProviderType());                
        assertEquals("Microsoft Enhanced RSA and AES Cryptographic Provider", info.getHeader().getCspName());

        assertEquals(32, info.getVerifier().getVerifierHash().length);
    }
}
