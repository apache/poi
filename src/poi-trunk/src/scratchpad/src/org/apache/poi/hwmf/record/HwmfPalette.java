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

package org.apache.poi.hwmf.record;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfPalette {

    public static class PaletteEntry {
        private static final BitField PC_RESERVED   = BitFieldFactory.getInstance(0x01);
        private static final BitField PC_EXPLICIT   = BitFieldFactory.getInstance(0x02);
        private static final BitField PC_NOCOLLAPSE = BitFieldFactory.getInstance(0x04);

        private int values;
        private Color colorRef;

        private PaletteEntry() {
            this.values = PC_RESERVED.set(0);
            this.colorRef = Color.BLACK;
        }

        private PaletteEntry(PaletteEntry other) {
            this.values = other.values;
            this.colorRef = other.colorRef;
        }

        public int init(LittleEndianInputStream leis) throws IOException {
            // Values (1 byte):  An 8-bit unsigned integer that defines how the palette entry is to be used.
            // The Values field MUST be 0x00 or one of the values in the PaletteEntryFlag Enumeration table.
            values = leis.readUByte();
            // Blue (1 byte): An 8-bit unsigned integer that defines the blue intensity value for the palette entry.
            int blue = leis.readUByte();
            // Green (1 byte): An 8-bit unsigned integer that defines the green intensity value for the palette entry.
            int green = leis.readUByte();
            // Red (1 byte): An 8-bit unsigned integer that defines the red intensity value for the palette entry.
            int red = leis.readUByte();
            colorRef = new Color(red, green, blue);

            return 4*LittleEndianConsts.BYTE_SIZE;
        }

        /**
         * Specifies that the logical palette entry be used for palette animation. This value
         * prevents other windows from matching colors to the palette entry because the color frequently
         * changes. If an unused system-palette entry is available, the color is placed in that entry.
         * Otherwise, the color is not available for animation.
         */
        public boolean isReserved() {
            return PC_RESERVED.isSet(values);
        }

        /**
         * Specifies that the low-order word of the logical palette entry designates a hardware
         * palette index. This value allows the application to show the contents of the display device palette.
         */
        public boolean isExplicit() {
            return PC_EXPLICIT.isSet(values);
        }

        /**
         * Specifies that the color be placed in an unused entry in the system palette
         * instead of being matched to an existing color in the system palette. If there are no unused entries
         * in the system palette, the color is matched normally. Once this color is in the system palette,
         * colors in other logical palettes can be matched to this color.
         */
        public boolean isNoCollapse() {
            return PC_NOCOLLAPSE.isSet(values);
        }
    }

    public static abstract class WmfPaletteParent implements HwmfRecord, HwmfObjectTableEntry  {

        /**
         * Start (2 bytes):  A 16-bit unsigned integer that defines the offset into the Palette Object when
         * used with the META_SETPALENTRIES and META_ANIMATEPALETTE record types.
         * When used with META_CREATEPALETTE, it MUST be 0x0300
         */
        private int start;

        private List<PaletteEntry> palette = new ArrayList<>();

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            start = leis.readUShort();
            /**
             * NumberOfEntries (2 bytes):  A 16-bit unsigned integer that defines the number of objects in
             * aPaletteEntries.
             */
            int numberOfEntries = leis.readUShort();
            int size = 2*LittleEndianConsts.SHORT_SIZE;
            for (int i=0; i<numberOfEntries; i++) {
                PaletteEntry pe = new PaletteEntry();
                size += pe.init(leis);
                palette.add(pe);
            }
            return size;
        }

        @Override
        public final void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        protected List<PaletteEntry> getPaletteCopy() {
            List<PaletteEntry> newPalette = new ArrayList<>();
            for (PaletteEntry et : palette) {
                newPalette.add(new PaletteEntry(et));
            }
            return newPalette;
        }

        protected int getPaletteStart() {
            return start;
        }
    }

    /**
     * The META_CREATEPALETTE record creates a Palette Object
     */
    public static class WmfCreatePalette extends WmfPaletteParent implements HwmfObjectTableEntry {
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createPalette;
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            ctx.getProperties().setPalette(getPaletteCopy());
        }
    }

    /**
     * The META_SETPALENTRIES record defines RGB color values in a range of entries in the logical
     * palette that is defined in the playback device context.
     */
    public static class WmfSetPaletteEntries extends WmfPaletteParent {
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setPalEntries;
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties props = ctx.getProperties();
            List<PaletteEntry> palette = props.getPalette();
            if (palette == null) {
                palette = new ArrayList<>();
            }
            int start = getPaletteStart();
            for (int i=palette.size(); i<start; i++) {
                palette.add(new PaletteEntry());
            }
            int index = start;
            for (PaletteEntry palCopy : getPaletteCopy()) {
                if (palette.size() <= index) {
                    palette.add(palCopy);
                } else {
                    palette.set(index, palCopy);
                }
                index++;
            }
            props.setPalette(palette);
        }
    }

    /**
     * The META_RESIZEPALETTE record redefines the size of the logical palette that is defined in the
     * playback device context.
     */
    public static class WmfResizePalette implements HwmfRecord, HwmfObjectTableEntry {
        /**
         * A 16-bit unsigned integer that defines the number of entries in
         * the logical palette.
         */
        int numberOfEntries;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.resizePalette;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            numberOfEntries = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties props = ctx.getProperties();
            List<PaletteEntry> palette = props.getPalette();
            if (palette == null) {
                palette = new ArrayList<>();
            }
            for (int i=palette.size(); i<numberOfEntries; i++) {
                palette.add(new PaletteEntry());
            }
            palette = palette.subList(0, numberOfEntries);
            props.setPalette(palette);
        }
    }

    /**
     * The META_SELECTPALETTE record defines the current logical palette with a specified Palette Object.
     */
    public static class WmfSelectPalette implements HwmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the Palette Object to be selected.
         */
        private int paletteIndex;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.selectPalette;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            paletteIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(paletteIndex);
        }
    }

    /**
     * The META_REALIZEPALETTE record maps entries from the logical palette that
     * is defined in the playback device context to the system palette.
     */
    public static class WmfRealizePalette implements HwmfRecord {
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.realizePalette;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_ANIMATEPALETTE record redefines entries in the logical palette that
     * is defined in the playback device context with the specified Palette object
     *
     * The logical palette that is specified by the Palette object in this record is the
     * source of the palette changes, and the logical palette that is currently selected
     * into the playback device context is the destination. Entries in the destination
     * palette with the PC_RESERVED PaletteEntryFlag set SHOULD be modified by this record,
     * and entries with that flag clear SHOULD NOT be modified.
     * If none of the entries in the destination palette have the PC_RESERVED flag set, then
     * this record SHOULD have no effect.
     */
    public static class WmfAnimatePalette extends WmfPaletteParent {
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.animatePalette;
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties props = ctx.getProperties();
            List<PaletteEntry> dest = props.getPalette();
            List<PaletteEntry> src = getPaletteCopy();
            int start = getPaletteStart();
            if (dest == null) {
                dest = new ArrayList<>();
            }
            for (int i=dest.size(); i<start; i++) {
                dest.add(new PaletteEntry());
            }
            for (int i=0; i<src.size(); i++) {
                PaletteEntry pe = src.get(i);
                if (dest.size() <= start+i) {
                    dest.add(pe);
                } else {
                    PaletteEntry peDst = dest.get(start+i);
                    if (peDst.isReserved()) {
                        dest.set(start+i, pe);
                    }
                }
            }
            props.setPalette(dest);
        }
    }
}
