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
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.xslf.XSLFTestDataSamples;

/**
 * test common properties for sheets (slides, masters, layouts, etc.)
 *
 * @author Yegor Kozlov
 */
public class TestXSLFSheet extends TestCase {
    public void testCreateShapes(){
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertEquals(0, slide.getShapes().length);

        XSLFSimpleShape shape1 = slide.createAutoShape();
        assertEquals(1, slide.getShapes().length);
        assertSame(shape1, slide.getShapes()[0]);

        XSLFTextBox shape2 = slide.createTextBox();
        assertEquals(2, slide.getShapes().length);
        assertSame(shape1, slide.getShapes()[0]);
        assertSame(shape2, slide.getShapes()[1]);

        XSLFConnectorShape shape3 = slide.createConnector();
        assertEquals(3, slide.getShapes().length);
        assertSame(shape1, slide.getShapes()[0]);
        assertSame(shape2, slide.getShapes()[1]);
        assertSame(shape3, slide.getShapes()[2]);

        XSLFGroupShape shape4 = slide.createGroup();
        assertEquals(4, slide.getShapes().length);
        assertSame(shape1, slide.getShapes()[0]);
        assertSame(shape2, slide.getShapes()[1]);
        assertSame(shape3, slide.getShapes()[2]);
        assertSame(shape4, slide.getShapes()[3]);

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        slide = ppt.getSlides()[0];
        XSLFShape[] shapes = slide.getShapes();
        assertEquals(4, shapes.length);

        assertTrue(shapes[0] instanceof XSLFAutoShape);
        assertTrue(shapes[1] instanceof XSLFTextBox);
        assertTrue(shapes[2] instanceof XSLFConnectorShape);
        assertTrue(shapes[3] instanceof XSLFGroupShape);
    }
}