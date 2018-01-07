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

import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
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
public class TestXWPFTable extends TestCase {
    @Override
    protected void setUp() {
        /*
          XWPFDocument doc = new XWPFDocument();
          p = doc.createParagraph();

          this.ctRun = CTR.Factory.newInstance();
       */
    }

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
    }

    public void testTblGrid() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        CTTblGrid cttblgrid = ctTable.addNewTblGrid();
        cttblgrid.addNewGridCol().setW(new BigInteger("123"));
        cttblgrid.addNewGridCol().setW(new BigInteger("321"));

        XWPFTable xtab = new XWPFTable(ctTable, doc);
        assertEquals(123, xtab.getCTTbl().getTblGrid().getGridColArray(0).getW().intValue());
        assertEquals(321, xtab.getCTTbl().getTblGrid().getGridColArray(1).getW().intValue());
    }

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
    }


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
    }


    public void testSetGetWidth() {
        XWPFDocument doc = new XWPFDocument();

        CTTbl table = CTTbl.Factory.newInstance();
        table.addNewTblPr().addNewTblW().setW(new BigInteger("1000"));

        XWPFTable xtab = new XWPFTable(table, doc);

        assertEquals(1000, xtab.getWidth());

        xtab.setWidth(100);
        assertEquals(100, table.getTblPr().getTblW().getW().intValue());
    }

    public void testSetGetHeight() {
        XWPFDocument doc = new XWPFDocument();

        CTTbl table = CTTbl.Factory.newInstance();

        XWPFTable xtab = new XWPFTable(table, doc);
        XWPFTableRow row = xtab.createRow();
        row.setHeight(20);
        assertEquals(20, row.getHeight());
    }

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
    }

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
        // set inside horizontal border
        table.setInsideHBorder(XWPFBorderType.SINGLE, 4, 0, "FF0000");
        // get inside horizontal border components
        int s = table.getInsideHBorderSize();
        assertEquals(4, s);
        int sp = table.getInsideHBorderSpace();
        assertEquals(0, sp);
        String clr = table.getInsideHBorderColor();
        assertEquals("FF0000", clr);
        XWPFBorderType bt = table.getInsideHBorderType();
        assertEquals(XWPFBorderType.SINGLE, bt);
    }

    public void testSetGetVBorders() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        // set inside vertical border
        table.setInsideVBorder(XWPFBorderType.DOUBLE, 4, 0, "00FF00");
        // get inside vertical border components
        XWPFBorderType bt = table.getInsideVBorderType();
        assertEquals(XWPFBorderType.DOUBLE, bt);
        int sz = table.getInsideVBorderSize();
        assertEquals(4, sz);
        int sp = table.getInsideVBorderSpace();
        assertEquals(0, sp);
        String clr = table.getInsideVBorderColor();
        assertEquals("00FF00", clr);
    }

    public void testSetGetRowBandSize() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        table.setRowBandSize(12);
        int sz = table.getRowBandSize();
        assertEquals(12, sz);
    }

    public void testSetGetColBandSize() {
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        table.setColBandSize(16);
        int sz = table.getColBandSize();
        assertEquals(16, sz);
    }

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
    }
}