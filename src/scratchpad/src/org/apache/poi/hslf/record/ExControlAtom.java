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
 * An atom record that specifies an ActiveX control.
 *
 * @author Yegor Kozlov
 */
public final class ExControlAtom extends RecordAtom {


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

    /**
     * An integer that specifies which presentation slide is associated with the ActiveX control.
     * <p>
     * It MUST be 0x00000000 or equal to the value of the slideId field of a SlidePersistAtom record.
     * The value 0x00000000 specifies a null reference.
     * </p>
     *
     * @return an integer that specifies which presentation slide is associated with the ActiveX control
     */
    public int getSlideId() {
        return _id;
    }

    /**
     * Sets which presentation slide is associated with the ActiveX control.
     *
     * @param id an integer that specifies which presentation slide is associated with the ActiveX control
     * <p>
     * It MUST be 0x00000000 or equal to the value of the slideId field of a SlidePersistAtom record.
     * The value 0x00000000 specifies a null reference.
     * </p>
     */
    public void setSlideId(int id) {
        _id = id;
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
