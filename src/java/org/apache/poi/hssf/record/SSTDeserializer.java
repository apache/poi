
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

import org.apache.poi.util.BinaryTree;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Handles the task of deserializing a SST string.  The two main entry points are
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
class SSTDeserializer
{

    private BinaryTree strings;
    /** this is the number of characters that have been read prior to the continuation */
    private int continuationReadChars;
    /** this is the string we were working on before hitting the end of the current record. This string is NOT finished. */
    private String unfinishedString;
    /** this is true if the string uses wide characters */
    private boolean wideChar;
    /** this is true if the string is a rich text string */
    private boolean richText;
    /** this is true if the string is a far east string or some other wierd string */
    private boolean extendedText;
    /** Number of formatting runs in this rich text field */
    private short runCount;
    /** Number of characters in current string */
    private int charCount;
    private int extensionLength;
    private int continueSkipBytes = 0;


    public SSTDeserializer( BinaryTree strings )
    {
        this.strings = strings;
        initVars();
    }

    private void initVars()
    {
        runCount = 0;
        continuationReadChars = 0;
        unfinishedString = "";
//        bytesInCurrentSegment = 0;
//        stringDataOffset = 0;
        wideChar = false;
        richText = false;
        extendedText = false;
        continueSkipBytes = 0;
    }

    /**
     * This is the starting point where strings are constructed.  Note that
     * strings may span across multiple continuations. Read the SST record
     * carefully before beginning to hack.
     */
    public void manufactureStrings( final byte[] data, final int initialOffset)
    {
        initVars();

        int offset = initialOffset;
        final int dataSize = data.length;
        while ( offset < dataSize )
        {
            int remaining = dataSize - offset;

            if ( ( remaining > 0 ) && ( remaining < LittleEndianConsts.SHORT_SIZE ) )
            {
                throw new RecordFormatException( "Cannot get length of the last string in SSTRecord" );
            }
            if ( remaining == LittleEndianConsts.SHORT_SIZE )
            {
              //JMH Dont know about this
                setContinuationCharsRead( 0 );//LittleEndian.getUShort( data, offset ) );
                unfinishedString = "";
                break;
            }
            charCount = LittleEndian.getUShort( data, offset );
            int charsRead = charCount;
            readStringHeader( data, offset );
            boolean stringContinuesOverContinuation = remaining < totalStringSize();
            if ( stringContinuesOverContinuation )
            {
                int remainingBytes = dataSize - offset - stringHeaderOverhead();
                //Only read the size of the string or whatever is left before the
                //continuation
                charsRead = Math.min(charsRead, calculateCharCount( remainingBytes ));
                setContinuationCharsRead( charsRead );                
                if (charsRead == charCount) {
                  //Since all of the characters will have been read, but the entire string (including formatting runs etc)
                  //hasnt, Compute the number of bytes to skip when the continue record starts
                  continueSkipBytes = offsetForContinuedRecord(0) - (remainingBytes - calculateByteCount(charsRead));
                }
            }
            processString( data, offset, charsRead );
            offset += totalStringSize();
            if ( stringContinuesOverContinuation )
            {
                break;
            }
        }
    }

//    private void dump( final byte[] data, int offset, int length )
//    {
//        try
//        {
//            System.out.println( "------------------- SST DUMP -------------------------" );
//            HexDump.dump( (byte[]) data, offset, System.out, offset, length );
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
//    }

    /**
     * Detemines the option types for the string (ie, compressed or uncompressed unicode, rich text string or
     * plain string etc) and calculates the length and offset for the string.
     *
     */
    private void readStringHeader( final byte[] data, final int index )
    {

        byte optionFlag = data[index + LittleEndianConsts.SHORT_SIZE];

        wideChar = ( optionFlag & 1 ) == 1;
        extendedText = ( optionFlag & 4 ) == 4;
        richText = ( optionFlag & 8 ) == 8;
        runCount = 0;
        if ( richText )
        {
            runCount = LittleEndian.getShort( data, index + SSTRecord.STRING_MINIMAL_OVERHEAD );
        }
        extensionLength = 0;
        if ( extendedText )
        {
            extensionLength = LittleEndian.getInt( data, index + SSTRecord.STRING_MINIMAL_OVERHEAD
                    + (richText ? LittleEndianConsts.SHORT_SIZE : 0) );
        }

    }


