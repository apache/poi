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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestAgileEncryptionParameters {

    static byte testData[];
    
    @Parameter(value = 0)
    public CipherAlgorithm ca;
    @Parameter(value = 1)
    public HashAlgorithm ha;
    @Parameter(value = 2)
    public ChainingMode cm;

    @Parameters(name="{0} {1} {2}")
    public static Collection<Object[]> data() {
        CipherAlgorithm caList[] = { CipherAlgorithm.aes128, CipherAlgorithm.aes192, CipherAlgorithm.aes256, CipherAlgorithm.rc2, CipherAlgorithm.des, CipherAlgorithm.des3 };
        HashAlgorithm haList[] = { HashAlgorithm.sha1, HashAlgorithm.sha256, HashAlgorithm.sha384, HashAlgorithm.sha512, HashAlgorithm.md5 };
        ChainingMode cmList[] = { ChainingMode.cbc, ChainingMode.cfb };

        List<Object[]> data = new ArrayList<>();
        for (CipherAlgorithm ca : caList) {
            for (HashAlgorithm ha : haList) {
                for (ChainingMode cm : cmList) {
                    data.add(new Object[]{ca,ha,cm});
                }
            }
        }
        
        return data;
    }
    
    @BeforeClass
    public static void initTestData() throws Exception {
        InputStream testFile = POIDataSamples.getDocumentInstance().openResourceAsStream("SampleDoc.docx");
        testData = IOUtils.toByteArray(testFile);
        testFile.close();
    }
    
    @Test
    public void testAgileEncryptionModes() throws Exception {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength(ca.jceId);
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files", maxKeyLen >= ca.defaultKeySize);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        POIFSFileSystem fsEnc = new POIFSFileSystem();
        EncryptionInfo infoEnc = new EncryptionInfo(EncryptionMode.agile, ca, ha, -1, -1, cm);
        Encryptor enc = infoEnc.getEncryptor();
        enc.confirmPassword("foobaa");
        OutputStream os = enc.getDataStream(fsEnc);
        os.write(testData);
        os.close();
        bos.reset();
        fsEnc.writeFilesystem(bos);
        fsEnc.close();
        
        POIFSFileSystem fsDec = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        EncryptionInfo infoDec = new EncryptionInfo(fsDec);
        Decryptor dec = infoDec.getDecryptor();
        boolean passed = dec.verifyPassword("foobaa");
        assertTrue(passed);
        InputStream is = dec.getDataStream(fsDec);
        byte actualData[] = IOUtils.toByteArray(is);
        is.close();
        fsDec.close();
        assertArrayEquals("Failed roundtrip - "+ca+"-"+ha+"-"+cm, testData, actualData);
    }
}
