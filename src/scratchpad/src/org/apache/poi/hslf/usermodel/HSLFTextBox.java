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
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.VerticalAlignment;

/**
 * Represents a TextFrame shape in PowerPoint.
 * <p>
 * Contains the text in a text frame as well as the properties and methods
 * that control alignment and anchoring of the text.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class HSLFTextBox extends HSLFTextShape implements TextBox<HSLFShape,HSLFTextParagraph> {

    /**
     * Create a TextBox object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected HSLFTextBox(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFTextBox(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(parent);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     */
    public HSLFTextBox(){
        this(null);
    }

    /**
     * Create a new TextBox and initialize its internal structures
     *
     * @return the created <code>EscherContainerRecord</code> which holds shape data
     */
    @Override
    protected EscherContainerRecord createSpContainer(boolean isChild){
        EscherContainerRecord ecr = super.createSpContainer(isChild);

        setShapeType(ShapeType.TEXT_BOX);

        //set default properties for a TextBox
        setEscherProperty(EscherPropertyTypes.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(EscherPropertyTypes.FILL__FILLBACKCOLOR, 0x8000000);
        setEscherProperty(EscherPropertyTypes.FILL__NOFILLHITTEST, 0x100000);
        setEscherProperty(EscherPropertyTypes.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH, 0x80000);
        setEscherProperty(EscherPropertyTypes.SHADOWSTYLE__COLOR, 0x8000002);

        // init paragraphs
        getTextParagraphs();

        return ecr;
    }

    @Override
    protected void setDefaultTextProperties(HSLFTextParagraph _txtrun){
        setVerticalAlignment(VerticalAlignment.TOP);
        setEscherProperty(EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x20002);
    }
}
