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

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.charts.XSSFChartDataFactory;

public final class TestXSSFScatterChartData  extends TestCase {
 
	public void testOneSeriePlot() throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet();
		XSSFDrawing drawing = sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		XSSFChart chart = drawing.createChart(anchor);

		ChartAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
		ChartAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

		ScatterChartData scatterChartData =
				XSSFChartDataFactory.getInstance().createScatterChartData();

		ScatterChartSerie serie = scatterChartData.addSerie();
		serie.setXValues(sheet, new CellRangeAddress(0,0,1,10));
		serie.setYValues(sheet, new CellRangeAddress(1,1,1,10));

		assertEquals(scatterChartData.getSeries().size(), 1);

		chart.plot(scatterChartData, bottomAxis, leftAxis);
	}

}
