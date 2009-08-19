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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 * Test helper class for invoking functions with numeric results.
 *
 * @author Josh Micich
 */
public final class NumericFunctionInvoker {

	private NumericFunctionInvoker() {
		// no instances of this class
	}

	private static final class NumericEvalEx extends Exception {
		public NumericEvalEx(String msg) {
			super(msg);
		}
	}

	/**
	 * Invokes the specified function with the arguments.
	 * <p/>
	 * Assumes that the cell coordinate parameters of
	 *  <code>Function.evaluate(args, srcCellRow, srcCellCol)</code>
	 * are not required.
	 * <p/>
	 * This method cannot be used for confirming error return codes.  Any non-numeric evaluation
	 * result causes the current junit test to fail.
	 */
	public static double invoke(Function f, ValueEval[] args) {
		return invoke(f, args, -1, -1);
	}
	/**
	 * Invokes the specified operator with the arguments.
	 * <p/>
	 * This method cannot be used for confirming error return codes.  Any non-numeric evaluation
	 * result causes the current junit test to fail.
	 */
	public static double invoke(Function f, ValueEval[] args, int srcCellRow, int srcCellCol) {
		try {
			return invokeInternal(f, args, srcCellRow, srcCellCol);
		} catch (NumericEvalEx e) {
			throw new AssertionFailedError("Evaluation of function (" + f.getClass().getName()
					+ ") failed: " + e.getMessage());
		}
	}
	/**
	 * Formats nicer error messages for the junit output
	 */
	private static double invokeInternal(Function target, ValueEval[] args, int srcCellRow, int srcCellCol)
				throws NumericEvalEx {
		ValueEval evalResult;
		try {
			evalResult = target.evaluate(args, srcCellRow, (short)srcCellCol);
		} catch (NotImplementedException e) {
			throw new NumericEvalEx("Not implemented:" + e.getMessage());
		}

		if(evalResult == null) {
			throw new NumericEvalEx("Result object was null");
		}
		if(evalResult instanceof ErrorEval) {
			ErrorEval ee = (ErrorEval) evalResult;
			throw new NumericEvalEx(formatErrorMessage(ee));
		}
		if(!(evalResult instanceof NumericValueEval)) {
			throw new NumericEvalEx("Result object type (" + evalResult.getClass().getName()
					+ ") is invalid.  Expected implementor of ("
					+ NumericValueEval.class.getName() + ")");
		}

		NumericValueEval result = (NumericValueEval) evalResult;
		return result.getNumberValue();
	}
	private static String formatErrorMessage(ErrorEval ee) {
		if(errorCodesAreEqual(ee, ErrorEval.VALUE_INVALID)) {
			return "Error code: #VALUE! (invalid value)";
		}
		return "Error code=" + ee.getErrorCode();
	}
	private static boolean errorCodesAreEqual(ErrorEval a, ErrorEval b) {
		if(a==b) {
			return true;
		}
		return a.getErrorCode() == b.getErrorCode();
	}
}
