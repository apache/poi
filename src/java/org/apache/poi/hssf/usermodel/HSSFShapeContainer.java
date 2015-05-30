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

package org.apache.poi.hssf.usermodel;

import java.util.List;

/**
 * An interface that indicates whether a class can contain children.
 */
public interface HSSFShapeContainer extends Iterable<HSSFShape> {

    /**
     * @return children contained by this shape
     */
    List<HSSFShape> getChildren();

    /**
     * Adds a shape to the list of child records.
     *
     * @param shape
     */
    void addShape(HSSFShape shape);

    /**
     * Set coordinates of this group relative to the parent.
     */
    void setCoordinates(int x1, int y1, int x2, int y2);

    void clear();

    /**
     * @return the top left x coordinate of this group
     */
    int getX1();

    /**
     * @return the top left y coordinate of this group
     */
    int getY1();

    /**
     * @return the bottom right x coordinate of this group
     */
    int getX2();

    /**
     * @return the bottom right y coordinate of this group
     */
    int getY2();

    /**
     * Removes first level shape.
     *
     * @param shape to be removed
     * @return true if shape is removed, false otherwise
     */
    boolean removeShape(HSSFShape shape);
}
