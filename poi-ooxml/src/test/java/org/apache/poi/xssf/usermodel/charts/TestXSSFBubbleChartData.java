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

package org.apache.poi.xssf.usermodel.charts;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.io.IOException;
import java.util.Calendar;

/**
 * Tests for XSSFBubbleChartData.
 */
final class TestXSSFBubbleChartData {

    @Test
    void testExample() throws IOException {
        Object[][] chartData = new Object[][]{
                new Object[]{"", "Category 1"},
                new Object[]{"Bubble Size", "Bubble Date"},
                new Object[]{1000, newCalendar(2020, 0, 1)},
                new Object[]{10, newCalendar(2020, 0, 1)},
                new Object[]{300, newCalendar(2021, 0, 1)},
                new Object[]{"", ""},
                new Object[]{"", "Category 2"},
                new Object[]{"Bubble Size", "Bubble Date"},
                new Object[]{100, newCalendar(2018, 0, 1)},
                new Object[]{100, newCalendar(2020, 0, 1)}
        };

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("bubblechart");
            DataFormat format = wb.createDataFormat();
            XSSFCellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(14);

            // put sheet data
            Row row;
            Cell cell;
            int rowIndex = 0;
            int colIndex = 0;
            for (Object[] dataRow : chartData) {
                row = sheet.createRow((short) rowIndex);
                colIndex = 0;
                for (Object value : dataRow) {
                    cell = row.createCell((short) colIndex);
                    if (value instanceof String) cell.setCellValue((String) value);
                    if (value instanceof Number) cell.setCellValue(((Number)value).doubleValue());
                    if (value instanceof Calendar) {
                        cell.setCellValue((Calendar) value);
                        cell.setCellStyle(dateStyle);
                    }
                    colIndex++;
                }
                rowIndex++;
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // create the chart

            // chart data sources
            XDDFDataSource<Double> xs1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, 4, 1, 1));
            XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, 4, 0, 0));
            XDDFNumericalDataSource<Double> bSz1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, 4, 0, 0));

            XDDFDataSource<Double> xs2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(8, 9, 1, 1));
            XDDFNumericalDataSource<Double> ys2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(8, 9, 0, 0));
            XDDFNumericalDataSource<Double> bSz2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(8, 9, 0, 0));

            // chart in drawing
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 0, 15, 20);
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Chart Title");
            chart.setTitleOverlay(false);
            chart.getFormattedTitle().getParagraph(0).addDefaultRunProperties().setFontSize(20d);

            // value axis x
            XDDFValueAxis valAxisX = chart.createValueAxis(AxisPosition.BOTTOM);
            valAxisX.setTitle("Axis Title");

            // value axis y
            XDDFValueAxis valAxisY = chart.createValueAxis(AxisPosition.LEFT);
            valAxisY.setTitle("Axis Title");

            // cross axes
            valAxisY.setCrosses(AxisCrosses.AUTO_ZERO);

            // chart data
            //XDDFChartData data = chart.createData(ChartTypes.???, valAxisX, valAxisY);
            XDDFBubbleChartData data = new XDDFBubbleChartData(chart, chart.getCTChart().getPlotArea().addNewBubbleChart(), valAxisX, valAxisY);

            // series
            XDDFBubbleChartData.Series series1 = (XDDFBubbleChartData.Series)data.addSeries(xs1, ys1);
            series1.setTitle("Category 1", new CellReference(sheet.getSheetName(), 0, 1, true, true));
            // set bubble sizes
            series1.setBubbleSizes(bSz1);
            // add data labels
            // pos 8 = INT_R , showVal = true, showLegendKey= false, showCatName = true
            CTDLbls ctDLbls = setDataLabels(series1, 8, true, false, true);

            XDDFBubbleChartData.Series series2 = (XDDFBubbleChartData.Series)data.addSeries(xs2, ys2);
            series2.setTitle("Category 2", new CellReference(sheet.getSheetName(), 6, 1, true, true));
            // set bubble sizes
            series2.setBubbleSizes(bSz2);
            // add data labels
            // pos 8 = INT_R , showVal = true, showLegendKey= false, showCatName = true
            ctDLbls = setDataLabels(series2, 8, true, false, true);

            // plot chart
            chart.plot(data);

            // legend
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.RIGHT);

            // set series fill color
            solidFillSeries(data, 0, PresetColor.BLUE);
            solidFillSeries(data, 1, PresetColor.RED);

            // set rounded corners false
            setRoundedCorners(chart, false);

            // Write the output to a file
            try (UnsynchronizedByteArrayOutputStream outStream = UnsynchronizedByteArrayOutputStream.builder().get()) {
                wb.write(outStream);
                try (XSSFWorkbook wb2 = new XSSFWorkbook(outStream.toInputStream())) {
                    // see if this fails
                }
            }
        }
    }

    private static Calendar newCalendar(int year, int month, int dayOfMonth) {
        return LocaleUtil.getLocaleCalendar(year, month, dayOfMonth);
    }

    private static void solidFillSeries(XDDFChartData data, int index, PresetColor color) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
        XDDFChartData.Series series = data.getSeries(index);
        XDDFShapeProperties properties = series.getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }
        properties.setFillProperties(fill);
        series.setShapeProperties(properties);
    }

    private static CTDLbls setDataLabels(XDDFChartData.Series series, int pos, boolean... show) {
        /*
        INT_BEST_FIT   1
        INT_B          2
        INT_CTR        3
        INT_IN_BASE    4
        INT_IN_END     5
        INT_L          6
        INT_OUT_END    7
        INT_R          8
        INT_T          9
        */
        CTDLbls ctDLbls = null;
        if (series instanceof XDDFBarChartData.Series) {
            CTBarSer ctBarSer = ((XDDFBarChartData.Series)series).getCTBarSer();
            if (ctBarSer.isSetDLbls()) ctBarSer.unsetDLbls();
            ctDLbls = ctBarSer.addNewDLbls();
            if (!(pos == 3 || pos == 4 || pos == 5 || pos == 7)) pos = 3; // bar chart does not provide other pos
            ctDLbls.addNewDLblPos().setVal(STDLblPos.Enum.forInt(pos));
        } else if (series instanceof XDDFLineChartData.Series) {
            CTLineSer ctLineSer = ((XDDFLineChartData.Series)series).getCTLineSer();
            if (ctLineSer.isSetDLbls()) ctLineSer.unsetDLbls();
            ctDLbls = ctLineSer.addNewDLbls();
            if (!(pos == 3 || pos == 6 || pos == 8 || pos == 9 || pos == 2)) pos = 3; // line chart does not provide other pos
            ctDLbls.addNewDLblPos().setVal(STDLblPos.Enum.forInt(pos));
        } else if (series instanceof XDDFPieChartData.Series) {
            CTPieSer ctPieSer = ((XDDFPieChartData.Series)series).getCTPieSer();
            if (ctPieSer.isSetDLbls()) ctPieSer.unsetDLbls();
            ctDLbls = ctPieSer.addNewDLbls();
            if (!(pos == 3 || pos == 1 || pos == 4 || pos == 5)) pos = 3; // pie chart does not provide other pos
            ctDLbls.addNewDLblPos().setVal(STDLblPos.Enum.forInt(pos));
        } else if (series instanceof XDDFBubbleChartData.Series) {
            CTBubbleSer ctBubbleSer = ((XDDFBubbleChartData.Series)series).getCTBubbleSer();
            if (ctBubbleSer.isSetDLbls()) ctBubbleSer.unsetDLbls();
            ctDLbls = ctBubbleSer.addNewDLbls();
            if (!(pos == 3 || pos == 2 || pos == 6 || pos == 8 || pos == 9)) pos = 3; // bubble chart does not provide other pos
            ctDLbls.addNewDLblPos().setVal(STDLblPos.Enum.forInt(pos));
        }// else if ...

        if (ctDLbls != null) {
            ctDLbls.addNewShowVal().setVal((show.length>0)?show[0]:false);
            ctDLbls.addNewShowLegendKey().setVal((show.length>1)?show[1]:false);
            ctDLbls.addNewShowCatName().setVal((show.length>2)?show[2]:false);
            ctDLbls.addNewShowSerName().setVal((show.length>3)?show[3]:false);
            ctDLbls.addNewShowPercent().setVal((show.length>4)?show[4]:false);
            ctDLbls.addNewShowBubbleSize().setVal((show.length>5)?show[5]:false);
            ctDLbls.addNewShowLeaderLines().setVal((show.length>6)?show[8]:false);

            return ctDLbls;
        }
        return null;
    }

    private static void setRoundedCorners(XDDFChart chart, boolean setVal) {
        if (chart.getCTChartSpace().getRoundedCorners() == null) chart.getCTChartSpace().addNewRoundedCorners();
        chart.getCTChartSpace().getRoundedCorners().setVal(setVal);
    }
}
