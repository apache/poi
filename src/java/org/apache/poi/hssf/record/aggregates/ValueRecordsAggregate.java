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

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;

/**
 *
 * Aggregate value records together.  Things are easier to handle that way.
 *
 * @author  andy
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ValueRecordsAggregate {
    private int                       firstcell = -1;
    private int                       lastcell  = -1;
    private CellValueRecordInterface[][] records;

    /** Creates a new instance of ValueRecordsAggregate */

    public ValueRecordsAggregate()
    {
        records = new CellValueRecordInterface[30][]; // We start with 30 Rows.
    }

  public void insertCell(CellValueRecordInterface cell) {
    short column = cell.getColumn();
    int row = cell.getRow();
    if (row >= records.length) {
      CellValueRecordInterface[][] oldRecords = records;
      int newSize = oldRecords.length * 2;
      if(newSize<row+1) newSize=row+1;
      records = new CellValueRecordInterface[newSize][];
      System.arraycopy(oldRecords, 0, records, 0, oldRecords.length);
    }
    CellValueRecordInterface[] rowCells = records[row];
    if (rowCells == null) {
      int newSize = column + 1;
      if(newSize<10) newSize=10;
      rowCells = new CellValueRecordInterface[newSize];
      records[row] = rowCells;
    }
    if (column >= rowCells.length) {
      CellValueRecordInterface[] oldRowCells = rowCells;
      int newSize = oldRowCells.length * 2;
      if(newSize<column+1) newSize=column+1;
      // if(newSize>257) newSize=257; // activate?
      rowCells = new CellValueRecordInterface[newSize];
      System.arraycopy(oldRowCells, 0, rowCells, 0, oldRowCells.length);
      records[row] = rowCells;
    }
    rowCells[column] = cell;

    if ((column < firstcell) || (firstcell == -1)) {
      firstcell = column;
    }
    if ((column > lastcell) || (lastcell == -1)) {
      lastcell = column;
    }
  }

    public void removeCell(CellValueRecordInterface cell) {
        if (cell == null) {
            throw new IllegalArgumentException("cell must not be null");
        }
        int row = cell.getRow();
        if (row >= records.length) {
            throw new RuntimeException("cell row is out of range");
        }
        CellValueRecordInterface[] rowCells = records[row];
        if (rowCells == null) {
            throw new RuntimeException("cell row is already empty");
        }
        short column = cell.getColumn();
        if (column >= rowCells.length) {
            throw new RuntimeException("cell column is out of range");
        }
        rowCells[column] = null;
    }

    public void removeAllCellsValuesForRow(int rowIndex) {
        if (rowIndex >= records.length) {
            throw new IllegalArgumentException("Specified rowIndex " + rowIndex 
                    + " is outside the allowable range (0.." +records.length + ")");
        }
        records[rowIndex] = null;
    }
    

    public int getPhysicalNumberOfCells()
    {
    int count=0;
    for(int r=0;r<records.length;r++) {
      CellValueRecordInterface[] rowCells=records[r];
      if (rowCells != null)
        for(short c=0;c<rowCells.length;c++) {
          if(rowCells[c]!=null) count++;
        }
    }
    return count;
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
        
        // First up, locate all the shared formulas for this sheet
        List sharedFormulas = new java.util.ArrayList();
        for (k = offset; k < records.size(); k++)
        {
            Record rec = ( Record ) records.get(k);
            if (rec instanceof SharedFormulaRecord) {
                sharedFormulas.add(rec);
            }
            if(rec instanceof EOFRecord) {
                // End of current sheet. Ignore all subsequent shared formula records (Bugzilla 44449)
                break;
            }
        }

        // Now do the main processing sweep
        for (k = offset; k < records.size(); k++)
        {
            Record rec = ( Record ) records.get(k);

            if (rec instanceof StringRecord == false && !rec.isInValueSection() && !(rec instanceof UnknownRecord))
            {
                break;
            } else if (rec instanceof SharedFormulaRecord) {
                // Already handled, not to worry
            } else if (rec instanceof FormulaRecord)
            {
              FormulaRecord formula = (FormulaRecord)rec;
              if (formula.isSharedFormula()) {
                // Traverse the list of shared formulas in
                //  reverse order, and try to find the correct one
                //  for us
                boolean found = false;
                for (int i=sharedFormulas.size()-1;i>=0;i--) {
                    // TODO - there is no junit test case to justify this reversed loop
                    // perhaps it could just run in the normal direction?
                    SharedFormulaRecord shrd = (SharedFormulaRecord)sharedFormulas.get(i);
                    if (shrd.isFormulaInShared(formula)) {
                        shrd.convertSharedFormulaRecord(formula);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    handleMissingSharedFormulaRecord(formula);
                }
              }
                
              lastFormulaAggregate = new FormulaRecordAggregate((FormulaRecord)rec, null);
              insertCell( lastFormulaAggregate );
            }
            else if (rec instanceof StringRecord)
            {
                lastFormulaAggregate.setStringRecord((StringRecord)rec);
            }
            else if (rec.isValue())
            {
                insertCell(( CellValueRecordInterface ) rec);
            }
        }
        return k;
    }

    /**
     * Sometimes the shared formula flag "seems" to be erroneously set, in which case there is no 
     * call to <tt>SharedFormulaRecord.convertSharedFormulaRecord</tt> and hence the 
     * <tt>parsedExpression</tt> field of this <tt>FormulaRecord</tt> will not get updated.<br/>
     * As it turns out, this is not a problem, because in these circumstances, the existing value
     * for <tt>parsedExpression</tt> is perfectly OK.<p/>
     * 
     * This method may also be used for setting breakpoints to help diagnose issues regarding the
     * abnormally-set 'shared formula' flags. 
     * (see TestValueRecordsAggregate.testSpuriousSharedFormulaFlag()).<p/>
     * 
     * The method currently does nothing but do not delete it without finding a nice home for this 
     * comment.
     */
    private static void handleMissingSharedFormulaRecord(FormulaRecord formula) {
        // could log an info message here since this is a fairly unusual occurrence.
    }
    
    /** Tallies a count of the size of the cell records
     *  that are attached to the rows in the range specified.
     */
    public int getRowCellBlockSize(int startRow, int endRow) {
      MyIterator itr = new MyIterator(startRow, endRow);
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
        if (row > records.length-1) //previously this said row > records.length which means if 
            return false;  // if records.length == 60 and I pass "60" here I get array out of bounds
      CellValueRecordInterface[] rowCells=records[row]; //because a 60 length array has the last index = 59
      if(rowCells==null) return false;
      for(int col=0;col<rowCells.length;col++) {
        if(rowCells[col]!=null) return true;
      }
      return false;
    }

    /** Serializes the cells that are allocated to a certain row range*/
    public int serializeCellRow(final int row, int offset, byte [] data)
    {
      MyIterator itr = new MyIterator(row, row);
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
    
    public int visitCellsForRow(int rowIndex, RecordVisitor rv) {
        int result = 0;
        CellValueRecordInterface[] cellRecs = records[rowIndex];
        if (cellRecs != null) {
            for (int i = 0; i < cellRecs.length; i++) {
                CellValueRecordInterface cvr = cellRecs[i];
                if (cvr == null) {
                    continue;
                }
                if (cvr instanceof FormulaRecordAggregate) {
                    FormulaRecordAggregate fmAgg = (FormulaRecordAggregate) cvr;
                    Record fmAggRec = fmAgg.getFormulaRecord();
                    rv.visitRecord(fmAggRec);
                    result += fmAggRec.getRecordSize();
                    fmAggRec = fmAgg.getStringRecord();
                    if (fmAggRec != null) {
                        rv.visitRecord(fmAggRec);
                        result += fmAggRec.getRecordSize();
                    }
                } else {
                    Record rec = (Record) cvr;
                    rv.visitRecord(rec);
                    result += rec.getRecordSize();
                }
            }
        }
        return result;
    }

    public CellValueRecordInterface[] getValueRecords() {
        List temp = new ArrayList();
        
        for (int i = 0; i < records.length; i++) {
            CellValueRecordInterface[] rowCells = records[i];
            if (rowCells == null) {
                continue;
            }
            for (int j = 0; j < rowCells.length; j++) {
                CellValueRecordInterface cell = rowCells[j];
                if (cell != null) {
                    temp.add(cell);
                }
            }
        }
        
        CellValueRecordInterface[] result = new CellValueRecordInterface[temp.size()];
        temp.toArray(result);
        return result;
    }
    public Iterator getIterator()
    {
    return new MyIterator();
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
  
  private final class MyIterator implements Iterator {
    short nextColumn=-1;
    int nextRow,lastRow;

    public MyIterator()
    {
      this.nextRow=0;
      this.lastRow=records.length-1;
      findNext();
    }
    
    public MyIterator(int firstRow,int lastRow)
    {
      this.nextRow=firstRow;
      this.lastRow=lastRow;
      findNext();
    }

    public boolean hasNext() {
      return nextRow<=lastRow;
    }
    public Object next() {
      Object o=records[nextRow][nextColumn];
      findNext();
      return o;
    }
    public void remove() {
      throw new UnsupportedOperationException("gibt's noch nicht");
    }

    private void findNext() {
      nextColumn++;
      for(;nextRow<=lastRow;nextRow++) {
                                           //previously this threw array out of bounds...
        CellValueRecordInterface[] rowCells=(nextRow < records.length) ? records[nextRow] : null;
        if(rowCells==null) { // This row is empty
          nextColumn=0;
          continue;
        }
        for(;nextColumn<rowCells.length;nextColumn++) {
          if(rowCells[nextColumn]!=null) return;
        }
        nextColumn=0;
      }
    }

  }
}
