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

package org.apache.poi.hssf.usermodel;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests for the <tt>org.apache.poi.hssf.usermodel</tt> package.
 *
 * @author Josh Micich
 */
public class AllUserModelTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllUserModelTests.class.getName());

		result.addTestSuite(TestBug42464.class);
		result.addTestSuite(TestBugs.class);
		result.addTestSuite(TestCellStyle.class);
		result.addTestSuite(TestCloneSheet.class);
		result.addTestSuite(TestDataValidation.class);
		result.addTestSuite(TestEscherGraphics.class);
		result.addTestSuite(TestEscherGraphics2d.class);
		result.addTestSuite(TestFontDetails.class);
		result.addTestSuite(TestFormulaEvaluatorBugs.class);
		result.addTestSuite(TestFormulaEvaluatorDocs.class);
		result.addTestSuite(TestFormulas.class);
		result.addTestSuite(TestHSSFCell.class);
		result.addTestSuite(TestHSSFClientAnchor.class);
		result.addTestSuite(TestHSSFComment.class);
		result.addTestSuite(TestHSSFConditionalFormatting.class);
		result.addTestSuite(TestHSSFDataFormat.class);
		result.addTestSuite(TestHSSFDataFormatter.class);
		result.addTestSuite(TestHSSFDateUtil.class);
		result.addTestSuite(TestHSSFFont.class);
		result.addTestSuite(TestHSSFFormulaEvaluator.class);
		result.addTestSuite(TestHSSFHeaderFooter.class);
		result.addTestSuite(TestHSSFHyperlink.class);
		result.addTestSuite(TestHSSFName.class);
		result.addTestSuite(TestHSSFOptimiser.class);
		result.addTestSuite(TestHSSFPalette.class);
		result.addTestSuite(TestHSSFPatriarch.class);
		result.addTestSuite(TestHSSFPicture.class);
		result.addTestSuite(TestHSSFPictureData.class);
		result.addTestSuite(TestHSSFRichTextString.class);
		result.addTestSuite(TestHSSFRow.class);
		result.addTestSuite(TestHSSFSheet.class);
		result.addTestSuite(TestHSSFSheetShiftRows.class);
		result.addTestSuite(TestHSSFSheetUpdateArrayFormulas.class);
		result.addTestSuite(TestHSSFTextbox.class);
		result.addTestSuite(TestHSSFWorkbook.class);
		result.addTestSuite(TestOLE2Embeding.class);
		result.addTestSuite(TestPOIFSProperties.class);
		result.addTestSuite(TestReadWriteChart.class);
		result.addTestSuite(TestRowStyle.class);
		result.addTestSuite(TestSanityChecker.class);
		result.addTestSuite(TestSheetHiding.class);
		if (false) { // deliberately avoiding this one
			result.addTestSuite(TestUnfixedBugs.class);
		}
		result.addTestSuite(TestUnicodeWorkbook.class);
		result.addTestSuite(TestUppercaseWorkbook.class);
		result.addTestSuite(TestWorkbook.class);

		return result;
	}
}
