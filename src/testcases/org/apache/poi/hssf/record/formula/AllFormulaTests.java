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
        

package org.apache.poi.hssf.record.formula;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests for this package.
 * 
 * @author Josh Micich
 */
public class AllFormulaTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite("Tests for org.apache.poi.hssf.record.formula");
		result.addTestSuite(TestArea3DPtg.class);
		result.addTestSuite(TestAreaErrPtg.class);
        result.addTestSuite(TestAreaPtg.class);
		result.addTestSuite(TestErrPtg.class);
		result.addTestSuite(TestFuncPtg.class);
		result.addTestSuite(TestIntersectionPtg.class);
		result.addTestSuite(TestPercentPtg.class);
		result.addTestSuite(TestRangePtg.class);
		result.addTestSuite(TestRef3DPtg.class);
		result.addTestSuite(TestReferencePtg.class);
		result.addTestSuite(TestSheetNameFormatter.class);
		result.addTestSuite(TestUnionPtg.class);
		return result;
	}
}
