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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Write out an SST header record.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SSTRecordHeader
{
    int numStrings;
    int numUniqueStrings;

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