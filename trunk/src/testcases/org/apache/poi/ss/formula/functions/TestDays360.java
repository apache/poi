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

package org.apache.poi.ss.formula.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

public final class TestDays360 {

    /**
     * @param month 1-based
     */
    private static Date makeDate(int year, int month, int day) {
        Calendar cal = LocaleUtil.getLocaleCalendar(year, month-1, day);
        return cal.getTime();
    }

    private static Date decrementDay(Date d) {
        Calendar c = LocaleUtil.getLocaleCalendar();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, -1);
        return c.getTime();
    }

    @Test
    public void testBasic() {
        confirm(120, 2009, 1, 15, 2009, 5, 15);
        confirm(158, 2009, 1, 26, 2009, 7, 4);

        // same results in leap years
        confirm(120, 2008, 1, 15, 2008, 5, 15);
        confirm(158, 2008, 1, 26, 2008, 7, 4);

        // longer time spans
        confirm(562, 2008, 8, 11, 2010, 3, 3);
        confirm(916, 2007, 2, 23, 2009, 9, 9);
        
        // other tests
        confirm(1, makeDate(1993, 2, 28), makeDate(1993, 3, 1), false);
        confirm(1, makeDate(1996, 2, 29), makeDate(1996, 3, 1), false);
        confirm(-2, makeDate(1993, 2, 28), makeDate(1993, 2, 28), false);
        confirm(3, makeDate(1993, 2, 28), makeDate(1993, 3, 1), true);
        confirm(2, makeDate(1996, 2, 29), makeDate(1996, 3, 1), true);

        // from https://support.office.com/en-us/article/DAYS360-function-B9A509FD-49EF-407E-94DF-0CBDA5718C2A
        confirm(1, makeDate(2011, 1, 30), makeDate(2011, 2, 1), false);
        confirm(360, makeDate(2011, 1, 1), makeDate(2011, 12, 31), false);
        confirm(30, makeDate(2011, 1, 1), makeDate(2011, 2, 1), false);
    }

    private static void confirm(int expResult, int y1, int m1, int d1, int y2, int m2, int d2) {
        confirm(expResult, makeDate(y1, m1, d1), makeDate(y2, m2, d2), false);
        confirm(-expResult, makeDate(y2, m2, d2), makeDate(y1, m1, d1), false);
    }
    
    /**
     * The <tt>method</tt> parameter only makes a difference when the second parameter
     * is the last day of the month that does <em>not</em> have 30 days.
     */
    @Test
    public void testMonthBoundaries() {
        // jan
        confirmMonthBoundary(false, 2001, 1, 0, 0, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 1, 0, 0, 1, 2, 3);
        // feb
        confirmMonthBoundary(false, 2001, 2,-2, 1, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 2, 0, 1, 2, 3, 4);
        // mar
        confirmMonthBoundary(false, 2001, 3, 0, 0, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 3, 0, 0, 1, 2, 3);
        // apr
        confirmMonthBoundary(false, 2001, 4, 0, 1, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 4, 0, 1, 2, 3, 4);
        // may
        confirmMonthBoundary(false, 2001, 5, 0, 0, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 5, 0, 0, 1, 2, 3);
        // jun
        confirmMonthBoundary(false, 2001, 6, 0, 1, 2, 3, 4);
        confirmMonthBoundary(true,  2001, 6, 0, 1, 2, 3, 4);
        // leap year
        confirmMonthBoundary(false, 2012, 2, -1, 1, 2, 3, 4);
        confirmMonthBoundary(true,  2012, 2, 0, 1, 2, 3, 4);

        // bug 60029
        Date start = makeDate(2018, 2, 28);
        Date end = makeDate(2018, 3, 31);
        confirm(30, start, end, false);

        // examples from https://support.office.com/en-us/article/DAYS360-function-B9A509FD-49EF-407E-94DF-0CBDA5718C2A
        start = makeDate(2011, 1, 30);
        end = makeDate(2011, 2, 1);
        confirm(1, start, end, false);

        start = makeDate(2011, 1, 1);
        end = makeDate(2011, 12, 31);
        confirm(360, start, end, false);

        start = makeDate(2011, 1, 1);
        end = makeDate(2011, 2, 1);
        confirm(30, start, end, false);
    }


    /**
     * @param monthNo 1-based
     */
    private static void confirmMonthBoundary(boolean method, int year, int monthNo, int...diffs) {
        Date firstDayOfNextMonth = makeDate(year, monthNo+1, 1);
        Date secondArg = decrementDay(firstDayOfNextMonth);
        Date firstArg = secondArg;

        for (int expResult : diffs) {
            confirm(expResult, firstArg, secondArg, method);
            firstArg = decrementDay(firstArg);
        }

    }
    private static void confirm(int expResult, Date firstArg, Date secondArg, boolean method) {
        ValueEval ve;
        if (method) {
            ve = invokeDays360(convert(firstArg), convert(secondArg), BoolEval.TRUE);
        } else {
            ve = invokeDays360(convert(firstArg), convert(secondArg));
        }
        assertTrue("wrong return type (" + ve.getClass().getName() + ")", ve instanceof NumberEval);

        NumberEval numberEval = (NumberEval) ve;
        String err = String.format(Locale.ROOT, "days360(%tF,%tF,%b) wrong result", firstArg, secondArg, method);
        assertEquals(err, expResult, numberEval.getNumberValue(), 0);
    }
    
    private static ValueEval invokeDays360(ValueEval...args) {
        return new Days360().evaluate(args, -1, -1);
    }
    
    private static NumberEval convert(Date d) {
        return new NumberEval(HSSFDateUtil.getExcelDate(d));
    }
}
