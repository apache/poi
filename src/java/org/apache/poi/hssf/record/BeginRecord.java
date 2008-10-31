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

import org.apache.poi.util.LittleEndian;

/**
 * The begin record defines the start of a block of records for a (grpahing
 * data object. This record is matched with a corresponding EndRecord.
 *
 * @see EndRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class BeginRecord extends Record {
    public static final short sid = 0x1033;

    public BeginRecord()
    {
    }

    /**
     * @param in unused (since this record has no data)
     */
    public BeginRecord(RecordInputStream in)
    {
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BEGIN]\n");
        buffer.append("[/BEGIN]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putUShort(data, 0 + offset, sid);
        LittleEndian.putUShort(data, 2 + offset, 0);   // no record info
        return getRecordSize();
    }

    protected int getDataSize() {
        return 0;
    }

    public short getSid()
    {
        return sid;
    }
}
