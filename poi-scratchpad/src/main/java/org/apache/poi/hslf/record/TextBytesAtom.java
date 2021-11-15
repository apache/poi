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

import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * A TextBytesAtom (type 4008). Holds text in ascii form (unknown
 *  code page, for now assumed to be the default of
 *  org.apache.poi.util.StringUtil, which is the Excel default).
 * The trailing return character is always stripped from this
 */

public final class TextBytesAtom extends RecordAtom {
    public static final long _type = RecordTypes.TextBytesAtom.typeID;

    private byte[] _header;

    /** The bytes that make up the text */
    private byte[] _text;

    /** Grabs the text. Uses the default codepage */
    public String getText() {
        return StringUtil.getFromCompressedUnicode(_text,0,_text.length);
    }

    /** Updates the text in the Atom. Must be 8 bit ascii */
    public void setText(byte[] b) {
        // Set the text
        _text = b.clone();

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header,4,_text.length);
    }

    /* *************** record code follows ********************** */

    /**
     * For the TextBytes Atom
     */
    protected TextBytesAtom(byte[] source, int start, int len) {
        // Sanity Checking
        if(len < 8) { len = 8; }

        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Grab the text
        _text = IOUtils.safelyClone(source, start+8, len-8, getMaxRecordLength());
    }

    /**
     * Create an empty TextBytes Atom
     */
    public TextBytesAtom() {
        _header = new byte[8];
        LittleEndian.putUShort(_header, 0, 0);
        LittleEndian.putUShort(_header, 2, (int)_type);
        LittleEndian.putInt(_header, 4, 0);

        _text = new byte[]{};
    }

    /**
     * We are of type 4008
     */
    @Override
    public long getRecordType() { return _type; }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        // Header - size or type unchanged
        out.write(_header);

        // Write out our text
        out.write(_text);
    }

    /**
     * dump debug info; use getText() to return a string
     * representation of the atom
     */
    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "text", this::getText
        );
    }
}
