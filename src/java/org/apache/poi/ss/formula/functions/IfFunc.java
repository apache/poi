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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;

import java.util.function.BiFunction;

/**
 * Implementation for the Excel function IF
 * <p>
 * Note that Excel is a bit picky about the arguments to this function,
 *  when serialised into {@link Ptg}s in a HSSF file. While most cases are
 *  pretty chilled about the R vs V state of {@link RefPtg} arguments,
 *  for IF special care is needed to avoid Excel showing #VALUE.
 * See bug numbers #55324 and #55747 for the full details on this.
 * TODO Fix this...
 */
public final class IfFunc extends Var2or3ArgFunction implements ArrayFunction {

    @Override
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		boolean b;
		try {
			b = evaluateFirstArg(arg0, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		if (b) {
			if (arg1 == MissingArgEval.instance) {
				return BlankEval.instance;
			}
			return arg1;
		}
		return BoolEval.FALSE;
	}

    @Override
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		boolean b;
		try {
			b = evaluateFirstArg(arg0, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		if (b) {
			if (arg1 == MissingArgEval.instance) {
				return BlankEval.instance;
			}
			return arg1;
		}
		if (arg2 == MissingArgEval.instance) {
			return BlankEval.instance;
		}
		return arg2;
	}

	public static boolean evaluateFirstArg(ValueEval arg, int srcCellRow, int srcCellCol)
			throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		Boolean b = OperandResolver.coerceValueToBoolean(ve, false);
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}


 	@Override
	public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		if (args.length < 2 || args.length > 3) {
			return ErrorEval.VALUE_INVALID;
		}

		ValueEval arg0 = args[0];
		ValueEval arg1 = args[1];
		ValueEval arg2 = args.length == 2 ? BoolEval.FALSE : args[2];
		return evaluateArrayArgs(arg0, arg1, arg2, srcRowIndex, srcColumnIndex);
	}

	ValueEval evaluateArrayArgs(ValueEval arg0, ValueEval arg1, ValueEval arg2, int srcRowIndex, int srcColumnIndex) {
		int w1, w2, h1, h2;
		int a1FirstCol = 0, a1FirstRow = 0;
		if (arg0 instanceof AreaEval) {
			AreaEval ae = (AreaEval)arg0;
			w1 = ae.getWidth();
			h1 = ae.getHeight();
			a1FirstCol = ae.getFirstColumn();
			a1FirstRow = ae.getFirstRow();
		} else if (arg0 instanceof RefEval){
			RefEval ref = (RefEval)arg0;
			w1 = 1;
			h1 = 1;
			a1FirstCol = ref.getColumn();
			a1FirstRow = ref.getRow();
		} else {
			w1 = 1;
			h1 = 1;
		}
		int a2FirstCol = 0, a2FirstRow = 0;
		if (arg1 instanceof AreaEval) {
			AreaEval ae = (AreaEval)arg1;
			w2 = ae.getWidth();
			h2 = ae.getHeight();
			a2FirstCol = ae.getFirstColumn();
			a2FirstRow = ae.getFirstRow();
		} else if (arg1 instanceof RefEval){
			RefEval ref = (RefEval)arg1;
			w2 = 1;
			h2 = 1;
			a2FirstCol = ref.getColumn();
			a2FirstRow = ref.getRow();
		} else {
			w2 = 1;
			h2 = 1;
		}

		int a3FirstCol = 0, a3FirstRow = 0;
		if (arg2 instanceof AreaEval) {
			AreaEval ae = (AreaEval)arg2;
			a3FirstCol = ae.getFirstColumn();
			a3FirstRow = ae.getFirstRow();
		} else if (arg2 instanceof RefEval){
			RefEval ref = (RefEval)arg2;
			a3FirstCol = ref.getColumn();
			a3FirstRow = ref.getRow();
		}

		int width = Math.max(w1, w2);
		int height = Math.max(h1, h2);

		ValueEval[] vals = new ValueEval[height * width];

		int idx = 0;
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				ValueEval vA;
				try {
					vA = OperandResolver.getSingleValue(arg0, a1FirstRow + i, a1FirstCol + j);
				} catch (FormulaParseException e) {
					vA = ErrorEval.NAME_INVALID;
				} catch (EvaluationException e) {
					vA = e.getErrorEval();
				}
				ValueEval vB;
				try {
					vB = OperandResolver.getSingleValue(arg1, a2FirstRow + i, a2FirstCol + j);
				} catch (FormulaParseException e) {
					vB = ErrorEval.NAME_INVALID;
				} catch (EvaluationException e) {
					vB = e.getErrorEval();
				}

				ValueEval vC;
				try {
					vC = OperandResolver.getSingleValue(arg2, a3FirstRow + i, a3FirstCol + j);
				} catch (FormulaParseException e) {
					vC = ErrorEval.NAME_INVALID;
				} catch (EvaluationException e) {
					vC = e.getErrorEval();
				}

				if(vA instanceof ErrorEval){
					vals[idx++] = vA;
				} else if (vB instanceof ErrorEval) {
					vals[idx++] = vB;
				} else {
					Boolean b;
					try {
						b = OperandResolver.coerceValueToBoolean(vA, false);
						vals[idx++] = b != null && b ? vB : vC;
					} catch (EvaluationException e) {
						vals[idx++] = e.getErrorEval();
					}
				}

			}
		}

		if (vals.length == 1) {
			return vals[0];
		}

		return new CacheAreaEval(srcRowIndex, srcColumnIndex, srcRowIndex + height - 1, srcColumnIndex + width - 1, vals);
	}

}
