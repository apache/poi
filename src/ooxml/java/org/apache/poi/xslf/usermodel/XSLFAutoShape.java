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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a preset geometric shape. 
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFAutoShape extends XSLFTextShape {

    /*package*/ XSLFAutoShape(CTShape shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    /*package*/
    static XSLFAutoShape create(CTShape shape, XSLFSheet sheet) {
        if (shape.getSpPr().isSetCustGeom()) {
            return new XSLFFreeformShape(shape, sheet);
        } else if (shape.getNvSpPr().getCNvSpPr().isSetTxBox()) {
            return new XSLFTextBox(shape, sheet);
        } else {
            return new XSLFAutoShape(shape, sheet);
        }
    }

    /**
     * @param shapeId 1-based shapeId
     */
    static CTShape prototype(int shapeId) {
        CTShape ct = CTShape.Factory.newInstance();
        CTShapeNonVisual nvSpPr = ct.addNewNvSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("AutoShape " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvSpPr();
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.RECT);
        prst.addNewAvLst();
        return ct;
    }

    /**
     * Specifies a solid color fill. The shape is filled entirely with the specified color.
     *
     * @param color the solid color fill.
     * The value of <code>null</code> unsets the solidFIll attribute from the underlying xml
     */
    public void setFillColor(Color color) {
        CTShapeProperties spPr = getSpPr();
        if (color == null) {
            if(spPr.isSetSolidFill()) spPr.unsetSolidFill();
        }
        else {
            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});

            fill.setSrgbClr(rgb);
        }
    }

    /**
     *
     * @return solid fill color of null if not set
     */
    public Color getFillColor(){
        CTShapeProperties spPr = getSpPr();
        if(!spPr.isSetSolidFill() ) return null;

        CTSolidColorFillProperties fill = spPr.getSolidFill();
        if(!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

    protected CTTextBody getTextBody(boolean create){
        CTShape shape = (CTShape) getXmlObject();
        CTTextBody txBody = shape.getTxBody();
        if (txBody == null && create) {
            txBody = shape.addNewTxBody();
            txBody.addNewBodyPr();
            txBody.addNewLstStyle();
        }
        return txBody;
    }

}