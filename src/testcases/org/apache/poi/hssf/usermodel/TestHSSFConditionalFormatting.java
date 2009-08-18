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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author Dmitriy Kumshayev
 */
public final class TestHSSFConditionalFormatting extends TestCase {
	public void testCreateCF() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		String formula = "7";

		HSSFSheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

		HSSFConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(formula);
		HSSFFontFormatting fontFmt = rule1.createFontFormatting();
		fontFmt.setFontStyle(true, false);

		HSSFBorderFormatting bordFmt = rule1.createBorderFormatting();
		bordFmt.setBorderBottom(HSSFBorderFormatting.BORDER_THIN);
		bordFmt.setBorderTop(HSSFBorderFormatting.BORDER_THICK);
		bordFmt.setBorderLeft(HSSFBorderFormatting.BORDER_DASHED);
		bordFmt.setBorderRight(HSSFBorderFormatting.BORDER_DOTTED);

		HSSFPatternFormatting patternFmt = rule1.createPatternFormatting();
		patternFmt.setFillBackgroundColor(HSSFColor.YELLOW.index);


		HSSFConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2");
		HSSFConditionalFormattingRule [] cfRules =
		{
			rule1, rule2
		};

		short col = 1;
		CellRangeAddress [] regions = {
			new CellRangeAddress(0, 65535, col, col)
		};

		sheetCF.addConditionalFormatting(regions, cfRules);
		sheetCF.addConditionalFormatting(regions, cfRules);

		// Verification
		assertEquals(2, sheetCF.getNumConditionalFormattings());
		sheetCF.removeConditionalFormatting(1);
		assertEquals(1, sheetCF.getNumConditionalFormattings());
		HSSFConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
		assertNotNull(cf);

		regions = cf.getFormattingRanges();
		assertNotNull(regions);
		assertEquals(1, regions.length);
		CellRangeAddress r = regions[0];
		assertEquals(1, r.getFirstColumn());
		assertEquals(1, r.getLastColumn());
		assertEquals(0, r.getFirstRow());
		assertEquals(65535, r.getLastRow());

		assertEquals(2, cf.getNumberOfRules());

		rule1 = cf.getRule(0);
		assertEquals("7",rule1.getFormula1());
		assertNull(rule1.getFormula2());

		HSSFFontFormatting    r1fp = rule1.getFontFormatting();
		assertNotNull(r1fp);

		assertTrue(r1fp.isItalic());
		assertFalse(r1fp.isBold());

		HSSFBorderFormatting  r1bf = rule1.getBorderFormatting();
		assertNotNull(r1bf);
		assertEquals(HSSFBorderFormatting.BORDER_THIN, r1bf.getBorderBottom());
		assertEquals(HSSFBorderFormatting.BORDER_THICK,r1bf.getBorderTop());
		assertEquals(HSSFBorderFormatting.BORDER_DASHED,r1bf.getBorderLeft());
		assertEquals(HSSFBorderFormatting.BORDER_DOTTED,r1bf.getBorderRight());

		HSSFPatternFormatting r1pf = rule1.getPatternFormatting();
		assertNotNull(r1pf);
		assertEquals(HSSFColor.YELLOW.index,r1pf.getFillBackgroundColor());

		rule2 = cf.getRule(1);
		assertEquals("2",rule2.getFormula2());
		assertEquals("1",rule2.getFormula1());
	}

	public void testClone() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		String formula = "7";

		HSSFSheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

		HSSFConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(formula);
		HSSFFontFormatting fontFmt = rule1.createFontFormatting();
		fontFmt.setFontStyle(true, false);

		HSSFPatternFormatting patternFmt = rule1.createPatternFormatting();
		patternFmt.setFillBackgroundColor(HSSFColor.YELLOW.index);


		HSSFConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2");
		HSSFConditionalFormattingRule [] cfRules =
		{
			rule1, rule2
		};

		short col = 1;
		CellRangeAddress [] regions = {
			new CellRangeAddress(0, 65535, col, col)
		};

		sheetCF.addConditionalFormatting(regions, cfRules);

		try {
			wb.cloneSheet(0);
		} catch (RuntimeException e) {
			if (e.getMessage().indexOf("needs to define a clone method") > 0) {
				throw new AssertionFailedError("Indentified bug 45682");
			}
			throw e;
		}
		assertEquals(2, wb.getNumberOfSheets());
	}

	public void testShiftRows() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();

		HSSFSheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

		HSSFConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
				ComparisonOperator.BETWEEN, "sum(A10:A15)", "1+sum(B16:B30)");
		HSSFFontFormatting fontFmt = rule1.createFontFormatting();
		fontFmt.setFontStyle(true, false);

		HSSFPatternFormatting patternFmt = rule1.createPatternFormatting();
		patternFmt.setFillBackgroundColor(HSSFColor.YELLOW.index);
		HSSFConditionalFormattingRule [] cfRules = { rule1, };

		CellRangeAddress [] regions = {
			new CellRangeAddress(2, 4, 0, 0), // A3:A5
		};
		sheetCF.addConditionalFormatting(regions, cfRules);

		// This row-shift should destroy the CF region
		sheet.shiftRows(10, 20, -9);
		assertEquals(0, sheetCF.getNumConditionalFormattings());

		// re-add the CF
		sheetCF.addConditionalFormatting(regions, cfRules);

		// This row shift should only affect the formulas
		sheet.shiftRows(14, 17, 8);
		HSSFConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
		assertEquals("SUM(A10:A23)", cf.getRule(0).getFormula1());
		assertEquals("1+SUM(B24:B30)", cf.getRule(0).getFormula2());

		sheet.shiftRows(0, 8, 21);
		cf = sheetCF.getConditionalFormattingAt(0);
		assertEquals("SUM(A10:A21)", cf.getRule(0).getFormula1());
		assertEquals("1+SUM(#REF!)", cf.getRule(0).getFormula2());
	}
}
