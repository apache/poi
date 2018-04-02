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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;
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
        assertEquals(false, ps1.getValidSettings());
        assertEquals(false, ps1.getLandscape());


        // Had valid print settings before repeating
        Sheet s2 = wb2.createSheet();
        PrintSetup ps2 = s2.getPrintSetup();

        ps2.setLandscape(false);
        assertEquals(true, ps2.getValidSettings());
        assertEquals(false, ps2.getLandscape());
        s2.setRepeatingColumns(cra);
        s2.setRepeatingRows(cra);

        ps2 = s2.getPrintSetup();
        assertEquals(true, ps2.getValidSettings());
        assertEquals(false, ps2.getLandscape());

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
        cell.setCellType(CellType.STRING);
        cell.setCellValue("multiple");
        cell = row.createCell(colIndex++);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("unique");

        writeRow(sheet, rowIndex++, 80d, "INDEX(A2:A7, MATCH(FALSE, ISBLANK(A2:A7), 0))");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B2, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B3, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 2d,  "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B4, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 30d, "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B5, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");
        writeRow(sheet, rowIndex++, 2d,  "IFERROR(INDEX(A2:A7, MATCH(1, (COUNTIF(B2:B6, A2:A7) = 0) * (NOT(ISBLANK(A2:A7))), 0)), \"\")");

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
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(col0Value);

        // formula value cell
        CellRangeAddress range = new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex);
        sheet.setArrayFormula(col1Value, range);
    }
}
