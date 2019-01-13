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

import static org.apache.poi.hwmf.record.HwmfDraw.boundsToString;
import static org.apache.poi.hwmf.record.HwmfDraw.dimToString;
import static org.apache.poi.hwmf.record.HwmfDraw.normalizeBounds;
import static org.apache.poi.hwmf.record.HwmfDraw.pointToString;
import static org.apache.poi.hwmf.record.HwmfDraw.readBounds;
import static org.apache.poi.hwmf.record.HwmfDraw.readPointS;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfWindowing {

    /**
     * The META_SETVIEWPORTORG record defines the viewport origin in the playback device context.
     */
    public static class WmfSetViewportOrg implements HwmfRecord {

        protected final Point2D origin = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setViewportOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readPointS(leis, origin);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D old = prop.getViewport();
            double oldX = (old == null ? 0 : old.getX());
            double oldY = (old == null ? 0 : old.getY());
            if (oldX != origin.getX() || oldY != origin.getY()) {
                prop.setViewportOrg(origin.getX(), origin.getY());
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return pointToString(origin);
        }
    }

    /**
     * The META_SETVIEWPORTEXT record sets the horizontal and vertical extents
     * of the viewport in the playback device context.
     */
    public static class WmfSetViewportExt implements HwmfRecord {

        protected final Dimension2D extents = new Dimension2DDouble();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setViewportExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // A signed integer that defines the vertical extent of the viewport in device units.
            int height = leis.readShort();
            // A signed integer that defines the horizontal extent of the viewport in device units.
            int width = leis.readShort();
            extents.setSize(width, height);
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D old = prop.getViewport();
            double oldW = (old == null ? 0 : old.getWidth());
            double oldH = (old == null ? 0 : old.getHeight());
            if (oldW != extents.getWidth() || oldH != extents.getHeight()) {
                prop.setViewportExt(extents.getWidth(), extents.getHeight());
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return dimToString(extents);
        }
    }

    /**
     * The META_OFFSETVIEWPORTORG record moves the viewport origin in the playback device context
     * by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetViewportOrg implements HwmfRecord {

        protected final Point2D offset = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.offsetViewportOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readPointS(leis, offset);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D viewport = prop.getViewport();
            if (offset.getX() != 0 || offset.getY() != 0) {
                double x = (viewport == null) ? 0 : viewport.getX();
                double y = (viewport == null) ? 0 : viewport.getY();
                prop.setViewportOrg(x + offset.getX(), y + offset.getY());
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return pointToString(offset);
        }
    }

    /**
     * The META_SETWINDOWORG record defines the output window origin in the playback device context.
     */
    public static class WmfSetWindowOrg implements HwmfRecord {

        protected final Point2D origin = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setWindowOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readPointS(leis, origin);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            final Rectangle2D old = prop.getWindow();
            if (old.getX() != getX() || old.getY() != getY()) {
                prop.setWindowOrg(getX(), getY());
                ctx.updateWindowMapMode();
            }
        }

        public double getY() {
            return origin.getY();
        }

        public double getX() {
            return origin.getX();
        }

        @Override
        public String toString() {
            return pointToString(origin);
        }
    }

    /**
     * The META_SETWINDOWEXT record defines the horizontal and vertical extents
     * of the output window in the playback device context.
     */
    public static class WmfSetWindowExt implements HwmfRecord {

        protected final Dimension2D size = new Dimension2DDouble();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setWindowExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // A signed integer that defines the vertical extent of the window in logical units.
            int height = leis.readShort();
            // A signed integer that defines the horizontal extent of the window in logical units.
            int width = leis.readShort();
            size.setSize(width, height);
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D old = prop.getWindow();
            double oldW = (old == null ? 0 : old.getWidth());
            double oldH = (old == null ? 0 : old.getHeight());
            if (oldW != size.getWidth() || oldH != size.getHeight()) {
                prop.setWindowExt(size.getWidth(), size.getHeight());
                ctx.updateWindowMapMode();
            }
        }

        public Dimension2D getSize() {
            return size;
        }

        @Override
        public String toString() {
            return dimToString(size);
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetWindowOrg implements HwmfRecord {

        protected final Point2D offset = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.offsetWindowOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readPointS(leis, offset);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D old = prop.getWindow();
            if (offset.getX() != 0 || offset.getY() != 0) {
                prop.setWindowOrg(old.getX() + offset.getX(), old.getY() + offset.getY());
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return pointToString(offset);
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfScaleWindowExt implements HwmfRecord {

        protected final Dimension2D scale = new Dimension2DDouble();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.scaleWindowExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // A signed integer that defines the amount by which to divide the
            // result of multiplying the current y-extent by the value of the yNum member.
            double yDenom = leis.readShort();
            // A signed integer that defines the amount by which to multiply the
            // current y-extent.
            double yNum = leis.readShort();
            // A signed integer that defines the amount by which to divide the
            // result of multiplying the current x-extent by the value of the xNum member.
            double xDenom = leis.readShort();
            // A signed integer that defines the amount by which to multiply the
            // current x-extent.
            double xNum = leis.readShort();

            scale.setSize(xNum / xDenom, yNum / yDenom);

            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            Rectangle2D old = prop.getWindow();
            if (scale.getWidth() != 1.0 || scale.getHeight() != 1.0) {
                double width = old.getWidth() * scale.getWidth();
                double height = old.getHeight() * scale.getHeight();
                ctx.getProperties().setWindowExt(width, height);
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return "{ scaleX: "+scale.getWidth()+", scaleY: "+scale.getHeight()+" }";
        }
    }


    /**
     * The META_SCALEVIEWPORTEXT record scales the horizontal and vertical extents of the viewport
     * that is defined in the playback device context by using the ratios formed by the specified
     * multiplicands and divisors.
     */
    public static class WmfScaleViewportExt implements HwmfRecord {

        protected final Dimension2D scale = new Dimension2DDouble();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.scaleViewportExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // A signed integer that defines the amount by which to divide the
            // result of multiplying the current y-extent by the value of the yNum member.
            double yDenom = leis.readShort();
            // A signed integer that defines the amount by which to multiply the
            // current y-extent.
            double yNum = leis.readShort();
            // A signed integer that defines the amount by which to divide the
            // result of multiplying the current x-extent by the value of the xNum member.
            double xDenom = leis.readShort();
            // A signed integer that defines the amount by which to multiply the
            // current x-extent.
            double xNum = leis.readShort();

            scale.setSize(xNum / xDenom, yNum / yDenom);

            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            final HwmfDrawProperties prop = ctx.getProperties();
            final Rectangle2D old = prop.getViewport() == null ? prop.getWindow() : prop.getViewport();

            if (scale.getWidth() != 1.0 || scale.getHeight() != 1.0) {
                double width = old.getWidth() * scale.getWidth();
                double height = old.getHeight() * scale.getHeight();
                prop.setViewportExt(width, height);
                ctx.updateWindowMapMode();
            }
        }

        @Override
        public String toString() {
            return "{ scaleX: "+scale.getWidth()+", scaleY: "+scale.getHeight()+" }";
        }
    }

    /**
     * The META_OFFSETCLIPRGN record moves the clipping region in the playback device context by the
     * specified offsets.
     */
    public static class WmfOffsetClipRgn implements HwmfRecord, HwmfObjectTableEntry {

        protected final Point2D offset = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.offsetClipRgn;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readPointS(leis, offset);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
        }

        @Override
        public String toString() {
            return pointToString(offset);
        }
    }

    /**
     * The META_EXCLUDECLIPRECT record sets the clipping region in the playback device context to the
     * existing clipping region minus the specified rectangle.
     */
    public static class WmfExcludeClipRect implements HwmfRecord, HwmfObjectTableEntry {

        /** a rectangle in logical units */
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.excludeClipRect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readBounds(leis, bounds);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            ctx.setClip(normalizeBounds(bounds), HwmfRegionMode.RGN_DIFF, false);
        }

        @Override
        public String toString() {
            return boundsToString(bounds);
        }
    }


    /**
     * The META_INTERSECTCLIPRECT record sets the clipping region in the playback device context to the
     * intersection of the existing clipping region and the specified rectangle.
     */
    public static class WmfIntersectClipRect implements HwmfRecord, HwmfObjectTableEntry {

        /** a rectangle in logical units */
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.intersectClipRect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return readBounds(leis, bounds);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            ctx.setClip(bounds, HwmfRegionMode.RGN_AND, true);
        }

        @Override
        public String toString() {
            return boundsToString(bounds);
        }
    }

    /**
     * The META_SELECTCLIPREGION record specifies a Region Object to be the current clipping region.
     */
    public static class WmfSelectClipRegion implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be clipped.
         */
        private int region;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.selectClipRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }

    public static class WmfScanObject {
        /**
         * A 16-bit unsigned integer that specifies the number of horizontal (x-axis)
         * coordinates in the ScanLines array. This value MUST be a multiple of 2, since left and right
         * endpoints are required to specify each scanline.
         */
        private int count;
        /**
         * A 16-bit unsigned integer that defines the vertical (y-axis) coordinate, in logical units, of the top scanline.
         */
        private int top;
        /**
         * A 16-bit unsigned integer that defines the vertical (y-axis) coordinate, in logical units, of the bottom scanline.
         */
        private int bottom;
        /**
         * A 16-bit unsigned integer that defines the horizontal (x-axis) coordinate,
         * in logical units, of the left endpoint of the scanline.
         */
        private int[] left_scanline;
        /**
         * A 16-bit unsigned integer that defines the horizontal (x-axis) coordinate,
         * in logical units, of the right endpoint of the scanline.
         */
        private int[] right_scanline;
        /**
         * A 16-bit unsigned integer that MUST be the same as the value of the Count
         * field; it is present to allow upward travel in the structure.
         */
        private int count2;

        public int init(LittleEndianInputStream leis) {
            count = leis.readUShort();
            top = leis.readUShort();
            bottom = leis.readUShort();
            int size = 3*LittleEndianConsts.SHORT_SIZE;
            left_scanline = new int[count/2];
            right_scanline = new int[count/2];
            for (int i=0; i<count/2; i++) {
                left_scanline[i] = leis.readUShort();
                right_scanline[i] = leis.readUShort();
                size += 2*LittleEndianConsts.SHORT_SIZE;
            }
            count2 = leis.readUShort();
            size += LittleEndianConsts.SHORT_SIZE;
            return size;
        }
    }

    public static class WmfCreateRegion implements HwmfRecord, HwmfObjectTableEntry {
        /**
         * A 16-bit signed integer. A value that MUST be ignored.
         */
        private int nextInChain;
        /**
         * A 16-bit signed integer that specifies the region identifier. It MUST be 0x0006.
         */
        private int objectType;
        /**
         * A 32-bit unsigned integer. A value that MUST be ignored.
         */
        private int objectCount;
        /**
         * A 16-bit signed integer that defines the size of the region in bytes plus the size of aScans in bytes.
         */
        private int regionSize;
        /**
         * A 16-bit signed integer that defines the number of scanlines composing the region.
         */
        private int scanCount;

        /**
         * A 16-bit signed integer that defines the maximum number of points in any one scan in this region.
         */
        private int maxScan;

        private Rectangle2D bounds = new Rectangle2D.Double();

        /**
         * An array of Scan objects that define the scanlines in the region.
         */
        private WmfScanObject[] scanObjects;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.createRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            nextInChain = leis.readShort();
            objectType = leis.readShort();
            objectCount = leis.readInt();
            regionSize = leis.readShort();
            scanCount = leis.readShort();
            maxScan = leis.readShort();
            // A 16-bit signed integer that defines the x-coordinate, in logical units, of the
            // upper-left corner of the rectangle.
            double left = leis.readShort();
            // A 16-bit signed integer that defines the y-coordinate, in logical units, of the
            // upper-left corner of the rectangle.
            double top = leis.readShort();
            // A 16-bit signed integer that defines the x-coordinate, in logical units, of the
            // lower-right corner of the rectangle.
            double right = leis.readShort();
            // A 16-bit signed integer that defines the y-coordinate, in logical units, of the
            // lower-right corner of the rectangle.
            double bottom = leis.readShort();
            bounds.setRect(left, top, right-left, bottom-top);
            
            int size = 9*LittleEndianConsts.SHORT_SIZE+LittleEndianConsts.INT_SIZE;

            scanObjects = new WmfScanObject[scanCount];
            for (int i=0; i<scanCount; i++) {
                size += (scanObjects[i] = new WmfScanObject()).init(leis);
            }

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            Rectangle2D lastRect = null;
            Area scanLines = new Area();
            int count = 0;
            for (WmfScanObject so : scanObjects) {
                int y = Math.min(so.top, so.bottom);
                int h = Math.abs(so.top - so.bottom - 1);
                for (int i=0; i<so.count/2; i++) {
                    int x = Math.min(so.left_scanline[i], so.right_scanline[i]);
                    int w = Math.abs(so.right_scanline[i] - so.left_scanline[i] - 1);
                    lastRect = new Rectangle2D.Double(x,y,w,h);
                    scanLines.add(new Area(lastRect));
                    count++;
                }
            }
            
            Shape region = null;
            if (count > 0) {
                region = (count == 1) ? lastRect : scanLines;
            }

            ctx.getProperties().setRegion(region);
        }
    }
}
