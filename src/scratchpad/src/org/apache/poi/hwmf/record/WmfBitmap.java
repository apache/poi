package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfBitmap {

    /**
     * The META_SETDIBTODEV record sets a block of pixels in the playback device context
     * using deviceindependent color data.
     * The source of the color data is a DIB
     */
    public static class WmfSetDibToDev implements WmfRecord {

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of the
         * DIB contains explicit RGB values or indexes into a palette.
         * This MUST be one of the values in the ColorUsage Enumeration:
         * DIB_RGB_COLORS = 0x0000,
         * DIB_PAL_COLORS = 0x0001,
         * DIB_PAL_INDICES = 0x0002
         */
        int colorUsage;  
        /**
         * A 16-bit unsigned integer that defines the number of scan lines in the source.
         */
        int scanCount;
        /**
         * A 16-bit unsigned integer that defines the starting scan line in the source.
         */
        int startScan;  
        /**
         * A 16-bit unsigned integer that defines the y-coordinate, in logical units, of the
         * source rectangle.
         */
        int yDib;  
        /**
         * A 16-bit unsigned integer that defines the x-coordinate, in logical units, of the
         * source rectangle.
         */
        int xDib;  
        /**
         * A 16-bit unsigned integer that defines the height, in logical units, of the
         * source and destination rectangles.
         */
        int height;
        /**
         * A 16-bit unsigned integer that defines the width, in logical units, of the
         * source and destination rectangles.
         */
        int width;
        /**
         * A 16-bit unsigned integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the destination rectangle.
         */
        int yDest;
        /**
         * A 16-bit unsigned integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the destination rectangle.
         */
        int xDest;
        /**
         * A variable-sized DeviceIndependentBitmap Object that is the source of the color data.
         */
        WmfBitmapDib dib;        
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setDibToDev;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorUsage = leis.readUShort();
            scanCount = leis.readUShort();
            startScan = leis.readUShort();
            yDib = leis.readUShort();
            xDib = leis.readUShort();
            height = leis.readUShort();
            width = leis.readUShort();
            yDest = leis.readUShort();
            xDest = leis.readUShort();
            
            int size = 9*LittleEndianConsts.SHORT_SIZE;
            dib = new WmfBitmapDib();
            size += dib.init(leis);
            
            return size;
        }        
    }
}
