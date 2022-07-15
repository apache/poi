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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class Ellipse implements GeometryRow {

    Ellipse _master;

    // x coordinate of center point
    Double x;
    // y coordinate of center point
    Double y;

    // x coordinate of first point on ellipse
    Double a;
    // y coordinate of first point on ellipse
    Double b;

    // x coordinate of second point on ellipse
    Double c;
    // y coordinate of second point on ellipse
    Double d;

    Boolean deleted;

    // TODO: support formulas

    public Ellipse(RowType row) {

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
                            + "' in Ellipse row");
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
        _master = (Ellipse) row;
    }

    public Path2D.Double getPath() {

        if (getDel())
            return null;

        // intentionally shadowing variables here
        double cx = getX(); // center
        double cy = getY();
        double a = getA(); // left
        double b = getB();
        double c = getC(); // top
        double d = getD();

        // compute radius
        double rx = Math.hypot(a - cx, b - cy);
        double ry = Math.hypot(c - cx, d - cy);

        // compute angle of ellipse
        double angle = (2.0 * Math.PI + (cy > b ? 1.0 : -1.0)
                * Math.acos((cx - a) / rx))
                % (2.0 * Math.PI);

        // create ellipse
        Ellipse2D.Double ellipse = new Ellipse2D.Double(cx - rx, cy - ry,
                rx * 2, ry * 2);

        // create a path, rotate it about its center
        Path2D.Double path = new Path2D.Double(ellipse);

        AffineTransform tr = new AffineTransform();
        tr.rotate(angle, cx, cy);
        path.transform(tr);

        return path;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {
        throw new POIXMLException("Ellipse elements cannot be part of a path");
    }
}
