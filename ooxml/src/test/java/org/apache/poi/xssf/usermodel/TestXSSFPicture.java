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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BaseTestPicture;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.STEditAs;

public final class TestXSSFPicture extends BaseTestPicture {

    public TestXSSFPicture() {
        super(XSSFITestDataProvider.instance);
    }

    protected Picture getPictureShape(Drawing<?> pat, int picIdx) {
        return (Picture)((XSSFDrawing)pat).getShapes().get(picIdx);
    }

    @Test
    void create() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            byte[] jpegData = "test jpeg data".getBytes(LocaleUtil.CHARSET_1252);

            List<XSSFPictureData> pictures = wb.getAllPictures();
            assertEquals(0, pictures.size());

            int jpegIdx = wb.addPicture(jpegData, XSSFWorkbook.PICTURE_TYPE_JPEG);
            assertEquals(1, pictures.size());
            assertEquals("jpeg", pictures.get(jpegIdx).suggestFileExtension());
            assertArrayEquals(jpegData, pictures.get(jpegIdx).getData());

            XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 1, 1, 10, 30);
            assertEquals(AnchorType.MOVE_AND_RESIZE, anchor.getAnchorType());
            anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
            assertEquals(AnchorType.DONT_MOVE_AND_RESIZE, anchor.getAnchorType());

            XSSFPicture shape = drawing.createPicture(anchor, jpegIdx);
            assertEquals(anchor, shape.getAnchor());
            assertNotNull(shape.getPictureData());
            assertArrayEquals(jpegData, shape.getPictureData().getData());

            CTTwoCellAnchor ctShapeHolder = drawing.getCTDrawing().getTwoCellAnchorArray(0);
            // STEditAs.ABSOLUTE corresponds to ClientAnchor.DONT_MOVE_AND_RESIZE
            assertEquals(STEditAs.ABSOLUTE, ctShapeHolder.getEditAs());
        }
    }

    /**
     * test that ShapeId in CTNonVisualDrawingProps is incremented
     *
     * See Bugzilla 50458
     */
    @Test
    void incrementShapeId() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 1, 1, 10, 30);
            byte[] jpegData = "picture1".getBytes(LocaleUtil.CHARSET_1252);
            int jpegIdx = wb.addPicture(jpegData, XSSFWorkbook.PICTURE_TYPE_JPEG);

            XSSFPicture shape1 = drawing.createPicture(anchor, jpegIdx);
            assertEquals(1, shape1.getCTPicture().getNvPicPr().getCNvPr().getId());

            jpegData = "picture2".getBytes(LocaleUtil.CHARSET_1252);
            jpegIdx = wb.addPicture(jpegData, XSSFWorkbook.PICTURE_TYPE_JPEG);
            XSSFPicture shape2 = drawing.createPicture(anchor, jpegIdx);
            assertEquals(2, shape2.getCTPicture().getNvPicPr().getCNvPr().getId());
        }
    }

    /**
     * same image refrerred by mulitple sheets
     */
    @Test
    void multiRelationShips() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            byte[] pic1Data = "test jpeg data".getBytes(LocaleUtil.CHARSET_1252);
            byte[] pic2Data = "test png data".getBytes(LocaleUtil.CHARSET_1252);

            List<XSSFPictureData> pictures = wb1.getAllPictures();
            assertEquals(0, pictures.size());

            int pic1 = wb1.addPicture(pic1Data, XSSFWorkbook.PICTURE_TYPE_JPEG);
            int pic2 = wb1.addPicture(pic2Data, XSSFWorkbook.PICTURE_TYPE_PNG);

            XSSFSheet sheet1 = wb1.createSheet();
            XSSFDrawing drawing1 = sheet1.createDrawingPatriarch();
            XSSFPicture shape1 = drawing1.createPicture(new XSSFClientAnchor(), pic1);
            XSSFPicture shape2 = drawing1.createPicture(new XSSFClientAnchor(), pic2);

            XSSFSheet sheet2 = wb1.createSheet();
            XSSFDrawing drawing2 = sheet2.createDrawingPatriarch();
            XSSFPicture shape3 = drawing2.createPicture(new XSSFClientAnchor(), pic2);
            XSSFPicture shape4 = drawing2.createPicture(new XSSFClientAnchor(), pic1);

            assertEquals(2, pictures.size());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                pictures = wb2.getAllPictures();
                assertEquals(2, pictures.size());

                sheet1 = wb2.getSheetAt(0);
                drawing1 = sheet1.createDrawingPatriarch();
                XSSFPicture shape11 = (XSSFPicture) drawing1.getShapes().get(0);
                assertArrayEquals(shape1.getPictureData().getData(), shape11.getPictureData().getData());
                XSSFPicture shape22 = (XSSFPicture) drawing1.getShapes().get(1);
                assertArrayEquals(shape2.getPictureData().getData(), shape22.getPictureData().getData());

                sheet2 = wb2.getSheetAt(1);
                drawing2 = sheet2.createDrawingPatriarch();
                XSSFPicture shape33 = (XSSFPicture) drawing2.getShapes().get(0);
                assertArrayEquals(shape3.getPictureData().getData(), shape33.getPictureData().getData());
                XSSFPicture shape44 = (XSSFPicture) drawing2.getShapes().get(1);
                assertArrayEquals(shape4.getPictureData().getData(), shape44.getPictureData().getData());
            }
        }
    }
}
