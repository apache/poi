
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
