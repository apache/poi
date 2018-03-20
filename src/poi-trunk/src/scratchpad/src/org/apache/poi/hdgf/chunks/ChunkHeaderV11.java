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

package org.apache.poi.hdgf.chunks;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A chunk header from v11+
 */
public final class ChunkHeaderV11 extends ChunkHeaderV6 {
    /**
     * Does the chunk have a separator?
     */
    public boolean hasSeparator() {
        short unknown2 = getUnknown2();
        short unknown3 = getUnknown3();
        
        switch (getType()) {
            case 0x1f: case 0xc9:
                // For some reason, there are two types that don't have a
                //  separator despite the flags that indicate they do
                return false;
            
            case 0x69:
                return true;

            case 0xa9: case 0xaa: case 0xb4: case 0xb6:
                if (unknown2 == 2 && unknown3 == 0x54) {
                    return true;
                }
                break;
                
            default:
                break;
        }

        if (
            (unknown2 == 2 && unknown3 == 0x55) || 
            (unknown2 == 3 && unknown3 != 0x50)
        ) { 
            return true; 
        }
        
        // If there's a trailer, there's a separator
        return hasTrailer();
    }

    @Override
    public Charset getChunkCharset() {
        return StandardCharsets.UTF_16LE;
    }
}
