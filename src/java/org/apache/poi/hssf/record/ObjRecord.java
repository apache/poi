
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
        


package org.apache.poi.hssf.record;



import org.apache.poi.util.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * The obj record is used to hold various graphic objects and controls.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class ObjRecord
    extends Record
{
    public final static short      sid                             = 0x5D;
    private List subrecords;

    //00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
    //00000010 26 01 00 00 00 00 00 00 00 00                   &.........


    public ObjRecord()
    {
        subrecords = new ArrayList(2);
    }

    /**
     * Constructs a OBJ record and sets its fields appropriately.
     *
     * @param id    id must be 0x5D or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public ObjRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a obj record and sets its fields appropriately.
     *
     * @param id    id must be 0x5D or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public ObjRecord(short id, short size, byte[] data, int offset)
    {
        super(id, size, data, offset);
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

    protected void fillFields(byte [] data, short size, int offset)
    {
        subrecords = new ArrayList();
        int pos = offset;
        while (pos - offset < size)
        {
            short subRecordSid = LittleEndian.getShort(data, pos);
            short subRecordSize = LittleEndian.getShort(data, pos + 2);
            Record subRecord = SubRecord.createSubRecord(subRecordSid, subRecordSize, data, pos + 4);
            subrecords.add(subRecord);
            pos += 4 + subRecordSize;
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
        return 4  + size;
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
        rec.subrecords = new ArrayList();

        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
            subrecords.add(( (Record) iterator.next() ).clone());

        return rec;
    }

}  // END OF CLASS




