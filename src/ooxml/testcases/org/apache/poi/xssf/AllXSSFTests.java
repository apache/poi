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

import org.apache.poi.ss.format.TestCellFormatPart;
import org.apache.poi.xssf.extractor.TestXSSFExcelExtractor;
import org.apache.poi.xssf.io.TestLoadSaveXSSF;
import org.apache.poi.xssf.model.TestCommentsTable;
import org.apache.poi.xssf.model.TestSharedStringsTable;
import org.apache.poi.xssf.usermodel.AllXSSFUsermodelTests;
import org.apache.poi.xssf.util.TestCTColComparator;
import org.apache.poi.xssf.util.TestNumericRanges;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for <tt>org.apache.poi.xssf</tt> and sub-packages.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllXSSFUsermodelTests.class,
    //TestXSSFReader.class, //converted to junit4
    TestXSSFExcelExtractor.class,
    TestLoadSaveXSSF.class,
    TestCommentsTable.class,
    TestSharedStringsTable.class,
    //TestStylesTable.class, //converted to junit4
    //TestCellReference.class, //converted to junit4
    TestCTColComparator.class,
    TestNumericRanges.class,       
    TestCellFormatPart.class,
    TestXSSFCloneSheet.class
})
public final class AllXSSFTests {
}
