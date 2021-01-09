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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

/**
 * @author centic
 *
 * This testcase contains tests for bugs that are yet to be fixed. Therefore,
 * the standard ant test target does not run these tests. Run this testcase with
 * the single-test target. The names of the tests usually correspond to the
 * Bugzilla id's PLEASE MOVE tests from this class to TestBugs once the bugs are
 * fixed, so that they are then run automatically.
 */
public final class TestUnfixedBugs {
    @Test
    void test54071() throws Exception {
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("54071.xlsx");
        Sheet sheet = workbook.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        Row title = sheet.getRow(0);

        Date prev = null;
        for (int row = 1; row < rows; row++) {
            Row rowObj = sheet.getRow(row);
            for (int col = 0; col < 1; col++) {
                String titleName = title.getCell(col).toString();
                Cell cell = rowObj.getCell(col);
                if (titleName.startsWith("time")) {
                    // here the output will produce ...59 or ...58 for the rows, probably POI is
                    // doing some different rounding or some other small difference...
                    //System.out.println("==Time:"+cell.getDateCellValue());
                    if(prev != null) {
                        assertEquals(prev, cell.getDateCellValue());
                    }

                    prev = cell.getDateCellValue();
                }
            }
        }

        workbook.close();
    }

    @Test
    void test54071Simple() {
        double value1 = 41224.999988425923;
        double value2 = 41224.999988368058;

        int wholeDays1 = (int)Math.floor(value1);
        int millisecondsInDay1 = (int)((value1 - wholeDays1) * DateUtil.DAY_MILLISECONDS + 0.5);

        int wholeDays2 = (int)Math.floor(value2);
        int millisecondsInDay2 = (int)((value2 - wholeDays2) * DateUtil.DAY_MILLISECONDS + 0.5);

        assertEquals(wholeDays1, wholeDays2);
        // here we see that the time-value is 5 milliseconds apart, one is 86399000 and the other is 86398995,
        // thus one is one second higher than the other
        assertEquals(millisecondsInDay1, millisecondsInDay2, "The time-values are 5 milliseconds apart");

        // when we do the calendar-stuff, there is a boolean which determines if
        // the milliseconds are rounded or not, having this at "false" causes the
        // second to be different here!
        int startYear = 1900;
        int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
        Calendar calendar1 = LocaleUtil.getLocaleCalendar(startYear,0, wholeDays1 + dayAdjust);
        calendar1.set(Calendar.MILLISECOND, millisecondsInDay1);
        // this is the rounding part:
        calendar1.add(Calendar.MILLISECOND, 500);
        calendar1.clear(Calendar.MILLISECOND);

        Calendar calendar2 = LocaleUtil.getLocaleCalendar(startYear,0, wholeDays2 + dayAdjust);
        calendar2.set(Calendar.MILLISECOND, millisecondsInDay2);
        // this is the rounding part:
        calendar2.add(Calendar.MILLISECOND, 500);
        calendar2.clear(Calendar.MILLISECOND);

        // now the calendars are equal
        assertEquals(calendar1, calendar2);

        assertEquals(DateUtil.getJavaDate(value1, false), DateUtil.getJavaDate(value2, false));
    }

    // When this is fixed, the test case should go to BaseTestXCell with
    // adjustments to use _testDataProvider to also verify this for XSSF
    @Test
    void testBug57294() throws IOException {
        Workbook wb = SXSSFITestDataProvider.instance.createWorkbook();

        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        RichTextString str = new XSSFRichTextString("Test rich text string");
        str.applyFont(2, 4, (short)0);
        assertEquals(3, str.numFormattingRuns());
        cell.setCellValue(str);

        Workbook wbBack = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        wb.close();

        // re-read after serializing and reading back
        Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(0);
        assertNotNull(cellBack);
        RichTextString strBack = cellBack.getRichStringCellValue();
        assertNotNull(strBack);
        assertEquals(3, strBack.numFormattingRuns());
        assertEquals(0, strBack.getIndexOfFormattingRun(0));
        assertEquals(2, strBack.getIndexOfFormattingRun(1));
        assertEquals(4, strBack.getIndexOfFormattingRun(2));

        wbBack.close();
    }

    // This test will run green, but the resulting file is formatted incorrectly,
    // see the bug at https://bz.apache.org/bugzilla/show_bug.cgi?id=55752
    @Test
    void testBug55752() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("test");

