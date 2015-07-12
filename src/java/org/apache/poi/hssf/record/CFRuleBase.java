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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;

/**
 * Conditional Formatting Rules. This can hold old-style rules
 *   
 * 
 * <p>This is for the older-style Excel conditional formattings,
 *  new-style (Excel 2007+) also make use of {@link CFRule12Record}
 *  and {@link CFExRuleRecord} for their rules.
 */
public abstract class CFRuleBase extends StandardRecord {
    public static final class ComparisonOperator {
        public static final byte NO_COMPARISON = 0;
        public static final byte BETWEEN       = 1;
        public static final byte NOT_BETWEEN   = 2;
        public static final byte EQUAL         = 3;
        public static final byte NOT_EQUAL     = 4;
        public static final byte GT            = 5;
        public static final byte LT            = 6;
        public static final byte GE            = 7;
        public static final byte LE            = 8;
        private static final byte max_operator = 8;
    }

    private byte condition_type;
    // The only kinds that CFRuleRecord handles
    public static final byte CONDITION_TYPE_CELL_VALUE_IS = 1;
    public static final byte CONDITION_TYPE_FORMULA = 2;
    // These are CFRule12Rule only
    public static final byte CONDITION_TYPE_COLOR_SCALE = 3;
    public static final byte CONDITION_TYPE_DATA_BAR = 4;
    public static final byte CONDITION_TYPE_FILTER = 5;
    public static final byte CONDITION_TYPE_ICON_SET = 6;

    private byte comparison_operator;

    private Formula formula1;
    private Formula formula2;

    /** Creates new CFRuleRecord */
    protected CFRuleBase(byte conditionType, byte comparisonOperation) {
        setConditionType(conditionType);
        setComparisonOperation(comparisonOperation);
        formula1 = Formula.create(Ptg.EMPTY_PTG_ARRAY);
        formula2 = Formula.create(Ptg.EMPTY_PTG_ARRAY);
    }
    protected CFRuleBase(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2) {
        this(conditionType, comparisonOperation);
        this.formula1 = Formula.create(formula1);
        this.formula2 = Formula.create(formula2);
    }
    protected CFRuleBase() {}

    public byte getConditionType() {
        return condition_type;
    }
    protected void setConditionType(byte condition_type) {
        if ((this instanceof CFRuleRecord)) {
            if (condition_type == CONDITION_TYPE_CELL_VALUE_IS ||
                condition_type == CONDITION_TYPE_FORMULA) {
                // Good, valid combination
            } else {
                throw new IllegalArgumentException("CFRuleRecord only accepts Value-Is and Formula types");
            }
        }
        this.condition_type = condition_type;
    }

    public void setComparisonOperation(byte operation) {
        if (operation < 0 || operation > ComparisonOperator.max_operator)
            throw new IllegalArgumentException(
                    "Valid operators are only in the range 0 to " +ComparisonOperator.max_operator);
        
        this.comparison_operator = operation;
    }
    public byte getComparisonOperation() {
        return comparison_operator;
    }

    /**
     * get the stack of the 1st expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from
     *	 existing excel file
     * callers should check for null!
     */
    public Ptg[] getParsedExpression1() {
        return formula1.getTokens();
    }
    public void setParsedExpression1(Ptg[] ptgs) {
        formula1 = Formula.create(ptgs);
    }
    protected Formula getFormula1() {
        return formula1;
    }
    protected void setFormula1(Formula formula1) {
        this.formula1 = formula1;
    }

    /**
     * get the stack of the 2nd expression as a list
     *
     * @return array of {@link Ptg}s, possibly <code>null</code>
     */
    public Ptg[] getParsedExpression2() {
        return Formula.getTokens(formula2);
    }
    public void setParsedExpression2(Ptg[] ptgs) {
        formula2 = Formula.create(ptgs);
    }
    protected Formula getFormula2() {
        return formula2;
    }
    protected void setFormula2(Formula formula2) {
        this.formula2 = formula2;
    }

    /**
     * @param ptgs must not be <code>null</code>
     * @return encoded size of the formula tokens (does not include 2 bytes for ushort length)
     */
    protected static int getFormulaSize(Formula formula) {
        return formula.getEncodedTokenSize();
    }

    /**
     * TODO - parse conditional format formulas properly i.e. produce tRefN and tAreaN instead of tRef and tArea
     * this call will produce the wrong results if the formula contains any cell references
     * One approach might be to apply the inverse of SharedFormulaRecord.convertSharedFormulas(Stack, int, int)
     * Note - two extra parameters (rowIx & colIx) will be required. They probably come from one of the Region objects.
     *
     * @return <code>null</code> if <tt>formula</tt> was null.
     */
    protected static Ptg[] parseFormula(String formula, HSSFSheet sheet) {
        if(formula == null) {
            return null;
        }
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        return HSSFFormulaParser.parse(formula, sheet.getWorkbook(), FormulaType.CELL, sheetIndex);
    }
}
