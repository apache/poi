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
public final class TestNumberedList2 extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	protected void setUp() throws Exception {
	}

	public void testNumberedList() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("numbers2.ppt"));
		assertTrue("No Exceptions while reading file", true);

		final Slide[] slides = ppt.getSlides();
		assertEquals(2, slides.length);
		checkSlide0(slides[0]);
		checkSlide1(slides[1]);
	}
	private void checkSlide0(final Slide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(2, numberedListArray.length);
		final StyleTextProp9Atom numberedListInfoForTextBox0 = numberedListArray[0];
		final StyleTextProp9Atom numberedListInfoForTextBox1 = numberedListArray[1];
		assertNotNull(numberedListInfoForTextBox0);
		assertNotNull(numberedListInfoForTextBox1);
		final TextPFException9[] autoNumbersOfTextBox0 = numberedListInfoForTextBox0.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getAutoNumberStartNumber());//Default value = 1 will be used 
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbersOfTextBox0[0].getAutoNumberScheme());
		final TextPFException9[] autoNumbersOfTextBox1 = numberedListInfoForTextBox1.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox1[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)6), autoNumbersOfTextBox1[0].getAutoNumberStartNumber());//Default value = 1 will be used 
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbersOfTextBox1[0].getAutoNumberScheme());

		
		TextRun[] textRuns = s.getTextRuns();
		assertEquals(2, textRuns.length);

		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertEquals("List Item One\rList Item Two\rList Item Three", textRun.getRawText());
		assertEquals(1, textRuns[0].getRichTextRuns().length);
		assertTrue(textRun.isBullet());

		assertEquals("A numbered list may start at any number \rThis would be used as a continuation list on another page\rThis list should start with #6", textRuns[1].getRawText());
		
		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textRuns.length, styleAtoms.length);
		checkSingleRunWrapper(44, styleAtoms[0]);
		checkSingleRunWrapper(130, styleAtoms[1]);
	}
	private void checkSlide1(final Slide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);
		final StyleTextProp9Atom numberedListInfoForTextBox = numberedListArray[0];
		assertNotNull(numberedListInfoForTextBox);
		final TextPFException9[] autoNumbersOfTextBox = numberedListInfoForTextBox.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox[0].getAutoNumberStartNumber());//Default value = 1 will be used 
		assertTrue(TextAutoNumberSchemeEnum.ANM_ArabicPeriod == autoNumbersOfTextBox[0].getAutoNumberScheme());
			
		TextRun[] textRuns = s.getTextRuns();
		assertEquals(3, textRuns.length);

		RichTextRun textRun = textRuns[0].getRichTextRuns()[0];
		assertEquals("Bulleted list\rMore bullets", textRun.getRawText());
		assertEquals(1, textRuns[0].getRichTextRuns().length);
		assertTrue(textRun.isBullet());

		assertEquals("Numbered list between two bulleted lists\rSecond numbered list item", textRuns[1].getRawText());
		assertEquals("Second bulleted list \u2013 should appear after numbered list\rMore bullets", textRuns[2].getRawText());
		
		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textRuns.length, styleAtoms.length);
		checkSingleRunWrapper(27, styleAtoms[0]);
		checkSingleRunWrapper(67, styleAtoms[1]);
		checkSingleRunWrapper(70, styleAtoms[2]);
	}
	private void checkSingleRunWrapper(final int exceptedLength, final EscherTextboxWrapper wrapper) {
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		assertEquals(1, textProps.size());
		final TextPropCollection[] props = (TextPropCollection[]) textProps.toArray(new TextPropCollection[textProps.size()]);
		assertEquals(exceptedLength, props[0].getCharactersCovered());
	}
}
