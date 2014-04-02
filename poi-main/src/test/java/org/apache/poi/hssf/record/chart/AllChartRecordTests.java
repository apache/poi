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

package org.apache.poi.hssf.record.chart;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests for package <tt>org.apache.poi.hssf.record.class</tt>.
 * 
 * @author Josh Micich
 */
public final class AllChartRecordTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite(AllChartRecordTests.class.getName());

		result.addTestSuite(TestAreaFormatRecord.class);
		result.addTestSuite(TestAreaRecord.class);
		result.addTestSuite(TestAxisLineFormatRecord.class);
		result.addTestSuite(TestAxisOptionsRecord.class);
		result.addTestSuite(TestAxisParentRecord.class);
		result.addTestSuite(TestAxisRecord.class);
		result.addTestSuite(TestAxisUsedRecord.class);
		result.addTestSuite(TestBarRecord.class);
		result.addTestSuite(TestCategorySeriesAxisRecord.class);
		result.addTestSuite(TestChartFormatRecord.class);
		result.addTestSuite(TestChartRecord.class);
		result.addTestSuite(TestChartTitleFormatRecord.class);
		result.addTestSuite(TestDatRecord.class);
		result.addTestSuite(TestDataFormatRecord.class);
		result.addTestSuite(TestDefaultDataLabelTextPropertiesRecord.class);
		result.addTestSuite(TestFontBasisRecord.class);
		result.addTestSuite(TestFontIndexRecord.class);
		result.addTestSuite(TestFrameRecord.class);
		result.addTestSuite(TestLegendRecord.class);
		result.addTestSuite(TestLineFormatRecord.class);
		result.addTestSuite(TestLinkedDataRecord.class);
		result.addTestSuite(TestNumberFormatIndexRecord.class);
		result.addTestSuite(TestObjectLinkRecord.class);
		result.addTestSuite(TestPlotAreaRecord.class);
		result.addTestSuite(TestPlotGrowthRecord.class);
		result.addTestSuite(TestSeriesChartGroupIndexRecord.class);
		result.addTestSuite(TestSeriesIndexRecord.class);
		result.addTestSuite(TestSeriesLabelsRecord.class);
		result.addTestSuite(TestSeriesListRecord.class);
		result.addTestSuite(TestSeriesRecord.class);
		result.addTestSuite(TestSeriesTextRecord.class);
		result.addTestSuite(TestSeriesToChartGroupRecord.class);
		result.addTestSuite(TestSheetPropertiesRecord.class);
		result.addTestSuite(TestTextRecord.class);
		result.addTestSuite(TestTickRecord.class);
		result.addTestSuite(TestUnitsRecord.class);
		result.addTestSuite(TestValueRangeRecord.class);
		return result;
	}
}
