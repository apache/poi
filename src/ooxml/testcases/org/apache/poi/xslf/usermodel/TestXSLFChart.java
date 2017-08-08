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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.AxisCrosses;
import org.apache.poi.xddf.usermodel.AxisOrientation;
import org.apache.poi.xddf.usermodel.AxisPosition;
import org.apache.poi.xddf.usermodel.AxisTickMark;
import org.apache.poi.xddf.usermodel.BarDirection;
import org.apache.poi.xddf.usermodel.BarGrouping;
import org.apache.poi.xddf.usermodel.Grouping;
import org.apache.poi.xddf.usermodel.LayoutMode;
import org.apache.poi.xddf.usermodel.LayoutTarget;
import org.apache.poi.xddf.usermodel.LegendPosition;
import org.apache.poi.xddf.usermodel.RadarStyle;
import org.apache.poi.xddf.usermodel.ScatterStyle;
import org.apache.poi.xddf.usermodel.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.XDDFChartData;
import org.apache.poi.xddf.usermodel.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.XDDFDataSource;
import org.apache.poi.xddf.usermodel.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.XDDFManualLayout;
import org.apache.poi.xddf.usermodel.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.XDDFPieChartData;
import org.apache.poi.xddf.usermodel.XDDFRadarChartData;
import org.apache.poi.xddf.usermodel.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.XDDFValueAxis;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

/**
 * a modified version from POI-examples
 */
public class TestXSLFChart {
    @Test
    public void testFillPieChartTemplate() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("pie-chart.pptx");
        XSLFChart chart = findChart(pptx);
        List<XDDFChartData> data = findChartData(chart);

        XDDFPieChartData pie = (XDDFPieChartData) data.get(0);
        XDDFPieChartData.Series firstSeries = (XDDFPieChartData.Series) pie.getSeries().get(0);
        firstSeries.setExplosion(25);
        assertEquals(25, firstSeries.getExplosion());

