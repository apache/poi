
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
 * The dat record is used to store options for the chart.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class DatRecord
    extends Record
{
    public final static short      sid                             = 0x1063;
    private  short      field_1_options;
    private  BitField   horizontalBorder                            = new BitField(0x1);
    private  BitField   verticalBorder                              = new BitField(0x2);
    private  BitField   border                                      = new BitField(0x4);
    private  BitField   showSeriesKey                               = new BitField(0x8);


    public DatRecord()
    {

    }

    /**
     * Constructs a Dat record and sets its fields appropriately.
     *
     * @param id    id must be 0x1063 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DatRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a Dat record and sets its fields appropriately.
     *
     * @param id    id must be 0x1063 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DatRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Dat record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_options                = LittleEndian.getShort(data, pos + 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DAT]\n");
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .horizontalBorder         = ").append(isHorizontalBorder()).append('\n'); 
        buffer.append("         .verticalBorder           = ").append(isVerticalBorder()).append('\n'); 
        buffer.append("         .border                   = ").append(isBorder()).append('\n'); 
        buffer.append("         .showSeriesKey            = ").append(isShowSeriesKey()).append('\n'); 

        buffer.append("[/DAT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_options);

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
        DatRecord rec = new DatRecord();
    
        rec.field_1_options = field_1_options;
        return rec;
    }




    /**
     * Get the options field for the Dat record.
     */
    public short getOptions()
    {
        return field_1_options;
    }

    /**
     * Set the options field for the Dat record.
     */
    public void setOptions(short field_1_options)
    {
        this.field_1_options = field_1_options;
    }

    /**
     * Sets the horizontal border field value.
     * has a horizontal border
     */
    public void setHorizontalBorder(boolean value)
    {
        field_1_options = horizontalBorder.setShortBoolean(field_1_options, value);
    }

    /**
     * has a horizontal border
     * @return  the horizontal border field value.
     */
    public boolean isHorizontalBorder()
    {
        return horizontalBorder.isSet(field_1_options);
    }

    /**
     * Sets the vertical border field value.
     * has vertical border
     */
    public void setVerticalBorder(boolean value)
    {
        field_1_options = verticalBorder.setShortBoolean(field_1_options, value);
    }

    /**
     * has vertical border
     * @return  the vertical border field value.
     */
    public boolean isVerticalBorder()
    {
        return verticalBorder.isSet(field_1_options);
    }

    /**
     * Sets the border field value.
     * data table has a border
     */
    public void setBorder(boolean value)
    {
        field_1_options = border.setShortBoolean(field_1_options, value);
    }

    /**
     * data table has a border
     * @return  the border field value.
     */
    public boolean isBorder()
    {
        return border.isSet(field_1_options);
    }

    /**
     * Sets the show series key field value.
     * shows the series key
     */
    public void setShowSeriesKey(boolean value)
    {
        field_1_options = showSeriesKey.setShortBoolean(field_1_options, value);
    }

    /**
     * shows the series key
     * @return  the show series key field value.
     */
    public boolean isShowSeriesKey()
    {
        return showSeriesKey.isSet(field_1_options);
    }


}  // END OF CLASS




