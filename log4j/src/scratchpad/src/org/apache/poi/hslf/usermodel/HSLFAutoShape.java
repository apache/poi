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

package org.apache.poi.hslf.usermodel;

import static org.apache.poi.hslf.usermodel.HSLFFreeformShape.ShapePath.CURVES_CLOSED;
import static org.apache.poi.hslf.usermodel.HSLFFreeformShape.ShapePath.LINES_CLOSED;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.sl.draw.geom.AdjustPoint;
import org.apache.poi.sl.draw.geom.ArcToCommand;
import org.apache.poi.sl.draw.geom.ClosePathCommand;
import org.apache.poi.sl.draw.geom.CurveToCommand;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.draw.geom.LineToCommand;
import org.apache.poi.sl.draw.geom.MoveToCommand;
import org.apache.poi.sl.draw.geom.Path;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Represents an AutoShape.<p>
 *
 * AutoShapes are drawing objects with a particular shape that may be customized through smart resizing and adjustments.
 * See {@link ShapeTypes}
 */
public class HSLFAutoShape extends HSLFTextShape implements AutoShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFAutoShape.class);

    static final byte[] SEGMENTINFO_MOVETO   = new byte[]{0x00, 0x40};
    static final byte[] SEGMENTINFO_LINETO   = new byte[]{0x00, (byte)0xAC};
    static final byte[] SEGMENTINFO_ESCAPE   = new byte[]{0x01, 0x00};
    static final byte[] SEGMENTINFO_ESCAPE2  = new byte[]{0x01, 0x20};
    static final byte[] SEGMENTINFO_CUBICTO  = new byte[]{0x00, (byte)0xAD};
    // OpenOffice inserts 0xB3 instead of 0xAD.
    // protected static final byte[] SEGMENTINFO_CUBICTO2 = new byte[]{0x00, (byte)0xB3};
    static final byte[] SEGMENTINFO_CLOSE    = new byte[]{0x01, (byte)0x60};
    static final byte[] SEGMENTINFO_END      = new byte[]{0x00, (byte)0x80};

    private static final BitField PATH_INFO = BitFieldFactory.getInstance(0xE000);
    private static final BitField ESCAPE_INFO = BitFieldFactory.getInstance(0x1F00);

    enum PathInfo {
        lineTo(0),curveTo(1),moveTo(2),close(3),end(4),escape(5),clientEscape(6);
        private final int flag;
        PathInfo(int flag) {
            this.flag = flag;
        }
        public int getFlag() {
            return flag;
        }
        static PathInfo valueOf(int flag) {
            for (PathInfo v : values()) {
                if (v.flag == flag) {
                    return v;
                }
            }
            return null;
        }
    }

    enum EscapeInfo {
        EXTENSION(0x0000),
        ANGLE_ELLIPSE_TO(0x0001),
        ANGLE_ELLIPSE(0x0002),
        ARC_TO(0x0003),
        ARC(0x0004),
        CLOCKWISE_ARC_TO(0x0005),
        CLOCKWISE_ARC(0x0006),
        ELLIPTICAL_QUADRANT_X(0x0007),
        ELLIPTICAL_QUADRANT_Y(0x0008),
        QUADRATIC_BEZIER(0x0009),
        NO_FILL(0X000A),
        NO_LINE(0X000B),
        AUTO_LINE(0X000C),
        AUTO_CURVE(0X000D),
        CORNER_LINE(0X000E),
        CORNER_CURVE(0X000F),
        SMOOTH_LINE(0X0010),
        SMOOTH_CURVE(0X0011),
        SYMMETRIC_LINE(0X0012),
        SYMMETRIC_CURVE(0X0013),
        FREEFORM(0X0014),
        FILL_COLOR(0X0015),
        LINE_COLOR(0X0016);

        private final int flag;
        EscapeInfo(int flag) {
            this.flag = flag;
        }
        public int getFlag() {
            return flag;
        }
        static EscapeInfo valueOf(int flag) {
            for (EscapeInfo v : values()) {
                if (v.flag == flag) {
                    return v;
                }
            }
            return null;
        }
    }

    protected HSLFAutoShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    public HSLFAutoShape(ShapeType type, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(null, parent);
        createSpContainer(type, parent instanceof HSLFGroupShape);
    }

    public HSLFAutoShape(ShapeType type){
        this(type, null);
    }

    protected EscherContainerRecord createSpContainer(ShapeType shapeType, boolean isChild){
        EscherContainerRecord ecr = super.createSpContainer(isChild);

        setShapeType(shapeType);

        //set default properties for an autoshape
        setEscherProperty(EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING, 0x40000);
        setEscherProperty(EscherPropertyTypes.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(EscherPropertyTypes.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(EscherPropertyTypes.FILL__FILLBACKCOLOR, 0x8000000);
        setEscherProperty(EscherPropertyTypes.FILL__NOFILLHITTEST, 0x100010);
        setEscherProperty(EscherPropertyTypes.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH, 0x80008);
        setEscherProperty(EscherPropertyTypes.SHADOWSTYLE__COLOR, 0x8000002);

        return ecr;
    }

    @Override
    protected void setDefaultTextProperties(HSLFTextParagraph _txtrun){
        setVerticalAlignment(VerticalAlignment.MIDDLE);
        setHorizontalCentered(true);
        setWordWrap(false);
    }

    /**
     * Gets adjust value which controls smart resizing of the auto-shape.<p>
     *
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     *
     * @param idx the adjust index in the [0, 9] range
     * @return the adjustment value
     */
    public int getAdjustmentValue(int idx){
        if(idx < 0 || idx > 9) throw new IllegalArgumentException("The index of an adjustment value must be in the [0, 9] range");
        return getEscherProperty(ADJUST_VALUES[idx]);
    }

    /**
     * Sets adjust value which controls smart resizing of the auto-shape.<p>
     *
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     *
     * @param idx the adjust index in the [0, 9] range
     * @param val the adjustment value
     */
    public void setAdjustmentValue(int idx, int val){
        if(idx < 0 || idx > 9) throw new IllegalArgumentException("The index of an adjustment value must be in the [0, 9] range");
        setEscherProperty(ADJUST_VALUES[idx], val);
    }

    @Override
    public CustomGeometry getGeometry() {
        return getGeometry(new Path2D.Double());
    }

    CustomGeometry getGeometry(Path2D path2D) {
        final CustomGeometry cusGeo = new CustomGeometry();

        final AbstractEscherOptRecord opt = getEscherOptRecord();

        EscherArrayProperty verticesProp = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__VERTICES);
        EscherArrayProperty segmentsProp = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__SEGMENTINFO);

        // return empty path if either GEOMETRY__VERTICES or GEOMETRY__SEGMENTINFO is missing, see Bugzilla 54188

        //sanity check
        if(verticesProp == null) {
            LOG.log(POILogger.WARN, "Freeform is missing GEOMETRY__VERTICES ");
            return super.getGeometry();
        }
        if(segmentsProp == null) {
            LOG.log(POILogger.WARN, "Freeform is missing GEOMETRY__SEGMENTINFO ");
            return super.getGeometry();
        }

        final Iterator<byte[]> vertIter = verticesProp.iterator();
        final Iterator<byte[]> segIter = segmentsProp.iterator();
        final int[] xyPoints = new int[2];
        boolean isClosed = false;

        final Path path = new Path();
        cusGeo.addPath(path);

        while (segIter.hasNext()) {
            byte[] segElem = segIter.next();
            HSLFAutoShape.PathInfo pi = getPathInfo(segElem);
            if (pi == null) {
                continue;
            }
            switch (pi) {
                case escape:
                    handleEscapeInfo(path, path2D, segElem, vertIter);
                    break;
                case moveTo:
                    handleMoveTo(vertIter, xyPoints, path, path2D);
                    break;
                case lineTo:
                    handleLineTo(vertIter, xyPoints, path, path2D);
                    break;
                case curveTo:
                    handleCurveTo(vertIter, xyPoints, path, path2D);
                    break;
                case close:
                    if (path2D.getCurrentPoint() != null) {
                        path.addCommand(new ClosePathCommand());
                        path2D.closePath();
                    }
                    isClosed = true;
                    break;
                default:
                    break;
            }
        }

        if (!isClosed) {
            handleClosedShape(opt, path, path2D);
        }

        final Rectangle2D bounds = getBounds(opt, path2D);

        path.setW((int)Math.rint(bounds.getWidth()));
        path.setH((int)Math.rint(bounds.getHeight()));

        return cusGeo;
    }

    private static Rectangle2D getBounds(AbstractEscherOptRecord opt, Path2D path2D) {
        EscherSimpleProperty geoLeft = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__LEFT);
        EscherSimpleProperty geoRight = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__RIGHT);
        EscherSimpleProperty geoTop = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__TOP);
        EscherSimpleProperty geoBottom = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__BOTTOM);

        if (geoLeft != null && geoRight != null && geoTop != null && geoBottom != null) {
            final Rectangle2D bounds = new Rectangle2D.Double();
            bounds.setFrameFromDiagonal(
                    new Point2D.Double(geoLeft.getPropertyValue(), geoTop.getPropertyValue()),
                    new Point2D.Double(geoRight.getPropertyValue(), geoBottom.getPropertyValue())
            );
            return bounds;
        } else {
            return path2D.getBounds2D();
        }
    }

    private static void handleClosedShape(AbstractEscherOptRecord opt, Path path, Path2D path2D) {
        EscherSimpleProperty shapePath = getEscherProperty(opt, EscherPropertyTypes.GEOMETRY__SHAPEPATH);
        HSLFFreeformShape.ShapePath sp = HSLFFreeformShape.ShapePath.valueOf(shapePath == null ? 1 : shapePath.getPropertyValue());
        if (sp == LINES_CLOSED || sp == CURVES_CLOSED) {
            path.addCommand(new ClosePathCommand());
            path2D.closePath();
        }
    }

    private static void handleMoveTo(Iterator<byte[]> vertIter, int[] xyPoints, Path path, Path2D path2D) {
        if (!vertIter.hasNext()) {
            return;
        }
        final MoveToCommand m = new MoveToCommand();
        m.setPt(fillPoint(vertIter.next(), xyPoints));
        path.addCommand(m);
        path2D.moveTo(xyPoints[0], xyPoints[1]);
    }

    private static void handleLineTo(Iterator<byte[]> vertIter, int[] xyPoints, Path path, Path2D path2D) {
        if (!vertIter.hasNext()) {
            return;
        }
        handleMoveTo0(path, path2D);

        final LineToCommand m = new LineToCommand();
        m.setPt(fillPoint(vertIter.next(), xyPoints));
        path.addCommand(m);
        path2D.lineTo(xyPoints[0], xyPoints[1]);
    }

    private static void handleCurveTo(Iterator<byte[]> vertIter, int[] xyPoints, Path path, Path2D path2D) {
        if (!vertIter.hasNext()) {
            return;
        }
        handleMoveTo0(path, path2D);

        final CurveToCommand m = new CurveToCommand();

        int[] pts = new int[6];
        AdjustPoint[] ap = new AdjustPoint[3];

        for (int i=0; vertIter.hasNext() && i<3; i++) {
            ap[i] = fillPoint(vertIter.next(), xyPoints);
            pts[i*2] = xyPoints[0];
            pts[i*2+1] = xyPoints[1];
        }

        m.setPt1(ap[0]);
        m.setPt2(ap[1]);
        m.setPt3(ap[2]);

        path.addCommand(m);
        path2D.curveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
    }

    /**
     * Sometimes the path2D is not initialized - this initializes it with the 0,0 position
     */
    private static void handleMoveTo0(Path moveLst, Path2D path2D) {
        if (path2D.getCurrentPoint() == null) {
            final MoveToCommand m = new MoveToCommand();

            AdjustPoint pt = new AdjustPoint();
            pt.setX("0");
            pt.setY("0");
            m.setPt(pt);
            moveLst.addCommand(m);
            path2D.moveTo(0, 0);
        }
    }

    private static void handleEscapeInfo(Path pathCT, Path2D path2D, byte[] segElem, Iterator<byte[]> vertIter) {
        HSLFAutoShape.EscapeInfo ei = getEscapeInfo(segElem);
        if (ei == null) {
            return;
        }
        switch (ei) {
            case EXTENSION:
                break;
            case ANGLE_ELLIPSE_TO:
                break;
            case ANGLE_ELLIPSE:
                break;
            case ARC_TO: {
                // The first two POINT values specify the bounding rectangle of the ellipse.
                // The second two POINT values specify the radial vectors for the ellipse.
                // The radial vectors are cast from the center of the bounding rectangle.
                // The path starts at the POINT where the first radial vector intersects the
                // bounding rectangle and goes to the POINT where the second radial vector
                // intersects the bounding rectangle. The drawing direction is always counterclockwise.
                // If the path has already been started, a line is drawn from the last POINT to
                // the starting POINT of the arc; otherwise, a new path is started.
                // The number of arc segments drawn equals the number of segments divided by four.

                int[] r1 = new int[2], r2 = new int[2], start = new int[2], end = new int[2];
                fillPoint(vertIter.next(), r1);
                fillPoint(vertIter.next(), r2);
                fillPoint(vertIter.next(), start);
                fillPoint(vertIter.next(), end);

                Arc2D arc2D = new Arc2D.Double();
                Rectangle2D.Double bounds = new Rectangle2D.Double();
                bounds.setFrameFromDiagonal(xy2p(r1), xy2p(r2));
                arc2D.setFrame(bounds);
                arc2D.setAngles(xy2p(start), xy2p(end));
                path2D.append(arc2D, true);


                ArcToCommand arcTo = new ArcToCommand();
                arcTo.setHR(d2s(bounds.getHeight()/2.0));
                arcTo.setWR(d2s(bounds.getWidth()/2.0));

                arcTo.setStAng(d2s(-arc2D.getAngleStart()*60000.));
                arcTo.setSwAng(d2s(-arc2D.getAngleExtent()*60000.));

                pathCT.addCommand(arcTo);

                break;
            }
            case ARC:
                break;
            case CLOCKWISE_ARC_TO:
                break;
            case CLOCKWISE_ARC:
                break;
            case ELLIPTICAL_QUADRANT_X:
                break;
            case ELLIPTICAL_QUADRANT_Y:
                break;
            case QUADRATIC_BEZIER:
                break;
            case NO_FILL:
                break;
            case NO_LINE:
                break;
            case AUTO_LINE:
                break;
            case AUTO_CURVE:
                break;
            case CORNER_LINE:
                break;
            case CORNER_CURVE:
                break;
            case SMOOTH_LINE:
                break;
            case SMOOTH_CURVE:
                break;
            case SYMMETRIC_LINE:
                break;
            case SYMMETRIC_CURVE:
                break;
            case FREEFORM:
                break;
            case FILL_COLOR:
                break;
            case LINE_COLOR:
                break;
            default:
                break;
        }
    }

    private static String d2s(double d) {
        return Integer.toString((int)Math.rint(d));
    }

    private static Point2D xy2p(int[] xyPoints) {
        return new Point2D.Double(xyPoints[0],xyPoints[1]);
    }

    private static HSLFAutoShape.PathInfo getPathInfo(byte[] elem) {
        int elemUS = LittleEndian.getUShort(elem, 0);
        int pathInfo = PATH_INFO.getValue(elemUS);
        return HSLFAutoShape.PathInfo.valueOf(pathInfo);
    }

    private static HSLFAutoShape.EscapeInfo getEscapeInfo(byte[] elem) {
        int elemUS = LittleEndian.getUShort(elem, 0);
        int escInfo = ESCAPE_INFO.getValue(elemUS);
        return HSLFAutoShape.EscapeInfo.valueOf(escInfo);
    }


    private static AdjustPoint fillPoint(byte[] xyMaster, int[] xyPoints) {
        if (xyMaster == null || xyPoints == null) {
            LOG.log(POILogger.WARN, "Master bytes or points not set - ignore point");
            return null;
        }
        if ((xyMaster.length != 4 && xyMaster.length != 8) || xyPoints.length != 2) {
            LOG.log(POILogger.WARN, "Invalid number of master bytes for a single point - ignore point");
            return null;
        }

        int x, y;
        if (xyMaster.length == 4) {
            x = LittleEndian.getShort(xyMaster, 0);
            y = LittleEndian.getShort(xyMaster, 2);
        } else {
            x = LittleEndian.getInt(xyMaster, 0);
            y = LittleEndian.getInt(xyMaster, 4);
        }

        xyPoints[0] = x;
        xyPoints[1] = y;

        return toPoint(xyPoints);
    }

    private static AdjustPoint toPoint(int[] xyPoints) {
        AdjustPoint pt = new AdjustPoint();
        pt.setX(Integer.toString(xyPoints[0]));
        pt.setY(Integer.toString(xyPoints[1]));
        return pt;
    }
}
