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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Direct tests for all implementors of <code>Function</code>.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestAverage.class,
    TestCountFuncs.class,
    TestDate.class,
    TestDays360.class,
    TestFinanceLib.class,
    TestFind.class,
    TestIndex.class,
    TestIndexFunctionFromSpreadsheet.class,
    TestIndirect.class,
    TestIsBlank.class,
    TestLen.class,
    TestLookupFunctionsFromSpreadsheet.class,
    TestMatch.class,
    TestMathX.class,
    TestMid.class,
    TestNper.class,
    TestOffset.class,
    TestPmt.class,
    TestRoundFuncs.class,
    TestRowCol.class,
    TestStatsLib.class,
    TestSubtotal.class,
    TestSumif.class,
    TestSumproduct.class,
    TestText.class,
    TestTFunc.class,
    TestTime.class,
    TestTrim.class,
    TestTrunc.class,
    TestValue.class,
    TestXYNumericFunction.class,
    TestAddress.class,
    TestAreas.class,
    TestClean.class
})
public class AllIndividualFunctionEvaluationTests {
}
