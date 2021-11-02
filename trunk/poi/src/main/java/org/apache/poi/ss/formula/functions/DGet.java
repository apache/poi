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
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation of the DGet function:
 * Finds the value of a column in an area with given conditions.
 */
public final class DGet implements IDStarAlgorithm {
    private ValueEval result;

    @Override
    public boolean processMatch(ValueEval eval) {
        if(result == null) // First match, just set the value.
        {
            result = eval;
        }
        else // There was a previous match. Only one non-blank result is allowed. #NUM! on multiple values.
        {
            if(result instanceof BlankEval) {
                result = eval;
            }
            else {
                // We have a previous filled result.
                if(!(eval instanceof BlankEval)) {
                    result = ErrorEval.NUM_ERROR;
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ValueEval getResult() {
        if(result == null) {
            return ErrorEval.VALUE_INVALID;
        } else if(result instanceof BlankEval) {
            return ErrorEval.VALUE_INVALID;
        } else
            try {
                if(OperandResolver.coerceValueToString(OperandResolver.getSingleValue(result, 0, 0)).isEmpty()) {
                    return ErrorEval.VALUE_INVALID;
                }
                else {
                    return result;
                }
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
    }
}
