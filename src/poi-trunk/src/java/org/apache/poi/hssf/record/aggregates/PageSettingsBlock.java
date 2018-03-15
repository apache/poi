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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.*;
import org.apache.poi.util.HexDump;

/**
 * Groups the page settings records for a worksheet.<p>
 *
 * See OOO excelfileformat.pdf sec 4.4 'Page Settings Block'
 */
public final class PageSettingsBlock extends RecordAggregate {

    /**
     * PLS is potentially a <i>continued</i> record, but is currently uninterpreted by POI
     */
    private static final class PLSAggregate extends RecordAggregate {
        private static final ContinueRecord[] EMPTY_CONTINUE_RECORD_ARRAY = { };
        private final Record _pls;
        /**
         * holds any continue records found after the PLS record.<br>
         * This would not be required if PLS was properly interpreted.
         * Currently, PLS is an {@link UnknownRecord} and does not automatically
         * include any trailing {@link ContinueRecord}s.
         */
        private ContinueRecord[] _plsContinues;

        public PLSAggregate(RecordStream rs) {
            _pls = rs.getNext();
            if (rs.peekNextSid()==ContinueRecord.sid) {
                List<ContinueRecord> temp = new ArrayList<>();
                while (rs.peekNextSid()==ContinueRecord.sid) {
                    temp.add((ContinueRecord)rs.getNext());
                }
                _plsContinues = new ContinueRecord[temp.size()];
                temp.toArray(_plsContinues);
            } else {
                _plsContinues = EMPTY_CONTINUE_RECORD_ARRAY;
            }
        }

        @Override
        public void visitContainedRecords(RecordVisitor rv) {
            rv.visitRecord(_pls);
            for (ContinueRecord _plsContinue : _plsContinues) {
                rv.visitRecord(_plsContinue);
            }
        }
    }

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
    private final List<PLSAggregate> _plsRecords;
    private PrintSetupRecord _printSetup;
    private Record _bitmap;
    private HeaderFooterRecord _headerFooter;
    /**
     * HeaderFooterRecord records belonging to preceding CustomViewSettingsRecordAggregates.
     * The indicator of such records is a non-zero GUID,
     *  see {@link  org.apache.poi.hssf.record.HeaderFooterRecord#getGuid()}
     */
    private final List<HeaderFooterRecord> _sviewHeaderFooters = new ArrayList<>();
    private Record _printSize;

    public PageSettingsBlock(RecordStream rs) {
        _plsRecords = new ArrayList<>();
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
        _plsRecords = new ArrayList<>();
        _rowBreaksRecord = new HorizontalPageBreakRecord();
        _columnBreaksRecord = new VerticalPageBreakRecord();
        _header = new HeaderRecord("");
        _footer = new FooterRecord("");
        _hCenter = createHCenter();
        _vCenter = createVCenter();
        _printSetup = createPrintSetup();
    }

