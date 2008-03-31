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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
/**
 * 
 * @author Dmitriy Kumshayev
 */
public final class TestHSSFConfditionalFormatting extends TestCase
{
	public void XtestLastAndFirstColumns() 
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		String formula = "7";

		HSSFFontFormatting fontFmt = new HSSFFontFormatting();
		fontFmt.setFontStyle(true, false);

		HSSFBorderFormatting bordFmt = new HSSFBorderFormatting();
		bordFmt.setBorderBottom(HSSFBorderFormatting.BORDER_THIN);
		bordFmt.setBorderTop(HSSFBorderFormatting.BORDER_THICK);
		bordFmt.setBorderLeft(HSSFBorderFormatting.BORDER_DASHED);
		bordFmt.setBorderRight(HSSFBorderFormatting.BORDER_DOTTED);

		HSSFPatternFormatting patternFmt = new HSSFPatternFormatting();
		patternFmt.setFillBackgroundColor(HSSFColor.RED.index);

		HSSFConditionalFormattingRule [] cfRules =
		{
			sheet.createConditionalFormattingRule(formula, fontFmt, bordFmt, patternFmt),
			sheet.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2", fontFmt, bordFmt, patternFmt)
		};

		short col = 1;
		Region [] regions =
		{
			new Region(0,col,-1,col)
		};

		sheet.addConditionalFormatting(regions, cfRules);
		sheet.addConditionalFormatting(regions, cfRules);

		// Verification
		assertEquals(2, sheet.getNumConditionalFormattings());
		sheet.removeConditionalFormatting(1);
		assertEquals(1, sheet.getNumConditionalFormattings());
		HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(0);
		assertNotNull(cf);

		regions = cf.getFormattingRegions();
		assertNotNull(regions);
		assertEquals(1, regions.length);
		Region r = regions[0];
		assertEquals(1, r.getColumnFrom());
		assertEquals(1, r.getColumnTo());
		assertEquals(0, r.getRowFrom());
		assertEquals(-1, r.getRowTo());

		assertEquals(2, cf.getNumberOfRules());

		HSSFConditionalFormattingRule rule1 = cf.getRule(0);
		assertEquals("7",rule1.getFormula1()); 
		assertNull(rule1.getFormula2());

		HSSFConditionalFormattingRule rule2 = cf.getRule(1);
		assertEquals("2",rule2.getFormula2()); 
		assertEquals("1",rule2.getFormula1()); 
	}
	
	public void XtestOutput() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		String formula = "7";

		HSSFFontFormatting fontFmt = new HSSFFontFormatting();
		fontFmt.setFontStyle(true, false);

		HSSFBorderFormatting bordFmt = new HSSFBorderFormatting();
		bordFmt.setBorderBottom(HSSFBorderFormatting.BORDER_THIN);
		bordFmt.setBorderTop(HSSFBorderFormatting.BORDER_THICK);
		bordFmt.setBorderLeft(HSSFBorderFormatting.BORDER_DASHED);
		bordFmt.setBorderRight(HSSFBorderFormatting.BORDER_DOTTED);

		HSSFPatternFormatting patternFmt = new HSSFPatternFormatting();
		patternFmt.setFillBackgroundColor(HSSFColor.RED.index);

		HSSFConditionalFormattingRule [] cfRules =
		{
			sheet.createConditionalFormattingRule(formula, fontFmt, bordFmt, patternFmt),
			sheet.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2", fontFmt, bordFmt, patternFmt)
		};

		short col = 1;
		Region [] regions =
		{
			new Region(0,col,-1,col)
		};

		sheet.addConditionalFormatting(regions, cfRules);

		try {
			OutputStream os = new FileOutputStream("C:/josh/temp/cfExample.xls");
			wb.write(os);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void testReadWrite() {
		
		HSSFWorkbook wb;
		try {
			InputStream is = new FileInputStream("C:/josh/temp/cfEx.xls");
			wb = new HSSFWorkbook(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		HSSFSheet sheet = wb.getSheetAt(0);
		
		int nCFs = sheet.getNumConditionalFormattings();
		HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(0);
		Region[] regions = cf.getFormattingRegions();

		sheet.removeConditionalFormatting(0);

		HSSFFontFormatting fontFmt = new HSSFFontFormatting();
		fontFmt.setFontStyle(false, true);
		HSSFConditionalFormattingRule rule1 = cf.getRule(0); 
		HSSFConditionalFormattingRule rule = sheet.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "5", "10",  fontFmt, null, null);

		byte[] rawRecord1 = rule1.getCfRuleRecord().getFontFormatting().getRawRecord();
		for (int i = 0; i < rawRecord1.length; i++) {
			System.out.print(rawRecord1[i] + ",");
		}
		System.out.println();
		
		byte[] rawRecord = fontFmt.getFontFormattingBlock().getRawRecord();
		for (int i = 0; i < rawRecord.length; i++) {
			System.out.print(rawRecord[i]+ ",");
		}
		System.out.println();
		
		rule.getCfRuleRecord().setFontFormatting(rule1.getCfRuleRecord().getFontFormatting());
		
		sheet.addConditionalFormatting(regions, new HSSFConditionalFormattingRule[] { rule, });
		
		
		HSSFWorkbook wb2;
if(false)		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			
			wb2 = new HSSFWorkbook(bais);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
		try {
			OutputStream os = new FileOutputStream("C:/josh/temp/cfEx3.xls");
			wb.write(os);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
	}
}
