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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.ddf.NullEscherSerializationListener;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Removal;

/**
 * Specifies a group of drawing objects.
 * <p>
 * Contains a single {@link EscherRecordTypes#DGG_CONTAINER OfficeArtDggContainer} that specifies the group of drawing
 * objects. Get the {@link org.apache.poi.ddf.EscherContainerRecord} representation via {@link #getEscherContainer()}.
 * <p>
 * Referred to as an {@code MsoDrawingGroup} in {@code [MS-XLS].pdf v20190618}.
 */
public final class DrawingGroupRecord extends AbstractEscherHolderRecord {
    public static final short sid = 0xEB;

    private static final int DEFAULT_MAX_RECORD_SIZE = 8228;
    private static int MAX_RECORD_SIZE = DEFAULT_MAX_RECORD_SIZE;

    /**
     * @param size the max record size allowed for DrawingGroupRecord
     */
    public static void setMaxRecordSize(int size) {
        MAX_RECORD_SIZE = size;
    }

    /**
     * @return the max record size allowed for DrawingGroupRecord
     */
    public static int getMaxRecordSize() {
        return MAX_RECORD_SIZE;
    }

    private static int getMaxDataSize() {
        return MAX_RECORD_SIZE - 4;
    }

    public DrawingGroupRecord() {}

    public DrawingGroupRecord(DrawingGroupRecord other) {
        super(other);
    }

    public DrawingGroupRecord( RecordInputStream in ) {
        super( in );
    }

    protected String getRecordName()
    {
        return "MSODRAWINGGROUP";
    }

    public short getSid()
    {
        return sid;
    }

    public int serialize(int offset, byte[] data)
    {
        byte[] rawData = getRawData();
        if (getEscherRecords().isEmpty() && rawData != null)
        {
            return writeData( offset, data, rawData );
        }
        byte[] buffer = new byte[getRawDataSize()];
        int pos = 0;
        for (EscherRecord r : getEscherRecords()) {
            pos += r.serialize(pos, buffer, new NullEscherSerializationListener());
        }

        return writeData( offset, data, buffer );
    }

    /**
     * Process the bytes into escher records.
     * (Not done by default in case we break things,
     *  unless you set the "poi.deserialize.escher"
     *  system property)
     *
     * @deprecated Call {@link #decode()} instead.
     */
    @Removal(version = "5.3")
    @Deprecated
    public void processChildRecords() {
        decode();
    }

    public int getRecordSize() {
        // TODO - convert this to a RecordAggregate
        return grossSizeFromDataSize(getRawDataSize());
    }

    private int getRawDataSize() {
        List<EscherRecord> escherRecords = getEscherRecords();
        byte[] rawData = getRawData();
        if (escherRecords.isEmpty() && rawData != null) {
            return rawData.length;
        }
        int size = 0;
        for (EscherRecord r : escherRecords) {
            size += r.getRecordSize();
        }
        return size;
    }

    static int grossSizeFromDataSize(int dataSize)
    {
        return dataSize + ( (dataSize - 1) / getMaxDataSize() + 1 ) * 4;
    }

    private int writeData( int offset, byte[] data, byte[] rawData )
    {
        int writtenActualData = 0;
        int writtenRawData = 0;
        while (writtenRawData < rawData.length)
        {
            final int maxDataSize = getMaxDataSize();
            int segmentLength = Math.min( rawData.length - writtenRawData, maxDataSize);
            if (writtenRawData / maxDataSize >= 2)
                writeContinueHeader( data, offset, segmentLength );
            else
                writeHeader( data, offset, segmentLength );
            writtenActualData += 4;
            offset += 4;
            System.arraycopy( rawData, writtenRawData, data, offset, segmentLength );
            offset += segmentLength;
            writtenRawData += segmentLength;
            writtenActualData += segmentLength;
        }
        return writtenActualData;
    }

    private void writeHeader( byte[] data, int offset, int sizeExcludingHeader )
    {
        LittleEndian.putShort(data, offset, getSid());
        LittleEndian.putShort(data, offset + 2, (short) sizeExcludingHeader);
    }

    private void writeContinueHeader( byte[] data, int offset, int sizeExcludingHeader )
    {
        LittleEndian.putShort(data, offset, ContinueRecord.sid);
        LittleEndian.putShort(data, offset + 2, (short) sizeExcludingHeader);
    }

    @Override
    public DrawingGroupRecord copy() {
        return new DrawingGroupRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DRAWING_GROUP;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
