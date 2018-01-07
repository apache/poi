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

package org.apache.poi.hslf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test drawing shapes via Graphics2D
 *
 * @author Yegor Kozlov
 */
public final class TestPPGraphics2D {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    private HSLFSlideShow ppt;

    @Before
    public void setUp() throws Exception {
		ppt = new HSLFSlideShow(_slTests.openResourceAsStream("empty.ppt"));
    }

    @Test
    public void testGraphics() throws Exception {
    	// Starts off empty
    	assertTrue(ppt.getSlides().isEmpty());

    	// Add a slide
        HSLFSlide slide = ppt.createSlide();
    	assertEquals(1, ppt.getSlides().size());

    	// Add some stuff into it
        HSLFGroupShape group = new HSLFGroupShape();
        Dimension pgsize = ppt.getPageSize();
        java.awt.Rectangle bounds = new java.awt.Rectangle(0, 0, (int)pgsize.getWidth(), (int)pgsize.getHeight());
        group.setAnchor(bounds);
        slide.addShape(group);

        PPGraphics2D graphics = new PPGraphics2D(group);
        graphics.setColor(Color.blue);
        graphics.draw(new Rectangle(1296, 2544, 1344, 0));

        graphics.setColor(Color.red);
        graphics.setStroke(new BasicStroke((float)2.5));
        graphics.drawLine(500, 500, 1500, 2500);

        graphics.setColor(Color.green);
        graphics.setPaint(Color.gray);
        graphics.drawOval(4000, 1000, 1000, 1000);

        // Write the file out
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        // And read it back in
        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(1, ppt.getSlides().size());

        slide = ppt.getSlides().get(0);
        List<HSLFShape> shape = slide.getShapes();
        assertEquals(shape.size(), 1); //group shape

        assertTrue(shape.get(0) instanceof HSLFGroupShape); //group shape

        group = (HSLFGroupShape)shape.get(0);
        shape = group.getShapes();
        assertEquals(shape.size(), 3);
    }

}
