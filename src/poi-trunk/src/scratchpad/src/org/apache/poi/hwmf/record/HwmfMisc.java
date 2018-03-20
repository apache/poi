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

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfFill.ColorUsage;
import org.apache.poi.hwmf.record.HwmfFill.HwmfImageRecord;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfMisc {

    /**
     * The META_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class WmfSaveDc implements HwmfRecord {
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.saveDc;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.saveProperties();
        }
    }

    /**
     * The META_SETRELABS record is reserved and not supported.
     */
    public static class WmfSetRelabs implements HwmfRecord {
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setRelabs;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_RESTOREDC record restores the playback device context from a previously saved device
     * context.
     */
    public static class WmfRestoreDc implements HwmfRecord {

        /**
         * nSavedDC (2 bytes):  A 16-bit signed integer that defines the saved state to be restored. If this
         * member is positive, nSavedDC represents a specific instance of the state to be restored. If
         * this member is negative, nSavedDC represents an instance relative to the current state.
         */
        private int nSavedDC;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.restoreDc;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            nSavedDC = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.restoreProperties(nSavedDC);
        }
    }

    /**
     * The META_SETBKCOLOR record sets the background color in the playback device context to a
     * specified color, or to the nearest physical color if the device cannot represent the specified color.
     */
    public static class WmfSetBkColor implements HwmfRecord {

        private HwmfColorRef colorRef;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setBkColor;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new HwmfColorRef();
            return colorRef.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setBackgroundColor(colorRef);
        }
    }

    /**
     * The META_SETBKMODE record defines the background raster operation mix mode in the playback
     * device context. The background mix mode is the mode for combining pens, text, hatched brushes,
     * and interiors of filled objects with background colors on the output surface.
     */
    public static class WmfSetBkMode implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines background mix mode.
         */
        public enum HwmfBkMode {
            TRANSPARENT(0x0001), OPAQUE(0x0002);

            int flag;
            HwmfBkMode(int flag) {
                this.flag = flag;
            }

            static HwmfBkMode valueOf(int flag) {
                for (HwmfBkMode bs : values()) {
                    if (bs.flag == flag) return bs;
                }
                return null;
            }
        }

        private HwmfBkMode bkMode;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setBkMode;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bkMode = HwmfBkMode.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setBkMode(bkMode);
        }
    }

    /**
     * The META_SETLAYOUT record defines the layout orientation in the playback device context.
     * The layout orientation determines the direction in which text and graphics are drawn
     */
    public static class WmfSetLayout implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines the layout of text and graphics.
         * LAYOUT_LTR = 0x0000
         * LAYOUT_RTL = 0x0001
         * LAYOUT_BITMAPORIENTATIONPRESERVED = 0x0008
         */
        private int layout;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setLayout;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            layout = leis.readUShort();
            // A 16-bit field that MUST be ignored.
            /*int reserved =*/ leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_SETMAPMODE record defines the mapping mode in the playback device context.
     * The mapping mode defines the unit of measure used to transform page-space units into
     * device-space units, and also defines the orientation of the device's x and y axes.
     */
    public static class WmfSetMapMode implements HwmfRecord {

        private HwmfMapMode mapMode;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setMapMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapMode = HwmfMapMode.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setMapMode(mapMode);
            ctx.updateWindowMapMode();
        }
    }

    /**
     * The META_SETMAPPERFLAGS record defines the algorithm that the font mapper uses when it maps
     * logical fonts to physical fonts.
     */
    public static class WmfSetMapperFlags implements HwmfRecord {

        /**
         * A 32-bit unsigned integer that defines whether the font mapper should attempt to
         * match a font's aspect ratio to the current device's aspect ratio. If bit 0 is
         * set, the mapper selects only matching fonts.
         */
        private long mapperValues;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setMapperFlags;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapperValues = leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_SETROP2 record defines the foreground raster operation mix mode in the playback device
     * context. The foreground mix mode is the mode for combining pens and interiors of filled objects with
     * foreground colors on the output surface.
     */
    public static class WmfSetRop2 implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines the foreground binary raster
         * operation mixing mode
         */
        private HwmfBinaryRasterOp drawMode;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setRop2;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            drawMode = HwmfBinaryRasterOp.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_SETSTRETCHBLTMODE record defines the bitmap stretching mode in the playback device
     * context.
     */
    public static class WmfSetStretchBltMode implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines bitmap stretching mode.
         * This MUST be one of the values:
         * BLACKONWHITE = 0x0001,
         * WHITEONBLACK = 0x0002,
         * COLORONCOLOR = 0x0003,
         * HALFTONE = 0x0004
         */
        private int setStretchBltMode;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setStretchBltMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            setStretchBltMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    /**
     * The META_DIBCREATEPATTERNBRUSH record creates a Brush Object with a
     * pattern specified by a DeviceIndependentBitmap (DIB) Object
     */
    public static class WmfDibCreatePatternBrush implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {

        private HwmfBrushStyle style;

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of a DIB
         * Object contains explicit RGB values, or indexes into a palette.
         *
         * If the Style field specifies BS_PATTERN, a ColorUsage value of DIB_RGB_COLORS MUST be
         * used regardless of the contents of this field.
         *
         * If the Style field specified anything but BS_PATTERN, this field MUST be one of the ColorUsage values.
         */
        private ColorUsage colorUsage;

        private HwmfBitmapDib patternDib;
        private HwmfBitmap16 pattern16;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.dibCreatePatternBrush;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            style = HwmfBrushStyle.valueOf(leis.readUShort());
            colorUsage = ColorUsage.valueOf(leis.readUShort());
            int size = 2*LittleEndianConsts.SHORT_SIZE;
            switch (style) {
            case BS_SOLID:
            case BS_NULL:
            case BS_DIBPATTERN:
            case BS_DIBPATTERNPT:
            case BS_HATCHED:
            case BS_PATTERN:
                patternDib = new HwmfBitmapDib();
                size += patternDib.init(leis, (int)(recordSize-6-size));
                break;
            case BS_INDEXED:
            case BS_DIBPATTERN8X8:
            case BS_MONOPATTERN:
            case BS_PATTERN8X8:
                throw new RuntimeException("pattern not supported");
            }
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties prop = ctx.getProperties();
            prop.setBrushStyle(style);
            prop.setBrushBitmap(getImage());
        }

        @Override
        public BufferedImage getImage() {
            if (patternDib != null) {
                return patternDib.getImage();
            } else if (pattern16 != null) {
                return pattern16.getImage();
            } else {
                return null;
            }
        }
    }

    /**
     * The META_DELETEOBJECT record deletes an object, including Bitmap16, Brush,
     * DeviceIndependentBitmap, Font, Palette, Pen, and Region. After the object is deleted,
     * its index in the WMF Object Table is no longer valid but is available to be reused.
     */
    public static class WmfDeleteObject implements HwmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to
         * get the object to be deleted.
         */
        private int objectIndex;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.deleteObject;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.unsetObjectTableEntry(objectIndex);
        }
    }

    public static class WmfCreatePatternBrush implements HwmfRecord, HwmfObjectTableEntry {

        private HwmfBitmap16 pattern;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createPatternBrush;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            pattern = new HwmfBitmap16(true);
            return pattern.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties dp = ctx.getProperties();
            dp.setBrushBitmap(pattern.getImage());
            dp.setBrushStyle(HwmfBrushStyle.BS_PATTERN);
        }
    }

    public static class WmfCreatePenIndirect implements HwmfRecord, HwmfObjectTableEntry {

        private HwmfPenStyle penStyle;
        /**
         * A 32-bit PointS Object that specifies a point for the object dimensions.
         * The x-coordinate is the pen width. The y-coordinate is ignored.
         */
        private int xWidth;
        @SuppressWarnings("unused")
        private int yWidth;
        /**
         * A 32-bit ColorRef Object that specifies the pen color value.
         */
        private HwmfColorRef colorRef;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createPenIndirect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            penStyle = HwmfPenStyle.valueOf(leis.readUShort());
            xWidth = leis.readShort();
            yWidth = leis.readShort();
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            return size+3*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties p = ctx.getProperties();
            p.setPenStyle(penStyle);
            p.setPenColor(colorRef);
            p.setPenWidth(xWidth);
        }
    }

    /**
     * The META_CREATEBRUSHINDIRECT record creates a Brush Object
     * from a LogBrush Object.
     *
     * The following table shows the relationship between values in the BrushStyle,
     * ColorRef and BrushHatch fields in a LogBrush Object. Only supported brush styles are listed.
     *
     * <table>
     * <tr>
     *   <th>BrushStyle</th>
     *   <th>ColorRef</th>
     *   <th>BrushHatch</th>
     * </tr>
     * <tr>
     *   <td>BS_SOLID</td>
     *   <td>SHOULD be a ColorRef Object, which determines the color of the brush.</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     * </tr>
     * <tr>
     *   <td>BS_NULL</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     * </tr>
     * <tr>
     *   <td>BS_PATTERN</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created.</td>
     * </tr>
     * <tr>
     *   <td>BS_DIBPATTERN</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created</td>
     * </tr>
     * <tr>
     *   <td>BS_DIBPATTERNPT</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created.</td>
     * </tr>
     * <tr>
     *   <td>BS_HATCHED</td>
     *   <td>SHOULD be a ColorRef Object, which determines the foreground color of the hatch pattern.</td>
     *   <td>A value from the {@link HwmfHatchStyle} Enumeration that specifies the orientation of lines used to create the hatch.</td>
     * </tr>
     * </table>
     */
    public static class WmfCreateBrushIndirect implements HwmfRecord, HwmfObjectTableEntry {
        private HwmfBrushStyle brushStyle;

        private HwmfColorRef colorRef;

        /**
         * A 16-bit field that specifies the brush hatch type.
         * Its interpretation depends on the value of BrushStyle.
         *
         */
        private HwmfHatchStyle brushHatch;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createBrushIndirect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            brushStyle = HwmfBrushStyle.valueOf(leis.readUShort());
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            brushHatch = HwmfHatchStyle.valueOf(leis.readUShort());
            return size+2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties p = ctx.getProperties();
            p.setBrushStyle(brushStyle);
            p.setBrushColor(colorRef);
            p.setBrushHatch(brushHatch);
        }
    }
}