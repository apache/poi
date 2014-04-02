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

import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.textproperties.TextPFException9;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.StyleTextProp9Atom;
import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.hslf.record.TextAutoNumberSchemeEnum;
import org.apache.poi.POIDataSamples;


/**
 * Test that checks numbered list functionality.
 * 
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 */
public final class TestNumberedList extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	protected void setUp() throws Exception {
	}

	public void testNumberedList() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("numbers.ppt"));
		assertTrue("No Exceptions while reading file", true);

		final Slide[] slides = ppt.getSlides();
		assertEquals(2, slides.length);
		checkSlide0(slides[0]);
		checkSlide1(slides[1]);
	}
	private void checkSlide0(final Slide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);//Just one text box here
		final StyleTextProp9Atom numberedListInfo = numberedListArray[0];
		assertNotNull(numberedListInfo);
		final TextPFException9[] autoNumbers = numberedListInfo.getAutoNumberTypes();
		assertNotNull(autoNumbers);
		assertEquals(4, autoNumbers.length);
		assertTrue(4 == autoNumbers[0].getAutoNumberStartNumber());
		assertNull(autoNumbers[1].getAutoNumberStartNumber());
		assertTrue(3 == autoNumbers[2].getAutoNumberStartNumber());
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbers[0].getAutoNumberScheme());
		assertNull(autoNumbers[1].getAutoNumberScheme());
		assertTrue(TextAutoNumberSchemeEnum.ANM_AlphaLcParenRight == autoNumbers[2].getAutoNumberScheme());
			
		TextRun[] textRuns = s.getTextRuns();
		assertEquals(2, textRuns.length);

		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertEquals("titTe", textRun.getRawText());
		assertEquals(1, textRuns[0].getRichTextRuns().length);
		assertFalse(textRun.isBullet());

		assertEquals("This is a text placeholder that \rfollows the design pattern\rJust a test\rWithout any paragraph\rSecond paragraph first line c) ;\rSecond paragraph second line d) . \r", textRuns[1].getRawText());
		
		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textRuns.length, styleAtoms.length);
		final EscherTextboxWrapper wrapper =  styleAtoms[1];
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		final TextPropCollection[] props = (TextPropCollection[]) textProps.toArray(new TextPropCollection[textProps.size()]);
		assertEquals(60, props[0].getCharactersCovered());
		assertEquals(34, props[1].getCharactersCovered());
		assertEquals(68, props[2].getCharactersCovered());
	}
	private void checkSlide1(final Slide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);//Just one text box here
		final StyleTextProp9Atom numberedListInfo = numberedListArray[0];
		assertNotNull(numberedListInfo);
		final TextPFException9[] autoNumbers = numberedListInfo.getAutoNumberTypes();
		assertNotNull(autoNumbers);
		assertEquals(4, autoNumbers.length);
		assertTrue(9 == autoNumbers[0].getAutoNumberStartNumber());
		assertNull(autoNumbers[1].getAutoNumberStartNumber());
		assertTrue(3 == autoNumbers[2].getAutoNumberStartNumber());
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicParenRight == autoNumbers[0].getAutoNumberScheme());
		assertNull(autoNumbers[1].getAutoNumberScheme());
		assertTrue(TextAutoNumberSchemeEnum.ANM_AlphaUcPeriod == autoNumbers[2].getAutoNumberScheme());

		final TextRun[] textRuns = s.getTextRuns();
		assertEquals(2, textRuns.length);

		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertEquals("Second Slide Title", textRun.getRawText());
		assertEquals(1, textRuns[0].getRichTextRuns().length);
		assertFalse(textRun.isBullet());

		assertEquals("This is a text placeholder that \rfollows the design pattern\rJust a test\rWithout any paragraph\rSecond paragraph first line c) ;\rSecond paragraph second line d) . \r", textRuns[1].getRawText());
		
		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textRuns.length, styleAtoms.length);
		final EscherTextboxWrapper wrapper =  styleAtoms[1];
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		
		final TextPropCollection[] props = (TextPropCollection[]) textProps.toArray(new TextPropCollection[textProps.size()]);
		assertEquals(33, props[0].getCharactersCovered());
		assertEquals(61, props[1].getCharactersCovered());
		assertEquals(68, props[2].getCharactersCovered());
	}
}
