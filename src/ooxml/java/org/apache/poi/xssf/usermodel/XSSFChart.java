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

package org.apache.poi.xssf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartAxisFactory;
import org.apache.poi.ss.usermodel.charts.ChartData;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.xddf.usermodel.AxisPosition;
import org.apache.poi.xddf.usermodel.ChartTypes;
import org.apache.poi.xddf.usermodel.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.XDDFChartAxis;
import org.apache.poi.xddf.usermodel.XDDFChartData;
import org.apache.poi.xddf.usermodel.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.XDDFDateAxis;
import org.apache.poi.xddf.usermodel.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.XDDFManualLayout;
import org.apache.poi.xddf.usermodel.XDDFPieChartData;
import org.apache.poi.xddf.usermodel.XDDFRadarChartData;
import org.apache.poi.xddf.usermodel.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFCategoryAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFChartAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFChartDataFactory;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.apache.poi.xssf.usermodel.charts.XSSFDateAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFManualLayout;
import org.apache.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDateAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPageMargins;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPrintSettings;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextField;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents a SpreadsheetML Chart
 */
public final class XSSFChart extends POIXMLDocumentPart implements Chart, ChartAxisFactory {

	/**
	 * Parent graphic frame.
	 */
	private XSSFGraphicFrame frame;

	/**
	 * Root element of the SpreadsheetML Chart part
	 */
	private CTChartSpace chartSpace;
	/**
	 * The Chart within that
	 */
	private CTChart chart;

	@Deprecated
	List<XSSFChartAxis> axis = new ArrayList<XSSFChartAxis>();

	List<XDDFChartAxis> axes = new ArrayList<XDDFChartAxis>();

	/**
	 * Create a new SpreadsheetML chart
	 */
	protected XSSFChart() {
		super();
		createChart();
	}

