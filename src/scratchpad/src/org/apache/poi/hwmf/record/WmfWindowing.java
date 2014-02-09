package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfWindowing {
    
    /**
     * The META_OFFSETCLIPRGN record moves the clipping region in the playback device context by the
     * specified offsets.
     */
    public static class WmfOffsetClipRgn implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the number of logical units to move up or down.
         */
        int yOffset;
        
        /**
         * A 16-bit signed integer that defines the number of logical units to move left or right. 
         */
        int xOffset;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.offsetClipRgn;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    
    /**
     * The META_SETVIEWPORTORG record defines the viewport origin in the playback device context.
     */
    public static class WmfSetViewportOrg implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setViewportOrg;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETVIEWPORTEXT record sets the horizontal and vertical extents
     * of the viewport in the playback device context.
     */
    public static class WmfSetViewportExt implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical extent
         * of the viewport in device units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the horizontal extent
         * of the viewport in device units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setViewportExt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_OFFSETVIEWPORTORG record moves the viewport origin in the playback device context
     * by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetViewportOrg implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int yOffset;
        
        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int xOffset;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.offsetViewportOrg;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETWINDOWORG record defines the output window origin in the playback device context.
     */
    public static class WmfSetWindowOrg implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setWindowOrg;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETWINDOWEXT record defines the horizontal and vertical extents
     * of the output window in the playback device context.
     */
    public static class WmfSetWindowExt implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical extent of
         * the window in logical units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the horizontal extent of
         * the window in logical units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setWindowExt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetWindowOrg implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int yOffset;
        
        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int xOffset;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.offsetWindowOrg;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfScaleWindowExt implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current y-extent by the value of the yNum member.
         */
        int yDenom;
        
        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current y-extent.
         */
        int yNum;
        
        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current x-extent by the value of the xNum member.
         */
        int xDenom;
        
        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current x-extent.
         */
        int xNum;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.scaleWindowExt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yDenom = leis.readShort();
            yNum = leis.readShort();
            xDenom = leis.readShort();
            xNum = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }


    /**
     * The META_SCALEVIEWPORTEXT record scales the horizontal and vertical extents of the viewport
     * that is defined in the playback device context by using the ratios formed by the specified
     * multiplicands and divisors.
     */
    public static class WmfScaleViewportExt implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current y-extent by the value of the yNum member.
         */
        int yDenom;
        
        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current y-extent.
         */
        int yNum;
        
        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current x-extent by the value of the xNum member.
         */
        int xDenom;
        
        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current x-extent.
         */
        int xNum;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.scaleViewportExt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yDenom = leis.readShort();
            yNum = leis.readShort();
            xDenom = leis.readShort();
            xNum = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_EXCLUDECLIPRECT record sets the clipping region in the playback device context to the
     * existing clipping region minus the specified rectangle.
     */
    public static class WmfExcludeClipRect implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        int bottom;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        int right;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int top;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int left;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.excludeClipRect;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottom = leis.readShort();
            right = leis.readShort();
            top = leis.readShort();
            left = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }


    /**
     * The META_INTERSECTCLIPRECT record sets the clipping region in the playback device context to the
     * intersection of the existing clipping region and the specified rectangle.
     */
    public static class WmfIntersectClipRect implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        int bottom;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        int right;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int top;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int left;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.intersectClipRect;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottom = leis.readShort();
            right = leis.readShort();
            top = leis.readShort();
            left = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_INTERSECTCLIPRECT record sets the clipping region in the playback device context to the
     * intersection of the existing clipping region and the specified rectangle.
     */
    public static class WmfSelectClipRegion implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be clipped.
         */
        int region;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.selectClipRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

}
