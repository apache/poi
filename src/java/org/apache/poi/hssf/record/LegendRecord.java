
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.record;



import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;

/**
 * The legend record specifies the location of legend on a chart and it's overall size.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class LegendRecord
    extends Record
{
    public final static short      sid                             = 0x1015;
    private  int        field_1_xPosition;
    private  int        field_2_yPosition;
    private  int        field_3_xSize;
    private  int        field_4_ySize;
    private  byte       field_5_type;
    public final static byte        TYPE_BOTTOM                    = 0;
    public final static byte        TYPE_CORNER                    = 1;
    public final static byte        TYPE_TOP                       = 2;
    public final static byte        TYPE_RIGHT                     = 3;
    public final static byte        TYPE_LEFT                      = 4;
    public final static byte        TYPE_NOT_DOCKED                = 7;
    private  byte       field_6_spacing;
    public final static byte        SPACING_CLOSE                  = 0;
    public final static byte        SPACING_MEDIUM                 = 1;
    public final static byte        SPACING_OPEN                   = 2;
    private  short      field_7_options;
    private BitField   autoPosition                               = new BitField(0x1);
    private BitField   autoSeries                                 = new BitField(0x2);
    private BitField   autoPosX                                   = new BitField(0x4);
    private BitField   autoPosY                                   = new BitField(0x8);
    private BitField   vert                                       = new BitField(0x10);
    private BitField   containsDataTable                          = new BitField(0x20);


    public LegendRecord()
    {

    }

    /**
     * Constructs a Legend record and sets its fields appropriately.
     *
     * @param id    id must be 0x1015 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public LegendRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Legend record and sets its fields appropriately.
     *
     * @param id    id must be 0x1015 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public LegendRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Legend record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_xPosition               = LittleEndian.getInt(data, 0x0 + offset);
        field_2_yPosition               = LittleEndian.getInt(data, 0x4 + offset);
        field_3_xSize                   = LittleEndian.getInt(data, 0x8 + offset);
        field_4_ySize                   = LittleEndian.getInt(data, 0xc + offset);
        field_5_type                    = data[ 0x10 + offset ];
        field_6_spacing                 = data[ 0x11 + offset ];
        field_7_options                 = LittleEndian.getShort(data, 0x12 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[Legend]\n");

        buffer.append("    .xPosition            = ")
            .append("0x")
            .append(HexDump.toHex((int)getXPosition()))
            .append(" (").append(getXPosition()).append(" )\n");

        buffer.append("    .yPosition            = ")
            .append("0x")
            .append(HexDump.toHex((int)getYPosition()))
            .append(" (").append(getYPosition()).append(" )\n");

        buffer.append("    .xSize                = ")
            .append("0x")
            .append(HexDump.toHex((int)getXSize()))
            .append(" (").append(getXSize()).append(" )\n");

        buffer.append("    .ySize                = ")
            .append("0x")
            .append(HexDump.toHex((int)getYSize()))
            .append(" (").append(getYSize()).append(" )\n");

        buffer.append("    .type                 = ")
            .append("0x")
            .append(HexDump.toHex((byte)getType()))
            .append(" (").append(getType()).append(" )\n");

        buffer.append("    .spacing              = ")
            .append("0x")
            .append(HexDump.toHex((byte)getSpacing()))
            .append(" (").append(getSpacing()).append(" )\n");

        buffer.append("    .options              = ")
            .append("0x")
            .append(HexDump.toHex((short)getOptions()))
            .append(" (").append(getOptions()).append(" )\n");
        buffer.append("         .autoPosition             = ").append(isAutoPosition        ()).append('\n');
        buffer.append("         .autoSeries               = ").append(isAutoSeries          ()).append('\n');
        buffer.append("         .autoPosX                 = ").append(isAutoPosX            ()).append('\n');
        buffer.append("         .autoPosY                 = ").append(isAutoPosY            ()).append('\n');
        buffer.append("         .vert                     = ").append(isVert                ()).append('\n');
        buffer.append("         .containsDataTable        = ").append(isContainsDataTable   ()).append('\n');

        buffer.append("[/Legend]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putInt(data, 4 + offset, field_1_xPosition);
        LittleEndian.putInt(data, 8 + offset, field_2_yPosition);
        LittleEndian.putInt(data, 12 + offset, field_3_xSize);
        LittleEndian.putInt(data, 16 + offset, field_4_ySize);
        data[ 20 + offset ] = field_5_type;
        data[ 21 + offset ] = field_6_spacing;
        LittleEndian.putShort(data, 22 + offset, field_7_options);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 4 + 4 + 4 + 4 + 1 + 1 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the x position field for the Legend record.
     */
    public int getXPosition()
    {
        return field_1_xPosition;
    }

    /**
     * Set the x position field for the Legend record.
     */
    public void setXPosition(int field_1_xPosition)
    {
        this.field_1_xPosition = field_1_xPosition;
    }

    /**
     * Get the y position field for the Legend record.
     */
    public int getYPosition()
    {
        return field_2_yPosition;
    }

    /**
     * Set the y position field for the Legend record.
     */
    public void setYPosition(int field_2_yPosition)
    {
        this.field_2_yPosition = field_2_yPosition;
    }

    /**
     * Get the x size field for the Legend record.
     */
    public int getXSize()
    {
        return field_3_xSize;
    }

    /**
     * Set the x size field for the Legend record.
     */
    public void setXSize(int field_3_xSize)
    {
        this.field_3_xSize = field_3_xSize;
    }

    /**
     * Get the y size field for the Legend record.
     */
    public int getYSize()
    {
        return field_4_ySize;
    }

    /**
     * Set the y size field for the Legend record.
     */
    public void setYSize(int field_4_ySize)
    {
        this.field_4_ySize = field_4_ySize;
    }

    /**
     * Get the type field for the Legend record.
     *
     * @return  One of 
     *        TYPE_BOTTOM
     *        TYPE_CORNER
     *        TYPE_TOP
     *        TYPE_RIGHT
     *        TYPE_LEFT
     *        TYPE_NOT_DOCKED
     */
    public byte getType()
    {
        return field_5_type;
    }

    /**
     * Set the type field for the Legend record.
     *
     * @param field_5_type
     *        One of 
     *        TYPE_BOTTOM
     *        TYPE_CORNER
     *        TYPE_TOP
     *        TYPE_RIGHT
     *        TYPE_LEFT
     *        TYPE_NOT_DOCKED
     */
    public void setType(byte field_5_type)
    {
        this.field_5_type = field_5_type;
    }

    /**
     * Get the spacing field for the Legend record.
     *
     * @return  One of 
     *        SPACING_CLOSE
     *        SPACING_MEDIUM
     *        SPACING_OPEN
     */
    public byte getSpacing()
    {
        return field_6_spacing;
    }

    /**
     * Set the spacing field for the Legend record.
     *
     * @param field_6_spacing
     *        One of 
     *        SPACING_CLOSE
     *        SPACING_MEDIUM
     *        SPACING_OPEN
     */
    public void setSpacing(byte field_6_spacing)
    {
        this.field_6_spacing = field_6_spacing;
    }

    /**
     * Get the options field for the Legend record.
     */
    public short getOptions()
    {
        return field_7_options;
    }

    /**
     * Set the options field for the Legend record.
     */
    public void setOptions(short field_7_options)
    {
        this.field_7_options = field_7_options;
    }

    /**
     * Sets the auto position field value.
     * set to true if legend is docked
     */
    public void setAutoPosition(boolean value)
    {
        field_7_options = autoPosition.setShortBoolean(field_7_options, value);
    }

    /**
     * set to true if legend is docked
     * @return  the auto position field value.
     */
    public boolean isAutoPosition()
    {
        return autoPosition.isSet(field_7_options);
    }

    /**
     * Sets the auto series field value.
     * automatic series distribution
     */
    public void setAutoSeries(boolean value)
    {
        field_7_options = autoSeries.setShortBoolean(field_7_options, value);
    }

    /**
     * automatic series distribution
     * @return  the auto series field value.
     */
    public boolean isAutoSeries()
    {
        return autoSeries.isSet(field_7_options);
    }

    /**
     * Sets the auto pos x field value.
     * x positioning is done automatically
     */
    public void setAutoPosX(boolean value)
    {
        field_7_options = autoPosX.setShortBoolean(field_7_options, value);
    }

    /**
     * x positioning is done automatically
     * @return  the auto pos x field value.
     */
    public boolean isAutoPosX()
    {
        return autoPosX.isSet(field_7_options);
    }

    /**
     * Sets the auto pos y field value.
     * y positioning is done automatically
     */
    public void setAutoPosY(boolean value)
    {
        field_7_options = autoPosY.setShortBoolean(field_7_options, value);
    }

    /**
     * y positioning is done automatically
     * @return  the auto pos y field value.
     */
    public boolean isAutoPosY()
    {
        return autoPosY.isSet(field_7_options);
    }

    /**
     * Sets the vert field value.
     * if true legend is vertical (otherwise it's horizonal)
     */
    public void setVert(boolean value)
    {
        field_7_options = vert.setShortBoolean(field_7_options, value);
    }

    /**
     * if true legend is vertical (otherwise it's horizonal)
     * @return  the vert field value.
     */
    public boolean isVert()
    {
        return vert.isSet(field_7_options);
    }

    /**
     * Sets the contains data table field value.
     * true if the chart contains the data table
     */
    public void setContainsDataTable(boolean value)
    {
        field_7_options = containsDataTable.setShortBoolean(field_7_options, value);
    }

    /**
     * true if the chart contains the data table
     * @return  the contains data table field value.
     */
    public boolean isContainsDataTable()
    {
        return containsDataTable.isSet(field_7_options);
    }


}  // END OF CLASS




