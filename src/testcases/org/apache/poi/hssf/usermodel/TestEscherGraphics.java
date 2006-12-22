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

/**
 * Tests the capabilities of the EscherGraphics class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestEscherGraphics extends TestCase
{
    private HSSFShapeGroup escherGroup;
    private EscherGraphics graphics;

    protected void setUp() throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("test");
        escherGroup = sheet.createDrawingPatriarch().createGroup(new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short) 0,0));
        escherGroup = new HSSFShapeGroup(null, new HSSFChildAnchor());
        graphics = new EscherGraphics(this.escherGroup, workbook, Color.black, 1.0f);
        super.setUp();
    }

    public void testGetFont() throws Exception
    {
        Font f = graphics.getFont();
        if (f.toString().indexOf("dialog") == -1 && f.toString().indexOf("Dialog") == -1)
            assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", f.toString());
    }

    public void testGetFontMetrics() throws Exception
    {
        Font f = graphics.getFont();
        if (f.toString().indexOf("dialog") != -1 || f.toString().indexOf("Dialog") != -1)
            return;
        FontMetrics fontMetrics = graphics.getFontMetrics(graphics.getFont());
        assertEquals(7, fontMetrics.charWidth('X'));
        assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", fontMetrics.getFont().toString());
    }

    public void testSetFont() throws Exception
    {
        Font f = new Font("Helvetica", 0, 12);
        graphics.setFont(f);
        assertEquals(f, graphics.getFont());
    }

    public void testSetColor() throws Exception
    {
        graphics.setColor(Color.red);
        assertEquals(Color.red, graphics.getColor());
    }

    public void testFillRect() throws Exception
    {
        graphics.fillRect( 10, 10, 20, 20 );
        HSSFSimpleShape s = (HSSFSimpleShape) escherGroup.getChildren().get(0);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, s.getShapeType());
        assertEquals(10, s.getAnchor().getDx1());
        assertEquals(10, s.getAnchor().getDy1());
        assertEquals(30, s.getAnchor().getDy2());
        assertEquals(30, s.getAnchor().getDx2());
    }

    public void testDrawString() throws Exception
    {
        graphics.drawString("This is a test", 10, 10);
        HSSFTextbox t = (HSSFTextbox) escherGroup.getChildren().get(0);
        assertEquals("This is a test", t.getString().getString().toString());
    }

}
