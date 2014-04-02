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

import junit.framework.TestCase;

import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

public class TestXWPFTableCell extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

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
     * ensure that CTHMerge & CTTcBorders go in poi-ooxml.jar
     */
	public void test54099(){
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
}
