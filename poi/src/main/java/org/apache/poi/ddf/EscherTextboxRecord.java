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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;

/**
 * Holds data from the parent application. Most commonly used to store
 *  text in the format of the parent application, rather than in
 *  Escher format. We don't attempt to understand the contents, since
 *  they will be in the parent's format, not Escher format.
 */
public final class EscherTextboxRecord extends EscherRecord {

    //arbitrarily selected; may need to increase
    private static int DEFAULT_MAX_RECORD_LENGTH = 100_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    public static final short RECORD_ID = EscherRecordTypes.CLIENT_TEXTBOX.typeID;

    private static final byte[] NO_BYTES = new byte[0];

    /** The data for this record not including the 8 byte header */
    private byte[] thedata = NO_BYTES;

    /**
     * @param length the max record length allowed for EscherTextboxRecord
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for EscherTextboxRecord
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public EscherTextboxRecord() {}

    public EscherTextboxRecord(EscherTextboxRecord other) {
        super(other);
        thedata = (other.thedata == null) ? NO_BYTES : other.thedata.clone();
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );

        // Save the data, ready for the calling code to do something useful with it
        thedata = IOUtils.safelyClone(data, offset + 8, bytesRemaining, MAX_RECORD_LENGTH);
        return bytesRemaining + 8;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = thedata.length;
        LittleEndian.putInt(data, offset+4, remainingBytes);
        System.arraycopy(thedata, 0, data, offset+8, thedata.length);
        int pos = offset+8+thedata.length;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        int size = pos - offset;
        if (size != getRecordSize()) {
            throw new RecordFormatException(size + " bytes written but getRecordSize() reports " + getRecordSize());
        }
        return size;
    }

    /**
     * Returns any extra data associated with this record.  In practice excel
     * does not seem to put anything here, but with PowerPoint this will
     * contain the bytes that make up a TextHeaderAtom followed by a
     * TextBytesAtom/TextCharsAtom
     *
     * @return the extra data
     */
    public byte[] getData()
    {
        return thedata;
    }

    /**
     * Sets the extra data (in the parent application's format) to be
     * contained by the record. Used when the parent application changes
     * the contents.
     *
     * @param b the buffer which contains the data
     * @param start the start position in the buffer
     * @param length the length of the block
     */
    public void setData(byte[] b, int start, int length) {
        thedata = IOUtils.safelyClone(b, start, length, MAX_RECORD_LENGTH);
    }

    /**
     * Sets the extra data (in the parent application's format) to be
     * contained by the record. Used when the parent application changes
     * the contents.
     *
     * @param b the data
     */
    public void setData(byte[] b) {
        setData(b,0,b.length);
    }

    @Override
    public int getRecordSize()
    {
        return 8 + thedata.length;
    }

    @Override
    public String getRecordName() {
        return EscherRecordTypes.CLIENT_TEXTBOX.recordName;
    }

    @Override
    public Enum getGenericRecordType() {
        return EscherRecordTypes.CLIENT_TEXTBOX;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "isContainer", this::isContainerRecord,
            "extraData", this::getData
        );
    }

    @Override
    public EscherTextboxRecord copy() {
        return new EscherTextboxRecord(this);
    }
}
