
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

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
    Map records  = null;
    int     size     = 0;

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
        return firstrow;
    }

    public int getLastRowNum()
    {
        return lastrow;
    }

	/*
	 * No need to go through all the records as we're just collecting RowRecords 

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
	*/

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset    offset to begin writing at
     * @param data      byte array containing instance data
     * @return number of bytes written
     */

    public int serialize(int offset, byte [] data)
    {
        Iterator itr = records.values().iterator();
        int      pos = offset;

        while (itr.hasNext())
        {
            pos += (( Record ) itr.next()).serialize(pos, data);
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
        RowRecord rowRecord = (RowRecord) getRow( startRow );

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
    public static RowRecord createRow(int row)
    {
        RowRecord rowrec = new RowRecord();

        //rowrec.setRowNumber(( short ) row);
        rowrec.setRowNumber(row);
        rowrec.setHeight(( short ) 0xff);
        rowrec.setOptimize(( short ) 0x0);
        rowrec.setOptionFlags(( short ) 0x100);  // seems necessary for outlining
        rowrec.setXFIndex(( short ) 0xf);
        return rowrec;
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
        // colapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can determine
        //   this by looking at the hidden bit of the enclosing group.  You will have
        //   to look at the start and the end of the current group to determine which
        //   is the enclosing group
        // hidden bit only is altered for this outline level.  ie.  don't uncollapse contained groups
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

