
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

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.usermodel.HSSFCell; //kludge shouldn't refer to this

import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.DoubleList;
import org.apache.poi.util.IntList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * Aggregate value records together.  Things are easier to handle that way.
 *
 * @author  Andrew C. Oliver
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class ValueRecordsAggregate
    extends Record
{
    public final static short sid       = -1000;
    int                       firstcell = -1;
    int                       lastcell  = -1;
    //TreeMap                   records   = null;

    private final static int DEFAULT_ROWS=10000;
    private final static int DEFAULT_COLS=256;

    List celltype = null;  //array of HSSFCEll.CELL_TYPE_XXX tells us which arrays to use
    List xfs      = null;  // array of style types.  Index of XF record
    List numericcells = null; // numeric and Shared string indicies.  
    List formulaptgs = null; // array of arrays of PTGS
    List stringvals = null;  // array of actual string/formula string vals
    IntList populatedRows = null;  //indicies of populated rows
    int physCells; //physical number of cells

    public CellValueRecordInterface getCell(int row, short col) {
        return constructRecord(row, col);

    }

    public int getRecordSize() {
        //throw new RuntimeException("Not Implemented getRecordSize");

        int size = 0;
        Iterator irecs = getIterator();

        while (irecs.hasNext()) {
                size += (( Record ) irecs.next()).getRecordSize();
        }

        return size;
//        return size;
    }

    public int serialize(int offset, byte [] data)
    {
        throw new RuntimeException("This method shouldnt be called. ValueRecordsAggregate.serializeCellRow() should be called from RowRecordsAggregate.");
    }

    public ValueRecordsAggregate() {
        celltype = new ArrayList(DEFAULT_ROWS);
        xfs      = new ArrayList(DEFAULT_ROWS);
        numericcells = new ArrayList(DEFAULT_ROWS);
        formulaptgs  = new ArrayList(DEFAULT_ROWS);
        stringvals   = new ArrayList(DEFAULT_ROWS);
        populatedRows = new IntList(DEFAULT_ROWS);
        physCells = 0;
    }

    public Iterator getIterator() {
      return new VRAIterator(this);
    }

    /** Tallies a count of the size of the cell records
     *  that are attached to the rows in the range specified.
     */
    public int getRowCellBlockSize(int startRow, int endRow) {
      Iterator cellRec = new VRAIterator(this, startRow, endRow);;
      int size = 0;
      while (cellRec.hasNext()) {
        CellValueRecordInterface cell = (CellValueRecordInterface)cellRec.next();
        int row = cell.getRow();
        if ((row >=startRow) && (row <= endRow))
          size += ((Record)cell).getRecordSize();
      }
      return size;
    }

    /** Returns true if the row has cells attached to it */
    public boolean rowHasCells(int row) {
      IntList ctRow = (IntList) celltype.get(row);
      return ((ctRow != null) && (ctRow.size() > 0));
    }

    /** Serializes the cells that are allocated to a certain row range*/
    public int serializeCellRow(final int row, int offset, byte [] data)
    {
        Iterator itr = new VRAIterator(this, row);
        int      pos = offset;

        while (itr.hasNext())
        {
            CellValueRecordInterface cell = (CellValueRecordInterface)itr.next();
            pos += (( Record ) cell).serialize(pos, data);
        }
        return pos - offset;
    }



    public int construct(int offset, List records)
    {
        int k = 0;

        FormulaRecordAggregate lastFormulaAggregate = null;

        for (k = offset; k < records.size(); k++)
        {
            Record rec = ( Record ) records.get(k);

            if (rec instanceof StringRecord == false && !rec.isInValueSection() && !(rec instanceof UnknownRecord))
            {
                break;
            }
            if (rec instanceof FormulaRecord)
            {
                lastFormulaAggregate = new FormulaRecordAggregate((FormulaRecord)rec, null);
                insertCell( lastFormulaAggregate );
            }
            else if (rec instanceof StringRecord)
            {
                lastFormulaAggregate.setStringRecord((StringRecord)rec);
            } 
            else if (rec instanceof SharedFormulaRecord) {
            	lastFormulaAggregate.setSharedFormulaRecord((SharedFormulaRecord)rec);
            }
            else if (rec.isValue())
            {
                insertCell(( CellValueRecordInterface ) rec);
            }
        }
        return k;
    }

    public int getPhysicalNumberOfCells() {
        return physCells;
    }

    public int getPhysicalNumberOfCellsInRow(int row) {
        int count = -1;
        int col = -1;
        boolean firsttime = true;

        while (col > 0 || count == -1) {
            col = findNextPopulatedCell(row,col);
            count++;
        }
        return count;
    }

    public void setValue(int row, short cell, double val) {
        ((DoubleList)numericcells.get(row)).set(cell, val);
    }

    public void setStyle(int row, short cell, short xf) {
        ((IntList)xfs.get(row)).set(cell, xf);
    }


    public Iterator getRowCellIterator(int row) {
        return new VRAIterator(this, row);
    }

    public void removeRow(int row) {
        Iterator iterator = this.getRowCellIterator(row);
        while(iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    public void removeCell(CellValueRecordInterface cell) {
        int rownum = cell.getRow();
        int colnum = cell.getColumn();
        int xf     = cell.getXFIndex();
        int type   = determineType(cell);

        if (rownum < celltype.size() && colnum < ((IntList)celltype.get(rownum)).size()) {
            IntList ctRow = (IntList)celltype.get(rownum);
            if (ctRow.size()-1 == colnum) {
                ctRow.remove(colnum);
                if (ctRow.size() == 0 && celltype.size()-1 == rownum) {
                    celltype.remove(rownum);
                    int remp = populatedRows.indexOf(rownum);
                    System.err.println("remp == "+remp);
                    populatedRows.removeValue(rownum);
                }
            } else {
                ctRow.set(colnum,-1);
            }
            physCells--;
        } else {
          //this cell doesn't exist...
            throw new RuntimeException("Tried to remove a cell that does not exist r,c="+rownum+","+colnum);
        }
    }

    public void insertCell(CellValueRecordInterface cell) {
        int rownum = cell.getRow();
        int colnum = cell.getColumn();
        int xf     = cell.getXFIndex();
        int type   = determineType(cell);

        if (celltype.size() < rownum+1) {
            populatedRows.add(rownum); //this means we must never have had this row inserted
        }

        ensureRows(rownum);

        IntList ctRow = (IntList)celltype.get(rownum);
        IntList xfRow = (IntList)xfs.get(rownum);


        adjustIntList(ctRow, colnum+1);
        adjustIntList(xfRow, colnum+1);

        ctRow.set(colnum, type);
        xfRow.set(colnum, xf);

        insertCell(cell, type);
    }

    CellValueRecordInterface constructRecord(int row, int col) {
        if (celltype.size() < row || ((IntList)celltype.get(row)).size() < col) {
            throw new ArrayIndexOutOfBoundsException("constructRecord called with row = "+row+
                      "and col ="+col+" but there are only "+celltype.size()+" rows and "+
                      ((IntList)celltype.get(row)).size()+" cols!!");
        }

        CellValueRecordInterface retval = null;
        int type = ((IntList)celltype.get(row)).get(col);


        switch (type) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                NumberRecord nrecord = new NumberRecord();
                nrecord.setColumn((short)col);
                nrecord.setRow(row);
                nrecord.setValue(((DoubleList)numericcells.get(row)).get(col));
                nrecord.setXFIndex((short)((IntList)xfs.get(row)).get(col));
                retval = nrecord;
                break;
            case HSSFCell.CELL_TYPE_STRING:
                LabelSSTRecord srecord = new LabelSSTRecord();
                srecord.setColumn((short)col);
                srecord.setRow(row);
                srecord.setSSTIndex((int)((DoubleList)numericcells.get(row)).get(col));
                srecord.setXFIndex((short)((IntList)xfs.get(row)).get(col));
                retval=srecord;
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                BlankRecord brecord = new BlankRecord();
                brecord.setColumn((short)col);
                brecord.setRow(row);
                brecord.setXFIndex((short)((IntList)xfs.get(row)).get(col));
                retval=brecord;
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                FormulaRecord fr = new FormulaRecord();
                fr.setColumn((short)col);
                fr.setOptions((short)2);

                fr.setRow(row);
                fr.setXFIndex((short)((IntList)xfs.get(row)).get(col));
                StringRecord        st = null;
                String strval = (String)((List)stringvals.get(row)).get(col);
                List expressionlist =  (List)((List)formulaptgs.get(row)).get(col);
                fr.setParsedExpression(expressionlist);
                fr.setExpressionLength(calculatePtgSize(expressionlist));
                if (strval != null) {
                  st = new StringRecord();
                   st.setString(strval);
                }
                FormulaRecordAggregate frarecord = new FormulaRecordAggregate(fr,st);

                retval= frarecord;
                break;

            default:
                throw new RuntimeException("UnImplemented Celltype "+type);
        }

        return retval;
    }

    private short calculatePtgSize(List expressionlist) {
        short retval = 0;
        Iterator iter = expressionlist.iterator();
        while (iter.hasNext()) {
            retval += (short)((Ptg)iter.next()).getSize();
        }
        return retval;
    }

    private void insertCell(CellValueRecordInterface cell, int type) {
        int rownum = cell.getRow();
        int colnum = cell.getColumn();

        DoubleList  nmRow = (DoubleList)numericcells.get(rownum);

        switch (type) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                NumberRecord nrecord = (NumberRecord)cell;
                adjustDoubleList(nmRow, colnum+1);
                nmRow.set(colnum,nrecord.getValue());
                physCells++;
                break;
            case HSSFCell.CELL_TYPE_STRING:
                LabelSSTRecord srecord = (LabelSSTRecord)cell;
                adjustDoubleList(nmRow, colnum+1);
                nmRow.set(colnum,srecord.getSSTIndex());
                physCells++;
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                List ptRow = (List)formulaptgs.get(rownum);
                List stRow = (List)stringvals.get(rownum);
                FormulaRecordAggregate frarecord = (FormulaRecordAggregate)cell;
                adjustDoubleList(nmRow, colnum+1);
                adjustObjectList(ptRow, colnum+1);
                adjustStringList(stRow, colnum+1);
                nmRow.set(colnum,frarecord.getFormulaRecord().getValue());
                ptRow.set(colnum,frarecord.getFormulaRecord().getParsedExpression());
                StringRecord str = frarecord.getStringRecord();
                if (str != null) {
                    stRow.set(colnum,str.getString());
                } else {
                    stRow.set(colnum,null);
                }
                physCells++;
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                //BlankRecord brecord = (BlankRecord)cell;
                physCells++;
                break;

            default:
                throw new RuntimeException("UnImplemented Celltype "+cell.toString());
        }
    }

    private int determineType(CellValueRecordInterface cval)
    {
        Record record = ( Record ) cval;
        int    sid    = record.getSid();
        int    retval = 0;

        switch (sid)
        {

            case NumberRecord.sid :
                retval = HSSFCell.CELL_TYPE_NUMERIC;
                break;

            case BlankRecord.sid :
                retval = HSSFCell.CELL_TYPE_BLANK;
                break;

            case LabelSSTRecord.sid :
                retval = HSSFCell.CELL_TYPE_STRING;
                break;

            case FormulaRecordAggregate.sid :
                retval = HSSFCell.CELL_TYPE_FORMULA;
                break;

            case BoolErrRecord.sid :
                BoolErrRecord boolErrRecord = ( BoolErrRecord ) record;

                retval = (boolErrRecord.isBoolean())
                         ? HSSFCell.CELL_TYPE_BOOLEAN
                         : HSSFCell.CELL_TYPE_ERROR;
                break;
        }
        return retval;
    }

    private void ensureRows(int rownum) {
        adjustRows(celltype, rownum+1, IntList.class);
        adjustRows(xfs, rownum+1, IntList.class);
        adjustRows(numericcells, rownum+1, DoubleList.class);
        adjustRows(formulaptgs, rownum+1, ArrayList.class);
        adjustRows(stringvals, rownum+1, ArrayList.class);

    }

    private void adjustRows(List list, int size, Class theclass) {
        while (list.size() < size) {
            try {
                list.add(theclass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Could Not Instantiate Row in adjustRows");
            }
        }
    }

    private void adjustIntList(IntList list, int size) {
        while (list.size() < size) {
            list.add(-1);
        }
    }

    private void adjustDoubleList(DoubleList list, int size) {
        while (list.size() < size) {
            list.add(-1);
        }
    }

    private void adjustObjectList(List list, int size) {
        while (list.size() < size) {
            list.add(new ArrayList());
        }
    }

    private void adjustStringList(List list, int size) {
        while (list.size() < size) {
            list.add(new String());
        }
    }


    protected int findNextPopulatedCell(int row, int col) {

        IntList ctRow = (IntList) celltype.get(row);
        int retval = -1;
        if (ctRow.size() > col+1) {
            for (int k = col+1; k < ctRow.size() +1; k++) {

                if (k != ctRow.size()) {
                   int val = ctRow.get(k);

                   if (val != -1) {
                       retval = k;
                       break;
                   }  // end if (val !=...

                } //end if (k !=..

            }   //end for

        }  //end if (ctRow.size()...
        return retval;
    }



    public short getSid() {
      return sid;
    }


    public void fillFields(byte[] data, short size, int offset) {

    }

    protected void validateSid(short sid) {

    }


}

class VRAIterator implements Iterator {
    private boolean hasNext;
    private ValueRecordsAggregate vra;
    private int popindex;
    private int row;
    private int rowlimit;
    private int col;
    CellValueRecordInterface current = null;
    CellValueRecordInterface next    = null;

    public VRAIterator(ValueRecordsAggregate vra) {
      this(vra, 0, -1);
    }

    public VRAIterator(ValueRecordsAggregate vra, int row) {
        this(vra, row, row);
    }

    public VRAIterator(ValueRecordsAggregate vra, int startRow, int endRow) {
        this.vra = vra;
        this.row = startRow;
        this.rowlimit = endRow;
        this.popindex = vra.populatedRows.indexOf(row);
        if (vra.getPhysicalNumberOfCells() > 0) {
            next = findNextCell(null);
            hasNext = (next != null);
        }
    }


    public boolean hasNext() {
        return hasNext;
    }

    public Object next() {
        current = next;
        next = findNextCell(current);
        if (next == null) {
            hasNext = false;
        }
        return current;
    }

    public void remove() {
       vra.removeCell(current);
    }

    private CellValueRecordInterface findNextCell(CellValueRecordInterface current) {
        IntList ctRow = null;
        int rowNum = -1;
        int colNum = -1;
        int newCol = -1;
        boolean wasntFirst = false;

        if (current != null) {
            wasntFirst = true;
            rowNum = current.getRow();
            colNum = current.getColumn();
            ctRow = ((IntList)vra.celltype.get(rowNum));
        }

        //if popindex = row iwth no cells, fast forward till we get to one with size > 0
        while ((ctRow == null || ctRow.size() == 0) && vra.populatedRows.size() > popindex) {
            if (wasntFirst == true) {
                throw new RuntimeException("CANT HAPPEN WASNTFIRST BUT WE'RE FASTFORWARDING!");
            }
            rowNum = vra.populatedRows.get(popindex);
            ctRow = (IntList)vra.celltype.get(rowNum);
            if (ctRow.size() == 0) {
                if ((rowlimit == -1)||(rowNum<=rowlimit)) {
                    popindex++;
                } else {
                    this.hasNext = false;
                }
            }
        }

        if (rowNum == -1) {
            return null;
        }

        while (newCol == -1) {
            newCol = findNextPopulatedCell(rowNum,colNum);
            colNum = newCol;
            if (colNum == -1) {                          //end of row, forward one row
                popindex++;
                if (popindex < vra.populatedRows.size() && ((rowlimit == -1)||(rowNum<=rowlimit))) {
                    rowNum = vra.populatedRows.get(popindex);
                    //Return null if the row is out of range
                    if ((rowlimit != -1) &&( rowNum > rowlimit))
                      return null;
                } else {
                    return null;
                }
            }
        }

        return vra.constructRecord(rowNum,colNum);
    }

    private int findNextPopulatedCell(int row, int col) {

        /*IntList ctRow = (IntList) vra.celltype.get(row);
        int retval = -1;
        if (ctRow.size() > col+1) {
            for (int k = col+1; k < ctRow.size() +1; k++) {

                if (k != ctRow.size()) {
                   int val = ctRow.get(k);

                   if (val != -1) {
                       retval = k;
                       break;
                   }  // end if (val !=...

                } //end if (k !=..

            }   //end for

        }  //end if (ctRow.size()...
        return retval;*/
        return vra.findNextPopulatedCell(row, col);
    }

}