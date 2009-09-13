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
import org.apache.poi.openxml4j.opc.PackageRelationship;

/**
 * This object specifies a group shape that represents many shapes grouped together. This shape is to be treated
 * just as if it were a regular shape but instead of being described by a single geometry it is made up of all the
 * shape geometries encompassed within it. Within a group shape each of the shapes that make up the group are
 * specified just as they normally would.
 *
 * @author Yegor Kozlov
 */
public final class XSSFShapeGroup extends XSSFShape {
    private static CTGroupShape prototype = null;

    private CTGroupShape ctGroup;

    /**
     * Construct a new XSSFSimpleShape object.
     *
     * @param drawing the XSSFDrawing that owns this shape
     * @param ctGroup the XML bean that stores this group content
     */
    protected XSSFShapeGroup(XSSFDrawing drawing, CTGroupShape ctGroup) {
        this.drawing = drawing;
        this.ctGroup = ctGroup;
    }

    /**
     * Initialize default structure of a new shape group
     */
    protected static CTGroupShape prototype() {
        if (prototype == null) {
            CTGroupShape shape = CTGroupShape.Factory.newInstance();

            CTGroupShapeNonVisual nv = shape.addNewNvGrpSpPr();
            CTNonVisualDrawingProps nvpr = nv.addNewCNvPr();
            nvpr.setId(0);
            nvpr.setName("Group 0");
            nv.addNewCNvGrpSpPr();
            CTGroupShapeProperties sp = shape.addNewGrpSpPr();
            CTGroupTransform2D t2d = sp.addNewXfrm();
            CTPositiveSize2D p1 = t2d.addNewExt();
            p1.setCx(0);
            p1.setCy(0);
            CTPoint2D p2 = t2d.addNewOff();
            p2.setX(0);
            p2.setY(0);
            CTPositiveSize2D p3 = t2d.addNewChExt();
            p3.setCx(0);
            p3.setCy(0);
            CTPoint2D p4 = t2d.addNewChOff();
            p4.setX(0);
            p4.setY(0);

            prototype = shape;
        }
        return prototype;
    }

    /**
     * Constructs a textbox.
     *
     * @param anchor the child anchor describes how this shape is attached
     *               to the group.
     * @return      the newly created textbox.
     */
    public XSSFTextBox createTextbox(XSSFChildAnchor anchor){
        CTShape ctShape = ctGroup.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());

        XSSFTextBox shape = new XSSFTextBox(getDrawing(), ctShape);
        shape.parent = this;
        shape.anchor = anchor;
        shape.getCTShape().getSpPr().setXfrm(anchor.getCTTransform2D());
        return shape;

    }
    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor the child anchor describes how this shape is attached
     *               to the group.
     * @return the newly created shape.
     */
    public XSSFSimpleShape createSimpleShape(XSSFChildAnchor anchor) {
        CTShape ctShape = ctGroup.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());

        XSSFSimpleShape shape = new XSSFSimpleShape(getDrawing(), ctShape);
        shape.parent = this;
        shape.anchor = anchor;
        shape.getCTShape().getSpPr().setXfrm(anchor.getCTTransform2D());
        return shape;
    }

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor the child anchor describes how this shape is attached
     *               to the group.
     * @return the newly created shape.
     */
    public XSSFConnector createConnector(XSSFChildAnchor anchor) {
        CTConnector ctShape = ctGroup.addNewCxnSp();
        ctShape.set(XSSFConnector.prototype());

        XSSFConnector shape = new XSSFConnector(getDrawing(), ctShape);
        shape.parent = this;
        shape.anchor = anchor;
        shape.getCTConnector().getSpPr().setXfrm(anchor.getCTTransform2D());
        return shape;
    }

    /**
     * Creates a picture.
     *
     * @param anchor       the client anchor describes how this picture is attached to the sheet.
     * @param pictureIndex the index of the picture in the workbook collection of pictures,
     *                     {@link XSSFWorkbook#getAllPictures()} .
     * @return the newly created picture shape.
     */
    public XSSFPicture createPicture(XSSFClientAnchor anchor, int pictureIndex) {
        PackageRelationship rel = getDrawing().addPictureReference(pictureIndex);

        CTPicture ctShape = ctGroup.addNewPic();
        ctShape.set(XSSFPicture.prototype());

        XSSFPicture shape = new XSSFPicture(getDrawing(), ctShape);
        shape.parent = this;
        shape.anchor = anchor;
        shape.setPictureReference(rel);
        return shape;
    }

    public CTGroupShape getCTGroupShape() {
        return ctGroup;
    }

    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     */
    public void setCoordinates(int x1, int y1, int x2, int y2) {
        CTGroupTransform2D t2d = ctGroup.getGrpSpPr().getXfrm();
        CTPoint2D off = t2d.getOff();
        off.setX(x1);
        off.setY(y1);
        CTPositiveSize2D ext = t2d.getExt();
        ext.setCx(x2);
        ext.setCy(y2);

        CTPoint2D chOff = t2d.getChOff();
        chOff.setX(x1);
        chOff.setY(y1);
        CTPositiveSize2D chExt = t2d.getChExt();
        chExt.setCx(x2);
        chExt.setCy(y2);
    }

    protected CTShapeProperties getShapeProperties() {
        throw new IllegalStateException("Not supported for shape group");
    }

}
