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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.microsoft.schemas.office.visio.x2012.main.RowType;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.usermodel.section.geometry.ArcTo;
import org.apache.poi.xdgf.usermodel.section.geometry.Ellipse;
import org.apache.poi.xdgf.usermodel.section.geometry.EllipticalArcTo;
import org.apache.poi.xdgf.usermodel.section.geometry.GeometryRow;
import org.apache.poi.xdgf.usermodel.section.geometry.InfiniteLine;
import org.apache.poi.xdgf.usermodel.section.geometry.LineTo;
import org.apache.poi.xdgf.usermodel.section.geometry.MoveTo;
import org.apache.poi.xdgf.usermodel.section.geometry.NURBSTo;
import org.apache.poi.xdgf.usermodel.section.geometry.PolyLineTo;
import org.apache.poi.xdgf.usermodel.section.geometry.RelCubBezTo;
import org.apache.poi.xdgf.usermodel.section.geometry.RelEllipticalArcTo;
import org.apache.poi.xdgf.usermodel.section.geometry.RelLineTo;
import org.apache.poi.xdgf.usermodel.section.geometry.RelMoveTo;
import org.apache.poi.xdgf.usermodel.section.geometry.RelQuadBezTo;
import org.apache.poi.xdgf.usermodel.section.geometry.SplineKnot;
import org.apache.poi.xdgf.usermodel.section.geometry.SplineStart;

@Internal
enum GeometryRowTypes {
    ARC_TO("ArcTo", ArcTo::new),
    ELLIPSE("Ellipse", Ellipse::new),
    ELLIPTICAL_ARC_TO("EllipticalArcTo", EllipticalArcTo::new),
    INFINITE_LINE("InfiniteLine", InfiniteLine::new),
    LINE_TO("LineTo", LineTo::new),
    MOVE_TO("MoveTo", MoveTo::new),
    NURBS_TO("NURBSTo", NURBSTo::new),
    // Note - two different spellings depending on version used...!
    POLYLINE_TO("PolylineTo", PolyLineTo::new),
    REL_CUB_BEZ_TO("RelCubBezTo", RelCubBezTo::new),
    REL_ELLIPTICAL_ARC_TO("RelEllipticalArcTo", RelEllipticalArcTo::new),
    REL_LINE_TO("RelLineTo", RelLineTo::new),
    REL_MOVE_TO("RelMoveTo", RelMoveTo::new),
    REL_QUAD_BEZ_TO("RelQuadBezTo", RelQuadBezTo::new),
    SPLINE_KNOT("SplineKnot", SplineKnot::new),
    SPLINE_START("SplineStart", SplineStart::new)
    ;

    private final String rowType;
    private final Function<RowType, ? extends GeometryRow> constructor;

    GeometryRowTypes(String rowType, Function<RowType, ? extends GeometryRow> constructor) {
        this.rowType = rowType;
        this.constructor = constructor;
    }

    public String getRowType() {
        return rowType;
    }

    public static GeometryRow load(RowType row) {
        final String name = row.getT();
        GeometryRowTypes l = LOOKUP.get(name);
        if (l == null) {
            final String typeName = row.schemaType().getName().getLocalPart();
            throw new POIXMLException("Invalid '" + typeName + "' name '" + name + "'");
        }
        return l.constructor.apply(row);
    }

    private static final Map<String, GeometryRowTypes> LOOKUP =
        Stream.of(values()).collect(Collectors.toMap(GeometryRowTypes::getRowType, Function.identity()));
}
