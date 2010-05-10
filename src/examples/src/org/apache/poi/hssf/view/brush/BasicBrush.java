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
 * This is a basic brush that just draws the line with the given parameters.
 * This is a {@link BasicStroke} object that can be used as a {@link Brush}.
 *
 * @author Ken Arnold, Industrious Media LLC
 * @see BasicStroke
 */
public class BasicBrush extends BasicStroke implements Brush {
    /**
     * Creates a new basic brush with the given width. Invokes {@link
     * BasicStroke#BasicStroke(float)}
     *
     * @param width The brush width.
     *
     * @see BasicStroke#BasicStroke(float)
     */
    public BasicBrush(float width) {
        super(width);
    }

    /**
     * Creates a new basic brush with the given width, cap, and join.  Invokes
     * {@link BasicStroke#BasicStroke(float,int,int)}
     *
     * @param width The brush width.
     * @param cap   The capping style.
     * @param join  The join style.
     *
     * @see BasicStroke#BasicStroke(float, int, int)
     */
    public BasicBrush(float width, int cap, int join) {
        super(width, cap, join);
    }

    /**
     * Creates a new basic brush with the given parameters.  Invokes {@link
     * BasicStroke#BasicStroke(float,int,int,float,float[],float)} with a miter
     * limit of 11 (the normal default value).
     *
     * @param width   The brush width.
     * @param cap     The capping style.
     * @param join    The join style.
     * @param dashes  The dash intervals.
     * @param dashPos The intial dash position in the dash intervals.
     *
     * @see BasicStroke#BasicStroke(float, int, int, float, float[], float)
     */
    public BasicBrush(float width, int cap, int join, float[] dashes,
            int dashPos) {
        super(width, cap, join, 11.0f, dashes, dashPos);
    }
}