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

package org.apache.poi.xssf.usermodel.charts;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.xssf.usermodel.*;

public final class TestXSSFChartLegend extends TestCase {
 
	public void testLegendPositionAccessMethods() throws Exception {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();

		legend.setPosition(LegendPosition.TOP_RIGHT);
		assertEquals(LegendPosition.TOP_RIGHT, legend.getPosition());
	}
}
