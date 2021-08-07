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

/**
 * Implementation of Excel function XLOOKUP()
 *
 * <b>Syntax</b><br>
 * <b>XLOOKUP</b><p>
 *
 * @since POI 5.0.1
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
        return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
    }

    private ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval lookupEval, ValueEval indexEval,
                               ValueEval valueEval) {
        try {
            ValueEval lookupValue = OperandResolver.getSingleValue(lookupEval, srcRowIndex, srcColumnIndex);
            String lookup = OperandResolver.coerceValueToString(lookupValue);
            int matchedRow = matchedIndex(indexEval, lookup);
            if (matchedRow != -1) {
                if (valueEval instanceof AreaEval) {
                    AreaEval area = (AreaEval)valueEval;
                    if (area.getWidth() == 1) {
                        return area.getRelativeValue(matchedRow, 0);
                    } else {
                        return area.getRow(matchedRow);
                    }
                }
            }
            return ErrorEval.NUM_ERROR;
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private int matchedIndex(ValueEval areaEval, String lookup) {
        if (areaEval instanceof AreaEval) {
            AreaEval area = (AreaEval)areaEval;
            for (int r = 0; r <= area.getHeight(); r++) {
                for (int c = 0; c <= area.getWidth(); c++) {
                    ValueEval cellEval = area.getRelativeValue(r, c);
                    String cellValue = OperandResolver.coerceValueToString(cellEval);
                    if (lookup.equals(cellValue)) {
                        return r;
                    }
                }
            }
        }
        return -1;
    }
}
