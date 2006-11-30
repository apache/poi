package org.apache.poi.hssf.usermodel;

import java.io.FileInputStream;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

/**
 * Tests for how HSSFWorkbook behaves with XLS files
 *  with a WORKBOOK directory entry (instead of the more
 *  usual, Workbook)
 */
public class TestUppercaseWorkbook extends TestCase {
	private String dirPath;
	private String xlsA = "WORKBOOK_in_capitals.xls";

	protected void setUp() throws Exception {
		super.setUp();
		
        dirPath = System.getProperty("HSSF.testdata.path");
	}

	/**
	 * Test that we can open a file with WORKBOOK
	 */
	public void testOpen() throws Exception {
		FileInputStream is = new FileInputStream(dirPath + "/" + xlsA);
		
		POIFSFileSystem fs = new POIFSFileSystem(is);

		// Ensure that we have a WORKBOOK entry
		fs.getRoot().getEntry("WORKBOOK");
		assertTrue(true);
		
		// Try to open the workbook
		HSSFWorkbook wb = new HSSFWorkbook(fs);
	}
}
