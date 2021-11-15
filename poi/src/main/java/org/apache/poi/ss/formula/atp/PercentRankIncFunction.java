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
import org.apache.poi.ss.formula.functions.PercentRank;

/**
 * Implementation of 'Analysis Toolpak' the Excel function PERCENTRANK.INC()
 *
 * <b>Syntax</b>:<br>
 * <b>PERCENTRANK.INC</b>(<b>array</b>, <b>X</b>, <b>[significance]</b>)<p>
 *
 * <b>array</b>  The array or range of data with numeric values that defines relative standing.<br>
 * <b>X</b>  The value for which you want to know the rank.<br>
 * <b>significance</b>  Optional. A value that identifies the number of significant digits for the returned percentage value.
 * If omitted, PERCENTRANK.INC uses three digits (0.xxx).<br>
 * <br>
 * Returns a number between 0 and 1 representing a percentage. PERCENTRANK.INC gives same result as PERCENTRANK
 * with min value having a result of 0 and max has a result of 1. PERCENTRANK.EXC returns value between 0 and 1 (exclusive).
 *
 * @see PercentRank
 * @see PercentRankExcFunction
 * @since POI 5.1.0
 */
final class PercentRankIncFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new PercentRankIncFunction(ArgumentsEvaluator.instance);

    private ArgumentsEvaluator evaluator;

    private PercentRankIncFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return PercentRank.instance.evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }
}
