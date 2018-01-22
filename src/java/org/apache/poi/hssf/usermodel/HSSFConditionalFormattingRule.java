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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.ColorGradientFormatting;
import org.apache.poi.hssf.record.cf.DataBarFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.IconMultiStateFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.ConditionFilterData;
import org.apache.poi.ss.usermodel.ConditionFilterType;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;

/**
 *
 * High level representation of Conditional Formatting Rule.
 * It allows to specify formula based conditions for the Conditional Formatting
 * and the formatting settings such as font, border and pattern.
 */
public final class HSSFConditionalFormattingRule implements ConditionalFormattingRule {
    private static final byte CELL_COMPARISON = CFRuleRecord.CONDITION_TYPE_CELL_VALUE_IS;

    private final CFRuleBase cfRuleRecord;
    private final HSSFWorkbook workbook;
    private final HSSFSheet sheet;

    HSSFConditionalFormattingRule(HSSFSheet pSheet, CFRuleBase pRuleRecord) {
        if (pSheet == null) {
            throw new IllegalArgumentException("pSheet must not be null");
        }
        if (pRuleRecord == null) {
            throw new IllegalArgumentException("pRuleRecord must not be null");
        }
        sheet = pSheet;
        workbook = pSheet.getWorkbook();
        cfRuleRecord = pRuleRecord;
    }

    /**
     * Only newer style formatting rules have priorities. For older ones,
     *  we don't know priority for these, other than definition/model order, 
     *  which appears to be what Excel uses.
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getPriority()
     */
    public int getPriority() {
        CFRule12Record rule12 = getCFRule12Record(false);
        if (rule12 == null) return 0;
        return rule12.getPriority();
    }
    
    /**
     * Always true for HSSF files, per Microsoft Excel documentation
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getStopIfTrue()
     */
    public boolean getStopIfTrue() {
        return true;
    }
    
    CFRuleBase getCfRuleRecord() {
        return cfRuleRecord;
    }
    private CFRule12Record getCFRule12Record(boolean create) {
        if (cfRuleRecord instanceof CFRule12Record) {
            // Good
        } else {
            if (create) throw new IllegalArgumentException("Can't convert a CF into a CF12 record");
            return null;
        }
        return (CFRule12Record)cfRuleRecord;
    }
    
    /**
     * Always null for HSSF records, until someone figures out where to find it
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getNumberFormat()
     */
    public ExcelNumberFormat getNumberFormat() {
        return null;
    }

    private HSSFFontFormatting getFontFormatting(boolean create) {
        FontFormatting fontFormatting = cfRuleRecord.getFontFormatting();
        if (fontFormatting == null) {
            if (!create) return null;
            fontFormatting = new FontFormatting();
            cfRuleRecord.setFontFormatting(fontFormatting);
        }
        return new HSSFFontFormatting(cfRuleRecord, workbook);
    }

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    public HSSFFontFormatting getFontFormatting() {
        return getFontFormatting(false);
    }
    /**
     * create a new font formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - font formatting object, never returns <code>null</code>.
     */
    public HSSFFontFormatting createFontFormatting() {
        return getFontFormatting(true);
    }

    private HSSFBorderFormatting getBorderFormatting(boolean create) {
        BorderFormatting borderFormatting = cfRuleRecord.getBorderFormatting();
        if (borderFormatting == null) {
            if (!create) return null;
            borderFormatting = new BorderFormatting();
            cfRuleRecord.setBorderFormatting(borderFormatting);
        }
        return new HSSFBorderFormatting(cfRuleRecord, workbook);
    }

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    public HSSFBorderFormatting getBorderFormatting() {
        return getBorderFormatting(false);
    }
    /**
     * create a new border formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - border formatting object, never returns <code>null</code>.
     */
    public HSSFBorderFormatting createBorderFormatting() {
        return getBorderFormatting(true);
    }

    private HSSFPatternFormatting getPatternFormatting(boolean create) {
        PatternFormatting patternFormatting = cfRuleRecord.getPatternFormatting();
        if (patternFormatting == null) {
            if (!create) return null;
            patternFormatting = new PatternFormatting();
            cfRuleRecord.setPatternFormatting(patternFormatting);
        }
        return new HSSFPatternFormatting(cfRuleRecord, workbook);
    }

    /**
     * @return - pattern formatting object  if defined, <code>null</code> otherwise
     */
    public HSSFPatternFormatting getPatternFormatting()
    {
        return getPatternFormatting(false);
    }
    /**
     * create a new pattern formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - pattern formatting object, never returns <code>null</code>.
     */
    public HSSFPatternFormatting createPatternFormatting()
    {
        return getPatternFormatting(true);
    }
    
