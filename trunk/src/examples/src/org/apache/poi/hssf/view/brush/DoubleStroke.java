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
package org.apache.poi.hssf.view.brush;

import java.awt.*;

/**
 * This Stroke implementation applies a BasicStroke to a shape twice. If you
 * draw with this Stroke, then instead of outlining the shape, you're outlining
 * the outline of the shape.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class DoubleStroke implements Brush {
    BasicStroke stroke1, stroke2; // the two strokes to use

    /**
     * Creates a new double-stroke brush.  This surrounds a cell with a two
     * lines separated by white space between.
     *
     * @param width1 The width of the blank space in the middle
     * @param width2 The width of the each of the two drawn strokes.
     */
    public DoubleStroke(float width1, float width2) {
        stroke1 = new BasicStroke(width1); // Constructor arguments specify
        stroke2 = new BasicStroke(width2); // the line widths for the strokes
    }

    /**
     * Stroke the outline.
     *
     * @param s The shape in which to stroke.
     *
     * @return The created stroke as a new shape.
     */
    public Shape createStrokedShape(Shape s) {
        // Use the first stroke to create an outline of the shape
        Shape outline = stroke1.createStrokedShape(s);
        // Use the second stroke to create an outline of that outline.
        // It is this outline of the outline that will be filled in
        return stroke2.createStrokedShape(outline);
    }

    /** {@inheritDoc} */
    public float getLineWidth() {
        return stroke1.getLineWidth() + 2 * stroke2.getLineWidth();
    }
}