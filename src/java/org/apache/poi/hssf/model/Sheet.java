
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.model;

import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ColumnInfoRecordsAggregate;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.IntList;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * @author  Shawn Laubach (slaubach at apache dot org) Gridlines, Headers, Footers, and PrintSetup
 * @author Jason Height (jheight at chariot dot net dot au) Clone support
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 *
 * @see org.apache.poi.hssf.model.Workbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 * @version 1.0-pre
 */

public class Sheet implements Model
{
    public static final short   LeftMargin = 0;
    public static final short   RightMargin = 1;
    public static final short   TopMargin = 2;
    public static final short   BottomMargin = 3;

    private static POILogger            log              = POILogFactory.getLogger(Sheet.class);

    protected ArrayList                  records           =     null;
              int                        preoffset         =     0;            // offset of the sheet in a new file
              int                        loc               =     0;
    protected boolean                    containsLabels    =     false;
    protected int                        dimsloc           =     0;
    protected DimensionsRecord           dims;
    protected DefaultColWidthRecord      defaultcolwidth   =     null;
    protected DefaultRowHeightRecord     defaultrowheight  =     null;
    protected GridsetRecord              gridset           =     null;
    protected PrintSetupRecord           printSetup        =     null;
    protected HeaderRecord               header            =     null;
    protected FooterRecord               footer            =     null;
    protected PrintGridlinesRecord       printGridlines    =     null;
    protected WindowTwoRecord            windowTwo         =     null;
    protected MergeCellsRecord           merged            =     null;
    protected Margin[]                   margins           =     null;
    protected List                       mergedRecords     =     new ArrayList();
    protected int                        numMergedRegions  =     0;
    protected SelectionRecord            selection         =     null;
    protected ColumnInfoRecordsAggregate columns           =     null;
    protected ValueRecordsAggregate      cells             =     null;
    protected RowRecordsAggregate        rows              =     null;
    private   Iterator                   valueRecIterator  =     null;
    private   Iterator                   rowRecIterator    =     null;
    protected int                        eofLoc            =     0;
    protected ProtectRecord              protect           =     null;
    protected PageBreakRecord            rowBreaks         =     null;
    protected PageBreakRecord            colBreaks         =     null;

	
    public static final byte PANE_LOWER_RIGHT = (byte)0;
    public static final byte PANE_UPPER_RIGHT = (byte)1;
    public static final byte PANE_LOWER_LEFT = (byte)2;
    public static final byte PANE_UPPER_LEFT = (byte)3;

    /**
     * Creates new Sheet with no intialization --useless at this point
     * @see #createSheet(List,int,int)
     */
    public Sheet()
    {
    }

