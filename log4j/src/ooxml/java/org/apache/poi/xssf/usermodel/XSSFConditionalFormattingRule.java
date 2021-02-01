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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionFilterData;
import org.apache.poi.ss.usermodel.ConditionFilterType;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfvo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColorScale;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIconSet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCfType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCfvoType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STConditionalFormattingOperator;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STIconSetType;

/**
 * XSSF support for Conditional Formatting rules
 */
public class XSSFConditionalFormattingRule implements ConditionalFormattingRule {
    private final CTCfRule _cfRule;
    private XSSFSheet _sh;

    private static Map<STCfType.Enum, ConditionType> typeLookup = new HashMap<>();
    private static Map<STCfType.Enum, ConditionFilterType> filterTypeLookup = new HashMap<>();
    static {
        typeLookup.put(STCfType.CELL_IS, ConditionType.CELL_VALUE_IS);
        typeLookup.put(STCfType.EXPRESSION, ConditionType.FORMULA);
        typeLookup.put(STCfType.COLOR_SCALE, ConditionType.COLOR_SCALE);
        typeLookup.put(STCfType.DATA_BAR, ConditionType.DATA_BAR);
        typeLookup.put(STCfType.ICON_SET, ConditionType.ICON_SET);

        // These are all subtypes of Filter, we think...
        typeLookup.put(STCfType.TOP_10, ConditionType.FILTER);
        typeLookup.put(STCfType.UNIQUE_VALUES, ConditionType.FILTER);
        typeLookup.put(STCfType.DUPLICATE_VALUES, ConditionType.FILTER);
        typeLookup.put(STCfType.CONTAINS_TEXT, ConditionType.FILTER);
        typeLookup.put(STCfType.NOT_CONTAINS_TEXT, ConditionType.FILTER);
        typeLookup.put(STCfType.BEGINS_WITH, ConditionType.FILTER);
        typeLookup.put(STCfType.ENDS_WITH, ConditionType.FILTER);
        typeLookup.put(STCfType.CONTAINS_BLANKS, ConditionType.FILTER);
        typeLookup.put(STCfType.NOT_CONTAINS_BLANKS, ConditionType.FILTER);
        typeLookup.put(STCfType.CONTAINS_ERRORS, ConditionType.FILTER);
        typeLookup.put(STCfType.NOT_CONTAINS_ERRORS, ConditionType.FILTER);
        typeLookup.put(STCfType.TIME_PERIOD, ConditionType.FILTER);
        typeLookup.put(STCfType.ABOVE_AVERAGE, ConditionType.FILTER);

        filterTypeLookup.put(STCfType.TOP_10, ConditionFilterType.TOP_10);
        filterTypeLookup.put(STCfType.UNIQUE_VALUES, ConditionFilterType.UNIQUE_VALUES);
        filterTypeLookup.put(STCfType.DUPLICATE_VALUES, ConditionFilterType.DUPLICATE_VALUES);
        filterTypeLookup.put(STCfType.CONTAINS_TEXT, ConditionFilterType.CONTAINS_TEXT);
        filterTypeLookup.put(STCfType.NOT_CONTAINS_TEXT, ConditionFilterType.NOT_CONTAINS_TEXT);
        filterTypeLookup.put(STCfType.BEGINS_WITH, ConditionFilterType.BEGINS_WITH);
        filterTypeLookup.put(STCfType.ENDS_WITH, ConditionFilterType.ENDS_WITH);
        filterTypeLookup.put(STCfType.CONTAINS_BLANKS, ConditionFilterType.CONTAINS_BLANKS);
        filterTypeLookup.put(STCfType.NOT_CONTAINS_BLANKS, ConditionFilterType.NOT_CONTAINS_BLANKS);
        filterTypeLookup.put(STCfType.CONTAINS_ERRORS, ConditionFilterType.CONTAINS_ERRORS);
        filterTypeLookup.put(STCfType.NOT_CONTAINS_ERRORS, ConditionFilterType.NOT_CONTAINS_ERRORS);
        filterTypeLookup.put(STCfType.TIME_PERIOD, ConditionFilterType.TIME_PERIOD);
        filterTypeLookup.put(STCfType.ABOVE_AVERAGE, ConditionFilterType.ABOVE_AVERAGE);

    }

