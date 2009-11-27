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

package org.apache.poi.hssf.record;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.util.LittleEndianInput;

/**
 * @author Josh Micich
 */
public final class TestSharedFormulaRecord extends TestCase {

	/**
	 * A sample spreadsheet known to have one sheet with 4 shared formula ranges
	 */
	private static final String SHARED_FORMULA_TEST_XLS = "SharedFormulaTest.xls";
	/**
	 * Binary data for an encoded formula.  Taken from attachment 22062 (bugzilla 45123/45421).
	 * The shared formula is in Sheet1!C6:C21, with text "SUMPRODUCT(--(End_Acct=$C6),--(End_Bal))"
	 * This data is found at offset 0x1A4A (within the shared formula record).
	 * The critical thing about this formula is that it contains shared formula tokens (tRefN*,
	 * tAreaN*) with operand class 'array'.
	 */
	private static final byte[] SHARED_FORMULA_WITH_REF_ARRAYS_DATA = {
		0x1A, 0x00,
		0x63, 0x02, 0x00, 0x00, 0x00,
		0x6C, 0x00, 0x00, 0x02, (byte)0x80,  // tRefNA
		0x0B,
		0x15,
		0x13,
		0x13,
		0x63, 0x03, 0x00, 0x00, 0x00,
		0x15,
		0x13,
		0x13,
		0x42, 0x02, (byte)0xE4, 0x00,
	};

	/**
	 * The method <tt>SharedFormulaRecord.convertSharedFormulas()</tt> converts formulas from
	 * 'shared formula' to 'single cell formula' format.  It is important that token operand
	 * classes are preserved during this transformation, because Excel may not tolerate the
	 * incorrect encoding.  The formula here is one such example (Excel displays #VALUE!).
	 */
	public void testConvertSharedFormulasOperandClasses_bug45123() {

		LittleEndianInput in = TestcaseRecordInputStream.createLittleEndian(SHARED_FORMULA_WITH_REF_ARRAYS_DATA);
		int encodedLen = in.readUShort();
		Ptg[] sharedFormula = Ptg.readTokens(encodedLen, in);

		Ptg[] convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 100, 200);

		RefPtg refPtg = (RefPtg) convertedFormula[1];
		assertEquals("$C101", refPtg.toFormulaString());
		if (refPtg.getPtgClass() == Ptg.CLASS_REF) {
			throw new AssertionFailedError("Identified bug 45123");
		}

