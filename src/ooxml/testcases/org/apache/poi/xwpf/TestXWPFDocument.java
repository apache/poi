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

import java.io.File;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;

import junit.framework.TestCase;

public class TestXWPFDocument extends TestCase {
	private File sampleFile;
	private File complexFile;

	protected void setUp() throws Exception {
		super.setUp();
		
		sampleFile = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "sample.docx"
		);
		complexFile = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "IllustrativeCases.docx"
		);
		
		assertTrue(sampleFile.exists());
		assertTrue(complexFile.exists());
	}

	public void testContainsMainContentType() throws Exception {
		Package pack = POIXMLDocument.openPackage(sampleFile.toString());
		
		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(XWPFRelation.MAIN_CONTENT_TYPE)) {
				found = true;
			}
			System.out.println(part);
		}
		assertTrue(found);
	}

	public void testOpen() throws Exception {
		POIXMLDocument.openPackage(sampleFile.toString());
		POIXMLDocument.openPackage(complexFile.toString());
		
		new XWPFDocument(
				POIXMLDocument.openPackage(sampleFile.toString())
		);
		new XWPFDocument(
				POIXMLDocument.openPackage(complexFile.toString())
		);
		
		XWPFDocument xml;
		
		// Simple file
		xml = new XWPFDocument(
				POIXMLDocument.openPackage(sampleFile.toString())
		);
		// Check it has key parts
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocument().getBody());
		assertNotNull(xml.getStyle());
		
		// Complex file
		xml = new XWPFDocument(
				POIXMLDocument.openPackage(complexFile.toString())
		);
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocument().getBody());
		assertNotNull(xml.getStyle());
	}
	
	public void testMetadataBasics() throws Exception {
		XWPFDocument xml = new XWPFDocument(
				POIXMLDocument.openPackage(sampleFile.toString())
		);
		assertNotNull(xml.getProperties().getCoreProperties());
		assertNotNull(xml.getProperties().getExtendedProperties());
		
		assertEquals("Microsoft Office Word", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
		assertEquals(1315, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
		assertEquals(10, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());
		
		assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
		assertEquals(null, xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
	}
	
	public void testMetadataComplex() throws Exception {
		XWPFDocument xml = new XWPFDocument(
				POIXMLDocument.openPackage(complexFile.toString())
		);
		assertNotNull(xml.getProperties().getCoreProperties());
		assertNotNull(xml.getProperties().getExtendedProperties());
		
		assertEquals("Microsoft Office Outlook", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
		assertEquals(5184, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
		assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());
		
		assertEquals(" ", xml.getProperties().getCoreProperties().getTitle());
		assertEquals(" ", xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
	}
}
