
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
        

/*
 * ExtSSTInfoSubRecord.java
 *
 * Created on September 8, 2001, 8:37 PM
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Extended SST table info subrecord<P>
 * contains the elements of "info" in the SST's array field<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.ExtSSTRecord
 */

public class ExtSSTInfoSubRecord
    extends Record
{
   public static final int INFO_SIZE = 8;
    public final static short sid =
        0xFFF;                                             // only here for conformance, doesn't really have an sid
    private int               field_1_stream_pos;          // stream pointer to the SST record
    private short             field_2_bucket_sst_offset;   // don't really understand this yet.
    private short             field_3_zero;                // must be 0;

    /** Creates new ExtSSTInfoSubRecord */

    public ExtSSTInfoSubRecord()
    {
    }

    public ExtSSTInfoSubRecord(RecordInputStream in)
    {
        field_1_stream_pos        = in.readInt();
        field_2_bucket_sst_offset = in.readShort();
        field_3_zero              = in.readShort();
    }

    public void setStreamPos(int pos)
    {
        field_1_stream_pos = pos;
    }

    public void setBucketRecordOffset(short offset)
    {
        field_2_bucket_sst_offset = offset;
    }

    public int getStreamPos()
    {
        return field_1_stream_pos;
    }

    public short getBucketSSTOffset()
    {
        return field_2_bucket_sst_offset;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[EXTSST]\n");
        buffer.append("    .streampos      = ")
            .append(Integer.toHexString(getStreamPos())).append("\n");
        buffer.append("    .bucketsstoffset= ")
            .append(Integer.toHexString(getBucketSSTOffset())).append("\n");
        buffer.append("    .zero           = ")
            .append(Integer.toHexString(field_3_zero)).append("\n");
        buffer.append("[/EXTSST]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putInt(data, 0 + offset, getStreamPos());
        LittleEndian.putShort(data, 4 + offset, getBucketSSTOffset());
        LittleEndian.putShort(data, 6 + offset, ( short ) 0);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8;
    }

    public short getSid()
    {
        return sid;
    }
}
