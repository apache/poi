/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.util.BinaryTree;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title:        Static String Table Record
 * <P>
 * Description:  This holds all the strings for LabelSSTRecords.
 * <P>
 * REFERENCE:    PG 389 Microsoft Excel 97 Developer's Kit (ISBN:
 *               1-57231-498-2)
 * <P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.LabelSSTRecord
 * @see org.apache.poi.hssf.record.ContinueRecord
 */

public class SSTRecord
        extends Record
{

    /** how big can an SST record be? As big as any record can be: 8228 bytes */
    static final int MAX_RECORD_SIZE = 8228;

    /** standard record overhead: two shorts (record id plus data space size)*/
    static final int STD_RECORD_OVERHEAD =
            2 * LittleEndianConsts.SHORT_SIZE;

    /** SST overhead: the standard record overhead, plus the number of strings and the number of unique strings -- two ints */
    static final int SST_RECORD_OVERHEAD =
            ( STD_RECORD_OVERHEAD + ( 2 * LittleEndianConsts.INT_SIZE ) );

    /** how much data can we stuff into an SST record? That would be _max minus the standard SST record overhead */
    static final int MAX_DATA_SPACE = MAX_RECORD_SIZE - SST_RECORD_OVERHEAD;

    /** overhead for each string includes the string's character count (a short) and the flag describing its characteristics (a byte) */
    static final int STRING_MINIMAL_OVERHEAD = LittleEndianConsts.SHORT_SIZE + LittleEndianConsts.BYTE_SIZE;

    public static final short sid = 0xfc;

    /** union of strings in the SST and EXTSST */
    private int field_1_num_strings;

    /** according to docs ONLY SST */
    private int field_2_num_unique_strings;
    private BinaryTree field_3_strings;

    /** this is the number of characters we expect in the first sub-record in a subsequent continuation record */
    private int __expected_chars;

    /** this is the string we were working on before hitting the end of the current record. This string is NOT finished. */
    private String _unfinished_string;

    /** this is the total length of the current string being handled */
    private int _total_length_bytes;

    /** this is the offset into a string field of the actual string data */
    private int _string_data_offset;

    /** this is true if the string uses wide characters */
    private boolean _wide_char;

    /** Record lengths for initial SST record and all continue records */
    private List _record_lengths = null;

    /**
     * default constructor
     */

    public SSTRecord()
    {
        field_1_num_strings = 0;
        field_2_num_unique_strings = 0;
        field_3_strings = new BinaryTree();
        setExpectedChars( 0 );
        _unfinished_string = "";
        _total_length_bytes = 0;
        _string_data_offset = 0;
        _wide_char = false;
    }

    /**
     * Constructs an SST record and sets its fields appropriately.
     *
     * @param id must be 0xfc or an exception will be throw upon
     *           validation
     * @param size the size of the data area of the record
     * @param data of the record (should not contain sid/len)
     */

    public SSTRecord( final short id, final short size, final byte[] data )
    {
        super( id, size, data );
    }

    /**
     * Constructs an SST record and sets its fields appropriately.
     *
     * @param id must be 0xfc or an exception will be throw upon
     *           validation
     * @param size the size of the data area of the record
     * @param data of the record (should not contain sid/len)
     * @param offset of the record
     */

    public SSTRecord( final short id, final short size, final byte[] data,
                      int offset )
    {
        super( id, size, data, offset );
    }

    /**
     * Add a string. Determines whether 8-bit encoding can be used, or
     * whether 16-bit encoding must be used.
     * <p>
     * THIS IS THE PREFERRED METHOD OF ADDING A STRING. IF YOU USE THE
     * OTHER ,code>addString</code> METHOD AND FORCE 8-BIT ENCODING ON
     * A STRING THAT SHOULD USE 16-BIT ENCODING, YOU WILL CORRUPT THE
     * STRING; IF YOU USE THAT METHOD AND FORCE 16-BIT ENCODING, YOU
     * ARE WASTING SPACE WHEN THE WORKBOOK IS WRITTEN OUT.
     *
     * @param string string to be added
     *
     * @return the index of that string in the table
     */

    public int addString( final String string )
    {
        int rval;

        if ( string == null )
        {
            rval = addString( "", false );
        }
        else
        {

            // scan for characters greater than 255 ... if any are
            // present, we have to use 16-bit encoding. Otherwise, we
            // can use 8-bit encoding
            boolean useUTF16 = false;
            int strlen = string.length();

            for ( int j = 0; j < strlen; j++ )
            {
                if ( string.charAt( j ) > 255 )
                {
                    useUTF16 = true;
                    break;
                }
            }
            rval = addString( string, useUTF16 );
        }
        return rval;
    }

    /**
     * Add a string and assert the encoding (8-bit or 16-bit) to be
     * used.
     * <P>
     * USE THIS METHOD AT YOUR OWN RISK. IF YOU FORCE 8-BIT ENCODING,
     * YOU MAY CORRUPT YOUR STRING. IF YOU FORCE 16-BIT ENCODING AND
     * IT ISN'T NECESSARY, YOU WILL WASTE SPACE WHEN THIS RECORD IS
     * WRITTEN OUT.
     *
     * @param string string to be added
     * @param useUTF16 if true, forces 16-bit encoding. If false,
     *                 forces 8-bit encoding
     *
     * @return the index of that string in the table
     */

    public int addString( final String string, final boolean useUTF16 )
    {
        field_1_num_strings++;
        String str = ( string == null ) ? ""
                : string;
        int rval = -1;
        UnicodeString ucs = new UnicodeString();

        ucs.setString( str );
        ucs.setCharCount( (short) str.length() );
        ucs.setOptionFlags( (byte) ( useUTF16 ? 1
                : 0 ) );
        Integer integer = (Integer) field_3_strings.getKeyForValue( ucs );

        if ( integer != null )
        {
            rval = integer.intValue();
        }
        else
        {

            // This is a new string -- we didn't see it among the
            // strings we've already collected
            rval = field_3_strings.size();
            field_2_num_unique_strings++;
            integer = new Integer( rval );
            field_3_strings.put( integer, ucs );
        }
        return rval;
    }

    /**
     * @return number of strings
     */

    public int getNumStrings()
    {
        return field_1_num_strings;
    }

    /**
     * @return number of unique strings
     */

    public int getNumUniqueStrings()
    {
        return field_2_num_unique_strings;
    }

    /**
     * USE THIS METHOD AT YOUR OWN PERIL: THE <code>addString</code>
     * METHODS MANIPULATE THE NUMBER OF STRINGS AS A SIDE EFFECT; YOUR
     * ATTEMPTS AT MANIPULATING THE STRING COUNT IS LIKELY TO BE VERY
     * WRONG AND WILL RESULT IN BAD BEHAVIOR WHEN THIS RECORD IS
     * WRITTEN OUT AND ANOTHER PROCESS ATTEMPTS TO READ THE RECORD
     *
     * @param count  number of strings
     *
     */

    public void setNumStrings( final int count )
    {
        field_1_num_strings = count;
    }

    /**
     * USE THIS METHOD AT YOUR OWN PERIL: THE <code>addString</code>
     * METHODS MANIPULATE THE NUMBER OF UNIQUE STRINGS AS A SIDE
     * EFFECT; YOUR ATTEMPTS AT MANIPULATING THE UNIQUE STRING COUNT
     * IS LIKELY TO BE VERY WRONG AND WILL RESULT IN BAD BEHAVIOR WHEN
     * THIS RECORD IS WRITTEN OUT AND ANOTHER PROCESS ATTEMPTS TO READ
     * THE RECORD
     *
     * @param count  number of strings
     */

    public void getNumUniqueStrings( final int count )
    {
        field_2_num_unique_strings = count;
    }

    /**
     * Get a particular string by its index
     *
     * @param id index into the array of strings
     *
     * @return the desired string
     */

    public String getString( final int id )
    {
        return ( (UnicodeString) field_3_strings.get( new Integer( id ) ) )
                .getString();
    }

    public boolean getString16bit( final int id )
    {
        UnicodeString unicodeString = ( (UnicodeString) field_3_strings.get( new Integer( id ) ) );
        return ( ( unicodeString.getOptionFlags() & 0x01 ) == 1 );
    }

    /**
     * Return a debugging string representation
     *
     * @return string representation
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "[SST]\n" );
        buffer.append( "    .numstrings     = " )
                .append( Integer.toHexString( getNumStrings() ) ).append( "\n" );
        buffer.append( "    .uniquestrings  = " )
                .append( Integer.toHexString( getNumUniqueStrings() ) ).append( "\n" );
        for ( int k = 0; k < field_3_strings.size(); k++ )
        {
            buffer.append( "    .string_" + k + "      = " )
                    .append( ( (UnicodeString) field_3_strings
                    .get( new Integer( k ) ) ).toString() ).append( "\n" );
        }
        buffer.append( "[/SST]\n" );
        return buffer.toString();
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
            _unfinished_string = "";
            _total_length_bytes = 0;
            _string_data_offset = 0;
            _wide_char = false;
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
                        ? ( data_length
                        / LittleEndianConsts.SHORT_SIZE )
                        : ( data_length
                        / LittleEndianConsts.BYTE_SIZE ) );

                LittleEndian.putShort( input, (byte) 0, size );
                System.arraycopy( record, 0, input,
                        LittleEndianConsts.SHORT_SIZE,
                        record.length );
                UnicodeString ucs = new UnicodeString( UnicodeString.sid,
                        (short) input.length,
                        input );

                _unfinished_string = _unfinished_string + ucs.getString();
                setExpectedChars( getExpectedChars() - size );
            }
            else
            {
                setupStringParameters( record, -LittleEndianConsts.SHORT_SIZE,
                        getExpectedChars() );
                byte[] str_data = new byte[_total_length_bytes];
                int length = STRING_MINIMAL_OVERHEAD
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
                System.arraycopy( str_data, _string_data_offset, bstring,
                        STRING_MINIMAL_OVERHEAD,
                        bstring.length - STRING_MINIMAL_OVERHEAD );

                // use special constructor to create the final string
                UnicodeString string =
                        new UnicodeString( UnicodeString.sid,
                                (short) bstring.length, bstring,
                                _unfinished_string );
                Integer integer = new Integer( field_3_strings.size() );

                field_3_strings.put( integer, string );
                manufactureStrings( record,
                        _total_length_bytes
                        - LittleEndianConsts
                        .SHORT_SIZE, (short) record.length );
            }
        }
    }

    /**
     * @return sid
     */

    public short getSid()
    {
        return sid;
    }

    /**
     * @return hashcode
     */

    public int hashCode()
    {
        return field_2_num_unique_strings;
    }

    public boolean equals( Object o )
    {
        if ( ( o == null ) || ( o.getClass() != this.getClass() ) )
        {
            return false;
        }
        SSTRecord other = (SSTRecord) o;

        return ( ( field_1_num_strings == other
                .field_1_num_strings ) && ( field_2_num_unique_strings == other
                .field_2_num_unique_strings ) && field_3_strings
                .equals( other.field_3_strings ) );
    }

    /**
     * validate SID
     *
     * @param id the alleged SID
     *
     * @exception RecordFormatException if validation fails
     */

    protected void validateSid( final short id )
            throws RecordFormatException
    {
        if ( id != sid )
        {
            throw new RecordFormatException( "NOT An SST RECORD" );
        }
    }

    /**
     * Fill the fields from the data
     * <P>
     * The data consists of sets of string data. This string data is
     * arranged as follows:
     * <P>
     * <CODE>
     * short  string_length;   // length of string data
     * byte   string_flag;     // flag specifying special string
     *                         // handling
     * short  run_count;       // optional count of formatting runs
     * int    extend_length;   // optional extension length
     * char[] string_data;     // string data, can be byte[] or
     *                         // short[] (length of array is
     *                         // string_length)
     * int[]  formatting_runs; // optional formatting runs (length of
     *                         // array is run_count)
     * byte[] extension;       // optional extension (length of array
     *                         // is extend_length)
     * </CODE>
     * <P>
     * The string_flag is bit mapped as follows:
     * <P>
     * <TABLE>
     *   <TR>
     *      <TH>Bit number</TH>
     *      <TH>Meaning if 0</TH>
     *      <TH>Meaning if 1</TH>
     *   <TR>
     *   <TR>
     *      <TD>0</TD>
     *      <TD>string_data is byte[]</TD>
     *      <TD>string_data is short[]</TH>
     *   <TR>
     *   <TR>
     *      <TD>1</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TH>
     *   <TR>
     *   <TR>
     *      <TD>2</TD>
     *      <TD>extension is not included</TD>
     *      <TD>extension is included</TH>
     *   <TR>
     *   <TR>
     *      <TD>3</TD>
     *      <TD>formatting run data is not included</TD>
     *      <TD>formatting run data is included</TH>
     *   <TR>
     *   <TR>
     *      <TD>4</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TH>
     *   <TR>
     *   <TR>
     *      <TD>5</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TH>
     *   <TR>
     *   <TR>
     *      <TD>6</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TH>
     *   <TR>
     *   <TR>
     *      <TD>7</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TH>
     *   <TR>
     * </TABLE>
     * <P>
     * We can handle eating the overhead associated with bits 2 or 3
     * (or both) being set, but we have no idea what to do with the
     * associated data. The UnicodeString class can handle the byte[]
     * vs short[] nature of the actual string data
     *
     * @param data raw data
     * @param size size of the raw data
     */

    protected void fillFields( final byte[] data, final short size,
                               int offset )
    {

        // this method is ALWAYS called after construction -- using
        // the nontrivial constructor, of course -- so this is where
        // we initialize our fields
        field_1_num_strings = LittleEndian.getInt( data, 0 + offset );
        field_2_num_unique_strings = LittleEndian.getInt( data, 4 + offset );
        field_3_strings = new BinaryTree();
        setExpectedChars( 0 );
        _unfinished_string = "";
        _total_length_bytes = 0;
        _string_data_offset = 0;
        _wide_char = false;
        manufactureStrings( data, 8 + offset, size );
    }

    /**
     * @return the number of characters we expect in the first
     *         sub-record in a subsequent continuation record
     */

    int getExpectedChars()
    {
        return __expected_chars;
    }

    /**
     * @return an iterator of the strings we hold. All instances are
     *         UnicodeStrings
     */

    Iterator getStrings()
    {
        return field_3_strings.values().iterator();
    }

    /**
     * @return count of the strings we hold.
     */

    int countStrings()
    {
        return field_3_strings.size();
    }

    /**
     * @return the unfinished string
     */

    String getUnfinishedString()
    {
        return _unfinished_string;
    }

    /**
     * @return the total length of the current string
     */

    int getTotalLength()
    {
        return _total_length_bytes;
    }

    /**
     * @return offset into current string data
     */

    int getStringDataOffset()
    {
        return _string_data_offset;
    }

    /**
     * @return true if current string uses wide characters
     */

    boolean isWideChar()
    {
        return _wide_char;
    }


    private void manufactureStrings( final byte[] data, final int index,
                                     short size )
    {
        int offset = index;

        while ( offset < size )
        {
            int remaining = size - offset;

            if ( ( remaining > 0 )
                    && ( remaining < LittleEndianConsts.SHORT_SIZE ) )
            {
                throw new RecordFormatException(
                        "Cannot get length of the last string in SSTRecord" );
            }
            if ( remaining == LittleEndianConsts.SHORT_SIZE )
            {
                setExpectedChars( LittleEndian.getShort( data, offset ) );
                _unfinished_string = "";
                break;
            }
            short char_count = LittleEndian.getShort( data, offset );

            setupStringParameters( data, offset, char_count );
            if ( remaining < _total_length_bytes )
            {
                setExpectedChars( calculateCharCount( _total_length_bytes
                        - remaining ) );
                char_count -= getExpectedChars();
                _total_length_bytes = remaining;
            }
            else
            {
                setExpectedChars( 0 );
            }
            processString( data, offset, char_count );
            offset += _total_length_bytes;
            if ( getExpectedChars() != 0 )
            {
                break;
            }
        }
    }

    private void setupStringParameters( final byte[] data, final int index,
                                        final int char_count )
    {
        byte flag = data[index + LittleEndianConsts.SHORT_SIZE];

        _wide_char = ( flag & 1 ) == 1;
        boolean extended = ( flag & 4 ) == 4;
        boolean formatted_run = ( flag & 8 ) == 8;

        _total_length_bytes = STRING_MINIMAL_OVERHEAD
                + calculateByteCount( char_count );
        _string_data_offset = STRING_MINIMAL_OVERHEAD;
        if ( formatted_run )
        {
            short run_count = LittleEndian.getShort( data,
                    index
                    + _string_data_offset );

            _string_data_offset += LittleEndianConsts.SHORT_SIZE;
            _total_length_bytes += LittleEndianConsts.SHORT_SIZE
                    + ( LittleEndianConsts.INT_SIZE
                    * run_count );
        }
        if ( extended )
        {
            int extension_length = LittleEndian.getInt( data,
                    index
                    + _string_data_offset );

            _string_data_offset += LittleEndianConsts.INT_SIZE;
            _total_length_bytes += LittleEndianConsts.INT_SIZE
                    + extension_length;
        }
    }

    private void processString( final byte[] data, final int index,
                                final short char_count )
    {
        byte[] str_data = new byte[_total_length_bytes];
        int length = STRING_MINIMAL_OVERHEAD
                + calculateByteCount( char_count );
        byte[] bstring = new byte[length];

        System.arraycopy( data, index, str_data, 0, str_data.length );
        int offset = 0;

        LittleEndian.putShort( bstring, offset, char_count );
        offset += LittleEndianConsts.SHORT_SIZE;
        bstring[offset] = str_data[offset];
        System.arraycopy( str_data, _string_data_offset, bstring,
                STRING_MINIMAL_OVERHEAD,
                bstring.length - STRING_MINIMAL_OVERHEAD );
        UnicodeString string = new UnicodeString( UnicodeString.sid,
                (short) bstring.length,
                bstring );

        if ( getExpectedChars() != 0 )
        {
            _unfinished_string = string.getString();
        }
        else
        {
            Integer integer = new Integer( field_3_strings.size() );

            // This retry loop is a nasty hack that lets us get around the issue of duplicate
            // strings in the SST record.  There should never be duplicates but because we don't
            // handle rich text records correctly this may occur.  Also some Excel alternatives
            // do not seem correctly add strings to this table.
            //
            // The hack bit is that we add spaces to the end of the string until don't get an
            // illegal argument exception when adding.  One day we will have to deal with this
            // more gracefully.
            boolean added = false;
            while ( !added )
            {
                try
                {
                    field_3_strings.put( integer, string );
                    added = true;
                }
                catch ( IllegalArgumentException duplicateValue )
                {
                    string.setString( string.getString() + " " );
                }
            }
        }
    }

    private void setExpectedChars( final int count )
    {
        __expected_chars = count;
    }

    private int calculateByteCount( final int character_count )
    {
        return character_count * ( _wide_char ? LittleEndianConsts.SHORT_SIZE
                : LittleEndianConsts.BYTE_SIZE );
    }

    private int calculateCharCount( final int byte_count )
    {
        return byte_count / ( _wide_char ? LittleEndianConsts.SHORT_SIZE
                : LittleEndianConsts.BYTE_SIZE );
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @return byte array containing instance data
     */

    public int serialize( int offset, byte[] data )
    {
        SSTSerializer serializer = new SSTSerializer(
                _record_lengths, field_3_strings, getNumStrings(), getNumUniqueStrings() );
        return serializer.serialize( offset, data );
    }


    // we can probably simplify this later...this calculates the size
    // w/o serializing but still is a bit slow
    public int getRecordSize()
    {
        SSTSerializer serializer = new SSTSerializer(
                _record_lengths, field_3_strings, getNumStrings(), getNumUniqueStrings() );

        return serializer.getRecordSize();
    }

}


