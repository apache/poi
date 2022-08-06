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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * A calculator for workdays, considering dates as excel representations.
 */
public class WorkdayCalculator {
    public static final WorkdayCalculator instance = new WorkdayCalculator();

    private static final Set<Integer> standardWeekend =
            new HashSet<>(Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY));
    private static final Set<Integer> sunMonWeekend =
            new HashSet<>(Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY));
    private static final Set<Integer> monTuesWeekend =
            new HashSet<>(Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY));
    private static final Set<Integer> tuesWedsWeekend =
            new HashSet<>(Arrays.asList(Calendar.TUESDAY, Calendar.WEDNESDAY));
    private static final Set<Integer> wedsThursWeekend =
            new HashSet<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.THURSDAY));
    private static final Set<Integer> thursFriWeekend =
            new HashSet<>(Arrays.asList(Calendar.THURSDAY, Calendar.FRIDAY));
    private static final Set<Integer> friSatWeekend =
            new HashSet<>(Arrays.asList(Calendar.FRIDAY, Calendar.SATURDAY));
    private static final Set<Integer> monWeekend =  Collections.singleton(Calendar.MONDAY);
    private static final Set<Integer> tuesWeekend =  Collections.singleton(Calendar.TUESDAY);
    private static final Set<Integer> wedsWeekend =  Collections.singleton(Calendar.WEDNESDAY);
    private static final Set<Integer> thursWeekend = Collections.singleton(Calendar.THURSDAY);
    private static final Set<Integer> friWeekend = Collections.singleton(Calendar.FRIDAY);
    private static final Set<Integer> satWeekend = Collections.singleton(Calendar.SATURDAY);
    private static final Set<Integer> sunWeekend = Collections.singleton(Calendar.SUNDAY);
    private static final Map<Integer, Set<Integer>> weekendTypeMap = new HashMap<>();

    static {
        weekendTypeMap.put(1, standardWeekend);
        weekendTypeMap.put(2, sunMonWeekend);
        weekendTypeMap.put(3, monTuesWeekend);
        weekendTypeMap.put(4, tuesWedsWeekend);
        weekendTypeMap.put(5, wedsThursWeekend);
        weekendTypeMap.put(6, thursFriWeekend);
        weekendTypeMap.put(7, friSatWeekend);
        weekendTypeMap.put(11, sunWeekend);
        weekendTypeMap.put(12, monWeekend);
        weekendTypeMap.put(13, tuesWeekend);
        weekendTypeMap.put(14, wedsWeekend);
        weekendTypeMap.put(15, thursWeekend);
        weekendTypeMap.put(16, friWeekend);
        weekendTypeMap.put(17, satWeekend);
    }

    /**
     * Constructor.
     */
    private WorkdayCalculator() {
        // enforcing singleton
    }

    public Set<Integer> getValidWeekendTypes() {
        return weekendTypeMap.keySet();
    }

    /**
     * Calculate how many workdays are there between a start and an end date, as excel representations, considering a range of holidays.
     *
     * @param start start date.
     * @param end end date.
     * @param holidays an array of holidays.
     * @return number of workdays between start and end dates, including both dates.
     */
    public int calculateWorkdays(double start, double end, double[] holidays) {
        Integer[] weekendDays = new Integer[standardWeekend.size()];
        weekendDays = standardWeekend.toArray(weekendDays);
        int weekendDay1Past = weekendDays.length == 0 ? 0 : this.pastDaysOfWeek(start, end, weekendDays[0]);
        int weekendDay2Past = weekendDays.length <= 1 ? 0 : this.pastDaysOfWeek(start, end, weekendDays[1]);
        int nonWeekendHolidays = this.calculateNonWeekendHolidays(start, end, holidays);
        return (int) (end - start + 1) - weekendDay1Past - weekendDay2Past - nonWeekendHolidays;
    }

    /**
     * Calculate the workday past x workdays from a starting date, considering a range of holidays.
     * Uses Sat/Sun weekend.
     *
     * @param start start date.
     * @param workdays number of workdays to be past from starting date.
     * @param holidays an array of holidays.
     * @return date past x workdays.
     */
    public Date calculateWorkdays(double start, int workdays, double[] holidays) {
        return calculateWorkdays(start, workdays, 1, holidays);
    }

    /**
     * Calculate the workday past x workdays from a starting date, considering a range of holidays.
     *
     * @param start start date.
     * @param workdays number of workdays to be past from starting date.
     * @param weekendType weekend parameter (see https://support.microsoft.com/en-us/office/workday-intl-function-a378391c-9ba7-4678-8a39-39611a9bf81d)
     * @param holidays an array of holidays.
     * @return date past x workdays.
     */
    public Date calculateWorkdays(double start, int workdays, int weekendType, double[] holidays) {
        Set<Integer> weekendDays = weekendTypeMap.getOrDefault(weekendType, standardWeekend);
        Date startDate = DateUtil.getJavaDate(start);
        int direction = workdays < 0 ? -1 : 1;
        Calendar endDate = LocaleUtil.getLocaleCalendar();
        endDate.setTime(startDate);
        double excelEndDate = DateUtil.getExcelDate(endDate.getTime());
        while (workdays != 0) {
            endDate.add(Calendar.DAY_OF_YEAR, direction);
            excelEndDate += direction;
            if (!isWeekend(endDate, weekendDays) && !isHoliday(excelEndDate, holidays)) {
                workdays -= direction;
            }
        }
        return endDate.getTime();
    }

    /**
     * Calculates how many days of week past between a start and an end date.
     *
     * @param start start date.
     * @param end end date.
     * @param dayOfWeek a day of week as represented by {@link Calendar} constants.
     * @return how many days of week past in this interval.
     */
    protected int pastDaysOfWeek(double start, double end, int dayOfWeek) {
        int pastDaysOfWeek = 0;
        int startDay = (int) Math.floor(Math.min(start, end));
        int endDay = (int) Math.floor(Math.max(end, start));
        for (; startDay <= endDay; startDay++) {
            Calendar today = LocaleUtil.getLocaleCalendar();
            today.setTime(DateUtil.getJavaDate(startDay));
            if (today.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                pastDaysOfWeek++;
            }
        }
        return start <= end ? pastDaysOfWeek : -pastDaysOfWeek;
    }

    /**
     * Calculates how many holidays in a list are workdays, considering an interval of dates.
     *
     * @param start start date.
     * @param end end date.
     * @param holidays an array of holidays.
     * @return number of holidays that occur in workdays, between start and end dates.
     */
    protected int calculateNonWeekendHolidays(double start, double end, double[] holidays) {
        int nonWeekendHolidays = 0;
        double startDay = Math.min(start, end);
        double endDay = Math.max(end, start);
        for (double holiday : holidays) {
            if (isInARange(startDay, endDay, holiday)) {
                if (!isWeekend(holiday)) {
                    nonWeekendHolidays++;
                }
            }
        }
        return start <= end ? nonWeekendHolidays : -nonWeekendHolidays;
    }

    /**
     * @param aDate a given date.
     * @return <code>true</code> if date is weekend, <code>false</code> otherwise.
     */
    protected boolean isWeekend(double aDate) {
        Calendar date = LocaleUtil.getLocaleCalendar();
        date.setTime(DateUtil.getJavaDate(aDate));
        return isWeekend(date);
    }

    private boolean isWeekend(Calendar date) {
        return isWeekend(date, standardWeekend);
    }

    private boolean isWeekend(Calendar date, Set<Integer> weekendDays) {
        return weekendDays.contains(date.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * @param aDate a given date.
     * @param holidays an array of holidays.
     * @return <code>true</code> if date is a holiday, <code>false</code> otherwise.
     */
    protected boolean isHoliday(double aDate, double[] holidays) {
        for (double holiday : holidays) {
            if (Math.round(holiday) == Math.round(aDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param start start date.
     * @param end end date.
     * @param aDate a date to be analyzed.
     * @return <code>true</code> if aDate is between start and end dates, <code>false</code> otherwise.
     */
    protected boolean isInARange(double start, double end, double aDate) {
        return aDate >= start && aDate <= end;
    }

}
