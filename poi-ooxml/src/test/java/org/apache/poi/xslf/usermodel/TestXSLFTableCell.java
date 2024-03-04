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
package org.apache.poi.xslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestXSLFTableCell
{

    @Test
    void testCorrectlyReadsTextRunStylingForCellsWithNoTheme() throws IOException {
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("table-with-no-theme.pptx");

        XSLFSlide slide = ppt.getSlides().get(0);
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSLFTable);
        XSLFTable tbl = (XSLFTable)shapes.get(0);
        assertEquals(1, tbl.getNumberOfColumns());
        assertEquals(2, tbl.getNumberOfRows());

        List<XSLFTableRow> rows = tbl.getRows();
        assertEquals(2, rows.size());

        // First row has 1 col and 1 runs
        XSLFTableRow row0 = rows.get(0);
        List<XSLFTableCell> cells0 = row0.getCells();
        assertEquals(1, cells0.size());

        List<XSLFTextParagraph> paras0 =  cells0.get(0).getTextParagraphs();
        assertEquals(1, paras0.size());

        List<XSLFTextRun> runs0 =  paras0.get(0).getTextRuns();
        assertEquals(1, runs0.size());

        // IMPORTANT -> this should not be a normal text run (was a bug previously)
        XSLFTextRun run00 = runs0.get(0);
        assertEquals("XSLFCellTextRun", run00.getClass().getSimpleName());
        assertFalse(run00.isBold());
        assertFalse(run00.isItalic());
        assertNotNull(run00.getFontColor());
        assertTrue(run00.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run00.getFontColor()).getSolidColor().getColor());

        // Second row has 1 col and 3 runs
        XSLFTableRow row1 = rows.get(1);
        List<XSLFTableCell> cells1 = row1.getCells();
        assertEquals(1, cells1.size());

        List<XSLFTextParagraph> paras1 = cells1.get(0).getTextParagraphs();
        assertEquals(1, paras1.size());

        List<XSLFTextRun> runs1 =  paras1.get(0).getTextRuns();
        assertEquals(3, runs1.size());

        // IMPORTANT -> this should not be a normal text run (was a bug previously)
        XSLFTextRun run10 = runs1.get(0);
        assertEquals("XSLFCellTextRun", run10.getClass().getSimpleName());
        assertTrue(run10.isBold());
        assertFalse(run10.isItalic());
        assertNotNull(run10.getFontColor());
        assertTrue(run10.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run10.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run11 = runs1.get(1);
        assertEquals("XSLFCellTextRun", run11.getClass().getSimpleName());
        assertFalse(run11.isBold());
        assertFalse(run11.isItalic());
        assertNotNull(run11.getFontColor());
        assertTrue(run11.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run11.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run12 = runs1.get(2);
        assertEquals("XSLFCellTextRun", run12.getClass().getSimpleName());
        assertFalse(run12.isBold());
        assertTrue(run12.isItalic());
        assertNotNull(run12.getFontColor());
        assertTrue(run12.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run12.getFontColor()).getSolidColor().getColor());

        ppt.close();
    }

    @Test
    void testCorrectlyReadsTextRunStylingForCellsWithTheme() throws IOException {
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("table-with-theme.pptx");

        XSLFSlide slide = ppt.getSlides().get(0);
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals(1, shapes.size());
        assertTrue(shapes.get(0) instanceof XSLFTable);
        XSLFTable tbl = (XSLFTable)shapes.get(0);
        assertEquals(1, tbl.getNumberOfColumns());
        assertEquals(2, tbl.getNumberOfRows());

        List<XSLFTableRow> rows = tbl.getRows();
        assertEquals(2, rows.size());

        // First row has 1 col and 3 runs
        XSLFTableRow row0 = rows.get(0);
        List<XSLFTableCell> cells0 = row0.getCells();
        assertEquals(1, cells0.size());

        List<XSLFTextParagraph> paras0 =  cells0.get(0).getTextParagraphs();
        assertEquals(1, paras0.size());

        List<XSLFTextRun> runs0 =  paras0.get(0).getTextRuns();
        assertEquals(3, runs0.size());

        // IMPORTANT -> this should not be a normal text run (was a bug previously)
        XSLFTextRun run00 = runs0.get(0);
        assertEquals("XSLFCellTextRun", run00.getClass().getSimpleName());
        assertTrue(run00.isBold());
        assertFalse(run00.isItalic());
        assertNotNull(run00.getFontColor());
        assertTrue(run00.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.white, ((PaintStyle.SolidPaint)run00.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run01 = runs0.get(1);
        assertEquals("XSLFCellTextRun", run01.getClass().getSimpleName());
        assertTrue(run01.isBold());
        assertFalse(run01.isItalic());
        assertNotNull(run01.getFontColor());
        assertTrue(run01.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.white, ((PaintStyle.SolidPaint)run01.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run02 = runs0.get(2);
        assertEquals("XSLFCellTextRun", run02.getClass().getSimpleName());
        assertFalse(run02.isBold());
        assertFalse(run02.isItalic());
        assertNotNull(run02.getFontColor());
        assertTrue(run02.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.red, ((PaintStyle.SolidPaint)run02.getFontColor()).getSolidColor().getColor());

        // Second row has 1 col and 7 runs
        XSLFTableRow row1 = rows.get(1);
        List<XSLFTableCell> cells1 = row1.getCells();
        assertEquals(1, cells1.size());

        List<XSLFTextParagraph> paras1 = cells1.get(0).getTextParagraphs();
        assertEquals(1, paras1.size());

        List<XSLFTextRun> runs1 =  paras1.get(0).getTextRuns();
        assertEquals(7, runs1.size());

        // IMPORTANT -> this should not be a normal text run (was a bug previously)
        XSLFTextRun run10 = runs1.get(0);
        assertEquals("XSLFCellTextRun", run10.getClass().getSimpleName());
        assertTrue(run10.isBold());
        assertFalse(run10.isItalic());
        assertNotNull(run10.getFontColor());
        assertTrue(run10.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run10.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run11 = runs1.get(1);
        assertEquals("XSLFCellTextRun", run11.getClass().getSimpleName());
        assertFalse(run11.isBold());
        assertFalse(run11.isItalic());
        assertNotNull(run11.getFontColor());
        assertTrue(run11.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run11.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run12 = runs1.get(2);
        assertEquals("XSLFCellTextRun", run12.getClass().getSimpleName());
        assertFalse(run12.isBold());
        assertTrue(run12.isItalic());
        assertNotNull(run12.getFontColor());
        assertTrue(run12.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run12.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run13 = runs1.get(3);
        assertEquals("XSLFCellTextRun", run13.getClass().getSimpleName());
        assertFalse(run13.isBold());
        assertFalse(run13.isItalic());
        assertNotNull(run13.getFontColor());
        assertTrue(run13.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run13.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run14 = runs1.get(4);
        assertEquals("XSLFCellTextRun", run14.getClass().getSimpleName());
        assertFalse(run14.isBold());
        assertFalse(run14.isItalic());
        assertNotNull(run14.getFontColor());
        assertTrue(run14.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.yellow, ((PaintStyle.SolidPaint)run14.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run15 = runs1.get(5);
        assertEquals("XSLFCellTextRun", run15.getClass().getSimpleName());
        assertFalse(run15.isBold());
        assertFalse(run15.isItalic());
        assertNotNull(run15.getFontColor());
        assertTrue(run15.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run15.getFontColor()).getSolidColor().getColor());

        XSLFTextRun run16 = runs1.get(6);
        assertEquals("XSLFCellTextRun", run16.getClass().getSimpleName());
        assertFalse(run16.isBold());
        assertFalse(run16.isItalic());
        assertNotNull(run16.getFontColor());
        assertTrue(run16.getFontColor() instanceof PaintStyle.SolidPaint);
        assertEquals(Color.black, ((PaintStyle.SolidPaint)run16.getFontColor()).getSolidColor().getColor());

        ppt.close();
    }

    @Test
    void testBug68703() throws IOException {
        try(XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("bug68703.pptx")) {
            XSLFSlide firstSlide = pptx.getSlides().get(0);
            XSLFTable table = (XSLFTable) firstSlide.getShapes().get(0);
            XSLFTableCell cell = table.getCell(0, 0);
            List<XSLFTextParagraph> cellParagraphs = cell.getTextParagraphs();
            List<XSLFTextRun> cellTextRuns = cellParagraphs.get(0).getTextRuns();
            PaintStyle fontColor = cellTextRuns.get(0).getFontColor();
            assertNotNull(fontColor);
            assertTrue(fontColor instanceof PaintStyle.SolidPaint);
            assertEquals(Color.black, ((PaintStyle.SolidPaint) fontColor).getSolidColor().getColor());
        }
    }

}
