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

import java.io.File;

import org.apache.poi.hwpf.HWPFXML;
import org.apache.poi.hwpf.usermodel.HWPFXMLDocument;
import org.apache.poi.hxf.HXFDocument;

import junit.framework.TestCase;

/**
 * Tests for HXFWordExtractor
 */
public class TestHXFWordExtractor extends TestCase {
	/**
	 * A very simple file
	 */
	private HWPFXML xmlA;
	/**
	 * A fairly complex file
	 */
	private HWPFXML xmlB;

	protected void setUp() throws Exception {
		super.setUp();
		
		File fileA = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "sample.docx"
		);
		File fileB = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "IllustrativeCases.docx"
		);
		
		xmlA = new HWPFXML(HXFDocument.openPackage(fileA));
		xmlB = new HWPFXML(HXFDocument.openPackage(fileB));
	}

	/**
	 * Get text out of the simple file
	 */
	public void testGetSimpleText() throws Exception {
		new HXFWordExtractor(xmlA.getPackage());
		new HXFWordExtractor(new HWPFXMLDocument(xmlA));
		
		HXFWordExtractor extractor = 
			new HXFWordExtractor(xmlA.getPackage());
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Check contents
		assertTrue(text.startsWith(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nunc at risus vel erat tempus posuere. Aenean non ante. Suspendisse vehicula dolor sit amet odio."
		));
		assertTrue(text.endsWith(
				"Phasellus ultricies mi nec leo. Sed tempus. In sit amet lorem at velit faucibus vestibulum.\n"
		));
		
		// Check number of paragraphs
		int ps = 0;
		char[] t = text.toCharArray();
		for (int i = 0; i < t.length; i++) {
			if(t[i] == '\n') { ps++; }
		}
		assertEquals(3, ps);
	}
	
	/**
	 * Tests getting the text out of a complex file
	 */
	public void testGetComplexText() throws Exception {
		HXFWordExtractor extractor = 
			new HXFWordExtractor(xmlB.getPackage());
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		char euro = '\u20ac';
		System.err.println("'"+text.substring(text.length() - 20) + "'");
		
		// Check contents
		assertTrue(text.startsWith(
				"  \n(V) ILLUSTRATIVE CASES\n\n"
		));
		assertTrue(text.endsWith(
				"As well as gaining "+euro+"90 from child benefit increases, he will also receive the early childhood supplement of "+euro+"250 per quarter for Vincent for the full four quarters of the year.\n\n\n\n \n\n\n"
		));
		
		// Check number of paragraphs
		int ps = 0;
		char[] t = text.toCharArray();
		for (int i = 0; i < t.length; i++) {
			if(t[i] == '\n') { ps++; }
		}
		assertEquals(79, ps);
	}
}
