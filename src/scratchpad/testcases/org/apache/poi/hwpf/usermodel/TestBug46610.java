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

package org.apache.poi.hwpf.usermodel;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.junit.jupiter.api.Test;

public final class TestBug46610 {

	@Test
	void testUtf() throws Exception {
		String text = runExtract("Bug46610_1.doc");
		assertNotNull(text);
	}

	@Test
	void testUtf2() throws Exception {
		String text = runExtract("Bug46610_2.doc");
		assertNotNull(text);
	}

	@Test
	void testExtraction() throws Exception {
		String text = runExtract("Bug46610_3.doc");
		assertContains(text, "\u0421\u0412\u041e\u042e");
	}

	private static String runExtract(String sampleName) throws Exception {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(sampleName);
		StringBuilder out = new StringBuilder();

		Range globalRange = doc.getRange();
		for (int i = 0; i < globalRange.numParagraphs(); i++) {
			Paragraph p = globalRange.getParagraph(i);
			out.append(p.text());
			out.append("\n");
			for (int j = 0; j < p.numCharacterRuns(); j++) {
				CharacterRun characterRun = p.getCharacterRun(j);
				characterRun.text();
			}
		doc.close();
		}
		return out.toString();
	}
}
