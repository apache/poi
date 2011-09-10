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
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.AxisOrientation;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTOrientation;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLogBase;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrosses;

/**
 * Base class for all axis types.
 *
 * @author Roman Kashitsyn
 */
@Beta
public abstract class XSSFChartAxis implements ChartAxis {

	protected XSSFChart chart;

	private static final double MIN_LOG_BASE = 2.0;
	private static final double MAX_LOG_BASE = 1000.0;

	protected XSSFChartAxis(XSSFChart chart) {
		this.chart = chart;
	}

	public AxisPosition getPosition() {
		return toAxisPosition(getCTAxPos());
	}

	public void setPosition(AxisPosition position) {
		getCTAxPos().setVal(fromAxisPosition(position));
	}

	public void setNumberFormat(String format) {
		getCTNumFmt().setFormatCode(format);
		getCTNumFmt().setSourceLinked(true);
	}

	public String getNumberFormat() {
		return getCTNumFmt().getFormatCode();
	}

	public boolean isSetLogBase() {
		return getCTScaling().isSetLogBase();
	}

	public void setLogBase(double logBase) {
		if (logBase < MIN_LOG_BASE ||
			MAX_LOG_BASE < logBase) {
			throw new IllegalArgumentException("Axis log base must be between 2 and 1000 (inclusive), got: " + logBase);
		}
		CTScaling scaling = getCTScaling();
		if (scaling.isSetLogBase()) {
			scaling.getLogBase().setVal(logBase);
		} else {
			scaling.addNewLogBase().setVal(logBase);
		}
	}

	public double getLogBase() {
		CTLogBase logBase = getCTScaling().getLogBase();
		if (logBase != null) {
			return logBase.getVal();
		}
		return 0.0;
	}

	public boolean isSetMinimum() {
		return getCTScaling().isSetMin();
	}

	public void setMinimum(double min) {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMin()) {
			scaling.getMin().setVal(min);
		} else {
			scaling.addNewMin().setVal(min);
		}
	}

	public double getMinimum() {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMin()) {
			return scaling.getMin().getVal();
		} else {
			return 0.0;
		}
	}

	public boolean isSetMaximum() {
		return getCTScaling().isSetMax();
	}

	public void setMaximum(double max) {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMax()) {
			scaling.getMax().setVal(max);
		} else {
			scaling.addNewMax().setVal(max);
		}
	}

	public double getMaximum() {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMax()) {
			return scaling.getMax().getVal();
		} else {
			return 0.0;
		}
	}

	public AxisOrientation getOrientation() {
		return toAxisOrientation(getCTScaling().getOrientation());
	}

	public void setOrientation(AxisOrientation orientation) {
		CTScaling scaling = getCTScaling();
		STOrientation.Enum stOrientation = fromAxisOrientation(orientation);
		if (scaling.isSetOrientation()) {
			scaling.getOrientation().setVal(stOrientation);
		} else {
			getCTScaling().addNewOrientation().setVal(stOrientation);
		}
	}

	public AxisCrosses getCrosses() {
		return toAxisCrosses(getCTCrosses());
	}

	public void setCrosses(AxisCrosses crosses) {
		getCTCrosses().setVal(fromAxisCrosses(crosses));
	}

	protected abstract CTAxPos getCTAxPos();
	protected abstract CTNumFmt getCTNumFmt();
	protected abstract CTScaling getCTScaling();
	protected abstract CTCrosses getCTCrosses();

	private static STOrientation.Enum fromAxisOrientation(AxisOrientation orientation) {
		switch (orientation) {
			case MIN_MAX: return STOrientation.MIN_MAX;
			case MAX_MIN: return STOrientation.MAX_MIN;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AxisOrientation toAxisOrientation(CTOrientation ctOrientation) {
		switch (ctOrientation.getVal().intValue()) {
			case STOrientation.INT_MIN_MAX: return AxisOrientation.MIN_MAX;
			case STOrientation.INT_MAX_MIN: return AxisOrientation.MAX_MIN;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static STCrosses.Enum fromAxisCrosses(AxisCrosses crosses) {
		switch (crosses) {
			case AUTO_ZERO: return STCrosses.AUTO_ZERO;
			case MIN: return STCrosses.MIN;
			case MAX: return STCrosses.MAX;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AxisCrosses toAxisCrosses(CTCrosses ctCrosses) {
		switch (ctCrosses.getVal().intValue()) {
			case STCrosses.INT_AUTO_ZERO: return AxisCrosses.AUTO_ZERO;
			case STCrosses.INT_MAX: return AxisCrosses.MAX;
			case STCrosses.INT_MIN: return AxisCrosses.MIN;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static STAxPos.Enum fromAxisPosition(AxisPosition position) {
		switch (position) {
			case BOTTOM: return STAxPos.B;
			case LEFT: return STAxPos.L;
			case RIGHT: return STAxPos.R;
			case TOP: return STAxPos.T;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AxisPosition toAxisPosition(CTAxPos ctAxPos) {
		switch (ctAxPos.getVal().intValue()) {
			case STAxPos.INT_B: return AxisPosition.BOTTOM;
			case STAxPos.INT_L: return AxisPosition.LEFT;
			case STAxPos.INT_R: return AxisPosition.RIGHT;
			case STAxPos.INT_T: return AxisPosition.TOP;
			default: return AxisPosition.BOTTOM;
		}
	}
}
