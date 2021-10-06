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
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

/**
 * Implementation of 'Analysis Toolpak' Excel function IFS()<br>
 * <p>
 * The IFS function checks whether one or more conditions are met and returns a value that corresponds to the first TRUE condition.
 * IFS can take the place of multiple nested IF statements, and is much easier to read with multiple conditions.
 * <p>
 * <b>Syntax</b><br>
 * <b>IFS</b>(IFS([Something is True1, Value if True1, [Something is True2, Value if True2],…[Something is True127, Value if True127]))
 *
 * @author Pieter Degraeuwe
 */
final class Ifs implements FreeRefFunction {

    public static final FreeRefFunction instance = new Ifs();

    private Ifs() {
        // enforce singleton
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length % 2 != 0) {
            return ErrorEval.VALUE_INVALID;
        }

        for (int i = 0; i < args.length; i = i + 2) {
            BoolEval logicalTest = (BoolEval) args[i];
            if (logicalTest.getBooleanValue()) {
                return args[i + 1];
            }
        }

        return ErrorEval.NA;
    }

}
