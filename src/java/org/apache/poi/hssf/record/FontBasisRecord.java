
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
        field_1_xBasis                  = LittleEndian.getShort(data, 0x0 + offset);
        field_2_yBasis                  = LittleEndian.getShort(data, 0x2 + offset);
        field_3_heightBasis             = LittleEndian.getShort(data, 0x4 + offset);
        field_4_scale                   = LittleEndian.getShort(data, 0x6 + offset);
        field_5_indexToFontTable        = LittleEndian.getShort(data, 0x8 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FontBasis]\n");

        buffer.append("    .xBasis               = ")
            .append("0x")
            .append(HexDump.toHex((short)getXBasis()))
            .append(" (").append(getXBasis()).append(" )\n");

        buffer.append("    .yBasis               = ")
            .append("0x")
            .append(HexDump.toHex((short)getYBasis()))
            .append(" (").append(getYBasis()).append(" )\n");

        buffer.append("    .heightBasis          = ")
            .append("0x")
            .append(HexDump.toHex((short)getHeightBasis()))
            .append(" (").append(getHeightBasis()).append(" )\n");

        buffer.append("    .scale                = ")
            .append("0x")
            .append(HexDump.toHex((short)getScale()))
            .append(" (").append(getScale()).append(" )\n");

        buffer.append("    .indexToFontTable     = ")
            .append("0x")
            .append(HexDump.toHex((short)getIndexToFontTable()))
            .append(" (").append(getIndexToFontTable()).append(" )\n");

        buffer.append("[/FontBasis]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_xBasis);
        LittleEndian.putShort(data, 6 + offset, field_2_yBasis);
        LittleEndian.putShort(data, 8 + offset, field_3_heightBasis);
        LittleEndian.putShort(data, 10 + offset, field_4_scale);
        LittleEndian.putShort(data, 12 + offset, field_5_indexToFontTable);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
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




