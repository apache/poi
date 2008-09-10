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
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the super class for all excel function evaluator
 * classes that take variable number of operands, and
 * where the order of operands does not matter
 */
public abstract class MultiOperandNumericFunction implements Function {
	static final double[] EMPTY_DOUBLE_ARRAY = { };

	private static class DoubleList {
		private double[] _array;
		private int _count;

		public DoubleList() {
			_array = new double[8];
			_count = 0;
		}

		public double[] toArray() {
			if(_count < 1) {
				return EMPTY_DOUBLE_ARRAY;
			}
			double[] result = new double[_count];
			System.arraycopy(_array, 0, result, 0, _count);
			return result;
		}

		private void ensureCapacity(int reqSize) {
			if(reqSize > _array.length) {
				int newSize = reqSize * 3 / 2; // grow with 50% extra
				double[] newArr = new double[newSize];
				System.arraycopy(_array, 0, newArr, 0, _count);
				_array = newArr;
			}
		}

		public void add(double value) {
			ensureCapacity(_count + 1);
			_array[_count] = value;
			_count++;
		}
	}

	private static final int DEFAULT_MAX_NUM_OPERANDS = 30;

	protected abstract ValueEvalToNumericXlator getXlator();

	
	public final Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		
		double d;
		try {
			double[] values = getNumberArray(args);
			d = evaluate(values);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		
		if (Double.isNaN(d) || Double.isInfinite(d))
			return ErrorEval.NUM_ERROR;
		
		return new NumberEval(d);
	}

	protected abstract double evaluate(double[] values) throws EvaluationException;
	
	
	/**
	 * Maximum number of operands accepted by this function.
	 * Subclasses may override to change default value.
	 */
	protected int getMaxNumOperands() {
		return DEFAULT_MAX_NUM_OPERANDS;
	}

	/**
	 * Returns a double array that contains values for the numeric cells
	 * from among the list of operands. Blanks and Blank equivalent cells
	 * are ignored. Error operands or cells containing operands of type
	 * that are considered invalid and would result in #VALUE! error in
	 * excel cause this function to return <code>null</code>.
	 *
	 * @return never <code>null</code>
	 */
	protected final double[] getNumberArray(Eval[] operands) throws EvaluationException {
		if (operands.length > getMaxNumOperands()) {
			throw EvaluationException.invalidValue();
		}
		DoubleList retval = new DoubleList();

		for (int i=0, iSize=operands.length; i<iSize; i++) {
			collectValues(operands[i], retval);
		}
		return retval.toArray();
	}

	/**
	 * Collects values from a single argument
	 */
	private void collectValues(Eval operand, DoubleList temp) throws EvaluationException {

		if (operand instanceof AreaEval) {
			AreaEval ae = (AreaEval) operand;
			int width = ae.getWidth();
			int height = ae.getHeight();
			for (int rrIx=0; rrIx<height; rrIx++) {
				for (int rcIx=0; rcIx<width; rcIx++) {
					ValueEval ve1 = ae.getRelativeValue(rrIx, rcIx);
					 /*
					 * TODO: For an AreaEval, we are constructing a RefEval
					 * per element.
					 * For now this is a tempfix solution since this may
					 * require a more generic fix at the level of
					 * HSSFFormulaEvaluator where we store an array
					 * of RefEvals as the "values" array.
					 */
					RefEval re = new Ref2DEval(null, ve1);
					ValueEval ve = attemptXlateToNumeric(re);
					if (ve instanceof ErrorEval) {
						throw new EvaluationException((ErrorEval)ve);
					}
					if (ve instanceof BlankEval) {
						// note - blanks are ignored, so returned array will be smaller.
						continue;
					}
					if (ve instanceof NumericValueEval) {
						NumericValueEval nve = (NumericValueEval) ve;
						temp.add(nve.getNumberValue());
					} else {
						throw new RuntimeException("Unexpected value class (" + ve.getClass().getName() + ")");
					}
				}
			}
			return;
		}

		// for ValueEvals other than AreaEval
		ValueEval ve = attemptXlateToNumeric((ValueEval) operand);

		if (ve instanceof NumericValueEval) {
			NumericValueEval nve = (NumericValueEval) ve;
			temp.add(nve.getNumberValue());
			return;
		}

		if (ve instanceof BlankEval) {
			// ignore blanks
			return;
		}
		if (ve instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)ve);
		}
		throw new RuntimeException("Unexpected value class (" + ve.getClass().getName() + ")");
	}


	protected ValueEval attemptXlateToNumeric(ValueEval ve) {
		return getXlator().attemptXlateToNumeric(ve);
	}
}
