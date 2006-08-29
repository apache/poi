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

/**
 * Represents a autoshape in a PowerPoint drawing
 *
 *  @author Yegor Kozlov
 */
public class AutoShape extends SimpleShape {

    protected AutoShape(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    public AutoShape(int type, Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(type, parent instanceof ShapeGroup);
    }

    public AutoShape(int type){
        this(type, null);
    }

    protected EscherContainerRecord createSpContainer(int shapeType, boolean isChild){
        EscherContainerRecord spcont = super.createSpContainer(isChild);

        EscherSpRecord spRecord = spcont.getChildById(EscherSpRecord.RECORD_ID);
        short type = (short)((shapeType << 4) | 0x2);
        spRecord.setOptions(type);

        //set default properties for a line
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(spcont, EscherOptRecord.RECORD_ID);

        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.FILL__FILLCOLOR, 134217732));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.FILL__FILLBACKCOLOR, 134217728));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.FILL__NOFILLHITTEST, 1048592));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__COLOR, 134217729));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 524296));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.SHADOWSTYLE__COLOR, 134217730));

        opt.sortProperties();

        return spcont;
    }

}
