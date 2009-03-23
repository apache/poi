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

package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CalcCountRecord;
import org.apache.poi.hssf.record.CalcModeRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DefaultColWidthRecord;
import org.apache.poi.hssf.record.DefaultRowHeightRecord;
import org.apache.poi.hssf.record.DeltaRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.DrawingRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.GridsetRecord;
import org.apache.poi.hssf.record.GutsRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.IterationRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.ObjectProtectRecord;
import org.apache.poi.hssf.record.PaneRecord;
import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.hssf.record.PrintGridlinesRecord;
import org.apache.poi.hssf.record.PrintHeadersRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RefModeRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.SaveRecalcRecord;
import org.apache.poi.hssf.record.ScenarioProtectRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.UncalcedRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.ColumnInfoRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ConditionalFormattingTable;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.aggregates.MergedCellsTable;
import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.hssf.record.aggregates.RecordAggregate;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.PositionTrackingVisitor;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Low level model implementation of a Sheet (one workbook contains many sheets)
 * This file contains the low level binary records starting at the sheets BOF and
 * ending with the sheets EOF.  Use HSSFSheet for a high level representation.
 * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
 * <P>
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org) Gridlines, Headers, Footers, PrintSetup, and Setting Default Column Styles
 * @author Jason Height (jheight at chariot dot net dot au) Clone support. DBCell & Index Record writing support
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little)
 *
 * @see org.apache.poi.hssf.model.Workbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 */
public final class Sheet implements Model {
    public static final short   LeftMargin = 0;
    public static final short   RightMargin = 1;
    public static final short   TopMargin = 2;
    public static final short   BottomMargin = 3;

    private static POILogger            log              = POILogFactory.getLogger(Sheet.class);

    private List<RecordBase>             records;
    protected PrintGridlinesRecord       printGridlines    =     null;
    protected GridsetRecord              gridset           =     null;
    private   GutsRecord                 _gutsRecord;
    protected DefaultColWidthRecord      defaultcolwidth   =     null;
    protected DefaultRowHeightRecord     defaultrowheight  =     null;
    private PageSettingsBlock _psBlock;

    // 'Worksheet Protection Block'
    protected ProtectRecord              protect           =     null;
    protected ObjectProtectRecord        objprotect        =     null;
    protected ScenarioProtectRecord      scenprotect       =     null;
    protected PasswordRecord             password          =     null;

    protected WindowTwoRecord            windowTwo         =     null;
    protected SelectionRecord            selection         =     null;
    /** java object always present, but if empty no BIFF records are written */
    private final MergedCellsTable       _mergedCellsTable;
    /** always present in this POI object, not always written to Excel file */
    /*package*/ColumnInfoRecordsAggregate _columnInfos;
    /** the DimensionsRecord is always present */
    private DimensionsRecord             _dimensions;
    /** always present */
    protected final RowRecordsAggregate        _rowsAggregate;
    private   DataValidityTable          _dataValidityTable=     null;
    private   ConditionalFormattingTable condFormatting;

    private   Iterator                   rowRecIterator    =     null;

    /** Add an UncalcedRecord if not true indicating formulas have not been calculated */
    protected boolean _isUncalced = false;

    public static final byte PANE_LOWER_RIGHT = (byte)0;
    public static final byte PANE_UPPER_RIGHT = (byte)1;
    public static final byte PANE_LOWER_LEFT = (byte)2;
    public static final byte PANE_UPPER_LEFT = (byte)3;