    /**
     * Reads a string or the first part of a string.
     *
     * @param characters the number of characters to write.
     *
     * @return the number of bytes written.
     */
    private int processString( final byte[] data, final int dataIndex, final int characters )
    {

        // length is the length we store it as.  not the length that is read.
        int length = SSTRecord.STRING_MINIMAL_OVERHEAD + calculateByteCount( characters );
        byte[] unicodeStringBuffer = new byte[length];

        int offset = 0;

        // Set the length in characters
        LittleEndian.putUShort( unicodeStringBuffer, offset, characters );
        offset += LittleEndianConsts.SHORT_SIZE;
        // Set the option flags
        unicodeStringBuffer[offset] = data[dataIndex + offset];
        // Copy in the string data
        int bytesRead = unicodeStringBuffer.length - SSTRecord.STRING_MINIMAL_OVERHEAD;
        arraycopy( data, dataIndex + stringHeaderOverhead(), unicodeStringBuffer, SSTRecord.STRING_MINIMAL_OVERHEAD, bytesRead );
        // Create the unicode string
        UnicodeString string = new UnicodeString( UnicodeString.sid,
                (short) unicodeStringBuffer.length,
                unicodeStringBuffer );
        setContinuationCharsRead( calculateCharCount(bytesRead));

        if ( isStringFinished() )
        {
            Integer integer = new Integer( strings.size() );
            addToStringTable( strings, integer, string );
        }
        else
        {
            unfinishedString = string.getString();
        }

        return bytesRead;
    }

    private boolean isStringFinished()
    {
        return getContinuationCharsRead() == charCount;
    }

    /**
     * Okay, we are doing some major cheating here. Because we can't handle rich text strings properly
     * we end up getting duplicate strings.  To get around this I'm doing two things: 1. Converting rich
     * text to normal text and 2. If there's a duplicate I'm adding a space onto the end.  Sneaky perhaps
     * but it gets the job done until we can handle this a little better.
     */
    static public void addToStringTable( BinaryTree strings, Integer integer, UnicodeString string )
    {

        if ( string.isRichText() )
            string.setOptionFlags( (byte) ( string.getOptionFlags() & ( ~8 ) ) );
        if ( string.isExtendedText() )
            string.setOptionFlags( (byte) ( string.getOptionFlags() & ( ~4 ) ) );

        boolean added = false;
        while ( added == false )
        {
            try
            {
                strings.put( integer, string );
                added = true;
            }
            catch ( Exception ignore )
            {
                string.setString( string.getString() + " " );
            }
        }

    }


    private int calculateCharCount( final int byte_count )
    {
        return byte_count / ( wideChar ? LittleEndianConsts.SHORT_SIZE : LittleEndianConsts.BYTE_SIZE );
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
        if ( isStringFinished() )
        {
            final int offset = continueSkipBytes;
            initVars();
            manufactureStrings( record, offset);
        }
        else
        {
            // reset the wide bit because that can change across a continuation. the fact that it's
            // actually rich text doesn't change across continuations even though the rich text
            // may on longer be set in the "new" option flag.  confusing huh?
            wideChar = ( record[0] & 1 ) == 1;

            if ( stringSpansContinuation( record.length - LittleEndianConsts.BYTE_SIZE ) )
            {
                processEntireContinuation( record );
            }
            else
            {
                readStringRemainder( record );
            }
        }

    }

