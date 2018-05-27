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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TestXSSFDrawing {
    @Test
    public void testRead() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithDrawing.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        //the sheet has one relationship and it is XSSFDrawing
        List<RelationPart> rels = sheet.getRelationParts();
        assertEquals(1, rels.size());
        RelationPart rp = rels.get(0);
        assertTrue(rp.getDocumentPart() instanceof XSSFDrawing);

        XSSFDrawing drawing = rp.getDocumentPart();
        //sheet.createDrawingPatriarch() should return the same instance of XSSFDrawing
        assertSame(drawing, sheet.createDrawingPatriarch());
        String drawingId = rp.getRelationship().getId();

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

        checkRewrite(wb);
        wb.close();
    }

    @Test
    public void testNew() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
        //multiple calls of createDrawingPatriarch should return the same instance of XSSFDrawing
        XSSFDrawing dr1 = sheet.createDrawingPatriarch();
        XSSFDrawing dr2 = sheet.createDrawingPatriarch();
        assertSame(dr1, dr2);

        List<RelationPart> rels = sheet.getRelationParts();
        assertEquals(1, rels.size());
        RelationPart rp = rels.get(0);
        assertTrue(rp.getDocumentPart() instanceof XSSFDrawing);

        XSSFDrawing drawing = rp.getDocumentPart();
        String drawingId = rp.getRelationship().getId();

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
        rt.applyFont(0, 5, wb1.createFont());
        rt.applyFont(5, 6, wb1.createFont());
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
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

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

        checkRewrite(wb2);
        wb2.close();
    }

    @Test
    public void testMultipleDrawings() throws IOException{
        XSSFWorkbook wb = new XSSFWorkbook();
        for (int i = 0; i < 3; i++) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            assertNotNull(drawing);
        }
        OPCPackage pkg = wb.getPackage();
        try {
            assertEquals(3, pkg.getPartsByContentType(XSSFRelation.DRAWINGS.getContentType()).size());
            checkRewrite(wb);
        } finally {
            pkg.close();
        }
        wb.close();
    }

    @Test
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

        checkRewrite(wb);
        wb.close();
    }

    /**
     * ensure that rich text attributes defined in a XSSFRichTextString
     * are passed to XSSFSimpleShape.
     *
     * See Bugzilla 52219.
     */
    @Test
    public void testRichText() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 128, 128), wb.getStylesSource().getIndexedColors()));
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
        assertArrayEquals(
                new byte[]{0, (byte)128, (byte)128} ,
                rPr.getSolidFill().getSrgbClr().getVal());

        checkRewrite(wb);
        wb.close();
    }

    /**
     *  test that anchor is not null when reading shapes from existing drawings
     */
    @Test
    public void testReadAnchors() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFClientAnchor anchor1 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4);
        XSSFShape shape1 = drawing.createTextbox(anchor1);
        assertNotNull(shape1);

        XSSFClientAnchor anchor2 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 5);
        XSSFShape shape2 = drawing.createTextbox(anchor2);
        assertNotNull(shape2);

        int pictureIndex= wb1.addPicture(new byte[]{}, XSSFWorkbook.PICTURE_TYPE_PNG);
        XSSFClientAnchor anchor3 = new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 6);
        XSSFShape shape3 = drawing.createPicture(anchor3, pictureIndex);
        assertNotNull(shape3);

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(shapes.get(0).getAnchor(), anchor1);
        assertEquals(shapes.get(1).getAnchor(), anchor2);
        assertEquals(shapes.get(2).getAnchor(), anchor3);

        checkRewrite(wb2);
        wb2.close();
    }

    /**
     * ensure that font and color rich text attributes defined in a XSSFRichTextString
     * are passed to XSSFSimpleShape.
     *
     * See Bugzilla 54969.
     */
    @Test
    public void testRichTextFontAndColor() throws IOException {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 128, 128), wb.getStylesSource().getIndexedColors()));
        font.setFontName("Arial");
        rt.applyFont(font);

        shape.setText(rt);

        CTTextParagraph pr = shape.getCTShape().getTxBody().getPArray(0);
        assertEquals(1, pr.sizeOfRArray());

        CTTextCharacterProperties rPr = pr.getRArray(0).getRPr();
        assertEquals("Arial", rPr.getLatin().getTypeface());
        assertArrayEquals(
                new byte[]{0, (byte)128, (byte)128} ,
                rPr.getSolidFill().getSrgbClr().getVal());
        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test setText single paragraph to ensure backwards compatibility
     */
    @Test
    public void testSetTextSingleParagraph() throws IOException {
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test String");

        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(new Color(0, 255, 255), wb.getStylesSource().getIndexedColors()));
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
        assertArrayEquals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test addNewTextParagraph
     */
    @Test
    public void testAddNewTextParagraph() throws IOException {
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

        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test addNewTextParagraph using RichTextString
     */
    @Test
    public void testAddNewTextParagraphWithRTS() throws IOException {
    	XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));
        XSSFRichTextString rt = new XSSFRichTextString("Test Rich Text String");

        XSSFFont font = wb1.createFont();
        font.setColor(new XSSFColor(new Color(0, 255, 255), wb1.getStylesSource().getIndexedColors()));
        font.setFontName("Arial");
        rt.applyFont(font);

        XSSFFont midfont = wb1.createFont();
        midfont.setColor(new XSSFColor(new Color(0, 255, 0), wb1.getStylesSource().getIndexedColors()));
        rt.applyFont(5, 14, midfont);	// set the text "Rich Text" to be green and the default font

        XSSFTextParagraph para = shape.addNewTextParagraph(rt);

        // Save and re-load it
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

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
        assertArrayEquals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        // second run properties
        assertEquals("Rich Text", runs.get(1).getText());
        assertEquals(XSSFFont.DEFAULT_FONT_NAME, runs.get(1).getFontFamily());

        clr = runs.get(1).getFontColor();
        assertArrayEquals(
                new int[] { 0, 255, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        // third run properties
        assertEquals(" String", runs.get(2).getText());
        assertEquals("Arial", runs.get(2).getFontFamily());
        clr = runs.get(2).getFontColor();
        assertArrayEquals(
                new int[] { 0, 255, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        checkRewrite(wb2);
        wb2.close();
    }

    /**
     * Test add multiple paragraphs and retrieve text
     */
    @Test
    public void testAddMultipleParagraphs() throws IOException {
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

        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test setting the text, then adding multiple paragraphs and retrieve text
     */
    @Test
    public void testSetAddMultipleParagraphs() throws IOException {
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

        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test reading text from a textbox in an existing file
     */
    @Test
    public void testReadTextBox() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithDrawing.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        //the sheet has one relationship and it is XSSFDrawing
        List<RelationPart> rels = sheet.getRelationParts();
        assertEquals(1, rels.size());
        RelationPart rp = rels.get(0);
        assertTrue(rp.getDocumentPart() instanceof XSSFDrawing);

        XSSFDrawing drawing = rp.getDocumentPart();
        //sheet.createDrawingPatriarch() should return the same instance of XSSFDrawing
        assertSame(drawing, sheet.createDrawingPatriarch());
        String drawingId = rp.getRelationship().getId();

        //there should be a relation to this drawing in the worksheet
        assertTrue(sheet.getCTWorksheet().isSetDrawing());
        assertEquals(drawingId, sheet.getCTWorksheet().getDrawing().getId());

        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(6, shapes.size());

        assertTrue(shapes.get(4) instanceof XSSFSimpleShape);

        XSSFSimpleShape textbox = (XSSFSimpleShape) shapes.get(4);
        assertEquals("Sheet with various pictures\n(jpeg, png, wmf, emf and pict)", textbox.getText());

        checkRewrite(wb);
        wb.close();
    }


    /**
     * Test reading multiple paragraphs from a textbox in an existing file
     */
    @Test
    public void testReadTextBoxParagraphs() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTextBox.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        //the sheet has one relationship and it is XSSFDrawing
        List<RelationPart> rels = sheet.getRelationParts();
        assertEquals(1, rels.size());
        RelationPart rp = rels.get(0);

        assertTrue(rp.getDocumentPart() instanceof XSSFDrawing);

        XSSFDrawing drawing = rp.getDocumentPart();

        //sheet.createDrawingPatriarch() should return the same instance of XSSFDrawing
        assertSame(drawing, sheet.createDrawingPatriarch());
        String drawingId = rp.getRelationship().getId();

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
        assertArrayEquals(
                new int[] { 255, 0, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        clr = paras.get(1).getTextRuns().get(0).getFontColor();
        assertArrayEquals(
                new int[] { 0, 255, 0 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        clr = paras.get(2).getTextRuns().get(0).getFontColor();
        assertArrayEquals(
                new int[] { 0, 0, 255 } ,
                new int[] { clr.getRed(), clr.getGreen(), clr.getBlue() });

        checkRewrite(wb);
        wb.close();
    }

    /**
     * Test adding and reading back paragraphs as bullet points
     */
    @Test
    public void testAddBulletParagraphs() throws IOException {

        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
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
        assertNotNull(para);
        para = shape.addNewTextParagraph(paraString2);
        para.setBullet(true);

        para = shape.addNewTextParagraph(paraString3);
        para.setBullet(true);
        para.setLevel(1);

        para = shape.addNewTextParagraph(paraString4);
        para.setBullet(true);

        para = shape.addNewTextParagraph(paraString5);
        assertNotNull(para);
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
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

        // Check
        drawing = sheet.createDrawingPatriarch();

        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape);

        XSSFSimpleShape sshape = (XSSFSimpleShape) shapes.get(0);

        List<XSSFTextParagraph> paras = sshape.getTextParagraphs();
        assertEquals(12, paras.size());  // this should be 12 as XSSFSimpleShape creates a default paragraph (no text), and then we added to that

        String builder =
                paraString1 +
                "\n" +
                "\u2022 " +
                paraString2 +
                "\n" +
                "\t\u2022 " +
                paraString3 +
                "\n" +
                "\u2022 " +
                paraString4 +
                "\n" +
                paraString5 +
                "\n" +
                "1. " +
                paraString6 +
                "\n" +
                "\t3. " +
                paraString7 +
                "\n" +
                "\t4. " +
                paraString8 +
                "\n" +
                "\t" +   // should be empty
                "\n" +
                "\t5. " +
                paraString9 +
                "\n" +
                "2. " +
                paraString10;

        assertEquals(builder, sshape.getText());

        checkRewrite(wb2);
        wb2.close();
    }

    /**
     * Test reading bullet numbering from a textbox in an existing file
     */
    @Test
    public void testReadTextBox2() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTextBox2.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();
        XSSFSimpleShape textbox = (XSSFSimpleShape) shapes.get(0);
        String extracted = textbox.getText();
        String sb =
                "1. content1A\n" +
                "\t1. content1B\n" +
                "\t2. content2B\n" +
                "\t3. content3B\n" +
                "2. content2A\n" +
                "\t3. content2BStartAt3\n" +
                "\t\n\t\n\t" +
                "4. content2BStartAt3Incremented\n" +
                "\t\n\t\n\t\n\t";

        assertEquals(sb, extracted);
        checkRewrite(wb);
        wb.close();
    }

    @Test
    public void testXSSFSimpleShapeCausesNPE56514() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("56514.xlsx");
        XSSFSheet sheet = wb1.getSheetAt(0);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(4, shapes.size());

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        drawing = sheet.createDrawingPatriarch();

        shapes = drawing.getShapes();
        assertEquals(4, shapes.size());
        wb2.close();
        }

    @Test
    public void testXSSFSAddPicture() throws Exception {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
        //multiple calls of createDrawingPatriarch should return the same instance of XSSFDrawing
        XSSFDrawing dr1 = sheet.createDrawingPatriarch();
        XSSFDrawing dr2 = sheet.createDrawingPatriarch();
        assertSame(dr1, dr2);

        List<RelationPart> rels = sheet.getRelationParts();
        assertEquals(1, rels.size());
        RelationPart rp = rels.get(0);
        assertTrue(rp.getDocumentPart() instanceof XSSFDrawing);

        assertEquals(0, rp.getDocumentPart().getRelations().size());

        XSSFDrawing drawing = rp.getDocumentPart();
        String drawingId = rp.getRelationship().getId();

        //there should be a relation to this drawing in the worksheet
        assertTrue(sheet.getCTWorksheet().isSetDrawing());
        assertEquals(drawingId, sheet.getCTWorksheet().getDrawing().getId());

        byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");

        ClientAnchor anchor = wb1.getCreationHelper().createClientAnchor();
        anchor.setCol1(1);
        anchor.setRow1(1);

        drawing.createPicture(anchor, wb1.addPicture(pictureData, Workbook.PICTURE_TYPE_JPEG));
        final int pictureIndex = wb1.addPicture(pictureData, Workbook.PICTURE_TYPE_JPEG);
        drawing.createPicture(anchor, pictureIndex);
        drawing.createPicture(anchor, pictureIndex);

        // repeated additions of same share package relationship
        assertEquals(2, rp.getDocumentPart().getPackagePart().getRelationships().size());

        List<XSSFShape> shapes = drawing.getShapes();
        assertEquals(3, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFPicture);
        assertTrue(shapes.get(1) instanceof XSSFPicture);

        // Save and re-load it
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

        // Check
        dr1 = sheet.createDrawingPatriarch();
        CTDrawing ctDrawing = dr1.getCTDrawing();

        // Connector, shapes and text boxes are all two cell anchors
        assertEquals(0, ctDrawing.sizeOfAbsoluteAnchorArray());
        assertEquals(0, ctDrawing.sizeOfOneCellAnchorArray());
        assertEquals(3, ctDrawing.sizeOfTwoCellAnchorArray());

        shapes = dr1.getShapes();
        assertEquals(3, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFPicture);
        assertTrue(shapes.get(1) instanceof XSSFPicture);

        checkRewrite(wb2);
        wb2.close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBug56835CellComment() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        // first comment works
        ClientAnchor anchor = new XSSFClientAnchor(1, 1, 2, 2, 3, 3, 4, 4);
        XSSFComment comment = drawing.createCellComment(anchor);
        assertNotNull(comment);

        // Should fail if we try to add the same comment for the same cell
        try {
            drawing.createCellComment(anchor);
        } finally {
            wb.close();
        }
    }

    @Test
    public void testGroupShape() throws Exception {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFSimpleShape s0 = drawing.createSimpleShape(drawing.createAnchor(0, 0, Units.pixelToEMU(30), Units.pixelToEMU(30), 1, 1, 10, 10));
        s0.setShapeType(ShapeTypes.RECT);
        s0.setLineStyleColor(100, 0, 0);

        XSSFShapeGroup g1 = drawing.createGroup(drawing.createAnchor(0, 0, 300, 300, 1, 1, 10, 10));
        CTGroupTransform2D xfrmG1 = g1.getCTGroupShape().getGrpSpPr().getXfrm();

        XSSFSimpleShape s1 = g1.createSimpleShape(new XSSFChildAnchor(
            (int)(xfrmG1.getChExt().getCx()*0.1),
            (int)(xfrmG1.getChExt().getCy()*0.1),
            (int)(xfrmG1.getChExt().getCx()*0.9),
            (int)(xfrmG1.getChExt().getCy()*0.9)
        ));
        s1.setShapeType(ShapeTypes.RECT);
        s1.setLineStyleColor(0, 100, 0);

        XSSFShapeGroup g2 = g1.createGroup(new XSSFChildAnchor(
            (int)(xfrmG1.getChExt().getCx()*0.2),
            (int)(xfrmG1.getChExt().getCy()*0.2),
            (int)(xfrmG1.getChExt().getCx()*0.8),
            (int)(xfrmG1.getChExt().getCy()*0.8)
        ));
        CTGroupTransform2D xfrmG2 = g2.getCTGroupShape().getGrpSpPr().getXfrm();

        XSSFSimpleShape s2 = g2.createSimpleShape(new XSSFChildAnchor(
            (int)(xfrmG2.getChExt().getCx()*0.1),
            (int)(xfrmG2.getChExt().getCy()*0.1),
            (int)(xfrmG2.getChExt().getCx()*0.9),
            (int)(xfrmG2.getChExt().getCy()*0.9)
        ));
        s2.setShapeType(ShapeTypes.RECT);
        s2.setLineStyleColor(0, 0, 100);

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();

        XSSFDrawing draw = wb2.getSheetAt(0).getDrawingPatriarch();
        List<XSSFShape> shapes = draw.getShapes();
        assertEquals(2, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape);
        assertTrue(shapes.get(1) instanceof XSSFShapeGroup);
        shapes = draw.getShapes((XSSFShapeGroup)shapes.get(1));
        assertEquals(2, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape);
        assertTrue(shapes.get(1) instanceof XSSFShapeGroup);
        shapes = draw.getShapes((XSSFShapeGroup)shapes.get(1));
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSSFSimpleShape);

        wb2.close();
    }


    private static void checkRewrite(XSSFWorkbook wb) throws IOException {
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wb2);
        wb2.close();
    }
}
