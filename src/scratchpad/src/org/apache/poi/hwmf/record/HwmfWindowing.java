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
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfWindowing {

    /**
     * The META_SETVIEWPORTORG record defines the viewport origin in the playback device context.
     */
    public static class WmfSetViewportOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        private int y;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        private int x;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setViewportOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setViewportOrg(x, y);
        }
    }

    /**
     * The META_SETVIEWPORTEXT record sets the horizontal and vertical extents
     * of the viewport in the playback device context.
     */
    public static class WmfSetViewportExt implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical extent
         * of the viewport in device units.
         */
        private int height;

        /**
         * A 16-bit signed integer that defines the horizontal extent
         * of the viewport in device units.
         */
        private int width;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setViewportExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            height = leis.readShort();
            width = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setViewportExt(width, height);
        }
    }

    /**
     * The META_OFFSETVIEWPORTORG record moves the viewport origin in the playback device context
     * by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetViewportOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        private int yOffset;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        private int xOffset;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetViewportOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Rectangle2D viewport = ctx.getProperties().getViewport();
            double x = (viewport == null) ? 0 : viewport.getX();
            double y = (viewport == null) ? 0 : viewport.getY();
            ctx.getProperties().setViewportOrg(x+xOffset, y+yOffset);
        }
    }

    /**
     * The META_SETWINDOWORG record defines the output window origin in the playback device context.
     */
    public static class WmfSetWindowOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units.
         */
        private int y;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units.
         */
        private int x;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setWindowOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setWindowOrg(x, y);
            ctx.updateWindowMapMode();
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }
    }

    /**
     * The META_SETWINDOWEXT record defines the horizontal and vertical extents
     * of the output window in the playback device context.
     */
    public static class WmfSetWindowExt implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical extent of
         * the window in logical units.
         */
        private int height;

        /**
         * A 16-bit signed integer that defines the horizontal extent of
         * the window in logical units.
         */
        private int width;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setWindowExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            height = leis.readShort();
            width = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setWindowExt(width, height);
            ctx.updateWindowMapMode();
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfOffsetWindowOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        private int yOffset;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        private int xOffset;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetWindowOrg;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Rectangle2D window = ctx.getProperties().getWindow();
            ctx.getProperties().setWindowOrg(window.getX()+xOffset, window.getY()+yOffset);
            ctx.updateWindowMapMode();
        }
    }

    /**
     * The META_OFFSETWINDOWORG record moves the output window origin in the
     * playback device context by specified horizontal and vertical offsets.
     */
    public static class WmfScaleWindowExt implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current y-extent by the value of the yNum member.
         */
        private int yDenom;

        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current y-extent.
         */
        private int yNum;

        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current x-extent by the value of the xNum member.
         */
        private int xDenom;

        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current x-extent.
         */
        private int xNum;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.scaleWindowExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yDenom = leis.readShort();
            yNum = leis.readShort();
            xDenom = leis.readShort();
            xNum = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Rectangle2D window = ctx.getProperties().getWindow();
            double width = window.getWidth() * xNum / xDenom;
            double height = window.getHeight() * yNum / yDenom;
            ctx.getProperties().setWindowExt(width, height);
            ctx.updateWindowMapMode();
        }
    }


    /**
     * The META_SCALEVIEWPORTEXT record scales the horizontal and vertical extents of the viewport
     * that is defined in the playback device context by using the ratios formed by the specified
     * multiplicands and divisors.
     */
    public static class WmfScaleViewportExt implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current y-extent by the value of the yNum member.
         */
        private int yDenom;

        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current y-extent.
         */
        private int yNum;

        /**
         * A 16-bit signed integer that defines the amount by which to divide the
         * result of multiplying the current x-extent by the value of the xNum member.
         */
        private int xDenom;

        /**
         * A 16-bit signed integer that defines the amount by which to multiply the
         * current x-extent.
         */
        private int xNum;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.scaleViewportExt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yDenom = leis.readShort();
            yNum = leis.readShort();
            xDenom = leis.readShort();
            xNum = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Rectangle2D viewport = ctx.getProperties().getViewport();
            if (viewport == null) {
                viewport = ctx.getProperties().getWindow();
            }
            double width = viewport.getWidth() * xNum / xDenom;
            double height = viewport.getHeight() * yNum / yDenom;
            ctx.getProperties().setViewportExt(width, height);
        }
    }

    /**
     * The META_OFFSETCLIPRGN record moves the clipping region in the playback device context by the
     * specified offsets.
     */
    public static class WmfOffsetClipRgn implements HwmfRecord, HwmfObjectTableEntry {

        /**
         * A 16-bit signed integer that defines the number of logical units to move up or down.
         */
        private int yOffset;

        /**
         * A 16-bit signed integer that defines the number of logical units to move left or right.
         */
        private int xOffset;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetClipRgn;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yOffset = leis.readShort();
            xOffset = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
        }
    }

    /**
     * The META_EXCLUDECLIPRECT record sets the clipping region in the playback device context to the
     * existing clipping region minus the specified rectangle.
     */
    public static class WmfExcludeClipRect implements HwmfRecord, HwmfObjectTableEntry {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int bottom;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int right;

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int top;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int left;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.excludeClipRect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottom = leis.readShort();
            right = leis.readShort();
            top = leis.readShort();
            left = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
        }
    }


    /**
     * The META_INTERSECTCLIPRECT record sets the clipping region in the playback device context to the
     * intersection of the existing clipping region and the specified rectangle.
     */
    public static class WmfIntersectClipRect implements HwmfRecord, HwmfObjectTableEntry {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int bottom;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int right;

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int top;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int left;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.intersectClipRect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottom = leis.readShort();
            right = leis.readShort();
            top = leis.readShort();
            left = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
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
        public HwmfRecordType getRecordType() {
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
        private int left_scanline[];
        /**
         * A 16-bit unsigned integer that defines the horizontal (x-axis) coordinate,
         * in logical units, of the right endpoint of the scanline.
         */
        private int right_scanline[];
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

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int bottom;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * lower-right corner of the rectangle.
         */
        private int right;

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int top;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int left;

        /**
         * An array of Scan objects that define the scanlines in the region.
         */
        private WmfScanObject scanObjects[];

        @Override
        public HwmfRecordType getRecordType() {
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
            left = leis.readShort();
            top = leis.readShort();
            right = leis.readShort();
            bottom = leis.readShort();
            
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
