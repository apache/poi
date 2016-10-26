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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

/**
 * Tests for XSLFPowerPointExtractor
 */
public class TestXSLFPowerPointExtractor {
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

	/**
	 * Get text out of the simple file
	 * @throws XmlException
	 * @throws OpenXML4JException
	 */
    @Test
    public void testGetSimpleText()
    throws IOException, XmlException, OpenXML4JException {
        XMLSlideShow xmlA = openPPTX("sample.pptx");
        @SuppressWarnings("resource")
        OPCPackage pkg = xmlA.getPackage();

	    new XSLFPowerPointExtractor(xmlA).close();
		new XSLFPowerPointExtractor(pkg).close();

		XSLFPowerPointExtractor extractor =
			new XSLFPowerPointExtractor(xmlA);
		extractor.getText();

		String text = extractor.getText();
		assertTrue(text.length() > 0);

		// Check Basics
		assertTrue(text.startsWith("Lorem ipsum dolor sit amet\n"));
		assertContains(text, "amet\n\n");

		// Our placeholder master text
		// This shouldn't show up in the output
	    // String masterText =
	    //     "Click to edit Master title style\n" +
        //     "Click to edit Master subtitle style\n" +
        //     "\n\n\n\n\n\n" +
        //     "Click to edit Master title style\n" +
        //     "Click to edit Master text styles\n" +
        //     "Second level\n" +
        //     "Third level\n" +
        //     "Fourth level\n" +
        //     "Fifth level\n";

		// Just slides, no notes
		text = extractor.getText(true, false, false);
		String slideText =
	        "Lorem ipsum dolor sit amet\n" +
            "Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
            "\n" +
            "Lorem ipsum dolor sit amet\n" +
            "Lorem\n" +
            "ipsum\n" +
            "dolor\n" +
            "sit\n" +
            "amet\n" +
            "\n";
		assertEquals(slideText, text);

		// Just notes, no slides
		text = extractor.getText(false, true);
		assertEquals("\n\n1\n\n\n2\n", text);

		// Both
		text = extractor.getText(true, true, false);
		String bothText =
	        "Lorem ipsum dolor sit amet\n" +
            "Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
            "\n\n\n1\n" +
            "Lorem ipsum dolor sit amet\n" +
            "Lorem\n" +
            "ipsum\n" +
            "dolor\n" +
            "sit\n" +
            "amet\n" +
            "\n\n\n2\n";
        assertEquals(bothText, text);

		// With Slides and Master Text
        text = extractor.getText(true, false, true);
        String smText =
            "Lorem ipsum dolor sit amet\n" +
            "Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
            "\n" +
            "Lorem ipsum dolor sit amet\n" +
            "Lorem\n" +
            "ipsum\n" +
            "dolor\n" +
            "sit\n" +
            "amet\n" +
            "\n";
        assertEquals(smText, text);

		// With Slides, Notes and Master Text
        text = extractor.getText(true, true, true);
        String snmText =
            "Lorem ipsum dolor sit amet\n" +
            "Nunc at risus vel erat tempus posuere. Aenean non ante.\n" +
            "\n\n\n1\n" +
            "Lorem ipsum dolor sit amet\n" +
            "Lorem\n" +
            "ipsum\n" +
            "dolor\n" +
            "sit\n" +
            "amet\n" +
            "\n\n\n2\n";
        assertEquals(snmText, text);

		// Via set defaults
		extractor.setSlidesByDefault(false);
		extractor.setNotesByDefault(true);
		text = extractor.getText();
		assertEquals("\n\n1\n\n\n2\n", text);

		extractor.close();
		xmlA.close();
	}

    public void testGetComments() throws IOException {
        XMLSlideShow xml = openPPTX("45545_Comment.pptx");
        XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xml);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        // Check comments are there
        assertContains(text, "testdoc");
        assertContains(text, "test phrase");

        // Check the authors came through too
        assertContains(text, "XPVMWARE01");

        extractor.close();
        xml.close();
    }

	public void testGetMasterText() throws Exception {
	    XMLSlideShow xml = openPPTX("WithMaster.pptx");
	    XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xml);
	    extractor.setSlidesByDefault(true);
	    extractor.setNotesByDefault(false);
	    extractor.setMasterByDefault(true);

	    String text = extractor.getText();
	    assertTrue(text.length() > 0);

	    // Check master text is there
	    assertContains(text, "Footer from the master slide");

	    // Theme text shouldn't show up
	    // String themeText =
        //     "Theme Master Title\n" +
        //     "Theme Master first level\n" +
        //     "And the 2nd level\n" +
        //     "Our 3rd level goes here\n" +
        //     "And onto the 4th, such fun....\n" +
        //     "Finally is the Fifth level\n";

	    // Check the whole text
	    String wholeText =
            "First page title\n" +
            "First page subtitle\n" +
            "This is the Master Title\n" +
            "This text comes from the Master Slide\n" +
            "\n" +
            // TODO Detect we didn't have a title, and include the master one
            "2nd page subtitle\n" +
            "Footer from the master slide\n" +
            "This is the Master Title\n" +
            "This text comes from the Master Slide\n";
	    assertEquals(wholeText, text);

		extractor.close();
		xml.close();
	}

	@Test
	public void testTable() throws Exception {
        XMLSlideShow xml = openPPTX("present1.pptx");
        XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xml);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        // Check comments are there
        assertTrue("Unable to find expected word in text\n" + text, text.contains("TEST"));

		extractor.close();
		xml.close();
    }

    /**
     * Test that we can get the text from macro enabled,
     *  template, theme, slide enabled etc formats, as
     *  well as from the normal file
     */
    @Test
    public void testDifferentSubformats() throws Exception {
        String[] extensions = new String[] {
            "pptx", "pptm", "ppsm", "ppsx", "thmx",
            // "xps" - Doesn't have a core document
        };
        for(String extension : extensions) {
            String filename = "testPPT." + extension;
            XMLSlideShow xml = openPPTX(filename);
            XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xml);

            String text = extractor.getText();
            if (extension.equals("thmx")) {
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

 		 extractor.close();
 		 xml.close();
       }
    }

    @Test
    public void test45541() throws Exception {
        // extract text from a powerpoint that has a header in the notes-element
        POITextExtractor extr = ExtractorFactory.createExtractor(
            slTests.getFile("45541_Header.pptx"));
        String text = extr.getText();
        assertNotNull(text);
        assertFalse("Had: " + text, text.contains("testdoc"));

        text = ((XSLFPowerPointExtractor)extr).getText(false, true);
        assertContains(text, "testdoc");
        extr.close();
       assertNotNull(text);

        // extract text from a powerpoint that has a footer in the master-slide
        extr = ExtractorFactory.createExtractor(
            slTests.getFile("45541_Footer.pptx"));
        text = extr.getText();
        assertNotContained(text, "testdoc");

        text = ((XSLFPowerPointExtractor)extr).getText(false, true);
        assertNotContained(text, "testdoc");

        text = ((XSLFPowerPointExtractor)extr).getText(false, false, true);
        assertNotContained(text, "testdoc");

        extr.close();
    }


    @Test
    public void bug54570() throws IOException {
        XMLSlideShow xml = openPPTX("bug54570.pptx");
        XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xml);
        String text = extractor.getText();
        assertNotNull(text);
        extractor.close();
        xml.close();
    }

    private XMLSlideShow openPPTX(String file) throws IOException {
        InputStream is = slTests.openResourceAsStream(file);
        try {
            return new XMLSlideShow(is);
        } finally {
            is.close();
        }
    }
}
