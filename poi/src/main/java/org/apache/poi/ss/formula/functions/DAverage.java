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

import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Implementation of the DAverage function:
 * Gets the average value of a column in an area with given conditions.
 */
public final class DAverage implements IDStarAlgorithm {
    private long count;
    private double total;

    @Override
    public boolean processMatch(ValueEval eval) {
        if (eval instanceof NumericValueEval) {
            count++;
            total += ((NumericValueEval)eval).getNumberValue();
        }
        return true;
    }

    @Override
    public ValueEval getResult() {
        return count == 0 ? NumberEval.ZERO : new NumberEval(getAverage());
    }

    private double getAverage() {
        return divide(total, count);
    }

    private static double divide(final double total, final long count) {
        return BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(count), MathContext.DECIMAL128)
                .doubleValue();
    }
}
