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

package org.apache.poi.hwpf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HWPFTestEncryption {
    @AfterClass
    public static void clearPass() {
        Biff8EncryptionKey.setCurrentUserPassword(null);
    }

    @Parameter(value = 0)
    public String file;

    @Parameter(value = 1)
    public String password;

    @Parameter(value = 2)
    public String expected;

    @Parameters(name="{0}")
    public static Collection<String[]> data() {
        return Arrays.asList(
            new String[]{ "password_tika_binaryrc4.doc", "tika", "This is an encrypted Word 2007 File." },
            new String[]{ "password_password_cryptoapi.doc", "password", "This is a test" }
        );
    }

    @Test
    public void extract() throws IOException {
        Biff8EncryptionKey.setCurrentUserPassword(password);
        HWPFDocument docD = HWPFTestDataSamples.openSampleFile(file);
        WordExtractor we = new WordExtractor(docD);
        String actual = we.getText().trim();
        assertEquals(expected, actual);
        we.close();
        docD.close();
    }
}