    /**
     * Reads the remainder string and any subsequent strings from the continuation record.
     *
     * @param record  The entire continuation record data.
     */
    private void readStringRemainder( final byte[] record )
    {
        int stringRemainderSizeInBytes = calculateByteCount( charCount-getContinuationCharsRead() );
        byte[] unicodeStringData = new byte[SSTRecord.STRING_MINIMAL_OVERHEAD
                + stringRemainderSizeInBytes];

        // write the string length
        LittleEndian.putShort( unicodeStringData, 0, (short) (charCount-getContinuationCharsRead()) );

        // write the options flag
        unicodeStringData[LittleEndianConsts.SHORT_SIZE] = createOptionByte( wideChar, richText, extendedText );

        // copy the bytes/words making up the string; skipping
        // past all the overhead of the str_data array
        arraycopy( record, LittleEndianConsts.BYTE_SIZE, unicodeStringData,
                SSTRecord.STRING_MINIMAL_OVERHEAD,
                stringRemainderSizeInBytes );

        // use special constructor to create the final string
        UnicodeString string = new UnicodeString( UnicodeString.sid,
                (short) unicodeStringData.length, unicodeStringData,
                unfinishedString );
        Integer integer = new Integer( strings.size() );

        addToStringTable( strings, integer, string );

        int newOffset = offsetForContinuedRecord( stringRemainderSizeInBytes );
        manufactureStrings( record, newOffset);
    }

    /**
     * Calculates the size of the string in bytes based on the character width
     */
    private int stringSizeInBytes()
    {
        return calculateByteCount( charCount );
    }

    /**
     * Calculates the size of the string in byes.  This figure includes all the over
     * heads for the string.
     */
    private int totalStringSize()
    {
        return stringSizeInBytes()
                + stringHeaderOverhead()
                + LittleEndianConsts.INT_SIZE * runCount
                + extensionLength;
    }

    private int stringHeaderOverhead()
    {
        return SSTRecord.STRING_MINIMAL_OVERHEAD
                + ( richText ? LittleEndianConsts.SHORT_SIZE : 0 )
                + ( extendedText ? LittleEndianConsts.INT_SIZE : 0 );
    }

    private int offsetForContinuedRecord( int stringRemainderSizeInBytes )
    {
        int offset = stringRemainderSizeInBytes + runCount * LittleEndianConsts.INT_SIZE + extensionLength;        
        if (stringRemainderSizeInBytes != 0)
          //If a portion of the string remains then the wideChar options byte is repeated,
          //so need to skip this.
          offset += + LittleEndianConsts.BYTE_SIZE;
        return offset;  
    }

    private byte createOptionByte( boolean wideChar, boolean richText, boolean farEast )
    {
        return (byte) ( ( wideChar ? 1 : 0 ) + ( farEast ? 4 : 0 ) + ( richText ? 8 : 0 ) );
    }

    /**
     * If the continued record is so long is spans into the next continue then
     * simply suck the remaining string data into the existing <code>unfinishedString</code>.
     *
     * @param record    The data from the continuation record.
     */
    private void processEntireContinuation( final byte[] record )
    {
        // create artificial data to create a UnicodeString
        int dataLengthInBytes = record.length - LittleEndianConsts.BYTE_SIZE;
        byte[] unicodeStringData = new byte[record.length + LittleEndianConsts.SHORT_SIZE];

        int charsRead = calculateCharCount( dataLengthInBytes );
        LittleEndian.putShort( unicodeStringData, (byte) 0, (short) charsRead );
        arraycopy( record, 0, unicodeStringData, LittleEndianConsts.SHORT_SIZE, record.length );
        UnicodeString ucs = new UnicodeString( UnicodeString.sid, (short) unicodeStringData.length, unicodeStringData, unfinishedString);

        unfinishedString = ucs.getString();
        setContinuationCharsRead( getContinuationCharsRead() + charsRead );
        if (getContinuationCharsRead() == charCount) {
          Integer integer = new Integer( strings.size() );
          addToStringTable( strings, integer, ucs );
        }
    }

    private boolean stringSpansContinuation( int continuationSizeInBytes )
    {
        return calculateByteCount( charCount - getContinuationCharsRead() ) > continuationSizeInBytes;
    }

    /**
     * @return the number of characters we expect in the first
     *         sub-record in a subsequent continuation record
     */

    int getContinuationCharsRead()
    {
        return continuationReadChars;
    }

    private void setContinuationCharsRead( final int count )
    {
        continuationReadChars = count;
    }

