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

import org.apache.poi.ss.usermodel.ShapeContainer;

/**
 * An interface that indicates whether a class can contain children.
 */
public interface HSSFShapeContainer extends ShapeContainer<HSSFShape>
{
    /**
     * @return  Any children contained by this shape.
     */
    List<HSSFShape> getChildren();

    /**
     * add shape to the list of child records
     * @param shape
     */
    public void addShape(HSSFShape shape);

    /**
     * set coordinates of this group relative to the parent
     */
    void setCoordinates( int x1, int y1, int x2, int y2 );

    void clear();

    /**
     *@return The top left x coordinate of this group.
     */
    public int getX1();

    /**
     *@return The top left y coordinate of this group.
     */
    public int getY1();

    /**
     *@return The bottom right x coordinate of this group.
     */
    public int getX2();

    /**
     * @return The bottom right y coordinate of this group.
     */
    public int getY2();

    /**
     * remove first level shapes
     * @param shape to be removed
     * @return true if shape is removed else return false
     */
    public boolean removeShape(HSSFShape shape);
}
