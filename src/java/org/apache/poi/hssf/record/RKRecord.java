
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
import org.apache.poi.hssf.util.RKUtil;

/**
 * Title:        RK Record
 * Description:  An internal 32 bit number with the two most significant bits
 *               storing the type.  This is part of a bizarre scheme to save disk
 *               space and memory (gee look at all the other whole records that
 *               are in the file just "cause"..,far better to waste processor
 *               cycles on this then leave on of those "valuable" records out).<P>
 * We support this in READ-ONLY mode.  HSSF converts these to NUMBER records<P>
 *
 *
 *
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.NumberRecord
 */

public class RKRecord
    extends Record
    implements CellValueRecordInterface
{
    public final static short sid                      = 0x27e;
    public final static short RK_IEEE_NUMBER           = 0;
    public final static short RK_IEEE_NUMBER_TIMES_100 = 1;
    public final static short RK_INTEGER               = 2;
    public final static short RK_INTEGER_TIMES_100     = 3;
    private short             field_1_row;
    private short             field_2_col;
    private short             field_3_xf_index;
    private int               field_4_rk_number;

    public RKRecord()
    {
    }

    /**
     * Constructs a RK record and sets its fields appropriately.
     *
     * @param id     id must be 0x27e or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public RKRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a RK record and sets its fields appropriately.
     *
     * @param id     id must be 0x27e or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the data
     */

    public RKRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid RK RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_row       = LittleEndian.getShort(data, 0 + offset);
        field_2_col       = LittleEndian.getShort(data, 2 + offset);
        field_3_xf_index  = LittleEndian.getShort(data, 4 + offset);
        field_4_rk_number = LittleEndian.getInt(data, 6 + offset);
    }

    public short getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return field_2_col;
    }

    public short getXFIndex()
    {
        return field_3_xf_index;
    }

    public int getRKField()
    {
        return field_4_rk_number;
    }

    /**
     * Get the type of the number
     *
     * @return one of these values:
     *         <OL START="0">
     *             <LI>RK_IEEE_NUMBER</LI>
     *             <LI>RK_IEEE_NUMBER_TIMES_100</LI>
     *             <LI>RK_INTEGER</LI>
     *             <LI>RK_INTEGER_TIMES_100</LI>
     *         </OL>
     */

    public short getRKType()
    {
        return ( short ) (field_4_rk_number & 3);
    }

    /**
     * Extract the value of the number
     * <P>
     * The mechanism for determining the value is dependent on the two
     * low order bits of the raw number. If bit 1 is set, the number
     * is an integer and can be cast directly as a double, otherwise,
     * it's apparently the exponent and mantissa of a double (and the
     * remaining low-order bits of the double's mantissa are 0's).
     * <P>
     * If bit 0 is set, the result of the conversion to a double is
     * divided by 100; otherwise, the value is left alone.
     * <P>
     * [insert picture of Screwy Squirrel in full Napoleonic regalia]
     *
     * @return the value as a proper double (hey, it <B>could</B>
     *         happen)
     */

    public double getRKNumber()
    {
        return RKUtil.decodeNumber(field_4_rk_number);
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[RK]\n");
        buffer.append("    .row            = ")
            .append(Integer.toHexString(getRow())).append("\n");
        buffer.append("    .col            = ")
            .append(Integer.toHexString(getColumn())).append("\n");
        buffer.append("    .xfindex        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("    .rknumber       = ")
            .append(Integer.toHexString(getRKField())).append("\n");
        buffer.append("        .rktype     = ")
            .append(Integer.toHexString(getRKType())).append("\n");
        buffer.append("        .rknumber   = ").append(getRKNumber())
            .append("\n");
        buffer.append("[/RK]\n");
        return buffer.toString();
    }

//temporarily just constructs a new number record and returns its value
    public int serialize(int offset, byte [] data)
    {
        NumberRecord rec = new NumberRecord();

        rec.setColumn(getColumn());
        rec.setRow(getRow());
        rec.setValue(getRKNumber());
        rec.setXFIndex(getXFIndex());
        return rec.serialize(offset, data);
    }

    /**
     * Debugging main()
     * <P>
     * Normally I'd do this in a junit test, but let's face it -- once
     * this algorithm has been tested and it works, we are never ever
     * going to change it. This is driven by the Faceless Enemy's
     * minions, who dare not change the algorithm out from under us.
     *
     * @param ignored_args command line arguments, which we blithely
     *                     ignore
     */

    public static void main(String ignored_args[])
    {
        int[]    values  =
        {
            0x3FF00000, 0x405EC001, 0x02F1853A, 0x02F1853B, 0xFCDD699A
        };
        double[] rvalues =
        {
            1, 1.23, 12345678, 123456.78, -13149594
        };

        for (int j = 0; j < values.length; j++)
        {
            System.out.println("input = " + Integer.toHexString(values[ j ])
                               + " -> " + rvalues[ j ] + ": "
                               + RKUtil.decodeNumber(values[ j ]));
        }
    }

    public short getSid()
    {
        return this.sid;
    }

    public boolean isBefore(CellValueRecordInterface i)
    {
        if (this.getRow() > i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() > i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    public boolean isAfter(CellValueRecordInterface i)
    {
        if (this.getRow() < i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() < i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    public boolean isEqual(CellValueRecordInterface i)
    {
        return ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()));
    }

    public boolean isInValueSection()
    {
        return true;
    }

    public boolean isValue()
    {
        return true;
    }

    public void setColumn(short col)
    {
    }

    public void setRow(short row)
    {
    }

    /**
     * NO OP!
     */

    public void setXFIndex(short xf)
    {
    }
}
