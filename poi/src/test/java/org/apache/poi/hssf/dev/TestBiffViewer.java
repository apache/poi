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
import java.util.Map;

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
        excludes.put("poi-fuzz.xls", RecordFormatException.class);
        excludes.put("protected_66115.xls", RecordFormatException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5786329142919168.xls", IllegalStateException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5889658057523200.xls", IndexOutOfBoundsException.class);

        return excludes;
    }

    @Override
    void runOneFile(File fileIn) throws IOException {
        BiffViewer bv = new BiffViewer();
        bv.setInterpretRecords(true);
        bv.setDumpBiffHex(true);
        bv.parse(fileIn, null);
    }

}
