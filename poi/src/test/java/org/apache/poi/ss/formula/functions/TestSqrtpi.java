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
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Sqrtpi}
 */
final class TestSqrtpi {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    //https://support.microsoft.com/en-us/office/sqrtpi-function-1fb4e63f-9b51-46d6-ad68-b3e7a8b519b4
    @Test
    void testBasic() {
        //the expected values were observed in my copy of Excel
        confirmValue(Arrays.asList(1), 1.77245385090552);
        confirmValue(Arrays.asList(2), 2.506628274631);
    }

    @Test
    void testNumError() {
        confirmNumError(Arrays.asList(-1));
    }

    @Test
    void testInvalidError() {
        confirmInvalid(Arrays.asList());
        confirmInvalid(Arrays.asList("num"));
        confirmInvalid(Arrays.asList(3, "num"));
    }

    private static ValueEval invokeValue(List<Object> numberList) {
        ValueEval[] args = new ValueEval[numberList.size()];
        int i = 0;
        for (Object obj : numberList) {
            if (obj instanceof Number) {
                args[i++] = new NumberEval(((Number)obj).doubleValue());
            } else {
                args[i++] = new StringEval(obj.toString());
            }
        }
        return Sqrtpi.instance.evaluate(args, ec);
    }

    private static void confirmValue(List<Object> numberList, double expected) {
        ValueEval result = invokeValue(numberList);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.00000000000001);
    }

    private static void confirmNumError(List<Object> numberList) {
        ValueEval result = invokeValue(numberList);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    private static void confirmInvalid(List<Object> numberList) {
        ValueEval result = invokeValue(numberList);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }
}
