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

package org.apache.poi.hssf.record.formula.functions;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

/**
 * Implementation of Excel functions DAY, MONTH and YEAR
 *
 *
 * @author Guenter Kickinger g.kickinger@gmx.net
 */
public final class CalendarFieldFunction implements Function {

	public static final Function YEAR = new CalendarFieldFunction(Calendar.YEAR, false);
	public static final Function MONTH = new CalendarFieldFunction(Calendar.MONTH, true);
	public static final Function DAY = new CalendarFieldFunction(Calendar.DAY_OF_MONTH, false);

	private final int _dateFieldId;
	private final boolean _needsOneBaseAdjustment;

	private CalendarFieldFunction(int dateFieldId, boolean needsOneBaseAdjustment) {
		_dateFieldId = dateFieldId;
		_needsOneBaseAdjustment = needsOneBaseAdjustment;
	}

	public ValueEval evaluate(ValueEval[] operands, int srcCellRow, short srcCellCol) {
		if (operands.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}

		int val;
		try {
			ValueEval ve = OperandResolver.getSingleValue(operands[0], srcCellRow, srcCellCol);
			val = OperandResolver.coerceValueToInt(ve);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		if (val < 0) {
			return ErrorEval.NUM_ERROR;
		}
		return new NumberEval(getCalField(val));
	}

	private int getCalField(int serialDay) {
		if (serialDay == 0) {
			// Special weird case
			// day zero should be 31-Dec-1899,  but Excel seems to think it is 0-Jan-1900
			switch (_dateFieldId) {
				case Calendar.YEAR: return 1900;
				case Calendar.MONTH: return 1;
				case Calendar.DAY_OF_MONTH: return 0;
			}
			throw new IllegalStateException("bad date field " + _dateFieldId);
		}
		Date d = HSSFDateUtil.getJavaDate(serialDay, false); // TODO fix 1900/1904 problem

		Calendar c = new GregorianCalendar();
		c.setTime(d);

		int result = c.get(_dateFieldId);
		if (_needsOneBaseAdjustment) {
			result++;
		}
		return result;
	}
}
