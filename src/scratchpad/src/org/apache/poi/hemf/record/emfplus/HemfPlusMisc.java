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

import static org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readRectF;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.hemf.record.emf.HemfFill;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusMisc {
    public interface EmfPlusObjectId {
        BitField OBJECT_ID = BitFieldFactory.getInstance(0x00FF);

        int getFlags();

        /**
         * The index in the EMF+ Object Table to associate with the object
         * created by this record. The value MUST be zero to 63, inclusive.
         */
        default int getObjectId() {
            return OBJECT_ID.getValue(getFlags());
        }
    }

    public enum CombineMode {
        CombineModeReplace(0x00000000),
        CombineModeIntersect(0x00000001),
        CombineModeUnion(0x00000002),
        CombineModeXOR(0x00000003),
        CombineModeExclude(0x00000004),
        CombineModeComplement(0x00000005)
        ;

        public final int id;

        CombineMode(int id) {
            this.id = id;
        }

        public static CombineMode valueOf(int id) {
            for (CombineMode wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    public static abstract class EmfPlusFlagOnly implements HemfPlusRecord {
        private int flags;
        private HemfPlusRecordType recordType;

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public final HemfPlusRecordType getEmfPlusRecordType() {
            return recordType;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;
            assert(dataSize == 0);
            recordType = HemfPlusRecordType.getById(recordId);
            return 0;
        }
    }

    public static class EmfPlusEOF extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetPixelOffsetMode record specifies how pixels are centered with respect to the
     * coordinates of the drawing surface.
     */
    public static class EmfPlusSetPixelOffsetMode extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetAntiAliasMode record specifies the anti-aliasing mode for text output.
     */
    public static class EmfPlusSetAntiAliasMode extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetCompositingMode record specifies how source colors are combined with background colors.
     */
    public static class EmfPlusSetCompositingMode extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetCompositingQuality record specifies the desired level of quality for creating
     * composite images from multiple objects.
     */
    public static class EmfPlusSetCompositingQuality extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetInterpolationMode record specifies how image scaling, including stretching and
     * shrinking, is performed.
     */
    public static class EmfPlusSetInterpolationMode extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusGetDC record specifies that subsequent EMF records encountered in the metafile
     * SHOULD be processed.
     */
    public static class EmfPlusGetDC extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusSetTextRenderingHint record specifies the quality of text rendering, including the type
     * of anti-aliasing.
     */
    public static class EmfPlusSetTextRenderingHint extends EmfPlusFlagOnly {
    }

    /**
     * The EmfPlusResetWorldTransform record resets the current world space transform to the identify matrix.
     */
    public static class EmfPlusResetWorldTransform extends EmfPlusFlagOnly {
    }


    /**
     * The EmfPlusSetWorldTransform record sets the world transform according to the values in a
     * specified transform matrix.
     */
    public static class EmfPlusSetWorldTransform implements HemfPlusRecord {
        private int flags;
        private final AffineTransform matrixData = new AffineTransform();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.setWorldTransform;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            return HemfFill.readXForm(leis, matrixData);
        }
    }

    /**
     * The EmfPlusMultiplyWorldTransform record multiplies the current world space transform by a
     * specified transform matrix.
     */
    public static class EmfPlusMultiplyWorldTransform extends EmfPlusSetWorldTransform {
        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.multiplyWorldTransform;
        }
    }

    /**
     * The EmfPlusSetPageTransform record specifies scaling factors and units for converting page space
     * coordinates to device space coordinates.
     */
    public static class EmfPlusSetPageTransform implements HemfPlusRecord {
        private int flags;
        private double pageScale;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.setPageTransform;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;
            pageScale = leis.readFloat();
            return LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EmfPlusSetClipRegion record combines the current clipping region with another graphics region.
     */
    public static class EmfPlusSetClipRegion extends EmfPlusSetClipPath {

    }


    /**
     * The EmfPlusSetClipPath record combines the current clipping region with a graphics path.
     */
    public static class EmfPlusSetClipPath extends EmfPlusFlagOnly implements EmfPlusObjectId {
        private static final BitField COMBINE_MODE = BitFieldFactory.getInstance(0x0F00);

        public CombineMode getCombineMode() {
            return CombineMode.valueOf(COMBINE_MODE.getValue(getFlags()));
        }
    }

    /** The EmfPlusSetClipRect record combines the current clipping region with a rectangle. */
    public static class EmfPlusSetClipRect implements HemfPlusRecord {
        private static final BitField COMBINE_MODE = BitFieldFactory.getInstance(0x0F00);

        private int flags;
        private final Rectangle2D clipRect = new Rectangle2D.Double();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.setClipRect;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        public CombineMode getCombineMode() {
            return CombineMode.valueOf(COMBINE_MODE.getValue(getFlags()));
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // An EmfPlusRectF object that defines the rectangle to use in the CombineMode operation.
            return readRectF(leis, clipRect);
        }
    }


    /** The EmfPlusResetClip record resets the current clipping region for the world space to infinity. */
    public static class EmfPlusResetClip extends EmfPlusFlagOnly {

    }

    /**
     * The EmfPlusSave record saves the graphics state, identified by a specified index, on a stack of saved
     * graphics states.
     */
    public static class EmfPlusSave implements HemfPlusRecord {
        private int flags;
        private int stackIndex;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.save;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that specifies a level to associate with the graphics state.
            // The level value can be used by a subsequent EmfPlusRestore record operation to retrieve the graphics state.
            stackIndex = leis.readInt();

            return LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EmfPlusRestore record restores the graphics state, identified by a specified index, from a stack
     * of saved graphics states.
     */
    public static class EmfPlusRestore extends EmfPlusSave {
        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.restore;
        }
    }

    /** The EmfPlusSetRenderingOrigin record specifies the rendering origin for graphics output. */
    public static class EmfPlusSetRenderingOrigin implements HemfPlusRecord {
        int flags;
        Point2D origin = new Point2D.Double();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.setRenderingOrigin;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        public Point2D getOrigin() {
            return origin;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that defines the horizontal coordinate value of the rendering origin.
            double x = leis.readUInt();
            // A 32-bit unsigned integer that defines the vertical coordinate value of the rendering origin.
            double y = leis.readUInt();

            origin.setLocation(x,y);

            return LittleEndianConsts.INT_SIZE*2;
        }
    }

}
