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

package org.apache.poi.hslf.record;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Is a container for all sound related atoms and containers. It contains:
 *<li>1. SoundCollAtom (2021)
 *<li>2. Sound (2022), for each sound, if any
 *
 * @author Yegor Kozlov
 */
public final class SoundCollection extends RecordContainer {
    /**
     * Record header data.
     */
    private byte[] _header;

    /**
     * Set things up, and find our more interesting children
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected SoundCollection(byte[] source, int start, int len) {
        // Grab the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Find our children
        _children = Record.findChildRecords(source,start+8,len-8);
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.SoundCollection.typeID;
    }

    /**
     * Have the contents printer out into an OutputStream, used when
     * writing a file back out to disk.
     *
     * @param out the output stream.
     * @throws java.io.IOException if there was an error writing to the stream.
     */
    public void writeOut(OutputStream out) throws IOException {
        writeOut(_header[0],_header[1],getRecordType(),_children,out);
    }
}
