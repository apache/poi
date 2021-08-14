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

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.LookupUtils.ValueVector;
import org.apache.poi.ss.formula.TwoDEval;
/**
 * Implementation of the VLOOKUP() function.<p>
 *
 * VLOOKUP finds a row in a lookup table by the first column value and returns the value from another column.<br>
 *
 * <b>Syntax</b>:<br>
 * <b>VLOOKUP</b>(<b>lookup_value</b>, <b>table_array</b>, <b>col_index_num</b>, range_lookup)<p>
 *
 * <b>lookup_value</b>  The value to be found in the first column of the table array.<br>
 * <b>table_array</b> An area reference for the lookup data. <br>
 * <b>col_index_num</b> a 1 based index specifying which column value of the lookup data will be returned.<br>
 * <b>range_lookup</b> If TRUE (default), VLOOKUP finds the largest value less than or equal to
 * the lookup_value.  If FALSE, only exact matches will be considered<br>
 */
public final class Vlookup extends Var3or4ArgFunction {
    private static final ValueEval DEFAULT_ARG3 = BoolEval.TRUE;

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
            ValueEval arg2) {
        return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1, arg2, DEFAULT_ARG3);
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval lookup_value, ValueEval table_array,
            ValueEval col_index, ValueEval range_lookup) {
        try {
            // Evaluation order:
            // lookup_value , table_array, range_lookup, find lookup value, col_index, fetch result
            ValueEval lookupValue = OperandResolver.getSingleValue(lookup_value, srcRowIndex, srcColumnIndex);
            TwoDEval tableArray = LookupUtils.resolveTableArrayArg(table_array);
            boolean isRangeLookup;
            try {
                isRangeLookup = LookupUtils.resolveRangeLookupArg(range_lookup, srcRowIndex, srcColumnIndex);
            } catch(RuntimeException e) {
                isRangeLookup = true;
            }
            int rowIndex = LookupUtils.lookupFirstIndexOfValue(lookupValue, LookupUtils.createColumnVector(tableArray, 0), isRangeLookup);
            int colIndex = LookupUtils.resolveRowOrColIndexArg(col_index, srcRowIndex, srcColumnIndex);
            ValueVector resultCol = createResultColumnVector(tableArray, colIndex);
            return resultCol.getItem(rowIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }


    /**
     * Returns one column from an {@code AreaEval}
     *
     * @param colIndex assumed to be non-negative
     *
     * @throws EvaluationException (#REF!) if colIndex is too high
     */
    private ValueVector createResultColumnVector(TwoDEval tableArray, int colIndex) throws EvaluationException {
        if(colIndex >= tableArray.getWidth()) {
            throw EvaluationException.invalidRef();
        }
        return LookupUtils.createColumnVector(tableArray, colIndex);
    }
}
