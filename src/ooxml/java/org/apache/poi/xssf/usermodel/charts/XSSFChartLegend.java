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

import org.apache.poi.util.Internal;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegendPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos;

/**
 * Represents a SpreadsheetML chart legend
 * @author Roman Kashitsyn
 */
public final class XSSFChartLegend implements ChartLegend {

	/**
	 * Underlaying CTLagend bean
	 */
	private CTLegend legend;

	/**
	 * Create a new SpreadsheetML chart legend
	 */
	public XSSFChartLegend(XSSFChart chart) {
		CTChart ctChart = chart.getCTChart();
		this.legend = (ctChart.isSetLegend()) ?
			ctChart.getLegend() :
			ctChart.addNewLegend();
	}

	/**
	 * Return the underlying CTLegend bean.
	 *
	 * @return the underlying CTLegend bean
	 */
	@Internal
	public CTLegend getCTLegend(){
		return legend;
	}

	public void setPosition(LegendPosition position) {
		if (!legend.isSetLegendPos()) {
			legend.addNewLegendPos();
		}
		legend.getLegendPos().setVal(fromLegendPosition(position));
	}

	/*
	 * According to ECMA-376 default position is RIGHT.
	 */
	public LegendPosition getPosition() {
		if (legend.isSetLegendPos()) {
			return toLegendPosition(legend.getLegendPos());
		} else {
			return LegendPosition.RIGHT;
		}
	}

	private STLegendPos.Enum fromLegendPosition(LegendPosition position) {
		switch (position) {
			case BOTTOM: return STLegendPos.B;
			case LEFT: return STLegendPos.L;
			case RIGHT: return STLegendPos.R;
			case TOP: return STLegendPos.T;
			case TOP_RIGHT: return STLegendPos.TR;
			default:
				throw new IllegalArgumentException();
		}
	}

	private LegendPosition toLegendPosition(CTLegendPos ctLegendPos) {
		switch (ctLegendPos.getVal().intValue()) {
			case STLegendPos.INT_B: return LegendPosition.BOTTOM;
			case STLegendPos.INT_L: return LegendPosition.LEFT;
			case STLegendPos.INT_R: return LegendPosition.RIGHT;
			case STLegendPos.INT_T: return LegendPosition.TOP;
			case STLegendPos.INT_TR: return LegendPosition.TOP_RIGHT;
			default:
				throw new IllegalArgumentException();
		}
	}
}
