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

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfFill {
    /**
     * A record which contains an image (to be extracted)
     */
    public interface HwmfImageRecord {
        BufferedImage getImage();
    }
    
    /**
     * The ColorUsage Enumeration (a 16-bit unsigned integer) specifies whether a color table
     * exists in a device-independent bitmap (DIB) and how to interpret its values,
     * i.e. if contains explicit RGB values or indexes into a palette.
     */
    public enum ColorUsage {
        /**
         * The color table contains RGB values
         */
        DIB_RGB_COLORS(0x0000),
        /**
         * The color table contains 16-bit indices into the current logical palette in
         * the playback device context.
         */
        DIB_PAL_COLORS(0x0001),
        /**
         * No color table exists. The pixels in the DIB are indices into the current
         * logical palette in the playback device context.
         */
        DIB_PAL_INDICES(0x0002)
        ;


        public final int flag;
        ColorUsage(int flag) {
            this.flag = flag;
        }

        static ColorUsage valueOf(int flag) {
            for (ColorUsage bs : values()) {
                if (bs.flag == flag) return bs;
            }
            return null;
        }
    }
    
    
    /**
     * The META_FILLREGION record fills a region using a specified brush.
     */
    public static class WmfFillRegion implements HwmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be filled.
         */
        private int regionIndex;

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get the
         * brush to use for filling the region.
         */
        private int brushIndex;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.fillRegion;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            brushIndex = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(regionIndex);
            ctx.applyObjectTableEntry(brushIndex);
            
            Shape region = ctx.getProperties().getRegion();
            if (region != null) {
                ctx.fill(region);
            }
            
        }
    }

    /**
     * The META_PAINTREGION record paints the specified region by using the brush that is
     * defined in the playback device context.
     */
    public static class WmfPaintRegion implements HwmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be painted.
         */
        int regionIndex;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.paintRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(regionIndex);

            Shape region = ctx.getProperties().getRegion();
            if (region != null) {
                ctx.fill(region);
            }        
        }
    }
    
    
    /**
     * The META_FLOODFILL record fills an area of the output surface with the brush that
     * is defined in the playback device context.
     */
    public static class WmfFloodFill implements HwmfRecord {
        
        /**
         * A 32-bit ColorRef Object that defines the color value.
         */
        private HwmfColorRef colorRef;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * point where filling is to start.
         */
        private int yStart;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * point where filling is to start.
         */
        private int xStart;
        
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.floodFill;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            yStart = leis.readShort();
            xStart = leis.readShort();
            return size+2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }

    /**
     * The META_SETPOLYFILLMODE record sets polygon fill mode in the playback device context for
     * graphics operations that fill polygons.
     */
    public static class WmfSetPolyfillMode implements HwmfRecord {
        /**
         * A 16-bit unsigned integer that defines polygon fill mode.
         * This MUST be one of the values: ALTERNATE = 0x0001, WINDING = 0x0002
         */
        public enum HwmfPolyfillMode {
            /**
             * Selects alternate mode (fills the area between odd-numbered and
             * even-numbered polygon sides on each scan line).
             */
            ALTERNATE(0x0001, Path2D.WIND_EVEN_ODD),
            /**
             * Selects winding mode (fills any region with a nonzero winding value).
             */
            WINDING(0x0002, Path2D.WIND_NON_ZERO);

            public final int wmfFlag;
            public final int awtFlag;
            HwmfPolyfillMode(int wmfFlag, int awtFlag) {
                this.wmfFlag = wmfFlag;
                this.awtFlag = awtFlag;
            }

            static HwmfPolyfillMode valueOf(int wmfFlag) {
                for (HwmfPolyfillMode pm : values()) {
                    if (pm.wmfFlag == wmfFlag) return pm;
                }
                return null;
            }
        }
        
        /**
         * A 16-bit unsigned integer that defines polygon fill mode.
         * This MUST be one of the values: ALTERNATE = 0x0001, WINDING = 0x0002
         */
        private HwmfPolyfillMode polyfillMode;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setPolyFillMode;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            polyfillMode = HwmfPolyfillMode.valueOf(leis.readUShort() & 3);
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setPolyfillMode(polyfillMode);
        }
    }


    /**
     * The META_EXTFLOODFILL record fills an area with the brush that is defined in
     * the playback device context.
     */
    public static class WmfExtFloodFill implements HwmfRecord {
        
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
        private int mode;
        
        /**
         * A 32-bit ColorRef Object that defines the color value.
         */
        private HwmfColorRef colorRef;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the point
         * to be set.
         */
        private int y;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the point
         * to be set.
         */
        private int x;  
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.extFloodFill;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mode = leis.readUShort();
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            y = leis.readShort();
            x = leis.readShort();
            return size+3*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }

    /**
     * The META_INVERTREGION record draws a region in which the colors are inverted.
     */
    public static class WmfInvertRegion implements HwmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be inverted.
         */
        private int region;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.invertRegion;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }
    

    /**
     * The META_PATBLT record paints a specified rectangle using the brush that is defined in the playback
     * device context. The brush color and the surface color or colors are combined using the specified
     * raster operation.
     */
    public static class WmfPatBlt implements HwmfRecord {
        
        /**
         * A 32-bit unsigned integer that defines the raster operation code.
         * This code MUST be one of the values in the Ternary Raster Operation enumeration table.
         */
        private HwmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the rectangle.
         */
        private int height;
        
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the rectangle.
         */
        private int width;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle to be filled.
         */
        private int yLeft;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle to be filled.
         */
        private int xLeft;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.patBlt;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();

            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);
            
            height = leis.readShort();
            width = leis.readShort();
            yLeft = leis.readShort();
            xLeft = leis.readShort();

            return 6*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }

    /**
     * The META_STRETCHBLT record specifies the transfer of a block of pixels according to a raster
     * operation, with possible expansion or contraction.
     * The destination of the transfer is the current output region in the playback device context.
     * There are two forms of META_STRETCHBLT, one which specifies a bitmap as the source, and the other
     * which uses the playback device context as the source. Definitions follow for the fields that are the
     * same in the two forms of META_STRETCHBLT are defined below. The subsections that follow specify
     * the packet structures of the two forms of META_STRETCHBLT.
     * The expansion or contraction is performed according to the stretching mode currently set in the
     * playback device context, which MUST be a value from the StretchMode.
     */
    public static class WmfStretchBlt implements HwmfRecord {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the new 
         * image. This code MUST be one of the values in the Ternary Raster Operation Enumeration
         */
        private HwmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source rectangle.
         */
        private int srcHeight; 
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source rectangle.
         */
        private int srcWidth; 
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left corner 
         * of the source rectangle.
         */
        private int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left corner 
         * of the source rectangle.
         */
        private int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the destination rectangle.
         */
        private int destHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the destination rectangle.
         */
        private int destWidth;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        private int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        private int xDest;
        
        /**
         * A variable-sized Bitmap16 Object that defines source image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        HwmfBitmap16 target;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.stretchBlt;
        }
        
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize > ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();
            
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            srcHeight = leis.readShort();
            srcWidth = leis.readShort();
            ySrc = leis.readShort();
            xSrc = leis.readShort();
            size = 6*LittleEndianConsts.SHORT_SIZE;
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            destHeight = leis.readShort();
            destWidth = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();
            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new HwmfBitmap16();
                size += target.init(leis);
            }
            
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }

    /**
     * The META_STRETCHDIB record specifies the transfer of color data from a
     * block of pixels in device independent format according to a raster operation,
     * with possible expansion or contraction.
     * The source of the color data is a DIB, and the destination of the transfer is
     * the current output region in the playback device context.
     */
    public static class WmfStretchDib implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush in
         * the playback device context, and the destination pixels are to be combined to
         * form the new image.
         */
        private HwmfTernaryRasterOp rasterOperation;

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of the
         * DIB contains explicit RGB values or indexes into a palette.
         */
        private ColorUsage colorUsage;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the
         * source rectangle.
         */
        private int srcHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the
         * source rectangle.
         */
        private int srcWidth; 
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * source rectangle.
         */
        private int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the 
         * source rectangle.
         */
        private int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the 
         * destination rectangle.
         */
        private int destHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the 
         * destination rectangle.
         */
        private int destWidth;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the 
         * upper-left corner of the destination rectangle.
         */
        private int yDst;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the 
         * upper-left corner of the destination rectangle.
         */
        private int xDst;
        /**
         * A variable-sized DeviceIndependentBitmap Object (section 2.2.2.9) that is the 
         * source of the color data.
         */
        private HwmfBitmapDib dib;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.stretchDib;
        }
        
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();
            
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            colorUsage = ColorUsage.valueOf(leis.readUShort());
            srcHeight = leis.readShort();
            srcWidth = leis.readShort();
            ySrc = leis.readShort();
            xSrc = leis.readShort();
            destHeight = leis.readShort();
            destWidth = leis.readShort();
            yDst = leis.readShort();
            xDst = leis.readShort();
            
            int size = 11*LittleEndianConsts.SHORT_SIZE;
            dib = new HwmfBitmapDib();
            size += dib.init(leis, (int)(recordSize-6-size));

            return size;
        }        

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            
        }

        @Override
        public BufferedImage getImage() {
            return dib.getImage();
        }
    }
    
    public static class WmfBitBlt implements HwmfRecord {

        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush in the playback 
         * device context, and the destination pixels are to be combined to form the new image.
         */
        private HwmfTernaryRasterOp rasterOperation;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left corner 
        of the source rectangle.
         */
        private int ySrc; 
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left corner 
        of the source rectangle.
         */
        private int xSrc; 
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source and 
        destination rectangles.
         */
        private int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source and destination 
        rectangles.
         */
        private int width;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left 
        corner of the destination rectangle.
         */
        private int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
        corner of the destination rectangle.
         */
        private int xDest;
        
        /**
         * A variable-sized Bitmap16 Object that defines source image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        private HwmfBitmap16 target;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.bitBlt;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize/2 != ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();
            
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            ySrc = leis.readShort();
            xSrc = leis.readShort();

            size = 4*LittleEndianConsts.SHORT_SIZE;
            
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            
            height = leis.readShort();
            width = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();

            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new HwmfBitmap16();
                size += target.init(leis);
            }
            
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            
        }
    }


    /**
     * The META_SETDIBTODEV record sets a block of pixels in the playback device context
     * using deviceindependent color data.
     * The source of the color data is a DIB
     */
    public static class WmfSetDibToDev implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of the
         * DIB contains explicit RGB values or indexes into a palette.
         */
        private ColorUsage colorUsage;  
        /**
         * A 16-bit unsigned integer that defines the number of scan lines in the source.
         */
        private int scanCount;
        /**
         * A 16-bit unsigned integer that defines the starting scan line in the source.
         */
        private int startScan;  
        /**
         * A 16-bit unsigned integer that defines the y-coordinate, in logical units, of the
         * source rectangle.
         */
        private int yDib;  
        /**
         * A 16-bit unsigned integer that defines the x-coordinate, in logical units, of the
         * source rectangle.
         */
        private int xDib;  
        /**
         * A 16-bit unsigned integer that defines the height, in logical units, of the
         * source and destination rectangles.
         */
        private int height;
        /**
         * A 16-bit unsigned integer that defines the width, in logical units, of the
         * source and destination rectangles.
         */
        private int width;
        /**
         * A 16-bit unsigned integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the destination rectangle.
         */
        private int yDest;
        /**
         * A 16-bit unsigned integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the destination rectangle.
         */
        private int xDest;
        /**
         * A variable-sized DeviceIndependentBitmap Object that is the source of the color data.
         */
        private HwmfBitmapDib dib;        
        
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setDibToDev;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorUsage = ColorUsage.valueOf(leis.readUShort());
            scanCount = leis.readUShort();
            startScan = leis.readUShort();
            yDib = leis.readUShort();
            xDib = leis.readUShort();
            height = leis.readUShort();
            width = leis.readUShort();
            yDest = leis.readUShort();
            xDest = leis.readUShort();
            
            int size = 9*LittleEndianConsts.SHORT_SIZE;
            dib = new HwmfBitmapDib();
            size += dib.init(leis, (int)(recordSize-6-size));
            
            return size;
        }        

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            
        }

        @Override
        public BufferedImage getImage() {
            return dib.getImage();
        }
    }


    public static class WmfDibBitBlt implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {

        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the
         * new image. This code MUST be one of the values in the Ternary Raster Operation Enumeration.
         */
        HwmfTernaryRasterOp rasterOperation;  
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the source rectangle.
         */
        private int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the source rectangle.
         */
        private int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source and 
         * destination rectangles.
         */
        private int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source and destination
         * rectangles.
         */
        private int width;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the upper-left
         * corner of the destination rectangle.
         */
        private int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the upper-left 
         * corner of the destination rectangle.
         */
        private int xDest;
        
        /**
         * A variable-sized DeviceIndependentBitmap Object that defines image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        private HwmfBitmapDib target;
        
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.dibBitBlt;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize/2 != ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();
            
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            ySrc = leis.readShort();
            xSrc = leis.readShort();
            size = 4*LittleEndianConsts.SHORT_SIZE;
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            height = leis.readShort();
            width = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();
            
            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new HwmfBitmapDib();
                size += target.init(leis, (int)(recordSize-6-size));
            }
            
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            
        }

        @Override
        public BufferedImage getImage() {
            return (target == null) ? null : target.getImage();
        }
    }

    public static class WmfDibStretchBlt implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the
         * new image. This code MUST be one of the values in the Ternary Raster Operation Enumeration.
         */
        private HwmfTernaryRasterOp rasterOperation;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the source rectangle.
         */
        private int srcHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the source rectangle.
         */
        private int srcWidth;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the source rectangle.
         */
        private int ySrc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the source rectangle.
         */
        private int xSrc;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the
         * destination rectangle.
         */
        private int destHeight;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the
         * destination rectangle.
         */
        private int destWidth;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units,
         * of the upper-left corner of the destination rectangle.
         */
        private int yDest;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units,
         * of the upper-left corner of the destination rectangle.
         */
        private int xDest;
        /**
         * A variable-sized DeviceIndependentBitmap Object that defines image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        HwmfBitmapDib target;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.dibStretchBlt;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            boolean hasBitmap = (recordSize > ((recordFunction >> 8) + 3));

            int size = 0;
            int rasterOpCode = leis.readUShort();
            int rasterOpIndex = leis.readUShort();
            
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
            assert(rasterOpCode == rasterOperation.opCode);

            srcHeight = leis.readShort();
            srcWidth = leis.readShort();
            ySrc = leis.readShort();
            xSrc = leis.readShort();
            size = 6*LittleEndianConsts.SHORT_SIZE;
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            destHeight = leis.readShort();
            destWidth = leis.readShort();
            yDest = leis.readShort();
            xDest = leis.readShort();
            size += 4*LittleEndianConsts.SHORT_SIZE;
            if (hasBitmap) {
                target = new HwmfBitmapDib();
                size += target.init(leis, (int)(recordSize-6-size));
            }
            
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            
        }

        @Override
        public BufferedImage getImage() {
            return target.getImage();
        }
    }
}
