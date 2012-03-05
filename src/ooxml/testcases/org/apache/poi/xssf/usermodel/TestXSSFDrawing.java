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

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;

/**
 * @author Yegor Kozlov
 */
public class TestXSSFDrawing extends TestCase {
    public void testRead(){
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithDrawing.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        //the sheet has one relationship and it is XSSFDrawing
        List<POIXMLDocumentPart> rels = sheet.getRelations();
        assertEquals(1, rels.size());
        assertTrue(rels.get(0) instanceof XSSFDrawing);

        XSSFDrawing drawing = (XSSFDrawing)rels.get(0);
        //sheet.createDrawingPatriarch() should return the same instance of XSSFDrawing
        assertSame(drawing, sheet.createDrawingPatriarch());
        String drawingId = drawing.getPackageRelationship().getId();

        //there should be a relation to this drawing in the worksheet
        assertTrue(sheet.getCTWorksheet().isSetDrawing());
        assertEquals(drawingId, sheet.getCTWorksheet().getDrawing().getId());

        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(6, shapes.size());

        assertTrue(shapes.get(0) instanceof XSSFPicture);
        assertTrue(shapes.get(1) instanceof XSSFPicture);
        assertTrue(shapes.get(2) instanceof XSSFPicture);
        assertTrue(shapes.get(3) instanceof XSSFPicture);
        assertTrue(shapes.get(4) instanceof XSSFSimpleShape);
        assertTrue(shapes.get(5) instanceof XSSFPicture);

    }

    public void testNew() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        //multiple calls of createDrawingPatriarch should return the same instance of XSSFDrawing
        XSSFDrawing dr1 = sheet.createDrawingPatriarch();
        XSSFDrawing dr2 = sheet.createDrawingPatriarch();
        assertSame(dr1, dr2);

        List<POIXMLDocumentPart> rels = sheet.getRelations();
        assertEquals(1, rels.size());
        assertTrue(rels.get(0) instanceof XSSFDrawing);

        XSSFDrawing drawing = (XSSFDrawing)rels.get(0);
        String drawingId = drawing.getPackageRelationship().getId();

        //there should be a relation to this drawing in the worksheet
        assertTrue(sheet.getCTWorksheet().isSetDrawing());
        assertEquals(drawingId, sheet.getCTWorksheet().getDrawing().getId());

        XSSFConnector c1= drawing.createConnector(new XSSFClientAnchor(0,0,0,0,0,0,2,2));
        c1.setLineWidth(2.5);
        c1.setLineStyle(1);

        XSSFShapeGroup c2 = drawing.createGroup(new XSSFClientAnchor(0,0,0,0,0,0,5,5));

        XSSFSimpleShape c3 = drawing.createSimpleShape(new XSSFClientAnchor(0,0,0,0,2,2,3,4));
        c3.setText(new XSSFRichTextString("Test String"));
        c3.setFillColor(128, 128, 128);

        XSSFTextBox c4 = drawing.createTextbox(new XSSFClientAnchor(0,0,0,0,4,4,5,6));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");
        rt.applyFont(0, 5, wb.createFont());
        rt.applyFont(5, 6, wb.createFont());
        c4.setText(rt);

        c4.setNoFill(true);
        assertEquals(4, drawing.getCTDrawing().sizeOfTwoCellAnchorArray());

        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(4, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFConnector);
        assertTrue(shapes.get(1) instanceof XSSFShapeGroup);
        assertTrue(shapes.get(2) instanceof XSSFSimpleShape);
        assertTrue(shapes.get(3) instanceof XSSFSimpleShape); //

        // Save and re-load it
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        // Check
        dr1 = sheet.createDrawingPatriarch();
        CTDrawing ctDrawing = dr1.getCTDrawing();
        
        // Connector, shapes and text boxes are all two cell anchors
        assertEquals(0, ctDrawing.sizeOfAbsoluteAnchorArray());
        assertEquals(0, ctDrawing.sizeOfOneCellAnchorArray());
        assertEquals(4, ctDrawing.sizeOfTwoCellAnchorArray());

        shapes = dr1.getShapes();
        assertEquals(4, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFConnector);
        assertTrue(shapes.get(1) instanceof XSSFShapeGroup);
        assertTrue(shapes.get(2) instanceof XSSFSimpleShape);
        assertTrue(shapes.get(3) instanceof XSSFSimpleShape); //

        // Ensure it got the right namespaces
        String xml = ctDrawing.toString();
        assertTrue(xml.contains("xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\""));
        assertTrue(xml.contains("xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""));
    }
    
    public void testMultipleDrawings(){
        XSSFWorkbook wb = new XSSFWorkbook();
        for (int i = 0; i < 3; i++) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
        }
        OPCPackage pkg = wb.getPackage();
        assertEquals(3, pkg.getPartsByContentType(XSSFRelation.DRAWINGS.getContentType()).size());
    }

    public void testClone() throws Exception{
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithDrawing.xlsx");
        XSSFSheet sheet1 = wb.getSheetAt(0);

        XSSFSheet sheet2 = wb.cloneSheet(0);
        
        //the source sheet has one relationship and it is XSSFDrawing
        List<POIXMLDocumentPart> rels1 = sheet1.getRelations();
        assertEquals(1, rels1.size());
        assertTrue(rels1.get(0) instanceof XSSFDrawing);

        List<POIXMLDocumentPart> rels2 = sheet2.getRelations();
        assertEquals(1, rels2.size());
        assertTrue(rels2.get(0) instanceof XSSFDrawing);

        XSSFDrawing drawing1 = (XSSFDrawing)rels1.get(0);
        XSSFDrawing drawing2 = (XSSFDrawing)rels2.get(0);
        assertNotSame(drawing1, drawing2);  // drawing2 is a clone of drawing1

        List<XSSFShape> shapes1 = drawing1.getShapes();
        List<XSSFShape> shapes2 = drawing2.getShapes();
        assertEquals(shapes1.size(), shapes2.size());

        for(int i = 0; i < shapes1.size(); i++){
            XSSFShape sh1 = (XSSFShape)shapes1.get(i);
            XSSFShape sh2 = (XSSFShape)shapes2.get(i);

            assertTrue(sh1.getClass() == sh2.getClass());
            assertEquals(sh1.getShapeProperties().toString(), sh2.getShapeProperties().toString());
        }
    }

    /**
     * ensure that rich text attributes defined in a XSSFRichTextString
     * are passed to XSSFSimpleShape.
     *
     * See Bugzilla 52219.
     */
    public void testRichText(){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 128, 128)));
        font.setItalic(true);
        font.setBold(true);
        font.setUnderline(FontUnderline.SINGLE);
        rt.applyFont(font);

        shape.setText(rt);

        CTTextParagraph pr = shape.getCTShape().getTxBody().getPArray(0);
        assertEquals(1, pr.sizeOfRArray());

        CTTextCharacterProperties rPr = pr.getRArray(0).getRPr();
        assertEquals(true, rPr.getB());
        assertEquals(true, rPr.getI());
        assertEquals(STTextUnderlineType.SNG, rPr.getU());
        assertTrue(Arrays.equals(
                new byte[]{0, (byte)128, (byte)128} ,
                rPr.getSolidFill().getSrgbClr().getVal()));

    }
}
