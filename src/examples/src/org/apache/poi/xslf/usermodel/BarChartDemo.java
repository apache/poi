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

            // Category Axis Data
            List<String> listCategories = new ArrayList<String>(3);

            // Values
            List<Double> listValues = new ArrayList<Double>(3);

            // set model
            String ln;
            while((ln = modelReader.readLine()) != null){
                String[] vals = ln.split("\\s+");
                listCategories.add(vals[0]);
                listValues.add(Double.valueOf(vals[1]));
            }
            String[] categories = listCategories.toArray(new String[listCategories.size()]);
            Double[] values = listValues.toArray(new Double[listValues.size()]);

            try (XMLSlideShow pptx = new XMLSlideShow(argIS)) {
                XSLFSlide slide = pptx.getSlides().get(0);
                setBarData(findChart(slide), chartTitle, categories, values);

                XSLFChart chart = findChart(pptx.createSlide().importContent(slide));
                setColumnData(chart, "Column variant");

                // save the result
                try (OutputStream out = new FileOutputStream("bar-chart-demo-output.pptx")) {
                    pptx.write(out);
                }
            }
        }
    }

    private static void setBarData(XSLFChart chart, String chartTitle, String[] categories, Double[] values) {
        final List<XDDFChartData> series = chart.getChartSeries();
        final XDDFBarChartData bar = (XDDFBarChartData) series.get(0);

        final int numOfPoints = categories.length;
        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 0, 0));
        final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 1, 1));
        final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange);
        final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(values, valuesDataRange);

        bar.getSeries().get(0).replaceData(categoriesData, valuesData);
        bar.getSeries().get(0).setTitle(chartTitle, chart.setSheetTitle(chartTitle));
        chart.plot(bar);
    }

    private static void setColumnData(XSLFChart chart, String chartTitle) {
        // Series Text
        List<XDDFChartData> series = chart.getChartSeries();
        XDDFBarChartData bar = (XDDFBarChartData) series.get(0);
        bar.getSeries().get(0).setTitle(chartTitle, chart.setSheetTitle(chartTitle));

        // in order to transform a bar chart into a column chart, you just need to change the bar direction
        bar.setBarDirection(BarDirection.COL);

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
}
