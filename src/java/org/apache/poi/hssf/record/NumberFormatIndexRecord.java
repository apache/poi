
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
 * The number format index record indexes format table.  This applies to an axis.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class NumberFormatIndexRecord
    extends Record
{
    public final static short      sid                             = 0x104e;
    private  short      field_1_formatIndex;


    public NumberFormatIndexRecord()
    {

    }

    /**
     * Constructs a NumberFormatIndex record and sets its fields appropriately.
     *
     * @param id    id must be 0x104e or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public NumberFormatIndexRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a NumberFormatIndex record and sets its fields appropriately.
     *
     * @param id    id must be 0x104e or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public NumberFormatIndexRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a NumberFormatIndex record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_formatIndex            = LittleEndian.getShort(data, pos + 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[IFMT]\n");
        buffer.append("    .formatIndex          = ")
            .append("0x").append(HexDump.toHex(  getFormatIndex ()))
            .append(" (").append( getFormatIndex() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/IFMT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_formatIndex);

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
        NumberFormatIndexRecord rec = new NumberFormatIndexRecord();
    
        rec.field_1_formatIndex = field_1_formatIndex;
        return rec;
    }




    /**
     * Get the format index field for the NumberFormatIndex record.
     */
    public short getFormatIndex()
    {
        return field_1_formatIndex;
    }

    /**
     * Set the format index field for the NumberFormatIndex record.
     */
    public void setFormatIndex(short field_1_formatIndex)
    {
        this.field_1_formatIndex = field_1_formatIndex;
    }


}  // END OF CLASS




