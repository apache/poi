
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to calculate the record sizes for a particular record.  This kind of
 * sucks because it's similar to the SST serialization code.  In general
 * the SST serialization code needs to be rewritten.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SSTRecordSizeCalculator
{
    private UnicodeString unistr = null;
    private int stringReminant = 0;
    private int unipos = 0;
    /** Is there any more string to be written? */
    private boolean isRemainingString = false;
    private int totalBytesWritten = 0;
    private boolean finished = false;
    private boolean firstRecord = true;
    private int totalWritten = 0;
    private int recordSize = 0;
    private List recordLengths = new ArrayList();
    private int pos = 0;
    private Map strings;

    public SSTRecordSizeCalculator(Map strings)
    {
        this.strings = strings;
    }

    /**
     * Calculate the size in bytes of the SST record.  This will include continue
     * records.
     *
     * @return the size of the SST record.
     */
    public int getRecordSize()
    {
        initVars();

        int retval;
        int totalStringSpaceRequired = SSTSerializer.calculateUnicodeSize(strings);

        if ( totalStringSpaceRequired > SSTRecord.MAX_DATA_SPACE )
        {
            retval = sizeOverContinuation( totalStringSpaceRequired );
        }
        else
        {
            // short data: write one simple SST record
            retval = SSTRecord.SST_RECORD_OVERHEAD + totalStringSpaceRequired;
            recordLengths.add( new Integer( totalStringSpaceRequired ) );
        }
        return retval;
    }

    public List getRecordLengths()
    {
        return recordLengths;
    }

    private int sizeOverContinuation( int totalStringSpaceRequired )
    {
        int retval;

        while ( !finished )
        {
            recordSize = 0;
            pos = 0;

            if ( firstRecord )
            {
                addMaxLengthRecordSize();
            }
            else
            {

                // writing continue record
                pos = 0;
                int toBeWritten = ( totalStringSpaceRequired - totalBytesWritten ) + ( isRemainingString ? 1 : 0 );
                int size = Math.min( SSTRecord.MAX_RECORD_SIZE - SSTRecord.STD_RECORD_OVERHEAD, toBeWritten );

                if ( size == toBeWritten )
                {
                    finished = true;
                }
                recordSize = size + SSTRecord.STD_RECORD_OVERHEAD;
                recordLengths.add( new Integer( size ) );
                pos = 4;
            }
            if ( isRemainingString )
            {
                calcReminant();
            }
            calcRemainingStrings();
            totalWritten += recordSize;
        }
        retval = totalWritten;

        return retval;
    }

    private void addMaxLengthRecordSize()
    {
        // writing SST record
        recordSize = SSTRecord.MAX_RECORD_SIZE;
        pos = 12;
        firstRecord = false;
        recordLengths.add( new Integer( recordSize - SSTRecord.STD_RECORD_OVERHEAD ) );
    }

    private void calcRemainingStrings()
    {
        for ( ; unipos < strings.size(); unipos++ )
        {
            int available = SSTRecord.MAX_RECORD_SIZE - pos;
            Integer intunipos = new Integer( unipos );

            unistr = ( (UnicodeString) strings.get( intunipos ) );
            if ( unistr.getRecordSize() <= available )
            {
                totalBytesWritten += unistr.getRecordSize();
                pos += unistr.getRecordSize();
            }
            else
            {
                if ( available >= SSTRecord.STRING_MINIMAL_OVERHEAD )
                {
                    int toBeWritten =
                            unistr.maxBrokenLength( available );

                    totalBytesWritten += toBeWritten;
                    stringReminant =
                            ( unistr.getRecordSize() - toBeWritten )
                            + LittleEndianConsts.BYTE_SIZE;
                    if ( available != toBeWritten )
                    {
                        int shortrecord = recordSize
                                - ( available - toBeWritten );

                        recordLengths.set(
                                recordLengths.size() - 1,
                                new Integer(
                                        shortrecord - SSTRecord.STD_RECORD_OVERHEAD ) );
                        recordSize = shortrecord;
                    }
                    isRemainingString = true;
                    unipos++;
                }
                else
                {
                    int shortrecord = recordSize - available;

                    recordLengths.set( recordLengths.size() - 1,
                            new Integer( shortrecord - SSTRecord.STD_RECORD_OVERHEAD ) );
                    recordSize = shortrecord;
                }
                break;
            }
        }
    }

    private void calcReminant()
    {
        int available = SSTRecord.MAX_RECORD_SIZE - pos;

        if ( stringReminant <= available )
        {

            // write reminant
            totalBytesWritten += stringReminant - 1;
            pos += stringReminant;
            isRemainingString = false;
        }
        else
        {

            // write as much of the remnant as possible
            int toBeWritten = unistr.maxBrokenLength( available );

            if ( available != toBeWritten )
            {
                int shortrecord = recordSize - ( available - toBeWritten );
                recordLengths.set( recordLengths.size() - 1,
                        new Integer( shortrecord - SSTRecord.STD_RECORD_OVERHEAD ) );
                recordSize = shortrecord;
            }
            totalBytesWritten += toBeWritten - 1;
            pos += toBeWritten;
            stringReminant -= toBeWritten - 1;
            isRemainingString = true;
        }
    }

    private void initVars()
    {
        unistr = null;
        stringReminant = 0;
        unipos = 0;
        isRemainingString = false;
        totalBytesWritten = 0;
        finished = false;
        firstRecord = true;
        totalWritten = 0;
    }

}
