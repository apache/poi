
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
 * The default data label text properties record identifies the text characteristics of the preceeding text record.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class DefaultDataLabelTextPropertiesRecord
    extends Record
{
    public final static short      sid                             = 0x1024;
    private  short      field_1_categoryDataType;
    public final static short       CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC = 0;
    public final static short       CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC = 1;
    public final static short       CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC = 2;


    public DefaultDataLabelTextPropertiesRecord()
    {

    }

    /**
     * Constructs a DefaultDataLabelTextProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1024 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DefaultDataLabelTextPropertiesRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a DefaultDataLabelTextProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1024 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DefaultDataLabelTextPropertiesRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a DefaultDataLabelTextProperties record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_categoryDataType       = LittleEndian.getShort(data, pos + 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DEFAULTTEXT]\n");
        buffer.append("    .categoryDataType     = ")
            .append("0x").append(HexDump.toHex(  getCategoryDataType ()))
            .append(" (").append( getCategoryDataType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/DEFAULTTEXT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_categoryDataType);

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
        DefaultDataLabelTextPropertiesRecord rec = new DefaultDataLabelTextPropertiesRecord();
    
        rec.field_1_categoryDataType = field_1_categoryDataType;
        return rec;
    }




    /**
     * Get the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @return  One of 
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public short getCategoryDataType()
    {
        return field_1_categoryDataType;
    }

    /**
     * Set the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @param field_1_categoryDataType
     *        One of 
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public void setCategoryDataType(short field_1_categoryDataType)
    {
        this.field_1_categoryDataType = field_1_categoryDataType;
    }


}  // END OF CLASS




