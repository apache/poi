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

package org.apache.poi.hemf.record.emf;

import static org.apache.poi.hemf.record.emf.HemfDraw.readRectL;
import static org.apache.poi.hemf.record.emf.HemfDraw.readPointL;
import static org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.apache.poi.hwmf.record.HwmfColorRef;
import org.apache.poi.hwmf.record.HwmfDraw;
import org.apache.poi.hwmf.record.HwmfFill;
import org.apache.poi.hwmf.record.HwmfFill.ColorUsage;
import org.apache.poi.hwmf.record.HwmfTernaryRasterOp;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfFill {
    private static final int MAX_RECORD_LENGTH = 10_000_000;

    public enum HemfRegionMode {
        RGN_AND(0x01),
        RGN_OR(0x02),
        RGN_XOR(0x03),
        RGN_DIFF(0x04),
        RGN_COPY(0x05);

        int flag;
        HemfRegionMode(int flag) {
            this.flag = flag;
        }

        public static HemfRegionMode valueOf(int flag) {
            for (HemfRegionMode rm : values()) {
                if (rm.flag == flag) return rm;
            }
            return null;
        }

    }


    /**
     * The EMR_SETPOLYFILLMODE record defines polygon fill mode.
     */
    public static class EmfSetPolyfillMode extends HwmfFill.WmfSetPolyfillMode implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setPolyfillMode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the polygon fill mode and
            // MUST be in the PolygonFillMode enumeration.
            polyfillMode = HwmfPolyfillMode.valueOf((int)leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }
    }

    public static class EmfExtFloodFill extends HwmfFill.WmfExtFloodFill implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extFloodFill;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readPointL(leis, start);
            size = colorRef.init(leis);
            // A 32-bit unsigned integer that specifies how to use the Color value to determine the area for
            // the flood fill operation. The value MUST be in the FloodFill enumeration
            mode = (int)leis.readUInt();
            return size + LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EMR_STRETCHBLT record specifies a block transfer of pixels from a source bitmap to a destination rectangle,
     * optionally in combination with a brush pattern, according to a specified raster operation, stretching or
     * compressing the output to fit the dimensions of the destination, if necessary.
     */
    public static class EmfStretchBlt extends HwmfFill.WmfBitBlt implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        /** An XForm object that specifies a world-space to page-space transform to apply to the source bitmap. */
        protected final byte[] xformSrc = new byte[24];

        /** A WMF ColorRef object that specifies the background color of the source bitmap. */
        protected final HwmfColorRef bkColorSrc = new HwmfColorRef();

        /**
         * A 32-bit unsigned integer that specifies how to interpret values in the color table in
         * the source bitmap header. This value MUST be in the DIBColors enumeration
         */
        protected int usageSrc;

        /** The source bitmap header. */
        protected byte[] bmiSrc;

        /** The source bitmap bits. */
        protected byte[] bitsSrc;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.stretchBlt;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            size += readBounds2(leis, this.dstBounds);

            // A 32-bit unsigned integer that specifies the raster operation code. This code defines how the
            // color data of the source rectangle is to be combined with the color data of the destination
            // rectangle and optionally a brush pattern, to achieve the final color.
            int rasterOpIndex = (int)leis.readUInt();

            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);

            size += LittleEndianConsts.INT_SIZE;

            final Point2D srcPnt = new Point2D.Double();
            size += readPointL(leis, srcPnt);

            leis.readFully(xformSrc);
            size += 24;

            size += bkColorSrc.init(leis);

            usageSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();

            size += 5*LittleEndianConsts.INT_SIZE;

            if (srcEqualsDstDimension()) {
                srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), dstBounds.getWidth(), dstBounds.getHeight());
            } else {
                int srcWidth = leis.readInt();
                int srcHeight = leis.readInt();
                size += 2 * LittleEndianConsts.INT_SIZE;
                srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), srcWidth, srcHeight);
            }

            // size + type and size field
            final int undefinedSpace1 = (int)(offBmiSrc - size - HEADER_SIZE);
            assert(undefinedSpace1 >= 0);
            leis.skipFully(undefinedSpace1);
            size += undefinedSpace1;

            bmiSrc = IOUtils.safelyAllocate(cbBmiSrc, MAX_RECORD_LENGTH);
            leis.readFully(bmiSrc);
            size += cbBmiSrc;

            final int undefinedSpace2 = (int)(offBitsSrc - size - HEADER_SIZE);
            assert(undefinedSpace2 >= 0);
            leis.skipFully(undefinedSpace2);
            size += undefinedSpace2;

            bitsSrc = IOUtils.safelyAllocate(cbBitsSrc, MAX_RECORD_LENGTH);
            leis.readFully(bitsSrc);
            size += cbBitsSrc;

            return size;
        }

        protected boolean srcEqualsDstDimension() {
            return false;
        }
    }

    /**
     * The EMR_BITBLT record specifies a block transfer of pixels from a source bitmap to a destination rectangle,
     * optionally in combination with a brush pattern, according to a specified raster operation.
     */
    public static class EmfBitBlt extends EmfStretchBlt {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.bitBlt;
        }

        @Override
        protected boolean srcEqualsDstDimension() {
            return false;
        }
    }


    /** The EMR_FRAMERGN record draws a border around the specified region using the specified brush. */
    public static class EmfFrameRgn extends HwmfDraw.WmfFrameRegion implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.frameRgn;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            // A 32-bit unsigned integer that specifies the brush EMF Object Table index.
            brushIndex = (int)leis.readUInt();
            // A 32-bit signed integer that specifies the width of the vertical brush stroke, in logical units.
            width = leis.readInt();
            // A 32-bit signed integer that specifies the height of the horizontal brush stroke, in logical units.
            height = leis.readInt();
            size += 4*LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(brushIndex);

            Area frame = new Area();
            for (Rectangle2D rct : rgnRects) {
                frame.add(new Area(rct));
            }
            Rectangle2D frameBounds = frame.getBounds2D();
            AffineTransform at = new AffineTransform();
            at.translate(bounds.getX()-frameBounds.getX(), bounds.getY()-frameBounds.getY());
            at.scale(bounds.getWidth()/frameBounds.getWidth(), bounds.getHeight()/frameBounds.getHeight());
            frame.transform(at);

            ctx.fill(frame);
        }
    }

    /** The EMR_INVERTRGN record inverts the colors in the specified region. */
    public static class EmfInvertRgn implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.invertRgn;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }
    }

    /**
     * The EMR_PAINTRGN record paints the specified region by using the brush currently selected into the
     * playback device context.
     */
    public static class EmfPaintRgn extends EmfInvertRgn {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.paintRgn;
        }
    }

    /** The EMR_FILLRGN record fills the specified region by using the specified brush. */
    public static class EmfFillRgn extends HwmfFill.WmfFillRegion implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.fillRgn;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            brushIndex = (int)leis.readUInt();
            size += 2*LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }
    }

    public static class EmfExtSelectClipRgn implements HemfRecord {
        protected HemfRegionMode regionMode;
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extSelectClipRgn;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the size of region data in bytes
            long rgnDataSize = leis.readUInt();
            // A 32-bit unsigned integer that specifies the way to use the region.
            regionMode = HemfRegionMode.valueOf((int)leis.readUInt());
            long size = 2* LittleEndianConsts.INT_SIZE;

            // If RegionMode is RGN_COPY, this data can be omitted and the clip region
            // SHOULD be set to the default (NULL) clip region.
            if (regionMode != HemfRegionMode.RGN_COPY) {
                size += readRgnData(leis, rgnRects);
            }
            return size;
        }
    }

    public static class EmfAlphaBlend implements HemfRecord {
        /** the destination bounding rectangle in device units */
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        /** the destination rectangle */
        protected final Rectangle2D destRect = new Rectangle2D.Double();
        /** the source rectangle */
        protected final Rectangle2D srcRect = new Rectangle2D.Double();
        /**
         * The blend operation code. The only source and destination blend operation that has been defined
         * is 0x00, which specifies that the source bitmap MUST be combined with the destination bitmap based
         * on the alpha transparency values of the source pixels.
         */
        protected byte blendOperation;
        /** This value MUST be 0x00 and MUST be ignored. */
        protected byte blendFlags;
        /**
         * An 8-bit unsigned integer that specifies alpha transparency, which determines the blend of the source
         * and destination bitmaps. This value MUST be used on the entire source bitmap. The minimum alpha
         * transparency value, zero, corresponds to completely transparent; the maximum value, 0xFF, corresponds
         * to completely opaque. In effect, a value of 0xFF specifies that the per-pixel alpha values determine
         * the blend of the source and destination bitmaps.
         */
        protected int srcConstantAlpha;
        /**
         * A byte that specifies how source and destination pixels are interpreted with respect to alpha transparency.
         *
         * 0x00:
         * The pixels in the source bitmap do not specify alpha transparency.
         * In this case, the SrcConstantAlpha value determines the blend of the source and destination bitmaps.
         * Note that in the following equations SrcConstantAlpha is divided by 255,
         * which produces a value in the range 0 to 1.
         *
         * 0x01: "AC_SRC_ALPHA"
         * Indicates that the source bitmap is 32 bits-per-pixel and specifies an alpha transparency value
         * for each pixel.
         */
        protected byte alphaFormat;
        /** a world-space to page-space transform to apply to the source bitmap. */
        protected final AffineTransform xFormSrc = new AffineTransform();
        /** the background color of the source bitmap. */
        protected final HwmfColorRef bkColorSrc = new HwmfColorRef();
        /**
         * A 32-bit unsigned integer that specifies how to interpret values in the
         * color table in the source bitmap header.
         */
        protected ColorUsage usageSrc;

        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.alphaBlend;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            long size = readRectL(leis, bounds);
            size += readBounds2(leis, destRect);

            blendOperation = leis.readByte();
            assert (blendOperation == 0);
            blendFlags = leis.readByte();
            assert (blendOperation == 0);
            srcConstantAlpha = leis.readUByte();
            alphaFormat = leis.readByte();

            // A 32-bit signed integer that specifies the logical x-coordinate of the upper-left
            // corner of the source rectangle.
            final int xSrc = leis.readInt();
            // A 32-bit signed integer that specifies the logical y-coordinate of the upper-left
            // corner of the source rectangle.
            final int ySrc = leis.readInt();

            size += 3*LittleEndianConsts.INT_SIZE;
            size += readXForm(leis, xFormSrc);
            size += bkColorSrc.init(leis);

            usageSrc = ColorUsage.valueOf((int)leis.readUInt());


            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();

            // A 32-bit signed integer that specifies the logical width of the source rectangle.
            // This value MUST be greater than zero.
            final int cxSrc = leis.readInt();
            // A 32-bit signed integer that specifies the logical height of the source rectangle.
            // This value MUST be greater than zero.
            final int cySrc = leis.readInt();

            srcRect.setRect(xSrc, ySrc, cxSrc, cySrc);

            size += 7 * LittleEndianConsts.INT_SIZE;

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }
    }

    public static class EmfSetDiBitsToDevice implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final Point2D dest = new Point2D.Double();
        protected final Rectangle2D src = new Rectangle2D.Double();
        protected ColorUsage usageSrc;
        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setDiBitsToDevice;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            int startIdx = leis.getReadIndex();

            // A WMF RectL object that defines the destination bounding rectangle in device units.
            long size = readRectL(leis, bounds);
            // the logical x/y-coordinate of the upper-left corner of the destination rectangle.
            size += readPointL(leis, dest);
            // the source rectangle
            size += readBounds2(leis, src);
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies how to interpret values in the color table
            // in the source bitmap header. This value MUST be in the DIBColors enumeration
            usageSrc = ColorUsage.valueOf((int)leis.readUInt());
            // A 32-bit unsigned integer that specifies the first scan line in the array.
            final int iStartScan = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the number of scan lines.
            final int cScans = (int)leis.readUInt();
            size += 7*LittleEndianConsts.INT_SIZE;

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }
    }

    static long readBitmap(final LittleEndianInputStream leis, final HwmfBitmapDib bitmap,
            final int startIdx, final int offBmiSrc, final int cbBmiSrc, final int offBitsSrc, int cbBitsSrc)
    throws IOException {
        final int offCurr = leis.getReadIndex()-(startIdx-HEADER_SIZE);
        final int undefinedSpace1 = offBmiSrc-offCurr;
        assert(undefinedSpace1 >= 0);

        final int undefinedSpace2 = offBitsSrc-offCurr-cbBmiSrc-undefinedSpace1;
        assert(undefinedSpace2 >= 0);

        leis.skipFully(undefinedSpace1);

        if (cbBmiSrc == 0 || cbBitsSrc == 0) {
            return undefinedSpace1;
        }

        final LittleEndianInputStream leisDib;
        if (undefinedSpace2 == 0) {
            leisDib = leis;
        } else {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(cbBmiSrc+cbBitsSrc);
            final long cbBmiSrcAct = IOUtils.copy(leis, bos, cbBmiSrc);
            assert (cbBmiSrcAct == cbBmiSrc);
            leis.skipFully(undefinedSpace2);
            final long cbBitsSrcAct = IOUtils.copy(leis, bos, cbBitsSrc);
            assert (cbBitsSrcAct == cbBitsSrc);
            leisDib = new LittleEndianInputStream(new ByteArrayInputStream(bos.toByteArray()));
        }
        final int dibSize = cbBmiSrc+cbBitsSrc;
        final int dibSizeAct = bitmap.init(leisDib, dibSize);
        assert (dibSizeAct <= dibSize);
        return undefinedSpace1 + cbBmiSrc + undefinedSpace2 + cbBitsSrc;
    }


    static long readRgnData(final LittleEndianInputStream leis, final List<Rectangle2D> rgnRects) {
        // *** RegionDataHeader ***
        // A 32-bit unsigned integer that specifies the size of this object in bytes. This MUST be 0x00000020.
        long rgnHdrSiez = leis.readUInt();
        assert(rgnHdrSiez == 0x20);
        // A 32-bit unsigned integer that specifies the region type. This SHOULD be RDH_RECTANGLES (0x00000001)
        long rgnHdrType = leis.readUInt();
        assert(rgnHdrType == 1);
        // A 32-bit unsigned integer that specifies the number of rectangles in this region.
        long rgnCntRect = leis.readUInt();
        // A 32-bit unsigned integer that specifies the size of the buffer of rectangles in bytes.
        long rgnCntBytes = leis.readUInt();
        long size = 4*LittleEndianConsts.INT_SIZE;
        // A 128-bit WMF RectL object, which specifies the bounds of the region.
        Rectangle2D rgnBounds = new Rectangle2D.Double();
        size += readRectL(leis, rgnBounds);
        for (int i=0; i<rgnCntRect; i++) {
            Rectangle2D rgnRct = new Rectangle2D.Double();
            size += readRectL(leis, rgnRct);
            rgnRects.add(rgnRct);
        }
        return size;
    }


    static int readBounds2(LittleEndianInputStream leis, Rectangle2D bounds) {
        /**
         * The 32-bit signed integers that defines the corners of the bounding rectangle.
         */
        int x = leis.readInt();
        int y = leis.readInt();
        int w = leis.readInt();
        int h = leis.readInt();

        bounds.setRect(x, y, w, h);

        return 4 * LittleEndianConsts.INT_SIZE;
    }

    static int readXForm(LittleEndianInputStream leis, AffineTransform xform) {
        // mapping <java AffineTransform> = <xform>:
        // m00 (scaleX) = eM11 (Horizontal scaling component)
        // m11 (scaleY) = eM22 (Vertical scaling component)
        // m01 (shearX) = eM12 (Horizontal proportionality constant)
        // m10 (shearY) = eM21 (Vertical proportionality constant)
        // m02 (translateX) = eDx (The horizontal translation component, in logical units.)
        // m12 (translateY) = eDy (The vertical translation component, in logical units.)

        // A 32-bit floating-point value of the transform matrix.
        double eM11 = leis.readFloat();

        // A 32-bit floating-point value of the transform matrix.
        double eM12 = leis.readFloat();

        // A 32-bit floating-point value of the transform matrix.
        double eM21 = leis.readFloat();

        // A 32-bit floating-point value of the transform matrix.
        double eM22 = leis.readFloat();

        // A 32-bit floating-point value that contains a horizontal translation component, in logical units.
        double eDx = leis.readFloat();

        // A 32-bit floating-point value that contains a vertical translation component, in logical units.
        double eDy = leis.readFloat();

        xform.setTransform(eM11, eM21, eM12, eM22, eDx, eDy);

        return 6 * LittleEndian.INT_SIZE;
    }
}
