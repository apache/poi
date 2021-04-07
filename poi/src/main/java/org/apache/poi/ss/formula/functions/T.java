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

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation of Excel T() function
 * <p>
 * If the argument is a text or error value it is returned unmodified.  All other argument types
 * cause an empty string result.  If the argument is an area, the first (top-left) cell is used
 * (regardless of the coordinates of the evaluating formula cell).
 */
public final class T extends Fixed1ArgFunction {

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        ValueEval arg = arg0;
        if (arg instanceof RefEval) {
            // always use the first sheet
            RefEval re = (RefEval)arg;
            arg = re.getInnerValueEval(re.getFirstSheetIndex());
        } else if (arg instanceof AreaEval) {
            // when the arg is an area, choose the top left cell
            arg = ((AreaEval) arg).getRelativeValue(0, 0);
        }

        if (arg instanceof StringEval) {
            // Text values are returned unmodified
            return arg;
        }

        if (arg instanceof ErrorEval) {
            // Error values also returned unmodified
            return arg;
        }
        // for all other argument types the result is empty string
        return StringEval.EMPTY_INSTANCE;
    }
}
