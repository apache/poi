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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.junit.Test;

/**
 * Tests of implementation of {@link DataFormat}
 *
 */
public abstract class BaseTestDataFormat {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestDataFormat(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public void assertNotBuiltInFormat(String customFmt) {
        //check it is not in built-in formats
        assertEquals(-1, BuiltinFormats.getBuiltinFormat(customFmt));
    }

    @Test
    public final void testBuiltinFormats() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        DataFormat df = wb.createDataFormat();

        String[] formats = BuiltinFormats.getAll();
        for (int idx = 0; idx < formats.length; idx++) {
            String fmt = formats[idx];
            assertEquals(idx, df.getFormat(fmt));
        }

        //default format for new cells is General
        Sheet sheet = wb.createSheet();
        Cell cell = sheet.createRow(0).createCell(0);
        assertEquals(0, cell.getCellStyle().getDataFormat());
        assertEquals("General", cell.getCellStyle().getDataFormatString());

        //create a custom data format
        String customFmt = "#0.00 AM/PM";
        //check it is not in built-in formats
        assertNotBuiltInFormat(customFmt);
        int customIdx = df.getFormat(customFmt);
        //The first user-defined format starts at 164.
        assertTrue(customIdx >= BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX);
        //read and verify the string representation
        assertEquals(customFmt, df.getFormat((short)customIdx));

        wb.close();
    }

    /**
     * [Bug 49928] formatCellValue returns incorrect value for \u00a3 formatted cells
     */
    @Test
    public abstract void test49928() throws IOException;
    protected final static String poundFmt = "\"\u00a3\"#,##0;[Red]\\-\"\u00a3\"#,##0";
    public void doTest49928Core(Workbook wb){
        DataFormatter df = new DataFormatter();

        Sheet sheet = wb.getSheetAt(0);
        Cell cell = sheet.getRow(0).getCell(0);
        CellStyle style = cell.getCellStyle();

        // not expected normally, id of a custom format should be greater
        // than BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX
        short  poundFmtIdx = 6;

        assertEquals(poundFmt, style.getDataFormatString());
        assertEquals(poundFmtIdx, style.getDataFormat());
        assertEquals("\u00a31", df.formatCellValue(cell));


        DataFormat dataFormat = wb.createDataFormat();
        assertEquals(poundFmtIdx, dataFormat.getFormat(poundFmt));
        assertEquals(poundFmt, dataFormat.getFormat(poundFmtIdx));
    }

    @Test
    public void testReadbackFormat() throws IOException {
        readbackFormat("built-in format", "0.00");
        readbackFormat("overridden built-in format", poundFmt);

        String customFormat = "#0.00 AM/PM";
        assertNotBuiltInFormat(customFormat);
        readbackFormat("custom format", customFormat);
    }

    private void readbackFormat(String msg, String fmt) throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        try {
            DataFormat dataFormat = wb.createDataFormat();
            short fmtIdx = dataFormat.getFormat(fmt);
            String readbackFmt = dataFormat.getFormat(fmtIdx);
            assertEquals(msg, fmt, readbackFmt);
        } finally {
            wb.close();
        }
    }

    @Test
    public abstract void test58532() throws IOException;
    public void doTest58532Core(Workbook wb) {
        Sheet s = wb.getSheetAt(0);
        DataFormatter fmt = new DataFormatter();
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

        // Column A is the raw values
        // Column B is the ##/#K/#M values
        // Column C is strings of what they should look like
        // Column D is the #.##/#.#K/#.#M values
        // Column E is strings of what they should look like

        String formatKMWhole = "[>999999]#,,\"M\";[>999]#,\"K\";#";
        String formatKM3dp = "[>999999]#.000,,\"M\";[>999]#.000,\"K\";#.000";

        // Check the formats are as expected
        Row headers = s.getRow(0);
        assertNotNull(headers);
        assertEquals(formatKMWhole, headers.getCell(1).getStringCellValue());
        assertEquals(formatKM3dp, headers.getCell(3).getStringCellValue());

        Row r2 = s.getRow(1);
        assertNotNull(r2);
        assertEquals(formatKMWhole, r2.getCell(1).getCellStyle().getDataFormatString());
        assertEquals(formatKM3dp, r2.getCell(3).getCellStyle().getDataFormatString());

        // For all of the contents rows, check that DataFormatter is able
        //  to format the cells to the same value as the one next to it
        for (int rn=1; rn<s.getLastRowNum(); rn++) {
            Row r = s.getRow(rn);
            if (r == null) break;

            double value = r.getCell(0).getNumericCellValue();

            String expWhole = r.getCell(2).getStringCellValue();
            String exp3dp   = r.getCell(4).getStringCellValue();

            assertEquals("Wrong formatting of " + value + " for row " + rn,
                         expWhole, fmt.formatCellValue(r.getCell(1), eval));
            assertEquals("Wrong formatting of " + value + " for row " + rn,
                         exp3dp, fmt.formatCellValue(r.getCell(3), eval));
        }
    }

    /**
     * Localized accountancy formats
     */
    @Test
    public final void test58536() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        DataFormatter formatter = new DataFormatter();
        DataFormat fmt = wb.createDataFormat();
        Sheet sheet = wb.createSheet();
        Row r = sheet.createRow(0);

        char pound = '\u00A3';
        String formatUK  = "_-[$"+pound+"-809]* #,##0_-;\\-[$"+pound+"-809]* #,##0_-;_-[$"+pound+"-809]* \"-\"??_-;_-@_-";

        CellStyle cs = wb.createCellStyle();
        cs.setDataFormat(fmt.getFormat(formatUK));

        Cell pve = r.createCell(0);
        pve.setCellValue(12345);
        pve.setCellStyle(cs);

        Cell nve = r.createCell(1);
        nve.setCellValue(-12345);
        nve.setCellStyle(cs);

        Cell zero = r.createCell(2);
        zero.setCellValue(0);
        zero.setCellStyle(cs);

        assertEquals(pound+"   12,345", formatter.formatCellValue(pve));
        assertEquals("-"+pound+"   12,345", formatter.formatCellValue(nve));
        // TODO Fix this to not have an extra 0 at the end
        //assertEquals(pound+"   -  ", formatter.formatCellValue(zero));
        
        wb.close();
    }

    /**
     * Using a single quote (') instead of a comma (,) as
     *  a number separator, eg 1000 -> 1'000
     */
    @Test
    public final void test55265() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        DataFormatter formatter = new DataFormatter();
        DataFormat fmt = wb.createDataFormat();
        Sheet sheet = wb.createSheet();
        Row r = sheet.createRow(0);

        CellStyle cs = wb.createCellStyle();
        cs.setDataFormat(fmt.getFormat("#'##0"));

        Cell zero = r.createCell(0);
        zero.setCellValue(0);
        zero.setCellStyle(cs);

        Cell sml = r.createCell(1);
        sml.setCellValue(12);
        sml.setCellStyle(cs);

        Cell med = r.createCell(2);
        med.setCellValue(1234);
        med.setCellStyle(cs);

        Cell lge = r.createCell(3);
        lge.setCellValue(12345678);
        lge.setCellStyle(cs);

        assertEquals("0", formatter.formatCellValue(zero));
        assertEquals("12", formatter.formatCellValue(sml));
        assertEquals("1'234", formatter.formatCellValue(med));
        assertEquals("12'345'678", formatter.formatCellValue(lge));
        wb.close();
    }
}
