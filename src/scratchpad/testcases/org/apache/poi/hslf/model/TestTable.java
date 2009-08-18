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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * Test <code>Table</code> object.
 *
 * @author Yegor Kozlov
 */
public final class TestTable extends TestCase {

    /**
     * Test that ShapeFactory works properly and returns <code>Table</code>
     */
    public void testShapeFactory() throws Exception {
        SlideShow ppt = new SlideShow();

        Slide slide = ppt.createSlide();

        Table tbl = new Table(2, 5);
        slide.addShape(tbl);

        TableCell cell = tbl.getCell(0, 0);
        //table cells have type=TextHeaderAtom.OTHER_TYPE, see bug #46033
        assertEquals(TextHeaderAtom.OTHER_TYPE, cell.getTextRun().getRunType());

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

    /**
     * Error constructing Table when rownum=1
     */
    public void test45889(){
        SlideShow ppt = new SlideShow();
        Slide slide = ppt.createSlide();
        Shape[] shapes;
        Table tbl1 = new Table(1, 5);
        assertEquals(5, tbl1.getNumberOfColumns());
        assertEquals(1, tbl1.getNumberOfRows());
        slide.addShape(tbl1);

        shapes = slide.getShapes();
        assertEquals(1, shapes.length);

        Table tbl2 = (Table)shapes[0];
        assertSame(tbl1.getSpContainer(), tbl2.getSpContainer());

        assertEquals(tbl1.getNumberOfColumns(), tbl2.getNumberOfColumns());
        assertEquals(tbl1.getNumberOfRows(), tbl2.getNumberOfRows());
    }

    public void testIllegalCOnstruction(){
        try {
            new Table(0, 5);
            fail("Table(rownum, colnum) must throw IllegalArgumentException if any of tghe arguments is less than 1");
        } catch (IllegalArgumentException e){

        }
        try {
            new Table(5, 0);
            fail("Table(rownum, colnum) must throw IllegalArgumentException if any of tghe arguments is less than 1");
        } catch (IllegalArgumentException e){

        }
    }
}
