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

import junit.framework.TestCase;

import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

public final class TestBug46610 extends TestCase {

	public void testUtf() {
		runExtract("Bug46610_1.doc");
	}

	public void testUtf2() {
		runExtract("Bug46610_2.doc");
	}

	public void testExtraction() {
		String text = runExtract("Bug46610_3.doc");
		assertTrue(text.contains("\u0421\u0412\u041e\u042e"));
	}

	private static String runExtract(String sampleName) {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(sampleName);
		StringBuffer out = new StringBuffer();

		Range globalRange = doc.getRange();
		for (int i = 0; i < globalRange.numParagraphs(); i++) {
			Paragraph p = globalRange.getParagraph(i);
			out.append(p.text());
			out.append("\n");
			for (int j = 0; j < p.numCharacterRuns(); j++) {
				CharacterRun characterRun = p.getCharacterRun(j);
				characterRun.text();
			}
		}
		return out.toString();
	}
}
