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

import org.apache.poi.ss.usermodel.PageOrder;
import org.apache.poi.ss.usermodel.PaperSize;
import org.apache.poi.ss.usermodel.PrintCellComments;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageSetup;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STOrientation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPageOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link XSSFPrintSetup}
 */
class TestXSSFPrintSetup {
    @Test
    void testSetGetPaperSize() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setPaperSize(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(PaperSize.A4_PAPER, printSetup.getPaperSizeEnum());
        assertEquals(9, printSetup.getPaperSize());

        printSetup.setPaperSize(PaperSize.A3_PAPER);
        assertEquals(8, pSetup.getPaperSize());
    }

    @Test
    void testSetGetScale() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setScale(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getScale());

        printSetup.setScale((short) 100);
        assertEquals(100, pSetup.getScale());
    }

    @Test
    void testSetGetPageStart() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setFirstPageNumber(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getPageStart());

        printSetup.setPageStart((short) 1);
        assertEquals(1, pSetup.getFirstPageNumber());
    }

    @Test
    void testSetGetFitWidthHeight() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setFitToWidth(50);
        pSetup.setFitToHeight(99);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(50, printSetup.getFitWidth());
        assertEquals(99, printSetup.getFitHeight());

        printSetup.setFitWidth((short) 66);
        printSetup.setFitHeight((short) 80);
        assertEquals(66, pSetup.getFitToWidth());
        assertEquals(80, pSetup.getFitToHeight());

    }

    @Test
    void testSetGetLeftToRight() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setPageOrder(STPageOrder.DOWN_THEN_OVER);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getLeftToRight());

        printSetup.setLeftToRight(true);
        assertEquals(PageOrder.OVER_THEN_DOWN.getValue(), pSetup.getPageOrder().intValue());
    }

    @Test
    void testSetGetOrientation() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setOrientation(STOrientation.PORTRAIT);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(PrintOrientation.PORTRAIT, printSetup.getOrientation());
        assertFalse(printSetup.getLandscape());
        assertFalse(printSetup.getNoOrientation());

        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
        assertEquals(pSetup.getOrientation().intValue(), printSetup.getOrientation().getValue());
        assertTrue(printSetup.getLandscape());
        assertFalse(printSetup.getNoOrientation());
    }

    @Test
    void testSetGetValidSettings() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setUsePrinterDefaults(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getValidSettings());

        printSetup.setValidSettings(true);
        assertTrue(pSetup.getUsePrinterDefaults());
    }

    @Test
    void testSetGetNoColor() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setBlackAndWhite(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getNoColor());

        printSetup.setNoColor(true);
        assertTrue(pSetup.getBlackAndWhite());
    }

    @Test
    void testSetGetDraft() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setDraft(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getDraft());

        printSetup.setDraft(true);
        assertTrue(pSetup.getDraft());
    }

    @Test
    void testSetGetNotes() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setCellComments(STCellComments.NONE);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getNotes());

        printSetup.setNotes(true);
        assertEquals(PrintCellComments.AS_DISPLAYED.getValue(), pSetup.getCellComments().intValue());
    }

    @Test
    void testSetGetUsePage() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setUseFirstPageNumber(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertFalse(printSetup.getUsePage());

        printSetup.setUsePage(true);
        assertTrue(pSetup.getUseFirstPageNumber());
    }

    @Test
    void testSetGetHVResolution() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setHorizontalDpi(120);
        pSetup.setVerticalDpi(100);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(120, printSetup.getHResolution());
        assertEquals(100, printSetup.getVResolution());

        printSetup.setHResolution((short) 150);
        printSetup.setVResolution((short) 130);
        assertEquals(150, pSetup.getHorizontalDpi());
        assertEquals(130, pSetup.getVerticalDpi());
    }

    @Test
    void testSetGetHeaderFooterMargin() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageMargins pMargins = worksheet.addNewPageMargins();
        pMargins.setHeader(1.5);
        pMargins.setFooter(2);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(1.5, printSetup.getHeaderMargin(), 0.0);
        assertEquals(2.0, printSetup.getFooterMargin(), 0.0);

        printSetup.setHeaderMargin(5);
        printSetup.setFooterMargin(3.5);
        assertEquals(5.0, pMargins.getHeader(), 0.0);
        assertEquals(3.5, pMargins.getFooter(), 0.0);
    }

    @Test
    void testSetGetMargins() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageMargins pMargins = worksheet.addNewPageMargins();
        pMargins.setTop(5.3);
        pMargins.setBottom(1.5);
        pMargins.setLeft(2);
        pMargins.setRight(3.2);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(5.3, printSetup.getTopMargin(), 0.0);
        assertEquals(1.5, printSetup.getBottomMargin(), 0.0);
        assertEquals(2, printSetup.getLeftMargin(), 0.0);
        assertEquals(3.2, printSetup.getRightMargin(), 0.0);

        printSetup.setTopMargin(9);
        printSetup.setBottomMargin(6.4);
        printSetup.setLeftMargin(7.8);
        printSetup.setRightMargin(8.1);
        assertEquals(9, pMargins.getTop(), 0.0);
        assertEquals(6.4, pMargins.getBottom(), 0.0);
        assertEquals(7.8, pMargins.getLeft(), 0.0);
        assertEquals(8.1, pMargins.getRight(), 0.0);
    }

    @Test
    void testSetGetCopies() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setCopies(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getCopies());

        printSetup.setCopies((short) 15);
        assertEquals(15, pSetup.getCopies());
    }

    @Test
    void testSetSaveRead() throws Exception {
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFSheet s1 = wb.createSheet();
        assertFalse(s1.getCTWorksheet().isSetPageSetup());
        assertTrue(s1.getCTWorksheet().isSetPageMargins());

       XSSFPrintSetup print = s1.getPrintSetup();
        assertTrue(s1.getCTWorksheet().isSetPageSetup());
        assertTrue(s1.getCTWorksheet().isSetPageMargins());

       print.setCopies((short)3);
       print.setLandscape(true);
       assertEquals(3, print.getCopies());
        assertTrue(print.getLandscape());

       XSSFSheet s2 = wb.createSheet();
        assertFalse(s2.getCTWorksheet().isSetPageSetup());
        assertTrue(s2.getCTWorksheet().isSetPageMargins());

       // Round trip and check
       XSSFWorkbook wbBack = XSSFITestDataProvider.instance.writeOutAndReadBack(wb);

       s1 = wbBack.getSheetAt(0);
       s2 = wbBack.getSheetAt(1);

        assertTrue(s1.getCTWorksheet().isSetPageSetup());
        assertTrue(s1.getCTWorksheet().isSetPageMargins());
        assertFalse(s2.getCTWorksheet().isSetPageSetup());
        assertTrue(s2.getCTWorksheet().isSetPageMargins());

       print = s1.getPrintSetup();
       assertEquals(3, print.getCopies());
        assertTrue(print.getLandscape());

       wb.close();
    }

    /**
     * Open a file with print settings, save and check.
     * Then, change, save, read, check
     */
    @Test
    void testRoundTrip() {
       // TODO
    }

    @Test
    void testSetLandscapeFalse() {
        XSSFPrintSetup ps = new XSSFPrintSetup(CTWorksheet.Factory.newInstance());

        assertFalse(ps.getLandscape());

        ps.setLandscape(true);
        assertTrue(ps.getLandscape());

        ps.setLandscape(false);
        assertFalse(ps.getLandscape());
    }

    @Test
    void testSetLeftToRight() {
        XSSFPrintSetup ps = new XSSFPrintSetup(CTWorksheet.Factory.newInstance());

        assertFalse(ps.getLeftToRight());

        ps.setLeftToRight(true);
        assertTrue(ps.getLeftToRight());

        ps.setLeftToRight(false);
        assertFalse(ps.getLeftToRight());
    }
}