    /**
     * NOTE: does not set priority, so this assumes the rule will not be added to the sheet yet
     * @param sh
     */
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
            _cfRule.setDxfId(dxfId - 1L);
        }
        return dxf;
    }

    public int getPriority() {
        final int priority = _cfRule.getPriority();
        // priorities start at 1, if it is less, it is undefined, use definition order in caller
        return priority >=1 ? priority : 0;
    }

    public boolean getStopIfTrue() {
        return _cfRule.getStopIfTrue();
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

        return new XSSFBorderFormatting(border, _sh.getWorkbook().getStylesSource().getIndexedColors());
    }

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFBorderFormatting getBorderFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetBorder()) return null;

        return new XSSFBorderFormatting(dxf.getBorder(), _sh.getWorkbook().getStylesSource().getIndexedColors());
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

        return new XSSFFontFormatting(font, _sh.getWorkbook().getStylesSource().getIndexedColors());
    }

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFFontFormatting getFontFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetFont()) return null;

        return new XSSFFontFormatting(dxf.getFont(), _sh.getWorkbook().getStylesSource().getIndexedColors());
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

        return new XSSFPatternFormatting(fill, _sh.getWorkbook().getStylesSource().getIndexedColors());
    }

    /**
     * @return - pattern formatting object  if defined,  <code>null</code> otherwise
     */
    public XSSFPatternFormatting getPatternFormatting(){
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetFill()) return null;

        return new XSSFPatternFormatting(dxf.getFill(), _sh.getWorkbook().getStylesSource().getIndexedColors());
    }

    /**
     *
     * @param color
     * @return data bar formatting
     */
    public XSSFDataBarFormatting createDataBarFormatting(XSSFColor color) {
        // Is it already there?
        if (_cfRule.isSetDataBar() && _cfRule.getType() == STCfType.DATA_BAR)
            return getDataBarFormatting();

        // Mark it as being a Data Bar
        _cfRule.setType(STCfType.DATA_BAR);

        // Ensure the right element
        CTDataBar bar = null;
        if (_cfRule.isSetDataBar()) {
            bar = _cfRule.getDataBar();
        } else {
            bar = _cfRule.addNewDataBar();
        }
        // Set the color
        bar.setColor(color.getCTColor());

        // Add the default thresholds
        CTCfvo min = bar.addNewCfvo();
        min.setType(STCfvoType.Enum.forString(RangeType.MIN.name));
        CTCfvo max = bar.addNewCfvo();
        max.setType(STCfvoType.Enum.forString(RangeType.MAX.name));

        // Wrap and return
        return new XSSFDataBarFormatting(bar, _sh.getWorkbook().getStylesSource().getIndexedColors());
    }
    public XSSFDataBarFormatting getDataBarFormatting() {
        if (_cfRule.isSetDataBar()) {
            CTDataBar bar = _cfRule.getDataBar();
            return new XSSFDataBarFormatting(bar, _sh.getWorkbook().getStylesSource().getIndexedColors());
        } else {
            return null;
        }
    }

    public XSSFIconMultiStateFormatting createMultiStateFormatting(IconSet iconSet) {
        // Is it already there?
        if (_cfRule.isSetIconSet() && _cfRule.getType() == STCfType.ICON_SET)
            return getMultiStateFormatting();

        // Mark it as being an Icon Set
        _cfRule.setType(STCfType.ICON_SET);

        // Ensure the right element
        CTIconSet icons = null;
        if (_cfRule.isSetIconSet()) {
            icons = _cfRule.getIconSet();
        } else {
            icons = _cfRule.addNewIconSet();
        }
        // Set the type of the icon set
        if (iconSet.name != null) {
            STIconSetType.Enum xIconSet = STIconSetType.Enum.forString(iconSet.name);
            icons.setIconSet(xIconSet);
        }

        // Add a default set of thresholds
        int jump = 100 / iconSet.num;
        STCfvoType.Enum type = STCfvoType.Enum.forString(RangeType.PERCENT.name);
        for (int i=0; i<iconSet.num; i++) {
            CTCfvo cfvo = icons.addNewCfvo();
            cfvo.setType(type);
            cfvo.setVal(Integer.toString(i*jump));
        }

        // Wrap and return
        return new XSSFIconMultiStateFormatting(icons);
    }
    public XSSFIconMultiStateFormatting getMultiStateFormatting() {
        if (_cfRule.isSetIconSet()) {
            CTIconSet icons = _cfRule.getIconSet();
            return new XSSFIconMultiStateFormatting(icons);
        } else {
            return null;
        }
    }

    public XSSFColorScaleFormatting createColorScaleFormatting() {
        // Is it already there?
        if (_cfRule.isSetColorScale() && _cfRule.getType() == STCfType.COLOR_SCALE)
            return getColorScaleFormatting();

        // Mark it as being a Color Scale
        _cfRule.setType(STCfType.COLOR_SCALE);

        // Ensure the right element
        CTColorScale scale = null;
        if (_cfRule.isSetColorScale()) {
            scale = _cfRule.getColorScale();
        } else {
            scale = _cfRule.addNewColorScale();
        }

        // Add a default set of thresholds and colors
        if (scale.sizeOfCfvoArray() == 0) {
            CTCfvo cfvo;
            cfvo = scale.addNewCfvo();
            cfvo.setType(STCfvoType.Enum.forString(RangeType.MIN.name));
            cfvo = scale.addNewCfvo();
            cfvo.setType(STCfvoType.Enum.forString(RangeType.PERCENTILE.name));
            cfvo.setVal("50");
            cfvo = scale.addNewCfvo();
            cfvo.setType(STCfvoType.Enum.forString(RangeType.MAX.name));

            for (int i=0; i<3; i++) {
                scale.addNewColor();
            }
        }

        // Wrap and return
        return new XSSFColorScaleFormatting(scale, _sh.getWorkbook().getStylesSource().getIndexedColors());
    }
    public XSSFColorScaleFormatting getColorScaleFormatting() {
        if (_cfRule.isSetColorScale()) {
            CTColorScale scale = _cfRule.getColorScale();
            return new XSSFColorScaleFormatting(scale, _sh.getWorkbook().getStylesSource().getIndexedColors());
        } else {
            return null;
        }
    }

    /**
     * Return the number format from the dxf style record if present, null if not
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getNumberFormat()
     */
    public ExcelNumberFormat getNumberFormat() {
        CTDxf dxf = getDxf(false);
        if(dxf == null || !dxf.isSetNumFmt()) return null;

        CTNumFmt numFmt = dxf.getNumFmt();
        return new ExcelNumberFormat((int) numFmt.getNumFmtId(), numFmt.getFormatCode());
    }

    /**
     * Type of conditional formatting rule.
     */
    @Override
    public ConditionType getConditionType() {
        return typeLookup.get(_cfRule.getType());
    }

    /**
     * Will return null if {@link #getConditionType()} != {@link ConditionType#FILTER}
     * @see org.apache.poi.ss.usermodel.ConditionalFormattingRule#getConditionFilterType()
     */
    public ConditionFilterType getConditionFilterType() {
        return filterTypeLookup.get(_cfRule.getType());
    }

    public ConditionFilterData getFilterConfiguration() {
        return new XSSFConditionFilterData(_cfRule);
    }

    /**
     * The comparison function used when the type of conditional formatting is set to
     * {@link ConditionType#CELL_VALUE_IS}
     * <p>
     *     MUST be a constant from {@link org.apache.poi.ss.usermodel.ComparisonOperator}
     * </p>
     *
     * @return the conditional format operator
     */
    @Override
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
    public String getFormula1(){
        return _cfRule.sizeOfFormulaArray() > 0 ? _cfRule.getFormulaArray(0) : null;
    }

    /**
     * The formula used to evaluate the second operand of the comparison when
     * comparison type is  {@link ConditionType#CELL_VALUE_IS} and operator
     * is either {@link org.apache.poi.ss.usermodel.ComparisonOperator#BETWEEN} or {@link org.apache.poi.ss.usermodel.ComparisonOperator#NOT_BETWEEN}
     *
     * @return  the second formula
     */
    public String getFormula2(){
        return _cfRule.sizeOfFormulaArray() == 2 ? _cfRule.getFormulaArray(1) : null;
    }

    public String getText() {
        return _cfRule.getText();
    }

    /**
     * Conditional format rules don't define stripes, so always 0
     * @see org.apache.poi.ss.usermodel.DifferentialStyleProvider#getStripeSize()
     */
    public int getStripeSize() {
        return 0;
    }
}
