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

package org.apache.poi.xssf.usermodel;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.xssf.usermodel.extensions.TestXSSFBorder;
import org.apache.poi.xssf.usermodel.extensions.TestXSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.TestXSSFSheetComments;
import org.apache.poi.xssf.usermodel.helpers.TestColumnHelper;
import org.apache.poi.xssf.usermodel.helpers.TestHeaderFooterHelper;

/**
 * Collects all tests for <tt>org.apache.poi.xssf.usermodel</tt> and sub-packages.
 *
 * @author Josh Micich
 */
public final class AllXSSFUsermodelTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllXSSFUsermodelTests.class.getName());
		result.addTestSuite(TestFormulaEvaluatorOnXSSF.class);
		result.addTestSuite(TestSheetHiding.class);
		result.addTestSuite(TestXSSFBugs.class);
		result.addTestSuite(TestXSSFDataFormat.class);
		result.addTestSuite(TestXSSFCellStyle.class);
		result.addTestSuite(TestXSSFComment.class);
		result.addTestSuite(TestXSSFDialogSheet.class);
		result.addTestSuite(TestXSSFDrawing.class);
		result.addTestSuite(TestXSSFFont.class);
		result.addTestSuite(TestXSSFFormulaEvaluation.class);
		result.addTestSuite(TestXSSFHeaderFooter.class);
		result.addTestSuite(TestXSSFHyperlink.class);
		result.addTestSuite(TestXSSFName.class);
		result.addTestSuite(TestXSSFPicture.class);
		result.addTestSuite(TestXSSFPictureData.class);
		result.addTestSuite(TestXSSFPrintSetup.class);
		result.addTestSuite(TestXSSFRichTextString.class);
		result.addTestSuite(TestXSSFRow.class);
		result.addTestSuite(TestXSSFSheet.class);
		result.addTestSuite(TestXSSFSheetUpdateArrayFormulas.class);
		result.addTestSuite(TestXSSFWorkbook.class);

		result.addTestSuite(TestXSSFBorder.class);
		result.addTestSuite(TestXSSFCellFill.class);
		result.addTestSuite(TestXSSFSheetComments.class);

		result.addTestSuite(TestColumnHelper.class);
		result.addTestSuite(TestHeaderFooterHelper.class);

		return result;
	}
}
