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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

/**
 * Common superclass for testing implementations of
 *  {@link org.apache.poi.ss.usermodel.Cell}
 */
public abstract class BaseTestCell {

    protected final ITestDataProvider _testDataProvider;

    /**
     * @param testDataProvider an object that provides test data in HSSF / XSSF specific way
     */
    protected BaseTestCell(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    public void testSetValues() throws Exception {
        Workbook book = _testDataProvider.createWorkbook();
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
        assertEquals(false, cell.getBooleanCellValue());
        assertEquals(CellType.BOOLEAN, cell.getCellType());
        cell.setCellValue(true);
        assertEquals(true, cell.getBooleanCellValue());
        assertProhibitedValueAccess(cell, CellType.NUMERIC, CellType.STRING,
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
        
        book.close();
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
    public void testBool() throws IOException {

        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s = wb1.createSheet("testSheet1");
        Row r;
        Cell c;
        // B1
        r = s.createRow(0);
        c=r.createCell(1);
        assertEquals(0, c.getRowIndex());
        assertEquals(1, c.getColumnIndex());
        c.setCellValue(true);
        assertEquals("B1 value", true, c.getBooleanCellValue());

        // C1
        c=r.createCell(2);
        assertEquals(0, c.getRowIndex());
        assertEquals(2, c.getColumnIndex());
        c.setCellValue(false);
        assertEquals("C1 value", false, c.getBooleanCellValue());

        // Make sure values are saved and re-read correctly.
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        
        s = wb2.getSheet("testSheet1");
        r = s.getRow(0);
        assertEquals("Row 1 should have 2 cells", 2, r.getPhysicalNumberOfCells());
        
        c = r.getCell(1);
        assertEquals(0, c.getRowIndex());
        assertEquals(1, c.getColumnIndex());
        assertEquals(CellType.BOOLEAN, c.getCellType());
        assertEquals("B1 value", true, c.getBooleanCellValue());
        
        c = r.getCell(2);
        assertEquals(0, c.getRowIndex());
        assertEquals(2, c.getColumnIndex());
        assertEquals(CellType.BOOLEAN, c.getCellType());
        assertEquals("C1 value", false, c.getBooleanCellValue());
        
        wb2.close();
    }
    
    /**
     * test that Error types (BoolErrRecord) are supported properly.
     * @see #testBool
     */
    @Test
    public void testErr() throws IOException {

        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s = wb1.createSheet("testSheet1");
        Row r;
        Cell c;

        // B1
        r = s.createRow(0);
        c=r.createCell(1);
        assertEquals(0, c.getRowIndex());
        assertEquals(1, c.getColumnIndex());
        c.setCellErrorValue(FormulaError.NULL.getCode());
        assertEquals("B1 value == #NULL!", FormulaError.NULL.getCode(), c.getErrorCellValue());

        // C1
        c=r.createCell(2);
        assertEquals(0, c.getRowIndex());
        assertEquals(2, c.getColumnIndex());
        c.setCellErrorValue(FormulaError.DIV0.getCode());
        assertEquals("C1 value == #DIV/0!", FormulaError.DIV0.getCode(), c.getErrorCellValue());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        s = wb2.getSheet("testSheet1");

        r = s.getRow(0);
        assertEquals("Row 1 should have 2 cells", 2, r.getPhysicalNumberOfCells());

        c = r.getCell(1);
        assertEquals(0, c.getRowIndex());
        assertEquals(1, c.getColumnIndex());
        assertEquals(CellType.ERROR, c.getCellType());
        assertEquals("B1 value == #NULL!", FormulaError.NULL.getCode(), c.getErrorCellValue());

        c = r.getCell(2);
        assertEquals(0, c.getRowIndex());
        assertEquals(2, c.getColumnIndex());
        assertEquals(CellType.ERROR, c.getCellType());
        assertEquals("C1 value == #DIV/0!", FormulaError.DIV0.getCode(), c.getErrorCellValue());

        wb2.close();
    }

    /**
     * test that Cell Styles being applied to formulas remain intact
     */
    @Test
    public void testFormulaStyle() throws Exception {

        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s = wb1.createSheet("testSheet1");
        Row r;
        Cell c;
        CellStyle cs = wb1.createCellStyle();
        Font f = wb1.createFont();
        f.setFontHeightInPoints((short) 20);
        f.setColor(IndexedColors.RED.getIndex());
        f.setBold(true);
        f.setFontName("Arial Unicode MS");
        cs.setFillBackgroundColor((short)3);
        cs.setFont(f);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);

        r = s.createRow(0);
        c=r.createCell(0);
        c.setCellStyle(cs);
        c.setCellFormula("2*3");

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        s = wb2.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);

        assertEquals("Formula Cell at 0,0", CellType.FORMULA, c.getCellType());
        cs = c.getCellStyle();

        assertNotNull("Formula Cell Style", cs);
        assertEquals("Font Index Matches", f.getIndexAsInt(), cs.getFontIndex());
        assertEquals("Top Border", BorderStyle.THIN, cs.getBorderTop());
        assertEquals("Left Border", BorderStyle.THIN, cs.getBorderLeft());
        assertEquals("Right Border", BorderStyle.THIN, cs.getBorderRight());
        assertEquals("Bottom Border", BorderStyle.THIN, cs.getBorderBottom());
        wb2.close();
    }

    /**tests the toString() method of HSSFCell*/
    @Test
    public void testToString() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
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
        c.set(2010, 01, 02, 00, 00, 00);
        r.createCell(7).setCellValue(c);
        CellStyle dateStyle = wb1.createCellStyle();
        short formatId = wb1.getCreationHelper().createDataFormat().getFormat("m/d/yy h:mm"); // any date format will do
        dateStyle.setDataFormat(formatId);
        r.getCell(7).setCellStyle(dateStyle);

        assertEquals("Boolean", "FALSE", r.getCell(0).toString());
        assertEquals("Boolean", "TRUE", r.getCell(1).toString());
        assertEquals("Numeric", "1.5", r.getCell(2).toString());
        assertEquals("String", "Astring", r.getCell(3).toString());
        assertEquals("Error", "#DIV/0!", r.getCell(4).toString());
        assertEquals("Formula", "A1+B1", r.getCell(5).toString());
        assertEquals("Blank", "", r.getCell(6).toString());
        // toString on a date-formatted cell displays dates as dd-MMM-yyyy, which has locale problems with the month
        String dateCell1 = r.getCell(7).toString();
        assertTrue("Date (Day)", dateCell1.startsWith("02-"));
        assertTrue("Date (Year)", dateCell1.endsWith("-2010"));


        //Write out the file, read it in, and then check cell values
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        r = wb2.getSheetAt(0).getRow(0);
        assertEquals("Boolean", "FALSE", r.getCell(0).toString());
        assertEquals("Boolean", "TRUE", r.getCell(1).toString());
        assertEquals("Numeric", "1.5", r.getCell(2).toString());
        assertEquals("String", "Astring", r.getCell(3).toString());
        assertEquals("Error", "#DIV/0!", r.getCell(4).toString());
        assertEquals("Formula", "A1+B1", r.getCell(5).toString());
        assertEquals("Blank", "", r.getCell(6).toString());
        String dateCell2 = r.getCell(7).toString();
        assertEquals("Date", dateCell1, dateCell2);
        wb2.close();
    }

