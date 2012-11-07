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

import junit.framework.TestCase;

import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.POIDataSamples;


/**
 * Test that checks numbered list functionality.
 * 
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 */
public final class TestTable extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	protected void setUp() throws Exception {
	}

	public void testTable() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("54111.ppt"));
		assertTrue("No Exceptions while reading file", true);

		final Slide[] slides = ppt.getSlides();
		assertEquals(1, slides.length);
		checkSlide(slides[0]);
	}
	private void checkSlide(final Slide s) {
		TextRun[] textRuns = s.getTextRuns();
		assertEquals(2, textRuns.length);

		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertEquals("Table sample", textRun.getRawText().trim());
		assertEquals(1, textRuns[0].getRichTextRuns().length);
		assertFalse(textRun.isBullet());

		assertEquals("Dummy text", textRuns[1].getRawText());
		
		final Shape[] shapes = s.getShapes();
		assertNotNull(shapes);
		assertEquals(3, shapes.length);
		assertTrue(shapes[2] instanceof Table);
		final Table table = (Table) shapes[2];
		assertEquals(4, table.getNumberOfColumns());
		assertEquals(6, table.getNumberOfRows());
		for (int x = 0; x < 4; x ++) {
			assertEquals("TH Cell " + (x + 1), table.getCell(0, x).getTextRun().getRawText());
			for (int y = 1; y < 6; y++) {
				assertEquals("Row " + y + ", Cell " + (x + 1), table.getCell(y, x).getText());
			}
		}
	}
}
