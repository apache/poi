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
import java.util.GregorianCalendar;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

/**
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
public final class DateFunc extends NumericFunction.MultiArg {

	public static final Function instance = new DateFunc();

	private DateFunc() {
		super(3,3);
	}

	protected double evaluate(double[] ds) throws EvaluationException {
		int year = getYear(ds[0]);
		int month = (int) ds[1] - 1;
		int day = (int) ds[2];

		if (year < 0 || month < 0 || day < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}

		if (year == 1900 && month == Calendar.FEBRUARY && day == 29) {
			return 60.0;
		}

		if (year == 1900) {
			if ((month == Calendar.JANUARY && day >= 60) ||
					(month == Calendar.FEBRUARY && day >= 30)) {
				day--;
			}
		}

		Calendar c = new GregorianCalendar();

		c.set(year, month, day, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);

		return HSSFDateUtil.getExcelDate(c.getTime(), false); // TODO - fix 1900/1904 problem
	}

	private static int getYear(double d) {
		int year = (int)d;

		if (year < 0) {
			return -1;
		}

		return year < 1900 ? 1900 + year : year;
	}
}
