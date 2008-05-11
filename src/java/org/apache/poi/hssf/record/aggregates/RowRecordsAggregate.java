
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

import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.RowRecord;


import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public final class RowRecordsAggregate extends Record {
    private int     firstrow = -1;
    private int     lastrow  = -1;
    private Map records  = null; // TODO - use a proper key in this map
    private int     size     = 0;

    /** Creates a new instance of ValueRecordsAggregate */

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
        records.remove(row);
    }

    public RowRecord getRow(int rownum) {
        // Row must be between 0 and 65535
        if(rownum < 0 || rownum > 65535) {
            throw new IllegalArgumentException("The row number must be between 0 and 65535");
        }

        RowRecord row = new RowRecord(rownum);
        return ( RowRecord ) records.get(row);
    }

    public int getPhysicalNumberOfRows()
    {
        return records.size();
    }

    public int getFirstRowNum()
    {
        return firstrow;
    }

    public int getLastRowNum()
    {
        return lastrow;
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
      //Given that we basically iterate through the rows in order,
      //For a performance improvement, it would be better to return an instance of
      //an iterator and use that instance throughout, rather than recreating one and
      //having to move it to the right position.
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

      //Given that we basically iterate through the rows in order,
      //For a performance improvement, it would be better to return an instance of
      //an iterator and use that instance throughout, rather than recreating one and
      //having to move it to the right position.
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
        int pos = offset;

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
            if (null != cells && cells.rowHasCells(row)) {
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
     * You never fill an aggregate
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
     * Performs a deep clone of the record
     */
    public Object clone()
    {
        RowRecordsAggregate rec = new RowRecordsAggregate();
        for ( Iterator rowIter = getIterator(); rowIter.hasNext(); )
        {
            //return the cloned Row Record & insert
            RowRecord row = (RowRecord) ( (RowRecord) rowIter.next() ).clone();
            rec.insertRow( row );
        }
        return rec;
    }


    public int findStartOfRowOutlineGroup(int row)
    {
        // Find the start of the group.
        RowRecord rowRecord = this.getRow( row );
        int level = rowRecord.getOutlineLevel();
        int currentRow = row;
        while (this.getRow( currentRow ) != null)
        {
            rowRecord = this.getRow( currentRow );
            if (rowRecord.getOutlineLevel() < level)
                return currentRow + 1;
            currentRow--;
        }

        return currentRow + 1;
    }

    public int findEndOfRowOutlineGroup( int row )
    {
        int level = getRow( row ).getOutlineLevel();
        int currentRow;
        for (currentRow = row; currentRow < this.getLastRowNum(); currentRow++)
        {
            if (getRow(currentRow) == null || getRow(currentRow).getOutlineLevel() < level)
            {
                break;
            }
        }

        return currentRow-1;
    }

    public int writeHidden( RowRecord rowRecord, int row, boolean hidden )
    {
        int level = rowRecord.getOutlineLevel();
        while (rowRecord != null && this.getRow(row).getOutlineLevel() >= level)
        {
            rowRecord.setZeroHeight( hidden );
            row++;
            rowRecord = this.getRow( row );
        }
        return row - 1;
    }

    public void collapseRow( int rowNumber )
    {

        // Find the start of the group.
        int startRow = findStartOfRowOutlineGroup( rowNumber );
        RowRecord rowRecord = getRow( startRow );

        // Hide all the columns until the end of the group
        int lastRow = writeHidden( rowRecord, startRow, true );

        // Write collapse field
        if (getRow(lastRow + 1) != null)
        {
            getRow(lastRow + 1).setColapsed( true );
        }
        else
        {
            RowRecord row = createRow( lastRow + 1);
            row.setColapsed( true );
            insertRow( row );
        }
    }

    /**
     * Create a row record.
     *
     * @param row number
     * @return RowRecord created for the passed in row number
     * @see org.apache.poi.hssf.record.RowRecord
     */
    public static RowRecord createRow(int rowNumber) {
        return new RowRecord(rowNumber);
    }

    public boolean isRowGroupCollapsed( int row )
    {
        int collapseRow = findEndOfRowOutlineGroup( row ) + 1;

        if (getRow(collapseRow) == null)
            return false;
        else
            return getRow( collapseRow ).getColapsed();
    }

    public void expandRow( int rowNumber )
    {
        int idx = rowNumber;
        if (idx == -1)
            return;

        // If it is already expanded do nothing.
        if (!isRowGroupCollapsed(idx))
            return;

        // Find the start of the group.
        int startIdx = findStartOfRowOutlineGroup( idx );
        RowRecord row = getRow( startIdx );

        // Find the end of the group.
        int endIdx = findEndOfRowOutlineGroup( idx );

        // expand:
        // collapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can determine
        //   this by looking at the hidden bit of the enclosing group.  You will have
        //   to look at the start and the end of the current group to determine which
        //   is the enclosing group
        // hidden bit only is altered for this outline level.  ie.  don't un-collapse contained groups
        if ( !isRowGroupHiddenByParent( idx ) )
        {
            for ( int i = startIdx; i <= endIdx; i++ )
            {
                if ( row.getOutlineLevel() == getRow( i ).getOutlineLevel() )
                    getRow( i ).setZeroHeight( false );
                else if (!isRowGroupCollapsed(i))
                    getRow( i ).setZeroHeight( false );
            }
        }

        // Write collapse field
        getRow( endIdx + 1 ).setColapsed( false );
    }

    public boolean isRowGroupHiddenByParent( int row )
    {
        // Look out outline details of end
        int endLevel;
        boolean endHidden;
        int endOfOutlineGroupIdx = findEndOfRowOutlineGroup( row );
        if (getRow( endOfOutlineGroupIdx + 1 ) == null)
        {
            endLevel = 0;
            endHidden = false;
        }
        else
        {
            endLevel = getRow( endOfOutlineGroupIdx + 1).getOutlineLevel();
            endHidden = getRow( endOfOutlineGroupIdx + 1).getZeroHeight();
        }

        // Look out outline details of start
        int startLevel;
        boolean startHidden;
        int startOfOutlineGroupIdx = findStartOfRowOutlineGroup( row );
        if (startOfOutlineGroupIdx - 1 < 0 || getRow(startOfOutlineGroupIdx - 1) == null)
        {
            startLevel = 0;
            startHidden = false;
        }
        else
        {
            startLevel = getRow( startOfOutlineGroupIdx - 1).getOutlineLevel();
            startHidden = getRow( startOfOutlineGroupIdx - 1 ).getZeroHeight();
        }

        if (endLevel > startLevel)
        {
            return endHidden;
        }
        else
        {
            return startHidden;
        }
    }

}

