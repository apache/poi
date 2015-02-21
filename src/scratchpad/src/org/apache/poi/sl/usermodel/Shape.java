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

import org.apache.poi.sl.draw.geom.CustomGeometry;

public interface Shape extends PlaceableShape {
    CustomGeometry getGeometry();
    
	ShapeType getShapeType();

	void setAnchor(Rectangle2D anchor);

	ShapeContainer getParent();
	
	boolean isPlaceholder();
	
    /**
    *
    * @return the sheet this shape belongs to
    */
   Sheet getSheet();
	
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
