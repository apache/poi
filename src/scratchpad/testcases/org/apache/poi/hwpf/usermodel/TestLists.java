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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * Tests for our handling of lists
 */
public final class TestLists extends TestCase {
   public void testBasics() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      Range r = doc.getRange();
      
      assertEquals(40, r.numParagraphs());
      assertEquals("Heading Level 1\r", r.getParagraph(0).text());
      assertEquals("This document has different lists in it for testing\r", r.getParagraph(1).text());
      assertEquals("The end!\r", r.getParagraph(38).text());
      assertEquals("\r", r.getParagraph(39).text());
      
      assertEquals(0, r.getParagraph(0).getLvl());
      assertEquals(9, r.getParagraph(1).getLvl());
      assertEquals(9, r.getParagraph(38).getLvl());
      assertEquals(9, r.getParagraph(39).getLvl());
   }
   
	public void testUnorderedLists() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      Range r = doc.getRange();
      assertEquals(40, r.numParagraphs());
      
      // Normal bullet points
      assertEquals("This document has different lists in it for testing\r", r.getParagraph(1).text());
      assertEquals("Unordered list 1\r", r.getParagraph(2).text());
      assertEquals("UL 2\r", r.getParagraph(3).text());
      assertEquals("UL 3\r", r.getParagraph(4).text());
      assertEquals("Next up is an ordered list:\r", r.getParagraph(5).text());
      
      assertEquals(9, r.getParagraph(1).getLvl());
      assertEquals(9, r.getParagraph(2).getLvl());
      assertEquals(9, r.getParagraph(3).getLvl());
      assertEquals(9, r.getParagraph(4).getLvl());
      assertEquals(9, r.getParagraph(5).getLvl());
      
      assertEquals(0, r.getParagraph(1).getIlvl());
      assertEquals(0, r.getParagraph(2).getIlvl());
      assertEquals(0, r.getParagraph(3).getIlvl());
      assertEquals(0, r.getParagraph(4).getIlvl());
      assertEquals(0, r.getParagraph(5).getIlvl());
      
      // Tick bullets
      assertEquals("Now for an un-ordered list with a different bullet style:\r", r.getParagraph(9).text());
      assertEquals("Tick 1\r", r.getParagraph(10).text());
      assertEquals("Tick 2\r", r.getParagraph(11).text());
      assertEquals("Multi-level un-ordered list:\r", r.getParagraph(12).text());
      
      assertEquals(9, r.getParagraph(9).getLvl());
      assertEquals(9, r.getParagraph(10).getLvl());
      assertEquals(9, r.getParagraph(11).getLvl());
      assertEquals(9, r.getParagraph(12).getLvl());

      assertEquals(0, r.getParagraph(9).getIlvl());
      assertEquals(0, r.getParagraph(10).getIlvl());
      assertEquals(0, r.getParagraph(11).getIlvl());
      assertEquals(0, r.getParagraph(12).getIlvl());

      // TODO Test for tick not bullet
	}
   
   public void testOrderedLists() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      Range r = doc.getRange();
      assertEquals(40, r.numParagraphs());
      
      assertEquals("Next up is an ordered list:\r", r.getParagraph(5).text());
      assertEquals("Ordered list 1\r", r.getParagraph(6).text());
      assertEquals("OL 2\r", r.getParagraph(7).text());
      assertEquals("OL 3\r", r.getParagraph(8).text());
      assertEquals("Now for an un-ordered list with a different bullet style:\r", r.getParagraph(9).text());
      
      assertEquals(9, r.getParagraph(5).getLvl());
      assertEquals(9, r.getParagraph(6).getLvl());
      assertEquals(9, r.getParagraph(7).getLvl());
      assertEquals(9, r.getParagraph(8).getLvl());
      assertEquals(9, r.getParagraph(9).getLvl());

      assertEquals(0, r.getParagraph(5).getIlvl());
      assertEquals(0, r.getParagraph(6).getIlvl());
      assertEquals(0, r.getParagraph(7).getIlvl());
      assertEquals(0, r.getParagraph(8).getIlvl());
      assertEquals(0, r.getParagraph(9).getIlvl());
   }
   
   public void testMultiLevelLists() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      Range r = doc.getRange();
      assertEquals(40, r.numParagraphs());
      
      assertEquals("Multi-level un-ordered list:\r", r.getParagraph(12).text());
      assertEquals("ML 1:1\r", r.getParagraph(13).text());
      assertEquals("ML 1:2\r", r.getParagraph(14).text());
      assertEquals("ML 2:1\r", r.getParagraph(15).text());
      assertEquals("ML 2:2\r", r.getParagraph(16).text());
      assertEquals("ML 2:3\r", r.getParagraph(17).text());
      assertEquals("ML 3:1\r", r.getParagraph(18).text());
      assertEquals("ML 4:1\r", r.getParagraph(19).text());
      assertEquals("ML 5:1\r", r.getParagraph(20).text());
      assertEquals("ML 5:2\r", r.getParagraph(21).text());
      assertEquals("ML 2:4\r", r.getParagraph(22).text());
      assertEquals("ML 1:3\r", r.getParagraph(23).text());
      assertEquals("Multi-level ordered list:\r", r.getParagraph(24).text());
      assertEquals("OL 1\r", r.getParagraph(25).text());
      assertEquals("OL 2\r", r.getParagraph(26).text());
      assertEquals("OL 2.1\r", r.getParagraph(27).text());
      assertEquals("OL 2.2\r", r.getParagraph(28).text());
      assertEquals("OL 2.2.1\r", r.getParagraph(29).text());
      assertEquals("OL 2.2.2\r", r.getParagraph(30).text());
      assertEquals("OL 2.2.2.1\r", r.getParagraph(31).text());
      assertEquals("OL 2.2.3\r", r.getParagraph(32).text());
      assertEquals("OL 3\r", r.getParagraph(33).text());
      assertEquals("Finally we want some indents, to tell the difference\r", r.getParagraph(34).text());
      
      for(int i=12; i<=34; i++) {
         assertEquals(9, r.getParagraph(12).getLvl());
      }
      assertEquals(0, r.getParagraph(12).getIlvl());
      assertEquals(0, r.getParagraph(13).getIlvl());
      assertEquals(0, r.getParagraph(14).getIlvl());
      assertEquals(1, r.getParagraph(15).getIlvl());
      assertEquals(1, r.getParagraph(16).getIlvl());
      assertEquals(1, r.getParagraph(17).getIlvl());
      assertEquals(2, r.getParagraph(18).getIlvl());
      assertEquals(3, r.getParagraph(19).getIlvl());
      assertEquals(4, r.getParagraph(20).getIlvl());
      assertEquals(4, r.getParagraph(21).getIlvl());
      assertEquals(1, r.getParagraph(22).getIlvl());
      assertEquals(0, r.getParagraph(23).getIlvl());
      assertEquals(0, r.getParagraph(24).getIlvl());
      assertEquals(0, r.getParagraph(25).getIlvl());
      assertEquals(0, r.getParagraph(26).getIlvl());
      assertEquals(1, r.getParagraph(27).getIlvl());
      assertEquals(1, r.getParagraph(28).getIlvl());
      assertEquals(2, r.getParagraph(29).getIlvl());
      assertEquals(2, r.getParagraph(30).getIlvl());
      assertEquals(3, r.getParagraph(31).getIlvl());
      assertEquals(2, r.getParagraph(32).getIlvl());
      assertEquals(0, r.getParagraph(33).getIlvl());
      assertEquals(0, r.getParagraph(34).getIlvl());
   }
   
   public void testIndentedText() {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      Range r = doc.getRange();
      
      assertEquals(40, r.numParagraphs());
      assertEquals("Finally we want some indents, to tell the difference\r", r.getParagraph(34).text());
      assertEquals("Indented once\r", r.getParagraph(35).text());
      assertEquals("Indented twice\r", r.getParagraph(36).text());
      assertEquals("Indented three times\r", r.getParagraph(37).text());
      assertEquals("The end!\r", r.getParagraph(38).text());
      
      assertEquals(9, r.getParagraph(34).getLvl());
      assertEquals(9, r.getParagraph(35).getLvl());
      assertEquals(9, r.getParagraph(36).getLvl());
      assertEquals(9, r.getParagraph(37).getLvl());
      assertEquals(9, r.getParagraph(38).getLvl());
      assertEquals(9, r.getParagraph(39).getLvl());
      
      assertEquals(0, r.getParagraph(34).getIlvl());
      assertEquals(0, r.getParagraph(35).getIlvl());
      assertEquals(0, r.getParagraph(36).getIlvl());
      assertEquals(0, r.getParagraph(37).getIlvl());
      assertEquals(0, r.getParagraph(38).getIlvl());
      assertEquals(0, r.getParagraph(39).getIlvl());
      
      // TODO Test the indent
   }
   
   public void testWriteRead() throws IOException {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Lists.doc");
      doc = HWPFTestDataSamples.writeOutAndReadBack(doc);
      
      Range r = doc.getRange();
      
      // Check a couple at random
      assertEquals(4, r.getParagraph(21).getIlvl());
      assertEquals(1, r.getParagraph(22).getIlvl());
      assertEquals(0, r.getParagraph(23).getIlvl());
   }
}