    /**
     *  Test that setting cached formula result keeps the cell type
     */
    @Test
    public void testSetFormulaValue() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
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
        wb.close();

    }

    private Cell createACell(Workbook wb) {
        return wb.createSheet("Sheet1").createRow(0).createCell(0);
    }
    
    /**
     * bug 58452: Copy cell formulas containing unregistered function names
     * Make sure that formulas with unknown/unregistered UDFs can be written to and read back from a file.
     */
    @Test
    public void testFormulaWithUnknownUDF() throws IOException {
        final Workbook wb1 = _testDataProvider.createWorkbook();
        final FormulaEvaluator evaluator1 = wb1.getCreationHelper().createFormulaEvaluator();
        try {
            final Cell cell1 = wb1.createSheet().createRow(0).createCell(0);
            final String formula = "myFunc(\"arg\")";
            cell1.setCellFormula(formula);
            confirmFormulaWithUnknownUDF(formula, cell1, evaluator1);
            
            final Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
            final FormulaEvaluator evaluator2 = wb2.getCreationHelper().createFormulaEvaluator();
            try {
                final Cell cell2 = wb2.getSheetAt(0).getRow(0).getCell(0);
                confirmFormulaWithUnknownUDF(formula, cell2, evaluator2);
            } finally {
                wb2.close();
            }
        } finally {
            wb1.close();
        }
    }
    
    private static void confirmFormulaWithUnknownUDF(String expectedFormula, Cell cell, FormulaEvaluator evaluator) {
        assertEquals(expectedFormula, cell.getCellFormula());
        try {
            evaluator.evaluate(cell);
            fail("Expected NotImplementedFunctionException/NotImplementedException");
        } catch (final org.apache.poi.ss.formula.eval.NotImplementedException e) {
            // expected
        }
    }

    @Test
    public void testChangeTypeStringToBool() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);

        cell.setCellValue("TRUE");
        assertEquals(CellType.STRING, cell.getCellType());
        // test conversion of cell from text to boolean
        cell.setCellType(CellType.BOOLEAN);

        assertEquals(CellType.BOOLEAN, cell.getCellType());
        assertEquals(true, cell.getBooleanCellValue());
        cell.setCellType(CellType.STRING);
        assertEquals("TRUE", cell.getRichStringCellValue().getString());

        // 'false' text to bool and back
        cell.setCellValue("FALSE");
        cell.setCellType(CellType.BOOLEAN);
        assertEquals(CellType.BOOLEAN, cell.getCellType());
        assertEquals(false, cell.getBooleanCellValue());
        cell.setCellType(CellType.STRING);
        assertEquals("FALSE", cell.getRichStringCellValue().getString());
        
        wb.close();
    }

    @Test
    public void testChangeTypeBoolToString() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);

        cell.setCellValue(true);
        // test conversion of cell from boolean to text
        cell.setCellType(CellType.STRING);
        assertEquals("TRUE", cell.getRichStringCellValue().getString());
        
        wb.close();
    }

    @Test
    public void testChangeTypeErrorToNumber() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);
        cell.setCellErrorValue(FormulaError.NAME.getCode());
        try {
            cell.setCellValue(2.5);
        } catch (ClassCastException e) {
            fail("Identified bug 46479b");
        }
        assertEquals(2.5, cell.getNumericCellValue(), 0.0);
        
        wb.close();
    }

    @Test
    public void testChangeTypeErrorToBoolean() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);
        cell.setCellErrorValue(FormulaError.NAME.getCode());
        cell.setCellValue(true);
        // Identify bug 46479c
        assertEquals(true, cell.getBooleanCellValue());
        
        wb.close();
    }

    /**
     * Test for a bug observed around svn r886733 when using
     * {@link FormulaEvaluator#evaluateInCell(Cell)} with a
     * string result type.
     */
    @Test
    public void testConvertStringFormulaCell() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cellA1 = createACell(wb);
        cellA1.setCellFormula("\"abc\"");

        // default cached formula result is numeric zero
        assertEquals(0.0, cellA1.getNumericCellValue(), 0.0);

        FormulaEvaluator fe = cellA1.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        fe.evaluateFormulaCell(cellA1);
        assertEquals("abc", cellA1.getStringCellValue());

        fe.evaluateInCell(cellA1);
        assertFalse("Identified bug with writing back formula result of type string", cellA1.getStringCellValue().isEmpty());
        assertEquals("abc", cellA1.getStringCellValue());
        
        wb.close();
    }
    
    /**
     * similar to {@link #testConvertStringFormulaCell()} but checks at a
     * lower level that {#link {@link Cell#setCellType(CellType)} works properly
     */
    @Test
    public void testSetTypeStringOnFormulaCell() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

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
        assertEquals(true, cellA1.getBooleanCellValue());
        cellA1.setCellType(CellType.STRING);
        assertEquals("TRUE", cellA1.getStringCellValue());

        cellA1.setCellFormula("#NAME?");
        fe.clearAllCachedResultValues();
        fe.evaluateFormulaCell(cellA1);
        confirmCannotReadString(cellA1);
        assertEquals(FormulaError.NAME, forInt(cellA1.getErrorCellValue()));
        cellA1.setCellType(CellType.STRING);
        assertEquals("#NAME?", cellA1.getStringCellValue());
        
        wb.close();
    }

    private static void confirmCannotReadString(Cell cell) {
        assertProhibitedValueAccess(cell, CellType.STRING);
    }

    /**
     * Test for bug in convertCellValueToBoolean to make sure that formula results get converted
     */
    @Test
    public void testChangeTypeFormulaToBoolean() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = createACell(wb);
        cell.setCellFormula("1=1");
        cell.setCellValue(true);
        cell.setCellType(CellType.BOOLEAN);
        assertTrue("Identified bug 46479d", cell.getBooleanCellValue());
        assertEquals(true, cell.getBooleanCellValue());
        
        wb.close();
    }

    /**
     * Bug 40296:      HSSFCell.setCellFormula throws
     *   ClassCastException if cell is created using HSSFRow.createCell(short column, int type)
     */
    @Test
    public void test40296() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
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
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
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
        wb2.close();
    }

    @Test
    public void testSetStringInFormulaCell_bug44606() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
        cell.setCellFormula("B1&C1");
        cell.setCellValue(wb.getCreationHelper().createRichTextString("hello"));
        wb.close();
    }

    /**
     *  Make sure that cell.setCellType(CellType.BLANK) preserves the cell style
     */
    @Test
    public void testSetBlank_bug47028() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        CellStyle style = wb.createCellStyle();
        Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
        cell.setCellStyle(style);
        int i1 = cell.getCellStyle().getIndex();
        cell.setCellType(CellType.BLANK);
        int i2 = cell.getCellStyle().getIndex();
        assertEquals(i1, i2);
        wb.close();
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
    public void testNanAndInfinity() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet workSheet = wb1.createSheet("Sheet1");
        Row row = workSheet.createRow(0);

        Cell cell0 = row.createCell(0);
        cell0.setCellValue(Double.NaN);
        assertEquals("Double.NaN should change cell type to CellType#ERROR", CellType.ERROR, cell0.getCellType());
        assertEquals("Double.NaN should change cell value to #NUM!", FormulaError.NUM, forInt(cell0.getErrorCellValue()));

        Cell cell1 = row.createCell(1);
        cell1.setCellValue(Double.POSITIVE_INFINITY);
        assertEquals("Double.POSITIVE_INFINITY should change cell type to CellType#ERROR", CellType.ERROR, cell1.getCellType());
        assertEquals("Double.POSITIVE_INFINITY should change cell value to #DIV/0!", FormulaError.DIV0, forInt(cell1.getErrorCellValue()));

        Cell cell2 = row.createCell(2);
        cell2.setCellValue(Double.NEGATIVE_INFINITY);
        assertEquals("Double.NEGATIVE_INFINITY should change cell type to CellType#ERROR", CellType.ERROR, cell2.getCellType());
        assertEquals("Double.NEGATIVE_INFINITY should change cell value to #DIV/0!", FormulaError.DIV0, forInt(cell2.getErrorCellValue()));

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
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
        wb2.close();
    }

    @Test
    public void testDefaultStyleProperties() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();

        Cell cell = wb1.createSheet("Sheet1").createRow(0).createCell(0);
        CellStyle style = cell.getCellStyle();

        assertTrue(style.getLocked());
        assertFalse(style.getHidden());
        assertEquals(0, style.getIndention());
        assertEquals(0, style.getFontIndex());
        assertEquals(HorizontalAlignment.GENERAL, style.getAlignment());
        assertEquals(0, style.getDataFormat());
        assertEquals(false, style.getWrapText());

        CellStyle style2 = wb1.createCellStyle();
        assertTrue(style2.getLocked());
        assertFalse(style2.getHidden());
        style2.setLocked(false);
        style2.setHidden(true);
        assertFalse(style2.getLocked());
        assertTrue(style2.getHidden());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
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
        wb2.close();
    }

    @Test
    public void testBug55658SetNumericValue() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sh = wb1.createSheet();
        Row row = sh.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("some");

        cell = row.createCell(1);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("24");

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        assertEquals("some", wb2.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals("24", wb2.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
        wb2.close();
    }

    @Test
    public void testRemoveHyperlink() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
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
        link3.setAddress("http://poi.apache.org/");
        cell3.setHyperlink(link3);
        assertNotNull(cell3.getHyperlink());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        assertNotNull(wb2);
        
        cell1 = wb2.getSheet("test").getRow(0).getCell(1);
        assertNull(cell1.getHyperlink());
        cell2 = wb2.getSheet("test").getRow(0).getCell(0);
        assertNull(cell2.getHyperlink());
        cell3 = wb2.getSheet("test").getRow(0).getCell(2);
        assertNotNull(cell3.getHyperlink());
        wb2.close();
    }

    /**
     * Cell with the formula that returns error must return error code(There was
     * an problem that cell could not return error value form formula cell).
     */
    @Test
    public void testGetErrorCellValueFromFormulaCell() throws IOException {
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
    public void testSetRemoveStyle() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        // different default style indexes for HSSF and XSSF/SXSSF
        CellStyle defaultStyle = wb.getCellStyleAt(wb instanceof HSSFWorkbook ? (short)15 : (short)0);
        
        // Starts out with the default style
        assertEquals(defaultStyle, cell.getCellStyle());
        
        // Create some styles, no change
        CellStyle style1 = wb.createCellStyle();
        CellStyle style2 = wb.createCellStyle();
        style1.setDataFormat((short)2);
        style2.setDataFormat((short)3);
        
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
        
        wb.close();
    }

    @Test
    public void test57008() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet();
        
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("row 0, cell 0 _x0046_ without changes");
        
        Cell cell1 = row0.createCell(1);
        cell1.setCellValue("row 0, cell 1 _x005fx0046_ with changes");
        
        Cell cell2 = row0.createCell(2);
        cell2.setCellValue("hgh_x0041_**_x0100_*_x0101_*_x0190_*_x0200_*_x0300_*_x0427_*");

        checkUnicodeValues(wb1);
        
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        checkUnicodeValues(wb2);
        wb2.close();
        wb1.close();
    }

    /**
     * Setting a cell value of a null RichTextString should set
     *  the cell to Blank, test case for 58558
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSetCellValueNullRichTextString() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
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

        wb.close();
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
    public void testMaxTextLength() throws IOException{
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Cell cell = sheet.createRow(0).createCell(0);

        int maxlen = wb instanceof HSSFWorkbook ? 
                SpreadsheetVersion.EXCEL97.getMaxTextLength()
                : SpreadsheetVersion.EXCEL2007.getMaxTextLength();
        assertEquals(32767, maxlen);

        StringBuilder b = new StringBuilder() ;

        // 32767 is okay
        for( int i = 0 ; i < maxlen ; i++ )
        {
            b.append( "X" ) ;
        }
        cell.setCellValue(b.toString());

        b.append("X");
        // 32768 produces an invalid XLS file
        try {
            cell.setCellValue(b.toString());
            fail("Expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The maximum length of cell contents (text) is 32,767 characters", e.getMessage());
        }
        wb.close();
    }

    /**
     * Tests that the setAsActiveCell and getActiveCell function pairs work together
     */
    @Test
    public void setAsActiveCell() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);

        A1.setAsActiveCell();
        assertEquals(A1.getAddress(), sheet.getActiveCell());

        B1.setAsActiveCell();
        assertEquals(B1.getAddress(), sheet.getActiveCell());
        
        wb.close();
    }

    @Test
    public void getCellComment() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        CreationHelper factory = wb.getCreationHelper();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(1);
        
        // cell does not have a comment
        assertNull(cell.getCellComment());
 
        // add a cell comment
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex()+1);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum()+3);

        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("Hello, World!");
        comment.setString(str);
        comment.setAuthor("Apache POI");
        cell.setCellComment(comment);
        // ideally assertSame, but XSSFCell creates a new XSSFCellComment wrapping the same bean for every call to getCellComment.
        assertEquals(comment, cell.getCellComment());

        wb.close();
    }

    @Test
    public void testSetErrorValue() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellFormula("A2");
            cell.setCellErrorValue(FormulaError.NAME.getCode());

            assertEquals("Should still be a formula even after we set an error value",
                    CellType.FORMULA, cell.getCellType());
            assertEquals("Should still be a formula even after we set an error value",
                    CellType.ERROR, cell.getCachedFormulaResultType());
            assertEquals("A2", cell.getCellFormula());
            try {
                cell.getNumericCellValue();
                fail("Should catch exception here");
            } catch (IllegalStateException e) {
                // expected here
            }
            try {
                cell.getStringCellValue();
                fail("Should catch exception here");
            } catch (IllegalStateException e) {
                // expected here
            }
            try {
                cell.getRichStringCellValue();
                fail("Should catch exception here");
            } catch (IllegalStateException e) {
                // expected here
            }
            try {
                cell.getDateCellValue();
                fail("Should catch exception here");
            } catch (IllegalStateException e) {
                // expected here
            }
            assertEquals(FormulaError.NAME.getCode(), cell.getErrorCellValue());
            assertNull(cell.getHyperlink());
        }
    }
}
