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

package org.apache.poi.hssf.model;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.FormulaExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests 'operand class' transformation performed by
 * <tt>OperandClassTransformer</tt> by comparing its results with those
 * directly produced by Excel (in a sample spreadsheet).
 * 
 * @author Josh Micich
 */
public final class TestRVA extends TestCase {

	private static final String NEW_LINE = System.getProperty("line.separator");

	public void testFormulas() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testRVA.xls");
		HSSFSheet sheet = wb.getSheetAt(0);

		int countFailures = 0;
		int countErrors = 0;

		int rowIx = 0;
		while (rowIx < 65535) {
			HSSFRow row = sheet.getRow(rowIx);
			if (row == null) {
				break;
			}
			HSSFCell cell = row.getCell(0);
			if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				break;
			}
			String formula = cell.getCellFormula();
			try {
				confirmCell(cell, formula, wb);
			} catch (AssertionFailedError e) {
				System.out.flush();
				System.err.println("Problem with row[" + rowIx + "] formula '" + formula + "'");
				System.err.println(e.getMessage());
				System.err.flush();
				countFailures++;
			} catch (RuntimeException e) {
				System.err.println("Problem with row[" + rowIx + "] formula '" + formula + "'");
				countErrors++;
				e.printStackTrace();
			}
			rowIx++;
		}
		if (countErrors + countFailures > 0) {
			String msg = "One or more RVA tests failed: countFailures=" + countFailures
					+ " countFailures=" + countErrors + ". See stderr for details.";
			throw new AssertionFailedError(msg);
		}
	}

	private void confirmCell(HSSFCell formulaCell, String formula, HSSFWorkbook wb) {
		Ptg[] excelPtgs = FormulaExtractor.getPtgs(formulaCell);
		Ptg[] poiPtgs = HSSFFormulaParser.parse(formula, wb);
		int nExcelTokens = excelPtgs.length;
		int nPoiTokens = poiPtgs.length;
		if (nExcelTokens != nPoiTokens) {
			if (nExcelTokens == nPoiTokens + 1 && excelPtgs[0].getClass() == AttrPtg.class) {
				// compensate for missing tAttrVolatile, which belongs in any formula 
				// involving OFFSET() et al. POI currently does not insert where required
				Ptg[] temp = new Ptg[nExcelTokens];
				temp[0] = excelPtgs[0];
				System.arraycopy(poiPtgs, 0, temp, 1, nPoiTokens);
				poiPtgs = temp;
			} else {
				throw new RuntimeException("Expected " + nExcelTokens + " tokens but got "
						+ nPoiTokens);
			}
		}
		boolean hasMismatch = false;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nExcelTokens; i++) {
			Ptg poiPtg = poiPtgs[i];
			Ptg excelPtg = excelPtgs[i];
			if (excelPtg.getClass() != poiPtg.getClass()) {
				hasMismatch = true;
				sb.append("  mismatch token type[" + i + "] " + getShortClassName(excelPtg) + " "
						+ excelPtg.getRVAType() + " - " + getShortClassName(poiPtg) + " "
						+ poiPtg.getRVAType());
				sb.append(NEW_LINE);
				continue;
			}
			if (poiPtg.isBaseToken()) {
				continue;
			}
			sb.append("  token[" + i + "] " + excelPtg.toString() + " "
					+ excelPtg.getRVAType());

			if (excelPtg.getPtgClass() != poiPtg.getPtgClass()) {
				hasMismatch = true;
				sb.append(" - was " + poiPtg.getRVAType());
			}
			sb.append(NEW_LINE);
		}
		if (false) { // set 'true' to see trace of RVA values
			System.out.println(formulaCell.getRowIndex() + " " + formula);
			System.out.println(sb.toString());
		}
		if (hasMismatch) {
			throw new AssertionFailedError(sb.toString());
		}
	}

	private String getShortClassName(Object o) {
		String cn = o.getClass().getName();
		int pos = cn.lastIndexOf('.');
		return cn.substring(pos + 1);
	}
}
