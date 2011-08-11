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

import org.apache.poi.sl.usermodel.ShapeContainer;
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
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

/**
 * Represents a custom geometric shape.
 * This shape will consist of a series of lines and curves described within a creation path.
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFFreeformShape extends XSLFAutoShape {

    /*package*/ XSLFFreeformShape(CTShape shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    /**
     * Set the shape path
     *
     * @param path  shape outline
     * @return the number of points written
     */
    public int setPath(GeneralPath path) {
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
            }
            it.next();
        }
        getSpPr().getCustGeom().getPathLst().setPathArray(new CTPath2D[]{ctPath});
        setAnchor(bounds);
        return numPoints;
    }

    public GeneralPath getPath() {
        GeneralPath path = new GeneralPath();
        Rectangle2D bounds = getAnchor();
        int x0 = Units.toEMU(bounds.getX());
        int y0 = Units.toEMU(bounds.getY());
        CTCustomGeometry2D geom = getSpPr().getCustGeom();
        for(CTPath2D spPath : geom.getPathLst().getPathList()){
            for(XmlObject ch : spPath.selectPath("*")){
                if(ch instanceof CTPath2DMoveTo){
                    CTAdjPoint2D pt = ((CTPath2DMoveTo)ch).getPt();
                    path.moveTo(Units.toPoints((Long)pt.getX() + x0),
                                Units.toPoints((Long)pt.getY() + y0));
                } else if (ch instanceof CTPath2DLineTo){
                    CTAdjPoint2D pt = ((CTPath2DLineTo)ch).getPt();
                    path.lineTo(Units.toPoints((Long)pt.getX() + x0),
                                Units.toPoints((Long)pt.getY() + y0));
                } else if (ch instanceof CTPath2DCubicBezierTo){
                    CTPath2DCubicBezierTo bez = ((CTPath2DCubicBezierTo)ch);
                    CTAdjPoint2D pt1 = bez.getPtArray(0);
                    CTAdjPoint2D pt2 = bez.getPtArray(1);
                    CTAdjPoint2D pt3 = bez.getPtArray(2);
                    path.curveTo(
                            Units.toPoints((Long) pt1.getX() + x0),
                            Units.toPoints((Long) pt1.getY() + y0),
                            Units.toPoints((Long) pt2.getX() + x0),
                            Units.toPoints((Long) pt2.getY() + y0),
                            Units.toPoints((Long) pt3.getX() + x0),
                            Units.toPoints((Long) pt3.getY() + y0)
                    );

                } else if (ch instanceof CTPath2DClose){
                    path.closePath();
                }
            }
        }

        return path;
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