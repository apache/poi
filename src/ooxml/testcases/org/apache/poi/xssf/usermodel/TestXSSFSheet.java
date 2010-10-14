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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.apache.poi.util.HexDump;
import org.apache.poi.hssf.record.PasswordRecord;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;


@SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
public final class TestXSSFSheet extends BaseTestSheet {

    public TestXSSFSheet() {
        super(XSSFITestDataProvider.instance);
    }

    //TODO column styles are not yet supported by XSSF
    public void testDefaultColumnStyle() {
        //super.testDefaultColumnStyle();
    }

    public void testTestGetSetMargin() {
        baseTestGetSetMargin(new double[]{0.7, 0.7, 0.75, 0.75, 0.3, 0.3});
    }

    public void testExistingHeaderFooter() {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("45540_classic_Header.xlsx");
        XSSFOddHeader hdr;
        XSSFOddFooter ftr;

        // Sheet 1 has a header with center and right text
        XSSFSheet s1 = workbook.getSheetAt(0);
        assertNotNull(s1.getHeader());
        assertNotNull(s1.getFooter());
        hdr = (XSSFOddHeader) s1.getHeader();
        ftr = (XSSFOddFooter) s1.getFooter();

        assertEquals("&Ctestdoc&Rtest phrase", hdr.getText());
        assertEquals(null, ftr.getText());

        assertEquals("", hdr.getLeft());
        assertEquals("testdoc", hdr.getCenter());
        assertEquals("test phrase", hdr.getRight());

        assertEquals("", ftr.getLeft());
        assertEquals("", ftr.getCenter());
        assertEquals("", ftr.getRight());

        // Sheet 2 has a footer, but it's empty
        XSSFSheet s2 = workbook.getSheetAt(1);
        assertNotNull(s2.getHeader());
        assertNotNull(s2.getFooter());
        hdr = (XSSFOddHeader) s2.getHeader();
        ftr = (XSSFOddFooter) s2.getFooter();

        assertEquals(null, hdr.getText());
        assertEquals("&L&F", ftr.getText());

        assertEquals("", hdr.getLeft());
        assertEquals("", hdr.getCenter());
        assertEquals("", hdr.getRight());

        assertEquals("&F", ftr.getLeft());
        assertEquals("", ftr.getCenter());
        assertEquals("", ftr.getRight());

        // Save and reload
        XSSFWorkbook wb = XSSFTestDataSamples.writeOutAndReadBack(workbook);

        hdr = (XSSFOddHeader) wb.getSheetAt(0).getHeader();
        ftr = (XSSFOddFooter) wb.getSheetAt(0).getFooter();

        assertEquals("", hdr.getLeft());
        assertEquals("testdoc", hdr.getCenter());
        assertEquals("test phrase", hdr.getRight());

        assertEquals("", ftr.getLeft());
        assertEquals("", ftr.getCenter());
        assertEquals("", ftr.getRight());
    }

    public void testGetAllHeadersFooters() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
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

