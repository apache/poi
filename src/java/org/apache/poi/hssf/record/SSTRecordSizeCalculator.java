/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

import org.apache.poi.util.LittleEndianConsts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to calculate the record sizes for a particular record.
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
