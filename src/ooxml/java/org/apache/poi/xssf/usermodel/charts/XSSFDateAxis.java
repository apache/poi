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

import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisOrientation;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.AxisTickMark;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.xddf.usermodel.chart.XDDFDateAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDateAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

/**
 * Date axis type.  Currently only implements the same values as {@link XSSFCategoryAxis}, since the two are nearly identical.
 *
 * @deprecated use {@link XDDFDateAxis} instead
 */
@Deprecated
@Removal(version="4.2")
public class XSSFDateAxis extends XSSFChartAxis {

	private CTDateAx ctDateAx;

	public XSSFDateAxis(XSSFChart chart, long id, AxisPosition pos) {
		super(chart);
		createAxis(id, pos);
	}

	public XSSFDateAxis(XSSFChart chart, CTDateAx ctDateAx) {
		super(chart);
		this.ctDateAx = ctDateAx;
	}

	@Override
	public long getId() {
		return ctDateAx.getAxId().getVal();
	}

	@Override
	@Internal
    public CTShapeProperties getLine() {
        return ctDateAx.getSpPr();
    }

	@Override
	protected CTAxPos getCTAxPos() {
		return ctDateAx.getAxPos();
	}

	@Override
	protected CTNumFmt getCTNumFmt() {
		if (ctDateAx.isSetNumFmt()) {
			return ctDateAx.getNumFmt();
		}
		return ctDateAx.addNewNumFmt();
	}

	@Override
	protected CTScaling getCTScaling() {
		return ctDateAx.getScaling();
	}

	@Override
	protected CTCrosses getCTCrosses() {
		return ctDateAx.getCrosses();
	}

	@Override
	protected CTBoolean getDelete() {
		return ctDateAx.getDelete();
	}

	@Override
	protected CTTickMark getMajorCTTickMark() {
		return ctDateAx.getMajorTickMark();
	}

	@Override
	protected CTTickMark getMinorCTTickMark() {
		return ctDateAx.getMinorTickMark();
	}

	@Override
	@Internal
	public CTChartLines getMajorGridLines() {
	    return ctDateAx.getMajorGridlines();
	}

	@Override
	public void crossAxis(ChartAxis axis) {
		ctDateAx.getCrossAx().setVal(axis.getId());
	}

	private void createAxis(long id, AxisPosition pos) {
		ctDateAx = chart.getCTChart().getPlotArea().addNewDateAx();
		ctDateAx.addNewAxId().setVal(id);
		ctDateAx.addNewAxPos();
		ctDateAx.addNewScaling();
		ctDateAx.addNewCrosses();
		ctDateAx.addNewCrossAx();
		ctDateAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
		ctDateAx.addNewDelete();
		ctDateAx.addNewMajorTickMark();
		ctDateAx.addNewMinorTickMark();

		setPosition(pos);
		setOrientation(AxisOrientation.MIN_MAX);
		setCrosses(AxisCrosses.AUTO_ZERO);
		setVisible(true);
		setMajorTickMark(AxisTickMark.CROSS);
		setMinorTickMark(AxisTickMark.NONE);
	}

	@Override
    public boolean hasNumberFormat() {
        return ctDateAx.isSetNumFmt();
    }
}
