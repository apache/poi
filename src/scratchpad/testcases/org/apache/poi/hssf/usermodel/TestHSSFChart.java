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

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

public class TestHSSFChart extends TestCase {
	private String dirName;

	protected void setUp() throws Exception {
		dirName = System.getProperty("HSSF.testdata.path");
	}

	public void testSingleChart() throws Exception {
		
	}

	public void testTwoCharts() throws Exception {
		
	}

	public void BROKENtestThreeCharts() throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook(
				new FileInputStream(new File(dirName, "WithThreeCharts.xls"))
		);
		
		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);
		
		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(2, HSSFChart.getSheetCharts(s2).length);
		assertEquals(1, HSSFChart.getSheetCharts(s3).length);
		
		HSSFChart[] charts;
		
		charts = HSSFChart.getSheetCharts(s2);
		assertNull(charts[0].getChartTitle());
		assertEquals("Pie Chart Title Thingy", charts[1].getChartTitle());
		
		charts = HSSFChart.getSheetCharts(s3);
		assertEquals("Sheet 3 Chart with Title", charts[1].getChartTitle());
	}
}
