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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation of the DCountA function:
 * Counts the number of non-blank cells in a column in an area with given conditions.
 */
public final class DCountA implements IDStarAlgorithm {
    private long count;

    @Override
    public boolean processMatch(ValueEval eval) {
        if (!(eval instanceof BlankEval)) {
            count++;
        }
        return true;
    }

    @Override
    public ValueEval getResult() {
        return new NumberEval(count);
    }

    @Override
    public boolean allowEmptyMatchField() {
        return true;
    }
}
