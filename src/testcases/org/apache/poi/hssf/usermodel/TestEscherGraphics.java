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
        assertEquals("java.awt.Font[family=Arial,name=Arial,style=plain,size=10]", f.toString());
    }

    public void testGetFontMetrics() throws Exception
    {
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
        assertEquals("This is a test", t.getString().toString());
    }

}
