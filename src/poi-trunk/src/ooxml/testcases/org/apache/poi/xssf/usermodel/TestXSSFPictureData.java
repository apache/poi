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

package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.List;

import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFPictureData {
    @Test
    public void testRead() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithDrawing.xlsx");
        List<XSSFPictureData> pictures = wb.getAllPictures();
        //wb.getAllPictures() should return the same instance across multiple calls
        assertSame(pictures, wb.getAllPictures());

        assertEquals(5, pictures.size());
        String[] ext = {"jpeg", "emf", "png", "emf", "wmf"};
        String[] mimetype = {"image/jpeg", "image/x-emf", "image/png", "image/x-emf", "image/x-wmf"};
        for (int i = 0; i < pictures.size(); i++) {
            assertEquals(ext[i], pictures.get(i).suggestFileExtension());
            assertEquals(mimetype[i], pictures.get(i).getMimeType());
        }

        int num = pictures.size();

        byte[] pictureData = {0xA, 0xB, 0XC, 0xD, 0xE, 0xF};

        int idx = wb.addPicture(pictureData, XSSFWorkbook.PICTURE_TYPE_JPEG);
        assertEquals(num + 1, pictures.size());
        //idx is 0-based index in the #pictures array
        assertEquals(pictures.size() - 1, idx);
        XSSFPictureData pict = pictures.get(idx);
        assertEquals("jpeg", pict.suggestFileExtension());
        assertArrayEquals(pictureData, pict.getData());
        wb.close();
    }

    @Test
    public void testNew() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        byte[] jpegData = "test jpeg data".getBytes(LocaleUtil.CHARSET_1252);
        byte[] wmfData =  "test wmf data".getBytes(LocaleUtil.CHARSET_1252);
        byte[] pngData =  "test png data".getBytes(LocaleUtil.CHARSET_1252);

        List<XSSFPictureData> pictures = wb.getAllPictures();
        assertEquals(0, pictures.size());

        int jpegIdx = wb.addPicture(jpegData, XSSFWorkbook.PICTURE_TYPE_JPEG);
        assertEquals(1, pictures.size());
        assertEquals("jpeg", pictures.get(jpegIdx).suggestFileExtension());
        assertArrayEquals(jpegData, pictures.get(jpegIdx).getData());

        int wmfIdx = wb.addPicture(wmfData, XSSFWorkbook.PICTURE_TYPE_WMF);
        assertEquals(2, pictures.size());
        assertEquals("wmf", pictures.get(wmfIdx).suggestFileExtension());
        assertArrayEquals(wmfData, pictures.get(wmfIdx).getData());

        int pngIdx = wb.addPicture(pngData, XSSFWorkbook.PICTURE_TYPE_PNG);
        assertEquals(3, pictures.size());
        assertEquals("png", pictures.get(pngIdx).suggestFileExtension());
        assertArrayEquals(pngData, pictures.get(pngIdx).getData());

        //TODO finish usermodel API for XSSFPicture
        XSSFPicture p1 = drawing.createPicture(new XSSFClientAnchor(), jpegIdx);
        assertNotNull(p1);
        XSSFPicture p2 = drawing.createPicture(new XSSFClientAnchor(), wmfIdx);
        assertNotNull(p2);
        XSSFPicture p3 = drawing.createPicture(new XSSFClientAnchor(), pngIdx);
        assertNotNull(p3);

        //check that the added pictures are accessible after write
        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        List<XSSFPictureData> pictures2 = wbBack.getAllPictures();
        assertEquals(3, pictures2.size());

        assertEquals("jpeg", pictures2.get(jpegIdx).suggestFileExtension());
        assertArrayEquals(jpegData, pictures2.get(jpegIdx).getData());

        assertEquals("wmf", pictures2.get(wmfIdx).suggestFileExtension());
        assertArrayEquals(wmfData, pictures2.get(wmfIdx).getData());

        assertEquals("png", pictures2.get(pngIdx).suggestFileExtension());
        assertArrayEquals(pngData, pictures2.get(pngIdx).getData());
        wbBack.close();
        wb.close();
    }

    /**
     * Bug 53568:  XSSFPicture.getPictureData() can return null.
     */
    @Test
    public void test53568() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53568.xlsx");
        List<XSSFPictureData> pictures = wb.getAllPictures();
        assertNotNull(pictures);
        assertEquals(4, pictures.size());

        XSSFSheet sheet1 = wb.getSheetAt(0);
        List<XSSFShape> shapes1 = sheet1.createDrawingPatriarch().getShapes();
        assertNotNull(shapes1);
        assertEquals(5, shapes1.size());
        
        for(int i = 0; i < wb.getNumberOfSheets(); i++){
            XSSFSheet sheet = wb.getSheetAt(i);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            for(XSSFShape shape : drawing.getShapes()){
                if(shape instanceof XSSFPicture){
                    XSSFPicture pic = (XSSFPicture)shape;
                    XSSFPictureData picData = pic.getPictureData();
                    assertNotNull(picData);
                }
            }
        }
        wb.close();
    }
}
