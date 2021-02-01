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
import org.apache.poi.ss.formula.eval.*;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Implementation for Excel FACTDOUBLE() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>FACTDOUBLE  </b>(<b>number</b>)<br>
 * <p>
 * Returns the double factorial of a number.
 * <p>
 * Number is the value for which to return the double factorial. If number is not an integer, it is truncated.
 * <p>
 * Remarks
 * <ul>
 * <li>If number is nonnumeric, FACTDOUBLE returns the #VALUE! error value.</li>
 * <li>If number is negative, FACTDOUBLE returns the #NUM! error value.</li>
 * </ul>
 * Use a cache for more speed of previously calculated factorial
 *
 * @author cedric dot walter @ gmail dot com
 */
public class FactDouble extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new FactDouble();

    //Caching of previously calculated factorial for speed
    static HashMap<Integer, BigInteger> cache = new HashMap<>();

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE) {
        int number;
        try {
            number = OperandResolver.coerceValueToInt(numberVE);
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }

        if (number < 0) {
            return ErrorEval.NUM_ERROR;
        }

        return new NumberEval(factorial(number).longValue());
    }

    public static BigInteger factorial(int n) {
        if (n == 0 || n < 0) {
            return BigInteger.ONE;
        }

        if (cache.containsKey(n))  {
            return cache.get(n);
        }

        BigInteger result = BigInteger.valueOf(n).multiply(factorial(n - 2));
        cache.put(n, result);
        return result;
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}
