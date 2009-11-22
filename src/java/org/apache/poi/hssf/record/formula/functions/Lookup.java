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
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.ValueVector;

/**
 * Implementation of Excel function LOOKUP.<p/>
 *
 * LOOKUP finds an index  row in a lookup table by the first column value and returns the value from another column.
 *
 * <b>Syntax</b>:<br/>
 * <b>VLOOKUP</b>(<b>lookup_value</b>, <b>lookup_vector</b>, result_vector)<p/>
 *
 * <b>lookup_value</b>  The value to be found in the lookup vector.<br/>
 * <b>lookup_vector</> An area reference for the lookup data. <br/>
 * <b>result_vector</b> Single row or single column area reference from which the result value is chosen.<br/>
 *
 * @author Josh Micich
 */
public final class Lookup extends Var2or3ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		// complex rules to choose lookupVector and resultVector from the single area ref
		throw new RuntimeException("Two arg version of LOOKUP not supported yet");
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		try {
			ValueEval lookupValue = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			AreaEval aeLookupVector = LookupUtils.resolveTableArrayArg(arg1);
			AreaEval aeResultVector = LookupUtils.resolveTableArrayArg(arg2);

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

	private static ValueVector createVector(AreaEval ae) {
		ValueVector result = LookupUtils.createVector(ae);
		if (result != null) {
			return result;
		}
		// extra complexity required to emulate the way LOOKUP can handles these abnormal cases.
		throw new RuntimeException("non-vector lookup or result areas not supported yet");
	}
}
