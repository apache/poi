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

package org.apache.poi.hssf.record.formula.atp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.usermodel.DateUtil;
/**
 * Implementation of Excel 'Analysis ToolPak' function YEARFRAC()<br/>
 *
 * Returns the fraction of the year spanned by two dates.<p/>
 *
 * <b>Syntax</b><br/>
 * <b>YEARFRAC</b>(<b>startDate</b>, <b>endDate</b>, basis)<p/>
 *
 * The <b>basis</b> optionally specifies the behaviour of YEARFRAC as follows:
 *
 * <table border="0" cellpadding="1" cellspacing="0" summary="basis parameter description">
 *   <tr><th>Value</th><th>Days per Month</th><th>Days per Year</th></tr>
 *   <tr align='center'><td>0 (default)</td><td>30</td><td>360</td></tr>
 *   <tr align='center'><td>1</td><td>actual</td><td>actual</td></tr>
 *   <tr align='center'><td>2</td><td>actual</td><td>360</td></tr>
 *   <tr align='center'><td>3</td><td>actual</td><td>365</td></tr>
 *   <tr align='center'><td>4</td><td>30</td><td>360</td></tr>
 * </table>
 *
 */
final class YearFrac implements FreeRefFunction {

	public static final FreeRefFunction instance = new YearFrac();

	private YearFrac() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		int srcCellRow = ec.getRowIndex();
		int srcCellCol = ec.getColumnIndex();
		double result;
		try {
			int basis = 0; // default
			switch(args.length) {
				case 3:
					basis = evaluateIntArg(args[2], srcCellRow, srcCellCol);
				case 2:
					break;
				default:
					return ErrorEval.VALUE_INVALID;
			}
			double startDateVal = evaluateDateArg(args[0], srcCellRow, srcCellCol);
			double endDateVal = evaluateDateArg(args[1], srcCellRow, srcCellCol);
			result = YearFracCalculator.calculate(startDateVal, endDateVal, basis);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		return new NumberEval(result);
	}

	private static double evaluateDateArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, (short) srcCellCol);

		if (ve instanceof StringEval) {
			String strVal = ((StringEval) ve).getStringValue();
			Double dVal = OperandResolver.parseDouble(strVal);
			if (dVal != null) {
				return dVal.doubleValue();
			}
			Calendar date = parseDate(strVal);
			return DateUtil.getExcelDate(date, false);
		}
		return OperandResolver.coerceValueToDouble(ve);
	}

	private static Calendar parseDate(String strVal) throws EvaluationException {
		String[] parts = Pattern.compile("/").split(strVal);
		if (parts.length != 3) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		String part2 = parts[2];
		int spacePos = part2.indexOf(' ');
		if (spacePos > 0) {
			// drop time portion if present
			part2 = part2.substring(0, spacePos);
		}
		int f0;
		int f1;
		int f2;
		try {
			f0 = Integer.parseInt(parts[0]);
			f1 = Integer.parseInt(parts[1]);
			f2 = Integer.parseInt(part2);
		} catch (NumberFormatException e) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		if (f0<0 || f1<0 || f2<0 || (f0>12 && f1>12 && f2>12)) {
			// easy to see this cannot be a valid date
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}

		if (f0 >= 1900 && f0 < 9999) {
			// when 4 digit value appears first, the format is YYYY/MM/DD, regardless of OS settings
			return makeDate(f0, f1, f2);
		}
		// otherwise the format seems to depend on OS settings (default date format)
		if (false) {
			// MM/DD/YYYY is probably a good guess, if the in the US
			return makeDate(f2, f0, f1);
		}
		// TODO - find a way to choose the correct date format
		throw new RuntimeException("Unable to determine date format for text '" + strVal + "'");
	}

	/**
	 * @param month 1-based
	 */
	private static Calendar makeDate(int year, int month, int day) throws EvaluationException {
		if (month < 1 || month > 12) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		Calendar cal = new GregorianCalendar(year, month-1, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (day <1 || day>cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal;
	}

	private static int evaluateIntArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, (short) srcCellCol);
		return OperandResolver.coerceValueToInt(ve);
	}
}
