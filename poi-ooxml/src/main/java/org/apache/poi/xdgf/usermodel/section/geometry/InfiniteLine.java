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

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

/**
 * Contains the x- and y-coordinates of two points on an infinite line.
 */
public class InfiniteLine implements GeometryRow {

    InfiniteLine _master;

    // An x-coordinate of a point on the infinite line; paired with y-coordinate
    // represented by the Y cell.
    Double x;

    // A y-coordinate of a point on the infinite line; paired with x-coordinate
    // represented by the X cell.
    Double y;

    // An x-coordinate of a point on the infinite line; paired with y-coordinate
    // represented by the B cell.
    Double a;

    // A y-coordinate of a point on an infinite line; paired with x-coordinate
    // represented by the A cell.
    Double b;

    Boolean deleted;

    // TODO: support formulas

    public InfiniteLine(RowType row) {

        if (row.isSetDel())
            deleted = row.getDel();

        for (CellType cell : row.getCellArray()) {
            String cellName = cell.getN();

            if (cellName.equals("X")) {
                x = XDGFCell.parseDoubleValue(cell);
            } else if (cellName.equals("Y")) {
                y = XDGFCell.parseDoubleValue(cell);
            } else if (cellName.equals("A")) {
                a = XDGFCell.parseDoubleValue(cell);
            } else if (cellName.equals("B")) {
                b = XDGFCell.parseDoubleValue(cell);
            } else {
                throw new POIXMLException("Invalid cell '" + cellName
                        + "' in InfiniteLine row");
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

    @Override
    public void setupMaster(GeometryRow row) {
        _master = (InfiniteLine) row;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {

        if (getDel())
            return;

        throw new POIXMLException(
                "InfiniteLine elements cannot be part of a path");
    }

    // returns this object as a line that extends between the boundaries of
    // the document
    public Path2D.Double getPath() {
        Path2D.Double path = new Path2D.Double();

        // this is a bit of a hack, but it works
        double max_val = 100000;

        // compute slope..
        double x0 = getX();
        double y0 = getY();
        double x1 = getA(); // second x
        double y1 = getB(); // second y

        if (x0 == x1) {
            path.moveTo(x0, -max_val);
            path.lineTo(x0, max_val);
        } else if (y0 == y1) {
            path.moveTo(-max_val, y0);
            path.lineTo(max_val, y0);
        } else {

            // normal case: compute slope/intercept
            double m = (y1 - y0) / (x1 - x0);
            double c = y0 - m * x0;

            // y = mx + c

            path.moveTo(max_val, m * max_val + c);
            path.lineTo(max_val, (max_val - c) / m);
        }

        return path;
    }
}
