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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * links a series to its position in the series list.<p/>
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class SeriesIndexRecord extends Record {
    public final static short      sid                             = 0x1065;
    private  short      field_1_index;


    public SeriesIndexRecord()
    {

    }

    public SeriesIndexRecord(RecordInputStream in)
    {
        field_1_index                  = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SINDEX]\n");
        buffer.append("    .index                = ")
            .append("0x").append(HexDump.toHex(  getIndex ()))
            .append(" (").append( getIndex() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/SINDEX]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_index);

        return getRecordSize();
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        SeriesIndexRecord rec = new SeriesIndexRecord();
    
        rec.field_1_index = field_1_index;
        return rec;
    }




    /**
     * Get the index field for the SeriesIndex record.
     */
    public short getIndex()
    {
        return field_1_index;
    }

    /**
     * Set the index field for the SeriesIndex record.
     */
    public void setIndex(short field_1_index)
    {
        this.field_1_index = field_1_index;
    }
}
