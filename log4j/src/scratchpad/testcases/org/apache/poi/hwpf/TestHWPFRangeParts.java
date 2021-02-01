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

package org.apache.poi.hwpf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hwpf.usermodel.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that we pull out the right bits of a file into the different ranges
 */
public final class TestHWPFRangeParts {
	private static final char page_break = (char)12;
	private static final String headerDef =
		"\u0003\r\r" +
		"\u0004\r\r" +
		"\u0003\r\r" +
		"\u0004\r\r"
	;
	private static final String footerDef = "\r";
	private static final String endHeaderFooter = "\r\r";


	private static final String a_page_1 =
		"This is a sample word document. It has two pages. It has a three column heading, and a three column footer\r" +
		"\r" +
		"HEADING TEXT\r" +
		"\r" +
		"More on page one\r" +
		"\r\r" +
		"End of page 1\r"
	;
	private static final String a_page_2 =
		"This is page two. It also has a three column heading, and a three column footer.\r"
	;

	private static final String a_header =
		"First header column!\tMid header Right header!\r"
	;
	private static final String a_footer =
		"Footer Left\tFooter Middle Footer Right\r"
	;


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

	private static final String u_header =
		"\r\r" +
		"This is a simple header, with a \u20ac euro symbol in it.\r"
	;
	private static final String u_footer =
		"\r\r\r" +
		"The footer, with Moli\u00e8re, has Unicode in it.\r" +
		"\r\r\r\r"
	;

	/**
	 * A document made up only of basic ASCII text
	 */
	private HWPFDocument docAscii;
	/**
	 * A document with some unicode in it too
	 */
	private HWPFDocument docUnicode;

	@BeforeEach
    void setUp() {
		docUnicode = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
		docAscii = HWPFTestDataSamples.openSampleFile("ThreeColHeadFoot.doc");
	}

   /**
    * Note - this test runs several times, to ensure that things
    *  don't get broken as we write out and read back in again
    * TODO - Make this work with 3+ runs
    */
   @Test
	void testContents() {
      HWPFDocument doc = docAscii;
      for(int run=0; run<3; run++) {
   		Range r;

   		// Now check the real ranges
   		r = doc.getRange();
   		assertEquals(
   				a_page_1 +
   				page_break + "\r" +
   				a_page_2,
   				r.text()
   		);

   		r = doc.getHeaderStoryRange();
   		assertEquals(
   				headerDef +
   				a_header +
   				footerDef +
   				a_footer +
   				endHeaderFooter,
   				r.text()
   		);

   		r = doc.getOverallRange();
   		assertEquals(
   				a_page_1 +
   				page_break + "\r" +
   				a_page_2 +
   				headerDef +
   				a_header +
   				footerDef +
   				a_footer +
   				endHeaderFooter +
   				"\r",
   				r.text()
   		);

         // Write out and read back in again, ready for
         //  the next run of the test
         // TODO run more than once
         if(run < 1)
            doc = HWPFTestDataSamples.writeOutAndReadBack(doc);
      }
	}

	@Test
	void testContentsUnicode() {
		Range r;

		// Now check the real ranges
		r = docUnicode.getRange();
		assertEquals(
				u_page_1 +
				page_break + "\r" +
				u_page_2,
				r.text()
		);

		r = docUnicode.getHeaderStoryRange();
		assertEquals(
				headerDef +
				u_header +
				footerDef +
				u_footer +
				endHeaderFooter,
				r.text()
		);

		r = docUnicode.getOverallRange();
		assertEquals(
				u_page_1 +
				page_break + "\r" +
				u_page_2 +
				headerDef +
				u_header +
				footerDef +
				u_footer +
				endHeaderFooter +
				"\r",
				r.text()
		);
	}
}
