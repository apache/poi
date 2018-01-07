/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.*;


/**
 * Returns the rank of a number in a list of numbers. The rank of a number is its size relative to other values in a list.

 * Syntax:
 *    RANK(number,ref,order)
 *       Number   is the number whose rank you want to find.
 *       Ref     is an array of, or a reference to, a list of numbers. Nonnumeric values in ref are ignored.
 *       Order   is a number specifying how to rank number.

 * If order is 0 (zero) or omitted, Microsoft Excel ranks number as if ref were a list sorted in descending order.
 * If order is any nonzero value, Microsoft Excel ranks number as if ref were a list sorted in ascending order.
 * 
 * @author Rubin Wang
 */
public class Rank extends Var2or3ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		try {
			ValueEval ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			double result = OperandResolver.coerceValueToDouble(ve);
			if (Double.isNaN(result) || Double.isInfinite(result)) {
				throw new EvaluationException(ErrorEval.NUM_ERROR);
			}

			if(arg1 instanceof RefListEval) {
			    return eval(result, ((RefListEval)arg1), true);
            }

			final AreaEval aeRange = convertRangeArg(arg1);

			return eval(result, aeRange, true);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1, ValueEval arg2) {
		try {
			ValueEval ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			final double result = OperandResolver.coerceValueToDouble(ve);
			if (Double.isNaN(result) || Double.isInfinite(result)) {
				throw new EvaluationException(ErrorEval.NUM_ERROR);
			}

            ve = OperandResolver.getSingleValue(arg2, srcRowIndex, srcColumnIndex);
            int order_value = OperandResolver.coerceValueToInt(ve);
            final boolean order;
            if(order_value==0) {
                order = true;
            } else if(order_value==1) {
                order = false;
            } else {
                throw new EvaluationException(ErrorEval.NUM_ERROR);
            }

            if(arg1 instanceof RefListEval) {
                return eval(result, ((RefListEval)arg1), order);
            }

            final AreaEval aeRange = convertRangeArg(arg1);
			return eval(result, aeRange, order);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static ValueEval eval(double arg0, AreaEval aeRange, boolean descending_order) {
		int rank = 1;
		int height=aeRange.getHeight();
		int width= aeRange.getWidth();
		for (int r=0; r<height; r++) {
			for (int c=0; c<width; c++) {
				
				Double value = getValue(aeRange, r, c);
				if(value==null)continue;
				if(descending_order && value>arg0 || !descending_order && value<arg0){
					rank++;
				}
			}
		}
		return new NumberEval(rank);
	}

	private static ValueEval eval(double arg0, RefListEval aeRange, boolean descending_order) {
		int rank = 1;
		for(ValueEval ve : aeRange.getList()) {
            if (ve instanceof RefEval) {
                ve = ((RefEval) ve).getInnerValueEval(((RefEval) ve).getFirstSheetIndex());
            }

            final Double value;
            if (ve instanceof NumberEval) {
                value = ((NumberEval)ve).getNumberValue();
            } else {
                continue;
            }

            if(descending_order && value>arg0 || !descending_order && value<arg0){
                rank++;
            }
        }

		return new NumberEval(rank);
	}

	private static Double getValue(AreaEval aeRange, int relRowIndex, int relColIndex) {
		ValueEval addend = aeRange.getRelativeValue(relRowIndex, relColIndex);
		if (addend instanceof NumberEval) {
			return ((NumberEval)addend).getNumberValue();
		}
		// everything else (including string and boolean values) counts as zero
		return null;
	}

	private static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
		if (eval instanceof AreaEval) {
			return (AreaEval) eval;
		}
		if (eval instanceof RefEval) {
			return ((RefEval)eval).offset(0, 0, 0, 0);
		}
		throw new EvaluationException(ErrorEval.VALUE_INVALID);
	}

}
