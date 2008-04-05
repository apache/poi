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



/*
 * DateUtil.java
 *
 * Created on January 19, 2002, 9:30 AM
 */
package org.apache.poi.ss.usermodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Contains methods for dealing with Excel dates.
 *
 * @author  Michael Harhen
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Hack Kampbjorn (hak at 2mba.dk)
 * @author  Alex Jacoby (ajacoby at gmail.com)
 * @author  Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */

public class DateUtil
{
    protected DateUtil()
    {
    }

    private static final int    BAD_DATE         =
        -1;   // used to specify that date is invalid
    private static final long   DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
    
    /**
     * Given a Date, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param  date the Date
     */
    public static double getExcelDate(Date date) {
    	return getExcelDate(date, false);
    }
    /**
     * Given a Date, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Date
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(Date date, boolean use1904windowing) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTime(date);   // If date includes hours, minutes, and seconds, set them to 0
        return internalGetExcelDate(calStart, use1904windowing);
    }
    /**
     * Given a Date in the form of a Calendar, converts it into a double
     *  representing its internal Excel representation, which is the 
     *  number of days since 1/1/1900. Fractional days represent hours, 
     *  minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Calendar holding the date to convert
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(Calendar date, boolean use1904windowing) {
    	// Don't alter the supplied Calendar as we do our work
    	return internalGetExcelDate( (Calendar)date.clone(), use1904windowing );
    }
    private static double internalGetExcelDate(Calendar date, boolean use1904windowing) {
        if ((!use1904windowing && date.get(Calendar.YEAR) < 1900) || 
            (use1904windowing && date.get(Calendar.YEAR) < 1904)) 
        {
            return BAD_DATE;
        } else {
	    // Because of daylight time saving we cannot use
	    //     date.getTime() - calStart.getTimeInMillis()
	    // as the difference in milliseconds between 00:00 and 04:00
	    // can be 3, 4 or 5 hours but Excel expects it to always
	    // be 4 hours.
	    // E.g. 2004-03-28 04:00 CEST - 2004-03-28 00:00 CET is 3 hours
	    // and 2004-10-31 04:00 CET - 2004-10-31 00:00 CEST is 5 hours
            double fraction = (((date.get(Calendar.HOUR_OF_DAY) * 60
                                 + date.get(Calendar.MINUTE)
                                ) * 60 + date.get(Calendar.SECOND)
                               ) * 1000 + date.get(Calendar.MILLISECOND)
                              ) / ( double ) DAY_MILLISECONDS;
            Calendar calStart = dayStart(date);
            
            double value = fraction + absoluteDay(calStart, use1904windowing);
            
            if (!use1904windowing && value >= 60) {
                value++;
            } else if (use1904windowing) {
                value--;
            }
            
            return value;
        }
    }
    
    /**
     *  Given an Excel date with using 1900 date windowing, and
     *   converts it to a java.util.Date.
     *
     *  NOTE: If the default <code>TimeZone</code> in Java uses Daylight
     *  Saving Time then the conversion back to an Excel date may not give
     *  the same value, that is the comparison
     *  <CODE>excelDate == getExcelDate(getJavaDate(excelDate,false))</CODE>
     *  is not always true. For example if default timezone is
     *  <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after
     *  01:59 CET is 03:00 CEST, if the excel date represents a time between
     *  02:00 and 03:00 then it is converted to past 03:00 summer time
     *  
     *  @param date  The Excel date.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     *  @see java.util.TimeZone
     */
    public static Date getJavaDate(double date) {
    	return getJavaDate(date, false);
    }
    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.util.Date.
     *
     *  NOTE: If the default <code>TimeZone</code> in Java uses Daylight
     *  Saving Time then the conversion back to an Excel date may not give
     *  the same value, that is the comparison
     *  <CODE>excelDate == getExcelDate(getJavaDate(excelDate,false))</CODE>
     *  is not always true. For example if default timezone is
     *  <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after
     *  01:59 CET is 03:00 CEST, if the excel date represents a time between
     *  02:00 and 03:00 then it is converted to past 03:00 summer time
     *
     *  @param date  The Excel date.
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     *  @see java.util.TimeZone
     */
    public static Date getJavaDate(double date, boolean use1904windowing) {
        if (isValidExcelDate(date)) {
            int startYear = 1900;
            int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
            int wholeDays = (int)Math.floor(date);
            if (use1904windowing) {
                startYear = 1904;
                dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
            }
            else if (wholeDays < 61) {
                // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
                // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
                dayAdjust = 0;
            }
            GregorianCalendar calendar = new GregorianCalendar(startYear,0,
                                                     wholeDays + dayAdjust);
            int millisecondsInDay = (int)((date - Math.floor(date)) * 
                                          DAY_MILLISECONDS + 0.5);
            calendar.set(GregorianCalendar.MILLISECOND, millisecondsInDay);
            return calendar.getTime();
        }
        else {
            return null;
        }
    }
    
