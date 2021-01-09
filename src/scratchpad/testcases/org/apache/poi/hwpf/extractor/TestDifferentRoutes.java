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

package org.apache.poi.hwpf.extractor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the different routes to extracting text
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestDifferentRoutes {
	private static final String[] p_text = new String[] {
			"This is a simple word document\r",
			"\r",
			"It has a number of paragraphs in it\r",
			"\r",
			"Some of them even feature bold, italic and underlined text\r",
			"\r",
			"\r",
			"This bit is in a different font and size\r",
			"\r",
			"\r",
			"This bit features some red text.\r",
			"\r",
			"\r",
			"It is otherwise very very boring.\r"
	};

	private HWPFDocument doc;

	@BeforeEach
	void setUp() {
		doc = HWPFTestDataSamples.openSampleFile("test2.doc");
	}

	@AfterEach
	void tearDown() throws IOException {
		doc.close();
	}

	/**
	 * Test model based extraction
	 */
	@Test
	void testExtractFromModel() {
		Range r = doc.getRange();

		String[] text = new String[r.numParagraphs()];
		for (int i = 0; i < r.numParagraphs(); i++) {
			Paragraph p = r.getParagraph(i);
			text[i] = p.text();
		}

		assertArrayEquals(p_text, text);
	}

	/**
	 * Test textPieces based extraction
	 */
	@Test
	void testExtractFromTextPieces() throws Exception {
		String expected = StringUtil.join(p_text, "");
		assertEquals(expected, doc.getDocumentText());
	}
}
