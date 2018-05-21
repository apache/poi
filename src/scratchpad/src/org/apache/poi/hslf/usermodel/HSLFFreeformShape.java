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

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.sl.usermodel.FreeformShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * A "Freeform" shape.
 *
 * <p>
 * Shapes drawn with the "Freeform" tool have cubic bezier curve segments in the smooth sections
 * and straight-line segments in the straight sections. This object closely corresponds to <code>java.awt.geom.GeneralPath</code>.
 * </p>
 */
public final class HSLFFreeformShape extends HSLFAutoShape implements FreeformShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFFreeformShape.class);

    private static final byte[] SEGMENTINFO_MOVETO   = new byte[]{0x00, 0x40};
    private static final byte[] SEGMENTINFO_LINETO   = new byte[]{0x00, (byte)0xAC};
    private static final byte[] SEGMENTINFO_ESCAPE   = new byte[]{0x01, 0x00};
    private static final byte[] SEGMENTINFO_ESCAPE2  = new byte[]{0x01, 0x20};
    private static final byte[] SEGMENTINFO_CUBICTO  = new byte[]{0x00, (byte)0xAD};
    // OpenOffice inserts 0xB3 instead of 0xAD.
    // private static final byte[] SEGMENTINFO_CUBICTO2 = new byte[]{0x00, (byte)0xB3};
    private static final byte[] SEGMENTINFO_CLOSE    = new byte[]{0x01, (byte)0x60};
    private static final byte[] SEGMENTINFO_END      = new byte[]{0x00, (byte)0x80};

    private static final BitField PATH_INFO = BitFieldFactory.getInstance(0xE000);
    // private static final BitField ESCAPE_INFO = BitFieldFactory.getInstance(0x1F00);

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

    enum ShapePath {
        LINES(0),
        LINES_CLOSED(1),
        CURVES(2),
        CURVES_CLOSED(3),
        COMPLEX(4);

        private final int flag;
        ShapePath(int flag) {
            this.flag = flag;
        }
        public int getFlag() {
            return flag;
        }
        static ShapePath valueOf(int flag) {
            for (ShapePath v : values()) {
                if (v.flag == flag) {
                    return v;
                }
            }
            return null;
        }
    }
    
    /**
     * Create a Freeform object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected HSLFFreeformShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFFreeformShape(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super((EscherContainerRecord)null, parent);
        createSpContainer(ShapeType.NOT_PRIMITIVE, parent instanceof HSLFGroupShape);
    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     */
    public HSLFFreeformShape(){
        this(null);
    }

    @Override
    public int setPath(Path2D.Double path) {
        Rectangle2D bounds = path.getBounds2D();
        PathIterator it = path.getPathIterator(new AffineTransform());

        List<byte[]> segInfo = new ArrayList<>();
        List<Point2D.Double> pntInfo = new ArrayList<>();
        boolean isClosed = false;
        int numPoints = 0;
        while (!it.isDone()) {
            double[] vals = new double[6];
            int type = it.currentSegment(vals);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(SEGMENTINFO_MOVETO);
                    numPoints++;
                    break;
                case PathIterator.SEG_LINETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_ESCAPE);
                    numPoints++;
                    break;
                case PathIterator.SEG_CUBICTO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    pntInfo.add(new Point2D.Double(vals[2], vals[3]));
                    pntInfo.add(new Point2D.Double(vals[4], vals[5]));
                    segInfo.add(SEGMENTINFO_CUBICTO);
                    segInfo.add(SEGMENTINFO_ESCAPE2);
                    numPoints++;
                    break;
                case PathIterator.SEG_QUADTO:
                    //TODO: figure out how to convert SEG_QUADTO into SEG_CUBICTO
                    LOG.log(POILogger.WARN, "SEG_QUADTO is not supported");
                    break;
                case PathIterator.SEG_CLOSE:
                    pntInfo.add(pntInfo.get(0));
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_ESCAPE);
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_CLOSE);
                    isClosed = true;
                    numPoints++;
                    break;
                default:
                    LOG.log(POILogger.WARN, "Ignoring invalid segment type "+type);
                    break;
            }

            it.next();
        }
        if(!isClosed) {
            segInfo.add(SEGMENTINFO_LINETO);
        }
        segInfo.add(SEGMENTINFO_END);

        AbstractEscherOptRecord opt = getEscherOptRecord();
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 0x4));

        EscherArrayProperty verticesProp = new EscherArrayProperty((short)(EscherProperties.GEOMETRY__VERTICES + 0x4000), false, null);
        verticesProp.setNumberOfElementsInArray(pntInfo.size());
        verticesProp.setNumberOfElementsInMemory(pntInfo.size());
        verticesProp.setSizeOfElements(8);
        for (int i = 0; i < pntInfo.size(); i++) {
            Point2D.Double pnt = pntInfo.get(i);
            byte[] data = new byte[8];
            LittleEndian.putInt(data, 0, Units.pointsToMaster(pnt.getX() - bounds.getX()));
            LittleEndian.putInt(data, 4, Units.pointsToMaster(pnt.getY() - bounds.getY()));
            verticesProp.setElement(i, data);
        }
        opt.addEscherProperty(verticesProp);

        EscherArrayProperty segmentsProp = new EscherArrayProperty((short)(EscherProperties.GEOMETRY__SEGMENTINFO + 0x4000), false, null);
        segmentsProp.setNumberOfElementsInArray(segInfo.size());
        segmentsProp.setNumberOfElementsInMemory(segInfo.size());
        segmentsProp.setSizeOfElements(0x2);
        for (int i = 0; i < segInfo.size(); i++) {
            byte[] seg = segInfo.get(i);
            segmentsProp.setElement(i, seg);
        }
        opt.addEscherProperty(segmentsProp);

        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__RIGHT, Units.pointsToMaster(bounds.getWidth())));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__BOTTOM, Units.pointsToMaster(bounds.getHeight())));

        opt.sortProperties();

        setAnchor(bounds);

        return numPoints;
    }

    @Override
    public Path2D.Double getPath(){
        AbstractEscherOptRecord opt = getEscherOptRecord();

        EscherArrayProperty verticesProp = getShapeProp(opt, EscherProperties.GEOMETRY__VERTICES);
        EscherArrayProperty segmentsProp = getShapeProp(opt, EscherProperties.GEOMETRY__SEGMENTINFO);

        // return empty path if either GEOMETRY__VERTICES or GEOMETRY__SEGMENTINFO is missing, see Bugzilla 54188
        Path2D.Double path = new Path2D.Double();

        //sanity check
        if(verticesProp == null) {
            LOG.log(POILogger.WARN, "Freeform is missing GEOMETRY__VERTICES ");
            return path;
        }
        if(segmentsProp == null) {
            LOG.log(POILogger.WARN, "Freeform is missing GEOMETRY__SEGMENTINFO ");
            return path;
        }

        Iterator<byte[]> vertIter = verticesProp.iterator();
        Iterator<byte[]> segIter = segmentsProp.iterator();
        double xyPoints[] = new double[2];
        
        while (vertIter.hasNext() && segIter.hasNext()) {
            byte[] segElem = segIter.next();
            PathInfo pi = getPathInfo(segElem);
            if (pi != null) {
                switch (pi) {
                    case escape: {
                        // handleEscapeInfo(path, segElem, vertIter);
                        break;
                    }
                    case moveTo: {
                        fillPoint(vertIter.next(), xyPoints);
                        double x = xyPoints[0];
                        double y = xyPoints[1];
                        path.moveTo(x, y);
                        break;
                    }
                    case curveTo: {
                        fillPoint(vertIter.next(), xyPoints);
                        double x1 = xyPoints[0];
                        double y1 = xyPoints[1];
                        fillPoint(vertIter.next(), xyPoints);
                        double x2 = xyPoints[0];
                        double y2 = xyPoints[1];
                        fillPoint(vertIter.next(), xyPoints);
                        double x3 = xyPoints[0];
                        double y3 = xyPoints[1];
                        path.curveTo(x1, y1, x2, y2, x3, y3);
                        break;
                    }
                    case lineTo:
                        if (vertIter.hasNext()) {
                            fillPoint(vertIter.next(), xyPoints);
                            double x = xyPoints[0];
                            double y = xyPoints[1];
                            path.lineTo(x, y);
                        }
                        break;
                    case close:
                        path.closePath();
                        break;
                    default:
                        break;
                }
            }
        }

        EscherSimpleProperty shapePath = getShapeProp(opt, EscherProperties.GEOMETRY__SHAPEPATH);
        ShapePath sp = ShapePath.valueOf(shapePath == null ? 1 : shapePath.getPropertyValue());
        if (sp == ShapePath.LINES_CLOSED || sp == ShapePath.CURVES_CLOSED) {
            path.closePath();
        }
        
        Rectangle2D anchor = getAnchor();
        Rectangle2D bounds = path.getBounds2D();
        AffineTransform at = new AffineTransform();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(
                anchor.getWidth()/bounds.getWidth(),
                anchor.getHeight()/bounds.getHeight()
        );
        return new Path2D.Double(at.createTransformedShape(path));
    }
    
    private void fillPoint(byte xyMaster[], double xyPoints[]) {
        if (xyMaster == null || xyPoints == null) {
            LOG.log(POILogger.WARN, "Master bytes or points not set - ignore point");
            return;
        }
        if ((xyMaster.length != 4 && xyMaster.length != 8) || xyPoints.length != 2) {
            LOG.log(POILogger.WARN, "Invalid number of master bytes for a single point - ignore point");
            return;
        }
        
        int x, y;
        if (xyMaster.length == 4) {
            x = LittleEndian.getShort(xyMaster, 0);
            y = LittleEndian.getShort(xyMaster, 2);
        } else {
            x = LittleEndian.getInt(xyMaster, 0);
            y = LittleEndian.getInt(xyMaster, 4);
        }
        
        xyPoints[0] = Units.masterToPoints(x);
        xyPoints[1] = Units.masterToPoints(y);
    }
    
    private static <T extends EscherProperty> T getShapeProp(AbstractEscherOptRecord opt, int propId) {
        T prop = getEscherProperty(opt, (short)(propId + 0x4000));
        if (prop == null) {
            prop = getEscherProperty(opt, propId);
        }
        return prop;
    }
    