    /**
     * @param sid the record sid
     * 
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
            case UnknownRecord.PRINTSIZE_0033:
            case HeaderFooterRecord.sid: // extra header/footer settings supported by Excel 2007
                return true;
        }
        return false;
    }

    private boolean readARecord(RecordStream rs) {
        switch (rs.peekNextSid()) {
            case HorizontalPageBreakRecord.sid:
                checkNotPresent(_rowBreaksRecord);
                _rowBreaksRecord = (PageBreakRecord) rs.getNext();
                break;
            case VerticalPageBreakRecord.sid:
                checkNotPresent(_columnBreaksRecord);
                _columnBreaksRecord = (PageBreakRecord) rs.getNext();
                break;
            case HeaderRecord.sid:
                checkNotPresent(_header);
                _header = (HeaderRecord) rs.getNext();
                break;
            case FooterRecord.sid:
                checkNotPresent(_footer);
                _footer = (FooterRecord) rs.getNext();
                break;
            case HCenterRecord.sid:
                checkNotPresent(_hCenter);
                _hCenter = (HCenterRecord) rs.getNext();
                break;
            case VCenterRecord.sid:
                checkNotPresent(_vCenter);
                _vCenter = (VCenterRecord) rs.getNext();
                break;
            case LeftMarginRecord.sid:
                checkNotPresent(_leftMargin);
                _leftMargin = (LeftMarginRecord) rs.getNext();
                break;
            case RightMarginRecord.sid:
                checkNotPresent(_rightMargin);
                _rightMargin = (RightMarginRecord) rs.getNext();
                break;
            case TopMarginRecord.sid:
                checkNotPresent(_topMargin);
                _topMargin = (TopMarginRecord) rs.getNext();
                break;
            case BottomMarginRecord.sid:
                checkNotPresent(_bottomMargin);
                _bottomMargin = (BottomMarginRecord) rs.getNext();
                break;
            case UnknownRecord.PLS_004D:
                _plsRecords.add(new PLSAggregate(rs));
                break;
            case PrintSetupRecord.sid:
                checkNotPresent(_printSetup);
                _printSetup = (PrintSetupRecord)rs.getNext();
                break;
            case UnknownRecord.BITMAP_00E9:
                checkNotPresent(_bitmap);
                _bitmap = rs.getNext();
                break;
            case UnknownRecord.PRINTSIZE_0033:
                checkNotPresent(_printSize);
                _printSize = rs.getNext();
                break;
            case HeaderFooterRecord.sid:
                //there can be multiple HeaderFooterRecord records belonging to different sheet views
                HeaderFooterRecord hf = (HeaderFooterRecord)rs.getNext();
                if(hf.isCurrentSheet()) {
                    _headerFooter = hf;
                } else {
                    _sviewHeaderFooters.add(hf);
                }
                break;
            default:
                // all other record types are not part of the PageSettingsBlock
                return false;
        }
        return true;
    }

    private void checkNotPresent(Record rec) {
        if (rec != null) {
            throw new org.apache.poi.util.RecordFormatException("Duplicate PageSettingsBlock record (sid=0x"
                    + Integer.toHexString(rec.getSid()) + ")");
        }
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
     * @param column the column to add page breaks to
     * @param fromRow the starting row
     * @param toRow the ending row
     */
    public void setColumnBreak(short column, short fromRow, short toRow) {
        getColumnBreaksRecord().addBreak(column, fromRow, toRow);
    }

    /**
     * Removes a page break at the indicated column
     *
     * @param column the column to check for page breaks
     */
    public void removeColumnBreak(int column) {
        getColumnBreaksRecord().removeBreak(column);
    }




    @Override
    public void visitContainedRecords(RecordVisitor rv) {
        // Replicates record order from Excel 2007, though this is not critical

        visitIfPresent(_rowBreaksRecord, rv);
        visitIfPresent(_columnBreaksRecord, rv);
        // Write out empty header / footer records if these are missing
        if (_header == null) {
            rv.visitRecord(new HeaderRecord(""));
        } else {
            rv.visitRecord(_header);
        }
        if (_footer == null) {
            rv.visitRecord(new FooterRecord(""));
        } else {
            rv.visitRecord(_footer);
        }
        visitIfPresent(_hCenter, rv);
        visitIfPresent(_vCenter, rv);
        visitIfPresent(_leftMargin, rv);
        visitIfPresent(_rightMargin, rv);
        visitIfPresent(_topMargin, rv);
        visitIfPresent(_bottomMargin, rv);
        for (RecordAggregate pls : _plsRecords) {
            pls.visitContainedRecords(rv);
        }
        visitIfPresent(_printSetup, rv);
        visitIfPresent(_printSize, rv);
        visitIfPresent(_headerFooter, rv);
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
        retval.setCopies(( short ) 1);
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
        return _printSetup;
    }

    /**
     * Sets the PrintSetupRecord.
     * @param newPrintSetup The new PrintSetupRecord for the sheet.
     */
    public void setPrintSetup (PrintSetupRecord newPrintSetup)
    {
        _printSetup = newPrintSetup;
    }