    private HSSFDataBarFormatting getDataBarFormatting(boolean create) {
        CFRule12Record cfRule12Record = getCFRule12Record(create);
        if (cfRule12Record == null) return null;
        
        DataBarFormatting databarFormatting = cfRule12Record.getDataBarFormatting();
        if (databarFormatting == null) {
            if (!create) return null;
            cfRule12Record.createDataBarFormatting();
        }
        
        return new HSSFDataBarFormatting(cfRule12Record, sheet);
    }
    
    /**
     * @return databar / data-bar formatting object if defined, <code>null</code> otherwise
     */
    public HSSFDataBarFormatting getDataBarFormatting() {
        return getDataBarFormatting(false);
    }
    /**
     * create a new databar / data-bar formatting object if it does not exist,
     * otherwise just return the existing object.
     */
    public HSSFDataBarFormatting createDataBarFormatting() {
        return getDataBarFormatting(true);
    }
    
    private HSSFIconMultiStateFormatting getMultiStateFormatting(boolean create) {
        CFRule12Record cfRule12Record = getCFRule12Record(create);
        if (cfRule12Record == null) return null;
        
        IconMultiStateFormatting iconFormatting = cfRule12Record.getMultiStateFormatting();
        if (iconFormatting == null) {
            if (!create) return null;
            cfRule12Record.createMultiStateFormatting();
        }
        return new HSSFIconMultiStateFormatting(cfRule12Record, sheet);
    }
    
    /**
     * @return icon / multi-state formatting object if defined, <code>null</code> otherwise
     */
    public HSSFIconMultiStateFormatting getMultiStateFormatting() {
        return getMultiStateFormatting(false);
    }
    /**
     * create a new icon / multi-state formatting object if it does not exist,
     * otherwise just return the existing object.
     */
    public HSSFIconMultiStateFormatting createMultiStateFormatting() {
        return getMultiStateFormatting(true);
    }
    
    private HSSFColorScaleFormatting getColorScaleFormatting(boolean create) {
        CFRule12Record cfRule12Record = getCFRule12Record(create);
        if (cfRule12Record == null) return null;
        
        ColorGradientFormatting colorFormatting = cfRule12Record.getColorGradientFormatting();
        if (colorFormatting == null) {
            if (!create) return null;
            cfRule12Record.createColorGradientFormatting();
        }

        return new HSSFColorScaleFormatting(cfRule12Record, sheet);
    }
    
    /**
     * @return color scale / gradient formatting object if defined, <code>null</code> otherwise
     */
    public HSSFColorScaleFormatting getColorScaleFormatting() {
        return getColorScaleFormatting(false);
    }
    /**
     * create a new color scale / gradient formatting object if it does not exist,
     * otherwise just return the existing object.
     */
    public HSSFColorScaleFormatting createColorScaleFormatting() {
        return getColorScaleFormatting(true);
    }
    
    /**
     * @return -  the conditiontype for the cfrule
     */
    @Override
    public ConditionType getConditionType() {
        byte code = cfRuleRecord.getConditionType();
        return ConditionType.forId(code);
    }

    /**
     * always null (not a filter condition) or {@link ConditionFilterType#FILTER} if it is.
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getConditionFilterType()
     */
    public ConditionFilterType getConditionFilterType() {
        return getConditionType() == ConditionType.FILTER ? ConditionFilterType.FILTER : null;
    }
    
    public ConditionFilterData getFilterConfiguration() {
        return null;
    }
    
    /**
     * @return - the comparisionoperatation for the cfrule
     */
    @Override
    public byte getComparisonOperation() {
        return cfRuleRecord.getComparisonOperation();
    }

    public String getFormula1()
    {
        return toFormulaString(cfRuleRecord.getParsedExpression1());
    }

    public String getFormula2() {
        byte conditionType = cfRuleRecord.getConditionType();
        if (conditionType == CELL_COMPARISON) {
            byte comparisonOperation = cfRuleRecord.getComparisonOperation();
            switch(comparisonOperation) {
                case ComparisonOperator.BETWEEN:
                case ComparisonOperator.NOT_BETWEEN:
                    return toFormulaString(cfRuleRecord.getParsedExpression2());
            }
        }
        return null;
    }

    public String getText() {
        return null; // not available here, unless it exists and is unimplemented in cfRuleRecord
    }
    
    protected String toFormulaString(Ptg[] parsedExpression) {
        return toFormulaString(parsedExpression, workbook);
    }
    protected static String toFormulaString(Ptg[] parsedExpression, HSSFWorkbook workbook) {
        if(parsedExpression == null || parsedExpression.length == 0) {
            return null;
        }
        return HSSFFormulaParser.toFormulaString(workbook, parsedExpression);
    }
    
    /**
     * Conditional format rules don't define stripes, so always 0
     * @see org.apache.poi.ss.usermodel.DifferentialStyleProvider#getStripeSize()
     */
    public int getStripeSize() {
        return 0;
    }
}
