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

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

@Internal
public class HemfPlusHeader implements HemfPlusRecord {

    private int flags;
    private long version; //hack for now; replace with EmfPlusGraphicsVersion object
    private long emfPlusFlags;
    private long logicalDpiX;
    private long logicalDpiY;

    @Override
    public HemfPlusRecordType getEmfPlusRecordType() {
        return HemfPlusRecordType.header;
    }

    public int getFlags() {
        return flags;
    }

    @Override
    public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
        this.flags = flags;
        version = leis.readUInt();

        // verify MetafileSignature (20 bits) == 0xDBC01 and
        // GraphicsVersion (12 bits) in (1 or 2)
        assert((version & 0xFFFFFA00) == 0xDBC01000L && ((version & 0x3FF) == 1 || (version & 0x3FF) == 2));

        emfPlusFlags = leis.readUInt();

        logicalDpiX = leis.readUInt();
        logicalDpiY = leis.readUInt();
        return 4* LittleEndianConsts.INT_SIZE;
    }

    public long getVersion() {
        return version;
    }

    public long getEmfPlusFlags() {
        return emfPlusFlags;
    }

    public long getLogicalDpiX() {
        return logicalDpiX;
    }

    public long getLogicalDpiY() {
        return logicalDpiY;
    }

    @Override
    public String toString() {
        return "HemfPlusHeader{" +
                "flags=" + flags +
                ", version=" + version +
                ", emfPlusFlags=" + emfPlusFlags +
                ", logicalDpiX=" + logicalDpiX +
                ", logicalDpiY=" + logicalDpiY +
                '}';
    }
}