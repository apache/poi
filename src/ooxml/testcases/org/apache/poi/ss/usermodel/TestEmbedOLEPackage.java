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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.extractor.EmbeddedData;
import org.apache.poi.ss.extractor.EmbeddedExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestEmbedOLEPackage {
    @Test
    public void embedXSSF() throws IOException {
        Workbook wb1 = new XSSFWorkbook();
        Sheet sh = wb1.createSheet();
        int picIdx = wb1.addPicture(getSamplePng(), Workbook.PICTURE_TYPE_PNG);
        byte samplePPTX[] = getSamplePPT(true);
        int oleIdx = wb1.addOlePackage(samplePPTX, "dummy.pptx", "dummy.pptx", "dummy.pptx");

        Drawing<?> pat = sh.createDrawingPatriarch();
        ClientAnchor anchor = pat.createAnchor(0, 0, 0, 0, 1, 1, 3, 6);
        pat.createObjectData(anchor, oleIdx, picIdx);

        Workbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);

        pat = wb2.getSheetAt(0).getDrawingPatriarch();
        assertTrue(pat.iterator().next() instanceof ObjectData);

        EmbeddedExtractor ee = new EmbeddedExtractor();
        EmbeddedData ed = ee.extractAll(wb2.getSheetAt(0)).get(0);
        assertArrayEquals(samplePPTX, ed.getEmbeddedData());

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
        Sheet sh = wb1.createSheet();
        int picIdx = wb1.addPicture(getSamplePng(), Workbook.PICTURE_TYPE_PNG);
        byte samplePPT[] = getSamplePPT(false);
        int oleIdx = wb1.addOlePackage(samplePPT, "dummy.ppt", "dummy.ppt", "dummy.ppt");

        Drawing<?> pat = sh.createDrawingPatriarch();
        ClientAnchor anchor = pat.createAnchor(0, 0, 0, 0, 1, 1, 3, 6);
        pat.createObjectData(anchor, oleIdx, picIdx);

        Workbook wb2 = HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)wb1);

        pat = wb2.getSheetAt(0).getDrawingPatriarch();
        assertTrue(pat.iterator().next() instanceof ObjectData);

        EmbeddedExtractor ee = new EmbeddedExtractor();
        EmbeddedData ed = ee.extractAll(wb2.getSheetAt(0)).get(0);
        assertArrayEquals(samplePPT, ed.getEmbeddedData());

        wb2.close();
        wb1.close();
    }

    static byte[] getSamplePng() {
        return POIDataSamples.getSpreadSheetInstance().readFile("logoKarmokar4.png");
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