    /**
     * read support  (offset used as starting point for search) for low level
     * API.  Pass in an array of Record objects, the sheet number (0 based) and
     * a record offset (should be the location of the sheets BOF record).  A Sheet
     * object is constructed and passed back with all of its initialization set
     * to the passed in records and references to those records held. This function
     * is normally called via Workbook.
     *
     * @param recs array containing those records in the sheet in sequence (normally obtained from RecordFactory)
     * @param sheetnum integer specifying the sheet's number (0,1 or 2 in this release)
     * @param offset of the sheet's BOF record
     *
     * @return Sheet object with all values set to those read from the file
     *
     * @see org.apache.poi.hssf.model.Workbook
     * @see org.apache.poi.hssf.record.Record
     */
    public static Sheet createSheet(List recs, int sheetnum, int offset)
    {
        if (log.check( POILogger.DEBUG ))
            log.logFormatted(POILogger.DEBUG,
                    "Sheet createSheet (existing file) with %",
                    new Integer(recs.size()));
        Sheet     retval             = new Sheet();
        ArrayList records            = new ArrayList(recs.size() / 5);
        boolean   isfirstcell        = true;
        boolean   isfirstrow         = true;
        int       bofEofNestingLevel = 0;

        for (int k = offset; k < recs.size(); k++)
        {
            Record rec = ( Record ) recs.get(k);

            if (rec.getSid() == LabelRecord.sid)
            {
                if (log.check( POILogger.DEBUG ))
                    log.log(POILogger.DEBUG, "Hit label record.");
                retval.containsLabels = true;
            }
            else if (rec.getSid() == BOFRecord.sid)
            {
                bofEofNestingLevel++;
                if (log.check( POILogger.DEBUG ))
                    log.log(POILogger.DEBUG, "Hit BOF record. Nesting increased to " + bofEofNestingLevel);
            }
            else if (rec.getSid() == EOFRecord.sid)
            {
                --bofEofNestingLevel;
                if (log.check( POILogger.DEBUG ))
                    log.log(POILogger.DEBUG, "Hit EOF record. Nesting decreased to " + bofEofNestingLevel);
                if (bofEofNestingLevel == 0) {
                    records.add(rec);
                    retval.eofLoc = k;
                    break;
                }
            }
            else if (rec.getSid() == DimensionsRecord.sid)
            {
                // Make a columns aggregate if one hasn't ready been created.
                if (retval.columns == null)
                {
                    retval.columns = new ColumnInfoRecordsAggregate();
                    records.add(retval.columns);
                }

                retval.dims    = ( DimensionsRecord ) rec;
                retval.dimsloc = records.size();
            }
            else if (rec.getSid() == MergeCellsRecord.sid)
            {
                retval.mergedRecords.add(rec);
                retval.merged = ( MergeCellsRecord ) rec;
                retval.numMergedRegions += retval.merged.getNumAreas();
            }
            else if (rec.getSid() == ColumnInfoRecord.sid)
            {
                ColumnInfoRecord col = (ColumnInfoRecord)rec;
                if (retval.columns != null)
                {
                    rec = null; //only add the aggregate once
                }
                else
                {
                    rec = retval.columns = new ColumnInfoRecordsAggregate();
                }
                retval.columns.insertColumn(col);
            }
            else if (rec.getSid() == DefaultColWidthRecord.sid)
            {
                retval.defaultcolwidth = ( DefaultColWidthRecord ) rec;
            }
            else if (rec.getSid() == DefaultRowHeightRecord.sid)
            {
                retval.defaultrowheight = ( DefaultRowHeightRecord ) rec;
            }
            else if ( rec.isValue() && bofEofNestingLevel == 1 )
            {
                if ( isfirstcell )
                {
                    retval.cells = new ValueRecordsAggregate();
                    rec = retval.cells;
                    retval.cells.construct( k, recs );
                    isfirstcell = false;
                }
                else
                {
                    rec = null;
                }
            }
            else if ( rec.getSid() == StringRecord.sid )
            {
                rec = null;
            }
            else if ( rec.getSid() == RowRecord.sid )
            {
                RowRecord row = (RowRecord)rec;
                if (!isfirstrow) rec = null; //only add the aggregate once

                if ( isfirstrow )
                {
                    retval.rows = new RowRecordsAggregate();
                    rec = retval.rows;                    
                    isfirstrow = false;
                }
                retval.rows.insertRow(row);
            }
            else if ( rec.getSid() == PrintGridlinesRecord.sid )
            {
                retval.printGridlines = (PrintGridlinesRecord) rec;
            }
            else if ( rec.getSid() == HeaderRecord.sid && bofEofNestingLevel == 1)
            {
                retval.header = (HeaderRecord) rec;
            }
            else if ( rec.getSid() == FooterRecord.sid && bofEofNestingLevel == 1)
            {
                retval.footer = (FooterRecord) rec;
            }
            else if ( rec.getSid() == PrintSetupRecord.sid && bofEofNestingLevel == 1)
            {
                retval.printSetup = (PrintSetupRecord) rec;
            }
            else if ( rec.getSid() == LeftMarginRecord.sid)
            {
                retval.getMargins()[LeftMargin] = (LeftMarginRecord) rec;
            }
            else if ( rec.getSid() == RightMarginRecord.sid)
            {
                retval.getMargins()[RightMargin] = (RightMarginRecord) rec;
            }
            else if ( rec.getSid() == TopMarginRecord.sid)
            {
                retval.getMargins()[TopMargin] = (TopMarginRecord) rec;
            }
            else if ( rec.getSid() == BottomMarginRecord.sid)
            {
                retval.getMargins()[BottomMargin] = (BottomMarginRecord) rec;
            }
            else if ( rec.getSid() == SelectionRecord.sid )
            {
                retval.selection = (SelectionRecord) rec;
            }
            else if ( rec.getSid() == WindowTwoRecord.sid )
            {
                retval.windowTwo = (WindowTwoRecord) rec;
            }
			else if ( rec.getSid() == ProtectRecord.sid )
			{
				retval.protect = (ProtectRecord) rec;
			} 
			else if (rec.getSid() == PageBreakRecord.HORIZONTAL_SID) 
			{	
				retval.rowBreaks = (PageBreakRecord)rec;				
			}
			else if (rec.getSid() == PageBreakRecord.VERTICAL_SID) 
			{	
				retval.colBreaks = (PageBreakRecord)rec;				
			}
            
            if (rec != null)
            {
                records.add(rec);
            }
        }
        retval.records = records;
//        if (retval.rows == null)
//        {
//            retval.rows = new RowRecordsAggregate();
//        }
        retval.checkCells();
        retval.checkRows();
//        if (retval.cells == null)
//        {
//            retval.cells = new ValueRecordsAggregate();
//        }
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "sheet createSheet (existing file) exited");
        return retval;
    }

    /**
     * Clones the low level records of this sheet and returns the new sheet instance.
     * This method is implemented by adding methods for deep cloning to all records that
     * can be added to a sheet. The <b>Record</b> object does not implement cloneable. 
     * When adding a new record, implement a public clone method if and only if the record
     * belongs to a sheet. 
     */
    public Sheet cloneSheet()
    {
      ArrayList clonedRecords = new ArrayList(this.records.size());
      for (int i=0; i<this.records.size();i++) {
        Record rec = (Record)((Record)this.records.get(i)).clone();
        //Need to pull out the Row record and the Value records from their
        //Aggregates.
        //This is probably the best way to do it since we probably dont want the createSheet
        //To cater for these artificial Record types
        if (rec instanceof RowRecordsAggregate) {
          RowRecordsAggregate rrAgg = (RowRecordsAggregate)rec;
          for (Iterator rowIter = rrAgg.getIterator();rowIter.hasNext();) {
            Record rowRec = (Record)rowIter.next();
            clonedRecords.add(rowRec);
          }
        } else if (rec instanceof ValueRecordsAggregate) {
          ValueRecordsAggregate vrAgg = (ValueRecordsAggregate)rec;
          for (Iterator cellIter = vrAgg.getIterator();cellIter.hasNext();) {
            Record valRec = (Record)cellIter.next();
            clonedRecords.add(valRec);
          }
        } else if (rec instanceof FormulaRecordAggregate) {
          FormulaRecordAggregate fmAgg = (FormulaRecordAggregate)rec;
          Record fmAggRec = fmAgg.getFormulaRecord();
          if (fmAggRec != null)
            clonedRecords.add(fmAggRec);
          fmAggRec =   fmAgg.getStringRecord();
          if (fmAggRec != null)
            clonedRecords.add(fmAggRec);
        } else {
          clonedRecords.add(rec);
        }
      }
      return createSheet(clonedRecords, 0, 0);
    }


    /**
     * read support  (offset = 0) Same as createSheet(Record[] recs, int, int)
     * only the record offset is assumed to be 0.
     *
     * @param records  array containing those records in the sheet in sequence (normally obtained from RecordFactory)
     * @param sheetnum integer specifying the sheet's number (0,1 or 2 in this release)
     * @return Sheet object
     */

    public static Sheet createSheet(List records, int sheetnum)
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG,
                    "Sheet createSheet (exisiting file) assumed offset 0");
        return createSheet(records, sheetnum, 0);
    }

    /**
     * Creates a sheet with all the usual records minus values and the "index"
     * record (not required).  Sets the location pointer to where the first value
     * records should go.  Use this to create a sheet from "scratch".
     *
     * @return Sheet object with all values set to defaults
     */

    public static Sheet createSheet()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch called");
        Sheet     retval  = new Sheet();
        ArrayList records = new ArrayList(30);

        records.add(retval.createBOF());

        // records.add(retval.createIndex());
        records.add(retval.createCalcMode());
        records.add(retval.createCalcCount() );
        records.add( retval.createRefMode() );
        records.add( retval.createIteration() );
        records.add( retval.createDelta() );
        records.add( retval.createSaveRecalc() );
        records.add( retval.createPrintHeaders() );
        retval.printGridlines = (PrintGridlinesRecord) retval.createPrintGridlines();
        records.add( retval.printGridlines );
        retval.gridset = (GridsetRecord) retval.createGridset();
        records.add( retval.gridset );
        records.add( retval.createGuts() );
        retval.defaultrowheight =
                (DefaultRowHeightRecord) retval.createDefaultRowHeight();
        records.add( retval.defaultrowheight );
        records.add( retval.createWSBool() );

        retval.rowBreaks = new PageBreakRecord(PageBreakRecord.HORIZONTAL_SID);
        records.add(retval.rowBreaks);
        retval.colBreaks = new PageBreakRecord(PageBreakRecord.VERTICAL_SID);
        records.add(retval.colBreaks);
        
        retval.header = (HeaderRecord) retval.createHeader();
        records.add( retval.header );        
        retval.footer = (FooterRecord) retval.createFooter();
        records.add( retval.footer );
        records.add( retval.createHCenter() );
        records.add( retval.createVCenter() );
        retval.printSetup = (PrintSetupRecord) retval.createPrintSetup();
        records.add( retval.printSetup );
        retval.defaultcolwidth =
                (DefaultColWidthRecord) retval.createDefaultColWidth();
        records.add( retval.defaultcolwidth);
        ColumnInfoRecordsAggregate columns = new ColumnInfoRecordsAggregate();
        records.add( columns );
        retval.columns = columns;
        retval.dims    = ( DimensionsRecord ) retval.createDimensions();
        records.add(retval.dims);
        retval.dimsloc = records.size()-1;
        records.add(retval.windowTwo = retval.createWindowTwo());
        retval.setLoc(records.size() - 1);
        retval.selection = 
                (SelectionRecord) retval.createSelection();
        records.add(retval.selection);
		retval.protect = (ProtectRecord) retval.createProtect();
		records.add(retval.protect);
        records.add(retval.createEOF());


        retval.records = records;
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch exit");
        return retval;
    }

    private void checkCells()
    {
        if (cells == null)
        {
            cells = new ValueRecordsAggregate();
            records.add(getDimsLoc() + 1, cells);
        }
    }

    private void checkRows()
    {
        if (rows == null)
        {
            rows = new RowRecordsAggregate();
            records.add(getDimsLoc() + 1, rows);
        }
    }

    //public int addMergedRegion(short rowFrom, short colFrom, short rowTo,
    public int addMergedRegion(int rowFrom, short colFrom, int rowTo,
                               short colTo)
    {
        if (merged == null || merged.getNumAreas() == 1027)
        {
            merged = ( MergeCellsRecord ) createMergedCells();
            mergedRecords.add(merged);            
            records.add(records.size() - 1, merged);
        }
        merged.addArea(rowFrom, colFrom, rowTo, colTo);
        return numMergedRegions++; 
    }

    public void removeMergedRegion(int index)
    {
        //safety checks
        if (index >= numMergedRegions || mergedRecords.size() == 0)
           return;
            
        int pos = 0;
        int startNumRegions = 0;
        
        //optimisation for current record
        if (numMergedRegions - index < merged.getNumAreas())
        {
            pos = mergedRecords.size() - 1;
            startNumRegions = numMergedRegions - merged.getNumAreas(); 
        }
        else
        {
            for (int n = 0; n < mergedRecords.size(); n++)
            {
                MergeCellsRecord record = (MergeCellsRecord) mergedRecords.get(n);
                if (startNumRegions + record.getNumAreas() > index)
                {
                    pos = n;
                    break;
                }
                startNumRegions += record.getNumAreas(); 
            }
        }

        MergeCellsRecord rec = (MergeCellsRecord) mergedRecords.get(pos);
        rec.removeAreaAt(index - startNumRegions);
        numMergedRegions--;
        if (rec.getNumAreas() == 0)
        {
			mergedRecords.remove(pos);
			//get rid of the record from the sheet
			records.remove(merged);            
            if (merged == rec) {
            	//pull up the LAST record for operations when we finally
            	//support continue records for mergedRegions
            	if (mergedRecords.size() > 0) {
            		merged = (MergeCellsRecord) mergedRecords.get(mergedRecords.size() - 1);
            	} else {
            		merged = null;
            	}
            }
        }
    }

    public MergeCellsRecord.MergedRegion getMergedRegionAt(int index)
    {
        //safety checks
        if (index >= numMergedRegions || mergedRecords.size() == 0)
            return null;
            
        int pos = 0;
        int startNumRegions = 0;
        
        //optimisation for current record
        if (numMergedRegions - index < merged.getNumAreas())
        {
            pos = mergedRecords.size() - 1;
            startNumRegions = numMergedRegions - merged.getNumAreas();
        }
        else
        {
            for (int n = 0; n < mergedRecords.size(); n++)
            {
                MergeCellsRecord record = (MergeCellsRecord) mergedRecords.get(n);
                if (startNumRegions + record.getNumAreas() > index)
                {
                    pos = n;
                    break;
                }
                startNumRegions += record.getNumAreas(); 
            }
        }
        return ((MergeCellsRecord) mergedRecords.get(pos)).getAreaAt(index - startNumRegions);
    }

    public int getNumMergedRegions()
    {
        return numMergedRegions;
    }

    /**
     * This is basically a kludge to deal with the now obsolete Label records.  If
     * you have to read in a sheet that contains Label records, be aware that the rest
     * of the API doesn't deal with them, the low level structure only provides read-only
     * semi-immutable structures (the sets are there for interface conformance with NO
     * impelmentation).  In short, you need to call this function passing it a reference
     * to the Workbook object.  All labels will be converted to LabelSST records and their
     * contained strings will be written to the Shared String tabel (SSTRecord) within
     * the Workbook.
     *
     * @param wb sheet's matching low level Workbook structure containing the SSTRecord.
     * @see org.apache.poi.hssf.record.LabelRecord
     * @see org.apache.poi.hssf.record.LabelSSTRecord
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public void convertLabelRecords(Workbook wb)
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "convertLabelRecords called");
        if (containsLabels)
        {
            for (int k = 0; k < records.size(); k++)
            {
                Record rec = ( Record ) records.get(k);

                if (rec.getSid() == LabelRecord.sid)
                {
                    LabelRecord oldrec = ( LabelRecord ) rec;

                    records.remove(k);
                    LabelSSTRecord newrec   = new LabelSSTRecord();
                    int            stringid =
                        wb.addSSTString(oldrec.getValue());

                    newrec.setRow(oldrec.getRow());
                    newrec.setColumn(oldrec.getColumn());
                    newrec.setXFIndex(oldrec.getXFIndex());
                    newrec.setSSTIndex(stringid);
                          records.add(k, newrec);
                }
            }
        }
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "convertLabelRecords exit");
    }

    /**
     * Returns the number of low level binary records in this sheet.  This adjusts things for the so called
     * AgregateRecords.
     *
     * @see org.apache.poi.hssf.record.Record
     */

    public int getNumRecords()
    {
        checkCells();
        checkRows();
        if (log.check( POILogger.DEBUG ))
        {
            log.log(POILogger.DEBUG, "Sheet.getNumRecords");
            log.logFormatted(POILogger.DEBUG, "returning % + % + % - 2 = %", new int[]
            {
                records.size(), cells.getPhysicalNumberOfCells(),
                rows.getPhysicalNumberOfRows(),
                records.size() + cells.getPhysicalNumberOfCells()
                + rows.getPhysicalNumberOfRows() - 2
            });
        }
        return records.size() + cells.getPhysicalNumberOfCells()
               + rows.getPhysicalNumberOfRows() - 2;
    }

    /**
     * Per an earlier reported bug in working with Andy Khan's excel read library.  This
     * sets the values in the sheet's DimensionsRecord object to be correct.  Excel doesn't
     * really care, but we want to play nice with other libraries.
     *
     * @see org.apache.poi.hssf.record.DimensionsRecord
     */

    //public void setDimensions(short firstrow, short firstcol, short lastrow,
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
        dims.setFirstCol(firstcol);
        dims.setFirstRow(firstrow);
        dims.setLastCol(lastcol);
        dims.setLastRow(lastrow);
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.setDimensions exiting");
    }

    /**
     * set the locator for where we should look for the next value record.  The
     * algorythm will actually start here and find the correct location so you
     * can set this to 0 and watch performance go down the tubes but it will work.
     * After a value is set this is automatically advanced.  Its also set by the
     * create method.  So you probably shouldn't mess with this unless you have
     * a compelling reason why or the help for the method you're calling says so.
     * Check the other methods for whether they care about
     * the loc pointer.  Many of the "modify" and "remove" methods re-initialize this
     * to "dimsloc" which is the location of the Dimensions Record and presumably the
     * start of the value section (at or around 19 dec).
     *
     * @param loc the record number to start at
     *
     */

    public void setLoc(int loc)
    {
        valueRecIterator = null;
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "sheet.setLoc(): " + loc);
        this.loc = loc;
    }

    /**
     * Returns the location pointer to the first record to look for when adding rows/values
     *
     */

    public int getLoc()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "sheet.getLoc():" + loc);
        return loc;
    }

    /**
     * Set the preoffset when using DBCELL records (currently unused) - this is
     * the position of this sheet within the whole file.
     *
     * @param offset the offset of the sheet's BOF within the file.
     */

    public void setPreOffset(int offset)
    {
        this.preoffset = offset;
    }

    /**
     * get the preoffset when using DBCELL records (currently unused) - this is
     * the position of this sheet within the whole file.
     *
     * @return offset the offset of the sheet's BOF within the file.
     */

    public int getPreOffset()
    {
        return preoffset;
    }

    /**
     * Serializes all records in the sheet into one big byte array.  Use this to write
     * the sheet out.
     *
     * @return byte[] array containing the binary representation of the records in this sheet
     *
     */

    public byte [] serialize()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.serialize");

        // addDBCellRecords();
        byte[] retval    = null;

        // ArrayList bytes     = new ArrayList(4096);
        int    arraysize = getSize();
        int    pos       = 0;

        // for (int k = 0; k < records.size(); k++)
        // {
        // bytes.add((( Record ) records.get(k)).serialize());
        //
        // }
        // for (int k = 0; k < bytes.size(); k++)
        // {
        // arraysize += (( byte [] ) bytes.get(k)).length;
        // POILogger.DEBUG((new StringBuffer("arraysize=")).append(arraysize)
        // .toString());
        // }
        retval = new byte[ arraysize ];
        for (int k = 0; k < records.size(); k++)
        {

            // byte[] rec = (( byte [] ) bytes.get(k));
            // System.arraycopy(rec, 0, retval, pos, rec.length);
            pos += (( Record ) records.get(k)).serialize(pos,
                    retval);   // rec.length;
        }
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.serialize returning " + retval);
        return retval;
    }

    /**
     * Serializes all records in the sheet into one big byte array.  Use this to write
     * the sheet out.
     *
     * @param offset to begin write at
     * @param data   array containing the binary representation of the records in this sheet
     *
     */

    public int serialize(int offset, byte [] data)
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.serialize using offsets");

        // addDBCellRecords();
        // ArrayList bytes     = new ArrayList(4096);
        // int arraysize = getSize();   // 0;
        int pos       = 0;

        // for (int k = 0; k < records.size(); k++)
        // {
        // bytes.add((( Record ) records.get(k)).serialize());
        //
        // }
        // for (int k = 0; k < bytes.size(); k++)
        // {
        // arraysize += (( byte [] ) bytes.get(k)).length;
        // POILogger.DEBUG((new StringBuffer("arraysize=")).append(arraysize)
        // .toString());
        // }
        for (int k = 0; k < records.size(); k++)
        {
//             byte[] rec = (( byte [] ) bytes.get(k));
            // System.arraycopy(rec, 0, data, offset + pos, rec.length);
            Record record = (( Record ) records.get(k));

            //// uncomment to test record sizes ////
//            System.out.println( record.getClass().getName() );
//            byte[] data2 = new byte[record.getRecordSize()];
//            record.serialize(0, data2 );   // rec.length;
//            if (LittleEndian.getUShort(data2, 2) != record.getRecordSize() - 4
//                    && record instanceof RowRecordsAggregate == false
//                    && record instanceof ValueRecordsAggregate == false
//                    && record instanceof EscherAggregate == false)
//            {
//                throw new RuntimeException("Blah!!!  Size off by " + ( LittleEndian.getUShort(data2, 2) - record.getRecordSize() - 4) + " records.");
//            }

            pos += record.serialize(pos + offset, data );   // rec.length;

        }
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet.serialize returning ");
        return pos;
    }

    /**
     * Create a row record.  (does not add it to the records contained in this sheet)
     *
     * @param row number
     * @return RowRecord created for the passed in row number
     * @see org.apache.poi.hssf.record.RowRecord
     */

    public RowRecord createRow(int row)
    {
        return RowRecordsAggregate.createRow( row );
    }

    /**
     * Create a LABELSST Record (does not add it to the records contained in this sheet)
     *
     * @param row the row the LabelSST is a member of
     * @param col the column the LabelSST defines
     * @param index the index of the string within the SST (use workbook addSSTString method)
     * @return LabelSSTRecord newly created containing your SST Index, row,col.
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    //public LabelSSTRecord createLabelSST(short row, short col, int index)
    public LabelSSTRecord createLabelSST(int row, short col, int index)
    {
        log.logFormatted(POILogger.DEBUG, "create labelsst row,col,index %,%,%",
                         new int[]
        {
            row, col, index
        });
        LabelSSTRecord rec = new LabelSSTRecord();

        rec.setRow(row);
        rec.setColumn(col);
        rec.setSSTIndex(index);
        rec.setXFIndex(( short ) 0x0f);
        return rec;
    }

    /**
     * Create a NUMBER Record (does not add it to the records contained in this sheet)
     *
     * @param row the row the NumberRecord is a member of
     * @param col the column the NumberRecord defines
     * @param value for the number record
     *
     * @return NumberRecord for that row, col containing that value as added to the sheet
     */

    //public NumberRecord createNumber(short row, short col, double value)
    public NumberRecord createNumber(int row, short col, double value)
    {
        log.logFormatted(POILogger.DEBUG, "create number row,col,value %,%,%",
                         new double[]
        {
            row, col, value
        });
        NumberRecord rec = new NumberRecord();

        //rec.setRow(( short ) row);
        rec.setRow(row);
        rec.setColumn(col);
        rec.setValue(value);
        rec.setXFIndex(( short ) 0x0f);
        return rec;
    }

    /**
     * create a BLANK record (does not add it to the records contained in this sheet)
     *
     * @param row - the row the BlankRecord is a member of
     * @param col - the column the BlankRecord is a member of
     */

    //public BlankRecord createBlank(short row, short col)
    public BlankRecord createBlank(int row, short col)
    {
        //log.logFormatted(POILogger.DEBUG, "create blank row,col %,%", new short[]
        log.logFormatted(POILogger.DEBUG, "create blank row,col %,%", new int[]
        {
            row, col
        });
        BlankRecord rec = new BlankRecord();

        //rec.setRow(( short ) row);
        rec.setRow(row);
        rec.setColumn(col);
        rec.setXFIndex(( short ) 0x0f);
        return rec;
    }

    /**
     * Attempts to parse the formula into PTGs and create a formula record
     * DOES NOT WORK YET
     *
     * @param row - the row for the formula record
     * @param col - the column of the formula record
     * @param formula - a String representing the formula.  To be parsed to PTGs
     * @return bogus/useless formula record
     */

    //public FormulaRecord createFormula(short row, short col, String formula)
    public FormulaRecord createFormula(int row, short col, String formula)
    {
        log.logFormatted(POILogger.DEBUG, "create formula row,col,formula %,%,%",
                         //new short[]
                         new int[]
        {
            row, col
        }, formula);
        FormulaRecord rec = new FormulaRecord();

        rec.setRow(row);
        rec.setColumn(col);
        rec.setOptions(( short ) 2);
        rec.setValue(0);
        rec.setXFIndex(( short ) 0x0f);
        FormulaParser fp = new FormulaParser(formula,null); //fix - do we need this method?
        fp.parse();
        Ptg[] ptg  = fp.getRPNPtg();
        int   size = 0;

        for (int k = 0; k < ptg.length; k++)
        {
            size += ptg[ k ].getSize();
            rec.pushExpressionToken(ptg[ k ]);
        }
        rec.setExpressionLength(( short ) size);
        return rec;
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

    //public void addValueRecord(short row, CellValueRecordInterface col)
    public void addValueRecord(int row, CellValueRecordInterface col)
    {
        checkCells();
        log.logFormatted(POILogger.DEBUG, "add value record  row,loc %,%", new int[]
        {
            row, loc
        });
        DimensionsRecord d = ( DimensionsRecord ) records.get(getDimsLoc());

        if (col.getColumn() > d.getLastCol())
        {
            d.setLastCol(( short ) (col.getColumn() + 1));
        }
        if (col.getColumn() < d.getFirstCol())
        {
            d.setFirstCol(col.getColumn());
        }
        cells.insertCell(col);

        /*
         * for (int k = loc; k < records.size(); k++)
         * {
         *   Record rec = ( Record ) records.get(k);
         *
         *   if (rec.getSid() == RowRecord.sid)
         *   {
         *       RowRecord rowrec = ( RowRecord ) rec;
         *
         *       if (rowrec.getRowNumber() == col.getRow())
         *       {
         *           records.add(k + 1, col);
         *           loc = k;
         *           if (rowrec.getLastCol() <= col.getColumn())
         *           {
         *               rowrec.setLastCol((( short ) (col.getColumn() + 1)));
         *           }
         *           break;
         *       }
         *   }
         * }
         */
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

    //public void removeValueRecord(short row, CellValueRecordInterface col)
    public void removeValueRecord(int row, CellValueRecordInterface col)
    {
        checkCells();
        log.logFormatted(POILogger.DEBUG, "remove value record row,dimsloc %,%",
                         new int[]{row, dimsloc} );
        loc = dimsloc;
        cells.removeCell(col);

        /*
         * for (int k = loc; k < records.size(); k++)
         * {
         *   Record rec = ( Record ) records.get(k);
         *
         *   // checkDimsLoc(rec,k);
         *   if (rec.isValue())
         *   {
         *       CellValueRecordInterface cell =
         *           ( CellValueRecordInterface ) rec;
         *
         *       if ((cell.getRow() == col.getRow())
         *               && (cell.getColumn() == col.getColumn()))
         *       {
         *           records.remove(k);
         *           break;
         *       }
         *   }
         * }
         */
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

    public void replaceValueRecord(CellValueRecordInterface newval)
    {
        checkCells();
        setLoc(dimsloc);
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "replaceValueRecord ");
        cells.insertCell(newval);

        /*
         * CellValueRecordInterface oldval = getNextValueRecord();
         *
         * while (oldval != null)
         * {
         *   if (oldval.isEqual(newval))
         *   {
         *       records.set(( short ) (getLoc() - 1), newval);
         *       return;
         *   }
         *   oldval = getNextValueRecord();
         * }
         * addValueRecord(newval.getRow(), newval);
         * setLoc(dimsloc);
         */
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
     * @see #setLoc(int)
     */

    public void addRow(RowRecord row)
    {
        checkRows();
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "addRow ");
        DimensionsRecord d = ( DimensionsRecord ) records.get(getDimsLoc());

        if (row.getRowNumber() >= d.getLastRow())
        {
            d.setLastRow(row.getRowNumber() + 1);
        }
        if (row.getRowNumber() < d.getFirstRow())
        {
            d.setFirstRow(row.getRowNumber());
        }
        //IndexRecord index = null;
         //If the row exists remove it, so that any cells attached to the row are removed
         RowRecord existingRow = rows.getRow(row.getRowNumber());
         if (existingRow != null)
           rows.removeRow(existingRow);

        rows.insertRow(row);

        /*
         * for (int k = loc; k < records.size(); k++)
         * {
         *   Record rec = ( Record ) records.get(k);
         *
         *   if (rec.getSid() == IndexRecord.sid)
         *   {
         *       index = ( IndexRecord ) rec;
         *   }
         *   if (rec.getSid() == RowRecord.sid)
         *   {
         *       RowRecord rowrec = ( RowRecord ) rec;
         *
         *       if (rowrec.getRowNumber() > row.getRowNumber())
         *       {
         *           records.add(k, row);
         *           loc = k;
         *           break;
         *       }
         *   }
         *   if (rec.getSid() == WindowTwoRecord.sid)
         *   {
         *       records.add(k, row);
         *       loc = k;
         *       break;
         *   }
         * }
         * if (index != null)
         * {
         *   if (index.getLastRowAdd1() <= row.getRowNumber())
         *   {
         *       index.setLastRowAdd1(row.getRowNumber() + 1);
         *   }
         * }
         */
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

    public void removeRow(RowRecord row)
    {
        checkRows();
        // IndexRecord index = null;

        setLoc(getDimsLoc());
        rows.removeRow(row);

        /*
         * for (int k = loc; k < records.size(); k++)
         * {
         *   Record rec = ( Record ) records.get(k);
         *
         *   // checkDimsLoc(rec,k);
         *   if (rec.getSid() == RowRecord.sid)
         *   {
         *       RowRecord rowrec = ( RowRecord ) rec;
         *
         *       if (rowrec.getRowNumber() == row.getRowNumber())
         *       {
         *           records.remove(k);
         *           break;
         *       }
         *   }
         *   if (rec.getSid() == WindowTwoRecord.sid)
         *   {
         *       break;
         *   }
         * }
         */
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
     * @see #setLoc(int)
     */

    public CellValueRecordInterface getNextValueRecord()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "getNextValue loc= " + loc);
        if (valueRecIterator == null)
        {
            valueRecIterator = cells.getIterator();
        }
        if (!valueRecIterator.hasNext())
        {
            return null;
        }
        return ( CellValueRecordInterface ) valueRecIterator.next();

        /*
         *      if (this.getLoc() < records.size())
         *     {
         *         for (int k = getLoc(); k < records.size(); k++)
         *         {
         *             Record rec = ( Record ) records.get(k);
         *
         *             this.setLoc(k + 1);
         *             if (rec instanceof CellValueRecordInterface)
         *             {
         *                 return ( CellValueRecordInterface ) rec;
         *             }
         *         }
         *     }
         *     return null;
         */
    }

    /**
     * get the NEXT RowRecord or CellValueRecord(from LOC).  The first record that
     * is a Row record or CellValueRecord(starting at LOC) will be returned.
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with.
     * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
     *
     * @return RowRecord representing the next row record or CellValueRecordInterface
     *  representing the next cellvalue or NULL if there are no more
     * @see #setLoc(int)
     *
     */

/*    public Record getNextRowOrValue()
    {
        POILogger.DEBUG((new StringBuffer("getNextRow loc= ")).append(loc)
            .toString());
        if (this.getLoc() < records.size())
        {
            for (int k = this.getLoc(); k < records.size(); k++)
            {
                Record rec = ( Record ) records.get(k);

                this.setLoc(k + 1);
                if (rec.getSid() == RowRecord.sid)
                {
                    return rec;
                }
                else if (rec.isValue())
                {
                    return rec;
                }
            }
        }
        return null;
    }
 */

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
     * @see #setLoc(int)
     *
     */

    public RowRecord getNextRow()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "getNextRow loc= " + loc);
        if (rowRecIterator == null)
        {
            rowRecIterator = rows.getIterator();
        }
        if (!rowRecIterator.hasNext())
        {
            return null;
        }
        return ( RowRecord ) rowRecIterator.next();

/*        if (this.getLoc() < records.size())
        {
            for (int k = this.getLoc(); k < records.size(); k++)
            {
                Record rec = ( Record ) records.get(k);

                this.setLoc(k + 1);
                if (rec.getSid() == RowRecord.sid)
                {
                    return ( RowRecord ) rec;
                }
            }
        }*/
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
     * @see #setLoc(int)
     *
     */

    //public RowRecord getRow(short rownum)
    public RowRecord getRow(int rownum)
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "getNextRow loc= " + loc);
        return rows.getRow(rownum);

        /*
         * if (this.getLoc() < records.size())
         * {
         *   for (int k = this.getLoc(); k < records.size(); k++)
         *   {
         *       Record rec = ( Record ) records.get(k);
         *
         *       this.setLoc(k + 1);
         *       if (rec.getSid() == RowRecord.sid)
         *       {
         *           if ((( RowRecord ) rec).getRowNumber() == rownum)
         *           {
         *               return ( RowRecord ) rec;
         *           }
         *       }
         *   }
         * }
         */

        // return null;
    }

    /**
     * Not currently used method to calculate and add dbcell records
     *
     */

    public void addDBCellRecords()
    {
        int         offset        = 0;
        int         recnum        = 0;
        int         rownum        = 0;
        //int         lastrow       = 0;
        //long        lastrowoffset = 0;
        IndexRecord index         = null;

        // ArrayList rowOffsets = new ArrayList();
        IntList     rowOffsets    = new IntList();

        for (recnum = 0; recnum < records.size(); recnum++)
        {
            Record rec = ( Record ) records.get(recnum);

            if (rec.getSid() == IndexRecord.sid)
            {
                index = ( IndexRecord ) rec;
            }
            if (rec.getSid() != RowRecord.sid)
            {
                offset += rec.serialize().length;
            }
            else
            {
                break;
            }
        }

        // First Row Record
        for (; recnum < records.size(); recnum++)
        {
            Record rec = ( Record ) records.get(recnum);

            if (rec.getSid() == RowRecord.sid)
            {
                rownum++;
                rowOffsets.add(offset);
                if ((rownum % 32) == 0)
                {

                    // if this is the last rec in a  dbcell block
                    // find the next row or last value record
                    for (int rn = recnum; rn < records.size(); rn++)
                    {
                        rec = ( Record ) records.get(rn);
                        if ((!rec.isInValueSection())
                                || (rec.getSid() == RowRecord.sid))
                        {

                            // here is the next row or last value record
                            records.add(rn,
                                        createDBCell(offset, rowOffsets,
                                                     index));
                            recnum = rn;
                            break;
                        }
                    }
                }
                else
                {
                }
            }
            if (!rec.isInValueSection())
            {
                records.add(recnum, createDBCell(offset, rowOffsets, index));
                break;
            }
            offset += rec.serialize().length;
        }
    }

    /** not currently used */

    private DBCellRecord createDBCell(int offset, IntList rowoffsets,
                                      IndexRecord index)
    {
        DBCellRecord rec = new DBCellRecord();

        rec.setRowOffset(offset - rowoffsets.get(0));

        // test hack
        rec.addCellOffset(( short ) 0x0);

        // end test hack
        addDbCellToIndex(offset, index);
        return rec;
    }

    /** not currently used */

    private void addDbCellToIndex(int offset, IndexRecord index)
    {
        int numdbcells = index.getNumDbcells() + 1;

        index.addDbcell(offset + preoffset);

        // stupid but whenever we add an offset that causes everything to be shifted down 4
        for (int k = 0; k < numdbcells; k++)
        {
            int dbval = index.getDbcellAt(k);

            index.setDbcell(k, dbval + 4);
        }
    }

    /**
     * creates the BOF record
     * @see org.apache.poi.hssf.record.BOFRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a BOFRecord
     */

    protected Record createBOF()
    {
        BOFRecord retval = new BOFRecord();

        retval.setVersion(( short ) 0x600);
        retval.setType(( short ) 0x010);

        // retval.setBuild((short)0x10d3);
        retval.setBuild(( short ) 0x0dbb);
        retval.setBuildYear(( short ) 1996);
        retval.setHistoryBitMask(0xc1);
        retval.setRequiredVersion(0x6);
        return retval;
    }

    /**
     * creates the Index record  - not currently used
     * @see org.apache.poi.hssf.record.IndexRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a IndexRecord
     */

    protected Record createIndex()
    {
        IndexRecord retval = new IndexRecord();

        retval.setFirstRow(0);   // must be set explicitly
        retval.setLastRowAdd1(0);
        return retval;
    }

    /**
     * creates the CalcMode record and sets it to 1 (automatic formula caculation)
     * @see org.apache.poi.hssf.record.CalcModeRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a CalcModeRecord
     */

    protected Record createCalcMode()
    {
        CalcModeRecord retval = new CalcModeRecord();

        retval.setCalcMode(( short ) 1);
        return retval;
    }

    /**
     * creates the CalcCount record and sets it to 0x64 (default number of iterations)
     * @see org.apache.poi.hssf.record.CalcCountRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a CalcCountRecord
     */

    protected Record createCalcCount()
    {
        CalcCountRecord retval = new CalcCountRecord();

        retval.setIterations(( short ) 0x64);   // default 64 iterations
        return retval;
    }

    /**
     * creates the RefMode record and sets it to A1 Mode (default reference mode)
     * @see org.apache.poi.hssf.record.RefModeRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a RefModeRecord
     */

    protected Record createRefMode()
    {
        RefModeRecord retval = new RefModeRecord();

        retval.setMode(RefModeRecord.USE_A1_MODE);
        return retval;
    }

    /**
     * creates the Iteration record and sets it to false (don't iteratively calculate formulas)
     * @see org.apache.poi.hssf.record.IterationRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a IterationRecord
     */

    protected Record createIteration()
    {
        IterationRecord retval = new IterationRecord();

        retval.setIteration(false);
        return retval;
    }

    /**
     * creates the Delta record and sets it to 0.0010 (default accuracy)
     * @see org.apache.poi.hssf.record.DeltaRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a DeltaRecord
     */

    protected Record createDelta()
    {
        DeltaRecord retval = new DeltaRecord();

        retval.setMaxChange(0.0010);
        return retval;
    }

    /**
     * creates the SaveRecalc record and sets it to true (recalculate before saving)
     * @see org.apache.poi.hssf.record.SaveRecalcRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a SaveRecalcRecord
     */

    protected Record createSaveRecalc()
    {
        SaveRecalcRecord retval = new SaveRecalcRecord();

        retval.setRecalc(true);
        return retval;
    }

    /**
     * creates the PrintHeaders record and sets it to false (we don't create headers yet so why print them)
     * @see org.apache.poi.hssf.record.PrintHeadersRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a PrintHeadersRecord
     */

    protected Record createPrintHeaders()
    {
        PrintHeadersRecord retval = new PrintHeadersRecord();

        retval.setPrintHeaders(false);
        return retval;
    }

    /**
     * creates the PrintGridlines record and sets it to false (that makes for ugly sheets).  As far as I can
     * tell this does the same thing as the GridsetRecord
     *
     * @see org.apache.poi.hssf.record.PrintGridlinesRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a PrintGridlinesRecord
     */

    protected Record createPrintGridlines()
    {
        PrintGridlinesRecord retval = new PrintGridlinesRecord();

        retval.setPrintGridlines(false);
        return retval;
    }

    /**
     * creates the Gridset record and sets it to true (user has mucked with the gridlines)
     * @see org.apache.poi.hssf.record.GridsetRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a GridsetRecord
     */

    protected Record createGridset()
    {
        GridsetRecord retval = new GridsetRecord();

        retval.setGridset(true);
        return retval;
    }

    /**
     * creates the Guts record and sets leftrow/topcol guttter and rowlevelmax/collevelmax to 0
     * @see org.apache.poi.hssf.record.GutsRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a GutsRecordRecord
     */

    protected Record createGuts()
    {
        GutsRecord retval = new GutsRecord();

        retval.setLeftRowGutter(( short ) 0);
        retval.setTopColGutter(( short ) 0);
        retval.setRowLevelMax(( short ) 0);
        retval.setColLevelMax(( short ) 0);
        return retval;
    }

    /**
     * creates the DefaultRowHeight Record and sets its options to 0 and rowheight to 0xff
     * @see org.apache.poi.hssf.record.DefaultRowHeightRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a DefaultRowHeightRecord
     */

    protected Record createDefaultRowHeight()
    {
        DefaultRowHeightRecord retval = new DefaultRowHeightRecord();

        retval.setOptionFlags(( short ) 0);
        retval.setRowHeight(( short ) 0xff);
        return retval;
    }

    /**
     * creates the WSBoolRecord and sets its values to defaults
     * @see org.apache.poi.hssf.record.WSBoolRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a WSBoolRecord
     */

    protected Record createWSBool()
    {
        WSBoolRecord retval = new WSBoolRecord();

        retval.setWSBool1(( byte ) 0x4);
        retval.setWSBool2(( byte ) 0xffffffc1);
        return retval;
    }

    /**
     * creates the Header Record and sets it to nothing/0 length
     * @see org.apache.poi.hssf.record.HeaderRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a HeaderRecord
     */

    protected Record createHeader()
    {
        HeaderRecord retval = new HeaderRecord();

        retval.setHeaderLength(( byte ) 0);
        retval.setHeader(null);
        return retval;
    }

    /**
     * creates the Footer Record and sets it to nothing/0 length
     * @see org.apache.poi.hssf.record.FooterRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a FooterRecord
     */

    protected Record createFooter()
    {
        FooterRecord retval = new FooterRecord();

        retval.setFooterLength(( byte ) 0);
        retval.setFooter(null);
        return retval;
    }

    /**
     * creates the HCenter Record and sets it to false (don't horizontally center)
     * @see org.apache.poi.hssf.record.HCenterRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a HCenterRecord
     */

    protected Record createHCenter()
    {
        HCenterRecord retval = new HCenterRecord();

        retval.setHCenter(false);
        return retval;
    }

    /**
     * creates the VCenter Record and sets it to false (don't horizontally center)
     * @see org.apache.poi.hssf.record.VCenterRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a VCenterRecord
     */

    protected Record createVCenter()
    {
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

    protected Record createPrintSetup()
    {
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
     * creates the DefaultColWidth Record and sets it to 8
     * @see org.apache.poi.hssf.record.DefaultColWidthRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a DefaultColWidthRecord
     */

    protected Record createDefaultColWidth()
    {
        DefaultColWidthRecord retval = new DefaultColWidthRecord();

        retval.setColWidth(( short ) 8);
        return retval;
    }

    /**
     * creates the ColumnInfo Record and sets it to a default column/width
     * @see org.apache.poi.hssf.record.ColumnInfoRecord
     * @return record containing a ColumnInfoRecord
     */

    protected Record createColInfo()
    {
        return ColumnInfoRecordsAggregate.createColInfo();
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width)
     * @return default column width
     */

    public short getDefaultColumnWidth()
    {
        return defaultcolwidth.getColWidth();
    }

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    public boolean isGridsPrinted()
    {
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

    public void setDefaultColumnWidth(short dcw)
    {
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
     * get the width of a given column in units of 1/20th of a point width (twips?)
     * @param column index
     * @see org.apache.poi.hssf.record.DefaultColWidthRecord
     * @see org.apache.poi.hssf.record.ColumnInfoRecord
     * @see #setColumnWidth(short,short)
     * @return column width in units of 1/20th of a point (twips?)
     */

    public short getColumnWidth(short column)
    {
        short            retval = 0;
        ColumnInfoRecord ci     = null;

        if (columns != null)
        {
            for ( Iterator iterator = columns.getIterator(); iterator.hasNext(); )
            {
                ci = ( ColumnInfoRecord ) iterator.next();
                if ((ci.getFirstColumn() <= column)
                        && (column <= ci.getLastColumn()))
                {
                    break;
                }
                ci = null;
            }
        }
        if (ci != null)
        {
            retval = ci.getColumnWidth();
        }
        else
        {
            retval = defaultcolwidth.getColWidth();
        }
        return retval;
    }

    /**
     * set the width for a given column in 1/20th of a character width units
     * @param column - the column number
     * @param width (in units of 1/20th of a character width)
     */
    public void setColumnWidth(short column, short width)
    {
        setColumn( column, new Short(width), null, null, null);
    }

    public void setColumn(short column, Short width, Integer level, Boolean hidden, Boolean collapsed)
    {
        if (columns == null)
            columns = new ColumnInfoRecordsAggregate();

        columns.setColumn( column, width, level, hidden, collapsed );
    }

    /**
     * Creates an outline group for the specified columns.
     * @param fromColumn    group from this column (inclusive)
     * @param toColumn      group to this column (inclusive)
     * @param indent        if true the group will be indented by one level,
     *                      if false indenting will be removed by one level.
     */
    public void groupColumnRange(short fromColumn, short toColumn, boolean indent)
    {

        // Set the level for each column
        columns.groupColumnRange( fromColumn, toColumn, indent);

        // Determine the maximum overall level
        int maxLevel = 0;
        for ( Iterator iterator = columns.getIterator(); iterator.hasNext(); )
        {
            ColumnInfoRecord columnInfoRecord = (ColumnInfoRecord) iterator.next();
            maxLevel = Math.max(columnInfoRecord.getOutlineLevel(), maxLevel);
        }

        GutsRecord guts = (GutsRecord) findFirstRecordBySid( GutsRecord.sid );
        guts.setColLevelMax( (short) ( maxLevel+1 ) );
        if (maxLevel == 0)
            guts.setTopColGutter( (short)0 );
        else
            guts.setTopColGutter( (short) ( 29 + (12 * (maxLevel-1)) ) );
    }

    /**
     * creates the Dimensions Record and sets it to bogus values (you should set this yourself
     * or let the high level API do it for you)
     * @see org.apache.poi.hssf.record.DimensionsRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a DimensionsRecord
     */

    protected Record createDimensions()
    {
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
     * @see org.apache.poi.hssf.record.WindowTwoRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a WindowTwoRecord
     */

    protected WindowTwoRecord createWindowTwo()
    {
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
     *
     * @see org.apache.poi.hssf.record.SelectionRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a SelectionRecord
     */

    protected Record createSelection()
    {
        SelectionRecord retval = new SelectionRecord();

        retval.setPane(( byte ) 0x3);
        retval.setActiveCellCol(( short ) 0x0);
        retval.setActiveCellRow(( short ) 0x0);
        retval.setNumRefs(( short ) 0x0);
        return retval;
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
     * Returns the active column
     *
     * @see org.apache.poi.hssf.record.SelectionRecord
     * @return row the active column index
     */
    public short getActiveCellCol()
    {
        if (selection == null)
        {
            return (short) 0;
        }
        return selection.getActiveCellCol();
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

    protected Record createMergedCells()
    {
        MergeCellsRecord retval = new MergeCellsRecord();
        retval.setNumAreas(( short ) 0);
        return retval;
    }

    /**
     * creates the EOF record
     * @see org.apache.poi.hssf.record.EOFRecord
     * @see org.apache.poi.hssf.record.Record
     * @return record containing a EOFRecord
     */

    protected Record createEOF()
    {
        return new EOFRecord();
    }

    /**
     * get the location of the DimensionsRecord (which is the last record before the value section)
     * @return location in the array of records of the DimensionsRecord
     */

    public int getDimsLoc()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "getDimsLoc dimsloc= " + dimsloc);
        return dimsloc;
    }

    /**
     * in the event the record is a dimensions record, resets both the loc index and dimsloc index
     */

    public void checkDimsLoc(Record rec, int recloc)
    {
        if (rec.getSid() == DimensionsRecord.sid)
        {
            loc     = recloc;
            dimsloc = recloc;
        }
    }

    public int getSize()
    {
        int retval = 0;

        for (int k = 0; k < records.size(); k++)
        {
            retval += (( Record ) records.get(k)).getRecordSize();
        }
        return retval;
    }

    public List getRecords()
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
     * Returns the first occurance of a record matching a particular sid.
     */

    public Record findFirstRecordBySid(short sid)
    {
        for (Iterator iterator = records.iterator(); iterator.hasNext(); )
        {
            Record record = ( Record ) iterator.next();

            if (record.getSid() == sid)
            {
                return record;
            }
        }
        return null;
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
     * Finds the first occurance of a record matching a particular sid and
     * returns it's position.
     * @param sid   the sid to search for
     * @return  the record position of the matching record or -1 if no match
     *          is made.
     */
    public int findFirstRecordLocBySid( short sid )
    {
        int index = 0;
        for (Iterator iterator = records.iterator(); iterator.hasNext(); )
        {
            Record record = ( Record ) iterator.next();

            if (record.getSid() == sid)
            {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Returns the HeaderRecord.
     * @return HeaderRecord for the sheet.
     */
    public HeaderRecord getHeader ()
    {
	return header;
    }

    /**
     * Sets the HeaderRecord.
     * @param newHeader The new HeaderRecord for the sheet.
     */
    public void setHeader (HeaderRecord newHeader)
    {
    	header = newHeader;
    }

    /**
     * Returns the FooterRecord.
     * @return FooterRecord for the sheet.
     */
    public FooterRecord getFooter ()
    {
	    return footer;
    }

    /**
     * Sets the FooterRecord.
     * @param newFooter The new FooterRecord for the sheet.
     */
    public void setFooter (FooterRecord newFooter)
    {
	    footer = newFooter;
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
      * Gets the size of the margin in inches.
      * @param margin which margin to get
      * @return the size of the margin
      */
    public double getMargin(short margin) {
	if (getMargins()[margin] != null)
	    return margins[margin].getMargin();
	else {
	    switch ( margin )
		{
		case LeftMargin:
		    return .75;
		case RightMargin:
		    return .75;
		case TopMargin:
		    return 1.0;
		case BottomMargin:
		    return 1.0;
		default :
		    throw new RuntimeException( "Unknown margin constant:  " + margin );
		}
	}
    }

     /**
      * Sets the size of the margin in inches.
      * @param margin which margin to get
      * @param size the size of the margin
      */
    public void setMargin(short margin, double size) {
	Margin m = getMargins()[margin];
	if (m  == null) {
	    switch ( margin )
		{
		case LeftMargin:
		    m = new LeftMarginRecord();
		    records.add( getDimsLoc() + 1, m );
		    break;
		case RightMargin:
		    m = new RightMarginRecord();
		    records.add( getDimsLoc() + 1, m );
		    break;
		case TopMargin:
		    m = new TopMarginRecord();
		    records.add( getDimsLoc() + 1, m );
		    break;
		case BottomMargin:
		    m = new BottomMarginRecord();
		    records.add( getDimsLoc() + 1, m );
		    break;
		default :
		    throw new RuntimeException( "Unknown margin constant:  " + margin );
		}
	    margins[margin] = m;
	}
	m.setMargin( size );
    }

    public int getEofLoc()
    {
        return eofLoc;
    }

    /**
     * Creates a split (freezepane).
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int topRow, int leftmostColumn )
    {
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
     * Creates a split pane.
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

    public SelectionRecord getSelection()
    {
        return selection;
    }

    public void setSelection( SelectionRecord selection )
    {
        this.selection = selection;
    }

    /**
     * creates a Protect record with protect set to false.
     * @see org.apache.poi.hssf.record.ProtectRecord
     * @see org.apache.poi.hssf.record.Record
     * @return a ProtectRecord
     */
    protected Record createProtect()
    {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "create protect record with protection disabled");
        ProtectRecord retval = new ProtectRecord();

        retval.setProtect(false);
        // by default even when we support encryption we won't
        return retval;
    }

    public ProtectRecord getProtect()
    {
        return protect;
    }

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
     * Returns the array of margins.  If not created, will create.
     *
     * @return the array of marings.
     */
    protected Margin[] getMargins() {
        if (margins == null)
            margins = new Margin[4];
    	return margins;
    }

    public int aggregateDrawingRecords(DrawingManager drawingManager)
    {
        int loc = findFirstRecordLocBySid(DrawingRecord.sid);
        boolean noDrawingRecordsFound = loc == -1;
        if (noDrawingRecordsFound)
        {
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
            List records = getRecords();
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
            Record r = (Record) iterator.next();
            if (r instanceof EscherAggregate)
                r.getRecordSize();   // Trigger flatterning of user model and corresponding update of dgg record.
        }
    }

    /**
     * Shifts all the page breaks in the range "count" number of rows/columns
     * @param breaks The page record to be shifted
     * @param start Starting "main" value to shift breaks
     * @param stop Ending "main" value to shift breaks
     * @param count number of units (rows/columns) to shift by 
     */
    public void shiftBreaks(PageBreakRecord breaks, short start, short stop, int count) {
   	
    	if(rowBreaks == null)
    		return;
    	Iterator iterator = breaks.getBreaksIterator();
    	List shiftedBreak = new ArrayList();
    	while(iterator.hasNext()) 
    	{
    		PageBreakRecord.Break breakItem = (PageBreakRecord.Break)iterator.next();
    		short breakLocation = breakItem.main;
    		boolean inStart = (breakLocation >= start);
    		boolean inEnd = (breakLocation <= stop);
    		if(inStart && inEnd)
    			shiftedBreak.add(breakItem);
    	}
    	
    	iterator = shiftedBreak.iterator();
    	while (iterator.hasNext()) {    		
			PageBreakRecord.Break breakItem = (PageBreakRecord.Break)iterator.next();
    		breaks.removeBreak(breakItem.main);
    		breaks.addBreak((short)(breakItem.main+count), breakItem.subFrom, breakItem.subTo);
    	}
    }
    
    /**
     * Sets a page break at the indicated row
     * @param row
     */
    public void setRowBreak(int row, short fromCol, short toCol) {    	
    	rowBreaks.addBreak((short)row, fromCol, toCol);
    }

    /**
     * Removes a page break at the indicated row
     * @param row
     */
    public void removeRowBreak(int row) {
    	rowBreaks.removeBreak((short)row);
    }

    /**
     * Queries if the specified row has a page break
     * @param row
     * @return true if the specified row has a page break
     */
    public boolean isRowBroken(int row) {
    	return rowBreaks.getBreak((short)row) != null;
    }

    /**
     * Sets a page break at the indicated column
     *
     */
    public void setColumnBreak(short column, short fromRow, short toRow) {    	
    	colBreaks.addBreak(column, fromRow, toRow);
    }

    /**
     * Removes a page break at the indicated column
     *
     */
    public void removeColumnBreak(short column) {
    	colBreaks.removeBreak(column);
    }

    /**
     * Queries if the specified column has a page break
     *
     * @return true if the specified column has a page break
     */
    public boolean isColumnBroken(short column) {
    	return colBreaks.getBreak(column) != null;
    }
    
    /**
     * Shifts the horizontal page breaks for the indicated count
     * @param startingRow
     * @param endingRow
     * @param count
     */
    public void shiftRowBreaks(int startingRow, int endingRow, int count) {
    	shiftBreaks(rowBreaks, (short)startingRow, (short)endingRow, (short)count);
    }

    /**
     * Shifts the vertical page breaks for the indicated count
     * @param startingCol
     * @param endingCol
     * @param count
     */
    public void shiftColumnBreaks(short startingCol, short endingCol, short count) {
    	shiftBreaks(colBreaks, startingCol, endingCol, count);
    }
    
    /**
     * Returns all the row page breaks
     * @return
     */
    public Iterator getRowBreaks() {
    	return rowBreaks.getBreaksIterator();
    }
    
    /**
     * Returns the number of row page breaks
     * @return
     */
    public int getNumRowBreaks(){
    	return (int)rowBreaks.getNumBreaks();
    }
    
    /**
     * Returns all the column page breaks
     * @return
     */
    public Iterator getColumnBreaks(){
    	return colBreaks.getBreaksIterator();
    }
    
    /**
     * Returns the number of column page breaks
     * @return
     */
    public int getNumColumnBreaks(){
    	return (int)colBreaks.getNumBreaks();
    }

    public void setColumnGroupCollapsed( short columnNumber, boolean collapsed )
    {
        if (collapsed)
        {
            columns.collapseColumn( columnNumber );
        }
        else
        {
            columns.expandColumn( columnNumber );
        }
    }

//    private void collapseColumn( short columnNumber )
//    {
//        int idx = findColumnIdx( columnNumber, 0 );
//        if (idx == -1)
//            return;
//
//        // Find the start of the group.
//        ColumnInfoRecord columnInfo = (ColumnInfoRecord) columnSizes.get( findStartOfColumnOutlineGroup( idx ) );
//
//        // Hide all the columns until the end of the group
//        columnInfo = writeHidden( columnInfo, idx, true );
//
//        // Write collapse field
//        setColumn( (short) ( columnInfo.getLastColumn() + 1 ), null, null, null, Boolean.TRUE);
//    }

//    private void expandColumn( short columnNumber )
//    {
//        int idx = findColumnIdx( columnNumber, 0 );
//        if (idx == -1)
//            return;
//
//        // If it is already exapanded do nothing.
//        if (!isColumnGroupCollapsed(idx))
//            return;
//
//        // Find the start of the group.
//        int startIdx = findStartOfColumnOutlineGroup( idx );
//        ColumnInfoRecord columnInfo = getColInfo( startIdx );
//
//        // Find the end of the group.
//        int endIdx = findEndOfColumnOutlineGroup( idx );
//        ColumnInfoRecord endColumnInfo = getColInfo( endIdx );
//
//        // expand:
//        // colapsed bit must be unset
//        // hidden bit gets unset _if_ surrounding groups are expanded you can determine
//        //   this by looking at the hidden bit of the enclosing group.  You will have
//        //   to look at the start and the end of the current group to determine which
//        //   is the enclosing group
//        // hidden bit only is altered for this outline level.  ie.  don't uncollapse contained groups
//        if (!isColumnGroupHiddenByParent( idx ))
//        {
//            for (int i = startIdx; i <= endIdx; i++)
//            {
//                if (columnInfo.getOutlineLevel() == getColInfo(i).getOutlineLevel())
//                    getColInfo(i).setHidden( false );
//            }
//        }
//
//        // Write collapse field
//        setColumn( (short) ( columnInfo.getLastColumn() + 1 ), null, null, null, Boolean.FALSE);
//    }

//    private boolean isColumnGroupCollapsed( int idx )
//    {
//        int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup( idx );
//        if (endOfOutlineGroupIdx >= columnSizes.size())
//            return false;
//        if (getColInfo(endOfOutlineGroupIdx).getLastColumn() + 1 != getColInfo(endOfOutlineGroupIdx + 1).getFirstColumn())
//            return false;
//        else
//            return getColInfo(endOfOutlineGroupIdx+1).getCollapsed();
//    }

//    private boolean isColumnGroupHiddenByParent( int idx )
//    {
//        // Look out outline details of end
//        int endLevel;
//        boolean endHidden;
//        int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup( idx );
//        if (endOfOutlineGroupIdx >= columnSizes.size())
//        {
//            endLevel = 0;
//            endHidden = false;
//        }
//        else if (getColInfo(endOfOutlineGroupIdx).getLastColumn() + 1 != getColInfo(endOfOutlineGroupIdx + 1).getFirstColumn())
//        {
//            endLevel = 0;
//            endHidden = false;
//        }
//        else
//        {
//            endLevel = getColInfo( endOfOutlineGroupIdx + 1).getOutlineLevel();
//            endHidden = getColInfo( endOfOutlineGroupIdx + 1).getHidden();
//        }
//
//        // Look out outline details of start
//        int startLevel;
//        boolean startHidden;
//        int startOfOutlineGroupIdx = findStartOfColumnOutlineGroup( idx );
//        if (startOfOutlineGroupIdx <= 0)
//        {
//            startLevel = 0;
//            startHidden = false;
//        }
//        else if (getColInfo(startOfOutlineGroupIdx).getFirstColumn() - 1 != getColInfo(startOfOutlineGroupIdx - 1).getLastColumn())
//        {
//            startLevel = 0;
//            startHidden = false;
//        }
//        else
//        {
//            startLevel = getColInfo( startOfOutlineGroupIdx - 1).getOutlineLevel();
//            startHidden = getColInfo( startOfOutlineGroupIdx - 1 ).getHidden();
//        }
//
//        if (endLevel > startLevel)
//        {
//            return endHidden;
//        }
//        else
//        {
//            return startHidden;
//        }
//    }

//    private ColumnInfoRecord getColInfo(int idx)
//    {
//        return columns.getColInfo( idx );
//    }

//    private int findStartOfColumnOutlineGroup(int idx)
//    {
//        // Find the start of the group.
//        ColumnInfoRecord columnInfo = (ColumnInfoRecord) columnSizes.get( idx );
//        int level = columnInfo.getOutlineLevel();
//        while (idx != 0)
//        {
//            ColumnInfoRecord prevColumnInfo = (ColumnInfoRecord) columnSizes.get( idx - 1 );
//            if (columnInfo.getFirstColumn() - 1 == prevColumnInfo.getLastColumn())
//            {
//                if (prevColumnInfo.getOutlineLevel() < level)
//                {
//                    break;
//                }
//                idx--;
//                columnInfo = prevColumnInfo;
//            }
//            else
//            {
//                break;
//            }
//        }
//
//        return idx;
//    }

//    private int findEndOfColumnOutlineGroup(int idx)
//    {
//        // Find the end of the group.
//        ColumnInfoRecord columnInfo = (ColumnInfoRecord) columnSizes.get( idx );
//        int level = columnInfo.getOutlineLevel();
//        while (idx < columnSizes.size() - 1)
//        {
//            ColumnInfoRecord nextColumnInfo = (ColumnInfoRecord) columnSizes.get( idx + 1 );
//            if (columnInfo.getLastColumn() + 1 == nextColumnInfo.getFirstColumn())
//            {
//                if (nextColumnInfo.getOutlineLevel() < level)
//                {
//                    break;
//                }
//                idx++;
//                columnInfo = nextColumnInfo;
//            }
//            else
//            {
//                break;
//            }
//        }
//
//        return idx;
//    }

    public void groupRowRange(int fromRow, int toRow, boolean indent)
    {
        checkRows();
        for (int rowNum = fromRow; rowNum <= toRow; rowNum++)
        {
            RowRecord row = getRow( rowNum );
            if (row == null)
            {
                row = createRow( rowNum );
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
        Iterator iterator = rows.getIterator();
        while ( iterator.hasNext() )
        {
            RowRecord rowRecord = (RowRecord) iterator.next();
            maxLevel = Math.max(rowRecord.getOutlineLevel(), maxLevel);
        }

        GutsRecord guts = (GutsRecord) findFirstRecordBySid( GutsRecord.sid );
        guts.setRowLevelMax( (short) ( maxLevel + 1 ) );
        guts.setLeftRowGutter( (short) ( 29 + (12 * (maxLevel)) ) );
    }

    public void setRowGroupCollapsed( int row, boolean collapse )
    {
        if (collapse)
        {
            rows.collapseRow( row );
        }
        else
        {
            rows.expandRow( row );
        }
    }


//    private void collapseRow( int rowNumber )
//    {
//
//        // Find the start of the group.
//        int startRow = rows.findStartOfRowOutlineGroup( rowNumber );
//        RowRecord rowRecord = (RowRecord) rows.getRow( startRow );
//
//        // Hide all the columns until the end of the group
//        int lastRow = rows.writeHidden( rowRecord, startRow, true );
//
//        // Write collapse field
//        if (getRow(lastRow + 1) != null)
//        {
//            getRow(lastRow + 1).setColapsed( true );
//        }
//        else
//        {
//            RowRecord row = createRow( lastRow + 1);
//            row.setColapsed( true );
//            rows.insertRow( row );
//        }
//    }

//    private int findStartOfRowOutlineGroup(int row)
//    {
//        // Find the start of the group.
//        RowRecord rowRecord = rows.getRow( row );
//        int level = rowRecord.getOutlineLevel();
//        int currentRow = row;
//        while (rows.getRow( currentRow ) != null)
//        {
//            rowRecord = rows.getRow( currentRow );
//            if (rowRecord.getOutlineLevel() < level)
//                return currentRow + 1;
//            currentRow--;
//        }
//
//        return currentRow + 1;
//    }

//    private int writeHidden( RowRecord rowRecord, int row, boolean hidden )
//    {
//        int level = rowRecord.getOutlineLevel();
//        while (rowRecord != null && rows.getRow(row).getOutlineLevel() >= level)
//        {
//            rowRecord.setZeroHeight( hidden );
//            row++;
//            rowRecord = rows.getRow( row );
//        }
//        return row - 1;
//    }

//    private int findEndOfRowOutlineGroup( int row )
//    {
//        int level = getRow( row ).getOutlineLevel();
//        int currentRow;
//        for (currentRow = row; currentRow < rows.getLastRowNum(); currentRow++)
//        {
//            if (getRow(currentRow) == null || getRow(currentRow).getOutlineLevel() < level)
//            {
//                break;
//            }
//        }
//
//        return currentRow-1;
//    }

//    private boolean isRowGroupCollapsed( int row )
//    {
//        int collapseRow = rows.findEndOfRowOutlineGroup( row ) + 1;
//
//        if (getRow(collapseRow) == null)
//            return false;
//        else
//            return getRow( collapseRow ).getColapsed();
//    }


//    private boolean isRowGroupHiddenByParent( int row )
//    {
//        // Look out outline details of end
//        int endLevel;
//        boolean endHidden;
//        int endOfOutlineGroupIdx = rows.findEndOfRowOutlineGroup( row );
//        if (getRow( endOfOutlineGroupIdx + 1 ) == null)
//        {
//            endLevel = 0;
//            endHidden = false;
//        }
//        else
//        {
//            endLevel = getRow( endOfOutlineGroupIdx + 1).getOutlineLevel();
//            endHidden = getRow( endOfOutlineGroupIdx + 1).getZeroHeight();
//        }
//
//        // Look out outline details of start
//        int startLevel;
//        boolean startHidden;
//        int startOfOutlineGroupIdx = rows.findStartOfRowOutlineGroup( row );
//        if (startOfOutlineGroupIdx - 1 < 0 || getRow(startOfOutlineGroupIdx - 1) == null)
//        {
//            startLevel = 0;
//            startHidden = false;
//        }
//        else
//        {
//            startLevel = getRow( startOfOutlineGroupIdx - 1).getOutlineLevel();
//            startHidden = getRow( startOfOutlineGroupIdx - 1 ).getZeroHeight();
//        }
//
//        if (endLevel > startLevel)
//        {
//            return endHidden;
//        }
//        else
//        {
//            return startHidden;
//        }
//    }

}
