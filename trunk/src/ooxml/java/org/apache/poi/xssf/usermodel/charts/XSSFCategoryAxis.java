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

import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

/**
 * Category axis type.
 *
 * @author Martin Andersson
 */
@Beta
public class XSSFCategoryAxis extends XSSFChartAxis {

	private CTCatAx ctCatAx;

	public XSSFCategoryAxis(XSSFChart chart, long id, AxisPosition pos) {
		super(chart);
		createAxis(id, pos);
	}

	public XSSFCategoryAxis(XSSFChart chart, CTCatAx ctCatAx) {
		super(chart);
		this.ctCatAx = ctCatAx;
	}

	public long getId() {
		return ctCatAx.getAxId().getVal();
	}

	protected CTAxPos getCTAxPos() {
		return ctCatAx.getAxPos();
	}

	protected CTNumFmt getCTNumFmt() {
		if (ctCatAx.isSetNumFmt()) {
			return ctCatAx.getNumFmt();
		}
		return ctCatAx.addNewNumFmt();
	}

	protected CTScaling getCTScaling() {
		return ctCatAx.getScaling();
	}

	protected CTCrosses getCTCrosses() {
		return ctCatAx.getCrosses();
	}

	@Override
	protected CTBoolean getDelete() {
		return ctCatAx.getDelete();
	}

	@Override
	protected CTTickMark getMajorCTTickMark() {
		return ctCatAx.getMajorTickMark();
	}

	@Override
	protected CTTickMark getMinorCTTickMark() {
		return ctCatAx.getMinorTickMark();
	}

	public void crossAxis(ChartAxis axis) {
		ctCatAx.getCrossAx().setVal(axis.getId());
	}

	private void createAxis(long id, AxisPosition pos) {
		ctCatAx = chart.getCTChart().getPlotArea().addNewCatAx();
		ctCatAx.addNewAxId().setVal(id);
		ctCatAx.addNewAxPos();
		ctCatAx.addNewScaling();
		ctCatAx.addNewCrosses();
		ctCatAx.addNewCrossAx();
		ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
		ctCatAx.addNewDelete();
		ctCatAx.addNewMajorTickMark();
		ctCatAx.addNewMinorTickMark();

		setPosition(pos);
		setOrientation(AxisOrientation.MIN_MAX);
		setCrosses(AxisCrosses.AUTO_ZERO);
		setVisible(true);
		setMajorTickMark(AxisTickMark.CROSS);
		setMinorTickMark(AxisTickMark.NONE);
	}
}
