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

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class RelEllipticalArcTo implements GeometryRow {

    RelEllipticalArcTo _master;

    // The x-coordinate of the ending vertex on an arc relative to the width of
    // the shape.
    Double x;

    // The y-coordinate of the ending vertex on an arc relative to the height of
    // the shape.
    Double y;

    // The x-coordinate of the arc's control point relative to the shape's
    // width; a point on the arc.
    Double a;

    // The y-coordinate of an arc's control point relative to the shape's width.
    Double b;

    // The angle of an arc's major axis relative to the x-axis of its parent.
    Double c;

    // The ratio of an arc's major axis to its minor axis. Despite the usual
    // meaning of these words, the "major" axis does not have to be greater than
    // the "minor" axis, so this ratio does not have to be greater than 1.
    Double d;

    Boolean deleted;

    // TODO: support formulas

    public RelEllipticalArcTo(RowType row) {

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
                            + "' in RelEllipticalArcTo row");
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
        _master = (RelEllipticalArcTo) row;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {

        if (getDel())
            return;

        double w = parent.getWidth();
        double h = parent.getHeight();

        // intentionally shadowing variables here
        double x = getX() * w;
        double y = getY() * h;
        double a = getA() * w;
        double b = getB() * h;
        double c = getC();
        double d = getD();

        EllipticalArcTo.createEllipticalArc(x, y, a, b, c, d, path);

    }
}
