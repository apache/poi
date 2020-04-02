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

package org.apache.poi.xdgf.geom;

import com.graphbuilder.curve.BSpline;
import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.GroupIterator;
import com.graphbuilder.curve.NURBSpline;
import com.graphbuilder.curve.ShapeMultiPath;
import com.graphbuilder.curve.ValueVector;


public class SplineRenderer {

    public static ShapeMultiPath createNurbsSpline(ControlPath controlPoints,
            ValueVector knots, ValueVector weights, int degree) {

        double firstKnot = knots.get(0);
        final int count = knots.size();
        double lastKnot = knots.get(count - 1);

        // scale knots to [0, 1] based on first/last knots
        for (int i = 0; i < count; i++) {
            knots.set((knots.get(i) - firstKnot) / lastKnot, i);
        }

        // if we don't have enough knots, duplicate the last knot until we do
        final int  knotsToAdd = controlPoints.numPoints() + degree + 1;
        for (int i = count; i < knotsToAdd; i++) {
            knots.add(1);
        }

        GroupIterator gi = new GroupIterator("0:n-1", controlPoints.numPoints());

        NURBSpline spline = new NURBSpline(controlPoints, gi);

        spline.setDegree(degree);
        spline.setKnotVectorType(BSpline.NON_UNIFORM);
        spline.setKnotVector(knots);

        if (weights == null) {
            spline.setUseWeightVector(false);
        } else {
            spline.setWeightVector(weights);
        }

        // now that this is done, add it to the path
        ShapeMultiPath shape = new ShapeMultiPath();
        shape.setFlatness(0.01);

        spline.appendTo(shape);
        return shape;
    }

}
