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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hslf.model.textproperties.IndentProp;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Specifies the Indent Level for the text
 */
public final class MasterTextPropAtom extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    // indent details
    private List<IndentProp> indents;

    /**
     * @param length the max record length allowed for MasterTextPropAtom
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for MasterTextPropAtom
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Constructs a new empty master text prop atom.
     */
    public MasterTextPropAtom() {
        _header = new byte[8];
        _data = new byte[0];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);

        indents = new ArrayList<>();
    }

    /**
     * Constructs the ruler atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected MasterTextPropAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the record data.
        _data = IOUtils.safelyClone(source, start+8, len-8, MAX_RECORD_LENGTH);

        try {
            read();
        } catch (Exception e){
            LOG.atError().withThrowable(e).log("Failed to parse MasterTextPropAtom");
        }
    }

    /**
     * Gets the record type.
     *
     * @return the record type.
     */
    @Override
    public long getRecordType() {
        return RecordTypes.MasterTextPropAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        write();
        out.write(_header);
        out.write(_data);
    }

    /**
     * Write the internal variables to the record bytes
     */
    private void write() {
        int pos = 0;
        long newSize = Math.multiplyExact((long)indents.size(), (long)6);
        _data = IOUtils.safelyAllocate(newSize, MAX_RECORD_LENGTH);
        for (IndentProp prop : indents) {
            LittleEndian.putInt(_data, pos, prop.getCharactersCovered());
            LittleEndian.putShort(_data, pos+4, (short)prop.getIndentLevel());
            pos += 6;
        }
    }

    /**
     * Read the record bytes and initialize the internal variables
     */
    private void read() {
        int pos = 0;
        indents = new ArrayList<>(_data.length / 6);

        while (pos <= _data.length - 6) {
            int count = LittleEndian.getInt(_data, pos);
            short indent = LittleEndian.getShort(_data, pos+4);
            indents.add(new IndentProp(count, indent));
            pos += 6;
        }
    }

    /**
     * Returns the indent that applies at the given text offset
     */
    public int getIndentAt(int offset) {
        int charsUntil = 0;
        for (IndentProp prop : indents) {
            charsUntil += prop.getCharactersCovered();
            if (offset < charsUntil) {
                return prop.getIndentLevel();
            }
        }
        return -1;
    }

    public List<IndentProp> getIndents() {
        return Collections.unmodifiableList(indents);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "indents", this::getIndents
        );
    }
}
