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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.extractor.EmbeddedData;
import org.apache.poi.ss.extractor.EmbeddedExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEmbedOLEPackage {
    private static byte[] samplePPT, samplePPTX, samplePNG;
    
    @BeforeClass
    public static void init() throws IOException {
        samplePPT = getSamplePPT(false);
        samplePPTX = getSamplePPT(true);
        samplePNG = POIDataSamples.getSpreadSheetInstance().readFile("logoKarmokar4.png");
    }
    
    @Test
    public void embedXSSF() throws IOException {
        Workbook wb1 = new XSSFWorkbook();
        addEmbeddedObjects(wb1);

        Workbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        validateEmbeddedObjects(wb2);

        wb2.close();
        wb1.close();
    }

    @Test
    public void embedHSSF() throws IOException {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
        } catch (Exception e) {
            assumeTrue(false);
        }

        Workbook wb1 = new HSSFWorkbook();
        addEmbeddedObjects(wb1);
        Workbook wb2 = HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)wb1);
        validateEmbeddedObjects(wb2);

        wb2.close();
        wb1.close();
    }

    static void validateEmbeddedObjects(Workbook wb) throws IOException {
        boolean ooxml = wb.getClass().getName().toLowerCase(Locale.ROOT).contains("xssf");
        byte data[] = (ooxml) ? samplePPTX : samplePPT;
        Iterator<Integer> shapeIds = Arrays.asList(1025,1026,2049).iterator();
        EmbeddedExtractor ee = new EmbeddedExtractor();
        for (Sheet sheet : wb) {
            Drawing<? extends Shape> pat = sheet.getDrawingPatriarch();
            for (Shape shape : pat) {
                assertTrue(shape instanceof ObjectData);
                ObjectData od = (ObjectData)shape;
                EmbeddedData ed = ee.extractOne((DirectoryNode)od.getDirectory());
                assertArrayEquals(data, ed.getEmbeddedData());
                assertArrayEquals(samplePNG, od.getPictureData().getData());
                assertEquals((int)shapeIds.next(), od.getShapeId());
            }
        }
    }
    
    static void addEmbeddedObjects(Workbook wb) throws IOException {
        boolean ooxml = wb.getClass().getName().toLowerCase(Locale.ROOT).contains("xssf");
        int picIdx = wb.addPicture(samplePNG, Workbook.PICTURE_TYPE_PNG);
        byte data[] = (ooxml) ? samplePPTX : samplePPT;
        String ext = (ooxml) ? ".pptx" : ".ppt";
        
        int oleIdx1a = wb.addOlePackage(data, "dummy1a"+ext, "dummy1a"+ext, "dummy1a"+ext);
        int oleIdx1b = wb.addOlePackage(data, "dummy1b"+ext, "dummy1b"+ext, "dummy1b"+ext);
        int oleIdx2 = wb.addOlePackage(data, "dummy2"+ext, "dummy2"+ext, "dummy2"+ext);
        
        Sheet sh1 = wb.createSheet();
        Drawing<?> pat1 = sh1.createDrawingPatriarch();
        ClientAnchor anchor1a = pat1.createAnchor(0, 0, 0, 0, 1, 1, 3, 6);
        pat1.createObjectData(anchor1a, oleIdx1a, picIdx);
        ClientAnchor anchor1b = pat1.createAnchor(0, 0, 0, 0, 1, 1+7, 3, 6+7);
        pat1.createObjectData(anchor1b, oleIdx1b, picIdx);

        Sheet sh2 = wb.createSheet();
        Drawing<?> pat2 = sh2.createDrawingPatriarch();
        ClientAnchor anchor2 = pat2.createAnchor(0, 0, 0, 0, 1, 1, 3, 6);
        pat2.createObjectData(anchor2, oleIdx2, picIdx);
    }
    
    static byte[] getSamplePPT(boolean ooxml) throws IOException {
        SlideShow<?,?> ppt = (ooxml) ? new XMLSlideShow() : new org.apache.poi.hslf.usermodel.HSLFSlideShow();
        Slide<?,?> slide = ppt.createSlide();

        AutoShape<?,?> sh1 = slide.createAutoShape();
        sh1.setShapeType(ShapeType.STAR_32);
        sh1.setAnchor(new java.awt.Rectangle(50, 50, 100, 200));
        sh1.setFillColor(java.awt.Color.red);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ppt.write(bos);
        ppt.close();

        return bos.toByteArray();
    }
}
