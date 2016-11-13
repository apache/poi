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

import static java.util.Calendar.AUGUST;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MAY;
import static java.util.Calendar.OCTOBER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class TestHSSFDateUtil
 */
public class TestHSSFDateUtil {

    static TimeZone userTimeZone;
    
    @BeforeClass
    public static void setCEST() {
        userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CEST"));
    }
    
    @AfterClass
    public static void resetTimeZone() {
        LocaleUtil.setUserTimeZone(userTimeZone);
    }
    
    /**
     * Checks the date conversion functions in the HSSFDateUtil class.
     */
    @Test
    public void dateConversion() {

        // Iteratating over the hours exposes any rounding issues.
        Calendar cal = LocaleUtil.getLocaleCalendar(2002,JANUARY,1,0,1,1);
        for (int hour = 0; hour < 23; hour++) {
            double excelDate = HSSFDateUtil.getExcelDate(cal.getTime(), false);

            assertEquals("Checking hour = " + hour, cal.getTime().getTime(),
                    HSSFDateUtil.getJavaDate(excelDate, false).getTime());

            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        // check 1900 and 1904 date windowing conversions
        double excelDate = 36526.0;
        // with 1900 windowing, excelDate is Jan. 1, 2000
        // with 1904 windowing, excelDate is Jan. 2, 2004
        cal.set(2000,JANUARY,1,0,0,0); // Jan. 1, 2000
        Date dateIf1900 = cal.getTime();
        cal.add(Calendar.YEAR,4); // now Jan. 1, 2004
        cal.add(Calendar.DATE,1); // now Jan. 2, 2004
        Date dateIf1904 = cal.getTime();
        // 1900 windowing
        assertEquals("Checking 1900 Date Windowing",
                dateIf1900.getTime(),
                HSSFDateUtil.getJavaDate(excelDate,false).getTime());
        // 1904 windowing
        assertEquals("Checking 1904 Date Windowing",
                dateIf1904.getTime(),
                HSSFDateUtil.getJavaDate(excelDate,true).getTime());
    }

    /**
     * Checks the conversion of a java.util.date to Excel on a day when
     * Daylight Saving Time starts.
     */
    @Test
    public void excelConversionOnDSTStart() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,MARCH,28,0,0,0);
        for (int hour = 0; hour < 24; hour++) {
            
            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();

            
            double excelDate = HSSFDateUtil.getExcelDate(javaDate, false);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    hour,
                    differenceInHours);
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    javaDate.getTime(),
                    HSSFDateUtil.getJavaDate(excelDate, false).getTime());
        }
    }

    /**
     * Checks the conversion of an Excel date to a java.util.date on a day when
     * Daylight Saving Time starts.
     */
    @Test
    public void javaConversionOnDSTStart() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,MARCH,28,0,0,0);
        double excelDate = HSSFDateUtil.getExcelDate(cal.getTime(), false);
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {

            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = HSSFDateUtil.getJavaDate(excelDate, false);
            double actDate = HSSFDateUtil.getExcelDate(javaDate, false);
            assertEquals("Checking " + hour + " hours on Daylight Saving Time start date",
                    excelDate, actDate, oneMinute);
        }
    }

    /**
     * Checks the conversion of a java.util.Date to Excel on a day when
     * Daylight Saving Time ends.
     */
    @Test
    public void excelConversionOnDSTEnd() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,OCTOBER,31,0,0,0);
        for (int hour = 0; hour < 24; hour++) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();
            double excelDate = HSSFDateUtil.getExcelDate(javaDate, false);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals("Checking " + hour + " hour on Daylight Saving Time end date",
                    hour,
                    differenceInHours);
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    javaDate.getTime(),
                    HSSFDateUtil.getJavaDate(excelDate, false).getTime());
        }
    }

    /**
     * Checks the conversion of an Excel date to java.util.Date on a day when
     * Daylight Saving Time ends.
     */
    @Test
    public void javaConversionOnDSTEnd() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,OCTOBER,31,0,0,0);
        double excelDate = HSSFDateUtil.getExcelDate(cal.getTime(), false);
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = HSSFDateUtil.getJavaDate(excelDate, false);
            assertEquals("Checking " + hour + " hours on Daylight Saving Time start date",
                    excelDate,
                    HSSFDateUtil.getExcelDate(javaDate, false), oneMinute);
        }
    }

    /**
     * Tests that we deal with time-zones properly
     */
    @Test
    public void calendarConversion() {
        TimeZone userTZ = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            Calendar cal = LocaleUtil.getLocaleCalendar(2002,JANUARY,1,12,1,1);
            Date expected = cal.getTime();
    
            // Iterating over the hours exposes any rounding issues.
            for (int hour = -12; hour <= 12; hour++)
            {
                String id = "GMT" + (hour < 0 ? "" : "+") + hour + ":00";
                cal.setTimeZone(TimeZone.getTimeZone(id));
                cal.set(Calendar.HOUR_OF_DAY, 12);
                double excelDate = HSSFDateUtil.getExcelDate(cal, false);
                Date javaDate = HSSFDateUtil.getJavaDate(excelDate);
    
                // Should match despite time-zone
                assertEquals("Checking timezone " + id, expected.getTime(), javaDate.getTime());
            }
            
            // Check that the timezone aware getter works correctly 
            TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
            TimeZone ldn = TimeZone.getTimeZone("Europe/London");
            
            // 12:45 on 27th April 2012
            double excelDate = 41026.53125;
            
            // Same, no change
            assertEquals(
                  HSSFDateUtil.getJavaDate(excelDate, false).getTime(),
                  HSSFDateUtil.getJavaDate(excelDate, false, cet).getTime()
            );
            
            // London vs Copenhagen, should differ by an hour
            Date cetDate = HSSFDateUtil.getJavaDate(excelDate, false);
            Date ldnDate = HSSFDateUtil.getJavaDate(excelDate, false, ldn);
            assertEquals(ldnDate.getTime() - cetDate.getTime(), 60*60*1000);
        } finally {
            LocaleUtil.setUserTimeZone(userTZ);
        }
    }

    /**
     * Tests that we correctly detect date formats as such
     */
    @Test
    public void identifyDateFormats() {
        // First up, try with a few built in date formats
        short[] builtins = new short[] { 0x0e, 0x0f, 0x10, 0x16, 0x2d, 0x2e };
        for (short builtin : builtins) {
            String formatStr = HSSFDataFormat.getBuiltinFormat(builtin);
            assertTrue( HSSFDateUtil.isInternalDateFormat(builtin) );
            assertTrue( HSSFDateUtil.isADateFormat(builtin,formatStr) );
        }

        // Now try a few built-in non date formats
        builtins = new short[] { 0x01, 0x02, 0x17, 0x1f, 0x30 };
        for (short builtin : builtins) {
            String formatStr = HSSFDataFormat.getBuiltinFormat(builtin);
            assertFalse( HSSFDateUtil.isInternalDateFormat(builtin) );
            assertFalse( HSSFDateUtil.isADateFormat(builtin,formatStr) );
        }

        // Now for some non-internal ones
        // These come after the real ones
        int numBuiltins = HSSFDataFormat.getNumberOfBuiltinBuiltinFormats();
        assertTrue(numBuiltins < 60);
        short formatId = 60;
        assertFalse( HSSFDateUtil.isInternalDateFormat(formatId) );

        // Valid ones first
        String[] formats = new String[] {
                "yyyy-mm-dd", "yyyy/mm/dd", "yy/mm/dd", "yy/mmm/dd",
                "dd/mm/yy", "dd/mm/yyyy", "dd/mmm/yy",
                "dd-mm-yy", "dd-mm-yyyy",
                "DD-MM-YY", "DD-mm-YYYY",
                "dd\\-mm\\-yy", // Sometimes escaped
                "dd.mm.yyyy", "dd\\.mm\\.yyyy",
                "dd\\ mm\\.yyyy AM", "dd\\ mm\\.yyyy pm",
                 "dd\\ mm\\.yyyy\\-dd", "[h]:mm:ss",
                 "mm/dd/yy", "\"mm\"/\"dd\"/\"yy\"",
                 "m\\/d\\/yyyy", 

                // These crazy ones are valid
                "yyyy-mm-dd;@", "yyyy/mm/dd;@",
                "dd-mm-yy;@", "dd-mm-yyyy;@",
                // These even crazier ones are also valid
                // (who knows what they mean though...)
                "[$-F800]dddd\\,\\ mmm\\ dd\\,\\ yyyy",
                "[$-F900]ddd/mm/yyy",
                // These ones specify colours, who knew that was allowed?
                "[BLACK]dddd/mm/yy",
                "[yeLLow]yyyy-mm-dd"
        };
        for (String format : formats) {
            assertTrue(
                    format + " is a date format",
                    HSSFDateUtil.isADateFormat(formatId, format)
            );
        }

        // Then time based ones too
        formats = new String[] {
                "yyyy-mm-dd hh:mm:ss", "yyyy/mm/dd HH:MM:SS",
                "mm/dd HH:MM", "yy/mmm/dd SS",
                "mm/dd HH:MM AM", "mm/dd HH:MM am",
                "mm/dd HH:MM PM", "mm/dd HH:MM pm",
                "m/d/yy h:mm AM/PM",
                "hh:mm:ss", "hh:mm:ss.0", "mm:ss.0",
                //support elapsed time [h],[m],[s]
                "[hh]", "[mm]", "[ss]", "[SS]", "[red][hh]"
        };
        for (String format : formats) {
            assertTrue(
                    format + " is a datetime format",
                    HSSFDateUtil.isADateFormat(formatId, format)
            );
        }

        // Then invalid ones
        formats = new String[] {
                "yyyy*mm*dd",
                "0.0", "0.000",
                "0%", "0.0%",
                "[]Foo", "[BLACK]0.00%",
                "[ms]", "[Mh]",
                "", null
        };
        for (String format : formats) {
            assertFalse(
                    format + " is not a date or datetime format",
                    HSSFDateUtil.isADateFormat(formatId, format)
            );
        }

        // And these are ones we probably shouldn't allow,
        //  but would need a better regexp
        formats = new String[] {
                "yyyy:mm:dd",
        };
        for (String format : formats) {
        //    assertFalse( HSSFDateUtil.isADateFormat(formatId, formats[i]) );
        }
    }

    /**
     * Test that against a real, test file, we still do everything
     *  correctly
     * @throws IOException 
     */
    @Test
    public void onARealFile() throws IOException {

        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("DateFormats.xls");
        HSSFSheet sheet       = workbook.getSheetAt(0);
        InternalWorkbook wb           = workbook.getWorkbook();
        assertNotNull(wb);

        HSSFRow  row;
        HSSFCell cell;
        HSSFCellStyle style;

        double aug_10_2007 = 39304.0;

        // Should have dates in 2nd column
        // All of them are the 10th of August
        // 2 US dates, 3 UK dates
        row  = sheet.getRow(0);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertEquals("d-mmm-yy", style.getDataFormatString());
        assertTrue(HSSFDateUtil.isInternalDateFormat(style.getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(1);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(2);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertTrue(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(3);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(4);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
        
        workbook.close();
    }

    @Test
    public void excelDateBorderCases() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        df.setTimeZone(LocaleUtil.getUserTimeZone());
        
        assertEquals(1.0, DateUtil.getExcelDate(df.parse("1900-01-01")), 0.00001);
        assertEquals(31.0, DateUtil.getExcelDate(df.parse("1900-01-31")), 0.00001);
        assertEquals(32.0, DateUtil.getExcelDate(df.parse("1900-02-01")), 0.00001);
        assertEquals(/* BAD_DATE! */ -1.0, DateUtil.getExcelDate(df.parse("1899-12-31")), 0.00001);
    }

    @Test
    public void dateBug_2Excel() {
        assertEquals(59.0, HSSFDateUtil.getExcelDate(createDate(1900, FEBRUARY, 28), false), 0.00001);
        assertEquals(61.0, HSSFDateUtil.getExcelDate(createDate(1900, MARCH, 1), false), 0.00001);

        assertEquals(37315.00, HSSFDateUtil.getExcelDate(createDate(2002, FEBRUARY, 28), false), 0.00001);
        assertEquals(37316.00, HSSFDateUtil.getExcelDate(createDate(2002, MARCH, 1), false), 0.00001);
        assertEquals(37257.00, HSSFDateUtil.getExcelDate(createDate(2002, JANUARY, 1), false), 0.00001);
        assertEquals(38074.00, HSSFDateUtil.getExcelDate(createDate(2004, MARCH, 28), false), 0.00001);
    }

    @Test
    public void dateBug_2Java() {
        assertEquals(createDate(1900, FEBRUARY, 28), HSSFDateUtil.getJavaDate(59.0, false));
        assertEquals(createDate(1900, MARCH, 1), HSSFDateUtil.getJavaDate(61.0, false));

        assertEquals(createDate(2002, FEBRUARY, 28), HSSFDateUtil.getJavaDate(37315.00, false));
        assertEquals(createDate(2002, MARCH, 1), HSSFDateUtil.getJavaDate(37316.00, false));
        assertEquals(createDate(2002, JANUARY, 1), HSSFDateUtil.getJavaDate(37257.00, false));
        assertEquals(createDate(2004, MARCH, 28), HSSFDateUtil.getJavaDate(38074.00, false));
    }

    @Test
    public void date1904() {
        assertEquals(createDate(1904, JANUARY, 2), HSSFDateUtil.getJavaDate(1.0, true));
        assertEquals(createDate(1904, JANUARY, 1), HSSFDateUtil.getJavaDate(0.0, true));
        assertEquals(0.0, HSSFDateUtil.getExcelDate(createDate(1904, JANUARY, 1), true), 0.00001);
        assertEquals(1.0, HSSFDateUtil.getExcelDate(createDate(1904, JANUARY, 2), true), 0.00001);

        assertEquals(createDate(1998, JULY, 5), HSSFDateUtil.getJavaDate(35981, false));
        assertEquals(createDate(1998, JULY, 5), HSSFDateUtil.getJavaDate(34519, true));

        assertEquals(35981.0, HSSFDateUtil.getExcelDate(createDate(1998, JULY, 5), false), 0.00001);
        assertEquals(34519.0, HSSFDateUtil.getExcelDate(createDate(1998, JULY, 5), true), 0.00001);
    }

    /**
     * @param month zero based
     * @param day one based
     */
    private static Date createDate(int year, int month, int day) {
        return createDate(year, month, day, 0, 0, 0);
    }

    /**
     * @param month zero based
     * @param day one based
     */
    private static Date createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar c = LocaleUtil.getLocaleCalendar(year, month, day, hour, minute, second);
        return c.getTime();
    }

    /**
     * Check if HSSFDateUtil.getAbsoluteDay works as advertised.
     */
    @Test
    public void absoluteDay() {
        // 1 Jan 1900 is 1 day after 31 Dec 1899
        Calendar cal = LocaleUtil.getLocaleCalendar(1900,JANUARY,1,0,0,0);
        assertEquals("Checking absolute day (1 Jan 1900)", 1, HSSFDateUtil.absoluteDay(cal, false));
        // 1 Jan 1901 is 366 days after 31 Dec 1899
        cal.set(1901,JANUARY,1,0,0,0);
        assertEquals("Checking absolute day (1 Jan 1901)", 366, HSSFDateUtil.absoluteDay(cal, false));
    }

    @Test
    public void absoluteDayYearTooLow() {
        Calendar cal = LocaleUtil.getLocaleCalendar(1899,JANUARY,1,0,0,0);
        try {
        	HSSFDateUtil.absoluteDay(cal, false);
        	fail("Should fail here");
        } catch (IllegalArgumentException e) {
        	// expected here
        }

        try {
            cal.set(1903,JANUARY,1,0,0,0);
            HSSFDateUtil.absoluteDay(cal, true);
        	fail("Should fail here");
        } catch (IllegalArgumentException e) {
        	// expected here
        }
    }

    @Test
    public void convertTime() {

        final double delta = 1E-7; // a couple of digits more accuracy than strictly required
        assertEquals(0.5, HSSFDateUtil.convertTime("12:00"), delta);
        assertEquals(2.0/3, HSSFDateUtil.convertTime("16:00"), delta);
        assertEquals(0.0000116, HSSFDateUtil.convertTime("0:00:01"), delta);
        assertEquals(0.7330440, HSSFDateUtil.convertTime("17:35:35"), delta);
    }

    @Test
    public void parseDate() {
        assertEquals(createDate(2008, AUGUST, 3), HSSFDateUtil.parseYYYYMMDDDate("2008/08/03"));
        assertEquals(createDate(1994, MAY, 1), HSSFDateUtil.parseYYYYMMDDDate("1994/05/01"));
    }

    /**
     * Ensure that date values *with* a fractional portion get the right time of day
     */
    @Test
    public void convertDateTime() {
    	// Excel day 30000 is date 18-Feb-1982
        // 0.7 corresponds to time 16:48:00
        Date actual = HSSFDateUtil.getJavaDate(30000.7);
        Date expected = createDate(1982, 1, 18, 16, 48, 0);
        assertEquals(expected, actual);
    }

    /**
     * User reported a datetime issue in POI-2.5:
     *  Setting Cell's value to Jan 1, 1900 without a time doesn't return the same value set to
     * @throws IOException 
     */
    @Test
    public void bug19172() throws IOException
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFCell cell = sheet.createRow(0).createCell(0);

        // A pseudo special Excel dates
        Calendar cal = LocaleUtil.getLocaleCalendar(1900, JANUARY, 1);

        Date valueToTest = cal.getTime();

        cell.setCellValue(valueToTest);

        Date returnedValue = cell.getDateCellValue();

        assertEquals(valueToTest.getTime(), returnedValue.getTime());
        
        workbook.close();
    }

    /**
     * DateUtil.isCellFormatted(Cell) should not true for a numeric cell 
     * that's formatted as ".0000"
     */
    @Test
    public void bug54557() throws Exception {
       final String format = ".0000";
       boolean isDateFormat = HSSFDateUtil.isADateFormat(165, format);
       
       assertEquals(false, isDateFormat);
    }
    
    @Test
    public void bug56269() throws Exception {
        double excelFraction = 41642.45833321759d;
        Calendar calNoRound = HSSFDateUtil.getJavaCalendar(excelFraction, false);
        assertEquals(10, calNoRound.get(Calendar.HOUR));
        assertEquals(59, calNoRound.get(Calendar.MINUTE));
        assertEquals(59, calNoRound.get(Calendar.SECOND));
        Calendar calRound = HSSFDateUtil.getJavaCalendar(excelFraction, false, null, true);
        assertEquals(11, calRound.get(Calendar.HOUR));
        assertEquals(0, calRound.get(Calendar.MINUTE));
        assertEquals(0, calRound.get(Calendar.SECOND));
    }
}
