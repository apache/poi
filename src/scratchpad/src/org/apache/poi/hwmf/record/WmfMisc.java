package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfMisc {
    /**
     * The META_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class WmfSaveDc implements WmfRecord {
        public WmfRecordType getRecordType() { return WmfRecordType.saveDc; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }
    }

    /**
     * The META_SETRELABS record is reserved and not supported.
     */
    public static class WmfSetRelabs implements WmfRecord {
        public WmfRecordType getRecordType() { return WmfRecordType.setRelabs; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }
    }

    /**
     * The META_RESTOREDC record restores the playback device context from a previously saved device
     * context.
     */
    public static class WmfRestoreDc implements WmfRecord {
        
        /**
         * nSavedDC (2 bytes):  A 16-bit signed integer that defines the saved state to be restored. If this 
         * member is positive, nSavedDC represents a specific instance of the state to be restored. If 
         * this member is negative, nSavedDC represents an instance relative to the current state.
         */
        int nSavedDC;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.restoreDc;
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
    public static class WmfSetBkColor implements WmfRecord {
        
        WmfColorRef colorRef;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setBkColor;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            WmfColorRef colorRef = new WmfColorRef();
            return colorRef.init(leis);
        }
    }

    /**
     * The META_SETBKMODE record defines the background raster operation mix mode in the playback
     * device context. The background mix mode is the mode for combining pens, text, hatched brushes,
     * and interiors of filled objects with background colors on the output surface.
     */
    public static class WmfSetBkMode implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines background mix mode.
         * This MUST be either TRANSPARENT = 0x0001 or OPAQUE = 0x0002
         */
        int bkMode;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setBkMode;
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
    public static class WmfSetLayout implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the layout of text and graphics.
         * LAYOUT_LTR = 0x0000
         * LAYOUT_RTL = 0x0001
         * LAYOUT_BITMAPORIENTATIONPRESERVED = 0x0008
         */
        int layout;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setLayout;
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
    public static class WmfSetMapMode implements WmfRecord {
        
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
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setMapMode;
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
    public static class WmfSetMapperFlags implements WmfRecord {
        
        /**
         * A 32-bit unsigned integer that defines whether the font mapper should attempt to
         * match a font's aspect ratio to the current device's aspect ratio. If bit 0 is
         * set, the mapper selects only matching fonts.
         */
        long mapperValues;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setMapperFlags;
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
    public static class WmfSetRop2 implements WmfRecord {
        
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
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setRop2;
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
    public static class WmfSetStretchBltMode implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines bitmap stretching mode.
         * This MUST be one of the values:
         * BLACKONWHITE = 0x0001,
         * WHITEONBLACK = 0x0002,
         * COLORONCOLOR = 0x0003,
         * HALFTONE = 0x0004
         */
        int setStretchBltMode;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setStretchBltMode;
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
    public static class WmfDibCreatePatternBrush implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the brush style. The legal values for this
         * field are defined as follows: if the value is not BS_PATTERN, BS_DIBPATTERNPT MUST be
         * assumed.
         */
        public static enum BrushStyle {
            /**
             * A brush that paints a single, constant color, either solid or dithered.
             */
            BS_SOLID(0x0000),
            /**
             * A brush that does nothing. Using a BS_NULL brush in a graphics operation
             * MUST have the same effect as using no brush at all.
             */
            BS_NULL(0x0001),
            /**
             * A brush that paints a predefined simple pattern, or "hatch", onto a solid background.
             */
            BS_HATCHED(0x0002),
            /**
             * A brush that paints a pattern defined by a bitmap, which MAY be a Bitmap16
             * Object or a DeviceIndependentBitmap (DIB) Object.
             */
            BS_PATTERN(0x0003),
            /**
             * Not supported
             */
            BS_INDEXED(0x0004),
            /**
             * A pattern brush specified by a DIB.
             */
            BS_DIBPATTERN(0x0005),
            /**
             * A pattern brush specified by a DIB.
             */
            BS_DIBPATTERNPT(0x0006),
            /**
             * Not supported
             */
            BS_PATTERN8X8(0x0007),
            /**
             * Not supported
             */
            BS_DIBPATTERN8X8(0x0008),
            /**
             * Not supported
             */
            BS_MONOPATTERN(0x0009);
            
            int flag;
            BrushStyle(int flag) {
                this.flag = flag;
            }
            
            static BrushStyle valueOf(int flag) {
                for (BrushStyle bs : values()) {
                    if (bs.flag == flag) return bs;
                }
                return null;
            }
        }
        
        BrushStyle style;
        
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
        
        WmfBitmapDib patternDib;
        WmfBitmap16 pattern16;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.dibCreatePatternBrush;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            style = BrushStyle.valueOf(leis.readUShort());
            colorUsage = leis.readUShort();
            int size = 2*LittleEndianConsts.SHORT_SIZE; 
            switch (style) {
            case BS_SOLID:
            case BS_NULL:
            case BS_DIBPATTERN:
            case BS_DIBPATTERNPT:
            case BS_HATCHED:
                patternDib = new WmfBitmapDib();
                size += patternDib.init(leis);
                break;
            case BS_PATTERN:
                pattern16 = new WmfBitmap16();
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
    public static class WmfDeleteObject implements WmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to 
        get the object to be deleted.
         */
        int objectIndex;
        
        public WmfRecordType getRecordType() { return WmfRecordType.deleteObject; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

    public static class WmfCreatePatternBrush implements WmfRecord {
        
        WmfBitmap16 pattern;

        public WmfRecordType getRecordType() { return WmfRecordType.createPatternBrush; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            pattern = new WmfBitmap16(true);
            return pattern.init(leis);
        }
    }

    public static class WmfCreatePenIndirect implements WmfRecord {
        
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
        WmfColorRef colorRef;
        
        public WmfRecordType getRecordType() { return WmfRecordType.createPatternBrush; }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            penStyle = leis.readUShort();
            xWidth = leis.readShort();
            yWidth = leis.readShort();
            colorRef = new WmfColorRef();
            int size = 3*LittleEndianConsts.SHORT_SIZE;
            size += colorRef.init(leis);
            return size;
        }
    }
}