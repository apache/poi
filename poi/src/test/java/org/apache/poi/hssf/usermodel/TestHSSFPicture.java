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

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.ss.usermodel.BaseTestPicture;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

final class TestHSSFPicture extends BaseTestPicture {

    public TestHSSFPicture() {
        super(HSSFITestDataProvider.instance);
    }

    @Override
    protected Picture getPictureShape(Drawing<?> pat, int picIdx) {
        return (Picture)((HSSFPatriarch)pat).getChildren().get(picIdx);
    }

    /**
     * Bug # 45829 reported ArithmeticException (/ by zero) when resizing png with zero DPI.
     */
    @Test
    void bug45829() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sh1 = wb.createSheet();
            HSSFPatriarch p1 = sh1.createDrawingPatriarch();

            byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");
            int idx1 = wb.addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
            HSSFPicture pic = p1.createPicture(new HSSFClientAnchor(), idx1);
            assertDoesNotThrow((Executable) pic::resize);
        }
    }


    @Test
    void addPictures() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {

            HSSFSheet sh = wb1.createSheet("Pictures");
            HSSFPatriarch dr = sh.createDrawingPatriarch();
            assertEquals(0, dr.getChildren().size());
            HSSFClientAnchor anchor = wb1.getCreationHelper().createClientAnchor();

            //register a picture
            byte[] data1 = {1, 2, 3};
            int idx1 = wb1.addPicture(data1, Workbook.PICTURE_TYPE_JPEG);
            assertEquals(1, idx1);
            HSSFPicture p1 = dr.createPicture(anchor, idx1);
            assertArrayEquals(data1, p1.getPictureData().getData());

            // register another one
            byte[] data2 = {4, 5, 6};
            int idx2 = wb1.addPicture(data2, Workbook.PICTURE_TYPE_JPEG);
            assertEquals(2, idx2);
            HSSFPicture p2 = dr.createPicture(anchor, idx2);
            assertEquals(2, dr.getChildren().size());
            assertArrayEquals(data2, p2.getPictureData().getData());

            // confirm that HSSFPatriarch.getChildren() returns two picture shapes
            assertArrayEquals(data1, ((HSSFPicture) dr.getChildren().get(0)).getPictureData().getData());
            assertArrayEquals(data2, ((HSSFPicture) dr.getChildren().get(1)).getPictureData().getData());

            // write, read back and verify that our pictures are there
            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                List<? extends PictureData> lst2 = wb2.getAllPictures();
                assertEquals(2, lst2.size());
                assertArrayEquals(data1, lst2.get(0).getData());
                assertArrayEquals(data2, lst2.get(1).getData());

                // confirm that the pictures are in the Sheet's drawing
                sh = wb2.getSheet("Pictures");
                dr = sh.createDrawingPatriarch();
                assertEquals(2, dr.getChildren().size());
                assertArrayEquals(data1, ((HSSFPicture) dr.getChildren().get(0)).getPictureData().getData());
                assertArrayEquals(data2, ((HSSFPicture) dr.getChildren().get(1)).getPictureData().getData());

                // add a third picture
                byte[] data3 = {7, 8, 9};
                // picture index must increment across write-read
                int idx3 = wb2.addPicture(data3, Workbook.PICTURE_TYPE_JPEG);
                assertEquals(3, idx3);
                HSSFPicture p3 = dr.createPicture(anchor, idx3);
                assertArrayEquals(data3, p3.getPictureData().getData());
                assertEquals(3, dr.getChildren().size());
                assertArrayEquals(data1, ((HSSFPicture) dr.getChildren().get(0)).getPictureData().getData());
                assertArrayEquals(data2, ((HSSFPicture) dr.getChildren().get(1)).getPictureData().getData());
                assertArrayEquals(data3, ((HSSFPicture) dr.getChildren().get(2)).getPictureData().getData());

                // write and read again
                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    List<? extends PictureData> lst3 = wb3.getAllPictures();
                    // all three should be there
                    assertEquals(3, lst3.size());
                    assertArrayEquals(data1, lst3.get(0).getData());
                    assertArrayEquals(data2, lst3.get(1).getData());
                    assertArrayEquals(data3, lst3.get(2).getData());

                    sh = wb3.getSheet("Pictures");
                    dr = sh.createDrawingPatriarch();
                    assertEquals(3, dr.getChildren().size());

                    // forth picture
                    byte[] data4 = {10, 11, 12};
                    int idx4 = wb3.addPicture(data4, Workbook.PICTURE_TYPE_JPEG);
                    assertEquals(4, idx4);
                    dr.createPicture(anchor, idx4);
                    assertEquals(4, dr.getChildren().size());
                    assertArrayEquals(data1, ((HSSFPicture) dr.getChildren().get(0)).getPictureData().getData());
                    assertArrayEquals(data2, ((HSSFPicture) dr.getChildren().get(1)).getPictureData().getData());
                    assertArrayEquals(data3, ((HSSFPicture) dr.getChildren().get(2)).getPictureData().getData());
                    assertArrayEquals(data4, ((HSSFPicture) dr.getChildren().get(3)).getPictureData().getData());

                    try (HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb3)) {
                        List<? extends PictureData> lst4 = wb4.getAllPictures();
                        assertEquals(4, lst4.size());
                        assertArrayEquals(data1, lst4.get(0).getData());
                        assertArrayEquals(data2, lst4.get(1).getData());
                        assertArrayEquals(data3, lst4.get(2).getData());
                        assertArrayEquals(data4, lst4.get(3).getData());
                        sh = wb4.getSheet("Pictures");
                        dr = sh.createDrawingPatriarch();
                        assertEquals(4, dr.getChildren().size());
                        assertArrayEquals(data1, ((HSSFPicture) dr.getChildren().get(0)).getPictureData().getData());
                        assertArrayEquals(data2, ((HSSFPicture) dr.getChildren().get(1)).getPictureData().getData());
                        assertArrayEquals(data3, ((HSSFPicture) dr.getChildren().get(2)).getPictureData().getData());
                        assertArrayEquals(data4, ((HSSFPicture) dr.getChildren().get(3)).getPictureData().getData());
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Test
    void bsePictureRef() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sh = wb.createSheet("Pictures");
            HSSFPatriarch dr = sh.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor();

            InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);

            //register a picture
            byte[] data1 = {1, 2, 3};
            int idx1 = wb.addPicture(data1, Workbook.PICTURE_TYPE_JPEG);
            assertEquals(1, idx1);
            HSSFPicture p1 = dr.createPicture(anchor, idx1);

            EscherBSERecord bse = wb.getWorkbook().getBSERecord(idx1);

            assertEquals(1, bse.getRef());
            dr.createPicture(new HSSFClientAnchor(), idx1);
            assertEquals(2, bse.getRef());

            HSSFShapeGroup gr = dr.createGroup(new HSSFClientAnchor());
            gr.createPicture(new HSSFChildAnchor(), idx1);
            assertEquals(3, bse.getRef());
        }
    }

    @Test
    void readExistingImage() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("picture");
            HSSFPatriarch drawing = sheet.getDrawingPatriarch();
            assertEquals(1, drawing.getChildren().size());

            HSSFPicture picture = (HSSFPicture) drawing.getChildren().get(0);
            assertEquals("test", picture.getFileName());
        }
    }

    @SuppressWarnings("resource")
    @Test
    void setGetProperties() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {

            HSSFSheet sh = wb1.createSheet("Pictures");
            HSSFPatriarch dr = sh.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor();

            //register a picture
            byte[] data1 = {1, 2, 3};
            int idx1 = wb1.addPicture(data1, Workbook.PICTURE_TYPE_JPEG);
            HSSFPicture p1 = dr.createPicture(anchor, idx1);

            assertEquals("", p1.getFileName());
            p1.setFileName("aaa");
            assertEquals("aaa", p1.getFileName());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheet("Pictures");
                dr = sh.getDrawingPatriarch();

                p1 = (HSSFPicture) dr.getChildren().get(0);
                assertEquals("aaa", p1.getFileName());
            }
        }
    }

    @SuppressWarnings("resource")
    @Test
    void bug49658() throws IOException {
        // test if inserted EscherMetafileBlip will be read again
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {

            byte[] pictureDataEmf = POIDataSamples.getDocumentInstance().readFile("vector_image.emf");
            int indexEmf = wb1.addPicture(pictureDataEmf, HSSFWorkbook.PICTURE_TYPE_EMF);
            byte[] pictureDataPng = POIDataSamples.getSpreadSheetInstance().readFile("logoKarmokar4.png");
            int indexPng = wb1.addPicture(pictureDataPng, HSSFWorkbook.PICTURE_TYPE_PNG);
            byte[] pictureDataWmf = POIDataSamples.getSlideShowInstance().readFile("santa.wmf");
            int indexWmf = wb1.addPicture(pictureDataWmf, HSSFWorkbook.PICTURE_TYPE_WMF);

            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            CreationHelper ch = wb1.getCreationHelper();

            ClientAnchor anchor = ch.createClientAnchor();
            anchor.setCol1(2);
            anchor.setCol2(5);
            anchor.setRow1(1);
            anchor.setRow2(6);
            patriarch.createPicture(anchor, indexEmf);

            anchor = ch.createClientAnchor();
            anchor.setCol1(2);
            anchor.setCol2(5);
            anchor.setRow1(10);
            anchor.setRow2(16);
            patriarch.createPicture(anchor, indexPng);

            anchor = ch.createClientAnchor();
            anchor.setCol1(6);
            anchor.setCol2(9);
            anchor.setRow1(1);
            anchor.setRow2(6);
            patriarch.createPicture(anchor, indexWmf);


            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                byte[] pictureDataOut = wb2.getAllPictures().get(0).getData();
                assertArrayEquals(pictureDataEmf, pictureDataOut);

                byte[] wmfNoHeader = Arrays.copyOfRange(pictureDataWmf, 22, pictureDataWmf.length);
                pictureDataOut = wb2.getAllPictures().get(2).getData();
                assertArrayEquals(wmfNoHeader, pictureDataOut);
            }
        }
    }

}
