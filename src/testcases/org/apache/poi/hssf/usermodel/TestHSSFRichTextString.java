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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

public final class TestHSSFRichTextString extends TestCase {
    public void testApplyFont() {

        HSSFRichTextString r = new HSSFRichTextString("testing");
        assertEquals(0,r.numFormattingRuns());
        r.applyFont(2,4, new HSSFFont((short)1, null));
        assertEquals(2,r.numFormattingRuns());
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(0));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(1));
        assertEquals(1, r.getFontAtIndex(2));
        assertEquals(1, r.getFontAtIndex(3));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(4));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(5));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(6));

        r.applyFont(6,7, new HSSFFont((short)2, null));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(0));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(1));
        assertEquals(1, r.getFontAtIndex(2));
        assertEquals(1, r.getFontAtIndex(3));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(4));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(5));
        assertEquals(2, r.getFontAtIndex(6));

        r.applyFont(HSSFRichTextString.NO_FONT);
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(0));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(1));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(2));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(3));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(4));
        assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(5));

        r.applyFont(new HSSFFont((short)1, null));
        assertEquals(1, r.getFontAtIndex(0));
        assertEquals(1, r.getFontAtIndex(1));
        assertEquals(1, r.getFontAtIndex(2));
        assertEquals(1, r.getFontAtIndex(3));
        assertEquals(1, r.getFontAtIndex(4));
        assertEquals(1, r.getFontAtIndex(5));
        assertEquals(1, r.getFontAtIndex(6));

    }

    public void testClearFormatting() {

      HSSFRichTextString r = new HSSFRichTextString("testing");
      assertEquals(0, r.numFormattingRuns());
      r.applyFont(2, 4, new HSSFFont( (short) 1, null));
      assertEquals(2, r.numFormattingRuns());
      r.clearFormatting();
      assertEquals(0, r.numFormattingRuns());
    }


    /**
     * Test case proposed in Bug 40520:  formated twice => will format whole String
     */
    public void test40520_1() {

        short font = 3;

        HSSFRichTextString r = new HSSFRichTextString("f0_123456789012345678901234567890123456789012345678901234567890");

        r.applyFont(0,7,font);
        r.applyFont(5,9,font);

        for(int i=0; i < 7; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=5; i < 9; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=9; i < r.length(); i++) assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(i));
    }

    /**
     * Test case proposed in Bug 40520:  overlapped range => will format whole String
     */
    public void test40520_2() {

        short font = 3;
        HSSFRichTextString r = new HSSFRichTextString("f0_123456789012345678901234567890123456789012345678901234567890");

        r.applyFont(0,2,font);
        for(int i=0; i < 2; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=2; i < r.length(); i++) assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(i));

        r.applyFont(0,2,font);
        for(int i=0; i < 2; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=2; i < r.length(); i++) assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(i));
    }

    /**
     * Test case proposed in Bug 40520:  formated twice => will format whole String
     */
    public void test40520_3() {

        short font = 3;
        HSSFRichTextString r = new HSSFRichTextString("f0_123456789012345678901234567890123456789012345678901234567890");

        // wrong order => will format 0-6
        r.applyFont(0,2,font);
        r.applyFont(5,7,font);
        r.applyFont(0,2,font);

        r.applyFont(0,2,font);
        for(int i=0; i < 2; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=2; i < 5; i++) assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(i));
        for(int i=5; i < 7; i++) assertEquals(font, r.getFontAtIndex(i));
        for(int i=7; i < r.length(); i++) assertEquals(HSSFRichTextString.NO_FONT, r.getFontAtIndex(i));
    }
}
