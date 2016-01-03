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

import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfPalette {
    
    public static class PaletteEntry {
        // Values (1 byte):  An 8-bit unsigned integer that defines how the palette entry is to be used. 
        // The Values field MUST be 0x00 or one of the values in the PaletteEntryFlag Enumeration table.
        // Blue (1 byte): An 8-bit unsigned integer that defines the blue intensity value for the palette entry.
        // Green (1 byte): An 8-bit unsigned integer that defines the green intensity value for the palette entry.
        // Red (1 byte): An 8-bit unsigned integer that defines the red intensity value for the palette entry.
        private int values, blue, green, red;
        
        public int init(LittleEndianInputStream leis) throws IOException {
            values = leis.readUByte();
            blue = leis.readUByte();
            green = leis.readUByte();
            red = leis.readUByte();
            return 4*LittleEndianConsts.BYTE_SIZE;
        }
    }
    
    public static abstract class WmfPaletteParent implements HwmfRecord  {
    
        /**
         * Start (2 bytes):  A 16-bit unsigned integer that defines the offset into the Palette Object when
         * used with the META_SETPALENTRIES and META_ANIMATEPALETTE record types.
         * When used with META_CREATEPALETTE, it MUST be 0x0300
         */
        private int start;
        
        /**
         * NumberOfEntries (2 bytes):  A 16-bit unsigned integer that defines the number of objects in
         * aPaletteEntries.  
         */
        private int numberOfEntries;
        
        PaletteEntry entries[];
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            start = leis.readUShort();
            numberOfEntries = leis.readUShort();
            int size = 2*LittleEndianConsts.SHORT_SIZE;
            entries = new PaletteEntry[numberOfEntries];
            for (int i=0; i<numberOfEntries; i++) {
                entries[i] = new PaletteEntry();
                size += entries[i].init(leis);
            }
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

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
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {

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
        public void draw(HwmfGraphics ctx) {

        }
    }
    
    /**
     * The META_RESIZEPALETTE record redefines the size of the logical palette that is defined in the
     * playback device context.
     */
    public static class WmfResizePalette implements HwmfRecord {
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
        private int palette;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.selectPalette;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            palette = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }        

        @Override
        public void draw(HwmfGraphics ctx) {

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
        public void draw(HwmfGraphics ctx) {

        }
    }
}
