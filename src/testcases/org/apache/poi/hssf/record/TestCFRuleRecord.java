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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefNPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.util.LittleEndian;
import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * Tests the serialization and deserialization of the TestCFRuleRecord
 * class works correctly.
 */
public final class TestCFRuleRecord {
    @Test
    public void testConstructors () throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        CFRuleRecord rule1 = CFRuleRecord.create(sheet, "7");
        assertEquals(CFRuleBase.CONDITION_TYPE_FORMULA, rule1.getConditionType());
        assertEquals(ComparisonOperator.NO_COMPARISON, rule1.getComparisonOperation());
        assertNotNull(rule1.getParsedExpression1());
        assertSame(Ptg.EMPTY_PTG_ARRAY, rule1.getParsedExpression2());

        CFRuleRecord rule2 = CFRuleRecord.create(sheet, ComparisonOperator.BETWEEN, "2", "5");
        assertEquals(CFRuleBase.CONDITION_TYPE_CELL_VALUE_IS, rule2.getConditionType());
        assertEquals(ComparisonOperator.BETWEEN, rule2.getComparisonOperation());
        assertNotNull(rule2.getParsedExpression1());
        assertNotNull(rule2.getParsedExpression2());

        CFRuleRecord rule3 = CFRuleRecord.create(sheet, ComparisonOperator.EQUAL, null, null);
        assertEquals(CFRuleBase.CONDITION_TYPE_CELL_VALUE_IS, rule3.getConditionType());
        assertEquals(ComparisonOperator.EQUAL, rule3.getComparisonOperation());
        assertSame(Ptg.EMPTY_PTG_ARRAY, rule3.getParsedExpression2());
        assertSame(Ptg.EMPTY_PTG_ARRAY, rule3.getParsedExpression2());
        workbook.close();
    }

    @Test
    public void testCreateCFRuleRecord() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CFRuleRecord record = CFRuleRecord.create(sheet, "7");
        testCFRuleRecord(record);

        // Serialize
        byte [] serializedRecord = record.serialize();

        // Strip header
        byte [] recordData = new byte[serializedRecord.length-4];
        System.arraycopy(serializedRecord, 4, recordData, 0, recordData.length);

        // Deserialize
        record = new CFRuleRecord(TestcaseRecordInputStream.create(CFRuleRecord.sid, recordData));

        // Serialize again
        byte[] output = record.serialize();

        // Compare
        assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

        for (int i = 0; i < recordData.length;i++) {
            assertEquals("CFRuleRecord doesn't match", recordData[i], output[i+4]);
        }
        workbook.close();
    }

    @Test
    public void testCreateCFRule12Record() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CFRule12Record record = CFRule12Record.create(sheet, "7");
        testCFRule12Record(record);

        // Serialize
        byte [] serializedRecord = record.serialize();

        // Strip header
        byte [] recordData = new byte[serializedRecord.length-4];
        System.arraycopy(serializedRecord, 4, recordData, 0, recordData.length);

        // Deserialize
        record = new CFRule12Record(TestcaseRecordInputStream.create(CFRule12Record.sid, recordData));

        // Serialize again
        byte[] output = record.serialize();

        // Compare
        assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

        for (int i = 0; i < recordData.length;i++) {
            assertEquals("CFRule12Record doesn't match", recordData[i], output[i+4]);
        }
        workbook.close();
    }

    @Test
    public void testCreateIconCFRule12Record() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CFRule12Record record = CFRule12Record.create(sheet, IconSet.GREY_5_ARROWS);
        record.getMultiStateFormatting().getThresholds()[1].setType(RangeType.PERCENT.id);
        record.getMultiStateFormatting().getThresholds()[1].setValue(10d);
        record.getMultiStateFormatting().getThresholds()[2].setType(RangeType.NUMBER.id);
        record.getMultiStateFormatting().getThresholds()[2].setValue(-4d);
        
        // Check it 
        testCFRule12Record(record);
        assertEquals(IconSet.GREY_5_ARROWS, record.getMultiStateFormatting().getIconSet());
        assertEquals(5, record.getMultiStateFormatting().getThresholds().length);

        // Serialize
        byte [] serializedRecord = record.serialize();

        // Strip header
        byte [] recordData = new byte[serializedRecord.length-4];
        System.arraycopy(serializedRecord, 4, recordData, 0, recordData.length);

        // Deserialize
        record = new CFRule12Record(TestcaseRecordInputStream.create(CFRule12Record.sid, recordData));
        
        // Check it has the icon, and the right number of thresholds
        assertEquals(IconSet.GREY_5_ARROWS, record.getMultiStateFormatting().getIconSet());
        assertEquals(5, record.getMultiStateFormatting().getThresholds().length);

        // Serialize again
        byte[] output = record.serialize();

        // Compare
        assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

        for (int i = 0; i < recordData.length;i++) {
            assertEquals("CFRule12Record doesn't match", recordData[i], output[i+4]);
        }
        workbook.close();
    }
    
    private void testCFRuleRecord(CFRuleRecord record) {
        testCFRuleBase(record);
        
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
    private void testCFRule12Record(CFRule12Record record) {
        assertEquals(CFRule12Record.sid, record.getFutureRecordType());
        assertEquals("A1", record.getAssociatedRange().formatAsString());
        testCFRuleBase(record);
    }
    private void testCFRuleBase(CFRuleBase record) {
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

        PatternFormatting patternFormatting = new PatternFormatting();
        testPatternFormattingAccessors(patternFormatting);
        assertFalse(record.containsPatternFormattingBlock());
        record.setPatternFormatting(patternFormatting);
        assertTrue(record.containsPatternFormattingBlock());
    }

    private void testPatternFormattingAccessors(PatternFormatting patternFormatting) {
        patternFormatting.setFillBackgroundColor(HSSFColorPredefined.GREEN.getIndex());
        assertEquals(HSSFColorPredefined.GREEN.getIndex(),patternFormatting.getFillBackgroundColor());

        patternFormatting.setFillForegroundColor(HSSFColorPredefined.INDIGO.getIndex());
        assertEquals(HSSFColorPredefined.INDIGO.getIndex(),patternFormatting.getFillForegroundColor());

        patternFormatting.setFillPattern(PatternFormatting.DIAMONDS);
        assertEquals(PatternFormatting.DIAMONDS,patternFormatting.getFillPattern());
    }

    private void testBorderFormattingAccessors(BorderFormatting borderFormatting) {
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

        borderFormatting.setBottomBorderColor(HSSFColorPredefined.AQUA.getIndex());
        assertEquals(HSSFColorPredefined.AQUA.getIndex(), borderFormatting.getBottomBorderColor());

        borderFormatting.setDiagonalBorderColor(HSSFColorPredefined.RED.getIndex());
        assertEquals(HSSFColorPredefined.RED.getIndex(), borderFormatting.getDiagonalBorderColor());

        assertFalse(borderFormatting.isForwardDiagonalOn());
        borderFormatting.setForwardDiagonalOn(true);
        assertTrue(borderFormatting.isForwardDiagonalOn());

        borderFormatting.setLeftBorderColor(HSSFColorPredefined.BLACK.getIndex());
        assertEquals(HSSFColorPredefined.BLACK.getIndex(), borderFormatting.getLeftBorderColor());

        borderFormatting.setRightBorderColor(HSSFColorPredefined.BLUE.getIndex());
        assertEquals(HSSFColorPredefined.BLUE.getIndex(), borderFormatting.getRightBorderColor());

        borderFormatting.setTopBorderColor(HSSFColorPredefined.GOLD.getIndex());
        assertEquals(HSSFColorPredefined.GOLD.getIndex(), borderFormatting.getTopBorderColor());
    }


    private void testFontFormattingAccessors(FontFormatting fontFormatting) {
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

    @Test
    public void testWrite() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CFRuleRecord rr = CFRuleRecord.create(sheet, ComparisonOperator.BETWEEN, "5", "10");

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
        // check all remaining flag bits (some are not well understood yet)
        assertEquals(0x203FFFFF, flags);
        workbook.close();
    }

    private static final byte[] DATA_REFN = {
        // formula extracted from bugzilla 45234 att 22141
        1, 3,
        9, // formula 1 length
        0, 0, 0, -1, -1, 63, 32, 2, -128, 0, 0, 0, 5,
        // formula 1: "=B3=1" (formula is relative to B4)
        76, -1, -1, 0, -64, // tRefN(B1)
        30, 1, 0,
        11,
    };

    /**
     * tRefN and tAreaN tokens must be preserved when re-serializing conditional format formulas
     */
    @Test
    public void testReserializeRefNTokens() {

        RecordInputStream is = TestcaseRecordInputStream.create(CFRuleRecord.sid, DATA_REFN);
        CFRuleRecord rr = new CFRuleRecord(is);
        Ptg[] ptgs = rr.getParsedExpression1();
        assertEquals(3, ptgs.length);
        if (ptgs[0] instanceof RefPtg) {
            throw new AssertionFailedError("Identified bug 45234");
        }
        assertEquals(RefNPtg.class, ptgs[0].getClass());
        RefNPtg refNPtg = (RefNPtg) ptgs[0];
        assertTrue(refNPtg.isColRelative());
        assertTrue(refNPtg.isRowRelative());

        byte[] data = rr.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(CFRuleRecord.sid, DATA_REFN, data);
    }
    
    @Test
    public void testBug53691() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        CFRuleRecord record = CFRuleRecord.create(sheet, ComparisonOperator.BETWEEN, "2", "5");
        
        CFRuleRecord clone = record.clone();
        
        byte [] serializedRecord = record.serialize();
        byte [] serializedClone = clone.serialize();
        assertArrayEquals(serializedRecord, serializedClone);
        workbook.close();
    }
    
    @Test
    public void testBug57231_rewrite() throws IOException {
        HSSFWorkbook wb1 = HSSFITestDataProvider.instance.openSampleWorkbook("57231_MixedGasReport.xls");
        assertEquals(7, wb1.getNumberOfSheets());
        HSSFWorkbook wb2 = HSSFITestDataProvider.instance.writeOutAndReadBack(wb1);
        assertEquals(7, wb2.getNumberOfSheets());
        wb2.close();
        wb1.close();
    }
}
