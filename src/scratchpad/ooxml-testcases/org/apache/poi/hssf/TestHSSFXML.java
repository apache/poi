package org.apache.poi.hssf;

import java.io.File;

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
}