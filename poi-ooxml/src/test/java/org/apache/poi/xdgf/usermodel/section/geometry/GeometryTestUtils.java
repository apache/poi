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
import com.microsoft.schemas.office.visio.x2012.main.ShapeSheetType;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xdgf.usermodel.XDGFShape;
import org.junit.jupiter.api.Assertions;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class GeometryTestUtils {

    private static final double EPS = 1e-6;

    private GeometryTestUtils() {
    }

    public static RowType createRow(long index, Map<String, Object> cells) {
        RowType row = RowType.Factory.newInstance();
        row.setIX(index);
        row.setDel(false);

        CellType[] cellsArray = cells
                .entrySet()
                .stream()
                .map(entry -> createCell(entry.getKey(), entry.getValue().toString()))
                .toArray(CellType[]::new);
        row.setCellArray(cellsArray);

        return row;
    }

    private static CellType createCell(String name, String value) {
        CellType cell = CellType.Factory.newInstance();
        cell.setN(name);
        cell.setV(value);
        return cell;
    }

    public static void assertPath(Path2D expected, Path2D actual) {
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

    /**
     * Mocks a shape for testing geometries with relative coordinates.
     */
    public static XDGFShape mockShape(double width, double height) {
        ShapeSheetType shapeSheet = ShapeSheetType.Factory.newInstance();
        CellType[] cells = {
                createCell("Width", Double.toString(width)),
                createCell("Height", Double.toString(height))
        };
        shapeSheet.setCellArray(cells);

        // Parent page and document is not used during parsing. It's safe to leave them as nulls for mocking
        return new XDGFShape(shapeSheet, null, null);
    }

}
