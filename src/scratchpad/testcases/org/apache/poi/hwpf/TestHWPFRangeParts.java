
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

import java.io.FileInputStream;

import org.apache.poi.hwpf.usermodel.Range;

import junit.framework.TestCase;

/**
 * Test that we pull out the right bits of a file into
 *  the different ranges
 */
public class TestHWPFRangeParts extends TestCase {
	private static final String page_1 =
		"This is a sample word document. It has two pages. It has a three column heading, and a three column footer\r" +
		"\r" +
		"HEADING TEXT\r" + 
		"\r" +
		"More on page one\r" +
		"\r\r" +
		"End of page 1\r"
	;
	private static final char page_break = (char)12;
	private static final String page_2 =
		"This is page two. It also has a three column heading, and a three column footer.\r"
	;
	
	private static final String headerDef = 
		"\u0003\r\r" +
		"\u0004\r\r" +
		"\u0003\r\r" +
		"\u0004\r\r"
	;
	private static final String header =
		"First header column!\tMid header Right header!\r"
	;
	private static final String footerDef = 
		"\r"
	;
	private static final String footer =
		"Footer Left\tFooter Middle Footer Right\r"
	;
	private static final String endHeaderFooter =
		"\r\r"
	;
	
	private HWPFDocument doc;
	
	public void setUp() throws Exception {
		String filename = System.getProperty("HWPF.testdata.path");
		filename = filename + "/ThreeColHeadFoot.doc";
		
		doc = new HWPFDocument(
				new FileInputStream(filename)
		);
	}
	
	public void testBasics() throws Exception {
		// First check the start and end bits
		assertEquals(
				0,
				doc._cpSplit.getMainDocumentStart()
		);
		assertEquals(
				page_1.length() +
				2 + // page break
				page_2.length(),
				doc._cpSplit.getMainDocumentEnd()
		);
		
		assertEquals(
				238,
				doc._cpSplit.getFootnoteStart()
		);
		assertEquals(
				238,
				doc._cpSplit.getFootnoteEnd()
		);
		
		assertEquals(
				238,
				doc._cpSplit.getHeaderStoryStart()
		);
		assertEquals(
				238 + headerDef.length() + header.length() +
				footerDef.length() + footer.length() + endHeaderFooter.length(),
				doc._cpSplit.getHeaderStoryEnd()
		);
	}
	
	public void testContents() throws Exception {
		Range r;
		
		// Now check the real ranges
		r = doc.getRange();
		assertEquals(
				page_1 +
				page_break + "\r" +
				page_2,
				r.text()
		);
		
		r = doc.getHeaderStoryRange();
		assertEquals(
				headerDef +
				header +
				footerDef +
				footer + 
				endHeaderFooter,
				r.text()
		);
		
		r = doc.getOverallRange();
		assertEquals(
				page_1 +
				page_break + "\r" +
				page_2 + 
				headerDef +
				header +
				footerDef +
				footer + 
				endHeaderFooter +
				"\r",
				r.text()
		);
	}
}
