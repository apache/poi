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
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuideList;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a preset geometric shape. 
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFAutoShape extends XSLFTextShape {
    private static final Pattern adjPtrn = Pattern.compile("val\\s+(\\d+)");

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

    int getAdjustValue(String name, int defaultValue){
        /*
        CTShape shape = (CTShape) getXmlObject();
        CTGeomGuideList av = shape.getSpPr().getPrstGeom().getAvLst();
        if(av != null){
            for(CTGeomGuide gd : av.getGdList()){
                if(gd.getName().equals(name)) {
                    String fmla = gd.getFmla();
                    Matcher m = adjPtrn.matcher(fmla);
                    if(m.matches()){
                        int val = Integer.parseInt(m.group(1));
                        return 21600*val/100000;
                    }
                }
            }
        }
        */
        return defaultValue;
    }

    @Override
    protected java.awt.Shape getOutline(){
        java.awt.Shape outline = XSLFPresetGeometry.getOutline(this);
        Rectangle2D anchor = getAnchor();

        AffineTransform at = new AffineTransform();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(
                1.0f/21600*anchor.getWidth(),
                1.0f/21600*anchor.getHeight()
        );
        return outline == null ? anchor : at.createTransformedShape(outline);
    }

}
