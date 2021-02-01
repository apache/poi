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

package org.apache.poi.hdgf.streams;

import static org.apache.poi.hdgf.pointers.PointerV6.getNumPointersOffsetV6;
import static org.apache.poi.hdgf.pointers.PointerV6.getNumPointersV6;
import static org.apache.poi.hdgf.pointers.PointerV6.getPostNumPointersSkipV6;

import org.apache.poi.hdgf.pointers.Pointer;

public abstract class StreamTest {
    public static class TestPointer extends Pointer {
        private final boolean compressed;
        protected boolean hasPointers;
        public TestPointer(boolean compressed, int offset, int length, int type, short format) {
            this.compressed = compressed;
            setOffset(offset);
            setLength(length);
            setType(type);
            setFormat(format);
        }

        @Override
        public boolean destinationCompressed() { return compressed; }
        @Override
        public boolean destinationHasChunks() { return false; }
        @Override
        public boolean destinationHasPointers() { return hasPointers; }
        @Override
        public boolean destinationHasStrings() { return false; }

        @Override
        public int getSizeInBytes() { return -1; }
        @Override
        public int getNumPointersOffset(byte[] data) { 
            return getNumPointersOffsetV6(data); 
        }
        @Override
        public int getNumPointers(int offset, byte[] data) { 
            return getNumPointersV6(offset, data);
        }
        @Override
        public int getPostNumPointersSkip() {
            return getPostNumPointersSkipV6();
        }
    }
}
