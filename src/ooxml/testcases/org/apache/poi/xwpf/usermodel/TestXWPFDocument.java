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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

public final class TestXWPFDocument {

    @Test
    public void testContainsMainContentType() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
        OPCPackage pack = doc.getPackage();

        boolean found = false;
        for (PackagePart part : pack.getParts()) {
            if (part.getContentType().equals(XWPFRelation.DOCUMENT.getContentType())) {
                found = true;
            }
//            if (false) {
//                // successful tests should be silent
//                System.out.println(part);
//            }
        }
        assertTrue(found);
        
        pack.close();
        doc.close();
    }

    @Test
    public void testOpen() throws Exception {
        // Simple file
        try (XWPFDocument xml1 = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            // Check it has key parts
            assertNotNull(xml1.getDocument());
            assertNotNull(xml1.getDocument().getBody());
            assertNotNull(xml1.getStyle());
        }

        // Complex file
        try (XWPFDocument xml2 = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx")) {
            assertNotNull(xml2.getDocument());
            assertNotNull(xml2.getDocument().getBody());
            assertNotNull(xml2.getStyle());
        }
    }

    @Test
    public void testMetadataBasics() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            assertNotNull(xml.getProperties().getCoreProperties());
            assertNotNull(xml.getProperties().getExtendedProperties());

            assertEquals("Microsoft Office Word", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
            assertEquals(1315, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
            assertEquals(10, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

            assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
            assertFalse(xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().isPresent());
        }
    }

    @Test
    public void testMetadataComplex() throws IOException {
        XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
        assertNotNull(xml.getProperties().getCoreProperties());
        assertNotNull(xml.getProperties().getExtendedProperties());

        assertEquals("Microsoft Office Outlook", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
        assertEquals(5184, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
        assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

        assertEquals(" ", xml.getProperties().getCoreProperties().getTitle());
        assertEquals(" ", xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().get());
        xml.close();
    }

    @Test
    public void testWorkbookProperties() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        POIXMLProperties props = doc.getProperties();
        assertNotNull(props);
        assertEquals("Apache POI", props.getExtendedProperties().getUnderlyingProperties().getApplication());
        doc.close();
    }

    @Test
    public void testAddParagraph() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
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
        doc.close();
    }

    @Test
    public void testAddPicture() throws IOException, InvalidFormatException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
        byte[] jpeg = XWPFTestDataSamples.getImage("nature1.jpg");
        String relationId = doc.addPictureData(jpeg, Document.PICTURE_TYPE_JPEG);

        byte[] newJpeg = ((XWPFPictureData) doc.getRelationById(relationId)).getData();
        assertEquals(newJpeg.length, jpeg.length);
        for (int i = 0; i < jpeg.length; i++) {
            assertEquals(newJpeg[i], jpeg[i]);
        }
        doc.close();
    }

    @Test
    public void testAllPictureFormats() throws IOException, InvalidFormatException {
        XWPFDocument doc = new XWPFDocument();

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

        assertEquals(11, doc.getAllPictures().size());

        XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc);
        assertEquals(11, doc2.getAllPictures().size());
        doc2.close();
        doc.close();
    }

    @Test
    public void testRemoveBodyElement() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
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
        assertEquals(true, doc.removeBodyElement(1));
        assertEquals(3, doc.getParagraphs().size());
        assertEquals(3, doc.getBodyElements().size());

        assertEquals(p1, doc.getBodyElements().get(0));
        assertEquals(p1, doc.getParagraphs().get(0));
        assertEquals(p3, doc.getBodyElements().get(1));
        assertEquals(p3, doc.getParagraphs().get(1));
        assertEquals(p4, doc.getBodyElements().get(2));
        assertEquals(p4, doc.getParagraphs().get(2));

        // Remove the 1st
        assertEquals(true, doc.removeBodyElement(0));
        assertEquals(2, doc.getParagraphs().size());
        assertEquals(2, doc.getBodyElements().size());

        assertEquals(p3, doc.getBodyElements().get(0));
        assertEquals(p3, doc.getParagraphs().get(0));
        assertEquals(p4, doc.getBodyElements().get(1));
        assertEquals(p4, doc.getParagraphs().get(1));

        // Remove the last
        assertEquals(true, doc.removeBodyElement(1));
        assertEquals(1, doc.getParagraphs().size());
        assertEquals(1, doc.getBodyElements().size());

        assertEquals(p3, doc.getBodyElements().get(0));
        assertEquals(p3, doc.getParagraphs().get(0));
        doc.close();
    }

    @Test
    public void testRegisterPackagePictureData() throws IOException, InvalidFormatException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx");

        /* manually assemble a new image package part*/
        OPCPackage opcPckg = doc.getPackage();
        XWPFRelation jpgRelation = XWPFRelation.IMAGE_JPEG;
        PackagePartName partName = PackagingURIHelper.createPartName(jpgRelation.getDefaultFileName().replace('#', '2'));
        PackagePart newImagePart = opcPckg.createPart(partName, jpgRelation.getContentType());
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
        opcPckg.close();
        doc.close();
    }

    @Test
    public void testFindPackagePictureData() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx");
        byte[] nature1 = XWPFTestDataSamples.getImage("nature1.gif");
        XWPFPictureData part = doc.findPackagePictureData(nature1, Document.PICTURE_TYPE_GIF);
        assertNotNull(part);
        assertTrue(doc.getAllPictures().contains(part));
        assertTrue(doc.getAllPackagePictures().contains(part));
        doc.getPackage().revert();
        doc.close();
    }

    @Test
    public void testGetAllPictures() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx");
        List<XWPFPictureData> allPictures = doc.getAllPictures();
        List<XWPFPictureData> allPackagePictures = doc.getAllPackagePictures();

        assertNotNull(allPictures);
        assertEquals(3, allPictures.size());
        for (XWPFPictureData xwpfPictureData : allPictures) {
            assertTrue(allPackagePictures.contains(xwpfPictureData));
        }

        try {
            allPictures.add(allPictures.get(0));
            fail("This list must be unmodifiable!");
        } catch (UnsupportedOperationException e) {
            // all ok
        }

        doc.getPackage().revert();
        doc.close();
    }

    @Test
    public void testGetAllPackagePictures() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx");
        List<XWPFPictureData> allPackagePictures = doc.getAllPackagePictures();

        assertNotNull(allPackagePictures);
        assertEquals(5, allPackagePictures.size());

        try {
            allPackagePictures.add(allPackagePictures.get(0));
            fail("This list must be unmodifiable!");
        } catch (UnsupportedOperationException e) {
            // all ok
        }

        doc.getPackage().revert();
        doc.close();
    }

    @Test
    public void testPictureHandlingSimpleFile() throws IOException, InvalidFormatException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_1.docx");
        assertEquals(1, doc.getAllPackagePictures().size());
        byte[] newPic = XWPFTestDataSamples.getImage("abstract4.jpg");
        String id1 = doc.addPictureData(newPic, Document.PICTURE_TYPE_JPEG);
        assertEquals(2, doc.getAllPackagePictures().size());
        /* copy data, to avoid instance-equality */
        byte[] newPicCopy = Arrays.copyOf(newPic, newPic.length);
        String id2 = doc.addPictureData(newPicCopy, Document.PICTURE_TYPE_JPEG);
        assertEquals(id1, id2);
        doc.getPackage().revert();
        doc.close();
    }

    @Test
    public void testPictureHandlingHeaderDocumentImages() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_2.docx");
        assertEquals(1, doc.getAllPictures().size());
        assertEquals(1, doc.getAllPackagePictures().size());
        assertEquals(1, doc.getHeaderArray(0).getAllPictures().size());
        doc.getPackage().revert();
        doc.close();
    }

    @Test
    public void testPictureHandlingComplex() throws IOException, InvalidFormatException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("issue_51265_3.docx");
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
        doc.close();
    }

    @Test
    public void testZeroLengthLibreOfficeDocumentWithWaterMarkHeader() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("zero-length.docx");
        POIXMLProperties properties = doc.getProperties();

        assertNotNull(properties.getCoreProperties());

        XWPFHeader headerArray = doc.getHeaderArray(0);
        assertEquals(1, headerArray.getAllPictures().size());
        assertEquals("image1.png", headerArray.pictures.get(0).getFileName());
        assertEquals("", headerArray.getText());

        POIXMLProperties.ExtendedProperties extendedProperties = properties.getExtendedProperties();
        assertNotNull(extendedProperties);
        assertEquals(0, extendedProperties.getUnderlyingProperties().getCharacters());
        doc.close();
    }

    @Test
    public void testSettings() throws IOException {
        XWPFSettings settings = new XWPFSettings();
        assertEquals(100, settings.getZoomPercent());
        settings.setZoomPercent(50);
        assertEquals(50, settings.getZoomPercent());
        
        assertEquals(false, settings.getEvenAndOddHeadings());
        settings.setEvenAndOddHeadings(true);
        assertEquals(true, settings.getEvenAndOddHeadings());

        assertEquals(false, settings.getMirrorMargins());
        settings.setMirrorMargins(true);
        assertEquals(true, settings.getMirrorMargins());

        XWPFDocument doc = new XWPFDocument();
        assertEquals(100, doc.getZoomPercent());

        doc.setZoomPercent(50);
        assertEquals(50, doc.getZoomPercent());

        doc.setZoomPercent(200);
        assertEquals(200, doc.getZoomPercent());

        assertEquals(false, doc.getEvenAndOddHeadings());
        doc.setEvenAndOddHeadings(true);
        assertEquals(true, doc.getEvenAndOddHeadings());

        assertEquals(false, doc.getMirrorMargins());
        doc.setMirrorMargins(true);
        assertEquals(true, doc.getMirrorMargins());

        XWPFDocument back = XWPFTestDataSamples.writeOutAndReadBack(doc);
        assertEquals(200, back.getZoomPercent());
        back.close();

//        OutputStream out = new FileOutputStream("/tmp/testZoom.docx");
//        doc.write(out);
//        out.close();

        doc.close();
    }
	
	@Test
	public void testEnforcedWith() throws IOException {
		XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("EnforcedWith.docx");
		assertTrue(docx.isEnforcedProtection());
		docx.close();
	}
	
	@Test
	@Ignore("XWPF should be able to write to a new Stream when opened Read-Only")
	public void testWriteFromReadOnlyOPC() throws Exception {
	    OPCPackage opc = OPCPackage.open(
	            POIDataSamples.getDocumentInstance().getFile("SampleDoc.docx"),
	            PackageAccess.READ
	    );
	    XWPFDocument doc = new XWPFDocument(opc);
	    XWPFWordExtractor ext = new XWPFWordExtractor(doc);
	    String origText = ext.getText();
	    
	    doc = XWPFTestDataSamples.writeOutAndReadBack(doc);
	    ext.close();
	    ext = new XWPFWordExtractor(doc);
	    
	    assertEquals(origText, ext.getText());
	    ext.close();
	}
}
