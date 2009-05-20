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

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShapeNonVisual;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

/**
 * Represents a shape with a predefined geometry in a SpreadsheetML drawing.
 * Possible shape types are defined in {@link org.apache.poi.ss.usermodel.ShapeTypes}
 *
 * @author Yegor Kozlov
 */
public class XSSFSimpleShape extends XSSFShape { // TODO - instantiable superclass
    /**
     * A default instance of CTShape used for creating new shapes.
     */
    private static CTShape prototype = null;

    /**
     *  Xml bean that stores properties of this shape
     */
    private CTShape ctShape;

    protected XSSFSimpleShape(XSSFDrawing drawing, CTShape ctShape) {
        this.drawing = drawing;
        this.ctShape = ctShape;
    }

    /**
     * Prototype with the default structure of a new auto-shape.
     */
    protected static CTShape prototype() {
        if(prototype == null) {
            CTShape shape = CTShape.Factory.newInstance();

            CTShapeNonVisual nv = shape.addNewNvSpPr();
            CTNonVisualDrawingProps nvp = nv.addNewCNvPr();
            nvp.setId(1);
            nvp.setName("Shape 1");
            nv.addNewCNvSpPr();

            CTShapeProperties sp = shape.addNewSpPr();
            CTTransform2D t2d = sp.addNewXfrm();
            CTPositiveSize2D p1 = t2d.addNewExt();
            p1.setCx(0);
            p1.setCy(0);
            CTPoint2D p2 = t2d.addNewOff();
            p2.setX(0);
            p2.setY(0);

            CTPresetGeometry2D geom = sp.addNewPrstGeom();
            geom.setPrst(STShapeType.RECT);
            geom.addNewAvLst();

            CTShapeStyle style = shape.addNewStyle();
            CTSchemeColor scheme = style.addNewLnRef().addNewSchemeClr();
            scheme.setVal(STSchemeColorVal.ACCENT_1);
            scheme.addNewShade().setVal(50000);
            style.getLnRef().setIdx(2);

            CTStyleMatrixReference fillref = style.addNewFillRef();
            fillref.setIdx(1);
            fillref.addNewSchemeClr().setVal(STSchemeColorVal.ACCENT_1);

            CTStyleMatrixReference effectRef = style.addNewEffectRef();
            effectRef.setIdx(0);
            effectRef.addNewSchemeClr().setVal(STSchemeColorVal.ACCENT_1);

            CTFontReference fontRef = style.addNewFontRef();
            fontRef.setIdx(STFontCollectionIndex.MINOR);
            fontRef.addNewSchemeClr().setVal(STSchemeColorVal.LT_1);

            CTTextBody body = shape.addNewTxBody();
            CTTextBodyProperties bodypr = body.addNewBodyPr();
            bodypr.setAnchor(STTextAnchoringType.CTR);
            bodypr.setRtlCol(false);
            CTTextParagraph p = body.addNewP();
            p.addNewPPr().setAlgn(STTextAlignType.CTR);
            CTTextCharacterProperties endPr = p.addNewEndParaRPr();
            endPr.setLang("en-US");
            endPr.setSz(1100);

            body.addNewLstStyle();

            prototype = shape;
        }
        return prototype;
    }

    public CTShape getCTShape(){
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

    public void setText(XSSFRichTextString str){

        XSSFWorkbook wb = (XSSFWorkbook)getDrawing().getParent().getParent();
        str.setStylesTableReference(wb.getStylesSource());

        CTTextParagraph p = CTTextParagraph.Factory.newInstance();
        if(str.numFormattingRuns() == 0){
            CTRegularTextRun r = p.addNewR();
            CTTextCharacterProperties rPr = r.addNewRPr();
            rPr.setLang("en-US");
            rPr.setSz(1100);
            r.setT(str.getString());

        } else {
            for (int i = 0; i < str.getCTRst().sizeOfRArray(); i++) {
                CTRElt lt = str.getCTRst().getRArray(i);
                CTRPrElt ltPr = lt.getRPr();

                CTRegularTextRun r = p.addNewR();
                CTTextCharacterProperties rPr = r.addNewRPr();
                rPr.setLang("en-US");

                applyAttributes(ltPr, rPr);

                r.setT(lt.getT());
            }
        }
        ctShape.getTxBody().setPArray(new CTTextParagraph[]{p});

    }

    /**
     *
     * CTRPrElt --> CTFont adapter
     */
    private static void applyAttributes(CTRPrElt pr, CTTextCharacterProperties rPr){

        if(pr.sizeOfBArray() > 0) rPr.setB(pr.getBArray(0).getVal());
        //if(pr.sizeOfUArray() > 0) rPr.setU(pr.getUArray(0).getVal());
        if(pr.sizeOfIArray() > 0) rPr.setI(pr.getIArray(0).getVal());

        CTTextFont rFont = rPr.addNewLatin();
        rFont.setTypeface(pr.sizeOfRFontArray() > 0 ? pr.getRFontArray(0).getVal() : "Arial");
    }
}
