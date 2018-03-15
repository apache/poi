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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for the <tt>org.apache.poi.hssf.usermodel</tt> package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestBug42464.class,
    TestBugs.class,
    TestCellStyle.class,
    TestCloneSheet.class,
    TestDataValidation.class,
    TestEscherGraphics.class,
    TestEscherGraphics2d.class,
    TestFontDetails.class,
    TestFormulaEvaluatorBugs.class,
    TestFormulaEvaluatorDocs.class,
    TestFormulas.class,
    TestHSSFCell.class,
    TestHSSFClientAnchor.class,
    //TestHSSFComment.class, //converted to junit4
    TestHSSFConditionalFormatting.class,
    TestHSSFDataFormat.class,
    TestHSSFDataFormatter.class,
    TestHSSFDateUtil.class,
    TestHSSFFont.class,
    TestHSSFFormulaEvaluator.class,
    TestHSSFHeaderFooter.class,
    TestHSSFHyperlink.class,
    TestHSSFName.class,
    TestHSSFOptimiser.class,
    TestHSSFPalette.class,
    TestHSSFPatriarch.class,
    TestHSSFPicture.class,
    TestHSSFPictureData.class,
    TestHSSFRichTextString.class,
    TestHSSFRow.class,
    TestHSSFSheet.class,
    TestHSSFSheetShiftRows.class,
    TestHSSFSheetUpdateArrayFormulas.class,
    TestHSSFTextbox.class,
    TestHSSFWorkbook.class,
    TestOLE2Embeding.class,
    TestPOIFSProperties.class,
    TestReadWriteChart.class,
    TestRowStyle.class,
    TestSanityChecker.class,
    TestSheetHiding.class,
    /* deliberately avoiding this one
    TestUnfixedBugs.class,*/
    TestUnicodeWorkbook.class,
    TestNonStandardWorkbookStreamNames.class,
    TestWorkbook.class
})
public class AllUserModelTests {
}
