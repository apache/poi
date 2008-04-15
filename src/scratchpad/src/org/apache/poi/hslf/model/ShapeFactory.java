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
package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;

/**
 * Create a <code>Shape</code> object depending on its type
 *
 * @author Yegor Kozlov
 */
public class ShapeFactory {

    /**
     * Create a new shape from the data provided.  
     */
    public static Shape createShape(EscherContainerRecord spContainer, Shape parent){
        if (spContainer.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
            return new ShapeGroup(spContainer, parent);
        }

        Shape shape;
        EscherSpRecord spRecord = spContainer.getChildById(EscherSpRecord.RECORD_ID);

        int type = spRecord.getOptions() >> 4;
        switch (type){
            case ShapeTypes.TextBox:
                shape = new TextBox(spContainer, parent);
                break;
            case ShapeTypes.PictureFrame:
                shape = new Picture(spContainer, parent);
                break;
            case ShapeTypes.Line:
                shape = new Line(spContainer, parent);
                break;
            case ShapeTypes.NotPrimitive:
                if ((spRecord.getFlags() & EscherSpRecord.FLAG_GROUP) != 0)
                    //TODO: check if the shape group is a Table 
                    shape = new ShapeGroup(spContainer, parent);
                else {
                    //TODO: check if the shape has GEOMETRY__VERTICES or GEOMETRY__SEGMENTINFO properties.
                    //if it does, then return Freeform or Polygon
                    shape = new AutoShape(spContainer, parent);
                }
                break;
            default:
                shape = new AutoShape(spContainer, parent);
                break;
        }
        return shape;
    }

}
