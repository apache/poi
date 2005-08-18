
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

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 *
 * Aggregate value records together.  Things are easier to handle that way.
 *
 * @author  andy
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class ValueRecordsAggregate
    extends Record
{
    public final static short sid       = -1000;
    int                       firstcell = -1;
    int                       lastcell  = -1;
    TreeMap                   records   = null;

   /** This class is used to find a row in the TreeMap.
    *  
    * This instance of which is used by the rowHasCells method as the key.
    */
   private class RowComparator implements CellValueRecordInterface, Comparable {
     private int row;
     
     public void setRow(int row) {
       this.row = row;
     }
     
     public int compareTo(Object obj) {
         CellValueRecordInterface cell = (CellValueRecordInterface) obj;
 
         if (row == cell.getRow()) {
           return 0;
         }
         else if (row < cell.getRow()) {
           return -1;
         }
         else if (row > cell.getRow()){
           return 1;
         }
         return -1;         
     }
     public int getRow() { return row;}     
     public short getColumn() { return 0;}
     public void setColumn(short col){}
     public void setXFIndex(short xf){}
     public short getXFIndex(){return 0;}
     public boolean isBefore(CellValueRecordInterface i){ return false; }
     public boolean isAfter(CellValueRecordInterface i){ return false; }
     public boolean isEqual(CellValueRecordInterface i){ return false; }
     public Object clone(){ return null;}     
   }
   
   /**
    * Iterates the cell records that exist between the startRow and endRow (inclusive).
    * 
    * User must ensure that hasNext & next are called insequence for correct
    * operation. Could fix, but since this is only used internally to the
    * ValueRecordsAggregate class there doesnt seem much point.
    */   
   private class RowCellIterator implements Iterator {
     private int startRow;
     private int endRow;
     private Iterator internalIterator;
     private CellValueRecordInterface atCell;
     
     public class RowCellComparator extends RowComparator {
       public int compareTo(Object obj) {
           CellValueRecordInterface cell = (CellValueRecordInterface) obj;
  
           if (getRow() == cell.getRow() && cell.getColumn() == 0) {
             return 0;
           }
           else if (getRow() < cell.getRow()) {
             return -1;
           }
           else if (getRow() > cell.getRow()){
             return 1;
           }
           if (cell.getColumn() > 0)
           {
               return -1;
           }
           if (cell.getColumn() < 0)
           {
               return 1;
           }
           return -1;         
       }
     }
     
     private RowCellComparator rowCellCompare;
     
     
     public RowCellIterator(int startRow, int endRow) {
       this.startRow = startRow;
       this.endRow = endRow;
       rowCellCompare = new RowCellComparator();
       rowCellCompare.setRow(startRow);
     }
     
     public boolean hasNext() {
       if (internalIterator == null) {
         internalIterator = records.tailMap(rowCellCompare).values().iterator();
       }
       if (internalIterator.hasNext()) {
         atCell = (CellValueRecordInterface) internalIterator.next();
         return (atCell.getRow() <= endRow);
       } else return false;
     }
     
     public Object next() {
       return atCell;
     }
     
     public void remove() {
       //Do Nothing (Not called)
     }
   }
   
   //Only need a single instance of this class, but the row fields
   //will probably change each use. Instance is only used in the rowHasCells method.
   public final RowComparator compareRow = new RowComparator();
   
    /** Creates a new instance of ValueRecordsAggregate */

    public ValueRecordsAggregate()
    {
        records = new TreeMap();
    }

    public void insertCell(CellValueRecordInterface cell)
    {
        Object o = records.put(cell, cell);

        if ((cell.getColumn() < firstcell) || (firstcell == -1))
        {
            firstcell = cell.getColumn();
        }
        if ((cell.getColumn() > lastcell) || (lastcell == -1))
        {
            lastcell = cell.getColumn();
        }
    }

    public void removeCell(CellValueRecordInterface cell)
    {
        records.remove(cell);
    }

    public int getPhysicalNumberOfCells()
    {
        return records.size();
    }

    public int getFirstCellNum()
    {
        return firstcell;
    }

    public int getLastCellNum()
    {
        return lastcell;
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
            else if (rec instanceof SharedFormulaRecord)
            {
            	//these follow the first formula in a group
            	lastFormulaAggregate.setSharedFormulaRecord((SharedFormulaRecord)rec);
            }
            else if (rec.isValue())
            {
                insertCell(( CellValueRecordInterface ) rec);
            }
        }
        return k;
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset to begin writing at
     * @param data byte array containing instance data
     * @return number of bytes written
     */

    public int serialize(int offset, byte [] data)
    {
      throw new RuntimeException("This method shouldnt be called. ValueRecordsAggregate.serializeCellRow() should be called from RowRecordsAggregate.");
    }
    
    /** Tallies a count of the size of the cell records
     *  that are attached to the rows in the range specified.
     */
    public int getRowCellBlockSize(int startRow, int endRow) {
      RowCellIterator itr = new RowCellIterator(startRow, endRow);
      int size = 0;
      while (itr.hasNext()) {
        CellValueRecordInterface cell = (CellValueRecordInterface)itr.next();
        int row = cell.getRow();
        if (row > endRow)
          break;
        if ((row >=startRow) && (row <= endRow))
          size += ((Record)cell).getRecordSize();
      }
      return size;
    }

    /** Returns true if the row has cells attached to it */
    public boolean rowHasCells(int row) {
      compareRow.setRow(row);
      return records.containsKey(compareRow);
    }

    /** Serializes the cells that are allocated to a certain row range*/
    public int serializeCellRow(final int row, int offset, byte [] data)
    {
        RowCellIterator itr = new RowCellIterator(row, row);      
        int      pos = offset;

        while (itr.hasNext())
        {
            CellValueRecordInterface cell = (CellValueRecordInterface)itr.next();
            if (cell.getRow() != row)
              break;
            pos += (( Record ) cell).serialize(pos, data);
        }
        return pos - offset;
    }

    
    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */

    protected void fillFields(RecordInputStream in)
    {
    }

    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */

    protected void validateSid(short id)
    {
    }

    /**
     * return the non static version of the id for this record.
     */

    public short getSid()
    {
        return sid;
    }

    public int getRecordSize() {
    
        int size = 0;
        Iterator irecs = records.values().iterator();
        
        while (irecs.hasNext()) {
                size += (( Record ) irecs.next()).getRecordSize();
        }

        return size;
    }

    public Iterator getIterator()
    {
        return records.values().iterator();
    }

    /** Performs a deep clone of the record*/
    public Object clone() {
      ValueRecordsAggregate rec = new ValueRecordsAggregate();
      for (Iterator valIter = getIterator(); valIter.hasNext();) {
        CellValueRecordInterface val = (CellValueRecordInterface)((CellValueRecordInterface)valIter.next()).clone();
        rec.insertCell(val);
      }
      return rec;
    }
}