        // Defaults are odd
        assertEquals("odd footer left", sheet.getFooter().getLeft());
        assertEquals("odd header center", sheet.getHeader().getCenter());
    }

    public void testAutoSizeColumn() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
        sheet.createRow(0).createCell(13).setCellValue("test");

        sheet.autoSizeColumn(13);

        ColumnHelper columnHelper = sheet.getColumnHelper();
        CTCol col = columnHelper.getColumn(13, false);
        assertTrue(col.getBestFit());
    }

    /**
     * XSSFSheet autoSizeColumn() on empty RichTextString fails
     */
    public void test48325() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Test");
        CreationHelper factory = wb.getCreationHelper();

        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);

        XSSFFont font = wb.createFont();
        RichTextString rts = factory.createRichTextString("");
        rts.applyFont(font);
        cell.setCellValue(rts);

        sheet.autoSizeColumn(0);
    }

    public void testGetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFDrawing dg = sheet.createDrawingPatriarch();
        XSSFComment comment = dg.createCellComment(new XSSFClientAnchor());
        XSSFCell cell = sheet.createRow(9).createCell(2);
        comment.setAuthor("test C10 author");
        cell.setCellComment(comment);

        assertNotNull(sheet.getCellComment(9, 2));
        assertEquals("test C10 author", sheet.getCellComment(9, 2).getAuthor());
    }

    public void testSetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFDrawing dg = sheet.createDrawingPatriarch();
        XSSFComment comment = dg.createCellComment(new XSSFClientAnchor());

        Cell cell = sheet.createRow(0).createCell(0);
        CommentsTable comments = sheet.getCommentsTable(false);
        CTComments ctComments = comments.getCTComments();

        cell.setCellComment(comment);
        assertEquals("A1", ctComments.getCommentList().getCommentArray(0).getRef());
        comment.setAuthor("test A1 author");
        assertEquals("test A1 author", comments.getAuthor((int) ctComments.getCommentList().getCommentArray(0).getAuthorId()));
    }

    public void testGetActiveCell() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.setActiveCell("R5");

        assertEquals("R5", sheet.getActiveCell());

    }

    public void testCreateFreezePane() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();

        sheet.createFreezePane(2, 4);
        assertEquals(2.0, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getXSplit(), 0.0);
        assertEquals(STPane.BOTTOM_RIGHT, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getActivePane());
        sheet.createFreezePane(3, 6, 10, 10);
        assertEquals(3.0, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getXSplit(), 0.0);
        //    assertEquals(10, sheet.getTopRow());
        //    assertEquals(10, sheet.getLeftCol());
        sheet.createSplitPane(4, 8, 12, 12, 1);
        assertEquals(8.0, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getYSplit(), 0.0);
        assertEquals(STPane.BOTTOM_RIGHT, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getActivePane());
    }

    public void testNewMergedRegionAt() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CellRangeAddress region = CellRangeAddress.valueOf("B2:D4");
        sheet.addMergedRegion(region);
        assertEquals("B2:D4", sheet.getMergedRegion(0).formatAsString());
        assertEquals(1, sheet.getNumMergedRegions());
    }

    public void testRemoveMergedRegion_lowlevel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        CellRangeAddress region_1 = CellRangeAddress.valueOf("A1:B2");
        CellRangeAddress region_2 = CellRangeAddress.valueOf("C3:D4");
        CellRangeAddress region_3 = CellRangeAddress.valueOf("E5:F6");
        sheet.addMergedRegion(region_1);
        sheet.addMergedRegion(region_2);
        sheet.addMergedRegion(region_3);
        assertEquals("C3:D4", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
        assertEquals(3, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(1);
        assertEquals("E5:F6", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
        assertEquals(2, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(1);
        sheet.removeMergedRegion(0);
        assertEquals(0, sheet.getNumMergedRegions());
        assertNull(" CTMergeCells should be deleted after removing the last merged " +
                "region on the sheet.", sheet.getCTWorksheet().getMergeCells());
    }

    public void testSetDefaultColumnStyle() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        StylesTable stylesTable = workbook.getStylesSource();
        XSSFFont font = new XSSFFont();
        font.setFontName("Cambria");
        stylesTable.putFont(font);
        CTXf cellStyleXf = CTXf.Factory.newInstance();
        cellStyleXf.setFontId(1);
        cellStyleXf.setFillId(0);
        cellStyleXf.setBorderId(0);
        cellStyleXf.setNumFmtId(0);
        stylesTable.putCellStyleXf(cellStyleXf);
        CTXf cellXf = CTXf.Factory.newInstance();
        cellXf.setXfId(1);
        stylesTable.putCellXf(cellXf);
        XSSFCellStyle cellStyle = new XSSFCellStyle(1, 1, stylesTable, null);
        assertEquals(1, cellStyle.getFontIndex());

        sheet.setDefaultColumnStyle(3, cellStyle);
        assertEquals(1, ctWorksheet.getColsArray(0).getColArray(0).getStyle());
    }


    public void testGroupUngroupColumn() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        //one level
        sheet.groupColumn(2, 7);
        sheet.groupColumn(10, 11);
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(2, cols.sizeOfColArray());
        CTCol[] colArray = cols.getColArray();
        assertNotNull(colArray);
        assertEquals(2 + 1, colArray[0].getMin()); // 1 based
        assertEquals(7 + 1, colArray[0].getMax()); // 1 based
        assertEquals(1, colArray[0].getOutlineLevel());

        //two level
        sheet.groupColumn(1, 2);
        cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(4, cols.sizeOfColArray());
        colArray = cols.getColArray();
        assertEquals(2, colArray[1].getOutlineLevel());

        //three level
        sheet.groupColumn(6, 8);
        sheet.groupColumn(2, 3);
        cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(7, cols.sizeOfColArray());
        colArray = cols.getColArray();
        assertEquals(3, colArray[1].getOutlineLevel());
        assertEquals(3, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelCol());

        sheet.ungroupColumn(8, 10);
        colArray = cols.getColArray();
        //assertEquals(3, colArray[1].getOutlineLevel());

        sheet.ungroupColumn(4, 6);
        sheet.ungroupColumn(2, 2);
        colArray = cols.getColArray();
        assertEquals(4, colArray.length);
        assertEquals(2, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelCol());
    }


    public void testGroupUngroupRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        //one level
        sheet.groupRow(9, 10);
        assertEquals(2, sheet.getPhysicalNumberOfRows());
        CTRow ctrow = sheet.getRow(9).getCTRow();

        assertNotNull(ctrow);
        assertEquals(10, ctrow.getR());
        assertEquals(1, ctrow.getOutlineLevel());
        assertEquals(1, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());

        //two level
        sheet.groupRow(10, 13);
        assertEquals(5, sheet.getPhysicalNumberOfRows());
        ctrow = sheet.getRow(10).getCTRow();
        assertNotNull(ctrow);
        assertEquals(11, ctrow.getR());
        assertEquals(2, ctrow.getOutlineLevel());
        assertEquals(2, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());


        sheet.ungroupRow(8, 10);
        assertEquals(4, sheet.getPhysicalNumberOfRows());
        assertEquals(1, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());

        sheet.ungroupRow(10, 10);
        assertEquals(3, sheet.getPhysicalNumberOfRows());

        assertEquals(1, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());
    }

    public void testSetZoom() {
        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFSheet sheet1 = workBook.createSheet("new sheet");
        sheet1.setZoom(3, 4);   // 75 percent magnification
        long zoom = sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).getZoomScale();
        assertEquals(zoom, 75);

        sheet1.setZoom(200);
        zoom = sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).getZoomScale();
        assertEquals(zoom, 200);

        try {
            sheet1.setZoom(500);
            fail("Expecting exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Valid scale values range from 10 to 400", e.getMessage());
        }
    }

    /**
     * TODO - while this is internally consistent, I'm not
     *  completely clear in all cases what it's supposed to
     *  be doing... Someone who understands the goals a little
     *  better should really review this!
     */
    public void testSetColumnGroupCollapsed(){
        Workbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 =(XSSFSheet) wb.createSheet();

        CTCols cols=sheet1.getCTWorksheet().getColsArray(0);
        assertEquals(0,cols.sizeOfColArray());

        sheet1.groupColumn( (short)4, (short)7 );
        sheet1.groupColumn( (short)9, (short)12 );

        assertEquals(2,cols.sizeOfColArray());

        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(true, cols.getColArray(1).isSetCollapsed());
        assertEquals(10, cols.getColArray(1).getMin()); // 1 based
        assertEquals(13, cols.getColArray(1).getMax()); // 1 based

        sheet1.groupColumn( (short)10, (short)11 );
        assertEquals(4,cols.sizeOfColArray());

        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(true, cols.getColArray(1).isSetCollapsed());
        assertEquals(10, cols.getColArray(1).getMin()); // 1 based
        assertEquals(10, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(11, cols.getColArray(2).getMin()); // 1 based
        assertEquals(12, cols.getColArray(2).getMax()); // 1 based
        assertEquals(false,cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(13, cols.getColArray(3).getMin()); // 1 based
        assertEquals(13, cols.getColArray(3).getMax()); // 1 based

        // collapse columns - 1
        sheet1.setColumnGroupCollapsed( (short)5, true );
        assertEquals(5,cols.sizeOfColArray());

        assertEquals(true, cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(true, cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(false,cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(false,cols.getColArray(4).isSetHidden());
        assertEquals(true, cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based


        // expand columns - 1
        sheet1.setColumnGroupCollapsed( (short)5, false );

        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(false,cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(false,cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(false,cols.getColArray(4).isSetHidden());
        assertEquals(true, cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based


        //collapse - 2
        sheet1.setColumnGroupCollapsed( (short)9, true );
        assertEquals(6,cols.sizeOfColArray());
        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(false,cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(true, cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(true, cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(true, cols.getColArray(4).isSetHidden());
        assertEquals(true, cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based
        assertEquals(false,cols.getColArray(5).isSetHidden());
        assertEquals(true, cols.getColArray(5).isSetCollapsed());
        assertEquals(14, cols.getColArray(5).getMin()); // 1 based
        assertEquals(14, cols.getColArray(5).getMax()); // 1 based


        //expand - 2
        sheet1.setColumnGroupCollapsed( (short)9, false );
        assertEquals(6,cols.sizeOfColArray());
        assertEquals(14,cols.getColArray(5).getMin());

        //outline level 2: the line under ==> collapsed==True
        assertEquals(2,cols.getColArray(3).getOutlineLevel());
        assertEquals(true,cols.getColArray(4).isSetCollapsed());

        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(false,cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(true, cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(false,cols.getColArray(4).isSetHidden());
        assertEquals(true, cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based
        assertEquals(false,cols.getColArray(5).isSetHidden());
        assertEquals(false,cols.getColArray(5).isSetCollapsed());
        assertEquals(14, cols.getColArray(5).getMin()); // 1 based
        assertEquals(14, cols.getColArray(5).getMax()); // 1 based

        //DOCUMENTARE MEGLIO IL DISCORSO DEL LIVELLO
        //collapse - 3
        sheet1.setColumnGroupCollapsed( (short)10, true );
        assertEquals(6,cols.sizeOfColArray());
        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(false,cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(true, cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(false,cols.getColArray(4).isSetHidden());
        assertEquals(true, cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based
        assertEquals(false,cols.getColArray(5).isSetHidden());
        assertEquals(false,cols.getColArray(5).isSetCollapsed());
        assertEquals(14, cols.getColArray(5).getMin()); // 1 based
        assertEquals(14, cols.getColArray(5).getMax()); // 1 based


        //expand - 3
        sheet1.setColumnGroupCollapsed( (short)10, false );
        assertEquals(6,cols.sizeOfColArray());
        assertEquals(false,cols.getColArray(0).getHidden());
        assertEquals(false,cols.getColArray(5).getHidden());
        assertEquals(false,cols.getColArray(4).isSetCollapsed());

//      write out and give back
        // Save and re-load
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet1 = (XSSFSheet)wb.getSheetAt(0);
        assertEquals(6,cols.sizeOfColArray());

        assertEquals(false,cols.getColArray(0).isSetHidden());
        assertEquals(true, cols.getColArray(0).isSetCollapsed());
        assertEquals(5, cols.getColArray(0).getMin()); // 1 based
        assertEquals(8, cols.getColArray(0).getMax()); // 1 based
        assertEquals(false,cols.getColArray(1).isSetHidden());
        assertEquals(false,cols.getColArray(1).isSetCollapsed());
        assertEquals(9, cols.getColArray(1).getMin()); // 1 based
        assertEquals(9, cols.getColArray(1).getMax()); // 1 based
        assertEquals(false,cols.getColArray(2).isSetHidden());
        assertEquals(true, cols.getColArray(2).isSetCollapsed());
        assertEquals(10, cols.getColArray(2).getMin()); // 1 based
        assertEquals(10, cols.getColArray(2).getMax()); // 1 based
        assertEquals(false,cols.getColArray(3).isSetHidden());
        assertEquals(true, cols.getColArray(3).isSetCollapsed());
        assertEquals(11, cols.getColArray(3).getMin()); // 1 based
        assertEquals(12, cols.getColArray(3).getMax()); // 1 based
        assertEquals(false,cols.getColArray(4).isSetHidden());
        assertEquals(false,cols.getColArray(4).isSetCollapsed());
        assertEquals(13, cols.getColArray(4).getMin()); // 1 based
        assertEquals(13, cols.getColArray(4).getMax()); // 1 based
        assertEquals(false,cols.getColArray(5).isSetHidden());
        assertEquals(false,cols.getColArray(5).isSetCollapsed());
        assertEquals(14, cols.getColArray(5).getMin()); // 1 based
        assertEquals(14, cols.getColArray(5).getMax()); // 1 based
    }

    /**
     * TODO - while this is internally consistent, I'm not
     *  completely clear in all cases what it's supposed to
     *  be doing... Someone who understands the goals a little
     *  better should really review this!
     */
    public void testSetRowGroupCollapsed(){
        Workbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = (XSSFSheet)wb.createSheet();

        sheet1.groupRow( 5, 14 );
        sheet1.groupRow( 7, 14 );
        sheet1.groupRow( 16, 19 );

        assertEquals(14,sheet1.getPhysicalNumberOfRows());
        assertEquals(false,sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(6).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(7).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(9).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(14).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetHidden());

        //collapsed
        sheet1.setRowGroupCollapsed( 7, true );

        assertEquals(false,sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(6).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(7).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(9).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(14).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetHidden());

        //expanded
        sheet1.setRowGroupCollapsed( 7, false );

        assertEquals(false,sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(6).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(7).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(9).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(14).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetHidden());


        // Save and re-load
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet1 = (XSSFSheet)wb.getSheetAt(0);

        assertEquals(false,sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(6).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(7).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(9).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertEquals(true, sheet1.getRow(14).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(16).getCTRow().isSetHidden());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertEquals(false,sheet1.getRow(18).getCTRow().isSetHidden());
    }

    /**
     * Get / Set column width and check the actual values of the underlying XML beans
     */
    public void testColumnWidth_lowlevel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnWidth(1, 22 * 256);
        assertEquals(22 * 256, sheet.getColumnWidth(1));

        // Now check the low level stuff, and check that's all
        //  been set correctly
        XSSFSheet xs = sheet;
        CTWorksheet cts = xs.getCTWorksheet();

        CTCols[] cols_s = cts.getColsArray();
        assertEquals(1, cols_s.length);
        CTCols cols = cols_s[0];
        assertEquals(1, cols.sizeOfColArray());
        CTCol col = cols.getColArray(0);

        // XML is 1 based, POI is 0 based
        assertEquals(2, col.getMin());
        assertEquals(2, col.getMax());
        assertEquals(22.0, col.getWidth(), 0.0);
        assertTrue(col.getCustomWidth());

        // Now set another
        sheet.setColumnWidth(3, 33 * 256);

        cols_s = cts.getColsArray();
        assertEquals(1, cols_s.length);
        cols = cols_s[0];
        assertEquals(2, cols.sizeOfColArray());

        col = cols.getColArray(0);
        assertEquals(2, col.getMin()); // POI 1
        assertEquals(2, col.getMax());
        assertEquals(22.0, col.getWidth(), 0.0);
        assertTrue(col.getCustomWidth());

        col = cols.getColArray(1);
        assertEquals(4, col.getMin()); // POI 3
        assertEquals(4, col.getMax());
        assertEquals(33.0, col.getWidth(), 0.0);
        assertTrue(col.getCustomWidth());
    }

    /**
     * Setting width of a column included in a column span
     */
    public void test47862() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47862.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        //<cols>
        //  <col min="1" max="5" width="15.77734375" customWidth="1"/>
        //</cols>

        //a span of columns [1,5]
        assertEquals(1, cols.sizeOfColArray());
        CTCol col = cols.getColArray(0);
        assertEquals(1, col.getMin());
        assertEquals(5, col.getMax());
        double swidth = 15.77734375; //width of columns in the span
        assertEquals(swidth, col.getWidth(), 0.0);

        for (int i = 0; i < 5; i++) {
            assertEquals((int)(swidth*256), sheet.getColumnWidth(i));
        }

        int[] cw = new int[]{10, 15, 20, 25, 30};
        for (int i = 0; i < 5; i++) {
            sheet.setColumnWidth(i, cw[i]*256);
        }

        //the check below failed prior to fix of Bug #47862
        ColumnHelper.sortColumns(cols);
        //<cols>
        //  <col min="1" max="1" customWidth="true" width="10.0" />
        //  <col min="2" max="2" customWidth="true" width="15.0" />
        //  <col min="3" max="3" customWidth="true" width="20.0" />
        //  <col min="4" max="4" customWidth="true" width="25.0" />
        //  <col min="5" max="5" customWidth="true" width="30.0" />
        //</cols>

        //now the span is splitted into 5 individual columns
        assertEquals(5, cols.sizeOfColArray());
        for (int i = 0; i < 5; i++) {
            assertEquals(cw[i]*256, sheet.getColumnWidth(i));
            assertEquals(cw[i], cols.getColArray(i).getWidth(), 0.0);
        }

        //serialize and check again
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(5, cols.sizeOfColArray());
        for (int i = 0; i < 5; i++) {
            assertEquals(cw[i]*256, sheet.getColumnWidth(i));
            assertEquals(cw[i], cols.getColArray(i).getWidth(), 0.0);
        }
    }

    /**
     * Hiding a column included in a column span
     */
    public void test47804() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47804.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(2, cols.sizeOfColArray());
        CTCol col;
        //<cols>
        //  <col min="2" max="4" width="12" customWidth="1"/>
        //  <col min="7" max="7" width="10.85546875" customWidth="1"/>
        //</cols>

        //a span of columns [2,4]
        col = cols.getColArray(0);
        assertEquals(2, col.getMin());
        assertEquals(4, col.getMax());
        //individual column
        col = cols.getColArray(1);
        assertEquals(7, col.getMin());
        assertEquals(7, col.getMax());

        sheet.setColumnHidden(2, true); // Column C
        sheet.setColumnHidden(6, true); // Column G

        assertTrue(sheet.isColumnHidden(2));
        assertTrue(sheet.isColumnHidden(6));

        //other columns but C and G are not hidden
        assertFalse(sheet.isColumnHidden(1));
        assertFalse(sheet.isColumnHidden(3));
        assertFalse(sheet.isColumnHidden(4));
        assertFalse(sheet.isColumnHidden(5));

        //the check below failed prior to fix of Bug #47804
        ColumnHelper.sortColumns(cols);
        //the span is now splitted into three parts
        //<cols>
        //  <col min="2" max="2" customWidth="true" width="12.0" />
        //  <col min="3" max="3" customWidth="true" width="12.0" hidden="true"/>
        //  <col min="4" max="4" customWidth="true" width="12.0"/>
        //  <col min="7" max="7" customWidth="true" width="10.85546875" hidden="true"/>
        //</cols>

        assertEquals(4, cols.sizeOfColArray());
        col = cols.getColArray(0);
        assertEquals(2, col.getMin());
        assertEquals(2, col.getMax());
        col = cols.getColArray(1);
        assertEquals(3, col.getMin());
        assertEquals(3, col.getMax());
        col = cols.getColArray(2);
        assertEquals(4, col.getMin());
        assertEquals(4, col.getMax());
        col = cols.getColArray(3);
        assertEquals(7, col.getMin());
        assertEquals(7, col.getMax());

        //serialize and check again
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        assertTrue(sheet.isColumnHidden(2));
        assertTrue(sheet.isColumnHidden(6));
        assertFalse(sheet.isColumnHidden(1));
        assertFalse(sheet.isColumnHidden(3));
        assertFalse(sheet.isColumnHidden(4));
        assertFalse(sheet.isColumnHidden(5));
    }

    public void testCommentsTable() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet1 = workbook.createSheet();
        CommentsTable comment1 = sheet1.getCommentsTable(false);
        assertNull(comment1);

        comment1 = sheet1.getCommentsTable(true);
        assertNotNull(comment1);
        assertEquals("/xl/comments1.xml", comment1.getPackageRelationship().getTargetURI().toString());

        assertSame(comment1, sheet1.getCommentsTable(true));

        //second sheet
        XSSFSheet sheet2 = workbook.createSheet();
        CommentsTable comment2 = sheet2.getCommentsTable(false);
        assertNull(comment2);

        comment2 = sheet2.getCommentsTable(true);
        assertNotNull(comment2);

        assertSame(comment2, sheet2.getCommentsTable(true));
        assertEquals("/xl/comments2.xml", comment2.getPackageRelationship().getTargetURI().toString());

        //comment1 and  comment2 are different objects
        assertNotSame(comment1, comment2);

        //now test against a workbook containing cell comments
        workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
        sheet1 = workbook.getSheetAt(0);
        comment1 = sheet1.getCommentsTable(true);
        assertNotNull(comment1);
        assertEquals("/xl/comments1.xml", comment1.getPackageRelationship().getTargetURI().toString());
        assertSame(comment1, sheet1.getCommentsTable(true));
    }

    /**
     * Rows and cells can be created in random order,
     * but serialization forces strict ascending order of the CTRow and CTCell xml beans
     */
    public void testCreateRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet wsh = sheet.getCTWorksheet();
        CTSheetData sheetData = wsh.getSheetData();
        assertEquals(0, sheetData.sizeOfRowArray());

        XSSFRow row1 = sheet.createRow(2);
        row1.createCell(2);
        row1.createCell(1);

        XSSFRow row2 = sheet.createRow(1);
        row2.createCell(2);
        row2.createCell(1);
        row2.createCell(0);

        XSSFRow row3 = sheet.createRow(0);
        row3.createCell(3);
        row3.createCell(0);
        row3.createCell(2);
        row3.createCell(5);


        CTRow[] xrow = sheetData.getRowArray();
        assertEquals(3, xrow.length);

        //rows are unsorted: {2, 1, 0}
        assertEquals(2, xrow[0].sizeOfCArray());
        assertEquals(3, xrow[0].getR());
        assertTrue(xrow[0].equals(row1.getCTRow()));

        assertEquals(3, xrow[1].sizeOfCArray());
        assertEquals(2, xrow[1].getR());
        assertTrue(xrow[1].equals(row2.getCTRow()));

        assertEquals(4, xrow[2].sizeOfCArray());
        assertEquals(1, xrow[2].getR());
        assertTrue(xrow[2].equals(row3.getCTRow()));

        CTCell[] xcell = xrow[2].getCArray();
        assertEquals("D1", xcell[0].getR());
        assertEquals("A1", xcell[1].getR());
        assertEquals("C1", xcell[2].getR());
        assertEquals("F1", xcell[3].getR());

        //re-creating a row does NOT add extra data to the parent
        row2 = sheet.createRow(1);
        assertEquals(3, sheetData.sizeOfRowArray());
        //existing cells are invalidated
        assertEquals(0, sheetData.getRowArray(1).sizeOfCArray());
        assertEquals(0, row2.getPhysicalNumberOfCells());

        workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        wsh = sheet.getCTWorksheet();
        xrow = sheetData.getRowArray();
        assertEquals(3, xrow.length);

        //rows are sorted: {0, 1, 2}
        assertEquals(4, xrow[0].sizeOfCArray());
        assertEquals(1, xrow[0].getR());
        //cells are now sorted
        xcell = xrow[0].getCArray();
        assertEquals("A1", xcell[0].getR());
        assertEquals("C1", xcell[1].getR());
        assertEquals("D1", xcell[2].getR());
        assertEquals("F1", xcell[3].getR());


        assertEquals(0, xrow[1].sizeOfCArray());
        assertEquals(2, xrow[1].getR());

        assertEquals(2, xrow[2].sizeOfCArray());
        assertEquals(3, xrow[2].getR());

    }

    public void testSetAutoFilter() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D100"));

        assertEquals("A1:D100", sheet.getCTWorksheet().getAutoFilter().getRef());
    }

    public void testProtectSheet_lowlevel() {

    	XSSFWorkbook wb = new XSSFWorkbook();
    	XSSFSheet sheet = wb.createSheet();
        CTSheetProtection pr = sheet.getCTWorksheet().getSheetProtection();
        assertNull("CTSheetProtection should be null by default", pr);
        String password = "Test";
        sheet.protectSheet(password);
        pr = sheet.getCTWorksheet().getSheetProtection();
        assertNotNull("CTSheetProtection should be not null", pr);
        assertTrue("sheet protection should be on", pr.isSetSheet());
        assertTrue("object protection should be on", pr.isSetObjects());
        assertTrue("scenario protection should be on", pr.isSetScenarios());
        String hash = String.valueOf(HexDump.shortToHex(PasswordRecord.hashPassword(password))).substring(2);
        assertEquals("well known value for top secret hash should be "+ hash, hash, pr.xgetPassword().getStringValue());

        sheet.protectSheet(null);
        assertNull("protectSheet(null) should unset CTSheetProtection", sheet.getCTWorksheet().getSheetProtection());
    }

}
