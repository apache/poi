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
}

