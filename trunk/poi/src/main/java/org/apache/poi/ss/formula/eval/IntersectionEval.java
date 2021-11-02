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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.functions.Fixed2ArgFunction;
import org.apache.poi.ss.formula.functions.Function;

public final class IntersectionEval  extends Fixed2ArgFunction {

    public static final Function instance = new IntersectionEval();

    private IntersectionEval() {
        // enforces singleton
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {

        try {
            AreaEval reA = evaluateRef(arg0);
            AreaEval reB = evaluateRef(arg1);
            AreaEval result = resolveRange(reA, reB);
            if (result == null) {
                return ErrorEval.NULL_INTERSECTION;
            }
            return result;
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    /**
     * @return simple rectangular {@link AreaEval} which represents the intersection of areas
     * {@code aeA} and {@code aeB}. If the two areas do not intersect, the result is {@code null}.
     */
    private static AreaEval resolveRange(AreaEval aeA, AreaEval aeB) {

        int aeAfr = aeA.getFirstRow();
        int aeAfc = aeA.getFirstColumn();
        int aeBlc = aeB.getLastColumn();
        if (aeAfc > aeBlc) {
            return null;
        }
        int aeBfc = aeB.getFirstColumn();
        if (aeBfc > aeA.getLastColumn()) {
            return null;
        }
        int aeBlr = aeB.getLastRow();
        if (aeAfr > aeBlr) {
            return null;
        }
        int aeBfr = aeB.getFirstRow();
        int aeAlr = aeA.getLastRow();
        if (aeBfr > aeAlr) {
            return null;
        }


        int top = Math.max(aeAfr, aeBfr);
        int bottom = Math.min(aeAlr, aeBlr);
        int left = Math.max(aeAfc, aeBfc);
        int right = Math.min(aeA.getLastColumn(), aeBlc);

        return aeA.offset(top-aeAfr, bottom-aeAfr, left-aeAfc, right-aeAfc);
    }

    private static AreaEval evaluateRef(ValueEval arg) throws EvaluationException {
        if (arg instanceof AreaEval) {
            return (AreaEval) arg;
        }
        if (arg instanceof RefEval) {
            return ((RefEval) arg).offset(0, 0, 0, 0);
        }
        if (arg instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval)arg);
        }
        throw new IllegalArgumentException("Unexpected ref arg class (" + arg.getClass().getName() + ")");
    }
}
