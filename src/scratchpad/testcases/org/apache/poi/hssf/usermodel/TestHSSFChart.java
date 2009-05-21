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

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.chart.SeriesRecord;

/**
 * Tests for {@link HSSFChart}
 */
public final class TestHSSFChart extends TestCase {

	public void testSingleChart() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithChart.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(1, HSSFChart.getSheetCharts(s2).length);
		assertEquals(0, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the chart on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals(null, charts[0].getChartTitle());

		// Check x, y, width, height
		assertEquals(0, charts[0].getChartX());
		assertEquals(0, charts[0].getChartY());
		assertEquals(26492928, charts[0].getChartWidth());
		assertEquals(15040512, charts[0].getChartHeight());
	}

	public void testTwoCharts() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithTwoCharts.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(1, HSSFChart.getSheetCharts(s2).length);
		assertEquals(1, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the chart on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals(null, charts[0].getChartTitle());

		// And the third sheet
		charts = HSSFChart.getSheetCharts(s3);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("Squares", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("Base Numbers", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals(null, charts[0].getChartTitle());
	}

	public void testThreeCharts() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithThreeCharts.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(2, HSSFChart.getSheetCharts(s2).length);
		assertEquals(1, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the charts on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(2, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals(6, charts[0].getSeries()[0].getNumValues());
		assertEquals(6, charts[0].getSeries()[1].getNumValues());
		assertEquals(SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, charts[0].getSeries()[0].getValueType());
		assertEquals(SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, charts[0].getSeries()[1].getValueType());
		assertEquals(null, charts[0].getChartTitle());

		assertEquals(1, charts[1].getSeries().length);
		assertEquals(null, charts[1].getSeries()[0].getSeriesTitle());
		assertEquals("Pie Chart Title Thingy", charts[1].getChartTitle());

		// And the third sheet
		charts = HSSFChart.getSheetCharts(s3);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("Squares", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("Base Numbers", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals("Sheet 3 Chart with Title", charts[0].getChartTitle());
	}
}
