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

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

import junit.framework.TestCase;

public class TestXSSFHeaderFooter extends TestCase {
	public void testStripFields() {
		String simple = "I am a test header";
		String withPage = "I am a&P test header";
		String withLots = "I&A am&N a&P test&T header&U";
		String withFont = "I&22 am a&\"Arial,bold\" test header";
		String withOtherAnds = "I am a&P test header&&";
		String withOtherAnds2 = "I am a&P test header&a&b";
		
		assertEquals(simple, XSSFOddHeader.stripFields(simple));
		assertEquals(simple, XSSFOddHeader.stripFields(withPage));
		assertEquals(simple, XSSFOddHeader.stripFields(withLots));
		assertEquals(simple, XSSFOddHeader.stripFields(withFont));
		assertEquals(simple + "&&", XSSFOddHeader.stripFields(withOtherAnds));
		assertEquals(simple + "&a&b", XSSFOddHeader.stripFields(withOtherAnds2));
		
		// Now test the default strip flag
		XSSFEvenHeader head = new XSSFEvenHeader(CTHeaderFooter.Factory.newInstance());
		head.setCenter("Center");
		head.setLeft("In the left");
    
    	assertEquals("In the left", head.getLeft());
    	assertEquals("Center", head.getCenter());
    	assertEquals("", head.getRight());
    	
    	head.setLeft("Top &P&F&D Left");
    	assertEquals("Top &P&F&D Left", head.getLeft());
    	assertFalse(head.areFieldsStripped());
    	
    	head.setAreFieldsStripped(true);
    	assertEquals("Top  Left", head.getLeft());
    	assertTrue(head.areFieldsStripped());
    	
    	// Now even more complex
    	head.setCenter("HEADER TEXT &P&N&D&T&Z&F&F&A&G");
    	assertEquals("HEADER TEXT &G", head.getCenter());
	}
	
	// TODO Rest of tests
}
