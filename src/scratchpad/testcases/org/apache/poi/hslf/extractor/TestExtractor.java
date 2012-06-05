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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

/**
 * Tests that the extractor correctly gets the text out of our sample file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestExtractor extends TestCase {
   /** Extractor primed on the 2 page basic test data */
   private PowerPointExtractor ppe;
   private static final String expectText = "This is a test title\nThis is a test subtitle\nThis is on page 1\nThis is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n";

   /** Extractor primed on the 1 page but text-box'd test data */
   private PowerPointExtractor ppe2;
   private static final String expectText2 = "Hello, World!!!\nI am just a poor boy\nThis is Times New Roman\nPlain Text \n";
   
   /** Where our embeded files live */
   //private String pdirname;
   private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
   //private String pdirname;

   protected void setUp() throws Exception {
      ppe = new PowerPointExtractor(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
      ppe2 = new PowerPointExtractor(slTests.openResourceAsStream("with_textbox.ppt"));
   }

   private static void assertContains(String haystack, String needle) {
      assertContains(
            "Unable to find expected text '" + needle + "' in text:\n" + haystack,
            haystack, needle
      );
   }
   private static void assertContains(String reason, String haystack, String needle) {
      assertTrue(reason, haystack.contains(needle));
   }

    public void testReadSheetText() {
    	// Basic 2 page example
		String sheetText = ppe.getText();

		ensureTwoStringsTheSame(expectText, sheetText);
		
		
		// 1 page example with text boxes
		sheetText = ppe2.getText();

		ensureTwoStringsTheSame(expectText2, sheetText);
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
		ppe = new PowerPointExtractor(slTests.openResourceAsStream("missing_core_records.ppt"));

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
         POIFSFileSystem fs = new POIFSFileSystem(
             POIDataSamples.getSpreadSheetInstance().openResourceAsStream("excel_with_embeded.xls")
         );
         HSLFSlideShow ss;

         DirectoryNode dirA = (DirectoryNode)
             fs.getRoot().getEntry("MBD0000A3B6");
         DirectoryNode dirB = (DirectoryNode)
             fs.getRoot().getEntry("MBD0000A3B3");

         assertNotNull(dirA.getEntry("PowerPoint Document"));
         assertNotNull(dirB.getEntry("PowerPoint Document"));

         // Check the first file
         ss = new HSLFSlideShow(dirA);
         ppe = new PowerPointExtractor(ss);
         assertEquals("Sample PowerPoint file\nThis is the 1st file\nNot much too it\n",
                 ppe.getText(true, false)
         );

         // And the second
         ss = new HSLFSlideShow(dirB);
         ppe = new PowerPointExtractor(ss);
         assertEquals("Sample PowerPoint file\nThis is the 2nd file\nNot much too it either\n",
                 ppe.getText(true, false)
         );
     }

     /**
      * A powerpoint file with embeded powerpoint files
      */
     public void testExtractFromOwnEmbeded() throws Exception {
         String path = "ppt_with_embeded.ppt";
         ppe = new PowerPointExtractor(POIDataSamples.getSlideShowInstance().openResourceAsStream(path));
         List<OLEShape> shapes = ppe.getOLEShapes();
         assertEquals("Expected 6 ole shapes in " + path, 6, shapes.size());
         int num_ppt = 0, num_doc = 0, num_xls = 0;
         for(OLEShape ole : shapes) {
             String name = ole.getInstanceName();
             InputStream data = ole.getObjectData().getData();
             if ("Worksheet".equals(name)) {
                 HSSFWorkbook wb = new HSSFWorkbook(data);
                 num_xls++;
             } else if ("Document".equals(name)) {
                 HWPFDocument doc = new HWPFDocument(data);
                 num_doc++;
             } else if ("Presentation".equals(name)) {
                 num_ppt++;
                 SlideShow ppt = new SlideShow(data);
             }
         }
         assertEquals("Expected 2 embedded Word Documents", 2, num_doc);
         assertEquals("Expected 2 embedded Excel Spreadsheets", 2, num_xls);
         assertEquals("Expected 2 embedded PowerPoint Presentations", 2, num_ppt);
     }

    /**
     * A powerpoint file with embeded powerpoint files
     */
    public void test52991() throws Exception {
        String path = "badzip.ppt";
        ppe = new PowerPointExtractor(POIDataSamples.getSlideShowInstance().openResourceAsStream(path));
        List<OLEShape> shapes = ppe.getOLEShapes();
        
        for (OLEShape shape : shapes) {
            IOUtils.copy(shape.getObjectData().getData(), new ByteArrayOutputStream());
        }
    }

    /**
     * From bug #45543
     */
    public void testWithComments() throws Exception {
		ppe = new PowerPointExtractor(slTests.openResourceAsStream("WithComments.ppt"));

		String text = ppe.getText();
		assertFalse("Comments not in by default", text.contains("This is a test comment"));
		
		ppe.setCommentsByDefault(true);
		
		text = ppe.getText();
		assertContains(text, "This is a test comment");

		
		// And another file
		ppe = new PowerPointExtractor(slTests.openResourceAsStream("45543.ppt"));

		text = ppe.getText();
		assertFalse("Comments not in by default", text.contains("testdoc"));
		
		ppe.setCommentsByDefault(true);
		
		text = ppe.getText();
		assertContains(text, "testdoc");
    }
    
    /**
     * From bug #45537
     */
    public void testHeaderFooter() throws Exception {
       String  text;

       // With a header on the notes
       HSLFSlideShow hslf = new HSLFSlideShow(slTests.openResourceAsStream("45537_Header.ppt"));
       SlideShow ss = new SlideShow(hslf);
       assertNotNull(ss.getNotesHeadersFooters());
       assertEquals("testdoc test phrase", ss.getNotesHeadersFooters().getHeaderText());

       ppe = new PowerPointExtractor(hslf);

       text = ppe.getText();
       assertFalse("Header shouldn't be there by default\n" + text, text.contains("testdoc"));
       assertFalse("Header shouldn't be there by default\n" + text, text.contains("test phrase"));

       ppe.setNotesByDefault(true);
       text = ppe.getText();
       assertContains(text, "testdoc");
       assertContains(text, "test phrase");


       // And with a footer, also on notes
       hslf = new HSLFSlideShow(slTests.openResourceAsStream("45537_Footer.ppt"));
       ss = new SlideShow(hslf);
       assertNotNull(ss.getNotesHeadersFooters());
       assertEquals("testdoc test phrase", ss.getNotesHeadersFooters().getFooterText());

       ppe = new PowerPointExtractor(slTests.openResourceAsStream("45537_Footer.ppt"));

       text = ppe.getText();
       assertFalse("Header shouldn't be there by default\n" + text, text.contains("testdoc"));
       assertFalse("Header shouldn't be there by default\n" + text, text.contains("test phrase"));

       ppe.setNotesByDefault(true);
       text = ppe.getText();
       assertContains(text, "testdoc");
       assertContains(text, "test phrase");
    }
    
   public void testSlideMasterText() throws Exception {
      String masterTitleText = "This is the Master Title";
      String masterRandomText = "This text comes from the Master Slide";
      String masterFooterText = "Footer from the master slide";
      HSLFSlideShow hslf = new HSLFSlideShow(slTests.openResourceAsStream("WithMaster.ppt"));
      
      ppe = new PowerPointExtractor(hslf);
      
      String text = ppe.getText();
      //assertContains(text, masterTitleText); // TODO Is this available in PPT?
      //assertContains(text, masterRandomText); // TODO Extract
      assertContains(text, masterFooterText);
   }

    public void testMasterText() throws Exception {
       ppe = new PowerPointExtractor(slTests.openResourceAsStream("master_text.ppt"));
       
       // Initially not there
       String text = ppe.getText();
       assertFalse(text.contains("Text that I added to the master slide"));
       
       // Enable, shows up
       ppe.setMasterByDefault(true);
       text = ppe.getText();
       assertTrue(text.contains("Text that I added to the master slide"));

       // Make sure placeholder text does not come out
       assertFalse(text.contains("Click to edit Master"));
       
       // Now with another file only containing master text
       // Will always show up
       String masterText = "Footer from the master slide";
       HSLFSlideShow hslf = new HSLFSlideShow(slTests.openResourceAsStream("WithMaster.ppt"));
       
       ppe = new PowerPointExtractor(hslf);
       
       text = ppe.getText();
       assertContains(text.toLowerCase(), "master");
       assertContains(text, masterText);
    }

    
    /**
     * Tests that we can work with both {@link POIFSFileSystem}
     *  and {@link NPOIFSFileSystem}
     */
    public void testDifferentPOIFS() throws Exception {
       // Open the two filesystems
       DirectoryNode[] files = new DirectoryNode[2];
       files[0] = (new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"))).getRoot();
       files[1] = (new NPOIFSFileSystem(slTests.getFile("basic_test_ppt_file.ppt"))).getRoot();
       
       // Open directly 
       for(DirectoryNode dir : files) {
          PowerPointExtractor extractor = new PowerPointExtractor(dir, null);
          assertEquals(expectText, extractor.getText());
       }

       // Open via a HWPFDocument
       for(DirectoryNode dir : files) {
          HSLFSlideShow slideshow = new HSLFSlideShow(dir);
          PowerPointExtractor extractor = new PowerPointExtractor(slideshow);
          assertEquals(expectText, extractor.getText());
       }
    }
}