		confirmOperandClasses(sharedFormula, convertedFormula);
	}

	private static void confirmOperandClasses(Ptg[] originalPtgs, Ptg[] convertedPtgs) {
		assertEquals(originalPtgs.length, convertedPtgs.length);
		for (int i = 0; i < convertedPtgs.length; i++) {
			Ptg originalPtg = originalPtgs[i];
			Ptg convertedPtg = convertedPtgs[i];
			if (originalPtg.getPtgClass() != convertedPtg.getPtgClass()) {
				throw new ComparisonFailure("Different operand class for token[" + i + "]",
						String.valueOf(originalPtg.getPtgClass()), String.valueOf(convertedPtg.getPtgClass()));
			}
		}
	}

    public void testConvertSharedFormulas() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFEvaluationWorkbook fpb = HSSFEvaluationWorkbook.create(wb);
        Ptg[] sharedFormula, convertedFormula;

        sharedFormula = FormulaParser.parse("A2", fpb, FormulaType.CELL, -1);
        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 0, 0);
        confirmOperandClasses(sharedFormula, convertedFormula);
        //conversion relative to [0,0] should return the original formula
        assertEquals("A2", FormulaRenderer.toFormulaString(fpb, convertedFormula));

        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 1, 0);
        confirmOperandClasses(sharedFormula, convertedFormula);
        //one row down
        assertEquals("A3", FormulaRenderer.toFormulaString(fpb, convertedFormula));

        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 1, 1);
        confirmOperandClasses(sharedFormula, convertedFormula);
        //one row down and one cell right
        assertEquals("B3", FormulaRenderer.toFormulaString(fpb, convertedFormula));

        sharedFormula = FormulaParser.parse("SUM(A1:C1)", fpb, FormulaType.CELL, -1);
        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 0, 0);
        confirmOperandClasses(sharedFormula, convertedFormula);
        assertEquals("SUM(A1:C1)", FormulaRenderer.toFormulaString(fpb, convertedFormula));

        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 1, 0);
        confirmOperandClasses(sharedFormula, convertedFormula);
        assertEquals("SUM(A2:C2)", FormulaRenderer.toFormulaString(fpb, convertedFormula));

        convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 1, 1);
        confirmOperandClasses(sharedFormula, convertedFormula);
        assertEquals("SUM(B2:D2)", FormulaRenderer.toFormulaString(fpb, convertedFormula));
    }

    /**
	 * Make sure that POI preserves {@link SharedFormulaRecord}s
	 */
	public void testPreserveOnReserialize() {
		HSSFWorkbook wb;
		HSSFSheet sheet;
		HSSFCell cellB32769;
		HSSFCell cellC32769;

		// Reading directly from XLS file
		wb = HSSFTestDataSamples.openSampleWorkbook(SHARED_FORMULA_TEST_XLS);
		sheet = wb.getSheetAt(0);
		cellB32769 = sheet.getRow(32768).getCell(1);
		cellC32769 = sheet.getRow(32768).getCell(2);
		// check reading of formulas which are shared (two cells from a 1R x 8C range)
		assertEquals("B32770*2", cellB32769.getCellFormula());
		assertEquals("C32770*2", cellC32769.getCellFormula());
		confirmCellEvaluation(wb, cellB32769, 4);
		confirmCellEvaluation(wb, cellC32769, 6);
		// Confirm this example really does have SharedFormulas.
		// there are 3 others besides the one at A32769:H32769
		assertEquals(4, countSharedFormulas(sheet));


		// Re-serialize and check again
		wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		sheet = wb.getSheetAt(0);
		cellB32769 = sheet.getRow(32768).getCell(1);
		cellC32769 = sheet.getRow(32768).getCell(2);
		assertEquals("B32770*2", cellB32769.getCellFormula());
		confirmCellEvaluation(wb, cellB32769, 4);
		assertEquals(4, countSharedFormulas(sheet));
	}

	public void testUnshareFormulaDueToChangeFormula() {
		HSSFWorkbook wb;
		HSSFSheet sheet;
		HSSFCell cellB32769;
		HSSFCell cellC32769;

		wb = HSSFTestDataSamples.openSampleWorkbook(SHARED_FORMULA_TEST_XLS);
		sheet = wb.getSheetAt(0);
		cellB32769 = sheet.getRow(32768).getCell(1);
		cellC32769 = sheet.getRow(32768).getCell(2);

		// Updating cell formula, causing it to become unshared
		cellB32769.setCellFormula("1+1");
		confirmCellEvaluation(wb, cellB32769, 2);
		// currently (Oct 2008) POI handles this by exploding the whole shared formula group
		assertEquals(3, countSharedFormulas(sheet)); // one less now
		// check that nearby cell of the same group still has the same formula
		assertEquals("C32770*2", cellC32769.getCellFormula());
		confirmCellEvaluation(wb, cellC32769, 6);
	}
	public void testUnshareFormulaDueToDelete() {
		HSSFWorkbook wb;
		HSSFSheet sheet;
		HSSFCell cell;
		final int ROW_IX = 2;

		// changing shared formula cell to blank
		wb = HSSFTestDataSamples.openSampleWorkbook(SHARED_FORMULA_TEST_XLS);
		sheet = wb.getSheetAt(0);

		assertEquals("A$1*2", sheet.getRow(ROW_IX).getCell(1).getCellFormula());
		cell = sheet.getRow(ROW_IX).getCell(1);
		cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
		assertEquals(3, countSharedFormulas(sheet));

		wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		sheet = wb.getSheetAt(0);
		assertEquals("A$1*2", sheet.getRow(ROW_IX+1).getCell(1).getCellFormula());

		// deleting shared formula cell
		wb = HSSFTestDataSamples.openSampleWorkbook(SHARED_FORMULA_TEST_XLS);
		sheet = wb.getSheetAt(0);

		assertEquals("A$1*2", sheet.getRow(ROW_IX).getCell(1).getCellFormula());
		cell = sheet.getRow(ROW_IX).getCell(1);
		sheet.getRow(ROW_IX).removeCell(cell);
		assertEquals(3, countSharedFormulas(sheet));

		wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
		sheet = wb.getSheetAt(0);
		assertEquals("A$1*2", sheet.getRow(ROW_IX+1).getCell(1).getCellFormula());
	}

	private static void confirmCellEvaluation(HSSFWorkbook wb, HSSFCell cell, double expectedValue) {
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		CellValue cv = fe.evaluate(cell);
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(expectedValue, cv.getNumberValue(), 0.0);
	}

	/**
	 * @return the number of {@link SharedFormulaRecord}s encoded for the specified sheet
	 */
	private static int countSharedFormulas(HSSFSheet sheet) {
		Record[] records = RecordInspector.getRecords(sheet, 0);
		int count = 0;
		for (int i = 0; i < records.length; i++) {
			Record rec = records[i];
			if(rec instanceof SharedFormulaRecord) {
				count++;
			}
		}
		return count;
	}
}
