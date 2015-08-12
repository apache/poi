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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;

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
public final class IfFunc extends Var2or3ArgFunction {

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
}
