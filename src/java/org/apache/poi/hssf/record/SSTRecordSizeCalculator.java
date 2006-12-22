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

/**
 * Used to calculate the record sizes for a particular record.  This kind of
 * sucks because it's similar to the SST serialization code.  In general
 * the SST serialization code needs to be rewritten.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
class SSTRecordSizeCalculator
{
    private IntMapper strings;

    public SSTRecordSizeCalculator(IntMapper strings)
    {
        this.strings = strings;
    }

    public int getRecordSize() {
        UnicodeString.UnicodeRecordStats rs = new UnicodeString.UnicodeRecordStats();
        rs.remainingSize -= SSTRecord.SST_RECORD_OVERHEAD;
        rs.recordSize += SSTRecord.SST_RECORD_OVERHEAD;
        for (int i=0; i < strings.size(); i++ )
        {
          UnicodeString unistr = ( (UnicodeString) strings.get(i));
          unistr.getRecordSize(rs);
    }
        return rs.recordSize;
    }
}
