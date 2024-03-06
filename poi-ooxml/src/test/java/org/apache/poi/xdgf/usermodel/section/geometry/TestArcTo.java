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

import com.microsoft.schemas.office.visio.x2012.main.RowType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import com.microsoft.schemas.office.visio.x2012.main.TriggerType;

import org.apache.poi.xdgf.usermodel.section.GeometrySection;
import org.junit.jupiter.api.Test;

import java.awt.geom.Path2D;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestArcTo {

    // We draw a circular arc with radius 100 from (0, 0) to (100, 100)
    private static final double X0 = 0.0;
    private static final double Y0 = 0.0;
    private static final double X = 100.0;
    private static final double Y = 100.0;
    private static final double A = 29.289322; // a = radius - sqrt(((x + x0) / 2) ^ 2 + ((y + y0) / 2) ^2)

    @Test
    public void shouldDrawCircularArcWhenArcHeightMoreThanZero() {
        ArcTo arcTo = createArcTo(A);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating a circular arc
        arcTo.addToPath(actualPath, null);

        // This path can be used to draw a curve that approximates calculated arc.
        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.curveTo(26.521649, 0.0, 51.957040, 10.535684, 70.710678, 29.289321);
        expectedPath.curveTo(89.464316, 48.042960, 100.000000, 73.478351, X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @Test
    public void shouldDrawCircularArcWhenArcHeightLessThanZero() {
        ArcTo arcTo = createArcTo(-A);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating a circular arc
        arcTo.addToPath(actualPath, null);

        // This path can be used to draw a curve that approximates calculated arc.
        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.curveTo(0.0, 26.521649, 10.535684, 51.957040, 29.289321, 70.710678);
        expectedPath.curveTo(48.042960, 89.464316,  73.478351, 100.000000,  X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @Test
    public void shouldDrawLineInsteadOfArcWhenArcHeightIsZero() {
        ArcTo arcTo = createArcTo(0.0);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating a circular arc
        arcTo.addToPath(actualPath, null);

        // This path can be used to draw a curve that approximates calculated arc.
        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.lineTo(X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @Test
    public void shouldNotDrawAnythingWhenArcIsDeleted() {
        RowType row = RowType.Factory.newInstance();
        row.setIX(0L);
        row.setDel(true);

        ArcTo arcTo = new ArcTo(row);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating a circular arc
        arcTo.addToPath(actualPath, null);

        // This path can be used to draw a curve that approximates calculated arc.
        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    // this test is mostly used to trigger inclusion of some
    // classes into poi-ooxml-lite
    @Test
    public void testSnapshot() {
        SectionType sectionType = SectionType.Factory.newInstance();

        GeometrySection section = new GeometrySection(sectionType, null);
        assertNotNull(section);

        TriggerType[] triggerArray = sectionType.getTriggerArray();
        assertNotNull(triggerArray);

        RowType[] rowArray = sectionType.getRowArray();
        assertNotNull(rowArray);
    }

    private static ArcTo createArcTo(double a) {
        RowType row = GeometryTestUtils.createRow(
                0L,
                new HashMap<String, Object>() {{
                    put("X", X);
                    put("Y", Y);
                    put("A", a);
                }}
        );
        return new ArcTo(row);
    }

}
