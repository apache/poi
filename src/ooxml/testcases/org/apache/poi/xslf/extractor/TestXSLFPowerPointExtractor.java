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
package org.apache.poi.xslf.extractor;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;

import junit.framework.TestCase;

/**
 * Tests for HXFPowerPointExtractor
 */
public class TestXSLFPowerPointExtractor extends TestCase {
	/**
	 * A simple file
	 */
	private XSLFSlideShow xmlA;
	private OPCPackage pkg;

	protected void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		pkg = OPCPackage.open(slTests.openResourceAsStream("sample.pptx"));
		xmlA = new XSLFSlideShow(pkg);
	}

	/**
	 * Get text out of the simple file
	 */
	public void testGetSimpleText() throws Exception {
		new XSLFPowerPointExtractor(xmlA);
		new XSLFPowerPointExtractor(pkg);
		
		XSLFPowerPointExtractor extractor = 
			new XSLFPowerPointExtractor(xmlA);
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Check Basics
		assertTrue(text.startsWith("Lorem ipsum dolor sit amet\n"));
		assertTrue(text.endsWith("amet\n\n"));
		
		// Just slides, no notes
		text = extractor.getText(true, false);
		assertEquals(
				"Lorem ipsum dolor sit amet\n" +
				"Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
				"\n" +
				"Lorem ipsum dolor sit amet\n" +
				"Lorem\n" +
				"ipsum\n" +
				"dolor\n" +
				"sit\n" +
				"amet\n" +
				"\n", text
		);
		
		// Just notes, no slides
		text = extractor.getText(false, true);
		assertEquals(
				"\n\n\n\n", text
		);
		
		// Both
		text = extractor.getText(true, true);
		assertEquals(
				"Lorem ipsum dolor sit amet\n" +
				"Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
				"\n\n\n" +
				"Lorem ipsum dolor sit amet\n" +
				"Lorem\n" +
				"ipsum\n" +
				"dolor\n" +
				"sit\n" +
				"amet\n" +
				"\n\n\n", text
		);
		
		// Via set defaults
		extractor.setSlidesByDefault(false);
		extractor.setNotesByDefault(true);
		text = extractor.getText();
		assertEquals(
				"\n\n\n\n", text
		);
	}
	
	public void testGetComments() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		xmlA = new XSLFSlideShow(OPCPackage.open(slTests.openResourceAsStream("45545_Comment.pptx")));
		XSLFPowerPointExtractor extractor = 
			new XSLFPowerPointExtractor(xmlA);
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Check comments are there
		assertTrue("Unable to find expected word in text\n" + text, text.contains("testdoc"));
	}

    public void testTable() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        xmlA = new XSLFSlideShow(OPCPackage.open(slTests.openResourceAsStream("present1.pptx")));
        XSLFPowerPointExtractor extractor =
            new XSLFPowerPointExtractor(xmlA);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        // Check comments are there
        assertTrue("Unable to find expected word in text\n" + text, text.contains("TEST"));
    }
    
    /**
     * Test that we can get the text from macro enabled,
     *  template, theme, slide enabled etc formats, as 
     *  well as from the normal file
     */
    public void testDifferentSubformats() throws Exception {
       POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
       String[] extensions = new String[] {
             "pptx", "pptm", "ppsm", "ppsx",
             "thmx", 
             //"xps" // Doesn't have a core document
       };
       for(String extension : extensions) {
          String filename = "testPPT." + extension;
          xmlA = new XSLFSlideShow(OPCPackage.open(slTests.openResourceAsStream(filename)));
          XSLFPowerPointExtractor extractor =
             new XSLFPowerPointExtractor(xmlA);

         String text = extractor.getText();
         if(extension.equals("thmx")) {
            // Theme file doesn't have any textual content
            assertEquals(0, text.length());
            continue;
         }
         
         assertTrue(text.length() > 0);
         assertTrue(
               "Text missing for " + filename + "\n" + text, 
               text.contains("Attachment Test")
         );
         assertTrue(
               "Text missing for " + filename + "\n" + text, 
               text.contains("This is a test file data with the same content")
         );
         assertTrue(
               "Text missing for " + filename + "\n" + text, 
               text.contains("content parsing")
         );
         assertTrue(
               "Text missing for " + filename + "\n" + text, 
               text.contains("Different words to test against")
         );
         assertTrue(
               "Text missing for " + filename + "\n" + text, 
               text.contains("Mystery")
         );
       }
    }
}
