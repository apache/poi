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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Defines a legend for a chart.<p/>
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class LegendRecord extends StandardRecord {
    public final static short sid = 0x1015;

    private static final BitField autoPosition     = BitFieldFactory.getInstance(0x01);
    private static final BitField autoSeries       = BitFieldFactory.getInstance(0x02);
    private static final BitField autoXPositioning = BitFieldFactory.getInstance(0x04);
    private static final BitField autoYPositioning = BitFieldFactory.getInstance(0x08);
    private static final BitField vertical         = BitFieldFactory.getInstance(0x10);
    private static final BitField dataTable        = BitFieldFactory.getInstance(0x20);

    private  int        field_1_xAxisUpperLeft;
    private  int        field_2_yAxisUpperLeft;
    private  int        field_3_xSize;
    private  int        field_4_ySize;
    private  byte       field_5_type;
    public final static byte        TYPE_BOTTOM                    = 0;
    public final static byte        TYPE_CORNER                    = 1;
    public final static byte        TYPE_TOP                       = 2;
    public final static byte        TYPE_RIGHT                     = 3;
    public final static byte        TYPE_LEFT                      = 4;
    public final static byte        TYPE_UNDOCKED                  = 7;
    private  byte       field_6_spacing;
    public final static byte        SPACING_CLOSE                  = 0;
    public final static byte        SPACING_MEDIUM                 = 1;
    public final static byte        SPACING_OPEN                   = 2;
    private  short      field_7_options;


    public LegendRecord()
    {

    }

    public LegendRecord(RecordInputStream in)
    {
        field_1_xAxisUpperLeft         = in.readInt();
        field_2_yAxisUpperLeft         = in.readInt();
        field_3_xSize                  = in.readInt();
        field_4_ySize                  = in.readInt();
        field_5_type                   = in.readByte();
        field_6_spacing                = in.readByte();
        field_7_options                = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[LEGEND]\n");
        buffer.append("    .xAxisUpperLeft       = ")
            .append("0x").append(HexDump.toHex(  getXAxisUpperLeft ()))
            .append(" (").append( getXAxisUpperLeft() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .yAxisUpperLeft       = ")
            .append("0x").append(HexDump.toHex(  getYAxisUpperLeft ()))
            .append(" (").append( getYAxisUpperLeft() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .xSize                = ")
            .append("0x").append(HexDump.toHex(  getXSize ()))
            .append(" (").append( getXSize() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .ySize                = ")
            .append("0x").append(HexDump.toHex(  getYSize ()))
            .append(" (").append( getYSize() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .type                 = ")
            .append("0x").append(HexDump.toHex(  getType ()))
            .append(" (").append( getType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .spacing              = ")
            .append("0x").append(HexDump.toHex(  getSpacing ()))
            .append(" (").append( getSpacing() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .autoPosition             = ").append(isAutoPosition()).append('\n'); 
        buffer.append("         .autoSeries               = ").append(isAutoSeries()).append('\n'); 
        buffer.append("         .autoXPositioning         = ").append(isAutoXPositioning()).append('\n'); 
        buffer.append("         .autoYPositioning         = ").append(isAutoYPositioning()).append('\n'); 
        buffer.append("         .vertical                 = ").append(isVertical()).append('\n'); 
        buffer.append("         .dataTable                = ").append(isDataTable()).append('\n'); 

        buffer.append("[/LEGEND]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_xAxisUpperLeft);
        out.writeInt(field_2_yAxisUpperLeft);
        out.writeInt(field_3_xSize);
        out.writeInt(field_4_ySize);
        out.writeByte(field_5_type);
        out.writeByte(field_6_spacing);
        out.writeShort(field_7_options);
    }

    protected int getDataSize() {
        return 4 + 4 + 4 + 4 + 1 + 1 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        LegendRecord rec = new LegendRecord();
    
        rec.field_1_xAxisUpperLeft = field_1_xAxisUpperLeft;
        rec.field_2_yAxisUpperLeft = field_2_yAxisUpperLeft;
        rec.field_3_xSize = field_3_xSize;
        rec.field_4_ySize = field_4_ySize;
        rec.field_5_type = field_5_type;
        rec.field_6_spacing = field_6_spacing;
        rec.field_7_options = field_7_options;
        return rec;
    }




    /**
     * Get the x axis upper left field for the Legend record.
     */
    public int getXAxisUpperLeft()
    {
        return field_1_xAxisUpperLeft;
    }

    /**
     * Set the x axis upper left field for the Legend record.
     */
    public void setXAxisUpperLeft(int field_1_xAxisUpperLeft)
    {
        this.field_1_xAxisUpperLeft = field_1_xAxisUpperLeft;
    }

    /**
     * Get the y axis upper left field for the Legend record.
     */
    public int getYAxisUpperLeft()
    {
        return field_2_yAxisUpperLeft;
    }

    /**
     * Set the y axis upper left field for the Legend record.
     */
    public void setYAxisUpperLeft(int field_2_yAxisUpperLeft)
    {
        this.field_2_yAxisUpperLeft = field_2_yAxisUpperLeft;
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
     *        TYPE_UNDOCKED
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
     *        TYPE_UNDOCKED
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
     * automatic positioning (1=docked)
     */
    public void setAutoPosition(boolean value)
    {
        field_7_options = autoPosition.setShortBoolean(field_7_options, value);
    }

    /**
     * automatic positioning (1=docked)
     * @return  the auto position field value.
     */
    public boolean isAutoPosition()
    {
        return autoPosition.isSet(field_7_options);
    }

    /**
     * Sets the auto series field value.
     * excel 5 only (true)
     */
    public void setAutoSeries(boolean value)
    {
        field_7_options = autoSeries.setShortBoolean(field_7_options, value);
    }

    /**
     * excel 5 only (true)
     * @return  the auto series field value.
     */
    public boolean isAutoSeries()
    {
        return autoSeries.isSet(field_7_options);
    }

    /**
     * Sets the auto x positioning field value.
     * position of legend on the x axis is automatic
     */
    public void setAutoXPositioning(boolean value)
    {
        field_7_options = autoXPositioning.setShortBoolean(field_7_options, value);
    }

    /**
     * position of legend on the x axis is automatic
     * @return  the auto x positioning field value.
     */
    public boolean isAutoXPositioning()
    {
        return autoXPositioning.isSet(field_7_options);
    }

    /**
     * Sets the auto y positioning field value.
     * position of legend on the y axis is automatic
     */
    public void setAutoYPositioning(boolean value)
    {
        field_7_options = autoYPositioning.setShortBoolean(field_7_options, value);
    }

    /**
     * position of legend on the y axis is automatic
     * @return  the auto y positioning field value.
     */
    public boolean isAutoYPositioning()
    {
        return autoYPositioning.isSet(field_7_options);
    }

    /**
     * Sets the vertical field value.
     * vertical or horizontal legend (1 or 0 respectively).  Always 0 if not automatic.
     */
    public void setVertical(boolean value)
    {
        field_7_options = vertical.setShortBoolean(field_7_options, value);
    }

    /**
     * vertical or horizontal legend (1 or 0 respectively).  Always 0 if not automatic.
     * @return  the vertical field value.
     */
    public boolean isVertical()
    {
        return vertical.isSet(field_7_options);
    }

    /**
     * Sets the data table field value.
     * 1 if chart contains data table
     */
    public void setDataTable(boolean value)
    {
        field_7_options = dataTable.setShortBoolean(field_7_options, value);
    }

    /**
     * 1 if chart contains data table
     * @return  the data table field value.
     */
    public boolean isDataTable()
    {
        return dataTable.isSet(field_7_options);
    }
}
