package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndian;

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
}

class SSTRecordHeader
{
    int numStrings;
    int numUniqueStrings;

    /**
     *
     */
    public SSTRecordHeader( int numStrings, int numUniqueStrings )
    {
        this.numStrings = numStrings;
        this.numUniqueStrings = numUniqueStrings;
    }

    /**
     * Writes out the SST record.  This consists of the sid, the record size, the number of
     * strings and the number of unique strings.
     *
     * @param data          The data buffer to write the header to.
     * @param bufferIndex   The index into the data buffer where the header should be written.
     * @param recSize       The number of records written.
     *
     * @return The bufer of bytes modified.
     */
    public int writeSSTHeader( byte[] data, int bufferIndex, int recSize )
    {
        int offset = bufferIndex;

        LittleEndian.putShort( data, offset, SSTRecord.sid );
        offset += LittleEndianConsts.SHORT_SIZE;
        LittleEndian.putShort( data, offset, (short) ( recSize ) );
        offset += LittleEndianConsts.SHORT_SIZE;
//        LittleEndian.putInt( data, offset, getNumStrings() );
        LittleEndian.putInt( data, offset, numStrings );
        offset += LittleEndianConsts.INT_SIZE;
//        LittleEndian.putInt( data, offset, getNumUniqueStrings() );
        LittleEndian.putInt( data, offset, numUniqueStrings );
        offset += LittleEndianConsts.INT_SIZE;
        return offset - bufferIndex;
    }

}