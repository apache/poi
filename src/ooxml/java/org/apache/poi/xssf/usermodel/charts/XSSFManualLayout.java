/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.	See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.	 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   ==================================================================== */

package org.apache.poi.xssf.usermodel.charts;

import org.apache.poi.util.Internal;
import org.apache.poi.ss.usermodel.charts.ManualLayout;
import org.apache.poi.ss.usermodel.charts.LayoutMode;
import org.apache.poi.ss.usermodel.charts.LayoutTarget;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLayoutTarget;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLayout;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTManualLayout;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLayoutMode;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLayoutTarget;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLayoutMode;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLayoutTarget;

/**
 * Represents a SpreadsheetML manual layout.
 * @author Roman Kashitsyn
 */
public final class XSSFManualLayout implements ManualLayout {

	/**
	 * Underlaying CTManualLayout bean.
	 */
	private CTManualLayout layout;

	private static final LayoutMode defaultLayoutMode = LayoutMode.EDGE;
	private static final LayoutTarget defaultLayoutTarget = LayoutTarget.INNER;

	/**
	 * Create a new SpreadsheetML manual layout.
	 * @param layout a Spreadsheet ML layout that should be used as base.
	 */
	public XSSFManualLayout(CTLayout ctLayout) {
		initLayout(ctLayout);
	}

	/**
	 * Create a new SpreadsheetML manual layout for chart.
	 * @param chart a chart to create layout for.
	 */
	public XSSFManualLayout(XSSFChart chart) {
		CTPlotArea ctPlotArea = chart.getCTChart().getPlotArea();
		CTLayout ctLayout = ctPlotArea.isSetLayout() ?
			ctPlotArea.getLayout() : ctPlotArea.addNewLayout();

		initLayout(ctLayout);
	}

	/**
	 * Return the underlying CTManualLayout bean.
	 *
	 * @return the underlying CTManualLayout bean.
	 */
	@Internal public CTManualLayout getCTManualLayout(){
		return layout;
	}

	public void setWidthRatio(double ratio) {
		if (!layout.isSetW()) {
			layout.addNewW();
		}
		layout.getW().setVal(ratio);
	}

	public double getWidthRatio() {
		if (!layout.isSetW()) {
			return 0.0;
		}
		return layout.getW().getVal();
	}

	public void setHeightRatio(double ratio) {
		if (!layout.isSetH()) {
			layout.addNewH();
		}
		layout.getH().setVal(ratio);
	}

	public double getHeightRatio() {
		if (!layout.isSetH()) {
			return 0.0;
		}
		return layout.getH().getVal();
	}

	public LayoutTarget getTarget() {
		if (!layout.isSetLayoutTarget()) {
			return defaultLayoutTarget;
		}
		return toLayoutTarget(layout.getLayoutTarget());
	}

	public void setTarget(LayoutTarget target) {
		if (!layout.isSetLayoutTarget()) {
			layout.addNewLayoutTarget();
		}
		layout.getLayoutTarget().setVal(fromLayoutTarget(target));
	}

	public LayoutMode getXMode() {
		if (!layout.isSetXMode()) {
			return defaultLayoutMode;
		}
		return toLayoutMode(layout.getXMode());
	}

	public void setXMode(LayoutMode mode) {
		if (!layout.isSetXMode()) {
			layout.addNewXMode();
		}
		layout.getXMode().setVal(fromLayoutMode(mode));
	}

	public LayoutMode getYMode() {
		if (!layout.isSetYMode()) {
			return defaultLayoutMode;
		}
		return toLayoutMode(layout.getYMode());
	}

	public void setYMode(LayoutMode mode) {
		if (!layout.isSetYMode()) {
			layout.addNewYMode();
		}
		layout.getYMode().setVal(fromLayoutMode(mode));
	}

	public double getX() {
		if (!layout.isSetX()) {
			return 0.0;
		}
		return layout.getX().getVal();
	}

	public void setX(double x) {
		if (!layout.isSetX()) {
			layout.addNewX();
		}
		layout.getX().setVal(x);
	}

	public double getY() {
		if (!layout.isSetY()) {
			return 0.0;
		}
		return layout.getY().getVal();
	}

	public void setY(double y) {
		if (!layout.isSetY()) {
			layout.addNewY();
		}
		layout.getY().setVal(y);
	}

	public LayoutMode getWidthMode() {
		if (!layout.isSetWMode()) {
			return defaultLayoutMode;
		}
		return toLayoutMode(layout.getWMode());
	}

	public void setWidthMode(LayoutMode mode) {
		if (!layout.isSetWMode()) {
			layout.addNewWMode();
		}
		layout.getWMode().setVal(fromLayoutMode(mode));
	}

	public LayoutMode getHeightMode() {
		if (!layout.isSetHMode()) {
			return defaultLayoutMode;
		}
		return toLayoutMode(layout.getHMode());
	}

	public void setHeightMode(LayoutMode mode) {
		if (!layout.isSetHMode()) {
			layout.addNewHMode();
		}
		layout.getHMode().setVal(fromLayoutMode(mode));
	}

	private void initLayout(CTLayout ctLayout) {
		if (ctLayout.isSetManualLayout()) {
			this.layout = ctLayout.getManualLayout();
		} else {
			this.layout = ctLayout.addNewManualLayout();
		}
	}

	private STLayoutMode.Enum fromLayoutMode(LayoutMode mode) {
		switch (mode) {
		case EDGE: return STLayoutMode.EDGE;
		case FACTOR: return STLayoutMode.FACTOR;
		default:
			throw new IllegalArgumentException();
		}
	}

	private LayoutMode toLayoutMode(CTLayoutMode ctLayoutMode) {
		switch (ctLayoutMode.getVal().intValue()) {
		case STLayoutMode.INT_EDGE: return LayoutMode.EDGE;
		case STLayoutMode.INT_FACTOR: return LayoutMode.FACTOR;
		default:
			throw new IllegalArgumentException();
		}
	}

	private STLayoutTarget.Enum fromLayoutTarget(LayoutTarget target) {
		switch (target) {
		case INNER: return STLayoutTarget.INNER;
		case OUTER: return STLayoutTarget.OUTER;
		default:
			throw new IllegalArgumentException();
		}
	}

	private LayoutTarget toLayoutTarget(CTLayoutTarget ctLayoutTarget) {
		switch (ctLayoutTarget.getVal().intValue()) {
		case STLayoutTarget.INT_INNER: return LayoutTarget.INNER;
		case STLayoutTarget.INT_OUTER: return LayoutTarget.OUTER;
		default:
			throw new IllegalArgumentException();
		}
	}
}
