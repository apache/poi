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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisOrientation;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;

/**
 * Build a bar chart from a template pptx
 */
public class BarChartDemo {
    private static void usage(){
        System.out.println("Usage: BarChartDemo <bar-chart-template.pptx> <bar-chart-data.txt>");
        System.out.println("    bar-chart-template.pptx     template with a bar chart");
        System.out.println("    bar-chart-data.txt          the model to set. First line is chart title, " +
                "then go pairs {axis-label value}");
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            usage();
            return;
        }

        try (FileInputStream argIS = new FileInputStream(args[0]);
            BufferedReader modelReader = new BufferedReader(new FileReader(args[1]))) {

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

            try (XMLSlideShow pptx = new XMLSlideShow(argIS)) {
                XSLFSlide slide = pptx.getSlides().get(0);
                setBarData(findChart(slide), chartTitle, series, categories, values1, values2);

                XSLFChart chart = findChart(pptx.createSlide().importContent(slide));
                setColumnData(chart, "Column variant");

                // save the result
                try (OutputStream out = new FileOutputStream("bar-chart-demo-output.pptx")) {
                    pptx.write(out);
                }
            }
        }
    }

    private static void setBarData(XSLFChart chart, String chartTitle, String[] series, String[] categories, Double[] values1, Double[] values2) {
        final List<XDDFChartData> data = chart.getChartSeries();
        final XDDFBarChartData bar = (XDDFBarChartData) data.get(0);

        final int numOfPoints = categories.length;
        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnLanguages, columnLanguages));
        final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnCountries, columnCountries));
        final String valuesDataRange2 = chart.formatRange(new CellRangeAddress(1, numOfPoints, columnSpeakers, columnSpeakers));
        final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange, columnLanguages);
        final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(values1, valuesDataRange, columnCountries);
        values1[6] = 16.0; // if you ever want to change the underlying data, it has to be done before building the data source
        final XDDFNumericalDataSource<? extends Number> valuesData2 = XDDFDataSourcesFactory.fromArray(values2, valuesDataRange2, columnSpeakers);

        XDDFChartData.Series series1 = bar.getSeries(0);
        series1.replaceData(categoriesData, valuesData);
        series1.setTitle(series[0], chart.setSheetTitle(series[0], columnCountries));
        XDDFChartData.Series series2 = bar.addSeries(categoriesData, valuesData2);
        series2.setTitle(series[1], chart.setSheetTitle(series[1], columnSpeakers));

        chart.plot(bar);
        chart.setTitleText(chartTitle); // https://stackoverflow.com/questions/30532612
        // chart.setTitleOverlay(overlay);

        // adjust font size for readability
        bar.getCategoryAxis().getOrAddTextProperties().setFontSize(11.5);
        chart.getTitle().getOrAddTextProperties().setFontSize(18.2);
    }

    private static void setColumnData(XSLFChart chart, String chartTitle) {
        // Series Text
        List<XDDFChartData> series = chart.getChartSeries();
        XDDFBarChartData bar = (XDDFBarChartData) series.get(0);

        // in order to transform a bar chart into a column chart, you just need to change the bar direction
        bar.setBarDirection(BarDirection.COL);

        // looking for "Stacked Bar Chart"? uncomment the following line
        // bar.setBarGrouping(BarGrouping.STACKED);

        // additionally, you can adjust the axes
        bar.getCategoryAxis().setOrientation(AxisOrientation.MAX_MIN);
        bar.getValueAxes().get(0).setPosition(AxisPosition.TOP);
    }

    private static XSLFChart findChart(XSLFSlide slide) {
        // find chart in the slide
        XSLFChart chart = null;
        for(POIXMLDocumentPart part : slide.getRelations()){
            if(part instanceof XSLFChart){
                chart = (XSLFChart) part;
                break;
            }
        }

        if(chart == null) {
            throw new IllegalStateException("chart not found in the template");
        }
        return chart;
    }

    private static final int columnLanguages = 0;
    private static final int columnCountries = 1;
    private static final int columnSpeakers = 2;
}
