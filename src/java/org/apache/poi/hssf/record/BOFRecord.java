
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

/**
 * Title: Beginning Of File<P>
 * Description: Somewhat of a misnomer, its used for the beginning of a set of
 *              records that have a particular pupose or subject.
 *              Used in sheets and workbooks.<P>
 * REFERENCE:  PG 289 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver
 * @version 2.0-pre
 */

public class BOFRecord
    extends Record
{

    /**
     * for BIFF8 files the BOF is 0x809.  For earlier versions it was 0x09 or 0x(biffversion)09
     */

    public final static short sid = 0x809;
    private short             field_1_version;
    private short             field_2_type;
    private short             field_3_build;
    private short             field_4_year;
    private int               field_5_history;
    private int               field_6_rversion;

    /**
     * suggested default (0x06 - BIFF8)
     */

    public final static short VERSION             = 0x06;

    /**
     * suggested default 0x10d3
     */

    public final static short BUILD               = 0x10d3;

    /**
     * suggested default  0x07CC (1996)
     */

    public final static short BUILD_YEAR          = 0x07CC;   // 1996

    /**
     * suggested default for a normal sheet (0x41)
     */

    public final static short HISTORY_MASK        = 0x41;
    public final static short TYPE_WORKBOOK       = 0x05;
    public final static short TYPE_VB_MODULE      = 0x06;
    public final static short TYPE_WORKSHEET      = 0x10;
    public final static short TYPE_CHART          = 0x20;
    public final static short TYPE_EXCEL_4_MACRO  = 0x40;
    public final static short TYPE_WORKSPACE_FILE = 0x100;

    /**
     * Constructs an empty BOFRecord with no fields set.
     */

    public BOFRecord()
    {
    }

    /**
     * Constructs a BOFRecord and sets its fields appropriately
     *
     * @param id     id must be 0x809 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BOFRecord(short id, short size, byte [] data)
    {
        super(id, size, data);

        // fillFields(data,size);
    }

    /**
     * Constructs a BOFRecord and sets its fields appropriately
     *
     * @param id     id must be 0x809 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset the offset of the record's data
     */

    public BOFRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);

        // fillFields(data,size);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A BOF RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_version  = LittleEndian.getShort(data, 0 + offset);
        field_2_type     = LittleEndian.getShort(data, 2 + offset);
        field_3_build    = LittleEndian.getShort(data, 4 + offset);
        field_4_year     = LittleEndian.getShort(data, 6 + offset);
        field_5_history  = LittleEndian.getInt(data, 8 + offset);
        field_6_rversion = LittleEndian.getInt(data, 12 + offset);
    }

    /**
     * Version number - for BIFF8 should be 0x06
     * @see #VERSION
     * @param short version to be set
     */

    public void setVersion(short version)
    {
        field_1_version = version;
    }

    /**
     * type of object this marks
     * @see #TYPE_WORKBOOK
     * @see #TYPE_VB_MODULE
     * @see #TYPE_WORKSHEET
     * @see #TYPE_CHART
     * @see #TYPE_EXCEL_4_MACRO
     * @see #TYPE_WORKSPACE_FILE
     * @param short type to be set
     */

    public void setType(short type)
    {
        field_2_type = type;
    }

    /**
     * build that wrote this file
     * @see #BUILD
     * @param short build number to set
     */

    public void setBuild(short build)
    {
        field_3_build = build;
    }

    /**
     * Year of the build that wrote this file
     * @see #BUILD_YEAR
     * @param short build year to set
     */

    public void setBuildYear(short year)
    {
        field_4_year = year;
    }

    /**
     * set the history bit mask (not very useful)
     * @see #HISTORY_MASK
     * @param int bitmask to set for the history
     */

    public void setHistoryBitMask(int bitmask)
    {
        field_5_history = bitmask;
    }

    /**
     * set the minimum version required to read this file
     *
     * @see #VERSION
     * @param int version to set
     */

    public void setRequiredVersion(int version)
    {
        field_6_rversion = version;
    }

    /**
     * Version number - for BIFF8 should be 0x06
     * @see #VERSION
     * @return short version number of the generator of this file
     */

    public short getVersion()
    {
        return field_1_version;
    }

    /**
     * type of object this marks
     * @see #TYPE_WORKBOOK
     * @see #TYPE_VB_MODULE
     * @see #TYPE_WORKSHEET
     * @see #TYPE_CHART
     * @see #TYPE_EXCEL_4_MACRO
     * @see #TYPE_WORKSPACE_FILE
     * @return short type of object
     */

    public short getType()
    {
        return field_2_type;
    }

    /**
     * get the build that wrote this file
     * @see #BUILD
     * @return short build number of the generator of this file
     */

    public short getBuild()
    {
        return field_3_build;
    }

    /**
     * Year of the build that wrote this file
     * @see #BUILD_YEAR
     * @return short build year of the generator of this file
     */

    public short getBuildYear()
    {
        return field_4_year;
    }

    /**
     * get the history bit mask (not very useful)
     * @see #HISTORY_MASK
     * @return int bitmask showing the history of the file (who cares!)
     */

    public int getHistoryBitMask()
    {
        return field_5_history;
    }

    /**
     * get the minimum version required to read this file
     *
     * @see #VERSION
     * @return int least version that can read the file
     */

    public int getRequiredVersion()
    {
        return field_6_rversion;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BOF RECORD]\n");
        buffer.append("    .version         = ")
            .append(Integer.toHexString(getVersion())).append("\n");
        buffer.append("    .type            = ")
            .append(Integer.toHexString(getType())).append("\n");
        buffer.append("    .build           = ")
            .append(Integer.toHexString(getBuild())).append("\n");
        buffer.append("    .buildyear       = ").append(getBuildYear())
            .append("\n");
        buffer.append("    .history         = ")
            .append(Integer.toHexString(getHistoryBitMask())).append("\n");
        buffer.append("    .requiredversion = ")
            .append(Integer.toHexString(getRequiredVersion())).append("\n");
        buffer.append("[/BOF RECORD]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x10));   // 16 byte length
        LittleEndian.putShort(data, 4 + offset, getVersion());
        LittleEndian.putShort(data, 6 + offset, getType());
        LittleEndian.putShort(data, 8 + offset, getBuild());
        LittleEndian.putShort(data, 10 + offset, getBuildYear());
        LittleEndian.putInt(data, 12 + offset, getHistoryBitMask());
        LittleEndian.putInt(data, 16 + offset, getRequiredVersion());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 20;
    }

    public short getSid()
    {
        return this.sid;
    }
}
