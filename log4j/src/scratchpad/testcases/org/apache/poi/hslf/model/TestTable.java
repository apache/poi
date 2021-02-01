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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTable;
import org.apache.poi.hslf.usermodel.HSLFTableCell;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.junit.jupiter.api.Test;

/**
 * Test <code>Table</code> object.
 */
public final class TestTable {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Test that ShapeFactory works properly and returns <code>Table</code>
     */
    @Test
    void testShapeFactory() throws IOException {
        final int noColumns, noRows;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();

            HSLFTable tbl = slide.createTable(2, 5);

            HSLFTableCell cell = tbl.getCell(0, 0);
            assertNotNull(cell);
            noColumns = tbl.getNumberOfColumns();
            noRows = tbl.getNumberOfRows();

            //table cells have type=TextHeaderAtom.OTHER_TYPE, see bug #46033
            assertEquals(TextPlaceholder.OTHER.nativeId, cell.getTextParagraphs().get(0).getRunType());

            HSLFShape tblSh = slide.getShapes().get(0);
            assertTrue(tblSh instanceof HSLFTable);
            HSLFTable tbl2 = (HSLFTable) tblSh;
            assertEquals(noColumns, tbl2.getNumberOfColumns());
            assertEquals(noRows, tbl2.getNumberOfRows());

            ppt.write(out);
        }

        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()))) {
            HSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.getShapes().get(0) instanceof HSLFTable);
            HSLFTable tbl3 = (HSLFTable) slide.getShapes().get(0);
            assertEquals(noColumns, tbl3.getNumberOfColumns());
            assertEquals(noRows, tbl3.getNumberOfRows());
        }
    }

    /**
     * Error constructing Table when rownum=1
     */
    @Test
    void test45889() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();
            List<HSLFShape> shapes;
            HSLFTable tbl1 = slide.createTable(1, 5);
            assertEquals(5, tbl1.getNumberOfColumns());
            assertEquals(1, tbl1.getNumberOfRows());

            shapes = slide.getShapes();
            assertEquals(1, shapes.size());

            HSLFTable tbl2 = (HSLFTable) shapes.get(0);
            assertSame(tbl1.getSpContainer(), tbl2.getSpContainer());

            assertEquals(tbl1.getNumberOfColumns(), tbl2.getNumberOfColumns());
            assertEquals(tbl1.getNumberOfRows(), tbl2.getNumberOfRows());
        }
    }

    // Table(rownum, colnum) must throw IllegalArgumentException if any of the arguments is less than 1
    @Test
    void testIllegalRowCnstruction() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();
            assertThrows(IllegalArgumentException.class, () -> slide.createTable(0, 5));
        }
    }

    // Table(rownum, colnum) must throw IllegalArgumentException if any of the arguments is less than 1
    @Test
    void testIllegalColConstruction() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();
            assertThrows(IllegalArgumentException.class, () -> slide.createTable(5, 0));
        }
    }

    /**
     * Bug 57820: initTable throws NullPointerException
     * when the table is positioned with its top at -1
     */
    @Test
    void test57820() throws IOException {
        try (SlideShow<?,?> ppt = new HSLFSlideShow(_slTests.openResourceAsStream("bug57820-initTableNullRefrenceException.ppt"))) {

            List<? extends Slide<?, ?>> slides = ppt.getSlides();
            assertEquals(1, slides.size());

            List<? extends Shape<?, ?>> shapes = slides.get(0).getShapes(); //throws NullPointerException

            TableShape<?, ?> tbl = null;
            for (Shape<?, ?> s : shapes) {
                if (s instanceof TableShape) {
                    tbl = (TableShape<?, ?>) s;
                    break;
                }
            }

            assertNotNull(tbl);
            assertEquals(-1, tbl.getAnchor().getY(), 0);
        }
    }
}
