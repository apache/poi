
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

    /** Offsets from the beginning of the SST record (even across continuations) */
    int[] bucketAbsoluteOffsets;
    /** Offsets relative the start of the current SST or continue record */
    int[] bucketRelativeOffsets;
    int startOfSST, startOfRecord;

    public SSTSerializer( List recordLengths, BinaryTree strings, int numStrings, int numUniqueStrings )
    {
        this.recordLengths = recordLengths;
        this.strings = strings;
        this.numStrings = numStrings;
        this.numUniqueStrings = numUniqueStrings;
        this.sstRecordHeader = new SSTRecordHeader( numStrings, numUniqueStrings );

        int infoRecs = ExtSSTRecord.getNumberOfInfoRecsForStrings(strings.size());
        this.bucketAbsoluteOffsets = new int[infoRecs];
        this.bucketRelativeOffsets = new int[infoRecs];
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
     */
    private void serializeSingleSSTRecord( byte[] data, int offset, int record_length_index )
    {
        int len = ( (Integer) recordLengths.get( record_length_index ) ).intValue();
        int recordSize = SSTRecord.SST_RECORD_OVERHEAD + len - SSTRecord.STD_RECORD_OVERHEAD;
        sstRecordHeader.writeSSTHeader( data, 0 + offset, recordSize );
        int pos = SSTRecord.SST_RECORD_OVERHEAD;

        for ( int k = 0; k < strings.size(); k++ )
        {
            if (k % ExtSSTRecord.DEFAULT_BUCKET_SIZE == 0)
            {
              int index = k/ExtSSTRecord.DEFAULT_BUCKET_SIZE;
              if (index < ExtSSTRecord.MAX_BUCKETS) {
                //Excel only indexes the first 128 buckets.
                bucketAbsoluteOffsets[index] = pos;
                bucketRelativeOffsets[index] = pos;
              }
            }
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

        startOfSST = offset;

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
            startOfRecord = offset + totalWritten;
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

                if (stringIndex % ExtSSTRecord.DEFAULT_BUCKET_SIZE == 0)
                {
                  int index = stringIndex / ExtSSTRecord.DEFAULT_BUCKET_SIZE;
                  if (index < ExtSSTRecord.MAX_BUCKETS) {
                    bucketAbsoluteOffsets[index] = offset + totalWritten +
                        recordProcessor.getRecordOffset() - startOfSST;
                    bucketRelativeOffsets[index] = offset + totalWritten +
                        recordProcessor.getRecordOffset() - startOfRecord;
                  }
                }

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

    public int[] getBucketAbsoluteOffsets()
    {
        return bucketAbsoluteOffsets;
    }

    public int[] getBucketRelativeOffsets()
    {
        return bucketRelativeOffsets;
    }
}
