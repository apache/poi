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

import java.util.Map;

import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;

@Beta
public class XDDFRadarChartSeries extends XDDFChartSeries {
	private CTRadarSer series;
	private CTRadarChart chart;

	public XDDFRadarChartSeries(XSSFSheet sheet, CTRadarChart chart, Map<Long, XDDFCategoryAxis> categories, Map<Long, XDDFValueAxis> values) {
		super(sheet);
		this.chart = chart;
		this.series = chart.getSerArray(0);
		defineAxes(chart.getAxIdArray(), categories, values);
	}

	@Override
	protected CTStrRef getSeriesTxStrRef() {
		return series.getTx().getStrRef();
	}

	@Override
	public void setShowLeaderLines(boolean showLeaderLines) {
		if (!series.isSetDLbls()) {
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

    public RadarStyle getStyle() {
        return RadarStyle.valueOf(chart.getRadarStyle().getVal());
    }

    public void setStyle(RadarStyle style) {
        CTRadarStyle radarStyle = chart.getRadarStyle();
        if (radarStyle == null) {
        	radarStyle = chart.addNewRadarStyle();
        }
        radarStyle.setVal(style.underlying);
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
