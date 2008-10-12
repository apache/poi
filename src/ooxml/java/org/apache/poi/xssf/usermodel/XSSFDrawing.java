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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.STEditAs;
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
public class XSSFDrawing extends POIXMLDocumentPart {
    /**
     * Root element of the SpreadsheetML Drawing part
     */
    private CTDrawing drawing;

    /**
     * Create a new SpreadsheetML drawing
     *
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#createDrawingPatriarch()
     */
    public XSSFDrawing() {
        super(null, null);
        drawing = newDrawing();
    }

    /**
     * Construct a SpreadsheetML drawing from a package part
     *
     * @param part the package part holding the drawing data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawing+xml</code>
     * @param rel  the package relationship holding this drawing,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing
     */
    public XSSFDrawing(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
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
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTDrawing.type.getName().getNamespaceURI(), "wsDr", "xdr"));
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
        XSSFWorkbook wb = (XSSFWorkbook)getParent().getParent();
        XSSFPictureData data = wb.getAllPictures().get(pictureIndex);
        PackagePartName ppName = data.getPackagePart().getPartName();
        PackageRelationship rel = packagePart.addRelationship(ppName, TargetMode.INTERNAL, XSSFRelation.IMAGES.getRelation());
        addRelation(new XSSFPictureData(data.getPackagePart(), rel));
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        return new XSSFPicture(this, rel, ctAnchor);
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
        return new XSSFSimpleShape(this, ctAnchor);
    }

    /**
     * Create and initialize a CTTwoCellAnchor that anchors a shape against top-left and bottom-right cells.
     *
     * @return a new CTTwoCellAnchor
     */
    private CTTwoCellAnchor createTwoCellAnchor(XSSFClientAnchor anchor){
        CTTwoCellAnchor ctAnchor = drawing.addNewTwoCellAnchor();
        ctAnchor.setEditAs(STEditAs.ONE_CELL);
        ctAnchor.setFrom(anchor.getFrom());
        ctAnchor.setTo(anchor.getTo());
        ctAnchor.addNewClientData();
        return ctAnchor;
    }
}
