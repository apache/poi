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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;

@Beta
public class XSLFBarChartSeries extends XSLFChartSeries {
	private CTBarSer series;
	private CTBarChart chart;
	private XSLFCategoryAxis categoryAxis;
	private List<XSLFValueAxis> valueAxes;

	public XSLFBarChartSeries(XSSFSheet sheet, CTBarChart chart, Map<Long, XSLFCategoryAxis> categories, Map<Long, XSLFValueAxis> values) {
		super(sheet);
		this.chart = chart;
		this.series = chart.getSerArray(0);
		defineAxes(chart.getAxIdArray(), categories, values);
	}

	private void defineAxes(CTUnsignedInt[] axes, Map<Long, XSLFCategoryAxis> categories, Map<Long, XSLFValueAxis> values) {
		List<XSLFValueAxis> list = new ArrayList<XSLFValueAxis>(axes.length);
		for (int i = 0; i < axes.length; i++) {
			Long axisId = axes[i].getVal();
			XSLFCategoryAxis category = categories.get(axisId);
			if (category == null) {
				XSLFValueAxis axis = values.get(axisId);
				if (axis != null ) {
					list.add(axis);
				}
			} else {
				this.categoryAxis = category;
			}
		}
		this.valueAxes = Collections.unmodifiableList(list);
	}

	public XSLFCategoryAxis getCategoryAxis() {
		return categoryAxis;
	}

	public List<XSLFValueAxis> getValueAxes() {
		return valueAxes;
	}

	@Override
	public void setTitle(String title) {
		String titleRef = setSheetTitle(title);
		CTStrRef ref = series.getTx().getStrRef();
		ref.getStrCache().getPtArray(0).setV(title);
		ref.setF(titleRef);
	}

	@Override
	public void setShowLeaderLines(boolean showLeaderLines) {
		if(!series.isSetDLbls()) {
			series.addNewDLbls();
		}
		if (series.getDLbls().isSetShowLeaderLines()) {
            series.getDLbls().getShowLeaderLines().setVal(showLeaderLines);
		} else {
			series.getDLbls().addNewShowLeaderLines().setVal(showLeaderLines);
		}
	}

	@Override
	public void setVaryColors(boolean varyColors) {
		if (chart.isSetVaryColors()) {
			chart.getVaryColors().setVal(varyColors);
		} else {
			chart.addNewVaryColors().setVal(varyColors);
		}
	}

	public void setBarDirection(BarDirection direction) {
		chart.getBarDir().setVal(direction.underlying);
	}

	public void setBarGrouping(BarGrouping grouping) {
		if (chart.isSetGrouping()) {
			chart.getGrouping().setVal(grouping.underlying);
		} else {
			chart.addNewGrouping().setVal(grouping.underlying);
		}
	}

	public void setGapWidth(int width) {
		if (chart.isSetGapWidth()) {
			chart.getGapWidth().setVal(width);
		} else {
			chart.addNewGapWidth().setVal(width);
		}
	}

	@Override
	protected CTAxDataSource getAxDS() {
		return series.getCat();
	}

	@Override
	protected CTNumDataSource getNumDS() {
		return series.getVal();
	}
}
