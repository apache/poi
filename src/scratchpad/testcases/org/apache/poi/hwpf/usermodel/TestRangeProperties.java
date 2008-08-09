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

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.HWPFDocument;

import junit.framework.TestCase;

/**
 * Tests to ensure that our ranges end up with
 *  the right text in them, and the right font/styling
 *  properties applied to them.
 */
public class TestRangeProperties extends TestCase {
	private static final char page_break = (char)12;
	
	private static final String u_page_1 =
		"This is a fairly simple word document, over two pages, with headers and footers.\r" +
		"The trick with this one is that it contains some Unicode based strings in it.\r" +
		"Firstly, some currency symbols:\r" +
		"\tGBP - \u00a3\r" +
        "\tEUR - \u20ac\r" +
        "Now, we\u2019ll have some French text, in bold and big:\r" +
        "\tMoli\u00e8re\r" +
        "And some normal French text:\r" +
        "\tL'Avare ou l'\u00c9cole du mensonge\r" +
        "That\u2019s it for page one\r"
	;
	private static final String u_page_2 =
		"This is page two. Les Pr\u00e9cieuses ridicules. The end.\r"
	;
	
	private HWPFDocument u;
	// TODO - a non unicode document too
	
	private String dirname;
	
	protected void setUp() throws Exception {
		dirname = System.getProperty("HWPF.testdata.path");
		u = new HWPFDocument(
				new FileInputStream(new File(dirname, "HeaderFooterUnicode.doc"))
		);
	}

	public void testUnicodeTextParagraphs() throws Exception {
		Range r = u.getRange();
		assertEquals(
				u_page_1 +
				page_break + "\r" + 
				u_page_2,
				r.text()
		);
		
		assertEquals(
				5,
				r.numParagraphs()
		);
		
		System.out.println(r.getParagraph(2).text());
	}
	public void testUnicodeStyling() throws Exception {
		
	}
}
