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

import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.AxisOrientation;
import org.apache.poi.ss.usermodel.charts.AxisCrossBetween;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;

import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrossBetween;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;

/**
 * Value axis type.
 *
 * @author Roman Kashitsyn
 */
public class XSSFValueAxis extends XSSFChartAxis implements ValueAxis {

	private CTValAx ctValAx;

	public XSSFValueAxis(XSSFChart chart, long id, AxisPosition pos) {
		super(chart);
		createAxis(id, pos);
	}

	public long getId() {
		return ctValAx.getAxId().getVal();
	}

	public void setCrossBetween(AxisCrossBetween crossBetween) {
		ctValAx.getCrossBetween().setVal(fromCrossBetween(crossBetween));
	}

	public AxisCrossBetween getCrossBetween() {
		return toCrossBetween(ctValAx.getCrossBetween().getVal());
	}

	@Override
	protected CTAxPos getCTAxPos() {
		return ctValAx.getAxPos();
	}

	@Override
	protected CTNumFmt getCTNumFmt() {
		if (ctValAx.isSetNumFmt()) {
			return ctValAx.getNumFmt();
		}
		return ctValAx.addNewNumFmt();
	}

	@Override
	protected CTScaling getCTScaling() {
		return ctValAx.getScaling();
	}

	@Override
	protected CTCrosses getCTCrosses() {
		return ctValAx.getCrosses();
	}

	public void crossAxis(ChartAxis axis) {
		ctValAx.getCrossAx().setVal(axis.getId());
	}

	private void createAxis(long id, AxisPosition pos) {
		ctValAx = chart.getCTChart().getPlotArea().addNewValAx();
		ctValAx.addNewAxId().setVal(id);
		ctValAx.addNewAxPos();
		ctValAx.addNewScaling();
		ctValAx.addNewCrossBetween();
		ctValAx.addNewCrosses();
		ctValAx.addNewCrossAx();
		ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);

		setPosition(pos);
		setOrientation(AxisOrientation.MIN_MAX);
		setCrossBetween(AxisCrossBetween.MIDPOINT_CATEGORY);
		setCrosses(AxisCrosses.AUTO_ZERO);
	}

	private static STCrossBetween.Enum fromCrossBetween(AxisCrossBetween crossBetween) {
		switch (crossBetween) {
			case BETWEEN: return STCrossBetween.BETWEEN;
			case MIDPOINT_CATEGORY: return STCrossBetween.MID_CAT;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AxisCrossBetween toCrossBetween(STCrossBetween.Enum ctCrossBetween) {
		switch (ctCrossBetween.intValue()) {
			case STCrossBetween.INT_BETWEEN: return AxisCrossBetween.BETWEEN;
			case STCrossBetween.INT_MID_CAT: return AxisCrossBetween.MIDPOINT_CATEGORY;
			default:
				throw new IllegalArgumentException();
		}
	}
}
