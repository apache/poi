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
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for the Excel function ROW
 *
 * @author Josh Micich
 */
public final class RowFunc implements Function0Arg, Function1Arg {

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
        return new NumberEval(srcRowIndex+1);
    }
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        int rnum;

        if (arg0 instanceof AreaEval) {
            rnum = ((AreaEval) arg0).getFirstRow();
        } else if (arg0 instanceof RefEval) {
            rnum = ((RefEval) arg0).getRow();
        } else {
            // anything else is not valid argument
            return ErrorEval.VALUE_INVALID;
        }

        return new NumberEval(rnum + 1);
    }
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        switch (args.length) {
            case 1:
                return evaluate(srcRowIndex, srcColumnIndex, args[0]);
            case 0:
              return new NumberEval(srcRowIndex+1);
        }
        return ErrorEval.VALUE_INVALID;
    }
}
