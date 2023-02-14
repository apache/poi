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
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.LookupUtils;

/**
 * Implementation of Excel function XMATCH()
 *
 * <b>Syntax</b><br>
 * <b>XMATCH</b>(<b>lookup_value</b>, <b>lookup_array</b>, <b>[match_mode]</b>, <b>[search_mode]</b>)<p>
 *
 * https://support.microsoft.com/en-us/office/xmatch-function-d966da31-7a6b-4a13-a1c6-5a33ed6a0312
 * 
 * @since POI 5.2.0
 */
final class XMatchFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new XMatchFunction(ArgumentsEvaluator.instance);

    private final ArgumentsEvaluator evaluator;

    private XMatchFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        int srcRowIndex = ec.getRowIndex();
        int srcColumnIndex = ec.getColumnIndex();
        return _evaluate(args, srcRowIndex, srcColumnIndex);
    }

    private ValueEval _evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 2) {
            return ErrorEval.VALUE_INVALID;
        }
        LookupUtils.MatchMode matchMode = LookupUtils.MatchMode.ExactMatch;
        if (args.length > 2) {
            try {
                ValueEval matchModeValue = OperandResolver.getSingleValue(args[2], srcRowIndex, srcColumnIndex);
                int matchInt = OperandResolver.coerceValueToInt(matchModeValue);
                matchMode = LookupUtils.matchMode(matchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        LookupUtils.SearchMode searchMode = LookupUtils.SearchMode.IterateForward;
        if (args.length > 3) {
            try {
                ValueEval searchModeValue = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
                int searchInt = OperandResolver.coerceValueToInt(searchModeValue);
                searchMode = LookupUtils.searchMode(searchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], matchMode, searchMode);
    }

    private ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval lookupEval, ValueEval indexEval,
                               LookupUtils.MatchMode matchMode, LookupUtils.SearchMode searchMode) {
        try {
            ValueEval lookupValue = OperandResolver.getSingleValue(lookupEval, srcRowIndex, srcColumnIndex);
            TwoDEval tableArray = LookupUtils.resolveTableArrayArg(indexEval);
            LookupUtils.ValueVector vector;
            if (tableArray.isColumn()) {
                vector = LookupUtils.createColumnVector(tableArray, 0);
            } else {
                vector = LookupUtils.createRowVector(tableArray, 0);
            }
            int matchedIdx = LookupUtils.xlookupIndexOfValue(lookupValue, vector, matchMode, searchMode);
            return new NumberEval((double)matchedIdx + 1);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
