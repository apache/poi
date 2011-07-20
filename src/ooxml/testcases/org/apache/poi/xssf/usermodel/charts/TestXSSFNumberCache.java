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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.DataMarker;
import org.apache.poi.ss.util.SheetBuilder;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import junit.framework.TestCase;

public class TestXSSFNumberCache extends TestCase {
    private static Object[][] plotData = {
	{0,      1,       2,       3,       4},
	{0, "=B1*2", "=C1*2", "=D1*2", "=E1*4"}
    };

    public void testFormulaCache() {
	Workbook wb = new XSSFWorkbook();
	Sheet sheet = new SheetBuilder(wb, plotData).build();
	Drawing drawing = sheet.createDrawingPatriarch();
	ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
	Chart chart = drawing.createChart(anchor);

	ChartAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
	ChartAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

	ScatterChartData scatterChartData =
	    chart.getChartDataFactory().createScatterChartData();

	DataMarker xMarker = new DataMarker(sheet, CellRangeAddress.valueOf("A1:E1"));
	DataMarker yMarker = new DataMarker(sheet, CellRangeAddress.valueOf("A2:E2"));
	ScatterChartSerie serie = scatterChartData.addSerie(xMarker, yMarker);

	chart.plot(scatterChartData, bottomAxis, leftAxis);

	XSSFScatterChartData.Serie xssfScatterSerie =
	    (XSSFScatterChartData.Serie) serie;
	XSSFNumberCache yCache = xssfScatterSerie.getLastCalculatedYCache();

	assertEquals(5, yCache.getPointCount());
	assertEquals(4.0, yCache.getValueAt(3), 0.00001);
	assertEquals(16.0, yCache.getValueAt(5), 0.00001);
    }


}