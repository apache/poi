
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
 * The default data label text properties record identifies the text characteristics of the preceeding text record.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class DefaultDataLabelTextPropertiesRecord
    extends Record
{
    public final static short      sid                             = 0x1024;
    private  short      field_1_categoryDataType;
    public final static short       CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC = 0;
    public final static short       CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC = 1;
    public final static short       CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC = 2;


    public DefaultDataLabelTextPropertiesRecord()
    {

    }

    /**
     * Constructs a DefaultDataLabelTextProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1024 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DefaultDataLabelTextPropertiesRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a DefaultDataLabelTextProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1024 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DefaultDataLabelTextPropertiesRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a DefaultDataLabelTextProperties record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_categoryDataType        = LittleEndian.getShort(data, 0x0 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DefaultDataLabelTextProperties]\n");

        buffer.append("    .categoryDataType     = ")
            .append("0x")
            .append(HexDump.toHex((short)getCategoryDataType()))
            .append(" (").append(getCategoryDataType()).append(" )\n");

        buffer.append("[/DefaultDataLabelTextProperties]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_categoryDataType);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @return  One of 
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public short getCategoryDataType()
    {
        return field_1_categoryDataType;
    }

    /**
     * Set the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @param field_1_categoryDataType
     *        One of 
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public void setCategoryDataType(short field_1_categoryDataType)
    {
        this.field_1_categoryDataType = field_1_categoryDataType;
    }


}  // END OF CLASS




