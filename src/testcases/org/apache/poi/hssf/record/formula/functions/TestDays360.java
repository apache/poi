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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

/**
 * @author Josh Micich
 */
public final class TestDays360 extends TestCase {

	/**
	 * @param month 1-based
	 */
	private static Date makeDate(int year, int month, int day) {

		Calendar cal = new GregorianCalendar(year, month-1, day, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	private static Date decrementDay(Date d) {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(d.getTime());
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c.getTime();
	}
	private static String fmt(Date d) {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(d.getTime());
		StringBuilder sb = new StringBuilder();
		sb.append(c.get(Calendar.YEAR));
		sb.append("/");
		sb.append(c.get(Calendar.MONTH)+1);
		sb.append("/");
		sb.append(c.get(Calendar.DAY_OF_MONTH));
		return sb.toString();
	}


	public void testBasic() {
		confirm(120, 2009, 1, 15, 2009, 5, 15);
		confirm(158, 2009, 1, 26, 2009, 7, 4);

		// same results in leap years
		confirm(120, 2008, 1, 15, 2008, 5, 15);
		confirm(158, 2008, 1, 26, 2008, 7, 4);

		// longer time spans
		confirm(562, 2008, 8, 11, 2010, 3, 3);
		confirm(916, 2007, 2, 23, 2009, 9, 9);
	}

	private static void confirm(int expResult, int y1, int m1, int d1, int y2, int m2, int d2) {
		confirm(expResult, makeDate(y1, m1, d1), makeDate(y2, m2, d2), false);
		confirm(-expResult, makeDate(y2, m2, d2), makeDate(y1, m1, d1), false);

	}
	/**
	 * The <tt>method</tt> parameter only makes a difference when the second parameter
	 * is the last day of the month that does <em>not</em> have 30 days.
	 */
	public void DISABLED_testMonthBoundaries() {
		// jan
		confirmMonthBoundary(false, 1, 0, 0, 2, 3, 4);
		confirmMonthBoundary(true,  1, 0, 0, 1, 3, 4);
		// feb
		confirmMonthBoundary(false, 2,-2, 1, 2, 3, 4);
		confirmMonthBoundary(true,  2, 0, 1, 2, 3, 4);
		// mar
		confirmMonthBoundary(false, 3, 0, 0, 2, 3, 4);
		confirmMonthBoundary(true,  3, 0, 0, 1, 3, 4);
		// apr
		confirmMonthBoundary(false, 4, 0, 1, 2, 3, 4);
		confirmMonthBoundary(true,  4, 0, 1, 2, 3, 4);
		// may
		confirmMonthBoundary(false, 5, 0, 0, 2, 3, 4);
		confirmMonthBoundary(true,  5, 0, 0, 1, 3, 4);
		// jun
		confirmMonthBoundary(false, 6, 0, 1, 2, 3, 4);
		confirmMonthBoundary(true,  6, 0, 1, 2, 3, 4);
		// etc...
	}


	/**
	 * @param monthNo 1-based
	 * @param diffs
	 */
	private static void confirmMonthBoundary(boolean method, int monthNo, int...diffs) {
		Date firstDayOfNextMonth = makeDate(2001, monthNo+1, 1);
		Date secondArg = decrementDay(firstDayOfNextMonth);
		Date firstArg = secondArg;

		for (int i = 0; i < diffs.length; i++) {
			int expResult = diffs[i];
			confirm(expResult, firstArg, secondArg, method);
			firstArg = decrementDay(firstArg);
		}

	}
	private static void confirm(int expResult, Date firstArg, Date secondArg, boolean method) {

		ValueEval ve;
		if (method) {
			// TODO enable 3rd arg -
			ve = invokeDays360(convert(firstArg), convert(secondArg), BoolEval.valueOf(method));
		} else {
			ve = invokeDays360(convert(firstArg), convert(secondArg));
		}
		if (ve instanceof NumberEval) {

			NumberEval numberEval = (NumberEval) ve;
			if (numberEval.getNumberValue() != expResult) {
				throw new AssertionFailedError(fmt(firstArg) + " " + fmt(secondArg) + " " + method +
						" wrong result got (" + numberEval.getNumberValue()
						+ ") but expected (" + expResult + ")");
			}
			//	System.err.println(fmt(firstArg) + " " + fmt(secondArg) + " " + method + " success got (" + expResult + ")");
			return;
		}
		throw new AssertionFailedError("wrong return type (" + ve.getClass().getName() + ")");
	}
	private static ValueEval invokeDays360(ValueEval...args) {
		return new Days360().evaluate(args, -1, -1);
	}
	private static NumberEval convert(Date d) {
		return new NumberEval(HSSFDateUtil.getExcelDate(d));
	}
}

