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

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.hemf.record.emf.HemfFill;
import org.apache.poi.hemf.record.emfplus.HemfPlusMisc.EmfPlusObjectId;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.StringUtil;

public class HemfPlusDraw {
    private static final int MAX_OBJECT_SIZE = 1_000_000;

    public enum EmfPlusUnitType {
        /** Specifies a unit of logical distance within the world space. */
        World(0x00),
        /** Specifies a unit of distance based on the characteristics of the physical display. */
        Display(0x01),
        /** Specifies a unit of 1 pixel. */
        Pixel(0x02),
        /** Specifies a unit of 1 printer's point, or 1/72 inch. */
        Point(0x03),
        /** Specifies a unit of 1 inch. */
        Inch(0x04),
        /** Specifies a unit of 1/300 inch. */
        Document(0x05),
        /** Specifies a unit of 1 millimeter. */
        Millimeter(0x06)
        ;

        public final int id;

        EmfPlusUnitType(int id) {
            this.id = id;
        }

        public static EmfPlusUnitType valueOf(int id) {
            for (EmfPlusUnitType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }

    }

    public interface EmfPlusCompressed {
        /**
         * This bit indicates whether the data in the RectData field is compressed.
         * If set, RectData contains an EmfPlusRect object.
         * If clear, RectData contains an EmfPlusRectF object object.
         */
        BitField COMPRESSED = BitFieldFactory.getInstance(0x4000);

        int getFlags();

        /**
         * The index in the EMF+ Object Table to associate with the object
         * created by this record. The value MUST be zero to 63, inclusive.
         */
        default boolean isCompressed() {
            return COMPRESSED.isSet(getFlags());
        }

        default BiFunction<LittleEndianInputStream, Rectangle2D, Integer> getReadRect() {
            return isCompressed() ? HemfPlusDraw::readRectS : HemfPlusDraw::readRectF;
        }
    }

    public interface EmfPlusRelativePosition {
        /**
         * This bit indicates whether the PointData field specifies relative or absolute locations.
         * If set, each element in PointData specifies a location in the coordinate space that is relative to the
         * location specified by the previous element in the array. In the case of the first element in PointData,
         * a previous location at coordinates (0,0) is assumed.
         * If clear, PointData specifies absolute locations according to the {@link #isCompressed()} flag.
         *
         * Note If this flag is set, the {@link #isCompressed()} flag (above) is undefined and MUST be ignored.
         */
        BitField POSITION = BitFieldFactory.getInstance(0x0800);

        int getFlags();

        default boolean isRelativePosition() {
            return POSITION.isSet(getFlags());
        }
    }



    /**
     * The EmfPlusDrawPath record specifies drawing a graphics path
     */
    public static class EmfPlusDrawPath implements HemfPlusRecord {
        private int flags;
        private int penId;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.drawPath;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that specifies an index in the EMF+ Object Table for an EmfPlusPen object
            // to use for drawing the EmfPlusPath. The value MUST be zero to 63, inclusive.
            penId = leis.readInt();
            assert (0 <= penId && penId <= 63);

            assert (dataSize == LittleEndianConsts.INT_SIZE);

            return LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EmfPlusFillRects record specifies filling the interiors of a series of rectangles.
     */
    public static class EmfPlusFillRects implements HemfPlusRecord, EmfPlusCompressed {
        /**
         * If set, brushId specifies a color as an EmfPlusARGB object.
         * If clear, brushId contains the index of an EmfPlusBrush object in the EMF+ Object Table.
         */
        private static final BitField SOLID_COLOR = BitFieldFactory.getInstance(0x8000);

        private int flags;
        private int brushId;
        private final ArrayList<Rectangle2D> rectData = new ArrayList<>();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.fillRects;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that defines the brush, the content of which is
            // determined by the S bit in the Flags field.
            brushId = leis.readInt();

            // A 32-bit unsigned integer that specifies the number of rectangles in the RectData field.
            int count = leis.readInt();

            BiFunction<LittleEndianInputStream, Rectangle2D, Integer> readRect = getReadRect();

            rectData.ensureCapacity(count);

            int size = 2 * LittleEndianConsts.INT_SIZE;
            for (int i = 0; i<count; i++) {
                Rectangle2D rect = new Rectangle2D.Double();
                size += readRect.apply(leis, rect);
                rectData.add(rect);
            }

            return size;
        }
    }

    public static class EmfPlusDrawImagePoints implements HemfPlusRecord, EmfPlusObjectId, EmfPlusCompressed, EmfPlusRelativePosition {
        /**
         * This bit indicates that the rendering of the image includes applying an effect.
         * If set, an object of the Effect class MUST have been specified in an earlier EmfPlusSerializableObject record.
         */
        private static final BitField EFFECT = BitFieldFactory.getInstance(0x2000);

