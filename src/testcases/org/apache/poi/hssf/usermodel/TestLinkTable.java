package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
/**
 * Tests for LinkTable
 * 
 * @author Josh Micich
 */
public final class TestLinkTable extends TestCase {

	/**
	 * The example file attached to bugzilla 45046 is a clear example of Name records being present
	 * without an External Book (SupBook) record.  Excel has no trouble reading this file.<br/>
	 * TODO get OOO documentation updated to reflect this (that EXTERNALBOOK is optional). 
	 * 
	 * It's not clear what exact steps need to be taken in Excel to create such a workbook 
	 */
	public void testLinkTableWithoutExternalBookRecord_bug45046() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45046-21984.xls");
		} catch (RuntimeException e) {
			if ("DEFINEDNAME is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45046 b");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(3, wb.getNumberOfSheets());
		String formula = wb.getSheetAt(0).getRow(4).getCell(13).getCellFormula();
		
		if ("ipcSummenproduktIntern($P5,N$6,$A$9,N$5)".equals(formula)) {
			// The reported symptom of this bugzilla is an earlier bug (already fixed) 
			throw new AssertionFailedError("Identified bug 41726"); 
			// This is observable in version 3.0
		}
		
		assertEquals("ipcSummenproduktIntern($C5,N$2,$A$9,N$1)", formula);
	}
	
	public void testMultipleExternSheetRecords_bug45698() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45698-22488.xls");
		} catch (RuntimeException e) {
			if ("Extern sheet is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45698");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(7, wb.getNumberOfSheets());
	}
}
