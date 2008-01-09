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
package org.apache.poi.hssf;

import java.io.File;

import org.apache.poi.hssf.model.SharedStringsTable;
import org.apache.poi.hxf.HXFDocument;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;

import junit.framework.TestCase;

public class TestHSSFXML extends TestCase {
	/**
	 * Uses the old style schemas.microsoft.com schema uri
	 */
	private File sampleFileBeta;
	/**
	 * Uses the new style schemas.openxmlformats.org schema uri
	 */
	private File sampleFile;

	protected void setUp() throws Exception {
		super.setUp();
		
		sampleFile = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "sample.xlsx"
		);
		sampleFileBeta = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "sample-beta.xlsx"
		);
	}
	
	public void testContainsMainContentType() throws Exception {
		Package pack = HXFDocument.openPackage(sampleFile);
		
		boolean found = false;
		for(PackagePart part : pack.getParts()) {
			if(part.getContentType().equals(HSSFXML.MAIN_CONTENT_TYPE)) {
				found = true;
			}
			System.out.println(part);
		}
		assertTrue(found);
	}

	public void testOpen() throws Exception {
		HXFDocument.openPackage(sampleFile);
		HXFDocument.openPackage(sampleFileBeta);
		
		HSSFXML xml;
		
		// With an old-style uri, as found in a file produced
		//  with the office 2007 beta, will fail, as we don't
		//  translate things 
		try {
			xml = new HSSFXML(
					HXFDocument.openPackage(sampleFileBeta)
			);
			fail();
		} catch(Exception e) {}
		
		// With the finalised uri, should be fine
		xml = new HSSFXML(
				HXFDocument.openPackage(sampleFile)
		);
		
		// Check it has a workbook
		assertNotNull(xml.getWorkbook());
	}
	
	public void testSheetBasics() throws Exception {
		HSSFXML xml = new HSSFXML(
				HXFDocument.openPackage(sampleFile)
		);
		
		// Should have three sheets
		assertEquals(3, xml.getSheetReferences().sizeOfSheetArray());
		assertEquals(3, xml.getSheetReferences().getSheetArray().length);
		
		// Check they're as expected
		CTSheet[] sheets = xml.getSheetReferences().getSheetArray();
		assertEquals("Sheet1", sheets[0].getName());
		assertEquals("Sheet2", sheets[1].getName());
		assertEquals("Sheet3", sheets[2].getName());
		assertEquals("rId1", sheets[0].getId());
		assertEquals("rId2", sheets[1].getId());
		assertEquals("rId3", sheets[2].getId());
		
		// Now get those objects
		assertNotNull(xml.getSheet(sheets[0]));
		assertNotNull(xml.getSheet(sheets[1]));
		assertNotNull(xml.getSheet(sheets[2]));
	}
	
	public void testMetadataBasics() throws Exception {
		HSSFXML xml = new HSSFXML(
				HXFDocument.openPackage(sampleFile)
		);
		assertNotNull(xml.getCoreProperties());
		assertNotNull(xml.getExtendedProperties());
		
		assertEquals("Microsoft Excel", xml.getExtendedProperties().getApplication());
		assertEquals(0, xml.getExtendedProperties().getCharacters());
		assertEquals(0, xml.getExtendedProperties().getLines());
		
		assertEquals(null, xml.getCoreProperties().getTitleProperty().getValue());
		assertEquals(null, xml.getCoreProperties().getSubjectProperty().getValue());
	}
	
	public void testSharedStringBasics() throws Exception {
		HSSFXML xml = new HSSFXML(
				HXFDocument.openPackage(sampleFile)
		);
		assertNotNull(xml._getSharedStringsTable());
		
		SharedStringsTable sst = xml._getSharedStringsTable();
		assertEquals(10, sst.size());
		
		assertEquals("Lorem", sst.get(0));
		for(int i=0; i<sst.size(); i++) {
			assertEquals(sst.get(i), xml.getSharedString(i));
		}
		
		// Add a few more, then save and reload, checking
		//  changes have been kept
		sst.add("Foo");
		sst.add("Bar");
		sst.set(0, "LoremLorem");
		
		sst.write();
		
		xml = new HSSFXML(xml.getPackage());
		sst = xml._getSharedStringsTable();
		assertEquals(12, sst.size());
		
		assertEquals("LoremLorem", sst.get(0));
		for(int i=0; i<sst.size(); i++) {
			assertEquals(sst.get(i), xml.getSharedString(i));
		}
	}
}