
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

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

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndian;

/**
 * Process a single record.  That is, an SST record or a continue record.
 * Refactored from code originally in SSTRecord.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class RecordProcessor
{
    private byte[] data;
    private int recordOffset;
    private int available;
    private SSTRecordHeader sstRecordHeader;

    public RecordProcessor( byte[] data, int available, int numStrings, int numUniqueStrings )
    {
        this.data = data;
        this.available = available;
        this.sstRecordHeader = new SSTRecordHeader(numStrings, numUniqueStrings);
    }

    public int getAvailable()
    {
        return available;
    }

    public void writeRecordHeader( int offset, int totalWritten, int recordLength, boolean first_record )
    {
        if ( first_record )
        {
            available -= 8;
            recordOffset = sstRecordHeader.writeSSTHeader( data, recordOffset + offset + totalWritten, recordLength );
        }
        else
        {
            recordOffset = writeContinueHeader( data, recordOffset + offset + totalWritten, recordLength );
        }
    }

    public byte[] writeStringRemainder( boolean lastStringCompleted, byte[] stringreminant, int offset, int totalWritten )
    {
        if ( !lastStringCompleted )
        {
            // write reminant -- it'll all fit neatly
            System.arraycopy( stringreminant, 0, data, recordOffset + offset + totalWritten, stringreminant.length );
            adjustPointers( stringreminant.length );
        }
        else
        {
            // write as much of the remnant as possible
            System.arraycopy( stringreminant, 0, data, recordOffset + offset + totalWritten, available );
            byte[] leftover = new byte[( stringreminant.length - available ) + LittleEndianConsts.BYTE_SIZE];

            System.arraycopy( stringreminant, available, leftover, LittleEndianConsts.BYTE_SIZE, stringreminant.length - available );
            leftover[0] = stringreminant[0];
            stringreminant = leftover;
            adjustPointers( available );    // Consume all available remaining space
        }
        return stringreminant;
    }

    public void writeWholeString( UnicodeString unistr, int offset, int totalWritten )
    {
        unistr.serialize( recordOffset + offset + totalWritten, data );
        int rsize = unistr.getRecordSize();
        adjustPointers( rsize );
    }

    public byte[] writePartString( UnicodeString unistr, int offset, int totalWritten )
    {
        byte[] stringReminant;
        byte[] ucs = unistr.serialize();

        System.arraycopy( ucs, 0, data, recordOffset + offset + totalWritten, available );
        stringReminant = new byte[( ucs.length - available ) + LittleEndianConsts.BYTE_SIZE];
        System.arraycopy( ucs, available, stringReminant, LittleEndianConsts.BYTE_SIZE, ucs.length - available );
        stringReminant[0] = ucs[LittleEndianConsts.SHORT_SIZE];
        available = 0;
        return stringReminant;
    }


    private int writeContinueHeader( final byte[] data, final int pos,
                                     final int recsize )
    {
        int offset = pos;

        LittleEndian.putShort( data, offset, ContinueRecord.sid );
        offset += LittleEndianConsts.SHORT_SIZE;
        LittleEndian.putShort( data, offset, (short) ( recsize ) );
        offset += LittleEndianConsts.SHORT_SIZE;
        return offset - pos;
    }


    private void adjustPointers( int amount )
    {
        recordOffset += amount;
        available -= amount;
    }

    public int getRecordOffset()
    {
        return recordOffset;
    }
}

