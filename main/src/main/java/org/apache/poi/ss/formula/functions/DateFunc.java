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

import java.util.Calendar;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;


/**
 * Implementation for the Excel function DATE
 */
public final class DateFunc extends Fixed3ArgFunction {
	public static final Function instance = new DateFunc();

	private DateFunc() {
		// no fields to initialise
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		double result;
		try {
			double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
			double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
			double d2 = NumericFunction.singleOperandEvaluate(arg2, srcRowIndex, srcColumnIndex);
			result = evaluate(getYear(d0), (int) (d1 - 1), (int) d2);
			NumericFunction.checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}

	/**
	 * Note - works with Java Calendar months, not Excel months
	 */
	private static double evaluate(int year, int month, int pDay) throws EvaluationException {
	   // We don't support negative years yet
		if (year < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		// Negative months are fairly easy
		while (month < 0) {
		   year--;
		   month += 12;
		}
		// Negative days are handled by the Java Calendar
		
		// Excel has bugs around leap years in 1900, handle them
		// Special case for the non-existant 1900 leap year
		if (year == 1900 && month == Calendar.FEBRUARY && pDay == 29) {
			return 60.0;
		}

		// If they give a date in 1900 in Jan/Feb, with the days
		//  putting it past the leap year, adjust
		int day = pDay;
		if (year == 1900) {
			if ((month == Calendar.JANUARY && day >= 60) ||
					(month == Calendar.FEBRUARY && day >= 30)) {
				day--;
			}
		}

		// Turn this into a Java date
		Calendar c = LocaleUtil.getLocaleCalendar(year, month, day);
		
		// Handle negative days of the week, that pull us across
		//  the 29th of Feb 1900
		if (pDay < 0 && c.get(Calendar.YEAR) == 1900 &&
		      month > Calendar.FEBRUARY && 
		      c.get(Calendar.MONTH) < Calendar.MARCH) {
		   c.add(Calendar.DATE, 1);
		}

		// TODO Identify if we're doing 1900 or 1904 date windowing
		boolean use1904windowing = false;
		
		// Have this Java date turned back into an Excel one
		return DateUtil.getExcelDate(c.getTime(), use1904windowing);
	}

	private static int getYear(double d) {
		int year = (int)d;

		if (year < 0) {
			return -1;
		}

		return year < 1900 ? 1900 + year : year;
	}
}
