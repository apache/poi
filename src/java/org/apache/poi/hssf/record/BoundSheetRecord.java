
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Bound Sheet Record (aka BundleSheet) <P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheetname
 *               and tells where the Beginning of file record is within the HSSF
 *               file. <P>
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class BoundSheetRecord
    extends Record
{
    public final static short sid = 0x85;
    private int               field_1_position_of_BOF;
    private short             field_2_option_flags;
    private byte              field_3_sheetname_length;
    private byte              field_4_compressed_unicode_flag;   // not documented
    private String            field_5_sheetname;

    public BoundSheetRecord()
    {
    }

    /**
     * Constructs a BoundSheetRecord and sets its fields appropriately
     *
     * @param id     id must be 0x85 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BoundSheetRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a BoundSheetRecord and sets its fields appropriately
     *
     * @param id     id must be 0x85 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public BoundSheetRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A Bound Sheet RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_position_of_BOF         = LittleEndian.getInt(data,
                0 + offset);
        field_2_option_flags            = LittleEndian.getShort(data,
                4 + offset);
        field_3_sheetname_length        = data[ 6 + offset ];
        field_4_compressed_unicode_flag = data[ 7 + offset ];
        field_5_sheetname               = new String(data, 8 + offset,
                LittleEndian.ubyteToInt( field_3_sheetname_length));
    }

    /**
     * set the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     *
     * @param pos  offset in bytes
     */

    public void setPositionOfBof(int pos)
    {
        field_1_position_of_BOF = pos;
    }

    /**
     * set the option flags (unimportant for HSSF supported sheets)
     *
     * @param flags to set
     */

    public void setOptionFlags(short flags)
    {
        field_2_option_flags = flags;
    }

    /**
     * Set the length of the sheetname in characters
     *
     * @param len  number of characters in the sheet name
     * @see #setSheetname(String)
     */

    public void setSheetnameLength(byte len)
    {
        field_3_sheetname_length = len;
    }

    /**
     * set whether or not to interperate the Sheetname as compressed unicode (8/16 bit)
     * (This is undocumented but can be found as Q187919 on the Microsoft(tm) Support site)
     * @param flag (0/1) 0- compressed, 1 - uncompressed (16-bit)
     */

    public void setCompressedUnicodeFlag(byte flag)
    {
        field_4_compressed_unicode_flag = flag;
    }

    /**
     * Set the sheetname for this sheet.  (this appears in the tabs at the bottom)
     * @param sheetname the name of the sheet
     */

    public void setSheetname(String sheetname)
    {
        field_5_sheetname = sheetname;
    }

    /**
     * get the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     *
     * @return offset in bytes
     */

    public int getPositionOfBof()
    {
        return field_1_position_of_BOF;
    }

    /**
     * get the option flags (unimportant for HSSF supported sheets)
     *
     * @return flags to set
     */

    public short getOptionFlags()
    {
        return field_2_option_flags;
    }

    /**
     * get the length of the sheetname in characters
     *
     * @return number of characters in the sheet name
     * @see #getSheetname()
     */

    public byte getSheetnameLength()
    {
        return field_3_sheetname_length;
    }

    /**
     * get whether or not to interperate the Sheetname as compressed unicode (8/16 bit)
     * (This is undocumented but can be found as Q187919 on the Microsoft(tm) Support site)
     * @return flag (0/1) 0- compressed, 1 - uncompressed (16-bit)
     */

    public byte getCompressedUnicodeFlag()
    {
        return field_4_compressed_unicode_flag;
    }

    /**
     * get the sheetname for this sheet.  (this appears in the tabs at the bottom)
     * @return sheetname the name of the sheet
     */

    public String getSheetname()
    {
        return field_5_sheetname;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BOUNDSHEET]\n");
        buffer.append("    .bof             = ")
            .append(Integer.toHexString(getPositionOfBof())).append("\n");
        buffer.append("    .optionflags     = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("    .sheetname length= ")
            .append(Integer.toHexString(getSheetnameLength())).append("\n");
        buffer.append("    .unicodeflag     = ")
            .append(Integer.toHexString(getCompressedUnicodeFlag()))
            .append("\n");
        buffer.append("    .sheetname       = ").append(getSheetname())
            .append("\n");
        buffer.append("[/BOUNDSHEET]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) (0x08 + getSheetnameLength()));
        LittleEndian.putInt(data, 4 + offset, getPositionOfBof());
        LittleEndian.putShort(data, 8 + offset, getOptionFlags());
        data[ 10 + offset ] = getSheetnameLength();
        data[ 11 + offset ] = getCompressedUnicodeFlag();

        // we assume compressed unicode (bein the dern americans we are ;-p)
        StringUtil.putCompressedUnicode(getSheetname(), data, 12 + offset);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 12 + getSheetnameLength();
    }

    public short getSid()
    {
        return this.sid;
    }
}
