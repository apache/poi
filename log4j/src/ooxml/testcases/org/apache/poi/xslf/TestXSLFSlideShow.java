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
package org.apache.poi.xslf;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFBackground;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;

import static org.junit.jupiter.api.Assertions.*;

class TestXSLFSlideShow {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
    private OPCPackage pack;

    @BeforeEach
    void setUp() throws Exception {
		pack = OPCPackage.open(slTests.openResourceAsStream("sample.pptx"));
	}

	@AfterEach
	void tearDown() throws IOException {
    	pack.close();
	}

    @Test
	void testContainsMainContentType() throws Exception {
		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(XSLFRelation.MAIN.getContentType())) {
				found = true;
			}
		}
		assertTrue(found);
	}

    @Test
	void testOpen() throws IOException, OpenXML4JException, XmlException {
		// With the finalized uri, should be fine
		XSLFSlideShow xml = new XSLFSlideShow(pack);
		// Check the core
		assertNotNull(xml.getPresentation());

		// Check it has some slides
		assertNotEquals(0, xml.getSlideReferences().sizeOfSldIdArray());
		assertNotEquals(0, xml.getSlideMasterReferences().sizeOfSldMasterIdArray());

		xml.close();
	}

    @Test
	void testSlideBasics() throws IOException, OpenXML4JException, XmlException {
		XSLFSlideShow xml = new XSLFSlideShow(pack);

		// Should have 1 master
		assertEquals(1, xml.getSlideMasterReferences().sizeOfSldMasterIdArray());

		// Should have three sheets
		assertEquals(2, xml.getSlideReferences().sizeOfSldIdArray());

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
		CTSlideMasterIdListEntry[] masters = xml.getSlideMasterReferences().getSldMasterIdArray();

		// see SlideAtom.USES_MASTER_SLIDE_ID
		assertEquals(0x80000000L, masters[0].getId());
		assertEquals("rId1", masters[0].getId2());
		assertNotNull(xml.getSlideMaster(masters[0]));

		xml.close();
	}

    @Test
	void testMetadataBasics() throws IOException, OpenXML4JException, XmlException {
		XSLFSlideShow xml = new XSLFSlideShow(pack);

		assertNotNull(xml.getProperties().getCoreProperties());
		assertNotNull(xml.getProperties().getExtendedProperties());

		CTProperties props = xml.getProperties().getExtendedProperties().getUnderlyingProperties();
		assertEquals("Microsoft Office PowerPoint", props.getApplication());
		assertEquals(0, props.getCharacters());
		assertEquals(0, props.getLines());

		CoreProperties cprops = xml.getProperties().getCoreProperties();
		assertNull(cprops.getTitle());
		assertFalse(cprops.getUnderlyingProperties().getSubjectProperty().isPresent());

		xml.close();
	}

    @Test
    void testMasterBackground() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFBackground b = ppt.getSlideMasters().get(0).getBackground();
        b.setFillColor(Color.RED);

        XSLFSlide sl = ppt.createSlide();
        XSLFAutoShape as = sl.createAutoShape();
        as.setAnchor(new Rectangle2D.Double(100,100,100,100));
        as.setShapeType(ShapeType.CLOUD);

        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        ppt.close();

        XSLFBackground b2 = ppt2.getSlideMasters().get(0).getBackground();
        assertEquals(Color.RED, b2.getFillColor());

        ppt2.close();
    }
}
