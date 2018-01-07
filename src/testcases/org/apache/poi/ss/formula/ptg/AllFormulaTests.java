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

package org.apache.poi.ss.formula.ptg;

import org.apache.poi.ss.formula.TestSheetNameFormatter;
import org.apache.poi.ss.formula.eval.AllFormulaEvalTests;
import org.apache.poi.ss.formula.function.AllFormulaFunctionTests;
import org.apache.poi.ss.formula.functions.AllIndividualFunctionEvaluationTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for <tt>org.apache.poi.hssf.record.formula</tt>.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllFormulaEvalTests.class,
    AllFormulaFunctionTests.class,
    AllIndividualFunctionEvaluationTests.class,
    TestArea3DPtg.class,
    TestAreaErrPtg.class,
    TestAreaPtg.class,
    TestArrayPtg.class,
    TestAttrPtg.class,
    TestErrPtg.class,
    TestExternalFunctionFormulas.class,
    //TestFormulaShifter.class, //converted to junit4
    TestFuncPtg.class,
    TestFuncVarPtg.class,
    TestIntersectionPtg.class,
    TestPercentPtg.class,
    TestRangePtg.class,
    TestRef3DPtg.class,
    TestReferencePtg.class,
    TestSheetNameFormatter.class,
    TestUnionPtg.class
})
public class AllFormulaTests {
}
