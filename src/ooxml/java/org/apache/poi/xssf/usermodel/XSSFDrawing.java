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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.poi.openxml4j.opc.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.*;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a SpreadsheetML drawing
 *
 * @author Yegor Kozlov
 */
public final class XSSFDrawing extends POIXMLDocumentPart implements Drawing {
    /**
     * Root element of the SpreadsheetML Drawing part
     */
    private CTDrawing drawing;
    private boolean isNew;

    /**
     * Create a new SpreadsheetML drawing
     *
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#createDrawingPatriarch()
     */
    protected XSSFDrawing() {
        super();
        drawing = newDrawing();
        isNew = true;
    }

    /**
     * Construct a SpreadsheetML drawing from a package part
     *
     * @param part the package part holding the drawing data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawing+xml</code>
     * @param rel  the package relationship holding this drawing,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing
     */
    protected XSSFDrawing(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        drawing = CTDrawing.Factory.parse(part.getInputStream());
    }

    /**
     * Construct a new CTDrawing bean. By default, it's just an empty placeholder for drawing objects
     *
     * @return a new CTDrawing bean
     */
    private static CTDrawing newDrawing(){
        return CTDrawing.Factory.newInstance();
    }

    /**
     * Return the underlying CTDrawing bean, the root element of the SpreadsheetML Drawing part.
     *
     * @return the underlying CTDrawing bean
     */
    public CTDrawing getCTDrawing(){
        return drawing;
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        /*
            Saved drawings must have the following namespaces set:
            <xdr:wsDr
                xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing">
        */
        if(isNew) xmlOptions.setSaveSyntheticDocumentElement(new QName(CTDrawing.type.getName().getNamespaceURI(), "wsDr", "xdr"));
        Map map = new HashMap();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        drawing.save(out, xmlOptions);
        out.close();
    }

    /**
     * Constructs a textbox under the drawing.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return      the newly created textbox.
     */
    public XSSFTextBox createTextbox(XSSFClientAnchor anchor){
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTShape ctShape = ctAnchor.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());
        XSSFTextBox shape = new XSSFTextBox(this, ctShape);
        shape.anchor = anchor;
        return shape;

    }

    /**
     * Creates a picture.
     *
     * @param anchor    the client anchor describes how this picture is attached to the sheet.
     * @param pictureIndex the index of the picture in the workbook collection of pictures,
     *   {@link org.apache.poi.xssf.usermodel.XSSFWorkbook#getAllPictures()} .
     *
     * @return  the newly created picture shape.
     */
    public XSSFPicture createPicture(XSSFClientAnchor anchor, int pictureIndex)
    {
        PackageRelationship rel = addPictureReference(pictureIndex);

        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTPicture ctShape = ctAnchor.addNewPic();
        ctShape.set(XSSFPicture.prototype());

        XSSFPicture shape = new XSSFPicture(this, ctShape);
        shape.anchor = anchor;
        shape.setPictureReference(rel);
        return shape;
    }

    public XSSFPicture createPicture(ClientAnchor anchor, int pictureIndex){
        return createPicture((XSSFClientAnchor)anchor, pictureIndex);
    }

    /**
     * Add the indexed picture to this drawing relations
     *
     * @param pictureIndex the index of the picture in the workbook collection of pictures,
     *   {@link org.apache.poi.xssf.usermodel.XSSFWorkbook#getAllPictures()} .
     */
    protected PackageRelationship addPictureReference(int pictureIndex){
        XSSFWorkbook wb = (XSSFWorkbook)getParent().getParent();
        XSSFPictureData data = wb.getAllPictures().get(pictureIndex);
        PackagePartName ppName = data.getPackagePart().getPartName();
        PackageRelationship rel = getPackagePart().addRelationship(ppName, TargetMode.INTERNAL, XSSFRelation.IMAGES.getRelation());
        addRelation(new XSSFPictureData(data.getPackagePart(), rel));
        return rel;
    }

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public XSSFSimpleShape createSimpleShape(XSSFClientAnchor anchor)
    {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTShape ctShape = ctAnchor.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());
        XSSFSimpleShape shape = new XSSFSimpleShape(this, ctShape);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public XSSFConnector createConnector(XSSFClientAnchor anchor)
    {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTConnector ctShape = ctAnchor.addNewCxnSp();
        ctShape.set(XSSFConnector.prototype());

        XSSFConnector shape = new XSSFConnector(this, ctShape);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public XSSFShapeGroup createGroup(XSSFClientAnchor anchor)
    {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTGroupShape ctGroup = ctAnchor.addNewGrpSp();
        ctGroup.set(XSSFShapeGroup.prototype());

        XSSFShapeGroup shape = new XSSFShapeGroup(this, ctGroup);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Create and initialize a CTTwoCellAnchor that anchors a shape against top-left and bottom-right cells.
     *
     * @return a new CTTwoCellAnchor
     */
    private CTTwoCellAnchor createTwoCellAnchor(XSSFClientAnchor anchor) {
        CTTwoCellAnchor ctAnchor = drawing.addNewTwoCellAnchor();
        ctAnchor.setFrom(anchor.getFrom());
        ctAnchor.setTo(anchor.getTo());
        ctAnchor.addNewClientData();
        anchor.setTo(ctAnchor.getTo());
        anchor.setFrom(ctAnchor.getFrom());
        STEditAs.Enum aditAs;
        switch(anchor.getAnchorType()) {
            case ClientAnchor.DONT_MOVE_AND_RESIZE: aditAs = STEditAs.ABSOLUTE; break;
            case ClientAnchor.MOVE_AND_RESIZE: aditAs = STEditAs.TWO_CELL; break;
            case ClientAnchor.MOVE_DONT_RESIZE: aditAs = STEditAs.ONE_CELL; break;
            default: aditAs = STEditAs.ONE_CELL;
        }
        ctAnchor.setEditAs(aditAs);
        return ctAnchor;
    }
}
