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

package org.apache.poi.ss.formula.atp;

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.SATURDAY;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.DateUtil;

/**
 * @author jfaenomoto@gmail.com
 */
@SuppressWarnings("deprecation") // YK: heavily uses deprecated {@link java.util.Date(int year, int month, int date)}
public class TestWorkdayCalculator extends TestCase {

    public void testCalculateWorkdaysShouldReturnJustWeekdaysWhenNoWeekend() {
        final double A_MONDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 12));
        final double A_FRIDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 16));
        assertEquals(5, WorkdayCalculator.instance.calculateWorkdays(A_MONDAY, A_FRIDAY, new double[0]));
    }

    public void testCalculateWorkdaysShouldReturnAllDaysButNoSaturdays() {
        final double A_WEDNESDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 14));
        final double A_SATURDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 18));
        assertEquals(3, WorkdayCalculator.instance.calculateWorkdays(A_WEDNESDAY, A_SATURDAY, new double[0]));
    }

    public void testCalculateWorkdaysShouldReturnAllDaysButNoSundays() {
        final double A_SUNDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 11));
        final double A_THURSDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 15));
        assertEquals(4, WorkdayCalculator.instance.calculateWorkdays(A_SUNDAY, A_THURSDAY, new double[0]));
    }

    public void testCalculateWorkdaysShouldReturnAllDaysButNoHolidays() {
        final double A_MONDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 12));
        final double A_FRIDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 16));
        final double A_WEDNESDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 14));
        assertEquals(4, WorkdayCalculator.instance.calculateWorkdays(A_MONDAY, A_FRIDAY, new double[]{ A_WEDNESDAY }));
    }

    public void testCalculateWorkdaysShouldIgnoreWeekendHolidays() {
        final double A_FRIDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 16));
        final double A_SATURDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 17));
        final double A_SUNDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 18));
        final double A_WEDNESDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 21));
        assertEquals(4, WorkdayCalculator.instance.calculateWorkdays(A_FRIDAY, A_WEDNESDAY, new double[]{ A_SATURDAY, A_SUNDAY }));
    }

    public void testPastDaysOfWeekShouldReturn0Past0Saturdays() {
        final double A_WEDNESDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 7));
        final double A_FRIDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 9));
        assertEquals(0, WorkdayCalculator.instance.pastDaysOfWeek(A_WEDNESDAY, A_FRIDAY, SATURDAY));
    }

    public void testPastDaysOfWeekShouldReturn1Past1Saturdays() {
        final double A_WEDNESDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 7));
        final double A_SUNDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 11));
        assertEquals(1, WorkdayCalculator.instance.pastDaysOfWeek(A_WEDNESDAY, A_SUNDAY, SATURDAY));
    }

    public void testPastDaysOfWeekShouldReturn2Past2Saturdays() {
        final double A_THURSDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 8));
        final double A_MONDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 19));
        assertEquals(2, WorkdayCalculator.instance.pastDaysOfWeek(A_THURSDAY, A_MONDAY, SATURDAY));
    }

    public void testPastDaysOfWeekShouldReturn1BeginningFromASaturday() {
        final double A_SATURDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 10));
        final double A_SUNDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 11));
        assertEquals(1, WorkdayCalculator.instance.pastDaysOfWeek(A_SATURDAY, A_SUNDAY, SATURDAY));
    }

    public void testPastDaysOfWeekShouldReturn1EndingAtASaturday() {
        final double A_THURSDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 8));
        final double A_SATURDAY = DateUtil.getExcelDate(new Date(111, DECEMBER, 10));
        assertEquals(1, WorkdayCalculator.instance.pastDaysOfWeek(A_THURSDAY, A_SATURDAY, SATURDAY));
    }
}
