package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

public class TestHSSFRichTextString extends TestCase
{
    public void testApplyFont() throws Exception
    {

        HSSFRichTextString r = new HSSFRichTextString("testing");
        assertEquals(1,r.numFormattingRuns());
        r.applyFont(2,4, new HSSFFont((short)1, null));
        assertEquals(3,r.numFormattingRuns());
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

}