        private int flags;
        private int imageAttributesID;
        private EmfPlusUnitType srcUnit;
        private final Rectangle2D srcRect = new Rectangle2D.Double();
        private final Point2D upperLeft = new Point2D.Double();
        private final Point2D lowerRight = new Point2D.Double();
        private final Point2D lowerLeft = new Point2D.Double();
        private final AffineTransform trans = new AffineTransform();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.drawImagePoints;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that contains the index of the
            // optional EmfPlusImageAttributes object in the EMF+ Object Table.
            imageAttributesID = leis.readInt();

            // A 32-bit signed integer that defines the units of the SrcRect field.
            // It MUST be the UnitPixel value of the UnitType enumeration
            srcUnit = EmfPlusUnitType.valueOf(leis.readInt());
            assert(srcUnit == EmfPlusUnitType.Pixel);

            int size = 2 * LittleEndianConsts.INT_SIZE;

            // An EmfPlusRectF object that defines a portion of the image to be rendered.
            size += readRectF(leis, srcRect);

            // A 32-bit unsigned integer that specifies the number of points in the PointData array.
            // Exactly 3 points MUST be specified.
            int count = leis.readInt();
            assert(count == 3);
            size += LittleEndianConsts.INT_SIZE;

            BiFunction<LittleEndianInputStream, Point2D, Integer> readPoint;

            if (isRelativePosition()) {
                // If the POSITION flag is set in the Flags, the points specify relative locations.
                readPoint = HemfPlusDraw::readPointR;
            } else if (isCompressed()) {
                // If the POSITION bit is clear and the COMPRESSED bit is set in the Flags field, the points
                // specify absolute locations with integer values.
                readPoint = HemfPlusDraw::readPointS;
            } else {
                // If the POSITION bit is clear and the COMPRESSED bit is clear in the Flags field, the points
                // specify absolute locations with floating-point values.
                readPoint = HemfPlusDraw::readPointF;
            }

            // TODO: handle relative coordinates

            // An array of Count points that specify three points of a parallelogram.
            // The three points represent the upper-left, upper-right, and lower-left corners of the parallelogram.
            // The fourth point of the parallelogram is extrapolated from the first three.
            // The portion of the image specified by the SrcRect field SHOULD have scaling and shearing transforms
            // applied if necessary to fit inside the parallelogram.


            // size += readPoint.apply(leis, upperLeft);
            // size += readPoint.apply(leis, upperRight);
            // size += readPoint.apply(leis, lowerLeft);

            size += readPoint.apply(leis, lowerLeft);
            size += readPoint.apply(leis, lowerRight);
            size += readPoint.apply(leis, upperLeft);

            // https://math.stackexchange.com/questions/2772737/how-to-transform-arbitrary-rectangle-into-specific-parallelogram

            RealMatrix para2normal = MatrixUtils.createRealMatrix(new double[][] {
                { lowerLeft.getX(), lowerRight.getX(), upperLeft.getX() },
                { lowerLeft.getY(), lowerRight.getY(), upperLeft.getY() },
                { 1, 1, 1 }
            });

            RealMatrix rect2normal = MatrixUtils.createRealMatrix(new double[][]{
                { srcRect.getMinX(), srcRect.getMaxX(), srcRect.getMinX() },
                { srcRect.getMinY(), srcRect.getMinY(), srcRect.getMaxY() },
                { 1, 1, 1 }
            });

            RealMatrix normal2rect = new LUDecomposition(rect2normal).getSolver().getInverse();
            double[][] m = para2normal.multiply(normal2rect).getData();
            trans.setTransform(round10(m[0][0]), round10(m[1][0]), round10(m[0][1]), round10(m[1][1]), round10(m[0][2]), round10(m[1][2]));

            return size;
        }
    }

    /** The EmfPlusDrawImage record specifies drawing a scaled image. */
    public static class EmfPlusDrawImage implements HemfPlusRecord, EmfPlusObjectId, EmfPlusCompressed {
        private int flags;
        private int imageAttributesID;
        private EmfPlusUnitType srcUnit;
        private final Rectangle2D srcRect = new Rectangle2D.Double();
        private final Rectangle2D rectData = new Rectangle2D.Double();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.drawImage;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that contains the index of the
            // optional EmfPlusImageAttributes object in the EMF+ Object Table.
            imageAttributesID = leis.readInt();

