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
import org.apache.poi.hslf.HSLFSlideShow;

import java.awt.*;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

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
        getClass().getResourceAsStream("");
    }

    public void testGraphics() throws Exception {
        Slide slide = ppt.createSlide();

        Line line = new Line();
        line.setAnchor(new Rectangle(1296, 2544, 1344, 528));
        line.setLineWidth(3);
        line.setLineStyle(Line.LineDashSys);
        line.setLineColor(Color.red);
        slide.addShape(line);

        Ellipse ellipse = new Ellipse();
        ellipse.setAnchor(new Rectangle(4000, 1000, 1000, 1000));
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
        assertEquals(ppt.getSlides().length, 1);

        slide = ppt.getSlides()[0];
        Shape[] shape = slide.getShapes();
        assertEquals(shape.length, 2);

        assertTrue(shape[0] instanceof Line); //group shape
        assertEquals(shape[0].getAnchor(), new Rectangle(1296, 2544, 1344, 528)); //group shape

        assertTrue(shape[1] instanceof Ellipse); //group shape
        assertEquals(shape[1].getAnchor(), new Rectangle(4000, 1000, 1000, 1000)); //group shape
    }

}
