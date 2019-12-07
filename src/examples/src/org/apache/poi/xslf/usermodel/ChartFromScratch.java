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

package org.apache.poi.xslf.usermodel;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.AxisTickMark;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;

/**
 * Build a chart without reading template file
 */
public class ChartFromScratch {
    private static void usage(){
        System.out.println("Usage: ChartFromScratch <bar-chart-data.txt>");
        System.out.println("    bar-chart-data.txt          the model to set. First line is chart title, " +
                "then go pairs {axis-label value}");
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            usage();
            return;
        }

        try (BufferedReader modelReader = new BufferedReader(new FileReader(args[0]))) {

            String chartTitle = modelReader.readLine();  // first line is chart title
            String[] series = modelReader.readLine().split(",");

            // Category Axis Data
            List<String> listLanguages = new ArrayList<>(10);

            // Values
            List<Double> listCountries = new ArrayList<>(10);
            List<Double> listSpeakers = new ArrayList<>(10);

            // set model
            String ln;
            while((ln = modelReader.readLine()) != null) {
                String[] vals = ln.split(",");
                listCountries.add(Double.valueOf(vals[0]));
                listSpeakers.add(Double.valueOf(vals[1]));
                listLanguages.add(vals[2]);
            }

            String[] categories = listLanguages.toArray(new String[0]);
            Double[] values1 = listCountries.toArray(new Double[0]);
            Double[] values2 = listSpeakers.toArray(new Double[0]);

            try (XMLSlideShow ppt = new XMLSlideShow()) {
                createSlideWithChart(ppt, chartTitle, series, categories, values1, values2);
                createSlideWithChart(ppt, chartTitle, series, categories, values1, values2);
                createSlideWithChart(ppt, chartTitle, series, categories, values1, values2);
                // save the result
                try (OutputStream out = new FileOutputStream("chart-from-scratch.pptx")) {
                    ppt.write(out);
                }
            }
            try (FileInputStream is = new FileInputStream("chart-from-scratch.pptx")) {
                try (XMLSlideShow ppt = new XMLSlideShow(is)) {
                    for (XSLFSlide slide : ppt.getSlides()) {
                        for (XSLFShape shape : slide.getShapes()) {
                            if (shape instanceof XSLFGraphicFrame) {
                                XSLFGraphicFrame frame = (XSLFGraphicFrame) shape;
                                if (frame.hasChart()) {
                                    System.out.println(frame.getChart().getTitleShape().getText());
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Done");
    }

    private static void createSlideWithChart(XMLSlideShow ppt, String chartTitle, String[] series, String[] categories,
            Double[] values1, Double[] values2) {
        XSLFSlide slide = ppt.createSlide();
        XSLFChart chart = ppt.createChart();
        Rectangle2D rect2D = new java.awt.Rectangle(fromCM(1.5), fromCM(4), fromCM(22), fromCM(14));
        slide.addChart(chart, rect2D);
        setBarData(chart, chartTitle, series, categories, values1, values2);
    }

    private static int fromCM(double cm) {
        return (int) (Math.rint(cm * Units.EMU_PER_CENTIMETER));
    }

    private static void setBarData(XSLFChart chart, String chartTitle, String[] series, String[] categories, Double[] values1, Double[] values2) {
        // Use a category axis for the bottom axis.
        XDDFChartAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(series[2]);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(series[0]+","+series[1]);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setMajorTickMark(AxisTickMark.OUT);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

        final int numOfPoints = categories.length;
        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnLanguages, columnLanguages));
        final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnCountries, columnCountries));
        final String valuesDataRange2 = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnSpeakers, columnSpeakers));
        final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange, columnLanguages);
        final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(values1, valuesDataRange, columnCountries);
        valuesData.setFormatCode("General");
        values1[6] = 16.0; // if you ever want to change the underlying data, it has to be done before building the data source
        final XDDFNumericalDataSource<? extends Number> valuesData2 = XDDFDataSourcesFactory.fromArray(values2, valuesDataRange2, columnSpeakers);
        valuesData2.setFormatCode("General");


        XDDFBarChartData bar = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        bar.setBarGrouping(BarGrouping.CLUSTERED);

        XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) bar.addSeries(categoriesData, valuesData);
        series1.setTitle(series[0], chart.setSheetTitle(series[0], columnCountries));

        XDDFBarChartData.Series series2 = (XDDFBarChartData.Series) bar.addSeries(categoriesData, valuesData2);
        series2.setTitle(series[1], chart.setSheetTitle(series[1], columnSpeakers));

        bar.setVaryColors(true);
        bar.setBarDirection(BarDirection.COL);
        chart.plot(bar);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.LEFT);
        legend.setOverlay(false);

        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);
        chart.setAutoTitleDeleted(false);
    }

    private static final int columnLanguages = 0;
    private static final int columnCountries = 1;
    private static final int columnSpeakers = 2;
}
