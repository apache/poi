
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
 * The series label record defines the type of label associated with the data format record.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class SeriesLabelsRecord
    extends Record
{
    public final static short      sid                             = 0x100c;
    private  short      field_1_formatFlags;
    private  BitField   showActual                                  = new BitField(0x1);
    private  BitField   showPercent                                 = new BitField(0x2);
    private  BitField   labelAsPercentage                           = new BitField(0x4);
    private  BitField   smoothedLine                                = new BitField(0x8);
    private  BitField   showLabel                                   = new BitField(0x10);
    private  BitField   showBubbleSizes                             = new BitField(0x20);


    public SeriesLabelsRecord()
    {

    }

    /**
     * Constructs a SeriesLabels record and sets its fields appropriately.
     *
     * @param id    id must be 0x100c or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SeriesLabelsRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a SeriesLabels record and sets its fields appropriately.
     *
     * @param id    id must be 0x100c or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SeriesLabelsRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a SeriesLabels record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_formatFlags            = LittleEndian.getShort(data, pos + 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ATTACHEDLABEL]\n");
        buffer.append("    .formatFlags          = ")
            .append("0x").append(HexDump.toHex(  getFormatFlags ()))
            .append(" (").append( getFormatFlags() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .showActual               = ").append(isShowActual()).append('\n'); 
        buffer.append("         .showPercent              = ").append(isShowPercent()).append('\n'); 
        buffer.append("         .labelAsPercentage        = ").append(isLabelAsPercentage()).append('\n'); 
        buffer.append("         .smoothedLine             = ").append(isSmoothedLine()).append('\n'); 
        buffer.append("         .showLabel                = ").append(isShowLabel()).append('\n'); 
        buffer.append("         .showBubbleSizes          = ").append(isShowBubbleSizes()).append('\n'); 

        buffer.append("[/ATTACHEDLABEL]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_formatFlags);

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
        SeriesLabelsRecord rec = new SeriesLabelsRecord();
    
        rec.field_1_formatFlags = field_1_formatFlags;
        return rec;
    }




    /**
     * Get the format flags field for the SeriesLabels record.
     */
    public short getFormatFlags()
    {
        return field_1_formatFlags;
    }

    /**
     * Set the format flags field for the SeriesLabels record.
     */
    public void setFormatFlags(short field_1_formatFlags)
    {
        this.field_1_formatFlags = field_1_formatFlags;
    }

    /**
     * Sets the show actual field value.
     * show actual value of the data point
     */
    public void setShowActual(boolean value)
    {
        field_1_formatFlags = showActual.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show actual value of the data point
     * @return  the show actual field value.
     */
    public boolean isShowActual()
    {
        return showActual.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show percent field value.
     * show value as percentage of total (pie charts only)
     */
    public void setShowPercent(boolean value)
    {
        field_1_formatFlags = showPercent.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show value as percentage of total (pie charts only)
     * @return  the show percent field value.
     */
    public boolean isShowPercent()
    {
        return showPercent.isSet(field_1_formatFlags);
    }

    /**
     * Sets the label as percentage field value.
     * show category label/value as percentage (pie charts only)
     */
    public void setLabelAsPercentage(boolean value)
    {
        field_1_formatFlags = labelAsPercentage.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show category label/value as percentage (pie charts only)
     * @return  the label as percentage field value.
     */
    public boolean isLabelAsPercentage()
    {
        return labelAsPercentage.isSet(field_1_formatFlags);
    }

    /**
     * Sets the smoothed line field value.
     * show smooth line
     */
    public void setSmoothedLine(boolean value)
    {
        field_1_formatFlags = smoothedLine.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show smooth line
     * @return  the smoothed line field value.
     */
    public boolean isSmoothedLine()
    {
        return smoothedLine.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show label field value.
     * display category label
     */
    public void setShowLabel(boolean value)
    {
        field_1_formatFlags = showLabel.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * display category label
     * @return  the show label field value.
     */
    public boolean isShowLabel()
    {
        return showLabel.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show bubble sizes field value.
     * ??
     */
    public void setShowBubbleSizes(boolean value)
    {
        field_1_formatFlags = showBubbleSizes.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * ??
     * @return  the show bubble sizes field value.
     */
    public boolean isShowBubbleSizes()
    {
        return showBubbleSizes.isSet(field_1_formatFlags);
    }


}  // END OF CLASS




