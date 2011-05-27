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

package org.apache.poi.xwpf;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

public final class TestXWPFDocument extends TestCase {

	public void testContainsMainContentType() throws Exception {
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
		OPCPackage pack = doc.getPackage();

		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(XWPFRelation.DOCUMENT.getContentType())) {
				found = true;
			}
			if (false) {
				// successful tests should be silent
				System.out.println(part);
			}
		}
		assertTrue(found);
	}

	public void testOpen() throws Exception {
		XWPFDocument xml;

		// Simple file
		xml = XWPFTestDataSamples.openSampleDocument("sample.docx");
		// Check it has key parts
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocument().getBody());
		assertNotNull(xml.getStyle());

		// Complex file
		xml = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocument().getBody());
		assertNotNull(xml.getStyle());
	}

	public void testMetadataBasics() {
		XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("sample.docx");
		assertNotNull(xml.getProperties().getCoreProperties());
		assertNotNull(xml.getProperties().getExtendedProperties());

		assertEquals("Microsoft Office Word", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
		assertEquals(1315, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
		assertEquals(10, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

		assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
		assertEquals(null, xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
	}

	public void testMetadataComplex() {
		XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
		assertNotNull(xml.getProperties().getCoreProperties());
		assertNotNull(xml.getProperties().getExtendedProperties());

		assertEquals("Microsoft Office Outlook", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
		assertEquals(5184, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
		assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

		assertEquals(" ", xml.getProperties().getCoreProperties().getTitle());
		assertEquals(" ", xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
	}

	public void testWorkbookProperties() {
		XWPFDocument doc = new XWPFDocument();
		POIXMLProperties props = doc.getProperties();
		assertNotNull(props);
		assertEquals("Apache POI", props.getExtendedProperties().getUnderlyingProperties().getApplication());
	}
	
	public void testAddParagraph(){
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
	}
	
	public void testAddPicture(){
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
		byte[] jpeg = "This is a jpeg".getBytes();
		try {
			int jpegNum = doc.addPicture(jpeg, XWPFDocument.PICTURE_TYPE_JPEG);
			byte[] newJpeg = doc.getAllPictures().get(jpegNum).getData();
			assertEquals(newJpeg.length, jpeg.length);
			for(int i = 0 ; i < jpeg.length; i++){
				assertEquals(newJpeg[i], jpeg[i]); 
			}
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testRemoveBodyElement() {
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
	}
	
	public void testGIFSupport() throws Exception
	{
	    XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("WithGIF.docx");
	    ArrayList<PackagePart> gifParts = doc.getPackage().getPartsByContentType(XWPFRelation.IMAGE_GIF.getContentType());
	    assertEquals("Expected exactly one GIF part in package.",1,gifParts.size());
	    PackagePart gifPart = gifParts.get(0);
	    
	    List<POIXMLDocumentPart> relations = doc.getRelations();
	    POIXMLDocumentPart gifDocPart = null;
	    for (POIXMLDocumentPart docPart : relations)
        {
            if (gifPart == docPart.getPackagePart())
            {
                assertNull("More than one POIXMLDocumentPart for GIF PackagePart.",gifDocPart);
                gifDocPart = docPart;
            }
        }
	    assertNotNull("GIF part not related to document.xml PackagePart",gifDocPart);
	    assertTrue("XWPFRelation for GIF image was not recognized properly, as the POIXMLDocumentPart created was of a wrong type.",XWPFRelation.IMAGE_GIF.getRelationClass().isInstance(gifDocPart));
	}
}
