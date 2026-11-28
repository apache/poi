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

package org.apache.poi.xwpf.usermodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.TrackingInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;

public final class TestXWPFDocument {

    @Test
    void testContainsMainContentType() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
            OPCPackage pack = doc.getPackage()) {
            String ct = XWPFRelation.DOCUMENT.getContentType();
            boolean found = pack.getParts().stream().anyMatch(p -> ct.equals(p.getContentType()));
            assertTrue(found);
        }
    }

    @Test
    void testOpen() throws Exception {
        // Simple file
        try (XWPFDocument xml1 = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            // Check it has key parts
            assertNotNull(xml1.getDocument());
            assertNotNull(xml1.getDocument().getBody());
            assertNotNull(xml1.getStyle());
            assertNotNull(xml1.getTheme());
            assertEquals("Cambria", xml1.getTheme().getMajorFont());
        }

        // Complex file
        try (XWPFDocument xml2 = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx")) {
            assertNotNull(xml2.getDocument());
            assertNotNull(xml2.getDocument().getBody());
            assertNotNull(xml2.getStyle());
        }
    }

    @Test
    void testMetadataBasics() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            assertNotNull(xml.getProperties().getCoreProperties());
            assertNotNull(xml.getProperties().getExtendedProperties());

            assertEquals("Microsoft Office Word", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
            assertEquals(1315, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
            assertEquals(10, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

            assertNull(xml.getProperties().getCoreProperties().getTitle());
            assertFalse(xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().isPresent());
        }
    }

    @Test
    void testMetadataComplex() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx")) {
            assertNotNull(xml.getProperties().getCoreProperties());
            assertNotNull(xml.getProperties().getExtendedProperties());

            CTProperties up = xml.getProperties().getExtendedProperties().getUnderlyingProperties();
            assertEquals("Microsoft Office Outlook", up.getApplication());
            assertEquals(5184, up.getCharacters());
            assertEquals(0, up.getLines());

            POIXMLProperties.CoreProperties cp = xml.getProperties().getCoreProperties();
            assertEquals(" ", cp.getTitle());
            Optional<String> subjectProperty = cp.getUnderlyingProperties().getSubjectProperty();
            assertTrue(subjectProperty.isPresent());
            assertEquals(" ", subjectProperty.get());
        }
    }

    @Test
    void testWorkbookProperties() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            POIXMLProperties props = doc.getProperties();
            assertNotNull(props);
            assertEquals("Apache POI", props.getExtendedProperties().getUnderlyingProperties().getApplication());
        }
    }

    @Test
    void testAddParagraph() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            assertEquals(3, doc.getParagraphs().size());

            XWPFParagraph p = doc.createParagraph();
            assertEquals(p, doc.getParagraphs().get(3));
            assertEquals(4, doc.getParagraphs().size());

            assertEquals(3, doc.getParagraphPos(3));
            assertEquals(3, doc.getPosOfParagraph(p));

            CTP ctp = p.getCTP();
            XWPFParagraph newP = doc.getParagraph(ctp);
            assertSame(p, newP);
            XmlCursor cursor = doc.getDocument().getBody().getPArray(0).newCursor();
            XWPFParagraph cP = doc.insertNewParagraph(cursor);
            assertSame(cP, doc.getParagraphs().get(0));
            assertEquals(5, doc.getParagraphs().size());
        }
    }

    @Test
    void testAddPicture() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            byte[] jpeg = XWPFTestDataSamples.getImage("nature1.jpg");
            String relationId = doc.addPictureData(jpeg, Document.PICTURE_TYPE_JPEG);

            XWPFPictureData relationById = (XWPFPictureData) doc.getRelationById(relationId);
            assertNotNull(relationById);
            byte[] newJpeg = relationById.getData();
            assertEquals(newJpeg.length, jpeg.length);
            assertArrayEquals(jpeg, newJpeg);
        }
    }

    @Test
    void testAddPicture2() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            byte[] data = XWPFTestDataSamples.getImage("nature1.png");
            String relationId = doc.addPictureData(data, PictureType.PNG);

            XWPFPictureData relationById = (XWPFPictureData) doc.getRelationById(relationId);
            assertNotNull(relationById);
            byte[] newData = relationById.getData();
            assertEquals(newData.length, data.length);
            assertArrayEquals(data, newData);
        }
    }

    @Test
    void testAllPictureFormats() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = new XWPFDocument()) {

            doc.addPictureData(new byte[10], Document.PICTURE_TYPE_EMF);
            doc.addPictureData(new byte[11], Document.PICTURE_TYPE_WMF);
            doc.addPictureData(new byte[12], Document.PICTURE_TYPE_PICT);
            doc.addPictureData(new byte[13], Document.PICTURE_TYPE_JPEG);
            doc.addPictureData(new byte[14], Document.PICTURE_TYPE_PNG);
            doc.addPictureData(new byte[15], Document.PICTURE_TYPE_DIB);
            doc.addPictureData(new byte[16], Document.PICTURE_TYPE_GIF);
            doc.addPictureData(new byte[17], Document.PICTURE_TYPE_TIFF);
            doc.addPictureData(new byte[18], Document.PICTURE_TYPE_EPS);
            doc.addPictureData(new byte[19], Document.PICTURE_TYPE_BMP);
            doc.addPictureData(new byte[20], Document.PICTURE_TYPE_WPG);
            doc.addPictureData(new byte[21], Document.PICTURE_TYPE_SVG);

            assertEquals(12, doc.getAllPictures().size());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(12, doc2.getAllPictures().size());
            }
        }
    }

    @Test
    void testAddHyperlink() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx")) {
            XWPFParagraph p = doc.createParagraph();
            XWPFHyperlinkRun h = p.createHyperlinkRun("https://poi.apache.org/");
            h.setText("Apache POI");

            assertEquals("rId7", h.getHyperlinkId());

            assertEquals("https://poi.apache.org/", h.getHyperlink(doc).getURL());
            assertEquals(1, p.getRuns().size());
            assertEquals(h, p.getRuns().get(0));

            h = p.createHyperlinkRun("https://poi.apache.org/");
            h.setText("Apache POI");

            assertEquals("rId8", h.getHyperlinkId());
        }
    }

    /**
     * Looking up an id that isn't a hyperlink relationship must not rebuild the hyperlink list,
     * otherwise a document with n such links and n hyperlink relationships takes O(n^2) to extract
     */
    @Test
    void testUnknownHyperlinkIds() throws Exception {
        final int count = 2_000;
        try (XWPFDocument doc = new XWPFDocument()) {
            for (int i = 0; i < count; i++) {
                doc.getPackagePart().addRelationship(new URI("https://poi.apache.org/" + i),
                    TargetMode.EXTERNAL, XWPFRelation.HYPERLINK.getRelation(), "rIdH" + i);
            }
            doc.getPackagePart().addRelationship(new URI("https://poi.apache.org/"),
                TargetMode.EXTERNAL, "http://example.com/other", "rIdOther");
            XWPFParagraph p = doc.createParagraph();
            for (int i = 0; i < count; i++) {
                CTHyperlink link = p.getCTP().addNewHyperlink();
                link.setId("x" + i);
                link.addNewR().addNewT().setStringValue("a");
            }
            CTHyperlink known = p.getCTP().addNewHyperlink();
            known.setId("rIdH42");
            known.addNewR().addNewT().setStringValue("b");

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                List<XWPFHyperlink> loaded = doc2.hyperlinks;
                try (XWPFWordExtractor ex = new XWPFWordExtractor(doc2)) {
                    ex.setFetchHyperlinks(true);
                    assertThat(ex.getText(), containsString(" <https://poi.apache.org/42>"));
                }
                assertNull(doc2.getHyperlinkByID("x0"));
                assertNull(doc2.getHyperlinkByID("rIdOther"));
                // the misses above must not have rebuilt the list from the relationships
                assertSame(loaded, doc2.hyperlinks);
                assertEquals(count, loaded.size());
                assertEquals(count, doc2.getHyperlinks().length);
            }
        }
    }

    @Test
    void testHyperlinksFollowRelationships() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            PackagePart part = doc.getPackagePart();
            XWPFParagraph p = doc.createParagraph();
            String idA = p.createHyperlinkRun("https://poi.apache.org/a").getHyperlinkId();
            String idB = p.createHyperlinkRun("https://poi.apache.org/b").getHyperlinkId();

            // resolving one new link must not hide the other from getHyperlinks()
            assertEquals("https://poi.apache.org/a", doc.getHyperlinkByID(idA).getURL());
            XWPFHyperlink[] links = doc.getHyperlinks();
            assertEquals(2, links.length);
            assertEquals(idA, links[0].getId());
            assertEquals(idB, links[1].getId());

            // a replaced relationship resolves to its new target
            part.removeRelationship(idA);
            part.addRelationship(new URI("https://poi.apache.org/a2"),
                TargetMode.EXTERNAL, XWPFRelation.HYPERLINK.getRelation(), idA);
            assertEquals("https://poi.apache.org/a2", doc.getHyperlinkByID(idA).getURL());

            // and a removed one is no longer listed
            part.removeRelationship(idB);
            links = doc.getHyperlinks();
            assertEquals(1, links.length);
            assertEquals("https://poi.apache.org/a2", links[0].getURL());
            assertNull(doc.getHyperlinkByID(idB));

            // a relationship of another type isn't a hyperlink
            part.removeRelationship(idA);
            part.addRelationship(new URI("https://poi.apache.org/a3"),
                TargetMode.EXTERNAL, "http://example.com/other", idA);
            assertNull(doc.getHyperlinkByID(idA));
        }
    }

    @Test
    void testHyperlinkAddedToList() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            String id = doc.createParagraph().createHyperlinkRun("https://poi.apache.org/rel").getHyperlinkId();
            doc.hyperlinks.add(new XWPFHyperlink("myId", "https://poi.apache.org/mine"));
            assertEquals("https://poi.apache.org/mine", doc.getHyperlinkByID("myId").getURL());

            // getHyperlinks() lists it after the relationships and doesn't drop it from the list
            XWPFHyperlink[] links = doc.getHyperlinks();
            assertEquals(2, links.length);
            assertEquals(id, links[0].getId());
            assertEquals("myId", links[1].getId());
            assertEquals("https://poi.apache.org/mine", doc.getHyperlinkByID("myId").getURL());
        }
    }

    @Test
    void testRemovedRelationshipOfLoadedHyperlink() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createHyperlinkRun("https://poi.apache.org/loaded");
            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                XWPFHyperlink loaded = doc2.getHyperlinks()[0];
                assertEquals("https://poi.apache.org/loaded", doc2.getHyperlinkByID(loaded.getId()).getURL());

                // the list still has the entry read with the document, but the relationship is gone
                doc2.getPackagePart().removeRelationship(loaded.getId());
                assertNull(doc2.getHyperlinkByID(loaded.getId()));
                assertEquals(0, doc2.getHyperlinks().length);
            }
        }
    }

    @Test
    void testRemoveBodyElement() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            assertEquals(3, doc.getParagraphs().size());
            assertEquals(3, doc.getBodyElements().size());

            XWPFParagraph p1 = doc.getParagraphs().get(0);
            XWPFParagraph p2 = doc.getParagraphs().get(1);
            XWPFParagraph p3 = doc.getParagraphs().get(2);

            assertEquals(p1, doc.getBodyElements().get(0));
            assertEquals(p1, doc.getParagraphs().get(0));
            assertEquals(p2, doc.getBodyElements().get(1));
            assertEquals(p2, doc.getParagraphs().get(1));
            assertEquals(p3, doc.getBodyElements().get(2));
            assertEquals(p3, doc.getParagraphs().get(2));

            // Add another
            XWPFParagraph p4 = doc.createParagraph();

            assertEquals(4, doc.getParagraphs().size());
            assertEquals(4, doc.getBodyElements().size());
            assertEquals(p1, doc.getBodyElements().get(0));
            assertEquals(p1, doc.getParagraphs().get(0));
            assertEquals(p2, doc.getBodyElements().get(1));
            assertEquals(p2, doc.getParagraphs().get(1));
            assertEquals(p3, doc.getBodyElements().get(2));
            assertEquals(p3, doc.getParagraphs().get(2));
            assertEquals(p4, doc.getBodyElements().get(3));
            assertEquals(p4, doc.getParagraphs().get(3));

            // Remove the 2nd
            assertTrue(doc.removeBodyElement(1));
            assertEquals(3, doc.getParagraphs().size());
            assertEquals(3, doc.getBodyElements().size());

            assertEquals(p1, doc.getBodyElements().get(0));
            assertEquals(p1, doc.getParagraphs().get(0));
            assertEquals(p3, doc.getBodyElements().get(1));
            assertEquals(p3, doc.getParagraphs().get(1));
            assertEquals(p4, doc.getBodyElements().get(2));
            assertEquals(p4, doc.getParagraphs().get(2));

            // Remove the 1st
            assertTrue(doc.removeBodyElement(0));
            assertEquals(2, doc.getParagraphs().size());
            assertEquals(2, doc.getBodyElements().size());

            assertEquals(p3, doc.getBodyElements().get(0));
            assertEquals(p3, doc.getParagraphs().get(0));
            assertEquals(p4, doc.getBodyElements().get(1));
            assertEquals(p4, doc.getParagraphs().get(1));

            // Remove the last
            assertTrue(doc.removeBodyElement(1));
            assertEquals(1, doc.getParagraphs().size());
            assertEquals(1, doc.getBodyElements().size());

            assertEquals(p3, doc.getBodyElements().get(0));
            assertEquals(p3, doc.getParagraphs().get(0));
        }
    }

    @Test
    void testRemoveBodyElementParagraphWithPicture() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            doc.createParagraph().createRun().setText("keep");

            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getAllPackagePictures().size());

            assertTrue(doc.removeBodyElement(0));
            assertTrue(doc.getAllPictures().isEmpty());
            assertTrue(doc.getAllPackagePictures().isEmpty());
            assertTrue(doc.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).isEmpty());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getAllPackagePictures().isEmpty());
                assertTrue(doc2.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).isEmpty());
                assertEquals(1, doc2.getParagraphs().size());
                assertEquals("keep", doc2.getParagraphs().get(0).getText());
            }

            // Re-adding the same picture bytes creates a fresh part instead of reusing the deleted one
            doc.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(1, doc3.getAllPictures().size());
                assertArrayEquals(png, doc3.getAllPictures().get(0).getData());
            }
        }
    }

    @Test
    void testRemoveBodyElementSharedPictureBetweenParagraphs() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            doc.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);

            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).size());

            // Removing the first paragraph keeps the picture for the second paragraph
            assertTrue(doc.removeBodyElement(0));
            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getAllPackagePictures().size());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(1, doc2.getAllPictures().size());
                assertEquals(1, doc2.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).size());
                assertArrayEquals(png, doc2.getParagraphs().get(0).getRuns().get(0).getEmbeddedPictures().get(0).getPictureData().getData());

                // Removing the last paragraph using the picture drops the image part
                assertTrue(doc2.removeBodyElement(0));
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getAllPackagePictures().isEmpty());

                try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc2)) {
                    assertTrue(doc3.getAllPictures().isEmpty());
                    assertTrue(doc3.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).isEmpty());
                }
            }
        }
    }

    @Test
    void testRemoveBodyElementPictureAlsoUsedInHeader() throws Exception {
        // Loaded document where body and header share the same image part
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_2.docx")) {
            byte[] expectedBytes = doc.getHeaderArray(0).getAllPictures().get(0).getData();
            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getAllPackagePictures().size());
            assertEquals(1, doc.getHeaderArray(0).getAllPictures().size());

            assertTrue(doc.removeBodyElement(0));
            assertTrue(doc.getAllPictures().isEmpty());
            assertEquals(0, doc.getPackagePart().getRelationshipsByType(XWPFRelation.IMAGES.getRelation()).size());
            assertEquals(1, doc.getAllPackagePictures().size());
            assertEquals(1, doc.getHeaderArray(0).getAllPictures().size());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getAllPictures().isEmpty());
                assertEquals(1, doc2.getAllPackagePictures().size());
                assertEquals(1, doc2.getHeaderArray(0).getAllPictures().size());
                assertEquals(1, doc2.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).size());
                assertArrayEquals(expectedBytes, doc2.getHeaderArray(0).getAllPictures().get(0).getData());
            }
        }

        // Programmatically created document with the same picture in body and header
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
            header.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);

            assertTrue(doc.removeBodyElement(0));
            assertTrue(doc.getAllPictures().isEmpty());
            assertEquals(1, doc.getAllPackagePictures().size());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getAllPictures().isEmpty());
                assertEquals(1, doc2.getHeaderArray(0).getAllPictures().size());
                assertEquals(1, doc2.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).size());
                assertArrayEquals(png, doc2.getHeaderArray(0).getAllPictures().get(0).getData());
            }
        }
    }

    @Test
    void testRemoveBodyElementTableWithPicture() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        byte[] jpg = XWPFTestDataSamples.getImage("nature1.jpg");
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 1);
            XWPFTableCell cell = table.getRow(0).getCell(0);
            cell.getParagraphArray(0).createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);

            // Also place a picture inside a nested table in the same cell
            CTTbl nestedCtTbl = cell.getCTTc().addNewTbl();
            XWPFTable nestedTable = new XWPFTable(nestedCtTbl, cell, 1, 1);
            nestedTable.getRow(0).getCell(0).getParagraphArray(0).createRun()
                    .addPicture(new ByteArrayInputStream(jpg), PictureType.JPEG, "nature1.jpg", 100000, 100000);

            assertEquals(2, doc.getAllPictures().size());
            assertEquals(2, doc.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).size());

            assertTrue(doc.removeBodyElement(0));
            assertTrue(doc.getTables().isEmpty());
            assertTrue(doc.getAllPictures().isEmpty());
            assertTrue(doc.getAllPackagePictures().isEmpty());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getTables().isEmpty());
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getPackage().getPartsByName(Pattern.compile("/word/media/.*")).isEmpty());
            }
        }
    }

    @Test
    void testRemoveBodyElementChart() throws Exception {
        // Loaded document with two charts, each with style, colors, and an embedded Excel workbook
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61745.docx")) {
            assertEquals(2, doc.getCharts().size());
            assertEquals(2, doc.getPackage().getPartsByName(Pattern.compile("/word/charts/chart\\d+\\.xml")).size());
            assertEquals(2, doc.getPackage().getPartsByName(Pattern.compile("/word/embeddings/.*")).size());

            assertTrue(doc.removeBodyElement(0));
            assertEquals(1, doc.getCharts().size());
            assertEquals("/word/charts/chart2.xml", doc.getCharts().get(0).getPackagePart().getPartName().getName());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(1, doc2.getCharts().size());
                assertFalse(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/charts/chart1.xml")));
                assertFalse(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/charts/colors1.xml")));
                assertFalse(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/charts/style1.xml")));
                assertFalse(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/embeddings/Microsoft_Excel_Worksheet1.xlsx")));
                assertTrue(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/charts/chart2.xml")));
                assertTrue(doc2.getPackage().containPart(PackagingURIHelper.createPartName("/word/embeddings/Microsoft_Excel_Worksheet2.xlsx")));

                // Removing the second chart removes the remaining chart and embedded workbook parts
                assertTrue(doc2.removeBodyElement(0));
                assertTrue(doc2.getCharts().isEmpty());

                try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc2)) {
                    assertTrue(doc3.getCharts().isEmpty());
                    assertTrue(doc3.getPackage().getPartsByName(Pattern.compile("/word/(charts|embeddings)/.*")).isEmpty());
                }
            }
        }

        // Programmatically created chart with an embedded workbook
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFChart chart = doc.createChart();
            chart.saveWorkbook(chart.getWorkbook());
            assertEquals(1, doc.getCharts().size());
            assertEquals(1, doc.getPackage().getPartsByName(Pattern.compile("/word/embeddings/.*")).size());

            assertTrue(doc.removeBodyElement(0));
            assertTrue(doc.getCharts().isEmpty());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getCharts().isEmpty());
                assertTrue(doc2.getPackage().getPartsByName(Pattern.compile("/word/(charts|embeddings)/.*")).isEmpty());
            }
        }
    }

    @Test
    void testRemoveBodyElementHyperlink() throws Exception {
        String idUnique;
        String idShared;
        try (XWPFDocument doc = new XWPFDocument()) {
            idUnique = doc.createParagraph().createHyperlinkRun("https://poi.apache.org/unique").getHyperlinkId();
            idShared = doc.createParagraph().createHyperlinkRun("https://poi.apache.org/shared").getHyperlinkId();
            // Share idShared with a second paragraph
            CTHyperlink sharedLink = doc.createParagraph().getCTP().addNewHyperlink();
            sharedLink.setId(idShared);
            sharedLink.addNewR().addNewT().setStringValue("shared2");

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                // Also add a hyperlink after loading (exists in packagePart, not referenceRelationships)
                String idCreatedAfterLoad = doc2.createParagraph().createHyperlinkRun("https://poi.apache.org/new").getHyperlinkId();
                assertEquals(3, doc2.getHyperlinks().length);

                // Removing paragraph 0 drops idUnique
                assertTrue(doc2.removeBodyElement(0));
                assertNull(doc2.getHyperlinkByID(idUnique));
                assertNull(doc2.getReferenceRelationship(idUnique));
                assertNull(doc2.getPackagePart().getRelationship(idUnique));
                assertEquals(2, doc2.getHyperlinks().length);

                // Removing paragraph 0 (originally 1) keeps idShared because the next paragraph still uses it
                assertTrue(doc2.removeBodyElement(0));
                assertNotNull(doc2.getHyperlinkByID(idShared));
                assertNotNull(doc2.getPackagePart().getRelationship(idShared));

                // Removing paragraph 1 (idCreatedAfterLoad) drops its external relationship
                assertTrue(doc2.removeBodyElement(1));
                assertNull(doc2.getHyperlinkByID(idCreatedAfterLoad));
                assertNull(doc2.getPackagePart().getRelationship(idCreatedAfterLoad));
                assertEquals(1, doc2.getHyperlinks().length);

                try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc2)) {
                    assertEquals(1, doc3.getHyperlinks().length);
                    assertEquals("https://poi.apache.org/shared", doc3.getHyperlinks()[0].getURL());
                    assertEquals(1, doc3.getPackagePart().getRelationshipsByType(XWPFRelation.HYPERLINK.getRelation()).size());
                }
            }
        }
    }

    @Test
    void testRemoveBodyElementPlainParagraphLeavesRelationsUnchanged() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("plain paragraph");
            XWPFParagraph pWithContent = doc.createParagraph();
            pWithContent.createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            pWithContent.createHyperlinkRun("https://poi.apache.org/");

            int relCountBefore = doc.getPackagePart().getRelationships().size();
            assertTrue(doc.removeBodyElement(0));
            assertEquals(relCountBefore, doc.getPackagePart().getRelationships().size());
            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getHyperlinks().length);

            // Removing the paragraph with content drops its two relations while keeping document structural relations
            assertTrue(doc.removeBodyElement(0));
            assertEquals(relCountBefore - 2, doc.getPackagePart().getRelationships().size());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(relCountBefore - 2, doc2.getPackagePart().getRelationships().size());
                assertTrue(doc2.getAllPictures().isEmpty());
                assertEquals(0, doc2.getHyperlinks().length);
                assertNotNull(doc2.getSettings());
            }
        }
    }

    @Test
    void testRemoveBodyElementEmbeddedOleObject() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("EmbeddedDocument.docx")) {
            assertEquals(1, doc.getAllEmbeddedParts().size());
            assertEquals(1, doc.getAllPictures().size());

            // Body element 2 holds the VML imagedata and o:OLEObject
            assertTrue(doc.removeBodyElement(2));
            assertTrue(doc.getAllEmbeddedParts().isEmpty());
            assertTrue(doc.getAllPictures().isEmpty());
            assertTrue(doc.getAllPackagePictures().isEmpty());

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertTrue(doc2.getAllEmbeddedParts().isEmpty());
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getPackage().getPartsByName(Pattern.compile("/word/(embeddings|media)/.*")).isEmpty());
            }
        }
    }

    @Test
    void testRemoveBodyElementTextboxAlternateContentSmartArtAndExternalLink() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        byte[] jpg = XWPFTestDataSamples.getImage("nature1.jpg");
        try (XWPFDocument doc = new XWPFDocument()) {
            String txbxPicId = doc.addPictureData(png, PictureType.PNG);
            String fallbackPicId = doc.addPictureData(jpg, PictureType.JPEG);
            String extImgId = doc.getPackagePart().addExternalRelationship(
                    "https://poi.apache.org/linked.png", XWPFRelation.IMAGES.getRelation()).getId();

            // Create a SmartArt data part and relate it to the document
            PackagePartName dmName = PackagingURIHelper.createPartName("/word/diagrams/data1.xml");
            PackagePart dmPart = doc.getPackage().createPart(dmName, "application/vnd.openxmlformats-officedocument.drawingml.diagramData+xml");
            try (OutputStream out = dmPart.getOutputStream()) {
                out.write("<dgm:dataModel xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\"/>".getBytes(StandardCharsets.UTF_8));
            }
            String dmRelId = doc.getPackagePart().addRelationship(
                    dmName, TargetMode.INTERNAL,
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramData").getId();

            // Build a paragraph containing a textbox, mc:AlternateContent fallback, SmartArt relIds, and a linked image
            XWPFParagraph p = doc.createParagraph();
            String xml = "<w:r xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
                    + " xmlns:r=\"" + PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS + "\""
                    + " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
                    + " xmlns:v=\"urn:schemas-microsoft-com:vml\""
                    + " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
                    + " xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\">"
                    + "<mc:AlternateContent>"
                    + "<mc:Choice Requires=\"wps\">"
                    + "<w:txbxContent><w:p><w:r><a:blip r:embed=\"" + txbxPicId + "\" r:link=\"" + extImgId + "\"/></w:r></w:p></w:txbxContent>"
                    + "</mc:Choice>"
                    + "<mc:Fallback>"
                    + "<w:pict><v:shape><v:imagedata r:id=\"" + fallbackPicId + "\"/></v:shape></w:pict>"
                    + "</mc:Fallback>"
                    + "</mc:AlternateContent>"
                    + "<dgm:relIds r:dm=\"" + dmRelId + "\"/>"
                    + "</w:r>";
            XmlObject parsed = XmlObject.Factory.parse(xml);
            try (XmlCursor src = parsed.newCursor();
                 XmlCursor dst = p.getCTP().newCursor()) {
                src.toFirstChild();
                dst.toEndToken();
                src.copyXml(dst);
            }

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(2, doc2.getAllPictures().size());
                assertTrue(doc2.getPackage().containPart(dmName));
                assertNotNull(doc2.getPackagePart().getRelationship(extImgId));

                assertTrue(doc2.removeBodyElement(0));
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getAllPackagePictures().isEmpty());
                assertNull(doc2.getPackagePart().getRelationship(extImgId));
                assertFalse(doc2.getPackage().containPart(dmName));

                try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc2)) {
                    assertTrue(doc3.getAllPictures().isEmpty());
                    assertNull(doc3.getPackagePart().getRelationship(extImgId));
                    assertTrue(doc3.getPackage().getPartsByName(Pattern.compile("/word/(media|diagrams)/.*")).isEmpty());
                }
            }
        }
    }

    @Test
    void testRemoveBodyElementKeepsPartTargetedByInternalHyperlink() throws Exception {
        byte[] png = XWPFTestDataSamples.getImage("nature1.png");
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().addPicture(new ByteArrayInputStream(png), PictureType.PNG, "nature1.png", 100000, 100000);
            PackagePartName imageName = doc.getAllPictures().get(0).getPackagePart().getPartName();

            // Add a second paragraph with an internal hyperlink targeting the image part
            String linkId = doc.getPackagePart().addRelationship(
                    new URI("media/" + doc.getAllPictures().get(0).getFileName()),
                    TargetMode.INTERNAL, PackageRelationshipTypes.HYPERLINK_PART).getId();
            CTHyperlink link = doc.createParagraph().getCTP().addNewHyperlink();
            link.setId(linkId);
            link.addNewR().addNewT().setStringValue("see image");

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(1, doc2.getAllPictures().size());
                assertTrue(doc2.removeBodyElement(0));
                // Document's image relation is dropped, but the image part stays because the internal hyperlink targets it
                assertTrue(doc2.getAllPictures().isEmpty());
                assertTrue(doc2.getPackage().containPart(imageName));

                try (XWPFDocument doc3 = XWPFTestDataSamples.writeOutAndReadBack(doc2)) {
                    assertTrue(doc3.getAllPictures().isEmpty());
                    assertTrue(doc3.getPackage().containPart(imageName));
                    assertNotNull(doc3.getPackagePart().getRelationship(linkId));
                }
            }
        }
    }

    @Test
    void testRegisterPackagePictureData() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx")) {
            /* manually assemble a new image package part*/
            OPCPackage opcPkg = doc.getPackage();
            XWPFRelation jpgRelation = XWPFRelation.IMAGE_JPEG;
            PackagePartName partName = PackagingURIHelper.createPartName(jpgRelation.getDefaultFileName().replace('#', '2'));
            PackagePart newImagePart = opcPkg.createPart(partName, jpgRelation.getContentType());
            byte[] nature1 = XWPFTestDataSamples.getImage("abstract4.jpg");
            OutputStream os = newImagePart.getOutputStream();
            os.write(nature1);
            os.close();
            XWPFHeader xwpfHeader = doc.getHeaderArray(0);
            XWPFPictureData newPicData = new XWPFPictureData(newImagePart);
            /* new part is now ready to rumble */

            assertFalse(xwpfHeader.getAllPictures().contains(newPicData));
            assertFalse(doc.getAllPictures().contains(newPicData));
            assertFalse(doc.getAllPackagePictures().contains(newPicData));

            doc.registerPackagePictureData(newPicData);

            assertFalse(xwpfHeader.getAllPictures().contains(newPicData));
            assertFalse(doc.getAllPictures().contains(newPicData));
            assertTrue(doc.getAllPackagePictures().contains(newPicData));

            doc.getPackage().revert();
            opcPkg.close();
        }
    }

    @Test
    void testFindPackagePictureData() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx")) {
            byte[] nature1 = XWPFTestDataSamples.getImage("nature1.gif");
            XWPFPictureData part = doc.findPackagePictureData(nature1);
            assertNotNull(part);
            assertTrue(doc.getAllPictures().contains(part));
            assertTrue(doc.getAllPackagePictures().contains(part));
            doc.getPackage().revert();
        }
    }

    @Test
    void testGetAllPictures() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx")) {
            List<XWPFPictureData> allPictures = doc.getAllPictures();
            List<XWPFPictureData> allPackagePictures = doc.getAllPackagePictures();

            assertNotNull(allPictures);
            assertEquals(3, allPictures.size());
            for (XWPFPictureData xwpfPictureData : allPictures) {
                assertTrue(allPackagePictures.contains(xwpfPictureData));
            }

            assertThrows(UnsupportedOperationException.class, () -> allPictures.add(allPictures.get(0)), "This list must be unmodifiable!");

            doc.getPackage().revert();
        }
    }

    @Test
    void testGetAllPackagePictures() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx")) {
            List<XWPFPictureData> allPackagePictures = doc.getAllPackagePictures();

            assertNotNull(allPackagePictures);
            assertEquals(5, allPackagePictures.size());

            assertThrows(UnsupportedOperationException.class, () -> allPackagePictures.add(allPackagePictures.get(0)), "This list must be unmodifiable!");

            doc.getPackage().revert();
        }
    }

    @Test
    void testPictureHandlingSimpleFile() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx")) {
            assertEquals(1, doc.getAllPackagePictures().size());
            byte[] newPic = XWPFTestDataSamples.getImage("abstract4.jpg");
            String id1 = doc.addPictureData(newPic, Document.PICTURE_TYPE_JPEG);
            assertEquals(2, doc.getAllPackagePictures().size());
            /* copy data, to avoid instance-equality */
            byte[] newPicCopy = Arrays.copyOf(newPic, newPic.length);
            String id2 = doc.addPictureData(newPicCopy, Document.PICTURE_TYPE_JPEG);
            assertEquals(id1, id2);
            doc.getPackage().revert();
        }
    }

    @Test
    void testPictureHandlingHeaderDocumentImages() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_2.docx")) {
            assertEquals(1, doc.getAllPictures().size());
            assertEquals(1, doc.getAllPackagePictures().size());
            assertEquals(1, doc.getHeaderArray(0).getAllPictures().size());
            doc.getPackage().revert();
        }
    }

    @Test
    void testPictureHandlingComplex() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx")) {
            XWPFHeader xwpfHeader = doc.getHeaderArray(0);

            assertEquals(3, doc.getAllPictures().size());
            assertEquals(3, xwpfHeader.getAllPictures().size());
            assertEquals(5, doc.getAllPackagePictures().size());

            byte[] nature1 = XWPFTestDataSamples.getImage("nature1.jpg");
            String id = doc.addPictureData(nature1, Document.PICTURE_TYPE_JPEG);
            POIXMLDocumentPart part1 = xwpfHeader.getRelationById("rId1");
            XWPFPictureData part2 = (XWPFPictureData) doc.getRelationById(id);
            assertSame(part1, part2);

            doc.getPackage().revert();
        }
    }

    @Test
    void testZeroLengthLibreOfficeDocumentWithWaterMarkHeader() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("zero-length.docx")) {
            POIXMLProperties properties = doc.getProperties();

            assertNotNull(properties.getCoreProperties());

            XWPFHeader headerArray = doc.getHeaderArray(0);
            assertEquals(1, headerArray.getAllPictures().size());
            assertEquals("image1.png", headerArray.pictures.get(0).getFileName());
            assertEquals("", headerArray.getText());

            POIXMLProperties.ExtendedProperties extendedProperties = properties.getExtendedProperties();
            assertNotNull(extendedProperties);
            assertEquals(0, extendedProperties.getUnderlyingProperties().getCharacters());
        }
    }

    @Test
    void testSettings() throws IOException {
        XWPFSettings settings = new XWPFSettings();
        assertEquals(100, settings.getZoomPercent());
        settings.setZoomPercent(50);
        assertEquals(50, settings.getZoomPercent());

        assertFalse(settings.getEvenAndOddHeadings());
        settings.setEvenAndOddHeadings(true);
        assertTrue(settings.getEvenAndOddHeadings());

        assertFalse(settings.getMirrorMargins());
        settings.setMirrorMargins(true);
        assertTrue(settings.getMirrorMargins());

        try (XWPFDocument doc = new XWPFDocument()) {
            assertEquals(100, doc.getZoomPercent());

            doc.setZoomPercent(50);
            assertEquals(50, doc.getZoomPercent());

            doc.setZoomPercent(200);
            assertEquals(200, doc.getZoomPercent());

            assertFalse(doc.getEvenAndOddHeadings());
            doc.setEvenAndOddHeadings(true);
            assertTrue(doc.getEvenAndOddHeadings());

            assertFalse(doc.getMirrorMargins());
            doc.setMirrorMargins(true);
            assertTrue(doc.getMirrorMargins());

            try (XWPFDocument back = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(200, back.getZoomPercent());
            }
        }
    }

    @Test
    void testEnforcedWith() throws IOException {
        try (XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("EnforcedWith.docx")) {
            assertTrue(docx.isEnforcedProtection());
        }
    }

    @Test
    void testInsertNewParagraphWithSdt() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createTOC();
            doc.createParagraph();
            try (XWPFDocument docx = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                XWPFParagraph paragraph = docx.getParagraphArray(0);
                XmlCursor xmlCursor = paragraph.getCTP().newCursor();
                XWPFParagraph insertNewParagraph = docx.insertNewParagraph(xmlCursor);

                assertEquals(insertNewParagraph, docx.getParagraphs().get(0));
                assertEquals(insertNewParagraph, docx.getBodyElements().get(1));
            }
        }
    }

    @Test
    void testInputStreamClosed() throws IOException {
        try (TrackingInputStream stream = new TrackingInputStream(
                POIDataSamples.getDocumentInstance().openResourceAsStream("EnforcedWith.docx"))) {
            try (XWPFDocument docx = new XWPFDocument(stream)) {
                assertNotNull(docx.getDocument());
            }
            assertTrue(stream.isClosed(), "stream was closed?");
        }
    }

    @Test
    void testInputStreamNotClosedWhenOptionUsed() throws IOException {
        try (TrackingInputStream stream = new TrackingInputStream(
                POIDataSamples.getDocumentInstance().openResourceAsStream("EnforcedWith.docx"))) {
            try (XWPFDocument docx = new XWPFDocument(stream, false)) {
                assertNotNull(docx.getDocument());
            }
            assertFalse(stream.isClosed(), "stream was not closed?");
        }
    }

    @Test
    void testUnicodePathDocWithCorruptZipEntry() {
        // this is a file that we do not want to be able to parse, as it contains a corrupt zip entry
        POIXMLException ex = assertThrows(POIXMLException.class, () -> {
            try (XWPFDocument doc = new XWPFDocument(
                    POIDataSamples.getDocumentInstance().openResourceAsStream("unicode-path.docx"))) {
                // expect exception here

                assertNotNull(doc);
            }
        });
        assertEquals("InvalidFormatException", ex.getCause().getClass().getSimpleName());
    }

    @Test
    void testChartExIgnored() throws IOException {
        // see https://github.com/apache/poi/pull/982
        try (XWPFDocument doc = new XWPFDocument(
                POIDataSamples.getDocumentInstance().openResourceAsStream("chartex.docx"))) {
            assertNotNull(doc);
        }
    }

    @Test
    @Disabled("XWPF should be able to write to a new Stream when opened Read-Only")
    void testWriteFromReadOnlyOPC() throws Exception {
        try (OPCPackage opc = OPCPackage.open(
                POIDataSamples.getDocumentInstance().getFile("SampleDoc.docx"),
                PackageAccess.READ
            );
            XWPFDocument doc = new XWPFDocument(opc);
            XWPFWordExtractor ext = new XWPFWordExtractor(doc)
        ) {
            final String origText = ext.getText();

            try (XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc);
                XWPFWordExtractor ext2 = new XWPFWordExtractor(doc2)) {
                assertEquals(origText, ext2.getText());
            }
        }
    }

    @Test
    void testFileHandleLeak() {
        //noinspection resource
        assertThrows(POIXMLException.class,
                // Use XSSFWorkbook on purpose here to trigger missing closing of file-handle
                () -> new XSSFWorkbook(
                        POIDataSamples.getDocumentInstance().getFile("clusterfuzz-testcase-minimized-POIXWPFFuzzer-4791943399604224.docx")));
    }
}
