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

package org.apache.poi.openxml4j.opc.internal;

import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;

public final class TestContentTypeManager {

    /**
     * Test the properties part content parsing.
     */
    @Test
    void testContentType() throws Exception {
        String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

        // Retrieves core properties part
        try (OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ)) {
            PackageRelationshipCollection rels = p.getRelationshipsByType(PackageRelationshipTypes.CORE_PROPERTIES);
            PackageRelationship corePropertiesRelationship = rels.getRelationship(0);
            PackagePart coreDocument = p.getPart(corePropertiesRelationship);

            assertEquals("application/vnd.openxmlformats-package.core-properties+xml", coreDocument.getContentType());
        }
    }

    /**
     * Test the addition of several default and override content types.
     */
    @Test
    void testContentTypeAddition() throws Exception {
        ContentTypeManager ctm = new ZipContentTypeManager(null, null);

        PackagePartName name1 = PackagingURIHelper.createPartName("/foo/foo.XML");
        PackagePartName name2 = PackagingURIHelper.createPartName("/foo/foo2.xml");
        PackagePartName name3 = PackagingURIHelper.createPartName("/foo/doc.rels");
        PackagePartName name4 = PackagingURIHelper.createPartName("/foo/doc.RELS");

        // Add content types
        ctm.addContentType(name1, "foo-type1");
        ctm.addContentType(name2, "foo-type2");
        ctm.addContentType(name3, "text/xml+rel");
        ctm.addContentType(name4, "text/xml+rel");

        assertEquals(ctm.getContentType(name1), "foo-type1");
        assertEquals(ctm.getContentType(name2), "foo-type2");
        assertEquals(ctm.getContentType(name3), "text/xml+rel");
        assertEquals(ctm.getContentType(name3), "text/xml+rel");
    }

    /**
     * Test the addition then removal of content types.
     */
    @Test
    void testContentTypeRemoval() throws Exception {
        ContentTypeManager ctm = new ZipContentTypeManager(null, null);

        PackagePartName name1 = PackagingURIHelper.createPartName("/foo/foo.xml");
        PackagePartName name2 = PackagingURIHelper.createPartName("/foo/foo2.xml");
        PackagePartName name3 = PackagingURIHelper.createPartName("/foo/doc.rels");
        PackagePartName name4 = PackagingURIHelper.createPartName("/foo/doc.RELS");

        // Add content types
        ctm.addContentType(name1, "foo-type1");
        ctm.addContentType(name2, "foo-type2");
        ctm.addContentType(name3, "text/xml+rel");
        ctm.addContentType(name4, "text/xml+rel");
        ctm.removeContentType(name2);
        ctm.removeContentType(name3);

        assertEquals(ctm.getContentType(name1), "foo-type1");
        assertEquals(ctm.getContentType(name2), "foo-type1");
        assertNull(ctm.getContentType(name3));

        ctm.removeContentType(name1);
        assertNull(ctm.getContentType(name1));
        assertNull(ctm.getContentType(name2));
    }

    /**
     * Test the addition then removal of content types in a package.
     */
    @Disabled
    void testContentTypeRemovalPackage() {
        // TODO
    }

    protected byte[] toByteArray(Workbook wb) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            wb.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("failed to write excel file.");
        }
    }

    @Test
    void bug62629CombinePictures() throws Exception {
        // this file has incorrect default content-types which caused problems in Apache POI
        // we now handle this broken file more gracefully
        try (XSSFWorkbook targetWB = openSampleWorkbook("62629_target.xlsm");
            XSSFWorkbook mergeWB = openSampleWorkbook("62629_toMerge.xlsx")) {
            for (Sheet b_sheet : mergeWB) {
                XSSFSheet sheet = targetWB.createSheet(b_sheet.getSheetName());

                XSSFDrawing drawingOld = (XSSFDrawing)b_sheet.createDrawingPatriarch();
                XSSFDrawing drawingNew = sheet.createDrawingPatriarch();
                CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                List<XSSFShape> shapes = drawingOld.getShapes();

                for (XSSFShape shape : shapes) {
                    assertTrue(shape instanceof XSSFPicture);
                    XSSFPicture pic = (XSSFPicture)shape;
                    XSSFPictureData picData = pic.getPictureData();
                    int pictureIndex = targetWB.addPicture(picData.getData(), picData.getPictureType());
                    ClientAnchor oldAnchor = pic.getClientAnchor();
                    assertNotNull(oldAnchor);
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setAnchorType(oldAnchor.getAnchorType());
                    anchor.setDx1(oldAnchor.getDx1());
                    anchor.setDx2(oldAnchor.getDx2());
                    anchor.setDy1(oldAnchor.getDy1());
                    anchor.setDy2(oldAnchor.getDy2());
                    anchor.setCol1(oldAnchor.getCol1());
                    anchor.setCol2(oldAnchor.getCol2());
                    anchor.setRow1(oldAnchor.getRow1());
                    anchor.setRow2(oldAnchor.getRow2());
                    drawingNew.createPicture(anchor, pictureIndex);
                }
            }

            try (XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(targetWB)) {
                for (XSSFWorkbook wb : Arrays.asList(targetWB, mergeWB)) {
                    for (Sheet sheet : wb) {
                        XSSFSheet backSheet = wbBack.getSheet(sheet.getSheetName());
                        assertNotNull(backSheet);
                        XSSFDrawing origPat = (XSSFDrawing)sheet.createDrawingPatriarch();
                        XSSFDrawing backPat = backSheet.createDrawingPatriarch();
                        assertEquals(origPat.getShapes().size(), backPat.getShapes().size());
                    }
                }
            }
        }
    }
}
