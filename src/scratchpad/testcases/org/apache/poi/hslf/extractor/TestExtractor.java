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

import java.io.FileInputStream;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

/**
 * Tests that the extractor correctly gets the text out of our sample file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestExtractor extends TestCase {
	/** Extractor primed on the 2 page basic test data */
	private PowerPointExtractor ppe;
	/** Extractor primed on the 1 page but text-box'd test data */
	private PowerPointExtractor ppe2;
	/** Where to go looking for our test files */
	private String dirname;
	/** Where our embeded files live */
	private String pdirname;

    protected void setUp() throws Exception {
		dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		ppe = new PowerPointExtractor(filename);
		String filename2 = dirname + "/with_textbox.ppt";
		ppe2 = new PowerPointExtractor(filename2);
		
		pdirname = System.getProperty("POIFS.testdata.path");
    }

    public void testReadSheetText() {
    	// Basic 2 page example
		String sheetText = ppe.getText();
		String expectText = "This is a test title\nThis is a test subtitle\nThis is on page 1\nThis is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n";

		ensureTwoStringsTheSame(expectText, sheetText);
		
		
		// 1 page example with text boxes
		sheetText = ppe2.getText();
		expectText = "Hello, World!!!\nI am just a poor boy\nThis is Times New Roman\nPlain Text \n"; 

		ensureTwoStringsTheSame(expectText, sheetText);
    }
    
	public void testReadNoteText() {
		// Basic 2 page example
		String notesText = ppe.getNotes();
		String expectText = "These are the notes for page 1\nThese are the notes on page two, again lacking formatting\n";

		ensureTwoStringsTheSame(expectText, notesText);
		
		// Other one doesn't have notes
		notesText = ppe2.getNotes();
		expectText = "";
		
		ensureTwoStringsTheSame(expectText, notesText);
	}
	
	public void testReadBoth() {
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
	
    private void ensureTwoStringsTheSame(String exp, String act) {
		assertEquals(exp.length(),act.length());
		char[] expC = exp.toCharArray();
		char[] actC = act.toCharArray();
		for(int i=0; i<expC.length; i++) {
			assertEquals("Char " + i, expC[i], actC[i]);
		}
		assertEquals(exp,act);
    }
    
    public void testExtractFromEmbeded() throws Exception {
    	String filename3 = pdirname + "/excel_with_embeded.xls";
    	POIFSFileSystem fs = new POIFSFileSystem(
    			new FileInputStream(filename3)
    	);
    	HSLFSlideShow ss;
    	
    	DirectoryNode dirA = (DirectoryNode)
    		fs.getRoot().getEntry("MBD0000A3B6");
		DirectoryNode dirB = (DirectoryNode)
			fs.getRoot().getEntry("MBD0000A3B3");
		
		assertNotNull(dirA.getEntry("PowerPoint Document"));
		assertNotNull(dirB.getEntry("PowerPoint Document"));
    	
		// Check the first file
    	ss = new HSLFSlideShow(dirA, fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("Sample PowerPoint file\nThis is the 1st file\nNot much too it\n",
				ppe.getText(true, false)
		);

		// And the second
    	ss = new HSLFSlideShow(dirB, fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("Sample PowerPoint file\nThis is the 2nd file\nNot much too it either\n",
				ppe.getText(true, false)
		);
    }

    /**
     * A powerpoint file with embeded powerpoint files
     * TODO - figure out how to handle this, as ppt
     *  appears to embed not as ole2 streams
     */
    public void DISABLEDtestExtractFromOwnEmbeded() throws Exception {
    	String filename3 = pdirname + "/ppt_with_embeded.ppt";
    	POIFSFileSystem fs = new POIFSFileSystem(
    			new FileInputStream(filename3)
    	);
    	HSLFSlideShow ss;
    	
    	DirectoryNode dirA = (DirectoryNode)
    		fs.getRoot().getEntry("MBD0000A3B6");
		DirectoryNode dirB = (DirectoryNode)
			fs.getRoot().getEntry("MBD0000A3B3");
		
		assertNotNull(dirA.getEntry("PowerPoint Document"));
		assertNotNull(dirB.getEntry("PowerPoint Document"));
    	
		// Check the first file
    	ss = new HSLFSlideShow(dirA, fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("Sample PowerPoint file\nThis is the 1st file\nNot much too it\n",
				ppe.getText(true, false)
		);

		// And the second
    	ss = new HSLFSlideShow(dirB, fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("Sample PowerPoint file\nThis is the 2nd file\nNot much too it either\n",
				ppe.getText(true, false)
		);
		
		
		// Check the master doc two ways
    	ss = new HSLFSlideShow(fs.getRoot(), fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("I have embeded files in me\n",
				ppe.getText(true, false)
		);
		
    	ss = new HSLFSlideShow(fs);
		ppe = new PowerPointExtractor(ss);
		assertEquals("I have embeded files in me\n",
				ppe.getText(true, false)
		);
    }
    
    /**
     * From bug #45543
     */
    public void testWithComments() throws Exception {
		String filename;
		
		// New file
		filename = dirname + "/WithComments.ppt";
		ppe = new PowerPointExtractor(filename);

		String text = ppe.getText();
		assertFalse("Comments not in by default", contains(text, "This is a test comment"));
		
		ppe.setCommentsByDefault(true);
		
		text = ppe.getText();
		assertTrue("Unable to find expected word in text\n" + text, contains(text, "This is a test comment"));

		
		// And another file
		filename = dirname + "/45543.ppt";
		ppe = new PowerPointExtractor(filename);

        text = ppe.getText();
		assertFalse("Comments not in by default", contains(text, "testdoc"));
		
		ppe.setCommentsByDefault(true);
		
		text = ppe.getText();
		assertTrue("Unable to find expected word in text\n" + text, contains(text, "testdoc"));
    }
    
    /**
     * From bug #45537
     */
    public void testHeaderFooter() throws Exception {
		String filename, text;
		
		// With a header on the notes
		filename = dirname + "/45537_Header.ppt";
		HSLFSlideShow hslf = new HSLFSlideShow(new FileInputStream(filename));
		SlideShow ss = new SlideShow(hslf);
		assertNotNull(ss.getNotesHeadersFooters());
		assertEquals("testdoc test phrase", ss.getNotesHeadersFooters().getHeaderText());
		
		ppe = new PowerPointExtractor(hslf);

		text = ppe.getText();
		assertFalse("Unable to find expected word in text\n" + text, contains(text, "testdoc"));
        assertFalse("Unable to find expected word in text\n" + text, contains(text, "test phrase"));
        
        ppe.setNotesByDefault(true);
		text = ppe.getText();
		assertTrue("Unable to find expected word in text\n" + text, contains(text, "testdoc"));
        assertTrue("Unable to find expected word in text\n" + text, contains(text, "test phrase"));

        
		// And with a footer, also on notes
		filename = dirname + "/45537_Footer.ppt";
		hslf = new HSLFSlideShow(new FileInputStream(filename));
		ss = new SlideShow(hslf);
		assertNotNull(ss.getNotesHeadersFooters());
		assertEquals("testdoc test phrase", ss.getNotesHeadersFooters().getFooterText());
		
		ppe = new PowerPointExtractor(filename);

		text = ppe.getText();
		assertFalse("Unable to find expected word in text\n" + text, contains(text, "testdoc"));
        assertFalse("Unable to find expected word in text\n" + text, contains(text, "test phrase"));

        ppe.setNotesByDefault(true);
		text = ppe.getText();
		assertTrue("Unable to find expected word in text\n" + text, contains(text, "testdoc"));
        assertTrue("Unable to find expected word in text\n" + text, contains(text, "test phrase"));
    }

	private static boolean contains(String text, String searchString) {
		return text.indexOf(searchString) >=0;
	}
}
