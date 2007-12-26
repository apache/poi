package org.apache.poi.hssf;

import java.io.File;

import org.apache.poi.HXFDocument;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;

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
	}
}