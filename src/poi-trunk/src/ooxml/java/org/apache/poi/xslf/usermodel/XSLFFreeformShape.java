/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.FreeformShape;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTAdjPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTCustomGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DClose;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DCubicBezierTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DLineTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DMoveTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DQuadBezierTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual;

/**
 * Represents a custom geometric shape.
 * This shape will consist of a series of lines and curves described within a creation path.
 */
@Beta
public class XSLFFreeformShape extends XSLFAutoShape
    implements FreeformShape<XSLFShape,XSLFTextParagraph> {

    /*package*/ XSLFFreeformShape(CTShape shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    @Override
    public int setPath(Path2D.Double path) {
        CTPath2D ctPath = CTPath2D.Factory.newInstance();

        Rectangle2D bounds = path.getBounds2D();
        int x0 = Units.toEMU(bounds.getX());
        int y0 = Units.toEMU(bounds.getY());
        PathIterator it = path.getPathIterator(new AffineTransform());
        int numPoints = 0;
        ctPath.setH(Units.toEMU(bounds.getHeight()));
        ctPath.setW(Units.toEMU(bounds.getWidth()));
        while (!it.isDone()) {
            double[] vals = new double[6];
            int type = it.currentSegment(vals);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    CTAdjPoint2D mv = ctPath.addNewMoveTo().addNewPt();
                    mv.setX(Units.toEMU(vals[0]) - x0);
                    mv.setY(Units.toEMU(vals[1]) - y0);
                    numPoints++;
                    break;
                case PathIterator.SEG_LINETO:
                    CTAdjPoint2D ln = ctPath.addNewLnTo().addNewPt();
                    ln.setX(Units.toEMU(vals[0]) - x0);
                    ln.setY(Units.toEMU(vals[1]) - y0);
                    numPoints++;
                    break;
                case PathIterator.SEG_QUADTO:
                    CTPath2DQuadBezierTo qbez = ctPath.addNewQuadBezTo();
                    CTAdjPoint2D qp1 = qbez.addNewPt();
                    qp1.setX(Units.toEMU(vals[0]) - x0);
                    qp1.setY(Units.toEMU(vals[1]) - y0);
                    CTAdjPoint2D qp2 = qbez.addNewPt();
                    qp2.setX(Units.toEMU(vals[2]) - x0);
                    qp2.setY(Units.toEMU(vals[3]) - y0);
                    numPoints += 2;
                    break;
                case PathIterator.SEG_CUBICTO:
                    CTPath2DCubicBezierTo bez = ctPath.addNewCubicBezTo();
                    CTAdjPoint2D p1 = bez.addNewPt();
                    p1.setX(Units.toEMU(vals[0]) - x0);
                    p1.setY(Units.toEMU(vals[1]) - y0);
                    CTAdjPoint2D p2 = bez.addNewPt();
                    p2.setX(Units.toEMU(vals[2]) - x0);
                    p2.setY(Units.toEMU(vals[3]) - y0);
                    CTAdjPoint2D p3 = bez.addNewPt();
                    p3.setX(Units.toEMU(vals[4]) - x0);
                    p3.setY(Units.toEMU(vals[5]) - y0);
                    numPoints += 3;
                    break;
                case PathIterator.SEG_CLOSE:
                    numPoints++;
                    ctPath.addNewClose();
                    break;
                default:
                    throw new IllegalStateException("Unrecognized path segment type: " + type);
            }
            it.next();
        }
        
        XmlObject xo = getShapeProperties();
        if (!(xo instanceof CTShapeProperties)) {
            return -1;
        }

        ((CTShapeProperties)xo).getCustGeom().getPathLst().setPathArray(new CTPath2D[]{ctPath});
        setAnchor(bounds);
        return numPoints;
    }

    @Override
    public Path2D.Double getPath() {
        Path2D.Double path = new Path2D.Double();
        Rectangle2D bounds = getAnchor();

        XmlObject xo = getShapeProperties();
        if (!(xo instanceof CTShapeProperties)) {
            return null;
        }
        
        CTCustomGeometry2D geom = ((CTShapeProperties)xo).getCustGeom();
        for(CTPath2D spPath : geom.getPathLst().getPathArray()){
            double scaleW = bounds.getWidth() / Units.toPoints(spPath.getW());
            double scaleH = bounds.getHeight() / Units.toPoints(spPath.getH());
            for(XmlObject ch : spPath.selectPath("*")){
                if(ch instanceof CTPath2DMoveTo){
                    CTAdjPoint2D pt = ((CTPath2DMoveTo)ch).getPt();
                    path.moveTo(
                            (float) (Units.toPoints((Long) pt.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt.getY()) * scaleH));
                } else if (ch instanceof CTPath2DLineTo){
                    CTAdjPoint2D pt = ((CTPath2DLineTo)ch).getPt();
                    path.lineTo((float)Units.toPoints((Long)pt.getX()),
                                (float)Units.toPoints((Long)pt.getY()));
                } else if (ch instanceof CTPath2DQuadBezierTo){
                    CTPath2DQuadBezierTo bez = ((CTPath2DQuadBezierTo)ch);
                    CTAdjPoint2D pt1 = bez.getPtArray(0);
                    CTAdjPoint2D pt2 = bez.getPtArray(1);
                    path.quadTo(
                            (float) (Units.toPoints((Long) pt1.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt1.getY()) * scaleH),
                            (float) (Units.toPoints((Long) pt2.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt2.getY()) * scaleH));
                } else if (ch instanceof CTPath2DCubicBezierTo){
                    CTPath2DCubicBezierTo bez = ((CTPath2DCubicBezierTo)ch);
                    CTAdjPoint2D pt1 = bez.getPtArray(0);
                    CTAdjPoint2D pt2 = bez.getPtArray(1);
                    CTAdjPoint2D pt3 = bez.getPtArray(2);
                    path.curveTo(
                            (float) (Units.toPoints((Long) pt1.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt1.getY()) * scaleH),
                            (float) (Units.toPoints((Long) pt2.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt2.getY()) * scaleH),
                            (float) (Units.toPoints((Long) pt3.getX()) * scaleW),
                            (float) (Units.toPoints((Long) pt3.getY()) * scaleH));
                } else if (ch instanceof CTPath2DClose){
                    path.closePath();
                }
            }
        }

        // the created path starts at (x=0, y=0).
        // The returned path should fit in the bounding rectangle
        AffineTransform at = new AffineTransform();
        at.translate(bounds.getX(), bounds.getY());
        return new Path2D.Double(at.createTransformedShape(path));
    }
    /**
     * @param shapeId 1-based shapeId
     */
    static CTShape prototype(int shapeId) {
        CTShape ct = CTShape.Factory.newInstance();
        CTShapeNonVisual nvSpPr = ct.addNewNvSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Freeform " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvSpPr();
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTCustomGeometry2D geom = spPr.addNewCustGeom();
        geom.addNewAvLst();
        geom.addNewGdLst();
        geom.addNewAhLst();
        geom.addNewCxnLst();
        CTGeomRect rect = geom.addNewRect();
        rect.setR("r");
        rect.setB("b");
        rect.setT("t");
        rect.setL("l");
        geom.addNewPathLst();
        return ct;
    }
}
