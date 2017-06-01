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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.poi.xslf.usermodel.charts.AxisOrientation;
import org.apache.poi.xslf.usermodel.charts.AxisPosition;
import org.apache.poi.xslf.usermodel.charts.BarDirection;
import org.apache.poi.xslf.usermodel.charts.XSLFBarChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFCategoryDataSource;
import org.apache.poi.xslf.usermodel.charts.XSLFChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFNumericalDataSource;
import org.apache.poi.xslf.usermodel.charts.XSLFPieChartSeries;
import org.junit.Test;

public class TestXSLFChart {

    /**
     * a modified version from POI-examples
     */
    @Test
    public void testFillPieChartTemplate() throws IOException {

        String chartTitle = "Apache POI";  // first line is chart title

        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("pie-chart.pptx");
        XSLFSlide slide = pptx.getSlides().get(0);

        // find chart in the slide
        XSLFChart chart = null;
        for(POIXMLDocumentPart part : slide.getRelations()){
            if(part instanceof XSLFChart){
                chart = (XSLFChart) part;
                break;
            }
        }

        if(chart == null) throw new IllegalStateException("chart not found in the template");

        // Series Text
        List<XSLFChartSeries> series = chart.getChartSeries();
        XSLFPieChartSeries pie = (XSLFPieChartSeries) series.get(0);
        pie.setTitle(chartTitle);
        pie.setExplosion(25);

        // Category Axis Data
        List<String> categories = new ArrayList<String>(3);
        categories.add("First");
        categories.add("Second");
        categories.add("Third");

        // Values
        List<Integer> values = new ArrayList<Integer>(3);
        values.add(1);
        values.add(3);
        values.add(4);

        pie.setCategoryData(new XSLFCategoryDataSource(categories));
        pie.setFirstValues(new XSLFNumericalDataSource<Integer>(values));
        pie.fillChartData();

        pptx.close();
    }

    @Test
    public void testFillBarChartTemplate() throws IOException {

        String chartTitle = "Apache POI";  // first line is chart title

        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("bar-chart.pptx");
        XSLFSlide slide = pptx.getSlides().get(0);

        // find chart in the slide
        XSLFChart chart = null;
        for(POIXMLDocumentPart part : slide.getRelations()){
            if(part instanceof XSLFChart){
                chart = (XSLFChart) part;
                break;
            }
        }

        if(chart == null) throw new IllegalStateException("chart not found in the template");

        // Series Text
        List<XSLFChartSeries> series = chart.getChartSeries();
        XSLFBarChartSeries bar = (XSLFBarChartSeries) series.get(0);
        bar.setTitle(chartTitle);
        bar.setBarDirection(BarDirection.COL);

        // additionally, you can adjust the axes
		bar.getCategoryAxis().setOrientation(AxisOrientation.MIN_MAX);
		bar.getValueAxes().get(0).setPosition(AxisPosition.BOTTOM);

        // Category Axis Data
        List<String> categories = new ArrayList<String>(3);
        categories.add("First");
        categories.add("Second");
        categories.add("Third");

        // Values
        List<Integer> values = new ArrayList<Integer>(3);
        values.add(1);
        values.add(3);
        values.add(4);

        bar.setCategoryData(new XSLFCategoryDataSource(categories));
        bar.setFirstValues(new XSLFNumericalDataSource<Integer>(values));
        bar.fillChartData();

        pptx.close();
    }

}
