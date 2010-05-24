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

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;

/**
 * Test case for TEXT()
 *
 * @author Stephen Wolke (smwolke at geistig.com)
 */
public final class TestText extends TestCase {
	private static final TextFunction T = null;

	public void testTextWithStringFirstArg() {

		ValueEval strArg = new StringEval("abc");
		ValueEval formatArg = new StringEval("abc");
		ValueEval[] args = { strArg, formatArg };
		ValueEval result = T.TEXT.evaluate(args, -1, (short)-1);
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}

	public void testTextWithDeciamlFormatSecondArg() {

		ValueEval numArg = new NumberEval(321321.321);
		ValueEval formatArg = new StringEval("#,###.00000");
		ValueEval[] args = { numArg, formatArg };
		ValueEval result = T.TEXT.evaluate(args, -1, (short)-1);
		char groupSeparator = new DecimalFormatSymbols(Locale.getDefault()).getGroupingSeparator();
		char decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
		ValueEval testResult = new StringEval("321" + groupSeparator + "321" + decimalSeparator + "32100");
		assertEquals(testResult.toString(), result.toString());
		numArg = new NumberEval(321.321);
		formatArg = new StringEval("00000.00000");
		args[0] = numArg;
		args[1] = formatArg;
		result = T.TEXT.evaluate(args, -1, (short)-1);
		testResult = new StringEval("00321" + decimalSeparator + "32100");
		assertEquals(testResult.toString(), result.toString());

		formatArg = new StringEval("$#.#");
		args[1] = formatArg;
		result = T.TEXT.evaluate(args, -1, (short)-1);
		testResult = new StringEval("$321" + decimalSeparator + "3");
		assertEquals(testResult.toString(), result.toString());
	}

	public void testTextWithFractionFormatSecondArg() {

		ValueEval numArg = new NumberEval(321.321);
		ValueEval formatArg = new StringEval("# #/#");
		ValueEval[] args = { numArg, formatArg };
		ValueEval result = T.TEXT.evaluate(args, -1, (short)-1);
		ValueEval testResult = new StringEval("321 1/3");
		assertEquals(testResult.toString(), result.toString());

		formatArg = new StringEval("# #/##");
		args[1] = formatArg;
		result = T.TEXT.evaluate(args, -1, (short)-1);
		testResult = new StringEval("321 26/81");
		assertEquals(testResult.toString(), result.toString());

		formatArg = new StringEval("#/##");
		args[1] = formatArg;
		result = T.TEXT.evaluate(args, -1, (short)-1);
		testResult = new StringEval("26027/81");
		assertEquals(testResult.toString(), result.toString());
	}

	public void testTextWithDateFormatSecondArg() {

		ValueEval numArg = new NumberEval(321.321);
		ValueEval formatArg = new StringEval("dd:MM:yyyy hh:mm:ss");
		ValueEval[] args = { numArg, formatArg };
		ValueEval result = T.TEXT.evaluate(args, -1, (short)-1);
		ValueEval testResult = new StringEval("16:11:1900 07:42:14");
		assertEquals(testResult.toString(), result.toString());

		// this line is intended to compute how "November" would look like in the current locale
		String november = new SimpleDateFormat("MMMM").format(new GregorianCalendar(2010,10,15).getTime());

		formatArg = new StringEval("MMMM dd, yyyy");
		args[1] = formatArg;
		result = T.TEXT.evaluate(args, -1, (short)-1);
		testResult = new StringEval(november + " 16, 1900");
		assertEquals(testResult.toString(), result.toString());
	}
}
