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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;

/**
 * The special info runs contained in this text.
 * Special info runs consist of character properties which don?t follow styles.
 */
public final class TextSpecInfoAtom extends RecordAtom {

    private static final long _type = RecordTypes.TextSpecInfoAtom.typeID;

    /**
     * Record header.
     */
    private final byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs an empty atom, with a default run of size 1
     */
    public TextSpecInfoAtom() {
        _header = new byte[8];
        LittleEndian.putUInt(_header, 4, _type);
        reset(1);
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    public TextSpecInfoAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the record data.
        _data = IOUtils.safelyClone(source, start+8, len-8, getMaxRecordLength());
    }
    /**
     * Gets the record type.
     * @return the record type.
     */
    @Override
    public long getRecordType() { return _type; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    /**
     * Update the text length
     *
     * @param size the text length
     */
    public void setTextSize(int size){
        LittleEndian.putInt(_data, 0, size);
    }

    /**
     * Reset the content to one info run with the default values
     * @param size  the site of parent text
     */
    public void reset(int size){
        TextSpecInfoRun sir = new TextSpecInfoRun(size);
        UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream();
        try {
            sir.writeOut(bos);
        } catch (IOException e) {
            throw new HSLFException(e);
        }
        _data = bos.toByteArray();

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Adapts the size by enlarging the last {@link TextSpecInfoRun}
     * or chopping the runs to the given length
     */
    public void setParentSize(int size) {
        assert(size > 0);

        try (UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream()) {
            TextSpecInfoRun[] runs = getTextSpecInfoRuns();
            int remaining = size;
            int idx = 0;
            for (TextSpecInfoRun run : runs) {
                int len = run.getLength();
                if (len > remaining || idx == runs.length - 1) {
                    run.setLength(len = remaining);
                }
                remaining -= len;
                run.writeOut(bos);
                idx++;
            }

            _data = bos.toByteArray();

            // Update the size (header bytes 5-8)
            LittleEndian.putInt(_header, 4, _data.length);
        } catch (IOException e) {
            throw new HSLFException(e);
        }
    }

    /**
     * Get the number of characters covered by this records
     *
     * @return the number of characters covered by this records
     */
    public int getCharactersCovered(){
        int covered = 0;
        for (TextSpecInfoRun r : getTextSpecInfoRuns()) {
            covered += r.getLength();
        }
        return covered;
    }

    public TextSpecInfoRun[] getTextSpecInfoRuns(){
        LittleEndianByteArrayInputStream bis = new LittleEndianByteArrayInputStream(_data); // NOSONAR
        List<TextSpecInfoRun> lst = new ArrayList<>();
        while (bis.getReadIndex() < _data.length) {
            lst.add(new TextSpecInfoRun(bis));
        }
        return lst.toArray(new TextSpecInfoRun[0]);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "charactersCovered", this::getCharactersCovered,
            "textSpecInfoRuns", this::getTextSpecInfoRuns
        );
    }
}
