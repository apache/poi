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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.util.LittleEndian;

/**
 * Tests the serialization and deserialization of the TestCFRuleRecord
 * class works correctly.
 *
 * @author Dmitriy Kumshayev 
 */
public final class TestCFRuleRecord extends TestCase
{

	public void testCreateCFRuleRecord () 
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		CFRuleRecord record = CFRuleRecord.create(workbook, "7");
		testCFRuleRecord(record);

		// Serialize
		byte [] serializedRecord = record.serialize();

		// Strip header
		byte [] recordData = new byte[serializedRecord.length-4];
		System.arraycopy(serializedRecord, 4, recordData, 0, recordData.length);

		// Deserialize
		record = new CFRuleRecord(new TestcaseRecordInputStream(CFRuleRecord.sid, (short)recordData.length, recordData));

		// Serialize again
		byte[] output = record.serialize();

		// Compare
		assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

		for (int i = 0; i < recordData.length;i++) 
		{
			assertEquals("CFRuleRecord doesn't match", recordData[i], output[i+4]);
		}
	}

	private void testCFRuleRecord(CFRuleRecord record)
	{
		FontFormatting fontFormatting = new FontFormatting();
		testFontFormattingAccessors(fontFormatting);
		assertFalse(record.containsFontFormattingBlock());
		record.setFontFormatting(fontFormatting);
		assertTrue(record.containsFontFormattingBlock());

		BorderFormatting borderFormatting = new BorderFormatting();
		testBorderFormattingAccessors(borderFormatting);
		assertFalse(record.containsBorderFormattingBlock());
		record.setBorderFormatting(borderFormatting);
		assertTrue(record.containsBorderFormattingBlock());

		assertFalse(record.isLeftBorderModified());
		record.setLeftBorderModified(true);
		assertTrue(record.isLeftBorderModified());

		assertFalse(record.isRightBorderModified());
		record.setRightBorderModified(true);
		assertTrue(record.isRightBorderModified());

		assertFalse(record.isTopBorderModified());
		record.setTopBorderModified(true);
		assertTrue(record.isTopBorderModified());

		assertFalse(record.isBottomBorderModified());
		record.setBottomBorderModified(true);
		assertTrue(record.isBottomBorderModified());

		assertFalse(record.isTopLeftBottomRightBorderModified());
		record.setTopLeftBottomRightBorderModified(true);
		assertTrue(record.isTopLeftBottomRightBorderModified());

		assertFalse(record.isBottomLeftTopRightBorderModified());
		record.setBottomLeftTopRightBorderModified(true);
		assertTrue(record.isBottomLeftTopRightBorderModified());


		PatternFormatting patternFormatting = new PatternFormatting();
		testPatternFormattingAccessors(patternFormatting);
		assertFalse(record.containsPatternFormattingBlock());
		record.setPatternFormatting(patternFormatting);
		assertTrue(record.containsPatternFormattingBlock());

		assertFalse(record.isPatternBackgroundColorModified());
		record.setPatternBackgroundColorModified(true);
		assertTrue(record.isPatternBackgroundColorModified());

		assertFalse(record.isPatternColorModified());
		record.setPatternColorModified(true);
		assertTrue(record.isPatternColorModified());

		assertFalse(record.isPatternStyleModified());
		record.setPatternStyleModified(true);
		assertTrue(record.isPatternStyleModified());
	}

	private void testPatternFormattingAccessors(PatternFormatting patternFormatting)
	{
		patternFormatting.setFillBackgroundColor(HSSFColor.GREEN.index);
		assertEquals(HSSFColor.GREEN.index,patternFormatting.getFillBackgroundColor());

		patternFormatting.setFillForegroundColor(HSSFColor.INDIGO.index);
		assertEquals(HSSFColor.INDIGO.index,patternFormatting.getFillForegroundColor());

		patternFormatting.setFillPattern(PatternFormatting.DIAMONDS);
		assertEquals(PatternFormatting.DIAMONDS,patternFormatting.getFillPattern());
	}
	
	private void testBorderFormattingAccessors(BorderFormatting borderFormatting)
	{
		borderFormatting.setBackwardDiagonalOn(false);
		assertFalse(borderFormatting.isBackwardDiagonalOn());
		borderFormatting.setBackwardDiagonalOn(true);
		assertTrue(borderFormatting.isBackwardDiagonalOn());

		borderFormatting.setBorderBottom(BorderFormatting.BORDER_DOTTED);
		assertEquals(BorderFormatting.BORDER_DOTTED, borderFormatting.getBorderBottom());

		borderFormatting.setBorderDiagonal(BorderFormatting.BORDER_MEDIUM);
		assertEquals(BorderFormatting.BORDER_MEDIUM, borderFormatting.getBorderDiagonal());

		borderFormatting.setBorderLeft(BorderFormatting.BORDER_MEDIUM_DASH_DOT_DOT);
		assertEquals(BorderFormatting.BORDER_MEDIUM_DASH_DOT_DOT, borderFormatting.getBorderLeft());

		borderFormatting.setBorderRight(BorderFormatting.BORDER_MEDIUM_DASHED);
		assertEquals(BorderFormatting.BORDER_MEDIUM_DASHED, borderFormatting.getBorderRight());

		borderFormatting.setBorderTop(BorderFormatting.BORDER_HAIR);
		assertEquals(BorderFormatting.BORDER_HAIR, borderFormatting.getBorderTop());

		borderFormatting.setBottomBorderColor(HSSFColor.AQUA.index);
		assertEquals(HSSFColor.AQUA.index, borderFormatting.getBottomBorderColor());

		borderFormatting.setDiagonalBorderColor(HSSFColor.RED.index);
		assertEquals(HSSFColor.RED.index, borderFormatting.getDiagonalBorderColor());

		assertFalse(borderFormatting.isForwardDiagonalOn());
		borderFormatting.setForwardDiagonalOn(true);
		assertTrue(borderFormatting.isForwardDiagonalOn());

		borderFormatting.setLeftBorderColor(HSSFColor.BLACK.index);
		assertEquals(HSSFColor.BLACK.index, borderFormatting.getLeftBorderColor());

		borderFormatting.setRightBorderColor(HSSFColor.BLUE.index);
		assertEquals(HSSFColor.BLUE.index, borderFormatting.getRightBorderColor());

		borderFormatting.setTopBorderColor(HSSFColor.GOLD.index);
		assertEquals(HSSFColor.GOLD.index, borderFormatting.getTopBorderColor());
	}


	private void testFontFormattingAccessors(FontFormatting fontFormatting)
	{
		// Check for defaults
		assertFalse(fontFormatting.isEscapementTypeModified());
		assertFalse(fontFormatting.isFontCancellationModified());
		assertFalse(fontFormatting.isFontOutlineModified());
		assertFalse(fontFormatting.isFontShadowModified());
		assertFalse(fontFormatting.isFontStyleModified());
		assertFalse(fontFormatting.isUnderlineTypeModified());
		assertFalse(fontFormatting.isFontWeightModified());

		assertFalse(fontFormatting.isBold());
		assertFalse(fontFormatting.isItalic());
		assertFalse(fontFormatting.isOutlineOn());
		assertFalse(fontFormatting.isShadowOn());
		assertFalse(fontFormatting.isStruckout());

		assertEquals(0, fontFormatting.getEscapementType());
		assertEquals(-1, fontFormatting.getFontColorIndex());
		assertEquals(-1, fontFormatting.getFontHeight());
		assertEquals(0, fontFormatting.getFontWeight());
		assertEquals(0, fontFormatting.getUnderlineType());

		fontFormatting.setBold(true);
		assertTrue(fontFormatting.isBold());
		fontFormatting.setBold(false);
		assertFalse(fontFormatting.isBold());

		fontFormatting.setEscapementType(FontFormatting.SS_SUB);
		assertEquals(FontFormatting.SS_SUB, fontFormatting.getEscapementType());
		fontFormatting.setEscapementType(FontFormatting.SS_SUPER);
		assertEquals(FontFormatting.SS_SUPER, fontFormatting.getEscapementType());
		fontFormatting.setEscapementType(FontFormatting.SS_NONE);
		assertEquals(FontFormatting.SS_NONE, fontFormatting.getEscapementType());

		fontFormatting.setEscapementTypeModified(false);
		assertFalse(fontFormatting.isEscapementTypeModified());
		fontFormatting.setEscapementTypeModified(true);
		assertTrue(fontFormatting.isEscapementTypeModified());

		fontFormatting.setFontWieghtModified(false);
		assertFalse(fontFormatting.isFontWeightModified());
		fontFormatting.setFontWieghtModified(true);
		assertTrue(fontFormatting.isFontWeightModified());

		fontFormatting.setFontCancellationModified(false);
		assertFalse(fontFormatting.isFontCancellationModified());
		fontFormatting.setFontCancellationModified(true);
		assertTrue(fontFormatting.isFontCancellationModified());

		fontFormatting.setFontColorIndex((short)10);
		assertEquals(10,fontFormatting.getFontColorIndex());

		fontFormatting.setFontHeight(100);
		assertEquals(100,fontFormatting.getFontHeight());

		fontFormatting.setFontOutlineModified(false);
		assertFalse(fontFormatting.isFontOutlineModified());
		fontFormatting.setFontOutlineModified(true);
		assertTrue(fontFormatting.isFontOutlineModified());

		fontFormatting.setFontShadowModified(false);
		assertFalse(fontFormatting.isFontShadowModified());
		fontFormatting.setFontShadowModified(true);
		assertTrue(fontFormatting.isFontShadowModified());

		fontFormatting.setFontStyleModified(false);
		assertFalse(fontFormatting.isFontStyleModified());
		fontFormatting.setFontStyleModified(true);
		assertTrue(fontFormatting.isFontStyleModified());

		fontFormatting.setItalic(false);
		assertFalse(fontFormatting.isItalic());
		fontFormatting.setItalic(true);
		assertTrue(fontFormatting.isItalic());

		fontFormatting.setOutline(false);
		assertFalse(fontFormatting.isOutlineOn());
		fontFormatting.setOutline(true);
		assertTrue(fontFormatting.isOutlineOn());

		fontFormatting.setShadow(false);
		assertFalse(fontFormatting.isShadowOn());
		fontFormatting.setShadow(true);
		assertTrue(fontFormatting.isShadowOn());

		fontFormatting.setStrikeout(false);
		assertFalse(fontFormatting.isStruckout());
		fontFormatting.setStrikeout(true);
		assertTrue(fontFormatting.isStruckout());

		fontFormatting.setUnderlineType(FontFormatting.U_DOUBLE_ACCOUNTING);
		assertEquals(FontFormatting.U_DOUBLE_ACCOUNTING, fontFormatting.getUnderlineType());

		fontFormatting.setUnderlineTypeModified(false);
		assertFalse(fontFormatting.isUnderlineTypeModified());
		fontFormatting.setUnderlineTypeModified(true);
		assertTrue(fontFormatting.isUnderlineTypeModified());
	}

	public void testWrite() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		CFRuleRecord rr = CFRuleRecord.create(workbook, ComparisonOperator.BETWEEN, "5", "10");

		PatternFormatting patternFormatting = new PatternFormatting();
		patternFormatting.setFillPattern(PatternFormatting.BRICKS);
		rr.setPatternFormatting(patternFormatting);

		byte[] data = rr.serialize();
		assertEquals(26, data.length);
		assertEquals(3, LittleEndian.getShort(data, 6));
		assertEquals(3, LittleEndian.getShort(data, 8));

		int flags = LittleEndian.getInt(data, 10);
		assertEquals("unused flags should be 111", 0x00380000, flags & 0x00380000);
		assertEquals("undocumented flags should be 0000", 0, flags & 0x03C00000); // Otherwise Excel gets unhappy
		assertEquals(0xA03FFFFF, flags);
	}


	public static void main(String[] ignored_args)
	{
		System.out.println("Testing org.apache.poi.hssf.record.CFRuleRecord");
		junit.textui.TestRunner.run(TestCFRuleRecord.class);
	}
}
