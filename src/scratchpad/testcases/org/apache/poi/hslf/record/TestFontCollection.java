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

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Tests <code>FontCollection</code> and <code>FontEntityAtom</code> records
 *
 * @author Yegor Kozlov
 */
public final class TestFontCollection extends TestCase {
    // From a real file
    private byte[] data = new byte[]  {
        0x0F, 0x00, 0xD5-256, 0x07, 0x4C, 0x00, 0x00, 0x00,
        0x00, 0x00, 0xB7-256, 0x0F, 0x44, 0x00, 0x00, 0x00,
        0x54, 0x00, 0x69, 0x00, 0x6D, 0x00, 0x65, 0x00, 0x73, 0x00,
        0x20, 0x00, 0x4E, 0x00, 0x65, 0x00, 0x77, 0x00, 0x20, 0x00,
        0x52, 0x00, 0x6F, 0x00, 0x6D, 0x00, 0x61, 0x00, 0x6E, 0x00,
        0x00, 0x00, 0x74, 0x34, 0xB8-256, 0x00, 0x7C, 0xDA-256, 0x12, 0x00,
        0x64, 0xDA-256, 0x12, 0x00, 0x76, 0xC7-256, 0x0B, 0x30, 0x08, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7C, 0xDA-256, 0x12, 0x00,
        0x28, 0xDD-256, 0x0D, 0x30, 0x00, 0x00, 0x04, 0x00  };

    public void testFonts() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        Record[] child = fonts.getChildRecords();
        assertEquals(child.length, 1);

        FontEntityAtom fnt = (FontEntityAtom)child[0];
        assertEquals(fnt.getFontName(), "Times New Roman");
    }

    public void testAddFont() {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        int idx = fonts.addFont("Times New Roman");
        assertEquals(idx, 0);
        idx = fonts.addFont("Helvetica");
        assertEquals(idx, 1);
        idx = fonts.addFont("Arial");
        assertEquals(idx, 2);
        idx = fonts.addFont("Arial"); //the font being added twice
        assertEquals(idx, 2);

        // Font collection should contain 3 fonts
        Record[] child = fonts.getChildRecords();
        assertEquals(child.length, 3);

        // Check we get the right font name for the indicies
        assertEquals("Times New Roman", fonts.getFontWithId(0));
        assertEquals("Helvetica", fonts.getFontWithId(1));
        assertEquals("Arial", fonts.getFontWithId(2));
        assertNull(fonts.getFontWithId(3));
    }

    public void testWrite() throws Exception {
        FontCollection fonts = new FontCollection(data, 0, data.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fonts.writeOut(out);
        byte[] recdata = out.toByteArray();
        assertTrue(Arrays.equals(recdata, data));
    }
}
