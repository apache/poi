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

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.AxisTickLabelPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLegendEntry;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xddf.usermodel.text.XDDFRunProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This example is based on original contributions by Axel Richter on StackOverflow.
 *
 * <em>Note from original author</em>:
 * This only works for Excel since OpenOffice or LibreOffice Calc is not able having series having literal numeric values set.
 *
 * @see <a href="https://stackoverflow.com/questions/50772989/">Create target marker in a bar chart with openxmlformats</a>
 * @see <a href="https://stackoverflow.com/questions/50873700/">Change axis color and font of the chart in openxmlformats</a>
 * @see <a href="https://stackoverflow.com/questions/51530552/">Change colors of line chart Apache POI</a>
 */
class ExcelChartWithTargetLine {

    private static final int NUM_OF_ROWS = 6;

    private static void createChart(XSSFChart chart, XSSFSheet sheet, int[] chartedCols, double target) {
        // some colors
        XDDFFillProperties[] fills = new XDDFFillProperties[] {
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.TURQUOISE)),
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.CHARTREUSE)),
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.LAVENDER)),
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.CHOCOLATE)),
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.TOMATO)),
            new XDDFSolidFillProperties(XDDFColor.from(PresetColor.PLUM))
        };
        XDDFLineProperties solidTurquoise = new XDDFLineProperties(fills[0]);
        XDDFLineProperties solidTomato = new XDDFLineProperties(fills[4]);
        XDDFLineProperties solidPlum = new XDDFLineProperties(fills[5]);
        XDDFSolidFillProperties solidAlmond = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLANCHED_ALMOND));
        XDDFSolidFillProperties solidGray = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.DARK_SLATE_GRAY));


        // the bar chart

        XDDFCategoryAxis barCategories = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftValues = chart.createValueAxis(AxisPosition.LEFT);
        leftValues.crossAxis(barCategories);
        barCategories.crossAxis(leftValues);

        // from https://stackoverflow.com/questions/50873700/
        // colored major grid lines
        leftValues.getOrAddMajorGridProperties().setLineProperties(solidTomato);
        //colored axis line
        leftValues.getOrAddShapeProperties().setLineProperties(solidPlum);
        // axis font
        XDDFRunProperties props = leftValues.getOrAddTextProperties();
        props.setFontSize(14.0);
        props.setFillProperties(fills[5]);

        XDDFBarChartData bar = (XDDFBarChartData) chart.createData(ChartTypes.BAR, barCategories, leftValues);
        bar.setVaryColors(true);
        bar.setBarDirection(chartedCols.length > 1 ? BarDirection.COL : BarDirection.BAR);

        for (int c : chartedCols) {
            // the data sources
            XDDFCategoryDataSource xs = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(1, NUM_OF_ROWS, 0, 0));
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, NUM_OF_ROWS, c, c));
            XDDFBarChartData.Series series = (XDDFBarChartData.Series) bar.addSeries(xs, ys);
            series.setTitle(null, new CellReference(sheet.getSheetName(), 0, c, true, true));
            series.setFillProperties(fills[c]);
            series.setLineProperties(solidTurquoise); // bar border color different from fill
        }
        chart.plot(bar);


        // target line
        // line of a scatter chart from 0 (min) to 1 (max) having value of target

        XDDFValueAxis scatterX = chart.createValueAxis(AxisPosition.TOP);
        scatterX.setVisible(false);
        scatterX.setTickLabelPosition(AxisTickLabelPosition.NONE);
        XDDFValueAxis scatterY = chart.createValueAxis(AxisPosition.RIGHT);
        scatterY.setVisible(false);
        scatterY.setTickLabelPosition(AxisTickLabelPosition.NONE);
        scatterX.crossAxis(scatterY);
        scatterY.crossAxis(scatterX);
        if (chartedCols.length > 1) {
            scatterX.setMaximum(1.0);
        } else {
            scatterY.setMaximum(1.0);
        }

        XDDFScatterChartData scatter = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, scatterX, scatterY);
        scatter.setVaryColors(true);

        //  This only works for Excel since OpenOffice or LibreOffice Calc does not support literal numeric data series.
        XDDFNumericalDataSource<Double> targetDS = XDDFDataSourcesFactory.fromArray(new Double[] { target, target });
        XDDFNumericalDataSource<Double> zeroOneDS = XDDFDataSourcesFactory.fromArray(new Double[] { 0.0, 1.0 });

        if (chartedCols.length > 1) {
            // BarDirection.COL then X axis is from 0 to 1 and Y axis is target axis
            scatter.addSeries(zeroOneDS, targetDS).setLineProperties(solidTurquoise);
        } else {
            // BarDirection.BAR then X axis is target axis and Y axis is from 0 to 1
            scatter.addSeries(targetDS, zeroOneDS).setLineProperties(solidTurquoise);
        }

        chart.plot(scatter);


        // legend
        if (chartedCols.length > 1) {
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.LEFT);
            legend.setOverlay(false);

            // delete additional target line series legend entry
            XDDFLegendEntry entry = legend.addEntry();
            entry.setIndex(0);
            entry.setDelete(true);
        }


        // from https://stackoverflow.com/questions/51530552/
        // customize the chart

        // do not auto delete the title
        chart.setAutoTitleDeleted(false);

        // plot area background and border line
        XDDFShapeProperties chartProps = chart.getOrAddShapeProperties();
        chartProps.setFillProperties(solidAlmond);
        chartProps.setLineProperties(new XDDFLineProperties(solidGray));

        // line style of cat axis
        XDDFLineProperties categoriesProps = new XDDFLineProperties(solidGray);
        categoriesProps.setWidth(2.1);
        barCategories.getOrAddShapeProperties().setLineProperties(categoriesProps);
    }

    private static XSSFClientAnchor createAnchor(XSSFDrawing drawing, int[] chartedCols) {
        if (chartedCols.length > 1) {
            return drawing.createAnchor(0, 0, 0, 0, 0, 8, 10, 23);
        } else {
            return drawing.createAnchor(0, 0, 0, 0, 0, 8, 5, 23);
        }
    }

    public static void main(String[] args) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("targetline");
            final int NUM_OF_COLUMNS = 4;

            // create some data
            XSSFRow row;
            XSSFCell cell;
            String[] headings = new String[] { "Year", "Male", "Female", "Other" };
            int rowIndex = 0;
            row = sheet.createRow(rowIndex);
            for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
                cell = row.createCell(colIndex);
                cell.setCellValue(headings[colIndex]);
            }
            double[][] values = new double[][] { new double[] { 1980, 56.0, 44.1, 12.2 },
                    new double[] { 1985, 34.5, 41.0, 4 }, new double[] { 1990, 65.0, 68.5, 9.1 },
                    new double[] { 1995, 34.7, 47.6, 4.9 }, new double[] { 2000, 23.0, 64.5, 11.1 },
                    new double[] { 2005, 56.3, 69.8, 9.5 } };
            for (; rowIndex < NUM_OF_ROWS; rowIndex++) {
                row = sheet.createRow(rowIndex + 1);
                for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
                    cell = row.createCell(colIndex);
                    cell.setCellValue(values[rowIndex][colIndex]);
                }
            }

            int[] chartedCols = new int[] {  1,  2  , 3  };

            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = createAnchor(drawing, chartedCols);
            XSSFChart chart = drawing.createChart(anchor);
            createChart(chart, sheet, chartedCols, 42.0);

            try (FileOutputStream fos = new FileOutputStream("ExcelChartWithTargetLine.xlsx")) {
                workbook.write(fos);
            }
        }
    }
}
