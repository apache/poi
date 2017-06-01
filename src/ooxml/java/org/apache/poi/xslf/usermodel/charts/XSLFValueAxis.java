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

package org.apache.poi.xslf.usermodel.charts;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;

public class XSLFValueAxis extends XSLFChartAxis {

	private CTValAx ctValAx;

	public XSLFValueAxis(CTPlotArea plotArea, AxisPosition position) {
		initializeAxis(plotArea, position);
	}

	public XSLFValueAxis(CTValAx ctValAx) {
		this.ctValAx = ctValAx;
	}

	public void crossAxis(XSLFChartAxis axis) {
		ctValAx.getCrossAx().setVal(axis.getId());
	}

	@Override
	protected CTUnsignedInt getCTAxId() {
		return ctValAx.getAxId();
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

	@Override
	protected CTBoolean getDelete() {
		return ctValAx.getDelete();
	}

	@Override
	protected CTTickMark getMajorCTTickMark() {
		return ctValAx.getMajorTickMark();
	}

	@Override
	protected CTTickMark getMinorCTTickMark() {
		return ctValAx.getMinorTickMark();
	}

	public AxisCrossBetween getCrossBetween() {
		return AxisCrossBetween.valueOf(ctValAx.getCrossBetween().getVal());
	}
	public void setCrossBetween(AxisCrossBetween crossBetween) {
		ctValAx.getCrossBetween().setVal(crossBetween.underlying);
	}

	private void initializeAxis(CTPlotArea plotArea, AxisPosition position) {
		final long id = getNextAxId(plotArea);
		ctValAx = plotArea.addNewValAx();
		ctValAx.addNewAxId().setVal(id);
		ctValAx.addNewAxPos();
		ctValAx.addNewScaling();
		ctValAx.addNewCrossBetween();
		ctValAx.addNewCrosses();
		ctValAx.addNewCrossAx();
		ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
		ctValAx.addNewDelete();
		ctValAx.addNewMajorTickMark();
		ctValAx.addNewMinorTickMark();

		setPosition(position);
		setOrientation(AxisOrientation.MIN_MAX);
		setCrossBetween(AxisCrossBetween.MIDPOINT_CATEGORY);
		setCrosses(AxisCrosses.AUTO_ZERO);
		setVisible(true);
		setMajorTickMark(AxisTickMark.CROSS);
		setMinorTickMark(AxisTickMark.NONE);
	}
}
