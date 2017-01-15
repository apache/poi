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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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

//    @Before
//    public void setUp() throws Exception {
//        ppe = new PowerPointExtractor(slTests.getFile("basic_test_ppt_file.ppt").getCanonicalPath());
//        ppe2 = new PowerPointExtractor(slTests.getFile("with_textbox.ppt").getCanonicalPath());
//    }

//    @After
//    public void closeResources() throws Exception {
//        ppe2.close();
//        ppe.close();
//    }

    private PowerPointExtractor openExtractor(String fileName) throws IOException {
        InputStream is = slTests.openResourceAsStream(fileName);
        try {
            return new PowerPointExtractor(is);
        } finally {
            is.close();
        }
    }
    
    @Test
    public void testReadSheetText() throws IOException {
        // Basic 2 page example
        PowerPointExtractor ppe = openExtractor("basic_test_ppt_file.ppt");
        ensureTwoStringsTheSame(expectText, ppe.getText());
        ppe.close();

        // 1 page example with text boxes
        PowerPointExtractor ppe2 = openExtractor("with_textbox.ppt");
        ensureTwoStringsTheSame(expectText2, ppe2.getText());
        ppe2.close();
    }

    @Test
    public void testReadNoteText() throws IOException {
        // Basic 2 page example
        PowerPointExtractor ppe = openExtractor("basic_test_ppt_file.ppt");
        String notesText = ppe.getNotes();
        String expText = "These are the notes for page 1\nThese are the notes on page two, again lacking formatting\n";
        ensureTwoStringsTheSame(expText, notesText);
        ppe.close();

        // Other one doesn't have notes
        PowerPointExtractor ppe2 = openExtractor("with_textbox.ppt");
        notesText = ppe2.getNotes();
        expText = "";
        ensureTwoStringsTheSame(expText, notesText);
        ppe2.close();
    }

    @Test
    public void testReadBoth() throws IOException {
        String[] slText = new String[]{
                "This is a test title\nThis is a test subtitle\nThis is on page 1\n",
                "This is the title on page 2\nThis is page two\nIt has several blocks of text\nNone of them have formatting\n"
        };
        String[] ntText = new String[]{
                "These are the notes for page 1\n",
                "These are the notes on page two, again lacking formatting\n"
        };

        PowerPointExtractor ppe = openExtractor("basic_test_ppt_file.ppt");
        ppe.setSlidesByDefault(true);
        ppe.setNotesByDefault(false);
        assertEquals(slText[0] + slText[1], ppe.getText());

        ppe.setSlidesByDefault(false);
        ppe.setNotesByDefault(true);
        assertEquals(ntText[0] + ntText[1], ppe.getText());

        ppe.setSlidesByDefault(true);
        ppe.setNotesByDefault(true);
        assertEquals(slText[0] + slText[1] + "\n" + ntText[0] + ntText[1], ppe.getText());
        ppe.close();
    }

    /**
     * Test that when presented with a PPT file missing the odd
     * core record, we can still get the rest of the text out
     *
     * @throws Exception
     */
    @Test
    public void testMissingCoreRecords() throws IOException {
        PowerPointExtractor ppe = openExtractor("missing_core_records.ppt");

        String text = ppe.getText(true, false);
        String nText = ppe.getNotes();

        assertNotNull(text);
        assertNotNull(nText);

        // Notes record were corrupt, so don't expect any
        assertEquals(nText.length(), 0);

        // Slide records were fine
        assertTrue(text.startsWith("Using Disease Surveillance and Response"));
        
        ppe.close();
    }

    private void ensureTwoStringsTheSame(String exp, String act) {
        assertEquals(exp.length(), act.length());
        char[] expC = exp.toCharArray();
        char[] actC = act.toCharArray();
        for (int i = 0; i < expC.length; i++) {
            assertEquals("Char " + i, expC[i], actC[i]);
        }
        assertEquals(exp, act);
    }

    @Test
    public void testExtractFromEmbeded() throws IOException {
        InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("excel_with_embeded.xls");
        POIFSFileSystem fs = new POIFSFileSystem(is);
        DirectoryNode root = fs.getRoot();
        PowerPointExtractor ppe1 = assertExtractFromEmbedded(root, "MBD0000A3B6", "Sample PowerPoint file\nThis is the 1st file\nNot much too it\n");
        PowerPointExtractor ppe2 = assertExtractFromEmbedded(root, "MBD0000A3B3", "Sample PowerPoint file\nThis is the 2nd file\nNot much too it either\n");
        ppe2.close();
        ppe1.close();
        fs.close();
    }
    
    private PowerPointExtractor assertExtractFromEmbedded(DirectoryNode root, String entryName, String expected)
    throws IOException {
        DirectoryNode dir = (DirectoryNode)root.getEntry(entryName);
        assertTrue(dir.hasEntry("PowerPoint Document"));

        // Check the first file
        HSLFSlideShowImpl ppt = new HSLFSlideShowImpl(dir);
        PowerPointExtractor ppe = new PowerPointExtractor(ppt);
        assertEquals(expected, ppe.getText(true, false));
        return ppe;
    }

    /**
     * A powerpoint file with embeded powerpoint files
     */
    @Test
    public void testExtractFromOwnEmbeded() throws IOException {
        PowerPointExtractor ppe = openExtractor("ppt_with_embeded.ppt");
        List<OLEShape> shapes = ppe.getOLEShapes();
        assertEquals("Expected 6 ole shapes", 6, shapes.size());
        int num_ppt = 0, num_doc = 0, num_xls = 0;
        for (OLEShape ole : shapes) {
            String name = ole.getInstanceName();
            InputStream data = ole.getObjectData().getData();
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
        ppe.close();
    }

    /**
     * A powerpoint file with embeded powerpoint files
     */
    @Test
    public void test52991() throws IOException {
        PowerPointExtractor ppe = openExtractor("badzip.ppt");
        for (OLEShape shape : ppe.getOLEShapes()) {
            IOUtils.copy(shape.getObjectData().getData(), new ByteArrayOutputStream());
        }
        ppe.close();
    }

    /**
     * From bug #45543
     */
    @Test
    public void testWithComments() throws IOException {
        PowerPointExtractor ppe1 = openExtractor("WithComments.ppt");
        String text = ppe1.getText();
        assertFalse("Comments not in by default", text.contains("This is a test comment"));

        ppe1.setCommentsByDefault(true);

        text = ppe1.getText();
        assertContains(text, "This is a test comment");
        ppe1.close();


        // And another file
        PowerPointExtractor ppe2 = openExtractor("45543.ppt");
        text = ppe2.getText();
        assertFalse("Comments not in by default", text.contains("testdoc"));

        ppe2.setCommentsByDefault(true);

        text = ppe2.getText();
        assertContains(text, "testdoc");
        ppe2.close();
    }

    /**
     * From bug #45537
     */
    @Test
    public void testHeaderFooter() throws IOException {
        String text;

        // With a header on the notes
        InputStream is1 = slTests.openResourceAsStream("45537_Header.ppt");
        HSLFSlideShow ppt1 = new HSLFSlideShow(is1);
        is1.close();
        assertNotNull(ppt1.getNotesHeadersFooters());
        assertEquals("testdoc test phrase", ppt1.getNotesHeadersFooters().getHeaderText());

        PowerPointExtractor ppe1 = new PowerPointExtractor(ppt1.getSlideShowImpl());

        text = ppe1.getText();
        assertFalse("Header shouldn't be there by default\n" + text, text.contains("testdoc"));
        assertFalse("Header shouldn't be there by default\n" + text, text.contains("test phrase"));

        ppe1.setNotesByDefault(true);
        text = ppe1.getText();
        assertContains(text, "testdoc");
        assertContains(text, "test phrase");
        ppe1.close();
        ppt1.close();

        // And with a footer, also on notes
        InputStream is2 = slTests.openResourceAsStream("45537_Footer.ppt");
        HSLFSlideShow ppt2 = new HSLFSlideShow(is2);
        is2.close();
        
        assertNotNull(ppt2.getNotesHeadersFooters());
        assertEquals("testdoc test phrase", ppt2.getNotesHeadersFooters().getFooterText());
        ppt2.close();

        PowerPointExtractor ppe2 = openExtractor("45537_Footer.ppt");

        text = ppe2.getText();
        assertFalse("Header shouldn't be there by default\n" + text, text.contains("testdoc"));
        assertFalse("Header shouldn't be there by default\n" + text, text.contains("test phrase"));

        ppe2.setNotesByDefault(true);
        text = ppe2.getText();
        assertContains(text, "testdoc");
        assertContains(text, "test phrase");
        ppe2.close();
    }

    @SuppressWarnings("unused")
    @Test
    public void testSlideMasterText() throws IOException {
        String masterTitleText = "This is the Master Title";
        String masterRandomText = "This text comes from the Master Slide";
        String masterFooterText = "Footer from the master slide";
        PowerPointExtractor ppe = openExtractor("WithMaster.ppt");
        ppe.setMasterByDefault(true);

        String text = ppe.getText();
        assertContains(text, masterRandomText);
        assertContains(text, masterFooterText);
        ppe.close();
    }

    @Test
    public void testMasterText() throws IOException {
        PowerPointExtractor ppe1 = openExtractor("master_text.ppt");

        // Initially not there
        String text = ppe1.getText();
        assertFalse(text.contains("Text that I added to the master slide"));

        // Enable, shows up
        ppe1.setMasterByDefault(true);
        text = ppe1.getText();
        assertTrue(text.contains("Text that I added to the master slide"));

        // Make sure placeholder text does not come out
        assertFalse(text.contains("Click to edit Master"));
        ppe1.close();

        // Now with another file only containing master text
        // Will always show up
        PowerPointExtractor ppe2 = openExtractor("WithMaster.ppt");
        String masterText = "Footer from the master slide";

        text = ppe2.getText();
        assertContainsIgnoreCase(text, "master");
        assertContains(text, masterText);
        ppe2.close();
    }

    /**
     * Bug #54880 Chinese text not extracted properly
     */
    @Test
    public void testChineseText() throws IOException {
        PowerPointExtractor ppe = openExtractor("54880_chinese.ppt");

        String text = ppe.getText();

        // Check for the english text line
        assertContains(text, "Single byte");

        // Check for the english text in the mixed line
        assertContains(text, "Mix");

        // Check for the chinese text in the mixed line
        assertContains(text, "\u8868");

        // Check for the chinese only text line
        assertContains(text, "\uff8a\uff9d\uff76\uff78");
        ppe.close();
    }

    /**
     * Tests that we can work with both {@link POIFSFileSystem}
     * and {@link NPOIFSFileSystem}
     */
    @SuppressWarnings("resource")
    @Test
    public void testDifferentPOIFS() throws IOException {
        // Open the two filesystems
        File pptFile = slTests.getFile("basic_test_ppt_file.ppt");
        InputStream is1 = new FileInputStream(pptFile);
        OPOIFSFileSystem opoifs = new OPOIFSFileSystem(is1);
        is1.close();
        NPOIFSFileSystem npoifs = new NPOIFSFileSystem(pptFile);
        
        DirectoryNode[] files = { opoifs.getRoot(), npoifs.getRoot() };

        // Open directly
        for (DirectoryNode dir : files) {
            PowerPointExtractor extractor = new PowerPointExtractor(dir);
            assertEquals(expectText, extractor.getText());
        }

        // Open via a HSLFSlideShow
        for (DirectoryNode dir : files) {
            HSLFSlideShowImpl slideshow = new HSLFSlideShowImpl(dir);
            PowerPointExtractor extractor = new PowerPointExtractor(slideshow);
            assertEquals(expectText, extractor.getText());
            extractor.close();
            slideshow.close();
        }

        npoifs.close();
    }

    @Test
    public void testTable() throws Exception {
        PowerPointExtractor ppe1 = openExtractor("54111.ppt");
        String text1 = ppe1.getText();
        String target1 = "TH Cell 1\tTH Cell 2\tTH Cell 3\tTH Cell 4\n"+
                         "Row 1, Cell 1\tRow 1, Cell 2\tRow 1, Cell 3\tRow 1, Cell 4\n"+   
                         "Row 2, Cell 1\tRow 2, Cell 2\tRow 2, Cell 3\tRow 2, Cell 4\n"+
                         "Row 3, Cell 1\tRow 3, Cell 2\tRow 3, Cell 3\tRow 3, Cell 4\n"+
                         "Row 4, Cell 1\tRow 4, Cell 2\tRow 4, Cell 3\tRow 4, Cell 4\n"+ 
                         "Row 5, Cell 1\tRow 5, Cell 2\tRow 5, Cell 3\tRow 5, Cell 4\n";
        assertTrue(text1.contains(target1));
        ppe1.close();

        PowerPointExtractor ppe2 = openExtractor("54722.ppt");
        String text2 = ppe2.getText();

        String target2 = "this\tText\tis\twithin\ta\n" +
                "table\t1\t2\t3\t4";
        assertTrue(text2.contains(target2));
        ppe2.close();
    }

    // bug 60003
    @Test
    public void testExtractMasterSlideFooterText() throws Exception {
        PowerPointExtractor ppe = openExtractor("60003.ppt");
        ppe.setMasterByDefault(true);

        String text = ppe.getText();
        assertContains(text, "Prague");
        ppe.close();
    }
}
