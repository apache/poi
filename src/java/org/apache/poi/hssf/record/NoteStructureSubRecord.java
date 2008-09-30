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
 * Represents a NoteStructure (0xD) sub record.
 *
 * <p>
 * The docs say nothing about it. The length of this record is always 26 bytes.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class NoteStructureSubRecord
    extends SubRecord
{
    public final static short      sid                             = 0x0D;

    private byte[] reserved;

    /**
     * Construct a new <code>NoteStructureSubRecord</code> and
     * fill its data with the default values
     */
    public NoteStructureSubRecord()
    {
        //all we know is that the the length of <code>NoteStructureSubRecord</code> is always 22 bytes
        reserved = new byte[22];
    }

    /**
     * Constructs a NoteStructureSubRecord and sets its fields appropriately.
     *
     */
    public NoteStructureSubRecord(RecordInputStream in)
    {
        super(in);
    
    }

    /**
     * Read the record data from the supplied <code>RecordInputStream</code>
     */
    protected void fillFields(RecordInputStream in)
    {
        //just grab the raw data
        reserved = in.readRemainder();
    }

    /**
     * Convert this record to string.
     * Used by BiffViewer and other utulities.
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        String nl = System.getProperty("line.separator");
        buffer.append("[ftNts ]" + nl);
        buffer.append("  size     = ").append(getRecordSize()).append(nl);
        buffer.append("  reserved = ").append(HexDump.toHex(reserved)).append(nl);
        buffer.append("[/ftNts ]" + nl);
        return buffer.toString();
    }

    /**
     * Serialize the record data into the supplied array of bytes
     *
     * @param offset offset in the <code>data</code>
     * @param data the data to serialize into
     *
     * @return size of the record
     */
    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
        System.arraycopy(reserved, 0, data, offset + 4, getRecordSize() - 4);

        return getRecordSize();
    }

    /**
     * Size of record
     */
    public int getRecordSize()
    {
        return 4 + reserved.length;
    }

    /**
     * @return id of this record.
     */
    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        NoteStructureSubRecord rec = new NoteStructureSubRecord();
        byte[] recdata = new byte[reserved.length];
        System.arraycopy(reserved, 0, recdata, 0, recdata.length);
        rec.reserved = recdata;
        return rec;
    }

}


