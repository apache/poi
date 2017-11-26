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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public final class Mode implements Function {

	/**
	 * if v is zero length or contains no duplicates, return value is
	 * Double.NaN. Else returns the value that occurs most times and if there is
	 * a tie, returns the first such value.
	 *
	 * @param v An array of values on which the mode is computed.
	 */
	public static double evaluate(double[] v) throws EvaluationException {
		if (v.length < 2) {
			throw new EvaluationException(ErrorEval.NA);
		}

		// very naive impl, may need to be optimized
		int[] counts = new int[v.length];
		Arrays.fill(counts, 1);
		for (int i = 0, iSize = v.length; i < iSize; i++) {
			for (int j = i + 1, jSize = v.length; j < jSize; j++) {
				if (v[i] == v[j])
					counts[i]++;
			}
		}
		double maxv = 0;
		int maxc = 0;
		for (int i = 0, iSize = counts.length; i < iSize; i++) {
			if (counts[i] > maxc) {
				maxv = v[i];
				maxc = counts[i];
			}
		}
		if (maxc > 1) {
			return maxv;
		}
		throw new EvaluationException(ErrorEval.NA);

	}

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		double result;
		try {
			List<Double> temp = new ArrayList<>();
			for (ValueEval arg : args) {
				collectValues(arg, temp);
			}
			double[] values = new double[temp.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = temp.get(i).doubleValue();
			}
			result = evaluate(values);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}

	private static void collectValues(ValueEval arg, List<Double> temp) throws EvaluationException {
		if (arg instanceof TwoDEval) {
			TwoDEval ae = (TwoDEval) arg;
			int width = ae.getWidth();
			int height = ae.getHeight();
			for (int rrIx = 0; rrIx < height; rrIx++) {
				for (int rcIx = 0; rcIx < width; rcIx++) {
					ValueEval ve1 = ae.getValue(rrIx, rcIx);
					collectValue(ve1, temp, false);
				}
			}
			return;
		}
		if (arg instanceof RefEval) {
			RefEval re = (RefEval) arg;
			final int firstSheetIndex = re.getFirstSheetIndex();
			final int lastSheetIndex = re.getLastSheetIndex();
			for (int sIx = firstSheetIndex; sIx <= lastSheetIndex; sIx++) {
			    collectValue(re.getInnerValueEval(sIx), temp, true);
			}
			return;
		}
		collectValue(arg, temp, true);

	}

	private static void collectValue(ValueEval arg, List<Double> temp, boolean mustBeNumber)
			throws EvaluationException {
		if (arg instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) arg);
		}
		if (arg == BlankEval.instance || arg instanceof BoolEval || arg instanceof StringEval) {
			if (mustBeNumber) {
				throw EvaluationException.invalidValue();
			}
			return;
		}
		if (arg instanceof NumberEval) {
			temp.add(Double.valueOf(((NumberEval) arg).getNumberValue()));
			return;
		}
		throw new RuntimeException("Unexpected value type (" + arg.getClass().getName() + ")");
	}
}
