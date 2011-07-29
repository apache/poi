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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFontFormatting;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCfType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STConditionalFormattingOperator;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;

/**
 * @author Yegor Kozlov
 */
public class XSSFConditionalFormattingRule implements ConditionalFormattingRule {
    private final CTCfRule _cfRule;
    private XSSFSheet _sh;

    /*package*/ XSSFConditionalFormattingRule(XSSFSheet sh){
        _cfRule = CTCfRule.Factory.newInstance();
        _sh = sh;
    }

    /*package*/ XSSFConditionalFormattingRule(XSSFSheet sh, CTCfRule cfRule){
        _cfRule = cfRule;
        _sh = sh;
    }

    /*package*/  CTCfRule getCTCfRule(){
        return _cfRule;
    }

    /*package*/  CTDxf getDxf(boolean create){
        StylesTable styles = _sh.getWorkbook().getStylesSource();
        CTDxf dxf = null;
        if(styles._getDXfsSize() > 0 && _cfRule.isSetDxfId()){
            int dxfId = (int)_cfRule.getDxfId();
            dxf = styles.getDxfAt(dxfId);
        }
        if(create && dxf == null) {
            dxf = CTDxf.Factory.newInstance();
            int dxfId = styles.putDxf(dxf);
            _cfRule.setDxfId(dxfId - 1);
        }
        return dxf;
    }

    /**
     * Create a new border formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - border formatting object, never returns <code>null</code>.
     */
    public XSSFBorderFormatting createBorderFormatting(){
        CTDxf dxf = getDxf(true);
        CTBorder border;
        if(!dxf.isSetBorder()) {
            border = dxf.addNewBorder();
        } else {
            border = dxf.getBorder();
        }

        return new XSSFBorderFormatting(border);
    }

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFBorderFormatting getBorderFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetBorder()) return null;

        return new XSSFBorderFormatting(dxf.getBorder());
     }

    /**
     * Create a new font formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - font formatting object, never returns <code>null</code>.
     */
    public XSSFFontFormatting createFontFormatting(){
        CTDxf dxf = getDxf(true);
        CTFont font;
        if(!dxf.isSetFont()) {
            font = dxf.addNewFont();
        } else {
            font = dxf.getFont();
        }

        return new XSSFFontFormatting(font);
    }

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFFontFormatting getFontFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetFont()) return null;

        return new XSSFFontFormatting(dxf.getFont());
    }

    /**
     * Create a new pattern formatting structure if it does not exist,
     * otherwise just return existing object.
     *
     * @return - pattern formatting object, never returns <code>null</code>.
     */
    public XSSFPatternFormatting createPatternFormatting(){
        CTDxf dxf = getDxf(true);
        CTFill fill;
        if(!dxf.isSetFill()) {
            fill = dxf.addNewFill();
        } else {
            fill = dxf.getFill();
        }

        return new XSSFPatternFormatting(fill);
    }

    /**
     * @return - pattern formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFPatternFormatting getPatternFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetFill()) return null;

        return new XSSFPatternFormatting(dxf.getFill());
    }

    /**
     * Type of conditional formatting rule.
     * <p>
     * MUST be either {@link ConditionalFormattingRule#CONDITION_TYPE_CELL_VALUE_IS}
     * or  {@link ConditionalFormattingRule#CONDITION_TYPE_FORMULA}
     * </p>
     *
     * @return the type of condition
     */
    public byte getConditionType(){
        switch (_cfRule.getType().intValue()){
            case STCfType.INT_EXPRESSION: return ConditionalFormattingRule.CONDITION_TYPE_FORMULA;
            case STCfType.INT_CELL_IS: return ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS;
        }
        return 0;
    }

    /**
     * The comparison function used when the type of conditional formatting is set to
     * {@link ConditionalFormattingRule#CONDITION_TYPE_CELL_VALUE_IS}
     * <p>
     *     MUST be a constant from {@link org.apache.poi.ss.usermodel.ComparisonOperator}
     * </p>
     *
     * @return the conditional format operator
     */
    public byte getComparisonOperation(){
        STConditionalFormattingOperator.Enum op = _cfRule.getOperator();
        if(op == null) return ComparisonOperator.NO_COMPARISON;
        
        switch(op.intValue()){
            case STConditionalFormattingOperator.INT_LESS_THAN: return ComparisonOperator.LT;
            case STConditionalFormattingOperator.INT_LESS_THAN_OR_EQUAL: return ComparisonOperator.LE;
            case STConditionalFormattingOperator.INT_GREATER_THAN: return ComparisonOperator.GT;
            case STConditionalFormattingOperator.INT_GREATER_THAN_OR_EQUAL: return ComparisonOperator.GE;
            case STConditionalFormattingOperator.INT_EQUAL: return ComparisonOperator.EQUAL;
            case STConditionalFormattingOperator.INT_NOT_EQUAL: return ComparisonOperator.NOT_EQUAL;
            case STConditionalFormattingOperator.INT_BETWEEN: return ComparisonOperator.BETWEEN;
            case STConditionalFormattingOperator.INT_NOT_BETWEEN: return ComparisonOperator.NOT_BETWEEN;
        }
        return ComparisonOperator.NO_COMPARISON;
    }

    /**
     * The formula used to evaluate the first operand for the conditional formatting rule.
     * <p>
     * If the condition type is {@link ConditionalFormattingRule#CONDITION_TYPE_CELL_VALUE_IS},
     * this field is the first operand of the comparison.
     * If type is {@link ConditionalFormattingRule#CONDITION_TYPE_FORMULA}, this formula is used
     * to determine if the conditional formatting is applied.
     * </p>
     * <p>
     * If comparison type is {@link ConditionalFormattingRule#CONDITION_TYPE_FORMULA} the formula MUST be a Boolean function
     * </p>
     *
     * @return  the first formula
     */
    public String getFormula1(){
        return _cfRule.sizeOfFormulaArray() > 0 ? _cfRule.getFormulaArray(0) : null;
    }

    /**
     * The formula used to evaluate the second operand of the comparison when
     * comparison type is  {@link ConditionalFormattingRule#CONDITION_TYPE_CELL_VALUE_IS} and operator
     * is either {@link org.apache.poi.ss.usermodel.ComparisonOperator#BETWEEN} or {@link org.apache.poi.ss.usermodel.ComparisonOperator#NOT_BETWEEN}
     *
     * @return  the second formula
     */
    public String getFormula2(){
        return _cfRule.sizeOfFormulaArray() == 2 ? _cfRule.getFormulaArray(1) : null;
    }
}
