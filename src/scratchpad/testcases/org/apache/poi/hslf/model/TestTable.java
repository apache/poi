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

import junit.framework.TestCase;

import java.io.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.HSLFSlideShow;

/**
 * Test <code>Table</code> object.
 * 
 * @author Yegor Kozlov
 */
public class TestTable extends TestCase {

    /**
     * Test that ShapeFactory works properly and returns <code>Table</code>
     */
    public void testShapeFactory() throws Exception {
        SlideShow ppt = new SlideShow();

        Slide slide = ppt.createSlide();

        Table tbl = new Table(2, 5);
        slide.addShape(tbl);

        assertTrue(slide.getShapes()[0] instanceof Table);
        Table tbl2 = (Table)slide.getShapes()[0];
        assertEquals(tbl.getNumberOfColumns(), tbl2.getNumberOfColumns());
        assertEquals(tbl.getNumberOfRows(), tbl2.getNumberOfRows());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));
        slide = ppt.getSlides()[0];
        assertTrue(slide.getShapes()[0] instanceof Table);
        Table tbl3 = (Table)slide.getShapes()[0];
        assertEquals(tbl.getNumberOfColumns(), tbl3.getNumberOfColumns());
        assertEquals(tbl.getNumberOfRows(), tbl3.getNumberOfRows());
    }

}
