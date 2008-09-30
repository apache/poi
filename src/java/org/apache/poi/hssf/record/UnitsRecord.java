
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



import org.apache.poi.util.*;

/**
 * The units record describes units.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class UnitsRecord
    extends Record
{
    public final static short      sid                             = 0x1001;
    private  short      field_1_units;


    public UnitsRecord()
    {

    }

    /**
     * Constructs a Units record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */
    public UnitsRecord(RecordInputStream in)
    {
        super(in);
    
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_units                  = in.readShort();

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[UNITS]\n");
        buffer.append("    .units                = ")
            .append("0x").append(HexDump.toHex(  getUnits ()))
            .append(" (").append( getUnits() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/UNITS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_units);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        UnitsRecord rec = new UnitsRecord();
    
        rec.field_1_units = field_1_units;
        return rec;
    }




    /**
     * Get the units field for the Units record.
     */
    public short getUnits()
    {
        return field_1_units;
    }

    /**
     * Set the units field for the Units record.
     */
    public void setUnits(short field_1_units)
    {
        this.field_1_units = field_1_units;
    }


}  // END OF CLASS




