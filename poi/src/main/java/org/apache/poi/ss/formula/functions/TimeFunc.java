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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for the Excel function TIME
 *
 * @author Steven Butler (sebutler @ gmail dot com)
 *
 * Based on POI {@link DateFunc}
 */
public final class TimeFunc extends Fixed3ArgFunction {

	private static final int SECONDS_PER_MINUTE = 60;
	private static final int SECONDS_PER_HOUR = 3600;
	private static final int HOURS_PER_DAY = 24;
	private static final int SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR;


	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		double result;
		try {
			result = evaluate(evalArg(arg0, srcRowIndex, srcColumnIndex), evalArg(arg1, srcRowIndex, srcColumnIndex), evalArg(arg2, srcRowIndex, srcColumnIndex));
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}
	private static int evalArg(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
		if (arg == MissingArgEval.instance) {
			return 0;
		}
		ValueEval ev = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
		// Excel silently truncates double values to integers
		return OperandResolver.coerceValueToInt(ev);
	}
	/**
	 * Converts the supplied hours, minutes and seconds to an Excel time value.
	 *
	 *
	 * @param ds array of 3 doubles containing hours, minutes and seconds.
	 * Non-integer inputs are truncated to an integer before further calculation
	 * of the time value.
	 * @return An Excel representation of a time of day.
	 * If the time value represents more than a day, the days are removed from
	 * the result, leaving only the time of day component.
	 * @throws org.apache.poi.ss.formula.eval.EvaluationException
	 * If any of the arguments are greater than 32767 or the hours
	 * minutes and seconds when combined form a time value less than 0, the function
	 * evaluates to an error.
	 */
	private static double evaluate(int hours, int minutes, int seconds) throws EvaluationException {

		if (hours > 32767 || minutes > 32767 || seconds > 32767) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		int totalSeconds = hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds;

		if (totalSeconds < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return (totalSeconds % SECONDS_PER_DAY) / (double)SECONDS_PER_DAY;
	}
}
