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

import static org.apache.poi.sl.SLCommonUtils.openSampleSlideshow;
import static org.apache.poi.sl.SLCommonUtils.xslfOnly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.junit.Test;

public class TestHeadersFooters {
    @Test
    public void bug58144a() throws IOException {
        assumeFalse(xslfOnly());
        SlideShow<?,?> ppt = openSampleSlideshow("bug58144-headers-footers-2003.ppt");
        HSLFSlide sl = (HSLFSlide)ppt.getSlides().get(0);
        HeadersFooters hfs = sl.getHeadersFooters();
        assertNull(hfs.getHeaderText());
        assertEquals("Confidential", hfs.getFooterText());
        List<List<HSLFTextParagraph>> llp = sl.getTextParagraphs();
        assertEquals("Test", HSLFTextParagraph.getText(llp.get(0)));
        assertFalse(llp.get(0).get(0).isHeaderOrFooter());
        ppt.close();
    }
    
    @Test
    public void bug58144b() throws IOException {
        assumeFalse(xslfOnly());
        SlideShow<?,?> ppt = openSampleSlideshow("bug58144-headers-footers-2007.ppt");
        Slide<?,?> sl =  ppt.getSlides().get(0);
        HeadersFooters hfs2 = ((HSLFSlide)sl).getHeadersFooters();
        assertNull(hfs2.getHeaderText());
        assertEquals("Slide footer", hfs2.getFooterText());

        testSlideShow(ppt);
        ppt.close();
    }
    
    @Test
    public void bug58144c() throws IOException {
        SlideShow<?,?> ppt = openSampleSlideshow("bug58144-headers-footers-2007.pptx");
        testSlideShow(ppt);
        ppt.close();
    }
    
    private void testSlideShow(SlideShow<?,?> ppt) {
        Slide<?,?> sl =  ppt.getSlides().get(0);
        
        List<? extends Shape<?,?>> shapes = sl.getShapes();
        TextShape<?,?> ts0 = (TextShape<?,?>)shapes.get(0);
        assertEquals("Test file", ts0.getText());
        TextShape<?,?> ts1 = (TextShape<?,?>)shapes.get(1);
        assertEquals("Has some text in the headers and footers", ts1.getText());
        TextShape<?,?> ts2 = (TextShape<?,?>)shapes.get(2);
        assertEquals("Slide footer", ts2.getText());
        List<? extends TextParagraph<?,?,?>> ltp = ts2.getTextParagraphs();
        assertTrue(ltp.get(0).isHeaderOrFooter());
    }
}
