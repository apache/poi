/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xwpf.usermodel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.xmlbeans.XmlCursor;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

class TestXWPFTableCell {

    @Test
    void testSetGetVertAlignment() {
        // instantiate the following classes so they'll get picked up by
        // the XmlBean process and added to the jar file. they are required
        // for the following XWPFTableCell methods.
        CTShd ctShd = CTShd.Factory.newInstance();
        assertNotNull(ctShd);
        CTVerticalJc ctVjc = CTVerticalJc.Factory.newInstance();
        assertNotNull(ctVjc);
        STShd stShd = STShd.Factory.newInstance();
        assertNotNull(stShd);
        STVerticalJc stVjc = STVerticalJc.Factory.newInstance();
        assertNotNull(stVjc);

        // create a table
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        // table has a single row by default; grab it
        XWPFTableRow tr = table.getRow(0);
        assertNotNull(tr);
        // row has a single cell by default; grab it
        XWPFTableCell cell = tr.getCell(0);

        cell.setVerticalAlignment(XWPFVertAlign.BOTH);
        XWPFVertAlign al = cell.getVerticalAlignment();
        assertEquals(XWPFVertAlign.BOTH, al);
    }

    @Test
    void testSetGetColor() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        // table has a single row by default; grab it
        XWPFTableRow tr = table.getRow(0);
        assertNotNull(tr);
        // row has a single cell by default; grab it
        XWPFTableCell cell = tr.getCell(0);

