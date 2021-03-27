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

import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.IntMapper;

/**
 * This class handles serialization of SST records.  It utilizes the record processor
 * class write individual records. This has been refactored from the SSTRecord class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
final class SSTSerializer {

	private final int _numStrings;
	private final int _numUniqueStrings;

    private final IntMapper<UnicodeString> strings;

    /** Offsets from the beginning of the SST record (even across continuations) */
    private final int[] bucketAbsoluteOffsets;
    /** Offsets relative the start of the current SST or continue record */
    private final int[] bucketRelativeOffsets;

    public SSTSerializer( IntMapper<UnicodeString> strings, int numStrings, int numUniqueStrings )
    {
        this.strings = strings;
		_numStrings = numStrings;
		_numUniqueStrings = numUniqueStrings;

        int infoRecs = ExtSSTRecord.getNumberOfInfoRecsForStrings(strings.size());
        this.bucketAbsoluteOffsets = new int[infoRecs];
        this.bucketRelativeOffsets = new int[infoRecs];
    }

    public void serialize(ContinuableRecordOutput out) {
        out.writeInt(_numStrings);
        out.writeInt(_numUniqueStrings);

        for ( int k = 0; k < strings.size(); k++ )
        {
            if (k % ExtSSTRecord.DEFAULT_BUCKET_SIZE == 0)
            {
              int rOff = out.getTotalSize();
              int index = k/ExtSSTRecord.DEFAULT_BUCKET_SIZE;
              if (index < ExtSSTRecord.MAX_BUCKETS) {
                 //Excel only indexes the first 128 buckets.
                 bucketAbsoluteOffsets[index] = rOff;
                 bucketRelativeOffsets[index] = rOff;
              }
          }
          UnicodeString s = getUnicodeString(k);
          s.serialize(out);
        }
    }


    private UnicodeString getUnicodeString( int index )
    {
        return getUnicodeString(strings, index);
    }

    private static UnicodeString getUnicodeString( IntMapper<UnicodeString> strings, int index )
    {
        return ( strings.get( index ) );
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
