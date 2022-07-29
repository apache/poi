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

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import com.microsoft.schemas.office.visio.x2012.main.TriggerType;

import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xdgf.usermodel.section.GeometrySection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestArcTo {

    private static final double EPS = 0.000001;

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

        assertPath(expectedPath, actualPath);
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

        assertPath(expectedPath, actualPath);
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

        assertPath(expectedPath, actualPath);
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

        assertPath(expectedPath, actualPath);
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
        RowType row = RowType.Factory.newInstance();
        row.setIX(0L);
        row.setDel(false);

        CellType xCell = CellType.Factory.newInstance();
        xCell.setN("X");
        xCell.setV(Double.toString(X));

        CellType yCell = CellType.Factory.newInstance();
        yCell.setN("Y");
        yCell.setV(Double.toString(Y));


        CellType aCell = CellType.Factory.newInstance();
        aCell.setN("A");
        aCell.setV(Double.toString(a));

        CellType[] cells = new CellType[] { xCell , yCell, aCell };
        row.setCellArray(cells);

        return new ArcTo(row);
    }

    private static void assertPath(Path2D expected, Path2D actual) {
        PathIterator expectedIterator = expected.getPathIterator(null);
        PathIterator actualIterator = actual.getPathIterator(null);

        double[] expectedCoordinates = new double[6];
        double[] actualCoordinates = new double[6];
        while (!expectedIterator.isDone() && !actualIterator.isDone()) {
            int expectedSegmentType = expectedIterator.currentSegment(expectedCoordinates);
            int actualSegmentType = actualIterator.currentSegment(actualCoordinates);

            assertEquals(expectedSegmentType, actualSegmentType);
            assertCoordinates(expectedCoordinates, actualCoordinates);

            expectedIterator.next();
            actualIterator.next();
        }

        if (!expectedIterator.isDone() || !actualIterator.isDone()) {
            Assertions.fail("Path iterators have different number of segments");
        }
    }

    private static void assertCoordinates(double[] expected, double[] actual) {
        if (expected.length != actual.length) {
            Assertions.fail(String.format(
                    LocaleUtil.getUserLocale(),
                    "Given coordinates arrays have different length: expected=%s, actual=%s",
                    Arrays.toString(expected), Arrays.toString(actual)));
        }
        for (int i = 0; i < expected.length; i++) {
            double e = expected[i];
            double a = actual[i];

            if (Math.abs(e - a) > EPS) {
                Assertions.fail(String.format(
                        LocaleUtil.getUserLocale(),
                        "expected <%f> but found <%f>", e, a));
            }
        }
    }
}
