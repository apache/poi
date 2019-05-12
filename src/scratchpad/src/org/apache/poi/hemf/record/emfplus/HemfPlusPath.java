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

package org.apache.poi.hemf.record.emfplus;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusCompressed;
import org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusRelativePosition;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusPath {

    /** The PathPointType enumeration defines types of points on a graphics path. */
    public enum EmfPlusPathPointType {
        /** Specifies that the point is the starting point of a path. */
        START,
        /** Specifies that the point is one of the two endpoints of a line. */
        LINE,
        // not defined
        UNUSED,
        /** Specifies that the point is an endpoint or control point of a cubic Bezier curve */
        BEZIER;
    }

    public static class EmfPlusPath implements EmfPlusObjectData, EmfPlusCompressed, EmfPlusRelativePosition {
        /**
         * If set, the point types in the PathPointTypes array are specified by EmfPlusPathPointTypeRLE objects,
         * which use run-length encoding (RLE) compression, and/or EmfPlusPathPointType objects.
         * If clear, the point types in the PathPointTypes array are specified by EmfPlusPathPointType objects.
         */
        private static final BitField RLE_COMPRESSED = BitFieldFactory.getInstance(0x00001000);

        /** Specifies that a line segment that passes through the point is dashed. */
        private static final BitField POINT_TYPE_DASHED = BitFieldFactory.getInstance(0x10);

        /** Specifies that the point is a position marker. */
        private static final BitField POINT_TYPE_MARKER = BitFieldFactory.getInstance(0x20);

        /** Specifies that the point is the endpoint of a subpath. */
        private static final BitField POINT_TYPE_CLOSE = BitFieldFactory.getInstance(0x80);

        private static final BitField POINT_TYPE_ENUM = BitFieldFactory.getInstance(0x0F);


        private static final BitField POINT_RLE_BEZIER = BitFieldFactory.getInstance(0x80);

        private static final BitField POINT_RLE_COUNT = BitFieldFactory.getInstance(0x3F);

        private final HemfPlusHeader.EmfPlusGraphicsVersion version = new HemfPlusHeader.EmfPlusGraphicsVersion();
        private int pointFlags;
        private Point2D[] pathPoints;
        private byte[] pointTypes;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            long size = version.init(leis);

            // A 32-bit unsigned integer that specifies the number of points and associated point types that
            // are defined by this object.
            int pointCount = leis.readInt();

            // A 16-bit unsigned integer that specifies how to interpret the points
            // and associated point types that are defined by this object.
            pointFlags = leis.readShort();

            leis.skipFully(LittleEndianConsts.SHORT_SIZE);
            size += 2* LittleEndianConsts.INT_SIZE;

            BiFunction<LittleEndianInputStream,Point2D,Integer> readPoint;

            if (isRelativePosition()) {
                readPoint = HemfPlusDraw::readPointR;
            } else if (isCompressed()) {
                readPoint = HemfPlusDraw::readPointS;
            } else {
                readPoint = HemfPlusDraw::readPointF;
            }

            pathPoints = new Point2D[pointCount];
            for (int i=0; i<pointCount; i++) {
                pathPoints[i] = new Point2D.Double();
                size += readPoint.apply(leis,pathPoints[i]);
            }

            pointTypes = new byte[pointCount];
            final boolean isRLE = RLE_COMPRESSED.isSet(pointFlags);
            if (isRLE) {
                for (int i=0, rleCount; i<pointCount; i+=rleCount, size+=2) {
                    rleCount = POINT_RLE_COUNT.getValue(leis.readByte());
                    Arrays.fill(pointTypes, pointCount, pointCount+rleCount, leis.readByte());
                }
            } else {
                leis.readFully(pointTypes);
                size += pointCount;
            }

            int padding = (int)((4 - (size % 4)) % 4);
            leis.skipFully(padding);
            size += padding;

            return size;
        }

        public boolean isPointDashed(int index) {
            return POINT_TYPE_DASHED.isSet(pointTypes[index]);
        }

        public boolean isPointMarker(int index) {
            return POINT_TYPE_MARKER.isSet(pointTypes[index]);
        }

        public boolean isPointClosed(int index) {
            return POINT_TYPE_CLOSE.isSet(pointTypes[index]);
        }

        public EmfPlusPathPointType getPointType(int index) {
            return EmfPlusPathPointType.values()[POINT_TYPE_ENUM.getValue(pointTypes[index])];
        }

        @Override
        public int getFlags() {
            return pointFlags;
        }
    }


}