        cell.setColor("F0000F");
        String clr = cell.getColor();
        assertEquals("F0000F", clr);
    }

    /**
     * ensure that CTHMerge and CTTcBorders go in poi-ooxml.jar
     */
    @Test
    void test54099() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        XWPFTableRow tr = table.getRow(0);
        XWPFTableCell cell = tr.getCell(0);

        CTTc ctTc = cell.getCTTc();
        CTTcPr tcPr = ctTc.addNewTcPr();
        CTHMerge hMerge = tcPr.addNewHMerge();
        hMerge.setVal(STMerge.RESTART);

        CTTcBorders tblBorders = tcPr.addNewTcBorders();
        assertNotNull(tblBorders);
        CTVMerge vMerge = tcPr.addNewVMerge();
        assertNotNull(vMerge);
    }

    @Test
    void testCellVerticalAlign() throws Exception{
        try (XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("59030.docx")) {
            List<XWPFTable> tables = docx.getTables();
            assertEquals(1, tables.size());

            XWPFTable table = tables.get(0);

            List<XWPFTableRow> tableRows = table.getRows();
            assertEquals(2, tableRows.size());

            assertNull(tableRows.get(0).getCell(0).getVerticalAlignment());
            assertEquals(XWPFVertAlign.BOTTOM, tableRows.get(0).getCell(1).getVerticalAlignment());
            assertEquals(XWPFVertAlign.CENTER, tableRows.get(1).getCell(0).getVerticalAlignment());
            assertNull(tableRows.get(1).getCell(1).getVerticalAlignment()); // should return null since alignment isn't set
        }
    }

    @Test
    void testCellVerticalAlignShouldNotThrowNPE() throws Exception {
        try (XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("TestTableCellAlign.docx")) {
            String[] act =  docx.getTables().stream()
                .flatMap(t -> t.getRows().stream())
                .flatMap(r -> r.getTableICells().stream())
                .map(XWPFTableCell.class::cast)
                .map(XWPFTableCell::getVerticalAlignment)
                .map(e -> e == null ? null : e.name())
                .toArray(String[]::new);

            String[] exp = { null, "BOTTOM", "CENTER", null};
            assertArrayEquals(exp, act);
        }
    }

    @Test
    void testCellGetSetWidth() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable();
        XWPFTableRow tr = table.createRow();
        XWPFTableCell cell = tr.addNewTableCell();

        cell.setWidth("50%");
        assertEquals(TableWidthType.PCT, cell.getWidthType());
        assertEquals(50.0, cell.getWidthDecimal(), 0.0);
        assertEquals(2500, cell.getWidth());
        doc.close();
    }

    @Test
    void testAddParagraph() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable();
        XWPFTableRow tr = table.createRow();
        XWPFTableCell cell = tr.addNewTableCell();

        // cell have at least one paragraph by default
        assertEquals(1, cell.getParagraphs().size());
        assertEquals(1, cell.getBodyElements().size());
        assertEquals(cell.getParagraphArray(0), cell.getBodyElements().get(0));

        XWPFParagraph p = cell.addParagraph();
        assertEquals(2, cell.getParagraphs().size());
        assertEquals(2, cell.getBodyElements().size());
        assertEquals(p, cell.getParagraphArray(1));
        assertEquals(cell.getParagraphArray(1), cell.getBodyElements().get(1));

        doc.close();
    }

    @Test
    void testRemoveParagraph() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable();
        XWPFTableRow tr = table.createRow();
        XWPFTableCell cell = tr.addNewTableCell();

        // cell have at least one paragraph by default
        XWPFParagraph p0 = cell.getParagraphArray(0);
        XWPFParagraph p1 = cell.addParagraph();
        cell.addParagraph();

        // remove 3rd
        cell.removeParagraph(2);
        assertEquals(2, cell.getParagraphs().size());
        assertEquals(2, cell.getBodyElements().size());
        assertEquals(p0, cell.getParagraphArray(0));
        assertEquals(p1, cell.getParagraphArray(1));
        assertEquals(p0, cell.getBodyElements().get(0));
        assertEquals(p1, cell.getBodyElements().get(1));

        // remove 2nd
        cell.removeParagraph(1);
        assertEquals(1, cell.getParagraphs().size());
        assertEquals(1, cell.getBodyElements().size());
        assertEquals(p0, cell.getParagraphArray(0));
        assertEquals(p0, cell.getBodyElements().get(0));

        doc.close();
    }

    @Test
    void testRemoveTable() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable();
        XWPFTableRow tr = table.createRow();
        XWPFTableCell cell = tr.addNewTableCell();

         // cell have at least one paragraph by default
        XWPFParagraph p0 = cell.getParagraphArray(0);
        try (XmlCursor newCursor = p0.getCTP().newCursor()) {
            cell.insertNewTbl(newCursor);
        }

        assertEquals(1, cell.getTables().size());
        assertEquals(2, cell.getBodyElements().size());

        // remove table
        cell.removeTable(0);
        assertEquals(0, cell.getTables().size());
        assertEquals(1, cell.getBodyElements().size());
        assertEquals(p0, cell.getBodyElements().get(0));

        doc.close();
    }

    @Test
    void testBug63624() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(1, 1);
        XWPFTableRow row = table.getRow(0);
        XWPFTableCell cell = row.getCell(0);

        String expected = "this must not be empty";
        cell.setText(expected);
        String actual = cell.getText();
        assertEquals(expected, actual);
        doc.close();
    }

    @Test
    void test63624() {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(1, 1);
        XWPFTableRow row = table.getRow(0);
        XWPFTableCell cell = row.getCell(0);

        cell.setText("test text 1");
        assertEquals("test text 1", cell.getText());

        cell.appendText("test text 2");
        assertEquals("test text 1test text 2", cell.getText());
    }

    @Test
    void test65292() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(1, 1);
        XWPFTableCell cell = table.getRow(0).getCell(0);

        // cell have at least one paragraph by default when creating a cell
        assertEquals(1, cell.getParagraphs().size());

        cell.removeParagraph(0);
        assertEquals(0, cell.getParagraphs().size());

        // not add a paragraph when loading an existing empty table cell
        XWPFDocument readDoc = XWPFTestDataSamples.writeOutAndReadBack(doc);
        XWPFTableCell readCell = readDoc.getTableArray(0).getRow(0).getCell(0);
        assertEquals(0, readCell.getParagraphs().size());
    }

    @Test
    void bug66988() throws IOException {
        try (XWPFDocument document = XWPFTestDataSamples.openSampleDocument("Bug66988.docx")) {
            XWPFTableCell cell = document.getTableArray(0).getRow(0).getCell(0);
            cell.appendText("World");
            assertEquals("HelloWorld", cell.getText());
            cell.setText("FooBar");
            assertEquals("FooBar", cell.getText());
        }
    }
}
