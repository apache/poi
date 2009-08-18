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

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.BaseTestPicture;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.STEditAs;

import java.util.List;
import java.util.Arrays;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFPicture extends BaseTestPicture {

    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }

    public void testResize() {
        baseTestResize(new XSSFClientAnchor(0, 0, 504825, 85725, (short)0, 0, (short)1, 8));
    }


    public void testCreate(){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        byte[] jpegData = "test jpeg data".getBytes();

        List<XSSFPictureData> pictures = wb.getAllPictures();
        assertEquals(0, pictures.size());

        int jpegIdx = wb.addPicture(jpegData, XSSFWorkbook.PICTURE_TYPE_JPEG);
        assertEquals(1, pictures.size());
        assertEquals("jpeg", pictures.get(jpegIdx).suggestFileExtension());
        assertTrue(Arrays.equals(jpegData, pictures.get(jpegIdx).getData()));

        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 1, 1, 10, 30);
        assertEquals(ClientAnchor.MOVE_AND_RESIZE, anchor.getAnchorType());
        anchor.setAnchorType(ClientAnchor.DONT_MOVE_AND_RESIZE);
        assertEquals(ClientAnchor.DONT_MOVE_AND_RESIZE, anchor.getAnchorType());

        XSSFPicture shape = drawing.createPicture(anchor, jpegIdx);
        assertTrue(anchor.equals(shape.getAnchor()));
        assertNotNull(shape.getPictureData());
        assertTrue(Arrays.equals(jpegData, shape.getPictureData().getData()));

        CTTwoCellAnchor ctShapeHolder = drawing.getCTDrawing().getTwoCellAnchorArray(0);
        // STEditAs.ABSOLUTE corresponds to ClientAnchor.DONT_MOVE_AND_RESIZE
        assertEquals(STEditAs.ABSOLUTE, ctShapeHolder.getEditAs());
    }
}
