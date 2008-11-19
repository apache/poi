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

package org.apache.poi.xssf;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.ss.util.TestCellReference;
import org.apache.poi.xssf.eventusermodel.TestXSSFReader;
import org.apache.poi.xssf.extractor.TestXSSFExcelExtractor;
import org.apache.poi.xssf.io.TestLoadSaveXSSF;
import org.apache.poi.xssf.model.TestCommentsTable;
import org.apache.poi.xssf.model.TestSharedStringsTable;
import org.apache.poi.xssf.model.TestStylesTable;
import org.apache.poi.xssf.usermodel.AllXSSFUsermodelTests;
import org.apache.poi.xssf.util.TestCTColComparator;
import org.apache.poi.xssf.util.TestNumericRanges;

/**
 * Collects all tests for <tt>org.apache.poi.xssf</tt> and sub-packages.
 * 
 * @author Josh Micich
 */
public final class AllXSSFTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllXSSFTests.class.getName());
		result.addTest(AllXSSFUsermodelTests.suite());
		result.addTestSuite(TestXSSFReader.class);
		result.addTestSuite(TestXSSFExcelExtractor.class);
		result.addTestSuite(TestLoadSaveXSSF.class);
		result.addTestSuite(TestCommentsTable.class);
		result.addTestSuite(TestSharedStringsTable.class);
		result.addTestSuite(TestStylesTable.class);
		result.addTestSuite(TestCellReference.class);
		result.addTestSuite(TestCTColComparator.class);
		result.addTestSuite(TestNumericRanges.class);		
		return result;
	}
}
