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
import org.junit.jupiter.api.Test;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.HashMap;

public class TestEllipticalArcTo {

    private static final double X0 = 0.0;
    private static final double Y0 = 0.0;
    private static final double R = 100.0;
    private static final double X = R;
    private static final double Y = R;
    // Rotation angle does not affect the calculation
    private static final double C = 0.0;
    // Draw a circular arc, it does not matter for this test which type of arc we draw
    private static final double D = 1.0;

    @Test
    public void shouldAddArcToPathWhenControlPointIsNotColinearWithBeginAndEnd() {
        double a = R / 2.0;
        double b = R - Math.sqrt(R * R - a * a);
        EllipticalArcTo ellipticalArcTo = createEllipticalArcTo(a, b);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating an elliptical arc
        ellipticalArcTo.addToPath(actualPath, null);

        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        Arc2D arc = new Arc2D.Double(-R, Y0, R * 2, R * 2, 90, -90, Arc2D.OPEN);
        expectedPath.append(arc, false);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    @Test
    public void shouldAddLineToPathWhenControlPointIsColinearWithBeginAndEnd() {
        // We artificially set control point that is obviously colinear with begin and end.
        // However, when you draw a very small arc, it might happen that all three points are colinear to each other
        EllipticalArcTo ellipticalArcTo = createEllipticalArcTo(50.0, 50.0);

        Path2D.Double actualPath = new Path2D.Double();
        actualPath.moveTo(X0, Y0);

        // Shape isn't used while creating an elliptical arc
        ellipticalArcTo.addToPath(actualPath, null);

        Path2D.Double expectedPath = new Path2D.Double();
        expectedPath.moveTo(X0, Y0);
        expectedPath.lineTo(X, Y);

        GeometryTestUtils.assertPath(expectedPath, actualPath);
    }

    private static EllipticalArcTo createEllipticalArcTo(double a, double b) {
        RowType row = GeometryTestUtils.createRow(
                0L,
                new HashMap<String, Object>() {{
                    put("X", X);
                    put("Y", Y);
                    put("A", a);
                    put("B", b);
                    put("C", C);
                    put("D", D);
                }}
        );
        return new EllipticalArcTo(row);
    }

}
