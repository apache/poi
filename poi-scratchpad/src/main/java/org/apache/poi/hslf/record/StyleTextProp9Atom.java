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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hslf.model.textproperties.TextPFException9;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * The atom record that specifies additional text formatting.
 */
public final class StyleTextProp9Atom extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private final TextPFException9[] autoNumberSchemes;
    /** Record header. */
    private byte[] header;
    /** Record data. */
    private byte[] data;
    private short version;
    private short recordId;
    private int length;

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected StyleTextProp9Atom(byte[] source, int start, int len) {
        // Get the header.
        final List<TextPFException9> schemes = new LinkedList<>();
        header = Arrays.copyOfRange(source, start, start+8);
        this.version  = LittleEndian.getShort(header, 0);
        this.recordId = LittleEndian.getShort(header, 2);
        this.length   = LittleEndian.getInt(header, 4);

        // Get the record data.
        data = IOUtils.safelyClone(source,  start+8, len-8, MAX_RECORD_LENGTH);
        for (int i = 0; i < data.length; ) {
            final TextPFException9 item = new TextPFException9(data, i);
            schemes.add(item);
            i += item.getRecordLength();

            if (i+4 >= data.length) {
                break;
            }
            int textCfException9 = LittleEndian.getInt(data, i );
            i += 4;
            //TODO analyze textCfException when have some test data

            if (i+4 >= data.length) {
                break;
            }
            int textSiException = LittleEndian.getInt(data, i );
            i += 4;//TextCFException9 + SIException

            if (0 != (textSiException & 0x40)) {
                i += 2; //skip fBidi
            }
            if (i+4 >= data.length) {
                break;
            }
        }
        this.autoNumberSchemes = schemes.toArray(new TextPFException9[0]);
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return this.recordId; }

    public short getVersion() {
        return version;
    }

    public int getLength() {
        return length;
    }
    public TextPFException9[] getAutoNumberTypes() {
        return this.autoNumberSchemes;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(header);
        out.write(data);
    }

    /**
     * Update the text length
     *
     * @param size the text length
     */
    public void setTextSize(int size){
        LittleEndian.putInt(data, 0, size);
    }

    /**
     * Reset the content to one info run with the default values
     * @param size  the site of parent text
     */
    public void reset(int size){
        data = new byte[10];
        // 01 00 00 00
        LittleEndian.putInt(data, 0, size);
        // 01 00 00 00
        LittleEndian.putInt(data, 4, 1); //mask
        // 00 00
        LittleEndian.putShort(data, 8, (short)0); //langId

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(header, 4, data.length);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "autoNumberSchemes", this::getAutoNumberTypes
        );
    }
}
