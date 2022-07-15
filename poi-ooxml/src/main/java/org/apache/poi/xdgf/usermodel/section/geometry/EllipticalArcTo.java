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

package org.apache.poi.xdgf.usermodel.section.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class EllipticalArcTo implements GeometryRow {

    EllipticalArcTo _master;

    // The x-coordinate of the ending vertex on an arc.
    Double x;

    // The y-coordinate of the ending vertex on an arc.
    Double y;

    // The x-coordinate of the arc's control point; a point on the arc. The
    // control point is best located about halfway between the beginning and
    // ending vertices of the arc. Otherwise, the arc may grow to an extreme
    // size in order to pass through the control point, with unpredictable
    // results.
    Double a;

    // The y-coordinate of an arc's control point.
    Double b;

    // The angle of an arc's major axis relative to the x-axis of its parent
    // shape.
    Double c;

    // The ratio of an arc's major axis to its minor axis. Despite the usual
    // meaning of these words, the "major" axis does not have to be greater than
    // the "minor" axis, so this ratio does not have to be greater than 1.
    // Setting this cell to a value less than or equal to 0 or greater than 1000
    // can lead to unpredictable results.
    Double d;

    Boolean deleted;

    // TODO: support formulas

    public EllipticalArcTo(RowType row) {

        if (row.isSetDel())
            deleted = row.getDel();

        for (CellType cell : row.getCellArray()) {
            String cellName = cell.getN();

            switch (cellName) {
                case "X":
                    x = XDGFCell.parseDoubleValue(cell);
                    break;
                case "Y":
                    y = XDGFCell.parseDoubleValue(cell);
                    break;
                case "A":
                    a = XDGFCell.parseDoubleValue(cell);
                    break;
                case "B":
                    b = XDGFCell.parseDoubleValue(cell);
                    break;
                case "C":
                    c = XDGFCell.parseDoubleValue(cell);
                    break;
                case "D":
                    d = XDGFCell.parseDoubleValue(cell);
                    break;
                default:
                    throw new POIXMLException("Invalid cell '" + cellName
                            + "' in EllipticalArcTo row");
            }
        }
    }

    public boolean getDel() {
        if (deleted != null)
            return deleted;

        return _master != null && _master.getDel();
    }

    public Double getX() {
        return x == null ? _master.x : x;
    }

    public Double getY() {
        return y == null ? _master.y : y;
    }

    public Double getA() {
        return a == null ? _master.a : a;
    }

    public Double getB() {
        return b == null ? _master.b : b;
    }

    public Double getC() {
        return c == null ? _master.c : c;
    }

    public Double getD() {
        return d == null ? _master.d : d;
    }

    @Override
    public void setupMaster(GeometryRow row) {
        _master = (EllipticalArcTo) row;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {

        if (getDel())
            return;

        // intentionally shadowing variables here
        double x = getX();
        double y = getY();
        double a = getA();
        double b = getB();
        double c = getC();
        double d = getD();

        createEllipticalArc(x, y, a, b, c, d, path);
    }

    public static void createEllipticalArc(double x, double y, double a,
            double b, double c, double d, java.awt.geom.Path2D.Double path) {

        // Formula for center of ellipse by Junichi Yoda & nashwaan:
        // -> From http://visguy.com/vgforum/index.php?topic=2464.0
        //
        // x1,y1 = start; x2,y2 = end; x3,y3 = control point
        //
        // x0 =
        // ((x1-x2)*(x1+x2)*(y2-y3)-(x2-x3)*(x2+x3)*(y1-y2)+D^2*(y1-y2)*(y2-y3)*(y1-y3))/(2*((x1-x2)*(y2-y3)-(x2-x3)*(y1-y2)))
        // y0 =
        // ((x1-x2)*(x2-x3)*(x1-x3)/D^2+(x2-x3)*(y1-y2)*(y1+y2)-(x1-x2)*(y2-y3)*(y2+y3))/(2*((x2-x3)*(y1-y2)-(x1-x2)*(y2-y3)))
        // radii along axis: a = sqrt{ (x1-x0)^2 + (y1-y0)^2 * D^2 }
        //

        Point2D last = path.getCurrentPoint();
        double x0 = last.getX();
        double y0 = last.getY();

        // translate all of the points to the same angle as the ellipse
        AffineTransform at = AffineTransform.getRotateInstance(-c);
        double[] pts = new double[] { x0, y0, x, y, a, b };
        at.transform(pts, 0, pts, 0, 3);

        x0 = pts[0];
        y0 = pts[1];
        x = pts[2];
        y = pts[3];
        a = pts[4];
        b = pts[5];

        // nasty math time

        double d2 = d * d;
        double cx = ((x0 - x) * (x0 + x) * (y - b) - (x - a) * (x + a)
                * (y0 - y) + d2 * (y0 - y) * (y - b) * (y0 - b))
                / (2.0 * ((x0 - x) * (y - b) - (x - a) * (y0 - y)));
        double cy = ((x0 - x) * (x - a) * (x0 - a) / d2 + (x - a) * (y0 - y)
                * (y0 + y) - (x0 - x) * (y - b) * (y + b))
                / (2.0 * ((x - a) * (y0 - y) - (x0 - x) * (y - b)));

        // calculate radii of ellipse
        double rx = Math.sqrt(Math.pow(x0 - cx, 2) + Math.pow(y0 - cy, 2) * d2);
        double ry = rx / d;

        // Arc2D requires us to draw an arc from one point to another, so we
        // need to calculate the angle of the start point and end point along
        // the ellipse
        // - Derived from parametric form of ellipse: x = h + a*cos(t); y = k +
        // b*sin(t)

        double ctrlAngle = Math.toDegrees(Math.atan2((b - cy) / ry, (a - cx)
                / rx));
        double startAngle = Math.toDegrees(Math.atan2((y0 - cy) / ry, (x0 - cx)
                / rx));
        double endAngle = Math.toDegrees(Math.atan2((y - cy) / ry, (x - cx)
                / rx));

        double sweep = computeSweep(startAngle, endAngle, ctrlAngle);

        // Now we have enough information to go on. Create the arc.
        Arc2D arc = new Arc2D.Double(cx - rx, cy - ry, rx * 2, ry * 2,
                -startAngle, sweep, Arc2D.OPEN);

        // rotate the arc back to the original coordinate system
        at.setToRotation(c);
        path.append(at.createTransformedShape(arc), false);
    }

    protected static double computeSweep(double startAngle, double endAngle,
            double ctrlAngle) {
        double sweep;

        startAngle = (360.0 + startAngle) % 360.0;
        endAngle = (360.0 + endAngle) % 360.0;
        ctrlAngle = (360.0 + ctrlAngle) % 360.0;

        // different sweeps depending on where the control point is

        if (startAngle < endAngle) {
            if (startAngle < ctrlAngle && ctrlAngle < endAngle) {
                sweep = startAngle - endAngle;
            } else {
                sweep = 360 + (startAngle - endAngle);
            }
        } else {
            if (endAngle < ctrlAngle && ctrlAngle < startAngle) {
                sweep = startAngle - endAngle;
            } else {
                sweep = -(360 - (startAngle - endAngle));
            }
        }

        return sweep;
    }
}
