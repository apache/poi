/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.sl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.apache.poi.sl.TestTable.openSampleSlideshow;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.junit.Test;

public class TestHeadersFooters {
    @Test
    public void bug58144() throws IOException {
        SlideShow<?,?> ppt1 = openSampleSlideshow("bug58144-headers-footers-2003.ppt");
        HSLFSlide sl1 = (HSLFSlide)ppt1.getSlides().get(0);
        HeadersFooters hfs1 = sl1.getHeadersFooters();
        assertNull(hfs1.getHeaderText());
        assertEquals("Confidential", hfs1.getFooterText());
        List<List<HSLFTextParagraph>> llp1 = sl1.getTextParagraphs();
        assertEquals("Test", HSLFTextParagraph.getText(llp1.get(0)));
        assertFalse(llp1.get(0).get(0).isHeaderOrFooter());
        ppt1.close();

        String ppt2007s[] = {
            "bug58144-headers-footers-2007.ppt", "bug58144-headers-footers-2007.pptx"
        };
        
        for (String pptName : ppt2007s) {
            SlideShow<?,?> ppt2 = openSampleSlideshow(pptName);
            Slide<?,?> sl2 =  ppt2.getSlides().get(0);
            
            if (ppt2 instanceof HSLFSlideShow) {
                HeadersFooters hfs2 = ((HSLFSlide)sl2).getHeadersFooters();
                assertNull(hfs2.getHeaderText());
                assertEquals("Slide footer", hfs2.getFooterText());
            }
            
            List<? extends Shape<?,?>> shapes = sl2.getShapes();
            TextShape<?,?> ts0 = (TextShape<?,?>)shapes.get(0);
            assertEquals("Test file", ts0.getText());
            TextShape<?,?> ts1 = (TextShape<?,?>)shapes.get(1);
            assertEquals("Has some text in the headers and footers", ts1.getText());
            TextShape<?,?> ts2 = (TextShape<?,?>)shapes.get(2);
            assertEquals("Slide footer", ts2.getText());
            List<? extends TextParagraph<?,?,?>> ltp2 = ts2.getTextParagraphs();
            assertTrue(ltp2.get(0).isHeaderOrFooter());
            ppt2.close();
        }
    }
}