    private Margin getMarginRec(int marginIndex) {
        switch (marginIndex) {
            case InternalSheet.LeftMargin:   return _leftMargin;
            case InternalSheet.RightMargin:  return _rightMargin;
            case InternalSheet.TopMargin:    return _topMargin;
            case InternalSheet.BottomMargin: return _bottomMargin;
        }
        throw new IllegalArgumentException( "Unknown margin constant:  " + marginIndex );
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
        }
        switch (margin) {
            case InternalSheet.LeftMargin:   return .75;
            case InternalSheet.RightMargin:  return .75;
            case InternalSheet.TopMargin:    return 1.0;
            case InternalSheet.BottomMargin: return 1.0;
        }
        throw new IllegalArgumentException( "Unknown margin constant:  " + margin );
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    public void setMargin(short margin, double size) {
        Margin m = getMarginRec(margin);
        if (m  == null) {
            switch (margin) {
                case InternalSheet.LeftMargin:
                    _leftMargin = new LeftMarginRecord();
                    m = _leftMargin;
                    break;
                case InternalSheet.RightMargin:
                    _rightMargin = new RightMarginRecord();
                    m = _rightMargin;
                    break;
                case InternalSheet.TopMargin:
                    _topMargin = new TopMarginRecord();
                    m = _topMargin;
                    break;
                case InternalSheet.BottomMargin:
                    _bottomMargin = new BottomMarginRecord();
                    m = _bottomMargin;
                    break;
                default :
                    throw new IllegalArgumentException( "Unknown margin constant:  " + margin );
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
        List<PageBreakRecord.Break> shiftedBreak = new ArrayList<>();
        while(iterator.hasNext())
        {
            PageBreakRecord.Break breakItem = iterator.next();
            int breakLocation = breakItem.main;
            boolean inStart = (breakLocation >= start);
            boolean inEnd = (breakLocation <= stop);
            if(inStart && inEnd) {
                shiftedBreak.add(breakItem);
            }
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
     * @param row the row
     * @param fromCol the starting column
     * @param toCol the ending column
     */
    public void setRowBreak(int row, short fromCol, short toCol) {
        getRowBreaksRecord().addBreak((short)row, fromCol, toCol);
    }

    /**
     * Removes a page break at the indicated row
     * @param row the row
     */
    public void removeRowBreak(int row) {
        if (getRowBreaksRecord().getBreaks().length < 1) {
            throw new IllegalArgumentException("Sheet does not define any row breaks");
        }
        getRowBreaksRecord().removeBreak((short)row);
    }

    /**
     * Queries if the specified row has a page break
     * 
     * @param row the row to check for
     * 
     * @return true if the specified row has a page break
     */
    public boolean isRowBroken(int row) {
        return getRowBreaksRecord().getBreak(row) != null;
    }


    /**
     * Queries if the specified column has a page break
     * 
     * @param column the column to check for
     *
     * @return <code>true</code> if the specified column has a page break
     */
    public boolean isColumnBroken(int column) {
        return getColumnBreaksRecord().getBreak(column) != null;
    }

    /**
     * Shifts the horizontal page breaks for the indicated count
     * @param startingRow the starting row
     * @param endingRow the ending row
     * @param count the number of rows to shift by
     */
    public void shiftRowBreaks(int startingRow, int endingRow, int count) {
        shiftBreaks(getRowBreaksRecord(), startingRow, endingRow, count);
    }

    /**
     * Shifts the vertical page breaks for the indicated count
     * @param startingCol the starting column
     * @param endingCol the ending column
     * @param count the number of columns to shift by
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

    /**
     * HEADERFOOTER is new in 2007.  Some apps seem to have scattered this record long after
     * the {@link PageSettingsBlock} where it belongs.
     * 
     * @param rec the HeaderFooterRecord to set
     */
    public void addLateHeaderFooter(HeaderFooterRecord rec) {
        if (_headerFooter != null) {
            throw new IllegalStateException("This page settings block already has a header/footer record");
        }
        if (rec.getSid() != HeaderFooterRecord.sid) {
            throw new org.apache.poi.util.RecordFormatException("Unexpected header-footer record sid: 0x" + Integer.toHexString(rec.getSid()));
        }
        _headerFooter = rec;
    }

    /**
     * This method reads PageSettingsBlock records from the supplied RecordStream until the first
     * non-PageSettingsBlock record is encountered.  As each record is read, it is incorporated
     * into this PageSettingsBlock.<p>
     * 
     * The latest Excel version seems to write the PageSettingsBlock uninterrupted. However there
     * are several examples (that Excel reads OK) where these records are not written together:
     * 
     * <ul>
     * <li><b>HEADER_FOOTER(0x089C) after WINDOW2</b> - This record is new in 2007.  Some apps
     * seem to have scattered this record long after the PageSettingsBlock where it belongs
     * test samples: SharedFormulaTest.xls, ex44921-21902.xls, ex42570-20305.xls</li>
     * <li><b>PLS, WSBOOL, PageSettingsBlock</b> - WSBOOL is not a PSB record.
     * This happens in the test sample file "NoGutsRecords.xls" and "WORKBOOK_in_capitals.xls"</li>
     * <li><b>Margins after DIMENSION</b> - All of PSB should be before DIMENSION. (Bug-47199)</li>
     * </ul>
     * 
     * These were probably written by other applications (or earlier versions of Excel). It was
     * decided to not write specific code for detecting each of these cases.  POI now tolerates
     * PageSettingsBlock records scattered all over the sheet record stream, and in any order, but
     * does not allow duplicates of any of those records.<p>
     *
     * <b>Note</b> - when POI writes out this PageSettingsBlock, the records will always be written
     * in one consolidated block (in the standard ordering) regardless of how scattered the records
     * were when they were originally read.
     *
     * @param rs the RecordStream to read from
     * 
     * @throws  org.apache.poi.util.RecordFormatException if any PSB record encountered has the same type (sid) as
     * a record that is already part of this PageSettingsBlock
     */
    public void addLateRecords(RecordStream rs) {
        while(true) {
            if (!readARecord(rs)) {
                break;
            }
        }
    }

    /**
     * Some apps can define multiple HeaderFooterRecord records for a sheet.
     * When saving such a file Excel 2007 re-positions them according to the following rules:
     *  - take a HeaderFooterRecord and read 16-byte GUID at offset 12. If it is zero,
     *    it means the current sheet and the given HeaderFooterRecord belongs to this PageSettingsBlock
     *  - If GUID is not zero then search in preceding CustomViewSettingsRecordAggregates.
     *    Compare first 16 bytes of UserSViewBegin with the HeaderFooterRecord's GUID. If match,
     *    then append the HeaderFooterRecord to this CustomViewSettingsRecordAggregates
     *
     * @param sheetRecords the list of sheet records read so far
     */
    public void positionRecords(List<RecordBase> sheetRecords) {
        // Take a copy to loop over, so we can update the real one
        //  without concurrency issues
        List<HeaderFooterRecord> hfRecordsToIterate = new ArrayList<>(_sviewHeaderFooters);

        final Map<String, HeaderFooterRecord> hfGuidMap = new HashMap<>();

        for(final HeaderFooterRecord hf : hfRecordsToIterate) {
            hfGuidMap.put(HexDump.toHex(hf.getGuid()), hf);
        }

        // loop through HeaderFooterRecord records having not-empty GUID and match them with
        // CustomViewSettingsRecordAggregate blocks having UserSViewBegin with the same GUID
        for (RecordBase rb : sheetRecords) {
            if (rb instanceof CustomViewSettingsRecordAggregate) {
                final CustomViewSettingsRecordAggregate cv = (CustomViewSettingsRecordAggregate) rb;
                cv.visitContainedRecords(new RecordVisitor() {
                    @Override
                    public void visitRecord(Record r) {
                        if (r.getSid() == UserSViewBegin.sid) {
                            String guid = HexDump.toHex(((UserSViewBegin) r).getGuid());
                            HeaderFooterRecord hf = hfGuidMap.get(guid);

                            if (hf != null) {
                                cv.append(hf);
                                _sviewHeaderFooters.remove(hf);
                            }
                        }
                    }
                });
            }
        }
    }
}
