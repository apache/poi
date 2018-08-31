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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertContainsIgnoreCase;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFObjectShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Tests that the extractor correctly gets the text out of our sample file
 */
public final class TestExtractor {
    /**
     * Extractor primed on the 2 page basic test data
     */
    private static final String expectText = "This is a test title\nThis is a test subtitle\nThis is on page 1\nThis is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n";

    /**
     * Extractor primed on the 1 page but text-box'd test data
     */
    private static final String expectText2 = "Hello, World!!!\nI am just a poor boy\nThis is Times New Roman\nPlain Text \n";

    /**
     * Where our embeded files live
     */
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    @SuppressWarnings("unchecked")
    private SlideShowExtractor<?,?> openExtractor(String fileName) throws IOException {
        try (InputStream is = slTests.openResourceAsStream(fileName)) {
            return new SlideShowExtractor(SlideShowFactory.create(is));
        }
    }
    
    @Test
    public void testReadSheetText() throws IOException {
        // Basic 2 page example
        try (SlideShowExtractor ppe = openExtractor("basic_test_ppt_file.ppt")) {
            assertEquals(expectText, ppe.getText());
        }

        // 1 page example with text boxes
        try (SlideShowExtractor ppe = openExtractor("with_textbox.ppt")) {
            assertEquals(expectText2, ppe.getText());
        }
    }

    @Test
    public void testReadNoteText() throws IOException {
        // Basic 2 page example
        try (SlideShowExtractor ppe = openExtractor("basic_test_ppt_file.ppt")) {
            ppe.setNotesByDefault(true);
            ppe.setSlidesByDefault(false);
            ppe.setMasterByDefault(false);
            String notesText = ppe.getText();
            String expText = "\nThese are the notes for page 1\n\nThese are the notes on page two, again lacking formatting\n";
            assertEquals(expText, notesText);
        }

        // Other one doesn't have notes
        try (SlideShowExtractor ppe = openExtractor("with_textbox.ppt")) {
            ppe.setNotesByDefault(true);
            ppe.setSlidesByDefault(false);
            ppe.setMasterByDefault(false);
            String notesText = ppe.getText();
            String expText = "";
            assertEquals(expText, notesText);
        }
    }

    @Test
    public void testReadBoth() throws IOException {
        String[] slText = new String[]{
                "This is a test title\nThis is a test subtitle\nThis is on page 1\n",
                "This is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n"
        };
        String[] ntText = new String[]{
                "\nThese are the notes for page 1\n",
                "\nThese are the notes on page two, again lacking formatting\n"
        };

        try (SlideShowExtractor ppe = openExtractor("basic_test_ppt_file.ppt")) {
            ppe.setSlidesByDefault(true);
            ppe.setNotesByDefault(false);
            assertEquals(slText[0] + slText[1], ppe.getText());

            ppe.setSlidesByDefault(false);
            ppe.setNotesByDefault(true);
            assertEquals(ntText[0] + ntText[1], ppe.getText());

            ppe.setSlidesByDefault(true);
            ppe.setNotesByDefault(true);
            assertEquals(slText[0] + ntText[0] + slText[1] + ntText[1], ppe.getText());
        }
    }

    /**
     * Test that when presented with a PPT file missing the odd
     * core record, we can still get the rest of the text out
     */
    @Test
    public void testMissingCoreRecords() throws IOException {
        try (SlideShowExtractor<?,?> ppe = openExtractor("missing_core_records.ppt")) {
            ppe.setSlidesByDefault(true);
            ppe.setNotesByDefault(false);
            String text = ppe.getText();
            ppe.setSlidesByDefault(false);
            ppe.setNotesByDefault(true);
            String nText = ppe.getText();

            assertNotNull(text);
            assertNotNull(nText);

            // Notes record were corrupt, so don't expect any
            assertEquals(nText.length(), 0);

            // Slide records were fine
            assertContains(text, "Using Disease Surveillance and Response");
        }
    }

    @Test
    public void testExtractFromEmbeded() throws IOException {
        try (final InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("excel_with_embeded.xls");
            final POIFSFileSystem fs = new POIFSFileSystem(is)) {
            final DirectoryNode root = fs.getRoot();

            final String[] TEST_SET = {
                "MBD0000A3B6", "Sample PowerPoint file\nThis is the 1st file\nNot much too it\n",
                "MBD0000A3B3", "Sample PowerPoint file\nThis is the 2nd file\nNot much too it either\n"
            };

            for (int i=0; i<TEST_SET.length; i+=2) {
                DirectoryNode dir = (DirectoryNode)root.getEntry(TEST_SET[i]);
                assertTrue(dir.hasEntry(HSLFSlideShow.POWERPOINT_DOCUMENT));

                try (final SlideShow<?,?> ppt = SlideShowFactory.create(dir);
                     final SlideShowExtractor<?,?> ppe = new SlideShowExtractor<>(ppt)) {
                    assertEquals(TEST_SET[i+1], ppe.getText());
                }
            }
        }
    }

