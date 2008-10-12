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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageSetup;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STOrientation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPageOrder;

/**
 * Tests for {@link XSSFPrintSetup}
 *
 */
public class TestXSSFPrintSetup extends TestCase {


    public void testConstructor(){
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	CTPageMargins pMargins=CTPageMargins.Factory.newInstance();
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, pMargins);
	assertNotNull(printSetup);

	XSSFWorkbook wb=new XSSFWorkbook();
	printSetup=new XSSFPrintSetup(wb.createSheet());
	assertNotNull(printSetup);
    }

    public void testSetGetPaperSize() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setPaperSize(9);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(PaperSize.A4_PAPER,printSetup.getPaperSizeEnum());
	assertEquals(9,printSetup.getPaperSize());

	printSetup.setPaperSize(PaperSize.A3_PAPER);
	assertEquals(8,pSetup.getPaperSize());
    }


    public void testSetGetScale() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setScale(9);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(9,printSetup.getScale());

	printSetup.setScale((short)100);
	assertEquals(100,pSetup.getScale());
    }

    public void testSetGetPageStart() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setFirstPageNumber(9);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(9,printSetup.getPageStart());

	printSetup.setPageStart((short)1);
	assertEquals(1,pSetup.getFirstPageNumber());
    }


    public void testSetGetFitWidthHeight() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setFitToWidth(50);
	pSetup.setFitToHeight(99);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(50,printSetup.getFitWidth());
	assertEquals(99,printSetup.getFitHeight());

	printSetup.setFitWidth((short)66);
	printSetup.setFitHeight((short)80);
	assertEquals(66,pSetup.getFitToWidth());
	assertEquals(80,pSetup.getFitToHeight());

    }

    public void testSetGetLeftToRight() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setPageOrder(STPageOrder.DOWN_THEN_OVER);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getLeftToRight());
	
	printSetup.setLeftToRight(true);
	assertEquals(PageOrder.OVER_THEN_DOWN.getValue(),pSetup.getPageOrder());
    }

    public void testSetGetOrientation() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setOrientation(STOrientation.PORTRAIT);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(PrintOrientation.PORTRAIT,printSetup.getOrientation());
	assertEquals(false,printSetup.getLandscape());
	assertEquals(false,printSetup.getNoOrientation());

	printSetup.setOrientation(PrintOrientation.LANDSCAPE);
	assertEquals(pSetup.getOrientation(),printSetup.getOrientation().getValue());
	assertEquals(true,printSetup.getLandscape());
	assertEquals(false,printSetup.getNoOrientation());
    }


    public void testSetGetValidSettings() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setUsePrinterDefaults(false);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getValidSettings());

	printSetup.setValidSettings(true);
	assertEquals(true,pSetup.getUsePrinterDefaults());
    }

    public void testSetGetNoColor() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setBlackAndWhite(false);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getNoColor());

	printSetup.setNoColor(true);
	assertEquals(true,pSetup.getBlackAndWhite());
    }

    public void testSetGetDraft() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setDraft(false);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getDraft());

	printSetup.setDraft(true);
	assertEquals(true,pSetup.getDraft());
    }

    public void testSetGetNotes() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setCellComments(STCellComments.NONE);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getNotes());

	printSetup.setNotes(true);
	assertEquals(PrintCellComments.AS_DISPLAYED.getValue(),pSetup.getCellComments());
    }


    public void testSetGetUsePage() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setUseFirstPageNumber(false);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(false,printSetup.getUsePage());

	printSetup.setUsePage(true);
	assertEquals(true,pSetup.getUseFirstPageNumber());
    }

    public void testSetGetHVResolution() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setHorizontalDpi(120);
	pSetup.setVerticalDpi(100);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(120,printSetup.getHResolution());
	assertEquals(100,printSetup.getVResolution());

	printSetup.setHResolution((short)150);
	printSetup.setVResolution((short)130);
	assertEquals(150,pSetup.getHorizontalDpi());
	assertEquals(130,pSetup.getVerticalDpi());
    }

    public void testSetGetHeaderFooterMargin() {
	CTPageMargins pMargins=CTPageMargins.Factory.newInstance();
	pMargins.setHeader(1.5);
	pMargins.setFooter(2);
	XSSFPrintSetup printSetup=new XSSFPrintSetup( null, pMargins);
	assertEquals(1.5,printSetup.getHeaderMargin());
	assertEquals(2.0,printSetup.getFooterMargin());

	printSetup.setHeaderMargin(5);
	printSetup.setFooterMargin(3.5);
	assertEquals(5.0,pMargins.getHeader());
	assertEquals(3.5,pMargins.getFooter());
    }

    public void testSetGetCopies() {
	CTPageSetup pSetup=CTPageSetup.Factory.newInstance();
	pSetup.setCopies(9);
	XSSFPrintSetup printSetup=new XSSFPrintSetup(pSetup, null);
	assertEquals(9,printSetup.getCopies());

	printSetup.setCopies((short)15);
	assertEquals(15,pSetup.getCopies());
    }


}