	/**
	 * Construct a SpreadsheetML chart from a package part.
	 *
	 * @param part
	 *			the package part holding the chart data, the content type must be
	 *			<code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
	 *
	 * @since POI 3.14-Beta1
	 */
	protected XSSFChart(PackagePart part) throws IOException, XmlException {
		super(part);

		chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS).getChartSpace();
		chart = chartSpace.getChart();
	}

	/**
	 * Construct a new CTChartSpace bean. By default, it's just an empty placeholder for chart objects.
	 */
	private void createChart() {
		chartSpace = CTChartSpace.Factory.newInstance();
		chart = chartSpace.addNewChart();
		CTPlotArea plotArea = chart.addNewPlotArea();

		plotArea.addNewLayout();
		chart.addNewPlotVisOnly().setVal(true);

		CTPrintSettings printSettings = chartSpace.addNewPrintSettings();
		printSettings.addNewHeaderFooter();

		CTPageMargins pageMargins = printSettings.addNewPageMargins();
		pageMargins.setB(0.75);
		pageMargins.setL(0.70);
		pageMargins.setR(0.70);
		pageMargins.setT(0.75);
		pageMargins.setHeader(0.30);
		pageMargins.setFooter(0.30);
		printSettings.addNewPageSetup();
	}

	/**
	 * Return the underlying CTChartSpace bean, the root element of the SpreadsheetML Chart part.
	 *
	 * @return the underlying CTChartSpace bean
	 */
	@Internal
	public CTChartSpace getCTChartSpace() {
		return chartSpace;
	}

	/**
	 * Return the underlying CTChart bean, within the Chart Space
	 *
	 * @return the underlying CTChart bean
	 */
	@Internal
	public CTChart getCTChart() {
		return chart;
	}

	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

		/*
		 * Saved chart space must have the following namespaces set: <c:chartSpace
		 * xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
		 * xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r=
		 * "http://schemas.openxmlformats.org/officeDocument/2006/relationships">
		 */
		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		chartSpace.save(out, xmlOptions);
		out.close();
	}

	/**
	 * Returns the parent graphic frame.
	 *
	 * @return the graphic frame this chart belongs to
	 */
	public XSSFGraphicFrame getGraphicFrame() {
		return frame;
	}

	/**
	 * Sets the parent graphic frame.
	 */
	protected void setGraphicFrame(XSSFGraphicFrame frame) {
		this.frame = frame;
	}

	@Deprecated
	public XSSFChartDataFactory getChartDataFactory() {
		return XSSFChartDataFactory.getInstance();
	}

	@Deprecated
	public XSSFChart getChartAxisFactory() {
		return this;
	}

	@Deprecated
	public void plot(ChartData data, ChartAxis... chartAxis) {
		data.fillChart(this, chartAxis);
	}

	@Deprecated
	public XSSFValueAxis createValueAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFValueAxis valueAxis = new XSSFValueAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(valueAxis);
			valueAxis.crossAxis(ax);
		}
		axis.add(valueAxis);
		return valueAxis;
	}

	@Deprecated
	public XSSFCategoryAxis createCategoryAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFCategoryAxis categoryAxis = new XSSFCategoryAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(categoryAxis);
			categoryAxis.crossAxis(ax);
		}
		axis.add(categoryAxis);
		return categoryAxis;
	}

	@Deprecated
	public XSSFDateAxis createDateAxis(org.apache.poi.ss.usermodel.charts.AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFDateAxis dateAxis = new XSSFDateAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(dateAxis);
			dateAxis.crossAxis(ax);
		}
		axis.add(dateAxis);
		return dateAxis;
	}

    @Deprecated
    public List<? extends XSSFChartAxis> getAxis() {
        if (axis.isEmpty() && hasAxis()) {
            parseAxis();
        }
        return axis;
    }

    public XDDFValueAxis createValueAxis(AxisPosition pos) {
        XDDFValueAxis valueAxis = new XDDFValueAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(valueAxis);
            valueAxis.crossAxis(axis);
        }
        axes.add(valueAxis);
        return valueAxis;
    }

    public XDDFCategoryAxis createCategoryAxis(AxisPosition pos) {
        XDDFCategoryAxis categoryAxis = new XDDFCategoryAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(categoryAxis);
            categoryAxis.crossAxis(axis);
        }
        axes.add(categoryAxis);
        return categoryAxis;
    }

    public XDDFDateAxis createDateAxis(AxisPosition pos) {
        XDDFDateAxis dateAxis = new XDDFDateAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(dateAxis);
            dateAxis.crossAxis(axis);
        }
        axes.add(dateAxis);
        return dateAxis;
    }

    public List<? extends XDDFChartAxis> getAxes() {
        if (axes.isEmpty() && hasAxes()) {
            parseAxes();
        }
        return axes;
    }

    public XDDFChartData createData(ChartTypes type, XDDFChartAxis category, XDDFValueAxis values) {
        Map<Long, XDDFChartAxis> categories = Collections.singletonMap(category.getId(), category);
        Map<Long, XDDFValueAxis> mapValues = Collections.singletonMap(values.getId(), values);
        final CTPlotArea plotArea = chart.getPlotArea();
        switch (type) {
        case BAR:
            return new XDDFBarChartData(plotArea.addNewBarChart(), categories, mapValues);
        case LINE:
            return new XDDFLineChartData(plotArea.addNewLineChart(), categories, mapValues);
        case PIE:
            return new XDDFPieChartData(plotArea.addNewPieChart());
        case RADAR:
            return new XDDFRadarChartData(plotArea.addNewRadarChart(), categories, mapValues);
        case SCATTER:
            return new XDDFScatterChartData(plotArea.addNewScatterChart(), categories, mapValues);
        default:
            return null;
        }
    }

    public void plot(XDDFChartData data) {
        for (XDDFChartData.Series series : data.getSeries()) {
            series.plot();
        }
    }

    public XDDFManualLayout getOrAddManualLayout() {
        return new XDDFManualLayout(chart.getPlotArea());
    }

	@Deprecated
	public XSSFManualLayout getManualLayout() {
		return new XSSFManualLayout(this);
	}

	/**
	 * @return true if only visible cells will be present on the chart,
	 *         false otherwise
	 */
	public boolean isPlotOnlyVisibleCells() {
		return chart.getPlotVisOnly().getVal();
	}

	/**
	 * @param plotVisOnly a flag specifying if only visible cells should be
	 *        present on the chart
	 */
	public void setPlotOnlyVisibleCells(boolean plotVisOnly) {
		chart.getPlotVisOnly().setVal(plotVisOnly);
	}

	/**
	 * Returns the title static text, or null if none is set.
	 * Note that a title formula may be set instead.
	 * @return static title text, if set
	 * @deprecated POI 3.16, use {@link #getTitleText()} instead.
	 */
    @Deprecated
    @Removal(version="4.0")
	public XSSFRichTextString getTitle() {
	    return getTitleText();
	}

	/**
     * Returns the title static text, or null if none is set.
     * Note that a title formula may be set instead.
     * Empty text result is for backward compatibility, and could mean the title text is empty or there is a formula instead.
     * Check for a formula first, falling back on text for cleaner logic.
     * @return static title text if set,
     *         null if there is no title,
     *         empty string if the title text is empty or the title uses a formula instead
	 */
	public XSSFRichTextString getTitleText() {
		if(! chart.isSetTitle()) {
			return null;
		}

		// TODO Do properly
		CTTitle title = chart.getTitle();

		StringBuffer text = new StringBuffer();
		XmlObject[] t = title
			.selectPath("declare namespace a='"+XSSFDrawing.NAMESPACE_A+"' .//a:t");
		for (int m = 0; m < t.length; m++) {
			NodeList kids = t[m].getDomNode().getChildNodes();
			final int count = kids.getLength();
			for (int n = 0; n < count; n++) {
				Node kid = kids.item(n);
				if (kid instanceof Text) {
					text.append(kid.getNodeValue());
				}
			}
		}

		return new XSSFRichTextString(text.toString());
	}

	/**
	 * Sets the title text as a static string.
	 * @param newTitle to use
	 * @deprecated POI 3.16, use {@link #setTitleText(String)} instead.
	 */
    @Deprecated
    @Removal(version="4.0")
	public void setTitle(String newTitle) {

	}

    /**
     * Sets the title text as a static string.
     *
     * @param newTitle to use
     */
	public void setTitleText(String newTitle) {
		CTTitle ctTitle;
		if (chart.isSetTitle()) {
			ctTitle = chart.getTitle();
		} else {
			ctTitle = chart.addNewTitle();
		}

		CTTx tx;
		if (ctTitle.isSetTx()) {
			tx = ctTitle.getTx();
		} else {
			tx = ctTitle.addNewTx();
		}

		if (tx.isSetStrRef()) {
			tx.unsetStrRef();
		}

		CTTextBody rich;
		if (tx.isSetRich()) {
			rich = tx.getRich();
		} else {
			rich = tx.addNewRich();
			rich.addNewBodyPr();  // body properties must exist (but can be empty)
		}

		CTTextParagraph para;
		if (rich.sizeOfPArray() > 0) {
			para = rich.getPArray(0);
		} else {
			para = rich.addNewP();
		}

		if (para.sizeOfRArray() > 0) {
			CTRegularTextRun run = para.getRArray(0);
			run.setT(newTitle);
		} else if (para.sizeOfFldArray() > 0) {
			CTTextField fld = para.getFldArray(0);
			fld.setT(newTitle);
		} else {
			CTRegularTextRun run = para.addNewR();
			run.setT(newTitle);
		}
	}

	/**
	 * Get the chart title formula expression if there is one
	 * @return formula expression or null
	 */
	public String getTitleFormula() {
	    if(! chart.isSetTitle()) {
	        return null;
	    }

	    CTTitle title = chart.getTitle();

	    if (! title.isSetTx()) {
	        return null;
	    }

	    CTTx tx = title.getTx();

	    if (! tx.isSetStrRef()) {
	        return null;
	    }

	    return tx.getStrRef().getF();
	}

	/**
	 * Set the formula expression to use for the chart title
	 * @param formula
	 */
	public void setTitleFormula(String formula) {
	    CTTitle ctTitle;
	    if (chart.isSetTitle()) {
	        ctTitle = chart.getTitle();
	    } else {
	        ctTitle = chart.addNewTitle();
	    }

	    CTTx tx;
	    if (ctTitle.isSetTx()) {
	        tx = ctTitle.getTx();
	    } else {
	        tx = ctTitle.addNewTx();
	    }

	    if (tx.isSetRich()) {
	        tx.unsetRich();
	    }

	    CTStrRef strRef;
	    if (tx.isSetStrRef()) {
	        strRef = tx.getStrRef();
	    } else {
	        strRef = tx.addNewStrRef();
	    }

	    strRef.setF(formula);
	}

	@Deprecated
	public XSSFChartLegend getOrCreateLegend() {
		return new XSSFChartLegend(this);
	}

    public XDDFChartLegend getOrAddLegend() {
        return new XDDFChartLegend(chart);
    }

	public void deleteLegend() {
		if (chart.isSetLegend()) {
			chart.unsetLegend();
		}
	}

	/**
	 * @deprecated use {@link hasAxes} instead
	 * @return hasAxes()
	 */
	@Deprecated
	private boolean hasAxis() {
		return hasAxes();
	}

    private boolean hasAxes() {
        CTPlotArea ctPlotArea = chart.getPlotArea();
        int totalAxisCount = ctPlotArea.sizeOfValAxArray() + ctPlotArea.sizeOfCatAxArray() + ctPlotArea.sizeOfDateAxArray() + ctPlotArea.sizeOfSerAxArray();
        return totalAxisCount > 0;
    }

    private void parseAxes() {
        // TODO: add other axis types
        for (CTCatAx catAx : chart.getPlotArea().getCatAxArray()) {
            axes.add(new XDDFCategoryAxis(catAx));
        }
        for (CTDateAx dateAx : chart.getPlotArea().getDateAxArray()) {
            axes.add(new XDDFDateAxis(dateAx));
        }
        for (CTValAx valAx : chart.getPlotArea().getValAxArray()) {
            axes.add(new XDDFValueAxis(valAx));
        }
    }

	@Deprecated
	private void parseAxis() {
		// TODO: add other axis types
		parseCategoryAxis();
		parseDateAxis();
		parseValueAxis();
	}

	@Deprecated
	private void parseCategoryAxis() {
		for (CTCatAx catAx : chart.getPlotArea().getCatAxArray()) {
			axis.add(new XSSFCategoryAxis(this, catAx));
		}
	}

	@Deprecated
	private void parseDateAxis() {
		for (CTDateAx dateAx : chart.getPlotArea().getDateAxArray()) {
			axis.add(new XSSFDateAxis(this, dateAx));
		}
	}

	@Deprecated
	private void parseValueAxis() {
		for (CTValAx valAx : chart.getPlotArea().getValAxArray()) {
			axis.add(new XSSFValueAxis(this, valAx));
		}
	}

}
