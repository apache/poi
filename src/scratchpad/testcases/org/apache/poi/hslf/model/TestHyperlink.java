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

package org.apache.poi.hslf.model;

import junit.framework.TestCase;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Test Hyperlink.
 *
 * @author Yegor Kozlov
 */
public final class TestHyperlink extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    public void testTextRunHyperlinks() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("WithLinks.ppt"));

        TextRun[] run;
        Slide slide;
        slide = ppt.getSlides()[0];
        run = slide.getTextRuns();
        for (int i = 0; i < run.length; i++) {
            String text = run[i].getText();
            if (text.equals(
                    "This page has two links:\n" +
                    "http://jakarta.apache.org/poi/\n" +
                    "\n" +
                    "http://slashdot.org/\n" +
                    "\n" +
                    "In addition, its notes has one link")){

                Hyperlink[] links = run[i].getHyperlinks();
                assertNotNull(links);
                assertEquals(2, links.length);

                assertEquals("http://jakarta.apache.org/poi/", links[0].getTitle());
                assertEquals("http://jakarta.apache.org/poi/", links[0].getAddress());
                assertEquals("http://jakarta.apache.org/poi/", text.substring(links[0].getStartIndex(), links[0].getEndIndex()-1));

                assertEquals("http://slashdot.org/", links[1].getTitle());
                assertEquals("http://slashdot.org/", links[1].getAddress());
                assertEquals("http://slashdot.org/", text.substring(links[1].getStartIndex(), links[1].getEndIndex()-1));

            }
        }

        slide = ppt.getSlides()[1];
        run = slide.getTextRuns();
        for (int i = 0; i < run.length; i++) {
            String text = run[i].getText();
            if (text.equals(
                    "I have the one link:\n" +
                    "Jakarta HSSF")){

                Hyperlink[] links = run[i].getHyperlinks();
                assertNotNull(links);
                assertEquals(1, links.length);

                assertEquals("http://jakarta.apache.org/poi/hssf/", links[0].getTitle());
                assertEquals("http://jakarta.apache.org/poi/hssf/", links[0].getAddress());
                assertEquals("Jakarta HSSF", text.substring(links[0].getStartIndex(), links[0].getEndIndex()-1));

            }
        }

    }

}
