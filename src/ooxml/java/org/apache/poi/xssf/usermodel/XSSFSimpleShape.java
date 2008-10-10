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

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShapeNonVisual;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

/**
 * Represents an auto-shape in a SpreadsheetML drawing.
 *
 * @author Yegor Kozlov
 */
public class XSSFSimpleShape extends XSSFShape {

    private CTShape ctShape;

    /**
     * Construct a new XSSFSimpleShape object.
     *
     * @param parent the XSSFDrawing that owns this shape
     * @param anchor the two cell anchor placeholder for this shape,
     *               this object encloses the shape bean that holds all the shape properties
     */
    protected XSSFSimpleShape(XSSFDrawing parent, CTTwoCellAnchor anchor) {
        super(parent, anchor);
        ctShape = anchor.addNewSp();
        newShape(ctShape);
    }

    /**
     * Initialize default structure of a new auto-shape
     *
     * @param shape newly created shape to initialize
     */
    private static void newShape(CTShape shape) {
        CTShapeNonVisual nv = shape.addNewNvSpPr();
        CTNonVisualDrawingProps nvp = nv.addNewCNvPr();
        int shapeId = 1;
        nvp.setId(shapeId);
        nvp.setName("Shape " + shapeId);
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

        body.addNewLstStyle();
    }

    /**
     * Gets the shape type, one of the constants defined in {@link ShapeTypes}.
     *
     * @return the shape type
     * @see ShapeTypes
     */
    public int getShapeType() {
        return ctShape.getSpPr().getPrstGeom().getPrst().intValue();
    }

    /**
     * Sets the shape types.
     *
     * @param type the shape type, one of the constants defined in {@link ShapeTypes}.
     * @see ShapeTypes
     */
    public void setShapeType(int type) {
        ctShape.getSpPr().getPrstGeom().setPrst(STShapeType.Enum.forInt(type));
    }


    /**
     * Whether this shape is not filled with a color
     *
     * @return true if this shape is not filled with a color.
     */
    public boolean isNoFill() {
        return ctShape.getSpPr().isSetNoFill();
    }

    /**
     * Sets whether this shape is filled or transparent.
     *
     * @param noFill if true then no fill will be applied to the shape element.
     */
    public void setNoFill(boolean noFill) {
        CTShapeProperties props = ctShape.getSpPr();
        //unset solid and pattern fills if they are set
        if (props.isSetPattFill()) props.unsetPattFill();
        if (props.isSetSolidFill()) props.unsetSolidFill();

        props.setNoFill(CTNoFillProperties.Factory.newInstance());
    }

    /**
     * Sets the color used to fill this shape using the solid fill pattern.
     */
    public void setFillColor(int red, int green, int blue) {
        CTShapeProperties props = ctShape.getSpPr();
        CTSolidColorFillProperties fill = props.isSetSolidFill() ? props.getSolidFill() : props.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte)red, (byte)green, (byte)blue});
        fill.setSrgbClr(rgb);
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor( int red, int green, int blue ) {
        CTShapeProperties props = ctShape.getSpPr();
        CTLineProperties ln = props.isSetLn() ? props.getLn() : props.addNewLn();
        CTSolidColorFillProperties fill = ln.isSetSolidFill() ? ln.getSolidFill() : ln.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte)red, (byte)green, (byte)blue});
        fill.setSrgbClr(rgb);
    }

    /**
     * Specifies the width to be used for the underline stroke.
     *
     * @param lineWidth width in points
     */
    public void setLineWidth( double lineWidth ) {
        CTShapeProperties props = ctShape.getSpPr();
        CTLineProperties ln = props.isSetLn() ? props.getLn() : props.addNewLn();
        ln.setW((int)(lineWidth*EMU_PER_POINT));
    }

}
