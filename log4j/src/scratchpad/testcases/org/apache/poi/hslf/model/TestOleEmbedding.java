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

package org.apache.poi.hslf.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFObjectData;
import org.apache.poi.hslf.usermodel.HSLFObjectShape;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

/** Tests support for OLE objects. */
public final class TestOleEmbedding {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testOleEmbedding2003() throws IOException {
        HSLFSlideShowImpl slideShow = new HSLFSlideShowImpl(_slTests.openResourceAsStream("ole2-embedding-2003.ppt"));
        // Placeholder EMFs for clients that don't support the OLE components.
        List<HSLFPictureData> pictures = slideShow.getPictureData();
        assertEquals(2, pictures.size(), "Should be two pictures");

        long[] checkSums = {0xD37A4204L, 0x26A62F68L, 0x82853169L, 0xE0E45D2BL};
        int checkId = 0;

        // check for checksum to be uptodate
        for (HSLFPictureData pd : pictures) {
            long checkEMF = IOUtils.calculateChecksum(pd.getData());
            assertEquals(checkSums[checkId++], checkEMF);
        }

        // Actual embedded objects.
        HSLFObjectData[] objects = slideShow.getEmbeddedObjects();
        assertEquals(2, objects.length, "Should be two objects");
        for (HSLFObjectData od : objects) {
            long checkEMF = IOUtils.calculateChecksum(od.getInputStream());
            assertEquals(checkSums[checkId++], checkEMF);
        }

        slideShow.close();
    }



    @Test
    void testOLEShape() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("ole2-embedding-2003.ppt"));

        HSLFSlide slide = ppt.getSlides().get(0);
        int cnt = 0;
        for (HSLFShape sh : slide.getShapes()) {
            if(sh instanceof HSLFObjectShape){
                cnt++;
                HSLFObjectShape ole = (HSLFObjectShape)sh;
                HSLFObjectData data = ole.getObjectData();
                if("Worksheet".equals(ole.getInstanceName())){
                    //Voila! we created a workbook from the embedded OLE data
                    HSSFWorkbook wb = new HSSFWorkbook(data.getInputStream());
                    HSSFSheet sheet = wb.getSheetAt(0);
                    //verify we can access the xls data
                    assertEquals(1, sheet.getRow(0).getCell(0).getNumericCellValue(), 0);
                    assertEquals(1, sheet.getRow(1).getCell(0).getNumericCellValue(), 0);
                    assertEquals(2, sheet.getRow(2).getCell(0).getNumericCellValue(), 0);
                    assertEquals(3, sheet.getRow(3).getCell(0).getNumericCellValue(), 0);
                    assertEquals(8, sheet.getRow(5).getCell(0).getNumericCellValue(), 0);
                    wb.close();
                } else if ("Document".equals(ole.getInstanceName())){
                    //creating a HWPF document
                    HWPFDocument doc = new HWPFDocument(data.getInputStream());
                    String txt = doc.getRange().getParagraph(0).text();
                    assertEquals("OLE embedding is thoroughly unremarkable.\r", txt);
                    doc.close();
                }
            }

        }
        assertEquals(2, cnt, "Expected 2 OLE shapes");
        ppt.close();
    }

    @Test
    void testEmbedding() throws IOException {
    	HSLFSlideShow ppt = new HSLFSlideShow();

    	File pict = POIDataSamples.getSlideShowInstance().getFile("clock.jpg");
    	HSLFPictureData pictData = ppt.addPicture(pict, PictureType.JPEG);

    	InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("Employee.xls");
    	POIFSFileSystem poiData1 = new POIFSFileSystem(is);
    	is.close();

    	int oleObjectId1 = ppt.addEmbed(poiData1);

    	HSLFSlide slide1 = ppt.createSlide();
    	HSLFObjectShape oleShape1 = new HSLFObjectShape(pictData);
    	oleShape1.setObjectID(oleObjectId1);
    	slide1.addShape(oleShape1);
    	oleShape1.setAnchor(new Rectangle2D.Double(100,100,100,100));

    	// add second slide with different order in object creation
    	HSLFSlide slide2 = ppt.createSlide();
    	HSLFObjectShape oleShape2 = new HSLFObjectShape(pictData);

        is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleWithImages.xls");
        POIFSFileSystem poiData2 = new POIFSFileSystem(is);
        is.close();

        int oleObjectId2 = ppt.addEmbed(poiData2);

        oleShape2.setObjectID(oleObjectId2);
        slide2.addShape(oleShape2);
        oleShape2.setAnchor(new Rectangle2D.Double(100,100,100,100));

    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ppt.write(bos);

    	ppt = new HSLFSlideShow(new ByteArrayInputStream(bos.toByteArray()));
    	HSLFObjectShape comp = (HSLFObjectShape)ppt.getSlides().get(0).getShapes().get(0);
        byte[] compData = IOUtils.toByteArray(comp.getObjectData().getInputStream());

    	bos.reset();
    	poiData1.writeFilesystem(bos);
        byte[] expData = bos.toByteArray();

    	assertArrayEquals(expData, compData);

    	poiData1.close();
    	poiData2.close();
    	ppt.close();
    }


}
