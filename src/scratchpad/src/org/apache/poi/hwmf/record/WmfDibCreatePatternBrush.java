package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_DIBCREATEPATTERNBRUSH record creates a Brush Object with a
 * pattern specified by a DeviceIndependentBitmap (DIB) Object 
 */
public class WmfDibCreatePatternBrush implements WmfRecord {
    
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
