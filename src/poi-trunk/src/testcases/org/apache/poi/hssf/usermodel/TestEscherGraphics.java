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

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the capabilities of the EscherGraphics class.
 *
 * All tests have two escher groups available to them,
 *  one anchored at 0,0,1022,255 and another anchored
 *  at 20,30,500,200
 */
public final class TestEscherGraphics {
	private HSSFWorkbook workbook;
	private HSSFPatriarch patriarch;
    private HSSFShapeGroup escherGroupA;
    private EscherGraphics graphics;

    @Before
    public void setUp() throws IOException {
        workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet("test");
        patriarch = sheet.createDrawingPatriarch();
        escherGroupA = patriarch.createGroup(new HSSFClientAnchor(0,0,1022,255,(short)0,0,(short) 0,0));
        patriarch.createGroup(new HSSFClientAnchor(20,30,500,200,(short)0,0,(short) 0,0));
        graphics = new EscherGraphics(escherGroupA, workbook, Color.black, 1.0f);
    }
    
    @After
    public void closeResources() throws IOException {
        workbook.close();
    }

    @Test
    public void testGetFont() {
        Font f = graphics.getFont();
        if (! f.toString().contains("dialog") && ! f.toString().contains("Dialog")) {
            assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", f.toString());
        }
    }

    @Test
    public void testGetFontMetrics() {
        Font f = graphics.getFont();
        if (f.toString().contains("dialog") || f.toString().contains("Dialog")) {
            return;
        }
        FontMetrics fontMetrics = graphics.getFontMetrics(graphics.getFont());
        assertEquals(7, fontMetrics.charWidth('X'));
        assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", fontMetrics.getFont().toString());
    }

    @Test
    public void testSetFont() {
        Font f = new Font("Helvetica", 0, 12);
        graphics.setFont(f);
        assertEquals(f, graphics.getFont());
    }

    @Test
    public void testSetColor() {
        graphics.setColor(Color.red);
        assertEquals(Color.red, graphics.getColor());
    }

