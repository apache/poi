
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

/**
 * The group marker record is used as a position holder for groups.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class GroupMarkerSubRecord
    extends SubRecord
{
    public final static short      sid                             = 0x06;

    private byte[] reserved = new byte[0];    // would really love to know what goes in here.

    public GroupMarkerSubRecord()
    {

    }

    /**
     * Constructs a group marker record and sets its fields appropriately.
     *
     * @param id    id must be 0x00 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public GroupMarkerSubRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a group marker record and sets its fields appropriately.
     *
     * @param id    id must be 0x00 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public GroupMarkerSubRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Group Marker record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
//        int pos = 0;
        reserved = new byte[size];
        System.arraycopy(data, offset, reserved, 0, size);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        String nl = System.getProperty("line.separator");
        buffer.append("[ftGmo]" + nl);
        buffer.append("  reserved = ").append(HexDump.toHex(reserved)).append(nl);
        buffer.append("[/ftGmo]" + nl);
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
        System.arraycopy(reserved, 0, data, offset + 4, getRecordSize() - 4);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + reserved.length;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        GroupMarkerSubRecord rec = new GroupMarkerSubRecord();
        rec.reserved = new byte[reserved.length];
        for ( int i = 0; i < reserved.length; i++ )
            rec.reserved[i] = reserved[i];
        return rec;
    }



}  // END OF CLASS


