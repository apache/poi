package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.BinaryTree;
import org.apache.poi.util.HexDump;

import java.io.IOException;

class SSTDeserializer
{

    private BinaryTree strings;
    /** this is the number of characters we expect in the first sub-record in a subsequent continuation record */
    private int continuationExpectedChars;
    /** this is the string we were working on before hitting the end of the current record. This string is NOT finished. */
    private String unfinishedString;
    /** this is the total length of the current string being handled */
    private int totalLengthBytes;
    /** this is the offset into a string field of the actual string data */
    private int stringDataOffset;
    /** this is true if the string uses wide characters */
    private boolean wideChar;


    public SSTDeserializer(BinaryTree strings)
    {
        this.strings = strings;
        setExpectedChars( 0 );
        unfinishedString = "";
        totalLengthBytes = 0;
        stringDataOffset = 0;
        wideChar = false;
    }

    /**
     * This is the starting point where strings are constructed.  Note that
     * strings may span across multiple continuations. Read the SST record
     * carefully before beginning to hack.
     */
    public void manufactureStrings( final byte[] data, final int index,
                                     short size )
    {
        int offset = index;

        while ( offset < size )
        {
            int remaining = size - offset;

            if ( ( remaining > 0 ) && ( remaining < LittleEndianConsts.SHORT_SIZE ) )
            {
                throw new RecordFormatException( "Cannot get length of the last string in SSTRecord" );
            }
            if ( remaining == LittleEndianConsts.SHORT_SIZE )
            {
                setExpectedChars( LittleEndian.getUShort( data, offset ) );
                unfinishedString = "";
                break;
            }
            short charCount = LittleEndian.getShort( data, offset );

            setupStringParameters( data, offset, charCount );
            if ( remaining < totalLengthBytes )
            {
                setExpectedChars( calculateCharCount( totalLengthBytes - remaining ) );
                charCount -= getExpectedChars();
                totalLengthBytes = remaining;
            }
            else
            {
                setExpectedChars( 0 );
            }
            processString( data, offset, charCount );
            offset += totalLengthBytes;
            if ( getExpectedChars() != 0 )
            {
                break;
            }
        }
    }


    /**
     * Detemines the option types for the string (ie, compressed or uncompressed unicode, rich text string or
     * plain string etc) and calculates the length and offset for the string.
     *
     * @param data
     * @param index
     * @param char_count
     */
    private void setupStringParameters( final byte[] data, final int index,
                                        final int char_count )
    {
        byte optionFlag = data[index + LittleEndianConsts.SHORT_SIZE];

        wideChar = ( optionFlag & 1 ) == 1;
        boolean farEast = ( optionFlag & 4 ) == 4;
        boolean richText = ( optionFlag & 8 ) == 8;

        totalLengthBytes = SSTRecord.STRING_MINIMAL_OVERHEAD + calculateByteCount( char_count );
        stringDataOffset = SSTRecord.STRING_MINIMAL_OVERHEAD;
        if ( richText )
        {
            short run_count = LittleEndian.getShort( data, index + stringDataOffset );

            stringDataOffset += LittleEndianConsts.SHORT_SIZE;
            totalLengthBytes += LittleEndianConsts.SHORT_SIZE + ( LittleEndianConsts.INT_SIZE * run_count );
        }
        if ( farEast )
        {
            int extension_length = LittleEndian.getInt( data, index + stringDataOffset );

            stringDataOffset += LittleEndianConsts.INT_SIZE;
            totalLengthBytes += LittleEndianConsts.INT_SIZE + extension_length;
        }
    }


    private void processString( final byte[] data, final int index,
                                final short char_count )
    {
        byte[] stringDataBuffer = new byte[totalLengthBytes];
        int length = SSTRecord.STRING_MINIMAL_OVERHEAD + calculateByteCount( char_count );
        byte[] bstring = new byte[length];

        System.arraycopy( data, index, stringDataBuffer, 0, stringDataBuffer.length );
        int offset = 0;

        LittleEndian.putShort( bstring, offset, char_count );
        offset += LittleEndianConsts.SHORT_SIZE;
        bstring[offset] = stringDataBuffer[offset];

//        System.out.println( "offset = " + stringDataOffset );
//        System.out.println( "length = " + (bstring.length - STRING_MINIMAL_OVERHEAD) );
//        System.out.println( "src.length = " + str_data.length );
//        try
//        {
//            System.out.println( "----------------------- DUMP -------------------------" );
//            HexDump.dump( stringDataBuffer, (long)stringDataOffset, System.out, 1);
//        }
//        catch ( IOException e )
//        {
//        }
//        catch ( ArrayIndexOutOfBoundsException e )
//        {
//        }
//        catch ( IllegalArgumentException e )
//        {
//        }
        System.arraycopy( stringDataBuffer, stringDataOffset, bstring,
                SSTRecord.STRING_MINIMAL_OVERHEAD,
                bstring.length - SSTRecord.STRING_MINIMAL_OVERHEAD );
        UnicodeString string = new UnicodeString( UnicodeString.sid,
                (short) bstring.length,
                bstring );

        if ( getExpectedChars() != 0 )
        {
            unfinishedString = string.getString();
        }
        else
        {
            Integer integer = new Integer( strings.size() );
            addToStringTable( strings, integer, string );
        }
    }

