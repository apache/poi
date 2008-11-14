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

package org.apache.poi.hssf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

/**
 *
 * @author ROMANL
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Danny Mui (danny at muibros.com)
 * @author Amol S. Deshmukh &lt; amol at ap ache dot org &gt;
 */
public final class TestNamedRange extends TestCase {

	private static HSSFWorkbook openSample(String sampleFileName) {
		return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
	}

	/** Test of TestCase method, of class test.RangeTest. */
	public void testNamedRange() {
		HSSFWorkbook wb = openSample("Simple.xls");

		//Creating new Named Range
		HSSFName newNamedRange = wb.createName();

		//Getting Sheet Name for the reference
		String sheetName = wb.getSheetName(0);

		//Setting its name
		newNamedRange.setNameName("RangeTest");
		//Setting its reference
		newNamedRange.setReference(sheetName + "!$D$4:$E$8");

		//Getting NAmed Range
		HSSFName namedRange1 = wb.getNameAt(0);
		//Getting it sheet name
		sheetName = namedRange1.getSheetName();

		// sanity check
		SanityChecker c = new SanityChecker();
		c.checkHSSFWorkbook(wb);

		wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		HSSFName nm =wb.getNameAt(wb.getNameIndex("RangeTest"));
		assertTrue("Name is "+nm.getNameName(),"RangeTest".equals(nm.getNameName()));
		assertEquals(wb.getSheetName(0)+"!$D$4:$E$8", nm.getReference());
	}

	/**
	 * Reads an excel file already containing a named range.
	 * <p>
	 * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=9632" target="_bug">#9632</a>
	 */
	public void testNamedRead() {
		HSSFWorkbook wb = openSample("namedinput.xls");

		//Get index of the namedrange with the name = "NamedRangeName" , which was defined in input.xls as A1:D10
		int NamedRangeIndex	 = wb.getNameIndex("NamedRangeName");

		//Getting NAmed Range
		HSSFName namedRange1 = wb.getNameAt(NamedRangeIndex);
		String sheetName = wb.getSheetName(0);

		//Getting its reference
		String reference = namedRange1.getReference();

		assertEquals(sheetName+"!$A$1:$D$10", reference);

		HSSFName namedRange2 = wb.getNameAt(1);

		assertEquals(sheetName+"!$D$17:$G$27", namedRange2.getReference());
		assertEquals("SecondNamedRange", namedRange2.getNameName());
	}

	/**
	 * Reads an excel file already containing a named range and updates it
	 * <p>
	 * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=16411" target="_bug">#16411</a>
	 */
	public void testNamedReadModify() {
		HSSFWorkbook wb = openSample("namedinput.xls");

		HSSFName name = wb.getNameAt(0);
		String sheetName = wb.getSheetName(0);

		assertEquals(sheetName+"!$A$1:$D$10", name.getReference());

		name = wb.getNameAt(1);
		String newReference = sheetName +"!$A$1:$C$36";

		name.setReference(newReference);
		assertEquals(newReference, name.getReference());
	}

	/**
	 * Test that multiple named ranges can be added written and read
	 */
	public void testMultipleNamedWrite() {
		HSSFWorkbook wb	 = new HSSFWorkbook();


		wb.createSheet("testSheet1");
		String sheetName = wb.getSheetName(0);

		assertEquals("testSheet1", sheetName);

		//Creating new Named Range
		HSSFName newNamedRange = wb.createName();

		newNamedRange.setNameName("RangeTest");
		newNamedRange.setReference(sheetName + "!$D$4:$E$8");

		//Creating another new Named Range
		HSSFName newNamedRange2 = wb.createName();

		newNamedRange2.setNameName("AnotherTest");
		newNamedRange2.setReference(sheetName + "!$F$1:$G$6");

		wb.getNameAt(0);

		wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		HSSFName nm =wb.getNameAt(wb.getNameIndex("RangeTest"));
		assertTrue("Name is "+nm.getNameName(),"RangeTest".equals(nm.getNameName()));
		assertTrue("Reference is "+nm.getReference(),(wb.getSheetName(0)+"!$D$4:$E$8").equals(nm.getReference()));

		nm = wb.getNameAt(wb.getNameIndex("AnotherTest"));
		assertTrue("Name is "+nm.getNameName(),"AnotherTest".equals(nm.getNameName()));
		assertTrue("Reference is "+nm.getReference(),newNamedRange2.getReference().equals(nm.getReference()));
	}

