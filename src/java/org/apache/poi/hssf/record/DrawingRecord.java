/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

public class DrawingRecord extends Record
{
    public static final short sid = 0xEC;

    private byte[] recordData;

    public DrawingRecord()
    {
    }

    public DrawingRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public DrawingRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a MSODRAWING record");
        }
    }

    protected void fillFields( byte[] data, short size, int offset )
    {
        if (offset == 0 && size == data.length)
        {
            recordData = data;
        }
        else
        {
            recordData = new byte[size];
            System.arraycopy(data, offset, recordData, 0, size);
        }
    }

    protected void fillFields( byte[] data, short size )
    {
        recordData = data;
    }

    public int serialize( int offset, byte[] data )
    {
        if (recordData == null)
        {
            recordData = new byte[ 0 ];
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (recordData.length));
        if (recordData.length > 0)
        {
            System.arraycopy(recordData, 0, data, 4 + offset, recordData.length);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval = 4;

        if (recordData != null)
        {
            retval += recordData.length;
        }
        return retval;
    }

    public short getSid()
    {
        return sid;
    }

    public byte[] getData()
    {
        return recordData;
    }

    public void setData( byte[] thedata )
    {
        this.recordData = thedata;
    }

}
