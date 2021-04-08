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

package org.apache.poi.ss.usermodel;

/**
 * Common interface for all drawing shapes
 * 
 * @since POI 3.16-beta2
 */
public interface Shape {
    /**
     * @return the name of this shape
     */
    String getShapeName();
    
    /**
     * @return the parent shape.
     */
    Shape getParent();

    /**
     * @return  the anchor that is used by this shape.
     */
    ChildAnchor getAnchor();

    /**
     * Whether this shape is not filled with a color
     *
     * @return true if this shape is not filled with a color.
     */
    boolean isNoFill();

    /**
     * Sets whether this shape is filled or transparent.
     *
     * @param noFill if true then no fill will be applied to the shape element.
     */
    void setNoFill(boolean noFill);

    /**
     * Sets the color used to fill this shape using the solid fill pattern.
     */
    void setFillColor(int red, int green, int blue);

    /**
     * The color applied to the lines of this shape.
     */
    void setLineStyleColor(int red, int green, int blue);
}
