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

import java.awt.Color;
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

        for(XSSFShape sh : shapes) assertNotNull(sh.getAnchor());

        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
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
        assertNotNull(c2);

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
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
    
    public void testMultipleDrawings(){
        XSSFWorkbook wb = new XSSFWorkbook();
        for (int i = 0; i < 3; i++) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            assertNotNull(drawing);
        }
        OPCPackage pkg = wb.getPackage();
        assertEquals(3, pkg.getPartsByContentType(XSSFRelation.DRAWINGS.getContentType()).size());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
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
            XSSFShape sh1 = shapes1.get(i);
            XSSFShape sh2 = shapes2.get(i);

            assertTrue(sh1.getClass() == sh2.getClass());
            assertEquals(sh1.getShapeProperties().toString(), sh2.getShapeProperties().toString());
        }
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
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
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    /**
     *  test that anchor is not null when reading shapes from existing drawings
     */
    public void testReadAnchors(){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFClientAnchor anchor1 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4);
        XSSFShape shape1 = drawing.createTextbox(anchor1);
        assertNotNull(shape1);

        XSSFClientAnchor anchor2 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 5);
        XSSFShape shape2 = drawing.createTextbox(anchor2);
        assertNotNull(shape2);

        int pictureIndex= wb.addPicture(new byte[]{}, XSSFWorkbook.PICTURE_TYPE_PNG);
        XSSFClientAnchor anchor3 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 6);
        XSSFShape shape3 = drawing.createPicture(anchor3, pictureIndex);
        assertNotNull(shape3);

        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(shapes.get(0).getAnchor(), anchor1);
        assertEquals(shapes.get(1).getAnchor(), anchor2);
        assertEquals(shapes.get(2).getAnchor(), anchor3);
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
    
    /**
     * ensure that font and color rich text attributes defined in a XSSFRichTextString
     * are passed to XSSFSimpleShape.
     *
     * See Bugzilla 54969.
     */
    public void testRichTextFontAndColor() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 128, 128)));
        font.setFontName("Arial");
        rt.applyFont(font);

        shape.setText(rt);

        CTTextParagraph pr = shape.getCTShape().getTxBody().getPArray(0);
        assertEquals(1, pr.sizeOfRArray());

        CTTextCharacterProperties rPr = pr.getRArray(0).getRPr();
        assertEquals("Arial", rPr.getLatin().getTypeface());
        assertTrue(Arrays.equals(
                new byte[]{0, (byte)128, (byte)128} ,
                rPr.getSolidFill().getSrgbClr().getVal()));
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    /**
     * Test setText single paragraph to ensure backwards compatibility
     */
    public void testSetTextSingleParagraph() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 255, 255)));
        font.setFontName("Arial");
        rt.applyFont(font);

        shape.setText(rt);
        
        List<XSSFTextParagraph> paras = shape.getTextParagraphs();
        assertEquals(1, paras.size());
        assertEquals("Test String", paras.get(0).getText());
        
        List<XSSFTextRun> runs = paras.get(0).getTextRuns();
        assertEquals(1, runs.size());
        assertEquals("Arial", runs.get(0).getFontFamily());
        
        Color clr = runs.get(0).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
    
    /**
     * Test addNewTextParagraph 
     */
    public void testAddNewTextParagraph() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        
        XSSFTextParagraph para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 1");
                
        List<XSSFTextParagraph> paras = shape.getTextParagraphs();
        assertEquals(2, paras.size());	// this should be 2 as XSSFSimpleShape creates a default paragraph (no text), and then we add a string to that.
        
        List<XSSFTextRun> runs = para.getTextRuns();
        assertEquals(1, runs.size());
        assertEquals("Line 1", runs.get(0).getText());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    /**
     * Test addNewTextParagraph using RichTextString
     */
    public void testAddNewTextParagraphWithRTS() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test Rich Text String");

        XSSFFont font = wb.createFont();        
        font.setColor(new XSSFColor(new Color(0, 255, 255)));
        font.setFontName("Arial");
        rt.applyFont(font);
        
        XSSFFont midfont = wb.createFont();
        midfont.setColor(new XSSFColor(new Color(0, 255, 0)));
        rt.applyFont(5, 14, midfont);	// set the text "Rich Text" to be green and the default font
        
        XSSFTextParagraph para = shape.addNewTextParagraph(rt);
        
        // Save and re-load it
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        // Check
        drawing = sheet.createDrawingPatriarch();
        
        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape); 
        
        XSSFSimpleShape sshape = (XSSFSimpleShape) shapes.get(0);
        
        List<XSSFTextParagraph> paras = sshape.getTextParagraphs();
        assertEquals(2, paras.size());	// this should be 2 as XSSFSimpleShape creates a default paragraph (no text), and then we add a string to that.  
        
        List<XSSFTextRun> runs = para.getTextRuns();
        assertEquals(3, runs.size());
        
        // first run properties
        assertEquals("Test ", runs.get(0).getText());
        assertEquals("Arial", runs.get(0).getFontFamily());

        Color clr = runs.get(0).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));

        // second run properties        
        assertEquals("Rich Text", runs.get(1).getText());
        assertEquals(XSSFFont.DEFAULT_FONT_NAME, runs.get(1).getFontFamily());

        clr = runs.get(1).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 255, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));        
        
        // third run properties
        assertEquals(" String", runs.get(2).getText());
        assertEquals("Arial", runs.get(2).getFontFamily());
        clr = runs.get(2).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }    
    
    /**
     * Test add multiple paragraphs and retrieve text
     */
    public void testAddMultipleParagraphs() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        
        XSSFTextParagraph para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 1");
                
        para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 2");
        
        para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 3");
        
        List<XSSFTextParagraph> paras = shape.getTextParagraphs();
        assertEquals(4, paras.size());	// this should be 4 as XSSFSimpleShape creates a default paragraph (no text), and then we added 3 paragraphs
        assertEquals("Line 1\nLine 2\nLine 3", shape.getText());           
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
    
    /**
     * Test setting the text, then adding multiple paragraphs and retrieve text
     */
    public void testSetAddMultipleParagraphs() {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        
        shape.setText("Line 1");
                
        XSSFTextParagraph para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 2");
        
        para = shape.addNewTextParagraph();
        para.addNewTextRun().setText("Line 3");
        
        List<XSSFTextParagraph> paras = shape.getTextParagraphs();
        assertEquals(3, paras.size());	// this should be 3 as we overwrote the default paragraph with setText, then added 2 new paragraphs
        assertEquals("Line 1\nLine 2\nLine 3", shape.getText());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
    
    /**
     * Test reading text from a textbox in an existing file
     */
    public void testReadTextBox(){
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

        assertTrue(shapes.get(4) instanceof XSSFSimpleShape);

        XSSFSimpleShape textbox = (XSSFSimpleShape) shapes.get(4); 
        assertEquals("Sheet with various pictures\n(jpeg, png, wmf, emf and pict)", textbox.getText());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    
    /**
     * Test reading multiple paragraphs from a textbox in an existing file
     */
    public void testReadTextBoxParagraphs(){
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTextBox.xlsx");
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
        assertEquals(1, shapes.size());

        assertTrue(shapes.get(0) instanceof XSSFSimpleShape);

        XSSFSimpleShape textbox = (XSSFSimpleShape) shapes.get(0);
        
        List<XSSFTextParagraph> paras = textbox.getTextParagraphs();
        assertEquals(3, paras.size());
        
        assertEquals("Line 2", paras.get(1).getText());	// check content of second paragraph

        assertEquals("Line 1\nLine 2\nLine 3", textbox.getText());	// check content of entire textbox
        
        // check attributes of paragraphs
        assertEquals(TextAlign.LEFT, paras.get(0).getTextAlign());
        assertEquals(TextAlign.CENTER, paras.get(1).getTextAlign());
        assertEquals(TextAlign.RIGHT, paras.get(2).getTextAlign());
        
        Color clr = paras.get(0).getTextRuns().get(0).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 255, 0, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));
        
        clr = paras.get(1).getTextRuns().get(0).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 255, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));
        
        clr = paras.get(2).getTextRuns().get(0).getFontColor(); 
        assertTrue(Arrays.equals(
                new int[] { 0, 0, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() }));
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    /**
     * Test adding and reading back paragraphs as bullet points
     */
    public void testAddBulletParagraphs() {
    
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 10, 20));
        
        String paraString1 = "A normal paragraph";
        String paraString2 = "First bullet";
        String paraString3 = "Second bullet (level 1)";
        String paraString4 = "Third bullet";
        String paraString5 = "Another normal paragraph";
        String paraString6 = "First numbered bullet";
        String paraString7 = "Second bullet (level 1)";
        String paraString8 = "Third bullet (level 1)";
        String paraString9 = "Fourth bullet (level 1)";
        String paraString10 = "Fifth Bullet";
        
        XSSFTextParagraph para = shape.addNewTextParagraph(paraString1);
        para = shape.addNewTextParagraph(paraString2);
        para.setBullet(true);

        para = shape.addNewTextParagraph(paraString3);
        para.setBullet(true);
        para.setLevel(1);

        para = shape.addNewTextParagraph(paraString4);
        para.setBullet(true);
        
        para = shape.addNewTextParagraph(paraString5);
        para = shape.addNewTextParagraph(paraString6);
        para.setBullet(ListAutoNumber.ARABIC_PERIOD);

        para = shape.addNewTextParagraph(paraString7);
        para.setBullet(ListAutoNumber.ARABIC_PERIOD, 3);
        para.setLevel(1);
        
        para = shape.addNewTextParagraph(paraString8);
        para.setBullet(ListAutoNumber.ARABIC_PERIOD, 3);
        para.setLevel(1);

        para = shape.addNewTextParagraph("");
        para.setBullet(ListAutoNumber.ARABIC_PERIOD, 3);
        para.setLevel(1);
        
        para = shape.addNewTextParagraph(paraString9);
        para.setBullet(ListAutoNumber.ARABIC_PERIOD, 3);
        para.setLevel(1);
        
        para = shape.addNewTextParagraph(paraString10);
        para.setBullet(ListAutoNumber.ARABIC_PERIOD);
        
        // Save and re-load it
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        // Check
        drawing = sheet.createDrawingPatriarch();
        
        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape); 
        
        XSSFSimpleShape sshape = (XSSFSimpleShape) shapes.get(0);
        
        List<XSSFTextParagraph> paras = sshape.getTextParagraphs();
        assertEquals(12, paras.size());  // this should be 12 as XSSFSimpleShape creates a default paragraph (no text), and then we added to that
        
        StringBuilder builder = new StringBuilder();
        
        builder.append(paraString1);
        builder.append("\n");
        builder.append("\u2022 ");
        builder.append(paraString2);
        builder.append("\n");
        builder.append("\t\u2022 ");
        builder.append(paraString3);
        builder.append("\n");
        builder.append("\u2022 ");
        builder.append(paraString4);
        builder.append("\n");
        builder.append(paraString5);
        builder.append("\n");
        builder.append("1. ");
        builder.append(paraString6);
        builder.append("\n");
        builder.append("\t3. ");
        builder.append(paraString7);
        builder.append("\n");
        builder.append("\t4. ");
        builder.append(paraString8);
        builder.append("\n");
        builder.append("\t");   // should be empty
        builder.append("\n");
        builder.append("\t5. ");
        builder.append(paraString9);
        builder.append("\n");
        builder.append("2. ");
        builder.append(paraString10);
        
        assertEquals(builder.toString(), sshape.getText());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }  
    
    /**
     * Test reading bullet numbering from a textbox in an existing file
     */
    public void testReadTextBox2(){
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTextBox2.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();
        XSSFSimpleShape textbox = (XSSFSimpleShape) shapes.get(0);
        String extracted = textbox.getText();
        StringBuilder sb = new StringBuilder();
        sb.append("1. content1A\n");
        sb.append("\t1. content1B\n");
        sb.append("\t2. content2B\n");
        sb.append("\t3. content3B\n");
        sb.append("2. content2A\n");
        sb.append("\t3. content2BStartAt3\n");
        sb.append("\t\n\t\n\t");
        sb.append("4. content2BStartAt3Incremented\n");
        sb.append("\t\n\t\n\t\n\t");

        assertEquals(sb.toString(), extracted);
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }
}
