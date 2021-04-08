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

import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * The 'Conditional Formatting' facet of <tt>Sheet</tt>
 *
 * @since 3.8
 */
public interface SheetConditionalFormatting {
    /**
     * Add a new Conditional Formatting to the sheet.
     *
     * @param regions - list of rectangular regions to apply conditional formatting rules
     * @param rule -  the rule to apply
     *
     * @return index of the newly created Conditional Formatting object
     */
    int addConditionalFormatting(CellRangeAddress[] regions,
                                 ConditionalFormattingRule rule);

    /**
     * Add a new Conditional Formatting consisting of two rules.
     *
     * @param regions - list of rectangular regions to apply conditional formatting rules
     * @param rule1 -  the first rule
     * @param rule2 -  the second rule
     *
     * @return index of the newly created Conditional Formatting object
     */
    int addConditionalFormatting(CellRangeAddress[] regions,
                                 ConditionalFormattingRule rule1,
                                 ConditionalFormattingRule rule2);

    /**
     * Add a new Conditional Formatting set to the sheet.
     *
     * @param regions - list of rectangular regions to apply conditional formatting rules
     * @param cfRules - set of up to conditional formatting rules (max 3 for Excel pre-2007)
     *
     * @return index of the newly created Conditional Formatting object
     */
    int addConditionalFormatting(CellRangeAddress[] regions, ConditionalFormattingRule[] cfRules);

    /**
     * Adds a copy of a ConditionalFormatting object to the sheet
     * <p>
     *     This method could be used to copy ConditionalFormatting object
     *     from one sheet to another. For example:
     * </p>
     * <pre>
     * ConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
     * newSheet.addConditionalFormatting(cf);
     * </pre>
     *
     * @param cf the Conditional Formatting to clone
     * @return index of the new Conditional Formatting object
     */
    int addConditionalFormatting(ConditionalFormatting cf);

    /**
     * A factory method allowing to create a conditional formatting rule
     * with a cell comparison operator
     * <p>
     * The created conditional formatting rule compares a cell value
     * to a formula calculated result, using the specified operator.
     * The type  of the created condition is {@link ConditionType#CELL_VALUE_IS}
     * </p>
     *
     * @param comparisonOperation - MUST be a constant value from
     *		 <tt>{@link ComparisonOperator}</tt>: <p>
     * <ul>
     *		 <li>BETWEEN</li>
     *		 <li>NOT_BETWEEN</li>
     *		 <li>EQUAL</li>
     *		 <li>NOT_EQUAL</li>
     *		 <li>GT</li>
     *		 <li>LT</li>
     *		 <li>GE</li>
     *		 <li>LE</li>
     * </ul>
     * </p>
     * @param formula1 - formula for the valued, compared with the cell
     * @param formula2 - second formula (only used with
     * {@link ComparisonOperator#BETWEEN}) and {@link ComparisonOperator#NOT_BETWEEN} operations)
     */
    ConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula1,
            String formula2);

    /**
     * Create a conditional formatting rule that compares a cell value
     * to a formula calculated result, using an operator     *
     * <p>
      * The type  of the created condition is {@link ConditionType#CELL_VALUE_IS}
     * </p>
     *
     * @param comparisonOperation  MUST be a constant value from
     *		 <tt>{@link ComparisonOperator}</tt> except  BETWEEN and NOT_BETWEEN
     *
     * @param formula  the formula to determine if the conditional formatting is applied
     */
    ConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula);

    /**
     *  Create a conditional formatting rule based on a Boolean formula.
     *  When the formula result is true, the cell is highlighted.
     *
     * <p>
     *  The type of the created format condition is  {@link ConditionType#FORMULA}
     * </p>
     * @param formula   the formula to evaluate. MUST be a Boolean function.
     */
    ConditionalFormattingRule createConditionalFormattingRule(String formula);

    /**
     * Create a Databar conditional formatting rule.
     * <p>The thresholds and colour for it will be created, but will be 
     *  empty and require configuring with 
     *  {@link ConditionalFormattingRule#getDataBarFormatting()}
     *  then
     *  {@link DataBarFormatting#getMinThreshold()}
     *  and 
     *  {@link DataBarFormatting#getMaxThreshold()}
     */
    ConditionalFormattingRule createConditionalFormattingRule(ExtendedColor color);
    
    /**
     * Create an Icon Set / Multi-State conditional formatting rule.
     * <p>The thresholds for it will be created, but will be empty
     *  and require configuring with 
     *  {@link ConditionalFormattingRule#getMultiStateFormatting()}
     *  then
     *  {@link IconMultiStateFormatting#getThresholds()}
     */
    ConditionalFormattingRule createConditionalFormattingRule(IconSet iconSet);

    /**
     * Create a Color Scale / Color Gradient conditional formatting rule.
     * <p>The thresholds and colours for it will be created, but will be 
     *  empty and require configuring with 
     *  {@link ConditionalFormattingRule#getColorScaleFormatting()}
     *  then
     *  {@link ColorScaleFormatting#getThresholds()}
     *  and
     *  {@link ColorScaleFormatting#getColors()}
     */
    ConditionalFormattingRule createConditionalFormattingColorScaleRule();
    
    /**
    * Gets Conditional Formatting object at a particular index
    *
    * @param index  0-based index of the Conditional Formatting object to fetch
    * @return Conditional Formatting object or <code>null</code> if not found
    * @throws IllegalArgumentException if the index is  outside of the allowable range (0 ... numberOfFormats-1)
    */
    ConditionalFormatting getConditionalFormattingAt(int index);

    /**
     *
     * @return the number of conditional formats in this sheet
     */
    int getNumConditionalFormattings();

    /**
    * Removes a Conditional Formatting object by index
    *
    * @param index 0-based index of the Conditional Formatting object to remove
    * @throws IllegalArgumentException if the index is  outside of the allowable range (0 ... numberOfFormats-1)
    */
    void removeConditionalFormatting(int index);
}
