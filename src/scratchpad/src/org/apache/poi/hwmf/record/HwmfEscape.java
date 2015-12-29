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

package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfEscape implements HwmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines the escape function. The 
     * value MUST be from the MetafileEscapes enumeration.
     */
    private int escapeFunction;
    /**
     * A 16-bit unsigned integer that specifies the size, in bytes, of the 
     * EscapeData field.
     */
    private int byteCount;
    /**
     * An array of bytes of size ByteCount.
     */
    private byte escapeData[];
    
    @Override
    public HwmfRecordType getRecordType() {
        return HwmfRecordType.escape;
    }
    
    @Override
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        escapeFunction = leis.readUShort();
        byteCount = leis.readUShort();
        escapeData = new byte[byteCount];
        leis.read(escapeData);
        return 2*LittleEndianConsts.SHORT_SIZE+byteCount;
    }

    @Override
    public void draw(HwmfGraphics ctx) {
        
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("escape - function: "+escapeFunction+"\n");
        sb.append(HexDump.dump(escapeData, 0, 0));
        return sb.toString();
    }
}
