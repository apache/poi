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

import static org.apache.poi.poifs.storage.RawDataUtil.decompress;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.junit.jupiter.api.Test;

class TestText {

    @Test
    void testResultEqualsToNonExistingAbstractShape() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());

        assertEquals(5, textbox.getEscherContainer().getChildCount());

        //sp record
        byte[] expected = decompress("H4sIAAAAAAAAAFvEw/WBg4GBgZEFSHAxMAAA9gX7nhAAAAA=");
        byte[] actual = textbox.getEscherContainer().getChild(0).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPggxIANAABK4+laGgAAAA==");
        actual = textbox.getEscherContainer().getChild(2).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPzAAAQACl6c5QgAAAA=");
        actual = textbox.getEscherContainer().getChild(3).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNg4P3AAAQA6pyIkQgAAAA=");
        actual = textbox.getEscherContainer().getChild(4).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        ObjRecord obj = textbox.getObjRecord();

        expected = decompress("H4sIAAAAAAAAAItlkGIQZRBiYGNgZBBMYEADAOdCLuweAAAA");
        actual = obj.serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        TextObjectRecord tor = textbox.getTextObjectRecord();

        expected = decompress("H4sIAAAAAAAAANvGKMQgxMSABgBGi8T+FgAAAA==");
        actual = tor.serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        wb.close();
    }

    @Test
    void testAddTextToExistingFile() throws Exception {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            HSSFPatriarch patriarch = sh.createDrawingPatriarch();
            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
            textbox.setString(new HSSFRichTextString("just for test"));
            HSSFTextbox textbox2 = patriarch.createTextbox(new HSSFClientAnchor());
            textbox2.setString(new HSSFRichTextString("just for test2"));

            assertEquals(2, patriarch.getChildren().size());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                patriarch = sh.getDrawingPatriarch();

                assertEquals(2, patriarch.getChildren().size());
                HSSFTextbox text3 = patriarch.createTextbox(new HSSFClientAnchor());
                text3.setString(new HSSFRichTextString("text3"));
                assertEquals(3, patriarch.getChildren().size());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sh = wb3.getSheetAt(0);
                    patriarch = sh.getDrawingPatriarch();

                    assertEquals(3, patriarch.getChildren().size());
                    assertEquals("just for test", ((HSSFTextbox) patriarch.getChildren().get(0)).getString().getString());
                    assertEquals("just for test2", ((HSSFTextbox) patriarch.getChildren().get(1)).getString().getString());
                    assertEquals("text3", ((HSSFTextbox) patriarch.getChildren().get(2)).getString().getString());
                }
            }
        }
    }

    @Test
    void testSetGetProperties() throws Exception {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            HSSFPatriarch patriarch = sh.createDrawingPatriarch();
            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
            textbox.setString(new HSSFRichTextString("test"));
            assertEquals("test", textbox.getString().getString());

            textbox.setHorizontalAlignment((short) 5);
            assertEquals(5, textbox.getHorizontalAlignment());

            textbox.setVerticalAlignment((short) 6);
            assertEquals((short) 6, textbox.getVerticalAlignment());

            textbox.setMarginBottom(7);
            assertEquals(7, textbox.getMarginBottom());

            textbox.setMarginLeft(8);
            assertEquals(8, textbox.getMarginLeft());

            textbox.setMarginRight(9);
            assertEquals(9, textbox.getMarginRight());

            textbox.setMarginTop(10);
            assertEquals(10, textbox.getMarginTop());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                patriarch = sh.getDrawingPatriarch();
                textbox = (HSSFTextbox) patriarch.getChildren().get(0);
                assertEquals("test", textbox.getString().getString());
                assertEquals(5, textbox.getHorizontalAlignment());
                assertEquals((short) 6, textbox.getVerticalAlignment());
                assertEquals(7, textbox.getMarginBottom());
                assertEquals(8, textbox.getMarginLeft());
                assertEquals(9, textbox.getMarginRight());
                assertEquals(10, textbox.getMarginTop());

                textbox.setString(new HSSFRichTextString("test1"));
                textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
                textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
                textbox.setMarginBottom(71);
                textbox.setMarginLeft(81);
                textbox.setMarginRight(91);
                textbox.setMarginTop(101);

                assertEquals("test1", textbox.getString().getString());
                assertEquals(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED, textbox.getHorizontalAlignment());
                assertEquals(HSSFTextbox.VERTICAL_ALIGNMENT_TOP, textbox.getVerticalAlignment());
                assertEquals(71, textbox.getMarginBottom());
                assertEquals(81, textbox.getMarginLeft());
                assertEquals(91, textbox.getMarginRight());
                assertEquals(101, textbox.getMarginTop());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sh = wb3.getSheetAt(0);
                    patriarch = sh.getDrawingPatriarch();
                    textbox = (HSSFTextbox) patriarch.getChildren().get(0);

                    assertEquals("test1", textbox.getString().getString());
                    assertEquals(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED, textbox.getHorizontalAlignment());
                    assertEquals(HSSFTextbox.VERTICAL_ALIGNMENT_TOP, textbox.getVerticalAlignment());
                    assertEquals(71, textbox.getMarginBottom());
                    assertEquals(81, textbox.getMarginLeft());
                    assertEquals(91, textbox.getMarginRight());
                    assertEquals(101, textbox.getMarginTop());
                }
            }
        }
    }

    @Test
    void testExistingFileWithText() throws Exception {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("text");
            HSSFPatriarch drawing = sheet.getDrawingPatriarch();
            assertEquals(1, drawing.getChildren().size());
            HSSFTextbox textbox = (HSSFTextbox) drawing.getChildren().get(0);
            assertEquals(HSSFTextbox.VERTICAL_ALIGNMENT_TOP, textbox.getVerticalAlignment());
            assertEquals(0, textbox.getMarginTop());
            assertEquals(3600000, textbox.getMarginBottom());
            assertEquals(3600000, textbox.getMarginLeft());
            assertEquals(0, textbox.getMarginRight());
            assertEquals("teeeeesssstttt", textbox.getString().getString());
        }
    }
}
