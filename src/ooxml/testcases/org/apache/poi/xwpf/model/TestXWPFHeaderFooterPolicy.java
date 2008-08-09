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
package org.apache.poi.xwpf.model;

import java.io.File;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.XWPFDocument;

import junit.framework.TestCase;

/**
 * Tests for XWPF Header Footer Stuff
 */
public class TestXWPFHeaderFooterPolicy extends TestCase {
	private XWPFDocument noHeader;
	private XWPFDocument header;
	private XWPFDocument headerFooter;
	private XWPFDocument footer;
	private XWPFDocument oddEven;
	private XWPFDocument diffFirst;
	
	protected void setUp() throws Exception {
		super.setUp();
		File file;
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "NoHeadFoot.docx"
		);
		assertTrue(file.exists());
		noHeader = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "ThreeColHead.docx"
		);
		assertTrue(file.exists());
		header = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "SimpleHeadThreeColFoot.docx"
		);
		assertTrue(file.exists());
		headerFooter = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "FancyFoot.docx"
		);
		assertTrue(file.exists());
		footer = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "PageSpecificHeadFoot.docx"
		);
		assertTrue(file.exists());
		oddEven = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
		
		file = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "DiffFirstPageHeadFoot.docx"
		);
		assertTrue(file.exists());
		diffFirst = new XWPFDocument(POIXMLDocument.openPackage(file.toString()));
	}
	
	public void testPolicy() throws Exception {
		XWPFHeaderFooterPolicy policy;
		
		policy = noHeader.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNull(policy.getDefaultFooter());
		
		assertNull(policy.getHeader(1));
		assertNull(policy.getHeader(2));
		assertNull(policy.getHeader(3));
		assertNull(policy.getFooter(1));
		assertNull(policy.getFooter(2));
		assertNull(policy.getFooter(3));
		
		
		policy = header.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNull(policy.getDefaultFooter());
		
		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertNull(policy.getFooter(1));
		assertNull(policy.getFooter(2));
		assertNull(policy.getFooter(3));
		
		
		policy = footer.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		
		assertNull(policy.getHeader(1));
		assertNull(policy.getHeader(2));
		assertNull(policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
		
		
		policy = headerFooter.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		
		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
		
		
		policy = oddEven.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		assertNotNull(policy.getEvenPageHeader());
		assertNotNull(policy.getEvenPageFooter());
		
		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getEvenPageHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getEvenPageFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
		
		
		policy = diffFirst.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		assertNotNull(policy.getFirstPageHeader());
		assertNotNull(policy.getFirstPageFooter());
		assertNull(policy.getEvenPageHeader());
		assertNull(policy.getEvenPageFooter());
		
		assertEquals(policy.getFirstPageHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getFirstPageFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
	}
	
	public void testContents() throws Exception {
		XWPFHeaderFooterPolicy policy;
		
		// Just test a few bits
		policy = diffFirst.getHeaderFooterPolicy();
		
		assertEquals(
			"I am the header on the first page, and I" + '\u2019' + "m nice and simple\n",
			policy.getFirstPageHeader().getText()
		);
		assertEquals(
				"First header column!\tMid header\tRight header!\n", 
				policy.getDefaultHeader().getText()
		);
	}
}
