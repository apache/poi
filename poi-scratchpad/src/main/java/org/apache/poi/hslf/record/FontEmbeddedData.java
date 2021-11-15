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

import org.apache.poi.common.usermodel.fonts.FontFacet;
import org.apache.poi.common.usermodel.fonts.FontHeader;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

@SuppressWarnings("WeakerAccess")
public class FontEmbeddedData extends RecordAtom implements FontFacet {
    //arbitrarily selected; may need to increase (increased due to https://bz.apache.org/bugzilla/show_bug.cgi?id=65639)
    private static final int DEFAULT_MAX_RECORD_LENGTH = 5_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data - An EOT Font
     */
    private byte[] _data;

    /**
     * A cached FontHeader so that we don't keep creating new FontHeader instances
     */
    private FontHeader fontHeader;

    /**
     * @param length the max record length allowed for FontEmbeddedData
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for FontEmbeddedData
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Constructs a brand new font embedded record.
     */
    /* package */ FontEmbeddedData() {
        _header = new byte[8];
        _data = new byte[4];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Constructs the font embedded record from its source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    /* package */ FontEmbeddedData(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the record data.
        _data = IOUtils.safelyClone(source, start+8, len-8, MAX_RECORD_LENGTH);

        // Must be at least 4 bytes long
        if(_data.length < 4) {
            throw new IllegalArgumentException("The length of the data for a ExObjListAtom must be at least 4 bytes, but was only " + _data.length);
        }
    }

    @Override
    public long getRecordType() {
        return RecordTypes.FontEmbeddedData.typeID;
    }

    @Override
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    /**
     * Overwrite the font data. Reading values from this FontEmbeddedData instance while calling setFontData
     * is not thread safe.
     * @param fontData new font data
     */
    public void setFontData(byte[] fontData) {
        fontHeader = null;
        _data = fontData.clone();
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Read the font data. Reading values from this FontEmbeddedData instance while calling {@link #setFontData(byte[])}
     * is not thread safe.
     * @return font data
     */
    public FontHeader getFontHeader() {
        if (fontHeader == null) {
            FontHeader h = new FontHeader();
            h.init(_data, 0, _data.length);
            fontHeader = h;
        }
        return fontHeader;
    }

    @Override
    public int getWeight() {
        return getFontHeader().getWeight();
    }

    @Override
    public boolean isItalic() {
        return getFontHeader().isItalic();
    }

    public String getTypeface() {
        return getFontHeader().getFamilyName();
    }

    @Override
    public Object getFontData() {
        return this;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("fontHeader", this::getFontHeader);
    }
}
