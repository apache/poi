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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.NumericFunction;

/**
 * Implementation of Excel 'Analysis ToolPak' function MROUND()<br>
 *
 * Returns a number rounded to the desired multiple.<p>
 *
 * <b>Syntax</b><br>
 * <b>MROUND</b>(<b>number</b>, <b>multiple</b>)
 *
 * <p>
 *
 * @author Yegor Kozlov
 */
final class MRound implements FreeRefFunction {

	public static final FreeRefFunction instance = new MRound();

	private MRound() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        double number, multiple, result;

        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            number = OperandResolver.coerceValueToDouble(OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex()));
            multiple = OperandResolver.coerceValueToDouble(OperandResolver.getSingleValue(args[1], ec.getRowIndex(), ec.getColumnIndex()));

            if( multiple == 0.0 ) {
                result = 0.0;
            } else {
                if(number*multiple < 0) {
                    // Returns #NUM! because the number and the multiple have different signs
                    throw new EvaluationException(ErrorEval.NUM_ERROR);
                }
                result = multiple * Math.round( number / multiple );
            }
            NumericFunction.checkValue(result);
            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
	}
}
