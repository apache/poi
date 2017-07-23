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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfoPredefined;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.junit.Test;


/**
 * Test adding fonts to the presentation resources
 */
public final class TestPPFont {

    @Test
    public void testCreate() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
        assertEquals(1, ppt.getNumberOfFonts());
        assertEquals("Arial", ppt.getFont(0).getTypeface());

        //adding the same font twice
        assertEquals(0, (int)ppt.addFont(HSLFFontInfoPredefined.ARIAL).getIndex());
        assertEquals(1, ppt.getNumberOfFonts());

        assertEquals(1, (int)ppt.addFont(HSLFFontInfoPredefined.TIMES_NEW_ROMAN).getIndex());
        assertEquals(2, (int)ppt.addFont(HSLFFontInfoPredefined.COURIER_NEW).getIndex());
        assertEquals(3, (int)ppt.addFont(HSLFFontInfoPredefined.WINGDINGS).getIndex());

        assertEquals(4, ppt.getNumberOfFonts());

        assertEquals(HSLFFontInfoPredefined.TIMES_NEW_ROMAN.getTypeface(), ppt.getFont(1).getTypeface());
        assertEquals(HSLFFontInfoPredefined.COURIER_NEW.getTypeface(), ppt.getFont(2).getTypeface());

        HSLFFontInfo font3 = ppt.getFont(3);
        assertEquals(HSLFFontInfoPredefined.WINGDINGS.getTypeface(), font3.getTypeface());
        assertEquals(FontCharset.SYMBOL, font3.getCharset());
        assertEquals(FontPitch.VARIABLE, font3.getPitch());
        
        ppt.close();
    }
}
