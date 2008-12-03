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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public final class FuncVarEval extends FunctionEval {

    private AbstractFunctionPtg delegate;

    public FuncVarEval(AbstractFunctionPtg funcPtg) {
        delegate = funcPtg;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        Function f = getFunction();
        if (f == null) {
			return ErrorEval.FUNCTION_NOT_IMPLEMENTED;
		}
		return f.evaluate(operands, srcRow, srcCol);
    }

    public int getNumberOfOperands() {
        return delegate.getNumberOfOperands();
    }

    public int getType() {
        return delegate.getType();
    }

    public short getFunctionIndex() {
        return delegate.getFunctionIndex();
    }
}