        fillChartData(chart, pie);
        pptx.close();
    }

    @Test
    public void testFillBarChartTemplate() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("bar-chart.pptx");
        XSLFChart chart = findChart(pptx);
        List<XDDFChartData> data = findChartData(chart);

        XDDFBarChartData bar = (XDDFBarChartData) data.get(0);
        assertEquals(BarDirection.BAR, bar.getBarDirection());
        assertEquals(BarGrouping.CLUSTERED, bar.getBarGrouping());
        assertEquals(100, bar.getGapWidth());

        bar.setBarDirection(BarDirection.COL);
        assertEquals(BarDirection.COL, bar.getBarDirection());

        // additionally, you can adjust the axes
        bar.getCategoryAxis().setOrientation(AxisOrientation.MIN_MAX);
        bar.getValueAxes().get(0).setPosition(AxisPosition.BOTTOM);

        fillChartData(chart, bar);
        pptx.close();
    }

    @Test
    public void testFillLineChartTemplate() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("line-chart.pptx");
        XSLFChart chart = findChart(pptx);
        List<XDDFChartData> data = findChartData(chart);

        XDDFLineChartData line = (XDDFLineChartData) data.get(0);
        assertEquals(Grouping.STANDARD, line.getGrouping());
        line.setGrouping(Grouping.PERCENT_STACKED);
        assertEquals(Grouping.PERCENT_STACKED, line.getGrouping());

        fillChartData(chart, line);
        pptx.close();
    }

    @Test
    public void testFillRadarChartTemplate() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("radar-chart.pptx");
        XSLFChart chart = findChart(pptx);
        List<XDDFChartData> data = findChartData(chart);

        XDDFRadarChartData radar = (XDDFRadarChartData) data.get(0);
        assertEquals(RadarStyle.MARKER, radar.getStyle());
        radar.setStyle(RadarStyle.FILLED);
        assertEquals(RadarStyle.FILLED, radar.getStyle());

        fillChartData(chart, radar);
        pptx.close();
    }

    @Test
    public void testFillScatterChartTemplate() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("scatter-chart.pptx");
        XSLFChart chart = findChart(pptx);
        List<XDDFChartData> data = findChartData(chart);

        XDDFScatterChartData scatter = (XDDFScatterChartData) data.get(0);
        assertEquals(ScatterStyle.LINE_MARKER, scatter.getStyle());
        scatter.setStyle(ScatterStyle.SMOOTH);
        assertEquals(ScatterStyle.SMOOTH, scatter.getStyle());

        fillChartData(chart, scatter);
        pptx.close();
    }

	private void fillChartData(XSLFChart chart, XDDFChartData data) {
	    final int numOfPoints = 3;
        final String[] categories = {"First", "Second", "Third"};
        final Integer[] values = {1, 3, 4};

        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 0, 0));
        final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 1, 1));

		final XDDFChartData.Series series = data.getSeries().get(0);
		final XDDFDataSource<?> categoryData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange);
        final XDDFNumericalDataSource<Integer> valuesData = XDDFDataSourcesFactory.fromArray(values, valuesDataRange);
        series.replaceData(categoryData, valuesData);
        final String title = "Apache POI";
        series.setTitle(title, chart.setSheetTitle(title));
        chart.plot(data);
	}

    private XSLFChart findChart(XMLSlideShow pptx) {
        XSLFSlide slide = pptx.getSlides().get(0);

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

        checkLegendOperations(chart);
        return chart;
    }

    private List<XDDFChartData> findChartData(XSLFChart chart) {
        List<XDDFChartData> data = chart.getChartSeries();
        assertNotNull(data);
        assertEquals(1, data.size());

        XDDFChartData firstSeries = data.get(0);
		assertNotNull(firstSeries);
		if (firstSeries instanceof XDDFScatterChartData) {
            assertEquals(null, firstSeries.getCategoryAxis());
            assertEquals(2, firstSeries.getValueAxes().size());
            checkAxisOperations(firstSeries.getValueAxes().get(0));
            checkAxisOperations(firstSeries.getValueAxes().get(1));
		} else if (!(firstSeries instanceof XDDFPieChartData)) {
			assertNotNull(firstSeries.getCategoryAxis());
			assertEquals(1, firstSeries.getValueAxes().size());
			checkAxisOperations(firstSeries.getValueAxes().get(0));
		}

        return data;
    }

	private void checkLegendOperations(XSLFChart chart) {
		XDDFChartLegend legend = chart.getOrCreateLegend();
        assertFalse(legend.isOverlay());
		legend.setOverlay(true);
        assertTrue(legend.isOverlay());
		legend.setPosition(LegendPosition.TOP_RIGHT);
		assertEquals(LegendPosition.TOP_RIGHT, legend.getPosition());

		XDDFManualLayout layout = legend.getManualLayout();
		assertNotNull(layout.getTarget());
		assertNotNull(layout.getXMode());
		assertNotNull(layout.getYMode());
		assertNotNull(layout.getHeightMode());
		assertNotNull(layout.getWidthMode());
		/*
		 * According to interface, 0.0 should be returned for
		 * uninitialized double properties.
		 */
		assertTrue(layout.getX() == 0.0);
		assertTrue(layout.getY() == 0.0);
		assertTrue(layout.getWidthRatio() == 0.0);
		assertTrue(layout.getHeightRatio() == 0.0);

		final double newRatio = 1.1;
		final double newCoordinate = 0.3;
		final LayoutMode nonDefaultMode = LayoutMode.FACTOR;
		final LayoutTarget nonDefaultTarget = LayoutTarget.OUTER;

		layout.setWidthRatio(newRatio);
		assertTrue(layout.getWidthRatio() == newRatio);

		layout.setHeightRatio(newRatio);
		assertTrue(layout.getHeightRatio() == newRatio);

		layout.setX(newCoordinate);
		assertTrue(layout.getX() == newCoordinate);

		layout.setY(newCoordinate);
		assertTrue(layout.getY() == newCoordinate);

		layout.setXMode(nonDefaultMode);
		assertTrue(layout.getXMode() == nonDefaultMode);

		layout.setYMode(nonDefaultMode);
		assertTrue(layout.getYMode() == nonDefaultMode);

		layout.setWidthMode(nonDefaultMode);
		assertTrue(layout.getWidthMode() == nonDefaultMode);

		layout.setHeightMode(nonDefaultMode);
		assertTrue(layout.getHeightMode() == nonDefaultMode);

		layout.setTarget(nonDefaultTarget);
		assertTrue(layout.getTarget() == nonDefaultTarget);
	}

	private void checkAxisOperations(XDDFValueAxis axis) {
		axis.setCrossBetween(AxisCrossBetween.MIDPOINT_CATEGORY);
		assertEquals(AxisCrossBetween.MIDPOINT_CATEGORY, axis.getCrossBetween());

		axis.setCrosses(AxisCrosses.AUTO_ZERO);
		assertEquals(AxisCrosses.AUTO_ZERO, axis.getCrosses());

		final String numberFormat = "General";
		axis.setNumberFormat(numberFormat);
		assertEquals(numberFormat, axis.getNumberFormat());

		axis.setPosition(AxisPosition.BOTTOM);
		assertEquals(AxisPosition.BOTTOM, axis.getPosition());

		axis.setMajorTickMark(AxisTickMark.NONE);
		assertEquals(AxisTickMark.NONE, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.IN);
		assertEquals(AxisTickMark.IN, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.OUT);
		assertEquals(AxisTickMark.OUT, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.CROSS);
		assertEquals(AxisTickMark.CROSS, axis.getMajorTickMark());

		axis.setMinorTickMark(AxisTickMark.NONE);
		assertEquals(AxisTickMark.NONE, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.IN);
		assertEquals(AxisTickMark.IN, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.OUT);
		assertEquals(AxisTickMark.OUT, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.CROSS);
		assertEquals(AxisTickMark.CROSS, axis.getMinorTickMark());

		axis.setVisible(true);
		assertTrue(axis.isVisible());

		axis.setVisible(false);
		assertFalse(axis.isVisible());

		final double EPSILON = 1E-7;
		axis.setLogBase(Math.E);
		assertTrue(Math.abs(axis.getLogBase() - Math.E) < EPSILON);

		final double newValue = 10.0;

		axis.setMinimum(newValue);
		assertTrue(Math.abs(axis.getMinimum() - newValue) < EPSILON);

		axis.setMaximum(newValue);
		assertTrue(Math.abs(axis.getMaximum() - newValue) < EPSILON);

		IllegalArgumentException iae = null;
		try {
			axis.setLogBase(0.0);
		} catch (IllegalArgumentException e) {
			iae = e;
		}
		assertNotNull(iae);

		iae = null;
		try {
			axis.setLogBase(30000.0);
		} catch (IllegalArgumentException e) {
			iae = e;
		}
		assertNotNull(iae);
	}
}
