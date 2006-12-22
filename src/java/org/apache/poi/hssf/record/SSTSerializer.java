
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.IntMapper;
import org.apache.poi.util.LittleEndian;

/**
 * This class handles serialization of SST records.  It utilizes the record processor
 * class write individual records. This has been refactored from the SSTRecord class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SSTSerializer
{

    // todo: make private again
    private IntMapper strings;

    private SSTRecordHeader sstRecordHeader;

    /** Offsets from the beginning of the SST record (even across continuations) */
    int[] bucketAbsoluteOffsets;
    /** Offsets relative the start of the current SST or continue record */
    int[] bucketRelativeOffsets;
    int startOfSST, startOfRecord;

    public SSTSerializer( IntMapper strings, int numStrings, int numUniqueStrings )
    {
        this.strings = strings;
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
    public int serialize(int offset, byte[] data )
    {
      UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
      sstRecordHeader.writeSSTHeader( stats, data, 0 + offset, 0 );
      int pos = offset + SSTRecord.SST_RECORD_OVERHEAD;

        for ( int k = 0; k < strings.size(); k++ )
        {
            if (k % ExtSSTRecord.DEFAULT_BUCKET_SIZE == 0)
            {
              int index = k/ExtSSTRecord.DEFAULT_BUCKET_SIZE;
              if (index < ExtSSTRecord.MAX_BUCKETS) {
                //Excel only indexes the first 128 buckets.
              bucketAbsoluteOffsets[index] = pos-offset;
              bucketRelativeOffsets[index] = pos-offset;
              }
            }
          UnicodeString s = getUnicodeString(k);
          pos += s.serialize(stats, pos, data);
            }
      //Check to see if there is a hanging continue record length
      if (stats.lastLengthPos != -1) {
        short lastRecordLength = (short)(pos - stats.lastLengthPos-2);
        if (lastRecordLength > 8224)
          throw new InternalError();

        LittleEndian.putShort(data, stats.lastLengthPos, lastRecordLength);
                  }
      return pos - offset;
                }


    private UnicodeString getUnicodeString( int index )
    {
        return getUnicodeString(strings, index);
    }

    private static UnicodeString getUnicodeString( IntMapper strings, int index )
    {
        return ( (UnicodeString) strings.get( index ) );
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
