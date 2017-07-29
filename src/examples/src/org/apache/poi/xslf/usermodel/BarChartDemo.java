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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xslf.usermodel.charts.AxisOrientation;
import org.apache.poi.xslf.usermodel.charts.AxisPosition;
import org.apache.poi.xslf.usermodel.charts.BarDirection;
import org.apache.poi.xslf.usermodel.charts.XSLFBarChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFCategoryDataSource;
import org.apache.poi.xslf.usermodel.charts.XSLFChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFNumericalDataSource;

/**
 * Build a bar chart from a template pptx
 *
 * @author Yegor Kozlov
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

        BufferedReader modelReader = new BufferedReader(new FileReader(args[1]));
        XMLSlideShow pptx = null;
        try {
            String chartTitle = modelReader.readLine();  // first line is chart title

            // Category Axis Data
            List<String> categories = new ArrayList<String>(3);

            // Values
            List<Double> values = new ArrayList<Double>(3);

            // set model
            String ln;
            while((ln = modelReader.readLine()) != null){
                String[] vals = ln.split("\\s+");
                categories.add(vals[0]);
                values.add(Double.valueOf(vals[1]));
            }

            pptx = new XMLSlideShow(new FileInputStream(args[0]));
            XSLFSlide slide = pptx.getSlides().get(0);
            setBarData(findChart(slide), chartTitle, categories, values);

            XSLFChart chart = findChart(pptx.createSlide().importContent(slide));
            setColumnData(chart, "Column variant", categories, values);

            // save the result
            OutputStream out = new FileOutputStream("bar-chart-demo-output.pptx");
            try {
                pptx.write(out);
            } finally {
                out.close();
            }
        } finally {
            if (pptx != null) pptx.close();
            modelReader.close();
        }
    }

	private static void setBarData(XSLFChart chart, String chartTitle, List<String> categories, List<Double> values) {
		// Series Text
		List<XSLFChartSeries> series = chart.getChartSeries();
		XSLFBarChartSeries bar = (XSLFBarChartSeries) series.get(0);
		bar.setTitle(chartTitle);

		bar.setCategoryData(new XSLFCategoryDataSource(categories));
		bar.setFirstValues(new XSLFNumericalDataSource<Double>(values));
		bar.fillChartData();
	}

	private static void setColumnData(XSLFChart chart, String chartTitle, List<String> categories, List<Double> values) {
		// Series Text
		List<XSLFChartSeries> series = chart.getChartSeries();
		XSLFBarChartSeries bar = (XSLFBarChartSeries) series.get(0);
		bar.setTitle(chartTitle);

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

		if(chart == null) throw new IllegalStateException("chart not found in the template");
		return chart;
	}
}
