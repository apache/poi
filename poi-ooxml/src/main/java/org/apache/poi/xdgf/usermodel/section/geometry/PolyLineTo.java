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
import org.apache.poi.util.NotImplemented;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class PolyLineTo implements GeometryRow {

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
    @NotImplemented
    public void addToPath(java.awt.geom.Path2D.Double path, XDGFShape parent) {
        if (getDel())
            return;
        throw new POIXMLException("Polyline support not implemented");
    }
}
