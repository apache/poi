/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hslf.model;

import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherContainerRecord;

/**
 * Create a <code>Shape</code> object depending on its type
 *
 * @author Yegor Kozlov
 */
public class ShapeFactory {

    public static Shape createShape(EscherContainerRecord spContainer, Shape parent){
        if (spContainer.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
            return new ShapeGroup(spContainer, parent);
        }

        Shape shape;
        EscherSpRecord spRecord = spContainer.getChildById(EscherSpRecord.RECORD_ID);

        int type = spRecord.getOptions() >> 4;
        switch (type){
            case ShapeTypes.TextBox:
            case ShapeTypes.Rectangle:
                shape = new Shape(spContainer, parent);
                break;
            case ShapeTypes.PictureFrame:
                shape = new Shape(spContainer, parent);
                break;
            case ShapeTypes.Line:
                shape = new Line(spContainer, parent);
                break;
            case ShapeTypes.Ellipse:
                shape = new Ellipse(spContainer, parent);
                break;
            case ShapeTypes.NotPrimitive:
                shape = new ShapeGroup(spContainer, parent);
                break;
            default:
                shape = new Shape(spContainer, parent);
                break;
        }
        return shape;
    }

}
