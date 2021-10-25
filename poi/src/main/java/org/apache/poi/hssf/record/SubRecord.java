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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * Subrecords are part of the OBJ class.
 */
public abstract class SubRecord implements Duplicatable, GenericRecord {

    public enum SubRecordTypes {
        UNKNOWN(-1, UnknownSubRecord::new),
        END(0x0000, EndSubRecord::new),
        GROUP_MARKER(0x0006, GroupMarkerSubRecord::new),
        FT_CF(0x0007, FtCfSubRecord::new),
        FT_PIO_GRBIT(0x0008, FtPioGrbitSubRecord::new),
        EMBEDDED_OBJECT_REF(0x0009, EmbeddedObjectRefSubRecord::new),
        FT_CBLS(0x000C, FtCblsSubRecord::new),
        NOTE_STRUCTURE(0x000D, NoteStructureSubRecord::new),
        LBS_DATA(0x0013, LbsDataSubRecord::new),
        COMMON_OBJECT_DATA(0x0015, CommonObjectDataSubRecord::new),
        ;

        @FunctionalInterface
        public interface RecordConstructor<T extends SubRecord> {
            /**
             * read a sub-record from the supplied stream
             *
             * @param in    the stream to read from
             * @param cmoOt the objectType field of the containing CommonObjectDataSubRecord,
             *   we need it to propagate to next sub-records as it defines what data follows
             * @return the created sub-record
             */
            T apply(LittleEndianInput in, int size, int cmoOt);
        }

        private static final Map<Short,SubRecordTypes> LOOKUP =
            Arrays.stream(values()).collect(Collectors.toMap(SubRecordTypes::getSid, Function.identity()));

        public final short sid;
        public final RecordConstructor<?> recordConstructor;

        SubRecordTypes(int sid, RecordConstructor<?> recordConstructor) {
            this.sid = (short)sid;
            this.recordConstructor = recordConstructor;
        }

        public static SubRecordTypes forSID(int sid) {
            return LOOKUP.getOrDefault((short)sid, UNKNOWN);
        }

        public short getSid() {
            return sid;
        }
    }


    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for SubRecord
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for SubRecord
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    protected SubRecord() {}

    protected SubRecord(SubRecord other) {}

    /**
     * read a sub-record from the supplied stream
     *
     * @param in    the stream to read from
     * @param cmoOt the objectType field of the containing CommonObjectDataSubRecord,
     *   we need it to propagate to next sub-records as it defines what data follows
     * @return the created sub-record
     */
    public static SubRecord createSubRecord(LittleEndianInput in, int cmoOt) {
        int sid = in.readUShort();
        // Often (but not always) the datasize for the sub-record
        int size = in.readUShort();
        SubRecordTypes srt = SubRecordTypes.forSID(sid);
        return srt.recordConstructor.apply(in, size, srt == SubRecordTypes.UNKNOWN ? sid : cmoOt);
    }

    @Override
    public final String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    /**
     * @return the size of the data for this record (which is always 4 bytes less than the total
     * record size).  Note however, that ushort encoded after the record sid is usually but not
     * always the data size.
     */
    protected abstract int getDataSize();
    public byte[] serialize() {
        int size = getDataSize() + 4;
        UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(size);
        serialize(new LittleEndianOutputStream(baos));
        if (baos.size() != size) {
            throw new RuntimeException("write size mismatch");
        }
        return baos.toByteArray();
    }

    public abstract void serialize(LittleEndianOutput out);


    /**
     * Whether this record terminates the sub-record stream.
     * There are two cases when this method must be overridden and return {@code true}
     *  - EndSubRecord (sid = 0x00)
     *  - LbsDataSubRecord (sid = 0x12)
     *
     * @return whether this record is the last in the sub-record stream
     */
    public boolean isTerminating(){
        return false;
    }

    private static final class UnknownSubRecord extends SubRecord {

        private final int _sid;
        private final byte[] _data;

        public UnknownSubRecord(LittleEndianInput in, int size, int sid) {
            _sid = sid;
            byte[] buf = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
            in.readFully(buf);
            _data = buf;
        }
        @Override
        protected int getDataSize() {
            return _data.length;
        }
        @Override
        public void serialize(LittleEndianOutput out) {
            out.writeShort(_sid);
            out.writeShort(_data.length);
            out.write(_data);
        }

        @Override
        public UnknownSubRecord copy() {
            return this;
        }

        @Override
        public SubRecordTypes getGenericRecordType() {
            return SubRecordTypes.UNKNOWN;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "sid", () -> _sid,
                "data", () -> _data
            );
        }
    }

    @Override
    public abstract SubRecord copy();

    @Override
    public abstract SubRecordTypes getGenericRecordType();
}