            // A 32-bit signed integer that defines the units of the SrcRect field.
            // It MUST be the UnitPixel value of the UnitType enumeration
            srcUnit = EmfPlusUnitType.valueOf(leis.readInt());
            assert(srcUnit == EmfPlusUnitType.Pixel);

            int size = 2 * LittleEndianConsts.INT_SIZE;

            // An EmfPlusRectF object that specifies a portion of the image to be rendered. The portion of the image
            // specified by this rectangle is scaled to fit the destination rectangle specified by the RectData field.
            size += readRectF(leis, srcRect);

            // Either an EmfPlusRect or EmfPlusRectF object that defines the bounding box of the image. The portion of
            // the image specified by the SrcRect field is scaled to fit this rectangle.
            size += getReadRect().apply(leis, rectData);

            return size;
        }
    }

    /** The EmfPlusFillRegion record specifies filling the interior of a graphics region. */
    public static class EmfPlusFillRegion implements HemfPlusRecord {
        private static final BitField SOLID_COLOR = BitFieldFactory.getInstance(0x8000);

        private int flags;
        private int brushId;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.fillRegion;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        public boolean isSolidColor() {
            return SOLID_COLOR.isSet(getFlags());
        }

        public int getBrushId() {
            return (isSolidColor()) ? -1 : brushId;
        }

        public Color getSolidColor() {
            return (isSolidColor()) ? readARGB(brushId) : null;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that defines the brush, the content of which is determined by
            // the SOLID_COLOR bit in the Flags field.
            // If SOLID_COLOR is set, BrushId specifies a color as an EmfPlusARGB object.
            // If clear, BrushId contains the index of an EmfPlusBrush object in the EMF+ Object Table.
            brushId = leis.readInt();

            return LittleEndianConsts.INT_SIZE;
        }
    }

    /** The EmfPlusFillPath record specifies filling the interior of a graphics path. */
    public static class EmfPlusFillPath extends EmfPlusFillRegion {

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.fillPath;
        }

    }

    /** The EmfPlusDrawDriverString record specifies text output with character positions. */
    public static class EmfPlusDrawDriverString implements HemfPlusRecord, EmfPlusObjectId {
        /**
         * If set, brushId specifies a color as an EmfPlusARGB object.
         * If clear, brushId contains the index of an EmfPlusBrush object in the EMF+ Object Table.
         */
        private static final BitField SOLID_COLOR = BitFieldFactory.getInstance(0x8000);

        /**
         * If set, the positions of character glyphs SHOULD be specified in a character map lookup table.
         * If clear, the glyph positions SHOULD be obtained from an array of coordinates.
         */
        private static final BitField CMAP_LOOKUP = BitFieldFactory.getInstance(0x0001);

        /**
         * If set, the string SHOULD be rendered vertically.
         * If clear, the string SHOULD be rendered horizontally.
         */
        private static final BitField VERTICAL = BitFieldFactory.getInstance(0x0002);

        /**
         * If set, character glyph positions SHOULD be calculated relative to the position of the first glyph.
         * If clear, the glyph positions SHOULD be obtained from an array of coordinates.
         */
        private static final BitField REALIZED_ADVANCE = BitFieldFactory.getInstance(0x0004);

        /**
         * If set, less memory SHOULD be used to cache anti-aliased glyphs, which produces lower quality text rendering.
         * If clear, more memory SHOULD be used, which produces higher quality text rendering.
         */
        private static final BitField LIMIT_SUBPIXEL = BitFieldFactory.getInstance(0x0008);



        private int flags;
        private int brushId;
        private int optionsFlags;
        private String glyphs;
        private final List<Point2D> glpyhPos = new ArrayList<>();
        private final AffineTransform transformMatrix = new AffineTransform();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.drawDriverstring;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that specifies either the foreground color of the text or a graphics brush,
            // depending on the value of the SOLID_COLOR flag in the Flags.
            brushId = leis.readInt();

            // A 32-bit unsigned integer that specifies the spacing, orientation, and quality of rendering for the
            // string. This value MUST be composed of DriverStringOptions flags
            optionsFlags = leis.readInt();

            // A 32-bit unsigned integer that specifies whether a transform matrix is present in the
            // TransformMatrix field.
            boolean hasMatrix = leis.readInt() == 1;

            // A 32-bit unsigned integer that specifies number of glyphs in the string.
            int glyphCount = leis.readInt();

            int size = 4 * LittleEndianConsts.INT_SIZE;

            // TOOD: implement Non-Cmap-Lookup correctly

            // If the CMAP_LOOKUP flag in the optionsFlags field is set, each value in this array specifies a
            // Unicode character. Otherwise, each value specifies an index to a character glyph in the EmfPlusFont
            // object specified by the ObjectId value in Flags field.
            byte[] glyphBuf = IOUtils.toByteArray(leis, glyphCount*2, MAX_OBJECT_SIZE);
            glyphs = StringUtil.getFromUnicodeLE(glyphBuf);

            size += glyphBuf.length;

            // An array of EmfPlusPointF objects that specify the output position of each character glyph.
            // There MUST be GlyphCount elements, which have a one-to-one correspondence with the elements
            // in the Glyphs array.
            //
            // Glyph positions are calculated from the position of the first glyph if the REALIZED_ADVANCE flag in
            // Options flags is set. In this case, GlyphPos specifies the position of the first glyph only.
            int glyphPosCnt = REALIZED_ADVANCE.isSet(optionsFlags) ? 1 : glyphCount;
            for (int i=0; i<glyphCount; i++) {
                Point2D p = new Point2D.Double();
                size += readPointF(leis, p);
                glpyhPos.add(p);
            }

            if (hasMatrix) {
                size += HemfFill.readXForm(leis, transformMatrix);
            }


            return size;
        }
    }

    /** The EmfPlusDrawRects record specifies drawing a series of rectangles. */
    public static class EmfPlusDrawRects implements HemfPlusRecord, EmfPlusObjectId, EmfPlusCompressed {
        private int flags;
        private final List<Rectangle2D> rectData = new ArrayList<>();

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.drawRects;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            // A 32-bit unsigned integer that specifies the number of rectangles in the RectData member.
            int count = leis.readInt();
            int size = LittleEndianConsts.INT_SIZE;

            BiFunction<LittleEndianInputStream, Rectangle2D, Integer> readRect = getReadRect();

            for (int i=0; i<count; i++) {
                Rectangle2D rect = new Rectangle2D.Double();
                size += readRect.apply(leis, rect);
            }

            return size;
        }
    }


    static double round10(double d) {
        return new BigDecimal(d).setScale(10, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    static int readRectS(LittleEndianInputStream leis, Rectangle2D bounds) {
        // A 16-bit signed integer that defines the ... coordinate
        final int x = leis.readShort();
        final int y = leis.readShort();
        final int width = leis.readShort();
        final int height = leis.readShort();
        bounds.setRect(x, y, width, height);

        return 4 * LittleEndianConsts.SHORT_SIZE;
    }

    static int readRectF(LittleEndianInputStream leis, Rectangle2D bounds) {
        // A 32-bit floating-point that defines the ... coordinate
        final double x = leis.readFloat();
        final double y = leis.readFloat();
        final double width = leis.readFloat();
        final double height = leis.readFloat();
        bounds.setRect(x, y, width, height);

        return 4 * LittleEndianConsts.INT_SIZE;
    }

    /**
     * The EmfPlusPoint object specifies an ordered pair of integer (X,Y) values that define an absolute
     * location in a coordinate space.
     */
    static int readPointS(LittleEndianInputStream leis, Point2D point) {
        double x = leis.readShort();
        double y = leis.readShort();
        point.setLocation(x,y);
        return 2*LittleEndianConsts.SHORT_SIZE;
    }

    /**
     * The EmfPlusPointF object specifies an ordered pair of floating-point (X,Y) values that define an
     * absolute location in a coordinate space.
     */
    static int readPointF(LittleEndianInputStream leis, Point2D point) {
        double x = leis.readFloat();
        double y = leis.readFloat();
        point.setLocation(x,y);
        return 2*LittleEndianConsts.INT_SIZE;
    }

    /**
     * The EmfPlusPointR object specifies an ordered pair of integer (X,Y) values that define a relative
     * location in a coordinate space.
     */
    static int readPointR(LittleEndianInputStream leis, Point2D point) {
        int[] p = { 0 };
        int size = readEmfPlusInteger(leis, p);
        double x = p[0];
        size += readEmfPlusInteger(leis, p);
        double y = p[0];
        point.setLocation(x,y);
        return size;
    }

    private static int readEmfPlusInteger(LittleEndianInputStream leis, int[] value) {
        value[0] = leis.readByte();
        // check for EmfPlusInteger7 value
        if ((value[0] & 0x80) == 0) {
            return LittleEndianConsts.BYTE_SIZE;
        }
        // ok we've read a EmfPlusInteger15
        value[0] = ((value[0] << 8) | leis.readByte()) & 0x7FFF;
        return LittleEndianConsts.SHORT_SIZE;
    }

    static Color readARGB(int argb) {
        return new Color(  (argb >>> 8) & 0xFF, (argb >>> 16) & 0xFF, (argb >>> 24) & 0xFF, argb & 0xFF);
    }

}
