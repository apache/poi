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

package org.apache.poi.hemf.record.emf;

import java.awt.geom.Rectangle2D;

import org.apache.poi.hemf.draw.HemfGraphics;

/**
 * In EMF, shape records bring their own bounding.
 * The record bounding is in the same space as the global drawing context,
 * but the specified shape points can have a different space and therefore
 * need to be translated/normalized
 */
public interface HemfBounded {
    /**
     * Getter for the outer bounds which are given in the record
     *
     * @return the bounds specified in the record
     */
    Rectangle2D getRecordBounds();

    /**
     * Getter for the inner bounds which are calculated by the shape points
     *
     * @param ctx the graphics context
     * @return the bounds of the shape points
     */
    Rectangle2D getShapeBounds(HemfGraphics ctx);
}
