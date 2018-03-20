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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

public class TestEncryptionInfo {
    @Test
    public void testEncryptionInfo() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        assertEquals(3, info.getVersionMajor());
        assertEquals(2, info.getVersionMinor());

        assertEquals(CipherAlgorithm.aes128, info.getHeader().getCipherAlgorithm());
        assertEquals(HashAlgorithm.sha1, info.getHeader().getHashAlgorithm());
        assertEquals(128, info.getHeader().getKeySize());
        assertEquals(32, info.getVerifier().getEncryptedVerifierHash().length);
        assertEquals(CipherProvider.aes, info.getHeader().getCipherProvider());                
        assertEquals("Microsoft Enhanced RSA and AES Cryptographic Provider", info.getHeader().getCspName());
        
        fs.close();
    }
    
    @Test
    public void testEncryptionInfoSHA512() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protected_sha512.xlsx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        assertEquals(4, info.getVersionMajor());
        assertEquals(4, info.getVersionMinor());

        assertEquals(CipherAlgorithm.aes256, info.getHeader().getCipherAlgorithm());
        assertEquals(HashAlgorithm.sha512, info.getHeader().getHashAlgorithm());
        assertEquals(256, info.getHeader().getKeySize());
        assertEquals(64, info.getVerifier().getEncryptedVerifierHash().length);
        assertEquals(CipherProvider.aes, info.getHeader().getCipherProvider());                
//        assertEquals("Microsoft Enhanced RSA and AES Cryptographic Provider", info.getHeader().getCspName());
        
        fs.close();
    }
}
