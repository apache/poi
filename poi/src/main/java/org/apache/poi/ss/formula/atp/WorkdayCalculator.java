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

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Removal;

/**
 * A calculator for workdays, considering dates as excel representations.
 */
public class WorkdayCalculator {
    public static final WorkdayCalculator instance = new WorkdayCalculator();

    /**
     * Constructor.
     */
    private WorkdayCalculator() {
        // enforcing singleton
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
        int saturdaysPast = this.pastDaysOfWeek(start, end, Calendar.SATURDAY);
        int sundaysPast = this.pastDaysOfWeek(start, end, Calendar.SUNDAY);
        int nonWeekendHolidays = this.calculateNonWeekendHolidays(start, end, holidays);
        return (int) (end - start + 1) - saturdaysPast - sundaysPast - nonWeekendHolidays;
    }

    /**
     * Calculate the workday past x workdays from a starting date, considering a range of holidays.
     *
     * @param start start date.
     * @param workdays number of workdays to be past from starting date.
     * @param holidays an array of holidays.
     * @return date past x workdays.
     */
	public Date calculateWorkdays(double start, int workdays, double[] holidays) {
		Date startDate = DateUtil.getJavaDate(start);
		int direction = workdays < 0 ? -1 : 1;
		Calendar endDate = LocaleUtil.getLocaleCalendar();
		endDate.setTime(startDate);
		double excelEndDate = DateUtil.getExcelDate(endDate.getTime());
		while (workdays != 0) {
			endDate.add(Calendar.DAY_OF_YEAR, direction);
			excelEndDate += direction;
			if (endDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& endDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
					&& !isHoliday(excelEndDate,	holidays)) {
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
        int startDay = (int) Math.floor(start < end ? start : end);
        int endDay = (int) Math.floor(end > start ? end : start);
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
        double startDay = start < end ? start : end;
        double endDay = end > start ? end : start;
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
        return date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
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
