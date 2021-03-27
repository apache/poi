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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;

class TestXSLFTableRow {

    private static XMLSlideShow ppt;
    private static XSLFTable tbl;
    private static XSLFTableRow row;

    /** Copied from {@link TestXSLFTable#testRead()} */
    @BeforeEach
    void setUp() throws IOException {
        ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");

        XSLFSlide slide = ppt.getSlides().get(3);
        List<XSLFShape> shapes = slide.getShapes();
        tbl = (XSLFTable)shapes.get(0);
        List<XSLFTableRow> rows = tbl.getRows();
        row = rows.get(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        ppt.getPackage().revert();
        ppt.close();
    }


    @Test
    void constructor() {
        XSLFTableRow row2 = new XSLFTableRow(row.getXmlObject(), tbl);
        assertSame(row.getXmlObject(), row2.getXmlObject());
        assertEquals(row.getHeight(), row2.getHeight(), 1e-16);
    }

    @Test
    void testHeight() {
        final double h = 10.0;
        row.setHeight(h);
        assertEquals(h, row.getHeight(), 1e-16);
    }

    /** copied from {@link TestXSLFTable#testCreate()} */
    @Test
    void getCells() {
        List<XSLFTableCell> cells = row.getCells();
        assertNotNull(cells);
        assertEquals(3, cells.size());
    }

    @Test
    void testIterator() {
        int i = 0;
        for (XSLFTableCell cell : row) {
            i++;
            assertEquals("header"+i, cell.getText());
        }
        assertEquals(3, i);
    }

    /** copied from {@link TestXSLFTable#testCreate()} */
    @Test
    void addCell() {
        XSLFTableCell cell = row.addCell();
        assertNotNull(cell);

        assertNotNull(cell.getXmlObject());
        // by default table cell has no borders
        CTTableCell tc = (CTTableCell)cell.getXmlObject();
        assertTrue(tc.getTcPr().getLnB().isSetNoFill());
        assertTrue(tc.getTcPr().getLnT().isSetNoFill());
        assertTrue(tc.getTcPr().getLnL().isSetNoFill());
        assertTrue(tc.getTcPr().getLnR().isSetNoFill());
    }

    @Test
    void mergeCells() {
        assertThrows(IllegalArgumentException.class, () -> row.mergeCells(0, 0),
            "expected IllegalArgumentException when merging fewer than 2 columns");

        row.mergeCells(0, 1);
        List<XSLFTableCell> cells = row.getCells();
        //the top-left cell of a merged region is not regarded as merged
        assertFalse(cells.get(0).isMerged(), "top-left cell of merged region");
        assertTrue(cells.get(1).isMerged(), "inside merged region");
        assertFalse(cells.get(2).isMerged(), "outside merged region");
    }

    @Test
    void getXmlObject() {
        CTTableRow ctrow = row.getXmlObject();
        assertNotNull(ctrow);
    }


    @Test
    void getShapeNameOfCells() throws Exception {
        try(XMLSlideShow ss1 = XSLFTestDataSamples.openSampleDocument("table_test.pptx")) {
            for (XSLFSlide slide : ss1.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    assertEquals("Table 3", shape.getShapeName());
                    if (shape instanceof XSLFTable) {
                        for (XSLFTableRow row : ((XSLFTable) shape).getRows()) {
                            for (XSLFTableCell cell : row.getCells()) {
                                assertNull(cell.getShapeName()); // Do not throw NPE
                                assertThrows(IllegalStateException.class, cell::getShapeId);
                            }
                        }
                    }
                }
            }
        }
    }
}
