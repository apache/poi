
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
        

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.util.DoubleList2d;
import org.apache.poi.util.IntList;
import org.apache.poi.util.IntList2d;
import org.apache.poi.util.List2d;

import java.util.Iterator;
import java.util.List;

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

    private final static int DEFAULT_ROWS=10000;
    private final static int DEFAULT_COLS=256;

    IntList2d    celltype      = null;
    IntList2d    xfs           = null; // array of style types.  Index of XF record
    DoubleList2d numericcells  = null; // numeric and Shared string indicies.
    List2d       formulaptgs   = null; // array of arrays of FormulaRecordAggregate
    List2d       stringvals    = null; // array of actual string/formula string vals
    IntList      populatedRows = null; //indicies of populated rows
    int          physCells;            //physical number of cells

    public CellValueRecordInterface getCell(int row, short col) {
        return constructRecord(row, col);
    }

    public int getRecordSize() {
        int size = 0;
        Iterator irecs = getIterator();

        while (irecs.hasNext()) {
                size += (( Record ) irecs.next()).getRecordSize();
        }

        return size;
    }

    public int serialize(int offset, byte [] data)
    {
        throw new RuntimeException("This method shouldnt be called. ValueRecordsAggregate.serializeCellRow() should be called from RowRecordsAggregate.");
    }

    public ValueRecordsAggregate() {
        celltype      = new IntList2d();
        xfs           = new IntList2d();
        numericcells  = new DoubleList2d();
        formulaptgs   = new List2d();
        stringvals    = new List2d();
        populatedRows = new IntList();
        physCells     = 0;
    }

    public Iterator getIterator() {
      return new VRAIterator(this);
    }

    /** Tallies a count of the size of the cell records
     *  that are attached to the rows in the range specified.
     */
    public int getRowCellBlockSize(int startRow, int endRow) {
      //Make sure that the row has cells
      while (!rowHasCells(startRow) && (startRow <= endRow))
        startRow++;
      if (startRow > endRow) {
        //Couldnt find any cells between the row range provided.
        return 0;
      }

      Iterator cellRec = new VRAIterator(this, startRow, endRow);
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
    public boolean rowHasCells(int row)
    {
        if (row == -1)
            return false;

        int col = 0;
        while (celltype.isAllocated( col, row))
        {
            if (celltype.get( col, row ) != 0)
                return true;
            col++;
        }
        return false;
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
        int k;

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

        while (col > 0 || count == -1) {
            col = findNextPopulatedCell(row,col);
            count++;
        }
        return count;
    }

    public void setValue(int row, short cell, double val) {
        numericcells.set(cell, row, val);
    }

    public void setStyle(int row, short cell, short xf) {
        xfs.set(cell, row, xf);
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
        if (cell == null)
            return;

        int rownum = cell.getRow();
        int colnum = cell.getColumn();

        if (celltype.get( colnum, rownum ) != 0)
        {
            celltype.set( colnum, rownum, 0 );
            if (rowHasCells( rownum ))
                populatedRows.removeValue( populatedRows.indexOf( rownum ) );
            physCells--;
        }
        else
        {
            //this cell doesn't exist...  the old code falls through so lets make this fall through too.
        }
    }

    public void insertCell( CellValueRecordInterface cell )
    {
        int rownum = cell.getRow();
        int colnum = cell.getColumn();
        int xf     = cell.getXFIndex();
        int type   = determineType(cell);

        if (!populatedRows.contains( rownum ))
        {
            populatedRows.add(rownum); //this means we must never have had this row inserted
        }

//        ensureRows(rownum);

//        IntList ctRow = (IntList)celltype.get(rownum);
//        IntList xfRow = (IntList)xfs.get(rownum);

//        adjustIntList(ctRow, colnum+1);
//        adjustIntList(xfRow, colnum+1);

        celltype.set(colnum, rownum, type);
        xfs.set( colnum, rownum, xf);

        insertCell(cell, type);
    }

    CellValueRecordInterface constructRecord(int row, int col) {

        if (celltype.get( col, row) == 0)
            throw new ArrayIndexOutOfBoundsException("No cell at position col" + col + ", row " + row + ".");
//        if (celltype.size() < row || ((IntList)celltype.get(row)).size() < col) {
//            throw new ArrayIndexOutOfBoundsException("constructRecord called with row = "+row+
//                      "and col ="+col+" but there are only "+celltype.size()+" rows and "+
//                      ((IntList)celltype.get(row)).size()+" cols!!");
//        }

        CellValueRecordInterface retval;
        int type = celltype.get( col, row );

        switch (type) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                NumberRecord nrecord = new NumberRecord();
                nrecord.setColumn((short)col);
                nrecord.setRow(row);
                nrecord.setValue( numericcells.get( col, row));
                nrecord.setXFIndex((short)xfs.get( col, row ));
//                nrecord.setXFIndex((short)((IntList)xfs.get(row)).get(col));
                retval = nrecord;
                break;
            case HSSFCell.CELL_TYPE_STRING:
                LabelSSTRecord srecord = new LabelSSTRecord();
                srecord.setColumn((short)col);
                srecord.setRow(row);
                srecord.setSSTIndex((int) numericcells.get( col, row));
                srecord.setXFIndex((short)xfs.get( col, row ));
                retval=srecord;
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                BlankRecord brecord = new BlankRecord();
                brecord.setColumn((short)col);
                brecord.setRow(row);
                brecord.setXFIndex((short)xfs.get( col, row ));
                retval=brecord;
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                /*
                FormulaRecord fr = new FormulaRecord();
                fr.setColumn((short)col);
                fr.setOptions((short)2);

                fr.setRow(row);
                fr.setXFIndex((short)xfs.get( col, row ));
                StringRecord        st = null;
                String strval = (String)stringvals.get( col, row );
                List expressionlist =  (List) formulaptgs.get( col, row);
                fr.setParsedExpression(expressionlist);
                fr.setExpressionLength(calculatePtgSize(expressionlist));
                if (strval != null) {
                  st = new StringRecord();
                   st.setString(strval);
                }
                FormulaRecordAggregate frarecord = new FormulaRecordAggregate(fr,st);

                retval= frarecord;
                break;
                */
                retval = (CellValueRecordInterface) formulaptgs.get( col, row );
                break;
            default:
                throw new RuntimeException("UnImplemented Celltype "+type);
        }

        return retval;
    }

    private short calculatePtgSize(List expressionlist)
    {
        short retval = 0;
        Iterator iter = expressionlist.iterator();
        while (iter.hasNext()) {
            retval += (short)((Ptg)iter.next()).getSize();
        }
        return retval;
    }

    private void insertCell(CellValueRecordInterface cell, int type)
    {
        int rownum = cell.getRow();
        int colnum = cell.getColumn();

//        DoubleList  nmRow = (DoubleList)numericcells.get(rownum);

        switch (type) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                NumberRecord nrecord = (NumberRecord)cell;
//                adjustDoubleList(nmRow, colnum+1);
                numericcells.set(colnum, rownum, nrecord.getValue());
                physCells++;
                break;
            case HSSFCell.CELL_TYPE_STRING:
                LabelSSTRecord srecord = (LabelSSTRecord)cell;
//                adjustDoubleList(nmRow, colnum+1);
                numericcells.set(colnum, rownum, srecord.getSSTIndex());
//                nmRow.set(colnum,srecord.getSSTIndex());
                physCells++;
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                FormulaRecordAggregate frarecord = (FormulaRecordAggregate)cell;
                formulaptgs.set( colnum, rownum, frarecord);
                physCells++;
                break;
                /*
//                List ptRow = (List)formulaptgs.get(rownum);
//                List stRow = (List)stringvals.get(rownum);
                FormulaRecordAggregate frarecord = (FormulaRecordAggregate)cell;
//                adjustDoubleList(nmRow, colnum+1);
//                adjustObjectList(ptRow, colnum+1);
//                adjustStringList(stRow, colnum+1);
                numericcells.set(colnum, rownum, frarecord.getFormulaRecord().getValue());
                formulaptgs.set( colnum, rownum, frarecord.getFormulaRecord().getParsedExpression() );
                StringRecord str = frarecord.getStringRecord();
                if ( str != null )
                    stringvals.set( colnum, rownum, str.getString() );
                else
                    stringvals.set( colnum, rownum, null );
                physCells++;
                break;
                */
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

//    private void ensureRows(int rownum) {
        //adjustRows(celltype, rownum+1, IntList.class);
//        adjustRows(xfs, rownum+1, IntList.class);
//        adjustRows(numericcells, rownum+1, DoubleList.class);
//        adjustRows(formulaptgs, rownum+1, ArrayList.class);
//        adjustRows(stringvals, rownum+1, ArrayList.class);

//    }

//    private void adjustRows(List list, int size, Class theclass) {
//        while (list.size() < size) {
//            try {
//                list.add(theclass.newInstance());
//            } catch (Exception e) {
//                throw new RuntimeException("Could Not Instantiate Row in adjustRows");
//            }
//        }
//    }

//    private void adjustIntList(IntList list, int size) {
//        while (list.size() < size) {
//            list.add(-1);
//        }
//    }
//
//    private void adjustDoubleList(DoubleList list, int size) {
//        while (list.size() < size) {
//            list.add(-1);
//        }
//    }
//
//    private void adjustObjectList(List list, int size) {
//        while (list.size() < size) {
//            list.add(new ArrayList());
//        }
//    }

//    private void adjustStringList(List list, int size) {
//        while (list.size() < size) {
//            list.add(new String());
//        }
//    }

    /**
     * Find the next populated cell in the row starting from but not
     * including the current column
     *
     * @return the next valid column number
     */
    protected int findNextPopulatedCell(int row, int col) {

        int currentCol = col + 1;
        while (celltype.isAllocated( currentCol, row ))
        {
            if (celltype.get( currentCol, row) > 0)
                return currentCol;
            currentCol++;
        }
        return -1;

        /*
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
        */
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
        if (this.popindex == -1) {
            if (vra.populatedRows.size() == 0)
                hasNext = false;
            else
            {
                int lastRow = vra.populatedRows.get(vra.populatedRows.size()-1);
                if (lastRow == -1)
                {
                    hasNext = false;
                }
                else
                {
                    for (int i = row; i <= lastRow; i++)
                    {
                        this.popindex = vra.populatedRows.indexOf(i);
                        if (popindex != -1)
                            break;
                    }
                }
                if (popindex == -1)
                    hasNext = false;
                else
                {
                    next = findNextCell(null);
                    hasNext = (next != null);
                }
            }
        } else if (vra.getPhysicalNumberOfCells() > 0) {
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
//        IntList ctRow = null;
        int rowNum = -1;
        int colNum = -1;
        int newCol = -1;
        boolean wasntFirst = false;

        if (current != null) {
            wasntFirst = true;
            rowNum = current.getRow();
            colNum = current.getColumn();
//            ctRow = ((IntList)vra.celltype.get(rowNum));
        }

        //if popindex = row iwth no cells, fast forward till we get to one with size > 0
        while (!vra.rowHasCells( rowNum ) && vra.populatedRows.size() > popindex) {
            if (wasntFirst == true) {
                throw new RuntimeException("CANT HAPPEN WASNTFIRST BUT WE'RE FASTFORWARDING!");
            }
            rowNum = vra.populatedRows.get(popindex);
            if (!vra.rowHasCells( rowNum )) {
                if ((rowlimit == -1)||(rowNum<=rowlimit)) {
                    popindex++;
                } else {
                    this.hasNext = false;
                }
            }
        }
        /*while ((ctRow == null || ctRow.size() == 0) && vra.populatedRows.size() > popindex) {
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
        } */

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
