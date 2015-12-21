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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfWindowing {

    /**
     * The META_OFFSETCLIPRGN record moves the clipping region in the playback device context by the
     * specified offsets.
     */
    public static class WmfOffsetClipRgn implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the number of logical units to move up or down.
         */
        int yOffset;

        /**
         * A 16-bit signed integer that defines the number of logical units to move left or right.
         */
        int xOffset;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetClipRgn;
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
    public static class WmfSetViewportOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int y;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int x;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setViewportOrg;
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
    public static class WmfSetViewportExt implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setViewportExt;
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
    public static class WmfOffsetViewportOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int yOffset;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int xOffset;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetViewportOrg;
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
    public static class WmfSetWindowOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units.
         */
        int y;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units.
         */
        int x;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setWindowOrg;
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
    public static class WmfSetWindowExt implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setWindowExt;
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
    public static class WmfOffsetWindowOrg implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical offset, in device units.
         */
        int yOffset;

        /**
         * A 16-bit signed integer that defines the horizontal offset, in device units.
         */
        int xOffset;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.offsetWindowOrg;
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
    public static class WmfScaleWindowExt implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.scaleWindowExt;
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
    public static class WmfScaleViewportExt implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.scaleViewportExt;
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
    public static class WmfExcludeClipRect implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.excludeClipRect;
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
    public static class WmfIntersectClipRect implements HwmfRecord {

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

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.intersectClipRect;
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
    public static class WmfSelectClipRegion implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be clipped.
         */
        int region;

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.selectClipRegion;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }

    public static class WmfScanObject {
        /**
         * A 16-bit unsigned integer that specifies the number of horizontal (x-axis)
         * coordinates in the ScanLines array. This value MUST be a multiple of 2, since left and right
         * endpoints are required to specify each scanline.
         */
        int count;
        /**
         * A 16-bit unsigned integer that defines the vertical (y-axis) coordinate, in logical units, of the top scanline.
         */
        int top;
        /**
         * A 16-bit unsigned integer that defines the vertical (y-axis) coordinate, in logical units, of the bottom scanline.
         */
        int bottom;
        /**
         * A 16-bit unsigned integer that defines the horizontal (x-axis) coordinate,
         * in logical units, of the left endpoint of the scanline.
         */
        int left_scanline[];
        /**
         * A 16-bit unsigned integer that defines the horizontal (x-axis) coordinate,
         * in logical units, of the right endpoint of the scanline.
         */
        int right_scanline[];
        /**
         * A 16-bit unsigned integer that MUST be the same as the value of the Count
         * field; it is present to allow upward travel in the structure.
         */
        int count2;

        public int init(LittleEndianInputStream leis) {
            count = leis.readUShort();
            top = leis.readUShort();
            bottom = leis.readUShort();
            left_scanline = new int[count];
            right_scanline = new int[count];
            for (int i=0; i*2<count; i++) {
                left_scanline[i] = leis.readUShort();
                right_scanline[i] = leis.readUShort();
            }
            count2 = leis.readUShort();
            return 8 + count*4;
        }
    }

    public static class WmfCreateRegion implements HwmfRecord {
        /**
         * A 16-bit signed integer. A value that MUST be ignored.
         */
        int nextInChain;
        /**
         * A 16-bit signed integer that specifies the region identifier. It MUST be 0x0006.
         */
        int objectType;
        /**
         * A 32-bit unsigned integer. A value that MUST be ignored.
         */
        int objectCount;
        /**
         * A 16-bit signed integer that defines the size of the region in bytes plus the size of aScans in bytes.
         */
        int regionSize;
        /**
         * A 16-bit signed integer that defines the number of scanlines composing the region.
         */
        int scanCount;

        /**
         * A 16-bit signed integer that defines the maximum number of points in any one scan in this region.
         */
        int maxScan;

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

        /**
         * An array of Scan objects that define the scanlines in the region.
         */
        WmfScanObject scanObjects[];

        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createRegion;
        }

        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            nextInChain = leis.readShort();
            objectType = leis.readShort();
            objectCount = leis.readUShort();
            regionSize = leis.readShort();
            scanCount = leis.readShort();
            maxScan = leis.readShort();
            bottom = leis.readShort();
            right = leis.readShort();
            top = leis.readShort();
            left = leis.readShort();

            List<WmfScanObject> soList = new ArrayList<WmfScanObject>();
            int scanCountI = 0, size = 0;
            do {
                WmfScanObject so = new WmfScanObject();
                size += so.init(leis);
                scanCountI += so.count;
                soList.add(so);
            } while  (scanCountI < scanCount);
            scanObjects = soList.toArray(new WmfScanObject[soList.size()]);

            return 20 + size;
        }
    }
}
