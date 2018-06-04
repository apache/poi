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

import java.awt.geom.Point2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.geom.SplineRenderer;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.ShapeMultiPath;
import com.graphbuilder.curve.ValueVector;
import com.graphbuilder.geom.PointFactory;
import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class NURBSTo implements GeometryRow {

    NURBSTo _master;

    // The x-coordinate of the last control point of a NURBS.
    Double x;

    // The y-coordinate of the last control point of a NURBS.
    Double y;

    // The second to the last knot of the NURBS.
    Double a;

    // The last weight of the NURBS.
    Double b;

    // The first knot of the NURBS.
    Double c;

    // The first weight of the NURBS.
    Double d;

    // A NURBS formula.
    String e;

    Boolean deleted;

    // TODO: support formulas

    public NURBSTo(RowType row) {

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
            } else if (cellName.equals("C")) {
                c = XDGFCell.parseDoubleValue(cell);
            } else if (cellName.equals("D")) {
                d = XDGFCell.parseDoubleValue(cell);
            } else if (cellName.equals("E")) {
                e = cell.getV();
            } else {
                throw new POIXMLException("Invalid cell '" + cellName
                        + "' in NURBS row");
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

    public String getE() {
        return e == null ? _master.e : e;
    }

    @Override
    public void setupMaster(GeometryRow row) {
        _master = (NURBSTo) row;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {
        if (getDel())
            return;

        Point2D last = path.getCurrentPoint();

        // A NURBS formula: knotLast, degree, xType, yType, x1, y1, knot1,
        // weight1, ..
        String formula = getE().trim();
        if (!formula.startsWith("NURBS(") || !formula.endsWith(")"))
            throw new POIXMLException("Invalid NURBS formula: " + formula);

        String[] components = formula.substring(6, formula.length() - 1).split(
                ",");

        if (components.length < 8)
            throw new POIXMLException(
                    "Invalid NURBS formula (not enough arguments)");

        if ((components.length - 4) % 4 != 0)
            throw new POIXMLException(
                    "Invalid NURBS formula -- need 4 + n*4 arguments, got "
                            + components.length);

        double lastControlX = getX();
        double lastControlY = getY();
        double secondToLastKnot = getA();
        double lastWeight = getB();
        double firstKnot = getC();
        double firstWeight = getD();

        double lastKnot = Double.parseDouble(components[0].trim());
        int degree = Integer.parseInt(components[1].trim());
        int xType = Integer.parseInt(components[2].trim());
        int yType = Integer.parseInt(components[3].trim());

        double xScale = 1;
        double yScale = 1;

        if (xType == 0)
            xScale = parent.getWidth();
        if (yType == 0)
            yScale = parent.getHeight();

        // setup first knots/weights/control point
        ControlPath controlPath = new ControlPath();
        ValueVector knots = new ValueVector();
        ValueVector weights = new ValueVector();

        knots.add(firstKnot);
        weights.add(firstWeight);
        controlPath.addPoint(PointFactory.create(last.getX(), last.getY()));

        // iterate get knots/weights
        int sets = (components.length - 4) / 4;
        for (int i = 0; i < sets; i++) {
            double x1 = Double.parseDouble(components[4 + i * 4 + 0].trim());
            double y1 = Double.parseDouble(components[4 + i * 4 + 1].trim());
            double k = Double.parseDouble(components[4 + i * 4 + 2].trim());
            double w = Double.parseDouble(components[4 + i * 4 + 3].trim());

            controlPath.addPoint(PointFactory.create(x1 * xScale, y1 * yScale));
            knots.add(k);
            weights.add(w);
        }

        // last knots/weights/control point
        knots.add(secondToLastKnot);
        knots.add(lastKnot);

        weights.add(lastWeight);

        controlPath.addPoint(PointFactory.create(lastControlX, lastControlY));

        ShapeMultiPath shape = SplineRenderer.createNurbsSpline(controlPath,
                knots, weights, degree);
        path.append(shape, true);
    }
}
