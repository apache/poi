package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfText {

    /**
     * The META_SETTEXTCHAREXTRA record defines inter-character spacing for text justification in the 
     * playback device context. Spacing is added to the white space between each character, including
     * break characters, when a line of justified text is output.
     */
    public static class WmfSetTextCharExtra implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the amount of extra space, in
         * logical units, to be added to each character. If the current mapping mode is not MM_TEXT,
         * this value is transformed and rounded to the nearest pixel. For details about setting the
         * mapping mode, see META_SETMAPMODE
         */
        int charExtra;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setTextCharExtra;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            charExtra = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    /**
     * The META_SETTEXTCOLOR record defines the text foreground color in the playback device context.
     */
    public static class WmfSetTextColor implements WmfRecord {
        
        WmfColorRef colorRef;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setTextColor;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            WmfColorRef colorRef = new WmfColorRef();
            return colorRef.init(leis);
        }
    }
    
    /**
     * The META_SETTEXTJUSTIFICATION record defines the amount of space to add to break characters
     * in a string of justified text.
     */
    public static class WmfSetTextJustification implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that specifies the number of space characters in the line.
         */
        int breakCount;
        
        /**
         * A 16-bit unsigned integer that specifies the total extra space, in logical
         * units, to be added to the line of text. If the current mapping mode is not MM_TEXT, the value
         * identified by the BreakExtra member is transformed and rounded to the nearest pixel. For
         * details about setting the mapping mode, see {@link WmfSetMapMode}.
         */
        int breakExtra;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setBkColor;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            breakCount = leis.readUShort();
            breakExtra = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    /**
     * The META_TEXTOUT record outputs a character string at the specified location by using the font,
     * background color, and text color that are defined in the playback device context.
     */
    public static class WmfTextOut implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the length of the string, in bytes, pointed to by String.
         */
        int stringLength;
        /**
         * The size of this field MUST be a multiple of two. If StringLength is an odd
         * number, then this field MUST be of a size greater than or equal to StringLength + 1.
         * A variable-length string that specifies the text to be drawn.
         * The string does not need to be null-terminated, because StringLength specifies the
         * length of the string.
         * The string is written at the location specified by the XStart and YStart fields.
         */
        String text;
        /**
         * A 16-bit signed integer that defines the vertical (y-axis) coordinate, in logical
         * units, of the point where drawing is to start.
         */
        int yStart;
        /**
         * A 16-bit signed integer that defines the horizontal (x-axis) coordinate, in
         * logical units, of the point where drawing is to start.
         */
        int xStart;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.textOut;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            stringLength = leis.readShort();
            byte buf[] = new byte[stringLength+(stringLength%2)];
            leis.readFully(buf);
            text = new String(buf, "UTF16-LE").trim();
            yStart = leis.readShort();
            xStart = leis.readShort();
            return 3*LittleEndianConsts.SHORT_SIZE+buf.length;
        }
    }
    
    /**
     * The META_EXTTEXTOUT record outputs text by using the font, background color, and text color that
     * are defined in the playback device context. Optionally, dimensions can be provided for clipping,
     * opaquing, or both.
     */
    public static class WmfExtTextOut implements WmfRecord {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, where the 
        text string is to be located.
         */
        int y;  
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, where the 
        text string is to be located.
         */
        int x;  
        /**
         * A 16-bit signed integer that defines the length of the string.
         */
        int stringLength;
        /**
         * A 16-bit unsigned integer that defines the use of the application-defined 
         * rectangle. This member can be a combination of one or more values in the 
         * ExtTextOutOptions Flags:
         * 
         * ETO_OPAQUE (0x0002):
         * Indicates that the background color that is defined in the playback device context 
         * SHOULD be used to fill the rectangle.
         * 
         * ETO_CLIPPED (0x0004):
         * Indicates that the text SHOULD be clipped to the rectangle.
         * 
         * ETO_GLYPH_INDEX (0x0010):
         * Indicates that the string to be output SHOULD NOT require further processing 
         * with respect to the placement of the characters, and an array of character 
         * placement values SHOULD be provided. This character placement process is 
         * useful for fonts in which diacritical characters affect character spacing.
         * 
         * ETO_RTLREADING (0x0080):
         * Indicates that the text MUST be laid out in right-to-left reading order, instead of 
         * the default left-to-right order. This SHOULD be applied only when the font that is 
         * defined in the playback device context is either Hebrew or Arabic. <37>
         * 
         * ETO_NUMERICSLOCAL (0x0400):
         * Indicates that to display numbers, digits appropriate to the locale SHOULD be 
         * used.
         * 
         * ETO_NUMERICSLATIN (0x0800):
         * Indicates that to display numbers, European digits SHOULD be used. <39>
         * 
         * ETO_PDY (0x2000):
         * Indicates that both horizontal and vertical character displacement values 
         * SHOULD be provided.
         */
        int fwOpts;
        /**
         * An optional 8-byte Rect Object (section 2.2.2.18) that defines the 
         * dimensions, in logical coordinates, of a rectangle that is used for clipping, opaquing, or both.
         * 
         * The corners are given in the order left, top, right, bottom.
         * Each value is a 16-bit signed integer that defines the coordinate, in logical coordinates, of 
         * the upper-left corner of the rectangle
         */
        int left,top,right,bottom;
        /**
         * A variable-length string that specifies the text to be drawn. The string does 
         * not need to be null-terminated, because StringLength specifies the length of the string. If 
         * the length is odd, an extra byte is placed after it so that the following member (optional Dx) is 
         * aligned on a 16-bit boundary.
         */
        String text;
        /**
         * An optional array of 16-bit signed integers that indicate the distance between 
         * origins of adjacent character cells. For example, Dx[i] logical units separate the origins of 
         * character cell i and character cell i + 1. If this field is present, there MUST be the same 
         * number of values as there are characters in the string.
         */
        int dx[];
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.extTextOut;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            stringLength = leis.readShort();
            fwOpts = leis.readUShort();
            left = leis.readShort();
            top = leis.readShort();
            right = leis.readShort();
            bottom = leis.readShort();
            
            byte buf[] = new byte[stringLength+(stringLength%2)];
            leis.readFully(buf);
            text = new String(buf, "UTF16-LE");
            
            int size = 8*LittleEndianConsts.SHORT_SIZE+buf.length;
            if (size < recordSize) {
                dx = new int[text.length()];
                for (int i=0; i<dx.length; i++) {
                    dx[i] = leis.readShort();
                }
                size += dx.length*LittleEndianConsts.SHORT_SIZE;
            }
            
            return size;
        }
    }
    

    
    
    /**
     * The META_SETTEXTALIGN record defines text-alignment values in the playback device context.
     */
    public static class WmfSetTextAlign implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines text alignment.
         * This value MUST be a combination of one or more TextAlignmentMode Flags
         * for text with a horizontal baseline, and VerticalTextAlignmentMode Flags
         * for text with a vertical baseline.
         * 
         * TextAlignmentMode Flags:
         * TA_NOUPDATECP (0x0000):
         * The drawing position in the playback device context MUST NOT be updated after each
         * text output call. The reference point MUST be passed to the text output function.
         * 
         * TA_LEFT (0x0000):
         * The reference point MUST be on the left edge of the bounding rectangle.
         * 
         * TA_TOP (0x0000):
         * The reference point MUST be on the top edge of the bounding rectangle.
         * 
         * TA_UPDATECP (0x0001):
         * The drawing position in the playback device context MUST be updated after each text
         * output call. It MUST be used as the reference point.
         * 
         * TA_RIGHT (0x0002):
         * The reference point MUST be on the right edge of the bounding rectangle.
         * 
         * TA_CENTER (0x0006):
         * The reference point MUST be aligned horizontally with the center of the bounding
         * rectangle.
         * 
         * TA_BOTTOM (0x0008):
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         * 
         * TA_BASELINE (0x0018):
         * The reference point MUST be on the baseline of the text.
         * 
         * TA_RTLREADING (0x0100):
         * The text MUST be laid out in right-to-left reading order, instead of the default
         * left-toright order. This SHOULD be applied only when the font that is defined in the
         * playback device context is either Hebrew or Arabic.
         * 
         * 
         * VerticalTextAlignmentMode Flags (e.g. for Kanji fonts)
         * VTA_TOP (0x0000):
         * The reference point MUST be on the top edge of the bounding rectangle.
         * 
         * VTA_RIGHT (0x0000):
         * The reference point MUST be on the right edge of the bounding rectangle.
         * 
         * VTA_BOTTOM (0x0002):
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         * 
         * VTA_CENTER (0x0006):
         * The reference point MUST be aligned vertically with the center of the bounding
         * rectangle.
         * 
         * VTA_LEFT (0x0008):
         * The reference point MUST be on the left edge of the bounding rectangle.
         * 
         * VTA_BASELINE (0x0018):
         * The reference point MUST be on the baseline of the text.
         */
        int textAlignmentMode;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setTextAlign;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            textAlignmentMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }
}
