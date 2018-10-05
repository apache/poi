/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;
import java.util.Random;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BarAndLineChart {
    private static final int NUM_OF_ROWS = 7;
    private static final Random RNG = new Random();

    public static void main(String[] args) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Sheet1");

            XSSFRow row = sheet.createRow(0);
            row.createCell(0);
            row.createCell(1).setCellValue("Bars");
            row.createCell(2).setCellValue("Lines");

            XSSFCell cell;
            for (int r = 1; r < NUM_OF_ROWS; r++) {
                row = sheet.createRow(r);
                cell = row.createCell(0);
                cell.setCellValue("C" + r);
                cell = row.createCell(1);
                cell.setCellValue(RNG.nextDouble());
                cell = row.createCell(2);
                cell.setCellValue(RNG.nextDouble() * 10);
            }

            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 11, 15);

            XSSFChart chart = drawing.createChart(anchor);

            // the data sources
            XDDFCategoryDataSource xs = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(1, NUM_OF_ROWS - 1, 0, 0));
            XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, NUM_OF_ROWS - 1, 1, 1));
            XDDFNumericalDataSource<Double> ys2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, NUM_OF_ROWS - 1, 2, 2));

            // cat axis 1 (bars)
            XDDFCategoryAxis barCategories = chart.createCategoryAxis(AxisPosition.BOTTOM);

            // val axis 1 (left)
            XDDFValueAxis leftValues = chart.createValueAxis(AxisPosition.LEFT);
            leftValues.crossAxis(barCategories);
            barCategories.crossAxis(leftValues);

            // cat axis 2 (lines)
            XDDFCategoryAxis lineCategories = chart.createCategoryAxis(AxisPosition.BOTTOM);
            lineCategories.setVisible(false); // this cat axis is deleted

            // val axis 2 (right)
            XDDFValueAxis rightValues = chart.createValueAxis(AxisPosition.RIGHT);
            // this value axis crosses its category axis at max value
            rightValues.setCrosses(AxisCrosses.MAX);
            rightValues.crossAxis(lineCategories);
            lineCategories.crossAxis(rightValues);

            // the bar chart
            XDDFBarChartData bar = (XDDFBarChartData) chart.createData(ChartTypes.BAR, lineCategories, rightValues);
            XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) bar.addSeries(xs, ys1);
            series1.setTitle("Bars", new CellReference("Sheet1!$B$1"));
            bar.setVaryColors(true);
            bar.setBarDirection(BarDirection.COL);
            chart.plot(bar);

            // the line chart
            XDDFLineChartData lines = (XDDFLineChartData) chart.createData(ChartTypes.LINE, lineCategories,
                    rightValues);
            XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) lines.addSeries(xs, ys2);
            series2.setTitle("Lines", new CellReference("Sheet1!$C$1"));
            lines.setVaryColors(true);
            chart.plot(lines);

            // some colors
            solidFillSeries(bar, 0, PresetColor.CHARTREUSE);
            solidLineSeries(lines, 0, PresetColor.TURQUOISE);

            // legend
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);
            legend.setOverlay(false);

            try (FileOutputStream fileOut = new FileOutputStream("BarAndLineChart.xlsx")) {
                wb.write(fileOut);
            }
        }
    }

    private static void solidFillSeries(XDDFChartData data, int index, PresetColor color) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
        XDDFChartData.Series series = data.getSeries().get(index);
        XDDFShapeProperties properties = series.getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setFillProperties(fill);
        series.setShapeProperties(properties);
    }

    private static void solidLineSeries(XDDFChartData data, int index, PresetColor color) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
        XDDFLineProperties line = new XDDFLineProperties();
        line.setFillProperties(fill);
        XDDFChartData.Series series = data.getSeries().get(index);
        XDDFShapeProperties properties = series.getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setLineProperties(line);
        series.setShapeProperties(properties);
    }
}