	/**
	 * Test case provided by czhang@cambian.com (Chun Zhang)
	 * <p>
	 * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=13775" target="_bug">#13775</a>
	 */
	public void testMultiNamedRange() {

		 // Create a new workbook
		 HSSFWorkbook wb = new HSSFWorkbook ();


		 // Create a worksheet 'sheet1' in the new workbook
		 wb.createSheet ();
		 wb.setSheetName (0, "sheet1");

		 // Create another worksheet 'sheet2' in the new workbook
		 wb.createSheet ();
		 wb.setSheetName (1, "sheet2");

		 // Create a new named range for worksheet 'sheet1'
		 HSSFName namedRange1 = wb.createName();

		 // Set the name for the named range for worksheet 'sheet1'
		 namedRange1.setNameName("RangeTest1");

		 // Set the reference for the named range for worksheet 'sheet1'
		 namedRange1.setReference("sheet1" + "!$A$1:$L$41");

		 // Create a new named range for worksheet 'sheet2'
		 HSSFName namedRange2 = wb.createName();

		 // Set the name for the named range for worksheet 'sheet2'
		 namedRange2.setNameName("RangeTest2");

		 // Set the reference for the named range for worksheet 'sheet2'
		 namedRange2.setReference("sheet2" + "!$A$1:$O$21");

		 // Write the workbook to a file
		 // Read the Excel file and verify its content
		 wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		 HSSFName nm1 =wb.getNameAt(wb.getNameIndex("RangeTest1"));
		 assertTrue("Name is "+nm1.getNameName(),"RangeTest1".equals(nm1.getNameName()));
		 assertTrue("Reference is "+nm1.getReference(),(wb.getSheetName(0)+"!$A$1:$L$41").equals(nm1.getReference()));

		 HSSFName nm2 =wb.getNameAt(wb.getNameIndex("RangeTest2"));
		 assertTrue("Name is "+nm2.getNameName(),"RangeTest2".equals(nm2.getNameName()));
		 assertTrue("Reference is "+nm2.getReference(),(wb.getSheetName(1)+"!$A$1:$O$21").equals(nm2.getReference()));
	 }

	public void testUnicodeNamedRange() {
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("Test");
		HSSFName name = workBook.createName();
		name.setNameName("\u03B1");
		name.setReference("Test!$D$3:$E$8");


		HSSFWorkbook workBook2 = HSSFTestDataSamples.writeOutAndReadBack(workBook);
		HSSFName name2 = workBook2.getNameAt(0);

		assertEquals("\u03B1", name2.getNameName());
		assertEquals("Test!$D$3:$E$8", name2.getReference());
	}

