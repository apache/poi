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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblCellMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;


/**
 * Tests for XWPF Tables
 */
public class TestXWPFTable {

    @Test
    public void testConstructor() {
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testTblGrid() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        CTTblGrid cttblgrid = ctTable.addNewTblGrid();
        cttblgrid.addNewGridCol().setW(new BigInteger("123"));
        cttblgrid.addNewGridCol().setW(new BigInteger("321"));

        XWPFTable xtab = new XWPFTable(ctTable, doc);
        assertEquals(123, xtab.getCTTbl().getTblGrid().getGridColArray(0).getW().intValue());
        assertEquals(321, xtab.getCTTbl().getTblGrid().getGridColArray(1).getW().intValue());
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testGetText() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl table = CTTbl.Factory.newInstance();
        CTRow row = table.addNewTr();
        CTTc cell = row.addNewTc();
        CTP paragraph = cell.addNewP();
        CTR run = paragraph.addNewR();
        CTText text = run.addNewT();
        text.setStringValue("finally I can write!");

        XWPFTable xtab = new XWPFTable(table, doc);
        assertEquals("finally I can write!\n", xtab.getText());
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testCreateRow() {
        XWPFDocument doc = new XWPFDocument();

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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetWidth() {
        XWPFDocument doc = new XWPFDocument();

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

        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetHeight() {
        XWPFDocument doc = new XWPFDocument();

        CTTbl table = CTTbl.Factory.newInstance();

        XWPFTable xtab = new XWPFTable(table, doc);
        XWPFTableRow row = xtab.createRow();
        row.setHeight(20);
        assertEquals(20, row.getHeight());
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetMargins() {
        // instantiate the following class so it'll get picked up by
        // the XmlBean process and added to the jar file. it's required
        // for the following XWPFTable methods.
        CTTblCellMar ctm = CTTblCellMar.Factory.newInstance();
        assertNotNull(ctm);
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetHBorders() {
        // instantiate the following classes so they'll get picked up by
        // the XmlBean process and added to the jar file. they are required
        // for the following XWPFTable methods.
        CTTblBorders cttb = CTTblBorders.Factory.newInstance();
        assertNotNull(cttb);
        STBorder stb = STBorder.Factory.newInstance();
        assertNotNull(stb);
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetVBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetTopBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetBottomBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetLeftBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetRightBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
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
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetRowBandSize() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        table.setRowBandSize(12);
        int sz = table.getRowBandSize();
        assertEquals(12, sz);
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testSetGetColBandSize() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        table.setColBandSize(16);
        int sz = table.getColBandSize();
        assertEquals(16, sz);
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }

    @Test
    public void testCreateTable() throws Exception {
        // open an empty document
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");

        // create a table with 5 rows and 7 columns
        int noRows = 5;
        int noCols = 7;
        XWPFTable table = doc.createTable(noRows, noCols);

        // assert the table is empty
        List<XWPFTableRow> rows = table.getRows();
        assertEquals("Table has less rows than requested.", noRows, rows.size());
        for (XWPFTableRow xwpfRow : rows) {
            assertNotNull(xwpfRow);
            for (int i = 0; i < 7; i++) {
                XWPFTableCell xwpfCell = xwpfRow.getCell(i);
                assertNotNull(xwpfCell);
                assertEquals("Empty cells should not have one paragraph.", 1, xwpfCell.getParagraphs().size());
                xwpfCell = xwpfRow.getCell(i);
                assertEquals("Calling 'getCell' must not modify cells content.", 1, xwpfCell.getParagraphs().size());
            }
        }
        doc.getPackage().revert();
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }
    
    @Test
    public void testSetGetTableAlignment() {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable tbl = doc.createTable(1, 1);
        tbl.setTableAlignment(TableRowAlign.LEFT);
        assertEquals(TableRowAlign.LEFT, tbl.getTableAlignment());
        tbl.setTableAlignment(TableRowAlign.CENTER);
        assertEquals(TableRowAlign.CENTER, tbl.getTableAlignment());
        tbl.setTableAlignment(TableRowAlign.RIGHT);
        assertEquals(TableRowAlign.RIGHT, tbl.getTableAlignment());
        tbl.removeTableAlignment();
        assertNull(tbl.getTableAlignment());
        try {
            doc.close();
        } catch (IOException e) {
            fail("Unable to close doc");
        }
    }
}