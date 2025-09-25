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

package org.apache.poi.hslf.record;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfoPredefined;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@code FontCollection} and {@code FontEntityAtom} records
 */
public final class TestFontCollection {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    // From a real file
    private static byte[] data;

    @BeforeAll
    public static void init() throws IOException {
        data = RawDataUtil.decompress(
            "H4sIAAAAAAAAAONnuMruwwAC2/ldgGQIQyZDLkMqQzGDAoMfkC4H0kEM+U"+
            "CxRIY8oHyJyQ6GmltCDClAXHac24CDAQJAYhp3eQ0YGFgYAAusGftUAAAA"
        );
    }

    @Test
    void testFonts() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        Record[] child = fonts.getChildRecords();
        assertEquals(1, child.length);

        FontEntityAtom fnt = (FontEntityAtom)child[0];
        assertEquals("Times New Roman", fnt.getFontName());
    }

    @Test
    void testAddFont() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        HSLFFontInfo fi = fonts.addFont(HSLFFontInfoPredefined.TIMES_NEW_ROMAN);
        assertEquals(0, (int)fi.getIndex());
        fi = fonts.addFont(new HSLFFontInfo("Helvetica"));
        assertEquals(1, (int)fi.getIndex());
        fi = fonts.addFont(HSLFFontInfoPredefined.ARIAL);
        assertEquals(2, (int)fi.getIndex());
        //the font being added twice
        fi = fonts.addFont(HSLFFontInfoPredefined.ARIAL);
        assertEquals(2, (int)fi.getIndex());

        // Font collection should contain 3 fonts
        Record[] child = fonts.getChildRecords();
        assertEquals(3, child.length);

        // Check we get the right font name for the indicies
        fi = fonts.getFontInfo(0);
        assertNotNull(fi);
        assertEquals("Times New Roman", fi.getTypeface());
        fi = fonts.getFontInfo(1);
        assertNotNull(fi);
        assertEquals("Helvetica", fi.getTypeface());
        fi = fonts.getFontInfo(2);
        assertNotNull(fi);
        assertEquals("Arial", fi.getTypeface());
        assertNull(fonts.getFontInfo(3));
    }

    @Test
    void testWrite() throws Exception {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        fonts.writeOut(out);
        byte[] recdata = out.toByteArray();
        assertArrayEquals(recdata, data);
    }

    @Test
    void bug61881() throws IOException {
        try (InputStream is = _slTests.openResourceAsStream("bug61881.ppt")) {
            try (HSLFSlideShow ppt = new HSLFSlideShow(is)) {
                assertEquals("?imes New Roman",ppt.getFont(3).getTypeface());
            }
        }
    }
}
