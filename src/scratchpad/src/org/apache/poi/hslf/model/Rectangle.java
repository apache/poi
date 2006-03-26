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

import org.apache.poi.ddf.*;

import java.awt.*;

/**
 * Represents a line in a PowerPoint drawing
 *
 *  @author Yegor Kozlov
 */
public class Rectangle extends SimpleShape {

    protected Rectangle(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    public Rectangle(Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(parent instanceof ShapeGroup);
    }

    public Rectangle(){
        this(null);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild){
        EscherContainerRecord spcont = super.createSpContainer(isChild);
        spcont.setOptions((short)15);

        EscherSpRecord spRecord = spcont.getChildById(EscherSpRecord.RECORD_ID);
        short type = (ShapeTypes.Rectangle << 4) + 2;
        spRecord.setOptions(type);

        //set default properties for a rectangle
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(spcont, EscherOptRecord.RECORD_ID);

        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 4));
        opt.sortProperties();

        return spcont;
    }

}
