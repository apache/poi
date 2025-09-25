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

/**
 * Represents a polyline vertex in a shape's geometry.
 * Until POI 5.3.0, this class not was not properly implemented and was throwing an exception.
 */
public class PolyLineTo implements GeometryRow {

    private static final String POLYLINE_FORMULA_PREFIX = "POLYLINE(";
    private static final String POLYLINE_FORMULA_SUFFIX = ")";

    PolyLineTo _master;

    // The x-coordinate of the ending vertex of a polyline.
    Double x;

    // The y-coordinate of the ending vertex of a polyline.
    Double y;

    // The polyline formula
    String a;

    Boolean deleted;

    // TODO: support formulas

    public PolyLineTo(RowType row) {

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
                    a = cell.getV();
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

    public String getA() {
        return a == null ? _master.a : a;
    }

    @Override
    public void setupMaster(GeometryRow row) {
        _master = (PolyLineTo) row;
    }

    @Override
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {
        if (getDel())
            return;

        // A polyline formula: POLYLINE(xType, yType, x1, y1, x2, y2, ...)
        String formula = getA().trim();
        if (!formula.startsWith(POLYLINE_FORMULA_PREFIX) || !formula.endsWith(POLYLINE_FORMULA_SUFFIX)) {
            throw new POIXMLException("Invalid POLYLINE formula: " + formula);
        }

        String[] components = formula
                .substring(POLYLINE_FORMULA_PREFIX.length(), formula.length() - POLYLINE_FORMULA_SUFFIX.length())
                .split(",");

        if (components.length < 2) {
            throw new POIXMLException("Invalid POLYLINE formula (not enough arguments): " + formula);
        }

        if (components.length % 2 != 0) {
            throw new POIXMLException("Invalid POLYLINE formula -- need 2 + n*2 arguments, got " + components.length);
        }

        if (components.length > 2) {
            // If xType is zero, the X coordinates are interpreted as relative coordinates
            double xScale = Integer.parseInt(components[0].trim()) == 0 ? parent.getWidth() : 1.0;
            // If yType is zero, the Y coordinates are interpreted as relative coordinates
            double yScale = Integer.parseInt(components[1].trim()) == 0 ? parent.getHeight() : 1.0;

            for (int i = 2; i < components.length - 1; i += 2) {
                double x = Double.parseDouble(components[i].trim());
                double y = Double.parseDouble(components[i + 1].trim());

                path.lineTo(x * xScale, y * yScale);
            }
        }

        path.lineTo(getX(), getY());
    }
}
