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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;


public class TestXSSFDialogSheet extends TestCase {
	

	public void testCreateDialogSheet() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", CTDialogsheet.Factory.newInstance());
        assertNotNull(dialogsheet);
	}
    
    public void testGetDialog() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(dialogsheet.getDialog());
    	
    }
	
	public void testAddRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", CTDialogsheet.Factory.newInstance());
        assertNotNull(dialogsheet);
        Row row = dialogsheet.createRow(0);
        assertNull(row);
	}
    
    public void testGetSetAutoBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.getAutobreaks());
        sheet.setAutobreaks(false);
        assertFalse(sheet.getAutobreaks());
    }
    
    public void testIsSetFitToPage() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getFitToPage());
        sheet.setFitToPage(true);
        assertTrue(sheet.getFitToPage());
        sheet.setFitToPage(false);
        assertFalse(sheet.getFitToPage());
    }
    
   
    public void testGetFooter() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertNotNull(sheet.getFooter());
        sheet.getFooter().setCenter("test center footer");
        assertEquals("test center footer", sheet.getFooter().getCenter());
    }
    
    public void testGetAllHeadersFooters() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertNotNull(sheet);
        assertNotNull(sheet.getOddFooter());
        assertNotNull(sheet.getEvenFooter());
        assertNotNull(sheet.getFirstFooter());
        assertNotNull(sheet.getOddHeader());
        assertNotNull(sheet.getEvenHeader());
        assertNotNull(sheet.getFirstHeader());
        
        assertEquals("", sheet.getOddFooter().getLeft());
        sheet.getOddFooter().setLeft("odd footer left");
        assertEquals("odd footer left", sheet.getOddFooter().getLeft());
        
        assertEquals("", sheet.getEvenFooter().getLeft());
        sheet.getEvenFooter().setLeft("even footer left");
        assertEquals("even footer left", sheet.getEvenFooter().getLeft());
        
        assertEquals("", sheet.getFirstFooter().getLeft());
        sheet.getFirstFooter().setLeft("first footer left");
        assertEquals("first footer left", sheet.getFirstFooter().getLeft());
        
        assertEquals("", sheet.getOddHeader().getLeft());
        sheet.getOddHeader().setLeft("odd header left");
        assertEquals("odd header left", sheet.getOddHeader().getLeft());
        
        assertEquals("", sheet.getOddHeader().getRight());
        sheet.getOddHeader().setRight("odd header right");
        assertEquals("odd header right", sheet.getOddHeader().getRight());
        
        assertEquals("", sheet.getOddHeader().getCenter());
        sheet.getOddHeader().setCenter("odd header center");
        assertEquals("odd header center", sheet.getOddHeader().getCenter());

    }
    
    public void testGetSetHorizontallyCentered() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(true);
        assertTrue(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(false);
        assertFalse(sheet.getHorizontallyCenter());
    }
    
    public void testGetSetVerticallyCentered() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(true);
        assertTrue(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(false);
        assertFalse(sheet.getVerticallyCenter());
    }
    
    public void testIsSetPrintGridlines() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.isPrintGridlines());
        sheet.setPrintGridlines(true);
        assertTrue(sheet.isPrintGridlines());
    }
    
    public void testIsSetDisplayFormulas() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.isDisplayFormulas());
        sheet.setDisplayFormulas(true);
        assertTrue(sheet.isDisplayFormulas());
    }
    
    public void testIsSetDisplayGridLines() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.isDisplayGridlines());
        sheet.setDisplayGridlines(false);
        assertFalse(sheet.isDisplayGridlines());
    }
    
    public void testIsSetDisplayRowColHeadings() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.isDisplayRowColHeadings());
        sheet.setDisplayRowColHeadings(false);
        assertFalse(sheet.isDisplayRowColHeadings());
    }
    
    public void testGetScenarioProtect() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getScenarioProtect());
    }
	
}
