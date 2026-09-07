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
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for the IFERROR __xludf.DUMMYFUNCTION
 * <p>
 * https://support.microsoft.com/en-au/office/iferror-function-c526fd07-caeb-47b8-8bb6-63f3e417f611
 */
public class DummyFunction implements FreeRefFunction {
    public static final DummyFunction instance = new DummyFunction();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 2) {
            return ErrorEval.VALUE_INVALID;
        }
        return args[1];
    }
}
