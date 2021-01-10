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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RelationalOperationEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

class TestRelationalOperations {

    /**
     *  (1, 1)(1, 1) = 1
     *
     *   evaluates to
     *
     *   (TRUE, TRUE)(TRUE, TRUE)
     *
     */
    @Test
    void testEqMatrixByScalar_Numbers() {
        ValueEval[] values = new ValueEval[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = new NumberEval(1);
        }

        ValueEval arg1 = EvalFactory.createAreaEval("A1:B2", values);
        ValueEval arg2 = EvalFactory.createRefEval("D1", new NumberEval(1));

        RelationalOperationEval eq = (RelationalOperationEval)RelationalOperationEval.EqualEval;
        ValueEval result = eq.evaluateArray(new ValueEval[]{ arg1, arg2}, 2, 5);

        assertEquals(CacheAreaEval.class, result.getClass(), "expected CacheAreaEval");
        CacheAreaEval ce = (CacheAreaEval)result;
        assertEquals(2, ce.getWidth());
        assertEquals(2, ce.getHeight());
        for(int i =0; i < ce.getHeight(); i++){
            for(int j = 0; j < ce.getWidth(); j++){
                assertEquals(BoolEval.TRUE, ce.getRelativeValue(i, j));
            }
        }
    }

    @Test
    void testEqMatrixByScalar_String() {
        ValueEval[] values = new ValueEval[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = new StringEval("ABC");
        }

        ValueEval arg1 = EvalFactory.createAreaEval("A1:B2", values);
        ValueEval arg2 = EvalFactory.createRefEval("D1", new StringEval("ABC"));
        RelationalOperationEval eq = (RelationalOperationEval)RelationalOperationEval.EqualEval;
        ValueEval result = eq.evaluateArray(new ValueEval[]{ arg1, arg2}, 2, 5);

        assertEquals(CacheAreaEval.class, result.getClass(), "expected CacheAreaEval");
        CacheAreaEval ce = (CacheAreaEval)result;
        assertEquals(2, ce.getWidth());
        assertEquals(2, ce.getHeight());
        for(int i =0; i < ce.getHeight(); i++){
            for(int j = 0; j < ce.getWidth(); j++){
                assertEquals(BoolEval.TRUE, ce.getRelativeValue(i, j));
            }
        }
    }

    @Test
    void testEqMatrixBy_Row() {
        ValueEval[] matrix = {
                new NumberEval(-1), new NumberEval(1),
                new NumberEval(-1), new NumberEval(1)
        };


        ValueEval[] row = {
                new NumberEval(1), new NumberEval(1), new NumberEval(1)
        };

        ValueEval[] expected = {
                BoolEval.FALSE, BoolEval.TRUE, ErrorEval.VALUE_INVALID,
                BoolEval.FALSE, BoolEval.TRUE, ErrorEval.VALUE_INVALID
        };

        ValueEval arg1 = EvalFactory.createAreaEval("A1:B2", matrix);
        ValueEval arg2 = EvalFactory.createAreaEval("A4:C4", row);
        RelationalOperationEval eq = (RelationalOperationEval)RelationalOperationEval.EqualEval;
        ValueEval result = eq.evaluateArray(new ValueEval[]{ arg1, arg2}, 4, 5);

        assertEquals(CacheAreaEval.class, result.getClass(), "expected CacheAreaEval");
        CacheAreaEval ce = (CacheAreaEval)result;
        assertEquals(3, ce.getWidth());
        assertEquals(2, ce.getHeight());
        int idx = 0;
        for(int i =0; i < ce.getHeight(); i++){
            for(int j = 0; j < ce.getWidth(); j++){
                assertEquals(expected[idx++], ce.getRelativeValue(i, j), "[" + i + "," + j + "]");
            }
        }
    }

    @Test
    void testEqMatrixBy_Column() {
        ValueEval[] matrix = {
                new NumberEval(-1), new NumberEval(1),
                new NumberEval(-1), new NumberEval(1)
        };


        ValueEval[] column = {
                new NumberEval(1),
                new NumberEval(1),
                new NumberEval(1)
        };

        ValueEval[] expected = {
                BoolEval.FALSE, BoolEval.TRUE,
                BoolEval.FALSE, BoolEval.TRUE,
                ErrorEval.VALUE_INVALID, ErrorEval.VALUE_INVALID
        };

        ValueEval arg1 = EvalFactory.createAreaEval("A1:B2", matrix);
        ValueEval arg2 = EvalFactory.createAreaEval("A6:A8", column);
        RelationalOperationEval eq = (RelationalOperationEval)RelationalOperationEval.EqualEval;
        ValueEval result = eq.evaluateArray(new ValueEval[]{ arg1, arg2}, 4, 6);

        assertEquals(CacheAreaEval.class, result.getClass(), "expected CacheAreaEval");
        CacheAreaEval ce = (CacheAreaEval)result;
        assertEquals(2, ce.getWidth());
        assertEquals(3, ce.getHeight());
        int idx = 0;
        for(int i =0; i < ce.getHeight(); i++){
            for(int j = 0; j < ce.getWidth(); j++){
                assertEquals(expected[idx++], ce.getRelativeValue(i, j), "[" + i + "," + j + "]");
            }
        }
    }

    @Test
    void testEqMatrixBy_Matrix() {
        // A1:B2
        ValueEval[] matrix1 = {
                new NumberEval(-1), new NumberEval(1),
                new NumberEval(-1), new NumberEval(1)
        };

        // A10:C12
        ValueEval[] matrix2 = {
                new NumberEval(1), new NumberEval(1), new NumberEval(1),
                new NumberEval(1), new NumberEval(1), new NumberEval(1),
                new NumberEval(1), new NumberEval(1), new NumberEval(1)
        };

        ValueEval[] expected = {
                BoolEval.FALSE, BoolEval.TRUE, ErrorEval.VALUE_INVALID,
                BoolEval.FALSE, BoolEval.TRUE, ErrorEval.VALUE_INVALID,
                ErrorEval.VALUE_INVALID, ErrorEval.VALUE_INVALID, ErrorEval.VALUE_INVALID
        };

        ValueEval arg1 = EvalFactory.createAreaEval("A1:B2", matrix1);
        ValueEval arg2 = EvalFactory.createAreaEval("A10:C12", matrix2);
        RelationalOperationEval eq = (RelationalOperationEval)RelationalOperationEval.EqualEval;
        ValueEval result = eq.evaluateArray(new ValueEval[]{ arg1, arg2}, 4, 6);

        assertEquals(CacheAreaEval.class, result.getClass(), "expected CacheAreaEval");
        CacheAreaEval ce = (CacheAreaEval)result;
        assertEquals(3, ce.getWidth());
        assertEquals(3, ce.getHeight());
        int idx = 0;
        for(int i =0; i < ce.getHeight(); i++){
            for(int j = 0; j < ce.getWidth(); j++){
                assertEquals(expected[idx++], ce.getRelativeValue(i, j), "[" + i + "," + j + "]");
            }
        }
    }

}
