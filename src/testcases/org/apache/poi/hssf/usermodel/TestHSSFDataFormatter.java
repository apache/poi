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

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for HSSFDataFormatter.java
 */
public final class TestHSSFDataFormatter {
    private static TimeZone userTimeZone;

    @BeforeClass
    public static void setTimeZone() {
        userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        LocaleUtil.setUserLocale(Locale.US);
    }

    @AfterClass
    public static void resetTimeZone() {
        LocaleUtil.setUserTimeZone(userTimeZone);
        LocaleUtil.setUserLocale(Locale.ROOT);
    }


    private final HSSFDataFormatter formatter;
    private final HSSFWorkbook wb;

    public TestHSSFDataFormatter() {
        // create the formatter to test
        formatter = new HSSFDataFormatter();

        // create a workbook to test with
        wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFDataFormat format = wb.createDataFormat();

        // create a row and put some cells in it
        HSSFRow row = sheet.createRow(0);

        // date value for July 8 1901 1:19 PM
        double dateNum = 555.555;
        // date value for July 8 1901 11:23 AM
        double timeNum = 555.47431;

        //valid date formats -- all should have "Jul" in output
        String[] goodDatePatterns = {
            "[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy",
            "mmm/d/yy\\ h:mm PM;@",
            "mmmm/d/yy\\ h:mm;@",
            "mmmm/d;@",
            "mmmm/d/yy;@",
            "mmm/dd/yy;@",
            "[$-409]d\\-mmm;@",
            "[$-409]d\\-mmm\\-yy;@",
            "[$-409]dd\\-mmm\\-yy;@",
            "[$-409]mmm\\-yy;@",
            "[$-409]mmmm\\-yy;@",
            "[$-409]mmmm\\ d\\,\\ yyyy;@",
            "[$-409]mmm/d/yy\\ h:mm:ss;@",
            "[$-409]mmmm/d/yy\\ h:mm:ss am;@",
            "[$-409]mmmmm;@",
            "[$-409]mmmmm\\-yy;@",
            "mmmm/d/yyyy;@",
            "[$-409]d\\-mmm\\-yyyy;@",
            "[$-409]d\\-mmm;[$-3]d\\-mmm;@",      // international three-part
            "[$-41f]d\\-mmm;[$-41f]d\\-mmm;@",      // turkish international three-part
            "[$-F40f]d\\-mmm;[$-F40f]d\\-mmm;@",      // custom international three-part
            "[$-F40f]d\\-mmm;[$-F40f]d\\-mmm;0;@"      // custom international four-part
        };

        //valid time formats - all should have 11:23 in output
        String[] goodTimePatterns = {
            "HH:MM",
            "HH:MM:SS",
            "HH:MM;HH:MM;HH:MM", 
            // This is fun - blue if positive time,
            //  red if negative time or green for zero!
            "[BLUE]HH:MM;[RED]HH:MM;[GREEN]HH:MM", 
            "yyyy-mm-dd hh:mm",
            "yyyy-mm-dd hh:mm:ss",
        };

        // valid number formats
        String[] goodNumPatterns = {
            "#,##0.0000",
            "#,##0;[Red]#,##0",
            "(#,##0.00_);(#,##0.00)",
            "($#,##0.00_);[Red]($#,##0.00)",
            "$#,##0.00",
            "[$-809]#,##0.00", // international format
            "[$-2]#,##0.00", // international format
            "[$-041f]#,##0.00", // international format
            "0000.00000%",
            "0.000E+00",
            "0.00E+00",
            "[BLACK]0.00;[COLOR 5]##.##",
            "[>999999]#,,\"M\";[>999]#,\"K\";#", // num/K/M
            "[>999999]#.000,,\"M\";[>999]#.000,\"K\";#.000", // with decimals
            "[$-809]#,##0.00;[$-809]#,##0.00", // two-part international format
            "[$-809]#,##0.00;[$-809]#,##0.00;0", // three-part international format
            "[$-809]#,##0.00;[$-809]#,##0.00;0;@", // four-part international format
        };

        // invalid date formats -- will throw exception in DecimalFormat ctor
        String[] badNumPatterns = {
            "#,#$'#0.0000",
            "'#','#ABC#0;##,##0",
            "000 '123 4'5'6 000",
            "#''0#0'1#10L16EE"
        };

        // create cells with good date patterns
        for (int i = 0; i < goodDatePatterns.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(dateNum);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(goodDatePatterns[i]));
            cell.setCellStyle(cellStyle);
        }
        row = sheet.createRow(1);

