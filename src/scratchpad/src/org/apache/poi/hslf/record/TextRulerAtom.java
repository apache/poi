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

import static org.apache.poi.util.BitFieldFactory.getInstance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hslf.model.textproperties.HSLFTabStop;
import org.apache.poi.hslf.model.textproperties.HSLFTabStopPropCollection;
import org.apache.poi.util.BitField;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianOutputStream;
import org.apache.poi.util.POILogger;

/**
 * Ruler of a text as it differs from the style's ruler settings.
 */
public final class TextRulerAtom extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;
    
    private static final BitField DEFAULT_TAB_SIZE = getInstance(0x0001);
    private static final BitField C_LEVELS = getInstance(0x0002);
    private static final BitField TAB_STOPS = getInstance(0x0004);
    private static final BitField[] LEFT_MARGIN = {
        getInstance(0x0008), getInstance(0x0010), getInstance(0x0020),
        getInstance(0x0040), getInstance(0x0080),
    };
    private static final BitField[] INDENT = {
        getInstance(0x0100), getInstance(0x0200), getInstance(0x0400),
        getInstance(0x0800), getInstance(0x1000),
    };

    /**
     * Record header.
     */
    private final byte[] _header = new byte[8];

    //ruler internals
    private Integer defaultTabSize;
    private Integer numLevels;
    private final List<HSLFTabStop> tabStops = new ArrayList<>();
    //bullet.offset
    private final Integer[] leftMargin = new Integer[5];
    //text.offset
    private final Integer[] indent = new Integer[5];

    /**
     * Constructs a new empty ruler atom.
     */
    public TextRulerAtom() {
        LittleEndian.putShort(_header, 2, (short)getRecordType());
    }

    /**
     * Constructs the ruler atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected TextRulerAtom(final byte[] source, final int start, final int len) {
        final LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream(source, start, Math.min(len, MAX_RECORD_LENGTH));
        

        try {
            // Get the header.
            IOUtils.readFully(leis, _header);

            // Get the record data.
            read(leis);
        } catch (IOException e){
            logger.log(POILogger.ERROR, "Failed to parse TextRulerAtom: " + e.getMessage());
        }
    }

    /**
     * Gets the record type.
     *
     * @return the record type.
     */
    @Override
    public long getRecordType() {
        return RecordTypes.TextRulerAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    @Override
    public void writeOut(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(200);
        final LittleEndianOutputStream lbos = new LittleEndianOutputStream(bos);
        int mask = 0;
        mask |= writeIf(lbos, numLevels, C_LEVELS);
        mask |= writeIf(lbos, defaultTabSize, DEFAULT_TAB_SIZE);
        mask |= writeIf(lbos, tabStops, TAB_STOPS);
        for (int i=0; i<5; i++) {
            mask |= writeIf(lbos, leftMargin[i], LEFT_MARGIN[i]);
            mask |= writeIf(lbos, indent[i], INDENT[i]);
        }
        LittleEndian.putInt(_header, 4, bos.size()+4);
        out.write(_header);
        LittleEndian.putUShort(mask, out);
        LittleEndian.putUShort(0, out);
        bos.writeTo(out);
    }

    private static int writeIf(final LittleEndianOutputStream lbos, Integer value, BitField bit) {
        boolean isSet = false;
        if (value != null) {
            lbos.writeShort(value);
            isSet = true;
        }
        return bit.setBoolean(0, isSet);
    }
    
    private static int writeIf(final LittleEndianOutputStream lbos, List<HSLFTabStop> value, BitField bit) {
        boolean isSet = false;
        if (value != null && !value.isEmpty()) {
            HSLFTabStopPropCollection.writeTabStops(lbos, value);
            isSet = true;
        }
        return bit.setBoolean(0, isSet);
    }
    
    /**
     * Read the record bytes and initialize the internal variables
     */
    private void read(final LittleEndianByteArrayInputStream leis) {
        final int mask = leis.readInt();
        numLevels = readIf(leis, mask, C_LEVELS);
        defaultTabSize = readIf(leis, mask, DEFAULT_TAB_SIZE);
        if (TAB_STOPS.isSet(mask)) {
            tabStops.addAll(HSLFTabStopPropCollection.readTabStops(leis));
        }
        for (int i=0; i<5; i++) {
            leftMargin[i] = readIf(leis, mask, LEFT_MARGIN[i]);
            indent[i] = readIf(leis, mask, INDENT[i]);
        }
    }

    private static Integer readIf(final LittleEndianByteArrayInputStream leis, final int mask, final BitField bit) {
        return (bit.isSet(mask)) ? (int)leis.readShort() : null;
    }    
    
    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    public int getDefaultTabSize(){
        return defaultTabSize == null ? 0 : defaultTabSize;
    }

    /**
     * Number of indent levels (maximum 5).
     */
    public int getNumberOfLevels(){
        return numLevels == null ? 0 : numLevels;
    }

    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    public List<HSLFTabStop> getTabStops(){
        return tabStops;
    }

    /**
     * Paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    public Integer[] getTextOffsets(){
        return indent;
    }

    /**
     * First line of paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    public Integer[] getBulletOffsets(){
        return leftMargin;
    }

    public static TextRulerAtom getParagraphInstance(){
        final TextRulerAtom tra = new TextRulerAtom();
        tra.indent[0] = 249;
        tra.indent[1] = tra.leftMargin[1] = 321;
        return tra;
    }

    public void setParagraphIndent(short leftMargin, short indent) {
        Arrays.fill(this.leftMargin, null);
        Arrays.fill(this.indent, null);
        this.leftMargin[0] = (int)leftMargin;
        this.indent[0] = (int)indent;
        this.indent[1] = (int)indent;
    }
}
