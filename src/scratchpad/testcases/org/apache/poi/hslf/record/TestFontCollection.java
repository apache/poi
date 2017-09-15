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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfoPredefined;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@code FontCollection} and {@code FontEntityAtom} records
 */
public final class TestFontCollection {
    // From a real file
    private static byte[] data;
    
    @BeforeClass
    public static void init() throws IOException {
        data = RawDataUtil.decompress(
            "H4sIAAAAAAAAAONnuMruwwAC2/ldgGQIQyZDLkMqQzGDAoMfkC4H0kEM+U"+
            "CxRIY8oHyJyQ6GmltCDClAXHac24CDAQJAYhp3eQ0YGFgYAAusGftUAAAA"
        );
    }

    @Test
    public void testFonts() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        Record[] child = fonts.getChildRecords();
        assertEquals(child.length, 1);

        FontEntityAtom fnt = (FontEntityAtom)child[0];
        assertEquals(fnt.getFontName(), "Times New Roman");
    }

    @Test
    public void testAddFont() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        HSLFFontInfo fi = fonts.addFont(HSLFFontInfoPredefined.TIMES_NEW_ROMAN);
        assertEquals((int)fi.getIndex(), 0);
        fi = fonts.addFont(new HSLFFontInfo("Helvetica"));
        assertEquals((int)fi.getIndex(), 1);
        fi = fonts.addFont(HSLFFontInfoPredefined.ARIAL);
        assertEquals((int)fi.getIndex(), 2);
        //the font being added twice
        fi = fonts.addFont(HSLFFontInfoPredefined.ARIAL);
        assertEquals((int)fi.getIndex(), 2);

        // Font collection should contain 3 fonts
        Record[] child = fonts.getChildRecords();
        assertEquals(child.length, 3);

        // Check we get the right font name for the indicies
        assertEquals("Times New Roman", fonts.getFontInfo(0).getTypeface());
        assertEquals("Helvetica", fonts.getFontInfo(1).getTypeface());
        assertEquals("Arial", fonts.getFontInfo(2).getTypeface());
        assertNull(fonts.getFontInfo(3));
    }

    @Test
    public void testWrite() throws Exception {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fonts.writeOut(out);
        byte[] recdata = out.toByteArray();
        assertArrayEquals(recdata, data);
    }
}
