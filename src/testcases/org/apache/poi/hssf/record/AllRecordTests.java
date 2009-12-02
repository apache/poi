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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hssf.record.aggregates.AllRecordAggregateTests;
import org.apache.poi.hssf.record.cf.TestCellRange;
import org.apache.poi.hssf.record.chart.AllChartRecordTests;
import org.apache.poi.hssf.record.constant.TestConstantValueParser;
import org.apache.poi.hssf.record.crypto.AllHSSFEncryptionTests;
import org.apache.poi.hssf.record.formula.AllFormulaTests;
import org.apache.poi.hssf.record.pivot.AllPivotRecordTests;

/**
 * Collects all tests for package <tt>org.apache.poi.hssf.record</tt> and sub-packages.
 *
 * @author Josh Micich
 */
public final class AllRecordTests {

	public static Test suite() {
		TestSuite result = new TestSuite(AllRecordTests.class.getName());

		result.addTest(AllChartRecordTests.suite());
		result.addTest(AllHSSFEncryptionTests.suite());
		result.addTest(AllFormulaTests.suite());
		result.addTest(AllPivotRecordTests.suite());
		result.addTest(AllRecordAggregateTests.suite());

		result.addTestSuite(TestBOFRecord.class);
		result.addTestSuite(TestBoolErrRecord.class);
		result.addTestSuite(TestBoundSheetRecord.class);
		result.addTestSuite(TestCellRange.class);
		result.addTestSuite(TestCFHeaderRecord.class);
		result.addTestSuite(TestCFRuleRecord.class);
		result.addTestSuite(TestColumnInfoRecord.class);
		result.addTestSuite(TestCommonObjectDataSubRecord.class);
		result.addTestSuite(TestConstantValueParser.class);
		result.addTestSuite(TestDrawingGroupRecord.class);
		result.addTestSuite(TestEmbeddedObjectRefSubRecord.class);
		result.addTestSuite(TestEndSubRecord.class);
		result.addTestSuite(TestEscherAggregate.class);
		result.addTestSuite(TestExtendedFormatRecord.class);
		result.addTestSuite(TestExternalNameRecord.class);
		result.addTestSuite(TestFontRecord.class);
		result.addTestSuite(TestFormulaRecord.class);
		result.addTestSuite(TestHyperlinkRecord.class);
		result.addTestSuite(TestLabelRecord.class);
		result.addTestSuite(TestMergeCellsRecord.class);
		result.addTestSuite(TestNameRecord.class);
		result.addTestSuite(TestNoteRecord.class);
		result.addTestSuite(TestNoteStructureSubRecord.class);
		result.addTestSuite(TestObjRecord.class);
		result.addTestSuite(TestPaletteRecord.class);
		result.addTestSuite(TestPaneRecord.class);
		result.addTestSuite(TestRecalcIdRecord.class);
		result.addTestSuite(TestRecordFactory.class);
		result.addTestSuite(TestRecordFactoryInputStream.class);
		result.addTestSuite(TestRecordInputStream.class);
		result.addTestSuite(TestSCLRecord.class);
		result.addTestSuite(TestSSTDeserializer.class);
		result.addTestSuite(TestSSTRecord.class);
		result.addTestSuite(TestSSTRecordSizeCalculator.class);
		result.addTestSuite(TestSharedFormulaRecord.class);
		result.addTestSuite(TestStringRecord.class);
		result.addTestSuite(TestStyleRecord.class);
		result.addTestSuite(TestSubRecord.class);
		result.addTestSuite(TestSupBookRecord.class);
		result.addTestSuite(TestTableRecord.class);
		result.addTestSuite(TestTextObjectBaseRecord.class);
		result.addTestSuite(TestTextObjectRecord.class);
		result.addTestSuite(TestUnicodeNameRecord.class);
		result.addTestSuite(TestUnicodeString.class);
		result.addTestSuite(TestWriteAccessRecord.class);
		return result;
	}
}
