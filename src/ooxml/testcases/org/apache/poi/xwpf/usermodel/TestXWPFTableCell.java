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

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.List;

public class TestXWPFTableCell {

    @Test
    public void testSetGetVertAlignment() throws Exception {
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
    public void testSetGetColor() throws Exception {
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
    @SuppressWarnings("unused")
    @Test
    public void test54099() {
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
        CTVMerge vMerge = tcPr.addNewVMerge();
    }

    @Test
    public void testCellVerticalAlign() throws Exception{
        XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("59030.docx");
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

    // This is not a very useful test as written. It is not worth the execution time for a unit test
    @Ignore
    @Test
    public void testCellVerticalAlignShouldNotThrowNPE() throws Exception {
        XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("TestTableCellAlign.docx");
        List<XWPFTable> tables = docx.getTables();
        for (XWPFTable table : tables) {
            List<XWPFTableRow> tableRows = table.getRows();
            for (XWPFTableRow tableRow : tableRows) {
                List<XWPFTableCell> tableCells = tableRow.getTableCells();
                for (XWPFTableCell tableCell : tableCells) {
                    // getVerticalAlignment should return either an XWPFVertAlign enum or null if not set
                    tableCell.getVerticalAlignment();
                }
            }
        }
    }
    
    @Test
    public void testCellGetSetWidth() throws Exception {
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
}
