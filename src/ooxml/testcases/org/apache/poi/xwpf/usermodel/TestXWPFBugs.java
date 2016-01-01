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
package org.apache.poi.xwpf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange;
import org.junit.Test;

public class TestXWPFBugs {
    @Test
    public void bug55802() throws Exception {
        String blabla =
                "Bir, iki, \u00fc\u00e7, d\u00f6rt, be\u015f,\n" +
                        "\nalt\u0131, yedi, sekiz, dokuz, on.\n" +
                        "\nK\u0131rm\u0131z\u0131 don,\n" +
                        "\ngel bizim bah\u00e7eye kon,\n" +
                        "\nsar\u0131 limon";
        XWPFDocument doc = new XWPFDocument();
        XWPFRun run = doc.createParagraph().createRun();

        for (String str : blabla.split("\n")) {
            run.setText(str);
            run.addBreak();
        }

        run.setFontFamily("Times New Roman");
        run.setFontSize(20);
        assertEquals(run.getFontFamily(), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.cs), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.eastAsia), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.hAnsi), "Times New Roman");
        run.setFontFamily("Arial", FontCharRange.hAnsi);
        assertEquals(run.getFontFamily(FontCharRange.hAnsi), "Arial");
        
        doc.close();
    }


    @Test
    public void bug57312_NullPointException() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("57312.docx");
        assertNotNull(doc);

        for (IBodyElement bodyElement : doc.getBodyElements()) {
            BodyElementType elementType = bodyElement.getElementType();

            if (elementType == BodyElementType.PARAGRAPH) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;

                for (IRunElement iRunElem : paragraph.getIRuns()) {

                    if (iRunElem instanceof XWPFRun) {
                        XWPFRun runElement = (XWPFRun) iRunElem;

                        UnderlinePatterns underline = runElement.getUnderline();
                        assertNotNull(underline);

                        //System.out.println("Found: " + underline + ": " + runElement.getText(0));
                    }
                }
            }
        }
    }


    @Test
    public void test56392() throws IOException, OpenXML4JException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("56392.docx");
        assertNotNull(doc);
    }

    /**
     * Removing a run needs to remove it from both Runs and IRuns
     */
    @Test
    public void test57829() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
        assertNotNull(doc);
        assertEquals(3, doc.getParagraphs().size());

        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            paragraph.removeRun(0);
            assertNotNull(paragraph.getText());
        }
    }
    
  /**
   * Removing a run needs to take into account position of run if paragraph contains hyperlink runs
   */
  @Test
  public void test58618() throws Exception {
      XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("58618.docx");
      XWPFParagraph para = (XWPFParagraph)doc.getBodyElements().get(0);
      assertNotNull(para);
      assertEquals("Some text  some hyper links link link and some text.....", para.getText());
      XWPFRun run = para.insertNewRun(para.getRuns().size());
      run.setText("New Text");
      assertEquals("Some text  some hyper links link link and some text.....New Text", para.getText());
      para.removeRun(para.getRuns().size() -2);
      assertEquals("Some text  some hyper links link linkNew Text", para.getText());
  }
}
