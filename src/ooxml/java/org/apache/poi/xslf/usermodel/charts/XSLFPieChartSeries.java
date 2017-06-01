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

import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;

@Beta
public class XSLFPieChartSeries extends XSLFChartSeries {
	private CTPieSer series;
	private CTPieChart chart;

	public XSLFPieChartSeries(XSSFSheet sheet, CTPieChart chart) {
		super(sheet);
		this.chart = chart;
		this.series = chart.getSerArray(0);
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

	public void setExplosion(long explosion) {
		if (series.isSetExplosion()) {
			series.getExplosion().setVal(explosion);
		} else {
			series.addNewExplosion().setVal(explosion);
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
