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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.apache.poi.ss.usermodel.PaperSize;
import org.apache.poi.ss.usermodel.PageOrder;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.PrintCellComments;

/**
 * Tests for {@link XSSFPrintSetup}
 */
public class TestXSSFPrintSetup extends TestCase {


    public void testSetGetPaperSize() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setPaperSize(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(PaperSize.A4_PAPER, printSetup.getPaperSizeEnum());
        assertEquals(9, printSetup.getPaperSize());

        printSetup.setPaperSize(PaperSize.A3_PAPER);
        assertEquals(8, pSetup.getPaperSize());
    }


    public void testSetGetScale() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setScale(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getScale());

        printSetup.setScale((short) 100);
        assertEquals(100, pSetup.getScale());
    }

    public void testSetGetPageStart() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setFirstPageNumber(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getPageStart());

        printSetup.setPageStart((short) 1);
        assertEquals(1, pSetup.getFirstPageNumber());
    }


    public void testSetGetFitWidthHeight() {
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

    public void testSetGetLeftToRight() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setPageOrder(STPageOrder.DOWN_THEN_OVER);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getLeftToRight());

        printSetup.setLeftToRight(true);
        assertEquals(PageOrder.OVER_THEN_DOWN.getValue(), pSetup.getPageOrder().intValue());
    }

    public void testSetGetOrientation() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setOrientation(STOrientation.PORTRAIT);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(PrintOrientation.PORTRAIT, printSetup.getOrientation());
        assertEquals(false, printSetup.getLandscape());
        assertEquals(false, printSetup.getNoOrientation());

        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
        assertEquals(pSetup.getOrientation().intValue(), printSetup.getOrientation().getValue());
        assertEquals(true, printSetup.getLandscape());
        assertEquals(false, printSetup.getNoOrientation());
    }


    public void testSetGetValidSettings() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setUsePrinterDefaults(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getValidSettings());

        printSetup.setValidSettings(true);
        assertEquals(true, pSetup.getUsePrinterDefaults());
    }

    public void testSetGetNoColor() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setBlackAndWhite(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getNoColor());

        printSetup.setNoColor(true);
        assertEquals(true, pSetup.getBlackAndWhite());
    }

    public void testSetGetDraft() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setDraft(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getDraft());

        printSetup.setDraft(true);
        assertEquals(true, pSetup.getDraft());
    }

    public void testSetGetNotes() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setCellComments(STCellComments.NONE);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getNotes());

        printSetup.setNotes(true);
        assertEquals(PrintCellComments.AS_DISPLAYED.getValue(), pSetup.getCellComments().intValue());
    }


    public void testSetGetUsePage() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setUseFirstPageNumber(false);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(false, printSetup.getUsePage());

        printSetup.setUsePage(true);
        assertEquals(true, pSetup.getUseFirstPageNumber());
    }

    public void testSetGetHVResolution() {
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

    public void testSetGetHeaderFooterMargin() {
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

    public void testSetGetCopies() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTPageSetup pSetup = worksheet.addNewPageSetup();
        pSetup.setCopies(9);
        XSSFPrintSetup printSetup = new XSSFPrintSetup(worksheet);
        assertEquals(9, printSetup.getCopies());

        printSetup.setCopies((short) 15);
        assertEquals(15, pSetup.getCopies());
    }

}
