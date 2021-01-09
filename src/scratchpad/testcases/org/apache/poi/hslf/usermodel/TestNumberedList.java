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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
 *
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 */
public final class TestNumberedList {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testNumberedList() throws Exception {
		HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("numbers.ppt"));
		final List<HSLFSlide> slides = ppt.getSlides();
		assertEquals(2, slides.size());
		checkSlide0(slides.get(0));
		checkSlide1(slides.get(1));
		ppt.close();
	}

    private void checkSlide0(final HSLFSlide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);//Just one text box here
		final StyleTextProp9Atom numberedListInfo = numberedListArray[0];
		assertNotNull(numberedListInfo);
		final TextPFException9[] autoNumbers = numberedListInfo.getAutoNumberTypes();
		assertNotNull(autoNumbers);
		assertEquals(4, autoNumbers.length);
        assertEquals(4, (short) autoNumbers[0].getAutoNumberStartNumber());
		assertNull(autoNumbers[1].getAutoNumberStartNumber());
        assertEquals(3, (short) autoNumbers[2].getAutoNumberStartNumber());
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbers[0].getAutoNumberScheme());
		assertNull(autoNumbers[1].getAutoNumberScheme());
        assertSame(AutoNumberingScheme.alphaLcParenRight, autoNumbers[2].getAutoNumberScheme());

		List<List<HSLFTextParagraph>> textParass = s.getTextParagraphs();
		assertEquals(2, textParass.size());

		List<HSLFTextParagraph> textParas = textParass.get(0);
		assertEquals("titTe", HSLFTextParagraph.getRawText(textParas));
		assertEquals(1, textParas.size());
		assertFalse(textParas.get(0).isBullet());

		String expected =
	        "This is a text placeholder that \r" +
	        "follows the design pattern\r" +
	        "Just a test\rWithout any paragraph\r" +
	        "Second paragraph first line c) ;\r" +
	        "Second paragraph second line d) . \r";
		assertEquals(expected, HSLFTextParagraph.getRawText(textParass.get(1)));

		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textParass.size(), styleAtoms.length);
		final EscherTextboxWrapper wrapper =  styleAtoms[1];
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		assertEquals(60, textProps.get(0).getCharactersCovered());
		assertEquals(34, textProps.get(1).getCharactersCovered());
		assertEquals(68, textProps.get(2).getCharactersCovered());
	}

    private void checkSlide1(final HSLFSlide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);//Just one text box here
		final StyleTextProp9Atom numberedListInfo = numberedListArray[0];
		assertNotNull(numberedListInfo);
		final TextPFException9[] autoNumbers = numberedListInfo.getAutoNumberTypes();
		assertNotNull(autoNumbers);
		assertEquals(4, autoNumbers.length);
        assertEquals(9, (short) autoNumbers[0].getAutoNumberStartNumber());
		assertNull(autoNumbers[1].getAutoNumberStartNumber());
        assertEquals(3, (short) autoNumbers[2].getAutoNumberStartNumber());
        assertSame(AutoNumberingScheme.arabicParenRight, autoNumbers[0].getAutoNumberScheme());
		assertNull(autoNumbers[1].getAutoNumberScheme());
        assertSame(AutoNumberingScheme.alphaUcPeriod, autoNumbers[2].getAutoNumberScheme());

		final List<List<HSLFTextParagraph>> textParass = s.getTextParagraphs();
		assertEquals(2, textParass.size());

		List<HSLFTextParagraph> textParas = textParass.get(0);
		assertEquals("Second Slide Title", HSLFTextParagraph.getRawText(textParas));
		assertEquals(1, textParas.size());
		assertFalse(textParas.get(0).isBullet());

		String expected =
	        "This is a text placeholder that \r" +
	        "follows the design pattern\r" +
	        "Just a test\rWithout any paragraph\r" +
	        "Second paragraph first line c) ;\r" +
	        "Second paragraph second line d) . \r";
		assertEquals(expected, HSLFTextParagraph.getRawText(textParass.get(1)));

		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textParass.size(), styleAtoms.length);
		final EscherTextboxWrapper wrapper =  styleAtoms[1];
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();

		assertEquals(33, textProps.get(0).getCharactersCovered());
		assertEquals(61, textProps.get(1).getCharactersCovered());
		assertEquals(68, textProps.get(2).getCharactersCovered());
	}
}
