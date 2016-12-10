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

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.ss.usermodel.ShapeTypes;

/**
 * Represents an AutoShape.
 * <p>
 * AutoShapes are drawing objects with a particular shape that may be customized through smart resizing and adjustments.
 * See {@link ShapeTypes}
 * </p>
 *
 *  @author Yegor Kozlov
 */
public class HSLFAutoShape extends HSLFTextShape implements AutoShape<HSLFShape,HSLFTextParagraph> {

    protected HSLFAutoShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    public HSLFAutoShape(ShapeType type, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(null, parent);
        createSpContainer(type, parent instanceof HSLFGroupShape);
    }

    public HSLFAutoShape(ShapeType type){
        this(type, null);
    }

    protected EscherContainerRecord createSpContainer(ShapeType shapeType, boolean isChild){
        EscherContainerRecord ecr = super.createSpContainer(isChild);

        setShapeType(shapeType);

        //set default properties for an autoshape
        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x40000);
        setEscherProperty(EscherProperties.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(EscherProperties.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(EscherProperties.FILL__FILLBACKCOLOR, 0x8000000);
        setEscherProperty(EscherProperties.FILL__NOFILLHITTEST, 0x100010);
        setEscherProperty(EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80008);
        setEscherProperty(EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);

        return ecr;
    }

    @Override
    protected void setDefaultTextProperties(HSLFTextParagraph _txtrun){
        setVerticalAlignment(VerticalAlignment.MIDDLE);
        setHorizontalCentered(true);
        setWordWrap(false);
    }

    /**
     * Gets adjust value which controls smart resizing of the auto-shape.
     *
     * <p>
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     * </p>
     *
     * @param idx the adjust index in the [0, 9] range
     * @return the adjustment value
     */
    public int getAdjustmentValue(int idx){
        if(idx < 0 || idx > 9) throw new IllegalArgumentException("The index of an adjustment value must be in the [0, 9] range");

        return getEscherProperty((short)(EscherProperties.GEOMETRY__ADJUSTVALUE + idx));
    }

    /**
     * Sets adjust value which controls smart resizing of the auto-shape.
     *
     * <p>
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     * </p>
     *
     * @param idx the adjust index in the [0, 9] range
     * @param val the adjustment value
     */
    public void setAdjustmentValue(int idx, int val){
        if(idx < 0 || idx > 9) throw new IllegalArgumentException("The index of an adjustment value must be in the [0, 9] range");

        setEscherProperty((short)(EscherProperties.GEOMETRY__ADJUSTVALUE + idx), val);
    }
}
