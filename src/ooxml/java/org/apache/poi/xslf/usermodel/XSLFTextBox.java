/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;


/**
 * @author Yegor Kozlov
 */
@Beta
public class XSLFTextBox extends XSLFAutoShape {

    /*package*/ XSLFTextBox(CTShape shape, XSLFSheet sheet){
        super(shape, sheet);
    }

    /**
     *
     * @param shapeId   1-based shapeId
     */
    static CTShape prototype(int shapeId){
        CTShape ct = CTShape.Factory.newInstance();
        CTShapeNonVisual nvSpPr = ct.addNewNvSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("TextBox " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvSpPr().setTxBox(true);
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.RECT);
        prst.addNewAvLst();
        CTTextBody txBody = ct.addNewTxBody();
        txBody.addNewBodyPr();
        txBody.addNewLstStyle();

        return ct;
    }

    /**
     * Specifies that the corresponding shape should be represented by the generating application
     * as a placeholder. When a shape is considered a placeholder by the generating application
     * it can have special properties to alert the user that they may enter content into the shape.
     * Different types of placeholders are allowed and can be specified by using the placeholder
     * type attribute for this element
     *
     * @param placeholder
     */
    public void setPlaceholder(Placeholder placeholder){
        CTShape sh =  (CTShape)getXmlObject();
        CTApplicationNonVisualDrawingProps nv = sh.getNvSpPr().getNvPr();
        if(placeholder == null) {
            if(nv.isSetPh()) nv.unsetPh();
        } else {
            nv.addNewPh().setType(STPlaceholderType.Enum.forInt(placeholder.ordinal() + 1));
        }
    }

    public Placeholder getPlaceholder(){
        CTShape sh =  (CTShape)getXmlObject();
        CTPlaceholder ph = sh.getNvSpPr().getNvPr().getPh();
        if(ph == null) return null;
        else {
            int val = ph.getType().intValue();
            return Placeholder.values()[val - 1];
        }
    }
}