            for (int i = 0; i < 4; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < 2; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(wb.createCellStyle());
                }
            }

            // set content
            Row row1 = sheet.getRow(0);
            row1.getCell(0).setCellValue("AAA");
            Row row2 = sheet.getRow(1);
            row2.getCell(0).setCellValue("BBB");
            Row row3 = sheet.getRow(2);
            row3.getCell(0).setCellValue("CCC");
            Row row4 = sheet.getRow(3);
            row4.getCell(0).setCellValue("DDD");

            // merge cells
            CellRangeAddress range1 = new CellRangeAddress(0, 0, 0, 1);
            assertEquals(0, sheet.addMergedRegion(range1));
            CellRangeAddress range2 = new CellRangeAddress(1, 1, 0, 1);
            assertEquals(1, sheet.addMergedRegion(range2));
            CellRangeAddress range3 = new CellRangeAddress(2, 2, 0, 1);
            assertEquals(2, sheet.addMergedRegion(range3));
            assertEquals(0, range3.getFirstColumn());
            assertEquals(1, range3.getLastColumn());
            assertEquals(2, range3.getLastRow());
            CellRangeAddress range4 = new CellRangeAddress(3, 3, 0, 1);
            assertEquals(3, sheet.addMergedRegion(range4));

            // set border
            RegionUtil.setBorderBottom(BorderStyle.THIN, range1, sheet);

            row2.getCell(0).getCellStyle().setBorderBottom(BorderStyle.THIN);
            row2.getCell(1).getCellStyle().setBorderBottom(BorderStyle.THIN);

            Cell cell0 = CellUtil.getCell(row3, 0);
            CellUtil.setCellStyleProperty(cell0, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            Cell cell1 = CellUtil.getCell(row3, 1);
            CellUtil.setCellStyleProperty(cell1, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            RegionUtil.setBorderBottom(BorderStyle.THIN, range4, sheet);

            // write to file for manual inspection
            XSSFTestDataSamples.writeOut(wb, "bug 55752 for review");
        }

        fail("Test runs ok, but the resulting file is incorrectly formatted");
    }

    @Test
    void test57423() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57423.xlsx");

        Sheet testSheet = wb.getSheetAt(0);

        // row shift (negative or positive) causes corrupted output xlsx file when the shift value is bigger
        // than the number of rows being shifted
        // Excel 2010 on opening the output file says:
        // "Excel found unreadable content" and offers recovering the file by removing the unreadable content
        // This can be observed in cases like the following:
        // negative shift of 1 row by less than -1
        // negative shift of 2 rows by less than -2
        // positive shift of 1 row by 2 or more
        // positive shift of 2 rows by 3 or more

        //testSheet.shiftRows(4, 5, -3);
        testSheet.shiftRows(10, 10, 2);

        checkRows57423(testSheet);

        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        /* XSSFTestDataSamples.writeOut(wb, "bug 57423 for manual review"); */

        wb.close();

        checkRows57423(wbBack.getSheetAt(0));

        wbBack.close();

        fail("Excel reports that the workbook is corrupt");
    }

    private void checkRows57423(Sheet testSheet) throws IOException {
        checkRow57423(testSheet, 0, "0");
        checkRow57423(testSheet, 1, "1");
        checkRow57423(testSheet, 2, "2");
        checkRow57423(testSheet, 3, "3");
        checkRow57423(testSheet, 4, "4");
        checkRow57423(testSheet, 5, "5");
        checkRow57423(testSheet, 6, "6");
        checkRow57423(testSheet, 7, "7");
        checkRow57423(testSheet, 8, "8");
        checkRow57423(testSheet, 9, "9");

        assertNull(testSheet.getRow(10), "Row number 10 should be gone after the shift");

        checkRow57423(testSheet, 11, "11");
        checkRow57423(testSheet, 12, "10");
        checkRow57423(testSheet, 13, "13");
        checkRow57423(testSheet, 14, "14");
        checkRow57423(testSheet, 15, "15");
        checkRow57423(testSheet, 16, "16");
        checkRow57423(testSheet, 17, "17");
        checkRow57423(testSheet, 18, "18");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ((XSSFSheet)testSheet).write(stream);
        } finally {
            stream.close();
        }

        // verify that the resulting XML has the rows in correct order as required by Excel
        String xml = new String(stream.toByteArray(), StandardCharsets.UTF_8);
        int posR12 = xml.indexOf("<row r=\"12\"");
        int posR13 = xml.indexOf("<row r=\"13\"");

        // both need to be found
        assertTrue(posR12 != -1);
        assertTrue(posR13 != -1);

        assertTrue(posR12 < posR13, "Need to find row 12 before row 13 after the shifting, but had row 12 at " + posR12 + " and row 13 at " + posR13);
    }

    private void checkRow57423(Sheet testSheet, int rowNum, String contents) {
        Row row = testSheet.getRow(rowNum);
        assertNotNull(row, "Expecting row at rownum " + rowNum);

        CTRow ctRow = ((XSSFRow)row).getCTRow();
        assertEquals(rowNum+1, ctRow.getR());

        Cell cell = row.getCell(0);
        assertNotNull(cell, "Expecting cell at rownum " + rowNum);
        assertEquals( contents + ".0", cell.toString(), "Did not have expected contents at rownum " + rowNum );
    }

    @Test
    void bug57423_shiftRowsByLargeOffset() throws IOException {
        try (
                XSSFWorkbook wb = new XSSFWorkbook()
                //OutputStream out = new FileOutputStream("/tmp/57423." + wb.getClass().getName() + ".xlsx"));
        ) {
            Sheet sh = wb.createSheet();
            sh.createRow(0).createCell(0).setCellValue("a");
            sh.createRow(1).createCell(0).setCellValue("b");
            sh.createRow(2).createCell(0).setCellValue("c");
            sh.shiftRows(0, 1, 3);

            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);

            assertThatRowsInAscendingOrder(wb);
            assertThatRowsInAscendingOrder(wbBack);

            //wbBack.write(out);
            // Excel reports that the workbook is corrupt because the rows are not in ascending order
            // LibreOffice doesn't complain when rows are not in ascending order

            wbBack.close();

            fail("Excel reports that the workbook is corrupt, LibreOffice can read it");
        }
    }

    private void assertThatRowsInAscendingOrder(final XSSFWorkbook wb) {
        // Check that CTRows are stored in ascending order of row index
        long maxSeenRowNum = 0; //1-based
        for (final CTRow ctRow : wb.getSheetAt(0).getCTWorksheet().getSheetData().getRowList()) {
            final long rowNum = ctRow.getR(); //1-based
            assertTrue(rowNum > maxSeenRowNum, "Row " + rowNum + " (1-based) is not in ascending order; previously saw " + maxSeenRowNum);
            maxSeenRowNum = rowNum;
        }
    }

    @Test
    void testBug60355() throws IOException {
        try (Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("HsGetVal.xlsx")){

            Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
            System.out.println("cell_4_1 formula:" + sheet.getRow(4).getCell(1).getCellFormula());
            System.out.println("cell_4_2 formula:" + sheet.getRow(4).getCell(2).getCellFormula());

            // hard code HsGetValue test values for formulas on the sheet
            Hashtable<CellAddress, String> cellToValueTable = new Hashtable<>();
            CellAddress cell4_1 = new CellAddress(4, 1);
            cellToValueTable.put(cell4_1, "678.0");
            CellAddress cell4_2 = new CellAddress(4, 2);
            cellToValueTable.put(cell4_2, "123.0");

            String[] functionNames = {HsGetValue.name};
            FreeRefFunction[] functionImpls = {new HsGetValue(cellToValueTable)};
            UDFFinder udfs = new DefaultUDFFinder(functionNames, functionImpls);
            UDFFinder udfToolpack = new AggregatingUDFFinder(udfs);
            workbook.addToolPack(udfToolpack);

            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.setIgnoreMissingWorkbooks(true);
            formulaEvaluator.evaluateAll();
        }
    }

    public static class HsGetValue implements FreeRefFunction {
        public static final String name = "HsGetValue";

        private final Hashtable<CellAddress, String> cellValues;

        public HsGetValue(Hashtable<CellAddress, String> cellValues) {
            super();
            this.cellValues = cellValues;
        }

        @Override
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext evaluationContext) {
            int row = evaluationContext.getRowIndex();
            int column = evaluationContext.getColumnIndex();
            CellAddress cell = new CellAddress(row, column);

            String value = cellValues.get(cell);
            return new NumberEval( Double.parseDouble(value) );
        }

    }

    @Test
    void testBug64759() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("64759.xlsx")) {
            Sheet cloned = wb.cloneSheet(0);
            assertNotNull(cloned);
        }
    }
}
