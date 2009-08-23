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

import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.ObjectData;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.POIDataSamples;

public final class TestOleEmbedding extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    /**
     * Tests support for OLE objects.
     *
     * @throws Exception if an error occurs.
     */
    public void testOleEmbedding2003() throws Exception {
        HSLFSlideShow slideShow = new HSLFSlideShow(_slTests.openResourceAsStream("ole2-embedding-2003.ppt"));
        // Placeholder EMFs for clients that don't support the OLE components.
        PictureData[] pictures = slideShow.getPictures();
        assertEquals("Should be two pictures", 2, pictures.length);
        //assertDigestEquals("Wrong data for picture 1", "8d1fbadf4814f321bb1ccdd056e3c788", pictures[0].getData());
        //assertDigestEquals("Wrong data for picture 2", "987a698e83559cf3d38a0deeba1cc63b", pictures[1].getData());

        // Actual embedded objects.
        ObjectData[] objects = slideShow.getEmbeddedObjects();
        assertEquals("Should be two objects", 2, objects.length);
        //assertDigestEquals("Wrong data for objecs 1", "0d1fcc61a83de5c4894dc0c88e9a019d", objects[0].getData());
        //assertDigestEquals("Wrong data for object 2", "b323604b2003a7299c77c2693b641495", objects[1].getData());
    }

    public void testOLEShape() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("ole2-embedding-2003.ppt"));

        Slide slide = ppt.getSlides()[0];
        Shape[] sh = slide.getShapes();
        int cnt = 0;
        for (int i = 0; i < sh.length; i++) {
            if(sh[i] instanceof OLEShape){
                cnt++;
                OLEShape ole = (OLEShape)sh[i];
                ObjectData data = ole.getObjectData();
                if("Worksheet".equals(ole.getInstanceName())){
                    //Voila! we created a workbook from the embedded OLE data
                    HSSFWorkbook wb = new HSSFWorkbook(data.getData());
                    HSSFSheet sheet = wb.getSheetAt(0);
                    //verify we can access the xls data
                    assertEquals(1, sheet.getRow(0).getCell(0).getNumericCellValue(), 0);
                    assertEquals(1, sheet.getRow(1).getCell(0).getNumericCellValue(), 0);
                    assertEquals(2, sheet.getRow(2).getCell(0).getNumericCellValue(), 0);
                    assertEquals(3, sheet.getRow(3).getCell(0).getNumericCellValue(), 0);
                    assertEquals(8, sheet.getRow(5).getCell(0).getNumericCellValue(), 0);
                } else if ("Document".equals(ole.getInstanceName())){
                    //creating a HWPF document
                    HWPFDocument doc = new HWPFDocument(data.getData());
                    String txt = doc.getRange().getParagraph(0).text();
                    assertEquals("OLE embedding is thoroughly unremarkable.\r", txt);
                }
            }

        }
        assertEquals("Expected 2 OLE shapes", 2, cnt);
    }
}
