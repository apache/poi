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

package org.apache.poi.ddf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * This record is used whenever a escher record is encountered that
 * we do not explicitly support.
 */
public final class UnknownEscherRecord extends EscherRecord {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the 8 byte header */
    private byte[] thedata = NO_BYTES;
    private final List<EscherRecord> _childRecords = new ArrayList<>();

    /**
     * @param length the max record length allowed for UnknownEscherRecord
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for UnknownEscherRecord
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public UnknownEscherRecord() {}

    public UnknownEscherRecord(UnknownEscherRecord other) {
        super(other);
        other._childRecords.stream().map(EscherRecord::copy).forEach(_childRecords::add);
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        /*
         * Have a check between available bytes and bytesRemaining,
         * take the available length if the bytesRemaining out of range.
         */
        int available = data.length - (offset + 8);
        if (bytesRemaining > available) {
            bytesRemaining = available;
        }

        if (isContainerRecord()) {
            int bytesWritten = 0;
            thedata = new byte[0];
            offset += 8;
            bytesWritten += 8;
            while ( bytesRemaining > 0 ) {
                EscherRecord child = recordFactory.createRecord( data, offset );
                int childBytesWritten = child.fillFields( data, offset, recordFactory );
                bytesWritten += childBytesWritten;
                offset += childBytesWritten;
                bytesRemaining -= childBytesWritten;
                getChildRecords().add( child );
            }
            return bytesWritten;
        }

        if (bytesRemaining < 0) {
            bytesRemaining = 0;
        }

        thedata = IOUtils.safelyClone(data, offset + 8, bytesRemaining, MAX_RECORD_LENGTH);

        return bytesRemaining + 8;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = thedata.length;
        for (EscherRecord r : _childRecords) {
            remainingBytes += r.getRecordSize();
        }
        LittleEndian.putInt(data, offset+4, remainingBytes);
        System.arraycopy(thedata, 0, data, offset+8, thedata.length);
        int pos = offset+8+thedata.length;
        for (EscherRecord r : _childRecords) {
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    /**
     * @return the data which makes up this record
     */
    public byte[] getData() {
        return thedata;
    }

    @Override
    public int getRecordSize() {
        return 8 + thedata.length;
    }

    @Override
    public List<EscherRecord> getChildRecords() {
        return _childRecords;
    }

    @Override
    public void setChildRecords(List<EscherRecord> childRecords) {
        if (childRecords == _childRecords) {
            return;
        }
        _childRecords.clear();
        _childRecords.addAll(childRecords);
    }

    @Override
    public String getRecordName() {
        return "Unknown 0x" + HexDump.toHex(getRecordId());
    }

    public void addChildRecord(EscherRecord childRecord) {
        getChildRecords().add( childRecord );
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "data", this::getData
        );
    }

    @Override
    public Enum getGenericRecordType() {
        return EscherRecordTypes.UNKNOWN;
    }

    @Override
    public UnknownEscherRecord copy() {
        return new UnknownEscherRecord(this);
    }
}
