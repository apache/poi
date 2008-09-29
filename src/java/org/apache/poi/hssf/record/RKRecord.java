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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.util.RKUtil;
import org.apache.poi.util.HexDump;

/**
 * Title:        RK Record (0x027E)
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
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.NumberRecord
 */
public final class RKRecord extends Record implements CellValueRecordInterface {
    public final static short sid                      = 0x027E;
    public final static short RK_IEEE_NUMBER           = 0;
    public final static short RK_IEEE_NUMBER_TIMES_100 = 1;
    public final static short RK_INTEGER               = 2;
    public final static short RK_INTEGER_TIMES_100     = 3;
    private int field_1_row;
    private int field_2_col;
    private int field_3_xf_index;
    private int field_4_rk_number;

    public RKRecord()
    {
    }

    /**
     * Constructs a RK record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */
    public RKRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid RK RECORD");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_row       = in.readUShort();
        field_2_col       = in.readUShort();
        field_3_xf_index  = in.readUShort();
        field_4_rk_number = in.readInt();
    }

    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return (short) field_2_col;
    }

    public short getXFIndex()
    {
        return (short) field_3_xf_index;
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
        StringBuffer sb = new StringBuffer();

        sb.append("[RK]\n");
        sb.append("    .row       = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .col       = ").append(HexDump.shortToHex(getColumn())).append("\n");
        sb.append("    .xfindex   = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
        sb.append("    .rknumber  = ").append(HexDump.intToHex(getRKField())).append("\n");
        sb.append("      .rktype  = ").append(HexDump.byteToHex(getRKType())).append("\n");
        sb.append("      .rknumber= ").append(getRKNumber()).append("\n");
        sb.append("[/RK]\n");
        return sb.toString();
    }

// temporarily just constructs a new number record and returns its value
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
        return sid;
    }

    public void setColumn(short col)
    {
    }

    public void setRow(int row)
    {
    }

    /**
     * NO OP!
     */

    public void setXFIndex(short xf)
    {
    }

    public Object clone() {
      RKRecord rec = new RKRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_col = field_2_col;
      rec.field_3_xf_index = field_3_xf_index;
      rec.field_4_rk_number = field_4_rk_number;
      return rec;
    }
}
