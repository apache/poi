/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xwpf.usermodel;

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTx;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestXWPFChart {

    /**
     * test method to check charts are not null
     */
    @Test
    void testRead() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("61745.docx")) {
            List<XWPFChart> charts = sampleDoc.getCharts();
            assertNotNull(charts);
            assertEquals(2, charts.size());
            checkData(charts.get(0));
            checkData(charts.get(1));
        }
    }

    private void checkData(XWPFChart chart) {
        assertNotNull(chart);
        assertEquals(1, chart.getChartSeries().size());
        XDDFChartData data = chart.getChartSeries().get(0);
        assertEquals(XDDFBarChartData.class, data.getClass());
        assertEquals(3, data.getSeries().size());
    }

    /**
     * test method to add chart title and check whether it's set
     */
    @Test
    void testChartTitle() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("61745.docx")) {
            List<XWPFChart> charts = sampleDoc.getCharts();
            XWPFChart chart = charts.get(0);
            CTChart ctChart = chart.getCTChart();
            CTTitle title = ctChart.getTitle();
            CTTx tx = title.addNewTx();
            CTTextBody rich = tx.addNewRich();
            rich.addNewBodyPr();
            rich.addNewLstStyle();
            CTTextParagraph p = rich.addNewP();
            CTRegularTextRun r = p.addNewR();
            r.addNewRPr();
            r.setT("XWPF CHART");
            assertEquals("XWPF CHART", chart.getCTChart().getTitle().getTx().getRich().getPArray(0).getRArray(0).getT());
        }
    }

    /**
     * test method to check relationship
     */
    @Test
    void testChartRelation() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("61745.docx")) {
            List<XWPFChart> charts = sampleDoc.getCharts();
            XWPFChart chart = charts.get(0);
            assertEquals(XWPFRelation.CHART.getContentType(), chart.getPackagePart().getContentType());
            assertEquals("/word/document.xml", chart.getParent().getPackagePart().getPartName().getName());
            assertEquals("/word/charts/chart1.xml", chart.getPackagePart().getPartName().getName());
        }
    }

    /**
     * test method to check adding chart in document
     */
    @Test
    void testAddChartsToNewDocument() throws InvalidFormatException, IOException {
        try (XWPFDocument document = new XWPFDocument()) {

            XWPFChart chart = document.createChart();
            assertEquals(1, document.getCharts().size());
            assertNotNull(chart);
            assertNotNull(chart.getCTChartSpace());
            assertNotNull(chart.getCTChart());
            assertEquals(XWPFChart.DEFAULT_HEIGHT, chart.getChartHeight());
            assertEquals(XWPFChart.DEFAULT_WIDTH, chart.getChartWidth());

            XWPFChart chart2 = document.createChart();
            assertEquals(2, document.getCharts().size());
            assertNotNull(chart2);
            assertNotNull(chart2.getCTChartSpace());
            assertNotNull(chart2.getCTChart());
            chart.setChartHeight(500500);
            assertEquals(500500, chart.getChartHeight());

            assertNotNull(XWPFTestDataSamples.writeOutAndReadBack(document));
        }
    }
}
