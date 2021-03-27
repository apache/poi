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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.textproperties.TextPFException9;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.StyleTextProp9Atom;
import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.junit.jupiter.api.Test;


/**
 * Test that checks numbered list functionality.
 * if a paragraph has autonumber ()
 * @see <a href="http://social.msdn.microsoft.com/Forums/mr-IN/os_binaryfile/thread/650888db-fabd-4b95-88dc-f0455f6e2d28">
 *     PPT: Missing TextAutoNumberScheme structure providing the style of the number bullets</a>
 */
public final class TestNumberedList2 {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
	void testNumberedList() throws IOException {
		try (HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("numbers2.ppt"))) {
			final List<HSLFSlide> slides = ppt.getSlides();
			assertEquals(2, slides.size());
			checkSlide0(slides.get(0));
			checkSlide1(slides.get(1));
		}
    }

    private void checkSlide0(final HSLFSlide s) {
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
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbersOfTextBox0[0].getAutoNumberScheme());
		final TextPFException9[] autoNumbersOfTextBox1 = numberedListInfoForTextBox1.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox1[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)6), autoNumbersOfTextBox1[0].getAutoNumberStartNumber());//Default value = 1 will be used
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbersOfTextBox1[0].getAutoNumberScheme());


		List<List<HSLFTextParagraph>> textParass = s.getTextParagraphs();
		assertEquals(2, textParass.size());

		List<HSLFTextParagraph> textParas = textParass.get(0);
		assertEquals("List Item One\rList Item Two\rList Item Three", HSLFTextParagraph.getRawText(textParas));
		assertEquals(3, textParas.size());
		assertTrue(textParas.get(0).isBullet());

		String expected =
	        "A numbered list may start at any number \r" +
	        "This would be used as a continuation list on another page\r" +
            "This list should start with #6";
		assertEquals(expected, HSLFTextParagraph.getRawText(textParass.get(1)));

		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textParass.size(), styleAtoms.length);
		checkSingleRunWrapper(44, styleAtoms[0]);
		checkSingleRunWrapper(130, styleAtoms[1]);
	}

	private void checkSlide1(final HSLFSlide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);
		final StyleTextProp9Atom numberedListInfoForTextBox = numberedListArray[0];
		assertNotNull(numberedListInfoForTextBox);
		final TextPFException9[] autoNumbersOfTextBox = numberedListInfoForTextBox.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox[0].getAutoNumberStartNumber());//Default value = 1 will be used
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbersOfTextBox[0].getAutoNumberScheme());

		List<List<HSLFTextParagraph>> textParass = s.getTextParagraphs();
		assertEquals(3, textParass.size());

		List<HSLFTextParagraph> textParas = textParass.get(0);
		assertEquals("Bulleted list\rMore bullets", HSLFTextParagraph.getRawText(textParas));
		assertEquals(2, textParas.size());
		assertTrue(textParas.get(0).isBullet());

		String expected = "Numbered list between two bulleted lists\rSecond numbered list item";
		assertEquals(expected, HSLFTextParagraph.getRawText(textParass.get(1)));
		expected = "Second bulleted list \u2013 should appear after numbered list\rMore bullets";
		assertEquals(expected, HSLFTextParagraph.getRawText(textParass.get(2)));

		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textParass.size(), styleAtoms.length);
		checkSingleRunWrapper(27, styleAtoms[0]);
		checkSingleRunWrapper(67, styleAtoms[1]);
		checkSingleRunWrapper(70, styleAtoms[2]);
	}

	private void checkSingleRunWrapper(final int exceptedLength, final EscherTextboxWrapper wrapper) {
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		assertEquals(1, textProps.size());
		assertEquals(exceptedLength, textProps.get(0).getCharactersCovered());
	}
}
