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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * Test get/set chart title.
 */
public class TestXSSFChartTitle {
    private XSSFWorkbook createWorkbookWithChart() {
    	XSSFWorkbook wb = new XSSFWorkbook();
    	XSSFSheet sheet = wb.createSheet("linechart");
        final int NUM_OF_ROWS = 3;
        final int NUM_OF_COLUMNS = 10;

        // Create a row and put some cells in it. Rows are 0 based.
        Row row;
        Cell cell;
        for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
            row = sheet.createRow((short) rowIndex);
            for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
                cell = row.createCell((short) colIndex);
                cell.setCellValue(colIndex * (rowIndex + 1));
            }
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 15);

        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        // Use a category axis for the bottom axis.
        XDDFChartAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(0, 0, 0, NUM_OF_COLUMNS - 1));
        XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, 1, 0, NUM_OF_COLUMNS - 1));
        XDDFNumericalDataSource<Double> ys2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, 2, 0, NUM_OF_COLUMNS - 1));

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.addSeries(xs, ys1);
        data.addSeries(xs, ys2);
        chart.plot(data);

        return wb;
    }

    /**
     * Gets the first chart from the named sheet in the workbook.
     */
    private XSSFChart getChartFromWorkbook(XSSFWorkbook wb, String sheetName) {
        XSSFSheet sheet = wb.getSheet(sheetName);
        XSSFSheet xsheet = sheet;
        XSSFDrawing drawing = xsheet.getDrawingPatriarch();
        if (drawing != null) {
            List<XSSFChart> charts = drawing.getCharts();
            if (charts != null && charts.size() > 0) {
                return charts.get(0);
            }
        }
        return null;
    }

    @Test
    public void testNewChart() throws IOException {
        XSSFWorkbook wb = createWorkbookWithChart();
        XSSFChart chart = getChartFromWorkbook(wb, "linechart");
        assertNotNull(chart);
        assertNull(chart.getTitleText());
        final String myTitle = "My chart title";
        chart.setTitleText(myTitle);
        XSSFRichTextString queryTitle = chart.getTitleText();
        assertNotNull(queryTitle);
        assertEquals(myTitle, queryTitle.toString());

        final String myTitleFormula = "1 & \" and \" & 2";
        chart.setTitleFormula(myTitleFormula);
        // setting formula should unset text, but since there is a formula, returns an empty string
        assertEquals("", chart.getTitleText().toString());
        String titleFormula = chart.getTitleFormula();
        assertNotNull(titleFormula);
        assertEquals(myTitleFormula, titleFormula);
        wb.close();
    }

    @Test
    public void testExistingChartWithTitle() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chartTitle_withTitle.xlsx");
        XSSFChart chart = getChartFromWorkbook(wb, "Sheet1");
        assertNotNull(chart);
        XSSFRichTextString originalTitle = chart.getTitleText();
        assertNotNull(originalTitle);
        final String myTitle = "My chart title";
        assertFalse(myTitle.equals(originalTitle.toString()));
        chart.setTitleText(myTitle);
        XSSFRichTextString queryTitle = chart.getTitleText();
        assertNotNull(queryTitle);
        assertEquals(myTitle, queryTitle.toString());
        wb.close();
    }

    @Test
    public void testExistingChartNoTitle() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chartTitle_noTitle.xlsx");
        XSSFChart chart = getChartFromWorkbook(wb, "Sheet1");
        assertNotNull(chart);
        assertNull(chart.getTitleText());
        final String myTitle = "My chart title";
        chart.setTitleText(myTitle);
        XSSFRichTextString queryTitle = chart.getTitleText();
        assertNotNull(queryTitle);
        assertEquals(myTitle, queryTitle.toString());
        wb.close();
    }

    @Test
    public void testExistingChartWithFormulaTitle() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chartTitle_withTitleFormula.xlsx");
        XSSFChart chart = getChartFromWorkbook(wb, "Sheet1");
        assertNotNull(chart);
        XSSFRichTextString originalTitle = chart.getTitleText();
        assertNotNull(originalTitle);
        assertEquals("", originalTitle.toString());
        String formula = chart.getTitleFormula();
        assertNotNull(formula);
        assertEquals("Sheet1!$E$1", formula);
        wb.close();
    }

}
