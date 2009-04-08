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

package org.apache.poi.hssf.model;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests for <tt>org.apache.poi.hssf.model</tt>.
 * 
 * @author Josh Micich
 */
public final class AllModelTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite(AllModelTests.class.getName());
		result.addTestSuite(TestDrawingManager.class);
		result.addTestSuite(TestDrawingManager2.class);
		result.addTestSuite(TestFormulaParser.class);
		result.addTestSuite(TestFormulaParserEval.class);
		result.addTestSuite(TestFormulaParserIf.class);
		result.addTestSuite(TestLinkTable.class);
		result.addTestSuite(TestOperandClassTransformer.class);
		result.addTestSuite(TestRowBlocksReader.class);
		result.addTestSuite(TestRVA.class);
		result.addTestSuite(TestSheet.class);
		result.addTestSuite(TestSheetAdditional.class);
		result.addTestSuite(TestWorkbook.class);
		return result;
	}
}
