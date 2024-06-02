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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.DrawingGroupRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.StringUtil;
import org.apache.tools.ant.util.NullOutputStream;

class TestBiffDrawingToXml extends BaseTestIteratingXLS {

    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        // unsupported crypto api header
        excludes.put("35897-type4.xls", EncryptedDocumentException.class);
        excludes.put("51832.xls", EncryptedDocumentException.class);
        excludes.put("xor-encryption-abc.xls", EncryptedDocumentException.class);
        excludes.put("password.xls", EncryptedDocumentException.class);
        // HSSFWorkbook cannot open it as well
        excludes.put("43493.xls", RecordInputStream.LeftoverDataException.class);
        excludes.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        excludes.put("protected_66115.xls", EncryptedDocumentException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5285517825277952.xls", IllegalArgumentException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5436547081830400.xls", IllegalArgumentException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5889658057523200.xls", IndexOutOfBoundsException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-4977868385681408.xls", IllegalArgumentException.class);
        return excludes;
    }

    // output sheets with specified name
    private static final String[] SHEET_NAMES = {};

    // output sheets with specified indexes
    private static final int[] SHEET_IDX = {};

    // exclude workbook-level records
    private static final boolean EXCLUDE_WORKBOOK = false;


    @Override
    void runOneFile(File pFile) throws Exception {
        try (InputStream inp = new FileInputStream(pFile);
             OutputStream outputStream = NullOutputStream.INSTANCE) {
            writeToFile(outputStream, inp);
        }
    }

    public static void writeToFile(OutputStream fos, InputStream xlsWorkbook) throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook(xlsWorkbook)) {
            InternalWorkbook internalWorkbook = workbook.getInternalWorkbook();
            DrawingGroupRecord r = (DrawingGroupRecord) internalWorkbook.findFirstRecordBySid(DrawingGroupRecord.sid);

            StringBuilder builder = new StringBuilder();
            builder.append("<workbook>\n");
            String tab = "\t";
            if (!EXCLUDE_WORKBOOK && r != null) {
                r.decode();
                List<EscherRecord> escherRecords = r.getEscherRecords();
                for (EscherRecord record : escherRecords) {
                    builder.append(record.toXml(tab));
                }
            }
            int i = 0;
            for (HSSFSheet sheet : getSheets(workbook)) {
                HSSFPatriarch p = sheet.getDrawingPatriarch();
                if (p != null) {
                    builder.append(tab).append("<sheet").append(i).append(">\n");
                    builder.append(p.getBoundAggregate().toXml(tab + "\t"));
                    builder.append(tab).append("</sheet").append(i).append(">\n");
                    i++;
                }
            }
            builder.append("</workbook>\n");
            fos.write(builder.toString().getBytes(StringUtil.UTF8));
        }
    }

    private static List<HSSFSheet> getSheets(HSSFWorkbook workbook) {
        List<Integer> sheetIdx = Arrays.stream(SHEET_IDX).boxed().collect(Collectors.toList());
        List<String> sheetNms = Arrays.stream(SHEET_NAMES).collect(Collectors.toList());

        List<HSSFSheet> list = new ArrayList<>();

        for (Sheet sheet : workbook) {
            if ((sheetIdx.isEmpty() && sheetNms.isEmpty()) ||
                sheetIdx.contains(workbook.getSheetIndex(sheet)) ||
                sheetNms.contains(sheet.getSheetName())
            ) {
                list.add((HSSFSheet)sheet);
            }
        }

        return list;
    }
}
