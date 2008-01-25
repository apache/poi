
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
        


package org.apache.poi.hslf.extractor;


import junit.framework.TestCase;

/**
 * Tests that the extractor correctly gets the text out of our sample file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TextExtractor extends TestCase {
	/** Extractor primed on the 2 page basic test data */
	private PowerPointExtractor ppe;
	/** Extractor primed on the 1 page but text-box'd test data */
	private PowerPointExtractor ppe2;
	/** Where to go looking for our test files */
	private String dirname;

    public TextExtractor() throws Exception {
		dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		ppe = new PowerPointExtractor(filename);
		String filename2 = dirname + "/with_textbox.ppt";
		ppe2 = new PowerPointExtractor(filename2);
    }

    public void testReadSheetText() throws Exception {
    	// Basic 2 page example
		String sheetText = ppe.getText();
		String expectText = "This is a test title\nThis is a test subtitle\nThis is on page 1\nThis is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n";

		ensureTwoStringsTheSame(expectText, sheetText);
		
		
		// 1 page example with text boxes
		sheetText = ppe2.getText();
		expectText = "Hello, World!!!\nI am just a poor boy\nThis is Times New Roman\nPlain Text \n"; 

		ensureTwoStringsTheSame(expectText, sheetText);
    }
    
	public void testReadNoteText() throws Exception {
		// Basic 2 page example
		String notesText = ppe.getNotes();
		String expectText = "These are the notes for page 1\nThese are the notes on page two, again lacking formatting\n";

		ensureTwoStringsTheSame(expectText, notesText);
		
		// Other one doesn't have notes
		notesText = ppe2.getNotes();
		expectText = "";
		
		ensureTwoStringsTheSame(expectText, notesText);
	}
	
	public void testReadBoth() throws Exception {
		String[] slText = new String[] {
				"This is a test title\nThis is a test subtitle\nThis is on page 1\n",
				"This is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n"
		};
		String[] ntText = new String[] {
				"These are the notes for page 1\n",
				"These are the notes on page two, again lacking formatting\n"
		};
		
		ppe.setSlidesByDefault(true);
		ppe.setNotesByDefault(false);
		assertEquals(slText[0]+slText[1], ppe.getText());
		
		ppe.setSlidesByDefault(false);
		ppe.setNotesByDefault(true);
		assertEquals(ntText[0]+ntText[1], ppe.getText());
		
		ppe.setSlidesByDefault(true);
		ppe.setNotesByDefault(true);
		assertEquals(slText[0]+slText[1]+"\n"+ntText[0]+ntText[1], ppe.getText());
	}

	/**
	 * Test that when presented with a PPT file missing the odd
	 *  core record, we can still get the rest of the text out
	 * @throws Exception
	 */
	public void testMissingCoreRecords() throws Exception {
		String filename = dirname + "/missing_core_records.ppt";
		ppe = new PowerPointExtractor(filename);
		
		String text = ppe.getText(true, false);
		String nText = ppe.getNotes();

		assertNotNull(text);
		assertNotNull(nText);
		
		// Notes record were corrupt, so don't expect any
		assertEquals(nText.length(), 0);
		
		// Slide records were fine
		assertTrue(text.startsWith("Using Disease Surveillance and Response"));
	}
	
    private void ensureTwoStringsTheSame(String exp, String act) throws Exception {
		assertEquals(exp.length(),act.length());
		char[] expC = exp.toCharArray();
		char[] actC = act.toCharArray();
		for(int i=0; i<expC.length; i++) {
			System.out.println(i + "\t" + expC[i] + " " + actC[i]);
			assertEquals(expC[i],actC[i]);
		}
		assertEquals(exp,act);
    }
}
