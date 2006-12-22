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

public class TestHSSFRichTextString extends TestCase
{
    public void testApplyFont() throws Exception
    {

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

    public void testClearFormatting() throws Exception
    {

      HSSFRichTextString r = new HSSFRichTextString("testing");
      assertEquals(0, r.numFormattingRuns());
      r.applyFont(2, 4, new HSSFFont( (short) 1, null));
      assertEquals(2, r.numFormattingRuns());
      r.clearFormatting();
      assertEquals(0, r.numFormattingRuns());
    }
}
