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

import static org.apache.poi.hwmf.record.HwmfBrushStyle.BS_NULL;
import static org.apache.poi.hwmf.record.HwmfBrushStyle.BS_SOLID;

import java.awt.Color;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.hemf.draw.HemfDrawProperties;
import org.apache.poi.hemf.draw.HemfGraphics;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfColorRef;
import org.apache.poi.hwmf.record.HwmfDraw;
import org.apache.poi.hwmf.record.HwmfDraw.WmfSelectObject;
import org.apache.poi.hwmf.record.HwmfPenStyle;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfDraw {
    /**
     * The EMR_SELECTOBJECT record adds a graphics object to the current metafile playback device
     * context. The object is specified either by its index in the EMF Object Table or by its
     * value from the StockObject enumeration.
     */
    public static class EmfSelectObject extends WmfSelectObject implements HemfRecord {

        private static final HwmfColorRef WHITE = new HwmfColorRef(Color.WHITE);
        private static final HwmfColorRef LTGRAY = new HwmfColorRef(new Color(0x00C0C0C0));
        private static final HwmfColorRef GRAY = new HwmfColorRef(new Color(0x00808080));
        private static final HwmfColorRef DKGRAY = new HwmfColorRef(new Color(0x00404040));
        private static final HwmfColorRef BLACK = new HwmfColorRef(Color.BLACK);

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.selectObject;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies either the index of a graphics object in the
            // EMF Object Table or the index of a stock object from the StockObject enumeration.
            objectIndex = leis.readInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            if ((objectIndex & 0x80000000) != 0) {
                selectStockObject(ctx);
            } else {
                super.draw(ctx);
            }
        }

        private void selectStockObject(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            switch (objectIndex) {
                case 0x80000000:
                    // WHITE_BRUSH - A white, solid-color brush
                    // BrushStyle: BS_SOLID
                    // Color: 0x00FFFFFF
                    prop.setBrushColor(WHITE);
                    prop.setBrushStyle(BS_SOLID);
                    break;
                case 0x80000001:
                    // LTGRAY_BRUSH - A light gray, solid-color brush
                    // BrushStyle: BS_SOLID
                    // Color: 0x00C0C0C0
                    prop.setBrushColor(LTGRAY);
                    prop.setBrushStyle(BS_SOLID);
                    break;
                case 0x80000002:
                    // GRAY_BRUSH - A gray, solid-color brush
                    // BrushStyle: BS_SOLID
                    // Color: 0x00808080
                    prop.setBrushColor(GRAY);
                    prop.setBrushStyle(BS_SOLID);
                    break;
                case 0x80000003:
                    // DKGRAY_BRUSH - A dark gray, solid color brush
                    // BrushStyle: BS_SOLID
                    // Color: 0x00404040
                    prop.setBrushColor(DKGRAY);
                    prop.setBrushStyle(BS_SOLID);
                    break;
                case 0x80000004:
                    // BLACK_BRUSH - A black, solid color brush
                    // BrushStyle: BS_SOLID
                    // Color: 0x00000000
                    prop.setBrushColor(BLACK);
                    prop.setBrushStyle(BS_SOLID);
                    break;
                case 0x80000005:
                    // NULL_BRUSH - A null brush
                    // BrushStyle: BS_NULL
                    prop.setBrushStyle(BS_NULL);
                    break;
                case 0x80000006:
                    // WHITE_PEN - A white, solid-color pen
                    // PenStyle: PS_COSMETIC + PS_SOLID
                    // ColorRef: 0x00FFFFFF
                    prop.setPenStyle(HwmfPenStyle.valueOf(0));
                    prop.setPenWidth(1);
                    prop.setPenColor(WHITE);
                    break;
                case 0x80000007:
                    // BLACK_PEN - A black, solid-color pen
                    // PenStyle: PS_COSMETIC + PS_SOLID
                    // ColorRef: 0x00000000
                    prop.setPenStyle(HwmfPenStyle.valueOf(0));
                    prop.setPenWidth(1);
                    prop.setPenColor(BLACK);
                    break;
                case 0x80000008:
                    // NULL_PEN - A null pen
                    // PenStyle: PS_NULL
                    prop.setPenStyle(HwmfPenStyle.valueOf(HwmfPenStyle.HwmfLineDash.NULL.wmfFlag));
                    break;
                case 0x8000000A:
                    // OEM_FIXED_FONT - A fixed-width, OEM character set
                    // Charset: OEM_CHARSET
                    // PitchAndFamily: FF_DONTCARE + FIXED_PITCH
                    break;
                case 0x8000000B:
                    // ANSI_FIXED_FONT - A fixed-width font
                    // Charset: ANSI_CHARSET
                    // PitchAndFamily: FF_DONTCARE + FIXED_PITCH
                    break;
                case 0x8000000C:
                    // ANSI_VAR_FONT - A variable-width font
                    // Charset: ANSI_CHARSET
                    // PitchAndFamily: FF_DONTCARE + VARIABLE_PITCH
                    break;
                case 0x8000000D:
                    // SYSTEM_FONT - A font that is guaranteed to be available in the operating system
                    break;
                case 0x8000000E:
                    // DEVICE_DEFAULT_FONT
                    // The default font that is provided by the graphics device driver for the current output device
                    break;
                case 0x8000000F:
                    // DEFAULT_PALETTE
                    // The default palette that is defined for the current output device.
                    break;
                case 0x80000010:
                    // SYSTEM_FIXED_FONT
                    // A fixed-width font that is guaranteed to be available in the operating system.
                    break;
                case 0x80000011:
                    // DEFAULT_GUI_FONT
                    // The default font that is used for user interface objects such as menus and dialog boxes.
                    break;
                case 0x80000012:
                    // DC_BRUSH
                    // The solid-color brush that is currently selected in the playback device context.
                    break;
                case 0x80000013:
                    // DC_PEN
                    // The solid-color pen that is currently selected in the playback device context.
                    break;
            }
        }

    }


    /** The EMR_POLYBEZIER record specifies one or more Bezier curves. */
    public static class EmfPolyBezier extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezier;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            /* A 32-bit unsigned integer that specifies the number of points in the points
             * array. This value MUST be one more than three times the number of curves to
             * be drawn, because each Bezier curve requires two control points and an
             * endpoint, and the initial curve requires an additional starting point.
             *
             * Line width | Device supports wideline | Maximum points allowed
             *    1       |            n/a           |          16K
             *   > 1      |            yes           |          16K
             *   > 1      |             no           |         1360
             *
             * Any extra points MUST be ignored.
             */
            final int count = (int)leis.readUInt();
            final int points = Math.min(count, 16384);
            size += LittleEndianConsts.INT_SIZE;

            poly.reset();

            /* Cubic Bezier curves are defined using the endpoints and control points
             * specified by the points field. The first curve is drawn from the first
             * point to the fourth point, using the second and third points as control
             * points. Each subsequent curve in the sequence needs exactly three more points:
             * the ending point of the previous curve is used as the starting point,
             * the next two points in the sequence are control points,
             * and the third is the ending point.
             * The cubic Bezier curves SHOULD be drawn using the current pen.
             */

            Point2D pnt[] = { new Point2D.Double(), new Point2D.Double(), new Point2D.Double() };

            // points-1 because of the first point
            final int pointCnt = hasStartPoint() ? points-1 : points;
            for (int i=0; i+3<pointCnt; i+=3) {
                // x (4 bytes): A 32-bit signed integer that defines the horizontal (x) coordinate of the point.
                // y (4 bytes): A 32-bit signed integer that defines the vertical (y) coordinate of the point.
                if (i==0 && hasStartPoint()) {
                    size += readPoint(leis, pnt[0]);
                    poly.moveTo(pnt[0].getX(),pnt[0].getY());
                }

                size += readPoint(leis, pnt[0]);
                size += readPoint(leis, pnt[1]);
                size += readPoint(leis, pnt[2]);

                poly.curveTo(
                    pnt[0].getX(),pnt[0].getY(),
                    pnt[1].getX(),pnt[1].getY(),
                    pnt[2].getX(),pnt[2].getY()
                );
            }

            return size;
        }

        /**
         * @return true, if start point is in the list of points. false, if start point is taken from the context
         */
        protected boolean hasStartPoint() {
            return true;
        }
    }

    /**
     * The EMR_POLYBEZIER16 record specifies one or more Bezier curves.
     * The curves are drawn using the current pen.
     */
    public static class EmfPolyBezier16 extends EmfPolyBezier {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezier16;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }


    /**
     * The EMR_POLYGON record specifies a polygon consisting of two or more vertexes connected by
     * straight lines.
     */
    public static class EmfPolygon extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polygon;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // see PolyBezier about limits
            final int count = (int)leis.readUInt();
            final int points = Math.min(count, 16384);
            size += LittleEndianConsts.INT_SIZE;

            Point2D pnt = new Point2D.Double();
            for (int i=0; i<points; i++) {
                size += readPoint(leis, pnt);
                if (i==0) {
                    poly.moveTo(pnt.getX(), pnt.getY());

                    if (hasStartPoint()) {
                        continue;
                    }

                    // if this path is connected to the current position (= has no start point)
                    // the first entry is a dummy entry and will be skipped later
                }
                poly.lineTo(pnt.getX(), pnt.getY());
            }

            return size;
        }

        /**
         * @return true, if start point is in the list of points. false, if start point is taken from the context
         */
        protected boolean hasStartPoint() {
            return true;
        }
    }

    /**
     * The EMR_POLYGON16 record specifies a polygon consisting of two or more vertexes connected by straight lines.
     * The polygon is outlined by using the current pen and filled by using the current brush and polygon fill mode.
     * The polygon is closed automatically by drawing a line from the last vertex to the first
     */
    public static class EmfPolygon16 extends EmfPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polygon16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYLINE record specifies a series of line segments by connecting the points in the
     * specified array.
     */
    public static class EmfPolyline extends EmfPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyline;
        }

        @Override
        protected boolean isFill() {
            return false;
        }
    }

    /**
     * The EMR_POLYLINE16 record specifies a series of line segments by connecting the points in the
     * specified array.
     */
    public static class EmfPolyline16 extends EmfPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyline16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYBEZIERTO record specifies one or more Bezier curves based upon the current
     * position.
     */
    public static class EmfPolyBezierTo extends EmfPolyBezier {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezierTo;
        }

        @Override
        protected boolean hasStartPoint() {
            return false;
        }

        @Override
        protected Path2D getShape(HwmfGraphics ctx) {
            return polyTo(ctx, poly);
        }
    }

    /**
     * The EMR_POLYBEZIERTO16 record specifies one or more Bezier curves based on the current
     * position.
     */
    public static class EmfPolyBezierTo16 extends EmfPolyBezierTo {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezierTo16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /** The EMR_POLYLINETO record specifies one or more straight lines based upon the current position. */
    public static class EmfPolylineTo extends EmfPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polylineTo;
        }

        @Override
        protected boolean hasStartPoint() {
            return false;
        }

        @Override
        protected Path2D getShape(HwmfGraphics ctx) {
            return polyTo(ctx, poly);
        }
    }

    /**
     * The EMR_POLYLINETO16 record specifies one or more straight lines based upon the current position.
     * A line is drawn from the current position to the first point specified by the points field by using the
     * current pen. For each additional line, drawing is performed from the ending point of the previous
     * line to the next point specified by points.
     */
    public static class EmfPolylineTo16 extends EmfPolylineTo {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polylineTo16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYPOLYGON record specifies a series of closed polygons.
     */
    public static class EmfPolyPolygon extends HwmfDraw.WmfPolyPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolygon;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // A 32-bit unsigned integer that specifies the number of polygons.
            long numberOfPolygons = leis.readUInt();
            // A 32-bit unsigned integer that specifies the total number of points in all polygons.
            long count = Math.min(16384, leis.readUInt());

            size += 2 * LittleEndianConsts.INT_SIZE;

            // An array of 32-bit unsigned integers that specifies the point count for each polygon.
            long[] polygonPointCount = new long[(int)numberOfPolygons];

            size += numberOfPolygons * LittleEndianConsts.INT_SIZE;

            for (int i=0; i<numberOfPolygons; i++) {
                polygonPointCount[i] = leis.readUInt();
            }

            Point2D pnt = new Point2D.Double();
            for (long nPoints : polygonPointCount) {
                /**
                 * An array of WMF PointL objects that specifies the points for all polygons in logical units.
                 * The number of points is specified by the Count field value.
                 */
                Path2D poly = new Path2D.Double();
                for (int i=0; i<nPoints; i++) {
                    size += readPoint(leis, pnt);
                    if (i == 0) {
                        poly.moveTo(pnt.getX(), pnt.getY());
                    } else {
                        poly.lineTo(pnt.getX(), pnt.getY());
                    }
                }
                if (isClosed()) {
                    poly.closePath();
                }
                polyList.add(poly);
            }
            return size;
        }

        /**
         * @return true, if a polyline should be closed, i.e. is a polygon
         */
        protected boolean isClosed() {
            return true;
        }
    }

    /**
     * The EMR_POLYPOLYGON16 record specifies a series of closed polygons. Each polygon is outlined
     * using the current pen, and filled using the current brush and polygon fill mode.
     * The polygons drawn by this record can overlap.
     */
    public static class EmfPolyPolygon16 extends EmfPolyPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolygon16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYPOLYLINE record specifies multiple series of connected line segments.
     */
    public static class EmfPolyPolyline extends EmfPolyPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolyline;
        }

        @Override
        protected boolean isClosed() {
            return false;
        }

        @Override
        protected boolean isFill() {
            return false;
        }
    }

    /** The EMR_POLYPOLYLINE16 record specifies multiple series of connected line segments. */
    public static class EmfPolyPolyline16 extends EmfPolyPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolyline16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_SETPIXELV record defines the color of the pixel at the specified logical coordinates.
     */
    public static class EmfSetPixelV extends HwmfDraw.WmfSetPixel implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setPixelV;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readPointL(leis, point);
            size += colorRef.init(leis);
            return size;
        }
    }

    /**
     * The EMR_MOVETOEX record specifies coordinates of the new current position, in logical units.
     */
    public static class EmfSetMoveToEx extends HwmfDraw.WmfMoveTo implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setMoveToEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, point);
        }
    }

    /**
     * The EMR_ARCTO record specifies an elliptical arc.
     * It resets the current position to the end point of the arc.
     */
    public static class EmfArc extends HwmfDraw.WmfArc implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.arc;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            super.draw(ctx);
            ctx.getProperties().setLocation(endPoint);
        }
    }

    /**
     * The EMR_CHORD record specifies a chord, which is a region bounded by the intersection of an
     * ellipse and a line segment, called a secant. The chord is outlined by using the current pen
     * and filled by using the current brush.
     */
    public static class EmfChord extends HwmfDraw.WmfChord implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.chord;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }
    }

    /**
     * The EMR_PIE record specifies a pie-shaped wedge bounded by the intersection of an ellipse and two
     * radials. The pie is outlined by using the current pen and filled by using the current brush.
     */
    public static class EmfPie extends HwmfDraw.WmfPie implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.pie;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }
    }

    /**
     * The EMR_ELLIPSE record specifies an ellipse. The center of the ellipse is the center of the specified
     * bounding rectangle. The ellipse is outlined by using the current pen and is filled by using the current
     * brush.
     */
    public static class EmfEllipse extends HwmfDraw.WmfEllipse implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.ellipse;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readRectL(leis, bounds);
        }
    }

    /**
     * The EMR_RECTANGLE record draws a rectangle. The rectangle is outlined by using the current pen
     * and filled by using the current brush.
     */
    public static class EmfRectangle extends HwmfDraw.WmfRectangle implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.rectangle;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readRectL(leis, bounds);
        }
    }

    /**
     * The EMR_ROUNDRECT record specifies a rectangle with rounded corners. The rectangle is outlined
     * by using the current pen and filled by using the current brush.
     */
    public static class EmfRoundRect extends HwmfDraw.WmfRoundRect implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.roundRect;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // A 32-bit unsigned integer that defines the x-coordinate of the point.
            width = (int)leis.readUInt();
            height = (int)leis.readUInt();

            return size + 2*LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EMR_LINETO record specifies a line from the current position up to, but not including, the
     * specified point. It resets the current position to the specified point.
     */
    public static class EmfLineTo extends HwmfDraw.WmfLineTo implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.lineTo;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, point);
        }
    }

    /**
     * The EMR_ARCTO record specifies an elliptical arc.
     * It resets the current position to the end point of the arc.
     */
    public static class EmfArcTo extends HwmfDraw.WmfArc implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.arcTo;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            super.draw(ctx);
            ctx.getProperties().setLocation(endPoint);
        }
    }

    /** The EMR_POLYDRAW record specifies a set of line segments and Bezier curves. */
    public static class EmfPolyDraw extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyDraw;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            int count = (int)leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;
            Point2D points[] = new Point2D[count];
            for (int i=0; i<count; i++) {
                 size += readPoint(leis, points[i]);
            }

            poly.reset();

            for (int i=0; i<count; i++) {
                int mode = leis.readUByte();
                switch (mode & 0x06) {
                    // PT_LINETO
                    // Specifies that a line is to be drawn from the current position to this point, which
                    // then becomes the new current position.
                    case 0x02:
                        poly.lineTo(points[i].getX(), points[i].getY());
                        break;
                    // PT_BEZIERTO
                    // Specifies that this point is a control point or ending point for a Bezier curve.
                    // PT_BEZIERTO types always occur in sets of three.
                    // The current position defines the starting point for the Bezier curve.
                    // The first two PT_BEZIERTO points are the control points,
                    // and the third PT_BEZIERTO point is the ending point.
                    // The ending point becomes the new current position.
                    // If there are not three consecutive PT_BEZIERTO points, an error results.
                    case 0x04:
                        int mode2 = leis.readUByte();
                        int mode3 = leis.readUByte();
                        assert(mode2 == 0x04 && mode3 == 0x04);
                        poly.curveTo(
                            points[i].getX(), points[i].getY(),
                            points[i+1].getX(), points[i+1].getY(),
                            points[i+2].getX(), points[i+2].getY()
                        );
                        i+=2;
                        break;
                    // PT_MOVETO
                    // Specifies that this point starts a disjoint figure. This point becomes the new current position.
                    case 0x06:
                        poly.moveTo(points[i].getX(), points[i].getY());
                        break;
                    default:
                        // TODO: log error
                        break;
                }

                // PT_CLOSEFIGURE
                // A PT_LINETO or PT_BEZIERTO type can be combined with this value by using the bitwise operator OR
                // to indicate that the corresponding point is the last point in a figure and the figure is closed.
                // The current position is set to the ending point of the closing line.
                if ((mode & 0x01) == 0x01) {
                    this.poly.closePath();
                }
            }
            size += count;
            return size;
        }
    }

    public static class EmfPolyDraw16 extends EmfPolyDraw {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyDraw16;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    public static class EmfBeginPath implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.beginPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    public static class EmfEndPath implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.endPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    public static class EmfAbortPath implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.abortPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    public static class EmfCloseFigure implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.closeFigure;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    public static class EmfFlattenPath implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.flattenPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    public static class EmfWidenPath implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.widenPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    /**
     * The EMR_STROKEPATH record renders the specified path by using the current pen.
     */
    public static class EmfStrokePath implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.strokePath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 128-bit WMF RectL object, which specifies bounding rectangle, in device units
            return readRectL(leis, bounds);
        }
    }

    static long readRectL(LittleEndianInputStream leis, Rectangle2D bounds) {
        /* A 32-bit signed integer that defines the x coordinate, in logical coordinates,
         * of the ... corner of the rectangle.
         */
        final int left = leis.readInt();
        final int top = leis.readInt();
        final int right = leis.readInt();
        final int bottom = leis.readInt();
        bounds.setRect(left, top, right-left, bottom-top);

        return 4 * LittleEndianConsts.INT_SIZE;
    }

    static long readPointS(LittleEndianInputStream leis, Point2D point) {
        // x (2 bytes): A 16-bit signed integer that defines the horizontal (x) coordinate of the point.
        final int x = leis.readShort();
        // y (2 bytes): A 16-bit signed integer that defines the vertical (y) coordinate of the point.
        final int y = leis.readShort();
        point.setLocation(x, y);

        return 2*LittleEndianConsts.SHORT_SIZE;

    }
    static long readPointL(LittleEndianInputStream leis, Point2D point) {
        // x (4 bytes): A 32-bit signed integer that defines the horizontal (x) coordinate of the point.
        final int x = leis.readInt();
        // y (4 bytes): A 32-bit signed integer that defines the vertical (y) coordinate of the point.
        final int y = leis.readInt();
        point.setLocation(x, y);

        return 2*LittleEndianConsts.INT_SIZE;

    }

    static long readDimensionFloat(LittleEndianInputStream leis, Dimension2D dimension) {
        final double width = leis.readFloat();
        final double height = leis.readFloat();
        dimension.setSize(width, height);
        return 2*LittleEndianConsts.INT_SIZE;
    }

    static long readDimensionInt(LittleEndianInputStream leis, Dimension2D dimension) {
        final double width = leis.readUInt();
        final double height = leis.readUInt();
        dimension.setSize(width, height);
        return 2*LittleEndianConsts.INT_SIZE;
    }

    private static Path2D polyTo(HwmfGraphics ctx, Path2D poly) {
        Path2D polyCopy = new Path2D.Double();
        Point2D start = ctx.getProperties().getLocation();
        polyCopy.moveTo(start.getX(), start.getY());

        PathIterator iter = poly.getPathIterator(null);
        iter.next();

        polyCopy.append(iter, true);
        return polyCopy;
    }


}