    /**
     * Okay, we are doing some major cheating here. Because we can't handle rich text strings properly
     * we end up getting duplicate strings.  To get around this I'm doing do things: 1. Converting rich
     * text to normal text and 2. If there's a duplicate I'm adding a space onto the end.  Sneaky perhaps
     * but it gets the job done until we can handle this a little better.
     */
    static public void addToStringTable( BinaryTree strings, Integer integer, UnicodeString string )
    {
        if (string.isRichText())
            string.setOptionFlags( (byte)(string.getOptionFlags() & (~8) ) );

        boolean added = false;
        while (added == false)
        {
            try
            {
                strings.put( integer, string );
                added = true;
            }
            catch( Exception ignore )
            {
                string.setString( string.getString() + " " );
            }
        }
    }



    private int calculateCharCount( final int byte_count )
    {
        return byte_count / ( wideChar ? LittleEndianConsts.SHORT_SIZE
                : LittleEndianConsts.BYTE_SIZE );
    }

    /**
     * Process a Continue record. A Continue record for an SST record
     * contains the same kind of data that the SST record contains,
     * with the following exceptions:
     * <P>
     * <OL>
     * <LI>The string counts at the beginning of the SST record are
     *     not in the Continue record
     * <LI>The first string in the Continue record might NOT begin
     *     with a size. If the last string in the previous record is
     *     continued in this record, the size is determined by that
     *     last string in the previous record; the first string will
     *     begin with a flag byte, followed by the remaining bytes (or
     *     words) of the last string from the previous
     *     record. Otherwise, the first string in the record will
     *     begin with a string length
     * </OL>
     *
     * @param record the Continue record's byte data
     */

    public void processContinueRecord( final byte[] record )
    {
        if ( getExpectedChars() == 0 )
        {
            unfinishedString = "";
            totalLengthBytes = 0;
            stringDataOffset = 0;
            wideChar = false;
            manufactureStrings( record, 0, (short) record.length );
        }
        else
        {
            int data_length = record.length - LittleEndianConsts.BYTE_SIZE;

            if ( calculateByteCount( getExpectedChars() ) > data_length )
            {

                // create artificial data to create a UnicodeString
                byte[] input =
                        new byte[record.length + LittleEndianConsts.SHORT_SIZE];
                short size = (short) ( ( ( record[0] & 1 ) == 1 )
                        ? ( data_length / LittleEndianConsts.SHORT_SIZE )
                        : ( data_length / LittleEndianConsts.BYTE_SIZE ) );

                LittleEndian.putShort( input, (byte) 0, size );
                System.arraycopy( record, 0, input, LittleEndianConsts.SHORT_SIZE, record.length );
                UnicodeString ucs = new UnicodeString( UnicodeString.sid, (short) input.length, input );

                unfinishedString = unfinishedString + ucs.getString();
                setExpectedChars( getExpectedChars() - size );
            }
            else
            {
                setupStringParameters( record, -LittleEndianConsts.SHORT_SIZE,
                        getExpectedChars() );
                byte[] str_data = new byte[totalLengthBytes];
                int length = SSTRecord.STRING_MINIMAL_OVERHEAD
                        + ( calculateByteCount( getExpectedChars() ) );
                byte[] bstring = new byte[length];

                // Copy data from the record into the string
                // buffer. Copy skips the length of a short in the
                // string buffer, to leave room for the string length.
                System.arraycopy( record, 0, str_data,
                        LittleEndianConsts.SHORT_SIZE,
                        str_data.length
                        - LittleEndianConsts.SHORT_SIZE );

                // write the string length
                LittleEndian.putShort( bstring, 0,
                        (short) getExpectedChars() );

                // write the options flag
                bstring[LittleEndianConsts.SHORT_SIZE] =
                        str_data[LittleEndianConsts.SHORT_SIZE];

                // copy the bytes/words making up the string; skipping
                // past all the overhead of the str_data array
                System.arraycopy( str_data, stringDataOffset, bstring,
                        SSTRecord.STRING_MINIMAL_OVERHEAD,
                        bstring.length - SSTRecord.STRING_MINIMAL_OVERHEAD );

                // use special constructor to create the final string
                UnicodeString string =
                        new UnicodeString( UnicodeString.sid,
                                (short) bstring.length, bstring,
                                unfinishedString );
                Integer integer = new Integer( strings.size() );

//                field_3_strings.put( integer, string );
                addToStringTable( strings, integer, string );
                manufactureStrings( record, totalLengthBytes - LittleEndianConsts.SHORT_SIZE, (short) record.length );
            }
        }
    }

    /**
     * @return the number of characters we expect in the first
     *         sub-record in a subsequent continuation record
     */

    int getExpectedChars()
    {
        return continuationExpectedChars;
    }

    private void setExpectedChars( final int count )
    {
        continuationExpectedChars = count;
    }

    private int calculateByteCount( final int character_count )
    {
        return character_count * ( wideChar ? LittleEndianConsts.SHORT_SIZE : LittleEndianConsts.BYTE_SIZE );
    }


    /**
     * @return the unfinished string
     */

    String getUnfinishedString()
    {
        return unfinishedString;
    }

    /**
     * @return the total length of the current string
     */

    int getTotalLength()
    {
        return totalLengthBytes;
    }

    /**
     * @return offset into current string data
     */

    int getStringDataOffset()
    {
        return stringDataOffset;
    }

    /**
     * @return true if current string uses wide characters
     */

    boolean isWideChar()
    {
        return wideChar;
    }


}
