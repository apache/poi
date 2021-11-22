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
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

public class DateTimeMCAtom extends RecordAtom {

    /**
     * Record header.
     */
    private final byte[] _header;

    /**
     * A TextPosition that specifies the position of the metacharacter in the corresponding text.
     */
    private int position;

    /**
     * An unsigned byte that specifies the Format ID used to stylize datetime. The identifier specified by
     * the Format ID is converted based on the LCID [MS-LCID] into a value or string as specified in the
     * following tables. The LCID is specified in TextSIException.lid. If no valid LCID is found in
     * TextSIException.lid, TextSIException.altLid (if it exists) is used.
     * The value MUST be greater than or equal to 0x0 and MUST be less than or equal to 0xC.
     */
    private int index;

    private final byte[] unused = new byte[3];

    protected DateTimeMCAtom() {
        _header = new byte[8];
        position = 0;
        index = 0;

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, 8);
    }

    /**
     * Constructs the datetime atom record from its source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected DateTimeMCAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        position = LittleEndian.getInt(source, start+8);
        index  = LittleEndian.getUByte(source, start+12);
        System.arraycopy(source, start+13, unused, 0, 3);
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        LittleEndian.putInt(position, out);
        out.write(index);
        out.write(unused);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    @Override
    public long getRecordType() {
        return RecordTypes.DateTimeMCAtom.typeID;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "position", this::getPosition,
            "index", this::getIndex
        );
    }

}
