
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

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.UnknownRecord;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class RowRecordsAggregate
    extends Record
{
    int     firstrow = -1;
    int     lastrow  = -1;
    boolean firstdirty = false;
    boolean lastdirty  = false;
    Map records  = null;
    int     size     = 0;

    /** Creates a new instance of RowRecordsAggregate */

    public RowRecordsAggregate()
    {
        records = new TreeMap();

    }

    public void insertRow(RowRecord row)
    {
        size += row.getRecordSize();

        // Integer integer = new Integer(row.getRowNumber());
        records.put(row, row);
        if ((row.getRowNumber() < firstrow) || (firstrow == -1))
        {
            firstrow = row.getRowNumber();
        }
        if ((row.getRowNumber() > lastrow) || (lastrow == -1))
        {
            lastrow = row.getRowNumber();
        }
    }

    public void removeRow(RowRecord row)
    {
        size -= row.getRecordSize();

        // Integer integer = new Integer(row.getRowNumber());
        if (lastrow == row.getRowNumber()) {
            lastdirty = true;
        }
        if (firstrow == row.getRowNumber()) {
            firstdirty = true;
        }
        records.remove(row);
    }

    public RowRecord getRow(int rownum)
    {

        // Integer integer = new Integer(rownum);
        RowRecord row = new RowRecord();

        row.setRowNumber(( short ) rownum);
        return ( RowRecord ) records.get(row);
    }

    public int getPhysicalNumberOfRows()
    {
        return records.size();
    }

    public int getFirstRowNum()
    {
        if (firstdirty) {
            firstrow = findFirstRow();
        }
        return firstrow;
    }

    public int getLastRowNum()
    {
        if (lastdirty) {
            lastrow = findLastRow();
        }
        return lastrow;
    }

    public int construct(int offset, List records)
    {
        int k = 0;

        for (k = offset; k < records.size(); k++)
        {
            Record rec = ( Record ) records.get(k);

            if (!rec.isInValueSection() && !(rec instanceof UnknownRecord))
            {
                break;
            }
            if (rec.getSid() == RowRecord.sid)
            {
                insertRow(( RowRecord ) rec);
            }
        }
        return k;
    }

    /** Returns the number of row blocks.
     * <p/>The row blocks are goupings of rows that contain the DBCell record
     * after them
     */
    public int getRowBlockCount() {
      int size = records.size()/DBCellRecord.BLOCK_SIZE;
      if ((records.size() % DBCellRecord.BLOCK_SIZE) != 0)
          size++;
      return size;
    }

    public int getRowBlockSize(int block) {
      return 20 * getRowCountForBlock(block);
    }

    /** Returns the number of physical rows within a block*/
    public int getRowCountForBlock(int block) {
      int startIndex = block * DBCellRecord.BLOCK_SIZE;
      int endIndex = startIndex + DBCellRecord.BLOCK_SIZE - 1;
      if (endIndex >= records.size())
        endIndex = records.size()-1;

      return endIndex-startIndex+1;
    }

    /** Returns the physical row number of the first row in a block*/
    public int getStartRowNumberForBlock(int block) {
      //JMH Damn! I would like to directly index a record in the map rather than
      //iterating through it.
      int startIndex = block * DBCellRecord.BLOCK_SIZE;
      Iterator rowIter = records.values().iterator();
      RowRecord row = null;
      //Position the iterator at the start of the block
      for (int i=0; i<=startIndex;i++) {
        row = (RowRecord)rowIter.next();
      }

      return row.getRowNumber();
    }

    /** Returns the physical row number of the end row in a block*/
    public int getEndRowNumberForBlock(int block) {
      //JMH Damn! I would like to directly index a record in the map rather than
      //iterating through it.
      int endIndex = ((block + 1)*DBCellRecord.BLOCK_SIZE)-1;
      if (endIndex >= records.size())
        endIndex = records.size()-1;

      Iterator rowIter = records.values().iterator();
      RowRecord row = null;
      for (int i=0; i<=endIndex;i++) {
        row = (RowRecord)rowIter.next();
      }
      return row.getRowNumber();
    }


    /** Serializes a block of the rows */
    private int serializeRowBlock(final int block, final int offset, byte[] data) {
      final int startIndex = block*DBCellRecord.BLOCK_SIZE;
      final int endIndex = startIndex + DBCellRecord.BLOCK_SIZE;

      Iterator rowIterator = records.values().iterator();
      int pos = offset;

      //JMH TBD create an iterator that can start at a specific index.
      int i=0;
      for (;i<startIndex;i++)
        rowIterator.next();
      while(rowIterator.hasNext() && (i++ < endIndex)) {
        RowRecord row = (RowRecord)rowIterator.next();
        pos += row.serialize(pos, data);
      }
      return pos - offset;
    }

    public int serialize(int offset, byte [] data) {
      throw new RuntimeException("The serialize method that passes in cells should be used");
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset    offset to begin writing at
     * @param data      byte array containing instance data
     * @return number of bytes written
     */

    public int serialize(int offset, byte [] data, ValueRecordsAggregate cells)
    {
        Iterator itr = records.values().iterator();
        int      pos = offset;

        //DBCells are serialized before row records.
        final int blockCount = getRowBlockCount();
        for (int block=0;block<blockCount;block++) {
          //Serialize a block of rows.
          //Hold onto the position of the first row in the block
          final int rowStartPos = pos;
          //Hold onto the size of this block that was serialized
          final int rowBlockSize = serializeRowBlock(block, pos, data);
          pos += rowBlockSize;
          //Serialize a block of cells for those rows
          final int startRowNumber = getStartRowNumberForBlock(block);
          final int endRowNumber = getEndRowNumberForBlock(block);
          DBCellRecord cellRecord = new DBCellRecord();
          //Note: Cell references start from the second row...
          int cellRefOffset = (rowBlockSize-20);
          for (int row=startRowNumber;row<=endRowNumber;row++) {
            if (cells.rowHasCells(row)) {
              final int rowCellSize = cells.serializeCellRow(row, pos, data);
              pos += rowCellSize;
              //Add the offset to the first cell for the row into the DBCellRecord.
              cellRecord.addCellOffset((short)cellRefOffset);
              cellRefOffset = rowCellSize;
            }
          }
          //Calculate Offset from the start of a DBCellRecord to the first Row
          cellRecord.setRowOffset(pos - rowStartPos);
          pos += cellRecord.serialize(pos, data);
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

    protected void fillFields(byte [] data, short size, int offset)
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
        return -1000;
    }

    public int getRecordSize()
    {
        return size;
    }

    public Iterator getIterator()
    {
        return records.values().iterator();
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */
    private int findLastRow()
    {
        int rownum = lastrow-1;
        RowRecord r = getRow(rownum);

        while (r == null && rownum >= 0)
        {
            r = this.getRow(--rownum);
        }
        return rownum;
    }

    /**
     * used internally to refresh the "first row" when the first row is removed.
     */

    private int findFirstRow()
    {
        int rownum = firstrow+1;
        RowRecord r = getRow(rownum);

        while (r == null && rownum <= getLastRowNum())
        {
            r = getRow(++rownum);
        }

        if (rownum > getLastRowNum())
            return -1;

        return rownum;
    }


    /** Performs a deep clone of the record*/
    public Object clone() {
      RowRecordsAggregate rec = new RowRecordsAggregate();
      for (Iterator rowIter = getIterator(); rowIter.hasNext();) {
        //return the cloned Row Record & insert
        RowRecord row = (RowRecord)((RowRecord)rowIter.next()).clone();
        rec.insertRow(row);
      }
      return rec;
    }

}

