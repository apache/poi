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

package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test;

public final class TestSXSSFBugs extends BaseTestBugzillaIssues {
    public TestSXSSFBugs() {
        super(SXSSFITestDataProvider.instance);
    }

    // override some tests which do not work for SXSSF
    @Override @Ignore("cloneSheet() not implemented") @Test public void bug18800() { /* cloneSheet() not implemented */ }
    @Override @Ignore("cloneSheet() not implemented") @Test public void bug22720() { /* cloneSheet() not implemented */ }
    @Override @Ignore("Evaluation is not fully supported") @Test public void bug47815() { /* Evaluation is not supported */ }
    @Override @Ignore("Evaluation is not fully supported") @Test public void bug46729_testMaxFunctionArguments() { /* Evaluation is not supported */ }
    @Override @Ignore("Reading data is not supported") @Test public void bug57798() { /* Reading data is not supported */ }

    /**
     * Setting repeating rows and columns shouldn't break
     *  any print settings that were there before
     */
    @Test
    public void bug49253() throws Exception {
        Workbook wb1 = new SXSSFWorkbook();
        Workbook wb2 = new SXSSFWorkbook();
        CellRangeAddress cra = CellRangeAddress.valueOf("C2:D3");

        // No print settings before repeating
        Sheet s1 = wb1.createSheet(); 
        s1.setRepeatingColumns(cra);
        s1.setRepeatingRows(cra);

        PrintSetup ps1 = s1.getPrintSetup();
        assertFalse(ps1.getValidSettings());
        assertFalse(ps1.getLandscape());


        // Had valid print settings before repeating
        Sheet s2 = wb2.createSheet();
        PrintSetup ps2 = s2.getPrintSetup();

        ps2.setLandscape(false);
        assertTrue(ps2.getValidSettings());
        assertFalse(ps2.getLandscape());
        s2.setRepeatingColumns(cra);
        s2.setRepeatingRows(cra);

        ps2 = s2.getPrintSetup();
        assertTrue(ps2.getValidSettings());
        assertFalse(ps2.getLandscape());

        wb1.close();
        wb2.close();
    }
    
    // bug 60197: setSheetOrder should update sheet-scoped named ranges to maintain references to the sheets before the re-order
    @Test
    @Override
    public void bug60197_NamedRangesReferToCorrectSheetWhenSheetOrderIsChanged() throws Exception {
        try {
            super.bug60197_NamedRangesReferToCorrectSheetWhenSheetOrderIsChanged();
        } catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            //noinspection StatementWithEmptyBody
            if (cause instanceof IOException && cause.getMessage().equals("Stream closed")) {
                // expected on the second time that _testDataProvider.writeOutAndReadBack(SXSSFWorkbook) is called
                // if the test makes it this far, then we know that XSSFName sheet indices are updated when sheet
                // order is changed, which is the purpose of this test. Therefore, consider this a passing test.
            } else {
                throw e;
            }
        }
    }

    @Test
    public void bug61648() throws Exception {
        // works as expected
        writeWorkbook(new XSSFWorkbook(), XSSFITestDataProvider.instance);

        // does not work
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            writeWorkbook(wb, SXSSFITestDataProvider.instance);
            fail("Should catch exception here");
        } catch (RuntimeException e) {
            // this is not implemented yet
        }
    }

    void writeWorkbook(Workbook wb, ITestDataProvider testDataProvider) throws IOException {
        Sheet sheet = wb.createSheet("array formula test");

        int rowIndex = 0;
        int colIndex = 0;
        Row row = sheet.createRow(rowIndex++);

        Cell cell = row.createCell(colIndex++);
        cell.setCellValue("multiple");
        cell = row.createCell(colIndex);
        cell.setCellValue("unique");

        writeRow(sheet, rowIndex++, 80d, "INDEX(A2:A7, MATCH(FALSE, ISBLANK(A2:A7), 0))");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B2, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B3, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 2d,  "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B4, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B5, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex, 2d,  "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B6, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");

        /*FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();*/

        Workbook wbBack = testDataProvider.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();

        wb.close();
    }

    void writeRow(Sheet sheet, int rowIndex, Double col0Value, String col1Value) {
        int colIndex = 0;
        Row row = sheet.createRow(rowIndex);

        // numeric value cell
        Cell cell = row.createCell(colIndex++);
        cell.setCellValue(col0Value);

        // formula value cell
        CellRangeAddress range = new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex);
        sheet.setArrayFormula(col1Value, range);
    }

    @Test
    @Ignore("takes too long for the normal test run")
    public void test62872() throws Exception {
        final int COLUMN_COUNT = 300;
        final int ROW_COUNT = 600000;
        final int TEN_MINUTES = 1000*60*10;

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        SXSSFSheet sheet = workbook.createSheet("RawData");

        SXSSFRow row = sheet.createRow(0);
        SXSSFCell cell;

        for (int i = 1; i <= COLUMN_COUNT; i++) {
            cell = row.createCell(i - 1);
            cell.setCellValue("Column " + i);
        }

        for (int i = 1; i < ROW_COUNT; i++) {
            row = sheet.createRow(i);
            for (int j = 1; j <= COLUMN_COUNT; j++) {
                cell = row.createCell(j - 1);

                //make some noise
                cell.setCellValue(new Date(i*TEN_MINUTES+(j*TEN_MINUTES)/COLUMN_COUNT));
            }
            i++;
            // if (i % 1000 == 0)
            // logger.info("Created Row " + i);
        }

        try (FileOutputStream out = new FileOutputStream(File.createTempFile("test62872", ".xlsx"))) {
            workbook.write(out);
            workbook.dispose();
            workbook.close();
            out.flush();
        }
    }
}
