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
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Tne atom that holds starting and ending character positions of a hyperlink
 *
 * @author Yegor Kozlov
 */
public final class TxInteractiveInfoAtom extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs a brand new link related atom record.
     */
    public TxInteractiveInfoAtom() {
        _header = new byte[8];
        _data = new byte[8];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected TxInteractiveInfoAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the record data.
        _data = IOUtils.safelyClone(source, start+8, len-8, MAX_RECORD_LENGTH);
    }

    /**
     * Gets the beginning character position
     *
     * @return the beginning character position
     */
    public int getStartIndex() {
        return LittleEndian.getInt(_data, 0);
    }

    /**
     * Sets the beginning character position
     * @param idx the beginning character position
     */
    public void setStartIndex(int idx) {
        LittleEndian.putInt(_data, 0, idx);
    }

    /**
     * Gets the ending character position
     *
     * @return the ending character position
     */
    public int getEndIndex() {
        return LittleEndian.getInt(_data, 4);
    }

    /**
     * Sets the ending character position
     *
     * @param idx the ending character position
     */
    public void setEndIndex(int idx) {
        LittleEndian.putInt(_data, 4, idx);
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return RecordTypes.TxInteractiveInfoAtom.typeID; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "startIndex", this::getStartIndex,
            "endIndex", this::getEndIndex
        );
    }
}
