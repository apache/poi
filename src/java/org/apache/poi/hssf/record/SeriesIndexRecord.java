
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
 * links a series to its position in the series list.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public class SeriesIndexRecord
    extends Record
{
    public final static short      sid                             = 0x1065;
    private  short      field_1_index;


    public SeriesIndexRecord()
    {

    }

    /**
     * Constructs a SeriesIndex record and sets its fields appropriately.
     *
     * @param id    id must be 0x1065 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SeriesIndexRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a SeriesIndex record and sets its fields appropriately.
     *
     * @param id    id must be 0x1065 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SeriesIndexRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a SeriesIndex record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_index                  = LittleEndian.getShort(data, pos + 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SINDEX]\n");
        buffer.append("    .index                = ")
            .append("0x").append(HexDump.toHex(  getIndex ()))
            .append(" (").append( getIndex() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/SINDEX]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_index);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        SeriesIndexRecord rec = new SeriesIndexRecord();
    
        rec.field_1_index = field_1_index;
        return rec;
    }




    /**
     * Get the index field for the SeriesIndex record.
     */
    public short getIndex()
    {
        return field_1_index;
    }

    /**
     * Set the index field for the SeriesIndex record.
     */
    public void setIndex(short field_1_index)
    {
        this.field_1_index = field_1_index;
    }


}  // END OF CLASS




