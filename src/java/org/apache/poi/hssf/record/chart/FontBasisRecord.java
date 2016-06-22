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
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The font basis record stores various font metrics.
 */
public final class FontBasisRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x1060;
    private  short      field_1_xBasis;
    private  short      field_2_yBasis;
    private  short      field_3_heightBasis;
    private  short      field_4_scale;
    private  short      field_5_indexToFontTable;


    public FontBasisRecord()
    {

    }

    public FontBasisRecord(RecordInputStream in)
    {
        field_1_xBasis                 = in.readShort();
        field_2_yBasis                 = in.readShort();
        field_3_heightBasis            = in.readShort();
        field_4_scale                  = in.readShort();
        field_5_indexToFontTable       = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FBI]\n");
        buffer.append("    .xBasis               = ")
            .append("0x").append(HexDump.toHex(  getXBasis ()))
            .append(" (").append( getXBasis() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .yBasis               = ")
            .append("0x").append(HexDump.toHex(  getYBasis ()))
            .append(" (").append( getYBasis() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .heightBasis          = ")
            .append("0x").append(HexDump.toHex(  getHeightBasis ()))
            .append(" (").append( getHeightBasis() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .scale                = ")
            .append("0x").append(HexDump.toHex(  getScale ()))
            .append(" (").append( getScale() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .indexToFontTable     = ")
            .append("0x").append(HexDump.toHex(  getIndexToFontTable ()))
            .append(" (").append( getIndexToFontTable() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/FBI]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_xBasis);
        out.writeShort(field_2_yBasis);
        out.writeShort(field_3_heightBasis);
        out.writeShort(field_4_scale);
        out.writeShort(field_5_indexToFontTable);
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public FontBasisRecord clone() {
        FontBasisRecord rec = new FontBasisRecord();
    
        rec.field_1_xBasis = field_1_xBasis;
        rec.field_2_yBasis = field_2_yBasis;
        rec.field_3_heightBasis = field_3_heightBasis;
        rec.field_4_scale = field_4_scale;
        rec.field_5_indexToFontTable = field_5_indexToFontTable;
        return rec;
    }




    /**
     * Get the x Basis field for the FontBasis record.
     */
    public short getXBasis()
    {
        return field_1_xBasis;
    }

    /**
     * Set the x Basis field for the FontBasis record.
     */
    public void setXBasis(short field_1_xBasis)
    {
        this.field_1_xBasis = field_1_xBasis;
    }

    /**
     * Get the y Basis field for the FontBasis record.
     */
    public short getYBasis()
    {
        return field_2_yBasis;
    }

    /**
     * Set the y Basis field for the FontBasis record.
     */
    public void setYBasis(short field_2_yBasis)
    {
        this.field_2_yBasis = field_2_yBasis;
    }

    /**
     * Get the height basis field for the FontBasis record.
     */
    public short getHeightBasis()
    {
        return field_3_heightBasis;
    }

    /**
     * Set the height basis field for the FontBasis record.
     */
    public void setHeightBasis(short field_3_heightBasis)
    {
        this.field_3_heightBasis = field_3_heightBasis;
    }

    /**
     * Get the scale field for the FontBasis record.
     */
    public short getScale()
    {
        return field_4_scale;
    }

    /**
     * Set the scale field for the FontBasis record.
     */
    public void setScale(short field_4_scale)
    {
        this.field_4_scale = field_4_scale;
    }

    /**
     * Get the index to font table field for the FontBasis record.
     */
    public short getIndexToFontTable()
    {
        return field_5_indexToFontTable;
    }

    /**
     * Set the index to font table field for the FontBasis record.
     */
    public void setIndexToFontTable(short field_5_indexToFontTable)
    {
        this.field_5_indexToFontTable = field_5_indexToFontTable;
    }
}
