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
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xdgf.usermodel.XDGFShape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.geom.Path2D;
import java.util.HashMap;

public class TestPolylineTo {

    private static final double X0 = 0.0;
    private static final double Y0 = 0.0;
    private static final double X = 100.0;
    private static final double Y = 100.0;

    @ParameterizedTest
    @ValueSource(strings = {
            "POLYLINE(1, 1, 0.0, 50.0, 100.0, 50.0)",
            "POLYLINE(1, 0, 0.0,  0.5, 100.0,  0.5)",
            "POLYLINE(0, 1, 0.0, 50.0,   1.0, 50.0)",
            "POLYLINE(0, 0, 0.0,  0.5,   1.0,  0.5)"
    })
    public void shouldAddMultipleLinesToPath(String formula) {
        PolyLineTo polyLine = createPolyLine(formula);

        XDGFShape parent = GeometryTestUtils.mockShape(X - X0, Y - Y0);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        polyLine.addToPath(actualPath, parent);

        Path2D expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.lineTo(0.0, 50.0);
        expectedPath.lineTo(100.0, 50.0);
        expectedPath.lineTo(X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @Test
    public void shouldAddSingleLineToPath() {
        PolyLineTo polyLine = createPolyLine("POLYLINE(1, 1)");

        XDGFShape parent = GeometryTestUtils.mockShape(X - X0, Y - Y0);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        polyLine.addToPath(actualPath, parent);

        Path2D expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.lineTo(X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1, 1)",                 // Does not start with POLYLINE(
            "POLYLINE(1, 1",         // Does not end with )
            "POLYLINE()",            // Empty arguments
            "POLYLINE(1)",           // Not enough arguments (less than two)
            "POLYLINE(1, 1, 100.0)", // Odd number of arguments
    })
    public void shouldThrowExceptionWhenPolyLineFormulaIsIncorrect(String formula) {
        PolyLineTo polyLine = createPolyLine(formula);

        Path2D.Double path = new Path2D.Double();
        Assertions.assertThrows(POIXMLException.class, () -> polyLine.addToPath(path, null));
    }

    private static PolyLineTo createPolyLine(String formula) {
        RowType row = GeometryTestUtils.createRow(
                0L,
                new HashMap<String, Object>() {{
                    put("X", X);
                    put("Y", Y);
                    put("A", formula);
                }}
        );
        return new PolyLineTo(row);
    }

}
