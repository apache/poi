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

import static org.apache.poi.hssf.HSSFTestDataSamples.getSampleFile;
import static org.apache.poi.hssf.HSSFTestDataSamples.writeOutAndReadBack;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestXorEncryption {
    @Test
    void testXorEncryption() {
        // Xor-Password: abc
        // 2.5.343 XORObfuscation
        // key = 20810
        // verifier = 52250
        int verifier = CryptoFunctions.createXorVerifier1("abc");
        int key = CryptoFunctions.createXorKey1("abc");
        assertEquals(20810, key);
        assertEquals(52250, verifier);

        byte[] xorArrAct = CryptoFunctions.createXorArray1("abc");
        byte[] xorArrExp = HexRead.readFromString("AC-CC-A4-AB-D6-BA-C3-BA-D6-A3-2B-45-D3-79-29-BB");
        assertThat(xorArrExp, equalTo(xorArrAct));
    }

    @Test
    void testXorEncryptionEmptyPassword() {
        // Xor-Password: ""
        // 2.5.343 XORObfuscation
        // key = 0
        // verifier = 0
        int verifier = CryptoFunctions.createXorVerifier1("");
        int key = CryptoFunctions.createXorKey1("");
        assertEquals(0, key);
        assertEquals(0, verifier);

        byte[] xorArrAct = CryptoFunctions.createXorArray1("");
        byte[] xorArrExp = HexRead.readFromString("EE-FF-FF-EA-FF-FF-E6-02-00-FA-3C-00-FE-3C-00-00");
        assertThat("Having: " + HexDump.dump(xorArrAct, 0, 0),
                xorArrExp, equalTo(xorArrAct));
    }

    @Test
    void testUserFile() throws IOException {
        File f = getSampleFile("xor-encryption-abc.xls");
        Biff8EncryptionKey.setCurrentUserPassword("abc");
        try (POIFSFileSystem fs = new POIFSFileSystem(f, true);
             HSSFWorkbook hwb = new HSSFWorkbook(fs.getRoot(), true)) {
            HSSFSheet sh = hwb.getSheetAt(0);
            assertEquals(1.0, sh.getRow(0).getCell(0).getNumericCellValue(), 0.0);
            assertEquals(2.0, sh.getRow(1).getCell(0).getNumericCellValue(), 0.0);
            assertEquals(3.0, sh.getRow(2).getCell(0).getNumericCellValue(), 0.0);
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    @Test
    @Disabled("currently not supported")
    void encrypt() throws IOException {
        try {
            try (HSSFWorkbook hwb = HSSFTestDataSamples.openSampleWorkbook("SampleSS.xls")) {
                Biff8EncryptionKey.setCurrentUserPassword("abc");
                hwb.getInternalWorkbook().getWorkbookRecordList()
                    .add(1, new FilePassRecord(EncryptionMode.xor));
                try (HSSFWorkbook hwb2 = writeOutAndReadBack(hwb)) {
                    assertEquals(3, hwb2.getNumberOfSheets());
                }
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
}
