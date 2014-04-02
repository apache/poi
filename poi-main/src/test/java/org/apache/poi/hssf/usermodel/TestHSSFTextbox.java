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

/**
 * Test <code>HSSFTextbox</code>.
 *
 * @author Yegor Kozlov (yegor at apache.org)
 */
public final class TestHSSFTextbox extends TestCase{

    /**
     * Test that accessors to horizontal and vertical alignment work properly
     */
    public void testAlignment() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh1 = wb.createSheet();
        HSSFPatriarch patriarch = sh1.createDrawingPatriarch();

        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor(0, 0, 0, 0, (short) 1, 1, (short) 6, 4));
        HSSFRichTextString str = new HSSFRichTextString("Hello, World");
        textbox.setString(str);
        textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
        textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_CENTER);

        assertEquals(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED, textbox.getHorizontalAlignment());
        assertEquals(HSSFTextbox.VERTICAL_ALIGNMENT_CENTER, textbox.getVerticalAlignment());
    }

    /**
     * Excel requires at least one format run in HSSFTextbox.
     * When inserting text make sure that if font is not set we must set the default one.
     */
    public void testSetDeafultTextFormat() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFTextbox textbox1 = patriarch.createTextbox(new HSSFClientAnchor(0,0,0,0,(short)1,1,(short)3,3));
        HSSFRichTextString rt1 = new HSSFRichTextString("Hello, World!");
        assertEquals(0, rt1.numFormattingRuns());
        textbox1.setString(rt1);

        HSSFRichTextString rt2 = textbox1.getString();
        assertEquals(1, rt2.numFormattingRuns());
        assertEquals(HSSFRichTextString.NO_FONT, rt2.getFontOfFormattingRun(0));
    }
}
