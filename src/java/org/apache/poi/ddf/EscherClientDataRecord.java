
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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * The EscherClientDataRecord is used to store client specific data about the position of a
 * shape within a container.
 */
public class EscherClientDataRecord
    extends EscherRecord
{
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    public static final short RECORD_ID = (short) 0xF011;
    public static final String RECORD_DESCRIPTION = "MsofbtClientData";

    private byte[] remainingData;

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        remainingData = IOUtils.safelyAllocate(bytesRemaining, MAX_RECORD_LENGTH);
        System.arraycopy( data, pos, remainingData, 0, bytesRemaining );
        return 8 + bytesRemaining;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (remainingData == null) {
            remainingData = new byte[0];
        }
        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        LittleEndian.putInt( data, offset + 4, remainingData.length );
        System.arraycopy( remainingData, 0, data, offset + 8, remainingData.length );
        int pos = offset + 8 + remainingData.length;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    @Override
    public int getRecordSize()
    {
        return 8 + (remainingData == null ? 0 : remainingData.length);
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return "ClientData";
    }

    /**
     * Any data recording this record.
     * 
     * @return the remaining bytes
     */
    public byte[] getRemainingData()
    {
        return remainingData;
    }

    /**
     * Any data recording this record.
     * 
     * @param remainingData the remaining bytes
     */
    public void setRemainingData( byte[] remainingData ) {
        this.remainingData = (remainingData == null)
            ? new byte[0]
            : remainingData.clone();
    }

    @Override
    protected Object[][] getAttributeMap() {
        return new Object[][] {
            { "Extra Data", getRemainingData() }
        };
    }
}