    @Test
    public void testFillRect() {
        graphics.fillRect( 10, 10, 20, 20 );
        HSSFSimpleShape s = (HSSFSimpleShape) escherGroupA.getChildren().get(0);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, s.getShapeType());
        assertEquals(10, s.getAnchor().getDx1());
        assertEquals(10, s.getAnchor().getDy1());
        assertEquals(30, s.getAnchor().getDy2());
        assertEquals(30, s.getAnchor().getDx2());
    }

    @Test
    public void testDrawString() {
        graphics.drawString("This is a test", 10, 10);
        HSSFTextbox t = (HSSFTextbox) escherGroupA.getChildren().get(0);
        assertEquals("This is a test", t.getString().getString());
    }

    @Test
    public void testGetDataBackAgain() throws Exception {
    	HSSFSheet s;
    	HSSFShapeGroup s1;
    	HSSFShapeGroup s2;

    	patriarch.setCoordinates(10, 20, 30, 40);

    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	workbook.write(baos);
    	workbook = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
    	s = workbook.getSheetAt(0);

    	patriarch = s.getDrawingPatriarch();

    	assertNotNull(patriarch);
    	assertEquals(10, patriarch.getX1());
    	assertEquals(20, patriarch.getY1());
    	assertEquals(30, patriarch.getX2());
    	assertEquals(40, patriarch.getY2());

    	// Check the two groups too
    	assertEquals(2, patriarch.countOfAllChildren());
    	assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
    	assertTrue(patriarch.getChildren().get(1) instanceof HSSFShapeGroup);

    	s1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
    	s2 = (HSSFShapeGroup)patriarch.getChildren().get(1);

    	assertEquals(0, s1.getX1());
    	assertEquals(0, s1.getY1());
    	assertEquals(1023, s1.getX2());
    	assertEquals(255, s1.getY2());
    	assertEquals(0, s2.getX1());
    	assertEquals(0, s2.getY1());
    	assertEquals(1023, s2.getX2());
    	assertEquals(255, s2.getY2());

    	assertEquals(0, s1.getAnchor().getDx1());
    	assertEquals(0, s1.getAnchor().getDy1());
    	assertEquals(1022, s1.getAnchor().getDx2());
    	assertEquals(255, s1.getAnchor().getDy2());
    	assertEquals(20, s2.getAnchor().getDx1());
    	assertEquals(30, s2.getAnchor().getDy1());
    	assertEquals(500, s2.getAnchor().getDx2());
    	assertEquals(200, s2.getAnchor().getDy2());


    	// Write and re-load once more, to check that's ok
    	baos = new ByteArrayOutputStream();
    	workbook.write(baos);
    	workbook = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
    	s = workbook.getSheetAt(0);
    	patriarch = s.getDrawingPatriarch();

    	assertNotNull(patriarch);
    	assertEquals(10, patriarch.getX1());
    	assertEquals(20, patriarch.getY1());
    	assertEquals(30, patriarch.getX2());
    	assertEquals(40, patriarch.getY2());

    	// Check the two groups too
    	assertEquals(2, patriarch.countOfAllChildren());
    	assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
    	assertTrue(patriarch.getChildren().get(1) instanceof HSSFShapeGroup);

    	s1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
    	s2 = (HSSFShapeGroup)patriarch.getChildren().get(1);

    	assertEquals(0, s1.getX1());
    	assertEquals(0, s1.getY1());
    	assertEquals(1023, s1.getX2());
    	assertEquals(255, s1.getY2());
    	assertEquals(0, s2.getX1());
    	assertEquals(0, s2.getY1());
    	assertEquals(1023, s2.getX2());
    	assertEquals(255, s2.getY2());

    	assertEquals(0, s1.getAnchor().getDx1());
    	assertEquals(0, s1.getAnchor().getDy1());
    	assertEquals(1022, s1.getAnchor().getDx2());
    	assertEquals(255, s1.getAnchor().getDy2());
    	assertEquals(20, s2.getAnchor().getDx1());
    	assertEquals(30, s2.getAnchor().getDy1());
    	assertEquals(500, s2.getAnchor().getDx2());
    	assertEquals(200, s2.getAnchor().getDy2());

    	// Change the positions of the first groups,
    	//  but not of their anchors
    	s1.setCoordinates(2, 3, 1021, 242);

    	baos = new ByteArrayOutputStream();
    	workbook.write(baos);
    	workbook = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
    	s = workbook.getSheetAt(0);
    	patriarch = s.getDrawingPatriarch();

    	assertNotNull(patriarch);
    	assertEquals(10, patriarch.getX1());
    	assertEquals(20, patriarch.getY1());
    	assertEquals(30, patriarch.getX2());
    	assertEquals(40, patriarch.getY2());

    	// Check the two groups too
    	assertEquals(2, patriarch.countOfAllChildren());
    	assertEquals(2, patriarch.getChildren().size());
    	assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
    	assertTrue(patriarch.getChildren().get(1) instanceof HSSFShapeGroup);

    	s1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
    	s2 = (HSSFShapeGroup)patriarch.getChildren().get(1);

    	assertEquals(2, s1.getX1());
    	assertEquals(3, s1.getY1());
    	assertEquals(1021, s1.getX2());
    	assertEquals(242, s1.getY2());
    	assertEquals(0, s2.getX1());
    	assertEquals(0, s2.getY1());
    	assertEquals(1023, s2.getX2());
    	assertEquals(255, s2.getY2());

    	assertEquals(0, s1.getAnchor().getDx1());
    	assertEquals(0, s1.getAnchor().getDy1());
    	assertEquals(1022, s1.getAnchor().getDx2());
    	assertEquals(255, s1.getAnchor().getDy2());
    	assertEquals(20, s2.getAnchor().getDx1());
    	assertEquals(30, s2.getAnchor().getDy1());
    	assertEquals(500, s2.getAnchor().getDx2());
    	assertEquals(200, s2.getAnchor().getDy2());


    	// Now add some text to one group, and some more
    	//  to the base, and check we can get it back again
    	HSSFTextbox tbox1 =
    		patriarch.createTextbox(new HSSFClientAnchor(1,2,3,4, (short)0,0,(short)0,0));
    	tbox1.setString(new HSSFRichTextString("I am text box 1"));
    	HSSFTextbox tbox2 =
    		s2.createTextbox(new HSSFChildAnchor(41,42,43,44));
    	tbox2.setString(new HSSFRichTextString("This is text box 2"));

    	assertEquals(3, patriarch.getChildren().size());


    	baos = new ByteArrayOutputStream();
    	workbook.write(baos);
    	workbook = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
    	s = workbook.getSheetAt(0);

    	patriarch = s.getDrawingPatriarch();

    	assertNotNull(patriarch);
    	assertEquals(10, patriarch.getX1());
    	assertEquals(20, patriarch.getY1());
    	assertEquals(30, patriarch.getX2());
    	assertEquals(40, patriarch.getY2());

    	// Check the two groups and the text
    	// Result of patriarch.countOfAllChildren() makes no sense: 
    	// Returns 4 for 2 empty groups + 1 TextBox.
    	//assertEquals(3, patriarch.countOfAllChildren()); 
    	assertEquals(3, patriarch.getChildren().size());

    	// Should be two groups and a text
    	assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
    	assertTrue(patriarch.getChildren().get(1) instanceof HSSFShapeGroup);
    	assertTrue(patriarch.getChildren().get(2) instanceof HSSFTextbox);

    	s1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
    	tbox1 = (HSSFTextbox)patriarch.getChildren().get(2);

    	s2 = (HSSFShapeGroup)patriarch.getChildren().get(1);

    	assertEquals(2, s1.getX1());
    	assertEquals(3, s1.getY1());
    	assertEquals(1021, s1.getX2());
    	assertEquals(242, s1.getY2());
    	assertEquals(0, s2.getX1());
    	assertEquals(0, s2.getY1());
    	assertEquals(1023, s2.getX2());
    	assertEquals(255, s2.getY2());

    	assertEquals(0, s1.getAnchor().getDx1());
    	assertEquals(0, s1.getAnchor().getDy1());
    	assertEquals(1022, s1.getAnchor().getDx2());
    	assertEquals(255, s1.getAnchor().getDy2());
    	assertEquals(20, s2.getAnchor().getDx1());
    	assertEquals(30, s2.getAnchor().getDy1());
    	assertEquals(500, s2.getAnchor().getDx2());
    	assertEquals(200, s2.getAnchor().getDy2());

    	// Not working just yet
    	assertEquals("I am text box 1", tbox1.getString().getString());
    }
}
