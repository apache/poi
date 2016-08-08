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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.aggregates.AllRecordAggregateTests;
import org.apache.poi.hssf.record.cf.TestCellRange;
import org.apache.poi.hssf.record.chart.AllChartRecordTests;
import org.apache.poi.hssf.record.common.TestUnicodeString;
import org.apache.poi.hssf.record.pivot.AllPivotRecordTests;
import org.apache.poi.poifs.crypt.AllEncryptionTests;
import org.apache.poi.ss.formula.constant.TestConstantValueParser;
import org.apache.poi.ss.formula.ptg.AllFormulaTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for package <tt>org.apache.poi.hssf.record</tt> and sub-packages.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllChartRecordTests.class,
    AllEncryptionTests.class,
    AllFormulaTests.class,
    AllPivotRecordTests.class,
    AllRecordAggregateTests.class,
    TestArrayRecord.class,
    TestBOFRecord.class,
    TestBoolErrRecord.class,
    TestBoundSheetRecord.class,
    TestCellRange.class,
    TestCFHeaderRecord.class,
    TestCFRuleRecord.class,
    TestColumnInfoRecord.class,
    TestCommonObjectDataSubRecord.class,
    TestConstantValueParser.class,
    TestDVALRecord.class,
    TestDrawingGroupRecord.class,
    TestDrawingRecord.class,
    TestEmbeddedObjectRefSubRecord.class,
    TestEndSubRecord.class,
    TestEscherAggregate.class,
    TestExtendedFormatRecord.class,
    TestExternalNameRecord.class,
    TestFeatRecord.class,
    TestFontRecord.class,
    TestFormulaRecord.class,
    TestHyperlinkRecord.class,
    TestInterfaceEndRecord.class,
    TestLabelRecord.class,
    TestLbsDataSubRecord.class,
    TestMergeCellsRecord.class,
    TestNameRecord.class,
    TestNoteRecord.class,
    TestNoteStructureSubRecord.class,
    TestObjRecord.class,
    //TestPaletteRecord.class, //converted to junit4
    TestPaneRecord.class,
    TestPLVRecord.class,
    TestRecalcIdRecord.class,
    TestRecordFactory.class,
    TestRecordFactoryInputStream.class,
    TestRecordInputStream.class,
    TestSCLRecord.class,
    TestSSTDeserializer.class,
    TestSSTRecord.class,
    TestSSTRecordSizeCalculator.class,
    TestSharedFormulaRecord.class,
    TestStringRecord.class,
    TestStyleRecord.class,
    TestSubRecord.class,
    TestSupBookRecord.class,
    TestTableRecord.class,
    TestTextObjectBaseRecord.class,
    TestTextObjectRecord.class,
    TestUnicodeNameRecord.class,
    TestUnicodeString.class,
    TestWriteAccessRecord.class,
    TestDConRefRecord.class
})
public final class AllRecordTests {
}
