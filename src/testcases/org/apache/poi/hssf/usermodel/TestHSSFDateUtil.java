
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

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Class TestHSSFDateUtil
 *
 *
 * @author
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Hack Kampbjorn (hak at 2mba.dk)
 * @version %I%, %G%
 */

public class TestHSSFDateUtil
        extends TestCase
{
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

    public static void main(String [] args) {
        System.out
                .println("Testing org.apache.poi.hssf.usermodel.TestHSSFDateUtil");
        junit.textui.TestRunner.run(TestHSSFDateUtil.class);
    }

}
