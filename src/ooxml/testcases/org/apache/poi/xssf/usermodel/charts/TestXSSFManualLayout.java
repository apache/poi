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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ManualLayout;
import org.apache.poi.ss.usermodel.charts.LayoutMode;
import org.apache.poi.ss.usermodel.charts.LayoutTarget;
import org.apache.poi.xssf.usermodel.*;

public final class TestXSSFManualLayout extends TestCase {
	
	/*
	 * Accessor methods are not trivial. They use lazy underlying bean
	 * initialization so there can be some errors (NPE, for example).
	 */
	public void testAccessorMethods() throws Exception {
		final double newRatio = 1.1;
		final double newCoordinate = 0.3;
		final LayoutMode nonDefaultMode = LayoutMode.FACTOR;
		final LayoutTarget nonDefaultTarget = LayoutTarget.OUTER;

		ManualLayout layout = getEmptyLayout();

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

	/*
	 * Layout must have reasonable default values and must not throw
	 * any exceptions.
	 */
	public void testDefaultValues() throws Exception {
		ManualLayout layout = getEmptyLayout();
		
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
	}

	private ManualLayout getEmptyLayout() {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();
		return legend.getManualLayout();
	}
}
