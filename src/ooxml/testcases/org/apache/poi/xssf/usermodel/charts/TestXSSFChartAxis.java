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

import java.util.List;

import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.AxisTickMark;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import junit.framework.TestCase;

public final class TestXSSFChartAxis extends TestCase {

	private static final double EPSILON = 1E-7;
	private final XDDFChartAxis axis;

	public TestXSSFChartAxis() {
		super();
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet();
		XSSFDrawing drawing = sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		XSSFChart chart = drawing.createChart(anchor);
		axis = chart.createValueAxis(AxisPosition.BOTTOM);
	}

	public void testLogBaseIllegalArgument() throws Exception {
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

	public void testLogBaseLegalArgument() throws Exception {
		axis.setLogBase(Math.E);
		assertTrue(Math.abs(axis.getLogBase() - Math.E) < EPSILON);
	}

	public void testNumberFormat() throws Exception {
		final String numberFormat = "General";
		axis.setNumberFormat(numberFormat);
		assertEquals(numberFormat, axis.getNumberFormat());
	}

	public void testMaxAndMinAccessMethods() {
		final double newValue = 10.0;

		axis.setMinimum(newValue);
		assertTrue(Math.abs(axis.getMinimum() - newValue) < EPSILON);

		axis.setMaximum(newValue);
		assertTrue(Math.abs(axis.getMaximum() - newValue) < EPSILON);
	}

	public void testVisibleAccessMethods() {
		axis.setVisible(true);
		assertTrue(axis.isVisible());

		axis.setVisible(false);
		assertFalse(axis.isVisible());
	}

	public void testMajorTickMarkAccessMethods() {
		axis.setMajorTickMark(AxisTickMark.NONE);
		assertEquals(AxisTickMark.NONE, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.IN);
		assertEquals(AxisTickMark.IN, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.OUT);
		assertEquals(AxisTickMark.OUT, axis.getMajorTickMark());

		axis.setMajorTickMark(AxisTickMark.CROSS);
		assertEquals(AxisTickMark.CROSS, axis.getMajorTickMark());
	}

	public void testMinorTickMarkAccessMethods() {
		axis.setMinorTickMark(AxisTickMark.NONE);
		assertEquals(AxisTickMark.NONE, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.IN);
		assertEquals(AxisTickMark.IN, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.OUT);
		assertEquals(AxisTickMark.OUT, axis.getMinorTickMark());

		axis.setMinorTickMark(AxisTickMark.CROSS);
		assertEquals(AxisTickMark.CROSS, axis.getMinorTickMark());
	}

	public void testGetChartAxisBug57362() {
	  //Load existing excel with some chart on it having primary and secondary axis.
	    final XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("57362.xlsx");
        final XSSFSheet sh = workbook.getSheetAt(0);
        final XSSFDrawing drawing = sh.createDrawingPatriarch();
        final XSSFChart chart = drawing.getCharts().get(0);

        final List<? extends XDDFChartAxis> axisList = chart.getAxes();

        assertEquals(4, axisList.size());
        assertNotNull(axisList.get(0));
        assertNotNull(axisList.get(1));
        assertNotNull(axisList.get(2));
        assertNotNull(axisList.get(3));
	}
}
