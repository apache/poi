
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

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class TestHSSFDateUtil
 *
 *
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Hack Kampbjorn (hak at 2mba.dk)
 * @version %I%, %G%
 */

public class TestHSSFDateUtil
        extends TestCase
{

	public static final int CALENDAR_JANUARY = 0;
	public static final int CALENDAR_FEBRUARY = 0;
	public static final int CALENDAR_MARCH = 0;
	public static final int CALENDAR_APRIL = 0;

    public TestHSSFDateUtil(String s)
    {
        super(s);
    }

    /**
     * Checks the date conversion functions in the HSSFDateUtil class.
     */

    public void testDateConversion()
            throws Exception
    {

        // Iteratating over the hours exposes any rounding issues.
        for (int hour = 0; hour < 23; hour++)
        {
            GregorianCalendar date      = new GregorianCalendar(2002, 0, 1,
                    hour, 1, 1);
            double            excelDate =
                    HSSFDateUtil.getExcelDate(date.getTime());

            assertEquals("Checking hour = " + hour, date.getTime().getTime(),
                    HSSFDateUtil.getJavaDate(excelDate).getTime());
        }

        // check 1900 and 1904 date windowing conversions
        double excelDate = 36526.0;
        // with 1900 windowing, excelDate is Jan. 1, 2000
        // with 1904 windowing, excelDate is Jan. 2, 2004
        GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
        Date dateIf1900 = cal.getTime();
        cal.add(GregorianCalendar.YEAR,4); // now Jan. 1, 2004
        cal.add(GregorianCalendar.DATE,1); // now Jan. 2, 2004
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
    public void testExcelConversionOnDSTStart() {
        TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
        TimeZone.setDefault(cet);
        Calendar cal = new GregorianCalendar(2004, Calendar.MARCH, 28);
        for (int hour = 0; hour < 24; hour++) {

            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();
            double excelDate = HSSFDateUtil.getExcelDate(javaDate);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    hour,
                    differenceInHours);
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    javaDate.getTime(),
                    HSSFDateUtil.getJavaDate(excelDate).getTime());
        }
    }

    /**
     * Checks the conversion of an Excel date to a java.util.date on a day when
     * Daylight Saving Time starts.
     */
    public void testJavaConversionOnDSTStart() {
        TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
        TimeZone.setDefault(cet);
        Calendar cal = new GregorianCalendar(2004, Calendar.MARCH, 28);
        double excelDate = HSSFDateUtil.getExcelDate(cal.getTime());
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {

            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST            
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = HSSFDateUtil.getJavaDate(excelDate);
            assertEquals("Checking " + hour + " hours on Daylight Saving Time start date",
                    excelDate,
                    HSSFDateUtil.getExcelDate(javaDate), oneMinute);
        }
    }

    /**
     * Checks the conversion of a java.util.Date to Excel on a day when
     * Daylight Saving Time ends.
     */
    public void testExcelConversionOnDSTEnd() {
        TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
        TimeZone.setDefault(cet);
        Calendar cal = new GregorianCalendar(2004, Calendar.OCTOBER, 31);
        for (int hour = 0; hour < 24; hour++) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();
            double excelDate = HSSFDateUtil.getExcelDate(javaDate);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals("Checking " + hour + " hour on Daylight Saving Time end date",
                    hour,
                    differenceInHours);
            assertEquals("Checking " + hour + " hour on Daylight Saving Time start date",
                    javaDate.getTime(),
                    HSSFDateUtil.getJavaDate(excelDate).getTime());
        }
    }

    /**
     * Checks the conversion of an Excel date to java.util.Date on a day when
     * Daylight Saving Time ends.
     */
    public void testJavaConversionOnDSTEnd() {
        TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
        TimeZone.setDefault(cet);
        Calendar cal = new GregorianCalendar(2004, Calendar.OCTOBER, 31);
        double excelDate = HSSFDateUtil.getExcelDate(cal.getTime());
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = HSSFDateUtil.getJavaDate(excelDate);
            assertEquals("Checking " + hour + " hours on Daylight Saving Time start date",
                    excelDate,
                    HSSFDateUtil.getExcelDate(javaDate), oneMinute);
        }
    }
    
    /**
     * Tests that we correctly detect date formats as such
     */
    public void testIdentifyDateFormats() {
    	// First up, try with a few built in date formats
    	short[] builtins = new short[] { 0x0e, 0x0f, 0x10, 0x16, 0x2d, 0x2e };
    	for(int i=0; i<builtins.length; i++) {
    		String formatStr = HSSFDataFormat.getBuiltinFormat(builtins[i]);
    		assertTrue( HSSFDateUtil.isInternalDateFormat(builtins[i]) );
    		assertTrue( HSSFDateUtil.isADateFormat(builtins[i],formatStr) );
    	}
    	
    	// Now try a few built-in non date formats
    	builtins = new short[] { 0x01, 0x02, 0x17, 0x1f, 0x30 };
    	for(int i=0; i<builtins.length; i++) {
    		String formatStr = HSSFDataFormat.getBuiltinFormat(builtins[i]);
    		assertFalse( HSSFDateUtil.isInternalDateFormat(builtins[i]) );
    		assertFalse( HSSFDateUtil.isADateFormat(builtins[i],formatStr) );
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
    			"dd\\-mm\\-yy", // Sometimes escaped
    			
    			// These crazy ones are valid
    			"yyyy-mm-dd;@", "yyyy/mm/dd;@",
    			"dd-mm-yy;@", "dd-mm-yyyy;@",
    			// These even crazier ones are also valid
    			// (who knows what they mean though...)
    			"[$-F800]dddd\\,\\ mmm\\ dd\\,\\ yyyy",
    			"[$-F900]ddd/mm/yyy",
    	};
    	for(int i=0; i<formats.length; i++) {
    		assertTrue( HSSFDateUtil.isADateFormat(formatId, formats[i]) );
    	}
    	
    	// Then invalid ones
    	formats = new String[] {
    			"yyyy:mm:dd", 
    			"0.0", "0.000",
    			"0%", "0.0%",
    			"", null
    	};
    	for(int i=0; i<formats.length; i++) {
    		assertFalse( HSSFDateUtil.isADateFormat(formatId, formats[i]) );
    	}
    }

    /**
     * Test that against a real, test file, we still do everything
     *  correctly
     */
    public void testOnARealFile() throws Exception {
        String path     = System.getProperty("HSSF.testdata.path");
        String filename = path + "/DateFormats.xls";
        POIFSFileSystem fs =
            new POIFSFileSystem(new FileInputStream(filename));
        HSSFWorkbook workbook = new HSSFWorkbook(fs);
        HSSFSheet sheet       = workbook.getSheetAt(0);
        Workbook wb           = workbook.getWorkbook();
        
        HSSFRow  row;
        HSSFCell cell;
        HSSFCellStyle style;
        
        double aug_10_2007 = 39304.0;
        
        // Should have dates in 2nd column
        // All of them are the 10th of August
        // 2 US dates, 3 UK dates
        row  = sheet.getRow(0);
        cell = row.getCell((short)1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertEquals("d-mmm-yy", style.getDataFormatString(wb));
        assertTrue(HSSFDateUtil.isInternalDateFormat(style.getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString(wb)));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
        
        row  = sheet.getRow(1);
        cell = row.getCell((short)1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString(wb)));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
        
        row  = sheet.getRow(2);
        cell = row.getCell((short)1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertTrue(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString(wb)));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
        
        row  = sheet.getRow(3);
        cell = row.getCell((short)1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString(wb)));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
        
        row  = sheet.getRow(4);
        cell = row.getCell((short)1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(HSSFDateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(HSSFDateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString(wb)));
        assertTrue(HSSFDateUtil.isCellDateFormatted(cell));
    }
    /*
        //commented out until find the reson.
    public void testDateBug_2Excel() {
        assertEquals(59.0, HSSFDateUtil.getExcelDate(createDate(1900, CALENDAR_FEBRUARY, 28)), 0.00001);
        assertEquals(61.0, HSSFDateUtil.getExcelDate(createDate(1900, CALENDAR_MARCH, 1)), 0.00001);

        assertEquals(37315.00, HSSFDateUtil.getExcelDate(createDate(2002, CALENDAR_FEBRUARY, 28)), 0.00001);
        assertEquals(37316.00, HSSFDateUtil.getExcelDate(createDate(2002, CALENDAR_MARCH, 1)), 0.00001);
        assertEquals(37257.00, HSSFDateUtil.getExcelDate(createDate(2002, CALENDAR_JANUARY, 1)), 0.00001);
        assertEquals(38074.00, HSSFDateUtil.getExcelDate(createDate(2004, CALENDAR_MARCH, 28)), 0.00001);
    }
    */
    
    public void testDateBug_2Java() {
        assertEquals(createDate(1900, Calendar.FEBRUARY, 28), HSSFDateUtil.getJavaDate(59.0));
        assertEquals(createDate(1900, Calendar.MARCH, 1), HSSFDateUtil.getJavaDate(61.0));
        
        assertEquals(createDate(2002, Calendar.FEBRUARY, 28), HSSFDateUtil.getJavaDate(37315.00));
        assertEquals(createDate(2002, Calendar.MARCH, 1), HSSFDateUtil.getJavaDate(37316.00));
        assertEquals(createDate(2002, Calendar.JANUARY, 1), HSSFDateUtil.getJavaDate(37257.00));
        assertEquals(createDate(2004, Calendar.MARCH, 28), HSSFDateUtil.getJavaDate(38074.00));
    }

    private Date createDate(int year, int month, int day) {
        Calendar c = new GregorianCalendar();
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    
    public static void main(String [] args) {
        System.out
                .println("Testing org.apache.poi.hssf.usermodel.TestHSSFDateUtil");
        junit.textui.TestRunner.run(TestHSSFDateUtil.class);
    }
}

