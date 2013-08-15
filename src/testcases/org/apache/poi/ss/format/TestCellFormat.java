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
package org.apache.poi.ss.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import junit.framework.TestCase;

public class TestCellFormat extends TestCase {
    
    private static final String _255_POUND_SIGNS;
    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 255; i++) {
            sb.append('#');
        }
        _255_POUND_SIGNS = sb.toString();
    }
    
    public void testSome() {
        JLabel l = new JLabel();
        CellFormat fmt = CellFormat.getInstance(
                "\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)");
        fmt.apply(l, 1.1);
    }
    
    public void testPositiveFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(12.345);
        assertEquals("12.35", result.text);
    }
    
    public void testNegativeFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("-12.35", result.text);
    }
    
    public void testZeroFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(0.0);
        assertEquals("0.00", result.text);
    }
    
    public void testPositiveFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(12.345);
        assertEquals("12.35", result.text);
    }
    
    public void testNegativeFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("-12.35", result.text);
    }

    public void testNegativeFormatHasPosAndNegParts2() {
        CellFormat fmt = CellFormat.getInstance("0.00;(0.00)");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("(12.35)", result.text);
    }
    
    public void testZeroFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(0.0);
        assertEquals("0.00", result.text);
    }

    public void testFormatWithThreeSections() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00;-");
        
        assertEquals("12.35",  fmt.apply(12.345).text);
        assertEquals("-12.35", fmt.apply(-12.345).text);
        assertEquals("-",      fmt.apply(0.0).text);
        assertEquals("abc",    fmt.apply("abc").text);
    }
    
    public void testFormatWithFourSections() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00;-; @ ");
        
        assertEquals("12.35",  fmt.apply(12.345).text);
        assertEquals("-12.35", fmt.apply(-12.345).text);
        assertEquals("-",      fmt.apply(0.0).text);
        assertEquals(" abc ",  fmt.apply("abc").text);
    }

    public void testApplyCellForGeneralFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        Cell cell3 = row.createCell(3);
        Cell cell4 = row.createCell(4);
        
        CellFormat cf = CellFormat.getInstance("General");
        
        // case Cell.CELL_TYPE_BLANK
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("", result0.text);
        
        // case Cell.CELL_TYPE_BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals("TRUE", result1.text);
        
        // case Cell.CELL_TYPE_NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(cell2);
        assertEquals("1.23", result2.text);
        
        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(cell3);
        assertEquals("123", result3.text);
        
        // case Cell.CELL_TYPE_STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(cell4);
        assertEquals("abc", result4.text);
        
    }
    
    public void testApplyCellForAtFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        Cell cell3 = row.createCell(3);
        Cell cell4 = row.createCell(4);
        
        CellFormat cf = CellFormat.getInstance("@");
        
        // case Cell.CELL_TYPE_BLANK
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("", result0.text);
        
        // case Cell.CELL_TYPE_BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals("TRUE", result1.text);
        
        // case Cell.CELL_TYPE_NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(cell2);
        assertEquals("1.23", result2.text);
        
        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(cell3);
        assertEquals("123", result3.text);
        
        // case Cell.CELL_TYPE_STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(cell4);
        assertEquals("abc", result4.text);
        
    }
    
    public void testApplyCellForDateFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        
        CellFormat cf = CellFormat.getInstance("dd/mm/yyyy");
        
        cell0.setCellValue(10);
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("10/01/1900", result0.text);
        
        cell1.setCellValue(-1);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals(_255_POUND_SIGNS, result1.text);
        
    }
    
    public void testApplyCellForTimeFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        CellFormat cf = CellFormat.getInstance("hh:mm");
        
        cell.setCellValue(DateUtil.convertTime("03:04:05"));        
        CellFormatResult result = cf.apply(cell);
        assertEquals("03:04", result.text);
        
    }
    
   public void testApplyCellForDateFormatAndNegativeFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        
        CellFormat cf = CellFormat.getInstance("dd/mm/yyyy;(0)");
        
        cell0.setCellValue(10);
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("10/01/1900", result0.text);
        
        cell1.setCellValue(-1);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals("(1)", result1.text);
        
    }
    
    public void testApplyJLabelCellForGeneralFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        Cell cell3 = row.createCell(3);
        Cell cell4 = row.createCell(4);
        
        CellFormat cf = CellFormat.getInstance("General");
        
        JLabel label0 = new JLabel();
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        JLabel label4 = new JLabel();
        
        // case Cell.CELL_TYPE_BLANK
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("", result0.text);
        assertEquals("", label0.getText());
        
        // case Cell.CELL_TYPE_BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals("TRUE", result1.text);
        assertEquals("TRUE", label1.getText());
        
        // case Cell.CELL_TYPE_NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(label2, cell2);
        assertEquals("1.23", result2.text);
        assertEquals("1.23", label2.getText());
        
        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(label3, cell3);
        assertEquals("123", result3.text);
        assertEquals("123", label3.getText());
        
        // case Cell.CELL_TYPE_STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(label4, cell4);
        assertEquals("abc", result4.text);
        assertEquals("abc", label4.getText());
        
    }
    
    public void testApplyJLabelCellForAtFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        Cell cell3 = row.createCell(3);
        Cell cell4 = row.createCell(4);
        
        CellFormat cf = CellFormat.getInstance("@");
        
        JLabel label0 = new JLabel();
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        JLabel label4 = new JLabel();
        
        // case Cell.CELL_TYPE_BLANK
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("", result0.text);
        assertEquals("", label0.getText());
        
        // case Cell.CELL_TYPE_BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals("TRUE", result1.text);
        assertEquals("TRUE", label1.getText());
        
        // case Cell.CELL_TYPE_NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(label2, cell2);
        assertEquals("1.23", result2.text);
        assertEquals("1.23", label2.getText());
        
        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(label3, cell3);
        assertEquals("123", result3.text);
        assertEquals("123", label3.getText());
        
        // case Cell.CELL_TYPE_STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(label4, cell4);
        assertEquals("abc", result4.text);
        assertEquals("abc", label4.getText());
        
    }

    public void testApplyJLabelCellForDateFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        
        CellFormat cf = CellFormat.getInstance("dd/mm/yyyy");
        
        JLabel label0 = new JLabel();
        JLabel label1 = new JLabel();
        
        cell0.setCellValue(10);
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("10/01/1900", result0.text);
        assertEquals("10/01/1900", label0.getText());
        
        cell1.setCellValue(-1);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals(_255_POUND_SIGNS, result1.text);
        assertEquals(_255_POUND_SIGNS, label1.getText());
        
    }

    public void testApplyJLabelCellForTimeFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        CellFormat cf = CellFormat.getInstance("hh:mm");
        
        JLabel label = new JLabel();
        
        cell.setCellValue(DateUtil.convertTime("03:04:05"));        
        CellFormatResult result = cf.apply(label, cell);
        assertEquals("03:04", result.text);
        assertEquals("03:04", label.getText());
        
    }
    
    public void testApplyJLabelCellForDateFormatAndNegativeFormat() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        
        CellFormat cf = CellFormat.getInstance("dd/mm/yyyy;(0)");
        
        JLabel label0 = new JLabel();
        JLabel label1 = new JLabel();
        
        cell0.setCellValue(10);
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("10/01/1900", result0.text);
        assertEquals("10/01/1900", label0.getText());
        
        cell1.setCellValue(-1);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals("(1)", result1.text);
        assertEquals("(1)", label1.getText());
        
    }

    public void testApplyFormatHasOnePartAndPartHasCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10", cf.apply(cell).text);
        
        cell.setCellValue(0.123456789012345);
        assertEquals("0.123456789", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasTwoPartsFirstHasCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;0.000");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue(0.123456789012345);
        assertEquals("0.123", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.000", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10.000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
        cell.setCellValue("TRUE");
        assertEquals("TRUE", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasTwoPartsBothHaveCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;[>=10]0.000");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals(_255_POUND_SIGNS, cf.apply(cell).text);
        
        cell.setCellValue(-0.123456789012345);
        assertEquals(_255_POUND_SIGNS, cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals(_255_POUND_SIGNS, cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasThreePartsFirstHasCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;0.000;0.0000");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.0000", cf.apply(cell).text);
        
        cell.setCellValue(0.123456789012345);
        assertEquals("0.1235", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0000", cf.apply(cell).text);
        
        // Second format part ('0.000') is used for negative numbers
        // so result does not have a minus sign
        cell.setCellValue(-10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasThreePartsFirstTwoHaveCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;[>=10]0.000;0.0000");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0000", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10.0000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }

    public void testApplyFormatHasThreePartsFirstIsDateFirstTwoHaveCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;[>=10]dd/mm/yyyy;0.0");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10/01/1900", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10.0", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }

    public void testApplyFormatHasTwoPartsFirstHasConditionSecondIsGeneral() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;General");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }

    public void testApplyFormatHasThreePartsFirstTwoHaveConditionThirdIsGeneral() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;[>=10]0.000;General");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("abc", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasFourPartsFirstHasCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;0.000;0.0000;~~@~~");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.0000", cf.apply(cell).text);
        
        cell.setCellValue(0.123456789012345);
        assertEquals("0.1235", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0000", cf.apply(cell).text);
        
        // Second format part ('0.000') is used for negative numbers
        // so result does not have a minus sign
        cell.setCellValue(-10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("~~abc~~", cf.apply(cell).text);
        
    }
    
    public void testApplyFormatHasFourPartsSecondHasCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("0.00;[>=100]0.000;0.0000;~~@~~");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.00", cf.apply(cell).text);
        
        cell.setCellValue(0.123456789012345);
        assertEquals("0.12", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0000", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10.0000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("~~abc~~", cf.apply(cell).text);
        
        cell.setCellValue(true);
        assertEquals("~~TRUE~~", cf.apply(cell).text);
        
    }

    public void testApplyFormatHasFourPartsFirstTwoHaveCondition() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[>=100]0.00;[>=10]0.000;0.0000;~~@~~");
        
        cell.setCellValue(100);
        assertEquals("100.00", cf.apply(cell).text);
        
        cell.setCellValue(10);
        assertEquals("10.000", cf.apply(cell).text);
        
        cell.setCellValue(0);
        assertEquals("0.0000", cf.apply(cell).text);
        
        cell.setCellValue(-10);
        assertEquals("-10.0000", cf.apply(cell).text);
        
        cell.setCellValue("abc");
        assertEquals("~~abc~~", cf.apply(cell).text);
        
        cell.setCellValue(true);
        assertEquals("~~TRUE~~", cf.apply(cell).text);
    }
    
    /*
     * Test apply(Object value) with a number as parameter
     */
    public void testApplyObjectNumber() {
        
        CellFormat cf1 = CellFormat.getInstance("0.000");
        
        assertEquals("1.235", cf1.apply(1.2345).text);
        assertEquals("-1.235", cf1.apply(-1.2345).text);
        
        CellFormat cf2 = CellFormat.getInstance("0.000;(0.000)");
        
        assertEquals("1.235", cf2.apply(1.2345).text);
        assertEquals("(1.235)", cf2.apply(-1.2345).text);
        
        CellFormat cf3 = CellFormat.getInstance("[>1]0.000;0.0000");
        
        assertEquals("1.235", cf3.apply(1.2345).text);
        assertEquals("-1.2345", cf3.apply(-1.2345).text);
        
        CellFormat cf4 = CellFormat.getInstance("0.000;[>1]0.0000");
        
        assertEquals("1.235", cf4.apply(1.2345).text);
        assertEquals(_255_POUND_SIGNS, cf4.apply(-1.2345).text);
        
    }
    
    /*
     * Test apply(Object value) with a Date as parameter
     */
    public void testApplyObjectDate() throws ParseException {
        
        CellFormat cf1 = CellFormat.getInstance("m/d/yyyy");
        Date date1 = new SimpleDateFormat("M/d/y").parse("01/11/2012");
        assertEquals("1/11/2012", cf1.apply(date1).text);
        
    }

    public void testApplyCellForDateFormatWithConditions() {
        
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        CellFormat cf = CellFormat.getInstance("[<1]hh:mm:ss AM/PM;[>=1]dd/mm/yyyy hh:mm:ss AM/PM;General");
        
        cell.setCellValue(0.5);
        assertEquals("12:00:00 PM", cf.apply(cell).text);
        
        cell.setCellValue(1.5);
        assertEquals("01/01/1900 12:00:00 PM", cf.apply(cell).text);
        
        cell.setCellValue(-1);
        assertEquals(_255_POUND_SIGNS, cf.apply(cell).text);
        
    }
    
    /*
     * Test apply(Object value) with a String as parameter
     */
    public void testApplyObjectString() {
        
        CellFormat cf = CellFormat.getInstance("0.00");
        
        assertEquals("abc", cf.apply("abc").text);
        
    }
    
    /*
     * Test apply(Object value) with a Boolean as parameter
     */
    public void testApplyObjectBoolean() {
        
        CellFormat cf1 = CellFormat.getInstance("0");
        CellFormat cf2 = CellFormat.getInstance("General");
        CellFormat cf3 = CellFormat.getInstance("@");
        
        assertEquals("TRUE", cf1.apply(true).text);
        assertEquals("FALSE", cf2.apply(false).text);
        assertEquals("TRUE", cf3.apply(true).text);
        
    }
    
    public void testSimpleFractionFormat() {
        CellFormat cf1 = CellFormat.getInstance("# ?/?");
        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(123456.6);
        System.out.println(cf1.apply(cell).text);
        assertEquals("123456 3/5", cf1.apply(cell).text);
    }
}