	 /**
	  * Test to see if the print areas can be retrieved/created in memory
	  */
	 public void testSinglePrintArea() {
		 HSSFWorkbook workbook = new HSSFWorkbook();
		 workbook.createSheet("Test Print Area");
		 String sheetName = workbook.getSheetName(0);

		 String reference = "$A$1:$B$1";
		 workbook.setPrintArea(0, reference);

		 String retrievedPrintArea = workbook.getPrintArea(0);

	 	 assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
		 assertEquals("'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
	 }

	 /**
	  * For Convenience, don't force sheet names to be used
	  */
	 public void testSinglePrintAreaWOSheet()
	 {
		 HSSFWorkbook workbook = new HSSFWorkbook();
		 workbook.createSheet("Test Print Area");
		 String sheetName = workbook.getSheetName(0);

		 String reference = "$A$1:$B$1";
		 workbook.setPrintArea(0, reference);

		 String retrievedPrintArea = workbook.getPrintArea(0);

		 assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
		 assertEquals("'" + sheetName + "'!" + reference, retrievedPrintArea);
	 }

	 /**
	  * Test to see if the print area can be retrieved from an excel created file
	  */
	 public void testPrintAreaFileRead() {
		 HSSFWorkbook workbook = openSample("SimpleWithPrintArea.xls");

		String sheetName = workbook.getSheetName(0);
		String reference = sheetName+"!$A$1:$C$5";

		assertEquals(reference, workbook.getPrintArea(0));
	}

	 /**
	  * Test to see if the print area made it to the file
	  */
	 public void testPrintAreaFile() {
	 	HSSFWorkbook workbook = new HSSFWorkbook();
	 	workbook.createSheet("Test Print Area");
	 	String sheetName = workbook.getSheetName(0);


	 	String reference = "$A$1:$B$1";
	 	workbook.setPrintArea(0, reference);

		workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

	 	String retrievedPrintArea = workbook.getPrintArea(0);
	 	assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
	 	assertEquals("References Match", "'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
	}

	/**
	 * Test to see if multiple print areas made it to the file
	 */
	public void testMultiplePrintAreaFile() {
		HSSFWorkbook workbook = new HSSFWorkbook();

		workbook.createSheet("Sheet1");
		workbook.createSheet("Sheet2");
		workbook.createSheet("Sheet3");
		String reference1 = "$A$1:$B$1";
		String reference2 = "$B$2:$D$5";
		String reference3 = "$D$2:$F$5";

		workbook.setPrintArea(0, reference1);
		workbook.setPrintArea(1, reference2);
		workbook.setPrintArea(2, reference3);

		//Check created print areas
		String retrievedPrintArea;

		retrievedPrintArea = workbook.getPrintArea(0);
		assertNotNull("Print Area Not Found (Sheet 1)", retrievedPrintArea);
		assertEquals("Sheet1!" + reference1, retrievedPrintArea);

		retrievedPrintArea = workbook.getPrintArea(1);
		assertNotNull("Print Area Not Found (Sheet 2)", retrievedPrintArea);
		assertEquals("Sheet2!" + reference2, retrievedPrintArea);

		retrievedPrintArea = workbook.getPrintArea(2);
		assertNotNull("Print Area Not Found (Sheet 3)", retrievedPrintArea);
		assertEquals("Sheet3!" + reference3, retrievedPrintArea);

		// Check print areas after re-reading workbook
		workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

		retrievedPrintArea = workbook.getPrintArea(0);
		assertNotNull("Print Area Not Found (Sheet 1)", retrievedPrintArea);
		assertEquals("Sheet1!" + reference1, retrievedPrintArea);

		retrievedPrintArea = workbook.getPrintArea(1);
		assertNotNull("Print Area Not Found (Sheet 2)", retrievedPrintArea);
		assertEquals("Sheet2!" + reference2, retrievedPrintArea);

		retrievedPrintArea = workbook.getPrintArea(2);
		assertNotNull("Print Area Not Found (Sheet 3)", retrievedPrintArea);
		assertEquals("Sheet3!" + reference3, retrievedPrintArea);
	}

	/**
	 * Tests the setting of print areas with coordinates (Row/Column designations)
	 *
	 */
	public void testPrintAreaCoords(){
		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook.createSheet("Test Print Area");
		String sheetName = workbook.getSheetName(0);

		workbook.setPrintArea(0, 0, 1, 0, 0);

		String retrievedPrintArea = workbook.getPrintArea(0);

		assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
		assertEquals("'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
	}


	/**
	 * Tests the parsing of union area expressions, and re-display in the presence of sheet names
	 * with special characters.
	 */
	public void testPrintAreaUnion(){
		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook.createSheet("Test Print Area");
		String sheetName = workbook.getSheetName(0);


		if (false) { // TODO - fix formula parser to support unions
			String reference = "'" + sheetName + "'!$A$1:$B$1,'" + sheetName + "'!$D$1:$F$2";
			workbook.setPrintArea(0, reference);
			String retrievedPrintArea = workbook.getPrintArea(0);
			assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
			assertEquals(reference, retrievedPrintArea);
		}
	}

	/**
	 * Verifies an existing print area is deleted
	 *
	 */
	public void testPrintAreaRemove() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook.createSheet("Test Print Area");
		workbook.getSheetName(0);

		workbook.setPrintArea(0, 0, 1, 0, 0);

		String retrievedPrintArea = workbook.getPrintArea(0);

		assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);

		workbook.removePrintArea(0);
		assertNull("PrintArea was not removed", workbook.getPrintArea(0));
	}

	/**
	 * Verifies correct functioning for "single cell named range" (aka "named cell")
	 */
	public void testNamedCell_1() {

		// setup for this testcase
		String sheetName = "Test Named Cell";
		String cellName = "A name for a named cell";
		String cellValue = "TEST Value";
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);
		sheet.createRow(0).createCell(0).setCellValue(new HSSFRichTextString(cellValue));

		// create named range for a single cell using areareference
		HSSFName namedCell = wb.createName();
		namedCell.setNameName(cellName);
		String reference = "'" + sheetName + "'" + "!A1:A1";
		namedCell.setReference(reference);

		// retrieve the newly created named range
		int namedCellIdx = wb.getNameIndex(cellName);
		HSSFName aNamedCell = wb.getNameAt(namedCellIdx);
		assertNotNull(aNamedCell);

		// retrieve the cell at the named range and test its contents
		AreaReference aref = new AreaReference(aNamedCell.getReference());
		assertTrue("Should be exactly 1 cell in the named cell :'" +cellName+"'", aref.isSingleCell());

		CellReference cref = aref.getFirstCell();
		assertNotNull(cref);
		HSSFSheet s = wb.getSheet(cref.getSheetName());
		assertNotNull(s);
		HSSFRow r = sheet.getRow(cref.getRow());
		HSSFCell c = r.getCell(cref.getCol());
		String contents = c.getRichStringCellValue().getString();
		assertEquals("Contents of cell retrieved by its named reference", contents, cellValue);
	}

