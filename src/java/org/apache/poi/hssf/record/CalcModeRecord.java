
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
 * Title:        Calc Mode Record<P>
 * Description:  Tells the gui whether to calculate formulas
 *               automatically, manually or automatically
 *               except for tables.<P>
 * REFERENCE:  PG 292 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.CalcCountRecord
 */

public class CalcModeRecord
    extends Record
{
    public final static short sid                     = 0xD;

    /**
     * manually calculate formulas (0)
     */

    public final static short MANUAL                  = 0;

    /**
     * automatically calculate formulas (1)
     */

    public final static short AUTOMATIC               = 1;

    /**
     * automatically calculate formulas except for tables (-1)
     */

    public final static short AUTOMATIC_EXCEPT_TABLES = -1;
    private short             field_1_calcmode;

    public CalcModeRecord()
    {
    }

    /**
     * Constructs a CalcModeRecord and sets its fields appropriately
     *
     * @param id     id must be 0xD or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public CalcModeRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a CalcModeRecord and sets its fields appropriately
     *
     * @param id     id must be 0xD or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's start data
     */

    public CalcModeRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT An Calc Mode RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_calcmode = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set the calc mode flag for formulas
     *
     * @see #MANUAL
     * @see #AUTOMATIC
     * @see #AUTOMATIC_EXCEPT_TABLES
     *
     * @param calcmode one of the three flags above
     */

    public void setCalcMode(short calcmode)
    {
        field_1_calcmode = calcmode;
    }

    /**
     * get the calc mode flag for formulas
     *
     * @see #MANUAL
     * @see #AUTOMATIC
     * @see #AUTOMATIC_EXCEPT_TABLES
     *
     * @return calcmode one of the three flags above
     */

    public short getCalcMode()
    {
        return field_1_calcmode;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CALCMODE]\n");
        buffer.append("    .calcmode       = ")
            .append(Integer.toHexString(getCalcMode())).append("\n");
        buffer.append("[/CALCMODE]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, getCalcMode());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 6;
    }

    public short getSid()
    {
        return this.sid;
    }
}
