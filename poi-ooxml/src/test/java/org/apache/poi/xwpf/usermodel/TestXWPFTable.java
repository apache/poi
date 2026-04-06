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
package org.apache.poi.xwpf.usermodel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblCellMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJcTable;


/**
 * Tests for XWPF Tables
 */
class TestXWPFTable {

    @Test
    void testConstructor() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable xtab = new XWPFTable(ctTable, doc);
            assertNotNull(xtab);
            assertEquals(1, ctTable.sizeOfTrArray());
            assertEquals(1, ctTable.getTrArray(0).sizeOfTcArray());
            assertNotNull(ctTable.getTrArray(0).getTcArray(0).getPArray(0));

            ctTable = CTTbl.Factory.newInstance();
            xtab = new XWPFTable(ctTable, doc, 3, 2);
            assertNotNull(xtab);
            assertEquals(3, ctTable.sizeOfTrArray());
            assertEquals(2, ctTable.getTrArray(0).sizeOfTcArray());
            assertNotNull(ctTable.getTrArray(0).getTcArray(0).getPArray(0));
        }
    }

    @Test
    void testTblGrid() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            CTTblGrid cttblgrid = ctTable.addNewTblGrid();
            cttblgrid.addNewGridCol().setW(BigInteger.valueOf(123));
            cttblgrid.addNewGridCol().setW(BigInteger.valueOf(321));

            XWPFTable xtab = new XWPFTable(ctTable, doc);
            CTTblGridCol[] ca = xtab.getCTTbl().getTblGrid().getGridColArray();
            assertEquals("123", ca[0].getW().toString());
            assertEquals("321", ca[1].getW().toString());
        }
    }

    @Test
    void testGetText() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();
            CTRow row = table.addNewTr();
            CTTc cell = row.addNewTc();
            CTP paragraph = cell.addNewP();
            CTR run = paragraph.addNewR();
            CTText text = run.addNewT();
            text.setStringValue("finally I can write!");

            XWPFTable xtab = new XWPFTable(table, doc);
            assertEquals("finally I can write!\n", xtab.getText());
        }
    }

    @Test
    void testCreateRow() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            CTTbl table = CTTbl.Factory.newInstance();
            CTRow r1 = table.addNewTr();
            r1.addNewTc().addNewP();
            r1.addNewTc().addNewP();
            CTRow r2 = table.addNewTr();
            r2.addNewTc().addNewP();
            r2.addNewTc().addNewP();
            CTRow r3 = table.addNewTr();
            r3.addNewTc().addNewP();
            r3.addNewTc().addNewP();

            XWPFTable xtab = new XWPFTable(table, doc);
            assertEquals(3, xtab.getNumberOfRows());
            assertNotNull(xtab.getRow(2));

            //add a new row
            xtab.createRow();
            assertEquals(4, xtab.getNumberOfRows());

            //check number of cols
            assertEquals(2, table.getTrArray(0).sizeOfTcArray());

            //check creation of first row
            xtab = new XWPFTable(CTTbl.Factory.newInstance(), doc);
            assertEquals(1, xtab.getCTTbl().getTrArray(0).sizeOfTcArray());
        }
    }

    @Test
    void testSetGetWidth() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFTable xtab = doc.createTable();

            assertEquals(0, xtab.getWidth());
            assertEquals(TableWidthType.AUTO, xtab.getWidthType());

            xtab.setWidth(1000);
            assertEquals(TableWidthType.DXA, xtab.getWidthType());
            assertEquals(1000, xtab.getWidth());

            xtab.setWidth("auto");
            assertEquals(TableWidthType.AUTO, xtab.getWidthType());
            assertEquals(0, xtab.getWidth());
            assertEquals(0.0, xtab.getWidthDecimal(), 0.01);

            xtab.setWidth("999");
            assertEquals(TableWidthType.DXA, xtab.getWidthType());
            assertEquals(999, xtab.getWidth());

            xtab.setWidth("50.5%");
            assertEquals(TableWidthType.PCT, xtab.getWidthType());
            assertEquals(50.5, xtab.getWidthDecimal(), 0.01);

            // Test effect of setting width type to a new value

            // From PCT to NIL:
            xtab.setWidthType(TableWidthType.NIL);
            assertEquals(TableWidthType.NIL, xtab.getWidthType());
            assertEquals(0, xtab.getWidth());

            xtab.setWidth("999"); // Sets type to DXA
            assertEquals(TableWidthType.DXA, xtab.getWidthType());

            // From DXA to AUTO:
            xtab.setWidthType(TableWidthType.AUTO);
            assertEquals(TableWidthType.AUTO, xtab.getWidthType());
            assertEquals(0, xtab.getWidth());

            xtab.setWidthType(TableWidthType.PCT);
            assertEquals(TableWidthType.PCT, xtab.getWidthType());

            // From PCT to DXA:
            xtab.setWidth("33.3%");
            xtab.setWidthType(TableWidthType.DXA);
            assertEquals(TableWidthType.DXA, xtab.getWidthType());
            assertEquals(0, xtab.getWidth());

            // From DXA to DXA: (value should be unchanged)
            xtab.setWidth("999");
            xtab.setWidthType(TableWidthType.DXA);
            assertEquals(TableWidthType.DXA, xtab.getWidthType());
            assertEquals(999, xtab.getWidth());

            // From DXA to PCT:
            xtab.setWidthType(TableWidthType.PCT);
            assertEquals(TableWidthType.PCT, xtab.getWidthType());
            assertEquals(100.0, xtab.getWidthDecimal(), 0.0);
        }
    }

    @Test
    void testSetGetHeight() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            CTTbl table = CTTbl.Factory.newInstance();

            XWPFTable xtab = new XWPFTable(table, doc);
            XWPFTableRow row = xtab.createRow();
            row.setHeight(20);
            assertEquals(20, row.getHeight());
        }
    }

    @Test
    void testSetGetMargins() throws IOException {
        // instantiate the following class so it'll get picked up by
        // the XmlBean process and added to the jar file. it's required
        // for the following XWPFTable methods.
        CTTblCellMar ctm = CTTblCellMar.Factory.newInstance();
        assertNotNull(ctm);
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // set margins
            table.setCellMargins(50, 50, 250, 450);
            // get margin components
            int t = table.getCellMarginTop();
            assertEquals(50, t);
            int l = table.getCellMarginLeft();
            assertEquals(50, l);
            int b = table.getCellMarginBottom();
            assertEquals(250, b);
            int r = table.getCellMarginRight();
            assertEquals(450, r);
        }
    }

    @Test
    void testSetGetHBorders() throws IOException {
        // instantiate the following classes so they'll get picked up by
        // the XmlBean process and added to the jar file. they are required
        // for the following XWPFTable methods.
        CTTblBorders cttb = CTTblBorders.Factory.newInstance();
        assertNotNull(cttb);
        STBorder stb = STBorder.Factory.newInstance();
        assertNotNull(stb);
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getInsideHBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getInsideHBorderSize();
            assertEquals(-1, sz);
            int sp = table.getInsideHBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getInsideHBorderColor();
            assertNull(clr);
            // set inside horizontal border
            table.setInsideHBorder(XWPFBorderType.SINGLE, 4, 0, "FF0000");
            // get inside horizontal border components
            bt = table.getInsideHBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            sz = table.getInsideHBorderSize();
            assertEquals(4, sz);
            sp = table.getInsideHBorderSpace();
            assertEquals(0, sp);
            clr = table.getInsideHBorderColor();
            assertEquals("FF0000", clr);
            // remove the border and verify state
            table.removeInsideHBorder();
            bt = table.getInsideHBorderType();
            assertNull(bt);
            sz = table.getInsideHBorderSize();
            assertEquals(-1, sz);
            sp = table.getInsideHBorderSpace();
            assertEquals(-1, sp);
            clr = table.getInsideHBorderColor();
            assertNull(clr);
            // check other borders
            bt = table.getInsideVBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getTopBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getBottomBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getLeftBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getRightBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            // remove the rest all at once and test
            table.removeBorders();
            bt = table.getInsideVBorderType();
            assertNull(bt);
            bt = table.getTopBorderType();
            assertNull(bt);
            bt = table.getBottomBorderType();
            assertNull(bt);
            bt = table.getLeftBorderType();
            assertNull(bt);
            bt = table.getRightBorderType();
            assertNull(bt);
        }
    }

    @Test
    void testSetGetVBorders() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getInsideVBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getInsideVBorderSize();
            assertEquals(-1, sz);
            int sp = table.getInsideVBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getInsideVBorderColor();
            assertNull(clr);
            // set inside vertical border
            table.setInsideVBorder(XWPFBorderType.DOUBLE, 4, 0, "00FF00");
            // get inside vertical border components
            bt = table.getInsideVBorderType();
            assertEquals(XWPFBorderType.DOUBLE, bt);
            sz = table.getInsideVBorderSize();
            assertEquals(4, sz);
            sp = table.getInsideVBorderSpace();
            assertEquals(0, sp);
            clr = table.getInsideVBorderColor();
            assertEquals("00FF00", clr);
            // remove the border and verify state
            table.removeInsideVBorder();
            bt = table.getInsideVBorderType();
            assertNull(bt);
            sz = table.getInsideVBorderSize();
            assertEquals(-1, sz);
            sp = table.getInsideVBorderSpace();
            assertEquals(-1, sp);
            clr = table.getInsideVBorderColor();
            assertNull(clr);
            // check the rest
            bt = table.getInsideHBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getTopBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getBottomBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getLeftBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            bt = table.getRightBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            // remove the rest one at a time and test
            table.removeInsideHBorder();
            table.removeTopBorder();
            table.removeBottomBorder();
            table.removeLeftBorder();
            table.removeRightBorder();
            bt = table.getInsideHBorderType();
            assertNull(bt);
            bt = table.getTopBorderType();
            assertNull(bt);
            bt = table.getBottomBorderType();
            assertNull(bt);
            bt = table.getLeftBorderType();
            assertNull(bt);
            bt = table.getRightBorderType();
            assertNull(bt);
        }
    }

    @Test
    void testSetGetTopBorders() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getTopBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getTopBorderSize();
            assertEquals(-1, sz);
            int sp = table.getTopBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getTopBorderColor();
            assertNull(clr);
            // set top border
            table.setTopBorder(XWPFBorderType.THICK, 4, 0, "00FF00");
            // get inside vertical border components
            bt = table.getTopBorderType();
            assertEquals(XWPFBorderType.THICK, bt);
            sz = table.getTopBorderSize();
            assertEquals(4, sz);
            sp = table.getTopBorderSpace();
            assertEquals(0, sp);
            clr = table.getTopBorderColor();
            assertEquals("00FF00", clr);
            // remove the border and verify state
            table.removeTopBorder();
            bt = table.getTopBorderType();
            assertNull(bt);
            sz = table.getTopBorderSize();
            assertEquals(-1, sz);
            sp = table.getTopBorderSpace();
            assertEquals(-1, sp);
            clr = table.getTopBorderColor();
            assertNull(clr);
        }
    }

    @Test
    void testSetGetBottomBorders() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getBottomBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getBottomBorderSize();
            assertEquals(-1, sz);
            int sp = table.getBottomBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getBottomBorderColor();
            assertNull(clr);
            // set inside vertical border
            table.setBottomBorder(XWPFBorderType.DOTTED, 4, 0, "00FF00");
            // get inside vertical border components
            bt = table.getBottomBorderType();
            assertEquals(XWPFBorderType.DOTTED, bt);
            sz = table.getBottomBorderSize();
            assertEquals(4, sz);
            sp = table.getBottomBorderSpace();
            assertEquals(0, sp);
            clr = table.getBottomBorderColor();
            assertEquals("00FF00", clr);
            // remove the border and verify state
            table.removeBottomBorder();
            bt = table.getBottomBorderType();
            assertNull(bt);
            sz = table.getBottomBorderSize();
            assertEquals(-1, sz);
            sp = table.getBottomBorderSpace();
            assertEquals(-1, sp);
            clr = table.getBottomBorderColor();
            assertNull(clr);
        }
    }

    @Test
    void testSetGetLeftBorders() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getLeftBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getLeftBorderSize();
            assertEquals(-1, sz);
            int sp = table.getLeftBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getLeftBorderColor();
            assertNull(clr);
            // set inside vertical border
            table.setLeftBorder(XWPFBorderType.DASHED, 4, 0, "00FF00");
            // get inside vertical border components
            bt = table.getLeftBorderType();
            assertEquals(XWPFBorderType.DASHED, bt);
            sz = table.getLeftBorderSize();
            assertEquals(4, sz);
            sp = table.getLeftBorderSpace();
            assertEquals(0, sp);
            clr = table.getLeftBorderColor();
            assertEquals("00FF00", clr);
            // remove the border and verify state
            table.removeLeftBorder();
            bt = table.getLeftBorderType();
            assertNull(bt);
            sz = table.getLeftBorderSize();
            assertEquals(-1, sz);
            sp = table.getLeftBorderSpace();
            assertEquals(-1, sp);
            clr = table.getLeftBorderColor();
            assertNull(clr);
        }
    }

    @Test
    void testSetGetRightBorders() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            // check initial state
            XWPFBorderType bt = table.getRightBorderType();
            assertEquals(XWPFBorderType.SINGLE, bt);
            int sz = table.getRightBorderSize();
            assertEquals(-1, sz);
            int sp = table.getRightBorderSpace();
            assertEquals(-1, sp);
            String clr = table.getRightBorderColor();
            assertNull(clr);
            // set inside vertical border
            table.setRightBorder(XWPFBorderType.DOT_DASH, 4, 0, "00FF00");
            // get inside vertical border components
            bt = table.getRightBorderType();
            assertEquals(XWPFBorderType.DOT_DASH, bt);
            sz = table.getRightBorderSize();
            assertEquals(4, sz);
            sp = table.getRightBorderSpace();
            assertEquals(0, sp);
            clr = table.getRightBorderColor();
            assertEquals("00FF00", clr);
            // remove the border and verify state
            table.removeRightBorder();
            bt = table.getRightBorderType();
            assertNull(bt);
            sz = table.getRightBorderSize();
            assertEquals(-1, sz);
            sp = table.getRightBorderSpace();
            assertEquals(-1, sp);
            clr = table.getRightBorderColor();
            assertNull(clr);
        }
    }

    @Test
    void testSetGetRowBandSize() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            table.setRowBandSize(12);
            int sz = table.getRowBandSize();
            assertEquals(12, sz);
        }
    }

    @Test
    void testSetGetColBandSize() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl ctTable = CTTbl.Factory.newInstance();
            XWPFTable table = new XWPFTable(ctTable, doc);
            table.setColBandSize(16);
            int sz = table.getColBandSize();
            assertEquals(16, sz);
        }
    }

    @Test
    public void testCreateTable() throws Exception {
        // open an empty document
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {

            // create a table with 5 rows and 7 columns
            int noRows = 5;
            int noCols = 7;
            XWPFTable table = doc.createTable(noRows, noCols);

            // assert the table is empty
            List<XWPFTableRow> rows = table.getRows();
            assertEquals(noRows, rows.size(), "Table has less rows than requested.");
            for (XWPFTableRow xwpfRow : rows) {
                assertNotNull(xwpfRow);
                for (int i = 0; i < 7; i++) {
                    XWPFTableCell xwpfCell = xwpfRow.getCell(i);
                    assertNotNull(xwpfCell);
                    assertEquals(1, xwpfCell.getParagraphs().size(), "Empty cells should not have one paragraph.");
                    xwpfCell = xwpfRow.getCell(i);
                    assertEquals(1, xwpfCell.getParagraphs().size(), "Calling 'getCell' must not modify cells content.");
                }
            }
            doc.getPackage().revert();
        }
    }

    @Test
    void testSetGetTableAlignment() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable tbl = doc.createTable(1, 1);
            tbl.setTableAlignment(TableRowAlign.START);
            assertEquals(TableRowAlign.START, tbl.getTableAlignment());
            assertEquals(STJcTable.INT_START, tbl.getTableAlignment().getValue());
            tbl.setTableAlignment(TableRowAlign.LEFT);
            assertEquals(TableRowAlign.LEFT, tbl.getTableAlignment());
            assertEquals(STJcTable.INT_LEFT, tbl.getTableAlignment().getValue());
            tbl.setTableAlignment(TableRowAlign.CENTER);
            assertEquals(TableRowAlign.CENTER, tbl.getTableAlignment());
            assertEquals(STJcTable.INT_CENTER, tbl.getTableAlignment().getValue());
            tbl.setTableAlignment(TableRowAlign.RIGHT);
            assertEquals(TableRowAlign.RIGHT, tbl.getTableAlignment());
            assertEquals(STJcTable.INT_RIGHT, tbl.getTableAlignment().getValue());
            tbl.setTableAlignment(TableRowAlign.END);
            assertEquals(TableRowAlign.END, tbl.getTableAlignment());
            assertEquals(STJcTable.INT_END, tbl.getTableAlignment().getValue());
            tbl.removeTableAlignment();
            assertNull(tbl.getTableAlignment());
        }
    }

    @Test
    public void testGetTableIndent() throws Exception {
        // open an empty document
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("table-indent.docx")) {

            XWPFTable table1 = doc.getTableArray(0);
            // Indent not present in the document
            assertFalse(table1.hasIndent());
            assertEquals(0, table1.getIndent());

            XWPFTable table2 = doc.getTableArray(1);
            // Valid indent value with type dxa
            assertTrue(table2.hasIndent());
            assertEquals(732, table2.getIndent());

            XWPFTable table3 = doc.getTableArray(2);
            // Indent is of type "nil"
            assertTrue(table3.hasIndent());
            assertEquals(0, table3.getIndent());

            XWPFTable table4 = doc.getTableArray(3);
            // Indent is of type "pct" which should be ignored
            assertFalse(table4.hasIndent());
            assertEquals(0, table4.getIndent());

            XWPFTable table5 = doc.getTableArray(4);
            // Indent is of type "auto" which should be ignored
            assertFalse(table5.hasIndent());
            assertEquals(0, table5.getIndent());

            XWPFTable table6 = doc.getTableArray(5);
            // Valid indent value with empty type (defaults to dxa)
            assertTrue(table6.hasIndent());
            assertEquals(732, table6.getIndent());

            XWPFTable table7 = doc.getTableArray(6);
            // Valid indent value, negative values are allowed
            assertTrue(table7.hasIndent());
            assertEquals(-500, table7.getIndent());
        }
    }

    @Test
    void testSetGetTableIndent() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable tbl = doc.createTable(1, 1);
            assertFalse(tbl.hasIndent());
            tbl.setIndent(100);
            assertTrue(tbl.hasIndent());
            assertEquals(100, tbl.getIndent());
            tbl.setIndent(0);
            assertTrue(tbl.hasIndent());
            assertEquals(0, tbl.getIndent());
            tbl.setIndent(-100);
            assertTrue(tbl.hasIndent());
            assertEquals(-100, tbl.getIndent());
            tbl.removeIndent();
            assertFalse(tbl.hasIndent());
            assertEquals(0, tbl.getIndent());
        }
    }

    @Test
    public void testGetTableWidthIfNotPresent() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("table-indent.docx")) {
            // The first table in this document doesn't have a tblW item.
            XWPFTable table1 = doc.getTableArray(0);
            assertEquals(-1,table1.getWidth());
            assertEquals(TableWidthType.AUTO, table1.getWidthType());
        }
    }

    @Test
    void testTableWithSdtRow() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();

            CTRow normalRow = table.addNewTr();
            CTTc cell1 = normalRow.addNewTc();
            CTP p1 = cell1.addNewP();
            CTR r1 = p1.addNewR();
            r1.addNewT().setStringValue("Normal Row Cell 1");

            CTTc cell2 = normalRow.addNewTc();
            CTP p2 = cell2.addNewP();
            CTR r2 = p2.addNewR();
            r2.addNewT().setStringValue("Normal Row Cell 2");

            CTSdtRow sdtRow = table.addNewSdt();
            CTSdtContentRow sdtContent = sdtRow.addNewSdtContent();
            CTRow innerRow = sdtContent.addNewTr();

            CTTc sdtCell1 = innerRow.addNewTc();
            CTP sdtP1 = sdtCell1.addNewP();
            CTR sdtR1 = sdtP1.addNewR();
            sdtR1.addNewT().setStringValue("SDT Row Cell 1");

            CTTc sdtCell2 = innerRow.addNewTc();
            CTP sdtP2 = sdtCell2.addNewP();
            CTR sdtR2 = sdtP2.addNewR();
            sdtR2.addNewT().setStringValue("SDT Row Cell 2");

            XWPFTable xtab = new XWPFTable(table, doc);

            assertEquals(1, xtab.getNumberOfRows(), "Table should have 1 row at top level (SDT rows are not counted)");

            String text = xtab.getText();
            assertTrue(text.contains("Normal Row Cell 1"), "Text should contain normal row cell 1");
            assertTrue(text.contains("Normal Row Cell 2"), "Text should contain normal row cell 2");
            assertTrue(text.contains("SDT Row Cell 1"), "Text should contain SDT row cell 1");
            assertTrue(text.contains("SDT Row Cell 2"), "Text should contain SDT row cell 2");
        }
    }

    @Test
    void testTableWithNestedSdtRows() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();

            CTSdtRow outerSdtRow = table.addNewSdt();
            CTSdtContentRow outerContent = outerSdtRow.addNewSdtContent();

            CTSdtRow innerSdtRow = outerContent.addNewSdt();
            CTSdtContentRow innerContent = innerSdtRow.addNewSdtContent();
            CTRow row1 = innerContent.addNewTr();

            CTTc cell1 = row1.addNewTc();
            CTP p1 = cell1.addNewP();
            CTR r1 = p1.addNewR();
            r1.addNewT().setStringValue("Nested SDT Row");

            XWPFTable xtab = new XWPFTable(table, doc);

            String text = xtab.getText();
            assertTrue(text.contains("Nested SDT Row"), "Text should contain nested SDT row content");
        }
    }


    @Test
    void testTableWithOnlySdtRow() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();

            CTSdtRow sdtRow = table.addNewSdt();
            CTSdtContentRow sdtContent = sdtRow.addNewSdtContent();
            CTRow innerRow = sdtContent.addNewTr();

            CTTc cell = innerRow.addNewTc();
            CTP p = cell.addNewP();
            CTR r = p.addNewR();
            r.addNewT().setStringValue("Only SDT Row");

            XWPFTable xtab = new XWPFTable(table, doc);

            assertEquals(1, xtab.getNumberOfRows(), "Table has 1 row from createEmptyTable");
            assertEquals(2, xtab.getRows().size(), "Table should have 2 rows (1 empty + 1 SDT)");

            String text = xtab.getText();
            assertTrue(text.contains("Only SDT Row"), "Text should contain SDT row content");
        }
    }

    @Test
    void testTableWithMultipleSdtRows() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();

            // First SDT row
            CTSdtRow sdtRow1 = table.addNewSdt();
            CTSdtContentRow sdtContent1 = sdtRow1.addNewSdtContent();
            CTRow row1 = sdtContent1.addNewTr();
            CTTc cell1 = row1.addNewTc();
            CTP p1 = cell1.addNewP();
            CTR r1 = p1.addNewR();
            r1.addNewT().setStringValue("SDT Row 1");

            // Second SDT row
            CTSdtRow sdtRow2 = table.addNewSdt();
            CTSdtContentRow sdtContent2 = sdtRow2.addNewSdtContent();
            CTRow row2 = sdtContent2.addNewTr();
            CTTc cell2 = row2.addNewTc();
            CTP p2 = cell2.addNewP();
            CTR r2 = p2.addNewR();
            r2.addNewT().setStringValue("SDT Row 2");

            XWPFTable xtab = new XWPFTable(table, doc);

            assertEquals(1, xtab.getNumberOfRows(), "Table has 1 top-level row (empty + 2 SDTs)");
            assertEquals(3, xtab.getRows().size(), "Table should have 3 rows (empty + 2 SDTs)");

            String text = xtab.getText();
            assertTrue(text.contains("SDT Row 1"), "Text should contain first SDT row");
            assertTrue(text.contains("SDT Row 2"), "Text should contain second SDT row");
        }
    }

    @Test
    void testTableRowAccess() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            CTTbl table = CTTbl.Factory.newInstance();

            // Normal row
            CTRow normalRow = table.addNewTr();
            CTTc cell1 = normalRow.addNewTc();
            CTP p1 = cell1.addNewP();
            CTR r1 = p1.addNewR();
            r1.addNewT().setStringValue("Normal");

            // SDT row
            CTSdtRow sdtRow = table.addNewSdt();
            CTSdtContentRow sdtContent = sdtRow.addNewSdtContent();
            CTRow sdtInnerRow = sdtContent.addNewTr();
            CTTc cell2 = sdtInnerRow.addNewTc();
            CTP p2 = cell2.addNewP();
            CTR r2 = p2.addNewR();
            r2.addNewT().setStringValue("SDT");

            XWPFTable xtab = new XWPFTable(table, doc);

            // Note: getRow(int) uses ctTbl.sizeOfTrArray() for bounds check, which only counts top-level tr
            // So we use getRows() directly to access all rows including SDT rows
            List<XWPFTableRow> rows = xtab.getRows();

            // Row 0: normal row
            assertEquals("Normal", rows.get(0).getCell(0).getText().trim(), "Row 0 should be normal row");

            // Row 1: SDT row
            assertEquals("SDT", rows.get(1).getCell(0).getText().trim(), "Row 1 should be SDT row");

            XWPFTableRow row0 = xtab.getRow(0);
            assertNotNull(row0, "getRow(0) should work for top-level row");
            assertNull(xtab.getRow(1), "getRow(1) returns null due to bounds check limitation");
        }
    }
}
