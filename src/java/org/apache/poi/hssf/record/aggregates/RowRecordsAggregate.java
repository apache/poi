
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

