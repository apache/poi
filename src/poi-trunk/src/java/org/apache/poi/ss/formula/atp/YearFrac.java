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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.usermodel.DateUtil;
/**
 * Implementation of Excel 'Analysis ToolPak' function YEARFRAC()<br>
 *
 * Returns the fraction of the year spanned by two dates.<p>
 *
 * <b>Syntax</b><br>
 * <b>YEARFRAC</b>(<b>startDate</b>, <b>endDate</b>, basis)<p>
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
					// fall through
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
			Calendar date = DateParser.parseDate(strVal);
			return DateUtil.getExcelDate(date, false);
		}
		return OperandResolver.coerceValueToDouble(ve);
	}

	private static int evaluateIntArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, (short) srcCellCol);
		return OperandResolver.coerceValueToInt(ve);
	}
}
