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
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Handler for singular AverageIf which has different operand handling than
 * the generic AverageIfs version.
 */
public class AverageIf extends Baseifs {
    public static final FreeRefFunction instance = new AverageIf();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 2) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            AreaEval sumRange = convertRangeArg(args[0]);

            if (args.length == 3) {
                sumRange = convertRangeArg(args[2]);
            }

            // collect pairs of ranges and criteria
            AreaEval ae = convertRangeArg(args[0]);
            I_MatchPredicate mp = Countif.createCriteriaPredicate(args[1], ec.getRowIndex(), ec.getColumnIndex());

            if (mp instanceof Countif.ErrorMatcher) {
                throw new EvaluationException(ErrorEval.valueOf(((Countif.ErrorMatcher) mp).getValue()));
            }

            return aggregateMatchingCells(createAggregator(), sumRange, ae, mp);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    protected ValueEval aggregateMatchingCells(Aggregator aggregator, AreaEval sumRange, AreaEval testRange, I_MatchPredicate mp)
            throws EvaluationException {

        final int height = testRange.getHeight();
        final int width = testRange.getWidth();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {

                ValueEval _testValue = testRange.getRelativeValue(r, c);
                ValueEval _sumValue = sumRange.getRelativeValue(r, c);

                if (mp != null && mp.matches(_testValue)) {
                    // aggregate only if all of the corresponding criteria specified are true for that cell.
                    if (_testValue instanceof ErrorEval) {
                        throw new EvaluationException((ErrorEval) _testValue);
                    }
                    aggregator.addValue(_sumValue);
                }
            }
        }
        return aggregator.getResult();
    }

    @Override
    protected boolean hasInitialRange() {
        return false;
    }

    @Override
    protected Aggregator createAggregator() {
        return new Aggregator() {
            Double sum = 0.0;
            Integer count = 0;

            @Override
            public void addValue(ValueEval value) {

                if (!(value instanceof NumberEval)) return;

                final double d = ((NumberEval) value).getNumberValue();
                sum += d;
                count++;

            }

            @Override
            public ValueEval getResult() {
                return count == 0 ? ErrorEval.DIV_ZERO : new NumberEval(sum / count);
            }
        };
    }

}
