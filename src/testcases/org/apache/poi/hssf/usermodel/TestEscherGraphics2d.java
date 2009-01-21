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

import junit.framework.TestCase;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Tests the Graphics2d drawing capability.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestEscherGraphics2d extends TestCase {
	private HSSFShapeGroup escherGroup;
	private EscherGraphics2d graphics;

	protected void setUp() {

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("test");
		escherGroup = sheet.createDrawingPatriarch().createGroup(new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short) 0,0));
		escherGroup = new HSSFShapeGroup(null, new HSSFChildAnchor());
		EscherGraphics g = new EscherGraphics(this.escherGroup, workbook, Color.black, 1.0f);
		graphics = new EscherGraphics2d(g);
	}

	public void testDrawString() {
		graphics.drawString("This is a test", 10, 10);
		HSSFTextbox t = (HSSFTextbox) escherGroup.getChildren().get(0);
		assertEquals("This is a test", t.getString().getString().toString());

		// Check that with a valid font, it's still ok
		Font font = new Font("Forte", Font.PLAIN, 12);
		graphics.setFont(font);
		graphics.drawString("This is another test", 10, 10);

		// And test with ones that need the style appending
		font = new Font("dialog", Font.PLAIN, 12);
		graphics.setFont(font);
		graphics.drawString("This is another test", 10, 10);

		font = new Font("dialog", Font.BOLD, 12);
		graphics.setFont(font);
		graphics.drawString("This is another test", 10, 10);

		// But with an invalid font, we get an exception
		font = new Font("IamAmadeUPfont", Font.PLAIN, 22);
		graphics.setFont(font);
		try {
			graphics.drawString("This is another test", 10, 10);
			fail();
		} catch(IllegalArgumentException e) {
			// expected
		}
	}

	public void testFillRect() {
		graphics.fillRect( 10, 10, 20, 20 );
		HSSFSimpleShape s = (HSSFSimpleShape) escherGroup.getChildren().get(0);
		assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, s.getShapeType());
		assertEquals(10, s.getAnchor().getDx1());
		assertEquals(10, s.getAnchor().getDy1());
		assertEquals(30, s.getAnchor().getDy2());
		assertEquals(30, s.getAnchor().getDx2());
	}

	public void testGetFontMetrics() {
		FontMetrics fontMetrics = graphics.getFontMetrics(graphics.getFont());
		if (isDialogPresent()) // if dialog is returned we can't run the test properly.
			return;
		assertEquals(7, fontMetrics.charWidth('X'));
		assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", fontMetrics.getFont().toString());
	}

	public void testSetFont() {
		Font f = new Font("Helvetica", 0, 12);
		graphics.setFont(f);
		assertEquals(f, graphics.getFont());
	}

	public void testSetColor() {
		graphics.setColor(Color.red);
		assertEquals(Color.red, graphics.getColor());
	}

	public void testGetFont() {
		Font f = graphics.getFont();
		if (isDialogPresent()) // if dialog is returned we can't run the test properly.
			return;

		assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", f.toString());
	}

	private boolean isDialogPresent() {
		String fontDebugStr = graphics.getFont().toString();
		return fontDebugStr.indexOf("dialog") != -1 || fontDebugStr.indexOf("Dialog") != -1;
	}

	public void testDraw() {
		graphics.draw(new Line2D.Double(10,10,20,20));
		HSSFSimpleShape s = (HSSFSimpleShape) escherGroup.getChildren().get(0);
		assertTrue(s.getShapeType() == HSSFSimpleShape.OBJECT_TYPE_LINE);
		assertEquals(10, s.getAnchor().getDx1());
		assertEquals(10, s.getAnchor().getDy1());
		assertEquals(20, s.getAnchor().getDx2());
		assertEquals(20, s.getAnchor().getDy2());
	}
}
