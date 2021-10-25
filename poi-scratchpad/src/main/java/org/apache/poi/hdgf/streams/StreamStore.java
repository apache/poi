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

import org.apache.poi.util.IOUtils;

/**
 * Holds the representation of the stream on-disk, and
 *  handles de-compressing it as required.
 * In future, may also handle writing it back out again
 */
public class StreamStore { // TODO - instantiable superclass
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 10_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private byte[] contents;

    /**
     * @param length the max record length allowed for StreamStore
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for StreamStore
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Creates a new, non compressed Stream Store
     */
    protected StreamStore(byte[] data, int offset, int length) {
        contents = IOUtils.safelyClone(data, offset, length, MAX_RECORD_LENGTH);
    }

    protected void prependContentsWith(byte[] b) {
        byte[] newContents = IOUtils.safelyAllocate(contents.length + (long)b.length, MAX_RECORD_LENGTH);
        System.arraycopy(b, 0, newContents, 0, b.length);
        System.arraycopy(contents, 0, newContents, b.length, contents.length);
        contents = newContents;
    }
    protected void copyBlockHeaderToContents() {}

    protected byte[] getContents() { return contents; }
    public byte[] _getContents() { return contents; }
}
