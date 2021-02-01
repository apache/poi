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

package org.apache.poi.ddf;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

/**
 * An OfficeArtCOLORREF structure entry which also handles color extension opid data
 */
public class EscherColorRef {
    @SuppressWarnings("unused")
    private int opid = -1;
    private int colorRef;

    public enum SysIndexSource {
        /** Use the fill color of the shape. */
        FILL_COLOR(0xF0),
        /** If the shape contains a line, use the line color of the shape. Otherwise, use the fill color. */
        LINE_OR_FILL_COLOR(0xF1),
        /** Use the line color of the shape. */
        LINE_COLOR(0xF2),
        /** Use the shadow color of the shape. */
        SHADOW_COLOR(0xF3),
        /** Use the current, or last-used, color. */
        CURRENT_OR_LAST_COLOR(0xF4),
        /** Use the fill background color of the shape. */
        FILL_BACKGROUND_COLOR(0xF5),
        /** Use the line background color of the shape. */
        LINE_BACKGROUND_COLOR(0xF6),
        /** If the shape contains a fill, use the fill color of the shape. Otherwise, use the line color. */
        FILL_OR_LINE_COLOR(0xF7)
        ;
        private int value;
        SysIndexSource(int value) { this.value = value; }
    }

    /**
     * The following enum specifies values that indicate special procedural properties that
     * are used to modify the color components of another color. These values are combined with
     * those of the {@link SysIndexSource} enum or with a user-specified color.
     * The first six values are mutually exclusive.
     */
    public enum SysIndexProcedure {
        /**
         * Darken the color by the value that is specified in the blue field.
         * A blue value of 0xFF specifies that the color is to be left unchanged,
         * whereas a blue value of 0x00 specifies that the color is to be completely darkened.
         */
        DARKEN_COLOR(0x01),
        /**
         * Lighten the color by the value that is specified in the blue field.
         * A blue value of 0xFF specifies that the color is to be left unchanged,
         * whereas a blue value of 0x00 specifies that the color is to be completely lightened.
         */
        LIGHTEN_COLOR(0x02),
        /**
         * Add a gray level RGB value. The blue field contains the gray level to add:
         * NewColor = SourceColor + gray
         */
        ADD_GRAY_LEVEL(0x03),
        /**
         * Subtract a gray level RGB value. The blue field contains the gray level to subtract:
         * NewColor = SourceColor - gray
         */
        SUB_GRAY_LEVEL(0x04),
        /**
         * Reverse-subtract a gray level RGB value. The blue field contains the gray level from
         * which to subtract:
         * NewColor = gray - SourceColor
         */
        REVERSE_GRAY_LEVEL(0x05),
        /**
         * If the color component being modified is less than the parameter contained in the blue
         * field, set it to the minimum intensity. If the color component being modified is greater
         * than or equal to the parameter, set it to the maximum intensity.
         */
        THRESHOLD(0x06),
        /**
         * After making other modifications, invert the color.
         * This enum value is only for documentation and won't be directly returned.
         */
        INVERT_AFTER(0x20),
        /**
         * After making other modifications, invert the color by toggling just the high bit of each
         * color channel.
         * This enum value is only for documentation and won't be directly returned.
         */
        INVERT_HIGHBIT_AFTER(0x40)
        ;
        private BitField mask;
        SysIndexProcedure(int mask) {
            this.mask = new BitField(mask);
        }
    }
    
    /**
     * A bit that specifies whether the system color scheme will be used to determine the color. 
     * A value of 0x1 specifies that green and red will be treated as an unsigned 16-bit index
     * into the system color table. Values less than 0x00F0 map directly to system colors.
     */
    private static final BitField FLAG_SYS_INDEX     = new BitField(0x10000000);

    /**
     * A bit that specifies whether the current application-defined color scheme will be used
     * to determine the color. A value of 0x1 specifies that red will be treated as an index
     * into the current color scheme table. If this value is 0x1, green and blue MUST be 0x00.
     */
    private static final BitField FLAG_SCHEME_INDEX  = new BitField(0x08000000);
    
    /**
     * A bit that specifies whether the color is a standard RGB color.
     * 0x0 : The RGB color MAY use halftone dithering to display.
     * 0x1 : The color MUST be a solid color.
     */
    private static final BitField FLAG_SYSTEM_RGB    = new BitField(0x04000000);
    
    /**
     * A bit that specifies whether the current palette will be used to determine the color.
     * A value of 0x1 specifies that red, green, and blue contain an RGB value that will be
     * matched in the current color palette. This color MUST be solid.
     */
    private static final BitField FLAG_PALETTE_RGB   = new BitField(0x02000000);

    /**
     * A bit that specifies whether the current palette will be used to determine the color.
     * A value of 0x1 specifies that green and red will be treated as an unsigned 16-bit index into 
     * the current color palette. This color MAY be dithered. If this value is 0x1, blue MUST be 0x00.
     */
    private static final BitField FLAG_PALETTE_INDEX = new BitField(0x01000000);
    
    /**
     * An unsigned integer that specifies the intensity of the blue color channel. A value
     * of 0x00 has the minimum blue intensity. A value of 0xFF has the maximum blue intensity.
     */
    private static final BitField FLAG_BLUE          = new BitField(0x00FF0000);
    
    /**
     * An unsigned integer that specifies the intensity of the green color channel. A value
     * of 0x00 has the minimum green intensity. A value of 0xFF has the maximum green intensity.
     */
    private static final BitField FLAG_GREEN         = new BitField(0x0000FF00);
    
