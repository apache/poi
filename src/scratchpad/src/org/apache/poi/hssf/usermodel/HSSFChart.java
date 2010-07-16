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

package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.chart.*;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FooterRecord;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.PrintSetupRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtgBase;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressBase;

/**
 * Has methods for construction of a chart object.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class HSSFChart {
	private HSSFSheet sheet;
	private ChartRecord chartRecord;

	private LegendRecord legendRecord;
	private ChartTitleFormatRecord chartTitleFormat;
	private SeriesTextRecord chartTitleText;
	private List<ValueRangeRecord> valueRanges = new ArrayList<ValueRangeRecord>(); 
	
	private HSSFChartType type = HSSFChartType.Unknown;
	
	private List<HSSFSeries> series = new ArrayList<HSSFSeries>();

	public enum HSSFChartType {
		Area {
			@Override
			public short getSid() {
				return 0x101A;
			}
		},
		Bar {
			@Override
			public short getSid() {
				return 0x1017;
			}
		},
		Line {
			@Override
			public short getSid() {
				return 0x1018;
			}
		},
		Pie {
			@Override
			public short getSid() {
				return 0x1019;
			}
		},
		Scatter {
			@Override
			public short getSid() {
				return 0x101B;
			}
		},
		Unknown {
			@Override
			public short getSid() {
				return 0;
			}
		};
		
		public abstract short getSid();
	}

	private HSSFChart(HSSFSheet sheet, ChartRecord chartRecord) {
		this.chartRecord = chartRecord;
		this.sheet = sheet;
	}

	/**
	 * Creates a bar chart.  API needs some work. :)
	 * <p>
	 * NOTE:  Does not yet work...  checking it in just so others
	 * can take a look.
	 */
	public void createBarChart( HSSFWorkbook workbook, HSSFSheet sheet )
	{

		List<Record> records = new ArrayList<Record>();
		records.add( createMSDrawingObjectRecord() );
		records.add( createOBJRecord() );
		records.add( createBOFRecord() );
		records.add(new HeaderRecord(""));
		records.add(new FooterRecord(""));
		records.add( createHCenterRecord() );
		records.add( createVCenterRecord() );
		records.add( createPrintSetupRecord() );
		// unknown 33
		records.add( createFontBasisRecord1() );
		records.add( createFontBasisRecord2() );
		records.add(new ProtectRecord(false));
		records.add( createUnitsRecord() );
		records.add( createChartRecord( 0, 0, 30434904, 19031616 ) );
		records.add( createBeginRecord() );
		records.add( createSCLRecord( (short) 1, (short) 1 ) );
		records.add( createPlotGrowthRecord( 65536, 65536 ) );
		records.add( createFrameRecord1() );
		records.add( createBeginRecord() );
		records.add( createLineFormatRecord(true) );
		records.add( createAreaFormatRecord1() );
		records.add( createEndRecord() );
		records.add( createSeriesRecord() );
		records.add( createBeginRecord() );
		records.add( createTitleLinkedDataRecord() );
		records.add( createValuesLinkedDataRecord() );
		records.add( createCategoriesLinkedDataRecord() );
		records.add( createDataFormatRecord() );
		//		records.add(createBeginRecord());
		// unknown
		//		records.add(createEndRecord());
		records.add( createSeriesToChartGroupRecord() );
		records.add( createEndRecord() );
		records.add( createSheetPropsRecord() );
		records.add( createDefaultTextRecord( DefaultDataLabelTextPropertiesRecord.CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC ) );
		records.add( createAllTextRecord() );
		records.add( createBeginRecord() );
		// unknown
		records.add( createFontIndexRecord( 5 ) );
		records.add( createDirectLinkRecord() );
		records.add( createEndRecord() );
		records.add( createDefaultTextRecord( (short) 3 ) ); // eek, undocumented text type
		records.add( createUnknownTextRecord() );
		records.add( createBeginRecord() );
		records.add( createFontIndexRecord( (short) 6 ) );
		records.add( createDirectLinkRecord() );
		records.add( createEndRecord() );

		records.add( createAxisUsedRecord( (short) 1 ) );
		createAxisRecords( records );

		records.add( createEndRecord() );
		records.add( createDimensionsRecord() );
		records.add( createSeriesIndexRecord(2) );
		records.add( createSeriesIndexRecord(1) );
		records.add( createSeriesIndexRecord(3) );
		records.add(EOFRecord.instance);



		sheet.insertChartRecords( records );
		workbook.insertChartRecord();
	}

	/**
	 * Returns all the charts for the given sheet.
	 *
	 * NOTE: You won't be able to do very much with
	 *  these charts yet, as this is very limited support
	 */
	public static HSSFChart[] getSheetCharts(HSSFSheet sheet) {
		List<HSSFChart> charts = new ArrayList<HSSFChart>();
		HSSFChart lastChart = null;
		HSSFSeries lastSeries = null;
		// Find records of interest
		List<RecordBase> records = sheet.getSheet().getRecords();
		for(RecordBase r : records) {

			if(r instanceof ChartRecord) {
				lastSeries = null;
				
				lastChart = new HSSFChart(sheet,(ChartRecord)r);
				charts.add(lastChart);
			} else if(r instanceof LegendRecord) {
				lastChart.legendRecord = (LegendRecord)r;
			} else if(r instanceof SeriesRecord) {
				HSSFSeries series = lastChart.new HSSFSeries( (SeriesRecord)r );
				lastChart.series.add(series);
				lastSeries = series;
			} else if(r instanceof ChartTitleFormatRecord) {
				lastChart.chartTitleFormat =
					(ChartTitleFormatRecord)r;
			} else if(r instanceof SeriesTextRecord) {
				// Applies to a series, unless we've seen
				//  a legend already
				SeriesTextRecord str = (SeriesTextRecord)r;
				if(lastChart.legendRecord == null &&
						lastChart.series.size() > 0) {
					HSSFSeries series = (HSSFSeries)
						lastChart.series.get(lastChart.series.size()-1);
					series.seriesTitleText = str;
				} else {
					lastChart.chartTitleText = str;
				}
			} else if (r instanceof LinkedDataRecord) {
				LinkedDataRecord linkedDataRecord = (LinkedDataRecord) r;
				if (lastSeries != null) {
					lastSeries.insertData(linkedDataRecord);
				}
			} else if(r instanceof ValueRangeRecord){
				lastChart.valueRanges.add((ValueRangeRecord)r);
			} else if (r instanceof Record) {
				if (lastChart != null)
				{
					Record record = (Record) r;
					for (HSSFChartType type : HSSFChartType.values()) {
						if (type == HSSFChartType.Unknown)
						{
							continue;
						}
						if (record.getSid() == type.getSid()) {
							lastChart.type = type ;
							break;
						}
					}
				}
			}
		}

		return (HSSFChart[])
			charts.toArray( new HSSFChart[charts.size()] );
	}

	/** Get the X offset of the chart */
	public int getChartX() { return chartRecord.getX(); }
	/** Get the Y offset of the chart */
	public int getChartY() { return chartRecord.getY(); }
	/** Get the width of the chart. {@link ChartRecord} */
	public int getChartWidth() { return chartRecord.getWidth(); }
	/** Get the height of the chart. {@link ChartRecord} */
	public int getChartHeight() { return chartRecord.getHeight(); }

	/** Sets the X offset of the chart */
	public void setChartX(int x) { chartRecord.setX(x); }
	/** Sets the Y offset of the chart */
	public void setChartY(int y) { chartRecord.setY(y); }
	/** Sets the width of the chart. {@link ChartRecord} */
	public void setChartWidth(int width) { chartRecord.setWidth(width); }
	/** Sets the height of the chart. {@link ChartRecord} */
	public void setChartHeight(int height) { chartRecord.setHeight(height); }

	/**
	 * Returns the series of the chart
	 */
	public HSSFSeries[] getSeries() {
		return (HSSFSeries[])
			series.toArray(new HSSFSeries[series.size()]);
	}

	/**
	 * Returns the chart's title, if there is one,
	 *  or null if not
	 */
	public String getChartTitle() {
		if(chartTitleText != null) {
			return chartTitleText.getText();
		}
		return null;
	}

	/**
	 * Changes the chart's title, but only if there
	 *  was one already.
	 * TODO - add in the records if not
	 */
	public void setChartTitle(String title) {
		if(chartTitleText != null) {
			chartTitleText.setText(title);
		} else {
			throw new IllegalStateException("No chart title found to change");
		}
	}
	
	/**
	 * Set value range (basic Axis Options) 
	 * @param axisIndex 0 - primary axis, 1 - secondary axis
	 * @param minimum minimum value; Double.NaN - automatic; null - no change
	 * @param maximum maximum value; Double.NaN - automatic; null - no change
	 * @param majorUnit major unit value; Double.NaN - automatic; null - no change
	 * @param minorUnit minor unit value; Double.NaN - automatic; null - no change
	 */
	public void setValueRange( int axisIndex, Double minimum, Double maximum, Double majorUnit, Double minorUnit){
		ValueRangeRecord valueRange = (ValueRangeRecord)valueRanges.get( axisIndex );
		if( valueRange == null ) return;
		if( minimum != null ){
			valueRange.setAutomaticMinimum(minimum.isNaN());
			valueRange.setMinimumAxisValue(minimum);
		}
		if( maximum != null ){
			valueRange.setAutomaticMaximum(maximum.isNaN());
			valueRange.setMaximumAxisValue(maximum);
		}
		if( majorUnit != null ){
			valueRange.setAutomaticMajor(majorUnit.isNaN());
			valueRange.setMajorIncrement(majorUnit);
		}
		if( minorUnit != null ){
			valueRange.setAutomaticMinor(minorUnit.isNaN());
			valueRange.setMinorIncrement(minorUnit);
		}
	}

	private SeriesIndexRecord createSeriesIndexRecord( int index )
	{
		SeriesIndexRecord r = new SeriesIndexRecord();
		r.setIndex((short)index);
		return r;
	}

	private DimensionsRecord createDimensionsRecord()
	{
		DimensionsRecord r = new DimensionsRecord();
		r.setFirstRow(0);
		r.setLastRow(31);
		r.setFirstCol((short)0);
		r.setLastCol((short)1);
		return r;
	}

	private HCenterRecord createHCenterRecord()
	{
		HCenterRecord r = new HCenterRecord();
		r.setHCenter(false);
		return r;
	}

	private VCenterRecord createVCenterRecord()
	{
		VCenterRecord r = new VCenterRecord();
		r.setVCenter(false);
		return r;
	}

	private PrintSetupRecord createPrintSetupRecord()
	{
		PrintSetupRecord r = new PrintSetupRecord();
		r.setPaperSize((short)0);
		r.setScale((short)18);
		r.setPageStart((short)1);
		r.setFitWidth((short)1);
		r.setFitHeight((short)1);
		r.setLeftToRight(false);
		r.setLandscape(false);
		r.setValidSettings(true);
		r.setNoColor(false);
		r.setDraft(false);
		r.setNotes(false);
		r.setNoOrientation(false);
		r.setUsePage(false);
		r.setHResolution((short)0);
		r.setVResolution((short)0);
		r.setHeaderMargin(0.5);
		r.setFooterMargin(0.5);
		r.setCopies((short)15); // what the ??
		return r;
	}

	private FontBasisRecord createFontBasisRecord1()
	{
		FontBasisRecord r = new FontBasisRecord();
		r.setXBasis((short)9120);
		r.setYBasis((short)5640);
		r.setHeightBasis((short)200);
		r.setScale((short)0);
		r.setIndexToFontTable((short)5);
		return r;
	}

	private FontBasisRecord createFontBasisRecord2()
	{
		FontBasisRecord r = createFontBasisRecord1();
		r.setIndexToFontTable((short)6);
		return r;
	}

	private BOFRecord createBOFRecord()
	{
		BOFRecord r = new BOFRecord();
		r.setVersion((short)600);
		r.setType((short)20);
		r.setBuild((short)0x1CFE);
		r.setBuildYear((short)1997);
		r.setHistoryBitMask(0x40C9);
		r.setRequiredVersion(106);
		return r;
	}

	private UnknownRecord createOBJRecord()
	{
		byte[] data = {
			(byte) 0x15, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x11, (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB8, (byte) 0x03,
			(byte) 0x87, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		};

		return new UnknownRecord( (short) 0x005D, data );
	}

	private UnknownRecord createMSDrawingObjectRecord()
	{
		// Since we haven't created this object yet we'll just put in the raw
		// form for the moment.

		byte[] data = {
			(byte)0x0F, (byte)0x00, (byte)0x02, (byte)0xF0, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x08, (byte)0xF0, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x0F, (byte)0x00, (byte)0x03, (byte)0xF0, (byte)0xA8, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x0F, (byte)0x00, (byte)0x04, (byte)0xF0, (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x09, (byte)0xF0, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x02, (byte)0x00, (byte)0x0A, (byte)0xF0, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x0F, (byte)0x00, (byte)0x04, (byte)0xF0, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x92, (byte)0x0C, (byte)0x0A, (byte)0xF0, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x02, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A, (byte)0x00, (byte)0x00, (byte)0x93, (byte)0x00, (byte)0x0B, (byte)0xF0, (byte)0x36, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x7F, (byte)0x00, (byte)0x04, (byte)0x01, (byte)0x04, (byte)0x01, (byte)0xBF, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x81, (byte)0x01, (byte)0x4E, (byte)0x00,
			(byte)0x00, (byte)0x08, (byte)0x83, (byte)0x01, (byte)0x4D, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0xBF, (byte)0x01, (byte)0x10, (byte)0x00, (byte)0x11, (byte)0x00, (byte)0xC0, (byte)0x01,
			(byte)0x4D, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0xFF, (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x3F, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00,
			(byte)0xBF, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0xF0, (byte)0x12, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x04, (byte)0x00, (byte)0xC0, (byte)0x02, (byte)0x0A, (byte)0x00, (byte)0xF4, (byte)0x00, (byte)0x0E, (byte)0x00, (byte)0x66, (byte)0x01, (byte)0x20, (byte)0x00, (byte)0xE9, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x11, (byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
		};

		return new UnknownRecord((short)0x00EC, data);
	}

	private void createAxisRecords( List<Record> records )
	{
		records.add( createAxisParentRecord() );
		records.add( createBeginRecord() );
		records.add( createAxisRecord( AxisRecord.AXIS_TYPE_CATEGORY_OR_X_AXIS ) );
		records.add( createBeginRecord() );
		records.add( createCategorySeriesAxisRecord() );
		records.add( createAxisOptionsRecord() );
		records.add( createTickRecord1() );
		records.add( createEndRecord() );
		records.add( createAxisRecord( AxisRecord.AXIS_TYPE_VALUE_AXIS ) );
		records.add( createBeginRecord() );
		records.add( createValueRangeRecord() );
		records.add( createTickRecord2() );
		records.add( createAxisLineFormatRecord( AxisLineFormatRecord.AXIS_TYPE_MAJOR_GRID_LINE ) );
		records.add( createLineFormatRecord(false) );
		records.add( createEndRecord() );
		records.add( createPlotAreaRecord() );
		records.add( createFrameRecord2() );
		records.add( createBeginRecord() );
		records.add( createLineFormatRecord2() );
		records.add( createAreaFormatRecord2() );
		records.add( createEndRecord() );
		records.add( createChartFormatRecord() );
		records.add( createBeginRecord() );
		records.add( createBarRecord() );
		// unknown 1022
		records.add( createLegendRecord() );
		records.add( createBeginRecord() );
		// unknown 104f
		records.add( createTextRecord() );
		records.add( createBeginRecord() );
		// unknown 104f
		records.add( createLinkedDataRecord() );
		records.add( createEndRecord() );
		records.add( createEndRecord() );
		records.add( createEndRecord() );
		records.add( createEndRecord() );
	}

	private LinkedDataRecord createLinkedDataRecord()
	{
		LinkedDataRecord r = new LinkedDataRecord();
		r.setLinkType(LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT);
		r.setReferenceType(LinkedDataRecord.REFERENCE_TYPE_DIRECT);
		r.setCustomNumberFormat(false);
		r.setIndexNumberFmtRecord((short)0);
		r.setFormulaOfLink(null);
		return r;
	}

	private TextRecord createTextRecord()
	{
		TextRecord r = new TextRecord();
		r.setHorizontalAlignment(TextRecord.HORIZONTAL_ALIGNMENT_CENTER);
		r.setVerticalAlignment(TextRecord.VERTICAL_ALIGNMENT_CENTER);
		r.setDisplayMode((short)1);
		r.setRgbColor(0x00000000);
		r.setX(-37);
		r.setY(-60);
		r.setWidth(0);
		r.setHeight(0);
		r.setAutoColor(true);
		r.setShowKey(false);
		r.setShowValue(false);
		r.setVertical(false);
		r.setAutoGeneratedText(true);
		r.setGenerated(true);
		r.setAutoLabelDeleted(false);
		r.setAutoBackground(true);
		r.setRotation((short)0);
		r.setShowCategoryLabelAsPercentage(false);
		r.setShowValueAsPercentage(false);
		r.setShowBubbleSizes(false);
		r.setShowLabel(false);
		r.setIndexOfColorValue((short)77);
		r.setDataLabelPlacement((short)0);
		r.setTextRotation((short)0);
		return r;
	}

	private LegendRecord createLegendRecord()
	{
		LegendRecord r = new LegendRecord();
		r.setXAxisUpperLeft(3542);
		r.setYAxisUpperLeft(1566);
		r.setXSize(437);
		r.setYSize(213);
		r.setType(LegendRecord.TYPE_RIGHT);
		r.setSpacing(LegendRecord.SPACING_MEDIUM);
		r.setAutoPosition(true);
		r.setAutoSeries(true);
		r.setAutoXPositioning(true);
		r.setAutoYPositioning(true);
		r.setVertical(true);
		r.setDataTable(false);
		return r;
	}

	private BarRecord createBarRecord()
	{
		BarRecord r = new BarRecord();
		r.setBarSpace((short)0);
		r.setCategorySpace((short)150);
		r.setHorizontal(false);
		r.setStacked(false);
		r.setDisplayAsPercentage(false);
		r.setShadow(false);
		return r;
	}

	private ChartFormatRecord createChartFormatRecord()
	{
		ChartFormatRecord r = new ChartFormatRecord();
		r.setXPosition(0);
		r.setYPosition(0);
		r.setWidth(0);
		r.setHeight(0);
		r.setVaryDisplayPattern(false);
		return r;
	}

	private PlotAreaRecord createPlotAreaRecord()
	{
		PlotAreaRecord r = new PlotAreaRecord(  );
		return r;
	}

	private AxisLineFormatRecord createAxisLineFormatRecord( short format )
	{
		AxisLineFormatRecord r = new AxisLineFormatRecord();
		r.setAxisType( format );
		return r;
	}

	private ValueRangeRecord createValueRangeRecord()
	{
		ValueRangeRecord r = new ValueRangeRecord();
		r.setMinimumAxisValue( 0.0 );
		r.setMaximumAxisValue( 0.0 );
		r.setMajorIncrement( 0 );
		r.setMinorIncrement( 0 );
		r.setCategoryAxisCross( 0 );
		r.setAutomaticMinimum( true );
		r.setAutomaticMaximum( true );
		r.setAutomaticMajor( true );
		r.setAutomaticMinor( true );
		r.setAutomaticCategoryCrossing( true );
		r.setLogarithmicScale( false );
		r.setValuesInReverse( false );
		r.setCrossCategoryAxisAtMaximum( false );
		r.setReserved( true );	// what's this do??
		return r;
	}

	private TickRecord createTickRecord1()
	{
		TickRecord r = new TickRecord();
		r.setMajorTickType( (byte) 2 );
		r.setMinorTickType( (byte) 0 );
		r.setLabelPosition( (byte) 3 );
		r.setBackground( (byte) 1 );
		r.setLabelColorRgb( 0 );
		r.setZero1( (short) 0 );
		r.setZero2( (short) 0 );
		r.setZero3( (short) 45 );
		r.setAutorotate( true );
		r.setAutoTextBackground( true );
		r.setRotation( (short) 0 );
		r.setAutorotate( true );
		r.setTickColor( (short) 77 );
		return r;
	}

	private TickRecord createTickRecord2()
	{
		TickRecord r = createTickRecord1();
		r.setZero3((short)0);
		return r;
	}

	private AxisOptionsRecord createAxisOptionsRecord()
	{
		AxisOptionsRecord r = new AxisOptionsRecord();
		r.setMinimumCategory( (short) -28644 );
		r.setMaximumCategory( (short) -28715 );
		r.setMajorUnitValue( (short) 2 );
		r.setMajorUnit( (short) 0 );
		r.setMinorUnitValue( (short) 1 );
		r.setMinorUnit( (short) 0 );
		r.setBaseUnit( (short) 0 );
		r.setCrossingPoint( (short) -28644 );
		r.setDefaultMinimum( true );
		r.setDefaultMaximum( true );
		r.setDefaultMajor( true );
		r.setDefaultMinorUnit( true );
		r.setIsDate( true );
		r.setDefaultBase( true );
		r.setDefaultCross( true );
		r.setDefaultDateSettings( true );
		return r;
	}

	private CategorySeriesAxisRecord createCategorySeriesAxisRecord()
	{
		CategorySeriesAxisRecord r = new CategorySeriesAxisRecord();
		r.setCrossingPoint( (short) 1 );
		r.setLabelFrequency( (short) 1 );
		r.setTickMarkFrequency( (short) 1 );
		r.setValueAxisCrossing( true );
		r.setCrossesFarRight( false );
		r.setReversed( false );
		return r;
	}

	private AxisRecord createAxisRecord( short axisType )
	{
		AxisRecord r = new AxisRecord();
		r.setAxisType( axisType );
		return r;
	}

	private AxisParentRecord createAxisParentRecord()
	{
		AxisParentRecord r = new AxisParentRecord();
		r.setAxisType( AxisParentRecord.AXIS_TYPE_MAIN );
		r.setX( 479 );
		r.setY( 221 );
		r.setWidth( 2995 );
		r.setHeight( 2902 );
		return r;
	}

	private AxisUsedRecord createAxisUsedRecord( short numAxis )
	{
		AxisUsedRecord r = new AxisUsedRecord();
		r.setNumAxis( numAxis );
		return r;
	}

	private LinkedDataRecord createDirectLinkRecord()
	{
		LinkedDataRecord r = new LinkedDataRecord();
		r.setLinkType( LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT );
		r.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_DIRECT );
		r.setCustomNumberFormat( false );
		r.setIndexNumberFmtRecord( (short) 0 );
		r.setFormulaOfLink(null);
		return r;
	}

	private FontIndexRecord createFontIndexRecord( int index )
	{
		FontIndexRecord r = new FontIndexRecord();
		r.setFontIndex( (short) index );
		return r;
	}

	private TextRecord createAllTextRecord()
	{
		TextRecord r = new TextRecord();
		r.setHorizontalAlignment( TextRecord.HORIZONTAL_ALIGNMENT_CENTER );
		r.setVerticalAlignment( TextRecord.VERTICAL_ALIGNMENT_CENTER );
		r.setDisplayMode( TextRecord.DISPLAY_MODE_TRANSPARENT );
		r.setRgbColor( 0 );
		r.setX( -37 );
		r.setY( -60 );
		r.setWidth( 0 );
		r.setHeight( 0 );
		r.setAutoColor( true );
		r.setShowKey( false );
		r.setShowValue( true );
		r.setVertical( false );
		r.setAutoGeneratedText( true );
		r.setGenerated( true );
		r.setAutoLabelDeleted( false );
		r.setAutoBackground( true );
		r.setRotation( (short) 0 );
		r.setShowCategoryLabelAsPercentage( false );
		r.setShowValueAsPercentage( false );
		r.setShowBubbleSizes( false );
		r.setShowLabel( false );
		r.setIndexOfColorValue( (short) 77 );
		r.setDataLabelPlacement( (short) 0 );
		r.setTextRotation( (short) 0 );
		return r;
	}

	private TextRecord createUnknownTextRecord()
	{
		TextRecord r = new TextRecord();
		r.setHorizontalAlignment( TextRecord.HORIZONTAL_ALIGNMENT_CENTER );
		r.setVerticalAlignment( TextRecord.VERTICAL_ALIGNMENT_CENTER );
		r.setDisplayMode( TextRecord.DISPLAY_MODE_TRANSPARENT );
		r.setRgbColor( 0 );
		r.setX( -37 );
		r.setY( -60 );
		r.setWidth( 0 );
		r.setHeight( 0 );
		r.setAutoColor( true );
		r.setShowKey( false );
		r.setShowValue( false );
		r.setVertical( false );
		r.setAutoGeneratedText( true );
		r.setGenerated( true );
		r.setAutoLabelDeleted( false );
		r.setAutoBackground( true );
		r.setRotation( (short) 0 );
		r.setShowCategoryLabelAsPercentage( false );
		r.setShowValueAsPercentage( false );
		r.setShowBubbleSizes( false );
		r.setShowLabel( false );
		r.setIndexOfColorValue( (short) 77 );
		r.setDataLabelPlacement( (short) 11088 );
		r.setTextRotation( (short) 0 );
		return r;
	}

	private DefaultDataLabelTextPropertiesRecord createDefaultTextRecord( short categoryDataType )
	{
		DefaultDataLabelTextPropertiesRecord r = new DefaultDataLabelTextPropertiesRecord();
		r.setCategoryDataType( categoryDataType );
		return r;
	}

	private SheetPropertiesRecord createSheetPropsRecord()
	{
		SheetPropertiesRecord r = new SheetPropertiesRecord();
		r.setChartTypeManuallyFormatted( false );
		r.setPlotVisibleOnly( true );
		r.setDoNotSizeWithWindow( false );
		r.setDefaultPlotDimensions( true );
		r.setAutoPlotArea( false );
		return r;
	}

	private SeriesToChartGroupRecord createSeriesToChartGroupRecord()
	{
		return new SeriesToChartGroupRecord();
	}

	private DataFormatRecord createDataFormatRecord()
	{
		DataFormatRecord r = new DataFormatRecord();
		r.setPointNumber( (short) -1 );
		r.setSeriesIndex( (short) 0 );
		r.setSeriesNumber( (short) 0 );
		r.setUseExcel4Colors( false );
		return r;
	}

	private LinkedDataRecord createCategoriesLinkedDataRecord()
	{
		LinkedDataRecord r = new LinkedDataRecord();
		r.setLinkType( LinkedDataRecord.LINK_TYPE_CATEGORIES );
		r.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_WORKSHEET );
		r.setCustomNumberFormat( false );
		r.setIndexNumberFmtRecord( (short) 0 );
		Area3DPtg p = new Area3DPtg(0, 31, 1, 1,
		        false, false, false, false, 0);
		r.setFormulaOfLink(new Ptg[] { p, });
		return r;
	}

	private LinkedDataRecord createValuesLinkedDataRecord()
	{
		LinkedDataRecord r = new LinkedDataRecord();
		r.setLinkType( LinkedDataRecord.LINK_TYPE_VALUES );
		r.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_WORKSHEET );
		r.setCustomNumberFormat( false );
		r.setIndexNumberFmtRecord( (short) 0 );
		Area3DPtg p = new Area3DPtg(0, 31, 0, 0,
				false, false, false, false, 0);
		r.setFormulaOfLink(new Ptg[] { p, });
		return r;
	}

	private LinkedDataRecord createTitleLinkedDataRecord()
	{
		LinkedDataRecord r = new LinkedDataRecord();
		r.setLinkType( LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT );
		r.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_DIRECT );
		r.setCustomNumberFormat( false );
		r.setIndexNumberFmtRecord( (short) 0 );
		r.setFormulaOfLink(null);
		return r;
	}

	private SeriesRecord createSeriesRecord()
	{
		SeriesRecord r = new SeriesRecord();
		r.setCategoryDataType( SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC );
		r.setValuesDataType( SeriesRecord.VALUES_DATA_TYPE_NUMERIC );
		r.setNumCategories( (short) 32 );
		r.setNumValues( (short) 31 );
		r.setBubbleSeriesType( SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC );
		r.setNumBubbleValues( (short) 0 );
		return r;
	}

	private EndRecord createEndRecord()
	{
		return new EndRecord();
	}

	private AreaFormatRecord createAreaFormatRecord1()
	{
		AreaFormatRecord r = new AreaFormatRecord();
		r.setForegroundColor( 16777215 );	 // RGB Color
		r.setBackgroundColor( 0 );			// RGB Color
		r.setPattern( (short) 1 );			 // TODO: Add Pattern constants to record
		r.setAutomatic( true );
		r.setInvert( false );
		r.setForecolorIndex( (short) 78 );
		r.setBackcolorIndex( (short) 77 );
		return r;
	}

	private AreaFormatRecord createAreaFormatRecord2()
	{
		AreaFormatRecord r = new AreaFormatRecord();
		r.setForegroundColor(0x00c0c0c0);
		r.setBackgroundColor(0x00000000);
		r.setPattern((short)1);
		r.setAutomatic(false);
		r.setInvert(false);
		r.setForecolorIndex((short)22);
		r.setBackcolorIndex((short)79);
		return r;
	}

	private LineFormatRecord createLineFormatRecord( boolean drawTicks )
	{
		LineFormatRecord r = new LineFormatRecord();
		r.setLineColor( 0 );
		r.setLinePattern( LineFormatRecord.LINE_PATTERN_SOLID );
		r.setWeight( (short) -1 );
		r.setAuto( true );
		r.setDrawTicks( drawTicks );
		r.setColourPaletteIndex( (short) 77 );  // what colour is this?
		return r;
	}

	private LineFormatRecord createLineFormatRecord2()
	{
		LineFormatRecord r = new LineFormatRecord();
		r.setLineColor( 0x00808080 );
		r.setLinePattern( (short) 0 );
		r.setWeight( (short) 0 );
		r.setAuto( false );
		r.setDrawTicks( false );
		r.setUnknown( false );
		r.setColourPaletteIndex( (short) 23 );
		return r;
	}

	private FrameRecord createFrameRecord1()
	{
		FrameRecord r = new FrameRecord();
		r.setBorderType( FrameRecord.BORDER_TYPE_REGULAR );
		r.setAutoSize( false );
		r.setAutoPosition( true );
		return r;
	}

	private FrameRecord createFrameRecord2()
	{
		FrameRecord r = new FrameRecord();
		r.setBorderType( FrameRecord.BORDER_TYPE_REGULAR );
		r.setAutoSize( true );
		r.setAutoPosition( true );
		return r;
	}

	private PlotGrowthRecord createPlotGrowthRecord( int horizScale, int vertScale )
	{
		PlotGrowthRecord r = new PlotGrowthRecord();
		r.setHorizontalScale( horizScale );
		r.setVerticalScale( vertScale );
		return r;
	}

	private SCLRecord createSCLRecord( short numerator, short denominator )
	{
		SCLRecord r = new SCLRecord();
		r.setDenominator( denominator );
		r.setNumerator( numerator );
		return r;
	}

	private BeginRecord createBeginRecord()
	{
		return new BeginRecord();
	}

	private ChartRecord createChartRecord( int x, int y, int width, int height )
	{
		ChartRecord r = new ChartRecord();
		r.setX( x );
		r.setY( y );
		r.setWidth( width );
		r.setHeight( height );
		return r;
	}

	private UnitsRecord createUnitsRecord()
	{
		UnitsRecord r = new UnitsRecord();
		r.setUnits( (short) 0 );
		return r;
	}


	/**
	 * A series in a chart
	 */
	public class HSSFSeries {
		private SeriesRecord series;
		private SeriesTextRecord seriesTitleText;
		private LinkedDataRecord dataName;
		private LinkedDataRecord dataValues;
		private LinkedDataRecord dataCategoryLabels;
		private LinkedDataRecord dataSecondaryCategoryLabels;

		/* package */ HSSFSeries(SeriesRecord series) {
			this.series = series;
		}

		/* package */ void insertData(LinkedDataRecord data){
			switch(data.getLinkType()){
				case 0: dataName = data;
				break;
				case 1: dataValues = data;
				break;
				case 2: dataCategoryLabels = data;
				break;
				case 3: dataSecondaryCategoryLabels = data;
				break;
			}
		}
		
		/* package */ void setSeriesTitleText(SeriesTextRecord seriesTitleText)
		{
			this.seriesTitleText = seriesTitleText;
		}
		
		public short getNumValues() {
			return series.getNumValues();
		}
		/**
		 * See {@link SeriesRecord}
		 */
		public short getValueType() {
			return series.getValuesDataType();
		}

		/**
		 * Returns the series' title, if there is one,
		 *  or null if not
		 */
		public String getSeriesTitle() {
			if(seriesTitleText != null) {
				return seriesTitleText.getText();
			}
			return null;
		}

		/**
		 * Changes the series' title, but only if there
		 *  was one already.
		 * TODO - add in the records if not
		 */
		public void setSeriesTitle(String title) {
			if(seriesTitleText != null) {
				seriesTitleText.setText(title);
			} else {
				throw new IllegalStateException("No series title found to change");
			}
		}

		/**
		 * @return record with data names
		 */
		public LinkedDataRecord getDataName(){
			return dataName;
		}
		
		/**
		 * @return record with data values
		 */
		public LinkedDataRecord getDataValues(){
			return dataValues;
		}
		
		/**
		 * @return record with data category labels
		 */
		public LinkedDataRecord getDataCategoryLabels(){
			return dataCategoryLabels;
		}
		
		/**
		 * @return record with data secondary category labels
		 */
		public LinkedDataRecord getDataSecondaryCategoryLabels() {
			return dataSecondaryCategoryLabels;
		}
		
		/**
		 * @return record with series
		 */
		public SeriesRecord getSeries() {
			return series;
		}
		
		private CellRangeAddressBase getCellRange(LinkedDataRecord linkedDataRecord) {
			if (linkedDataRecord == null)
			{
				return null ;
			}
			
			int firstRow = 0;
			int lastRow = 0;
			int firstCol = 0;
			int lastCol = 0;
			
			for (Ptg ptg : linkedDataRecord.getFormulaOfLink()) {
				if (ptg instanceof AreaPtgBase) {
					AreaPtgBase areaPtg = (AreaPtgBase) ptg;
					
					firstRow = areaPtg.getFirstRow();
					lastRow = areaPtg.getLastRow();
					
					firstCol = areaPtg.getFirstColumn();
					lastCol = areaPtg.getLastColumn();
				}
			}
			
			return new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		}
		
		public CellRangeAddressBase getValuesCellRange() {
			return getCellRange(dataValues);
		}
	
		public CellRangeAddressBase getCategoryLabelsCellRange() {
			return getCellRange(dataCategoryLabels);
		}
	
		private Integer setVerticalCellRange(LinkedDataRecord linkedDataRecord,
				                             CellRangeAddressBase range) {
			if (linkedDataRecord == null)
			{
				return null;
			}
			
			List<Ptg> ptgList = new ArrayList<Ptg>();
			
			int rowCount = (range.getLastRow() - range.getFirstRow()) + 1;
			int colCount = (range.getLastColumn() - range.getFirstColumn()) + 1;
			
			for (Ptg ptg : linkedDataRecord.getFormulaOfLink()) {
				if (ptg instanceof AreaPtgBase) {
					AreaPtgBase areaPtg = (AreaPtgBase) ptg;
					
					areaPtg.setFirstRow(range.getFirstRow());
					areaPtg.setLastRow(range.getLastRow());
					
					areaPtg.setFirstColumn(range.getFirstColumn());
					areaPtg.setLastColumn(range.getLastColumn());
					ptgList.add(areaPtg);
				}
			}
			
			linkedDataRecord.setFormulaOfLink(ptgList.toArray(new Ptg[ptgList.size()]));
			
			return rowCount * colCount;
		}
		
		public void setValuesCellRange(CellRangeAddressBase range) {
			Integer count = setVerticalCellRange(dataValues, range);
			if (count == null)
			{
				return;
			}
			
			series.setNumValues((short)(int)count);
		}
		
		public void setCategoryLabelsCellRange(CellRangeAddressBase range) {
			Integer count = setVerticalCellRange(dataCategoryLabels, range);
			if (count == null)
			{
				return;
			}
			
			series.setNumCategories((short)(int)count);
		}
	}
	
	public HSSFSeries createSeries() throws Exception {
		ArrayList<RecordBase> seriesTemplate = new ArrayList<RecordBase>();
		boolean seriesTemplateFilled = false;
		
		int idx = 0;
		int deep = 0;
		int chartRecordIdx = -1;
		int chartDeep = -1;
		int lastSeriesDeep = -1;
		int endSeriesRecordIdx = -1;
		int seriesIdx = 0;
		final List<RecordBase> records = sheet.getSheet().getRecords();
		
		/* store first series as template and find last series index */
		for(final RecordBase record : records) {		
			
			idx++;
			
			if (record instanceof BeginRecord) {
				deep++;
			} else if (record instanceof EndRecord) {
				deep--;
				
				if (lastSeriesDeep == deep) {
					lastSeriesDeep = -1;
					endSeriesRecordIdx = idx;
					if (!seriesTemplateFilled) {
						seriesTemplate.add(record);
						seriesTemplateFilled = true;
					}
				}
				
				if (chartDeep == deep) {
					break;
				}
			}
			
			if (record instanceof ChartRecord) {
				if (record == chartRecord) {
					chartRecordIdx = idx;
					chartDeep = deep;
				}
			} else if (record instanceof SeriesRecord) {
				if (chartRecordIdx != -1) {
					seriesIdx++;
					lastSeriesDeep = deep;
				}
			}
			
			if (lastSeriesDeep != -1 && !seriesTemplateFilled) {
				seriesTemplate.add(record) ;
			}
		}
		
		/* check if a series was found */
		if (endSeriesRecordIdx == -1) {
			return null;
		}
		
		/* next index in the records list where the new series can be inserted */
		idx = endSeriesRecordIdx + 1;

		HSSFSeries newSeries = null;
		
		/* duplicate record of the template series */
		ArrayList<RecordBase> clonedRecords = new ArrayList<RecordBase>();
		for(final RecordBase record : seriesTemplate) {		
			
			Record newRecord = null;
			
			if (record instanceof BeginRecord) {
				newRecord = new BeginRecord();
			} else if (record instanceof EndRecord) {
				newRecord = new EndRecord();
			} else if (record instanceof SeriesRecord) {
				SeriesRecord seriesRecord = (SeriesRecord) ((SeriesRecord)record).clone();
				newSeries = new HSSFSeries(seriesRecord);
				newRecord = seriesRecord;
			} else if (record instanceof LinkedDataRecord) {
				LinkedDataRecord linkedDataRecord = (LinkedDataRecord) ((LinkedDataRecord)record).clone();
				if (newSeries != null) {
					newSeries.insertData(linkedDataRecord);
				}
				newRecord = linkedDataRecord;
			} else if (record instanceof DataFormatRecord) {
				DataFormatRecord dataFormatRecord = (DataFormatRecord) ((DataFormatRecord)record).clone();
				
				dataFormatRecord.setSeriesIndex((short)seriesIdx) ;
				dataFormatRecord.setSeriesNumber((short)seriesIdx) ;
				
				newRecord = dataFormatRecord;
			} else if (record instanceof SeriesTextRecord) {
				SeriesTextRecord seriesTextRecord = (SeriesTextRecord) ((SeriesTextRecord)record).clone();
				if (newSeries != null) {
					newSeries.setSeriesTitleText(seriesTextRecord);
				}
				newRecord = seriesTextRecord;
			} else if (record instanceof Record) {
				newRecord = (Record) ((Record)record).clone();
			}
			
			if (newRecord != null)
			{
				clonedRecords.add(newRecord);
			}
		}
		
		/* check if a user model series object was created */
		if (newSeries == null)
		{
			return null;
		}
		
		/* transfer series to record list */
		for(final RecordBase record : clonedRecords) {		
			records.add(idx++, record);
		}
		
		return newSeries;
	}
	
	public boolean removeSeries(HSSFSeries series) {
		int idx = 0;
		int deep = 0;
		int chartDeep = -1;
		int lastSeriesDeep = -1;
		int seriesIdx = -1;
		boolean removeSeries = false;
		boolean chartEntered = false;
		boolean result = false;
		final List<RecordBase> records = sheet.getSheet().getRecords();
		
		/* store first series as template and find last series index */
		Iterator<RecordBase> iter = records.iterator();
		while (iter.hasNext()) {		
			RecordBase record = iter.next();
			idx++;
			
			if (record instanceof BeginRecord) {
				deep++;
			} else if (record instanceof EndRecord) {
				deep--;
				
				if (lastSeriesDeep == deep) {
					lastSeriesDeep = -1;
					
					if (removeSeries) {
						removeSeries = false;
						result = true;
						iter.remove();
					}
				}
				
				if (chartDeep == deep) {
					break;
				}
			}
			
			if (record instanceof ChartRecord) {
				if (record == chartRecord) {
					chartDeep = deep;
					chartEntered = true;
				}
			} else if (record instanceof SeriesRecord) {
				if (chartEntered) {
					if (series.series == record) {
						lastSeriesDeep = deep;
						removeSeries = true;
					} else {
						seriesIdx++;
					}
				}
			} else if (record instanceof DataFormatRecord) {
				if (chartEntered && !removeSeries) {
					DataFormatRecord dataFormatRecord = (DataFormatRecord) record;
					dataFormatRecord.setSeriesIndex((short) seriesIdx);
					dataFormatRecord.setSeriesNumber((short) seriesIdx);
				}
			}
			
			if (removeSeries) {
				iter.remove();
			}
		}
		
		return result;
	}
	
	public HSSFChartType getType() {
		return type;
	}
}
