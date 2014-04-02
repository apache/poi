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
 *
 * @author Dmitriy Kumshayev
 * @author Yegor Kozlov
 */
public interface ConditionalFormattingRule {
    /**
     * This conditional formatting rule compares a cell value
     * to a formula calculated result, using an operator
     */
    public static final byte CONDITION_TYPE_CELL_VALUE_IS = 1;

    /**
     *  This conditional formatting rule contains a formula to evaluate.
     *  When the formula result is true, the cell is highlighted.
     */
    public static final byte CONDITION_TYPE_FORMULA = 2;

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
     * @return - pattern formatting object  if defined,  <code>null</code> otherwise
     */
    PatternFormatting getPatternFormatting();

    /**
     * Type of conditional formatting rule.
     * <p>
     * MUST be either {@link #CONDITION_TYPE_CELL_VALUE_IS} or  {@link #CONDITION_TYPE_FORMULA}
     * </p>
     *
     * @return the type of condition
     */
    byte getConditionType();

    /**
     * The comparison function used when the type of conditional formatting is set to
     * {@link #CONDITION_TYPE_CELL_VALUE_IS}
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
     * If the condition type is {@link #CONDITION_TYPE_CELL_VALUE_IS},
     * this field is the first operand of the comparison.
     * If type is {@link #CONDITION_TYPE_FORMULA}, this formula is used
     * to determine if the conditional formatting is applied.
     * </p>
     * <p>
     * If comparison type is {@link #CONDITION_TYPE_FORMULA} the formula MUST be a Boolean function
     * </p>
     *
     * @return  the first formula
     */
    String getFormula1();

    /**
     * The formula used to evaluate the second operand of the comparison when
     * comparison type is  {@link #CONDITION_TYPE_CELL_VALUE_IS} and operator
     * is either {@link ComparisonOperator#BETWEEN} or {@link ComparisonOperator#NOT_BETWEEN}
     *
     * @return  the second formula
     */
    String getFormula2();
}
