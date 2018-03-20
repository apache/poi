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

import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.LookupUtils.ValueVector;
import org.apache.poi.ss.formula.TwoDEval;

/**
 * Implementation of Excel function LOOKUP.<p>
 *
 * LOOKUP finds an index  row in a lookup table by the first column value and returns the value from another column.
 *
 * <b>Syntax</b>:<br>
 * <b>VLOOKUP</b>(<b>lookup_value</b>, <b>lookup_vector</b>, result_vector)<p>
 *
 * <b>lookup_value</b>  The value to be found in the lookup vector.<br>
 * <b>lookup_vector</> An area reference for the lookup data. <br>
 * <b>result_vector</b> Single row or single column area reference from which the result value is chosen.<br>
 *
 * @author Josh Micich
 */
public final class Lookup extends Var2or3ArgFunction {

	@Override
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		// complex rules to choose lookupVector and resultVector from the single area ref
		
		try {
			/*
			The array form of LOOKUP is very similar to the HLOOKUP and VLOOKUP functions. The difference is that HLOOKUP searches for the value of lookup_value in the first row, VLOOKUP searches in the first column, and LOOKUP searches according to the dimensions of array.
			If array covers an area that is wider than it is tall (more columns than rows), LOOKUP searches for the value of lookup_value in the first row.
			If an array is square or is taller than it is wide (more rows than columns), LOOKUP searches in the first column.
			With the HLOOKUP and VLOOKUP functions, you can index down or across, but LOOKUP always selects the last value in the row or column.
			 */
			ValueEval lookupValue = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			TwoDEval lookupArray = LookupUtils.resolveTableArrayArg(arg1);
			ValueVector lookupVector;
			ValueVector resultVector;
	
			if (lookupArray.getWidth() > lookupArray.getHeight()) {
				// If array covers an area that is wider than it is tall (more columns than rows), LOOKUP searches for the value of lookup_value in the first row.
				lookupVector = createVector(lookupArray.getRow(0));
				resultVector = createVector(lookupArray.getRow(lookupArray.getHeight() - 1));
			} else {
				// If an array is square or is taller than it is wide (more rows than columns), LOOKUP searches in the first column.
				lookupVector = createVector(lookupArray.getColumn(0));
				resultVector = createVector(lookupArray.getColumn(lookupArray.getWidth() - 1));
			}
			// if a rectangular area reference was passed in as arg1, lookupVector and resultVector should be the same size
			assert (lookupVector.getSize() == resultVector.getSize());
			
			int index = LookupUtils.lookupIndexOfValue(lookupValue, lookupVector, true);
			return resultVector.getItem(index);
		} catch (final EvaluationException e) {
			return e.getErrorEval();
		}
	}

	@Override
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		try {
			ValueEval lookupValue = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			TwoDEval aeLookupVector = LookupUtils.resolveTableArrayArg(arg1);
			TwoDEval aeResultVector = LookupUtils.resolveTableArrayArg(arg2);

			ValueVector lookupVector = createVector(aeLookupVector);
			ValueVector resultVector = createVector(aeResultVector);
			if(lookupVector.getSize() > resultVector.getSize()) {
				// Excel seems to handle this by accessing past the end of the result vector.
				throw new RuntimeException("Lookup vector and result vector of differing sizes not supported yet");
			}
			int index = LookupUtils.lookupIndexOfValue(lookupValue, lookupVector, true);

			return resultVector.getItem(index);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static ValueVector createVector(TwoDEval ae) {
		ValueVector result = LookupUtils.createVector(ae);
		if (result != null) {
			return result;
		}
		// extra complexity required to emulate the way LOOKUP can handles these abnormal cases.
		throw new RuntimeException("non-vector lookup or result areas not supported yet");
	}
}
