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

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFChart extends TestCase {

    /**
     * a modified version from POI-examples
     */
    public void testFillChartTemplate() throws Exception {

        String chartTitle = "Apache POI";  // first line is chart title

        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("pie-chart.pptx");
        XSLFSlide slide = pptx.getSlides()[0];

        // find chart in the slide
        XSLFChart chart = null;
        for(POIXMLDocumentPart part : slide.getRelations()){
            if(part instanceof XSLFChart){
                chart = (XSLFChart) part;
                break;
            }
        }

        if(chart == null) throw new IllegalStateException("chart not found in the template");

        // embedded Excel workbook that holds the chart data
        POIXMLDocumentPart xlsPart = chart.getRelations().get(0);
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();

        CTPieChart pieChart = plotArea.getPieChartArray(0);
        //Pie Chart Series
        CTPieSer ser = pieChart.getSerArray(0);

        // Series Text
        CTSerTx tx = ser.getTx();
        tx.getStrRef().getStrCache().getPtArray(0).setV(chartTitle);
        sheet.createRow(0).createCell(1).setCellValue(chartTitle);
        String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
        tx.getStrRef().setF(titleRef);


        // Category Axis Data
        CTAxDataSource cat = ser.getCat();
        CTStrData strData = cat.getStrRef().getStrCache();

        // Values
        CTNumDataSource valSrc = ser.getVal();
        CTNumData numData = valSrc.getNumRef().getNumCache();

        strData.setPtArray(null);  // unset old axis text
        numData.setPtArray(null);  // unset old values

        Map<String, Double> pieModel = new LinkedHashMap<String, Double>();
        pieModel.put("First", 1.0);
        pieModel.put("Second", 3.0);
        pieModel.put("Third", 4.0);

        // set model
        int idx = 0;
        int rownum = 1;
        for(String key : pieModel.keySet()){
            double val = pieModel.get(key);

            CTNumVal numVal = numData.addNewPt();
            numVal.setIdx(idx);
            numVal.setV("" + val);

            CTStrVal sVal = strData.addNewPt();
            sVal.setIdx(idx);
            sVal.setV(key);

            idx++;
            XSSFRow row = sheet.createRow(rownum++);
            row.createCell(0).setCellValue(key);
            row.createCell(1).setCellValue(val);
        }
        numData.getPtCount().setVal(idx);
        strData.getPtCount().setVal(idx);

        String numDataRange = new CellRangeAddress(1, rownum-1, 1, 1).formatAsString(sheet.getSheetName(), true);
        valSrc.getNumRef().setF(numDataRange);
        String axisDataRange = new CellRangeAddress(1, rownum-1, 0, 0).formatAsString(sheet.getSheetName(), true);
        cat.getStrRef().setF(axisDataRange);

        // updated the embedded workbook with the data
        OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
        wb.write(xlsOut);
        xlsOut.close();

    }

}