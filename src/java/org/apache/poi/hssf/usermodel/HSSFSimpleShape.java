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

/**
 * Represents a simple shape such as a line, rectangle or oval.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFSimpleShape
    extends HSSFShape
{
    // The commented out ones haven't been tested yet or aren't supported
    // by HSSFSimpleShape.

    public final static short       OBJECT_TYPE_LINE               = 1;
    public final static short       OBJECT_TYPE_RECTANGLE          = 2;
    public final static short       OBJECT_TYPE_OVAL               = 3;
//    public final static short       OBJECT_TYPE_ARC                = 4;
//    public final static short       OBJECT_TYPE_CHART              = 5;
//    public final static short       OBJECT_TYPE_TEXT               = 6;
//    public final static short       OBJECT_TYPE_BUTTON             = 7;
    public final static short       OBJECT_TYPE_PICTURE            = 8;
//    public final static short       OBJECT_TYPE_POLYGON            = 9;
//    public final static short       OBJECT_TYPE_CHECKBOX           = 11;
//    public final static short       OBJECT_TYPE_OPTION_BUTTON      = 12;
//    public final static short       OBJECT_TYPE_EDIT_BOX           = 13;
//    public final static short       OBJECT_TYPE_LABEL              = 14;
//    public final static short       OBJECT_TYPE_DIALOG_BOX         = 15;
//    public final static short       OBJECT_TYPE_SPINNER            = 16;
//    public final static short       OBJECT_TYPE_SCROLL_BAR         = 17;
//    public final static short       OBJECT_TYPE_LIST_BOX           = 18;
//    public final static short       OBJECT_TYPE_GROUP_BOX          = 19;
    public final static short       OBJECT_TYPE_COMBO_BOX          = 20;
    public final static short       OBJECT_TYPE_COMMENT            = 25;
//    public final static short       OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 30;

    int shapeType = OBJECT_TYPE_LINE;

    HSSFSimpleShape( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
    }

    /**
     * Gets the shape type.
     * @return  One of the OBJECT_TYPE_* constants.
     *
     * @see #OBJECT_TYPE_LINE
     * @see #OBJECT_TYPE_OVAL
     * @see #OBJECT_TYPE_RECTANGLE
     * @see #OBJECT_TYPE_PICTURE
     * @see #OBJECT_TYPE_COMMENT
     */
    public int getShapeType() { return shapeType; }

    /**
     * Sets the shape types.
     *
     * @param shapeType One of the OBJECT_TYPE_* constants.
     *
     * @see #OBJECT_TYPE_LINE
     * @see #OBJECT_TYPE_OVAL
     * @see #OBJECT_TYPE_RECTANGLE
     * @see #OBJECT_TYPE_PICTURE
     * @see #OBJECT_TYPE_COMMENT
     */
    public void setShapeType( int shapeType ){ this.shapeType = shapeType; }

}
