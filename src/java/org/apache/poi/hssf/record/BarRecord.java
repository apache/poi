
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
 * The bar record is used to define a bar chart.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class BarRecord
    extends Record
{
    public final static short      sid                             = 0x1017;
    private  short      field_1_barSpace;
    private  short      field_2_categorySpace;
    private  short      field_3_formatFlags;
    private  BitField   horizontal                                  = new BitField(0x1);
    private  BitField   stacked                                     = new BitField(0x2);
    private  BitField   displayAsPercentage                         = new BitField(0x4);
    private  BitField   shadow                                      = new BitField(0x8);


    public BarRecord()
    {

    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BarRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public BarRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Bar record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_barSpace               = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_categorySpace          = LittleEndian.getShort(data, pos + 0x2 + offset);
        field_3_formatFlags            = LittleEndian.getShort(data, pos + 0x4 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BAR]\n");
        buffer.append("    .barSpace             = ")
            .append("0x").append(HexDump.toHex(  getBarSpace ()))
            .append(" (").append( getBarSpace() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .categorySpace        = ")
            .append("0x").append(HexDump.toHex(  getCategorySpace ()))
            .append(" (").append( getCategorySpace() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formatFlags          = ")
            .append("0x").append(HexDump.toHex(  getFormatFlags ()))
            .append(" (").append( getFormatFlags() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .horizontal               = ").append(isHorizontal()).append('\n'); 
        buffer.append("         .stacked                  = ").append(isStacked()).append('\n'); 
        buffer.append("         .displayAsPercentage      = ").append(isDisplayAsPercentage()).append('\n'); 
        buffer.append("         .shadow                   = ").append(isShadow()).append('\n'); 

        buffer.append("[/BAR]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_barSpace);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_categorySpace);
        LittleEndian.putShort(data, 8 + offset + pos, field_3_formatFlags);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        BarRecord rec = new BarRecord();
    
        rec.field_1_barSpace = field_1_barSpace;
        rec.field_2_categorySpace = field_2_categorySpace;
        rec.field_3_formatFlags = field_3_formatFlags;
        return rec;
    }




    /**
     * Get the bar space field for the Bar record.
     */
    public short getBarSpace()
    {
        return field_1_barSpace;
    }

    /**
     * Set the bar space field for the Bar record.
     */
    public void setBarSpace(short field_1_barSpace)
    {
        this.field_1_barSpace = field_1_barSpace;
    }

    /**
     * Get the category space field for the Bar record.
     */
    public short getCategorySpace()
    {
        return field_2_categorySpace;
    }

    /**
     * Set the category space field for the Bar record.
     */
    public void setCategorySpace(short field_2_categorySpace)
    {
        this.field_2_categorySpace = field_2_categorySpace;
    }

    /**
     * Get the format flags field for the Bar record.
     */
    public short getFormatFlags()
    {
        return field_3_formatFlags;
    }

    /**
     * Set the format flags field for the Bar record.
     */
    public void setFormatFlags(short field_3_formatFlags)
    {
        this.field_3_formatFlags = field_3_formatFlags;
    }

    /**
     * Sets the horizontal field value.
     * true to display horizontal bar charts, false for vertical
     */
    public void setHorizontal(boolean value)
    {
        field_3_formatFlags = horizontal.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * true to display horizontal bar charts, false for vertical
     * @return  the horizontal field value.
     */
    public boolean isHorizontal()
    {
        return horizontal.isSet(field_3_formatFlags);
    }

    /**
     * Sets the stacked field value.
     * stack displayed values
     */
    public void setStacked(boolean value)
    {
        field_3_formatFlags = stacked.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * stack displayed values
     * @return  the stacked field value.
     */
    public boolean isStacked()
    {
        return stacked.isSet(field_3_formatFlags);
    }

    /**
     * Sets the display as percentage field value.
     * display chart values as a percentage
     */
    public void setDisplayAsPercentage(boolean value)
    {
        field_3_formatFlags = displayAsPercentage.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * display chart values as a percentage
     * @return  the display as percentage field value.
     */
    public boolean isDisplayAsPercentage()
    {
        return displayAsPercentage.isSet(field_3_formatFlags);
    }

    /**
     * Sets the shadow field value.
     * display a shadow for the chart
     */
    public void setShadow(boolean value)
    {
        field_3_formatFlags = shadow.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * display a shadow for the chart
     * @return  the shadow field value.
     */
    public boolean isShadow()
    {
        return shadow.isSet(field_3_formatFlags);
    }


}  // END OF CLASS




