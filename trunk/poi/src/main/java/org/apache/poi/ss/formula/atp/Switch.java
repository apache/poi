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
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

import static org.apache.poi.ss.formula.eval.RelationalOperationEval.EqualEval;

/**
 * Implementation of 'Analysis Toolpak' Excel function SWITCH()<br>
 * <p>
 * The SWITCH function evaluates one value (called the expression) against a list of values, and returns the result corresponding to the first matching value.
 * If there is no match, an optional default value may be returned.
 * <p>
 * <b>Syntax</b><br>
 * <b>SWITCH</b>SWITCH(expression, value1, result1, [default or value2, result2],…[default or value3, result3])
 *
 * @author Pieter Degraeuwe
 */
public final class Switch implements FreeRefFunction {

    public static final FreeRefFunction instance = new Switch();

    private Switch() {
        // enforce singleton
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 3) return ErrorEval.NA;

        final ValueEval expression;
        try {
            expression = OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex());
        } catch (Exception e) {
            return ErrorEval.NA;
        }

        for (int i = 1; i < args.length; i = i+2) {

            try {
                ValueEval value =  OperandResolver.getSingleValue(args[i], ec.getRowIndex(), ec.getColumnIndex());
                ValueEval result = args[i+1];
                //ValueEval result = OperandResolver.getSingleValue(args[i+1],ec.getRowIndex(),ec.getColumnIndex()) ;


                final ValueEval evaluate = EqualEval.evaluate(new ValueEval[]{expression, value}, ec.getRowIndex(), ec.getColumnIndex());
                if (evaluate instanceof BoolEval) {
                    BoolEval boolEval = (BoolEval) evaluate;
                    final boolean booleanValue = boolEval.getBooleanValue();
                    if (booleanValue) {
                        return result;
                    }

                }

            } catch (EvaluationException e) {
               return ErrorEval.NA;
            }

            if (i + 2 == args.length-1) {
                //last value in args is the default one
                return args[args.length-1];
            }

        }

/*
        if (args.length % 2 != 0) {
            return ErrorEval.VALUE_INVALID;
        }

        for (int i = 0; i < args.length; i = i + 2) {
            BoolEval logicalTest = (BoolEval) args[i];
            if (logicalTest.getBooleanValue()) {
                return args[i + 1];
            }
        }
*/

        return ErrorEval.NA;
    }

}
