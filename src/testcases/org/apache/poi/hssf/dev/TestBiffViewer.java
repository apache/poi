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
package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.BeforeAll;

class TestBiffViewer extends BaseTestIteratingXLS {
    @BeforeAll
    public static void setup() {
        EXCLUDED.clear();
        EXCLUDED.put("35897-type4.xls", IllegalArgumentException.class); // unsupported crypto api header
        EXCLUDED.put("51832.xls", IllegalArgumentException.class);
        EXCLUDED.put("xor-encryption-abc.xls", RecordFormatException.class);
        EXCLUDED.put("password.xls", IllegalArgumentException.class);
        EXCLUDED.put("46904.xls", OldExcelFormatException.class);
        EXCLUDED.put("59074.xls", OldExcelFormatException.class);
        EXCLUDED.put("testEXCEL_2.xls", OldExcelFormatException.class);  // Biff 2 / Excel 2, pre-OLE2
        EXCLUDED.put("testEXCEL_3.xls", OldExcelFormatException.class);  // Biff 3 / Excel 3, pre-OLE2
        EXCLUDED.put("testEXCEL_4.xls", OldExcelFormatException.class);  // Biff 4 / Excel 4, pre-OLE2
        EXCLUDED.put("testEXCEL_5.xls", OldExcelFormatException.class);  // Biff 5 / Excel 5
        EXCLUDED.put("60284.xls", OldExcelFormatException.class); // Biff 5 / Excel 5
        EXCLUDED.put("testEXCEL_95.xls", OldExcelFormatException.class); // Biff 5 / Excel 95
        EXCLUDED.put("43493.xls", RecordFormatException.class);  // HSSFWorkbook cannot open it as well
        // EXCLUDED.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        EXCLUDED.put("50833.xls", IllegalArgumentException.class);       // "Name is too long" when setting username
        EXCLUDED.put("XRefCalc.xls", RuntimeException.class);            // "Buffer overrun"
        EXCLUDED.put("61300.xls", IndexOutOfBoundsException.class);
        EXCLUDED.put("64130.xls", OldExcelFormatException.class); //Biff 5
    }

    @Override
    void runOneFile(File fileIn) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(fileIn, true);
             InputStream is = BiffViewer.getPOIFSInputStream(fs)) {
            // use a NullOutputStream to not write the bytes anywhere for best runtime
            PrintWriter dummy = new PrintWriter(new OutputStreamWriter(new NullOutputStream(), LocaleUtil.CHARSET_1252));
            BiffViewer.runBiffViewer(dummy, is, true, true, true, false);
        }
    }

//    @Test
//    @Disabled("only used for manual tests")
//    @SuppressWarnings("java:S2699")
//    void testOneFile() throws Exception {
//        POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();
//        runOneFile(samples.getFile("43493.xls"));
//    }
}
