/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * Title: Recalc Id Record<P>
 * Description:  This record contains an ID that marks when a worksheet was last
 *               recalculated. It's an optimization Excel uses to determine if it
 *               needs to  recalculate the spreadsheet when it's opened. So far, only
 *               the two values <code>0xC1 0x01 0x00 0x00 0x80 0x38 0x01 0x00</code>
 *               (do not recalculate) and <code>0xC1 0x01 0x00 0x00 0x60 0x69 0x01
 *               0x00</code> have been seen. If the field <code>isNeeded</code> is
 *               set to false (default), then this record is swallowed during the
 *               serialization process<P>
 * REFERENCE:  http://chicago.sourceforge.net/devel/docs/excel/biff8.html<P>
 * @author Luc Girardin (luc dot girardin at macrofocus dot com)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.model.Workbook
 */

public class RecalcIdRecord
    extends Record
{
    public final static short sid = 0x1c1;
    public short[]            field_1_recalcids;

    private boolean isNeeded = false;

    public RecalcIdRecord()
    {
    }

    /**
     * Constructs a RECALCID record and sets its fields appropriately.
     *
     * @param id     id must be 0x13d or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public RecalcIdRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a RECALCID record and sets its fields appropriately.
     *
     * @param id     id must be 0x13d or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record
     */

    public RecalcIdRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A RECALCID RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_recalcids = new short[ size / 2 ];
        for (int k = 0; k < field_1_recalcids.length; k++)
        {
            field_1_recalcids[ k ] = LittleEndian.getShort(data,
                                                        (k * 2) + offset);
        }
    }

    /**
     * set the recalc array.
     * @param array of recalc id's
     */

    public void setRecalcIdArray(short [] array)
    {
        field_1_recalcids = array;
    }

    /**
     * get the recalc array.
     * @return array of recalc id's
     */

    public short [] getRecalcIdArray()
    {
        return field_1_recalcids;
    }

    public void setIsNeeded(boolean isNeeded) {
        this.isNeeded = isNeeded;
    }

    public boolean isNeeded() {
        return isNeeded;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[RECALCID]\n");
        buffer.append("    .elements        = ").append(field_1_recalcids.length)
            .append("\n");
        for (int k = 0; k < field_1_recalcids.length; k++)
        {
            buffer.append("    .element_" + k + "       = ")
                .append(field_1_recalcids[ k ]).append("\n");
        }
        buffer.append("[/RECALCID]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        short[] tabids     = getRecalcIdArray();
        short   length     = ( short ) (tabids.length * 2);
        int     byteoffset = 4;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) length));

        // 2 (num bytes in a short)
        for (int k = 0; k < (length / 2); k++)
        {
            LittleEndian.putShort(data, byteoffset + offset, tabids[ k ]);
            byteoffset += 2;
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 4 + (getRecalcIdArray().length * 2);
    }

    public short getSid()
    {
        return this.sid;
    }
}
