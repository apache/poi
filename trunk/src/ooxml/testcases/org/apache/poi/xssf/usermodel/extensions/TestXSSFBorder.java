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

package org.apache.poi.xssf.usermodel.extensions;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;


public class TestXSSFBorder extends TestCase {
	
	public void testGetBorderStyle() {
		CTStylesheet stylesheet = CTStylesheet.Factory.newInstance();
		CTBorder border = stylesheet.addNewBorders().addNewBorder();
		CTBorderPr top = border.addNewTop();
		CTBorderPr right = border.addNewRight();
		CTBorderPr bottom = border.addNewBottom();
		
		top.setStyle(STBorderStyle.DASH_DOT);
		right.setStyle(STBorderStyle.NONE);
		bottom.setStyle(STBorderStyle.THIN);
		
		XSSFCellBorder cellBorderStyle = new XSSFCellBorder(border);
		assertEquals("DASH_DOT", cellBorderStyle.getBorderStyle(BorderSide.TOP).toString());
		
		assertEquals("NONE", cellBorderStyle.getBorderStyle(BorderSide.RIGHT).toString());
		assertEquals(BorderStyle.NONE.ordinal(), cellBorderStyle.getBorderStyle(BorderSide.RIGHT).ordinal());
		
		assertEquals("THIN", cellBorderStyle.getBorderStyle(BorderSide.BOTTOM).toString());
		
		assertEquals(BorderStyle.THIN.ordinal(), cellBorderStyle.getBorderStyle(BorderSide.BOTTOM).ordinal());
	}
	
}
