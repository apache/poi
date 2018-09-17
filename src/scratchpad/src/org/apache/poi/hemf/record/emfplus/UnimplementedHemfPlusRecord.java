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

package org.apache.poi.hemf.record.emfplus;


import java.io.IOException;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;

@Internal
public class UnimplementedHemfPlusRecord implements HemfPlusRecord {

    private static final int MAX_RECORD_LENGTH = 1_000_000;

    private HemfPlusRecordType recordType;
    private int flags;
    private byte[] recordBytes;

    @Override
    public HemfPlusRecordType getEmfPlusRecordType() {
        return recordType;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
        recordType = HemfPlusRecordType.getById(recordId);
        this.flags = flags;
        recordBytes = IOUtils.safelyAllocate(dataSize, MAX_RECORD_LENGTH);
        leis.readFully(recordBytes);
        return recordBytes.length;
    }

    public byte[] getRecordBytes() {
        //should probably defensively return a copy.
        return recordBytes;
    }
}