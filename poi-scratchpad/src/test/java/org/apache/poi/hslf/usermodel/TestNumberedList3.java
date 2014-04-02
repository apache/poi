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
 * if a paragraph has autonumber ()
 * @see <a href="http://social.msdn.microsoft.com/Forums/mr-IN/os_binaryfile/thread/650888db-fabd-4b95-88dc-f0455f6e2d28">
 *     PPT: Missing TextAutoNumberScheme structure providing the style of the number bullets</a>
 * 
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 */
public final class TestNumberedList3 extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	protected void setUp() throws Exception {
	}

	public void testNumberedList() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("numbers3.ppt"));
		assertTrue("No Exceptions while reading file", true);

		final Slide[] slides = ppt.getSlides();
		assertEquals(1, slides.length);
		final Slide slide = slides[0];
		checkSlide(slide);
	}
	private void checkSlide(final Slide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);
		final StyleTextProp9Atom numberedListInfoForTextBox = numberedListArray[0];
		assertNotNull(numberedListInfoForTextBox);
		final TextPFException9[] autoNumbersOfTextBox0 = numberedListInfoForTextBox.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getAutoNumberStartNumber());//Default value = 1 will be used 
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbersOfTextBox0[0].getAutoNumberScheme());
		
		final TextRun[] textRuns = s.getTextRuns();
		assertEquals(3, textRuns.length);
		assertEquals("Bulleted list\rMore bullets\rNo bullets here", textRuns[0].getRawText());
		assertEquals("Numbered list between two bulleted lists\rSecond numbered list item", textRuns[1].getRawText());
		assertEquals("Second bulleted list \u2013 should appear after numbered list\rMore bullets", textRuns[2].getRawText());
		assertEquals(2, textRuns[0].getRichTextRuns().length);
		assertEquals(1, textRuns[1].getRichTextRuns().length);
		assertEquals(1, textRuns[2].getRichTextRuns().length);
		assertNull(textRuns[0].getStyleTextProp9Atom());
		assertNotNull(textRuns[1].getStyleTextProp9Atom());
		assertNull(textRuns[2].getStyleTextProp9Atom());
		final TextPFException9[] autoNumbers = textRuns[1].getStyleTextProp9Atom().getAutoNumberTypes();
		assertEquals(1, autoNumbers.length);
		assertEquals(Short.valueOf((short)1), autoNumbers[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbers[0].getAutoNumberStartNumber());//Default value = 1 will be used 
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbersOfTextBox0[0].getAutoNumberScheme());
		
		final List<TextPropCollection> textProps = textRuns[1].getStyleTextPropAtom().getCharacterStyles();
		assertEquals(1, textProps.size());
		final TextPropCollection textProp = textProps.get(0);
		assertEquals(67, textProp.getCharactersCovered());
		
		
		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertTrue(textRun.isBullet());

		
		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textRuns.length, styleAtoms.length);
		checkSingleRunWrapper(43, styleAtoms[0]);
		checkSingleRunWrapper(67, styleAtoms[1]);
	}
	private void checkSingleRunWrapper(final int exceptedLength, final EscherTextboxWrapper wrapper) {
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		assertEquals(1, textProps.size());
		final TextPropCollection[] props = (TextPropCollection[]) textProps.toArray(new TextPropCollection[textProps.size()]);
		assertEquals(exceptedLength, props[0].getCharactersCovered());
	}
}
