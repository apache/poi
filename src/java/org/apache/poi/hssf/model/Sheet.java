
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.model;

import java.io.OutputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.hssf
    .record.*;       // normally I don't do this, buy we literally mean ALL
import org.apache.poi.hssf.record.formula.FormulaUtil;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.IntList;
import org.apache.poi.util.POILogger;
import org.apache.poi.hssf.record
    .aggregates.*;   // normally I don't do this, buy we literally mean ALL

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
 * @see org.apache.poi.hssf.model.Workbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 * @version 1.0-pre
 */

public class Sheet
    extends java.lang.Object
{
    protected ArrayList              records        = null;
    int                              preoffset      =
        0;      // offset of the sheet in a new file
    int                              loc            = 0;
    protected boolean                containsLabels = false;
    ;
    protected int                    dimsloc        = 0;
    protected DimensionsRecord       dims;
    protected DefaultColWidthRecord  defaultcolwidth  = null;
    protected DefaultRowHeightRecord defaultrowheight = null;
    protected GridsetRecord          gridset          = null;
    protected MergeCellsRecord       merged           = null;
    protected int                    mergedloc        = 0;
    private static POILogger         log              =
        POILogFactory.getLogger(Sheet.class);
    private ArrayList                columnSizes      =
        null;   // holds column info
    protected ValueRecordsAggregate  cells            = null;
    protected RowRecordsAggregate    rows             = null;
    private Iterator                 valueRecIterator = null;
    private Iterator                 rowRecIterator   = null;

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
        log.logFormatted(log.DEBUG,
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
                log.log(log.DEBUG, "Hit label record");
                retval.containsLabels = true;
            }
            else if (rec.getSid() == BOFRecord.sid)
            {
                bofEofNestingLevel++;
            }
            else if ((rec.getSid() == EOFRecord.sid)
                     && (--bofEofNestingLevel == 0))
            {
                log.log(log.DEBUG, "Hit EOF record at ");
                records.add(rec);
                break;
            }
            else if (rec.getSid() == DimensionsRecord.sid)
            {
                retval.dims    = ( DimensionsRecord ) rec;
                retval.dimsloc = records.size();
            }
            else if (rec.getSid() == MergeCellsRecord.sid)
            {
                retval.merged    = ( MergeCellsRecord ) rec;
                retval.mergedloc = k - offset;
            }
            else if (rec.getSid() == ColumnInfoRecord.sid)
            {
                if (retval.columnSizes == null)
                {
                    retval.columnSizes = new ArrayList();
                }
                retval.columnSizes.add(( ColumnInfoRecord ) rec);
            }
            else if (rec.getSid() == DefaultColWidthRecord.sid)
            {
                retval.defaultcolwidth = ( DefaultColWidthRecord ) rec;
            }
            else if (rec.getSid() == DefaultRowHeightRecord.sid)
            {
                retval.defaultrowheight = ( DefaultRowHeightRecord ) rec;
            }
            else if (rec.isValue())
            {
                if (isfirstcell)
                {
                    retval.cells = new ValueRecordsAggregate();
                    rec          = retval.cells;
                    retval.cells.construct(k, recs);
                    isfirstcell = false;
                }
                else
                {
                    rec = null;
                }
            }
            else if (rec.getSid() == RowRecord.sid)
            {
                if (isfirstrow)
                {
                    retval.rows = new RowRecordsAggregate();
                    rec         = retval.rows;
                    retval.rows.construct(k, recs);
                    isfirstrow = false;
                }
                else
                {
                    rec = null;
                }
            }
            if (rec != null)
            {
                records.add(rec);
            }
        }
        retval.records = records;
        log.log(log.DEBUG, "sheet createSheet (existing file) exited");
        return retval;
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
        log.log(log.DEBUG,
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
        log.log(log.DEBUG, "Sheet createsheet from scratch called");
        Sheet     retval  = new Sheet();
        ArrayList records = new ArrayList(30);

        records.add(retval.createBOF());

        // records.add(retval.createIndex());
        records.add(retval.createCalcMode());
        records.add(retval.createCalcCount());
        records.add(retval.createRefMode());
        records.add(retval.createIteration());
        records.add(retval.createDelta());
        records.add(retval.createSaveRecalc());
        records.add(retval.createPrintHeaders());
        records.add(retval.createPrintGridlines());
        retval.gridset = ( GridsetRecord ) retval.createGridset();
        records.add(retval.gridset);
        records.add(retval.createGuts());
        retval.defaultrowheight =
            ( DefaultRowHeightRecord ) retval.createDefaultRowHeight();
        records.add(retval.defaultrowheight);
        records.add(retval.createWSBool());
        records.add(retval.createHeader());
        records.add(retval.createFooter());
        records.add(retval.createHCenter());
        records.add(retval.createVCenter());
        records.add(retval.createPrintSetup());
        retval.defaultcolwidth =
            ( DefaultColWidthRecord ) retval.createDefaultColWidth();
        records.add(retval.defaultcolwidth);
        retval.dims    = ( DimensionsRecord ) retval.createDimensions();
        retval.dimsloc = 19;
        records.add(retval.dims);
        records.add(retval.createWindowTwo());
        retval.setLoc(records.size() - 1);
        records.add(retval.createSelection());
        records.add(retval.createEOF());
        retval.records = records;
        log.log(log.DEBUG, "Sheet createsheet from scratch exit");
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

    public int addMergedRegion(short rowFrom, short colFrom, short rowTo,
                               short colTo)
    {
        if (merged == null)
        {
            merged    = ( MergeCellsRecord ) createMergedCells();
            mergedloc = records.size() - 1;
            records.add(records.size() - 1, merged);
        }
        return merged.addArea(rowFrom, colFrom, rowTo, colTo);
    }

    public void removeMergedRegion(int index)
    {
        merged.removeAreaAt(index);
        if (merged.getNumAreas() == 0)
        {
            merged = null;
            records.remove(mergedloc);
            mergedloc = 0;
        }
    }

    public MergeCellsRecord.MergedRegion getMergedRegionAt(int index)
    {
        return merged.getAreaAt(index);
    }

    public int getNumMergedRegions()
    {
        return merged.getNumAreas();
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
        log.log(log.DEBUG, "convertLabelRecords called");
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
        log.log(log.DEBUG, "convertLabelRecords exit");
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
        log.log(log.DEBUG, "Sheet.getNumRecords");
        log.logFormatted(log.DEBUG, "returning % + % + % - 2 = %", new int[]
        {
            records.size(), cells.getPhysicalNumberOfCells(),
            rows.getPhysicalNumberOfRows(),
            records.size() + cells.getPhysicalNumberOfCells()
            + rows.getPhysicalNumberOfRows() - 2
        });
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

    public void setDimensions(short firstrow, short firstcol, short lastrow,
                              short lastcol)
    {
        log.log(log.DEBUG, "Sheet.setDimensions");
        log.log(log.DEBUG,
                (new StringBuffer("firstrow")).append(firstrow)
                    .append("firstcol").append(firstcol).append("lastrow")
                    .append(lastrow).append("lastcol").append(lastcol)
                    .toString());
        dims.setFirstCol(firstcol);
        dims.setFirstRow(firstrow);
        dims.setLastCol(lastcol);
        dims.setLastRow(lastrow);
        log.log(log.DEBUG, "Sheet.setDimensions exiting");
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
        log.log(log.DEBUG, "sheet.setLoc(): " + loc);
        this.loc = loc;
    }

    /**
     * Returns the location pointer to the first record to look for when adding rows/values
     *
     */

    public int getLoc()
    {
        log.log(log.DEBUG, "sheet.getLoc():" + loc);
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
        log.log(log.DEBUG, "Sheet.serialize");

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
        // log.debug((new StringBuffer("arraysize=")).append(arraysize)
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
        log.log(log.DEBUG, "Sheet.serialize returning " + retval);
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
        log.log(log.DEBUG, "Sheet.serialize using offsets");

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
        // log.debug((new StringBuffer("arraysize=")).append(arraysize)
        // .toString());
        // }
        for (int k = 0; k < records.size(); k++)
        {

            // byte[] rec = (( byte [] ) bytes.get(k));
            // System.arraycopy(rec, 0, data, offset + pos, rec.length);
            pos += (( Record ) records.get(k)).serialize(pos + offset,
                    data);   // rec.length;
        }
        log.log(log.DEBUG, "Sheet.serialize returning ");
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
        log.log(log.DEBUG, "create row number " + row);
        RowRecord rowrec = new RowRecord();

        rowrec.setRowNumber(( short ) row);
        rowrec.setHeight(( short ) 0xff);
        rowrec.setOptimize(( short ) 0x0);
        rowrec.setOptionFlags(( short ) 0x0);
        rowrec.setXFIndex(( short ) 0x0);
        return rowrec;
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

    public LabelSSTRecord createLabelSST(short row, short col, int index)
    {
        log.logFormatted(log.DEBUG, "create labelsst row,col,index %,%,%",
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

    public NumberRecord createNumber(short row, short col, double value)
    {
        log.logFormatted(log.DEBUG, "create number row,col,value %,%,%",
                         new double[]
        {
            row, col, value
        });
        NumberRecord rec = new NumberRecord();

        rec.setRow(( short ) row);
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

    public BlankRecord createBlank(short row, short col)
    {
        log.logFormatted(log.DEBUG, "create blank row,col %,%", new short[]
        {
            row, col
        });
        BlankRecord rec = new BlankRecord();

        rec.setRow(( short ) row);
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

    public FormulaRecord createFormula(short row, short col, String formula)
    {
        log.logFormatted(log.DEBUG, "create formula row,col,formula %,%,%",
                         new short[]
        {
            row, col
        }, formula);
        FormulaRecord rec = new FormulaRecord();

        rec.setRow(row);
        rec.setColumn(col);
        rec.setOptions(( short ) 2);
        rec.setValue(0);
        rec.setXFIndex(( short ) 0x0f);
        Ptg[] ptg  = FormulaUtil.parseFormula(formula);
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

    public void addValueRecord(short row, CellValueRecordInterface col)
    {
        checkCells();
        log.logFormatted(log.DEBUG, "add value record  row,loc %,%", new int[]
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

    public void removeValueRecord(short row, CellValueRecordInterface col)
    {
        checkCells();
        log.logFormatted(log.DEBUG, "remove value record row,dimsloc %,%",
                         new int[]
        {
            row, dimsloc
        });
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
        log.log(log.DEBUG, "replaceValueRecord ");
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
        log.log(log.DEBUG, "addRow ");
        DimensionsRecord d = ( DimensionsRecord ) records.get(getDimsLoc());

        if (row.getRowNumber() > d.getLastRow())
        {
            d.setLastRow(row.getRowNumber() + 1);
        }
        if (row.getRowNumber() < d.getFirstRow())
        {
            d.setFirstRow(row.getRowNumber());
        }
        //IndexRecord index = null;

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
        log.log(log.DEBUG, "exit addRow");
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
        log.log(log.DEBUG, "getNextValue loc= " + loc);
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
        log.debug((new StringBuffer("getNextRow loc= ")).append(loc)
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
        log.log(log.DEBUG, "getNextRow loc= " + loc);
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

    public RowRecord getRow(short rownum)
    {
        log.log(log.DEBUG, "getNextRow loc= " + loc);
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

        retval.setMode(retval.USE_A1_MODE);
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

        retval.setMaxChange((( double ) 0.0010));
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
        retval.setHeaderMargin(( double ) 0.5);
        retval.setFooterMargin(( double ) 0.5);
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
        ColumnInfoRecord retval = new ColumnInfoRecord();

        retval.setColumnWidth(( short ) 0x8);
        retval.setOptions(( short ) 6);
        retval.setXFIndex(( short ) 0x0f);
        return retval;
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
        int              k      = 0;

        if (columnSizes != null)
        {
            for (k = 0; k < columnSizes.size(); k++)
            {
                ci = ( ColumnInfoRecord ) columnSizes.get(k);
                if ((ci.getFirstColumn() >= column)
                        && (ci.getLastColumn() <= column))
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
        ColumnInfoRecord ci = null;
        int              k  = 0;

        if (columnSizes == null)
        {
            columnSizes = new ArrayList();
        }
        //int cioffset = getDimsLoc() - columnSizes.size();

        for (k = 0; k < columnSizes.size(); k++)
        {
            ci = ( ColumnInfoRecord ) columnSizes.get(k);
            if ((ci.getFirstColumn() >= column)
                    && (ci.getLastColumn() <= column))
            {
                break;
            }
            ci = null;
        }
        if (ci != null)
        {
            if (ci.getColumnWidth() == width)
            {

                // do nothing...the cell's width is equal to what we're setting it to.
            }
            else if ((ci.getFirstColumn() == column)
                     && (ci.getLastColumn() == column))
            {                               // if its only for this cell then
                ci.setColumnWidth(width);   // who cares, just change the width
            }
            else if ((ci.getFirstColumn() == column)
                     || (ci.getLastColumn() == column))
            {

                // okay so the width is different but the first or last column == the column we'return setting
                // we'll just divide the info and create a new one
                if (ci.getFirstColumn() == column)
                {
                    ci.setFirstColumn(( short ) (column + 1));
                }
                else
                {
                    ci.setLastColumn(( short ) (column - 1));
                }
                ColumnInfoRecord nci = ( ColumnInfoRecord ) createColInfo();

                nci.setFirstColumn(column);
                nci.setLastColumn(column);
                nci.setOptions(ci.getOptions());
                nci.setXFIndex(ci.getXFIndex());
                nci.setColumnWidth(width);
                columnSizes.add(k, nci);
                records.add((1 + getDimsLoc() - columnSizes.size()) + k, nci);
                dimsloc++;
            }
        }
        else
        {

            // okay so there ISN'T a column info record that cover's this column so lets create one!
            ColumnInfoRecord nci = ( ColumnInfoRecord ) createColInfo();

            nci.setFirstColumn(column);
            nci.setLastColumn(column);
            nci.setColumnWidth(width);
            columnSizes.add(k, nci);
            records.add((1 + getDimsLoc() - columnSizes.size()) + k, nci);
            dimsloc++;
        }
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

    protected Record createWindowTwo()
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
        log.log(log.DEBUG, "getDimsLoc dimsloc= " + dimsloc);
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
}