    private int calculateByteCount( final int character_count )
    {
        return character_count * ( wideChar ? LittleEndianConsts.SHORT_SIZE : LittleEndianConsts.BYTE_SIZE );
    }


    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * A subsequence of array components are copied from the source
     * array referenced by <code>src</code> to the destination array
     * referenced by <code>dst</code>. The number of components copied is
     * equal to the <code>length</code> argument. The components at
     * positions <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> in the source array are copied into
     * positions <code>dstOffset</code> through
     * <code>dstOffset+length-1</code>, respectively, of the destination
     * array.
     * <p>
     * If the <code>src</code> and <code>dst</code> arguments refer to the
     * same array object, then the copying is performed as if the
     * components at positions <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> were first copied to a temporary
     * array with <code>length</code> components and then the contents of
     * the temporary array were copied into positions
     * <code>dstOffset</code> through <code>dstOffset+length-1</code> of the
     * destination array.
     * <p>
     * If <code>dst</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>src</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown and the destination
     * array is not modified.
     * <p>
     * Otherwise, if any of the following is true, an
     * <code>ArrayStoreException</code> is thrown and the destination is
     * not modified:
     * <ul>
     * <li>The <code>src</code> argument refers to an object that is not an
     *     array.
     * <li>The <code>dst</code> argument refers to an object that is not an
     *     array.
     * <li>The <code>src</code> argument and <code>dst</code> argument refer to
     *     arrays whose component types are different primitive types.
     * <li>The <code>src</code> argument refers to an array with a primitive
     *     component type and the <code>dst</code> argument refers to an array
     *     with a reference component type.
     * <li>The <code>src</code> argument refers to an array with a reference
     *     component type and the <code>dst</code> argument refers to an array
     *     with a primitive component type.
     * </ul>
     * <p>
     * Otherwise, if any of the following is true, an
     * <code>IndexOutOfBoundsException</code> is
     * thrown and the destination is not modified:
     * <ul>
     * <li>The <code>srcOffset</code> argument is negative.
     * <li>The <code>dstOffset</code> argument is negative.
     * <li>The <code>length</code> argument is negative.
     * <li><code>srcOffset+length</code> is greater than
     *     <code>src.length</code>, the length of the source array.
     * <li><code>dstOffset+length</code> is greater than
     *     <code>dst.length</code>, the length of the destination array.
     * </ul>
     * <p>
     * Otherwise, if any actual component of the source array from
     * position <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> cannot be converted to the component
     * type of the destination array by assignment conversion, an
     * <code>ArrayStoreException</code> is thrown. In this case, let
     * <b><i>k</i></b> be the smallest nonnegative integer less than
     * length such that <code>src[srcOffset+</code><i>k</i><code>]</code>
     * cannot be converted to the component type of the destination
     * array; when the exception is thrown, source array components from
     * positions <code>srcOffset</code> through
     * <code>srcOffset+</code><i>k</i><code>-1</code>
     * will already have been copied to destination array positions
     * <code>dstOffset</code> through
     * <code>dstOffset+</code><i>k</I><code>-1</code> and no other
     * positions of the destination array will have been modified.
     * (Because of the restrictions already itemized, this
     * paragraph effectively applies only to the situation where both
     * arrays have component types that are reference types.)
     *
     * @param      src          the source array.
     * @param      src_position start position in the source array.
     * @param      dst          the destination array.
     * @param      dst_position pos   start position in the destination data.
     * @param      length       the number of array elements to be copied.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  ArrayStoreException  if an element in the <code>src</code>
     *               array could not be stored into the <code>dest</code> array
     *               because of a type mismatch.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dst</code> is <code>null</code>.
     */
    private void arraycopy( byte[] src, int src_position,
                            byte[] dst, int dst_position,
                            int length )
    {
        System.arraycopy( src, src_position, dst, dst_position, length );
    }

    /**
     * @return the unfinished string
     */
    String getUnfinishedString()
    {
        return unfinishedString;
    }

    /**
     * @return true if current string uses wide characters
     */
    boolean isWideChar()
    {
        return wideChar;
    }


}
