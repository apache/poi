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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.swing.JLabel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestCellFormat {

    private static TimeZone userTimeZone;

    @BeforeAll
    public static void setTimeZone() {
        userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        LocaleUtil.setUserLocale(Locale.US);
    }

    @AfterAll
    public static void resetTimeZone() {
        LocaleUtil.setUserTimeZone(userTimeZone);
        LocaleUtil.setUserLocale(Locale.ROOT);
    }


    private static final String _255_POUND_SIGNS;
    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 255; i++) {
            sb.append('#');
        }
        _255_POUND_SIGNS = sb.toString();
    }

    @Test
    void testSome() {
        JLabel l = new JLabel();
        CellFormat fmt = CellFormat.getInstance("\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)");
        fmt.apply(l, 1.1);
        assertEquals("$1.10 ", l.getText());
    }

    @Test
    void testPositiveFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(12.345);
        assertEquals("12.35", result.text);
    }

    @Test
    void testNegativeFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("-12.35", result.text);
    }

    @Test
    void testZeroFormatHasOnePart() {
        CellFormat fmt = CellFormat.getInstance("0.00");
        CellFormatResult result = fmt.apply(0.0);
        assertEquals("0.00", result.text);
    }

    @Test
    void testPositiveFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(12.345);
        assertEquals("12.35", result.text);
    }

    @Test
    void testNegativeFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("-12.35", result.text);
    }

    @Test
    void testNegativeFormatHasPosAndNegParts2() {
        CellFormat fmt = CellFormat.getInstance("0.00;(0.00)");
        CellFormatResult result = fmt.apply(-12.345);
        assertEquals("(12.35)", result.text);
    }

    @Test
    void testZeroFormatHasPosAndNegParts() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00");
        CellFormatResult result = fmt.apply(0.0);
        assertEquals("0.00", result.text);
    }

    @Test
    void testFormatWithThreeSections() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00;-");

        assertEquals("12.35",  fmt.apply(12.345).text);
        assertEquals("-12.35", fmt.apply(-12.345).text);
        assertEquals("-",      fmt.apply(0.0).text);
        assertEquals("abc",    fmt.apply("abc").text);
    }

    @Test
    void testFormatWithFourSections() {
        CellFormat fmt = CellFormat.getInstance("0.00;-0.00;-; @ ");

        assertEquals("12.35",  fmt.apply(12.345).text);
        assertEquals("-12.35", fmt.apply(-12.345).text);
        assertEquals("-",      fmt.apply(0.0).text);
        assertEquals(" abc ",  fmt.apply("abc").text);
    }

    @Test
    void testApplyCellForGeneralFormat() throws Exception {

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

        // case CellType.BLANK
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("", result0.text);

        // case CellType.BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals("TRUE", result1.text);

        // case CellType.NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(cell2);
        assertEquals("1.23", result2.text);

        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(cell3);
        assertEquals("123", result3.text);

        // case CellType.STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(cell4);
        assertEquals("abc", result4.text);

        wb.close();
    }

    @Test
    void testApplyCellForAtFormat() throws Exception {

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

        // case CellType.BLANK
        CellFormatResult result0 = cf.apply(cell0);
        assertEquals("", result0.text);

        // case CellType.BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(cell1);
        assertEquals("TRUE", result1.text);

        // case CellType.NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(cell2);
        assertEquals("1.23", result2.text);

        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(cell3);
        assertEquals("123", result3.text);

        // case CellType.STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(cell4);
        assertEquals("abc", result4.text);

        wb.close();
    }

    @Test
    void testApplyCellForDateFormat() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyCellForTimeFormat() throws Exception {

        // Create a workbook, row and cell to test with
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        CellFormat cf = CellFormat.getInstance("hh:mm");

        cell.setCellValue(DateUtil.convertTime("03:04:05"));
        CellFormatResult result = cf.apply(cell);
        assertEquals("03:04", result.text);

        wb.close();
    }

    @Test
    void testApplyCellForDateFormatAndNegativeFormat() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyJLabelCellForGeneralFormat() throws Exception {

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

        // case CellType.BLANK
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("", result0.text);
        assertEquals("", label0.getText());

        // case CellType.BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals("TRUE", result1.text);
        assertEquals("TRUE", label1.getText());

        // case CellType.NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(label2, cell2);
        assertEquals("1.23", result2.text);
        assertEquals("1.23", label2.getText());

        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(label3, cell3);
        assertEquals("123", result3.text);
        assertEquals("123", label3.getText());

        // case CellType.STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(label4, cell4);
        assertEquals("abc", result4.text);
        assertEquals("abc", label4.getText());

        wb.close();
    }

    @Test
    void testApplyJLabelCellForAtFormat() throws Exception {

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

        // case CellType.BLANK
        CellFormatResult result0 = cf.apply(label0, cell0);
        assertEquals("", result0.text);
        assertEquals("", label0.getText());

        // case CellType.BOOLEAN
        cell1.setCellValue(true);
        CellFormatResult result1 = cf.apply(label1, cell1);
        assertEquals("TRUE", result1.text);
        assertEquals("TRUE", label1.getText());

        // case CellType.NUMERIC
        cell2.setCellValue(1.23);
        CellFormatResult result2 = cf.apply(label2, cell2);
        assertEquals("1.23", result2.text);
        assertEquals("1.23", label2.getText());

        cell3.setCellValue(123.0);
        CellFormatResult result3 = cf.apply(label3, cell3);
        assertEquals("123", result3.text);
        assertEquals("123", label3.getText());

        // case CellType.STRING
        cell4.setCellValue("abc");
        CellFormatResult result4 = cf.apply(label4, cell4);
        assertEquals("abc", result4.text);
        assertEquals("abc", label4.getText());

        wb.close();
    }

    @Test
    void testApplyJLabelCellForDateFormat() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyJLabelCellForTimeFormat() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyJLabelCellForDateFormatAndNegativeFormat() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasOnePartAndPartHasCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasTwoPartsFirstHasCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasTwoPartsBothHaveCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasThreePartsFirstHasCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasThreePartsFirstTwoHaveCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasThreePartsFirstIsDateFirstTwoHaveCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasTwoPartsFirstHasConditionSecondIsGeneral() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasThreePartsFirstTwoHaveConditionThirdIsGeneral() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasFourPartsFirstHasCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasFourPartsSecondHasCondition() throws Exception {

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

        wb.close();
    }

    @Test
    void testApplyFormatHasFourPartsFirstTwoHaveCondition() throws Exception {

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

        wb.close();
    }

    /*
     * Test apply(Object value) with a number as parameter
     */
    @Test
    void testApplyObjectNumber() {

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
    @Test
    void testApplyObjectDate() throws ParseException {
        CellFormat cf1 = CellFormat.getInstance("m/d/yyyy");
        SimpleDateFormat sdf1 = new SimpleDateFormat("M/d/y", Locale.ROOT);
        sdf1.setTimeZone(TimeZone.getTimeZone("CET"));
        Date date1 = sdf1.parse("01/11/2012");
        assertEquals("1/11/2012", cf1.apply(date1).text);
    }

    @Test
    void testApplyCellForDateFormatWithConditions() throws Exception {

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

        wb.close();
    }

    /*
     * Test apply(Object value) with a String as parameter
     */
    @Test
    void testApplyObjectString() {

        CellFormat cf = CellFormat.getInstance("0.00");

        assertEquals("abc", cf.apply("abc").text);

    }

    /*
     * Test apply(Object value) with a Boolean as parameter
     */
    @Test
    void testApplyObjectBoolean() {

        CellFormat cf1 = CellFormat.getInstance("0");
        CellFormat cf2 = CellFormat.getInstance("General");
        CellFormat cf3 = CellFormat.getInstance("@");

        assertEquals("TRUE", cf1.apply(true).text);
        assertEquals("FALSE", cf2.apply(false).text);
        assertEquals("TRUE", cf3.apply(true).text);

    }

    @Test
    void testSimpleFractionFormat() throws IOException {
        CellFormat cf1 = CellFormat.getInstance("# ?/?");
        // Create a workbook, row and cell to test with
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(123456.6);
            //System.out.println(cf1.apply(cell).text);
            assertEquals("123456 3/5", cf1.apply(cell).text);
        }
    }

    @Test
    void testAccountingFormats() {
        char pound = '\u00A3';
        char euro  = '\u20AC';

        // Accounting -> 0 decimal places, default currency symbol
        String formatDft = "_-\"$\"* #,##0_-;\\-\"$\"* #,##0_-;_-\"$\"* \"-\"_-;_-@_-";
        // Accounting -> 0 decimal places, US currency symbol
        String formatUS  = "_-[$$-409]* #,##0_ ;_-[$$-409]* -#,##0 ;_-[$$-409]* \"-\"_-;_-@_-";
        // Accounting -> 0 decimal places, UK currency symbol
        String formatUK  = "_-[$"+pound+"-809]* #,##0_-;\\-[$"+pound+"-809]* #,##0_-;_-[$"+pound+"-809]* \"-\"??_-;_-@_-";
        // French style accounting, euro sign comes after not before
        String formatFR  = "_-#,##0* [$"+euro+"-40C]_-;\\-#,##0* [$"+euro+"-40C]_-;_-\"-\"??* [$"+euro+"-40C] _-;_-@_-";

        // R English (South Africa) style accounting, R currency symbol and 4-digit locale specifier
        String formatSA = "_-[$R-1C09]* #,##0_-;\\-[$R-1C09]* #,##0_-;_-[$R-1C09]* \"-\"??_-;_-@_-";
        // USD style accounting, USD currency symbol without -xxx or -xxxx locale specifier
        String formatUSD = "_([$USD]\\ * #,##0_);_([$USD]\\ * \\(#,##0\\);_([$USD]\\ * \"-\"??_);_(@_)";

        // Has +ve, -ve and zero rules
        CellFormat cfDft = CellFormat.getInstance(formatDft);
        CellFormat cfUS  = CellFormat.getInstance(formatUS);
        CellFormat cfUK  = CellFormat.getInstance(formatUK);
        CellFormat cfFR  = CellFormat.getInstance(formatFR);

        CellFormat cfSA  = CellFormat.getInstance(formatSA);
        CellFormat cfUSD = CellFormat.getInstance(formatUSD);

        // For +ve numbers, should be Space + currency symbol + spaces + whole number with commas + space
        // (Except French, which is mostly reversed...)
        assertEquals(" $   12 ", cfDft.apply(12.33).text);
        assertEquals(" $   12 ",  cfUS.apply(12.33).text);
        assertEquals(" "+pound+"   12 ", cfUK.apply(12.33).text);
        assertEquals(" 12   "+euro+" ", cfFR.apply(12.33).text);

        assertEquals(" R   12 ", cfSA.apply(Double.valueOf(12.33)).text);
        assertEquals(" USD    12 ", cfUSD.apply(Double.valueOf(12.33)).text);

        assertEquals(" $   16,789 ", cfDft.apply(16789.2).text);
        assertEquals(" $   16,789 ",  cfUS.apply(16789.2).text);
        assertEquals(" "+pound+"   16,789 ", cfUK.apply(16789.2).text);
        assertEquals(" 16,789   "+euro+" ", cfFR.apply(16789.2).text);

        assertEquals(" R   16,789 ", cfSA.apply(Double.valueOf(16789.2)).text);
        assertEquals(" USD    16,789 ", cfUSD.apply(Double.valueOf(16789.2)).text);

        // For -ve numbers, gets a bit more complicated...
        assertEquals("-$   12 ", cfDft.apply(-12.33).text);
        assertEquals(" $   -12 ",  cfUS.apply(-12.33).text);
        assertEquals("-"+pound+"   12 ", cfUK.apply(-12.33).text);
        assertEquals("-12   "+euro+" ", cfFR.apply(-12.33).text);

        assertEquals("-R   12 ", cfSA.apply(Double.valueOf(-12.33)).text);
        assertEquals(" USD    (12)", cfUSD.apply(Double.valueOf(-12.33)).text);

        assertEquals("-$   16,789 ", cfDft.apply(-16789.2).text);
        assertEquals(" $   -16,789 ",  cfUS.apply(-16789.2).text);
        assertEquals("-"+pound+"   16,789 ", cfUK.apply(-16789.2).text);
        assertEquals("-16,789   "+euro+" ", cfFR.apply(-16789.2).text);

        assertEquals("-R   16,789 ", cfSA.apply(Double.valueOf(-16789.2)).text);
        assertEquals(" USD    (16,789)", cfUSD.apply(Double.valueOf(-16789.2)).text);

        // For zero, should be Space + currency symbol + spaces + Minus + spaces
        assertEquals(" $   - ", cfDft.apply((double) 0).text);
        assertEquals(" $   - ", cfUS.apply((double) 0).text);
        // TODO Fix these to not have an incorrect bonus 0 on the end
        //assertEquals(" "+pound+"   -  ", cfUK.apply((double) 0).text);
        //assertEquals(" -    "+euro+"  ", cfFR.apply((double) 0).text);
    }

    @Test
    void testThreePartComplexFormat1() {
        // verify a rather complex format found e.g. in http://wahl.land-oberoesterreich.gv.at/Downloads/bp10.xls
        CellFormatPart posPart = new CellFormatPart("[$-F400]h:mm:ss\\ AM/PM");
        assertNotNull(posPart);
        assertEquals("1:00:12 AM", posPart.apply(new Date(12345)).text);

        CellFormatPart negPart = new CellFormatPart("[$-F40]h:mm:ss\\ AM/PM");
        assertNotNull(negPart);
        assertEquals("1:00:12 AM", posPart.apply(new Date(12345)).text);

        //assertNotNull(new CellFormatPart("_-* \"\"??_-;_-@_-"));

        CellFormat instance = CellFormat.getInstance("[$-F400]h:mm:ss\\ AM/PM;[$-F40]h:mm:ss\\ AM/PM;_-* \"\"??_-;_-@_-");
        assertNotNull(instance);
        assertEquals("1:00:12 AM", instance.apply(new Date(12345)).text);
    }

    @Test
    void testThreePartComplexFormat2() {
        // verify a rather complex format found e.g. in http://wahl.land-oberoesterreich.gv.at/Downloads/bp10.xls
        CellFormatPart posPart = new CellFormatPart("dd/mm/yyyy");
        assertNotNull(posPart);
        assertEquals("01/01/1970", posPart.apply(new Date(12345)).text);

        CellFormatPart negPart = new CellFormatPart("dd/mm/yyyy");
        assertNotNull(negPart);
        assertEquals("01/01/1970", posPart.apply(new Date(12345)).text);

        //assertNotNull(new CellFormatPart("_-* \"\"??_-;_-@_-"));

        CellFormat instance = CellFormat.getInstance("dd/mm/yyyy;dd/mm/yyyy;_-* \"\"??_-;_-@_-");
        assertNotNull(instance);
        assertEquals("01/01/1970", instance.apply(new Date(12345)).text);
    }

    @Test
    void testBug62865() {
        CellFormat cf = CellFormat.getInstance("\"ca. \"0");
        assertEquals("ca. 5", cf.apply((double) 5).text);
    }

    @Test
    void testNamedColorsExist() {
    	// Make sure we have all standard named colors defined 
    	// and are returned as non-null regardless of case
        Stream.of("black", "white", "red", "green", "blue", "yellow", "magenta", "cyan",
        		"Black", "White", "Red", "Green", "Blue", "Yellow", "Magenta", "Cyan",
        		"BLACK", "WHITE", "RED", "GREEN", "BLUE", "YELLOW", "MAGENTA", "CYAN")
        		.map(CellFormatPart::getColor)
                .forEach(Assertions::assertNotNull);
    }
    
    @Test
    void testIndexedColorsExist() {   	
    	// Make sure the standard indexed colors are returned correctly and regardless of case
    	for (int i = 0; i < 56; ++i) {
    		assertNotNull(CellFormatPart.getColor("Color " + (i + 1)));
    		assertNotNull(CellFormatPart.getColor("COLOR" + (i + 1)));
    		assertNotNull(CellFormatPart.getColor("color" + (i + 1)));
    	}
    }
    
    @Test
    void verifyNamedColors() {
    	assertEquals(CellFormatPart.getColor("Black"), new Color(0x000000));
    	assertEquals(CellFormatPart.getColor("white"), new Color(0xFFFFFF));
    	assertEquals(CellFormatPart.getColor("RED"), new Color(0xFF0000));
    	assertEquals(CellFormatPart.getColor("Green"), new Color(0x00FF00));
    	assertEquals(CellFormatPart.getColor("blue"), new Color(0x0000FF));
    	assertEquals(CellFormatPart.getColor("YELLOW"), new Color(0xFFFF00));
    	assertEquals(CellFormatPart.getColor("Magenta"), new Color(0xFF00FF));
    	assertEquals(CellFormatPart.getColor("cyan"), new Color(0x00FFFF));
    }
    
    @Test
    void verifyIndexedColors() {
    	assertEquals(CellFormatPart.getColor("Color1"), CellFormatPart.getColor("black"));
    	assertEquals(CellFormatPart.getColor("color2"), CellFormatPart.getColor("white"));
    	assertEquals(CellFormatPart.getColor("Color3"), CellFormatPart.getColor("red"));
    	assertEquals(CellFormatPart.getColor("color4"), CellFormatPart.getColor("green"));
    	assertEquals(CellFormatPart.getColor("Color5"), CellFormatPart.getColor("blue"));
    	assertEquals(CellFormatPart.getColor("color6"), CellFormatPart.getColor("yellow"));
    	assertEquals(CellFormatPart.getColor("Color7"), CellFormatPart.getColor("magenta"));
    	assertEquals(CellFormatPart.getColor("color8"), CellFormatPart.getColor("cyan"));
    	assertEquals(CellFormatPart.getColor("Color9"), new Color(0x800000));
    	assertEquals(CellFormatPart.getColor("color10"), new Color(0x008000));
    	assertEquals(CellFormatPart.getColor("Color11"), new Color(0x000080));
    	assertEquals(CellFormatPart.getColor("color12"), new Color(0x808000));
    	assertEquals(CellFormatPart.getColor("Color13"), new Color(0x800080));
    	assertEquals(CellFormatPart.getColor("color14"), new Color(0x008080));
    	assertEquals(CellFormatPart.getColor("Color15"), new Color(0xC0C0C0));
    	assertEquals(CellFormatPart.getColor("color16"), new Color(0x808080));
    	assertEquals(CellFormatPart.getColor("Color17"), new Color(0x9999FF));
    	assertEquals(CellFormatPart.getColor("COLOR18"), new Color(0x993366));
    	assertEquals(CellFormatPart.getColor("Color19"), new Color(0xFFFFCC));
    	assertEquals(CellFormatPart.getColor("color20"), new Color(0xCCFFFF));
    	assertEquals(CellFormatPart.getColor("Color21"), new Color(0x660066));
    	assertEquals(CellFormatPart.getColor("COLOR22"), new Color(0xFF8080));
    	assertEquals(CellFormatPart.getColor("Color23"), new Color(0x0066CC));
    	assertEquals(CellFormatPart.getColor("color24"), new Color(0xCCCCFF));
    	assertEquals(CellFormatPart.getColor("Color25"), new Color(0x000080));
    	assertEquals(CellFormatPart.getColor("color26"), new Color(0xFF00FF));
    	assertEquals(CellFormatPart.getColor("Color27"), new Color(0xFFFF00));
    	assertEquals(CellFormatPart.getColor("COLOR28"), new Color(0x00FFFF));
    	assertEquals(CellFormatPart.getColor("Color29"), new Color(0x800080));
    	assertEquals(CellFormatPart.getColor("color30"), new Color(0x800000));
    	assertEquals(CellFormatPart.getColor("Color31"), new Color(0x008080));
    	assertEquals(CellFormatPart.getColor("Color32"), new Color(0x0000FF));
    	assertEquals(CellFormatPart.getColor("Color33"), new Color(0x00CCFF));
    	assertEquals(CellFormatPart.getColor("Color34"), new Color(0xCCFFFF));
    	assertEquals(CellFormatPart.getColor("Color35"), new Color(0xCCFFCC));
    	assertEquals(CellFormatPart.getColor("Color36"), new Color(0xFFFF99));
    	assertEquals(CellFormatPart.getColor("Color37"), new Color(0x99CCFF));
    	assertEquals(CellFormatPart.getColor("Color38"), new Color(0xFF99CC));
    	assertEquals(CellFormatPart.getColor("Color39"), new Color(0xCC99FF));
    	assertEquals(CellFormatPart.getColor("Color40"), new Color(0xFFCC99));
    	assertEquals(CellFormatPart.getColor("Color41"), new Color(0x3366FF));
    	assertEquals(CellFormatPart.getColor("Color42"), new Color(0x33CCCC));
    	assertEquals(CellFormatPart.getColor("Color43"), new Color(0x99CC00));
    	assertEquals(CellFormatPart.getColor("Color44"), new Color(0xFFCC00));
    	assertEquals(CellFormatPart.getColor("Color45"), new Color(0xFF9900));
    	assertEquals(CellFormatPart.getColor("Color46"), new Color(0xFF6600));
    	assertEquals(CellFormatPart.getColor("Color47"), new Color(0x666699));
    	assertEquals(CellFormatPart.getColor("Color48"), new Color(0x969696));
    	assertEquals(CellFormatPart.getColor("Color49"), new Color(0x003366));
    	assertEquals(CellFormatPart.getColor("Color50"), new Color(0x339966));
    	assertEquals(CellFormatPart.getColor("Color51"), new Color(0x003300));
    	assertEquals(CellFormatPart.getColor("Color52"), new Color(0x333300));
    	assertEquals(CellFormatPart.getColor("Color53"), new Color(0x993300));
    	assertEquals(CellFormatPart.getColor("Color54"), new Color(0x993366));
    	assertEquals(CellFormatPart.getColor("Color55"), new Color(0x333399));
    	assertEquals(CellFormatPart.getColor("Color56"), new Color(0x333333));
    }
    
    @Test
    void testColorsInWorkbook() throws IOException {
        // Create a workbook, row and cell to test with
        try (Workbook wb = new HSSFWorkbook()) {
	        Sheet sheet = wb.createSheet();
	        Row row = sheet.createRow(0);
	        Cell cell = row.createCell(0);
	        CellFormatResult result;
	        CellFormat cf = CellFormat.getInstance(
	        		"[GREEN]#,##0.0;[RED]\\(#,##0.0\\);[COLOR22]\"===\";[COLOR 8]\\\"@\\\"");
	        
	        cell.setCellValue(100.0);
	        result = cf.apply(cell);
	        assertEquals("100.0", result.text);
	        assertEquals(result.textColor, CellFormatPart.getColor("color 4"));
	        
	        cell.setCellValue(-50.0);
	        result = cf.apply(cell);
	        assertEquals("(50.0)", result.text);
	        assertEquals(result.textColor, CellFormatPart.getColor("red"));
	
	        cell.setCellValue("foo");
	        result = cf.apply(cell);
	        assertEquals("\"foo\"", result.text);
	        assertEquals(result.textColor, CellFormatPart.getColor("cyan"));
	        
	        cell.setCellValue(0.0);
	        result = cf.apply(cell);
	        assertEquals("===", result.text);
	        assertEquals(result.textColor, CellFormatPart.getColor("color 22"));
        }
    }

    @Test
    void testElapsedSecondsRound() {
        CellFormatPart part = new CellFormatPart("[h]\\h m\\m s\\s");
        assertNotNull(part);
        assertEquals("0h 0m 9s", part.apply(0.0001).text);
    }
}