	/**
	 * Verifies correct functioning for "single cell named range" (aka "named cell")
	 */
	public void testNamedCell_2() {

		// setup for this testcase
		String sname = "TestSheet", cname = "TestName", cvalue = "TestVal";
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sname);
		sheet.createRow(0).createCell(0).setCellValue(new HSSFRichTextString(cvalue));

		// create named range for a single cell using cellreference
		HSSFName namedCell = wb.createName();
		namedCell.setNameName(cname);
		String reference = sname+"!A1";
		namedCell.setReference(reference);

		// retrieve the newly created named range
		int namedCellIdx = wb.getNameIndex(cname);
		HSSFName aNamedCell = wb.getNameAt(namedCellIdx);
		assertNotNull(aNamedCell);

		// retrieve the cell at the named range and test its contents
		CellReference cref = new CellReference(aNamedCell.getReference());
		assertNotNull(cref);
		HSSFSheet s = wb.getSheet(cref.getSheetName());
		HSSFRow r = sheet.getRow(cref.getRow());
		HSSFCell c = r.getCell(cref.getCol());
		String contents = c.getRichStringCellValue().getString();
		assertEquals("Contents of cell retrieved by its named reference", contents, cvalue);
	}

	public void testDeletedReference() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("24207.xls");
		assertEquals(2, wb.getNumberOfNames());

		HSSFName name1 = wb.getNameAt(0);
		assertEquals("a", name1.getNameName());
		assertEquals("Sheet1!$A$1", name1.getReference());
		new AreaReference(name1.getReference());
		assertTrue("Successfully constructed first reference", true);

		HSSFName name2 = wb.getNameAt(1);
		assertEquals("b", name2.getNameName());
		assertEquals("Sheet1!#REF!", name2.getReference());
		assertTrue(name2.isDeleted());
		try {
			new AreaReference(name2.getReference());
			fail("attempt to supply an invalid reference to AreaReference constructor results in exception");
		} catch (StringIndexOutOfBoundsException e) { // TODO - use a different exception for this condition
			// expected during successful test
		}
	}

	public void testRepeatingRowsAndColumsNames() {
		// First test that setting RR&C for same sheet more than once only creates a
		// single  Print_Titles built-in record
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("FirstSheet");

		// set repeating rows and columns twice for the first sheet
		for (int i = 0; i < 2; i++) {
			wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 3-1);
			sheet.createFreezePane(0, 3);
		}
		assertEquals(1, wb.getNumberOfNames());
		HSSFName nr1 = wb.getNameAt(0);

		assertEquals("Print_Titles", nr1.getNameName());
		if (false) {
			// 	TODO - full column references not rendering properly, absolute markers not present either
			assertEquals("FirstSheet!$A:$A,FirstSheet!$1:$3", nr1.getReference());
		} else {
			assertEquals("FirstSheet!A:A,FirstSheet!$A$1:$IV$3", nr1.getReference());
		}

		// Save and re-open
		HSSFWorkbook nwb = HSSFTestDataSamples.writeOutAndReadBack(wb);

		assertEquals(1, nwb.getNumberOfNames());
		nr1 = nwb.getNameAt(0);

		assertEquals("Print_Titles", nr1.getNameName());
		assertEquals("FirstSheet!A:A,FirstSheet!$A$1:$IV$3", nr1.getReference());

		// check that setting RR&C on a second sheet causes a new Print_Titles built-in
		// name to be created
		sheet = nwb.createSheet("SecondSheet");
		nwb.setRepeatingRowsAndColumns(1, 1, 2, 0, 0);

		assertEquals(2, nwb.getNumberOfNames());
		HSSFName nr2 = nwb.getNameAt(1);

		assertEquals("Print_Titles", nr2.getNameName());
		assertEquals("SecondSheet!B:C,SecondSheet!$A$1:$IV$1", nr2.getReference());

		if (false) {
			// In case you fancy checking in excel, to ensure it
			//  won't complain about the file now
			try {
				File tempFile = File.createTempFile("POI-45126-", ".xls");
				FileOutputStream fout = new FileOutputStream(tempFile);
				nwb.write(fout);
				fout.close();
				System.out.println("check out " + tempFile.getAbsolutePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
