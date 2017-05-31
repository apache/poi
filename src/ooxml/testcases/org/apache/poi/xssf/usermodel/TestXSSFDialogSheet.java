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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;


public class TestXSSFDialogSheet {

    @Test
	public void testCreateDialogSheet() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", CTDialogsheet.Factory.newInstance());
        assertNotNull(dialogsheet);
        workbook.close();
	}

    @Test
    public void testGetDialog() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(dialogsheet.getDialog());
        workbook.close();
    }

    @Test
	public void testAddRow() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", CTDialogsheet.Factory.newInstance());
        assertNotNull(dialogsheet);
        Row row = dialogsheet.createRow(0);
        assertNull(row);
        workbook.close();
	}

    @Test
    public void testGetSetAutoBreaks() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.getAutobreaks());
        sheet.setAutobreaks(false);
        assertFalse(sheet.getAutobreaks());
        workbook.close();
    }

    @Test
    public void testIsSetFitToPage() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getFitToPage());
        sheet.setFitToPage(true);
        assertTrue(sheet.getFitToPage());
        sheet.setFitToPage(false);
        assertFalse(sheet.getFitToPage());
        workbook.close();
    }


    @Test
    public void testGetFooter() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertNotNull(sheet.getFooter());
        sheet.getFooter().setCenter("test center footer");
        assertEquals("test center footer", sheet.getFooter().getCenter());
        workbook.close();
    }

    @Test
    public void testGetAllHeadersFooters() throws IOException {
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
        workbook.close();
    }

    @Test
    public void testGetSetHorizontallyCentered() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(true);
        assertTrue(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(false);
        assertFalse(sheet.getHorizontallyCenter());
        workbook.close();
    }

    @Test
    public void testGetSetVerticallyCentered() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(true);
        assertTrue(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(false);
        assertFalse(sheet.getVerticallyCenter());
        workbook.close();
    }

    @Test
    public void testIsSetPrintGridlines() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.isPrintGridlines());
        sheet.setPrintGridlines(true);
        assertTrue(sheet.isPrintGridlines());
        workbook.close();
    }

    @Test
    public void testIsSetDisplayFormulas() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.isDisplayFormulas());
        sheet.setDisplayFormulas(true);
        assertTrue(sheet.isDisplayFormulas());
        workbook.close();
    }

    @Test
    public void testIsSetDisplayGridLines() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.isDisplayGridlines());
        sheet.setDisplayGridlines(false);
        assertFalse(sheet.isDisplayGridlines());
        workbook.close();
    }

    @Test
    public void testIsSetDisplayRowColHeadings() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(sheet.isDisplayRowColHeadings());
        sheet.setDisplayRowColHeadings(false);
        assertFalse(sheet.isDisplayRowColHeadings());
        workbook.close();
    }

    @Test
    public void testGetScenarioProtect() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFDialogsheet sheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertFalse(sheet.getScenarioProtect());
        workbook.close();
    }

}
