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

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfMisc {

    /**
     * The META_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class WmfSaveDc implements HwmfRecord {
        public HwmfRecordType getRecordType() { return HwmfRecordType.saveDc; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }
    }

    /**
     * The META_SETRELABS record is reserved and not supported.
     */
    public static class WmfSetRelabs implements HwmfRecord {
        public HwmfRecordType getRecordType() { return HwmfRecordType.setRelabs; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
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
        int nSavedDC;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.restoreDc;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            nSavedDC = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETBKCOLOR record sets the background color in the playback device context to a
     * specified color, or to the nearest physical color if the device cannot represent the specified color.
     */
    public static class WmfSetBkColor implements HwmfRecord {

        HwmfColorRef colorRef;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setBkColor;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new HwmfColorRef();
            return colorRef.init(leis);
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
         * This MUST be either TRANSPARENT = 0x0001 or OPAQUE = 0x0002
         */
        int bkMode;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setBkMode;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bkMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
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
        int layout;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setLayout;
        }

        @SuppressWarnings("unused")
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            layout = leis.readUShort();
            // A 16-bit field that MUST be ignored.
            int reserved = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETMAPMODE record defines the mapping mode in the playback device context.
     * The mapping mode defines the unit of measure used to transform page-space units into
     * device-space units, and also defines the orientation of the device's x and y axes.
     */
    public static class WmfSetMapMode implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines the mapping mode.
         *
         * The MapMode defines how logical units are mapped to physical units;
         * that is, assuming that the origins in both the logical and physical coordinate systems
         * are at the same point on the drawing surface, what is the physical coordinate (x',y')
         * that corresponds to logical coordinate (x,y).
         *
         * For example, suppose the mapping mode is MM_TEXT. Given the following definition of that
         * mapping mode, and an origin (0,0) at the top left corner of the drawing surface, logical
         * coordinate (4,5) would map to physical coordinate (4,5) in pixels.
         *
         * Now suppose the mapping mode is MM_LOENGLISH, with the same origin as the previous
         * example. Given the following definition of that mapping mode, logical coordinate (4,-5)
         * would map to physical coordinate (0.04,0.05) in inches.
         *
         * This MUST be one of the following:
         *
         * MM_TEXT (= 0x0001):
         *  Each logical unit is mapped to one device pixel.
         *  Positive x is to the right; positive y is down.
         *
         * MM_LOMETRIC (= 0x0002):
         *  Each logical unit is mapped to 0.1 millimeter.
         *  Positive x is to the right; positive y is up.
         *
         * MM_HIMETRIC (= 0x0003):
         *  Each logical unit is mapped to 0.01 millimeter.
         *  Positive x is to the right; positive y is up.
         *
         * MM_LOENGLISH (= 0x0004):
         *  Each logical unit is mapped to 0.01 inch.
         *  Positive x is to the right; positive y is up.
         *
         * MM_HIENGLISH (= 0x0005):
         *  Each logical unit is mapped to 0.001 inch.
         *  Positive x is to the right; positive y is up.
         *
         * MM_TWIPS (= 0x0006):
         *  Each logical unit is mapped to one twentieth (1/20) of a point.
         *  In printing, a point is 1/72 of an inch; therefore, 1/20 of a point is 1/1440 of an inch.
         *  This unit is also known as a "twip".
         *  Positive x is to the right; positive y is up.
         *
         * MM_ISOTROPIC (= 0x0007):
         *  Logical units are mapped to arbitrary device units with equally scaled axes;
         *  that is, one unit along the x-axis is equal to one unit along the y-axis.
         *  The META_SETWINDOWEXT and META_SETVIEWPORTEXT records specify the units and the
         *  orientation of the axes.
         *  The processing application SHOULD make adjustments as necessary to ensure the x and y
         *  units remain the same size. For example, when the window extent is set, the viewport
         *  SHOULD be adjusted to keep the units isotropic.
         *
         * MM_ANISOTROPIC (= 0x0008):
         *  Logical units are mapped to arbitrary units with arbitrarily scaled axes.
         */
        int mapMode;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setMapMode;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
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
        long mapperValues;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setMapperFlags;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapperValues = leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
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
         * operation mixing mode. This MUST be one of the values:
         * R2_BLACK = 0x0001,
         * R2_NOTMERGEPEN = 0x0002,
         * R2_MASKNOTPEN = 0x0003,
         * R2_NOTCOPYPEN = 0x0004,
         * R2_MASKPENNOT = 0x0005,
         * R2_NOT = 0x0006,
         * R2_XORPEN = 0x0007,
         * R2_NOTMASKPEN = 0x0008,
         * R2_MASKPEN = 0x0009,
         * R2_NOTXORPEN = 0x000A,
         * R2_NOP = 0x000B,
         * R2_MERGENOTPEN = 0x000C,
         * R2_COPYPEN = 0x000D,
         * R2_MERGEPENNOT = 0x000E,
         * R2_MERGEPEN = 0x000F,
         * R2_WHITE = 0x0010
         */
        int drawMode;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setRop2;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            drawMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
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
        int setStretchBltMode;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setStretchBltMode;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            setStretchBltMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_DIBCREATEPATTERNBRUSH record creates a Brush Object with a
     * pattern specified by a DeviceIndependentBitmap (DIB) Object
     */
    public static class WmfDibCreatePatternBrush implements HwmfRecord {

        HwmfBrushStyle style;

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of a DIB
         * Object contains explicit RGB values, or indexes into a palette.
         *
         * If the Style field specifies BS_PATTERN, a ColorUsage value of DIB_RGB_COLORS MUST be
         * used regardless of the contents of this field.
         *
         * If the Style field specified anything but BS_PATTERN, this field MUST be one of the values:
         * DIB_RGB_COLORS = 0x0000,
         * DIB_PAL_COLORS = 0x0001,
         * DIB_PAL_INDICES = 0x0002
         */
        int colorUsage;

        HwmfBitmapDib patternDib;
        HwmfBitmap16 pattern16;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.dibCreatePatternBrush;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            style = HwmfBrushStyle.valueOf(leis.readUShort());
            colorUsage = leis.readUShort();
            int size = 2*LittleEndianConsts.SHORT_SIZE;
            switch (style) {
            case BS_SOLID:
            case BS_NULL:
            case BS_DIBPATTERN:
            case BS_DIBPATTERNPT:
            case BS_HATCHED:
                patternDib = new HwmfBitmapDib();
                size += patternDib.init(leis);
                break;
            case BS_PATTERN:
                pattern16 = new HwmfBitmap16();
                size += pattern16.init(leis);
                break;
            case BS_INDEXED:
            case BS_DIBPATTERN8X8:
            case BS_MONOPATTERN:
            case BS_PATTERN8X8:
                throw new RuntimeException("pattern not supported");
            }
            return size;
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
        get the object to be deleted.
         */
        int objectIndex;

        public HwmfRecordType getRecordType() { return HwmfRecordType.deleteObject; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

    public static class WmfCreatePatternBrush implements HwmfRecord {

        HwmfBitmap16 pattern;

        public HwmfRecordType getRecordType() { return HwmfRecordType.createPatternBrush; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            pattern = new HwmfBitmap16(true);
            return pattern.init(leis);
        }
    }

    public static class WmfCreatePenIndirect implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that specifies the pen style.
         * The value MUST be defined from the PenStyle Enumeration table.
         *
         * PS_COSMETIC = 0x0000,
         * PS_ENDCAP_ROUND = 0x0000,
         * PS_JOIN_ROUND = 0x0000,
         * PS_SOLID = 0x0000,
         * PS_DASH = 0x0001,
         * PS_DOT = 0x0002,
         * PS_DASHDOT = 0x0003,
         * PS_DASHDOTDOT = 0x0004,
         * PS_NULL = 0x0005,
         * PS_INSIDEFRAME = 0x0006,
         * PS_USERSTYLE = 0x0007,
         * PS_ALTERNATE = 0x0008,
         * PS_ENDCAP_SQUARE = 0x0100,
         * PS_ENDCAP_FLAT = 0x0200,
         * PS_JOIN_BEVEL = 0x1000,
         * PS_JOIN_MITER = 0x2000
         */
        int penStyle;
        /**
         * A 32-bit PointS Object that specifies a point for the object dimensions.
         * The xcoordinate is the pen width. The y-coordinate is ignored.
         */
        int xWidth, yWidth;
        /**
         * A 32-bit ColorRef Object that specifies the pen color value.
         */
        HwmfColorRef colorRef;

        public HwmfRecordType getRecordType() { return HwmfRecordType.createPenIndirect; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            penStyle = leis.readUShort();
            xWidth = leis.readShort();
            yWidth = leis.readShort();
            colorRef = new HwmfColorRef();
            int size = 3*LittleEndianConsts.SHORT_SIZE;
            size += colorRef.init(leis);
            return size;
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
    public static class WmfCreateBrushIndirect implements HwmfRecord {
        HwmfBrushStyle brushStyle;

        HwmfColorRef colorRef;

        /**
         * A 16-bit field that specifies the brush hatch type.
         * Its interpretation depends on the value of BrushStyle.
         * 
         */
        HwmfHatchStyle brushHatch;

        public HwmfRecordType getRecordType() { return HwmfRecordType.createBrushIndirect; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            brushStyle = HwmfBrushStyle.valueOf(leis.readUShort());
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            brushHatch = HwmfHatchStyle.valueOf(leis.readUShort());
            size += 4;
            return size;
        }
    }
}