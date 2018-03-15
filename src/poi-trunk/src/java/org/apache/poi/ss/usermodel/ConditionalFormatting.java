/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * The ConditionalFormatting class encapsulates all settings of Conditional Formatting.
 *
 * The class can be used
 *
 * <UL>
 * <LI>
 * to make a copy ConditionalFormatting settings.
 * </LI>
 *
 *
 * For example:
 * <PRE>
 * ConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
 * newSheet.addConditionalFormatting(cf);
 * </PRE>
 *
 *  <LI>
 *  or to modify existing Conditional Formatting settings (formatting regions and/or rules).
 *  </LI>
 *  </UL>
 *
 * Use {@link org.apache.poi.ss.usermodel.Sheet#getSheetConditionalFormatting()}
 * to get access to an instance of this class.
 * <P>
 * To create a new Conditional Formatting set use the following approach:
 *
 * <PRE>
 *
 * // Define a Conditional Formatting rule, which triggers formatting
 * // when cell's value is greater or equal than 100.0 and
 * // applies patternFormatting defined below.
 * ConditionalFormattingRule rule = sheet.createConditionalFormattingRule(
 *     ComparisonOperator.GE,
 *     "100.0", // 1st formula
 *     null     // 2nd formula is not used for comparison operator GE
 * );
 *
 * // Create pattern with red background
 * PatternFormatting patternFmt = rule.cretePatternFormatting();
 * patternFormatting.setFillBackgroundColor(IndexedColor.RED.getIndex());
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
public interface ConditionalFormatting {

    /**
     * @return array of <tt>CellRangeAddress</tt>s. Never <code>null</code>
     */
    CellRangeAddress[] getFormattingRanges();

    /**
     * Sets the cell ranges the rule conditional formatting must be applied to.
     * @param ranges non-null array of <tt>CellRangeAddress</tt>s
     */
    void setFormattingRanges(CellRangeAddress[] ranges);

    /**
     * Replaces an existing Conditional Formatting rule at position idx.
     * Excel pre-2007 allows to create up to 3 Conditional Formatting rules,
     *  2007 and later allow unlimited numbers.
     * This method can be useful to modify existing  Conditional Formatting rules.
     *
     * @param idx position of the rule. Should be between 0 and 2 for Excel before 2007, otherwise 0+.
     * @param cfRule - Conditional Formatting rule
     */
    void setRule(int idx, ConditionalFormattingRule cfRule);

    /**
     * Add a Conditional Formatting rule.
     * Excel pre-2007 allows to create up to 3 Conditional Formatting rules.
     *
     * @param cfRule - Conditional Formatting rule
     */
    void addRule(ConditionalFormattingRule cfRule);

    /**
     * @return the Conditional Formatting rule at position idx.
     */
    ConditionalFormattingRule getRule(int idx);

    /**
     * @return number of Conditional Formatting rules.
     */
    int getNumberOfRules();
}
