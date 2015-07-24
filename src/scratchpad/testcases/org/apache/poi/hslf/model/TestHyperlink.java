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

import static org.apache.poi.hslf.usermodel.HSLFTextParagraph.getRawText;
import static org.apache.poi.hslf.usermodel.HSLFTextParagraph.toExternalString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.*;
import org.junit.Test;

/**
 * Test Hyperlink.
 *
 * @author Yegor Kozlov
 */
public final class TestHyperlink {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    public void testTextRunHyperlinks() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("WithLinks.ppt"));

        HSLFSlide slide = ppt.getSlides().get(0);
        List<HSLFTextParagraph> para = slide.getTextParagraphs().get(1);
        
        String rawText = toExternalString(getRawText(para), para.get(0).getRunType());
        String expected =
            "This page has two links:\n"+
            "http://jakarta.apache.org/poi/\n"+
            "\n"+
            "http://slashdot.org/\n"+
            "\n"+
            "In addition, its notes has one link";
        assertEquals(expected, rawText);
        
        List<HSLFHyperlink> links = HSLFHyperlink.find(para);
        assertNotNull(links);
        assertEquals(2, links.size());

        assertEquals("http://jakarta.apache.org/poi/", links.get(0).getTitle());
        assertEquals("http://jakarta.apache.org/poi/", links.get(0).getAddress());
        assertEquals("http://jakarta.apache.org/poi/", rawText.substring(links.get(0).getStartIndex(), links.get(0).getEndIndex()-1));

        assertEquals("http://slashdot.org/", links.get(1).getTitle());
        assertEquals("http://slashdot.org/", links.get(1).getAddress());
        assertEquals("http://slashdot.org/", rawText.substring(links.get(1).getStartIndex(), links.get(1).getEndIndex()-1));

        slide = ppt.getSlides().get(1);
        para = slide.getTextParagraphs().get(1);
        rawText = toExternalString(getRawText(para), para.get(0).getRunType());
        expected = 
            "I have the one link:\n" +
            "Jakarta HSSF";
        assertEquals(expected, rawText);

        links = HSLFHyperlink.find(para);
        assertNotNull(links);
        assertEquals(1, links.size());

        assertEquals("http://jakarta.apache.org/poi/hssf/", links.get(0).getTitle());
        assertEquals("http://jakarta.apache.org/poi/hssf/", links.get(0).getAddress());
        assertEquals("Jakarta HSSF", rawText.substring(links.get(0).getStartIndex(), links.get(0).getEndIndex()-1));
    }
}
