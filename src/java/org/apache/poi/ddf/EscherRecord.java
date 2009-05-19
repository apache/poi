
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

import org.apache.poi.util.LittleEndian;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * The base abstract record from which all escher records are defined.  Subclasses will need
 * to define methods for serialization/deserialization and for determining the record size.
 *
 * @author Glen Stampoultzis
 */
public abstract class EscherRecord {
    private short _options;
    private short _recordId;

    /**
     * Create a new instance
     */
    public EscherRecord() {
        // fields uninitialised
    }

    /**
     * Delegates to fillFields(byte[], int, EscherRecordFactory)
     *
     * @see #fillFields(byte[], int, org.apache.poi.ddf.EscherRecordFactory)
     */
    protected int fillFields( byte[] data, EscherRecordFactory f )
    {
        return fillFields( data, 0, f );
    }

    /**
     * The contract of this method is to deserialize an escher record including
     * it's children.
     *
     * @param data      The byte array containing the serialized escher
     *                  records.
     * @param offset    The offset into the byte array.
     * @param recordFactory     A factory for creating new escher records.
     * @return          The number of bytes written.
     */
    public abstract int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory );

    /**
     * Reads the 8 byte header information and populates the <code>options</code>
     * and <code>recordId</code> records.
     *
     * @param data      the byte array to read from
     * @param offset    the offset to start reading from
     * @return          the number of bytes remaining in this record.  This
     *                  may include the children if this is a container.
     */
    protected int readHeader( byte[] data, int offset ) {
        EscherRecordHeader header = EscherRecordHeader.readHeader(data, offset);
        _options = header.getOptions();
        _recordId = header.getRecordId();
        return header.getRemainingBytes();
    }

    /**
     * Determine whether this is a container record by inspecting the option
     * field.
     * @return  true is this is a container field.
     */
    public boolean isContainerRecord() {
        return (_options & (short)0x000f) == (short)0x000f;
    }

    /**
     * @return The options field for this record.  All records have one.
     */
    public short getOptions() {
        return _options;
    }

    /**
     * Set the options this this record.  Container records should have the
     * last nibble set to 0xF.
     */
    public void setOptions( short options ) {
        _options = options;
    }

    /**
     * Serializes to a new byte array.  This is done by delegating to
     * serialize(int, byte[]);
     *
     * @return  the serialized record.
     * @see #serialize(int, byte[])
     */
    public byte[] serialize()
    {
        byte[] retval = new byte[getRecordSize()];

        serialize( 0, retval );
        return retval;
    }

    /**
     * Serializes to an existing byte array without serialization listener.
     * This is done by delegating to serialize(int, byte[], EscherSerializationListener).
     *
     * @param offset    the offset within the data byte array.
     * @param data      the data array to serialize to.
     * @return          The number of bytes written.
     *
     * @see #serialize(int, byte[], org.apache.poi.ddf.EscherSerializationListener)
     */
    public int serialize( int offset, byte[] data)
    {
        return serialize( offset, data, new NullEscherSerializationListener() );
    }

    /**
     * Serializes the record to an existing byte array.
     *
     * @param offset    the offset within the byte array
     * @param data      the data array to serialize to
     * @param listener  a listener for begin and end serialization events.  This
     *                  is useful because the serialization is
     *                  hierarchical/recursive and sometimes you need to be able
     *                  break into that.
     * @return the number of bytes written.
     */
    public abstract int serialize( int offset, byte[] data, EscherSerializationListener listener );

    /**
     * Subclasses should effeciently return the number of bytes required to
     * serialize the record.
     *
     * @return  number of bytes
     */
    abstract public int getRecordSize();

    /**
     * Return the current record id.
     *
     * @return  The 16 bit record id.
     */
    public short getRecordId() {
        return _recordId;
    }

    /**
     * Sets the record id for this record.
     */
    public void setRecordId( short recordId ) {
        _recordId = recordId;
    }

    /**
     * @return  Returns the children of this record.  By default this will
     *          be an empty list.  EscherCotainerRecord is the only record
     *          that may contain children.
     *
     * @see EscherContainerRecord
     */
    public List<EscherRecord> getChildRecords() { return Collections.emptyList(); }

    /**
     * Sets the child records for this record.  By default this will throw
     * an exception as only EscherContainerRecords may have children.
     *
     * @param childRecords  Not used in base implementation.
     */
    public void setChildRecords(List<EscherRecord> childRecords) {
        throw new UnsupportedOperationException("This record does not support child records.");
    }

    /**
     * Escher records may need to be clonable in the future.
     */
    public Object clone()
    {
        throw new RuntimeException( "The class " + getClass().getName() + " needs to define a clone method" );
    }

    /**
     * Returns the indexed child record.
     */
    public EscherRecord getChild( int index ) {
        return getChildRecords().get(index);
    }

    /**
     * The display methods allows escher variables to print the record names
     * according to their hierarchy.
     *
     * @param w         The print writer to output to.
     * @param indent    The current indent level.
     */
    public void display(PrintWriter w, int indent)
    {
        for (int i = 0; i < indent * 4; i++) w.print(' ');
        w.println(getRecordName());
    }

    /**
     * Subclasses should return the short name for this escher record.
     */
    public abstract String getRecordName();

    /**
     * Returns the instance part of the option record.
     *
     * @return The instance part of the record
     */
    public short getInstance() {
        return (short) ( _options >> 4 );
    }

    /**
     * This class reads the standard escher header.
     */
    static class EscherRecordHeader
    {
        private short options;
        private short recordId;
        private int remainingBytes;

        private EscherRecordHeader() {
            // fields uninitialised
        }

        public static EscherRecordHeader readHeader( byte[] data, int offset )
        {
            EscherRecordHeader header = new EscherRecordHeader();
            header.options = LittleEndian.getShort(data, offset);
            header.recordId = LittleEndian.getShort(data, offset + 2);
            header.remainingBytes = LittleEndian.getInt( data, offset + 4 );
            return header;
        }


        public short getOptions()
        {
            return options;
        }

        public short getRecordId()
        {
            return recordId;
        }

        public int getRemainingBytes()
        {
            return remainingBytes;
        }

        public String toString()
        {
            return "EscherRecordHeader{" +
                    "options=" + options +
                    ", recordId=" + recordId +
                    ", remainingBytes=" + remainingBytes +
                    "}";
        }
    }
}