        // create cells with time patterns
        for (int i = 0; i < goodTimePatterns.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(timeNum);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(goodTimePatterns[i]));
            cell.setCellStyle(cellStyle);
        }
        row = sheet.createRow(2);

        // create cells with num patterns
        for (int i = 0; i < goodNumPatterns.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(-1234567890.12345);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(goodNumPatterns[i]));
            cell.setCellStyle(cellStyle);
        }
        row = sheet.createRow(3);

        // create cells with bad num patterns
        for (int i = 0; i < badNumPatterns.length; i++) {
            HSSFCell cell = row.createCell(i);
            // If the '.' is any later, ExcelGeneralNumberFormat will render an integer, as Excel does.
            cell.setCellValue(12345678.9012345);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(badNumPatterns[i]));
            cell.setCellStyle(cellStyle);
        }

        // Built in formats

        { // Zip + 4 format
            row = sheet.createRow(4);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(123456789);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat("00000-0000"));
            cell.setCellStyle(cellStyle);
        }

        { // Phone number format
            row = sheet.createRow(5);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(5551234567D);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat("[<=9999999]###-####;(###) ###-####"));
            cell.setCellStyle(cellStyle);
        }

        { // SSN format
            row = sheet.createRow(6);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(444551234);
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat("000-00-0000"));
            cell.setCellStyle(cellStyle);
        }

        { // formula cell
            row = sheet.createRow(7);
            HSSFCell cell = row.createCell(0);
            cell.setCellType(CellType.FORMULA);
            cell.setCellFormula("SUM(12.25,12.25)/100");
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat("##.00%;"));
            cell.setCellStyle(cellStyle);
        }
    }

    /**
     * Test getting formatted values from numeric and date cells.
     */
    @Test
    public void testGetFormattedCellValueHSSFCell() {
        // Valid date formats -- cell values should be date formatted & not "555.555"
        HSSFRow row = wb.getSheetAt(0).getRow(0);
        Iterator<Cell> it = row.cellIterator();
        log("==== VALID DATE FORMATS ====");
        while (it.hasNext()) {
            Cell cell = it.next();
            String fmtval = formatter.formatCellValue(cell);
            log(fmtval);

            // should not be equal to "555.555"
            assertTrue( DateUtil.isCellDateFormatted(cell) );
            assertTrue( ! "555.555".equals(fmtval));

            String fmt = cell.getCellStyle().getDataFormatString();

            //assert the correct month form, as in the original Excel format
            String monthPtrn = fmt.contains("mmmm") ? "MMMM" : "MMM";
            // this line is intended to compute how "July" would look like in the current locale
            SimpleDateFormat sdf = new SimpleDateFormat(monthPtrn, LocaleUtil.getUserLocale());
            sdf.setTimeZone(LocaleUtil.getUserTimeZone());
            Calendar calDef = LocaleUtil.getLocaleCalendar(2010, 6, 15, 0, 0, 0);
            String jul = sdf.format(calDef.getTime());
            // special case for MMMMM = 1st letter of month name
            if(fmt.contains("mmmmm")) {
                jul = jul.substring(0,1);
            }
            // check we found july properly
            assertTrue("Format came out incorrect - " + fmt, fmtval.contains(jul));
        }

        row = wb.getSheetAt(0).getRow(1);
        it = row.cellIterator();
        log("==== VALID TIME FORMATS ====");
        while (it.hasNext()) {
            Cell cell = it.next();
            String fmt = cell.getCellStyle().getDataFormatString();
            String fmtval = formatter.formatCellValue(cell);
            log(fmtval);

            // should not be equal to "555.47431"
            assertTrue( DateUtil.isCellDateFormatted(cell) );
            assertTrue( ! "555.47431".equals(fmtval));

            // check we found the time properly
            assertTrue("Format came out incorrect - " + fmt + " - found " + fmtval + 
                       ", but expected to find '11:23'", fmtval.contains("11:23"));
        }

        // test number formats
        row = wb.getSheetAt(0).getRow(1);
        it = row.cellIterator();
        log("\n==== VALID NUMBER FORMATS ====");
        while (it.hasNext()) {
            HSSFCell cell = (HSSFCell) it.next();
            final String formatted = formatter.formatCellValue(cell);
            log(formatted);

            // should not include "12345678" - note that the input value was negative
            assertTrue(formatted != null && ! formatted.contains("12345678"));
        }

        // test bad number formats
        row = wb.getSheetAt(0).getRow(3);
        it = row.cellIterator();
        log("\n==== INVALID NUMBER FORMATS ====");
        while (it.hasNext()) {
            HSSFCell cell = (HSSFCell) it.next();
            log(formatter.formatCellValue(cell));
            // in some locales the the decimal delimiter is a comma, not a dot
            char decimalSeparator = DecimalFormatSymbols.getInstance(LocaleUtil.getUserLocale()).getDecimalSeparator();
            assertEquals("12345678" + decimalSeparator + "9", formatter.formatCellValue(cell));
        }

        // test Zip+4 format
        row = wb.getSheetAt(0).getRow(4);
        HSSFCell cell = row.getCell(0);
        log("\n==== ZIP FORMAT ====");
        log(formatter.formatCellValue(cell));
        assertEquals("12345-6789", formatter.formatCellValue(cell));

        // test phone number format
        row = wb.getSheetAt(0).getRow(5);
        cell = row.getCell(0);
        log("\n==== PHONE FORMAT ====");
        log(formatter.formatCellValue(cell));
        assertEquals("(555) 123-4567", formatter.formatCellValue(cell));

        // test SSN format
        row = wb.getSheetAt(0).getRow(6);
        cell = row.getCell(0);
        log("\n==== SSN FORMAT ====");
        log(formatter.formatCellValue(cell));
        assertEquals("444-55-1234", formatter.formatCellValue(cell));

        // null test-- null cell should result in empty String
        assertEquals(formatter.formatCellValue(null), "");

        // null test-- null cell should result in empty String
        assertEquals(formatter.formatCellValue(null), "");
    }

    @Test
    public void testGetFormattedCellValueHSSFCellHSSFFormulaEvaluator() {
        // test formula format
        HSSFRow row = wb.getSheetAt(0).getRow(7);
        HSSFCell cell = row.getCell(0);
        log("\n==== FORMULA CELL ====");

        // first without a formula evaluator
        log(formatter.formatCellValue(cell) + "\t (without evaluator)");
        assertEquals("SUM(12.25,12.25)/100", formatter.formatCellValue(cell));

        // now with a formula evaluator
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        log(formatter.formatCellValue(cell, evaluator) + "\t\t\t (with evaluator)");
        char decimalSeparator = DecimalFormatSymbols.getInstance(LocaleUtil.getUserLocale()).getDecimalSeparator();
        assertEquals("24" + decimalSeparator + "50%", formatter.formatCellValue(cell,evaluator));

    }

    /**
     * Test using a default number format. The format should be used when a
     * format pattern cannot be parsed by DecimalFormat.
     */
    @Test
    public void testSetDefaultNumberFormat() {
        HSSFRow row = wb.getSheetAt(0).getRow(3);
        Iterator<Cell> it = row.cellIterator();
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(LocaleUtil.getUserLocale());
        Format defaultFormat = new DecimalFormat("Balance $#,#00.00 USD;Balance -$#,#00.00 USD", dfs);
        formatter.setDefaultNumberFormat(defaultFormat);

        log("\n==== DEFAULT NUMBER FORMAT ====");
        while (it.hasNext()) {
            Cell cell = it.next();
            cell.setCellValue(cell.getNumericCellValue() * Math.random() / 1000000 - 1000);
            log(formatter.formatCellValue(cell));
            
            String formatted = formatter.formatCellValue(cell); 
            assertTrue("Doesn't start with Balance: " + formatted, formatted.startsWith("Balance "));
            assertTrue("Doesn't end with USD: " + formatted, formatted.endsWith(" USD"));
        }
    }

    /**
     * A format of "@" means use the general format
     */
    @Test
    public void testGeneralAtFormat() {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("47154.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        HSSFCell cellA1 = row.getCell(0);

        assertEquals(CellType.NUMERIC, cellA1.getCellType());
        assertEquals(2345.0, cellA1.getNumericCellValue(), 0.0001);
        assertEquals("@", cellA1.getCellStyle().getDataFormatString());

        HSSFDataFormatter f = new HSSFDataFormatter();

        assertEquals("2345", f.formatCellValue(cellA1));
    }

    /**
     * Tests various formattings of dates and numbers
     */
    @Test
    public void testFromFile() {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("Formatting.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);

        HSSFDataFormatter f = new HSSFDataFormatter();

        // This one is one of the nasty auto-locale changing ones...
        assertEquals("dd/mm/yyyy", sheet.getRow(1).getCell(0).getStringCellValue());
        assertEquals("m/d/yy",     sheet.getRow(1).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("11/24/06",   f.formatCellValue(sheet.getRow(1).getCell(1)));

        assertEquals("yyyy/mm/dd", sheet.getRow(2).getCell(0).getStringCellValue());
        assertEquals("yyyy/mm/dd", sheet.getRow(2).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("2006/11/24", f.formatCellValue(sheet.getRow(2).getCell(1)));

        assertEquals("yyyy-mm-dd", sheet.getRow(3).getCell(0).getStringCellValue());
        assertEquals("yyyy\\-mm\\-dd", sheet.getRow(3).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("2006-11-24", f.formatCellValue(sheet.getRow(3).getCell(1)));

        assertEquals("yy/mm/dd", sheet.getRow(4).getCell(0).getStringCellValue());
        assertEquals("yy/mm/dd", sheet.getRow(4).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("06/11/24", f.formatCellValue(sheet.getRow(4).getCell(1)));

        // Another builtin fun one
        assertEquals("dd/mm/yy", sheet.getRow(5).getCell(0).getStringCellValue());
        assertEquals("d/m/yy;@", sheet.getRow(5).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("24/11/06", f.formatCellValue(sheet.getRow(5).getCell(1)));

        assertEquals("dd-mm-yy", sheet.getRow(6).getCell(0).getStringCellValue());
        assertEquals("dd\\-mm\\-yy", sheet.getRow(6).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("24-11-06", f.formatCellValue(sheet.getRow(6).getCell(1)));


        // Another builtin fun one
        assertEquals("nn.nn", sheet.getRow(9).getCell(0).getStringCellValue());
        assertEquals("General", sheet.getRow(9).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("10.52", f.formatCellValue(sheet.getRow(9).getCell(1)));

        // text isn't quite the format rule...
        assertEquals("nn.nnn", sheet.getRow(10).getCell(0).getStringCellValue());
        assertEquals("0.000", sheet.getRow(10).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("10.520", f.formatCellValue(sheet.getRow(10).getCell(1)));

        // text isn't quite the format rule...
        assertEquals("nn.n", sheet.getRow(11).getCell(0).getStringCellValue());
        assertEquals("0.0", sheet.getRow(11).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("10.5", f.formatCellValue(sheet.getRow(11).getCell(1)));

        // text isn't quite the format rule...
        assertEquals("\u00a3nn.nn", sheet.getRow(12).getCell(0).getStringCellValue());
        assertEquals("\"\u00a3\"#,##0.00", sheet.getRow(12).getCell(1).getCellStyle().getDataFormatString());
        assertEquals("\u00a310.52", f.formatCellValue(sheet.getRow(12).getCell(1)));
    }

    private static void log(@SuppressWarnings("UnusedParameters") String msg) {
//      if (false) { // successful tests should be silent
//         System.out.println(msg);
//      }
    }
}
