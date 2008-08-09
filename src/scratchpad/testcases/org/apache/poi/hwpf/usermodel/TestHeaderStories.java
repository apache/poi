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

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;

/**
 * Tests for the handling of header stories into
 *  headers, footers etc
 */
public class TestHeaderStories extends TestCase {
	private HWPFDocument none;
	private HWPFDocument header; 
	private HWPFDocument footer; 
	private HWPFDocument headerFooter; 
	private HWPFDocument oddEven; 
	private HWPFDocument diffFirst; 
	private HWPFDocument unicode;
	
    protected void setUp() throws Exception {
		String dirname = System.getProperty("HWPF.testdata.path");
		
		none = new HWPFDocument(
				new FileInputStream(new File(dirname, "NoHeadFoot.doc"))
		);
		header = new HWPFDocument(
				new FileInputStream(new File(dirname, "ThreeColHead.doc"))
		);
		footer = new HWPFDocument(
				new FileInputStream(new File(dirname, "ThreeColFoot.doc"))
		);
		headerFooter = new HWPFDocument(
				new FileInputStream(new File(dirname, "SimpleHeadThreeColFoot.doc"))
		);
		oddEven = new HWPFDocument(
				new FileInputStream(new File(dirname, "PageSpecificHeadFoot.doc"))
		);
		diffFirst = new HWPFDocument(
				new FileInputStream(new File(dirname, "DiffFirstPageHeadFoot.doc"))
		);
		unicode = new HWPFDocument(
				new FileInputStream(new File(dirname, "HeaderFooterUnicode.doc"))
		);
    }
    
    public void testNone() throws Exception {
    	HeaderStories hs = new HeaderStories(none);
    	
    	assertNull(hs.getPlcfHdd());
    	assertEquals(0, hs.getRange().text().length());
    }
    
    public void testHeader() throws Exception {
    	HeaderStories hs = new HeaderStories(header);
    	
    	assertEquals(60, hs.getRange().text().length());
    	
    	// Should have the usual 6 separaters
    	// Then all 6 of the different header/footer kinds
    	// Finally a terminater
    	assertEquals(13, hs.getPlcfHdd().length());
    	
    	assertEquals(215, hs.getRange().getStartOffset());
    	
    	assertEquals(0, hs.getPlcfHdd().getProperty(0).getStart());
    	assertEquals(3, hs.getPlcfHdd().getProperty(1).getStart());
    	assertEquals(6, hs.getPlcfHdd().getProperty(2).getStart());
    	assertEquals(6, hs.getPlcfHdd().getProperty(3).getStart());
    	assertEquals(9, hs.getPlcfHdd().getProperty(4).getStart());
    	assertEquals(12, hs.getPlcfHdd().getProperty(5).getStart());
    	
    	assertEquals(12, hs.getPlcfHdd().getProperty(6).getStart());
    	assertEquals(12, hs.getPlcfHdd().getProperty(7).getStart());
    	assertEquals(59, hs.getPlcfHdd().getProperty(8).getStart());
    	assertEquals(59, hs.getPlcfHdd().getProperty(9).getStart());
    	assertEquals(59, hs.getPlcfHdd().getProperty(10).getStart());
    	assertEquals(59, hs.getPlcfHdd().getProperty(11).getStart());
    	
    	assertEquals(59, hs.getPlcfHdd().getProperty(12).getStart());
    	
    	assertEquals("\u0003\r\r", hs.getFootnoteSeparator());
		assertEquals("\u0004\r\r", hs.getFootnoteContSeparator());
		assertEquals("", hs.getFootnoteContNote());
		assertEquals("\u0003\r\r", hs.getEndnoteSeparator());
		assertEquals("\u0004\r\r", hs.getEndnoteContSeparator());
		assertEquals("", hs.getEndnoteContNote());

    }
}
