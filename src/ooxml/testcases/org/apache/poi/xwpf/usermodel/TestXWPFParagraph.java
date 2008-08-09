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
package org.apache.poi.xwpf.usermodel;

import java.io.File;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.XWPFDocument;

import junit.framework.TestCase;

/**
 * Tests for XWPF Paragraphs
 */
public class TestXWPFParagraph extends TestCase {
	/**
	 * A simple file
	 */
	private XWPFDocument xml;
	private File file;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "ThreeColHead.docx"
		);
		assertTrue(file.exists());
		xml = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
	}
	
	/**
	 * Check that we get the right paragraph from the header
	 */
	public void testHeaderParagraph() throws Exception {
		XWPFHeader hdr = xml.getDocumentHeader();
		assertNotNull(hdr);
		
		XWPFParagraph[] ps = hdr.getParagraphs();
		assertEquals(1, ps.length);
		XWPFParagraph p = ps[0];
		
		assertEquals(5, p.getCTP().getRArray().length);
		assertEquals(
				"First header column!\tMid header\tRight header!",
				p.getText()
		);
	}
	
	/**
	 * Check that we get the right paragraphs from the document
	 */
	public void testDocumentParagraph() throws Exception {
		XWPFParagraph[] ps = xml.getParagraphs();
		assertEquals(10, ps.length);
		
		assertFalse(ps[0].isEmpty());
		assertEquals(
				"This is a sample word document. It has two pages. It has a three column heading, but no footer.",
				ps[0].getText()
		);
		
		assertTrue(ps[1].isEmpty());
		assertEquals("", ps[1].getText());
		
		assertFalse(ps[2].isEmpty());
		assertEquals(
				"HEADING TEXT",
				ps[2].getText()
		);
		
		assertTrue(ps[3].isEmpty());
		assertEquals("", ps[3].getText());
		
		assertFalse(ps[4].isEmpty());
		assertEquals(
				"More on page one",
				ps[4].getText()
		);
	}
}