    /**
     * A powerpoint file with embeded powerpoint files
     */
    @Test
    public void testExtractFromOwnEmbeded() throws IOException {
        try (SlideShowExtractor<?,?> ppe = openExtractor("ppt_with_embeded.ppt")) {
            List<? extends ObjectShape> shapes = ppe.getOLEShapes();
            assertEquals("Expected 6 ole shapes", 6, shapes.size());
            int num_ppt = 0, num_doc = 0, num_xls = 0;
            for (ObjectShape ole : shapes) {
                String name = ((HSLFObjectShape)ole).getInstanceName();
                InputStream data = ole.getObjectData().getInputStream();
                if ("Worksheet".equals(name)) {
                    HSSFWorkbook wb = new HSSFWorkbook(data);
                    num_xls++;
                    wb.close();
                } else if ("Document".equals(name)) {
                    HWPFDocument doc = new HWPFDocument(data);
                    num_doc++;
                    doc.close();
                } else if ("Presentation".equals(name)) {
                    num_ppt++;
                    HSLFSlideShow ppt = new HSLFSlideShow(data);
                    ppt.close();
                }
                data.close();
            }
            assertEquals("Expected 2 embedded Word Documents", 2, num_doc);
            assertEquals("Expected 2 embedded Excel Spreadsheets", 2, num_xls);
            assertEquals("Expected 2 embedded PowerPoint Presentations", 2, num_ppt);
        }
    }

    /**
     * A powerpoint file with embeded powerpoint files
     */
    @Test
    public void test52991() throws IOException {
        try (SlideShowExtractor<?,?> ppe = openExtractor("badzip.ppt")) {
            for (ObjectShape shape : ppe.getOLEShapes()) {
                IOUtils.copy(shape.getObjectData().getInputStream(), new ByteArrayOutputStream());
            }
        }
    }

    /**
     * From bug #45543
     */
    @Test
    public void testWithComments() throws IOException {
        try (final SlideShowExtractor ppe = openExtractor("WithComments.ppt")) {
            String text = ppe.getText();
            assertFalse("Comments not in by default", text.contains("This is a test comment"));

            ppe.setCommentsByDefault(true);

            text = ppe.getText();
            assertContains(text, "This is a test comment");
        }


        // And another file
        try (SlideShowExtractor ppe = openExtractor("45543.ppt")) {
            String text = ppe.getText();
            assertFalse("Comments not in by default", text.contains("testdoc"));

            ppe.setCommentsByDefault(true);

            text = ppe.getText();
            assertContains(text, "testdoc");
        }
    }

    /**
     * From bug #45537
     */
    @Test
    public void testHeaderFooter() throws IOException {
        // With a header on the notes
        try (InputStream is = slTests.openResourceAsStream("45537_Header.ppt");
            HSLFSlideShow ppt = new HSLFSlideShow(is)) {

            assertNotNull(ppt.getNotesHeadersFooters());
            assertEquals("testdoc test phrase", ppt.getNotesHeadersFooters().getHeaderText());

            testHeaderFooterInner(ppt);
        }

        // And with a footer, also on notes
        try (final InputStream is = slTests.openResourceAsStream("45537_Footer.ppt");
            final HSLFSlideShow ppt = new HSLFSlideShow(is)) {
            assertNotNull(ppt.getNotesHeadersFooters());
            assertEquals("testdoc test phrase", ppt.getNotesHeadersFooters().getFooterText());

            testHeaderFooterInner(ppt);
        }
    }

    private void testHeaderFooterInner(final HSLFSlideShow ppt) throws IOException {
        try (final SlideShowExtractor<?,?> ppe = new SlideShowExtractor<>(ppt)) {
            String text = ppe.getText();
            assertFalse("Header shouldn't be there by default\n" + text, text.contains("testdoc"));
            assertFalse("Header shouldn't be there by default\n" + text, text.contains("test phrase"));

            ppe.setNotesByDefault(true);
            text = ppe.getText();
            assertContains(text, "testdoc");
            assertContains(text, "test phrase");
        }
    }

    @Test
    public void testSlideMasterText() throws IOException {
        String masterTitleText = "This is the Master Title";
        String masterRandomText = "This text comes from the Master Slide";
        String masterFooterText = "Footer from the master slide";
        try (final SlideShowExtractor ppe = openExtractor("WithMaster.ppt")) {
            ppe.setMasterByDefault(true);

            String text = ppe.getText();
            assertContains(text, masterRandomText);
            assertNotContained(text, masterTitleText);

            //make sure that the footer only appears once
            int masterFooters = 0;
            int offset = text.indexOf(masterFooterText);
            while (offset > -1) {
                masterFooters++;
                offset = text.indexOf(masterFooterText, offset+1);
            }
            assertEquals(1, masterFooters);
        }
    }

    @Test
    public void testSlideMasterText2() throws IOException {
        try (final SlideShowExtractor ppe = openExtractor("bug62591.ppt")) {
            ppe.setMasterByDefault(true);
            String text = ppe.getText();
            assertNotContained(text, "Titelmasterformat");
        }
    }

