/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.ddf;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.io.ByteArrayOutputStream;

/**
 * The EscherClientDataRecord is used to store client specific data about the position of a
 * shape within a container.
 *
 * @author Glen Stampoultzis
 */
public class EscherClientDataRecord
    extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF011;
    public static final String RECORD_DESCRIPTION = "MsofbtClientData";

    private byte[] remainingData;

    /**
     * This method deserializes the record from a byte array.
     *
     * @param data          The byte array containing the escher record information
     * @param offset        The starting offset into <code>data</code>.
     * @param recordFactory May be null since this is not a container record.
     * @return The number of bytes read from the byte array.
     */
    public int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        remainingData  =  new byte[bytesRemaining];
        System.arraycopy( data, pos, remainingData, 0, bytesRemaining );
        return 8 + bytesRemaining;
    }

    /**
     * This method serializes this escher record into a byte array.
     *
     * @param offset   The offset into <code>data</code> to start writing the record data to.
     * @param data     The byte array to serialize to.
     * @param listener A listener to retrieve start and end callbacks.  Use a <code>NullEscherSerailizationListener</code> to ignore these events.
     * @return The number of bytes written.
     * @see NullEscherSerializationListener
     */
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (remainingData == null) remainingData = new byte[0];
        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        LittleEndian.putInt( data, offset + 4, remainingData.length );
        System.arraycopy( remainingData, 0, data, offset + 8, remainingData.length );
        int pos = offset + 8 + remainingData.length;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    public int getRecordSize()
    {
        return 8 + (remainingData == null ? 0 : remainingData.length);
    }

    /**
     * Returns the identifier of this record.
     */
    public short getRecordId()
    {
        return RECORD_ID;
    }

    /**
     * The short name for this record
     */
    public String getRecordName()
    {
        return "ClientData";
    }

    /**
     * Returns the string representation of this record.
     */
    public String toString()
    {
        String nl = System.getProperty("line.separator");

        String extraData;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try
        {
            HexDump.dump(this.remainingData, 0, b, 0);
            extraData = b.toString();
        }
        catch ( Exception e )
        {
            extraData = "error";
        }
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex(RECORD_ID) + nl +
                "  Options: 0x" + HexDump.toHex(getOptions()) + nl +
                "  Extra Data:" + nl +
                extraData;

    }

    /**
     * Any data recording this record.
     */
    public byte[] getRemainingData()
    {
        return remainingData;
    }

    /**
     * Any data recording this record.
     */
    public void setRemainingData( byte[] remainingData )
    {
        this.remainingData = remainingData;
    }
}
