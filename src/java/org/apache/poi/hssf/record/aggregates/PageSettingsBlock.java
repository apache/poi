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

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.BottomMarginRecord;
import org.apache.poi.hssf.record.FooterRecord;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.HorizontalPageBreakRecord;
import org.apache.poi.hssf.record.LeftMarginRecord;
import org.apache.poi.hssf.record.Margin;
import org.apache.poi.hssf.record.PageBreakRecord;
import org.apache.poi.hssf.record.PrintSetupRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RightMarginRecord;
import org.apache.poi.hssf.record.TopMarginRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.VerticalPageBreakRecord;

/**
 * Groups the page settings records for a worksheet.<p/>
 * 
 * See OOO excelfileformat.pdf sec 4.4 'Page Settings Block'
 * 
 * @author Josh Micich
 */
public final class PageSettingsBlock extends RecordAggregate {
	// Every one of these component records is optional 
	// (The whole PageSettingsBlock may not be present) 
	private PageBreakRecord _rowBreaksRecord;
	private PageBreakRecord _columnBreaksRecord;
	private HeaderRecord _header;
	private FooterRecord _footer;
	private HCenterRecord _hCenter;
	private VCenterRecord _vCenter;
	private LeftMarginRecord _leftMargin;
	private RightMarginRecord _rightMargin;
	private TopMarginRecord _topMargin;
	private BottomMarginRecord _bottomMargin;
	private Record _pls;
	private PrintSetupRecord printSetup;
	private Record _bitmap;

	public PageSettingsBlock(RecordStream rs) {
		while(true) {
			if (!readARecord(rs)) {
				break;
			}
		}
	}

	/**
	 * Creates a PageSettingsBlock with default settings
	 */
	public PageSettingsBlock() {
		_rowBreaksRecord = new HorizontalPageBreakRecord();
		_columnBreaksRecord = new VerticalPageBreakRecord();
		_header = new HeaderRecord("");
		_footer = new FooterRecord("");
		_hCenter = createHCenter();
		_vCenter = createVCenter();
		printSetup = createPrintSetup();
	}

	/**
	 * @return <code>true</code> if the specified Record sid is one belonging to the 
	 * 'Page Settings Block'.
	 */
	public static boolean isComponentRecord(int sid) {
		switch (sid) {
			case HorizontalPageBreakRecord.sid:
			case VerticalPageBreakRecord.sid:
			case HeaderRecord.sid:
			case FooterRecord.sid:
			case HCenterRecord.sid:
			case VCenterRecord.sid:
			case LeftMarginRecord.sid:
			case RightMarginRecord.sid:
			case TopMarginRecord.sid:
			case BottomMarginRecord.sid:
			case UnknownRecord.PLS_004D:
			case PrintSetupRecord.sid:
			case UnknownRecord.BITMAP_00E9:
				return true;
		}
		return false;
	}

	private boolean readARecord(RecordStream rs) {
		switch (rs.peekNextSid()) {
			case HorizontalPageBreakRecord.sid:
				_rowBreaksRecord = (PageBreakRecord) rs.getNext();
				break;
			case VerticalPageBreakRecord.sid:
				_columnBreaksRecord = (PageBreakRecord) rs.getNext();
				break;
			case HeaderRecord.sid:
				_header = (HeaderRecord) rs.getNext();
				break;
			case FooterRecord.sid:
				_footer = (FooterRecord) rs.getNext();
				break;
			case HCenterRecord.sid:
				_hCenter = (HCenterRecord) rs.getNext();
				break;
			case VCenterRecord.sid:
				_vCenter = (VCenterRecord) rs.getNext();
				break;
			case LeftMarginRecord.sid:
				_leftMargin = (LeftMarginRecord) rs.getNext();
				break;
			case RightMarginRecord.sid:
				_rightMargin = (RightMarginRecord) rs.getNext();
				break;
			case TopMarginRecord.sid:
				_topMargin = (TopMarginRecord) rs.getNext();
				break;
			case BottomMarginRecord.sid:
				_bottomMargin = (BottomMarginRecord) rs.getNext();
				break;
			case 0x004D: // PLS
				_pls = rs.getNext();
				break;
			case PrintSetupRecord.sid:
				printSetup = (PrintSetupRecord)rs.getNext();
				break;
			case 0x00E9: // BITMAP
				_bitmap = rs.getNext();
				break;
			default:
				// all other record types are not part of the PageSettingsBlock
				return false;
		}
		return true;
	}

	private PageBreakRecord getRowBreaksRecord() {
		if (_rowBreaksRecord == null) {
			_rowBreaksRecord = new HorizontalPageBreakRecord();
		}
		return _rowBreaksRecord;
	}

	private PageBreakRecord getColumnBreaksRecord() {
		if (_columnBreaksRecord == null) {
			_columnBreaksRecord = new VerticalPageBreakRecord();
		}
		return _columnBreaksRecord;
	}


