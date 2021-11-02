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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation of the SINGLE function, used by Dynamic Arrays, which is 
 *  now largely replaced by the &#064; character.
 */
public final class Single implements FreeRefFunction {
    public static final FreeRefFunction instance = new Single();
    private Single() {
        // Enforce singleton
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        // Look for a single Eval which matches either our Row or
        //  our Column
        ValueEval intersect = null;
        int col = ec.getColumnIndex();
        int row = ec.getRowIndex();

        for (ValueEval val : args) {
            if (val instanceof AreaEvalBase) {
                AreaEvalBase area = (AreaEvalBase)val;

                if (area.contains(row, col)) {
                    if (intersect != null) return ErrorEval.VALUE_INVALID; 
                    intersect = area.getAbsoluteValue(row, col);
                } else if (area.containsRow(row)) {
                    if (intersect != null) return ErrorEval.VALUE_INVALID;
                    intersect = area.getAbsoluteValue(row, area.getFirstColumn());
                } else if (area.containsColumn(col)) {
                    if (intersect != null) return ErrorEval.VALUE_INVALID;
                    intersect = area.getAbsoluteValue(area.getFirstRow(), col);
                }
            }
        }
        
        // If we found only one, it's that
        if (intersect != null) {
            return intersect;
        }
        
        // If in doubt, it's probably invalid
        return ErrorEval.VALUE_INVALID;
    }
}
