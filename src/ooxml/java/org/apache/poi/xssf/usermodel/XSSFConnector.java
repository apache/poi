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

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

/**
 * A connection shape drawing element. A connection shape is a line, etc.
 * that connects two other shapes in this drawing.
 *
 * @author Yegor Kozlov
 */
public final class XSSFConnector extends XSSFShape {

    private static CTConnector prototype = null;

    private CTConnector ctShape;

    /**
     * Construct a new XSSFConnector object.
     *
     * @param drawing the XSSFDrawing that owns this shape
     * @param ctShape the shape bean that holds all the shape properties
     */
    protected XSSFConnector(XSSFDrawing drawing, CTConnector ctShape) {
        this.drawing = drawing;
        this.ctShape = ctShape;
    }

    /**
     * Initialize default structure of a new auto-shape
     *
     */
    protected static CTConnector prototype() {
        if(prototype == null) {
            CTConnector shape = CTConnector.Factory.newInstance();
            CTConnectorNonVisual nv = shape.addNewNvCxnSpPr();
            CTNonVisualDrawingProps nvp = nv.addNewCNvPr();
            nvp.setId(1);
            nvp.setName("Shape 1");
            nv.addNewCNvCxnSpPr();

            CTShapeProperties sp = shape.addNewSpPr();
            CTTransform2D t2d = sp.addNewXfrm();
            CTPositiveSize2D p1 = t2d.addNewExt();
            p1.setCx(0);
            p1.setCy(0);
            CTPoint2D p2 = t2d.addNewOff();
            p2.setX(0);
            p2.setY(0);

            CTPresetGeometry2D geom = sp.addNewPrstGeom();
            geom.setPrst(STShapeType.LINE);
            geom.addNewAvLst();

            CTShapeStyle style = shape.addNewStyle();
            CTSchemeColor scheme = style.addNewLnRef().addNewSchemeClr();
            scheme.setVal(STSchemeColorVal.ACCENT_1);
            style.getLnRef().setIdx(1);

            CTStyleMatrixReference fillref = style.addNewFillRef();
            fillref.setIdx(0);
            fillref.addNewSchemeClr().setVal(STSchemeColorVal.ACCENT_1);

            CTStyleMatrixReference effectRef = style.addNewEffectRef();
            effectRef.setIdx(0);
            effectRef.addNewSchemeClr().setVal(STSchemeColorVal.ACCENT_1);

            CTFontReference fontRef = style.addNewFontRef();
            fontRef.setIdx(STFontCollectionIndex.MINOR);
            fontRef.addNewSchemeClr().setVal(STSchemeColorVal.TX_1);

            prototype = shape;
        }
        return prototype;
    }

    public CTConnector getCTConnector(){
        return ctShape;
    }

    /**
     * Gets the shape type, one of the constants defined in {@link org.apache.poi.ss.usermodel.ShapeTypes}.
     *
     * @return the shape type
     * @see org.apache.poi.ss.usermodel.ShapeTypes
     */
    public int getShapeType() {
        return ctShape.getSpPr().getPrstGeom().getPrst().intValue();
    }

    /**
     * Sets the shape types.
     *
     * @param type the shape type, one of the constants defined in {@link org.apache.poi.ss.usermodel.ShapeTypes}.
     * @see org.apache.poi.ss.usermodel.ShapeTypes
     */
    public void setShapeType(int type) {
        ctShape.getSpPr().getPrstGeom().setPrst(STShapeType.Enum.forInt(type));
    }

    protected CTShapeProperties getShapeProperties(){
        return ctShape.getSpPr();
    }

}
