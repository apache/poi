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
import org.apache.poi.xdgf.util.ObjectFactory;

import com.microsoft.schemas.office.visio.x2012.main.RowType;

public class GeometryRowFactory {

    static final ObjectFactory<GeometryRow, RowType> _rowTypes;

    static {
        _rowTypes = new ObjectFactory<>();
        try {
            _rowTypes.put("ArcTo", ArcTo.class, RowType.class);
            _rowTypes.put("Ellipse", Ellipse.class, RowType.class);
            _rowTypes.put("EllipticalArcTo", EllipticalArcTo.class,
                    RowType.class);
            _rowTypes.put("InfiniteLine", InfiniteLine.class, RowType.class);
            _rowTypes.put("LineTo", LineTo.class, RowType.class);
            _rowTypes.put("MoveTo", MoveTo.class, RowType.class);
            _rowTypes.put("NURBSTo", NURBSTo.class, RowType.class);
            // Note - two different spellings depending on version used...!
            _rowTypes.put("PolylineTo", PolyLineTo.class, RowType.class);
            _rowTypes.put("PolyLineTo", PolyLineTo.class, RowType.class);
            _rowTypes.put("RelCubBezTo", RelCubBezTo.class, RowType.class);
            _rowTypes.put("RelEllipticalArcTo", RelEllipticalArcTo.class,
                    RowType.class);
            _rowTypes.put("RelLineTo", RelLineTo.class, RowType.class);
            _rowTypes.put("RelMoveTo", RelMoveTo.class, RowType.class);
            _rowTypes.put("RelQuadBezTo", RelQuadBezTo.class, RowType.class);
            _rowTypes.put("SplineKnot", SplineKnot.class, RowType.class);
            _rowTypes.put("SplineStart", SplineStart.class, RowType.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new POIXMLException("Internal error", e);
        }

    }

    public static GeometryRow load(RowType row) {
        return _rowTypes.load(row.getT(), row);
    }

}