    /**
     * An unsigned integer that specifies the intensity of the red color channel. A value
     * of 0x00 has the minimum red intensity. A value of 0xFF has the maximum red intensity.
     */
    private static final BitField FLAG_RED           = new BitField(0x000000FF);
    
    public EscherColorRef(int colorRef) {
        this.colorRef = colorRef;
    }
    
    public EscherColorRef(byte[] source, int start, int len) {
        assert(len == 4 || len == 6);
        
        int offset = start;
        if (len == 6) {
            opid = LittleEndian.getUShort(source, offset);
            offset += 2;
        }
        colorRef = LittleEndian.getInt(source, offset);
    }
    
    public boolean hasSysIndexFlag() {
        return FLAG_SYS_INDEX.isSet(colorRef);
    }
    
    public void setSysIndexFlag(boolean flag) {
        colorRef = FLAG_SYS_INDEX.setBoolean(colorRef, flag);
    }
    
    public boolean hasSchemeIndexFlag() {
        return FLAG_SCHEME_INDEX.isSet(colorRef);
    }
    
    public void setSchemeIndexFlag(boolean flag) {
        colorRef = FLAG_SCHEME_INDEX.setBoolean(colorRef, flag);
    }
    
    public boolean hasSystemRGBFlag() {
        return FLAG_SYSTEM_RGB.isSet(colorRef);
    }
    
    public void setSystemRGBFlag(boolean flag) {
        colorRef = FLAG_SYSTEM_RGB.setBoolean(colorRef, flag);
    }
    
    public boolean hasPaletteRGBFlag() {
        return FLAG_PALETTE_RGB.isSet(colorRef);
    }
    
    public void setPaletteRGBFlag(boolean flag) {
        colorRef = FLAG_PALETTE_RGB.setBoolean(colorRef, flag);
    }
    
    public boolean hasPaletteIndexFlag() {
        return FLAG_PALETTE_INDEX.isSet(colorRef);
    }
    
    public void setPaletteIndexFlag(boolean flag) {
        colorRef = FLAG_PALETTE_INDEX.setBoolean(colorRef, flag);
    }

    public int[] getRGB() {
        return new int[]{
            FLAG_RED.getValue(colorRef),
            FLAG_GREEN.getValue(colorRef),
            FLAG_BLUE.getValue(colorRef)
        };
    }
    
    /**
     * @return {@link SysIndexSource} if {@link #hasSysIndexFlag()} is {@code true}, otherwise null
     */
    public SysIndexSource getSysIndexSource() {
        if (!hasSysIndexFlag()) {
            return null;
        }
        int val = FLAG_RED.getValue(colorRef);
        for (SysIndexSource sis : SysIndexSource.values()) {
            if (sis.value == val) {
                return sis;
            }
        }
        return null;
    }
    
    /**
     * Return the {@link SysIndexProcedure} - for invert flag use {@link #getSysIndexInvert()}
     * @return {@link SysIndexProcedure} if {@link #hasSysIndexFlag()} is {@code true}, otherwise null
     */
    public SysIndexProcedure getSysIndexProcedure() {
        if (!hasSysIndexFlag()) {
            return null;
        }
        int val = FLAG_GREEN.getValue(colorRef);
        for (SysIndexProcedure sip : SysIndexProcedure.values()) {
            if (sip == SysIndexProcedure.INVERT_AFTER || sip == SysIndexProcedure.INVERT_HIGHBIT_AFTER) {
                continue;
            }
            if (sip.mask.isSet(val)) {
                return sip;
            }
        }
        return null;
    }
    
    /**
     * @return 0 for no invert flag, 1 for {@link SysIndexProcedure#INVERT_AFTER} and
     * 2 for {@link SysIndexProcedure#INVERT_HIGHBIT_AFTER} 
     */
    public int getSysIndexInvert() {
        if (!hasSysIndexFlag()) {
            return 0;
        }
        int val = FLAG_GREEN.getValue(colorRef);
        if ((SysIndexProcedure.INVERT_AFTER.mask.isSet(val))) {
            return 1;
        }
        if ((SysIndexProcedure.INVERT_HIGHBIT_AFTER.mask.isSet(val))) {
            return 2;
        }
        return 0;
    }
    
    /**
     * @return index of the scheme color or -1 if {@link #hasSchemeIndexFlag()} is {@code false}
     * 
     * @see org.apache.poi.hslf.record.ColorSchemeAtom#getColor(int)
     */
    public int getSchemeIndex() {
        if (!hasSchemeIndexFlag()) {
            return -1;
        }
        return FLAG_RED.getValue(colorRef);
    }
    
    /**
     * @return index of current palette (color) or -1 if {@link #hasPaletteIndexFlag()} is {@code false}
     */
    public int getPaletteIndex() {
        return (hasPaletteIndexFlag()) ? getIndex() : -1;
    }

    /**
     * @return index of system color table or -1 if {@link #hasSysIndexFlag()} is {@code false}
     * 
     * @see org.apache.poi.sl.usermodel.PresetColor
     */
    public int getSysIndex() {
        return (hasSysIndexFlag()) ? getIndex() : -1;
    }
    
    private int getIndex() {
        return (FLAG_GREEN.getValue(colorRef) << 8) | FLAG_RED.getValue(colorRef);
    }
}
