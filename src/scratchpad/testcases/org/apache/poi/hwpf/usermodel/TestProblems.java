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

package org.apache.poi.hwpf.usermodel;

import java.io.InputStream;
import java.util.List;

import junit.framework.AssertionFailedError;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.util.IOUtils;

/**
 * Test various problem documents
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestProblems extends HWPFTestCase {

   /**
    * ListEntry passed no ListTable
    */
   public void testListEntryNoListTable() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("ListEntryNoListTable.doc");

      Range r = doc.getRange();
      StyleSheet styleSheet = doc.getStyleSheet();
      for (int x = 0; x < r.numSections(); x++) {
         Section s = r.getSection(x);
         for (int y = 0; y < s.numParagraphs(); y++) {
            Paragraph paragraph = s.getParagraph(y);
            // System.out.println(paragraph.getCharacterRun(0).text());
         }
      }
   }

   /**
    * AIOOB for TableSprmUncompressor.unCompressTAPOperation
    */
   public void testSprmAIOOB() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("AIOOB-Tap.doc");

      Range r = doc.getRange();
      StyleSheet styleSheet = doc.getStyleSheet();
      for (int x = 0; x < r.numSections(); x++) {
         Section s = r.getSection(x);
         for (int y = 0; y < s.numParagraphs(); y++) {
            Paragraph paragraph = s.getParagraph(y);
            // System.out.println(paragraph.getCharacterRun(0).text());
         }
      }
   }

   /**
    * Test for TableCell not skipping the last paragraph. Bugs #45062 and
    * #44292
    */
   public void testTableCellLastParagraph() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug44292.doc");
      Range r = doc.getRange();
      assertEquals(6, r.numParagraphs());
      assertEquals(0, r.getStartOffset());
      assertEquals(87, r.getEndOffset());

      // Paragraph with table
      Paragraph p = r.getParagraph(0);
      assertEquals(0, p.getStartOffset());
      assertEquals(20, p.getEndOffset());

      // Check a few bits of the table directly
      assertEquals("One paragraph is ok\7", r.getParagraph(0).text());
      assertEquals("First para is ok\r", r.getParagraph(1).text());
      assertEquals("Second paragraph is skipped\7", r.getParagraph(2).text());
      assertEquals("One paragraph is ok\7", r.getParagraph(3).text());
      assertEquals("\7", r.getParagraph(4).text());
      assertEquals("\r", r.getParagraph(5).text());
      for(int i=0; i<=5; i++) {
         assertFalse(r.getParagraph(i).usesUnicode());
      }


      // Get the table
      Table t = r.getTable(p);

      // get the only row
      assertEquals(1, t.numRows());
      TableRow row = t.getRow(0);

      // sanity check our row
      assertEquals(5, row.numParagraphs());
      assertEquals(0, row._parStart);
      assertEquals(5, row._parEnd);
      assertEquals(0, row.getStartOffset());
      assertEquals(86, row.getEndOffset());


      // get the first cell
      TableCell cell = row.getCell(0);
      // First cell should have one paragraph
      assertEquals(1, cell.numParagraphs());
      assertEquals("One paragraph is ok\7", cell.getParagraph(0).text());
      assertEquals(0, cell._parStart);
      assertEquals(1, cell._parEnd);
      assertEquals(0, cell.getStartOffset());
      assertEquals(20, cell.getEndOffset());


      // get the second
      cell = row.getCell(1);
      // Second cell should be detected as having two paragraphs
      assertEquals(2, cell.numParagraphs());
      assertEquals("First para is ok\r", cell.getParagraph(0).text());
      assertEquals("Second paragraph is skipped\7", cell.getParagraph(1).text());
      assertEquals(1, cell._parStart);
      assertEquals(3, cell._parEnd);
      assertEquals(20, cell.getStartOffset());
      assertEquals(65, cell.getEndOffset());


      // get the last cell
      cell = row.getCell(2);
      // Last cell should have one paragraph
      assertEquals(1, cell.numParagraphs());
      assertEquals("One paragraph is ok\7", cell.getParagraph(0).text());
      assertEquals(3, cell._parStart);
      assertEquals(4, cell._parEnd);
      assertEquals(65, cell.getStartOffset());
      assertEquals(85, cell.getEndOffset());
   }

   public void testRangeDelete() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug28627.doc");

      Range range = doc.getRange();
      int numParagraphs = range.numParagraphs();

      int totalLength = 0, deletedLength = 0;

      for (int i = 0; i < numParagraphs; i++) {
         Paragraph para = range.getParagraph(i);
         String text = para.text();

         totalLength += text.length();
         if (text.indexOf("{delete me}") > -1) {
            para.delete();
            deletedLength = text.length();
         }
      }

      // check the text length after deletion
      int newLength = 0;
      range = doc.getRange();
      numParagraphs = range.numParagraphs();

      for (int i = 0; i < numParagraphs; i++) {
         Paragraph para = range.getParagraph(i);
         String text = para.text();

         newLength += text.length();
      }

      assertEquals(newLength, totalLength - deletedLength);
   }

   /**
    * With an encrypted file, we should give a suitable exception, and not OOM
    */
   public void testEncryptedFile() {
      try {
         HWPFTestDataSamples.openSampleFile("PasswordProtected.doc");
         fail();
      } catch (EncryptedDocumentException e) {
         // Good
      }
   }

   public void testWriteProperties() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");
      assertEquals("Nick Burch", doc.getSummaryInformation().getAuthor());

      // Write and read
      HWPFDocument doc2 = writeOutAndRead(doc);
      assertEquals("Nick Burch", doc2.getSummaryInformation().getAuthor());
   }

   /**
    * Test for reading paragraphs from Range after replacing some 
    * text in this Range.
    * Bug #45269
    */
   public void testReadParagraphsAfterReplaceText()throws Exception{
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug45269.doc");
      Range range = doc.getRange();

      String toFind = "campo1";
      String longer = " foi porraaaaa ";
      String shorter = " foi ";

      //check replace with longer text
      for (int x = 0; x < range.numParagraphs(); x++) {
         Paragraph para = range.getParagraph(x);
         int offset = para.text().indexOf(toFind);
         if (offset >= 0) {
            para.replaceText(toFind, longer, offset);
            assertEquals(offset, para.text().indexOf(longer));
         }
      }

      doc = HWPFTestDataSamples.openSampleFile("Bug45269.doc");
      range = doc.getRange();

      //check replace with shorter text
      for (int x = 0; x < range.numParagraphs(); x++) {
         Paragraph para = range.getParagraph(x);
         int offset = para.text().indexOf(toFind);
         if (offset >= 0) {
            para.replaceText(toFind, shorter, offset);
            assertEquals(offset, para.text().indexOf(shorter));
         }
      }
   }
   
   /**
    * Bug #49936 - Problems with reading the header out of
    *  the Header Stories
    */
   public void testProblemHeaderStories49936() throws Exception {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("HeaderFooterProblematic.doc");
      HeaderStories hs = new HeaderStories(doc);
      
      assertEquals("", hs.getFirstHeader());
      assertEquals("\r", hs.getEvenHeader());
      assertEquals("", hs.getOddHeader());
      
      assertEquals("", hs.getFirstFooter());
      assertEquals("", hs.getEvenFooter());
      assertEquals("", hs.getOddFooter());
      
      WordExtractor ext = new WordExtractor(doc);
      assertEquals("\n", ext.getHeaderText());
      assertEquals("", ext.getFooterText());
   }
   
   /**
    * Bug #45877 - problematic PAPX with no parent set
    */
   public void testParagraphPAPXNoParent45877() throws Exception {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug45877.doc");
      assertEquals(17, doc.getRange().numParagraphs());
      
      assertEquals("First paragraph\r", doc.getRange().getParagraph(0).text());
      assertEquals("After Crashing Part\r", doc.getRange().getParagraph(13).text());
   }

   /**
    * Bug #48245 - don't include the text from the
    *  next cell in the current one
    */
   public void testTableIterator() throws Exception {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("simple-table2.doc");
      Range r = doc.getRange();

      // Check the text is as we'd expect
      assertEquals(13, r.numParagraphs());
      assertEquals("Row 1/Cell 1\u0007", r.getParagraph(0).text());
      assertEquals("Row 1/Cell 2\u0007", r.getParagraph(1).text());
      assertEquals("Row 1/Cell 3\u0007", r.getParagraph(2).text());
      assertEquals("\u0007", r.getParagraph(3).text());
      assertEquals("Row 2/Cell 1\u0007", r.getParagraph(4).text());
      assertEquals("Row 2/Cell 2\u0007", r.getParagraph(5).text());
      assertEquals("Row 2/Cell 3\u0007", r.getParagraph(6).text());
      assertEquals("\u0007", r.getParagraph(7).text());
      assertEquals("Row 3/Cell 1\u0007", r.getParagraph(8).text());
      assertEquals("Row 3/Cell 2\u0007", r.getParagraph(9).text());
      assertEquals("Row 3/Cell 3\u0007", r.getParagraph(10).text());
      assertEquals("\u0007", r.getParagraph(11).text());
      assertEquals("\r", r.getParagraph(12).text());
      for(int i=0; i<=12; i++) {
         assertFalse(r.getParagraph(i).usesUnicode());
      }

      Paragraph p;

      // Take a look in detail at the first couple of
      //  paragraphs
      p = r.getParagraph(0);
      assertEquals(1,  p.numParagraphs());
      assertEquals(0,  p.getStartOffset());
      assertEquals(13, p.getEndOffset());
      assertEquals(0,  p._parStart);
      assertEquals(1,  p._parEnd);

      p = r.getParagraph(1);
      assertEquals(1,  p.numParagraphs());
      assertEquals(13, p.getStartOffset());
      assertEquals(26, p.getEndOffset());
      assertEquals(1,  p._parStart);
      assertEquals(2,  p._parEnd);

      p = r.getParagraph(2);
      assertEquals(1,  p.numParagraphs());
      assertEquals(26, p.getStartOffset());
      assertEquals(39, p.getEndOffset());
      assertEquals(2,  p._parStart);
      assertEquals(3,  p._parEnd);


      // Now look at the table
      Table table = r.getTable(r.getParagraph(0));
      assertEquals(3, table.numRows());

      TableRow row;
      TableCell cell;


      row = table.getRow(0);
      assertEquals(0, row._parStart);
      assertEquals(4, row._parEnd);

      cell = row.getCell(0);
      assertEquals(1, cell.numParagraphs());
      assertEquals(0, cell._parStart);
      assertEquals(1, cell._parEnd);
      assertEquals(0, cell.getStartOffset());
      assertEquals(13, cell.getEndOffset());
      assertEquals("Row 1/Cell 1\u0007", cell.text());
      assertEquals("Row 1/Cell 1\u0007", cell.getParagraph(0).text());

      cell = row.getCell(1);
      assertEquals(1, cell.numParagraphs());
      assertEquals(1, cell._parStart);
      assertEquals(2, cell._parEnd);
      assertEquals(13, cell.getStartOffset());
      assertEquals(26, cell.getEndOffset());
      assertEquals("Row 1/Cell 2\u0007", cell.text());
      assertEquals("Row 1/Cell 2\u0007", cell.getParagraph(0).text());

      cell = row.getCell(2);
      assertEquals(1, cell.numParagraphs());
      assertEquals(2, cell._parStart);
      assertEquals(3, cell._parEnd);
      assertEquals(26, cell.getStartOffset());
      assertEquals(39, cell.getEndOffset());
      assertEquals("Row 1/Cell 3\u0007", cell.text());
      assertEquals("Row 1/Cell 3\u0007", cell.getParagraph(0).text());


      // Onto row #2
      row = table.getRow(1);
      assertEquals(4, row._parStart);
      assertEquals(8, row._parEnd);

      cell = row.getCell(0);
      assertEquals(1, cell.numParagraphs());
      assertEquals(4, cell._parStart);
      assertEquals(5, cell._parEnd);
      assertEquals(40, cell.getStartOffset());
      assertEquals(53, cell.getEndOffset());
      assertEquals("Row 2/Cell 1\u0007", cell.text());

      cell = row.getCell(1);
      assertEquals(1, cell.numParagraphs());
      assertEquals(5, cell._parStart);
      assertEquals(6, cell._parEnd);
      assertEquals(53, cell.getStartOffset());
      assertEquals(66, cell.getEndOffset());
      assertEquals("Row 2/Cell 2\u0007", cell.text());

      cell = row.getCell(2);
      assertEquals(1, cell.numParagraphs());
      assertEquals(6, cell._parStart);
      assertEquals(7, cell._parEnd);
      assertEquals(66, cell.getStartOffset());
      assertEquals(79, cell.getEndOffset());
      assertEquals("Row 2/Cell 3\u0007", cell.text());


      // Finally row 3
      row = table.getRow(2);
      assertEquals(8, row._parStart);
      assertEquals(12, row._parEnd);

      cell = row.getCell(0);
      assertEquals(1, cell.numParagraphs());
      assertEquals(8, cell._parStart);
      assertEquals(9, cell._parEnd);
      assertEquals(80, cell.getStartOffset());
      assertEquals(93, cell.getEndOffset());
      assertEquals("Row 3/Cell 1\u0007", cell.text());

      cell = row.getCell(1);
      assertEquals(1, cell.numParagraphs());
      assertEquals(9, cell._parStart);
      assertEquals(10, cell._parEnd);
      assertEquals(93, cell.getStartOffset());
      assertEquals(106, cell.getEndOffset());
      assertEquals("Row 3/Cell 2\u0007", cell.text());

      cell = row.getCell(2);
      assertEquals(1, cell.numParagraphs());
      assertEquals(10, cell._parStart);
      assertEquals(11, cell._parEnd);
      assertEquals(106, cell.getStartOffset());
      assertEquals(119, cell.getEndOffset());
      assertEquals("Row 3/Cell 3\u0007", cell.text());
   }

    static void fixed(String bugzillaId) {
        throw new Error("Bug " + bugzillaId + " seems to be fixed. " +
                "Please resolve the issue in Bugzilla and remove fail() from the test");
    }

    /**
     * Bug 33519 - HWPF fails to read a file
     */
    public void test33519() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug33519.doc");
        WordExtractor extractor = new WordExtractor(doc);
        String text = extractor.getText();
    }

    /**
     * Bug 34898 - WordExtractor doesn't read the whole string from the file
     */
    public void test34898() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug34898.doc");
        WordExtractor extractor = new WordExtractor(doc);
        assertEquals("\u30c7\u30a3\u30ec\u30af\u30c8\u30ea", extractor.getText().trim());
    }

    /**
     * [FAILING] Bug 44331 - Output is corrupted
     */
    public void test44431() {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile("Bug44431.doc");
        WordExtractor extractor1 = new WordExtractor(doc1);

        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack(doc1);
        WordExtractor extractor2 = new WordExtractor(doc2);
        try {
            assertEquals(extractor1.getFooterText(), extractor2.getFooterText());
            assertEquals(extractor1.getHeaderText(), extractor2.getHeaderText());
            assertEquals(extractor1.getParagraphText(), extractor2.getParagraphText());

            assertEquals(extractor1.getText(), extractor2.getText());

            fixed("44431");
        } catch (AssertionFailedError e) {
            // expected exception
        }
    }

    /**
     * [RESOLVED FIXED] Bug 46817 - Regression: Text from some table cells
     * missing
     */
    public void test46817()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug46817.doc" );
        WordExtractor extractor = new WordExtractor( doc );
        String text = extractor.getText().trim();

        assertTrue( text.contains( "Nazwa wykonawcy" ) );
        assertTrue( text.contains( "kujawsko-pomorskie" ) );
        assertTrue( text.contains( "ekomel@ekomel.com.pl" ) );
    }

    /**
     * Bug 46220 - images are not properly extracted
     */
    public void test46220() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug46220.doc");
        // reference checksums as in Bugzilla
        String[] md5 = {
                "851be142bce6d01848e730cb6903f39e",
                "7fc6d8fb58b09ababd036d10a0e8c039",
                "a7dc644c40bc2fbf17b2b62d07f99248",
                "72d07b8db5fad7099d90bc4c304b4666"
        };
        List<Picture> pics = doc.getPicturesTable().getAllPictures();
        assertEquals(4, pics.size());
        for (int i = 0; i < pics.size(); i++) {
            Picture pic = pics.get(i);
            byte[] data = pic.getRawContent();
            // use Apache Commons Codec utils to compute md5
            assertEquals(md5[i], DigestUtils.md5Hex(data));
        }
    }

    /**
     * Bug 45473 - HWPF cannot read file after save
     */
    public void test45473() {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile("Bug45473.doc");
        String text1 = new WordExtractor(doc1).getText().trim();

        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack(doc1);
        String text2 = new WordExtractor(doc2).getText().trim();

        // the text in the saved document has some differences in line separators but we tolerate that
        assertEquals(text1.replaceAll("\n", ""), text2.replaceAll("\n", ""));
    }

    /**
     * [RESOLVED FIXED] Bug 47287 - StringIndexOutOfBoundsException in
     * CharacterRun.replaceText()
     */
    public void test47287()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47287.doc" );
        String[] values = { "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7",
                "1-8", "1-9", "1-10", "1-11", "1-12", "1-13", "1-14", "1-15", };
        int usedVal = 0;
        String PLACEHOLDER = "\u2002\u2002\u2002\u2002\u2002";
        Range r = doc.getRange();
        for ( int x = 0; x < r.numSections(); x++ )
        {
            Section s = r.getSection( x );
            for ( int y = 0; y < s.numParagraphs(); y++ )
            {
                Paragraph p = s.getParagraph( y );

                for ( int z = 0; z < p.numCharacterRuns(); z++ )
                {
                    boolean isFound = false;

                    // character run
                    CharacterRun run = p.getCharacterRun( z );
                    // character run text
                    String text = run.text();
                    String oldText = text;
                    int c = text.indexOf( "FORMTEXT " );
                    if ( c < 0 )
                    {
                        int k = text.indexOf( PLACEHOLDER );
                        if ( k >= 0 )
                        {
                            text = text.substring( 0, k ) + values[usedVal]
                                    + text.substring( k + PLACEHOLDER.length() );
                            usedVal++;
                            isFound = true;
                        }
                    }
                    else
                    {
                        for ( ; c >= 0; c = text.indexOf( "FORMTEXT ", c
                                + "FORMTEXT ".length() ) )
                        {
                            int k = text.indexOf( PLACEHOLDER, c );
                            if ( k >= 0 )
                            {
                                text = text.substring( 0, k )
                                        + values[usedVal]
                                        + text.substring( k
                                                + PLACEHOLDER.length() );
                                usedVal++;
                                isFound = true;
                            }
                        }
                    }
                    if ( isFound )
                    {
                        run.replaceText( oldText, text, 0 );
                    }

                }
            }
        }

        String docText = r.text();

        assertTrue( docText.contains( "1-1" ) );
        assertTrue( docText.contains( "1-12" ) );

        assertFalse( docText.contains( "1-13" ) );
        assertFalse( docText.contains( "1-15" ) );
    }

    private static void insertTable(int rows, int columns) {
        // POI apparently can't create a document from scratch,
        // so we need an existing empty dummy document
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("empty.doc");

        Range range = doc.getRange();
        Table table = range.insertBefore(new TableProperties((short) columns), rows);

        for (int rowIdx = 0; rowIdx < table.numRows(); rowIdx++) {
            TableRow row = table.getRow(rowIdx);
            for (int colIdx = 0; colIdx < row.numCells(); colIdx++) {
                TableCell cell = row.getCell(colIdx);
                Paragraph par = cell.getParagraph(0);
                par.insertBefore("" + (rowIdx * row.numCells() + colIdx));
            }
        }
    }

    /**
     * [FAILING] Bug 47563 - HWPF failing while creating tables,
     */
    public void test47563() {
        try {
            insertTable(1, 5);
            insertTable(1, 6);
            insertTable(5, 1);
            insertTable(6, 1);
            insertTable(2, 2);
            insertTable(3, 2);
            insertTable(2, 3);
            insertTable(3, 3);

            fixed("47563");
        } catch (Exception e) {
            // expected exception
        }
    }

    /**
     * Bug 4774 - text extracted by WordExtractor is broken
     */
    public void test47742() throws Exception {

        // (1) extract text from MS Word document via POI
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug47742.doc");
        String foundText = new WordExtractor(doc).getText();

        // (2) read text from text document (retrieved by saving the word
        // document as text file using encoding UTF-8)
        InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream("Bug47742-text.txt");
        byte[] expectedBytes = IOUtils.toByteArray(is);
        String expectedText = new String(expectedBytes, "utf-8").substring(1); // strip-off the unicode marker

        assertEquals(expectedText, foundText);
    }

    /**
     * [FAILING] Bug 47958 - Exception during Escher walk of pictures
     */
    public void test47958() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug47958.doc");
        try {
            for (Picture pic : doc.getPicturesTable().getAllPictures()) {
                System.out.println(pic.suggestFullFileName());
            }
            fixed("47958");
        } catch (Exception e) {
            // expected exception
        }
    }

    /**
     * Bug 50936  - HWPF fails to read a file
     */
    public void test50936() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug50936.doc");
    }

    /**
     * [FAILING] Bug 50955 -  error while retrieving the text file
     */
    public void test50955() {
        try {
            HWPFOldDocument doc = HWPFTestDataSamples.openOldSampleFile("Bug50955.doc");
            Word6Extractor extractor = new Word6Extractor(doc);
            String text = extractor.getText();
            fixed("50955");
        } catch (Exception e) {
            // expected exception
        }
    }

}
