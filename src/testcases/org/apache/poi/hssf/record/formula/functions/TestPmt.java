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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;

/**
 * @author Josh Micich
 */
public final class TestPmt extends TestCase {

	private static void confirm(double expected, NumberEval ne) {
		// only asserting accuracy to 4 fractional digits
		assertEquals(expected, ne.getNumberValue(), 0.00005);
	}
	private static ValueEval invoke(ValueEval[] args) {
		return FinanceFunction.PMT.evaluate(args, -1, (short)-1);
	}
	/**
	 * Invocation when not expecting an error result
	 */
	private static NumberEval invokeNormal(ValueEval[] args) {
		ValueEval ev = invoke(args);
		if(ev instanceof ErrorEval) {
			throw new AssertionFailedError("Normal evaluation failed with error code: "
					+ ev.toString());
		}
		return (NumberEval) ev;
	}

	private static void confirm(double expected, double rate, double nper, double pv, double fv, boolean isBeginning) {
		ValueEval[] args = {
				new NumberEval(rate),
				new NumberEval(nper),
				new NumberEval(pv),
				new NumberEval(fv),
				new NumberEval(isBeginning ? 1 : 0),
		};
		confirm(expected, invokeNormal(args));
	}


	public void testBasic() {
		confirm(-1037.0321, (0.08/12), 10, 10000, 0, false);
		confirm(-1030.1643, (0.08/12), 10, 10000, 0, true);
	}

	public void test3args() {

		ValueEval[] args = {
				new NumberEval(0.005),
				new NumberEval(24),
				new NumberEval(1000),
		};
		ValueEval ev = invoke(args);
		if(ev instanceof ErrorEval) {
			ErrorEval err = (ErrorEval) ev;
			if(err.getErrorCode() == HSSFErrorConstants.ERROR_VALUE) {
				throw new AssertionFailedError("Identified bug 44691");
			}
		}

		confirm(-44.3206, invokeNormal(args));
	}
}
