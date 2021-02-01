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
import static org.junit.jupiter.api.Assertions.assertNull;
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
 *
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 */
public final class TestNumberedList3 {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testNumberedList() throws IOException {
		try (HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("numbers3.ppt"))) {
			final List<HSLFSlide> slides = ppt.getSlides();
			assertEquals(1, slides.size());
			final HSLFSlide slide = slides.get(0);
			checkSlide(slide);
		}
	}
	private void checkSlide(final HSLFSlide s) {
		final StyleTextProp9Atom[] numberedListArray = s.getNumberedListInfo();
		assertNotNull(numberedListArray);
		assertEquals(1, numberedListArray.length);
		final StyleTextProp9Atom numberedListInfoForTextBox = numberedListArray[0];
		assertNotNull(numberedListInfoForTextBox);
		final TextPFException9[] autoNumbersOfTextBox0 = numberedListInfoForTextBox.getAutoNumberTypes();
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbersOfTextBox0[0].getAutoNumberStartNumber());//Default value = 1 will be used
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbersOfTextBox0[0].getAutoNumberScheme());

		final List<List<HSLFTextParagraph>> textParass = s.getTextParagraphs();
		assertEquals(3, textParass.size());
		assertEquals("Bulleted list\rMore bullets\rNo bullets here", HSLFTextParagraph.getRawText(textParass.get(0)));
		assertEquals("Numbered list between two bulleted lists\rSecond numbered list item", HSLFTextParagraph.getRawText(textParass.get(1)));
		assertEquals("Second bulleted list \u2013 should appear after numbered list\rMore bullets", HSLFTextParagraph.getRawText(textParass.get(2)));
		assertEquals(3, textParass.get(0).size());
		assertEquals(2, textParass.get(1).size());
		assertEquals(2, textParass.get(2).size());
		assertNull(textParass.get(0).get(0).getStyleTextProp9Atom());
		assertNotNull(textParass.get(1).get(0).getStyleTextProp9Atom());
		assertNull(textParass.get(2).get(0).getStyleTextProp9Atom());
		final TextPFException9[] autoNumbers = textParass.get(1).get(0).getStyleTextProp9Atom().getAutoNumberTypes();
		assertEquals(1, autoNumbers.length);
		assertEquals(Short.valueOf((short)1), autoNumbers[0].getfBulletHasAutoNumber());
		assertEquals(Short.valueOf((short)1), autoNumbers[0].getAutoNumberStartNumber());//Default value = 1 will be used
        assertSame(AutoNumberingScheme.arabicPeriod, autoNumbersOfTextBox0[0].getAutoNumberScheme());

		int chCovered = 0;
		for (HSLFTextParagraph htp : textParass.get(1)) {
    		for (HSLFTextRun htr : htp.getTextRuns()) {
    		    TextPropCollection textProp = htr.getCharacterStyle();
    		    chCovered += textProp.getCharactersCovered();
    		}
		}
		assertEquals(67, chCovered);

		assertTrue(textParass.get(0).get(0).isBullet());

		final EscherTextboxWrapper[] styleAtoms = s.getTextboxWrappers();
		assertEquals(textParass.size(), styleAtoms.length);
		checkSingleRunWrapper(43, styleAtoms[0]);
		checkSingleRunWrapper(67, styleAtoms[1]);
	}
	private void checkSingleRunWrapper(final int exceptedLength, final EscherTextboxWrapper wrapper) {
		final StyleTextPropAtom styleTextPropAtom = wrapper.getStyleTextPropAtom();
		final List<TextPropCollection> textProps = styleTextPropAtom.getCharacterStyles();
		assertEquals(1, textProps.size());
		assertEquals(exceptedLength, textProps.get(0).getCharactersCovered());
	}
}
