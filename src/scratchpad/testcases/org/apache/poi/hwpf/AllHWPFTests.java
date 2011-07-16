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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hwpf.converter.TestWordToConverterSuite;
import org.apache.poi.hwpf.converter.TestWordToFoConverter;
import org.apache.poi.hwpf.converter.TestWordToHtmlConverter;
import org.apache.poi.hwpf.extractor.TestDifferentRoutes;
import org.apache.poi.hwpf.extractor.TestWordExtractor;
import org.apache.poi.hwpf.extractor.TestWordExtractorBugs;
import org.apache.poi.hwpf.model.TestCHPBinTable;
import org.apache.poi.hwpf.model.TestDocumentProperties;
import org.apache.poi.hwpf.model.TestFileInformationBlock;
import org.apache.poi.hwpf.model.TestFontTable;
import org.apache.poi.hwpf.model.TestListTables;
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
import org.apache.poi.hwpf.usermodel.TestShapes;
import org.apache.poi.hwpf.usermodel.TestTableRow;

public final class AllHWPFTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite( AllHWPFTests.class.getName() );

        // org.apache.poi.hwpf
        suite.addTestSuite( TestFieldsTables.class );
        suite.addTestSuite( TestHWPFPictures.class );
        suite.addTestSuite( TestHWPFRangeParts.class );

        // org.apache.poi.hwpf.converter
        suite.addTestSuite( TestWordToConverterSuite.class );
        suite.addTestSuite( TestWordToFoConverter.class );
        suite.addTestSuite( TestWordToHtmlConverter.class );

        // org.apache.poi.hwpf.extractor
        suite.addTestSuite( TestDifferentRoutes.class );
        suite.addTestSuite( TestWordExtractor.class );
        suite.addTestSuite( TestWordExtractorBugs.class );

        // org.apache.poi.hwpf.model
        suite.addTestSuite( TestCHPBinTable.class );
        suite.addTestSuite( TestDocumentProperties.class );
        suite.addTestSuite( TestFileInformationBlock.class );
        suite.addTestSuite( TestFontTable.class );
        suite.addTestSuite( TestListTables.class );
        suite.addTestSuite( TestPAPBinTable.class );
        suite.addTestSuite( TestPlexOfCps.class );
        suite.addTestSuite( TestRevisionMarkAuthorTable.class );
        suite.addTestSuite( TestSavedByTable.class );
        suite.addTestSuite( TestSectionTable.class );
        suite.addTestSuite( TestStyleSheet.class );
        suite.addTestSuite( TestTextPieceTable.class );

        // org.apache.poi.hwpf.sprm
        suite.addTestSuite( TestSprms.class );

        // org.apache.poi.hwpf.usermodel
        suite.addTestSuite( TestBorderCode.class );
        suite.addTestSuite( TestBug46610.class );
        suite.addTestSuite( TestBug49820.class );
        suite.addTestSuite( TestBug50075.class );
        suite.addTestSuite( TestHeaderStories.class );
        suite.addTestSuite( TestHWPFOldDocument.class );
        suite.addTestSuite( TestLists.class );
        suite.addTestSuite( TestPictures.class );
        suite.addTestSuite( TestProblems.class );
        suite.addTestSuite( TestRange.class );
        suite.addTestSuite( TestRangeDelete.class );
        suite.addTestSuite( TestRangeInsertion.class );
        suite.addTestSuite( TestRangeProperties.class );
        suite.addTestSuite( TestRangeReplacement.class );
        suite.addTestSuite( TestRangeSymbols.class );
        suite.addTestSuite( TestShapes.class );
        suite.addTestSuite( TestTableRow.class );

        return suite;
    }
}
