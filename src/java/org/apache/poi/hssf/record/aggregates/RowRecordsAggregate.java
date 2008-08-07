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
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.RowRecord;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class RowRecordsAggregate extends Record {
    private int _firstrow = -1;
    private int _lastrow  = -1;
    private final Map _rowRecords;
    private final ValueRecordsAggregate _valuesAgg;

    /** Creates a new instance of ValueRecordsAggregate */

    public RowRecordsAggregate() {
        this(new TreeMap(), new ValueRecordsAggregate());
    }
    private RowRecordsAggregate(TreeMap rowRecords, ValueRecordsAggregate valuesAgg) {
        _rowRecords = rowRecords;
        _valuesAgg = valuesAgg;
    }

    public void insertRow(RowRecord row) {
        // Integer integer = new Integer(row.getRowNumber());
        _rowRecords.put(new Integer(row.getRowNumber()), row);
        if ((row.getRowNumber() < _firstrow) || (_firstrow == -1))
        {
            _firstrow = row.getRowNumber();
        }
        if ((row.getRowNumber() > _lastrow) || (_lastrow == -1))
        {
            _lastrow = row.getRowNumber();
        }
    }

    public void removeRow(RowRecord row) {
        int rowIndex = row.getRowNumber();
        _valuesAgg.removeAllCellsValuesForRow(rowIndex);
        Integer key = new Integer(rowIndex);
        RowRecord rr = (RowRecord) _rowRecords.remove(key);
        if (rr == null) {
            throw new RuntimeException("Invalid row index (" + key.intValue() + ")");
        }
        if (row != rr) {
            _rowRecords.put(key, rr);
            throw new RuntimeException("Attempt to remove row that does not belong to this sheet");
        }
    }

    public RowRecord getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex > 65535) {
            throw new IllegalArgumentException("The row number must be between 0 and 65535");
        }
        return (RowRecord) _rowRecords.get(new Integer(rowIndex));
    }

    public int getPhysicalNumberOfRows()
    {
        return _rowRecords.size();
    }

    public int getFirstRowNum()
    {
        return _firstrow;
    }

    public int getLastRowNum()
    {
        return _lastrow;
    }
    
    /** Returns the number of row blocks.
     * <p/>The row blocks are goupings of rows that contain the DBCell record
     * after them
     */
    public int getRowBlockCount() {
      int size = _rowRecords.size()/DBCellRecord.BLOCK_SIZE;
      if ((_rowRecords.size() % DBCellRecord.BLOCK_SIZE) != 0)
          size++;
      return size;
    }

    private int getRowBlockSize(int block) {
      return RowRecord.ENCODED_SIZE * getRowCountForBlock(block);
    }

    /** Returns the number of physical rows within a block*/
    public int getRowCountForBlock(int block) {
      int startIndex = block * DBCellRecord.BLOCK_SIZE;
      int endIndex = startIndex + DBCellRecord.BLOCK_SIZE - 1;
      if (endIndex >= _rowRecords.size())
        endIndex = _rowRecords.size()-1;

      return endIndex-startIndex+1;
    }

    /** Returns the physical row number of the first row in a block*/
    private int getStartRowNumberForBlock(int block) {
      //Given that we basically iterate through the rows in order,
      // TODO - For a performance improvement, it would be better to return an instance of
      //an iterator and use that instance throughout, rather than recreating one and
      //having to move it to the right position.
      int startIndex = block * DBCellRecord.BLOCK_SIZE;
      Iterator rowIter = _rowRecords.values().iterator();
      RowRecord row = null;
      //Position the iterator at the start of the block
      for (int i=0; i<=startIndex;i++) {
        row = (RowRecord)rowIter.next();
      }
      if (row == null) {
          throw new RuntimeException("Did not find start row for block " + block);
      }

      return row.getRowNumber();
    }

    /** Returns the physical row number of the end row in a block*/
    private int getEndRowNumberForBlock(int block) {
      int endIndex = ((block + 1)*DBCellRecord.BLOCK_SIZE)-1;
      if (endIndex >= _rowRecords.size())
        endIndex = _rowRecords.size()-1;

      Iterator rowIter = _rowRecords.values().iterator();
      RowRecord row = null;
      for (int i=0; i<=endIndex;i++) {
        row = (RowRecord)rowIter.next();
      }
      if (row == null) {
          throw new RuntimeException("Did not find start row for block " + block);
      }
      return row.getRowNumber();
    }


    /** Serializes a block of the rows */
    private int serializeRowBlock(final int block, final int offset, byte[] data) {
      final int startIndex = block*DBCellRecord.BLOCK_SIZE;
      final int endIndex = startIndex + DBCellRecord.BLOCK_SIZE;

      Iterator rowIterator = _rowRecords.values().iterator();
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
    

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset    offset to begin writing at
     * @param data      byte array containing instance data
     * @return number of bytes written
     */
    public int serialize(int offset, byte [] data) {
        ValueRecordsAggregate cells = _valuesAgg;
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
          int cellRefOffset = (rowBlockSize-RowRecord.ENCODED_SIZE);
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

    public int getRecordSize() {

        int retval = this._rowRecords.size() * RowRecord.ENCODED_SIZE;
        
        for (Iterator itr = _valuesAgg.getIterator(); itr.hasNext();) {
            RecordBase record = (RecordBase) itr.next();
            retval += record.getRecordSize();
        }

        // Add space for the IndexRecord and DBCell records
        final int nBlocks = getRowBlockCount();
        int nRows = 0;
        for (Iterator itr = getIterator(); itr.hasNext();) {
            RowRecord row = (RowRecord)itr.next();
            if (_valuesAgg.rowHasCells(row.getRowNumber())) {
                nRows++;
            }
        }
        retval += IndexRecord.getRecordSizeForBlockCount(nBlocks);
        retval += DBCellRecord.calculateSizeOfRecords(nBlocks, nRows);
        return retval;
    }

    public Iterator getIterator()
    {
        return _rowRecords.values().iterator();
    }
    
    
    public Iterator getAllRecordsIterator() {
        List result = new ArrayList(_rowRecords.size() * 2);
        result.addAll(_rowRecords.values());
        Iterator vi = _valuesAgg.getIterator();
        while (vi.hasNext()) {
            result.add(vi.next());
        }
        return result.iterator();
    }
    /**
     * Performs a deep clone of the record
     */
    public Object clone()
    {
        TreeMap rows = new TreeMap();

        for ( Iterator rowIter = getIterator(); rowIter.hasNext(); )
        {
            //return the cloned Row Record & insert
            RowRecord row = (RowRecord) ( (RowRecord) rowIter.next() ).clone();
            rows.put(row, row);
        }
        ValueRecordsAggregate valuesAgg = (ValueRecordsAggregate) _valuesAgg.clone();
        return new RowRecordsAggregate(rows, valuesAgg);
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

    public CellValueRecordInterface[] getValueRecords() {
        return _valuesAgg.getValueRecords();
    }

    public IndexRecord createIndexRecord(int indexRecordOffset, int sizeOfInitialSheetRecords) {
        IndexRecord result = new IndexRecord();
        result.setFirstRow(_firstrow);
        result.setLastRowAdd1(_lastrow + 1);
        // Calculate the size of the records from the end of the BOF
        // and up to the RowRecordsAggregate...

        // Add the references to the DBCells in the IndexRecord (one for each block)
        // Note: The offsets are relative to the Workbook BOF. Assume that this is
        // 0 for now.....

        int blockCount = getRowBlockCount();
        // Calculate the size of this IndexRecord
        int indexRecSize = IndexRecord.getRecordSizeForBlockCount(blockCount);

        int currentOffset = indexRecordOffset + indexRecSize + sizeOfInitialSheetRecords;

        for (int block = 0; block < blockCount; block++) {
            // each row-block has a DBCELL record.
            // The offset of each DBCELL record needs to be updated in the INDEX record

            // account for row records in this row-block
            currentOffset += getRowBlockSize(block);
            // account for cell value records after those
            currentOffset += _valuesAgg.getRowCellBlockSize(
                    getStartRowNumberForBlock(block), getEndRowNumberForBlock(block));

            // currentOffset is now the location of the DBCELL record for this row-block
            result.addDbcell(currentOffset);
            // Add space required to write the DBCELL record (whose reference was just added).
            currentOffset += (8 + (getRowCountForBlock(block) * 2));
        }
        return result;
    }
    public void constructCellValues(int offset, List records) {
        _valuesAgg.construct(offset, records);
    }
    public void insertCell(CellValueRecordInterface cvRec) {
        _valuesAgg.insertCell(cvRec);
    }
    public void removeCell(CellValueRecordInterface cvRec) {
        _valuesAgg.removeCell(cvRec);
    }
}

