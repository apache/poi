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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;


/**
 * Implementation for the Excel function SUMPRODUCT<p>
 *
 * Syntax : <br/>
 *  SUMPRODUCT ( array1[, array2[, array3[, ...]]])
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>array1, ... arrayN&nbsp;&nbsp;</th><td>typically area references,
 *      possibly cell references or scalar values</td></tr>
 *    </table><br/>
 *
 * Let A<b>n</b><sub>(<b>i</b>,<b>j</b>)</sub> represent the element in the <b>i</b>th row <b>j</b>th column
 * of the <b>n</b>th array<br/>
 * Assuming each array has the same dimensions (W, H), the result is defined as:<br/>
 * SUMPRODUCT = &Sigma;<sub><b>i</b>: 1..H</sub> &nbsp;
 * 	(&nbsp; &Sigma;<sub><b>j</b>: 1..W</sub> &nbsp;
 * 	  (&nbsp; &Pi;<sub><b>n</b>: 1..N</sub>
 * 			A<b>n</b><sub>(<b>i</b>,<b>j</b>)</sub>&nbsp;
 *    )&nbsp;
 *  )
 * </p>
 * @author Josh Micich
 */
public final class Sumproduct implements Function {


	public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {

		int maxN = args.length;

		if(maxN < 1) {
			return ErrorEval.VALUE_INVALID;
		}
		ValueEval firstArg = args[0];
		try {
			if(firstArg instanceof NumericValueEval) {
				return evaluateSingleProduct(args);
			}
			if(firstArg instanceof RefEval) {
				return evaluateSingleProduct(args);
			}
			if(firstArg instanceof AreaEval) {
				AreaEval ae = (AreaEval) firstArg;
				if(ae.isRow() && ae.isColumn()) {
					return evaluateSingleProduct(args);
				}
				return evaluateAreaSumProduct(args);
			}
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		throw new RuntimeException("Invalid arg type for SUMPRODUCT: ("
				+ firstArg.getClass().getName() + ")");
	}

	private static ValueEval evaluateSingleProduct(ValueEval[] evalArgs) throws EvaluationException {
		int maxN = evalArgs.length;

		double term = 1D;
		for(int n=0; n<maxN; n++) {
			double val = getScalarValue(evalArgs[n]);
			term *= val;
		}
		return new NumberEval(term);
	}

	private static double getScalarValue(ValueEval arg) throws EvaluationException {

		ValueEval eval;
		if (arg instanceof RefEval) {
			RefEval re = (RefEval) arg;
			eval = re.getInnerValueEval();
		} else {
			eval = arg;
		}

		if (eval == null) {
			throw new RuntimeException("parameter may not be null");
		}
		if (eval instanceof AreaEval) {
			AreaEval ae = (AreaEval) eval;
			// an area ref can work as a scalar value if it is 1x1
			if(!ae.isColumn() || !ae.isRow()) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			eval = ae.getRelativeValue(0, 0);
		}

		return getProductTerm(eval, true);
	}

	private static ValueEval evaluateAreaSumProduct(ValueEval[] evalArgs) throws EvaluationException {
		int maxN = evalArgs.length;
		AreaEval[] args = new AreaEval[maxN];
		try {
			System.arraycopy(evalArgs, 0, args, 0, maxN);
		} catch (ArrayStoreException e) {
			// one of the other args was not an AreaRef
			return ErrorEval.VALUE_INVALID;
		}


		AreaEval firstArg = args[0];

		int height = firstArg.getHeight();
		int width = firstArg.getWidth(); // TODO - junit

		// first check dimensions
		if (!areasAllSameSize(args, height, width)) {
			// normally this results in #VALUE!,
			// but errors in individual cells take precedence
			for (int i = 1; i < args.length; i++) {
				throwFirstError(args[i]);
			}
			return ErrorEval.VALUE_INVALID;
		}

		double acc = 0;

		for (int rrIx=0; rrIx<height; rrIx++) {
			for (int rcIx=0; rcIx<width; rcIx++) {
				double term = 1D;
				for(int n=0; n<maxN; n++) {
					double val = getProductTerm(args[n].getRelativeValue(rrIx, rcIx), false);
					term *= val;
				}
				acc += term;
			}
		}

		return new NumberEval(acc);
	}

	private static void throwFirstError(AreaEval areaEval) throws EvaluationException {
		int height = areaEval.getHeight();
		int width = areaEval.getWidth();
		for (int rrIx=0; rrIx<height; rrIx++) {
			for (int rcIx=0; rcIx<width; rcIx++) {
				ValueEval ve = areaEval.getRelativeValue(rrIx, rcIx);
				if (ve instanceof ErrorEval) {
					throw new EvaluationException((ErrorEval) ve);
				}
			}
		}
	}

	private static boolean areasAllSameSize(AreaEval[] args, int height, int width) {
		for (int i = 0; i < args.length; i++) {
			AreaEval areaEval = args[i];
			// check that height and width match
			if(areaEval.getHeight() != height) {
				return false;
			}
			if(areaEval.getWidth() != width) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Determines a <code>double</code> value for the specified <code>ValueEval</code>.
	 * @param isScalarProduct <code>false</code> for SUMPRODUCTs over area refs.
	 * @throws EvaluationException if <code>ve</code> represents an error value.
	 * <p/>
	 * Note - string values and empty cells are interpreted differently depending on
	 * <code>isScalarProduct</code>.  For scalar products, if any term is blank or a string, the
	 * error (#VALUE!) is raised.  For area (sum)products, if any term is blank or a string, the
	 * result is zero.
	 */
	private static double getProductTerm(ValueEval ve, boolean isScalarProduct) throws EvaluationException {

		if(ve instanceof BlankEval || ve == null) {
			// TODO - shouldn't BlankEval.INSTANCE be used always instead of null?
			// null seems to occur when the blank cell is part of an area ref (but not reliably)
			if(isScalarProduct) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			return 0;
		}

		if(ve instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)ve);
		}
		if(ve instanceof StringEval) {
			if(isScalarProduct) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			// Note for area SUMPRODUCTs, string values are interpreted as zero
			// even if they would parse as valid numeric values
			return 0;
		}
		if(ve instanceof NumericValueEval) {
			NumericValueEval nve = (NumericValueEval) ve;
			return nve.getNumberValue();
		}
		throw new RuntimeException("Unexpected value eval class ("
				+ ve.getClass().getName() + ")");
	}
}
