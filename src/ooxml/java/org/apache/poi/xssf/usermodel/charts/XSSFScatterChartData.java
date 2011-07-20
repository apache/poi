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

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.DataMarker;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ScatterChartSerie;
import org.apache.poi.ss.usermodel.charts.ChartDataFactory;
import org.apache.poi.ss.usermodel.charts.ChartAxis;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STScatterStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrossBetween;
import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;

import org.apache.poi.xssf.usermodel.XSSFChart;

/**
 * Represents DrawingML scatter chart.
 *
 * @author Roman Kashitsyn
 */
public class XSSFScatterChartData implements ScatterChartData {

    /**
     * List of all data series.
     */
    private List<Serie> series;

    public XSSFScatterChartData() {
	series = new ArrayList<Serie>();
    }

    /**
     * Package private ScatterChartSerie implementation.
     */
    static class Serie implements ScatterChartSerie {
	private int id;
	private int order;
	private boolean useCache;
	private DataMarker xMarker;
	private DataMarker yMarker;
	private XSSFNumberCache lastCaclulatedXCache;
	private XSSFNumberCache lastCalculatedYCache;

	protected Serie(int id, int order) {
	    super();
	    this.id = id;
	    this.order = order;
	    this.useCache = true;
	}

	public void setXValues(DataMarker marker) {
	    xMarker = marker;
	}

	public void setYValues(DataMarker marker) {
	    yMarker = marker;
	}

	/**
	 * @param useCache if true, cached results will be added on plot
	 */
	public void setUseCache(boolean useCache) {
	    this.useCache = useCache;
	}

	/**
	 * Returns last calculated number cache for X axis.
	 * @return last calculated number cache for X axis.
	 */
	XSSFNumberCache getLastCaculatedXCache() {
	    return lastCaclulatedXCache;
	}

	/**
	 * Returns last calculated number cache for Y axis.
	 * @return last calculated number cache for Y axis.
	 */
	XSSFNumberCache getLastCalculatedYCache() {
	    return lastCalculatedYCache;
	}

	protected void addToChart(CTScatterChart ctScatterChart) {
	    CTScatterSer scatterSer = ctScatterChart.addNewSer();
	    scatterSer.addNewIdx().setVal(this.id);
	    scatterSer.addNewOrder().setVal(this.order);

	    /* TODO: add some logic to automatically recognize cell
	     * types and choose appropriate data representation for
	     * X axis.
	     */
	    CTAxDataSource xVal = scatterSer.addNewXVal();
	    CTNumRef xNumRef = xVal.addNewNumRef();
	    xNumRef.setF(xMarker.formatAsString());

	    CTNumDataSource yVal = scatterSer.addNewYVal();
	    CTNumRef yNumRef = yVal.addNewNumRef();
	    yNumRef.setF(yMarker.formatAsString());

	    if (useCache) {
		/* We can not store cache since markers are not immutable */
		XSSFNumberCache.buildCache(xMarker, xNumRef);
		lastCalculatedYCache = XSSFNumberCache.buildCache(yMarker, yNumRef);
	    }
	}
    }

    public ScatterChartSerie addSerie(DataMarker xMarker, DataMarker yMarker) {
	int numOfSeries = series.size();
	Serie newSerie = new Serie(numOfSeries, numOfSeries);
	newSerie.setXValues(xMarker);
	newSerie.setYValues(yMarker);
	series.add(newSerie);
	return newSerie;
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
	if (!(chart instanceof XSSFChart)) {
	    throw new IllegalArgumentException("Chart must be instance of XSSFChart");
	}

	XSSFChart xssfChart = (XSSFChart) chart;
	CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	CTScatterChart scatterChart = plotArea.addNewScatterChart();
	addStyle(scatterChart);

	for (Serie s : series) {
	    s.addToChart(scatterChart);
	}

	for (ChartAxis ax : axis) {
	    scatterChart.addNewAxId().setVal(ax.getId());
	}
    }

    public List<? extends Serie> getSeries() {
	return series;
    }

    private void addStyle(CTScatterChart ctScatterChart) {
	CTScatterStyle scatterStyle = ctScatterChart.addNewScatterStyle();
	scatterStyle.setVal(STScatterStyle.LINE_MARKER);
    }
}
