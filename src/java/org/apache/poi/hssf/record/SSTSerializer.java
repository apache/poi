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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class handles serialization of SST records.  It utilizes the record processor
 * class write individual records. This has been refactored from the SSTRecord class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SSTSerializer
{

    // todo: make private again
    private List recordLengths;
    private BinaryTree strings;

    private int numStrings;
    private int numUniqueStrings;
    private SSTRecordHeader sstRecordHeader;

    public SSTSerializer( List recordLengths, BinaryTree strings, int numStrings, int numUniqueStrings )
    {
        this.recordLengths = recordLengths;
        this.strings = strings;
        this.numStrings = numStrings;
        this.numUniqueStrings = numUniqueStrings;
        this.sstRecordHeader = new SSTRecordHeader( numStrings, numUniqueStrings );
    }

    /**
     * Create a byte array consisting of an SST record and any
     * required Continue records, ready to be written out.
     * <p>
     * If an SST record and any subsequent Continue records are read
     * in to create this instance, this method should produce a byte
     * array that is identical to the byte array produced by
     * concatenating the input records' data.
     *
     * @return the byte array
     */
    public int serialize( int record_size, int offset, byte[] data )
    {
        int record_length_index = 0;

        if ( calculateUnicodeSize() > SSTRecord.MAX_DATA_SPACE )
            serializeLargeRecord( record_size, record_length_index, data, offset );
        else
            serializeSingleSSTRecord( data, offset, record_length_index );
        return record_size;
    }



    /**
     * Calculates the total unicode size for all the strings.
     *
     * @return the total size.
     */
    public static int calculateUnicodeSize(Map strings)
    {
        int retval = 0;

        for ( int k = 0; k < strings.size(); k++ )
        {
            retval += getUnicodeString( strings, k ).getRecordSize();
        }
        return retval;
    }

    public int calculateUnicodeSize()
    {
        return calculateUnicodeSize(strings);
    }

    /**
     * This case is chosen when an SST record does not span over to a continue record.
     *
     */
    private void serializeSingleSSTRecord( byte[] data, int offset, int record_length_index )
    {
        int len = ( (Integer) recordLengths.get( record_length_index ) ).intValue();
        int recordSize = SSTRecord.SST_RECORD_OVERHEAD + len - SSTRecord.STD_RECORD_OVERHEAD;
        sstRecordHeader.writeSSTHeader( data, 0 + offset, recordSize );
        int pos = SSTRecord.SST_RECORD_OVERHEAD;

        for ( int k = 0; k < strings.size(); k++ )
        {
            System.arraycopy( getUnicodeString( k ).serialize(), 0, data, pos + offset, getUnicodeString( k ).getRecordSize() );
            pos += getUnicodeString( k ).getRecordSize();
        }
    }

    /**
     * Large records are serialized to an SST and to one or more CONTINUE records.  Joy.  They have the special
     * characteristic that they can change the option field when a single string is split across to a
     * CONTINUE record.
     */
    private void serializeLargeRecord( int record_size, int record_length_index, byte[] buffer, int offset )
    {

        byte[] stringReminant = null;
        int stringIndex = 0;
        boolean lastneedcontinue = false;
        boolean first_record = true;
        int totalWritten = 0;

        while ( totalWritten != record_size )
        {
            int recordLength = ( (Integer) recordLengths.get( record_length_index++ ) ).intValue();
            RecordProcessor recordProcessor = new RecordProcessor( buffer,
                    recordLength, numStrings, numUniqueStrings );

            // write the appropriate header
            recordProcessor.writeRecordHeader( offset, totalWritten, recordLength, first_record );
            first_record = false;

            // now, write the rest of the data into the current
            // record space
            if ( lastneedcontinue )
            {
                lastneedcontinue = stringReminant.length > recordProcessor.getAvailable();
                // the last string in the previous record was not written out completely
                stringReminant = recordProcessor.writeStringRemainder( lastneedcontinue,
                        stringReminant, offset, totalWritten );
            }

            // last string's remnant, if any, is cleaned up as best as can be done ... now let's try and write
            // some more strings
            for ( ; stringIndex < strings.size(); stringIndex++ )
            {
                UnicodeString unistr = getUnicodeString( stringIndex );

                if ( unistr.getRecordSize() <= recordProcessor.getAvailable() )
                {
                    recordProcessor.writeWholeString( unistr, offset, totalWritten );
                }
                else
                {

                    // can't write the entire string out
                    if ( recordProcessor.getAvailable() >= SSTRecord.STRING_MINIMAL_OVERHEAD )
                    {

                        // we can write some of it
                        stringReminant = recordProcessor.writePartString( unistr, offset, totalWritten );
                        lastneedcontinue = true;
                        stringIndex++;
                    }
                    break;
                }
            }
            totalWritten += recordLength + SSTRecord.STD_RECORD_OVERHEAD;
        }
    }

    private UnicodeString getUnicodeString( int index )
    {
        return getUnicodeString(strings, index);
    }

    private static UnicodeString getUnicodeString( Map strings, int index )
    {
        Integer intunipos = new Integer( index );
        return ( (UnicodeString) strings.get( intunipos ) );
    }

    public int getRecordSize()
    {
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        int recordSize = calculator.getRecordSize();
        recordLengths = calculator.getRecordLengths();
        return recordSize;
    }

    public List getRecordLengths()
    {
        return recordLengths;
    }
}
