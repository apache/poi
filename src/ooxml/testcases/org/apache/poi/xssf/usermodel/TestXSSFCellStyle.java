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

package org.apache.poi.xssf.usermodel;

import junit.framework.TestCase;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;


public class TestXSSFCellStyle extends TestCase {
	
	private CTStylesheet ctStylesheet;
	private CTBorder ctBorder;
	private CTXf cellStyleXf;
	private CTXf cellXf;
	private XSSFCellStyle cellStyle;

	public void setUp() {
		ctStylesheet = CTStylesheet.Factory.newInstance();
		ctBorder = ctStylesheet.addNewBorders().insertNewBorder(0);
		cellStyleXf = ctStylesheet.addNewCellStyleXfs().addNewXf();
		cellStyleXf.setBorderId(0);
		cellXf = ctStylesheet.addNewCellXfs().addNewXf();
		cellXf.setXfId(0);
		cellStyle = new XSSFCellStyle(ctStylesheet, 0);
	}
	
	public void testGetBorderBottom() {		
		ctBorder.addNewBottom().setStyle(STBorderStyle.THIN);
		assertEquals((short)1, cellStyle.getBorderBottom());
	}

	public void testGetBorderBottomAsString() {
		ctBorder.addNewBottom().setStyle(STBorderStyle.THIN);
		assertEquals("thin", cellStyle.getBorderBottomAsString());
	}
	
	public void testGetBorderRight() {
		ctBorder.addNewRight().setStyle(STBorderStyle.MEDIUM);
		assertEquals((short)2, cellStyle.getBorderRight());
	}

	public void testGetBorderRightAsString() {
		ctBorder.addNewRight().setStyle(STBorderStyle.MEDIUM);
		assertEquals("medium", cellStyle.getBorderRightAsString());
	}
	
	public void testGetBorderLeft() {
		ctBorder.addNewLeft().setStyle(STBorderStyle.DASHED);
		assertEquals((short)3, cellStyle.getBorderLeft());
	}

	public void testGetBorderLeftAsString() {
		ctBorder.addNewLeft().setStyle(STBorderStyle.DASHED);
		assertEquals("dashed", cellStyle.getBorderLeftAsString());
	}
	
	public void testGetBorderTop() {
		ctBorder.addNewTop().setStyle(STBorderStyle.HAIR);
		assertEquals((short)7, cellStyle.getBorderTop());
	}

	public void testGetTopBottomAsString() {
		ctBorder.addNewTop().setStyle(STBorderStyle.HAIR);
		assertEquals("hair", cellStyle.getBorderTopAsString());
	}
}
