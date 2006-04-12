/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hslf.model;

import junit.framework.TestCase;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.HSLFSlideShow;

import java.awt.*;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

/**
 * Test drawing shapes via Graphics2D
 *
 * @author Yegor Kozlov
 */
public class TestShapes extends TestCase {
    private SlideShow ppt;

    protected void setUp() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/empty.ppt";
		ppt = new SlideShow(new HSLFSlideShow(filename));
    }

    public void testGraphics() throws Exception {
        Slide slide = ppt.createSlide();

        Line line = new Line();
        java.awt.Rectangle lineAnchor = new java.awt.Rectangle(100, 200, 50, 60);
        line.setAnchor(lineAnchor);
        System.out.println(line.getAnchor());
        line.setLineWidth(3);
        line.setLineStyle(Line.LineDashSys);
        line.setLineColor(Color.red);
        slide.addShape(line);

        AutoShape ellipse = new AutoShape(ShapeTypes.Ellipse);
        java.awt.Rectangle ellipseAnchor = new Rectangle(320, 154, 55, 111);
        ellipse.setAnchor(ellipseAnchor);
        ellipse.setLineWidth(2);
        ellipse.setLineStyle(Line.LineSolid);
        ellipse.setLineColor(Color.green);
        ellipse.setFillColor(Color.lightGray);
        slide.addShape(ellipse);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        //read ppt from byte array

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(1, ppt.getSlides().length);

        slide = ppt.getSlides()[0];
        Shape[] shape = slide.getShapes();
        assertEquals(2, shape.length);

        assertTrue(shape[0] instanceof Line); //group shape
        assertEquals(lineAnchor, shape[0].getAnchor()); //group shape

        assertTrue(shape[1] instanceof AutoShape); //group shape
        assertEquals(ellipseAnchor, shape[1].getAnchor()); //group shape
    }

    /**
     * Verify that we can read TextBox shapes
     * @throws Exception
     */
    public void testTextBoxRead() throws Exception {
        String dirname = System.getProperty("HSLF.testdata.path");
        String filename = dirname + "/with_textbox.ppt";
        ppt = new SlideShow(new HSLFSlideShow(filename));
        Slide sl = ppt.getSlides()[0];
        Shape[] sh = sl.getShapes();
        for (int i = 0; i < sh.length; i++) {
            assertTrue(sh[i] instanceof TextBox);
            TextBox txtbox = (TextBox)sh[i];
            String text = txtbox.getText();
            assertNotNull(text);

            assertEquals(txtbox.getRichTextRuns().length, 1);
            RichTextRun rt = txtbox.getRichTextRuns()[0];

            if (text.equals("Hello, World!!!")){
                assertEquals(32, rt.getFontSize());
                assertTrue(rt.isBold());
                assertTrue(rt.isItalic());
            } else if (text.equals("I am just a poor boy")){
                assertEquals(44, rt.getFontSize());
                assertTrue(rt.isBold());
            } else if (text.equals("This is Times New Roman")){
                assertEquals(16, rt.getFontSize());
                assertTrue(rt.isBold());
                assertTrue(rt.isItalic());
                assertTrue(rt.isUnderlined());
            } else if (text.equals("Plain Text")){
                assertEquals(18, rt.getFontSize());
            }
        }
    }

    /**
     * Verify that we can add TextBox shapes to a slide
     * @throws Exception
     */
    public void testTextBoxWrite() throws Exception {
        ppt = new SlideShow();
        Slide sl = ppt.createSlide();

        TextBox txtbox = new TextBox();
        txtbox.setText("Hello, World!");
        txtbox.setFontSize(42);
        txtbox.setBold(true);
        txtbox.setItalic(true);

        sl.addShape(txtbox);

        txtbox = new TextBox();
        txtbox.setText("Plain text in default font");
        sl.addShape(txtbox);

        assertEquals(sl.getShapes().length, 2);
        
        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        sl = ppt.getSlides()[0];
        assertEquals(sl.getShapes().length, 2);

        Shape[] sh = sl.getShapes();
        for (int i = 0; i < sh.length; i++) {
            assertTrue(sh[i] instanceof TextBox);
            txtbox = (TextBox)sh[i];
            String text = txtbox.getText();
            assertNotNull(text);

            assertEquals(txtbox.getRichTextRuns().length, 1);
            RichTextRun rt = txtbox.getRichTextRuns()[0];

            if (text.equals("Hello, World!")){
                assertEquals(42, rt.getFontSize());
                assertTrue(rt.isBold());
                assertTrue(rt.isItalic());
            }
        }
    }

}
