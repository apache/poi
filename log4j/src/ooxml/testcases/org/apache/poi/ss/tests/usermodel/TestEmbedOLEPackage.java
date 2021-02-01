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

package org.apache.poi.ss.tests.usermodel;

import static org.apache.poi.sl.tests.SLCommonUtils.xslfOnly;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.Ole10NativeException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.extractor.EmbeddedData;
import org.apache.poi.ss.extractor.EmbeddedExtractor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ObjectData;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFObjectData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestEmbedOLEPackage {
    private static byte[] samplePPT, samplePPTX, samplePNG;

    private static final POIDataSamples ssamples = POIDataSamples.getSpreadSheetInstance();

    @BeforeAll
    public static void init() throws IOException {
        samplePPT = getSamplePPT(false);
        samplePPTX = getSamplePPT(true);
        samplePNG = ssamples.readFile("logoKarmokar4.png");
    }

    @Test
    void embedPDF() throws IOException {
        try (InputStream is = ssamples.openResourceAsStream("bug64512_embed.xlsx");
            XSSFWorkbook wb = new XSSFWorkbook(is)) {
            List<XSSFObjectData> oleShapes = new ArrayList<>();
            List<Ole10Native> ole10s = new ArrayList<>();

            final boolean digestMatch =
                wb.getSheetAt(0).getDrawingPatriarch().getShapes().stream()
                .map(s -> (XSSFObjectData)s)
                .filter(oleShapes::add)
                .map(TestEmbedOLEPackage::extractOle10Native)
                .filter(ole10s::add)
                .map(TestEmbedOLEPackage::digest)
                .allMatch("FUJBVHTAZ0ly/TNDNmEj1gQ4a2TbZwDMVF4WUkDQLaM="::equals);

            assertEquals(2, oleShapes.size());
            assertEquals("Package", oleShapes.get(0).getOLE2ClassName());
            assertEquals("Package2", oleShapes.get(1).getOLE2ClassName());
            assertTrue(digestMatch);

            final String expLabel = "Apache_POI_project_logo_(2018).pdf";
            final String expFilenName = "C:\\Dell\\Apache_POI_project_logo_(2018).pdf";
            final String expCmd1 = "C:\\Users\\KIWIWI~1\\AppData\\Local\\Temp\\{84287F34-B79C-4F3A-9A92-6BB664586F48}\\Apache_POI_project_logo_(2018).pdf";
            final String expCmd2 = "C:\\Users\\KIWIWI~1\\AppData\\Local\\Temp\\{84287F34-B79C-4F3A-9A92-6BB664586F48}\\Apache_POI_project_logo_(2).pdf";

            assertTrue(ole10s.stream().map(Ole10Native::getLabel).allMatch(expLabel::equals));
            assertTrue(ole10s.stream().map(Ole10Native::getFileName).allMatch(expFilenName::equals));
            assertEquals(expCmd1, ole10s.get(0).getCommand());
            assertEquals(expCmd2, ole10s.get(1).getCommand());

            for (Ole10Native o : ole10s) {
                assertEquals(o.getLabel(), o.getLabel2());
                assertEquals(o.getCommand(), o.getCommand2());
                assertEquals(o.getFileName(), o.getFileName2());
            }

            Ole10Native scratch = new Ole10Native(expLabel, expFilenName, expCmd1,  ole10s.get(0).getDataBuffer());
            scratch.setLabel2(expLabel);
            scratch.setFileName2(expFilenName);
            scratch.setCommand2(expCmd1);

            try (POIFSFileSystem scratchFS = new POIFSFileSystem();
                POIFSFileSystem ole1FS = new POIFSFileSystem(new ByteArrayInputStream(oleShapes.get(0).getObjectData()))) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                scratch.writeOut(bos);
                scratchFS.createDocument(new ByteArrayInputStream(bos.toByteArray()), Ole10Native.OLE10_NATIVE);
                scratchFS.getRoot().setStorageClsid(ClassIDPredefined.OLE_V1_PACKAGE.getClassID());
                assertTrue(EntryUtils.areDirectoriesIdentical(ole1FS.getRoot(), scratchFS.getRoot()));
            }
        }
    }

    private static Ole10Native extractOle10Native(XSSFObjectData objectData) {
        try (InputStream is = objectData.getObjectPart().getInputStream();
             POIFSFileSystem poifs = new POIFSFileSystem(is)) {
            return Ole10Native.createFromEmbeddedOleObject(poifs);
        } catch (IOException | Ole10NativeException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    private static String digest(Ole10Native ole10) {
        MessageDigest sha = CryptoFunctions.getMessageDigest(HashAlgorithm.sha256);
        byte[] digest = sha.digest(ole10.getDataBuffer());
        return Base64.encodeBase64String(digest);
    }

    @Test
    void embedXSSF() throws IOException {
        Workbook wb1 = new XSSFWorkbook();
        addEmbeddedObjects(wb1);

        Workbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        validateEmbeddedObjects(wb2);

        wb2.close();
        wb1.close();
    }

    @Test
    void embedHSSF() throws IOException {
        assumeFalse(xslfOnly());

        HSSFWorkbook wb1 = new HSSFWorkbook();
        addEmbeddedObjects(wb1);
        Workbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        validateEmbeddedObjects(wb2);

        wb2.close();
        wb1.close();
    }

    static void validateEmbeddedObjects(Workbook wb) throws IOException {
        boolean ooxml = wb.getClass().getName().toLowerCase(Locale.ROOT).contains("xssf");
        byte[] data = (ooxml) ? samplePPTX : samplePPT;
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
        byte[] data = (ooxml) ? samplePPTX : samplePPT;
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
        SlideShow<?,?> ppt = SlideShowFactory.create(ooxml);
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
