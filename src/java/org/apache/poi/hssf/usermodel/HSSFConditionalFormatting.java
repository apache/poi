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

import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.aggregates.CFRecordsAggregate;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * HSSFConditionalFormatting class encapsulates all settings of Conditional Formatting.
 * <p/>
 * The class can be used
 * <p/>
 * <ul>
 * <li>
 * to make a copy HSSFConditionalFormatting settings.
 * </li>
 * <p/>
 * For example:
 * <pre>
 * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
 * newSheet.addConditionalFormatting(cf);
 * </pre>
 * <p/>
 * <li>
 * or to modify existing Conditional Formatting settings (formatting regions and/or rules).
 * </li>
 * </ul>
 * <p/>
 * Use {@link org.apache.poi.hssf.usermodel.HSSFSheet#getSheetConditionalFormatting()} to get access to an instance of this class.
 * <p/>
 * To create a new Conditional Formatting set use the following approach:
 * <p/>
 * <pre>
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
 * </pre>
 */
public final class HSSFConditionalFormatting implements ConditionalFormatting {

    private final HSSFWorkbook workbook;
    private final CFRecordsAggregate cfAggregate;

    HSSFConditionalFormatting(HSSFWorkbook workbook, CFRecordsAggregate cfAggregate) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null");
        }
        if (cfAggregate == null) {
            throw new IllegalArgumentException("cfAggregate must not be null");
        }
        this.workbook = workbook;
        this.cfAggregate = cfAggregate;
    }

    CFRecordsAggregate getCFRecordsAggregate() {
        return cfAggregate;
    }

    /**
     * @deprecated (Aug-2008) use {@link HSSFConditionalFormatting#getFormattingRanges()}
     */
    public org.apache.poi.ss.util.Region[] getFormattingRegions() {
        CellRangeAddress[] cellRanges = getFormattingRanges();
        return org.apache.poi.ss.util.Region.convertCellRangesToRegions(cellRanges);
    }

    /**
     * @return array of <tt>CellRangeAddress</tt>s. never <code>null</code>
     */
    public CellRangeAddress[] getFormattingRanges() {
        return cfAggregate.getHeader().getCellRanges();
    }

    /**
     * Replaces an existing Conditional Formatting rule at position idx.
     * Excel allows to create up to 3 Conditional Formatting rules.
     * This method can be useful to modify existing  Conditional Formatting rules.
     *
     * @param idx    position of the rule. should be between 0 and 2
     * @param cfRule Conditional Formatting rule
     */
    public void setRule(int idx, HSSFConditionalFormattingRule cfRule) {
        cfAggregate.setRule(idx, cfRule.getCfRuleRecord());
    }

    public void setRule(int idx, ConditionalFormattingRule cfRule) {
        setRule(idx, (HSSFConditionalFormattingRule) cfRule);
    }

    /**
     * Adds a Conditional Formatting rule.
     * Excel allows to create up to 3 Conditional Formatting rules.
     *
     * @param cfRule Conditional Formatting rule
     */
    public void addRule(HSSFConditionalFormattingRule cfRule) {
        cfAggregate.addRule(cfRule.getCfRuleRecord());
    }

    public void addRule(ConditionalFormattingRule cfRule) {
        addRule((HSSFConditionalFormattingRule) cfRule);
    }

    /**
     * @return the Conditional Formatting rule at position idx
     */
    public HSSFConditionalFormattingRule getRule(int idx) {
        CFRuleRecord ruleRecord = cfAggregate.getRule(idx);
        return new HSSFConditionalFormattingRule(workbook, ruleRecord);
    }

    /**
     * @return number of Conditional Formatting rules
     */
    public int getNumberOfRules() {
        return cfAggregate.getNumberOfRules();
    }

    public String toString() {
        return cfAggregate.toString();
    }
}
