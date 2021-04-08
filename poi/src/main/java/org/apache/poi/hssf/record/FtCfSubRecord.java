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

import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;


/**
 * The FtCf structure specifies the clipboard format of the picture-type Obj record containing this FtCf.
 */
public final class FtCfSubRecord extends SubRecord {
    public static final short sid = 0x07;
    public static final short length = 0x02;

    /**
     * Specifies the format of the picture is an enhanced metafile.
     */
    public static final short METAFILE_BIT    = (short)0x0002;

    /**
     * Specifies the format of the picture is a bitmap.
     */
    public static final short BITMAP_BIT      = (short)0x0009;

    /**
     * Specifies the picture is in an unspecified format that is
     * neither and enhanced metafile nor a bitmap.
     */
    public static final short UNSPECIFIED_BIT = (short)0xFFFF;

    private short flags;

    /**
     * Construct a new <code>FtPioGrbitSubRecord</code> and
     * fill its data with the default values
     */
    public FtCfSubRecord() {}

    public FtCfSubRecord(FtCfSubRecord other) {
        super(other);
        flags = other.flags;
    }

    public FtCfSubRecord(LittleEndianInput in, int size) {
        this(in,size,-1);
    }

    FtCfSubRecord(LittleEndianInput in, int size, int cmoOt) {
        if (size != length) {
            throw new RecordFormatException("Unexpected size (" + size + ")");
        }
        flags = in.readShort();
    }

    /**
     * Serialize the record data into the supplied array of bytes
     *
     * @param out the stream to serialize into
     */
    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(length);
        out.writeShort(flags);
    }

 protected int getDataSize() {
        return length;
    }

    /**
     * @return id of this record.
     */
    public short getSid()
    {
        return sid;
    }

    @Override
    public FtCfSubRecord copy() {
        return new FtCfSubRecord(this);
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    @Override
    public SubRecordTypes getGenericRecordType() {
        return SubRecordTypes.FT_CF;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("flags", getEnumBitsAsString(this::getFlags,
            new int[]{METAFILE_BIT,BITMAP_BIT,UNSPECIFIED_BIT},
            new String[]{"METAFILE","BITMAP","UNSPECIFIED"}));
    }
}