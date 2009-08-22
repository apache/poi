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

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Direct tests for all implementors of <code>Function</code>.
 * 
 * @author Josh Micich
 */
public final class AllIndividualFunctionEvaluationTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllIndividualFunctionEvaluationTests.class.getName());
		result.addTestSuite(TestAverage.class);
		result.addTestSuite(TestCountFuncs.class);
		result.addTestSuite(TestDate.class);
		result.addTestSuite(TestFind.class);
		result.addTestSuite(TestFinanceLib.class);
		result.addTestSuite(TestIndex.class);
		result.addTestSuite(TestIndexFunctionFromSpreadsheet.class);
		result.addTestSuite(TestIndirect.class);
		result.addTestSuite(TestIsBlank.class);
		result.addTestSuite(TestLen.class);
		result.addTestSuite(TestLookupFunctionsFromSpreadsheet.class);
		result.addTestSuite(TestMid.class);
		result.addTestSuite(TestMathX.class);
		result.addTestSuite(TestMatch.class);
		result.addTestSuite(TestNper.class);
		result.addTestSuite(TestPmt.class);
		result.addTestSuite(TestOffset.class);
		result.addTestSuite(TestRowCol.class);
		result.addTestSuite(TestSumproduct.class);
		result.addTestSuite(TestStatsLib.class);
		result.addTestSuite(TestTFunc.class);
		result.addTestSuite(TestTime.class);
		result.addTestSuite(TestTrim.class);
		result.addTestSuite(TestValue.class);
		result.addTestSuite(TestXYNumericFunction.class);
		return result;
	}
}
