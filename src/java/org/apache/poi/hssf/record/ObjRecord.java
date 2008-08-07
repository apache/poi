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
        
package org.apache.poi.hssf.record;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * The obj record is used to hold various graphic objects and controls.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class ObjRecord extends Record {
    public final static short      sid                             = 0x005D;
    private List subrecords;

    //00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
    //00000010 26 01 00 00 00 00 00 00 00 00                   &.........


    public ObjRecord()
    {
        subrecords = new ArrayList(2);
        // TODO - ensure 2 sub-records (ftCmo  15h, and ftEnd  00h) are always created
    }

    /**
     * Constructs a OBJ record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public ObjRecord(RecordInputStream in)
    {
        super(in);
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not an OBJ record");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
    	// TODO - problems with OBJ sub-records stream
    	// MS spec says first sub-records is always CommonObjectDataSubRecord, and last is 
    	// always EndSubRecord.  OOO spec does not mention ObjRecord(0x005D).
    	// Existing POI test data seems to violate that rule.  Some test data seems to contain
    	// garbage, and a crash is only averted by stopping at what looks like the 'EndSubRecord'
    	
        subrecords = new ArrayList();
        //Check if this can be continued, if so then the
        //following wont work properly
        int subSize = 0;
        byte[] subRecordData = in.readRemainder();
        
        RecordInputStream subRecStream = new RecordInputStream(new ByteArrayInputStream(subRecordData));
        while(subRecStream.hasNextRecord()) {
          subRecStream.nextRecord();
          Record subRecord = SubRecord.createSubRecord(subRecStream);
          subSize += subRecord.getRecordSize();
          subrecords.add(subRecord);
          if (subRecord instanceof EndSubRecord) {
        	  break;
          }
        }

        /**
         * Add the EndSubRecord explicitly.
         * 
         * TODO - the reason the EndSubRecord is always skipped is because its 'sid' is zero and
         * that causes subRecStream.hasNextRecord() to return false.
         * There may be more than the size of EndSubRecord left-over, if there is any padding 
         * after that record.  The content of the EndSubRecord and the padding is all zeros.
         * So there's not much to look at past the last substantial record.
         * 
         * See Bugs 41242/45133 for details.
         */
        if (subRecordData.length - subSize >= 4) {
            subrecords.add(new EndSubRecord());
        }
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[OBJ]\n");
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            buffer.append("SUBRECORD: " + record.toString());
        }
        buffer.append("[/OBJ]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        pos = offset + 4;
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            pos += record.serialize(pos, data);
        }
        // assume padding (if present) does not need to be written.
        // it is probably zero already, and it probably doesn't matter anyway

        return getRecordSize();
    }

    /**
     * Size of record (excluding 4 byte header)
     */
    public int getRecordSize()
    {
        int size = 0;
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            size += record.getRecordSize();
        }
        int oddBytes = size & 0x03;
        int padding = oddBytes == 0 ? 0 : 4 - oddBytes;
        return 4  + size + padding;
    }

    public short getSid()
    {
        return sid;
    }

    public List getSubRecords()
    {
        return subrecords;
    }

    public void clearSubRecords()
    {
        subrecords.clear();
    }

    public void addSubRecord(int index, Object element)
    {
        subrecords.add( index, element );
    }

    public boolean addSubRecord(Object o)
    {
        return subrecords.add( o );
    }

    public Object clone()
    {
        ObjRecord rec = new ObjRecord();

        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
            rec.addSubRecord(( (Record) iterator.next() ).clone());

        return rec;
    }
}
