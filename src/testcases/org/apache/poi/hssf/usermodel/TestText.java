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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.junit.Test;

/**
 * @author Evgeniy Berlog
 * @date 25.06.12
 */
public class TestText {

    @Test
    public void testResultEqualsToNonExistingAbstractShape() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());

        assertEquals(textbox.getEscherContainer().getChildRecords().size(), 5);

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
    public void testAddTextToExistingFile() throws Exception {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sh = wb1.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
        textbox.setString(new HSSFRichTextString("just for test"));
        HSSFTextbox textbox2 = patriarch.createTextbox(new HSSFClientAnchor());
        textbox2.setString(new HSSFRichTextString("just for test2"));

        assertEquals(patriarch.getChildren().size(), 2);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sh = wb2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);
        HSSFTextbox text3 = patriarch.createTextbox(new HSSFClientAnchor());
        text3.setString(new HSSFRichTextString("text3"));
        assertEquals(patriarch.getChildren().size(), 3);

        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        wb2.close();
        sh = wb3.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 3);
        assertEquals(((HSSFTextbox) patriarch.getChildren().get(0)).getString().getString(), "just for test");
        assertEquals(((HSSFTextbox) patriarch.getChildren().get(1)).getString().getString(), "just for test2");
        assertEquals(((HSSFTextbox) patriarch.getChildren().get(2)).getString().getString(), "text3");
        
        wb3.close();
    }

    @Test
    public void testSetGetProperties() throws Exception {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sh = wb1.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
        textbox.setString(new HSSFRichTextString("test"));
        assertEquals(textbox.getString().getString(), "test");

        textbox.setHorizontalAlignment((short) 5);
        assertEquals(textbox.getHorizontalAlignment(), 5);

        textbox.setVerticalAlignment((short) 6);
        assertEquals(textbox.getVerticalAlignment(), (short) 6);

        textbox.setMarginBottom(7);
        assertEquals(textbox.getMarginBottom(), 7);

        textbox.setMarginLeft(8);
        assertEquals(textbox.getMarginLeft(), 8);

        textbox.setMarginRight(9);
        assertEquals(textbox.getMarginRight(), 9);

        textbox.setMarginTop(10);
        assertEquals(textbox.getMarginTop(), 10);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        sh = wb2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        textbox = (HSSFTextbox) patriarch.getChildren().get(0);
        assertEquals(textbox.getString().getString(), "test");
        assertEquals(textbox.getHorizontalAlignment(), 5);
        assertEquals(textbox.getVerticalAlignment(), (short) 6);
        assertEquals(textbox.getMarginBottom(), 7);
        assertEquals(textbox.getMarginLeft(), 8);
        assertEquals(textbox.getMarginRight(), 9);
        assertEquals(textbox.getMarginTop(), 10);

        textbox.setString(new HSSFRichTextString("test1"));
        textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
        textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
        textbox.setMarginBottom(71);
        textbox.setMarginLeft(81);
        textbox.setMarginRight(91);
        textbox.setMarginTop(101);

        assertEquals(textbox.getString().getString(), "test1");
        assertEquals(textbox.getHorizontalAlignment(), HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
        assertEquals(textbox.getVerticalAlignment(), HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
        assertEquals(textbox.getMarginBottom(), 71);
        assertEquals(textbox.getMarginLeft(), 81);
        assertEquals(textbox.getMarginRight(), 91);
        assertEquals(textbox.getMarginTop(), 101);

        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        wb2.close();
        sh = wb3.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        textbox = (HSSFTextbox) patriarch.getChildren().get(0);

        assertEquals(textbox.getString().getString(), "test1");
        assertEquals(textbox.getHorizontalAlignment(), HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
        assertEquals(textbox.getVerticalAlignment(), HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
        assertEquals(textbox.getMarginBottom(), 71);
        assertEquals(textbox.getMarginLeft(), 81);
        assertEquals(textbox.getMarginRight(), 91);
        assertEquals(textbox.getMarginTop(), 101);
        
        wb3.close();
    }

    @Test
    public void testExistingFileWithText() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("text");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());
        HSSFTextbox textbox = (HSSFTextbox) drawing.getChildren().get(0);
        assertEquals(textbox.getHorizontalAlignment(), HSSFTextbox.HORIZONTAL_ALIGNMENT_LEFT);
        assertEquals(textbox.getVerticalAlignment(), HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
        assertEquals(textbox.getMarginTop(), 0);
        assertEquals(textbox.getMarginBottom(), 3600000);
        assertEquals(textbox.getMarginLeft(), 3600000);
        assertEquals(textbox.getMarginRight(), 0);
        assertEquals(textbox.getString().getString(), "teeeeesssstttt");
        wb.close();
    }
}
