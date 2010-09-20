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
import org.apache.poi.hwpf.model.StyleSheet;

public final class TestBug49820 extends TestCase {

  public void test() throws IOException {
    HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug49820.doc");
    
    Range documentRange = doc.getRange();
    StyleSheet styleSheet = doc.getStyleSheet();
    
    // JUnit asserts
    assertLevels(documentRange, styleSheet, 0, 0, 0);
    assertLevels(documentRange, styleSheet, 1, 1, 1);
    assertLevels(documentRange, styleSheet, 2, 2, 2);
    assertLevels(documentRange, styleSheet, 3, 3, 3);
    assertLevels(documentRange, styleSheet, 4, 4, 4);
    assertLevels(documentRange, styleSheet, 5, 5, 5);
    assertLevels(documentRange, styleSheet, 6, 6, 6);
    assertLevels(documentRange, styleSheet, 7, 7, 7);
    assertLevels(documentRange, styleSheet, 8, 8, 8);
    assertLevels(documentRange, styleSheet, 9, 9, 9);
    assertLevels(documentRange, styleSheet, 10, 9, 0);
    assertLevels(documentRange, styleSheet, 11, 9, 4);
    
    // output to console
    /*for (int i=0; i<documentRange.numParagraphs(); i++) {
      Paragraph par = documentRange.getParagraph(i);
      int styleLvl = styleSheet.getParagraphStyle(par.getStyleIndex()).getLvl();
      int parLvl = par.getLvl();
      System.out.println("Style level: " + styleLvl + ", paragraph level: " + parLvl + ", text: " + par.text());
    }*/
  }

  private void assertLevels(Range documentRange, StyleSheet styleSheet, int parIndex, int expectedStyleLvl, int expectedParLvl) {
    Paragraph par = documentRange.getParagraph(parIndex);
    assertEquals(expectedStyleLvl, styleSheet.getParagraphStyle(par.getStyleIndex()).getLvl());
    assertEquals(expectedParLvl, par.getLvl());
  }
}