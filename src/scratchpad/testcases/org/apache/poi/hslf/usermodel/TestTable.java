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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.draw.DrawTableShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.junit.Test;


/**
 * Table related tests
 */
public class TestTable {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    public void moveTable() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide = ppt.createSlide();
        int rows = 3, cols = 5;
        HSLFTable table = slide.createTable(rows, cols);
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                HSLFTableCell c = table.getCell(row, col);
                c.setText("r"+row+"c"+col);
            }
        }
        
        new DrawTableShape(table).setAllBorders(1.0, Color.black, StrokeStyle.LineDash.DASH_DOT);
        
        table.setAnchor(new Rectangle(100, 100, 400, 400));
        
        Rectangle rectExp = new Rectangle(420,367,80,133);
        Rectangle rectAct = table.getCell(rows-1, cols-1).getAnchor();
        assertEquals(rectExp, rectAct);
    }
    
    @Test
    public void testTable() throws Exception {
		HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("54111.ppt"));
		assertTrue("No Exceptions while reading file", true);

		List<HSLFSlide> slides = ppt.getSlides();
		assertEquals(1, slides.size());
		checkSlide(slides.get(0));
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
			assertEquals("TH Cell " + (x + 1), HSLFTextParagraph.getRawText(table.getCell(0, x).getTextParagraphs()));
			for (int y = 1; y < 6; y++) {
				assertEquals("Row " + y + ", Cell " + (x + 1), table.getCell(y, x).getText());
			}
		}
	}
}
