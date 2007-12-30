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
package org.apache.poi.hwpf;

import java.io.File;

import org.apache.poi.hxf.HXFDocument;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;

import junit.framework.TestCase;

public class TestHWPFXML extends TestCase {
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
	}

	public void testContainsMainContentType() throws Exception {
		Package pack = HXFDocument.openPackage(sampleFile);
		
		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(HWPFXML.MAIN_CONTENT_TYPE)) {
				found = true;
			}
			System.out.println(part);
		}
		assertTrue(found);
	}

	public void testOpen() throws Exception {
		HXFDocument.openPackage(sampleFile);
		HXFDocument.openPackage(complexFile);
		
		HWPFXML xml;
		
		// Simple file
		xml = new HWPFXML(
				HXFDocument.openPackage(sampleFile)
		);
		// Check it has key parts
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocumentBody());
		assertNotNull(xml.getStyle());
		
		// Complex file
		xml = new HWPFXML(
				HXFDocument.openPackage(complexFile)
		);
		assertNotNull(xml.getDocument());
		assertNotNull(xml.getDocumentBody());
		assertNotNull(xml.getStyle());
	}
	
	public void testMetadataBasics() throws Exception {
		HWPFXML xml = new HWPFXML(
				HXFDocument.openPackage(sampleFile)
		);
		assertNotNull(xml.getDocumentProperties());
		
		assertEquals("Microsoft Office Word", xml.getDocumentProperties().getApplication());
		assertEquals(1315, xml.getDocumentProperties().getCharacters());
		assertEquals(10, xml.getDocumentProperties().getLines());
	}
}
