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

/**
 * Represents a description of a conditional formatting rule
 */
public interface ConditionalFormattingRule extends DifferentialStyleProvider {
    /**
     * Create a new border formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - border formatting object, never returns <code>null</code>.
     */
    BorderFormatting createBorderFormatting();

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    BorderFormatting getBorderFormatting();

    /**
     * Create a new font formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - font formatting object, never returns <code>null</code>.
     */
    FontFormatting createFontFormatting();

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    FontFormatting getFontFormatting();

    /**
     * Create a new pattern formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - pattern formatting object, never returns <code>null</code>.
     */
    PatternFormatting createPatternFormatting();

    /**
     * @return - pattern formatting object if defined, <code>null</code> otherwise
     */
    PatternFormatting getPatternFormatting();

    /**
     * @return - databar / data-bar formatting object if defined, <code>null</code> otherwise
     */
    DataBarFormatting getDataBarFormatting();
    
    /**
     * @return - icon / multi-state formatting object if defined, <code>null</code> otherwise
     */
    IconMultiStateFormatting getMultiStateFormatting();
    
    /**
     * @return color scale / color grate formatting object if defined, <code>null</code> otherwise
     */
    ColorScaleFormatting getColorScaleFormatting();
    
    /**
     *
     * @return number format defined for this rule, or null if the cell default should be used
     */
    ExcelNumberFormat getNumberFormat();
    
    /**
     * Type of conditional formatting rule.
     *
     * @return the type of condition
     */
    ConditionType getConditionType();
    
    /**
     * This is null if 
     * <p>
     * <code>{@link #getConditionType()} != {@link ConditionType#FILTER}</code>
     * <p>
     * This is always {@link ConditionFilterType#FILTER} for HSSF rules of type {@link ConditionType#FILTER}.
     * <p>
     * For XSSF filter rules, this will indicate the specific type of filter.
     * 
     * @return filter type for filter rules, or null if not a filter rule.
     */
    ConditionFilterType getConditionFilterType();
    
    /**
     * This is null if 
     * <p>
     * <code>{@link #getConditionFilterType()} == null</code>
     * <p>
     * This means it is always null for HSSF, which does not define the extended condition types.
     * <p>
     * This object contains the additional configuration information for XSSF filter conditions.
     * 
     * @return the Filter Configuration Data, or null if there isn't any
     */
    public ConditionFilterData getFilterConfiguration();

    /**
     * The comparison function used when the type of conditional formatting is set to
     * {@link ConditionType#CELL_VALUE_IS}
     * <p>
     *     MUST be a constant from {@link ComparisonOperator}
     * </p>
     *
     * @return the conditional format operator
     */
    byte getComparisonOperation();

    /**
     * The formula used to evaluate the first operand for the conditional formatting rule.
     * <p>
     * If the condition type is {@link ConditionType#CELL_VALUE_IS},
     * this field is the first operand of the comparison.
     * If type is {@link ConditionType#FORMULA}, this formula is used
     * to determine if the conditional formatting is applied.
     * </p>
     * <p>
     * If comparison type is {@link ConditionType#FORMULA} the formula MUST be a Boolean function
     * </p>
     *
     * @return  the first formula
     */
    String getFormula1();

    /**
     * The formula used to evaluate the second operand of the comparison when
     * comparison type is  {@link ConditionType#CELL_VALUE_IS} and operator
     * is either {@link ComparisonOperator#BETWEEN} or {@link ComparisonOperator#NOT_BETWEEN}
     *
     * @return  the second formula
     */
    String getFormula2();

    /**
     * XSSF rules store textual condition values as an attribute and also as a formula that needs shifting.  Using the attribute is simpler/faster.
     * HSSF rules don't have this and return null.  We can fall back on the formula for those (AFAIK).
     * @return condition text if it exists, or null
     */
    String getText();
    
    /**
     * The priority of the rule, if defined, otherwise 0.
     * <p>
     * If priority is 0, just use definition order, as that's how older HSSF rules 
     *  are evaluated.
     * <p>
     * For XSSF, this should always be set. For HSSF, only newer style rules
     *  have this set, older ones will return 0.
     * <p>
     * If a rule is created but not yet added to a sheet, this value may not be valid.
     * @return rule priority
     */
    int getPriority();
    
    /**
     * Always true for HSSF rules, optional flag for XSSF rules.
     * See Excel help for more.
     * 
     * @return true if conditional formatting rule processing stops when this one is true, false if not
     * @see <a href="https://support.office.com/en-us/article/Manage-conditional-formatting-rule-precedence-063cde21-516e-45ca-83f5-8e8126076249">Microsoft Excel help</a>
     */
    boolean getStopIfTrue();
}
