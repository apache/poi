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
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfDraw {
    /**
     * The META_MOVETO record sets the output position in the playback device context to a specified
     * point.
     */
    public static class WmfMoveTo implements HwmfRecord {

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
            return HwmfRecordType.moveTo;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setLocation(x, y);
        }
    }

    /**
     * The META_LINETO record draws a line from the drawing position that is defined in the playback
     * device context up to, but not including, the specified point.
     */
    public static class WmfLineTo implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the vertical component of the drawing
         * destination position, in logical units.
         */
        private int y;

        /**
         * A 16-bit signed integer that defines the horizontal component of the drawing
         * destination position, in logical units.
         */
        private int x;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.lineTo;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Point2D start = ctx.getProperties().getLocation();
            Line2D line = new Line2D.Double(start.getX(), start.getY(), x, y);
            ctx.draw(line);
            ctx.getProperties().setLocation(x, y);
        }
    }

    /**
     * The META_POLYGON record paints a polygon consisting of two or more vertices connected by
     * straight lines. The polygon is outlined by using the pen and filled by using the brush and polygon fill
     * mode that are defined in the playback device context.
     */
    public static class WmfPolygon implements HwmfRecord {

        private Path2D poly = new Path2D.Double();
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.polygon;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            /**
             * A 16-bit signed integer that defines the number of points in the array.
             */
            int numberofPoints = leis.readShort();

            for (int i=0; i<numberofPoints; i++) {
                // A 16-bit signed integer that defines the horizontal (x) coordinate of the point.
                int x = leis.readShort();
                // A 16-bit signed integer that defines the vertical (y) coordinate of the point.
                int y = leis.readShort();
                if (i==0) {
                    poly.moveTo(x, y);
                } else {
                    poly.lineTo(x, y);
                }
            }

            return LittleEndianConsts.SHORT_SIZE+numberofPoints*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Path2D shape = getShape();
//            shape.closePath();
            Path2D p = (Path2D)shape.clone();
            p.setWindingRule(getWindingRule(ctx));
            ctx.fill(p);
        }

        protected Path2D getShape() {
            return (Path2D)poly.clone();
        }
    }

    /**
     * The META_POLYLINE record draws a series of line segments by connecting the points in the
     * specified array.
     */
    public static class WmfPolyline extends WmfPolygon {

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.polyline;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Path2D shape = getShape();
            Path2D p = (Path2D)shape.clone();
            p.setWindingRule(getWindingRule(ctx));
            ctx.draw(p);
        }
    }

    /**
     * The META_ELLIPSE record draws an ellipse. The center of the ellipse is the center of the specified
     * bounding rectangle. The ellipse is outlined by using the pen and is filled by using the brush; these
     * are defined in the playback device context.
     */
    public static class WmfEllipse implements HwmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        private int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        private int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the bounding rectangle.
         */
        private int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the bounding rectangle.
         */
        private int leftRect;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.ellipse;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            int x = Math.min(leftRect, rightRect);
            int y = Math.min(topRect, bottomRect);
            int w = Math.abs(leftRect - rightRect - 1);
            int h = Math.abs(topRect - bottomRect - 1);
            Shape s = new Ellipse2D.Double(x, y, w, h);
            ctx.fill(s);
        }
    }


    /**
     * The META_FRAMEREGION record draws a border around a specified region using a specified brush.
     */
    public static class WmfFrameRegion implements HwmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be framed.
         */
        private int regionIndex;
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get the
         * Brush to use for filling the region.
         */
        private int brushIndex;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the
         * region frame.
         */
        private int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the
         * region frame.
         */
        private int width;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.frameRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            brushIndex = leis.readUShort();
            height = leis.readShort();
            width = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(brushIndex);
            ctx.applyObjectTableEntry(regionIndex);
            Rectangle2D inner = ctx.getProperties().getRegion().getBounds();
            double x = inner.getX()-width;
            double y = inner.getY()-height;
            double w = inner.getWidth()+2*width;
            double h = inner.getHeight()+2*height;
            Rectangle2D outer = new Rectangle2D.Double(x,y,w,h);
            Area frame = new Area(outer);
            frame.subtract(new Area(inner));
            ctx.fill(frame);
        }
    }

    /**
     * The META_POLYPOLYGON record paints a series of closed polygons. Each polygon is outlined by
     * using the pen and filled by using the brush and polygon fill mode; these are defined in the playback
     * device context. The polygons drawn by this function can overlap.
     */
    public static class WmfPolyPolygon implements HwmfRecord {

        private List<Path2D> polyList = new ArrayList<>();
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.polyPolygon;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // see http://secunia.com/gfx/pdf/SA31675_BA.pdf ;)
            /**
             * A 16-bit unsigned integer that defines the number of polygons in the object.
             */
            int numberOfPolygons = leis.readUShort();
            /**
             * A NumberOfPolygons array of 16-bit unsigned integers that define the number of
             * points for each polygon in the object.
             */
            int[] pointsPerPolygon = new int[numberOfPolygons];

            int size = LittleEndianConsts.SHORT_SIZE;

            for (int i=0; i<numberOfPolygons; i++) {
                pointsPerPolygon[i] = leis.readUShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }

            for (int nPoints : pointsPerPolygon) {
                /**
                 * An array of 16-bit signed integers that define the coordinates of the polygons.
                 * (Note: MS-WMF wrongly says unsigned integers ...)
                 */
                Path2D poly = new Path2D.Double();
                for (int i=0; i<nPoints; i++) {
                    int x = leis.readShort();
                    int y = leis.readShort();
                    size += 2*LittleEndianConsts.SHORT_SIZE;
                    if (i == 0) {
                        poly.moveTo(x, y);
                    } else {
                        poly.lineTo(x, y);
                    }
                }
                poly.closePath();
                polyList.add(poly);
            }

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            if (polyList.isEmpty()) {
                return;
            }
            
            int windingRule = getWindingRule(ctx);
            Area area = null;
            for (Path2D poly : polyList) {
                Path2D p = (Path2D)poly.clone();
                p.setWindingRule(windingRule);
                Area newArea = new Area(p);
                if (area == null) {
                    area = newArea;
                } else {
                    area.exclusiveOr(newArea);
                }
            }
            ctx.fill(area);
        }
    }

    /**
     * The META_RECTANGLE record paints a rectangle. The rectangle is outlined by using the pen and
     * filled by using the brush that are defined in the playback device context.
     */
    public static class WmfRectangle implements HwmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        private int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        private int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the rectangle.
         */
        private int leftRect;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.frameRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            int x = Math.min(leftRect, rightRect);
            int y = Math.min(topRect, bottomRect);
            int w = Math.abs(leftRect - rightRect - 1);
            int h = Math.abs(topRect - bottomRect - 1);
            Shape s = new Rectangle2D.Double(x, y, w, h);
            ctx.fill(s);
        }
    }

    /**
     * The META_RECTANGLE record paints a rectangle. The rectangle is outlined by using the pen and
     * filled by using the brush that are defined in the playback device context.
     */
    public static class WmfSetPixel implements HwmfRecord {
        /**
         * A ColorRef Object that defines the color value.
         */
        HwmfColorRef colorRef;

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
            return HwmfRecordType.setPixel;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE+size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Shape s = new Rectangle2D.Double(x, y, 1, 1);
            ctx.fill(s);
        }
    }

    /**
     * The META_ROUNDRECT record paints a rectangle with rounded corners. The rectangle is outlined
     * using the pen and filled using the brush, as defined in the playback device context.
     */
    public static class WmfRoundRect implements HwmfRecord {
        /**
         * A 16-bit signed integer that defines the height, in logical coordinates, of the
         * ellipse used to draw the rounded corners.
         */
        private int height;

        /**
         * A 16-bit signed integer that defines the width, in logical coordinates, of the
         * ellipse used to draw the rounded corners.
         */
        private int width;

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        private int bottomRect;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        private int rightRect;

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        private int topRect;

        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the rectangle.
         */
        private int leftRect;


        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.roundRect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            height = leis.readShort();
            width = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 6*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            int x = Math.min(leftRect, rightRect);
            int y = Math.min(topRect, bottomRect);
            int w = Math.abs(leftRect - rightRect - 1);
            int h = Math.abs(topRect - bottomRect - 1);
            Shape s = new RoundRectangle2D.Double(x, y, w, h, width, height);
            ctx.fill(s);
        }
    }


    /**
     * The META_ARC record draws an elliptical arc.
     */
    public static class WmfArc implements HwmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the ending point of the radial line defining the ending point of the arc.
         */
        private int yEndArc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the ending point of the radial line defining the ending point of the arc.
         */
        private int xEndArc;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the ending point of the radial line defining the starting point of the arc.
         */
        private int yStartArc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the ending point of the radial line defining the starting point of the arc.
         */
        private int xStartArc;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        private int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        private int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the bounding rectangle.
         */
        private int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the bounding rectangle.
         */
        private int leftRect;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.arc;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yEndArc = leis.readShort();
            xEndArc = leis.readShort();
            yStartArc = leis.readShort();
            xStartArc = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 8*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            int x = Math.min(leftRect, rightRect);
            int y = Math.min(topRect, bottomRect);
            int w = Math.abs(leftRect - rightRect - 1);
            int h = Math.abs(topRect - bottomRect - 1);
            double startAngle = Math.toDegrees(Math.atan2(-(yStartArc - (topRect + h / 2.)), xStartArc - (leftRect + w / 2.)));
            double endAngle =   Math.toDegrees(Math.atan2(-(yEndArc - (topRect + h / 2.)), xEndArc - (leftRect + w / 2.)));
            double arcAngle = (endAngle - startAngle) + (endAngle - startAngle > 0 ? 0 : 360);
            if (startAngle < 0) {
                startAngle += 360;
            }

            boolean fillShape;
            int arcClosure;
            switch (getRecordType()) {
                default:
                case arc:
                    arcClosure = Arc2D.OPEN;
                    fillShape = false;
                    break;
                case chord:
                    arcClosure = Arc2D.CHORD;
                    fillShape = true;
                    break;
                case pie:
                    arcClosure = Arc2D.PIE;
                    fillShape = true;
                    break;
            }
            
            Shape s = new Arc2D.Double(x, y, w, h, startAngle, arcAngle, arcClosure);
            if (fillShape) {
                ctx.fill(s);
            } else {
                ctx.draw(s);
            }
        }
    }

    /**
     * The META_PIE record draws a pie-shaped wedge bounded by the intersection of an ellipse and two
     * radials. The pie is outlined by using the pen and filled by using the brush that are defined in the
     * playback device context.
     */
    public static class WmfPie extends WmfArc {

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.pie;
        }
    }

    /**
     * The META_CHORD record draws a chord, which is defined by a region bounded by the intersection of
     * an ellipse with a line segment. The chord is outlined using the pen and filled using the brush
     * that are defined in the playback device context.
     */
    public static class WmfChord extends WmfArc {

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.chord;
        }
    }


    /**
     * The META_SELECTOBJECT record specifies a graphics object for the playback device context. The
     * new object replaces the previous object of the same type, unless if the previous object is a palette
     * object. If the previous object is a palette object, then the META_SELECTPALETTE record must be
     * used instead of the META_SELECTOBJECT record, as the META_SELECTOBJECT record does not
     * support replacing the palette object type.
     */
    public static class WmfSelectObject implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to
         * get the object to be selected.
         */
        private int objectIndex;

        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.selectObject;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(objectIndex);
        }
    }
    
    private static int getWindingRule(HwmfGraphics ctx) {
        return ctx.getProperties().getPolyfillMode().awtFlag;
    }
 }
