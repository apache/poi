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

package org.apache.poi.hwpf;

import org.apache.poi.hwpf.converter.AbstractWordUtilsTest;
import org.apache.poi.hwpf.converter.TestWordToFoConverter;
import org.apache.poi.hwpf.converter.TestWordToHtmlConverter;
import org.apache.poi.hwpf.extractor.TestDifferentRoutes;
import org.apache.poi.hwpf.extractor.TestWordExtractor;
import org.apache.poi.hwpf.extractor.TestWordExtractorBugs;
import org.apache.poi.hwpf.model.TestBookmarksTables;
import org.apache.poi.hwpf.model.TestCHPBinTable;
import org.apache.poi.hwpf.model.TestDocumentProperties;
import org.apache.poi.hwpf.model.TestFileInformationBlock;
import org.apache.poi.hwpf.model.TestFontTable;
import org.apache.poi.hwpf.model.TestListTables;
import org.apache.poi.hwpf.model.TestNotesTables;
import org.apache.poi.hwpf.model.TestPAPBinTable;
import org.apache.poi.hwpf.model.TestPlexOfCps;
import org.apache.poi.hwpf.model.TestRevisionMarkAuthorTable;
import org.apache.poi.hwpf.model.TestSavedByTable;
import org.apache.poi.hwpf.model.TestSectionTable;
import org.apache.poi.hwpf.model.TestStyleSheet;
import org.apache.poi.hwpf.model.TestTextPieceTable;
import org.apache.poi.hwpf.sprm.TestSprms;
import org.apache.poi.hwpf.usermodel.TestBorderCode;
import org.apache.poi.hwpf.usermodel.TestBug46610;
import org.apache.poi.hwpf.usermodel.TestBug49820;
import org.apache.poi.hwpf.usermodel.TestBug50075;
import org.apache.poi.hwpf.usermodel.TestBugs;
import org.apache.poi.hwpf.usermodel.TestHWPFOldDocument;
import org.apache.poi.hwpf.usermodel.TestHeaderStories;
import org.apache.poi.hwpf.usermodel.TestLists;
import org.apache.poi.hwpf.usermodel.TestPictures;
import org.apache.poi.hwpf.usermodel.TestProblems;
import org.apache.poi.hwpf.usermodel.TestRange;
import org.apache.poi.hwpf.usermodel.TestRangeDelete;
import org.apache.poi.hwpf.usermodel.TestRangeInsertion;
import org.apache.poi.hwpf.usermodel.TestRangeProperties;
import org.apache.poi.hwpf.usermodel.TestRangeReplacement;
import org.apache.poi.hwpf.usermodel.TestRangeSymbols;
import org.apache.poi.hwpf.usermodel.TestTableRow;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // org.apache.poi.hwpf
    TestFieldsTables.class,
    TestHWPFPictures.class,
    TestHWPFRangeParts.class,

    // org.apache.poi.hwpf.converter
    // TestWordToConverterSuite.class,
    AbstractWordUtilsTest.class,
    TestWordToFoConverter.class,
    TestWordToHtmlConverter.class,

    // org.apache.poi.hwpf.extractor
    TestDifferentRoutes.class,
    TestWordExtractor.class,
    TestWordExtractorBugs.class,

    // org.apache.poi.hwpf.model
    TestBookmarksTables.class,
    TestCHPBinTable.class,
    TestDocumentProperties.class,
    TestFileInformationBlock.class,
    TestFontTable.class,
    TestListTables.class,
    TestNotesTables.class,
    TestPAPBinTable.class,
    TestPlexOfCps.class,
    TestRevisionMarkAuthorTable.class,
    TestSavedByTable.class,
    TestSectionTable.class,
    TestStyleSheet.class,
    TestTextPieceTable.class,

    // org.apache.poi.hwpf.sprm
    TestSprms.class,

    // org.apache.poi.hwpf.usermodel
    TestBorderCode.class,
    TestBug46610.class,
    TestBug49820.class,
    TestBug50075.class,
    TestBugs.class,
    TestHeaderStories.class,
    TestHWPFOldDocument.class,
    TestLists.class,
    TestPictures.class,
    TestProblems.class,
    TestRange.class,
    TestRangeDelete.class,
    TestRangeInsertion.class,
    TestRangeProperties.class,
    TestRangeReplacement.class,
    TestRangeSymbols.class,
    TestTableRow.class
})
public final class AllHWPFTests {
}
