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

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSignatureConfig {
    
    @Ignore("failing in automated builds, due to issues loading security classes")
    @Test
    public void testDigestAlgo() throws Exception {
        SignatureConfig sc = new SignatureConfig();
        assertEquals(HashAlgorithm.sha256, sc.getDigestAlgo());
        sc.setDigestAlgo(HashAlgorithm.sha1);
        assertEquals(HashAlgorithm.sha1, sc.getDigestAlgo());
    }
}
