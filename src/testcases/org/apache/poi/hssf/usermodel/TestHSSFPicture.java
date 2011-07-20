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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestPicture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Arrays;
import java.util.List;

/**
 * Test <code>HSSFPicture</code>.
 *
 * @author Yegor Kozlov (yegor at apache.org)
 */
public final class TestHSSFPicture extends BaseTestPicture {

    public TestHSSFPicture() {
        super(HSSFITestDataProvider.instance);
    }

    public void testResize() {
        baseTestResize(new HSSFClientAnchor(0, 0, 848, 240, (short)0, 0, (short)1, 9));
    }

    /**
     * Bug # 45829 reported ArithmeticException (/ by zero) when resizing png with zero DPI.
     */
    public void test45829() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh1 = wb.createSheet();
        HSSFPatriarch p1 = sh1.createDrawingPatriarch();

        byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");
        int idx1 = wb.addPicture( pictureData, HSSFWorkbook.PICTURE_TYPE_PNG );
        HSSFPicture pic = p1.createPicture(new HSSFClientAnchor(), idx1);
        pic.resize();
    }


    public void testAddPictures(){
        HSSFWorkbook wb = new HSSFWorkbook();
        
        HSSFSheet sh = wb.createSheet("Pictures");
        HSSFPatriarch dr = sh.createDrawingPatriarch();
        assertEquals(0, dr.getChildren().size());
        HSSFClientAnchor anchor = wb.getCreationHelper().createClientAnchor();

        //register a picture
        byte[] data1 = new byte[]{1, 2, 3};
        int idx1 = wb.addPicture(data1, Workbook.PICTURE_TYPE_JPEG);
        assertEquals(1, idx1);
        HSSFPicture p1 = dr.createPicture(anchor, idx1);
        assertTrue(Arrays.equals(data1, p1.getPictureData().getData()));

        // register another one
        byte[] data2 = new byte[]{4, 5, 6};
        int idx2 = wb.addPicture(data2, Workbook.PICTURE_TYPE_JPEG);
        assertEquals(2, idx2);
        HSSFPicture p2 = dr.createPicture(anchor, idx2);
        assertEquals(2, dr.getChildren().size());
        assertTrue(Arrays.equals(data2, p2.getPictureData().getData()));

        // confirm that HSSFPatriarch.getChildren() returns two picture shapes 
        assertTrue(Arrays.equals(data1, ((HSSFPicture)dr.getChildren().get(0)).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((HSSFPicture)dr.getChildren().get(1)).getPictureData().getData()));

        // write, read back and verify that our pictures are there
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        List<? extends PictureData> lst2 =  wb.getAllPictures();
        assertEquals(2, lst2.size());
        assertTrue(Arrays.equals(data1, lst2.get(0).getData()));
        assertTrue(Arrays.equals(data2, lst2.get(1).getData()));

        // confirm that the pictures are in the Sheet's drawing
        sh = wb.getSheet("Pictures");
        dr = sh.createDrawingPatriarch();
        assertEquals(2, dr.getChildren().size());
        assertTrue(Arrays.equals(data1, ((HSSFPicture)dr.getChildren().get(0)).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((HSSFPicture)dr.getChildren().get(1)).getPictureData().getData()));

        // add a third picture
        byte[] data3 = new byte[]{7, 8, 9};
        // picture index must increment across write-read
        int idx3 = wb.addPicture(data3, Workbook.PICTURE_TYPE_JPEG);
        assertEquals(3, idx3);
        HSSFPicture p3 = dr.createPicture(anchor, idx3);
        assertTrue(Arrays.equals(data3, p3.getPictureData().getData()));
        assertEquals(3, dr.getChildren().size());
        assertTrue(Arrays.equals(data1, ((HSSFPicture)dr.getChildren().get(0)).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((HSSFPicture)dr.getChildren().get(1)).getPictureData().getData()));
        assertTrue(Arrays.equals(data3, ((HSSFPicture)dr.getChildren().get(2)).getPictureData().getData()));

        // write and read again
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        List<? extends PictureData> lst3 =  wb.getAllPictures();
        // all three should be there
        assertEquals(3, lst3.size());
        assertTrue(Arrays.equals(data1, lst3.get(0).getData()));
        assertTrue(Arrays.equals(data2, lst3.get(1).getData()));
        assertTrue(Arrays.equals(data3, lst3.get(2).getData()));

        sh = wb.getSheet("Pictures");
        dr = sh.createDrawingPatriarch();
        assertEquals(3, dr.getChildren().size());

        // forth picture
        byte[] data4 = new byte[]{10, 11, 12};
        int idx4 = wb.addPicture(data4, Workbook.PICTURE_TYPE_JPEG);
        assertEquals(4, idx4);
        dr.createPicture(anchor, idx4);
        assertEquals(4, dr.getChildren().size());
        assertTrue(Arrays.equals(data1, ((HSSFPicture)dr.getChildren().get(0)).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((HSSFPicture)dr.getChildren().get(1)).getPictureData().getData()));
        assertTrue(Arrays.equals(data3, ((HSSFPicture)dr.getChildren().get(2)).getPictureData().getData()));
        assertTrue(Arrays.equals(data4, ((HSSFPicture)dr.getChildren().get(3)).getPictureData().getData()));

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        List<? extends PictureData> lst4 =  wb.getAllPictures();
        assertEquals(4, lst4.size());
        assertTrue(Arrays.equals(data1, lst4.get(0).getData()));
        assertTrue(Arrays.equals(data2, lst4.get(1).getData()));
        assertTrue(Arrays.equals(data3, lst4.get(2).getData()));
        assertTrue(Arrays.equals(data4, lst4.get(3).getData()));
        sh = wb.getSheet("Pictures");
        dr = sh.createDrawingPatriarch();
        assertEquals(4, dr.getChildren().size());
        assertTrue(Arrays.equals(data1, ((HSSFPicture)dr.getChildren().get(0)).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((HSSFPicture)dr.getChildren().get(1)).getPictureData().getData()));
        assertTrue(Arrays.equals(data3, ((HSSFPicture)dr.getChildren().get(2)).getPictureData().getData()));
        assertTrue(Arrays.equals(data4, ((HSSFPicture)dr.getChildren().get(3)).getPictureData().getData()));
    }

}
