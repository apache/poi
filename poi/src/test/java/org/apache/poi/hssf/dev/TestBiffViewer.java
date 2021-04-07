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
import java.util.Map;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.util.RecordFormatException;

class TestBiffViewer extends BaseTestIteratingXLS {
    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        // unsupported crypto api header
        excludes.put("35897-type4.xls", IllegalArgumentException.class);
        excludes.put("51832.xls", IllegalArgumentException.class);
        excludes.put("xor-encryption-abc.xls", RecordFormatException.class);
        excludes.put("password.xls", IllegalArgumentException.class);
        // HSSFWorkbook cannot open it as well
        excludes.put("43493.xls", RecordFormatException.class);
        // EXCLUDED.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        // "Name is too long" when setting username
        excludes.put("50833.xls", IllegalArgumentException.class);
        // "Buffer overrun"
        excludes.put("XRefCalc.xls", RuntimeException.class);

        excludes.put("61300.xls", IndexOutOfBoundsException.class);
        return excludes;
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
