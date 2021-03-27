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

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Supports custom palettes.
 */
public final class PaletteRecord extends StandardRecord {
    public static final short sid = 0x0092;
    /** The standard size of an XLS palette */
    public static final byte STANDARD_PALETTE_SIZE = (byte) 56;
    /** The byte index of the first color */
    public static final short FIRST_COLOR_INDEX = (short) 0x8;

    private static final int[] DEFAULT_COLORS = {
        0x000000, 0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF,
        0x800000, 0x008000, 0x000080, 0x808000, 0x800080, 0x008080, 0xC0C0C0, 0x808080,
        0x9999FF, 0x993366, 0xFFFFCC, 0xCCFFFF, 0x660066, 0xFF8080, 0x0066CC, 0xCCCCFF,
        0x000080, 0xFF00FF, 0xFFFF00, 0x00FFFF, 0x800080, 0x800000, 0x008080, 0x0000FF,
        0x00CCFF, 0xCCFFFF, 0xCCFFCC, 0xFFFF99, 0x99CCFF, 0xFF99CC, 0xCC99FF, 0xFFCC99,
        0x3366FF, 0x33CCCC, 0x99CC00, 0xFFCC00, 0xFF9900, 0xFF6600, 0x666699, 0x969696,
        0x003366, 0x339966, 0x003300, 0x333300, 0x993300, 0x993366, 0x333399, 0x333333
    };

    private final ArrayList<PColor> _colors = new ArrayList<>(100);

    public PaletteRecord() {
        Arrays.stream(DEFAULT_COLORS).mapToObj(PColor::new).forEach(_colors::add);
    }

    public PaletteRecord(PaletteRecord other) {
        super(other);
        _colors.ensureCapacity(other._colors.size());
        other._colors.stream().map(PColor::new).forEach(_colors::add);
    }

    public PaletteRecord(RecordInputStream in) {
       int field_1_numcolors = in.readShort();
       _colors.ensureCapacity(field_1_numcolors);
       for (int k = 0; k < field_1_numcolors; k++) {
           _colors.add(new PColor(in));
       }
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(_colors.size());
        for (PColor color : _colors) {
            color.serialize(out);
        }
    }

    @Override
    protected int getDataSize() {
        return 2 + _colors.size() * PColor.ENCODED_SIZE;
    }

    @Override
    public short getSid() {
        return sid;
    }

    /**
     * Returns the color value at a given index
     *
     * @param byteIndex palette index, must be &gt;= 0x8
     *
     * @return the RGB triplet for the color, or <code>null</code> if the specified index
     * does not exist
     */
    public byte[] getColor(int byteIndex) {
        int i = byteIndex - FIRST_COLOR_INDEX;
        if (i < 0 || i >= _colors.size()) {
            return null;
        }
        return _colors.get(i).getTriplet();
    }

    /**
     * Sets the color value at a given index
     *
     * If the given index is greater than the current last color index,
     * then black is inserted at every index required to make the palette continuous.
     *
     * @param byteIndex the index to set; if this index is less than 0x8 or greater than
     * 0x40, then no modification is made
     * @param red the red color part
     * @param green the green color part
     * @param blue the blue color part
     */
    public void setColor(short byteIndex, byte red, byte green, byte blue)
    {
        int i = byteIndex - FIRST_COLOR_INDEX;
        if (i < 0 || i >= STANDARD_PALETTE_SIZE)
        {
            return;
        }
        // may need to grow - fill intervening palette entries with black
        while (_colors.size() <= i) {
            _colors.add(new PColor(0, 0, 0));
        }
        PColor custColor = new PColor(red, green, blue);
        _colors.set(i, custColor);
    }

    @Override
    public PaletteRecord copy() {
        return new PaletteRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PALETTE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("colors", () -> _colors);
    }

    /**
     * PColor - element in the list of colors
     */
    private static final class PColor implements GenericRecord {
        public static final short ENCODED_SIZE = 4;
        private final int _red;
        private final int _green;
        private final int _blue;

        PColor(int rgb) {
            _red = (rgb >>> 16) & 0xFF;
            _green = (rgb >>> 8) & 0xFF;
            _blue = rgb & 0xFF;
        }

        PColor(int red, int green, int blue) {
            _red = red;
            _green = green;
            _blue = blue;
        }

        PColor(PColor other) {
            _red = other._red;
            _green = other._green;
            _blue = other._blue;
        }

        PColor(RecordInputStream in) {
            _red = in.readByte();
            _green = in.readByte();
            _blue = in.readByte();
            in.readByte(); // unused
        }

        byte[] getTriplet() {
            return new byte[] { (byte) _red, (byte) _green, (byte) _blue };
        }

        void serialize(LittleEndianOutput out) {
            out.writeByte(_red);
            out.writeByte(_green);
            out.writeByte(_blue);
            out.writeByte(0);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "red", () -> _red & 0xFF,
                "green", () -> _green & 0xFF,
                "blue", () -> _blue & 0xFF
            );
        }
    }
}
