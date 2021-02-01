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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;

/**
 * This structure appears as part of an Obj record that represents a checkbox or radio button.
 */
public final class FtCblsSubRecord extends SubRecord {
    public static final short sid = 0x0C;
    private static final int ENCODED_SIZE = 20;

    private final byte[] reserved;

    /**
     * Construct a new <code>FtCblsSubRecord</code> and
     * fill its data with the default values
     */
    public FtCblsSubRecord() {
        reserved = new byte[ENCODED_SIZE];
    }

    public FtCblsSubRecord(FtCblsSubRecord other) {
        super(other);
        reserved = other.reserved.clone();
    }

    public FtCblsSubRecord(LittleEndianInput in, int size) {
        this(in,size,-1);
    }

    FtCblsSubRecord(LittleEndianInput in, int size, int cmoOt) {
        if (size != ENCODED_SIZE) {
            throw new RecordFormatException("Unexpected size (" + size + ")");
        }
        //just grab the raw data
        byte[] buf = IOUtils.safelyAllocate(size, ENCODED_SIZE);
        in.readFully(buf);
        reserved = buf;
    }

    /**
     * Serialize the record data into the supplied array of bytes
     *
     * @param out the stream to serialize into
     */
    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(reserved.length);
        out.write(reserved);
    }

	protected int getDataSize() {
        return reserved.length;
    }

    /**
     * @return id of this record.
     */
    public short getSid()
    {
        return sid;
    }

    @Override
    public FtCblsSubRecord copy() {
        return new FtCblsSubRecord(this);
    }

    @Override
    public SubRecordTypes getGenericRecordType() {
        return SubRecordTypes.FT_CBLS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("reserved", () -> reserved);
    }
}