	/**
	 * Sets a page break at the indicated column
	 *
	 */
	public void setColumnBreak(short column, short fromRow, short toRow) {
		getColumnBreaksRecord().addBreak(column, fromRow, toRow);
	}

	/**
	 * Removes a page break at the indicated column
	 *
	 */
	public void removeColumnBreak(int column) {
		getColumnBreaksRecord().removeBreak(column);
	}




	public void visitContainedRecords(RecordVisitor rv) {
		visitIfPresent(_rowBreaksRecord, rv);
		visitIfPresent(_columnBreaksRecord, rv);
		visitIfPresent(_header, rv);
		visitIfPresent(_footer, rv);
		visitIfPresent(_hCenter, rv);
		visitIfPresent(_vCenter, rv);
		visitIfPresent(_leftMargin, rv);
		visitIfPresent(_rightMargin, rv);
		visitIfPresent(_topMargin, rv);
		visitIfPresent(_bottomMargin, rv);
		visitIfPresent(_pls, rv);
		visitIfPresent(printSetup, rv);
		visitIfPresent(_bitmap, rv);
	}
	private static void visitIfPresent(Record r, RecordVisitor rv) {
		if (r != null) {
			rv.visitRecord(r);
		}
	}
	private static void visitIfPresent(PageBreakRecord r, RecordVisitor rv) {
		if (r != null) {
			if (r.isEmpty()) {
				// its OK to not serialize empty page break records
				return;
			}
			rv.visitRecord(r);
		}
	}

	/**
	 * creates the HCenter Record and sets it to false (don't horizontally center)
	 */
	private static HCenterRecord createHCenter() {
		HCenterRecord retval = new HCenterRecord();

		retval.setHCenter(false);
		return retval;
	}

	/**
	 * creates the VCenter Record and sets it to false (don't horizontally center)
	*/
	private static VCenterRecord createVCenter() {
		VCenterRecord retval = new VCenterRecord();

		retval.setVCenter(false);
		return retval;
	}

	/**
	 * creates the PrintSetup Record and sets it to defaults and marks it invalid
	 * @see org.apache.poi.hssf.record.PrintSetupRecord
	 * @see org.apache.poi.hssf.record.Record
	 * @return record containing a PrintSetupRecord
	 */
	private static PrintSetupRecord createPrintSetup() {
		PrintSetupRecord retval = new PrintSetupRecord();

		retval.setPaperSize(( short ) 1);
		retval.setScale(( short ) 100);
		retval.setPageStart(( short ) 1);
		retval.setFitWidth(( short ) 1);
		retval.setFitHeight(( short ) 1);
		retval.setOptions(( short ) 2);
		retval.setHResolution(( short ) 300);
		retval.setVResolution(( short ) 300);
		retval.setHeaderMargin( 0.5);
		retval.setFooterMargin( 0.5);
		retval.setCopies(( short ) 0);
		return retval;
	}


	/**
	 * Returns the HeaderRecord.
	 * @return HeaderRecord for the sheet.
	 */
	public HeaderRecord getHeader ()
	{
	return _header;
	}

	/**
	 * Sets the HeaderRecord.
	 * @param newHeader The new HeaderRecord for the sheet.
	 */
	public void setHeader (HeaderRecord newHeader)
	{
		_header = newHeader;
	}

	/**
	 * Returns the FooterRecord.
	 * @return FooterRecord for the sheet.
	 */
	public FooterRecord getFooter ()
	{
		return _footer;
	}

	/**
	 * Sets the FooterRecord.
	 * @param newFooter The new FooterRecord for the sheet.
	 */
	public void setFooter (FooterRecord newFooter)
	{
		_footer = newFooter;
	}

	/**
	 * Returns the PrintSetupRecord.
	 * @return PrintSetupRecord for the sheet.
	 */
	public PrintSetupRecord getPrintSetup ()
	{
		return printSetup;
	}

	/**
	 * Sets the PrintSetupRecord.
	 * @param newPrintSetup The new PrintSetupRecord for the sheet.
	 */
	public void setPrintSetup (PrintSetupRecord newPrintSetup)
	{
		printSetup = newPrintSetup;
	}


	private Margin getMarginRec(int marginIndex) {
		switch (marginIndex) {
			case Sheet.LeftMargin:   return _leftMargin;
			case Sheet.RightMargin:  return _rightMargin;
			case Sheet.TopMargin:    return _topMargin;
			case Sheet.BottomMargin: return _bottomMargin;
		}
		throw new RuntimeException( "Unknown margin constant:  " + marginIndex );
	}


	/**
	 * Gets the size of the margin in inches.
	 * @param margin which margin to get
	 * @return the size of the margin
	 */
   public double getMargin(short margin) {
	   Margin m = getMarginRec(margin);
	   if (m != null) {
		return m.getMargin();
	} else {
	   switch ( margin )
	   {
	   case Sheet.LeftMargin:
		   return .75;
	   case Sheet.RightMargin:
		   return .75;
	   case Sheet.TopMargin:
		   return 1.0;
	   case Sheet.BottomMargin:
		   return 1.0;
	   }
		throw new RuntimeException( "Unknown margin constant:  " + margin );
   }
   }

