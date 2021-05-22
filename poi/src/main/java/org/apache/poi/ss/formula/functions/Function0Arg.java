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

import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Removal;

/**
 * Implemented by all functions that can be called with zero arguments
 *
 * @deprecated replaced by lambda expressions in 5.0.1
 */
@Deprecated
@Removal(version = "6.0.0")
public interface Function0Arg extends Function {
    /**
     * see {@link Function#evaluate(ValueEval[], int, int)}
     */
    ValueEval evaluate(int srcRowIndex, int srcColumnIndex);
}
