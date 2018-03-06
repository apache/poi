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
package org.apache.poi.poifs.crypt.dsig;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.junit.Ignore;
import org.junit.Test;

public class TestSignatureConfig {
    
    @Test
    @Ignore("failing in automated builds, due to issues loading security classes")
    public void testDigestAlgo() throws Exception {
        SignatureConfig sc = new SignatureConfig();
        assertEquals(HashAlgorithm.sha256, sc.getDigestAlgo());
        sc.setDigestAlgo(HashAlgorithm.sha1);
        assertEquals(HashAlgorithm.sha1, sc.getDigestAlgo());
    }
    
    @Test
    public void testHashOids() throws IOException {
        final String[][] checks = {
            { "sha1", "MCEwCQYFKw4DAhoFAAQU" },
            { "sha224", "MC0wDQYJYIZIAWUDBAIEBQAEHA==" },
            { "sha256", "MDEwDQYJYIZIAWUDBAIBBQAEIA==" },
            { "sha384", "MEEwDQYJYIZIAWUDBAICBQAEMA==" },
            { "sha512", "MFEwDQYJYIZIAWUDBAIDBQAEQA==" },
            { "ripemd128", "MB0wCQYFKyQDAgIFAAQQ" },
            { "ripemd160", "MCEwCQYFKyQDAgEFAAQU" },
            { "ripemd256", "MC0wCQYFKyQDAgMFAAQg" },
        };

        for (final String[] check : checks) {
            final HashAlgorithm ha = HashAlgorithm.valueOf(check[0]);
            try (final DigestOutputStream dos = new DigestOutputStream(ha, null)) {
                final String magic = DatatypeConverter.printBase64Binary(dos.getHashMagic());
                assertEquals("hash digest magic mismatches", check[1], magic);
            }
        }
    }
}
