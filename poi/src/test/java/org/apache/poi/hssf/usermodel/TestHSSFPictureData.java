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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test <code>HSSFPictureData</code>.
 * The code to retrieve images from a workbook provided by Trejkaz (trejkaz at trypticon dot org) in Bug 41223.
 */
final class TestHSSFPictureData {
    @BeforeAll
    public static void setUpClass() {
        final String tmpDirProperty = System.getProperty("java.io.tmpdir");
        if(tmpDirProperty == null || "".equals(tmpDirProperty)) {
            return;
        }
        // ensure that temp-dir exists because ImageIO requires it
        final File tmpDir = new File(tmpDirProperty);
        if(!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IllegalStateException("Could not create temporary directory " + tmpDirProperty + ", full path " + tmpDir.getAbsolutePath());
        }
        ImageIO.setCacheDirectory(tmpDir);
    }

    @Test
	void testPictures() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithImages.xls");

        // TODO - add getFormat() to interface PictureData and genericise wb.getAllPictures()
        List<HSSFPictureData> lst = wb.getAllPictures();
        //assertEquals(2, lst.size());

        try {
            for (final HSSFPictureData pict : lst) {
                String ext = pict.suggestFileExtension();
                byte[] data = pict.getData();
                if (ext.equals("jpeg")) {
                    //try to read image data using javax.imageio.* (JDK 1.4+)
                    BufferedImage jpg = ImageIO.read(new ByteArrayInputStream(data));
                    assertNotNull(jpg);
                    assertEquals(192, jpg.getWidth());
                    assertEquals(176, jpg.getHeight());
                    assertEquals(HSSFWorkbook.PICTURE_TYPE_JPEG, pict.getFormat());
                    assertEquals("image/jpeg", pict.getMimeType());
                } else if (ext.equals("png")) {
                    //try to read image data using javax.imageio.* (JDK 1.4+)
                    BufferedImage png = ImageIO.read(new ByteArrayInputStream(data));
                    assertNotNull(png);
                    assertEquals(300, png.getWidth());
                    assertEquals(300, png.getHeight());
                    assertEquals(HSSFWorkbook.PICTURE_TYPE_PNG, pict.getFormat());
                    assertEquals("image/png", pict.getMimeType());
            /*} else {
                //TODO: test code for PICT, WMF and EMF*/
                }
            }
        } catch (IIOException e) {
            assertFalse(e.getMessage().contains("Can't create cache file"), e.getMessage());
        }
    }

    @Test
	void testMacPicture() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("53446.xls");

        try{
            List<HSSFPictureData> lst = wb.getAllPictures();
            assertEquals(1, lst.size());

            HSSFPictureData pict = lst.get(0);
            String ext = pict.suggestFileExtension();
            assertEquals("png", ext, "Expected a PNG.");

            //try to read image data using javax.imageio.* (JDK 1.4+)
            byte[] data = pict.getData();
            BufferedImage png = ImageIO.read(new ByteArrayInputStream(data));
            assertNotNull(png);
            assertEquals(78, png.getWidth());
            assertEquals(76, png.getHeight());
            assertEquals(HSSFWorkbook.PICTURE_TYPE_PNG, pict.getFormat());
            assertEquals("image/png", pict.getMimeType());
        } catch (IIOException e) {
            assertFalse(e.getMessage().contains("Can't create cache file"), e.getMessage());
        }
    }

    @Test
    void testNotNullPictures() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SheetWithDrawing.xls");

        // TODO - add getFormat() to interface PictureData and genericise wb.getAllPictures()
        List<HSSFPictureData> lst = wb.getAllPictures();
        for(HSSFPictureData pict : lst){
            assertNotNull(pict);
        }
    }
}
