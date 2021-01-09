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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.poi.xddf.usermodel.chart.LayoutMode;
import org.apache.poi.xddf.usermodel.chart.LayoutTarget;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFManualLayout;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestXDDFManualLayout {

    private XSSFWorkbook wb;
    private XDDFManualLayout layout;

    @BeforeEach
    void createEmptyLayout() {
        wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        layout = legend.getOrAddManualLayout();
    }

    @AfterEach
    void closeWB() throws IOException {
        wb.close();
    }

	/*
	 * Accessor methods are not trivial. They use lazy underlying bean
	 * initialization so there can be some errors (NPE, for example).
	 */
    @Test
    void testAccessorMethods() {
		final double newRatio = 1.1;
		final double newCoordinate = 0.3;
		final LayoutMode nonDefaultMode = LayoutMode.FACTOR;
		final LayoutTarget nonDefaultTarget = LayoutTarget.OUTER;

		layout.setWidthRatio(newRatio);
        assertEquals(layout.getWidthRatio(), newRatio, 0.0);

		layout.setHeightRatio(newRatio);
        assertEquals(layout.getHeightRatio(), newRatio, 0.0);

		layout.setX(newCoordinate);
        assertEquals(layout.getX(), newCoordinate, 0.0);

		layout.setY(newCoordinate);
        assertEquals(layout.getY(), newCoordinate, 0.0);

		layout.setXMode(nonDefaultMode);
        assertSame(layout.getXMode(), nonDefaultMode);

		layout.setYMode(nonDefaultMode);
        assertSame(layout.getYMode(), nonDefaultMode);

		layout.setWidthMode(nonDefaultMode);
        assertSame(layout.getWidthMode(), nonDefaultMode);

		layout.setHeightMode(nonDefaultMode);
        assertSame(layout.getHeightMode(), nonDefaultMode);

		layout.setTarget(nonDefaultTarget);
        assertSame(layout.getTarget(), nonDefaultTarget);

	}

	/*
	 * Layout must have reasonable default values and must not throw
	 * any exceptions.
	 */
    @Test
    void testDefaultValues() {
		assertNotNull(layout.getTarget());
		assertNotNull(layout.getXMode());
		assertNotNull(layout.getYMode());
		assertNotNull(layout.getHeightMode());
		assertNotNull(layout.getWidthMode());
		/*
		 * According to interface, 0.0 should be returned for
		 * uninitialized double properties.
		 */
        assertEquals(0.0, layout.getX(), 0.0);
        assertEquals(0.0, layout.getY(), 0.0);
        assertEquals(0.0, layout.getWidthRatio(), 0.0);
        assertEquals(0.0, layout.getHeightRatio(), 0.0);
	}
}
