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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.xssf.XSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.tests.usermodel.BaseTestXSheet;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIgnoredError;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetData;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPane;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnsignedShortHex;


public final class TestXSSFSheet extends BaseTestXSheet {
    public TestXSSFSheet() {
        super(XSSFITestDataProvider.instance);
    }

    //TODO column styles are not yet supported by XSSF
    @Override
    protected void defaultColumnStyle() {
        //super.defaultColumnStyle();
    }

    @Test
    void existingHeaderFooter() throws IOException {
        try (XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("45540_classic_Header.xlsx")) {
            XSSFOddHeader hdr;
            XSSFOddFooter ftr;

            // Sheet 1 has a header with center and right text
            XSSFSheet s1 = wb1.getSheetAt(0);
            assertNotNull(s1.getHeader());
            assertNotNull(s1.getFooter());
            hdr = (XSSFOddHeader) s1.getHeader();
            ftr = (XSSFOddFooter) s1.getFooter();

            assertEquals("&Ctestdoc&Rtest phrase", hdr.getText());
            assertNull(ftr.getText());

            assertEquals("", hdr.getLeft());
            assertEquals("testdoc", hdr.getCenter());
            assertEquals("test phrase", hdr.getRight());

            assertEquals("", ftr.getLeft());
            assertEquals("", ftr.getCenter());
            assertEquals("", ftr.getRight());

            // Sheet 2 has a footer, but it's empty
            XSSFSheet s2 = wb1.getSheetAt(1);
            assertNotNull(s2.getHeader());
            assertNotNull(s2.getFooter());
            hdr = (XSSFOddHeader) s2.getHeader();
            ftr = (XSSFOddFooter) s2.getFooter();

            assertNull(hdr.getText());
            assertEquals("&L&F", ftr.getText());

            assertEquals("", hdr.getLeft());
            assertEquals("", hdr.getCenter());
            assertEquals("", hdr.getRight());

            assertEquals("&F", ftr.getLeft());
            assertEquals("", ftr.getCenter());
            assertEquals("", ftr.getRight());

            // Save and reload
            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                hdr = (XSSFOddHeader) wb2.getSheetAt(0).getHeader();
                ftr = (XSSFOddFooter) wb2.getSheetAt(0).getFooter();

                assertEquals("", hdr.getLeft());
                assertEquals("testdoc", hdr.getCenter());
                assertEquals("test phrase", hdr.getRight());

                assertEquals("", ftr.getLeft());
                assertEquals("", ftr.getCenter());
                assertEquals("", ftr.getRight());
            }
        }
    }

    @Test
    void getAllHeadersFooters() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
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
    }

    @Test
    void autoSizeColumn() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Sheet 1");
            sheet.createRow(0).createCell(13).setCellValue("test");

            sheet.autoSizeColumn(13);

            ColumnHelper columnHelper = sheet.getColumnHelper();
            CTCol col = columnHelper.getColumn(13, false);
            assertTrue(col.getBestFit());
        }
    }


    @Test
    void setCellComment() throws IOException {
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
        workbook.close();
    }

    @Test
    void getActiveCell() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CellAddress R5 = new CellAddress("R5");
        sheet.setActiveCell(R5);

        assertEquals(R5, sheet.getActiveCell());
        workbook.close();
    }

    @Test
    void createFreezePane_XSSF() throws IOException {
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

        workbook.close();
    }

    @Test
    void removeMergedRegion_lowlevel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        CellRangeAddress region_1 = CellRangeAddress.valueOf("A1:B2");
        CellRangeAddress region_2 = CellRangeAddress.valueOf("C3:D4");
        CellRangeAddress region_3 = CellRangeAddress.valueOf("E5:F6");
        CellRangeAddress region_4 = CellRangeAddress.valueOf("G7:H8");
        assertEquals(0, sheet.addMergedRegion(region_1));
        assertEquals(1, sheet.addMergedRegion(region_2));
        assertEquals(2, sheet.addMergedRegion(region_3));
        assertEquals("C3:D4", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
        assertEquals(3, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(1);
        assertEquals("E5:F6", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
        assertEquals(2, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(1);
        sheet.removeMergedRegion(0);
        assertEquals(0, sheet.getNumMergedRegions());
        assertNull(sheet.getCTWorksheet().getMergeCells(),
            "CTMergeCells should be deleted after removing the last merged region on the sheet.");
        assertEquals(0, sheet.addMergedRegion(region_1));
        assertEquals(1, sheet.addMergedRegion(region_2));
        assertEquals(2, sheet.addMergedRegion(region_3));
        assertEquals(3, sheet.addMergedRegion(region_4));
        // test invalid indexes OOBE
        Set<Integer> rmIdx = new HashSet<>(Arrays.asList(5, 6));
        sheet.removeMergedRegions(rmIdx);
        rmIdx = new HashSet<>(Arrays.asList(1, 3));
        sheet.removeMergedRegions(rmIdx);
        assertEquals("A1:B2", ctWorksheet.getMergeCells().getMergeCellArray(0).getRef());
        assertEquals("E5:F6", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
        workbook.close();
    }

    @Test
    void setDefaultColumnStyle() throws IOException {
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
        assertEquals(2, stylesTable.putCellStyleXf(cellStyleXf));
        CTXf cellXf = CTXf.Factory.newInstance();
        cellXf.setXfId(1);
        stylesTable.putCellXf(cellXf);
        XSSFCellStyle cellStyle = new XSSFCellStyle(1, 1, stylesTable, null);
        assertEquals(1, cellStyle.getFontIndex());

        sheet.setDefaultColumnStyle(3, cellStyle);
        assertEquals(1, ctWorksheet.getColsArray(0).getColArray(0).getStyle());
        workbook.close();
    }


    @Test
    void groupUngroupColumn() throws IOException {
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
        assertEquals(0, sheet.getColumnOutlineLevel(0));

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
        assertEquals(3, colArray[1].getOutlineLevel());

        sheet.ungroupColumn(4, 6);
        sheet.ungroupColumn(2, 2);
        colArray = cols.getColArray();
        assertEquals(4, colArray.length);
        assertEquals(2, sheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelCol());

        workbook.close();
    }

    @Test
    void groupUngroupRow() throws IOException {
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

        workbook.close();
    }

    @Test
    void setZoom() throws IOException {
        try (XSSFWorkbook workBook = new XSSFWorkbook()) {
            XSSFSheet sheet1 = workBook.createSheet("new sheet");
            sheet1.setZoom(75);   // 75 percent magnification
            long zoom = sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).getZoomScale();
            assertEquals(zoom, 75);

            sheet1.setZoom(200);
            zoom = sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).getZoomScale();
            assertEquals(zoom, 200);

            // Valid scale values range from 10 to 400
            assertThrows(IllegalArgumentException.class, () -> sheet1.setZoom(500));
        }
    }

    /**
     * TODO - while this is internally consistent, I'm not
     *  completely clear in all cases what it's supposed to
     *  be doing... Someone who understands the goals a little
     *  better should really review this!
     */
    @Test
    void setColumnGroupCollapsed() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet1 = wb1.createSheet();

            CTCols cols = sheet1.getCTWorksheet().getColsArray(0);
            assertEquals(0, cols.sizeOfColArray());

            sheet1.groupColumn(4, 7);

            assertEquals(1, cols.sizeOfColArray());
            checkColumnGroup(cols.getColArray(0), 4, 7); // false, true

            sheet1.groupColumn(9, 12);

            assertEquals(2, cols.sizeOfColArray());
            checkColumnGroup(cols.getColArray(0), 4, 7); // false, true
            checkColumnGroup(cols.getColArray(1), 9, 12); // false, true

            sheet1.groupColumn(10, 11);

            assertEquals(4, cols.sizeOfColArray());
            checkColumnGroup(cols.getColArray(0), 4, 7); // false, true
            checkColumnGroup(cols.getColArray(1), 9, 9); // false, true
            checkColumnGroup(cols.getColArray(2), 10, 11); // false, true
            checkColumnGroup(cols.getColArray(3), 12, 12); // false, true

            // collapse columns - 1
            sheet1.setColumnGroupCollapsed(5, true);

            // FIXME: we grew a column?
            assertEquals(5, cols.sizeOfColArray());
            checkColumnGroupIsCollapsed(cols.getColArray(0), 4, 7); // true, true
            checkColumnGroup(cols.getColArray(1), 8, 8); // false, true
            checkColumnGroup(cols.getColArray(2), 9, 9); // false, true
            checkColumnGroup(cols.getColArray(3), 10, 11); // false, true
            checkColumnGroup(cols.getColArray(4), 12, 12); // false, true


            // expand columns - 1
            sheet1.setColumnGroupCollapsed(5, false);
            assertEquals(5, cols.sizeOfColArray());

            checkColumnGroupIsExpanded(cols.getColArray(0), 4, 7); // false, true
            checkColumnGroup(cols.getColArray(1), 8, 8, false, false);
            checkColumnGroup(cols.getColArray(2), 9, 9); // false, true
            checkColumnGroup(cols.getColArray(3), 10, 11); // false, true
            checkColumnGroup(cols.getColArray(4), 12, 12); // false, true


            //collapse - 2
            sheet1.setColumnGroupCollapsed(9, true);
            // it grew again?
            assertEquals(6, cols.sizeOfColArray());
            checkColumnGroup(cols.getColArray(0), 4, 7); // false, true
            checkColumnGroup(cols.getColArray(1), 8, 8, false, false);
            checkColumnGroupIsCollapsed(cols.getColArray(2), 9, 9); // true, true
            checkColumnGroupIsCollapsed(cols.getColArray(3), 10, 11); // true, true
            checkColumnGroupIsCollapsed(cols.getColArray(4), 12, 12); // true, true
            // why was this column group added?
            checkColumnGroup(cols.getColArray(5), 13, 13); // false, true


            //expand - 2
            sheet1.setColumnGroupCollapsed(9, false);
            assertEquals(6, cols.sizeOfColArray());

            //outline level 2: the line under ==> collapsed==True
            assertEquals(2, cols.getColArray(3).getOutlineLevel());
            assertTrue(cols.getColArray(4).isSetCollapsed());

            checkColumnGroup(cols.getColArray(0), 4, 7);
            checkColumnGroup(cols.getColArray(1), 8, 8, false, false);
            checkColumnGroup(cols.getColArray(2), 9, 9); // false, true
            checkColumnGroupIsCollapsed(cols.getColArray(3), 10, 11); // true, true
            checkColumnGroup(cols.getColArray(4), 12, 12); // false, true
            checkColumnGroup(cols.getColArray(5), 13, 13, false, false);

            //DOCUMENTARE MEGLIO IL DISCORSO DEL LIVELLO
            //collapse - 3
            sheet1.setColumnGroupCollapsed(10, true);
            assertEquals(6, cols.sizeOfColArray());
            checkColumnGroup(cols.getColArray(0), 4, 7);
            checkColumnGroup(cols.getColArray(1), 8, 8, false, false);
            checkColumnGroup(cols.getColArray(2), 9, 9); // false, true
            checkColumnGroupIsCollapsed(cols.getColArray(3), 10, 11); // true, true
            checkColumnGroup(cols.getColArray(4), 12, 12); // false, true
            checkColumnGroup(cols.getColArray(5), 13, 13, false, false);


            //expand - 3
            sheet1.setColumnGroupCollapsed(10, false);
            assertEquals(6, cols.sizeOfColArray());
            assertFalse(cols.getColArray(0).getHidden());
            assertFalse(cols.getColArray(5).getHidden());
            assertFalse(cols.getColArray(4).isSetCollapsed());

            // write out and give back
            // Save and re-load
            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet1 = wb2.getSheetAt(0);
                cols = sheet1.getCTWorksheet().getColsArray(0);

                assertEquals(6, cols.sizeOfColArray());
                checkColumnGroup(cols.getColArray(0), 4, 7, false, true);
                checkColumnGroup(cols.getColArray(1), 8, 8, false, true);
                checkColumnGroup(cols.getColArray(2), 9, 9, false, true);
                checkColumnGroup(cols.getColArray(3), 10, 11, false, true);
                checkColumnGroup(cols.getColArray(4), 12, 12, false, true);
                checkColumnGroup(cols.getColArray(5), 13, 13, false, true);
            }
        }
    }

    /**
     * Verify that column groups were created correctly after Sheet.groupColumn
     *
     * @param col the column group xml bean
     * @param fromColumnIndex 0-indexed
     * @param toColumnIndex 0-indexed
     */
    @SuppressWarnings("SameParameterValue")
    private static void checkColumnGroup(
            CTCol col,
            int fromColumnIndex, int toColumnIndex,
            boolean isSetHidden, boolean isSetCollapsed
            ) {
        assertEquals(fromColumnIndex, col.getMin() - 1, "from column index"); // 1 based
        assertEquals(toColumnIndex, col.getMax() - 1, "to column index"); // 1 based
        assertEquals(isSetHidden, col.isSetHidden(), "isSetHidden");
        assertEquals(isSetCollapsed, col.isSetCollapsed(), "isSetCollapsed"); //not necessarily set
    }

    /**
     * Verify that column groups were created correctly after Sheet.groupColumn
     *
     * @param col the column group xml bean
     * @param fromColumnIndex 0-indexed
     * @param toColumnIndex 0-indexed
     */
    private static void checkColumnGroup(CTCol col, int fromColumnIndex, int toColumnIndex) {
        assertEquals(fromColumnIndex, col.getMin() - 1, "from column index"); // 1 based
        assertEquals(toColumnIndex, col.getMax() - 1, "to column index"); // 1 based
        assertFalse(col.isSetHidden(), "isSetHidden");
        assertTrue(col.isSetCollapsed(), "isSetCollapsed"); //not necessarily set
    }

    /**
     * Verify that column groups were created correctly after Sheet.groupColumn
     *
     * @param col the column group xml bean
     * @param fromColumnIndex 0-indexed
     * @param toColumnIndex 0-indexed
     */
    private static void checkColumnGroupIsCollapsed(CTCol col, int fromColumnIndex, int toColumnIndex) {
        assertEquals(fromColumnIndex, col.getMin() - 1, "from column index"); // 1 based
        assertEquals(toColumnIndex, col.getMax() - 1, "to column index"); // 1 based
        assertTrue(col.isSetHidden(), "isSetHidden");
        assertTrue(col.isSetCollapsed(), "isSetCollapsed");
        //assertTrue(col.getCollapsed(), "getCollapsed");
    }

    /**
     * Verify that column groups were created correctly after Sheet.groupColumn
     *
     * @param col the column group xml bean
     * @param fromColumnIndex 0-indexed
     * @param toColumnIndex 0-indexed
     */
    @SuppressWarnings("SameParameterValue")
    private static void checkColumnGroupIsExpanded(CTCol col, int fromColumnIndex, int toColumnIndex) {
        assertEquals(fromColumnIndex, col.getMin() - 1, "from column index"); // 1 based
        assertEquals(toColumnIndex, col.getMax() - 1, "to column index"); // 1 based
        assertFalse(col.isSetHidden(), "isSetHidden");
        assertTrue(col.isSetCollapsed(), "isSetCollapsed");
        //assertTrue(!col.isSetCollapsed() || !col.getCollapsed(), "isSetCollapsed");
        //assertFalse(col.getCollapsed(), "getCollapsed");
    }

    /**
     * TODO - while this is internally consistent, I'm not
     *  completely clear in all cases what it's supposed to
     *  be doing... Someone who understands the goals a little
     *  better should really review this!
     */
    @Test
    void setRowGroupCollapsed() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet1 = wb1.createSheet();

        sheet1.groupRow( 5, 14 );
        sheet1.groupRow( 7, 14 );
        sheet1.groupRow( 16, 19 );

        assertEquals(14,sheet1.getPhysicalNumberOfRows());
        assertFalse(sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(6).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(7).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(9).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(14).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(16).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(18).getCTRow().isSetHidden());

        //collapsed
        sheet1.setRowGroupCollapsed( 7, true );

        assertFalse(sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(6).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(7).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(9).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(14).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(16).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(18).getCTRow().isSetHidden());

        //expanded
        sheet1.setRowGroupCollapsed( 7, false );

        assertFalse(sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(6).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(7).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(9).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(14).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(16).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(18).getCTRow().isSetHidden());


        // Save and re-load
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet1 = wb2.getSheetAt(0);

        assertFalse(sheet1.getRow(6).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(6).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(7).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(7).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(9).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(9).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(14).getCTRow().isSetCollapsed());
        assertTrue (sheet1.getRow(14).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(16).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(16).getCTRow().isSetHidden());
        assertFalse(sheet1.getRow(18).getCTRow().isSetCollapsed());
        assertFalse(sheet1.getRow(18).getCTRow().isSetHidden());

        wb2.close();
    }

    /**
     * Get / Set column width and check the actual values of the underlying XML beans
     */
    @Test
    void columnWidth_lowlevel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnWidth(1, 22 * 256);
        assertEquals(22 * 256, sheet.getColumnWidth(1));

        // Now check the low level stuff, and check that's all
        //  been set correctly
        CTWorksheet cts = sheet.getCTWorksheet();

        assertEquals(1, cts.sizeOfColsArray());
        CTCols cols = cts.getColsArray(0);
        assertEquals(1, cols.sizeOfColArray());
        CTCol col = cols.getColArray(0);

        // XML is 1 based, POI is 0 based
        assertEquals(2, col.getMin());
        assertEquals(2, col.getMax());
        assertEquals(22.0, col.getWidth(), 0.0);
        assertTrue(col.getCustomWidth());

        // Now set another
        sheet.setColumnWidth(3, 33 * 256);

        assertEquals(1, cts.sizeOfColsArray());
        cols = cts.getColsArray(0);
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

        workbook.close();
    }

    /**
     * Setting width of a column included in a column span
     */
    @Test
    void bug47862() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("47862.xlsx");
        XSSFSheet sheet = wb1.getSheetAt(0);
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
            assertEquals(cw[i]*256L, sheet.getColumnWidth(i));
            assertEquals(cw[i], cols.getColArray(i).getWidth(), 0.0);
        }

        //serialize and check again
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        cols = sheet.getCTWorksheet().getColsArray(0);
        assertEquals(5, cols.sizeOfColArray());
        for (int i = 0; i < 5; i++) {
            assertEquals(cw[i]*256L, sheet.getColumnWidth(i));
            assertEquals(cw[i], cols.getColArray(i).getWidth(), 0.0);
        }

        wb2.close();
    }

    /**
     * Hiding a column included in a column span
     */
    @Test
    void bug47804() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("47804.xlsx");
        XSSFSheet sheet = wb1.getSheetAt(0);
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
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        assertTrue(sheet.isColumnHidden(2));
        assertTrue(sheet.isColumnHidden(6));
        assertFalse(sheet.isColumnHidden(1));
        assertFalse(sheet.isColumnHidden(3));
        assertFalse(sheet.isColumnHidden(4));
        assertFalse(sheet.isColumnHidden(5));

        wb2.close();
    }

    @Test
    void commentsTable() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet1 = wb1.createSheet();
        CommentsTable comment1 = sheet1.getCommentsTable(false);
        assertNull(comment1);

        comment1 = sheet1.getCommentsTable(true);
        assertNotNull(comment1);
        assertEquals("/xl/comments1.xml", comment1.getPackagePart().getPartName().getName());

        assertSame(comment1, sheet1.getCommentsTable(true));

        //second sheet
        XSSFSheet sheet2 = wb1.createSheet();
        CommentsTable comment2 = sheet2.getCommentsTable(false);
        assertNull(comment2);

        comment2 = sheet2.getCommentsTable(true);
        assertNotNull(comment2);

        assertSame(comment2, sheet2.getCommentsTable(true));
        assertEquals("/xl/comments2.xml", comment2.getPackagePart().getPartName().getName());

        //comment1 and  comment2 are different objects
        assertNotSame(comment1, comment2);
        wb1.close();

        //now test against a workbook containing cell comments
        XSSFWorkbook wb2 = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
        sheet1 = wb2.getSheetAt(0);
        comment1 = sheet1.getCommentsTable(true);
        assertNotNull(comment1);
        assertEquals("/xl/comments1.xml", comment1.getPackagePart().getPartName().getName());
        assertSame(comment1, sheet1.getCommentsTable(true));

        wb2.close();
    }

    /**
     * Rows and cells can be created in random order,
     * but CTRows are kept in ascending order
     */
    @Override
    @Test
    protected void createRow() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
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

        //rows are sorted: {0, 1, 2}
        assertEquals(4, xrow[0].sizeOfCArray());
        assertEquals(1, xrow[0].getR());
        assertEquals(xrow[0], row3.getCTRow());

        assertEquals(3, xrow[1].sizeOfCArray());
        assertEquals(2, xrow[1].getR());
        assertEquals(xrow[1], row2.getCTRow());

        assertEquals(2, xrow[2].sizeOfCArray());
        assertEquals(3, xrow[2].getR());
        assertEquals(xrow[2], row1.getCTRow());

        CTCell[] xcell = xrow[0].getCArray();
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

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        wsh = sheet.getCTWorksheet();
        assertNotNull(wsh);
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

        wb2.close();
    }

    @Test
    void setAutoFilter() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("new sheet");
        sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D100"));

        assertEquals("A1:D100", sheet.getCTWorksheet().getAutoFilter().getRef());

        // auto-filter must be registered in workboook.xml, see Bugzilla 50315
        XSSFName nm = wb.getBuiltInName(XSSFName.BUILTIN_FILTER_DB, 0);
        assertNotNull(nm);

        assertEquals(0, nm.getCTName().getLocalSheetId());
        assertTrue(nm.getCTName().getHidden());
        assertEquals("_xlnm._FilterDatabase", nm.getCTName().getName());
        assertEquals("'new sheet'!$A$1:$D$100", nm.getCTName().getStringValue());

        wb.close();
    }

    @Test
    void protectSheet_lowlevel() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        CTSheetProtection pr = sheet.getCTWorksheet().getSheetProtection();
        assertNull(pr, "CTSheetProtection should be null by default");
        String password = "Test";
        sheet.protectSheet(password);
        pr = sheet.getCTWorksheet().getSheetProtection();
        assertNotNull(pr, "CTSheetProtection should be not null");
        assertTrue(pr.isSetSheet(), "sheet protection should be on");
        assertTrue(pr.isSetObjects(), "object protection should be on");
        assertTrue(pr.isSetScenarios(), "scenario protection should be on");
        int hashVal = CryptoFunctions.createXorVerifier1(password);
        int actualVal = Integer.parseInt(pr.xgetPassword().getStringValue(),16);
        assertEquals(hashVal, actualVal, "well known value for top secret hash should match");

        sheet.protectSheet(null);
        assertNull(sheet.getCTWorksheet().getSheetProtection(), "protectSheet(null) should unset CTSheetProtection");

        wb.close();
    }

    @Test
    void protectSheet_emptyPassword() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        CTSheetProtection pr = sheet.getCTWorksheet().getSheetProtection();
        assertNull(pr, "CTSheetProtection should be null by default");
        String password = "";
        sheet.protectSheet(password);
        pr = sheet.getCTWorksheet().getSheetProtection();
        assertNotNull(pr, "CTSheetProtection should be not null");
        assertTrue(pr.isSetSheet(), "sheet protection should be on");
        assertTrue(pr.isSetObjects(), "object protection should be on");
        assertTrue(pr.isSetScenarios(), "scenario protection should be on");
        int hashVal = CryptoFunctions.createXorVerifier1(password);
        STUnsignedShortHex xpassword = pr.xgetPassword();
        int actualVal = Integer.parseInt(xpassword.getStringValue(),16);
        assertEquals(hashVal, actualVal, "well known value for top secret hash should match");

        sheet.protectSheet(null);
        assertNull(sheet.getCTWorksheet().getSheetProtection(), "protectSheet(null) should unset CTSheetProtection");

        wb.close();
    }

    @Test
    void protectSheet_lowlevel_2013() throws IOException {
        String password = "test";
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet xs = wb1.createSheet();
        xs.setSheetPassword(password, HashAlgorithm.sha384);
        XSSFWorkbook wb2 = writeOutAndReadBack(wb1);
        wb1.close();
        assertTrue(wb2.getSheetAt(0).validateSheetPassword(password));
        wb2.close();

        XSSFWorkbook wb3 = openSampleWorkbook("workbookProtection-sheet_password-2013.xlsx");
        assertTrue(wb3.getSheetAt(0).validateSheetPassword("pwd"));
        wb3.close();
    }


    @Test
    void bug49966() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("49966.xlsx");
        CalculationChain calcChain = wb1.getCalculationChain();
        assertNotNull(wb1.getCalculationChain());
        assertEquals(3, calcChain.getCTCalcChain().sizeOfCArray());

        XSSFSheet sheet = wb1.getSheetAt(0);
        XSSFRow row = sheet.getRow(0);

        sheet.removeRow(row);
        assertEquals(0, calcChain.getCTCalcChain().sizeOfCArray(), "XSSFSheet#removeRow did not clear calcChain entries");

        //calcChain should be gone
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        assertNull(wb2.getCalculationChain());
        wb2.close();
    }

    /**
     * See bug #50829 test data tables
     */
    @Test
    void tables() throws IOException {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTable.xlsx");
       assertEquals(3, wb.getNumberOfSheets());

       // Check the table sheet
       XSSFSheet s1 = wb.getSheetAt(0);
       assertEquals("a", s1.getRow(0).getCell(0).getRichStringCellValue().toString());
       assertEquals(1.0, s1.getRow(1).getCell(0).getNumericCellValue(), 0);

       List<XSSFTable> tables = s1.getTables();
       assertNotNull(tables);
       assertEquals(1, tables.size());

       XSSFTable table = tables.get(0);
       assertEquals("Tabella1", table.getName());
       assertEquals("Tabella1", table.getDisplayName());

       // And the others
       XSSFSheet s2 = wb.getSheetAt(1);
       assertEquals(0, s2.getTables().size());
       XSSFSheet s3 = wb.getSheetAt(2);
       assertEquals(0, s3.getTables().size());
       wb.close();
    }

    /**
     * Test to trigger OOXML-LITE generating to include org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetCalcPr
     */
    @Test
    void setForceFormulaRecalculation() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet("Sheet 1");

        assertFalse(sheet.getForceFormulaRecalculation());

        // Set
        sheet.setForceFormulaRecalculation(true);
        assertTrue(sheet.getForceFormulaRecalculation());

        // calcMode="manual" is unset when forceFormulaRecalculation=true
        CTCalcPr calcPr = wb1.getCTWorkbook().addNewCalcPr();
        calcPr.setCalcMode(STCalcMode.MANUAL);
        sheet.setForceFormulaRecalculation(true);
        assertEquals(STCalcMode.AUTO, calcPr.getCalcMode());

        // Check
        sheet.setForceFormulaRecalculation(false);
        assertFalse(sheet.getForceFormulaRecalculation());


        // Save, re-load, and re-check
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheet("Sheet 1");
        assertFalse(sheet.getForceFormulaRecalculation());
        wb2.close();
    }

    @Test
    void bug54607() throws IOException {
        // run with the file provided in the Bug-Report
        runGetTopRow("54607.xlsx", true, 1, 0, 0);
        runGetLeftCol("54607.xlsx", true, 0, 0, 0);

        // run with some other flie to see
        runGetTopRow("54436.xlsx", true, 0);
        runGetLeftCol("54436.xlsx", true, 0);
        runGetTopRow("TwoSheetsNoneHidden.xlsx", true, 0, 0);
        runGetLeftCol("TwoSheetsNoneHidden.xlsx", true, 0, 0);
        runGetTopRow("TwoSheetsNoneHidden.xls", false, 0, 0);
        runGetLeftCol("TwoSheetsNoneHidden.xls", false, 0, 0);
    }

    private void runGetTopRow(String file, boolean isXSSF, int... topRows) throws IOException {
        final Workbook wb = (isXSSF)
            ? XSSFTestDataSamples.openSampleWorkbook(file)
            : HSSFTestDataSamples.openSampleWorkbook(file);

        for (int si = 0; si < wb.getNumberOfSheets(); si++) {
            Sheet sh = wb.getSheetAt(si);
            assertNotNull(sh.getSheetName());
            assertEquals(topRows[si], sh.getTopRow(), "Did not match for sheet " + si);
        }

        // for XSSF also test with SXSSF
        if (isXSSF) {
            Workbook swb = new SXSSFWorkbook((XSSFWorkbook) wb);
            for (int si = 0; si < swb.getNumberOfSheets(); si++) {
                Sheet sh = swb.getSheetAt(si);
                assertNotNull(sh.getSheetName());
                assertEquals(topRows[si], sh.getTopRow(), "Did not match for sheet " + si);
            }
            swb.close();
        }

        wb.close();
    }

    private void runGetLeftCol(String file, boolean isXSSF, int... topRows) throws IOException {
        final Workbook wb = (isXSSF)
            ? XSSFTestDataSamples.openSampleWorkbook(file)
            : HSSFTestDataSamples.openSampleWorkbook(file);

        for (int si = 0; si < wb.getNumberOfSheets(); si++) {
            Sheet sh = wb.getSheetAt(si);
            assertNotNull(sh.getSheetName());
            assertEquals(topRows[si], sh.getLeftCol(), "Did not match for sheet " + si);
        }

        // for XSSF also test with SXSSF
        if(isXSSF) {
            Workbook swb = new SXSSFWorkbook((XSSFWorkbook) wb);
            for (int si = 0; si < swb.getNumberOfSheets(); si++) {
                Sheet sh = swb.getSheetAt(si);
                assertNotNull(sh.getSheetName());
                assertEquals(topRows[si], sh.getLeftCol(), "Did not match for sheet " + si);
            }
            swb.close();
        }

        wb.close();
    }

    @Test
    void bug55745() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55745.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);
            List<XSSFTable> tables = sheet.getTables();
            assertEquals(8, tables.size(), "Sheet should contain 8 tables");
            assertNotNull(sheet.getCommentsTable(false), "Sheet should contain a comments table");
        }
    }

    @Test
    void bug55723b() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();

        // stored with a special name
        assertNull(wb.getBuiltInName(XSSFName.BUILTIN_FILTER_DB, 0));

        CellRangeAddress range = CellRangeAddress.valueOf("A:B");
        AutoFilter filter = sheet.setAutoFilter(range);
        assertNotNull(filter);

        // stored with a special name
        XSSFName name = wb.getBuiltInName(XSSFName.BUILTIN_FILTER_DB, 0);
        assertNotNull(name);
        assertEquals("Sheet0!$A:$B", name.getRefersToFormula());

        range = CellRangeAddress.valueOf("B:C");
        filter = sheet.setAutoFilter(range);
        assertNotNull(filter);

        // stored with a special name
        name = wb.getBuiltInName(XSSFName.BUILTIN_FILTER_DB, 0);
        assertNotNull(name);
        assertEquals("Sheet0!$B:$C", name.getRefersToFormula());

        wb.close();
    }

    @Timeout(value = 180, unit = SECONDS)
    @Test
    void bug51585() throws IOException {
        XSSFTestDataSamples.openSampleWorkbook("51585.xlsx").close();
    }

    private XSSFWorkbook setupSheet(){
        //set up workbook
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        Row row1 = sheet.createRow(0);
        Cell cell = row1.createCell(0);
        cell.setCellValue("Names");
        Cell cell2 = row1.createCell(1);
        cell2.setCellValue("#");

        Row row2 = sheet.createRow(1);
        Cell cell3 = row2.createCell(0);
        cell3.setCellValue("Jane");
        Cell cell4 = row2.createCell(1);
        cell4.setCellValue(3);

        Row row3 = sheet.createRow(2);
        Cell cell5 = row3.createCell(0);
        cell5.setCellValue("John");
        Cell cell6 = row3.createCell(1);
        cell6.setCellValue(3);

        return wb;
    }

    @Test
    void testCreateTwoPivotTablesInOneSheet() throws IOException {
        XSSFWorkbook wb = setupSheet();
        XSSFSheet sheet = wb.getSheetAt(0);

        assertNotNull(wb);
        assertNotNull(sheet);
        XSSFPivotTable pivotTable = sheet.createPivotTable(wb.getCreationHelper().createAreaReference("A1:B2"),
                new CellReference("H5"));
        assertNotNull(pivotTable);
        assertTrue(wb.getPivotTables().size() > 0);
        XSSFPivotTable pivotTable2 = sheet.createPivotTable(wb.getCreationHelper().createAreaReference("A1:B2"),
                new CellReference("L5"), sheet);
        assertNotNull(pivotTable2);
        assertTrue(wb.getPivotTables().size() > 1);
        wb.close();
    }

    @Test
    void testCreateTwoPivotTablesInTwoSheets() throws IOException {
        XSSFWorkbook wb = setupSheet();
        XSSFSheet sheet = wb.getSheetAt(0);

        assertNotNull(wb);
        assertNotNull(sheet);
        XSSFPivotTable pivotTable = sheet.createPivotTable(wb.getCreationHelper().createAreaReference("A1:B2"), new CellReference("H5"));
        assertNotNull(pivotTable);
        assertTrue(wb.getPivotTables().size() > 0);
        assertNotNull(wb);
        XSSFSheet sheet2 = wb.createSheet();
        XSSFPivotTable pivotTable2 = sheet2.createPivotTable(wb.getCreationHelper().createAreaReference("A1:B2"),
                new CellReference("H5"), sheet);
        assertNotNull(pivotTable2);
        assertTrue(wb.getPivotTables().size() > 1);
        wb.close();
    }

    @Test
    void testCreatePivotTable() throws IOException {
        XSSFWorkbook wb = setupSheet();
        XSSFSheet sheet = wb.getSheetAt(0);

        assertNotNull(wb);
        assertNotNull(sheet);
        XSSFPivotTable pivotTable = sheet.createPivotTable(wb.getCreationHelper().createAreaReference("A1:B2"), new CellReference("H5"));
        assertNotNull(pivotTable);
        assertTrue(wb.getPivotTables().size() > 0);
        wb.close();
    }

    @Test
    void testCreatePivotTableInOtherSheetThanDataSheet() throws IOException {
        XSSFWorkbook wb = setupSheet();
        XSSFSheet sheet1 = wb.getSheetAt(0);
        XSSFSheet sheet2 = wb.createSheet();

        XSSFPivotTable pivotTable = sheet2.createPivotTable
                (wb.getCreationHelper().createAreaReference("A1:B2"), new CellReference("H5"), sheet1);
        assertEquals(0, pivotTable.getRowLabelColumns().size());

        assertEquals(1, wb.getPivotTables().size());
        assertEquals(0, sheet1.getPivotTables().size());
        assertEquals(1, sheet2.getPivotTables().size());
        wb.close();
    }

    @Test
    void testCreatePivotTableInOtherSheetThanDataSheetUsingAreaReference() throws IOException {
        XSSFWorkbook wb = setupSheet();
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFSheet sheet2 = wb.createSheet("TEST");

        XSSFPivotTable pivotTable = sheet2.createPivotTable(
                wb.getCreationHelper().createAreaReference(sheet.getSheetName()+"!A$1:B$2"),
                new CellReference("H5"));
        assertEquals(0, pivotTable.getRowLabelColumns().size());
        wb.close();
    }

    @Test
    void testCreatePivotTableWithConflictingDataSheets() throws IOException {
        try (XSSFWorkbook wb = setupSheet()) {
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFSheet sheet2 = wb.createSheet("TEST");
            CellReference cr = new CellReference("H5");
            AreaReference ar = wb.getCreationHelper().createAreaReference(sheet.getSheetName() + "!A$1:B$2");

            assertThrows(IllegalArgumentException.class, () -> sheet2.createPivotTable(ar, cr, sheet2));
        }
    }

    @Test
    void testReadFails() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            // Throws exception because we cannot read here
            assertThrows(POIXMLException.class, sheet::onDocumentRead);
        }
    }

    /**
     * This would be better off as a testable example rather than a simple unit test
     * since Sheet.createComment() was deprecated and removed.
     * https://poi.apache.org/spreadsheet/quick-guide.html#CellComments
     * Feel free to relocated or delete this unit test if it doesn't belong here.
     */
    @Test
    void testCreateComment() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
        XSSFSheet sheet = wb.createSheet();
        XSSFComment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        assertNotNull(comment);
        wb.close();
    }

    private void testCopyOneRow(String copyRowsTestWorkbook) throws IOException {
        final double FLOAT_PRECISION = 1e-9;
        final XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook(copyRowsTestWorkbook);
        final XSSFSheet sheet = wb.getSheetAt(0);
        final CellCopyPolicy defaultCopyPolicy = new CellCopyPolicy();
        sheet.copyRows(1, 1, 6, defaultCopyPolicy);

        final Row srcRow = sheet.getRow(1);
        final Row destRow = sheet.getRow(6);
        int col = 0;
        Cell cell;

        cell = CellUtil.getCell(destRow, col++);
        assertEquals("Source row ->", cell.getStringCellValue());

        // Style
        cell = CellUtil.getCell(destRow, col++);
        assertEquals("Red", cell.getStringCellValue(), "[Style] B7 cell value");
        assertEquals(CellUtil.getCell(srcRow, 1).getCellStyle(), cell.getCellStyle(), "[Style] B7 cell style");

        // Blank
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.BLANK, cell.getCellType(), "[Blank] C7 cell type");

        // Error
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.ERROR, cell.getCellType(), "[Error] D7 cell type");
        final FormulaError error = FormulaError.forInt(cell.getErrorCellValue());
        //FIXME: XSSFCell and HSSFCell expose different interfaces. getErrorCellString would be helpful here
        assertEquals(FormulaError.NA, error, "[Error] D7 cell value");

        // Date
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Date] E7 cell type");
        final Date date = LocaleUtil.getLocaleCalendar(2000, Calendar.JANUARY, 1).getTime();
        assertEquals(date, cell.getDateCellValue(), "[Date] E7 cell value");

        // Boolean
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.BOOLEAN, cell.getCellType(), "[Boolean] F7 cell type");
        assertTrue(cell.getBooleanCellValue(), "[Boolean] F7 cell value");

        // String
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.STRING, cell.getCellType(), "[String] G7 cell type");
        assertEquals("Hello", cell.getStringCellValue(), "[String] G7 cell value");

        // Int
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Int] H7 cell type");
        assertEquals(15, (int) cell.getNumericCellValue(), "[Int] H7 cell value");

        // Float
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Float] I7 cell type");
        assertEquals(12.5, cell.getNumericCellValue(), FLOAT_PRECISION, "[Float] I7 cell value");

        // Cell Formula
        cell = CellUtil.getCell(destRow, col++);
        assertEquals("Sheet1!J7", new CellReference(cell).formatAsString());
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula] J7 cell type");
        assertEquals("5+2", cell.getCellFormula(), "[Cell Formula] J7 cell formula");
        //System.out.println("Cell formula evaluation currently unsupported");

        // Cell Formula with Reference
        // Formula row references should be adjusted by destRowNum-srcRowNum
        cell = CellUtil.getCell(destRow, col++);
        assertEquals("Sheet1!K7", new CellReference(cell).formatAsString());
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference] K7 cell type");
        assertEquals("J7+H$2", cell.getCellFormula(), "[Cell Formula with Reference] K7 cell formula");

        // Cell Formula with Reference spanning multiple rows
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference spanning multiple rows] L7 cell type");
        assertEquals("G7&\" \"&G8", cell.getCellFormula(), "[Cell Formula with Reference spanning multiple rows] L7 cell formula");

        // Cell Formula with Reference spanning multiple rows
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Area Reference] M7 cell type");
        assertEquals("SUM(H7:I8)", cell.getCellFormula(), "[Cell Formula with Area Reference] M7 cell formula");

        // Array Formula
        cell = CellUtil.getCell(destRow, col++);
        //System.out.println("Array formulas currently unsupported");
        // FIXME: Array Formula set with Sheet.setArrayFormula() instead of cell.setFormula()
        /*
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Array Formula] N7 cell type");
        assertEquals("{SUM(H7:J7*{1,2,3})}", cell.getCellFormula(), "[Array Formula] N7 cell formula");
        */

        // Data Format
        cell = CellUtil.getCell(destRow, col++);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Data Format] O7 cell type");
        assertEquals(100.20, cell.getNumericCellValue(), FLOAT_PRECISION, "[Data Format] O7 cell value");
        //FIXME: currently fails
        final String moneyFormat = "\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)";
        assertEquals(moneyFormat, cell.getCellStyle().getDataFormatString(), "[Data Format] O7 data format");

        // Merged
        cell = CellUtil.getCell(destRow, col);
        assertEquals("Merged cells", cell.getStringCellValue(), "[Merged] P7:Q7 cell value");
        assertTrue(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("P7:Q7")), "[Merged] P7:Q7 merged region");

        // Merged across multiple rows
        // Microsoft Excel 2013 does not copy a merged region unless all rows of
        // the source merged region are selected
        // POI's behavior should match this behavior
        col += 2;
        cell = CellUtil.getCell(destRow, col);
        // Note: this behavior deviates from Microsoft Excel,
        // which will not overwrite a cell in destination row if merged region extends beyond the copied row.
        // The Excel way would require:
        //assertEquals("[Merged across multiple rows] R7:S8 merged region", "Should NOT be overwritten", cell.getStringCellValue());
        //assertFalse("[Merged across multiple rows] R7:S8 merged region",
        //        sheet.getMergedRegions().contains(CellRangeAddress.valueOf("R7:S8")));
        // As currently implemented, cell value is copied but merged region is not copied
        assertEquals("Merged cells across multiple rows", cell.getStringCellValue(), "[Merged across multiple rows] R7:S8 cell value");
        // shouldn't do 1-row merge
        assertFalse(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("R7:S7")), "[Merged across multiple rows] R7:S7 merged region (one row)");
        //shouldn't do 2-row merge
        assertFalse(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("R7:S8")), "[Merged across multiple rows] R7:S8 merged region");

        // Make sure other rows are blank (off-by-one errors)
        assertNull(sheet.getRow(5));
        assertNull(sheet.getRow(7));

        wb.close();
    }

    private void testCopyMultipleRows(String copyRowsTestWorkbook) throws IOException {
        final double FLOAT_PRECISION = 1e-9;
        final XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook(copyRowsTestWorkbook);
        final XSSFSheet sheet = wb.getSheetAt(0);
        final CellCopyPolicy defaultCopyPolicy = new CellCopyPolicy();
        sheet.copyRows(0, 3, 8, defaultCopyPolicy);

        sheet.getRow(0);
        final Row srcRow1 = sheet.getRow(1);
        final Row srcRow2 = sheet.getRow(2);
        final Row srcRow3 = sheet.getRow(3);
        final Row destHeaderRow = sheet.getRow(8);
        final Row destRow1 = sheet.getRow(9);
        final Row destRow2 = sheet.getRow(10);
        final Row destRow3 = sheet.getRow(11);
        int col = 0;
        Cell cell;

        // Header row should be copied
        assertNotNull(destHeaderRow);

        // Data rows
        cell = CellUtil.getCell(destRow1, col);
        assertEquals("Source row ->", cell.getStringCellValue());

        // Style
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals("Red", cell.getStringCellValue(), "[Style] B10 cell value");
        assertEquals(CellUtil.getCell(srcRow1, 1).getCellStyle(), cell.getCellStyle(), "[Style] B10 cell style");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals("Blue", cell.getStringCellValue(), "[Style] B11 cell value");
        assertEquals(CellUtil.getCell(srcRow2, 1).getCellStyle(), cell.getCellStyle(), "[Style] B11 cell style");

        // Blank
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.BLANK, cell.getCellType(), "[Blank] C10 cell type");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.BLANK, cell.getCellType(), "[Blank] C11 cell type");

        // Error
        col++;
        cell = CellUtil.getCell(destRow1, col);
        FormulaError error = FormulaError.forInt(cell.getErrorCellValue());
        //FIXME: XSSFCell and HSSFCell expose different interfaces. getErrorCellString would be helpful here
        assertEquals(CellType.ERROR, cell.getCellType(), "[Error] D10 cell type");
        assertEquals(FormulaError.NA, error, "[Error] D10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        error = FormulaError.forInt(cell.getErrorCellValue());
        //FIXME: XSSFCell and HSSFCell expose different interfaces. getErrorCellString would be helpful here
        assertEquals(CellType.ERROR, cell.getCellType(), "[Error] D11 cell type");
        assertEquals(FormulaError.NAME, error, "[Error] D11 cell value");

        // Date
        col++;
        cell = CellUtil.getCell(destRow1, col);
        Date date = LocaleUtil.getLocaleCalendar(2000, Calendar.JANUARY, 1).getTime();
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Date] E10 cell type");
        assertEquals(date, cell.getDateCellValue(), "[Date] E10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        date = LocaleUtil.getLocaleCalendar(2000, Calendar.JANUARY, 2).getTime();
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Date] E11 cell type");
        assertEquals(date, cell.getDateCellValue(), "[Date] E11 cell value");

        // Boolean
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.BOOLEAN, cell.getCellType(), "[Boolean] F10 cell type");
        assertTrue(cell.getBooleanCellValue(), "[Boolean] F10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.BOOLEAN, cell.getCellType(), "[Boolean] F11 cell type");
        assertFalse(cell.getBooleanCellValue(), "[Boolean] F11 cell value");

        // String
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.STRING, cell.getCellType(), "[String] G10 cell type");
        assertEquals("Hello", cell.getStringCellValue(), "[String] G10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.STRING, cell.getCellType(), "[String] G11 cell type");
        assertEquals("World", cell.getStringCellValue(), "[String] G11 cell value");

        // Int
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Int] H10 cell type");
        assertEquals(15, (int) cell.getNumericCellValue(), "[Int] H10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Int] H11 cell type");
        assertEquals(42, (int) cell.getNumericCellValue(), "[Int] H11 cell value");

        // Float
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Float] I10 cell type");
        assertEquals(12.5, cell.getNumericCellValue(), FLOAT_PRECISION, "[Float] I10 cell value");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Float] I11 cell type");
        assertEquals(5.5, cell.getNumericCellValue(), FLOAT_PRECISION, "[Float] I11 cell value");

        // Cell Formula
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula] J10 cell type");
        assertEquals("5+2", cell.getCellFormula(), "[Cell Formula] J10 cell formula");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula] J11 cell type");
        assertEquals("6+18", cell.getCellFormula(), "[Cell Formula] J11 cell formula");

        // Cell Formula with Reference
        col++;
        // Formula row references should be adjusted by destRowNum-srcRowNum
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference] K10 cell type");
        assertEquals("J10+H$2", cell.getCellFormula(), "[Cell Formula with Reference] K10 cell formula");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference] K11 cell type");
        assertEquals("J11+H$2", cell.getCellFormula(), "[Cell Formula with Reference] K11 cell formula");

        // Cell Formula with Reference spanning multiple rows
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference spanning multiple rows] L10 cell type");
        assertEquals("G10&\" \"&G11", cell.getCellFormula(), "[Cell Formula with Reference spanning multiple rows] L10 cell formula");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Reference spanning multiple rows] L11 cell type");
        assertEquals("G11&\" \"&G12", cell.getCellFormula(), "[Cell Formula with Reference spanning multiple rows] L11 cell formula");

        // Cell Formula with Area Reference
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Area Reference] M10 cell type");
        assertEquals("SUM(H10:I11)", cell.getCellFormula(), "[Cell Formula with Area Reference] M10 cell formula");

        cell = CellUtil.getCell(destRow2, col);
        // Also acceptable: SUM($H10:I$3), but this AreaReference isn't in ascending order
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Cell Formula with Area Reference] M11 cell type");
        assertEquals("SUM($H$3:I10)", cell.getCellFormula(), "[Cell Formula with Area Reference] M11 cell formula");

        // Array Formula
        col++;
        cell = CellUtil.getCell(destRow1, col);
        // System.out.println("Array formulas currently unsupported");
    /*
        // FIXME: Array Formula set with Sheet.setArrayFormula() instead of cell.setFormula()
        assertEquals(CellType.FORMULA, cell.getCellType(), "[Array Formula] N10 cell type");
        assertEquals("{SUM(H10:J10*{1,2,3})}", cell.getCellFormula(), "[Array Formula] N10 cell formula");

        cell = CellUtil.getCell(destRow2, col);
        // FIXME: Array Formula set with Sheet.setArrayFormula() instead of cell.setFormula()
        assertEquals(CellType.FORMULA, cell.getCellType(). "[Array Formula] N11 cell type");
        assertEquals("{SUM(H11:J11*{1,2,3})}", cell.getCellFormula(). "[Array Formula] N11 cell formula");
     */

        // Data Format
        col++;
        cell = CellUtil.getCell(destRow2, col);
        assertEquals(CellType.NUMERIC, cell.getCellType(), "[Data Format] O10 cell type");
        assertEquals(100.20, cell.getNumericCellValue(), FLOAT_PRECISION, "[Data Format] O10 cell value");
        final String moneyFormat = "\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)";
        assertEquals(moneyFormat, cell.getCellStyle().getDataFormatString(), "[Data Format] O10 cell data format");

        // Merged
        col++;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals("Merged cells", cell.getStringCellValue(), "[Merged] P10:Q10 cell value");
        assertTrue(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("P10:Q10")), "[Merged] P10:Q10 merged region");

        cell = CellUtil.getCell(destRow2, col);
        assertEquals("Merged cells", cell.getStringCellValue(), "[Merged] P11:Q11 cell value");
        assertTrue(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("P11:Q11")), "[Merged] P11:Q11 merged region");

        // Should Q10/Q11 be checked?

        // Merged across multiple rows
        // Microsoft Excel 2013 does not copy a merged region unless all rows of
        // the source merged region are selected
        // POI's behavior should match this behavior
        col += 2;
        cell = CellUtil.getCell(destRow1, col);
        assertEquals("Merged cells across multiple rows", cell.getStringCellValue(), "[Merged across multiple rows] R10:S11 cell value");
        assertTrue(sheet.getMergedRegions().contains(CellRangeAddress.valueOf("R10:S11")), "[Merged across multiple rows] R10:S11 merged region");

        // Row 3 (zero-based) was empty, so Row 11 (zero-based) should be empty too.
        if (srcRow3 == null) {
            assertNull(destRow3, "Row 3 was empty, so Row 11 should be empty");
        }

        // Make sure other rows are blank (off-by-one errors)
        assertNull(sheet.getRow(7), "Off-by-one lower edge case"); //one row above destHeaderRow
        assertNull(sheet.getRow(12), "Off-by-one upper edge case"); //one row below destRow3

        wb.close();
    }

    @Test
    void testCopyOneRow() throws IOException {
        testCopyOneRow("XSSFSheet.copyRows.xlsx");
    }

    @Test
    void testCopyMultipleRows() throws IOException {
        testCopyMultipleRows("XSSFSheet.copyRows.xlsx");
    }

    @Test
    void testIgnoredErrors() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CellRangeAddress region = CellRangeAddress.valueOf("B2:D4");
        sheet.addIgnoredErrors(region, IgnoredErrorType.NUMBER_STORED_AS_TEXT);
        final CTIgnoredError ignoredError = sheet.getCTWorksheet().getIgnoredErrors().getIgnoredErrorArray(0);
        assertEquals(1, ignoredError.getSqref().size());
        assertEquals("B2:D4", ignoredError.getSqref().get(0));
        assertTrue(ignoredError.getNumberStoredAsText());

        Map<IgnoredErrorType, Set<CellRangeAddress>> ignoredErrors = sheet.getIgnoredErrors();
        assertEquals(1, ignoredErrors.size());
        assertEquals(1, ignoredErrors.get(IgnoredErrorType.NUMBER_STORED_AS_TEXT).size());
        assertEquals("B2:D4", ignoredErrors.get(IgnoredErrorType.NUMBER_STORED_AS_TEXT).iterator().next().formatAsString());

        workbook.close();
    }

    @Test
    void testIgnoredErrorsMultipleTypes() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CellRangeAddress region = CellRangeAddress.valueOf("B2:D4");
        sheet.addIgnoredErrors(region, IgnoredErrorType.FORMULA, IgnoredErrorType.EVALUATION_ERROR);
        final CTIgnoredError ignoredError = sheet.getCTWorksheet().getIgnoredErrors().getIgnoredErrorArray(0);
        assertEquals(1, ignoredError.getSqref().size());
        assertEquals("B2:D4", ignoredError.getSqref().get(0));
        assertFalse(ignoredError.getNumberStoredAsText());
        assertTrue(ignoredError.getFormula());
        assertTrue(ignoredError.getEvalError());

        Map<IgnoredErrorType, Set<CellRangeAddress>> ignoredErrors = sheet.getIgnoredErrors();
        assertEquals(2, ignoredErrors.size());
        assertEquals(1, ignoredErrors.get(IgnoredErrorType.FORMULA).size());
        assertEquals("B2:D4", ignoredErrors.get(IgnoredErrorType.FORMULA).iterator().next().formatAsString());
        assertEquals(1, ignoredErrors.get(IgnoredErrorType.EVALUATION_ERROR).size());
        assertEquals("B2:D4", ignoredErrors.get(IgnoredErrorType.EVALUATION_ERROR).iterator().next().formatAsString());
        workbook.close();
    }

    @Test
    void testIgnoredErrorsMultipleCalls() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CellRangeAddress region = CellRangeAddress.valueOf("B2:D4");
        // Two calls means two elements, no clever collapsing just yet.
        sheet.addIgnoredErrors(region, IgnoredErrorType.EVALUATION_ERROR);
        sheet.addIgnoredErrors(region, IgnoredErrorType.FORMULA);

        CTIgnoredError ignoredError = sheet.getCTWorksheet().getIgnoredErrors().getIgnoredErrorArray(0);
        assertEquals(1, ignoredError.getSqref().size());
        assertEquals("B2:D4", ignoredError.getSqref().get(0));
        assertFalse(ignoredError.getFormula());
        assertTrue(ignoredError.getEvalError());

        ignoredError = sheet.getCTWorksheet().getIgnoredErrors().getIgnoredErrorArray(1);
        assertEquals(1, ignoredError.getSqref().size());
        assertEquals("B2:D4", ignoredError.getSqref().get(0));
        assertTrue(ignoredError.getFormula());
        assertFalse(ignoredError.getEvalError());

        Map<IgnoredErrorType, Set<CellRangeAddress>> ignoredErrors = sheet.getIgnoredErrors();
        assertEquals(2, ignoredErrors.size());
        assertEquals(1, ignoredErrors.get(IgnoredErrorType.FORMULA).size());
        assertEquals("B2:D4", ignoredErrors.get(IgnoredErrorType.FORMULA).iterator().next().formatAsString());
        assertEquals(1, ignoredErrors.get(IgnoredErrorType.EVALUATION_ERROR).size());
        assertEquals("B2:D4", ignoredErrors.get(IgnoredErrorType.EVALUATION_ERROR).iterator().next().formatAsString());
        workbook.close();
    }

    @Test
    void setTabColor() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            assertTrue(sh.getCTWorksheet().getSheetPr() == null || !sh.getCTWorksheet().getSheetPr().isSetTabColor());
            sh.setTabColor(new XSSFColor(IndexedColors.RED, null));
            assertTrue(sh.getCTWorksheet().getSheetPr().isSetTabColor());
            assertEquals(IndexedColors.RED.index,
                    sh.getCTWorksheet().getSheetPr().getTabColor().getIndexed());
        }
    }

    @Test
    void getTabColor() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            assertTrue(sh.getCTWorksheet().getSheetPr() == null || !sh.getCTWorksheet().getSheetPr().isSetTabColor());
            assertNull(sh.getTabColor());
            sh.setTabColor(new XSSFColor(IndexedColors.RED, null));
            XSSFColor expected = new XSSFColor(IndexedColors.RED, null);
            assertEquals(expected, sh.getTabColor());
        }
    }

    // Test using an existing workbook saved by Excel
    @Test
    void tabColor() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("SheetTabColors.xlsx")) {
            // non-colored sheets do not have a color
            assertNull(wb.getSheet("default").getTabColor());

            // test indexed-colored sheet
            XSSFColor expected = new XSSFColor(IndexedColors.RED, null);
            assertEquals(expected, wb.getSheet("indexedRed").getTabColor());

            // test regular-colored (non-indexed, ARGB) sheet
            expected = XSSFColor.from(CTColor.Factory.newInstance(), wb.getStylesSource().getIndexedColors());
            assertNotNull(expected);
            expected.setARGBHex("FF7F2700");
            assertEquals(expected, wb.getSheet("customOrange").getTabColor());
        }
    }

    /**
     * See bug #52425
     */
    @Test
    void testInsertCommentsToClonedSheet() throws IOException {
    	Workbook wb = XSSFTestDataSamples.openSampleWorkbook("52425.xlsx");
		CreationHelper helper = wb.getCreationHelper();
		Sheet sheet2 = wb.createSheet("Sheet 2");
		Sheet sheet3 = wb.cloneSheet(0);
		wb.setSheetName(2, "Sheet 3");

		// Adding Comment to new created Sheet 2
		addComments(helper, sheet2);
		// Adding Comment to cloned Sheet 3
		addComments(helper, sheet3);

        wb.close();
    }

    private void addComments(CreationHelper helper, Sheet sheet) {
		Drawing<?> drawing = sheet.createDrawingPatriarch();

		for (int i = 0; i < 2; i++) {
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(0);
			anchor.setRow1(i);
			anchor.setCol2(2);
			anchor.setRow2(3 + i);

			Comment comment = drawing.createCellComment(anchor);
			comment.setString(helper.createRichTextString("BugTesting"));

			Row row = sheet.getRow(i);
			if (row == null) {
                row = sheet.createRow(i);
            }
			Cell cell = row.getCell(0);
			if (cell == null) {
                cell = row.createCell(0);
            }

			cell.setCellComment(comment);
		}

    }

    // bug 59687:  XSSFSheet.RemoveRow doesn't handle row gaps properly when removing row comments
    @Test
    void testRemoveRowWithCommentAndGapAbove() throws IOException {
        try (Workbook wb = _testDataProvider.openSampleWorkbook("59687.xlsx")) {
            final Sheet sheet = wb.getSheetAt(0);

            // comment exists
            CellAddress commentCellAddress = new CellAddress("A4");
            assertNotNull(sheet.getCellComment(commentCellAddress));

            assertEquals(1, sheet.getCellComments().size(), "Wrong starting # of comments");

            sheet.removeRow(sheet.getRow(commentCellAddress.getRow()));

            assertEquals(0, sheet.getCellComments().size(), "There should not be any comments left!");
        }
    }

    @Test
    void testGetHeaderFooterProperties() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();

            XSSFHeaderFooterProperties hfProp = sh.getHeaderFooterProperties();
            assertNotNull(hfProp);
        }
    }

    @Test
    void testSheetForceFormulaRecalculationDefaultValues() throws IOException {
        try (Workbook wb = _testDataProvider.openSampleWorkbook("sample.xlsx")){
            for (Sheet s : wb) {
                assertEquals(wb.getForceFormulaRecalculation(),s.getForceFormulaRecalculation());
            }
        }
    }

    @Test
    void testWorkbookSetForceFormulaRecalculation() throws IOException {
        try (Workbook wb = _testDataProvider.openSampleWorkbook("sample.xlsx")){
            wb.setForceFormulaRecalculation(true);
            assertTrue(wb.getForceFormulaRecalculation());
        }
    }

    @Test
    void testNotCascadeWorkbookSetForceFormulaRecalculation() throws IOException {
        try (Workbook wb = _testDataProvider.openSampleWorkbook("sample.xlsx")) {
            // set all sheets to force recalculation
            for (Sheet s : wb) {
                s.setForceFormulaRecalculation(true);
                assertTrue(s.getForceFormulaRecalculation());
            }

            // disable on workbook-level
            wb.setForceFormulaRecalculation(false);

            // on sheet-level, the flag is still set
            for (Sheet s : wb) {
                assertTrue(s.getForceFormulaRecalculation(), "Sheet-level flag is still set to true");
            }
        }
    }
}
