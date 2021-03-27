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

import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.aggregates.CFRecordsAggregate;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * HSSFConditionalFormatting class encapsulates all settings of Conditional Formatting. 
 * 
 * The class can be used 
 * 
 * <UL>
 * <LI>
 * to make a copy HSSFConditionalFormatting settings.
 * </LI>
 *  
 * 
 * For example:
 * <PRE>
 * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
 * newSheet.addConditionalFormatting(cf);
 * </PRE>
 * 
 *  <LI>
 *  or to modify existing Conditional Formatting settings (formatting regions and/or rules).
 *  </LI>
 *  </UL>
 * 
 * Use {@link org.apache.poi.hssf.usermodel.HSSFSheet#getSheetConditionalFormatting()} to get access to an instance of this class.
 * <P>
 * To create a new Conditional Formatting set use the following approach:
 * 
 * <PRE>
 * 
 * // Define a Conditional Formatting rule, which triggers formatting
 * // when cell's value is greater or equal than 100.0 and
 * // applies patternFormatting defined below.
 * HSSFConditionalFormattingRule rule = sheet.createConditionalFormattingRule(
 *     ComparisonOperator.GE, 
 *     "100.0", // 1st formula 
 *     null     // 2nd formula is not used for comparison operator GE
 * );
 * 
 * // Create pattern with red background
 * HSSFPatternFormatting patternFmt = rule.cretePatternFormatting();
 * patternFormatting.setFillBackgroundColor(HSSFColor.RED.index);
 * 
 * // Define a region containing first column
 * Region [] regions =
 * {
 *     new Region(1,(short)1,-1,(short)1)
 * };
 *     
 * // Apply Conditional Formatting rule defined above to the regions  
 * sheet.addConditionalFormatting(regions, rule);
 * </PRE>
 */
public final class HSSFConditionalFormatting  implements ConditionalFormatting {
    private final HSSFSheet sheet;
    private final CFRecordsAggregate cfAggregate;

    HSSFConditionalFormatting(HSSFSheet sheet, CFRecordsAggregate cfAggregate) {
        if(sheet == null) {
            throw new IllegalArgumentException("sheet must not be null");
        }
        if(cfAggregate == null) {
            throw new IllegalArgumentException("cfAggregate must not be null");
        }
        this.sheet = sheet; 
        this.cfAggregate = cfAggregate;
    }
    CFRecordsAggregate getCFRecordsAggregate() {
        return cfAggregate;
    }

    /**
     * @return array of <tt>CellRangeAddress</tt>s. never <code>null</code> 
     */
    @Override
    public CellRangeAddress[] getFormattingRanges() {
        return cfAggregate.getHeader().getCellRanges();
    }

    @Override
    public void setFormattingRanges(
            final CellRangeAddress[] ranges) {
        cfAggregate.getHeader().setCellRanges(ranges);
    }

    /**
     * Replaces an existing Conditional Formatting rule at position idx. 
     * Older versions of Excel only allow up to 3 Conditional Formatting rules,
     *  and will ignore rules beyond that, while newer versions are fine.
     * This method can be useful to modify existing  Conditional Formatting rules.
     * 
     * @param idx position of the rule. Should be between 0 and 2 for older Excel versions
     * @param cfRule - Conditional Formatting rule
     */
    public void setRule(int idx, HSSFConditionalFormattingRule cfRule) {
        cfAggregate.setRule(idx, cfRule.getCfRuleRecord());
    }

    @Override
    public void setRule(int idx, ConditionalFormattingRule cfRule){
        setRule(idx, (HSSFConditionalFormattingRule)cfRule);
    }

    /**
     * add a Conditional Formatting rule. 
     * Excel allows to create up to 3 Conditional Formatting rules.
     * @param cfRule - Conditional Formatting rule
     */
    public void addRule(HSSFConditionalFormattingRule cfRule) {
        cfAggregate.addRule(cfRule.getCfRuleRecord());
    }

    @Override
    public void addRule(ConditionalFormattingRule cfRule){
        addRule((HSSFConditionalFormattingRule)cfRule);
    }

    /**
     * @return the Conditional Formatting rule at position idx.
     */
    @Override
    public HSSFConditionalFormattingRule getRule(int idx) {
        CFRuleBase ruleRecord = cfAggregate.getRule(idx);
        return new HSSFConditionalFormattingRule(sheet, ruleRecord);
    }

    /**
     * @return number of Conditional Formatting rules.
     */
    @Override
    public int getNumberOfRules() {
        return cfAggregate.getNumberOfRules();
    }

    @Override
    public String toString() {
        return cfAggregate.toString();
    }
}
