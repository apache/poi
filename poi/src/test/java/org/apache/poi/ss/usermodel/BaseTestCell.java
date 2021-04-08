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

import static org.apache.poi.ss.usermodel.FormulaError.forInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Common superclass for testing implementations of
 *  {@link org.apache.poi.ss.usermodel.Cell}
 */
@SuppressWarnings("deprecation")
public abstract class BaseTestCell {

    protected final ITestDataProvider _testDataProvider;

    /**
     * @param testDataProvider an object that provides test data in HSSF / XSSF specific way
     */
    protected BaseTestCell(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    void testSetValues() throws Exception {
        try (Workbook book = _testDataProvider.createWorkbook()) {
            Sheet sheet = book.createSheet("test");
            Row row = sheet.createRow(0);

            CreationHelper factory = book.getCreationHelper();
            Cell cell = row.createCell(0);

            cell.setCellValue(1.2);
            assertEquals(1.2, cell.getNumericCellValue(), 0.0001);
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.BOOLEAN, CellType.STRING,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellValue(false);
            assertFalse(cell.getBooleanCellValue());
            assertEquals(CellType.BOOLEAN, cell.getCellType());
            cell.setCellValue(true);
            assertTrue(cell.getBooleanCellValue());
            assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.STRING, CellType.BOOLEAN,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellValue(factory.createRichTextString("Foo"));
            assertEquals("Foo", cell.getRichStringCellValue().getString());
            assertEquals("Foo", cell.getStringCellValue());
            assertEquals(CellType.STRING, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.BOOLEAN,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellValue("345");
            assertEquals("345", cell.getRichStringCellValue().getString());
            assertEquals("345", cell.getStringCellValue());
            assertEquals(CellType.STRING, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.BOOLEAN,
                                        CellType.FORMULA, CellType.ERROR);

            Calendar c = LocaleUtil.getLocaleCalendar();
            c.setTimeInMillis(123456789);
            cell.setCellValue(c.getTime());
            assertEquals(c.getTime().getTime(), cell.getDateCellValue().getTime());
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.BOOLEAN, CellType.STRING,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellValue(c);
            assertEquals(c.getTime().getTime(), cell.getDateCellValue().getTime());
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.BOOLEAN, CellType.STRING,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellErrorValue(FormulaError.NA.getCode());
            assertEquals(FormulaError.NA.getCode(), cell.getErrorCellValue());
            assertEquals(CellType.ERROR, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.BOOLEAN,
                                        CellType.FORMULA, CellType.STRING);

            LocalDateTime ldt = DateUtil.toLocalDateTime(c);
            cell.setCellValue(ldt);
            assertEquals(ldt, cell.getLocalDateTimeCellValue());
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.BOOLEAN, CellType.STRING,
                                        CellType.FORMULA, CellType.ERROR);

            LocalDate ld = ldt.toLocalDate();
            cell.setCellValue(ld);
            assertEquals(ld, cell.getLocalDateTimeCellValue().toLocalDate());
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.BOOLEAN, CellType.STRING,
                                        CellType.FORMULA, CellType.ERROR);

            cell.setCellValue((String)null);
            assertEquals("", cell.getStringCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.setCellValue((LocalDate)null);
            assertNull(cell.getLocalDateTimeCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.setCellValue((LocalDateTime)null);
            assertNull(cell.getLocalDateTimeCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.setCellErrorValue(FormulaError.NA.getCode());
            assertEquals(FormulaError.NA.getCode(), cell.getErrorCellValue());
            assertEquals(CellType.ERROR, cell.getCellType());
            assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.BOOLEAN,
                                        CellType.FORMULA, CellType.STRING);
        }
    }

    private static void assertProhibitedValueAccess(Cell cell, CellType ... types) {
        for(CellType type : types){
            try {
                switch (type) {
                    case NUMERIC:
                        cell.getNumericCellValue();
                        break;
                    case STRING:
                        cell.getStringCellValue();
                        break;
                    case BOOLEAN:
                        cell.getBooleanCellValue();
                        break;
                    case FORMULA:
                        cell.getCellFormula();
                        break;
                    case ERROR:
                        cell.getErrorCellValue();
                        break;
                    default:
                        fail("Should get exception when reading cell type (" + type + ").");
                }

            } catch (IllegalStateException e){
                // expected during successful test
                assertTrue(e.getMessage().startsWith("Cannot get a"));
            }
        }
    }

    /**
     * test that Boolean (BoolErrRecord) are supported properly.
     */
    @Test
    void testBool() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet s = wb1.createSheet("testSheet1");
            Row r;
            Cell c;
            // B1
            r = s.createRow(0);
            c = r.createCell(1);
            assertEquals(0, c.getRowIndex());
            assertEquals(1, c.getColumnIndex());
            c.setCellValue(true);
            assertTrue(c.getBooleanCellValue(), "B1 value");

            // C1
            c = r.createCell(2);
            assertEquals(0, c.getRowIndex());
            assertEquals(2, c.getColumnIndex());
            c.setCellValue(false);
            assertFalse(c.getBooleanCellValue(), "C1 value");

            // Make sure values are saved and re-read correctly.
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                s = wb2.getSheet("testSheet1");
                r = s.getRow(0);
                assertEquals(2, r.getPhysicalNumberOfCells(), "Row 1 should have 2 cells");

                c = r.getCell(1);
                assertEquals(0, c.getRowIndex());
                assertEquals(1, c.getColumnIndex());
                assertEquals(CellType.BOOLEAN, c.getCellType());
                assertTrue(c.getBooleanCellValue(), "B1 value");

                c = r.getCell(2);
                assertEquals(0, c.getRowIndex());
                assertEquals(2, c.getColumnIndex());
                assertEquals(CellType.BOOLEAN, c.getCellType());
                assertFalse(c.getBooleanCellValue(), "C1 value");
            }
        }
    }

    /**
     * test that Error types (BoolErrRecord) are supported properly.
     * @see #testBool
     */
    @Test
    void testErr() throws IOException {

        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet s = wb1.createSheet("testSheet1");
            Row r;
            Cell c;

            // B1
            r = s.createRow(0);
            c = r.createCell(1);
            assertEquals(0, c.getRowIndex());
            assertEquals(1, c.getColumnIndex());
            c.setCellErrorValue(FormulaError.NULL.getCode());
            assertEquals(FormulaError.NULL.getCode(), c.getErrorCellValue(), "B1 value == #NULL!");

            // C1
            c = r.createCell(2);
            assertEquals(0, c.getRowIndex());
            assertEquals(2, c.getColumnIndex());
            c.setCellErrorValue(FormulaError.DIV0.getCode());
            assertEquals(FormulaError.DIV0.getCode(), c.getErrorCellValue(), "C1 value == #DIV/0!");

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                s = wb2.getSheet("testSheet1");

                r = s.getRow(0);
                assertEquals(2, r.getPhysicalNumberOfCells(), "Row 1 should have 2 cells");

                c = r.getCell(1);
                assertEquals(0, c.getRowIndex());
                assertEquals(1, c.getColumnIndex());
                assertEquals(CellType.ERROR, c.getCellType());
                assertEquals(FormulaError.NULL.getCode(), c.getErrorCellValue(), "B1 value == #NULL!");

                c = r.getCell(2);
                assertEquals(0, c.getRowIndex());
                assertEquals(2, c.getColumnIndex());
                assertEquals(CellType.ERROR, c.getCellType());
                assertEquals(FormulaError.DIV0.getCode(), c.getErrorCellValue(), "C1 value == #DIV/0!");
            }
        }
    }

    /**
     * test that Cell Styles being applied to formulas remain intact
     */
    @Test
    void testFormulaStyle() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet s = wb1.createSheet("testSheet1");
            Row r;
            Cell c;
            CellStyle cs = wb1.createCellStyle();
            Font f = wb1.createFont();
            f.setFontHeightInPoints((short) 20);
            f.setColor(IndexedColors.RED.getIndex());
            f.setBold(true);
            f.setFontName("Arial Unicode MS");
            cs.setFillBackgroundColor((short) 3);
            cs.setFont(f);
            cs.setBorderTop(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderBottom(BorderStyle.THIN);

            r = s.createRow(0);
            c = r.createCell(0);
            c.setCellStyle(cs);
            c.setCellFormula("2*3");

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                s = wb2.getSheetAt(0);
                r = s.getRow(0);
                c = r.getCell(0);

                assertEquals(CellType.FORMULA, c.getCellType(), "Formula Cell at 0,0");
                cs = c.getCellStyle();

                assertNotNull(cs, "Formula Cell Style");
                assertEquals(f.getIndex(), cs.getFontIndex(), "Font Index Matches");
                assertEquals(BorderStyle.THIN, cs.getBorderTop(), "Top Border");
                assertEquals(BorderStyle.THIN, cs.getBorderLeft(), "Left Border");
                assertEquals(BorderStyle.THIN, cs.getBorderRight(), "Right Border");
                assertEquals(BorderStyle.THIN, cs.getBorderBottom(), "Bottom Border");
            }
        }
    }

    /**tests the toString() method of HSSFCell*/
    @Test
    void testToString() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Row r = wb1.createSheet("Sheet1").createRow(0);
            CreationHelper factory = wb1.getCreationHelper();

            r.createCell(0).setCellValue(false);
            r.createCell(1).setCellValue(true);
            r.createCell(2).setCellValue(1.5);
            r.createCell(3).setCellValue(factory.createRichTextString("Astring"));
            r.createCell(4).setCellErrorValue(FormulaError.DIV0.getCode());
            r.createCell(5).setCellFormula("A1+B1");
            r.createCell(6); // blank

            // create date-formatted cell
            Calendar c = LocaleUtil.getLocaleCalendar();
            c.set(2010, Calendar.FEBRUARY, 2, 0, 0, 0);
            r.createCell(7).setCellValue(c);
            CellStyle dateStyle = wb1.createCellStyle();
            short formatId = wb1.getCreationHelper().createDataFormat().getFormat("m/d/yy h:mm"); // any date format will do
            dateStyle.setDataFormat(formatId);
            r.getCell(7).setCellStyle(dateStyle);

            assertEquals("FALSE", r.getCell(0).toString(), "Boolean");
            assertEquals("TRUE", r.getCell(1).toString(), "Boolean");
            assertEquals("1.5", r.getCell(2).toString(), "Numeric");
            assertEquals("Astring", r.getCell(3).toString(), "String");
            assertEquals("#DIV/0!", r.getCell(4).toString(), "Error");
            assertEquals("A1+B1", r.getCell(5).toString(), "Formula");
            assertEquals("", r.getCell(6).toString(), "Blank");
            // toString on a date-formatted cell displays dates as dd-MMM-yyyy, which has locale problems with the month
            String dateCell1 = r.getCell(7).toString();
            assertTrue(dateCell1.startsWith("02-"), "Date (Day)");
            assertTrue(dateCell1.endsWith("-2010"), "Date (Year)");


            //Write out the file, read it in, and then check cell values
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                r = wb2.getSheetAt(0).getRow(0);
                assertEquals("FALSE", r.getCell(0).toString(), "Boolean");
                assertEquals("TRUE", r.getCell(1).toString(), "Boolean");
                assertEquals("1.5", r.getCell(2).toString(), "Numeric");
                assertEquals("Astring", r.getCell(3).toString(), "String");
                assertEquals("#DIV/0!", r.getCell(4).toString(), "Error");
                assertEquals("A1+B1", r.getCell(5).toString(), "Formula");
                assertEquals("", r.getCell(6).toString(), "Blank");
                String dateCell2 = r.getCell(7).toString();
                assertEquals(dateCell1, dateCell2, "Date");
            }
        }
    }

    /**
     *  Test that setting cached formula result keeps the cell type
     */
    @Test
    void testSetFormulaValue() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);

            Cell c1 = r.createCell(0);
            c1.setCellFormula("NA()");
            assertEquals(0.0, c1.getNumericCellValue(), 0.0);
            assertEquals(CellType.NUMERIC, c1.getCachedFormulaResultType());
            c1.setCellValue(10);
            assertEquals(10.0, c1.getNumericCellValue(), 0.0);
            assertEquals(CellType.FORMULA, c1.getCellType());
            assertEquals(CellType.NUMERIC, c1.getCachedFormulaResultType());

            Cell c2 = r.createCell(1);
            c2.setCellFormula("NA()");
            assertEquals(0.0, c2.getNumericCellValue(), 0.0);
            assertEquals(CellType.NUMERIC, c2.getCachedFormulaResultType());
            c2.setCellValue("I changed!");
            assertEquals("I changed!", c2.getStringCellValue());
            assertEquals(CellType.FORMULA, c2.getCellType());
            assertEquals(CellType.STRING, c2.getCachedFormulaResultType());

            //calglin Cell.setCellFormula(null) for a non-formula cell
            Cell c3 = r.createCell(2);
            c3.setCellFormula(null);
            assertEquals(CellType.BLANK, c3.getCellType());
        }

    }

    private Cell createACell(Workbook wb) {
        return wb.createSheet("Sheet1").createRow(0).createCell(0);
    }

    /**
     * bug 58452: Copy cell formulas containing unregistered function names
     * Make sure that formulas with unknown/unregistered UDFs can be written to and read back from a file.
     */
    @Test
    void testFormulaWithUnknownUDF() throws IOException {
        try (final Workbook wb1 = _testDataProvider.createWorkbook()) {
            final FormulaEvaluator evaluator1 = wb1.getCreationHelper().createFormulaEvaluator();
            final Cell cell1 = wb1.createSheet().createRow(0).createCell(0);
            final String formula = "myFunc(\"arg\")";
            cell1.setCellFormula(formula);
            confirmFormulaWithUnknownUDF(formula, cell1, evaluator1);

            try (final Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                final FormulaEvaluator evaluator2 = wb2.getCreationHelper().createFormulaEvaluator();
                final Cell cell2 = wb2.getSheetAt(0).getRow(0).getCell(0);
                confirmFormulaWithUnknownUDF(formula, cell2, evaluator2);
            }
        }
    }

    private static void confirmFormulaWithUnknownUDF(String expectedFormula, Cell cell, FormulaEvaluator evaluator) {
        assertEquals(expectedFormula, cell.getCellFormula());
        assertThrows(NotImplementedException.class, () -> evaluator.evaluate(cell));
    }

    @Test
    void testChangeTypeStringToBool() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = createACell(wb);

            cell.setCellValue("TRUE");
            assertEquals(CellType.STRING, cell.getCellType());
            // test conversion of cell from text to boolean
            cell.setCellType(CellType.BOOLEAN);

            assertEquals(CellType.BOOLEAN, cell.getCellType());
            assertTrue(cell.getBooleanCellValue());
            cell.setCellType(CellType.STRING);
            assertEquals("TRUE", cell.getRichStringCellValue().getString());

            // 'false' text to bool and back
            cell.setCellValue("FALSE");
            cell.setCellType(CellType.BOOLEAN);
            assertEquals(CellType.BOOLEAN, cell.getCellType());
            assertFalse(cell.getBooleanCellValue());
            cell.setCellType(CellType.STRING);
            assertEquals("FALSE", cell.getRichStringCellValue().getString());
        }
    }

    @Test
    void testChangeTypeBoolToString() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = createACell(wb);

            cell.setCellValue(true);
            // test conversion of cell from boolean to text
            cell.setCellType(CellType.STRING);
            assertEquals("TRUE", cell.getRichStringCellValue().getString());
        }
    }

    @Test
    void testChangeTypeErrorToNumber() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = createACell(wb);
            cell.setCellErrorValue(FormulaError.NAME.getCode());
            // ClassCastException -> Identified bug 46479b
            cell.setCellValue(2.5);
            assertEquals(2.5, cell.getNumericCellValue(), 0.0);
        }
    }

    @Test
    void testChangeTypeErrorToBoolean() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);
        cell.setCellErrorValue(FormulaError.NAME.getCode());
        cell.setCellValue(true);
        // Identify bug 46479c
        assertTrue(cell.getBooleanCellValue());

        wb.close();
    }

    /**
     * Test for a bug observed around svn r886733 when using
     * {@link FormulaEvaluator#evaluateInCell(Cell)} with a
     * string result type.
     */
    @Test
    void testConvertStringFormulaCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cellA1 = createACell(wb);
            cellA1.setCellFormula("\"abc\"");

            // default cached formula result is numeric zero
            assertEquals(0.0, cellA1.getNumericCellValue(), 0.0);

            FormulaEvaluator fe = cellA1.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

            fe.evaluateFormulaCell(cellA1);
            assertEquals("abc", cellA1.getStringCellValue());

            fe.evaluateInCell(cellA1);
            assertFalse(cellA1.getStringCellValue().isEmpty(), "Identified bug with writing back formula result of type string");
            assertEquals("abc", cellA1.getStringCellValue());
        }
    }

    /**
     * similar to {@link #testConvertStringFormulaCell()} but checks at a
     * lower level that {#link {@link Cell#setCellType(CellType)} works properly
     */
    @Test
    void testSetTypeStringOnFormulaCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cellA1 = createACell(wb);
            FormulaEvaluator fe = cellA1.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

            cellA1.setCellFormula("\"DEF\"");
            fe.clearAllCachedResultValues();
            fe.evaluateFormulaCell(cellA1);
            assertEquals("DEF", cellA1.getStringCellValue());
            cellA1.setCellType(CellType.STRING);
            assertEquals("DEF", cellA1.getStringCellValue());

            cellA1.setCellFormula("25.061");
            fe.clearAllCachedResultValues();
            fe.evaluateFormulaCell(cellA1);
            confirmCannotReadString(cellA1);
            assertEquals(25.061, cellA1.getNumericCellValue(), 0.0);
            cellA1.setCellType(CellType.STRING);
            assertEquals("25.061", cellA1.getStringCellValue());

            cellA1.setCellFormula("TRUE");
            fe.clearAllCachedResultValues();
            fe.evaluateFormulaCell(cellA1);
            confirmCannotReadString(cellA1);
            assertTrue(cellA1.getBooleanCellValue());
            cellA1.setCellType(CellType.STRING);
            assertEquals("TRUE", cellA1.getStringCellValue());

            cellA1.setCellFormula("#NAME?");
            fe.clearAllCachedResultValues();
            fe.evaluateFormulaCell(cellA1);
            confirmCannotReadString(cellA1);
            assertEquals(FormulaError.NAME, forInt(cellA1.getErrorCellValue()));
            cellA1.setCellType(CellType.STRING);
            assertEquals("#NAME?", cellA1.getStringCellValue());
        }
    }

    private static void confirmCannotReadString(Cell cell) {
        assertProhibitedValueAccess(cell, CellType.STRING);
    }

    /**
     * Test for bug in convertCellValueToBoolean to make sure that formula results get converted
     */
    @Test
    void testChangeTypeFormulaToBoolean() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = createACell(wb);
            cell.setCellFormula("1=1");
            cell.setCellValue(true);
            cell.setCellType(CellType.BOOLEAN);
            assertTrue(cell.getBooleanCellValue(), "Identified bug 46479d");
            assertTrue(cell.getBooleanCellValue());
        }
    }

    /**
     * Bug 40296:      HSSFCell.setCellFormula throws
     *   ClassCastException if cell is created using HSSFRow.createCell(short column, int type)
     */
    @Test
    void test40296() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet workSheet = wb1.createSheet("Sheet1");
            Cell cell;
            Row row = workSheet.createRow(0);

            cell = row.createCell(0, CellType.NUMERIC);
            cell.setCellValue(1.0);
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(1.0, cell.getNumericCellValue(), 0.0);

            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(2.0);
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(2.0, cell.getNumericCellValue(), 0.0);

            cell = row.createCell(2, CellType.FORMULA);
            cell.setCellFormula("SUM(A1:B1)");
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("SUM(A1:B1)", cell.getCellFormula());

            //serialize and check again
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                row = wb2.getSheetAt(0).getRow(0);
                cell = row.getCell(0);
                assertEquals(CellType.NUMERIC, cell.getCellType());
                assertEquals(1.0, cell.getNumericCellValue(), 0.0);

                cell = row.getCell(1);
                assertEquals(CellType.NUMERIC, cell.getCellType());
                assertEquals(2.0, cell.getNumericCellValue(), 0.0);

                cell = row.getCell(2);
                assertEquals(CellType.FORMULA, cell.getCellType());
                assertEquals("SUM(A1:B1)", cell.getCellFormula());
            }
        }
    }

    @Test
    void testSetStringInFormulaCell_bug44606() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
            cell.setCellFormula("B1&C1");
            assertEquals(CellType.FORMULA, cell.getCellType());
            cell.setCellValue(wb.getCreationHelper().createRichTextString("hello"));
            assertEquals(CellType.FORMULA, cell.getCellType());
        }
    }

    /**
     *  Make sure that cell.setBlank() preserves the cell style
     */
    @Test
    void testSetBlank_bug47028() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            CellStyle style = wb.createCellStyle();
            Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
            cell.setCellStyle(style);
            int i1 = cell.getCellStyle().getIndex();
            cell.setBlank();
            int i2 = cell.getCellStyle().getIndex();
            assertEquals(i1, i2);
        }
    }

    /**
     * Excel's implementation of floating number arithmetic does not fully adhere to IEEE 754:
     *
     * From http://support.microsoft.com/kb/78113:
     *
     * <ul>
     * <li> Positive/Negative Infinities:
     *   Infinities occur when you divide by 0. Excel does not support infinities, rather,
     *   it gives a #DIV/0! error in these cases.
     * </li>
     * <li>
     *   Not-a-Number (NaN):
     *   NaN is used to represent invalid operations (such as infinity/infinity,
     *   infinity-infinity, or the square root of -1). NaNs allow a program to
     *   continue past an invalid operation. Excel instead immediately generates
     *   an error such as #NUM! or #DIV/0!.
     * </li>
     * </ul>
     */
    @Test
    void testNanAndInfinity() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet workSheet = wb1.createSheet("Sheet1");
            Row row = workSheet.createRow(0);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(Double.NaN);
            assertEquals(CellType.ERROR, cell0.getCellType(), "Double.NaN should change cell type to CellType#ERROR");
            assertEquals(FormulaError.NUM, forInt(cell0.getErrorCellValue()), "Double.NaN should change cell value to #NUM!");

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(Double.POSITIVE_INFINITY);
            assertEquals(CellType.ERROR, cell1.getCellType(), "Double.POSITIVE_INFINITY should change cell type to CellType#ERROR");
            assertEquals(FormulaError.DIV0, forInt(cell1.getErrorCellValue()), "Double.POSITIVE_INFINITY should change cell value to #DIV/0!");

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(Double.NEGATIVE_INFINITY);
            assertEquals(CellType.ERROR, cell2.getCellType(), "Double.NEGATIVE_INFINITY should change cell type to CellType#ERROR");
            assertEquals(FormulaError.DIV0, forInt(cell2.getErrorCellValue()), "Double.NEGATIVE_INFINITY should change cell value to #DIV/0!");

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                row = wb2.getSheetAt(0).getRow(0);

                cell0 = row.getCell(0);
                assertEquals(CellType.ERROR, cell0.getCellType());
                assertEquals(FormulaError.NUM, forInt(cell0.getErrorCellValue()));

                cell1 = row.getCell(1);
                assertEquals(CellType.ERROR, cell1.getCellType());
                assertEquals(FormulaError.DIV0, forInt(cell1.getErrorCellValue()));

                cell2 = row.getCell(2);
                assertEquals(CellType.ERROR, cell2.getCellType());
                assertEquals(FormulaError.DIV0, forInt(cell2.getErrorCellValue()));
            }
        }
    }

    @Test
    void testDefaultStyleProperties() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {

            Cell cell = wb1.createSheet("Sheet1").createRow(0).createCell(0);
            CellStyle style = cell.getCellStyle();

            assertTrue(style.getLocked());
            assertFalse(style.getHidden());
            assertEquals(0, style.getIndention());
            assertEquals(0, style.getFontIndex());
            assertEquals(HorizontalAlignment.GENERAL, style.getAlignment());
            assertEquals(0, style.getDataFormat());
            assertFalse(style.getWrapText());

            CellStyle style2 = wb1.createCellStyle();
            assertTrue(style2.getLocked());
            assertFalse(style2.getHidden());
            style2.setLocked(false);
            style2.setHidden(true);
            assertFalse(style2.getLocked());
            assertTrue(style2.getHidden());

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                cell = wb2.getSheetAt(0).getRow(0).getCell(0);
                style = cell.getCellStyle();
                assertFalse(style2.getLocked());
                assertTrue(style2.getHidden());
                assertTrue(style.getLocked());
                assertFalse(style.getHidden());

                style2.setLocked(true);
                style2.setHidden(false);
                assertTrue(style2.getLocked());
                assertFalse(style2.getHidden());
            }
        }
    }

    @Test
    void testBug55658SetNumericValue() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sh = wb1.createSheet();
            Row row = sh.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(23);

            cell.setCellValue("some");

            cell = row.createCell(1);
            cell.setCellValue(23);

            cell.setCellValue("24");

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                assertEquals("some", wb2.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
                assertEquals("24", wb2.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
            }
        }
    }

    @Test
    void testRemoveHyperlink() throws Exception {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sh = wb1.createSheet("test");
            Row row = sh.createRow(0);
            CreationHelper helper = wb1.getCreationHelper();

            Cell cell1 = row.createCell(1);
            Hyperlink link1 = helper.createHyperlink(HyperlinkType.URL);
            cell1.setHyperlink(link1);
            assertNotNull(cell1.getHyperlink());
            cell1.removeHyperlink();
            assertNull(cell1.getHyperlink());

            Cell cell2 = row.createCell(0);
            Hyperlink link2 = helper.createHyperlink(HyperlinkType.URL);
            cell2.setHyperlink(link2);
            assertNotNull(cell2.getHyperlink());
            cell2.setHyperlink(null);
            assertNull(cell2.getHyperlink());

            Cell cell3 = row.createCell(2);
            Hyperlink link3 = helper.createHyperlink(HyperlinkType.URL);
            link3.setAddress("https://poi.apache.org/");
            cell3.setHyperlink(link3);
            assertNotNull(cell3.getHyperlink());

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                assertNotNull(wb2);

                cell1 = wb2.getSheet("test").getRow(0).getCell(1);
                assertNull(cell1.getHyperlink());
                cell2 = wb2.getSheet("test").getRow(0).getCell(0);
                assertNull(cell2.getHyperlink());
                cell3 = wb2.getSheet("test").getRow(0).getCell(2);
                assertNotNull(cell3.getHyperlink());
            }
        }
    }

    /**
     * Cell with the formula that returns error must return error code(There was
     * an problem that cell could not return error value form formula cell).
     */
    @Test
    void testGetErrorCellValueFromFormulaCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellFormula("SQRT(-1)");
            wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(cell);
            assertEquals(36, cell.getErrorCellValue());
        }
    }

    @Test
    void testSetRemoveStyle() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            // different default style indexes for HSSF and XSSF/SXSSF
            CellStyle defaultStyle = wb.getCellStyleAt(wb instanceof HSSFWorkbook ? (short) 15 : (short) 0);

            // Starts out with the default style
            assertEquals(defaultStyle, cell.getCellStyle());

            // Create some styles, no change
            CellStyle style1 = wb.createCellStyle();
            CellStyle style2 = wb.createCellStyle();
            style1.setDataFormat((short) 2);
            style2.setDataFormat((short) 3);

            assertEquals(defaultStyle, cell.getCellStyle());

            // Apply one, changes
            cell.setCellStyle(style1);
            assertEquals(style1, cell.getCellStyle());

            // Apply the other, changes
            cell.setCellStyle(style2);
            assertEquals(style2, cell.getCellStyle());

            // Remove, goes back to default
            cell.setCellStyle(null);
            assertEquals(defaultStyle, cell.getCellStyle());

            // Add back, returns
            cell.setCellStyle(style2);
            assertEquals(style2, cell.getCellStyle());
        }
    }

    @Test
    void test57008() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet();

            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("row 0, cell 0 _x0046_ without changes");

            Cell cell1 = row0.createCell(1);
            cell1.setCellValue("row 0, cell 1 _x005fx0046_ with changes");

            Cell cell2 = row0.createCell(2);
            cell2.setCellValue("hgh_x0041_**_x0100_*_x0101_*_x0190_*_x0200_*_x0300_*_x0427_*");

            checkUnicodeValues(wb1);

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                checkUnicodeValues(wb2);
            }
        }
    }

    /**
     * Setting a cell value of a null RichTextString should set
     *  the cell to Blank, test case for 58558
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetCellValueNullRichTextString() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Cell cell = sheet.createRow(0).createCell(0);

            RichTextString nullStr = null;
            cell.setCellValue(nullStr);
            assertEquals("", cell.getStringCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());

            cell = sheet.createRow(0).createCell(1);
            cell.setCellValue(1.2d);
            assertEquals(CellType.NUMERIC, cell.getCellType());
            cell.setCellValue(nullStr);
            assertEquals("", cell.getStringCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());

            cell = sheet.createRow(0).createCell(1);
            cell.setCellValue(wb.getCreationHelper().createRichTextString("Test"));
            assertEquals(CellType.STRING, cell.getCellType());
            cell.setCellValue(nullStr);
            assertEquals("", cell.getStringCellValue());
            assertEquals(CellType.BLANK, cell.getCellType());
        }
    }

    private void checkUnicodeValues(Workbook wb) {
        assertEquals((wb instanceof HSSFWorkbook ? "row 0, cell 0 _x0046_ without changes" : "row 0, cell 0 F without changes"),
                wb.getSheetAt(0).getRow(0).getCell(0).toString());
        assertEquals((wb instanceof HSSFWorkbook ? "row 0, cell 1 _x005fx0046_ with changes" : "row 0, cell 1 _x005fx0046_ with changes"),
                wb.getSheetAt(0).getRow(0).getCell(1).toString());
        assertEquals((wb instanceof HSSFWorkbook ? "hgh_x0041_**_x0100_*_x0101_*_x0190_*_x0200_*_x0300_*_x0427_*" : "hghA**\u0100*\u0101*\u0190*\u0200*\u0300*\u0427*"),
                wb.getSheetAt(0).getRow(0).getCell(2).toString());
    }

    /**
     *  The maximum length of cell contents (text) is 32,767 characters.
     */
    @Test
    void testMaxTextLength() throws IOException{
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Cell cell = sheet.createRow(0).createCell(0);

            int maxlen = wb instanceof HSSFWorkbook ?
                    SpreadsheetVersion.EXCEL97.getMaxTextLength()
                    : SpreadsheetVersion.EXCEL2007.getMaxTextLength();
            assertEquals(32767, maxlen);

            StringBuilder b = new StringBuilder();

            // 32767 is okay
            for (int i = 0; i < maxlen; i++) {
                b.append("X");
            }
            cell.setCellValue(b.toString());

            b.append("X");
            // 32768 produces an invalid XLS file
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cell.setCellValue(b.toString()));
            assertEquals("The maximum length of cell contents (text) is 32767 characters", e.getMessage());
        }
    }

    /**
     * Tests that the setAsActiveCell and getActiveCell function pairs work together
     */
    @Test
    void setAsActiveCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell A1 = row.createCell(0);
            Cell B1 = row.createCell(1);

            A1.setAsActiveCell();
            assertEquals(A1.getAddress(), sheet.getActiveCell());

            B1.setAsActiveCell();
            assertEquals(B1.getAddress(), sheet.getActiveCell());
        }
    }

    @Test
    void getCellComment() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            CreationHelper factory = wb.getCreationHelper();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(1);

            // cell does not have a comment
            assertNull(cell.getCellComment());

            // add a cell comment
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + 1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 3);

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            Comment comment = drawing.createCellComment(anchor);
            RichTextString str = factory.createRichTextString("Hello, World!");
            comment.setString(str);
            comment.setAuthor("Apache POI");
            cell.setCellComment(comment);
            // ideally assertSame, but XSSFCell creates a new XSSFCellComment wrapping the same bean for every call to getCellComment.
            assertEquals(comment, cell.getCellComment());
        }
    }

    @Test
    void testSetErrorValue() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellFormula("A2");
            cell.setCellErrorValue(FormulaError.NAME.getCode());

            assertEquals(CellType.FORMULA, cell.getCellType(), "Should still be a formula even after we set an error value");
            assertEquals(CellType.ERROR, cell.getCachedFormulaResultType(), "Should still be a formula even after we set an error value");
            assertEquals("A2", cell.getCellFormula());
            assertThrows(IllegalStateException.class, cell::getNumericCellValue);
            assertThrows(IllegalStateException.class, cell::getStringCellValue);
            assertThrows(IllegalStateException.class, cell::getRichStringCellValue);
            assertThrows(IllegalStateException.class, cell::getDateCellValue);

            assertEquals(FormulaError.NAME.getCode(), cell.getErrorCellValue());
            assertNull(cell.getHyperlink());
        }
    }

    @Test
    void test62216() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell instance = wb.createSheet().createRow(0).createCell(0);
            String formula = "2";
            instance.setCellFormula(formula);
            instance.setCellErrorValue(FormulaError.NAME.getCode());

            assertEquals(formula, instance.getCellFormula());
        }
    }

    @Test
    void testSetNullValues() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = wb.createSheet("test").createRow(0).createCell(0);

            cell.setCellValue((Calendar) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            cell.setCellValue((Date) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            cell.setCellValue((String) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            cell.setCellValue((RichTextString) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            cell.setCellValue((String) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());
        }
    }

    @Test
    void testFormulaSetValueDoesNotChangeType() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellFormula("SQRT(-1)");

            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue(new Date());
            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue(GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ROOT));
            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue(1.0);
            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue("test");
            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue(wb.getCreationHelper().createRichTextString("test"));
            assertEquals(CellType.FORMULA, cell.getCellType());

            cell.setCellValue(false);
            assertEquals(CellType.FORMULA, cell.getCellType());
        }
    }

    @Test
    void testGetNumericCellValueOnABlankCellReturnsZero() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Cell cell = workbook.createSheet().createRow(0).createCell(0);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals(0, cell.getNumericCellValue(), 0);
        }
    }

    @Test
    void getDateCellValue_returnsNull_onABlankCell() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Cell cell = workbook.createSheet().createRow(0).createCell(0);
            assertEquals(CellType.BLANK, cell.getCellType());
            Date result = cell.getDateCellValue();
            assertNull(result);
        }
    }

    @Test
    void getBooleanCellValue_returnsFalse_onABlankCell() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Cell cell = workbook.createSheet().createRow(0).createCell(0);
            assertEquals(CellType.BLANK, cell.getCellType());
            boolean result = cell.getBooleanCellValue();
            assertFalse(result);
        }
    }

    @Test
    void setStringCellValue_ifThrows_shallNotChangeCell() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Cell cell = workbook.createSheet().createRow(0).createCell(0);

            final double value = 2.78;
            cell.setCellValue(value);
            assertEquals(CellType.NUMERIC, cell.getCellType());

            int badLength = cell.getSheet().getWorkbook().getSpreadsheetVersion().getMaxTextLength() + 1;
            String badStringValue = new String(new byte[badLength], StandardCharsets.UTF_8);

            try {
                cell.setCellValue(badStringValue);
            } catch (IllegalArgumentException e) {
                // no-op, expected to throw but we need to assert something more
            }

            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(value, cell.getNumericCellValue(), 0);
        }
    }

    @Test
    void setStringCellValueWithRichTextString_ifThrows_shallNotChangeCell() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Cell cell = workbook.createSheet().createRow(0).createCell(0);

            final double value = 2.78;
            cell.setCellValue(value);
            assertEquals(CellType.NUMERIC, cell.getCellType());

            int badLength = cell.getSheet().getWorkbook().getSpreadsheetVersion().getMaxTextLength() + 1;
            RichTextString badStringValue = cell.getSheet().getWorkbook().getCreationHelper().
                    createRichTextString(new String(new byte[badLength], StandardCharsets.UTF_8));

            try {
                cell.setCellValue(badStringValue);
            } catch (IllegalArgumentException e) {
                // no-op, expected to throw but we need to assert something more
            }

            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(value, cell.getNumericCellValue(), 0);
        }
    }

    @Test
    void setCellType_null_throwsIAE() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            assertThrows(IllegalArgumentException.class, () -> cell.setCellType(null));
        }
    }

    @Test
    void setCellType_NONE_throwsIAE() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            assertThrows(IllegalArgumentException.class, () -> cell.setCellType(CellType._NONE));
        }
    }


    @Test
    protected void setBlank_removesArrayFormula_ifCellIsPartOfAnArrayFormulaGroupContainingOnlyThisCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);

            cell.getSheet().setArrayFormula("1", CellRangeAddress.valueOf("A1"));
            cell.setCellValue("foo");
            assertTrue(cell.isPartOfArrayFormulaGroup());
            assertEquals("1", cell.getCellFormula());

            cell.setBlank();

            assertEquals(CellType.BLANK, cell.getCellType());
            assertFalse(cell.isPartOfArrayFormulaGroup());
        }
    }

    @Test
    protected void setBlank_throwsISE_ifCellIsPartOfAnArrayFormulaGroupContainingOtherCells() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.getSheet().setArrayFormula("1", CellRangeAddress.valueOf("A1:B1"));
            cell.setCellValue("foo");
            assertThrows(IllegalStateException.class, cell::setBlank);
        }
    }

    @Test
    protected void setCellFormula_throwsISE_ifCellIsPartOfAnArrayFormulaGroupContainingOtherCells() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);

            cell.getSheet().setArrayFormula("1", CellRangeAddress.valueOf("A1:B1"));
            assertTrue(cell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, cell.getCellType());

            assertThrows(IllegalStateException.class, () -> cell.setCellFormula("1"));
        }
    }

    @Test
    void removeFormula_preservesValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);

            cell.setCellFormula("#DIV/0!");
            cell.setCellValue(true);
            cell.removeFormula();
            assertEquals(CellType.BOOLEAN, cell.getCellType());
            assertTrue(cell.getBooleanCellValue());

            cell.setCellFormula("#DIV/0!");
            cell.setCellValue(2);
            cell.removeFormula();
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(2, cell.getNumericCellValue(), 0);

            cell.setCellFormula("#DIV/0!");
            cell.setCellValue("foo");
            cell.removeFormula();
            assertEquals(CellType.STRING, cell.getCellType());
            assertEquals("foo", cell.getStringCellValue());

            cell.setCellFormula("#DIV/0!");
            cell.setCellErrorValue(FormulaError.NUM.getCode());
            cell.removeFormula();
            assertEquals(CellType.ERROR, cell.getCellType());
            assertEquals(FormulaError.NUM.getCode(), cell.getErrorCellValue());
        }
    }

    @Test
    protected void removeFormula_turnsCellToBlank_whenFormulaWasASingleCellArrayFormula() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);

            cell.getSheet().setArrayFormula("#DIV/0!", CellRangeAddress.valueOf("A1"));
            cell.setCellValue(true);
            cell.removeFormula();
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.getSheet().setArrayFormula("#DIV/0!", CellRangeAddress.valueOf("A1"));
            cell.setCellValue(2);
            cell.removeFormula();
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.getSheet().setArrayFormula("#DIV/0!", CellRangeAddress.valueOf("A1"));
            cell.setCellValue(true);
            cell.removeFormula();
            assertEquals(CellType.BLANK, cell.getCellType());

            cell.getSheet().setArrayFormula("#DIV/0!", CellRangeAddress.valueOf("A1"));
            cell.setCellErrorValue(FormulaError.NUM.getCode());
            cell.removeFormula();
            assertEquals(CellType.BLANK, cell.getCellType());
        }
    }

    @Test
    void setCellFormula_onABlankCell_setsValueToZero() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.setCellFormula("\"foo\"");
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
            assertEquals(0, cell.getNumericCellValue(), 0);
        }
    }


    @Test
    void setCellFormula_onANonBlankCell_preservesTheValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.setCellValue(true);
            cell.setCellFormula("\"foo\"");
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType());
            assertTrue(cell.getBooleanCellValue(), "Expected a boolean cell-value, but had 'false'");
        }
    }

    @Test
    void setCellFormula_onAFormulaCell_changeFormula_preservesTheValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.setCellFormula("\"foo\"");
            cell.setCellValue(true);
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType());
            assertTrue(cell.getBooleanCellValue());

            cell.setCellFormula("\"bar\"");
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType());
            assertTrue(cell.getBooleanCellValue(), "Expected a boolean cell-value, but had 'false'");
        }
    }

    @Test
    protected void setCellFormula_onASingleCellArrayFormulaCell_preservesTheValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.getSheet().setArrayFormula("\"foo\"", CellRangeAddress.valueOf("A1"));
            cell.setCellValue(true);

            assertTrue(cell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType());
            assertTrue(cell.getBooleanCellValue());

            cell.getSheet().setArrayFormula("\"bar\"", CellRangeAddress.valueOf("A1"));
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType(),
                "Expected a boolean cell-value, but had " + cell.getCachedFormulaResultType());
            assertTrue(cell.getBooleanCellValue(),
                "Expected a boolean cell-value, but had 'false'");
        }
    }

    @Test
    void setCellType_FORMULA_onANonFormulaCell_throwsIllegalArgumentException() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            assertThrows(IllegalArgumentException.class, () -> cell.setCellType(CellType.FORMULA));
        }
    }

    @Test
    void setCellType_FORMULA_onAFormulaCell_doesNothing() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.setCellFormula("3");
            cell.setCellValue("foo");

            cell.setCellType(CellType.FORMULA);

            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.STRING, cell.getCachedFormulaResultType());
            assertEquals("foo", cell.getStringCellValue());
        }
    }

    @Test
    void setCellType_FORMULA_onAnArrayFormulaCell_doesNothing() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Cell cell = getInstance(wb);
            cell.getSheet().setArrayFormula("3", CellRangeAddress.valueOf("A1:A2"));
            cell.setCellValue("foo");

            cell.setCellType(CellType.FORMULA);

            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals(CellType.STRING, cell.getCachedFormulaResultType());
            assertEquals("foo", cell.getStringCellValue());
        }
    }

    @Test
    public final void setBlank_delegatesTo_setCellType_BLANK() {
        Cell cell = mock(CellBase.class);
        doCallRealMethod().when(cell).setBlank();

        cell.setBlank();

        verify(cell).setBlank();
    }

    private Cell getInstance(Workbook wb) {
        return wb.createSheet().createRow(0).createCell(0);
    }
}
