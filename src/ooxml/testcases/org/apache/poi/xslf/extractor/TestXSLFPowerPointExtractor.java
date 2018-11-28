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
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

/**
 * Tests for XSLFPowerPointExtractor
 */
public class TestXSLFPowerPointExtractor {
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

	/**
	 * Get text out of the simple file
	 */
    @Test
    public void testGetSimpleText() throws IOException {
        try (XMLSlideShow xmlA = openPPTX("sample.pptx");
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xmlA)) {

            extractor.getText();

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            // Check Basics
            assertStartsWith(text, "Lorem ipsum dolor sit amet\n");
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
            extractor.setSlidesByDefault(true);
            extractor.setNotesByDefault(false);
            extractor.setMasterByDefault(false);
            text = extractor.getText();
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
            extractor.setSlidesByDefault(false);
            extractor.setNotesByDefault(true);
            text = extractor.getText();
            assertEquals("\n\n1\n\n\n2\n", text);

            // Both
            extractor.setSlidesByDefault(true);
            extractor.setNotesByDefault(true);
            text = extractor.getText();
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
            extractor.setSlidesByDefault(true);
            extractor.setNotesByDefault(false);
            extractor.setMasterByDefault(true);
            text = extractor.getText();
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
            extractor.setSlidesByDefault(true);
            extractor.setNotesByDefault(true);
            extractor.setMasterByDefault(true);
            text = extractor.getText();
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
        }
	}

    @Test
    public void testGetComments() throws IOException {
        try (XMLSlideShow xml = openPPTX("45545_Comment.pptx");
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml)) {
            extractor.setCommentsByDefault(true);

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            // Check comments are there
            assertContains(text, "testdoc");
            assertContains(text, "test phrase");

            // Check the authors came through too
            assertContains(text, "XPVMWARE01");
        }
    }

    @Test
	public void testGetMasterText() throws Exception {
	    try (XMLSlideShow xml = openPPTX("WithMaster.pptx");
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml)) {
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
                "This text comes from the Master Slide\n" +
                "\n" +
                "2nd page subtitle\n" +
                "Footer from the master slide\n" +
                "This text comes from the Master Slide\n";
            assertEquals(wholeText, text);
        }
	}

	@Test
	public void testTable() throws Exception {
        try (XMLSlideShow xml = openPPTX("present1.pptx");
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml)) {

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            // Check comments are there
            assertContains(text, "TEST");
        }
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

            try (XMLSlideShow xml = openPPTX(filename);
                 SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml)) {

                String text = extractor.getText();
                if (extension.equals("thmx")) {
                    // Theme file doesn't have any textual content
                    assertEquals(filename, 0, text.length());
                    continue;
                }

                assertTrue(filename, text.length() > 0);
                assertContains(filename, text, "Attachment Test");
                assertContains(filename, text, "This is a test file data with the same content");
                assertContains(filename, text, "content parsing");
                assertContains(filename, text, "Different words to test against");
                assertContains(filename, text, "Mystery");
            }
       }
    }

    @Test
    public void test45541() throws IOException, OpenXML4JException, XmlException {
        // extract text from a powerpoint that has a header in the notes-element
        final File headerFile = slTests.getFile("45541_Header.pptx");
        try (final SlideShowExtractor extr = ExtractorFactory.createExtractor(headerFile)) {
            String text = extr.getText();
            assertNotNull(text);
            assertFalse("Had: " + text, text.contains("testdoc"));

            extr.setSlidesByDefault(false);
            extr.setNotesByDefault(true);

            text = extr.getText();
            assertContains(text, "testdoc");
            assertNotNull(text);
        }

        // extract text from a powerpoint that has a footer in the master-slide
        final File footerFile = slTests.getFile("45541_Footer.pptx");
        try (SlideShowExtractor extr = ExtractorFactory.createExtractor(footerFile)) {
            String text = extr.getText();
            assertNotContained(text, "testdoc");

            extr.setSlidesByDefault(false);
            extr.setNotesByDefault(true);
            text = extr.getText();
            assertNotContained(text, "testdoc");

            extr.setSlidesByDefault(false);
            extr.setNotesByDefault(false);
            extr.setMasterByDefault(true);
            text = extr.getText();
            assertNotContained(text, "testdoc");
        }
    }


    @Test
    public void bug54570() throws IOException {
        try (XMLSlideShow xml = openPPTX("bug54570.pptx");
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml)) {
            String text = extractor.getText();
            assertNotNull(text);
        }
    }

    private XMLSlideShow openPPTX(String file) throws IOException {
        try (InputStream is = slTests.openResourceAsStream(file)) {
            return new XMLSlideShow(is);
        }
    }

    @Test
    public void setSlTests() throws IOException {
        try (XMLSlideShow xml = openPPTX("aascu.org_hbcu_leadershipsummit_cooper_.pptx")) {
            SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(xml);
            assertNotNull(extractor);
            extractor.setSlidesByDefault(true);
            extractor.setNotesByDefault(true);
            extractor.setMasterByDefault(true);

            assertNotNull(extractor.getText());
        }
    }
}
