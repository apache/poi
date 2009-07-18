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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * A shape group may contain other shapes.  It was no actual form on the
 * sheet.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFShapeGroup
        extends HSSFShape
        implements HSSFShapeContainer
{
    List<HSSFShape> shapes = new ArrayList<HSSFShape>();
    int x1 = 0;
    int y1  = 0 ;
    int x2 = 1023;
    int y2 = 255;


    public HSSFShapeGroup( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
    }

    /**
     * Create another group under this group.
     * @param anchor    the position of the new group.
     * @return  the group
     */
    public HSSFShapeGroup createGroup(HSSFChildAnchor anchor)
    {
        HSSFShapeGroup group = new HSSFShapeGroup(this, anchor);
        group.anchor = anchor;
        shapes.add(group);
        return group;
    }

    /**
     * Create a new simple shape under this group.
     * @param anchor    the position of the shape.
     * @return  the shape
     */
    public HSSFSimpleShape createShape(HSSFChildAnchor anchor)
    {
        HSSFSimpleShape shape = new HSSFSimpleShape(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Create a new textbox under this group.
     * @param anchor    the position of the shape.
     * @return  the textbox
     */
    public HSSFTextbox createTextbox(HSSFChildAnchor anchor)
    {
        HSSFTextbox shape = new HSSFTextbox(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Creates a polygon
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public HSSFPolygon createPolygon(HSSFChildAnchor anchor)
    {
        HSSFPolygon shape = new HSSFPolygon(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Creates a picture.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public HSSFPicture createPicture(HSSFChildAnchor anchor, int pictureIndex)
    {
      HSSFPicture shape = new HSSFPicture(this, anchor);
      shape.anchor = anchor;
      shape.setPictureIndex( pictureIndex );
      shapes.add(shape);
      return shape;
    }
    /**
     * Return all children contained by this shape.
     */
    public List<HSSFShape> getChildren()
    {
        return shapes;
    }

    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     */
    public void setCoordinates( int x1, int y1, int x2, int y2 )
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * The top left x coordinate of this group.
     */
    public int getX1()
    {
        return x1;
    }

    /**
     * The top left y coordinate of this group.
     */
    public int getY1()
    {
        return y1;
    }

    /**
     * The bottom right x coordinate of this group.
     */
    public int getX2()
    {
        return x2;
    }

    /**
     * The bottom right y coordinate of this group.
     */
    public int getY2()
    {
        return y2;
    }

    /**
     * Count of all children and their childrens children.
     */
    public int countOfAllChildren()
    {
        int count = shapes.size();
        for ( Iterator iterator = shapes.iterator(); iterator.hasNext(); )
        {
            HSSFShape shape = (HSSFShape) iterator.next();
            count += shape.countOfAllChildren();
        }
        return count;
    }
}