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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFTable extends TestCase {

    public void testRead(){
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");

        XSLFSlide slide = ppt.getSlides()[3];
        XSLFShape[] shapes = slide.getShapes();
        assertEquals(1, shapes.length);
        assertTrue(shapes[0] instanceof XSLFTable);
        XSLFTable tbl = (XSLFTable)shapes[0];
        assertEquals(3, tbl.getNumberOfColumns());
        assertEquals(6, tbl.getNumberOfRows());
        assertNotNull(tbl.getCTTable());

        List<XSLFTableRow> rows = tbl.getRows();
        assertEquals(6, rows.size());

        assertEquals(90.0, tbl.getColumnWidth(0));
        assertEquals(240.0, tbl.getColumnWidth(1));
        assertEquals(150.0, tbl.getColumnWidth(2));

        for(XSLFTableRow row : tbl){
            // all rows have the same height
            assertEquals(29.2, row.getHeight());
        }

        XSLFTableRow row0 = rows.get(0);
        List<XSLFTableCell> cells0 = row0.getCells();
        assertEquals(3, cells0.size());
        assertEquals("header1", cells0.get(0).getText());
        assertEquals("header2", cells0.get(1).getText());
        assertEquals("header3", cells0.get(2).getText());

        XSLFTableRow row1 = rows.get(1);
        List<XSLFTableCell> cells1 = row1.getCells();
        assertEquals(3, cells1.size());
        assertEquals("A1", cells1.get(0).getText());
        assertEquals("B1", cells1.get(1).getText());
        assertEquals("C1", cells1.get(2).getText());
    }

    public void testCreate() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFTable tbl = slide.createTable();
        assertNotNull(tbl.getCTTable());
        assertNotNull(tbl.getCTTable().getTblGrid());
        assertNotNull(tbl.getCTTable().getTblPr());
        assertTrue(tbl.getXmlObject() instanceof CTGraphicalObjectFrame);
        assertEquals("Table 1", tbl.getShapeName());
        assertEquals(2, tbl.getShapeId());
        assertEquals(0, tbl.getRows().size());
        assertEquals(0, tbl.getCTTable().sizeOfTrArray());
        assertEquals(0, tbl.getCTTable().getTblGrid().sizeOfGridColArray());

        assertEquals(0, tbl.getNumberOfColumns());
        assertEquals(0, tbl.getNumberOfRows());

        XSLFTableRow row0 = tbl.addRow();
        assertNotNull(row0.getXmlObject());
        assertEquals(1, tbl.getNumberOfRows());
        assertSame(row0, tbl.getRows().get(0));
        assertEquals(20.0, row0.getHeight());
        row0.setHeight(30.0);
        assertEquals(30.0, row0.getHeight());

        assertEquals(0, row0.getCells().size());
        XSLFTableCell cell0 = row0.addCell();
        assertNotNull(cell0.getXmlObject());
        // by default table cell has no borders
        assertTrue(cell0.getXmlObject().getTcPr().getLnB().isSetNoFill());
        assertTrue(cell0.getXmlObject().getTcPr().getLnT().isSetNoFill());
        assertTrue(cell0.getXmlObject().getTcPr().getLnL().isSetNoFill());
        assertTrue(cell0.getXmlObject().getTcPr().getLnR().isSetNoFill());

        assertSame(cell0, row0.getCells().get(0));
        assertEquals(1, tbl.getNumberOfColumns());
        assertEquals(100.0, tbl.getColumnWidth(0));
        cell0.addNewTextParagraph().addNewTextRun().setText("POI");
        assertEquals("POI", cell0.getText());

        XSLFTableCell cell1 = row0.addCell();
        assertSame(cell1, row0.getCells().get(1));
        assertEquals(2, tbl.getNumberOfColumns());
        assertEquals(100.0, tbl.getColumnWidth(1));
        cell1.addNewTextParagraph().addNewTextRun().setText("Apache");
        assertEquals("Apache", cell1.getText());

        assertEquals(1.0, cell1.getBorderBottom());
        cell1.setBorderBottom(2.0);
        assertEquals(2.0, cell1.getBorderBottom());
        assertNull(cell1.getBorderBottomColor());
        cell1.setBorderBottomColor(Color.yellow);
        assertEquals(Color.yellow, cell1.getBorderBottomColor());

        assertEquals(1.0, cell1.getBorderTop());
        cell1.setBorderTop(2.0);
        assertEquals(2.0, cell1.getBorderTop());
        assertNull(cell1.getBorderTopColor());
        cell1.setBorderTopColor(Color.yellow);
        assertEquals(Color.yellow, cell1.getBorderTopColor());

        assertEquals(1.0, cell1.getBorderLeft());
        cell1.setBorderLeft(2.0);
        assertEquals(2.0, cell1.getBorderLeft());
        assertNull(cell1.getBorderLeftColor());
        cell1.setBorderLeftColor(Color.yellow);
        assertEquals(Color.yellow, cell1.getBorderLeftColor());

        assertEquals(1.0, cell1.getBorderRight());
        cell1.setBorderRight(2.0);
        assertEquals(2.0, cell1.getBorderRight());
        assertNull(cell1.getBorderRightColor());
        cell1.setBorderRightColor(Color.yellow);
        assertEquals(Color.yellow, cell1.getBorderRightColor());
    }
}