
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
 * The axis record defines the type of an axis.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class AxisRecord
    extends Record
{
    public final static short      sid                             = 0x101d;
    private  short      field_1_axisType;
    public final static short       AXIS_TYPE_CATEGORY_OR_X_AXIS   = 0;
    public final static short       AXIS_TYPE_VALUE_AXIS           = 1;
    public final static short       AXIS_TYPE_SERIES_AXIS          = 2;
    private  int        field_2_reserved1;
    private  int        field_3_reserved2;
    private  int        field_4_reserved3;
    private  int        field_5_reserved4;


    public AxisRecord()
    {

    }

    /**
     * Constructs a Axis record and sets its fields appropriately.
     *
     * @param id    id must be 0x101d or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public AxisRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Axis record and sets its fields appropriately.
     *
     * @param id    id must be 0x101d or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public AxisRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Axis record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_axisType                = LittleEndian.getShort(data, 0x0 + offset);
        field_2_reserved1               = LittleEndian.getInt(data, 0x2 + offset);
        field_3_reserved2               = LittleEndian.getInt(data, 0x6 + offset);
        field_4_reserved3               = LittleEndian.getInt(data, 0xa + offset);
        field_5_reserved4               = LittleEndian.getInt(data, 0xe + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[Axis]\n");

        buffer.append("    .axisType             = ")
            .append("0x")
            .append(HexDump.toHex((short)getAxisType()))
            .append(" (").append(getAxisType()).append(" )\n");

        buffer.append("    .reserved1            = ")
            .append("0x")
            .append(HexDump.toHex((int)getReserved1()))
            .append(" (").append(getReserved1()).append(" )\n");

        buffer.append("    .reserved2            = ")
            .append("0x")
            .append(HexDump.toHex((int)getReserved2()))
            .append(" (").append(getReserved2()).append(" )\n");

        buffer.append("    .reserved3            = ")
            .append("0x")
            .append(HexDump.toHex((int)getReserved3()))
            .append(" (").append(getReserved3()).append(" )\n");

        buffer.append("    .reserved4            = ")
            .append("0x")
            .append(HexDump.toHex((int)getReserved4()))
            .append(" (").append(getReserved4()).append(" )\n");

        buffer.append("[/Axis]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_axisType);
        LittleEndian.putInt(data, 6 + offset, field_2_reserved1);
        LittleEndian.putInt(data, 10 + offset, field_3_reserved2);
        LittleEndian.putInt(data, 14 + offset, field_4_reserved3);
        LittleEndian.putInt(data, 18 + offset, field_5_reserved4);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 4 + 4 + 4 + 4;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the axis type field for the Axis record.
     *
     * @return  One of 
     *        AXIS_TYPE_CATEGORY_OR_X_AXIS
     *        AXIS_TYPE_VALUE_AXIS
     *        AXIS_TYPE_SERIES_AXIS
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the Axis record.
     *
     * @param field_1_axisType
     *        One of 
     *        AXIS_TYPE_CATEGORY_OR_X_AXIS
     *        AXIS_TYPE_VALUE_AXIS
     *        AXIS_TYPE_SERIES_AXIS
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }

    /**
     * Get the reserved1 field for the Axis record.
     */
    public int getReserved1()
    {
        return field_2_reserved1;
    }

    /**
     * Set the reserved1 field for the Axis record.
     */
    public void setReserved1(int field_2_reserved1)
    {
        this.field_2_reserved1 = field_2_reserved1;
    }

    /**
     * Get the reserved2 field for the Axis record.
     */
    public int getReserved2()
    {
        return field_3_reserved2;
    }

    /**
     * Set the reserved2 field for the Axis record.
     */
    public void setReserved2(int field_3_reserved2)
    {
        this.field_3_reserved2 = field_3_reserved2;
    }

    /**
     * Get the reserved3 field for the Axis record.
     */
    public int getReserved3()
    {
        return field_4_reserved3;
    }

    /**
     * Set the reserved3 field for the Axis record.
     */
    public void setReserved3(int field_4_reserved3)
    {
        this.field_4_reserved3 = field_4_reserved3;
    }

    /**
     * Get the reserved4 field for the Axis record.
     */
    public int getReserved4()
    {
        return field_5_reserved4;
    }

    /**
     * Set the reserved4 field for the Axis record.
     */
    public void setReserved4(int field_5_reserved4)
    {
        this.field_5_reserved4 = field_5_reserved4;
    }


}  // END OF CLASS




