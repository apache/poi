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

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class ArcTo implements GeometryRow {

    ArcTo _master;

    // The x-coordinate of the ending vertex of an arc.
    Double x;

    // The y-coordinate of the ending vertex of an arc.
    Double y;

    // The distance from the arc's midpoint to the midpoint of its chord.
    Double a;

    Boolean deleted;

    // TODO: support formulas

    public ArcTo(RowType row) {

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
                default:
                    throw new POIXMLException("Invalid cell '" + cellName
                            + "' in ArcTo row");
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

    @Override
    public void setupMaster(GeometryRow row) {
        _master = (ArcTo) row;
    }

    @Override
    public void addToPath(Path2D.Double path, XDGFShape parent) {

        if (getDel())
            return;

        Point2D last = path.getCurrentPoint();

        // intentionally shadowing variables here
        double x = getX();
        double y = getY();
        double a = getA();

        if (a == 0) {
            path.lineTo(x, y);
            return;
        }

        double x0 = last.getX();
        double y0 = last.getY();

        // Find a normal to the chord of the circle.
        double nx = y - y0;
        double ny = x0 - x;
        double nLength = Math.sqrt(nx * nx + ny * ny);

        // Follow the normal with the height of the arc to get the third point on the circle.
        double x1 = (x0 + x) / 2 + a * nx / nLength;
        double y1 = (y0 + y) / 2 + a * ny / nLength;

        // Add an elliptical arc with rx / ry = 1 to the path because it's a circle.
        EllipticalArcTo.createEllipticalArc(x, y, x1, y1, 0.0, 1.0, path);
    }

    @Override
    public String toString() {
        return String.format(LocaleUtil.getUserLocale(),
                "ArcTo: x=%f; y=%f; a=%f", x, y, a);
    }
}