    /**
     * Given a format ID and its format String, will check to see if the
     *  format represents a date format or not.
     * Firstly, it will check to see if the format ID corresponds to an
     *  internal excel date format (eg most US date formats) 
     * If not, it will check to see if the format string only contains
     *  date formatting characters (ymd-/), which covers most
     *  non US date formats.
     *  
     * @param formatIndex The index of the format, eg from ExtendedFormatRecord.getFormatIndex
     * @param formatString The format string, eg from FormatRecord.getFormatString
     * @see #isInternalDateFormat(int)
     */
    public static boolean isADateFormat(int formatIndex, String formatString) {
    	// First up, is this an internal date format?
    	if(isInternalDateFormat(formatIndex)) {
    		return true;
    	}
    	
    	// If we didn't get a real string, it can't be
    	if(formatString == null || formatString.length() == 0) {
    		return false;
    	}
    	
    	String fs = formatString;
    	
    	// Translate \- into just -, before matching
    	fs = fs.replaceAll("\\\\-","-");
    	// And \, into ,
    	fs = fs.replaceAll("\\\\,",",");
    	// And '\ ' into ' '
    	fs = fs.replaceAll("\\\\ "," ");
    	
    	// If it end in ;@, that's some crazy dd/mm vs mm/dd
    	//  switching stuff, which we can ignore
    	fs = fs.replaceAll(";@", "");
    	
    	// If it starts with [$-...], then it is a date, but
    	//  who knows what that starting bit is all about
    	fs = fs.replaceAll("\\[\\$\\-.*?\\]", "");
    	
    	// Otherwise, check it's only made up, in any case, of:
    	//  y m d h s - / , . :
    	if(fs.matches("^[yYmMdDhHsS\\-/,. :]+$")) {
    		return true;
    	}
    	
    	return false;
    }

    /**
     * Given a format ID this will check whether the format represents
     *  an internal excel date format or not.
     * @see #isADateFormat(int, java.lang.String)  
     */
    public static boolean isInternalDateFormat(int format) {
      boolean retval =false;

            switch(format) {
                // Internal Date Formats as described on page 427 in
                // Microsoft Excel Dev's Kit...
                case 0x0e:
                case 0x0f:
                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x2d:
                case 0x2e:
                case 0x2f:
                    retval = true;
                    break;
                    
                default:
                    retval = false;
                    break;
            }
       return retval;
    }

    /**
     *  Check if a cell contains a date
     *  Since dates are stored internally in Excel as double values 
     *  we infer it is a date if it is formatted as such. 
     *  @see #isADateFormat(int, String)
     *  @see #isInternalDateFormat(int)
     */
    public static boolean isCellDateFormatted(Cell cell) {
        if (cell == null) return false;
        boolean bDate = false;
        
        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            if(style == null) return false;
            
            int i = style.getDataFormat();
            String f = style.getDataFormatString();
            bDate = isADateFormat(i, f);
        }
        return bDate;
    }
    /**
     *  Check if a cell contains a date, checking only for internal
     *   excel date formats.
     *  As Excel stores a great many of its dates in "non-internal"
     *   date formats, you will not normally want to use this method.
     *  @see #isADateFormat(int,String)
     *  @see #isInternalDateFormat(int)
     */
    public static boolean isCellInternalDateFormatted(Cell cell) {
        if (cell == null) return false;
        boolean bDate = false;
        
        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            int i = style.getDataFormat();
            bDate = isInternalDateFormat(i);
        }
        return bDate;
    }


    /**
     * Given a double, checks if it is a valid Excel date.
     *
     * @return true if valid
     * @param  value the double value
     */

    public static boolean isValidExcelDate(double value)
    {
        return (value > -Double.MIN_VALUE);
    }

    /**
     * Given a Calendar, return the number of days since 1900/12/31.
     *
     * @return days number of days since 1900/12/31
     * @param  cal the Calendar
     * @exception IllegalArgumentException if date is invalid
     */

    protected static int absoluteDay(Calendar cal, boolean use1904windowing)
    {
        return cal.get(Calendar.DAY_OF_YEAR)
               + daysInPriorYears(cal.get(Calendar.YEAR), use1904windowing);
    }

    /**
     * Return the number of days in prior years since 1900
     *
     * @return    days  number of days in years prior to yr.
     * @param     yr    a year (1900 < yr < 4000)
     * @param use1904windowing 
     * @exception IllegalArgumentException if year is outside of range.
     */

    private static int daysInPriorYears(int yr, boolean use1904windowing)
    {
        if ((!use1904windowing && yr < 1900) || (use1904windowing && yr < 1900)) {
            throw new IllegalArgumentException("'year' must be 1900 or greater");
        }
        
        int yr1  = yr - 1;
        int leapDays =   yr1 / 4   // plus julian leap days in prior years
                       - yr1 / 100 // minus prior century years
                       + yr1 / 400 // plus years divisible by 400 
                       - 460;      // leap days in previous 1900 years
        
        return 365 * (yr - (use1904windowing ? 1904 : 1900)) + leapDays;
    }
    
    // set HH:MM:SS fields of cal to 00:00:00:000
    private static Calendar dayStart(final Calendar cal)
    {
        cal.get(Calendar
            .HOUR_OF_DAY);   // force recalculation of internal fields
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.get(Calendar
            .HOUR_OF_DAY);   // force recalculation of internal fields
        return cal;
    }

    // ---------------------------------------------------------------------------------------------------------
}
