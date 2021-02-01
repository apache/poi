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
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Implementation of Excel functions Date parsing functions:
 *  Date - DAY, MONTH and YEAR
 *  Time - HOUR, MINUTE and SECOND
 *
 * @author Others (not mentioned in code)
 * @author Thies Wellpott
 */
public final class CalendarFieldFunction extends Fixed1ArgFunction {
	public static final Function YEAR = new CalendarFieldFunction(Calendar.YEAR);
	public static final Function MONTH = new CalendarFieldFunction(Calendar.MONTH);
	public static final Function DAY = new CalendarFieldFunction(Calendar.DAY_OF_MONTH);
	public static final Function HOUR = new CalendarFieldFunction(Calendar.HOUR_OF_DAY);
    public static final Function MINUTE = new CalendarFieldFunction(Calendar.MINUTE);
    public static final Function SECOND = new CalendarFieldFunction(Calendar.SECOND);

	private final int _dateFieldId;

	private CalendarFieldFunction(int dateFieldId) {
		_dateFieldId = dateFieldId;
	}

	public final ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
		double val;
		try {
			ValueEval ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			val = OperandResolver.coerceValueToDouble(ve);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		if (val < 0) {
			return ErrorEval.NUM_ERROR;
		}
		return new NumberEval(getCalField(val));
	}

	private int getCalField(double serialDate) {
	   // For some reason, a date of 0 in Excel gets shown
	   //  as the non existant 1900-01-00
		if (((int)serialDate) == 0) {
			switch (_dateFieldId) {
				case Calendar.YEAR: return 1900;
				case Calendar.MONTH: return 1;
				case Calendar.DAY_OF_MONTH: return 0;
			}
			// They want time, that's normal
		}

		// TODO Figure out if we're in 1900 or 1904
		// EXCEL functions round up nearly a half second (probably to prevent floating point
		// rounding issues); use UTC here to prevent daylight saving issues for HOUR
		Calendar c = DateUtil.getJavaCalendarUTC(serialDate + 0.4995 / DateUtil.SECONDS_PER_DAY, false);
		int result = c.get(_dateFieldId);
		
		// Month is a special case due to C semantics
		if (_dateFieldId == Calendar.MONTH) {
			result++;
		}
		
		return result;
	}
}
