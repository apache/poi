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
package org.apache.poi.hslf;

import java.io.File;

import org.apache.poi.hxf.HXFDocument;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;

import junit.framework.TestCase;

public class TestHSLFXML extends TestCase {
	private File sampleFile;

	protected void setUp() throws Exception {
		super.setUp();
		
		sampleFile = new File(
				System.getProperty("HSLF.testdata.path") +
				File.separator + "sample.pptx"
		);
	}

	public void testContainsMainContentType() throws Exception {
		Package pack = HXFDocument.openPackage(sampleFile);
		
		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(HSLFXML.MAIN_CONTENT_TYPE)) {
				found = true;
			}
			System.out.println(part);
		}
		assertTrue(found);
	}

	public void testOpen() throws Exception {
		HXFDocument.openPackage(sampleFile);
		
		HSLFXML xml;
		
		// With the finalised uri, should be fine
		xml = new HSLFXML(
				HXFDocument.openPackage(sampleFile)
		);
		
		// Check the core
		assertNotNull(xml.getPresentation());
		
		// Check it has some slides
		assertTrue(
			xml.getSlideReferences().sizeOfSldIdArray() > 0
		);
		assertTrue(
				xml.getSlideMasterReferences().sizeOfSldMasterIdArray() > 0
			);
	}
	
	public void testSlideBasics() throws Exception {
		HSLFXML xml = new HSLFXML(
				HXFDocument.openPackage(sampleFile)
		);
		
		// Should have 1 master
		assertEquals(1, xml.getSlideMasterReferences().sizeOfSldMasterIdArray());
		assertEquals(1, xml.getSlideMasterReferences().getSldMasterIdArray().length);
		
		// Should have three sheets
		assertEquals(2, xml.getSlideReferences().sizeOfSldIdArray());
		assertEquals(2, xml.getSlideReferences().getSldIdArray().length);
		
		// Check they're as expected
		CTSlideIdListEntry[] slides = xml.getSlideReferences().getSldIdArray();
		assertEquals(256, slides[0].getId());
		assertEquals(257, slides[1].getId());
		assertEquals("rId2", slides[0].getId2());
		assertEquals("rId3", slides[1].getId2());
		
		// Now get those objects
		assertNotNull(xml.getSlide(slides[0]));
		assertNotNull(xml.getSlide(slides[1]));
		
		// And check they have notes as expected
		assertNotNull(xml.getNotes(slides[0]));
		assertNotNull(xml.getNotes(slides[1]));
		
		// And again for the master
		CTSlideMasterIdListEntry[] masters =
			xml.getSlideMasterReferences().getSldMasterIdArray();
		assertEquals(2147483648l, masters[0].getId());
		assertEquals("rId1", masters[0].getId2());
		assertNotNull(xml.getSlideMaster(masters[0]));
	}
	
	public void testMetadataBasics() throws Exception {
		HSLFXML xml = new HSLFXML(
				HXFDocument.openPackage(sampleFile)
		);
		
		assertNotNull(xml.getCoreProperties());
		assertNotNull(xml.getExtendedProperties());
		
		assertEquals("Microsoft Office PowerPoint", xml.getExtendedProperties().getApplication());
		assertEquals(0, xml.getExtendedProperties().getCharacters());
		assertEquals(0, xml.getExtendedProperties().getLines());
		
		assertEquals(null, xml.getCoreProperties().getTitleProperty().getValue());
		assertEquals(null, xml.getCoreProperties().getSubjectProperty().getValue());
	}
}
