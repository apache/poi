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
package org.apache.poi.ss.formula.udf;

import static org.junit.Assert.assertNotNull;

import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.OperationEvaluationContext;

public class BaseTestUDFFinder {

    protected UDFFinder _instance;
    protected static final FreeRefFunction NotImplemented = new FreeRefFunction() {
        @Override
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            throw new RuntimeException("not implemented");
        }
    };

    protected void confirmFindFunction(String name) {
        FreeRefFunction func = _instance.findFunction(name);
        assertNotNull(func);
    }

}
