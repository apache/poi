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

package org.apache.poi.xddf.usermodel;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLogBase;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

/**
 * Base class for all axis types.
 */
@Beta
public abstract class XDDFChartAxis implements ChartAxis {
	protected abstract CTUnsignedInt getCTAxId();
	protected abstract CTAxPos getCTAxPos();
	protected abstract CTNumFmt getCTNumFmt();
	protected abstract CTScaling getCTScaling();
	protected abstract CTCrosses getCTCrosses();
	protected abstract CTBoolean getDelete();
	protected abstract CTTickMark getMajorCTTickMark();
	protected abstract CTTickMark getMinorCTTickMark();
	@Internal public abstract CTChartLines getMajorGridLines();
	@Internal public abstract CTShapeProperties getLine();

	@Override
	public long getId() {
		return getCTAxId().getVal();
	}

	@Override
	public AxisPosition getPosition() {
		return AxisPosition.valueOf(getCTAxPos().getVal());
	}
	@Override
	public void setPosition(AxisPosition position) {
		getCTAxPos().setVal(position.underlying);
	}

	@Override
	public void setNumberFormat(String format) {
		getCTNumFmt().setFormatCode(format);
		getCTNumFmt().setSourceLinked(true);
	}

	@Override
	public String getNumberFormat() {
		return getCTNumFmt().getFormatCode();
	}

	@Override
	public boolean isSetLogBase() {
		return getCTScaling().isSetLogBase();
	}

	private static final double MIN_LOG_BASE = 2.0;
	private static final double MAX_LOG_BASE = 1000.0;

	@Override
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

	@Override
	public double getLogBase() {
		CTLogBase logBase = getCTScaling().getLogBase();
		if (logBase != null) {
			return logBase.getVal();
		}
		return 0.0;
	}

	@Override
	public boolean isSetMinimum() {
		return getCTScaling().isSetMin();
	}

	@Override
	public void setMinimum(double min) {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMin()) {
			scaling.getMin().setVal(min);
		} else {
			scaling.addNewMin().setVal(min);
		}
	}

	@Override
	public double getMinimum() {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMin()) {
			return scaling.getMin().getVal();
		} else {
			return 0.0;
		}
	}

	@Override
	public boolean isSetMaximum() {
		return getCTScaling().isSetMax();
	}

	@Override
	public void setMaximum(double max) {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMax()) {
			scaling.getMax().setVal(max);
		} else {
			scaling.addNewMax().setVal(max);
		}
	}

	@Override
	public double getMaximum() {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetMax()) {
			return scaling.getMax().getVal();
		} else {
			return 0.0;
		}
	}

	@Override
	public AxisOrientation getOrientation() {
		return AxisOrientation.valueOf(getCTScaling().getOrientation().getVal());
	}

	@Override
	public void setOrientation(AxisOrientation orientation) {
		CTScaling scaling = getCTScaling();
		if (scaling.isSetOrientation()) {
			scaling.getOrientation().setVal(orientation.underlying);
		} else {
			scaling.addNewOrientation().setVal(orientation.underlying);
		}
	}

	@Override
	public AxisCrosses getCrosses() {
		return AxisCrosses.valueOf(getCTCrosses().getVal());
	}

	@Override
	public void setCrosses(AxisCrosses crosses) {
		getCTCrosses().setVal(crosses.underlying);
	}

	@Override
	public boolean isVisible() {
		return !getDelete().getVal();
	}

	@Override
	public void setVisible(boolean value) {
		getDelete().setVal(!value);
	}

	@Override
	public AxisTickMark getMajorTickMark() {
		return AxisTickMark.valueOf(getMajorCTTickMark().getVal());
	}

	@Override
	public void setMajorTickMark(AxisTickMark tickMark) {
		getMajorCTTickMark().setVal(tickMark.underlying);
	}

	@Override
	public AxisTickMark getMinorTickMark() {
		return AxisTickMark.valueOf(getMinorCTTickMark().getVal());
	}

	@Override
	public void setMinorTickMark(AxisTickMark tickMark) {
		getMinorCTTickMark().setVal(tickMark.underlying);
	}

	protected long getNextAxId(CTPlotArea plotArea) {
		long totalAxisCount =
			plotArea.sizeOfValAxArray()  +
			plotArea.sizeOfCatAxArray()  +
			plotArea.sizeOfDateAxArray() +
			plotArea.sizeOfSerAxArray();
		return totalAxisCount;
	}
}