    @Test
    public void testMasterText() throws IOException {
        try (final SlideShowExtractor ppe = openExtractor("master_text.ppt")) {
            // Initially not there
            String text = ppe.getText();
            assertFalse(text.contains("Text that I added to the master slide"));

            // Enable, shows up
            ppe.setMasterByDefault(true);
            text = ppe.getText();
            assertContains(text, "Text that I added to the master slide");

            // Make sure placeholder text does not come out
            assertNotContained(text, "Click to edit Master");
        }

        // Now with another file only containing master text
        // Will always show up
        try (final SlideShowExtractor ppe = openExtractor("WithMaster.ppt")) {
            String masterText = "Footer from the master slide";

            String text = ppe.getText();
            assertContainsIgnoreCase(text, "master");
            assertContains(text, masterText);
        }
    }

    /**
     * Bug #54880 Chinese text not extracted properly
     */
    @Test
    public void testChineseText() throws IOException {
        try (final SlideShowExtractor ppe = openExtractor("54880_chinese.ppt")) {
            String text = ppe.getText();

            // Check for the english text line
            assertContains(text, "Single byte");

            // Check for the english text in the mixed line
            assertContains(text, "Mix");

            // Check for the chinese text in the mixed line
            assertContains(text, "\u8868");

            // Check for the chinese only text line
            assertContains(text, "\uff8a\uff9d\uff76\uff78");
        }
    }

    /**
     * Tests that we can work with both {@link POIFSFileSystem}
     * and {@link POIFSFileSystem}
     */
    @SuppressWarnings("resource")
    @Test
    public void testDifferentPOIFS() throws IOException {
        // Open the two filesystems
        File pptFile = slTests.getFile("basic_test_ppt_file.ppt");
        try (final POIFSFileSystem npoifs = new POIFSFileSystem(pptFile, true)) {
            // Open directly
            try (SlideShow<?,?> ppt = SlideShowFactory.create(npoifs.getRoot());
                SlideShowExtractor<?,?> extractor = new SlideShowExtractor<>(ppt)) {
                assertEquals(expectText, extractor.getText());
            }
        }
    }

    @Test
    public void testTable() throws Exception {
        try (SlideShowExtractor ppe = openExtractor("54111.ppt")) {
            String text = ppe.getText();
            String target = "TH Cell 1\tTH Cell 2\tTH Cell 3\tTH Cell 4\n" +
                    "Row 1, Cell 1\tRow 1, Cell 2\tRow 1, Cell 3\tRow 1, Cell 4\n" +
                    "Row 2, Cell 1\tRow 2, Cell 2\tRow 2, Cell 3\tRow 2, Cell 4\n" +
                    "Row 3, Cell 1\tRow 3, Cell 2\tRow 3, Cell 3\tRow 3, Cell 4\n" +
                    "Row 4, Cell 1\tRow 4, Cell 2\tRow 4, Cell 3\tRow 4, Cell 4\n" +
                    "Row 5, Cell 1\tRow 5, Cell 2\tRow 5, Cell 3\tRow 5, Cell 4\n";
            assertContains(text, target);
        }

        try (SlideShowExtractor ppe = openExtractor("54722.ppt")) {
            String text = ppe.getText();

            String target = "this\tText\tis\twithin\ta\n" +
                    "table\t1\t2\t3\t4";
            assertContains(text, target);
        }
    }

    // bug 60003
    @Test
    public void testExtractMasterSlideFooterText() throws Exception {
        try (SlideShowExtractor ppe = openExtractor("60003.ppt")) {
            ppe.setMasterByDefault(true);

            String text = ppe.getText();
            assertContains(text, "Prague");
        }
    }

    @Test
    public void testExtractGroupedShapeText() throws Exception {
        try (final SlideShowExtractor ppe = openExtractor("bug62092.ppt")) {
            final String text = ppe.getText();

            //this tests that we're ignoring text shapes at depth=0
            //i.e. POI has already included them in the slide's getTextParagraphs()
            assertContains(text, "Text box1");
            assertEquals(1, countMatches(text,"Text box1"));


            //the WordArt and text box count tests will fail
            //if this content is available via getTextParagraphs() of the slide in POI
            //i.e. when POI is fixed, these tests will fail, and
            //we'll have to remove the workaround in HSLFExtractor's extractGroupText(...)
            assertEquals(1, countMatches(text,"WordArt1"));
            assertEquals(1, countMatches(text,"WordArt2"));
            assertEquals(1, countMatches(text,"Ungrouped text box"));//should only be 1
            assertContains(text, "Text box2");
            assertContains(text, "Text box3");
            assertContains(text, "Text box4");
            assertContains(text, "Text box5");

            //see below -- need to extract hyperlinks
            assertContains(text, "tika");
            assertContains(text, "MyTitle");

        }
    }

    private static int countMatches(final String base, final String find) {
        return base.split(find).length-1;
    }
}
