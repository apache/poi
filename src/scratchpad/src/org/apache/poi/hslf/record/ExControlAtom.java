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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

/**
 * Contains a long integer, slideID, which stores the unique slide identifier of the slide
 * where this control resides.
 *
 * @author Yegor Kozlov
 */
public class ExControlAtom extends RecordAtom {


    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * slideId.
     */
    private int _id;

    /**
     * Constructs a brand new embedded object atom record.
     */
    protected ExControlAtom() {
        _header = new byte[8];

        LittleEndian.putShort(_header, 2, (short) getRecordType());
        LittleEndian.putInt(_header, 4, 4);

    }

    /**
     * Constructs the ExControlAtom record from its source data.
     *
     * @param source the source data as a byte array.
     * @param start  the start offset into the byte array.
     * @param len    the length of the slice in the byte array.
     */
    protected ExControlAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source, start, _header, 0, 8);

        _id = LittleEndian.getInt(source, start + 8);
    }

    public int getSlideId() {
        return _id;
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.ExControlAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        byte[] data = new byte[4];
        LittleEndian.putInt(data, _id);
        out.write(data);
    }

}
