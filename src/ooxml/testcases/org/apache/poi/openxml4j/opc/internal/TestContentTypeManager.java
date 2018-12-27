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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
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
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class TestContentTypeManager {

    /**
     * Test the properties part content parsing.
     */
    @Test
    public void testContentType() throws Exception {
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
    public void testContentTypeAddition() throws Exception {
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
    public void testContentTypeRemoval() throws Exception {
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
    @Ignore
    @Test
    public void testContentTypeRemovalPackage() {
        // TODO
        fail("test not written");
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
    public void bug62629CombinePictures() throws Exception {
        // this file has incorrect default content-types which caused problems in Apache POI
        // we now handle this broken file more gracefully
        XSSFWorkbook book = XSSFTestDataSamples.openSampleWorkbook("62629_target.xlsm");
        XSSFWorkbook b = XSSFTestDataSamples.openSampleWorkbook("62629_toMerge.xlsx");
        for (int i = 0; i < b.getNumberOfSheets(); i++) {
            XSSFSheet sheet = book.createSheet(b.getSheetName(i));
            copyPictures(sheet, b.getSheetAt(i));
        }

        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(book);
        wbBack.close();
        book.close();
        b.close();
    }

    private static void copyPictures(Sheet newSheet, Sheet sheet) {
        Drawing drawingOld = sheet.createDrawingPatriarch();
        Drawing drawingNew = newSheet.createDrawingPatriarch();
        CreationHelper helper = newSheet.getWorkbook().getCreationHelper();
        if (drawingNew instanceof XSSFDrawing) {
            List<XSSFShape> shapes = ((XSSFDrawing) drawingOld).getShapes();
            for (int i = 0; i < shapes.size(); i++) {
                if (shapes.get(i) instanceof XSSFPicture) {
                    XSSFPicture pic = (XSSFPicture) shapes.get(i);
                    XSSFPictureData picData = pic.getPictureData();
                    int pictureIndex = newSheet.getWorkbook().addPicture(picData.getData(), picData.getPictureType());
                    XSSFClientAnchor anchor = null;
                    CTTwoCellAnchor oldAnchor = ((XSSFDrawing) drawingOld).getCTDrawing().getTwoCellAnchorArray(i);
                    if (oldAnchor != null) {
                        anchor = (XSSFClientAnchor) helper.createClientAnchor();
                        CTMarker markerFrom = oldAnchor.getFrom();
                        CTMarker markerTo = oldAnchor.getTo();
                        anchor.setDx1((int) markerFrom.getColOff());
                        anchor.setDx2((int) markerTo.getColOff());
                        anchor.setDy1((int) markerFrom.getRowOff());
                        anchor.setDy2((int) markerTo.getRowOff());
                        anchor.setCol1(markerFrom.getCol());
                        anchor.setCol2(markerTo.getCol());
                        anchor.setRow1(markerFrom.getRow());
                        anchor.setRow2(markerTo.getRow());
                    }
                    drawingNew.createPicture(anchor, pictureIndex);
                }
            }
        }
    }
}