    /**
     * read support  (offset used as starting point for search) for low level
     * API.  Pass in an array of Record objects, the sheet number (0 based) and
     * a record offset (should be the location of the sheets BOF record).  A Sheet
     * object is constructed and passed back with all of its initialization set
     * to the passed in records and references to those records held. This function
     * is normally called via Workbook.
     *
     * @param rs the stream to read records from
     *
     * @return Sheet object with all values set to those read from the file
     *
     * @see org.apache.poi.hssf.model.Workbook
     * @see org.apache.poi.hssf.record.Record
     */
    public static Sheet createSheet(RecordStream rs) {
        return new Sheet(rs);
    }
    private Sheet(RecordStream rs) {
        _mergedCellsTable = new MergedCellsTable();
        RowRecordsAggregate rra = null;

        records            = new ArrayList<RecordBase>(128);
        // TODO - take chart streams off into separate java objects
        int       bofEofNestingLevel = 1;  // nesting level can only get to 2 (when charts are present)
        int dimsloc = -1;

        if (rs.peekNextSid() == BOFRecord.sid) {
            BOFRecord bof = (BOFRecord) rs.getNext();
            if (bof.getType() != BOFRecord.TYPE_WORKSHEET) {
                // TODO - fix junit tests throw new RuntimeException("Bad BOF record type");
            }
            records.add(bof);
        } else {
            throw new RuntimeException("BOF record expected");
        }
        while (rs.hasNext()) {
            int recSid = rs.peekNextSid();

            if ( recSid == CFHeaderRecord.sid ) {
                condFormatting = new ConditionalFormattingTable(rs);
                records.add(condFormatting);
                continue;
            }

            if (recSid == ColumnInfoRecord.sid) {
                _columnInfos = new ColumnInfoRecordsAggregate(rs);
                records.add(_columnInfos);
                continue;
            }
            if ( recSid == DVALRecord.sid) {
                _dataValidityTable = new DataValidityTable(rs);
                records.add(_dataValidityTable);
                continue;
            }

            if (RecordOrderer.isRowBlockRecord(recSid) && bofEofNestingLevel == 1 ) {
                //only add the aggregate once
                if (rra != null) {
                    throw new RuntimeException("row/cell records found in the wrong place");
                }
                RowBlocksReader rbr = new RowBlocksReader(rs);
                _mergedCellsTable.addRecords(rbr.getLooseMergedCells());
                rra = new RowRecordsAggregate(rbr.getPlainRecordStream(), rbr.getSharedFormulaManager());
                records.add(rra); //only add the aggregate once
                continue;
            }

            if (PageSettingsBlock.isComponentRecord(recSid)) {
                PageSettingsBlock psb = new PageSettingsBlock(rs);
                if (_psBlock == null) {
                    _psBlock = psb;
                } else {
                    if (bofEofNestingLevel == 2) {
                        // It's normal for a chart to have its own PageSettingsBlock
                        // Fall through and add psb here, because chart records
                        // are stored loose among the sheet records.
                        // this latest psb does not clash with _psBlock
                    } else if (windowTwo != null) {
                        // probably 'Custom View Settings' sub-stream which is found between
                        // USERSVIEWBEGIN(01AA) and USERSVIEWEND(01AB)
                        // TODO - create UsersViewAggregate to hold these sub-streams, and simplify this code a bit
                        // This happens three times in test sample file "29982.xls"
                        // Also several times in bugzilla samples 46840-23373 and 46840-23374
                        if (recSid == UnknownRecord.HEADER_FOOTER_089C) {
                            _psBlock.addLateHeaderFooter(rs.getNext());
                        } else if (rs.peekNextSid() != UnknownRecord.USERSVIEWEND_01AB) {
                            // not quite the expected situation
                            throw new RuntimeException("two Page Settings Blocks found in the same sheet");
                        }
                    } else {
                            // Some apps write PLS, WSBOOL, <psb> but PLS is part of <psb>
                            // This happens in the test sample file "NoGutsRecords.xls" and "WORKBOOK_in_capitals.xls"
                            // In this case the first PSB is two records back
                            int prevPsbIx = records.size()-2;
                            if (_psBlock != records.get(prevPsbIx) || !(records.get(prevPsbIx+1) instanceof WSBoolRecord)) {
                                // not quite the expected situation
                                throw new RuntimeException("two Page Settings Blocks found in the same sheet");
                            }
                            records.remove(prevPsbIx); // WSBOOL will drop down one position.
                            psb = mergePSBs(_psBlock, psb);
                            _psBlock = psb;
                    }
                }
                records.add(psb);
                continue;
            }

            if (recSid == MergeCellsRecord.sid) {
                // when the MergedCellsTable is found in the right place, we expect those records to be contiguous
                _mergedCellsTable.read(rs);
                continue;
            }

            Record rec = rs.getNext();
            if ( recSid == IndexRecord.sid ) {
                // ignore INDEX record because it is only needed by Excel,
                // and POI always re-calculates its contents
                continue;
            }


            if (recSid == UncalcedRecord.sid) {
                // don't add UncalcedRecord to the list
                _isUncalced = true; // this flag is enough
                continue;
            }

            if (recSid == BOFRecord.sid)
            {
                bofEofNestingLevel++;
                if (log.check( POILogger.DEBUG ))
                    log.log(POILogger.DEBUG, "Hit BOF record. Nesting increased to " + bofEofNestingLevel);
            }
            else if (recSid == EOFRecord.sid)
            {
                --bofEofNestingLevel;
                if (log.check( POILogger.DEBUG ))
                    log.log(POILogger.DEBUG, "Hit EOF record. Nesting decreased to " + bofEofNestingLevel);
                if (bofEofNestingLevel == 0) {
                    records.add(rec);
                    break;
                }
            }
            else if (recSid == DimensionsRecord.sid)
            {
                // Make a columns aggregate if one hasn't ready been created.
                if (_columnInfos == null)
                {
                    _columnInfos = new ColumnInfoRecordsAggregate();
                    records.add(_columnInfos);
                }

                _dimensions    = ( DimensionsRecord ) rec;
                dimsloc = records.size();
            }
            else if (recSid == DefaultColWidthRecord.sid)
            {
                defaultcolwidth = ( DefaultColWidthRecord ) rec;
            }
            else if (recSid == DefaultRowHeightRecord.sid)
            {
                defaultrowheight = ( DefaultRowHeightRecord ) rec;
            }
            else if ( recSid == PrintGridlinesRecord.sid )
            {
                printGridlines = (PrintGridlinesRecord) rec;
            }
            else if ( recSid == GridsetRecord.sid )
            {
                gridset = (GridsetRecord) rec;
            }
            else if ( recSid == SelectionRecord.sid )
            {
                selection = (SelectionRecord) rec;
            }
            else if ( recSid == WindowTwoRecord.sid )
            {
                windowTwo = (WindowTwoRecord) rec;
            }
            else if ( recSid == ProtectRecord.sid )
            {
                protect = (ProtectRecord) rec;
            }
            else if ( recSid == ObjectProtectRecord.sid )
            {
                objprotect = (ObjectProtectRecord) rec;
            }
            else if ( recSid == ScenarioProtectRecord.sid )
            {
                scenprotect = (ScenarioProtectRecord) rec;
            }
            else if ( recSid == PasswordRecord.sid )
            {
                password = (PasswordRecord) rec;
            }

            records.add(rec);
        }
        if (windowTwo == null) {
            throw new RuntimeException("WINDOW2 was not found");
        }
        if (_dimensions == null) {
            // Excel seems to always write the DIMENSION record, but tolerates when it is not present
            // in all cases Excel (2007) adds the missing DIMENSION record
            if (rra == null) {
                // bug 46206 alludes to files which skip the DIMENSION record
                // when there are no row/cell records.
                // Not clear which application wrote these files.
                rra = new RowRecordsAggregate();
            } else {
                log.log(POILogger.WARN, "DIMENSION record not found even though row/cells present");
                // Not sure if any tools write files like this, but Excel reads them OK
            }
            dimsloc = findFirstRecordLocBySid(WindowTwoRecord.sid);
            _dimensions = rra.createDimensions();
            records.add(dimsloc, _dimensions);
        }
        if (rra == null) {
            rra = new RowRecordsAggregate();
            records.add(dimsloc + 1, rra);
        }
        _rowsAggregate = rra;
        // put merged cells table in the right place (regardless of where the first MergedCellsRecord was found */
        RecordOrderer.addNewSheetRecord(records, _mergedCellsTable);
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "sheet createSheet (existing file) exited");
    }
    /**
     * Hack to recover from the situation where the page settings block has been split by
     * an intervening {@link WSBoolRecord}
     */
    private static PageSettingsBlock mergePSBs(PageSettingsBlock a, PageSettingsBlock b) {
        List<Record> temp = new ArrayList<Record>();
        RecordTransferrer rt = new RecordTransferrer(temp);
        a.visitContainedRecords(rt);
        b.visitContainedRecords(rt);
        RecordStream rs = new RecordStream(temp, 0);
        PageSettingsBlock result = new PageSettingsBlock(rs);
        if (rs.hasNext()) {
            throw new RuntimeException("PageSettingsBlocks did not merge properly");
        }
        return result;
    }

    private static final class RecordTransferrer  implements RecordVisitor {

        private final List<Record> _destList;

        public RecordTransferrer(List<Record> destList) {
            _destList = destList;
        }
        public void visitRecord(Record r) {
            _destList.add(r);
        }
    }
    private static final class RecordCloner implements RecordVisitor {

        private final List<RecordBase> _destList;

        public RecordCloner(List<RecordBase> destList) {
            _destList = destList;
        }
        public void visitRecord(Record r) {
            _destList.add((RecordBase)r.clone());
        }
    }

    /**
     * Clones the low level records of this sheet and returns the new sheet instance.
     * This method is implemented by adding methods for deep cloning to all records that
     * can be added to a sheet. The <b>Record</b> object does not implement cloneable.
     * When adding a new record, implement a public clone method if and only if the record
     * belongs to a sheet.
     */
    public Sheet cloneSheet() {
        List<RecordBase> clonedRecords = new ArrayList<RecordBase>(records.size());
        for (int i = 0; i < records.size(); i++) {
            RecordBase rb = records.get(i);
            if (rb instanceof RecordAggregate) {
                ((RecordAggregate) rb).visitContainedRecords(new RecordCloner(clonedRecords));
                continue;
            }
            Record rec = (Record) ((Record) rb).clone();
            clonedRecords.add(rec);
        }
        return createSheet(new RecordStream(clonedRecords, 0));
    }

    /**
     * Creates a sheet with all the usual records minus values and the "index"
     * record (not required).  Sets the location pointer to where the first value
     * records should go.  Use this to create a sheet from "scratch".
     *
     * @return Sheet object with all values set to defaults
     */
    public static Sheet createSheet() {
        return new Sheet();
    }
    private Sheet() {
        _mergedCellsTable = new MergedCellsTable();
        records = new ArrayList<RecordBase>(32);

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch called");

        records.add(createBOF());

        records.add(createCalcMode());
        records.add(createCalcCount() );
        records.add(createRefMode() );
        records.add(createIteration() );
        records.add(createDelta() );
        records.add(createSaveRecalc() );
        records.add(createPrintHeaders() );
        printGridlines = createPrintGridlines();
        records.add( printGridlines );
        gridset = createGridset();
        records.add( gridset );
        _gutsRecord = createGuts();
        records.add( _gutsRecord );
        defaultrowheight = createDefaultRowHeight();
        records.add( defaultrowheight );
        records.add( createWSBool() );

        // 'Page Settings Block'
        _psBlock = new PageSettingsBlock();
        records.add(_psBlock);

        // 'Worksheet Protection Block' (after 'Page Settings Block' and before DEFCOLWIDTH)
        // PROTECT record normally goes here, don't add yet since the flag is initially false

        defaultcolwidth = createDefaultColWidth();
        records.add( defaultcolwidth);
        ColumnInfoRecordsAggregate columns = new ColumnInfoRecordsAggregate();
        records.add( columns );
        _columnInfos = columns;
        _dimensions = createDimensions();
        records.add(_dimensions);
        _rowsAggregate = new RowRecordsAggregate();
        records.add(_rowsAggregate);
        // 'Sheet View Settings'
        records.add(windowTwo = createWindowTwo());
        selection = createSelection();
        records.add(selection);

        records.add(_mergedCellsTable); // MCT comes after 'Sheet View Settings'
        records.add(EOFRecord.instance);

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch exit");
    }

    public RowRecordsAggregate getRowsAggregate() {
        return _rowsAggregate;
    }

    private MergedCellsTable getMergedRecords() {
        // always present
        return _mergedCellsTable;
    }

    /**
     * Updates formulas in cells and conditional formats due to moving of cells
     * @param externSheetIndex the externSheet index of this sheet
     */
    public void updateFormulasAfterCellShift(FormulaShifter shifter, int externSheetIndex) {
        getRowsAggregate().updateFormulasAfterRowShift(shifter, externSheetIndex);
        if (condFormatting != null) {
            getConditionalFormattingTable().updateFormulasAfterCellShift(shifter, externSheetIndex);
        }
        // TODO - adjust data validations
    }

    public int addMergedRegion(int rowFrom, int colFrom, int rowTo, int colTo) {
        // Validate input
        if (rowTo < rowFrom) {
            throw new IllegalArgumentException("The 'to' row (" + rowTo
                    + ") must not be less than the 'from' row (" + rowFrom + ")");
        }
        if (colTo < colFrom) {
            throw new IllegalArgumentException("The 'to' col (" + colTo
                    + ") must not be less than the 'from' col (" + colFrom + ")");
        }

        MergedCellsTable mrt = getMergedRecords();
        mrt.addArea(rowFrom, colFrom, rowTo, colTo);
        return mrt.getNumberOfMergedRegions()-1;
    }

    public void removeMergedRegion(int index)
    {
        //safety checks
        MergedCellsTable mrt = getMergedRecords();
        if (index >= mrt.getNumberOfMergedRegions()) {
            return;
        }
        mrt.remove(index);
    }

    public CellRangeAddress getMergedRegionAt(int index) {
        //safety checks
        MergedCellsTable mrt = getMergedRecords();
        if (index >=  mrt.getNumberOfMergedRegions()) {
            return null;
        }
        return mrt.get(index);
    }

    public int getNumMergedRegions() {
        return getMergedRecords().getNumberOfMergedRegions();
    }
    public ConditionalFormattingTable getConditionalFormattingTable() {
        if (condFormatting == null) {
            condFormatting = new ConditionalFormattingTable();
            RecordOrderer.addNewSheetRecord(records, condFormatting);
        }
        return condFormatting;
    }

    /**
     * Per an earlier reported bug in working with Andy Khan's excel read library.  This
     * sets the values in the sheet's DimensionsRecord object to be correct.  Excel doesn't
     * really care, but we want to play nice with other libraries.
     *
     * @see org.apache.poi.hssf.record.DimensionsRecord
     */
    public void setDimensions(int firstrow, short firstcol, int lastrow,
                              short lastcol)
    {
        if (log.check( POILogger.DEBUG ))
        {
            log.log(POILogger.DEBUG, "Sheet.setDimensions");
            log.log(POILogger.DEBUG,
                    (new StringBuffer("firstrow")).append(firstrow)
                        .append("firstcol").append(firstcol).append("lastrow")
                        .append(lastrow).append("lastcol").append(lastcol)
                        .toString());
        }
        _dimensions.setFirstCol(firstcol);
        _dimensions.setFirstRow(firstrow);
        _dimensions.setLastCol(lastcol);
        _dimensions.setLastRow(lastrow);
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.setDimensions exiting");
    }

    public void visitContainedRecords(RecordVisitor rv, int offset) {

        PositionTrackingVisitor ptv = new PositionTrackingVisitor(rv, offset);

        boolean haveSerializedIndex = false;

        for (int k = 0; k < records.size(); k++) {
            RecordBase record = records.get(k);

            if (record instanceof RecordAggregate) {
                RecordAggregate agg = (RecordAggregate) record;
                agg.visitContainedRecords(ptv);
            } else {
                ptv.visitRecord((Record) record);
            }

            // If the BOF record was just serialized then add the IndexRecord
            if (record instanceof BOFRecord) {
              if (!haveSerializedIndex) {
                haveSerializedIndex = true;
                // Add an optional UncalcedRecord. However, we should add
                //  it in only the once, after the sheet's own BOFRecord.
                // If there are diagrams, they have their own BOFRecords,
                //  and one shouldn't go in after that!
                if (_isUncalced) {
                    ptv.visitRecord(new UncalcedRecord());
                }
                //Can there be more than one BOF for a sheet? If not then we can
                //remove this guard. So be safe it is left here.
                if (_rowsAggregate != null) {
                    // find forward distance to first RowRecord
                    int initRecsSize = getSizeOfInitialSheetRecords(k);
                    int currentPos = ptv.getPosition();
                    ptv.visitRecord(_rowsAggregate.createIndexRecord(currentPos, initRecsSize));
                }
              }
            }
        }
    }
    /**
     * 'initial sheet records' are between INDEX and the 'Row Blocks'
     * @param bofRecordIndex index of record after which INDEX record is to be placed
     * @return count of bytes from end of INDEX record to first ROW record.
     */
    private int getSizeOfInitialSheetRecords(int bofRecordIndex) {

        int result = 0;
        // start just after BOF record (INDEX is not present in this list)
        for (int j = bofRecordIndex + 1; j < records.size(); j++) {
            RecordBase tmpRec = records.get(j);
            if (tmpRec instanceof RowRecordsAggregate) {
                break;
            }
            result += tmpRec.getRecordSize();
        }
        if (_isUncalced) {
            result += UncalcedRecord.getStaticRecordSize();
        }
        return result;
    }

    /**
     * Adds a value record to the sheet's contained binary records
     * (i.e. LabelSSTRecord or NumberRecord).
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     *
     * @param row the row to add the cell value to
     * @param col the cell value record itself.
     */
    public void addValueRecord(int row, CellValueRecordInterface col) {

        if(log.check(POILogger.DEBUG)) {
          log.log(POILogger.DEBUG, "add value record  row" + row);
        }
        DimensionsRecord d = _dimensions;

        if (col.getColumn() > d.getLastCol())
        {
            d.setLastCol(( short ) (col.getColumn() + 1));
        }
        if (col.getColumn() < d.getFirstCol())
        {
            d.setFirstCol(col.getColumn());
        }
        _rowsAggregate.insertCell(col);
    }

    /**
     * remove a value record from the records array.
     *
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     *
     * @param row - the row of the value record you wish to remove
     * @param col - a record supporting the CellValueRecordInterface.
     * @see org.apache.poi.hssf.record.CellValueRecordInterface
     */
    public void removeValueRecord(int row, CellValueRecordInterface col) {

        log.logFormatted(POILogger.DEBUG, "remove value record row %",
                         new int[]{row } );
        _rowsAggregate.removeCell(col);
    }

    /**
     * replace a value record from the records array.
     *
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     *
     * @param newval - a record supporting the CellValueRecordInterface.  this will replace
     *                the cell value with the same row and column.  If there isn't one, one will
     *                be added.
     */

    public void replaceValueRecord(CellValueRecordInterface newval) {

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "replaceValueRecord ");
        //The ValueRecordsAggregate use a tree map underneath.
        //The tree Map uses the CellValueRecordInterface as both the
        //key and the value, if we dont do a remove, then
        //the previous instance of the key is retained, effectively using
        //double the memory
        _rowsAggregate.removeCell(newval);
        _rowsAggregate.insertCell(newval);
    }

    /**
     * Adds a row record to the sheet
     *
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     *
     * @param row the row record to be added
     */

    public void addRow(RowRecord row)
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "addRow ");
        DimensionsRecord d = _dimensions;

        if (row.getRowNumber() >= d.getLastRow())
        {
            d.setLastRow(row.getRowNumber() + 1);
        }
        if (row.getRowNumber() < d.getFirstRow())
        {
            d.setFirstRow(row.getRowNumber());
        }

        //If the row exists remove it, so that any cells attached to the row are removed
        RowRecord existingRow = _rowsAggregate.getRow(row.getRowNumber());
        if (existingRow != null) {
            _rowsAggregate.removeRow(existingRow);
        }

        _rowsAggregate.insertRow(row);

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "exit addRow");
    }

    /**
     * Removes a row record
     *
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     *
     * @param row  the row record to remove
     */
    public void removeRow(RowRecord row) {
        _rowsAggregate.removeRow(row);
    }

    /**
     * get the NEXT value record (from LOC).  The first record that is a value record
     * (starting at LOC) will be returned.
     *
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with,
     * subsequent calls will return values in (physical) sequence or NULL when you get to the end.
     *
     * @return CellValueRecordInterface representing the next value record or NULL if there are no more
     */
    public CellValueRecordInterface[] getValueRecords() {
        return _rowsAggregate.getValueRecords();
    }

    /**
     * get the NEXT RowRecord (from LOC).  The first record that is a Row record
     * (starting at LOC) will be returned.
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with.
     * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
     *
     * @return RowRecord representing the next row record or NULL if there are no more
     */
    public RowRecord getNextRow() {
        if (rowRecIterator == null)
        {
            rowRecIterator = _rowsAggregate.getIterator();
        }
        if (!rowRecIterator.hasNext())
        {
            return null;
        }
        return ( RowRecord ) rowRecIterator.next();
    }

    /**
     * get the NEXT (from LOC) RowRecord where rownumber matches the given rownum.
     * The first record that is a Row record (starting at LOC) that has the
     * same rownum as the given rownum will be returned.
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with.
     * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
     *
     * @param rownum   which row to return (careful with LOC)
     * @return RowRecord representing the next row record or NULL if there are no more
     *
     */
    public RowRecord getRow(int rownum) {
        return _rowsAggregate.getRow(rownum);
    }

    /**
     * creates the BOF record
     */
    /* package */ static BOFRecord createBOF() {
        BOFRecord retval = new BOFRecord();

        retval.setVersion(( short ) 0x600);
        retval.setType(( short ) 0x010);

        retval.setBuild(( short ) 0x0dbb);
        retval.setBuildYear(( short ) 1996);
        retval.setHistoryBitMask(0xc1);
        retval.setRequiredVersion(0x6);
        return retval;
    }

    /**
     * creates the CalcMode record and sets it to 1 (automatic formula caculation)
     */
    private static CalcModeRecord createCalcMode() {
        CalcModeRecord retval = new CalcModeRecord();

        retval.setCalcMode(( short ) 1);
        return retval;
    }

    /**
     * creates the CalcCount record and sets it to 100 (default number of iterations)
     */
    private static CalcCountRecord createCalcCount() {
        CalcCountRecord retval = new CalcCountRecord();

        retval.setIterations(( short ) 100);   // default 100 iterations
        return retval;
    }

    /**
     * creates the RefMode record and sets it to A1 Mode (default reference mode)
     */
    private static RefModeRecord createRefMode() {
        RefModeRecord retval = new RefModeRecord();

        retval.setMode(RefModeRecord.USE_A1_MODE);
        return retval;
    }

    /**
     * creates the Iteration record and sets it to false (don't iteratively calculate formulas)
     */
    private static IterationRecord createIteration() {
        return new IterationRecord(false);
    }

    /**
     * creates the Delta record and sets it to 0.0010 (default accuracy)
     */
    private static DeltaRecord createDelta() {
        return new DeltaRecord(DeltaRecord.DEFAULT_VALUE);
    }

    /**
     * creates the SaveRecalc record and sets it to true (recalculate before saving)
     */
    private static SaveRecalcRecord createSaveRecalc() {
        SaveRecalcRecord retval = new SaveRecalcRecord();

        retval.setRecalc(true);
        return retval;
    }

    /**
     * creates the PrintHeaders record and sets it to false (we don't create headers yet so why print them)
     */
    private static PrintHeadersRecord createPrintHeaders() {
        PrintHeadersRecord retval = new PrintHeadersRecord();

        retval.setPrintHeaders(false);
        return retval;
    }

    /**
     * creates the PrintGridlines record and sets it to false (that makes for ugly sheets).  As far as I can
     * tell this does the same thing as the GridsetRecord
     */
    private static PrintGridlinesRecord createPrintGridlines() {
        PrintGridlinesRecord retval = new PrintGridlinesRecord();

        retval.setPrintGridlines(false);
        return retval;
    }

    /**
     * creates the Gridset record and sets it to true (user has mucked with the gridlines)
     */
    private static GridsetRecord createGridset() {
        GridsetRecord retval = new GridsetRecord();

        retval.setGridset(true);
        return retval;
    }

    /**
     * creates the Guts record and sets leftrow/topcol guttter and rowlevelmax/collevelmax to 0
      */
    private static GutsRecord createGuts() {
        GutsRecord retval = new GutsRecord();

        retval.setLeftRowGutter(( short ) 0);
        retval.setTopColGutter(( short ) 0);
        retval.setRowLevelMax(( short ) 0);
        retval.setColLevelMax(( short ) 0);
        return retval;
    }
    private GutsRecord getGutsRecord() {
        if (_gutsRecord == null) {
            GutsRecord result = createGuts();
            RecordOrderer.addNewSheetRecord(records, result);
            _gutsRecord = result;
        }

        return _gutsRecord;
    }

    /**
     * creates the DefaultRowHeight Record and sets its options to 0 and rowheight to 0xff
     */
    private static DefaultRowHeightRecord createDefaultRowHeight() {
        DefaultRowHeightRecord retval = new DefaultRowHeightRecord();

        retval.setOptionFlags(( short ) 0);
        retval.setRowHeight(( short ) 0xff);
        return retval;
    }

    /**
     * creates the WSBoolRecord and sets its values to defaults
     */
    private static WSBoolRecord createWSBool() {
        WSBoolRecord retval = new WSBoolRecord();

        retval.setWSBool1(( byte ) 0x4);
        retval.setWSBool2(( byte ) 0xffffffc1);
        return retval;
    }


    /**
     * creates the DefaultColWidth Record and sets it to 8
      */
    private static DefaultColWidthRecord createDefaultColWidth() {
        DefaultColWidthRecord retval = new DefaultColWidthRecord();
        retval.setColWidth(( short ) 8);
        return retval;
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width)
     * @return default column width
     */
    public int getDefaultColumnWidth() {
        return defaultcolwidth.getColWidth();
    }

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    public boolean isGridsPrinted()
    {
        if (gridset == null) {
            gridset = createGridset();
            //Insert the newlycreated Gridset record at the end of the record (just before the EOF)
            int loc = findFirstRecordLocBySid(EOFRecord.sid);
            records.add(loc, gridset);
        }
        return !gridset.getGridset();
    }

    /**
     * set whether gridlines printed or not.
     * @param value     True if gridlines printed.
     */

    public void setGridsPrinted(boolean value)
    {
        gridset.setGridset(!value);
    }

    /**
     * set the default column width for the sheet (if the columns do not define their own width)
     * @param dcw  default column width
     */
    public void setDefaultColumnWidth(int dcw) {
        defaultcolwidth.setColWidth(dcw);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height)
     */

    public void setDefaultRowHeight(short dch)
    {
        defaultrowheight.setRowHeight(dch);
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height)
     * @return  default row height
     */

    public short getDefaultRowHeight()
    {
        return defaultrowheight.getRowHeight();
    }

    /**
     * get the width of a given column in units of 1/256th of a character width
     * @param columnIndex index
     * @see org.apache.poi.hssf.record.DefaultColWidthRecord
     * @see org.apache.poi.hssf.record.ColumnInfoRecord
     * @see #setColumnWidth(int, int)
     * @return column width in units of 1/256th of a character width
     */

    public int getColumnWidth(int columnIndex) {

        ColumnInfoRecord ci = _columnInfos.findColumnInfo(columnIndex);
        if (ci != null) {
            return ci.getColumnWidth();
        }
        //default column width is measured in characters
        //multiply
        return (256*defaultcolwidth.getColWidth());
    }

    /**
     * get the index to the ExtendedFormatRecord "associated" with
     * the column at specified 0-based index. (In this case, an
     * ExtendedFormatRecord index is actually associated with a
     * ColumnInfoRecord which spans 1 or more columns)
     * <br/>
     * Returns the index to the default ExtendedFormatRecord (0xF)
     * if no ColumnInfoRecord exists that includes the column
     * index specified.
     * @param columnIndex
     * @return index of ExtendedFormatRecord associated with
     * ColumnInfoRecord that includes the column index or the
     * index of the default ExtendedFormatRecord (0xF)
     */
    public short getXFIndexForColAt(short columnIndex) {
        ColumnInfoRecord ci = _columnInfos.findColumnInfo(columnIndex);
        if (ci != null) {
            return (short)ci.getXFIndex();
        }
        return 0xF;
    }

    /**
     * set the width for a given column in 1/256th of a character width units
     *
     * @param column -
     *            the column number
     * @param width
     *            (in units of 1/256th of a character width)
     */
    public void setColumnWidth(int column, int width) {
        if(width > 255*256) throw new IllegalArgumentException("The maximum column width for an individual cell is 255 characters.");
        
        setColumn(column, null, new Integer(width), null, null, null);
    }

    /**
     * Get the hidden property for a given column.
     * @param columnIndex column index
     * @see org.apache.poi.hssf.record.DefaultColWidthRecord
     * @see org.apache.poi.hssf.record.ColumnInfoRecord
     * @see #setColumnHidden(int, boolean)
     * @return whether the column is hidden or not.
     */
    public boolean isColumnHidden(int columnIndex) {
        ColumnInfoRecord cir = _columnInfos.findColumnInfo(columnIndex);
        if (cir == null) {
            return false;
        }
        return cir.getHidden();
    }

    /**
     * Get the hidden property for a given column.
     * @param column - the column number
     * @param hidden - whether the column is hidden or not
     */
    public void setColumnHidden(int column, boolean hidden) {
        setColumn( column, null, null, null, Boolean.valueOf(hidden), null);
    }
    public void setDefaultColumnStyle(int column, int styleIndex) {
        setColumn(column, new Short((short)styleIndex), null, null, null, null);
    }

    private void setColumn(int column, Short xfStyle, Integer width, Integer level, Boolean hidden, Boolean collapsed) {
        _columnInfos.setColumn( column, xfStyle, width, level, hidden, collapsed );
    }


    /**
     * Creates an outline group for the specified columns.
     * @param fromColumn    group from this column (inclusive)
     * @param toColumn      group to this column (inclusive)
     * @param indent        if true the group will be indented by one level,
     *                      if false indenting will be removed by one level.
     */
    public void groupColumnRange(int fromColumn, int toColumn, boolean indent) {

        // Set the level for each column
        _columnInfos.groupColumnRange( fromColumn, toColumn, indent);

        // Determine the maximum overall level
        int maxLevel = _columnInfos.getMaxOutlineLevel();

        GutsRecord guts = getGutsRecord();
        guts.setColLevelMax( (short) ( maxLevel+1 ) );
        if (maxLevel == 0) {
            guts.setTopColGutter( (short)0 );
        } else {
            guts.setTopColGutter( (short) ( 29 + (12 * (maxLevel-1)) ) );
        }
    }

    /**
     * creates the Dimensions Record and sets it to bogus values (you should set this yourself
     * or let the high level API do it for you)
     */
    private static DimensionsRecord createDimensions() {
        DimensionsRecord retval = new DimensionsRecord();

        retval.setFirstCol(( short ) 0);
        retval.setLastRow(1);             // one more than it is
        retval.setFirstRow(0);
        retval.setLastCol(( short ) 1);   // one more than it is
        return retval;
    }

    /**
     * creates the WindowTwo Record and sets it to:  <P>
     * options        = 0x6b6 <P>
     * toprow         = 0 <P>
     * leftcol        = 0 <P>
     * headercolor    = 0x40 <P>
     * pagebreakzoom  = 0x0 <P>
     * normalzoom     = 0x0 <p>
     */
    private static WindowTwoRecord createWindowTwo() {
        WindowTwoRecord retval = new WindowTwoRecord();

        retval.setOptions(( short ) 0x6b6);
        retval.setTopRow(( short ) 0);
        retval.setLeftCol(( short ) 0);
        retval.setHeaderColor(0x40);
        retval.setPageBreakZoom(( short ) 0);
        retval.setNormalZoom(( short ) 0);
        return retval;
    }

    /**
     * Creates the Selection record and sets it to nothing selected
    */
    private static SelectionRecord createSelection() {
        return new SelectionRecord(0, 0);
    }

    public short getTopRow()
    {
        return (windowTwo==null) ? (short) 0 : windowTwo.getTopRow();
    }

    public void setTopRow(short topRow)
    {
        if (windowTwo!=null)
        {
            windowTwo.setTopRow(topRow);
        }
    }

    /**
     * Sets the left column to show in desktop window pane.
     * @param leftCol the left column to show in desktop window pane
     */
    public void setLeftCol(short leftCol){
        if (windowTwo!=null) {
            windowTwo.setLeftCol(leftCol);
        }
    }

    public short getLeftCol() {
        return (windowTwo==null) ? (short) 0 : windowTwo.getLeftCol();
    }

    /**
     * Returns the active row
     *
     * @see org.apache.poi.hssf.record.SelectionRecord
     * @return row the active row index
     */
    public int getActiveCellRow()
    {
        if (selection == null)
        {
            return 0;
        }
        return selection.getActiveCellRow();
    }

    /**
     * Sets the active row
     *
     * @param row the row index
     * @see org.apache.poi.hssf.record.SelectionRecord
     */
    public void setActiveCellRow(int row)
    {
        //shouldn't have a sheet w/o a SelectionRecord, but best to guard anyway
        if (selection != null)
        {
            selection.setActiveCellRow(row);
        }
    }

    /**
     * @see org.apache.poi.hssf.record.SelectionRecord
     * @return column of the active cell
     */
    public short getActiveCellCol() {
        if (selection == null) {
            return 0;
        }
        return (short)selection.getActiveCellCol();
    }

    /**
     * Sets the active column
     *
     * @param col the column index
     * @see org.apache.poi.hssf.record.SelectionRecord
     */
    public void setActiveCellCol(short col)
    {
        //shouldn't have a sheet w/o a SelectionRecord, but best to guard anyway
        if (selection != null)
        {
            selection.setActiveCellCol(col);
        }
    }

    public List<RecordBase> getRecords()
    {
        return records;
    }

    /**
     * Gets the gridset record for this sheet.
     */

    public GridsetRecord getGridsetRecord()
    {
        return gridset;
    }

    /**
     * Returns the first occurrence of a record matching a particular sid.
     */

    public Record findFirstRecordBySid(short sid)
    {
        int ix = findFirstRecordLocBySid(sid);
        if (ix < 0) {
            return null;
        }
        return (Record) records.get(ix);
    }

    /**
     * Sets the SCL record or creates it in the correct place if it does not
     * already exist.
     *
     * @param sclRecord     The record to set.
     */
    public void setSCLRecord(SCLRecord sclRecord)
    {
        int oldRecordLoc = findFirstRecordLocBySid(SCLRecord.sid);
        if (oldRecordLoc == -1)
        {
            // Insert it after the window record
            int windowRecordLoc = findFirstRecordLocBySid(WindowTwoRecord.sid);
            records.add(windowRecordLoc+1, sclRecord);
        }
        else
        {
            records.set(oldRecordLoc, sclRecord);
        }

    }

    /**
     * Finds the first occurrence of a record matching a particular sid and
     * returns it's position.
     * @param sid   the sid to search for
     * @return  the record position of the matching record or -1 if no match
     *          is made.
     */
    public int findFirstRecordLocBySid( short sid ) { // TODO - remove this method
        int max = records.size();
        for (int i=0; i< max; i++) {
            Object rb = records.get(i);
            if (!(rb instanceof Record)) {
                continue;
            }
            Record record = (Record) rb;
            if (record.getSid() == sid) {
                return i;
            }
        }
        return -1;
    }

    public WindowTwoRecord getWindowTwo() {
        return windowTwo;
    }

    /**
     * Returns the PrintGridlinesRecord.
     * @return PrintGridlinesRecord for the sheet.
     */
    public PrintGridlinesRecord getPrintGridlines ()
    {
        return printGridlines;
    }

    /**
     * Sets the PrintGridlinesRecord.
     * @param newPrintGridlines The new PrintGridlinesRecord for the sheet.
     */
    public void setPrintGridlines (PrintGridlinesRecord newPrintGridlines)
    {
        printGridlines = newPrintGridlines;
    }

    /**
     * Sets whether the sheet is selected
     * @param sel True to select the sheet, false otherwise.
     */
    public void setSelected(boolean sel) {
        windowTwo.setSelected(sel);
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int topRow, int leftmostColumn )
    {
        int paneLoc = findFirstRecordLocBySid(PaneRecord.sid);
        if (paneLoc != -1)
            records.remove(paneLoc);

        int loc = findFirstRecordLocBySid(WindowTwoRecord.sid);
        PaneRecord pane = new PaneRecord();
        pane.setX((short)colSplit);
        pane.setY((short)rowSplit);
        pane.setTopRow((short) topRow);
        pane.setLeftColumn((short) leftmostColumn);
        if (rowSplit == 0)
        {
            pane.setTopRow((short)0);
            pane.setActivePane((short)1);
        }
        else if (colSplit == 0)
        {
            pane.setLeftColumn((short)64);
            pane.setActivePane((short)2);
        }
        else
        {
            pane.setActivePane((short)0);
        }
        records.add(loc+1, pane);

        windowTwo.setFreezePanes(true);
        windowTwo.setFreezePanesNoSplit(true);

        SelectionRecord sel = (SelectionRecord) findFirstRecordBySid(SelectionRecord.sid);
        sel.setPane((byte)pane.getActivePane());

    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     *                      PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
    public void createSplitPane(int xSplitPos, int ySplitPos, int topRow, int leftmostColumn, int activePane )
    {
        int paneLoc = findFirstRecordLocBySid(PaneRecord.sid);
        if (paneLoc != -1)
            records.remove(paneLoc);

        int loc = findFirstRecordLocBySid(WindowTwoRecord.sid);
        PaneRecord r = new PaneRecord();
        r.setX((short)xSplitPos);
        r.setY((short)ySplitPos);
        r.setTopRow((short) topRow);
        r.setLeftColumn((short) leftmostColumn);
        r.setActivePane((short) activePane);
        records.add(loc+1, r);

        windowTwo.setFreezePanes(false);
        windowTwo.setFreezePanesNoSplit(false);

        SelectionRecord sel = (SelectionRecord) findFirstRecordBySid(SelectionRecord.sid);
        sel.setPane(PANE_LOWER_RIGHT);

    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     * @return null if no pane configured, or the pane information.
     */
    public PaneInformation getPaneInformation() {
      PaneRecord rec = (PaneRecord)findFirstRecordBySid(PaneRecord.sid);
      if (rec == null)
        return null;

      return new PaneInformation(rec.getX(), rec.getY(), rec.getTopRow(),
                                 rec.getLeftColumn(), (byte)rec.getActivePane(), windowTwo.getFreezePanes());
    }

    public SelectionRecord getSelection()
    {
        return selection;
    }

    public void setSelection( SelectionRecord selection )
    {
        this.selection = selection;
    }

    /**
     * creates an ObjectProtect record with protect set to false.
     */
    private static ObjectProtectRecord createObjectProtect() {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "create protect record with protection disabled");
        ObjectProtectRecord retval = new ObjectProtectRecord();

        retval.setProtect(false);
        return retval;
    }

    /**
     * creates a ScenarioProtect record with protect set to false.
     */
    private static ScenarioProtectRecord createScenarioProtect() {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "create protect record with protection disabled");
        ScenarioProtectRecord retval = new ScenarioProtectRecord();

        retval.setProtect(false);
        return retval;
    }

    /** Returns the ProtectRecord.
     * If one is not contained in the sheet, then one is created.
     */
    public ProtectRecord getProtect()
    {
        if (protect == null) {
            protect = new ProtectRecord(false);
            // Insert the newly created protect record just before DefaultColWidthRecord
            int loc = findFirstRecordLocBySid(DefaultColWidthRecord.sid);
            records.add(loc, protect);
        }
        return protect;
    }

    /** Returns the PasswordRecord.
     * If one is not contained in the sheet, then one is created.
     */
    public PasswordRecord getPassword()
    {
        if (password == null) {
            password = createPassword();
            //Insert the newly created password record at the end of the record (just before the EOF)
            int loc = findFirstRecordLocBySid(EOFRecord.sid);
            records.add(loc, password);
        }
        return password;
    }

    /**
     * creates a Password record with password set to 0x0000.
     */
    private static PasswordRecord createPassword() {
        return new PasswordRecord(0x0000);
    }

    /**

    /**
     * Sets whether the gridlines are shown in a viewer.
     * @param show whether to show gridlines or not
     */
    public void setDisplayGridlines(boolean show) {
        windowTwo.setDisplayGridlines(show);
    }

    /**
     * Returns if gridlines are displayed.
     * @return whether gridlines are displayed
     */
    public boolean isDisplayGridlines() {
    return windowTwo.getDisplayGridlines();
    }

    /**
     * Sets whether the formulas are shown in a viewer.
     * @param show whether to show formulas or not
     */
    public void setDisplayFormulas(boolean show) {
        windowTwo.setDisplayFormulas(show);
    }

    /**
     * Returns if formulas are displayed.
     * @return whether formulas are displayed
     */
    public boolean isDisplayFormulas() {
    return windowTwo.getDisplayFormulas();
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
     * @param show whether to show RowColHeadings or not
     */
    public void setDisplayRowColHeadings(boolean show) {
        windowTwo.setDisplayRowColHeadings(show);
    }

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    public boolean isDisplayRowColHeadings() {
        return windowTwo.getDisplayRowColHeadings();
    }


    /**
     * @return whether an uncalced record must be inserted or not at generation
     */
    public boolean getUncalced() {
        return _isUncalced;
    }
    /**
     * @param uncalced whether an uncalced record must be inserted or not at generation
     */
    public void setUncalced(boolean uncalced) {
        this._isUncalced = uncalced;
    }

    /**
     * Finds the DrawingRecord for our sheet, and
     *  attaches it to the DrawingManager (which knows about
     *  the overall DrawingGroup for our workbook).
     * If requested, will create a new DrawRecord
     *  if none currently exist
     * @param drawingManager The DrawingManager2 for our workbook
     * @param createIfMissing Should one be created if missing?
     */
    public int aggregateDrawingRecords(DrawingManager2 drawingManager, boolean createIfMissing)
    {
        int loc = findFirstRecordLocBySid(DrawingRecord.sid);
        boolean noDrawingRecordsFound = (loc == -1);
        if (noDrawingRecordsFound)
        {
            if(!createIfMissing) {
                // None found, and not allowed to add in
                return -1;
            }

            EscherAggregate aggregate = new EscherAggregate( drawingManager );
            loc = findFirstRecordLocBySid(EscherAggregate.sid);
            if (loc == -1)
            {
                loc = findFirstRecordLocBySid( WindowTwoRecord.sid );
            }
            else
            {
                getRecords().remove(loc);
            }
            getRecords().add( loc, aggregate );
            return loc;
        }
        else
        {
            List<RecordBase> records = getRecords();
            EscherAggregate r = EscherAggregate.createAggregate( records, loc, drawingManager );
            int startloc = loc;
            while ( loc + 1 < records.size()
                    && records.get( loc ) instanceof DrawingRecord
                    && records.get( loc + 1 ) instanceof ObjRecord )
            {
                loc += 2;
            }
            int endloc = loc-1;
            for(int i = 0; i < (endloc - startloc + 1); i++)
                records.remove(startloc);
            records.add(startloc, r);

            return startloc;
        }
    }

    /**
     * Perform any work necessary before the sheet is about to be serialized.
     * For instance the escher aggregates size needs to be calculated before
     * serialization so that the dgg record (which occurs first) can be written.
     */
    public void preSerialize()
    {
        for ( Iterator iterator = getRecords().iterator(); iterator.hasNext(); )
        {
            RecordBase r = (RecordBase) iterator.next();
            if (r instanceof EscherAggregate)
                r.getRecordSize();   // Trigger flatterning of user model and corresponding update of dgg record.
        }
    }


    public PageSettingsBlock getPageSettings() {
        if (_psBlock == null) {
            _psBlock = new PageSettingsBlock();
            RecordOrderer.addNewSheetRecord(records, _psBlock);
        }
        return _psBlock;
    }


    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        if (collapsed) {
            _columnInfos.collapseColumn(columnNumber);
        } else {
            _columnInfos.expandColumn(columnNumber);
        }
    }

    /**
     * protect a spreadsheet with a password (not encypted, just sets protect
     * flags and the password.
     * @param password to set
     * @param objects are protected
     * @param scenarios are protected
     */
    public void protectSheet( String password, boolean objects, boolean scenarios ) {
        int protIdx = -1;
        ProtectRecord prec = getProtect();
        PasswordRecord pass = getPassword();
        prec.setProtect(true);
        pass.setPassword(PasswordRecord.hashPassword(password));
        if((objprotect == null && objects) || (scenprotect != null && scenarios)) {
            protIdx = records.indexOf( protect );
        }
        if(objprotect == null && objects) {
            ObjectProtectRecord rec = createObjectProtect();
            rec.setProtect(true);
            records.add(protIdx+1,rec);
            objprotect = rec;
        }
        if(scenprotect == null && scenarios) {
            ScenarioProtectRecord srec = createScenarioProtect();
            srec.setProtect(true);
            records.add(protIdx+2,srec);
            scenprotect = srec;
        }
    }

    /**
     * unprotect objects in the sheet (will not protect them, but any set to false are
     * unprotected.
     * @param sheet is unprotected (false = unprotect)
     * @param objects are unprotected (false = unprotect)
     * @param scenarios are unprotected (false = unprotect)
     */
    public void unprotectSheet( boolean sheet, boolean objects, boolean scenarios ) {

        if (!sheet) {
           ProtectRecord prec = getProtect();
           prec.setProtect(sheet);
           PasswordRecord pass = getPassword();
           pass.setPassword((short)00);
        }
        if(objprotect != null && !objects) {
            objprotect.setProtect(false);
        }
        if(scenprotect != null && !scenarios) {
            scenprotect.setProtect(false);
        }
    }

    /**
     * @return {sheet is protected, objects are proteced, scenarios are protected}
     */
    public boolean[] isProtected() {
        return new boolean[] { (protect != null && protect.getProtect()),
                             (objprotect != null && objprotect.getProtect()),
                             (scenprotect != null && scenprotect.getProtect())};
    }


    public void groupRowRange(int fromRow, int toRow, boolean indent)
    {
        for (int rowNum = fromRow; rowNum <= toRow; rowNum++)
        {
            RowRecord row = getRow( rowNum );
            if (row == null)
            {
                row = RowRecordsAggregate.createRow(rowNum);
                addRow( row );
            }
            int level = row.getOutlineLevel();
            if (indent) level++; else level--;
            level = Math.max(0, level);
            level = Math.min(7, level);
            row.setOutlineLevel((short) ( level ));
        }

        recalcRowGutter();
    }

    private void recalcRowGutter()
    {
        int maxLevel = 0;
        Iterator iterator = _rowsAggregate.getIterator();
        while ( iterator.hasNext() )
        {
            RowRecord rowRecord = (RowRecord) iterator.next();
            maxLevel = Math.max(rowRecord.getOutlineLevel(), maxLevel);
        }

        // Grab the guts record, adding if needed
        GutsRecord guts = getGutsRecord();
        // Set the levels onto it
        guts.setRowLevelMax( (short) ( maxLevel + 1 ) );
        guts.setLeftRowGutter( (short) ( 29 + (12 * (maxLevel)) ) );
    }

    public DataValidityTable getOrCreateDataValidityTable() {
        if (_dataValidityTable == null) {
            DataValidityTable result = new DataValidityTable();
            RecordOrderer.addNewSheetRecord(records, result);
            _dataValidityTable = result;
        }
        return _dataValidityTable;
    }
    /**
     * Get the {@link NoteRecord}s (related to cell comments) for this sheet
     * @return never <code>null</code>, typically empty array
     */
    public NoteRecord[] getNoteRecords() {
        List<NoteRecord> temp = new ArrayList<NoteRecord>();
        for(int i=records.size()-1; i>=0; i--) {
            RecordBase rec = records.get(i);
            if (rec instanceof NoteRecord) {
                temp.add((NoteRecord) rec);
            }
        }
        if (temp.size() < 1) {
            return NoteRecord.EMPTY_ARRAY;
        }
        NoteRecord[] result = new NoteRecord[temp.size()];
        temp.toArray(result);
        return result;
    }
}