//    private void handleEscapeInfo(Path2D path, byte segElem[], Iterator<byte[]> vertIter) {
//        EscapeInfo ei = getEscapeInfo(segElem);
//        switch (ei) {
//            case EXTENSION:
//                break;
//            case ANGLE_ELLIPSE_TO:
//                break;
//            case ANGLE_ELLIPSE:
//                break;
//            case ARC_TO:
//                break;
//            case ARC:
//                break;
//            case CLOCKWISE_ARC_TO:
//                break;
//            case CLOCKWISE_ARC:
//                break;
//            case ELLIPTICAL_QUADRANT_X:
//                break;
//            case ELLIPTICAL_QUADRANT_Y:
//                break;
//            case QUADRATIC_BEZIER:
//                break;
//            case NO_FILL:
//                break;
//            case NO_LINE:
//                break;
//            case AUTO_LINE:
//                break;
//            case AUTO_CURVE:
//                break;
//            case CORNER_LINE:
//                break;
//            case CORNER_CURVE:
//                break;
//            case SMOOTH_LINE:
//                break;
//            case SMOOTH_CURVE:
//                break;
//            case SYMMETRIC_LINE:
//                break;
//            case SYMMETRIC_CURVE:
//                break;
//            case FREEFORM:
//                break;
//            case FILL_COLOR:
//                break;
//            case LINE_COLOR:
//                break;
//            default:
//                break;
//        }
//    }
    

    private static PathInfo getPathInfo(byte elem[]) {
        int elemUS = LittleEndian.getUShort(elem, 0);
        int pathInfo = PATH_INFO.getValue(elemUS);
        return PathInfo.valueOf(pathInfo);
    }
    
//    private static EscapeInfo getEscapeInfo(byte elem[]) {
//        int elemUS = LittleEndian.getUShort(elem, 0);
//        int escInfo = ESCAPE_INFO.getValue(elemUS);
//        return EscapeInfo.valueOf(escInfo);
//    }
}