	/**
	 * Sets the size of the margin in inches.
	 * @param margin which margin to get
	 * @param size the size of the margin
	 */
   public void setMargin(short margin, double size) {
   Margin m = getMarginRec(margin);
   if (m  == null) {
	   switch ( margin )
	   {
	   case Sheet.LeftMargin:
		   _leftMargin = new LeftMarginRecord();
		   m = _leftMargin;
		   break;
	   case Sheet.RightMargin:
		   _rightMargin = new RightMarginRecord();
		   m = _rightMargin;
		   break;
	   case Sheet.TopMargin:
		   _topMargin = new TopMarginRecord();
		   m = _topMargin;
		   break;
	   case Sheet.BottomMargin:
		   _bottomMargin = new BottomMarginRecord();
		   m = _bottomMargin;
		   break;
	   default :
		   throw new RuntimeException( "Unknown margin constant:  " + margin );
	   }
   }
   m.setMargin( size );
   }

	/**
	 * Shifts all the page breaks in the range "count" number of rows/columns
	 * @param breaks The page record to be shifted
	 * @param start Starting "main" value to shift breaks
	 * @param stop Ending "main" value to shift breaks
	 * @param count number of units (rows/columns) to shift by
	 */
	private static void shiftBreaks(PageBreakRecord breaks, int start, int stop, int count) {

		Iterator<PageBreakRecord.Break> iterator = breaks.getBreaksIterator();
		List<PageBreakRecord.Break> shiftedBreak = new ArrayList<PageBreakRecord.Break>();
		while(iterator.hasNext())
		{
			PageBreakRecord.Break breakItem = iterator.next();
			int breakLocation = breakItem.main;
			boolean inStart = (breakLocation >= start);
			boolean inEnd = (breakLocation <= stop);
			if(inStart && inEnd)
				shiftedBreak.add(breakItem);
		}

		iterator = shiftedBreak.iterator();
		while (iterator.hasNext()) {
			PageBreakRecord.Break breakItem = iterator.next();
			breaks.removeBreak(breakItem.main);
			breaks.addBreak((short)(breakItem.main+count), breakItem.subFrom, breakItem.subTo);
		}
	}


	/**
	 * Sets a page break at the indicated row
	 * @param row
	 */
	public void setRowBreak(int row, short fromCol, short toCol) {
		getRowBreaksRecord().addBreak((short)row, fromCol, toCol);
	}

	/**
	 * Removes a page break at the indicated row
	 * @param row
	 */
	public void removeRowBreak(int row) {
		if (getRowBreaksRecord().getBreaks().length < 1)
			throw new IllegalArgumentException("Sheet does not define any row breaks");
		getRowBreaksRecord().removeBreak((short)row);
	}

	/**
	 * Queries if the specified row has a page break
	 * @param row
	 * @return true if the specified row has a page break
	 */
	public boolean isRowBroken(int row) {
		return getRowBreaksRecord().getBreak(row) != null;
	}


	/**
	 * Queries if the specified column has a page break
	 *
	 * @return <code>true</code> if the specified column has a page break
	 */
	public boolean isColumnBroken(int column) {
		return getColumnBreaksRecord().getBreak(column) != null;
	}

	/**
	 * Shifts the horizontal page breaks for the indicated count
	 * @param startingRow
	 * @param endingRow
	 * @param count
	 */
	public void shiftRowBreaks(int startingRow, int endingRow, int count) {
		shiftBreaks(getRowBreaksRecord(), startingRow, endingRow, count);
	}

	/**
	 * Shifts the vertical page breaks for the indicated count
	 * @param startingCol
	 * @param endingCol
	 * @param count
	 */
	public void shiftColumnBreaks(short startingCol, short endingCol, short count) {
		shiftBreaks(getColumnBreaksRecord(), startingCol, endingCol, count);
	}

	/**
	 * @return all the horizontal page breaks, never <code>null</code>
	 */
	public int[] getRowBreaks() {
		return getRowBreaksRecord().getBreaks();
	}

	/**
	 * @return the number of row page breaks
	 */
	public int getNumRowBreaks(){
		return getRowBreaksRecord().getNumBreaks();
	}

	/**
	 * @return all the column page breaks, never <code>null</code>
	 */
	public int[] getColumnBreaks(){
		return getColumnBreaksRecord().getBreaks();
	}

	/**
	 * @return the number of column page breaks
	 */
	public int getNumColumnBreaks(){
		return getColumnBreaksRecord().getNumBreaks();
	}

	public VCenterRecord getVCenter() {
		return _vCenter;
	}

	public HCenterRecord getHCenter() {
		return _hCenter;
	}
}
