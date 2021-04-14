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

package org.apache.poi.hslf.usermodel;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.sl.usermodel.Line;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;

/**
 * Represents a line in a PowerPoint drawing
 */
public final class HSLFLine extends HSLFTextShape implements Line<HSLFShape,HSLFTextParagraph> {
    public HSLFLine(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    public HSLFLine(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(null, parent);
        createSpContainer(parent instanceof HSLFGroupShape);
    }

    public HSLFLine(){
        this(null);
    }

    @Override
    protected EscherContainerRecord createSpContainer(boolean isChild){
        EscherContainerRecord ecr = super.createSpContainer(isChild);

        setShapeType(ShapeType.LINE);

        EscherSpRecord spRecord = ecr.getChildById(EscherSpRecord.RECORD_ID);
        short type = (short)((ShapeType.LINE.nativeId << 4) | 0x2);
        spRecord.setOptions(type);

        //set default properties for a line
        AbstractEscherOptRecord opt = getEscherOptRecord();

        //default line properties
        setEscherProperty(opt, EscherPropertyTypes.GEOMETRY__SHAPEPATH, 4);
        setEscherProperty(opt, EscherPropertyTypes.GEOMETRY__FILLOK, 0x10000);
        setEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST, 0x100000);
        setEscherProperty(opt, EscherPropertyTypes.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(opt, EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH, 0xA0008);
        setEscherProperty(opt, EscherPropertyTypes.SHADOWSTYLE__COLOR, 0x8000002);

        return ecr;
    }

//    /**
//     * Sets the orientation of the line, if inverse is false, then line goes
//     * from top-left to bottom-right, otherwise use inverse equals true
//     *
//     * @param inverse the orientation of the line
//     */
//    public void setInverse(boolean inverse) {
//        setShapeType(inverse ? ShapeType.LINE_INV : ShapeType.LINE);
//    }
//
//    /**
//     * Gets the orientation of the line, if inverse is false, then line goes
//     * from top-left to bottom-right, otherwise inverse equals true
//     *
//     * @return inverse the orientation of the line
//     */
//    public boolean isInverse() {
//        return (getShapeType() == ShapeType.LINE_INV);
//    }
}
