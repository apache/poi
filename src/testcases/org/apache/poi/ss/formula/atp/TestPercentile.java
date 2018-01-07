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

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.formula.functions.EvalFactory;

import junit.framework.TestCase;

/**
 * Testcase for Excel function PERCENTILE()
 *
 * @author T. Gordon
 */
public class TestPercentile extends TestCase {

    public void testPercentile() {
        testBasic();
        testUnusualArgs();
        testUnusualArgs2();
        testUnusualArgs3();
        testErrors();
        testErrors2();
    }

    private static ValueEval invokePercentile(ValueEval[] args, ValueEval percentile) {
        AreaEval aeA = EvalFactory.createAreaEval("A1:A" + args.length, args);
        ValueEval[] args2 = { aeA, percentile };
        return AggregateFunction.PERCENTILE.evaluate(args2, -1, -1);
    }

    private void confirmPercentile(ValueEval percentile, ValueEval[] args, double expected) {
        ValueEval result = invokePercentile(args, percentile);
        assertEquals(NumberEval.class, result.getClass());
        double delta = 0.00000001;
        assertEquals(expected, ((NumberEval) result).getNumberValue(), delta);
    }

    private void confirmPercentile(ValueEval percentile, ValueEval[] args, ErrorEval expectedError) {
        ValueEval result = invokePercentile(args, percentile);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(expectedError.getErrorCode(), ((ErrorEval) result).getErrorCode());
    }

    public void testBasic() {
        ValueEval[] values = { new NumberEval(210.128), new NumberEval(65.2182), new NumberEval(32.231),
                new NumberEval(12.123), new NumberEval(45.32) };
        ValueEval percentile = new NumberEval(0.95);
        confirmPercentile(percentile, values, 181.14604);
    }

    public void testBlanks() {
        ValueEval[] values = { new NumberEval(210.128), new NumberEval(65.2182), new NumberEval(32.231),
                BlankEval.instance, new NumberEval(45.32) };
        ValueEval percentile = new NumberEval(0.95);
        confirmPercentile(percentile, values, 188.39153);
    }

    public void testUnusualArgs() {
        ValueEval[] values = { new NumberEval(1), new NumberEval(2), BoolEval.TRUE, BoolEval.FALSE };
        ValueEval percentile = new NumberEval(0.95);
        confirmPercentile(percentile, values, 1.95);
    }

    //percentile has to be between 0 and 1 - here we test less than zero
    public void testUnusualArgs2() {
        ValueEval[] values = { new NumberEval(1), new NumberEval(2), };
        ValueEval percentile = new NumberEval(-0.1);
        confirmPercentile(percentile, values, ErrorEval.NUM_ERROR);
    }

    //percentile has to be between 0 and 1 - here we test more than 1
    public void testUnusualArgs3() {
        ValueEval[] values = { new NumberEval(1), new NumberEval(2) };
        ValueEval percentile = new NumberEval(1.1);
        confirmPercentile(percentile, values, ErrorEval.NUM_ERROR);
    }

    //here we test where there are errors as part of inputs
    public void testErrors() {
        ValueEval[] values = { new NumberEval(1), ErrorEval.NAME_INVALID, new NumberEval(3), ErrorEval.DIV_ZERO, };
        ValueEval percentile = new NumberEval(0.95);
        confirmPercentile(percentile, values, ErrorEval.NAME_INVALID);
    }

    //here we test where there are errors as part of inputs
    public void testErrors2() {
        ValueEval[] values = { new NumberEval(1), new NumberEval(2), new NumberEval(3), ErrorEval.DIV_ZERO, };
        ValueEval percentile = new NumberEval(0.95);
        confirmPercentile(percentile, values, ErrorEval.DIV_ZERO);
    }
}
