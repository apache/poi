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

/**
 * Represents the (intermediate) evaluated result of a missing function argument.  In most cases
 * this can be translated into {@link BlankEval} but there are some notable exceptions.  Functions
 * COUNT and COUNTA <em>do</em> count their missing args.  Note - the differences between 
 * {@link MissingArgEval} and {@link BlankEval} have not been investigated fully, so the POI
 * evaluator may need to be updated to account for these as they are found.
 *
 * @author Josh Micich
 */
public final class MissingArgEval implements ValueEval {

    public static MissingArgEval instance = new MissingArgEval();

    private MissingArgEval() {
    }
}
