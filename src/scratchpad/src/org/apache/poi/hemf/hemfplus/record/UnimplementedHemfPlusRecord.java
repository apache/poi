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

package org.apache.poi.hemf.hemfplus.record;


import java.io.IOException;

import org.apache.poi.util.Internal;

@Internal
public class UnimplementedHemfPlusRecord implements HemfPlusRecord {

    private int recordId;
    private int flags;
    private byte[] recordBytes;

    @Override
    public HemfPlusRecordType getRecordType() {
        return HemfPlusRecordType.getById(recordId);
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public void init(byte[] recordBytes, int recordId, int flags) throws IOException {
        this.recordId = recordId;
        this.flags = flags;
        this.recordBytes = recordBytes;
    }

    public byte[] getRecordBytes() {
        //should probably defensively return a copy.
        return recordBytes;
    }
}
