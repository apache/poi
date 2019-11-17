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
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;

/**
 * Build a pie chart from a template pptx
 */
public class PieChartDemo {
    private static void usage(){
        System.out.println("Usage: PieChartDemo <pie-chart-template.pptx> <pie-chart-data.txt>");
        System.out.println("    pie-chart-template.pptx     template with a pie chart");
        System.out.println("    pie-chart-data.txt          the model to set. First line is chart title, " +
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

            try (XMLSlideShow pptx = new XMLSlideShow(argIS)) {
                XSLFSlide slide = pptx.getSlides().get(0);

                // find chart in the slide
                XSLFChart chart = null;
                for (POIXMLDocumentPart part : slide.getRelations()) {
                    if (part instanceof XSLFChart) {
                        chart = (XSLFChart) part;
                        break;
                    }
                }

	            if(chart == null) {
	                throw new IllegalStateException("chart not found in the template");
	            }

	            // Series Text
	            List<XDDFChartData> series = chart.getChartSeries();
	            XDDFPieChartData pie = (XDDFPieChartData) series.get(0);

	            // Category Axis Data
	            List<String> listCategories = new ArrayList<>(3);

	            // Values
	            List<Double> listValues = new ArrayList<>(3);

	            // set model
	            String ln;
	            while((ln = modelReader.readLine()) != null){
	                String[] vals = ln.split("\\s+");
	                listCategories.add(vals[0]);
	                listValues.add(Double.valueOf(vals[1]));
	            }
	            String[] categories = listCategories.toArray(new String[0]);
	            Double[] values = listValues.toArray(new Double[0]);

	            final int numOfPoints = categories.length;
	            final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 0, 0));
	            final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 1, 1));
	            final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange);
	            final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(values, valuesDataRange);

	            XDDFPieChartData.Series firstSeries = (XDDFPieChartData.Series) pie.getSeries(0);
	            firstSeries.replaceData(categoriesData, valuesData);
	            firstSeries.setTitle(chartTitle, chart.setSheetTitle(chartTitle, 0));
	            firstSeries.setExplosion(25L);
	            chart.plot(pie);

                // save the result
                try (OutputStream out = new FileOutputStream("pie-chart-demo-output.pptx")) {
                    pptx.write(out);
                }
            }
        }
    }
}
