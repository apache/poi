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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

/**
 * Specific test cases for YearFracCalculator
 */
public final class TestYearFracCalculator extends TestCase {

	public void testBasis1() {
		confirm(md(1999, 1, 1), md(1999, 4, 5), 1, 0.257534247);
		confirm(md(1999, 4, 1), md(1999, 4, 5), 1, 0.010958904);
		confirm(md(1999, 4, 1), md(1999, 4, 4), 1, 0.008219178);
		confirm(md(1999, 4, 2), md(1999, 4, 5), 1, 0.008219178);
		confirm(md(1999, 3, 31), md(1999, 4, 3), 1, 0.008219178);
		confirm(md(1999, 4, 5), md(1999, 4, 8), 1, 0.008219178);
		confirm(md(1999, 4, 4), md(1999, 4, 7), 1, 0.008219178);
		confirm(md(2000, 2, 5), md(2000, 6, 1), 0, 0.322222222);
	}

	private void confirm(double startDate, double endDate, int basis, double expectedValue) {
		double actualValue;
		try {
			actualValue = YearFracCalculator.calculate(startDate, endDate, basis);
		} catch (EvaluationException e) {
			throw new RuntimeException(e);
		}
		double diff = actualValue - expectedValue;
		if (Math.abs(diff) >  0.000000001) {
			double hours = diff * 365 * 24;
			System.out.println(startDate + " " + endDate + " off by " + hours + " hours");
			assertEquals(expectedValue, actualValue, 0.000000001);
		}
		
	}

	private static double md(int year, int month, int day) {
		Calendar c = new GregorianCalendar();
		
		c.set(year, month-1, day, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		return HSSFDateUtil.getExcelDate(c.getTime());
	}
}
