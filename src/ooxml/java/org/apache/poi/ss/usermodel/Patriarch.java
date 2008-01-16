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

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFAnchor;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPolygon;
import org.apache.poi.hssf.usermodel.HSSFShapeGroup;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTextbox;

public interface Patriarch {

    /**
     * Creates a new group record stored under this patriarch.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created group.
     */
    HSSFShapeGroup createGroup(HSSFClientAnchor anchor);

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    HSSFSimpleShape createSimpleShape(HSSFClientAnchor anchor);

    /**
     * Creates a picture.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    HSSFPicture createPicture(HSSFClientAnchor anchor, int pictureIndex);

    /**
     * Creates a polygon
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    HSSFPolygon createPolygon(HSSFClientAnchor anchor);

    /**
     * Constructs a textbox under the patriarch.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return      the newly created textbox.
     */
    HSSFTextbox createTextbox(HSSFClientAnchor anchor);

    /**
     * Constructs a cell comment.
     *
     * @param anchor    the client anchor describes how this comment is attached
     *                  to the sheet.
     * @return      the newly created comment.
     */
    HSSFComment createComment(HSSFAnchor anchor);

    /**
     * Returns a list of all shapes contained by the patriarch.
     */
    List getChildren();

    /**
     * Total count of all children and their children's children.
     */
    int countOfAllChildren();

    /**
     * Sets the coordinate space of this group.  All children are contrained
     * to these coordinates.
     */
    void setCoordinates(int x1, int y1, int x2, int y2);

    /**
     * The top left x coordinate of this group.
     */
    int getX1();

    /**
     * The top left y coordinate of this group.
     */
    int getY1();

    /**
     * The bottom right x coordinate of this group.
     */
    int getX2();

    /**
     * The bottom right y coordinate of this group.
     */
    int getY2();

}