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

import junit.framework.TestCase;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Tests for XWPF Run
 */
public class TestXWPFTable extends TestCase {

    protected void setUp() {
	/*
        XWPFDocument doc = new XWPFDocument();
        p = doc.createParagraph();

        this.ctRun = CTR.Factory.newInstance();
	 */
    }

    public void testConstructor() {
	CTTbl ctTable=CTTbl.Factory.newInstance();
	XWPFTable xtab=new XWPFTable(null, ctTable);
	assertNotNull(xtab);
	assertEquals(1,ctTable.sizeOfTrArray());
	assertEquals(1,ctTable.getTrArray(0).sizeOfTcArray());
	assertNotNull(ctTable.getTrArray(0).getTcArray(0).getPArray(0));
	
	ctTable=CTTbl.Factory.newInstance();
	xtab=new XWPFTable(null, ctTable, 3,2);
	assertNotNull(xtab);
	assertEquals(3,ctTable.sizeOfTrArray());
	assertEquals(2,ctTable.getTrArray(0).sizeOfTcArray());
	assertNotNull(ctTable.getTrArray(0).getTcArray(0).getPArray(0));
    }        
    
    
    public void testGetText(){
	CTTbl table = CTTbl.Factory.newInstance();
	CTRow row=table.addNewTr();
	CTTc cell=row.addNewTc();
	CTP paragraph=cell.addNewP();
	CTR run=paragraph.addNewR();
	CTText text=run.addNewT();
	text.setStringValue("finally I can write!");
	
	XWPFTable xtab=new XWPFTable(null, table);
    	assertEquals("finally I can write!\n",xtab.getText());
    }
    
    
    public void testCreateRow(){
	CTTbl table = CTTbl.Factory.newInstance();
	CTRow r1=table.addNewTr();
	r1.addNewTc().addNewP();
	r1.addNewTc().addNewP();
	CTRow r2=table.addNewTr();
	r2.addNewTc().addNewP();
	r2.addNewTc().addNewP();
	CTRow r3=table.addNewTr();
	r3.addNewTc().addNewP();
	r3.addNewTc().addNewP();
	
	XWPFTable xtab=new XWPFTable(null, table);
    	assertEquals(3,xtab.getNumberOfRows());
    	assertNotNull(xtab.getRow(2));
    	
    	//add a new row
    	xtab.createRow();
    	
    	//check number of cols
    	assertEquals(2,table.getTrArray(0).sizeOfTcArray());
    	    	
    	//check creation of first row
	xtab=new XWPFTable(null, CTTbl.Factory.newInstance());
    	assertEquals(1,xtab.getCTTbl().getTrArray(0).sizeOfTcArray());
    }
    
    
    public void testSetGetWidth(){
	CTTbl table = CTTbl.Factory.newInstance();
	table.addNewTblPr().addNewTblW().setW(new BigInteger("1000"));
	
	XWPFTable xtab=new XWPFTable(null, table);
	
	assertEquals(1000,xtab.getWidth());
	
	xtab.setWidth(100);
	assertEquals(100,table.getTblPr().getTblW().getW().intValue());
    }
    
    

}