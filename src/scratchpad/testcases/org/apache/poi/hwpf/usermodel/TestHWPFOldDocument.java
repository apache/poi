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

import org.apache.poi.OldFileFormatException;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * Tests for Word 6 and Word 95 support
 */
public final class TestHWPFOldDocument extends HWPFTestCase {
   /**
    * Test a simple Word 6 document
    */
   public void testWord6() throws Exception {
      // Can't open as HWPFDocument
      try {
         HWPFTestDataSamples.openSampleFile("Word6.doc");
         fail("Shouldn't be openable");
      } catch(OldFileFormatException e) {}
      
      // Open
      HWPFOldDocument doc = HWPFTestDataSamples.openOldSampleFile("Word6.doc");
      
      // Check
      assertEquals(1, doc.getRange().numSections());
      assertEquals(1, doc.getRange().numParagraphs());
      assertEquals(1, doc.getRange().numCharacterRuns());
      
      assertEquals(
            "The quick brown fox jumps over the lazy dog\r",
            doc.getRange().getParagraph(0).text()
      );
   }
   
   /**
    * Test a simple Word 95 document
    */
   public void testWord95() throws Exception {
      // Can't open as HWPFDocument
      try {
         HWPFTestDataSamples.openSampleFile("Word95.doc");
         fail("Shouldn't be openable");
      } catch(OldFileFormatException e) {}
      
      // Open
      HWPFOldDocument doc = HWPFTestDataSamples.openOldSampleFile("Word95.doc");
      
      // Check
      assertEquals(1, doc.getRange().numSections());
      assertEquals(7, doc.getRange().numParagraphs());
      
      assertEquals(
            "The quick brown fox jumps over the lazy dog\r",
            doc.getRange().getParagraph(0).text()
      );
      assertEquals("\r", doc.getRange().getParagraph(1).text());
      assertEquals(
            "Paragraph 2\r",
            doc.getRange().getParagraph(2).text()
      );
      assertEquals("\r", doc.getRange().getParagraph(3).text());
      assertEquals(
            "Paragraph 3. Has some RED text and some " +
            "BLUE BOLD text in it.\r",
            doc.getRange().getParagraph(4).text()
      );
      assertEquals("\r", doc.getRange().getParagraph(5).text());
      assertEquals(
            "Last (4th) paragraph.\r",
            doc.getRange().getParagraph(6).text()
      );
      
      assertEquals(1, doc.getRange().getParagraph(0).numCharacterRuns());
      assertEquals(1, doc.getRange().getParagraph(1).numCharacterRuns());
      assertEquals(1, doc.getRange().getParagraph(2).numCharacterRuns());
      assertEquals(1, doc.getRange().getParagraph(3).numCharacterRuns());
      // Normal, red, normal, blue+bold, normal
      assertEquals(5, doc.getRange().getParagraph(4).numCharacterRuns());
      assertEquals(1, doc.getRange().getParagraph(5).numCharacterRuns());
      // Normal, superscript for 4th, normal
      assertEquals(3, doc.getRange().getParagraph(6).numCharacterRuns());
   }
   
   /**
    * Test a word document that has sections,
    *  as well as the usual paragraph stuff.
    */
   public void testWord6Sections() throws Exception {
      HWPFOldDocument doc = HWPFTestDataSamples.openOldSampleFile("Word6_sections.doc");
      
      assertEquals(3, doc.getRange().numSections());
      assertEquals(6, doc.getRange().numParagraphs());
      
      assertEquals(
            "This is a test.\r",
            doc.getRange().getParagraph(0).text()
      );
      assertEquals("\r", doc.getRange().getParagraph(1).text());
      assertEquals("\u000c", doc.getRange().getParagraph(2).text()); // Section line?
      assertEquals("This is a new section.\r", doc.getRange().getParagraph(3).text());
      assertEquals("\u000c", doc.getRange().getParagraph(4).text()); // Section line?
      assertEquals("\r", doc.getRange().getParagraph(5).text());
   }
   
   /**
    * Another word document with sections, this time with a 
    *  few more section properties set on it
    */
   public void testWord6Sections2() throws Exception {
      HWPFOldDocument doc = HWPFTestDataSamples.openOldSampleFile("Word6_sections2.doc");
      
      assertEquals(1, doc.getRange().numSections());
      assertEquals(57, doc.getRange().numParagraphs());
      
      assertEquals(
            "\r",
            doc.getRange().getParagraph(0).text()
      );
      assertEquals(
            "STATEMENT  OF  INSOLVENCY  PRACTICE  10  (SCOTLAND)\r",
            doc.getRange().getParagraph(1).text()
      );
   }
}
