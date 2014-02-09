package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfFill {
    /**
     * The META_FILLREGION record fills a region using a specified brush.
     */
    public static class WmfFillRegion implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be filled.
         */
        int region;

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get the
         * brush to use for filling the region.
         */
        int brush;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.fillRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readUShort();
            brush = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_PAINTREGION record paints the specified region by using the brush that is
     * defined in the playback device context.
     */
    public static class WmfPaintRegion implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be painted.
         */
        int region;

        public WmfRecordType getRecordType() {
            return WmfRecordType.paintRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    
    /**
     * The META_FLOODFILL record fills an area of the output surface with the brush that
     * is defined in the playback device context.
     */
    public static class WmfFloodFill implements WmfRecord {
        
        /**
         * A 32-bit ColorRef Object that defines the color value.
         */
        WmfColorRef colorRef;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * point where filling is to start.
         */
        int yStart;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * point where filling is to start.
         */
        int xStart;
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.floodFill;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new WmfColorRef();
            int size = colorRef.init(leis);
            yStart = leis.readShort();
            xStart = leis.readShort();
            return size+2*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_SETPOLYFILLMODE record sets polygon fill mode in the playback device context for
     * graphics operations that fill polygons.
     */
    public static class WmfSetPolyfillMode implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines polygon fill mode.
         * This MUST be one of the values: ALTERNATE = 0x0001, WINDING = 0x0002
         */
        int polyFillMode;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setPolyFillMode;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            polyFillMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }


    /**
     * The META_EXTFLOODFILL record fills an area with the brush that is defined in
     * the playback device context.
     */
    public static class WmfExtFloodFill implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the fill operation to be performed. This
         * member MUST be one of the values in the FloodFill Enumeration table:
         * 
         * FLOODFILLBORDER = 0x0000:
         * The fill area is bounded by the color specified by the Color member.
         * This style is identical to the filling performed by the META_FLOODFILL record.
         * 
         * FLOODFILLSURFACE = 0x0001:
         * The fill area is bounded by the color that is specified by the Color member.
         * Filling continues outward in all directions as long as the color is encountered.
         * This style is useful for filling areas with multicolored boundaries.
         */
        int mode;
        
        /**
         * A 32-bit ColorRef Object that defines the color value.
         */
        WmfColorRef colorRef;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the point
         * to be set.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the point
         * to be set.
         */
        int x;  
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.extFloodFill;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mode = leis.readUShort();
            colorRef = new WmfColorRef();
            int size = colorRef.init(leis);
            y = leis.readShort();
            x = leis.readShort();
            return size+3*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_INVERTREGION record draws a region in which the colors are inverted.
     */
    public static class WmfInvertRegion implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be inverted.
         */
        int region;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.invertRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }
    

    /**
     * The META_PATBLT record paints a specified rectangle using the brush that is defined in the playback
     * device context. The brush color and the surface color or colors are combined using the specified
     * raster operation.
     */
    public static class WmfPatBlt implements WmfRecord {
        
        /**
         * A 32-bit unsigned integer that defines the raster operation code.
         * This code MUST be one of the values in the Ternary Raster Operation enumeration table.
         */
        WmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the rectangle.
         */
        int height;
        
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the rectangle.
         */
        int width;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle to be filled.
         */
        int yLeft;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle to be filled.
         */
        int xLeft;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.patBlt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            int rasterOpIndex = leis.readUShort();
            int rasterOpCode = leis.readUShort();

            rasterOperation = WmfTernaryRasterOp.fromOpIndex(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);
            
            height = leis.readShort();
            width = leis.readShort();
            yLeft = leis.readShort();
            xLeft = leis.readShort();

            return 6*LittleEndianConsts.SHORT_SIZE;
        }
    }

    public static class WmfStretchBlt implements WmfRecord {
        /**
         */
        
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the new 
         * image. This code MUST be one of the values in the Ternary Raster Operation Enumeration
         */
        WmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source rectangle.
         */
        int srcHeight; 
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source rectangle.
         */
        int srcWidth; 
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left corner 
         * of the source rectangle.
         */
        int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left corner 
         * of the source rectangle.
         */
        int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the destination rectangle.
         */
        int destHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the destination rectangle.
         */
        int destWidth;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        int xDest;
        
        /**
         * A variable-sized Bitmap16 Object that defines source image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        WmfBitmap16 target;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.stretchBlt;
        }
        
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize > ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpIndex = leis.readUShort();
            int rasterOpCode = leis.readUShort();
            
            rasterOperation = WmfTernaryRasterOp.fromOpIndex(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            srcHeight = leis.readShort();
            srcWidth = leis.readShort();
            ySrc = leis.readShort();
            xSrc = leis.readShort();
            size = 6*LittleEndianConsts.SHORT_SIZE;
            if (!hasBitmap) {
                @SuppressWarnings("unused")
                int reserved = leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            destHeight = leis.readShort();
            destWidth = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();
            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new WmfBitmap16();
                size += target.init(leis);
            }
            
            return size;
        }
    }

    public static class WmfBitBlt implements WmfRecord {

        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush in the playback 
         * device context, and the destination pixels are to be combined to form the new image.
         */
        WmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left corner 
        of the source rectangle.
         */
        int ySrc; 
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left corner 
        of the source rectangle.
         */
        int xSrc; 
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source and 
        destination rectangles.
         */
        int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source and destination 
        rectangles.
         */
        int width;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left 
        corner of the destination rectangle.
         */
        int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
        corner of the destination rectangle.
         */
        int xDest;
        
        /**
         * A variable-sized Bitmap16 Object that defines source image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        WmfBitmap16 target;

        public WmfRecordType getRecordType() {
            return WmfRecordType.bitBlt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize > ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpIndex = leis.readUShort();
            int rasterOpCode = leis.readUShort();
            
            rasterOperation = WmfTernaryRasterOp.fromOpIndex(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            ySrc = leis.readShort();
            xSrc = leis.readShort();

            size = 4*LittleEndianConsts.SHORT_SIZE;
            
            if (!hasBitmap) {
                @SuppressWarnings("unused")
                int reserved = leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            
            height = leis.readShort();
            width = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();

            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new WmfBitmap16();
                size += target.init(leis);
            }
            
            return size;
        }
    }


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


    public static class WmfDibBitBlt implements WmfRecord {

        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the
         * new image. This code MUST be one of the values in the Ternary Raster Operation Enumeration.
         */
        WmfTernaryRasterOp rasterOperation;  
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the source rectangle.
         */
        int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the source rectangle.
         */
        int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source and 
         * destination rectangles.
         */
        int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source and destination
         * rectangles.
         */
        int width;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left
         * corner of the destination rectangle.
         */
        int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        int xDest;
        
        /**
         * A variable-sized DeviceIndependentBitmap Object that defines image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        WmfBitmapDib target;
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.dibBitBlt;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize > ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpIndex = leis.readUShort();
            int rasterOpCode = leis.readUShort();
            
            rasterOperation = WmfTernaryRasterOp.fromOpIndex(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            ySrc = leis.readShort();
            xSrc = leis.readShort();
            size = 4*LittleEndianConsts.SHORT_SIZE;
            if (!hasBitmap) {
                @SuppressWarnings("unused")
                int reserved = leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            height = leis.readShort();
            width = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();
            
            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new WmfBitmapDib();
                size += target.init(leis);
            }
            
            return size;
        }
    }
}
