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
package org.apache.poi.hdgf.pointers;

import org.apache.poi.hdgf.exceptions.OldVisioFormatException;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Factor class to create the appropriate pointers, based on the version
 *  of the file
 */
public final class PointerFactory {
    private static final int MAX_NUMBER_OF_POINTERS = 100_000;

    private final int version;

    public PointerFactory(int version) {
        this.version = version;
    }

    public int getVersion() { return version; }

    /**
     * Creates a single Pointer from the data at the given offset
     */
    public Pointer createPointer(byte[] data, int offset) {
        Pointer p;
        if(version >= 6) {
            p = new PointerV6();
            p.setType(LittleEndian.getInt(data, offset));
            p.setAddress((int)LittleEndian.getUInt(data, offset+4));
            p.setOffset((int)LittleEndian.getUInt(data, offset+8));
            p.setLength((int)LittleEndian.getUInt(data, offset+12));
            p.setFormat(LittleEndian.getShort(data, offset+16));

            return p;
        } else if(version == 5) {
            p = new PointerV5();
            p.setType(LittleEndian.getShort(data, offset));
            p.setFormat(LittleEndian.getShort(data, offset+2));
            p.setAddress((int)LittleEndian.getUInt(data, offset+4));
            p.setOffset((int)LittleEndian.getUInt(data, offset+8));
            p.setLength((int)LittleEndian.getUInt(data, offset+12));

            return p;
        } else {
            throw new OldVisioFormatException("Visio files with versions below 5 are not supported, yours was " + version);
        }
    }

    /**
     * Parsers the {@link PointerContainingStream} contents and
     *  creates all the child Pointers for it
     */
    public Pointer[] createContainerPointers(Pointer parent, byte[] data) {
        // Where in the stream does the "number of pointers" offset live?
        int numPointersOffset = parent.getNumPointersOffset(data);
        // How many do we have?
        int numPointers = parent.getNumPointers(numPointersOffset, data);
        // How much to skip for the num pointers + any extra data?
        int skip = parent.getPostNumPointersSkip();

        if (numPointers < 0) {
            throw new IllegalArgumentException("Cannot create container pointers with negative count: " + numPointers);
        }

        IOUtils.safelyAllocateCheck(numPointers, MAX_NUMBER_OF_POINTERS);

        // Create
        int pos = numPointersOffset + skip;
        Pointer[] childPointers = new Pointer[numPointers];
        for(int i=0; i<numPointers; i++) {
            childPointers[i] = this.createPointer(data, pos);
            pos += childPointers[i].getSizeInBytes();
        }

        return childPointers;
    }
}
