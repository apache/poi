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

package org.apache.poi.examples.xslf;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFDoughnutChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Build a chart without reading template file
 */
@SuppressWarnings({"java:S106","java:S4823","java:S1192"})
public final class DoughnutChartFromScratch {
    private DoughnutChartFromScratch() {}

    private static void usage(){
        System.out.println("Usage: DoughnutChartFromScratch <bar-chart-data.txt>");
        System.out.println("    bar-chart-data.txt          the model to set. First line is chart title, " +
                "then go pairs {axis-label value}");
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            usage();
            return;
        }

        try (BufferedReader modelReader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {

            String chartTitle = modelReader.readLine();  // first line is chart title
            String seriesText = modelReader.readLine();
            String[] series = seriesText == null ? new String[0] : seriesText.split(",");

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
                createSlideWithChart(ppt, chartTitle, series, categories, values1, COLUMN_COUNTRIES);
                createSlideWithChart(ppt, chartTitle, series, categories, values2, COLUMN_SPEAKERS);
                // save the result
                try (OutputStream out = new FileOutputStream("doughnut-chart-from-scratch.pptx")) {
                    ppt.write(out);
                }
            }
            try (FileInputStream is = new FileInputStream("doughnut-chart-from-scratch.pptx")) {
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
                                             Double[] values, int valuesColumn) {
        XSLFSlide slide = ppt.createSlide();
        XSLFChart chart = ppt.createChart();
        Rectangle2D rect2D = new java.awt.Rectangle(fromCM(1.5), fromCM(4), fromCM(22), fromCM(14));
        slide.addChart(chart, rect2D);
        setDoughnutData(chart, chartTitle, series, categories, values, valuesColumn);
    }

    private static int fromCM(double cm) {
        return (int) (Math.rint(cm * Units.EMU_PER_CENTIMETER));
    }

    private static void setDoughnutData(XSLFChart chart, String chartTitle, String[] series, String[] categories,
                                        Double[] values, int valuesColumn) {
        final int numOfPoints = categories.length;
        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, COLUMN_LANGUAGES, COLUMN_LANGUAGES));
        final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, valuesColumn, valuesColumn));
        final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange, COLUMN_LANGUAGES);
        final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(values, valuesDataRange, valuesColumn);
        valuesData.setFormatCode("General");

        XDDFDoughnutChartData data = (XDDFDoughnutChartData) chart.createData(ChartTypes.DOUGHNUT, null, null);
        XDDFDoughnutChartData.Series series1 = (XDDFDoughnutChartData.Series) data.addSeries(categoriesData, valuesData);
        series1.setTitle(series[0], chart.setSheetTitle(series[valuesColumn - 1], valuesColumn));

        data.setVaryColors(true);
        data.setHoleSize(42);
        data.setFirstSliceAngle(90);
        chart.plot(data);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.LEFT);
        legend.setOverlay(false);

        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);
        chart.setAutoTitleDeleted(false);
    }

    private static final int COLUMN_LANGUAGES = 0;
    private static final int COLUMN_COUNTRIES = 1;
    private static final int COLUMN_SPEAKERS = 2;
}
