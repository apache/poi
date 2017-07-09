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

package org.apache.poi.sl.usermodel;

import java.awt.geom.Rectangle2D;

public interface PlaceableShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> {
    ShapeContainer<S,P> getParent();
    
    /**
     * @return the sheet this shape belongs to
     */
    Sheet<S,P> getSheet();
    
    /**
     * @return the position of this shape within the drawing canvas.
     *         The coordinates are expressed in points
     */
    Rectangle2D getAnchor();

    /**
     * @param anchor the position of this shape within the drawing canvas.
     *               The coordinates are expressed in points
     */
    void setAnchor(Rectangle2D anchor);

    /**
     * Rotation angle in degrees
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @return rotation angle in degrees
     */
    double getRotation();

    /**
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    void setRotation(double theta);

    /**
     * @param flip whether the shape is horizontally flipped
     */
    void setFlipHorizontal(boolean flip);

    /**
     * Whether the shape is vertically flipped
     *
     * @param flip whether the shape is vertically flipped
     */
    void setFlipVertical(boolean flip);

    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    boolean getFlipHorizontal();

    /**
     * Whether the shape is vertically flipped
     *
     * @return whether the shape is vertically flipped
     */
    boolean getFlipVertical();
}
