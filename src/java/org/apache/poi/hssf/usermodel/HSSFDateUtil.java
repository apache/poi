
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * DateUtil.java
 *
 * Created on January 19, 2002, 9:30 AM
 */
package org.apache.poi.hssf.usermodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Contains methods for dealing with Excel dates.
 *
 * @author  Michael Harhen
 * @author  Glen Stampoultzis (glens at apache.org)
 */

public class HSSFDateUtil
{
    private HSSFDateUtil()
    {
    }

    private static final int    BAD_DATE          =
        -1;   // used to specify that date is invalid
    private static final long   DAY_MILLISECONDS  = 24 * 60 * 60 * 1000;
    private static final double CAL_1900_ABSOLUTE =
        ( double ) absoluteDay(new GregorianCalendar(1900, Calendar
        .JANUARY, 1)) - 2.0;

    /**
     * Given a Date, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param  date the Date
     */

    public static double getExcelDate(Date date)
    {
        Calendar calStart = new GregorianCalendar();

        calStart.setTime(
            date);   // If date includes hours, minutes, and seconds, set them to 0
        if (calStart.get(Calendar.YEAR) < 1900)
        {
            return BAD_DATE;
        }
        else
        {
            calStart = dayStart(calStart);
            double fraction = (date.getTime() - calStart.getTime().getTime())
                              / ( double ) DAY_MILLISECONDS;

            return fraction + ( double ) absoluteDay(calStart)
                   - CAL_1900_ABSOLUTE;
        }
    }

    /**
     * Given a excel date, converts it into a Date.
     *
     * @param  date the Excel Date
     *
     * @return Java representation of a date (null if error)
     */

    public static Date getJavaDate(double date)
    {
        if (isValidExcelDate(date))
        {
            int               wholeDaysSince1900 = ( int ) Math.floor(date);
            GregorianCalendar calendar           = new GregorianCalendar(1900,
                                                       0, wholeDaysSince1900
                                                       - 1);
            int               millisecondsInDay  =
                ( int ) ((date - Math.floor(date))
                         * ( double ) DAY_MILLISECONDS + 0.5);

            calendar.set(GregorianCalendar.MILLISECOND, millisecondsInDay);
            return calendar.getTime();
        }
        else
        {
            return null;
        }
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
     * Given a Calendar, return the number of days since 1600/12/31.
     *
     * @return days number of days since 1600/12/31
     * @param  cal the Calendar
     * @exception IllegalArgumentException if date is invalid
     */

    private static int absoluteDay(Calendar cal)
    {
        return cal.get(Calendar.DAY_OF_YEAR)
               + daysInPriorYears(cal.get(Calendar.YEAR));
    }

    /**
     * Return the number of days in prior years since 1601
     *
     * @return    days  number of days in years prior to yr.
     * @param     yr    a year (1600 < yr < 4000)
     * @exception IllegalArgumentException if year is outside of range.
     */

    private static int daysInPriorYears(int yr)
    {
        if (yr < 1601)
        {
            throw new IllegalArgumentException(
                "'year' must be 1601 or greater");
        }
        int y    = yr - 1601;
        int days = 365 * y      // days in prior years
                   + y / 4      // plus julian leap days in prior years
                   - y / 100    // minus prior century years
                   + y / 400;   // plus years divisible by 400

        return days;
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
