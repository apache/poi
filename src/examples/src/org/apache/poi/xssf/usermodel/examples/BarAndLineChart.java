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

import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LayoutMode;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFManualLayout;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xddf.usermodel.text.UnderlineType;
import org.apache.poi.xddf.usermodel.text.XDDFFont;
import org.apache.poi.xddf.usermodel.text.XDDFRunProperties;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// original contributions by Axel Richter on https://stackoverflow.com/questions/47065690
// additional title formatting from https://stackoverflow.com/questions/50418856
// and legend positioning from https://stackoverflow.com/questions/49615379
// this would probably be an answer for https://stackoverflow.com/questions/36447925 too
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
            chart.setTitleText("This is my title");
            chart.setTitleOverlay(true);
            XDDFRunProperties properties = new XDDFRunProperties();
            properties.setBold(true);
            properties.setItalic(true);
            properties.setUnderline(UnderlineType.DOT_DOT_DASH_HEAVY);
            properties.setFontSize(22.5);
            XDDFFont[] fonts = new XDDFFont[] {
                    new XDDFFont(FontGroup.LATIN, "Calibri", null, null, null),
                    new XDDFFont(FontGroup.COMPLEX_SCRIPT, "Liberation Sans", null, null, null)
                    };
            properties.setFonts(fonts);
            properties.setLineProperties(new XDDFLineProperties(
                    new XDDFSolidFillProperties(XDDFColor.from(PresetColor.SIENNA))));
            XDDFTextParagraph paragraph = chart.getTitle().getBody().getParagraph(0);
            paragraph.setDefaultRunProperties(properties);

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
            XDDFBarChartData bar = (XDDFBarChartData) chart.createData(ChartTypes.BAR, barCategories, leftValues);
            XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) bar.addSeries(xs, ys1);
            series1.setTitle(null, new CellReference(sheet.getSheetName(), 0, 1, true,true));
            bar.setVaryColors(true);
            bar.setBarDirection(BarDirection.COL);
            chart.plot(bar);

            // the line chart on secondary axis
            XDDFLineChartData lines = (XDDFLineChartData) chart.createData(ChartTypes.LINE, lineCategories,
                    rightValues);

            //uncomment below line if only primary axis required and comment above line
            // the line chart on primary axis
            /*XDDFLineChartData lines = (XDDFLineChartData) chart.createData(ChartTypes.LINE, lineCategories,
                    leftValues);*/


            XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) lines.addSeries(xs, ys2);
            series2.setTitle(null, new CellReference(sheet.getSheetName(), 0, 2, true, true));
            series2.setSmooth(false);
            series2.setMarkerStyle(MarkerStyle.DIAMOND);
            series2.setMarkerSize((short)14);
            lines.setVaryColors(true);
            chart.plot(lines);

            // some colors
            XDDFFillProperties solidChartreuse = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.CHARTREUSE));
            XDDFFillProperties solidTurquoise = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.TURQUOISE));
            XDDFLineProperties solidLines = new XDDFLineProperties(solidTurquoise);
            series1.setFillProperties(solidChartreuse);
            series1.setLineProperties(solidLines); // bar border color different from fill
            series2.setLineProperties(solidLines);

            // legend
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.LEFT);
            legend.setOverlay(false);
            XDDFManualLayout layout = legend.getOrAddManualLayout();
            layout.setXMode(LayoutMode.EDGE);
            layout.setYMode(LayoutMode.EDGE);
            layout.setX(0.00); //left edge of the chart
            layout.setY(0.25); //25% of chart's height from top edge of the chart

            try (FileOutputStream fileOut = new FileOutputStream("BarAndLineChart.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
