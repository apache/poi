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

package org.apache.poi.xdgf.usermodel.section;

import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.geom.SplineCollector;
import org.apache.poi.xdgf.usermodel.XDGFCell;
import org.apache.poi.xdgf.usermodel.XDGFShape;
import org.apache.poi.xdgf.usermodel.XDGFSheet;
import org.apache.poi.xdgf.usermodel.section.geometry.Ellipse;
import org.apache.poi.xdgf.usermodel.section.geometry.GeometryRow;
import org.apache.poi.xdgf.usermodel.section.geometry.GeometryRowFactory;
import org.apache.poi.xdgf.usermodel.section.geometry.InfiniteLine;
import org.apache.poi.xdgf.usermodel.section.geometry.SplineKnot;
import org.apache.poi.xdgf.usermodel.section.geometry.SplineStart;

import com.microsoft.schemas.office.visio.x2012.main.RowType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;

public class GeometrySection extends XDGFSection {

    GeometrySection _master;

    // rows
    SortedMap<Long, GeometryRow> _rows = new TreeMap<>();

    public GeometrySection(SectionType section, XDGFSheet containingSheet) {
        super(section, containingSheet);

        for (RowType row: section.getRowArray()) {
            if (_rows.containsKey(row.getIX()))
                throw new POIXMLException("Index element '" + row.getIX() + "' already exists");

            _rows.put(row.getIX(), GeometryRowFactory.load(row));
        }
    }

    @Override
    public void setupMaster(XDGFSection master) {

        _master = (GeometrySection)master;

        for (Entry<Long, GeometryRow> entry : _rows.entrySet()) {
            GeometryRow masterRow = _master._rows.get(entry.getKey());
            if (masterRow != null) {
                try {
                    entry.getValue().setupMaster(masterRow);
                } catch (ClassCastException e) {
                    // this can happen when a dynamic connector overrides its master's geometry
                    // .. probably can happen elsewhere too, I imagine.
                    //throw XDGFException.error("Mismatched geometry section '" + entry.getKey() + "' in master", this, e);
                }
            }
        }
    }

    // returns True if this row shouldn't be displayed
    public Boolean getNoShow() {
        Boolean noShow = XDGFCell.maybeGetBoolean(_cells, "NoShow");
        if (noShow == null) {
            if (_master != null)
                return _master.getNoShow();

            return false;
        }

        return noShow;
    }

    public Iterable<GeometryRow> getCombinedRows() {
        return new CombinedIterable<>(_rows,
                _master == null ? null : _master._rows);
    }

    public Path2D.Double getPath(XDGFShape parent) {

        Iterator<GeometryRow> rows = getCombinedRows().iterator();

        // special cases
        GeometryRow first = rows.next();

        if (first instanceof Ellipse) {
            return ((Ellipse)first).getPath();
        } else if (first instanceof InfiniteLine) {
            return ((InfiniteLine)first).getPath();
        } else if (first instanceof SplineStart) {
            throw new POIXMLException("SplineStart must be preceded by another type");
        } else {

            // everything else is a path
            Path2D.Double path = new Path2D.Double();

            // dealing with splines makes this more complex
            SplineCollector renderer = null;
            GeometryRow row;

            while (true) {

                if (first != null) {
                    row = first;
                    first = null;
                } else {
                    if (!rows.hasNext())
                        break;
                    row = rows.next();
                }

                if (row instanceof SplineStart) {
                    if (renderer != null)
                        throw new POIXMLException("SplineStart found multiple times!");
                    renderer = new SplineCollector((SplineStart) row);
                } else if (row instanceof SplineKnot) {
                    if (renderer == null)
                        throw new POIXMLException("SplineKnot found without SplineStart!");
                    renderer.addKnot((SplineKnot) row);
                } else {
                    if (renderer != null) {
                        renderer.addToPath(path, parent);
                        renderer = null;
                    }

                    row.addToPath(path, parent);
                }
            }

            // just in case we end iteration
            if (renderer != null)
                renderer.addToPath(path, parent);

            return path;
        }
    }

}
