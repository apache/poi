/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.sl.draw.DrawTableShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.junit.jupiter.api.Test;


/**
 * Table related tests
 */
public class TestTable {
    @Test
    void moveTable() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide = ppt.createSlide();
        int rows = 3, cols = 5;
        HSLFTable table = slide.createTable(rows, cols);
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                HSLFTableCell c = table.getCell(row, col);
                assertNotNull(c);
                c.setText("r"+row+"c"+col);
            }
        }

        new DrawTableShape(table).setAllBorders(1.0, Color.black, StrokeStyle.LineDash.DASH_DOT);

        table.setAnchor(new Rectangle2D.Double(100, 100, 400, 400));

        Rectangle2D rectExp = new Rectangle2D.Double(420,366.625,80,133.375);
        HSLFTableCell c = table.getCell(rows - 1, cols - 1);
        assertNotNull(c);
        Rectangle2D rectAct = c.getAnchor();
        assertEquals(rectExp, rectAct);
        ppt.close();
    }

    @Test
    void testTable() throws IOException {
		try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("54111.ppt")) {
            List<HSLFSlide> slides = ppt.getSlides();
            assertEquals(1, slides.size());
            checkSlide(slides.get(0));
        }
	}

	private void checkSlide(final HSLFSlide s) {
		List<List<HSLFTextParagraph>> textRuns = s.getTextParagraphs();
		assertEquals(2, textRuns.size());

		HSLFTextRun textRun = textRuns.get(0).get(0).getTextRuns().get(0);
		assertEquals("Table sample", textRun.getRawText().trim());
		assertEquals(1, textRuns.get(0).get(0).getTextRuns().size());
		assertFalse(textRun.getTextParagraph().isBullet());

		assertEquals("Dummy text", HSLFTextParagraph.getRawText(textRuns.get(1)));

		List<HSLFShape> shapes = s.getShapes();
		assertNotNull(shapes);
		assertEquals(3, shapes.size());
		assertTrue(shapes.get(2) instanceof HSLFTable);
		final HSLFTable table = (HSLFTable) shapes.get(2);
		assertEquals(4, table.getNumberOfColumns());
		assertEquals(6, table.getNumberOfRows());
		for (int x = 0; x < 4; x ++) {
            HSLFTableCell c = table.getCell(0, x);
            assertNotNull(c);
			assertEquals("TH Cell " + (x + 1), HSLFTextParagraph.getRawText(c.getTextParagraphs()));
			for (int y = 1; y < 6; y++) {
			    c = table.getCell(y, x);
			    assertNotNull(c);
				assertEquals("Row " + y + ", Cell " + (x + 1), c.getText());
			}
		}
	}

    @Test
    void testAddText() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        HSLFSlide slide = ppt1.createSlide();
        HSLFTable tab = slide.createTable(4, 5);

        int rows = tab.getNumberOfRows();
        int cols = tab.getNumberOfColumns();
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                HSLFTableCell c = tab.getCell(row, col);
                assertNotNull(c);
                c.setText("r"+(row+1)+"c"+(col+1));
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ppt1.write(bos);
        ppt1.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        HSLFSlideShow ppt2 = new HSLFSlideShow(bis);
        slide = ppt2.getSlides().get(0);
        tab = (HSLFTable)slide.getShapes().get(0);

        rows = tab.getNumberOfRows();
        cols = tab.getNumberOfColumns();
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                HSLFTableCell c = tab.getCell(row, col);
                assertNotNull(c);
                c.setText(c.getText()+"...");
            }
        }

        bos.reset();
        ppt2.write(bos);
        ppt2.close();

        bis = new ByteArrayInputStream(bos.toByteArray());
        HSLFSlideShow ppt3 = new HSLFSlideShow(bis);
        slide = ppt3.getSlides().get(0);
        tab = (HSLFTable)slide.getShapes().get(0);

        rows = tab.getNumberOfRows();
        cols = tab.getNumberOfColumns();
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                HSLFTableCell c = tab.getCell(row, col);
                assertNotNull(c);
                assertEquals("r"+(row+1)+"c"+(col+1)+"...", c.getText());
            }
        }

        ppt3.close();
    }
}
