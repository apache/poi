
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
 * The font basis record stores various font metrics.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class FontBasisRecord
    extends Record
{
    public final static short      sid                             = 0x1060;
    private  short      field_1_xBasis;
    private  short      field_2_yBasis;
    private  short      field_3_heightBasis;
    private  short      field_4_scale;
    private  short      field_5_indexToFontTable;


    public FontBasisRecord()
    {

    }

    /**
     * Constructs a FontBasis record and sets its fields appropriately.
     *
     * @param id    id must be 0x1060 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public FontBasisRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a FontBasis record and sets its fields appropriately.
     *
     * @param id    id must be 0x1060 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public FontBasisRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a FontBasis record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_xBasis                 = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_yBasis                 = LittleEndian.getShort(data, pos + 0x2 + offset);
        field_3_heightBasis            = LittleEndian.getShort(data, pos + 0x4 + offset);
        field_4_scale                  = LittleEndian.getShort(data, pos + 0x6 + offset);
        field_5_indexToFontTable       = LittleEndian.getShort(data, pos + 0x8 + offset);

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

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_xBasis);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_yBasis);
        LittleEndian.putShort(data, 8 + offset + pos, field_3_heightBasis);
        LittleEndian.putShort(data, 10 + offset + pos, field_4_scale);
        LittleEndian.putShort(data, 12 + offset + pos, field_5_indexToFontTable);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
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


}  // END OF CLASS




