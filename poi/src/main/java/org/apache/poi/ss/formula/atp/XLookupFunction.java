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

import java.util.Optional;

/**
 * Implementation of Excel function XLOOKUP()
 *
 * POI does not currently support having return values with multiple columns and just takes the first cell
 * right now.
 *
 * <b>Syntax</b><br>
 * <b>XLOOKUP</b>(<b>lookup_value</b>, <b>lookup_array</b>, <b>return_array</b>, <b>[if_not_found]</b>, <b>[match_mode]</b>, <b>[search_mode]</b>)<p>
 *
 * @since POI 5.2.0
 */
final class XLookupFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new XLookupFunction(ArgumentsEvaluator.instance);

    private ArgumentsEvaluator evaluator;

    private XLookupFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        int srcRowIndex = ec.getRowIndex();
        int srcColumnIndex = ec.getColumnIndex();
        if (args.length < 3) {
            return ErrorEval.VALUE_INVALID;
        }
        Optional<String> notFound = Optional.empty();
        if (args.length > 3) {
            try {
                ValueEval notFoundValue = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
                String notFoundText = laxValueToString(notFoundValue);
                if (notFoundText != null) {
                    String trimmedText = notFoundText.trim();
                    if (trimmedText.length() > 0) {
                        notFound = Optional.of(trimmedText);
                    }
                }
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        }
        LookupUtils.MatchMode matchMode = LookupUtils.MatchMode.ExactMatch;
        if (args.length > 4) {
            try {
                ValueEval matchModeValue = OperandResolver.getSingleValue(args[4], srcRowIndex, srcColumnIndex);
                int matchInt = OperandResolver.coerceValueToInt(matchModeValue);
                matchMode = LookupUtils.matchMode(matchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        LookupUtils.SearchMode searchMode = LookupUtils.SearchMode.IterateForward;
        if (args.length > 5) {
            try {
                ValueEval searchModeValue = OperandResolver.getSingleValue(args[5], srcRowIndex, srcColumnIndex);
                int searchInt = OperandResolver.coerceValueToInt(searchModeValue);
                searchMode = LookupUtils.searchMode(searchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2], notFound, matchMode, searchMode, ec.isSingleValue());
    }

    private ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval lookupEval, ValueEval indexEval,
                               ValueEval returnEval, Optional<String> notFound, LookupUtils.MatchMode matchMode,
                               LookupUtils.SearchMode searchMode, boolean isSingleValue) {
        try {
            ValueEval lookupValue = OperandResolver.getSingleValue(lookupEval, srcRowIndex, srcColumnIndex);
            TwoDEval tableArray = LookupUtils.resolveTableArrayArg(indexEval);
            int matchedRow;
            try {
                matchedRow = LookupUtils.xlookupIndexOfValue(lookupValue, LookupUtils.createColumnVector(tableArray, 0), matchMode, searchMode);
            } catch (EvaluationException e) {
                if (ErrorEval.NA.equals(e.getErrorEval())) {
                    if (notFound.isPresent()) {
                        return new StringEval(notFound.get());
                    }
                    return ErrorEval.NA;
                } else {
                    return e.getErrorEval();
                }
            }
            if (returnEval instanceof AreaEval) {
                AreaEval area = (AreaEval)returnEval;
                if (isSingleValue) {
                    return area.getRelativeValue(matchedRow, 0);
                }
                return area.offset(matchedRow, matchedRow,0, area.getWidth() - 1);
            } else {
                return returnEval;
            }
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private String laxValueToString(ValueEval eval) {
        return  (eval instanceof MissingArgEval) ? "" : OperandResolver.coerceValueToString(eval);
    }
}
