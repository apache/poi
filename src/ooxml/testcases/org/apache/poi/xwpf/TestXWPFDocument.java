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

import junit.framework.TestCase;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRelation;

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
}
