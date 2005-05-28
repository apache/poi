
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
	// Extractor primed on the test data
	private PowerPointExtractor ppe;

    public TextExtractor() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		ppe = new PowerPointExtractor(filename);
    }

    public void testReadSheetText() throws Exception {
		String sheetText = ppe.getText();
		String expectText = "This is a test title\nThis is a test subtitle\nThis is on page 1\nThis is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n";

		assertEquals(expectText.length(),sheetText.length());
		char[] st = sheetText.toCharArray();
		char[] et = expectText.toCharArray();
		for(int i=0; i<et.length; i++) {
			System.out.println(i + "\t" + et[i] + " " + st[i]);
			assertEquals(et[i],st[i]);
		}
		assertEquals(expectText,sheetText);
    }

	public void testReadNoteText() throws Exception {
		String notesText = ppe.getNotes();
		String expectText = "These are the notes for page 1\nThese are the notes on page two, again lacking formatting\n";

		assertEquals(expectText.length(),notesText.length());
		char[] nt = notesText.toCharArray();
		char[] et = expectText.toCharArray();
		for(int i=0; i<et.length; i++) {
			System.out.println(i + "\t" + et[i] + " " + nt[i]);
			assertEquals(et[i],nt[i]);
		}
		assertEquals(expectText,notesText);
	}
}
