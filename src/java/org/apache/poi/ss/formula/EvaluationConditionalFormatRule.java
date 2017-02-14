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

package org.apache.poi.ss.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ConditionFilterData;
import org.apache.poi.ss.usermodel.ConditionFilterType;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Abstracted and cached version of a Conditional Format rule for use with a
 * {@link ConditionalFormattingEvaluator}. This references a rule, its owning
 * {@link ConditionalFormatting}, its priority order (lower index = higher priority in Excel),
 * and the information needed to evaluate the rule for a given cell.
 * <p/>
 * Having this all combined and cached avoids repeated access calls to the
 * underlying structural objects, XSSF CT* objects and HSSF raw byte structures.
 * Those objects can be referenced from here. This object will be out of sync if
 * anything modifies the referenced structures' evaluation properties.
 * <p/>
 * The assumption is that consuming applications will read the display properties once and
 * create whatever style objects they need, caching those at the application level.
 * Thus this class only caches values needed for evaluation, not display.
 */
public class EvaluationConditionalFormatRule implements Comparable<EvaluationConditionalFormatRule> {

    private final WorkbookEvaluator workbookEvaluator;
    private final Sheet sheet;
    private final ConditionalFormatting formatting;
    private final ConditionalFormattingRule rule;
    
    /* cached values */
    private final CellRangeAddress[] regions;
    /**
     * Depending on the rule type, it may want to know about certain values in the region when evaluating {@link #matches(Cell)},
     * such as top 10, unique, duplicate, average, etc.  This collection stores those if needed so they are not repeatedly calculated
     */
    private final Map<CellRangeAddress, Set<ValueAndFormat>> meaningfulRegionValues = new HashMap<CellRangeAddress, Set<ValueAndFormat>>();
    
    private final int priority;
    private final int formattingIndex;
    private final int ruleIndex;
    private final String formula1;
    private final String formula2;
    private final OperatorEnum operator;
    private final ConditionType type;
    
    /**
     *
     * @param workbookEvaluator
     * @param sheet
     * @param formatting
     * @param formattingIndex for priority, zero based
     * @param rule
     * @param ruleIndex for priority, zero based, if this is an HSSF rule.  Unused for XSSF rules
     * @param regions could be read from formatting, but every call creates new objects in a new array.
     *                  this allows calling it once per formatting instance, and re-using the array.
     */
    public EvaluationConditionalFormatRule(WorkbookEvaluator workbookEvaluator, Sheet sheet, ConditionalFormatting formatting, int formattingIndex, ConditionalFormattingRule rule, int ruleIndex, CellRangeAddress[] regions) {
        super();
        this.workbookEvaluator = workbookEvaluator;
        this.sheet = sheet;
        this.formatting = formatting;
        this.rule = rule;
        this.formattingIndex = formattingIndex;
        this.ruleIndex = ruleIndex;
        
        this.priority = rule.getPriority();
        
        this.regions = regions;
        formula1 = rule.getFormula1();
        formula2 = rule.getFormula2();
        
        operator = OperatorEnum.values()[rule.getComparisonOperation()];
        type = rule.getConditionType();
    }

    public Sheet getSheet() {
        return sheet;
    }
   
    /**
     * @return the formatting
     */
    public ConditionalFormatting getFormatting() {
        return formatting;
    }
    
    public int getFormattingIndex() {
        return formattingIndex;
    }
    
    /**
     * @return the rule
     */
    public ConditionalFormattingRule getRule() {
        return rule;
    }
    
    public int getRuleIndex() {
        return ruleIndex;
    }
    
    /**
     * @return the regions
     */
    public CellRangeAddress[] getRegions() {
        return regions;
    }
    
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * @return the formula1
     */
    public String getFormula1() {
        return formula1;
    }
    
    /**
     * @return the formula2
     */
    public String getFormula2() {
        return formula2;
    }
    
    /**
     * @return the operator
     */
    public OperatorEnum getOperator() {
        return operator;
    }
    
    /**
     * @return the type
     */
    public ConditionType getType() {
        return type;
    }
    
    /**
     * Defined as equal sheet name and formatting and rule indexes
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! obj.getClass().equals(this.getClass())) return false;
        final EvaluationConditionalFormatRule r = (EvaluationConditionalFormatRule) obj;
        return getSheet().getSheetName().equalsIgnoreCase(r.getSheet().getSheetName())
            && getFormattingIndex() == r.getFormattingIndex()
            && getRuleIndex() == r.getRuleIndex();
    }

    /**
     * Per Excel Help, XSSF rule priority is sheet-wide, not just within the owning ConditionalFormatting object.
     * This can be seen by creating 4 rules applying to two different ranges and examining the XML.
     * <p/>
     * HSSF priority is based on definition/persistence order.
     * 
     * @param o
     * @return comparison based on sheet name, formatting index, and rule priority
     */
    public int compareTo(EvaluationConditionalFormatRule o) {
        int cmp = getSheet().getSheetName().compareToIgnoreCase(o.getSheet().getSheetName());
        if (cmp != 0) return cmp;
        
        final int x = getPriority();
        final int y = o.getPriority();
        // logic from Integer.compare()
        cmp = (x < y) ? -1 : ((x == y) ? 0 : 1);
        if (cmp != 0) return cmp;

        cmp = new Integer(getFormattingIndex()).compareTo(new Integer(o.getFormattingIndex()));
        if (cmp != 0) return cmp;
        return new Integer(getRuleIndex()).compareTo(new Integer(o.getRuleIndex()));
    }
    
    public int hashCode() {
        int hash = sheet.getSheetName().hashCode();
        hash = 31 * hash + formattingIndex;
        hash = 31 * hash + ruleIndex;
        return hash;
    }
    
    /**
     * @param cell
     * @return true if this rule evaluates to true for the given cell
     */
    /* package */ boolean matches(Cell cell) {
        // first check that it is in one of the regions defined for this format
        CellRangeAddress region = null;
        for (CellRangeAddress r : regions) {
            if (r.isInRange(cell)) {
                region = r;
                break;
            }
        }
        
        if (region == null) return false; // cell not in range of this rule
        
        final ConditionType ruleType = getRule().getConditionType();
        
        // these rules apply to all cells in a region. Specific condition criteria
        // may specify no special formatting for that value partition, but that's display logic
        if (ruleType.equals(ConditionType.COLOR_SCALE)
            || ruleType.equals(ConditionType.DATA_BAR)
            || ruleType.equals(ConditionType.ICON_SET)) {
           return true; 
        }
        
        if (ruleType.equals(ConditionType.CELL_VALUE_IS)) {
            return checkValue(cell, region);
        }
        if (ruleType.equals(ConditionType.FORMULA)) {
            return checkFormula(cell, region);
        }
        if (ruleType.equals(ConditionType.FILTER)) {
            return checkFilter(cell, region);
        }
        
        // TODO: anything else, we don't handle yet, such as top 10
        return false;
    }
    
    /**
     * @param cell the cell to check for
     * @param region for adjusting relative formulas
     * @return if the value of the cell is valid or not for the formatting rule
     */
    private boolean checkValue(Cell cell, CellRangeAddress region) {
        if (cell == null || DataValidationEvaluator.isType(cell, CellType.BLANK)
           || DataValidationEvaluator.isType(cell,CellType.ERROR) 
           || (DataValidationEvaluator.isType(cell,CellType.STRING) 
                   && (cell.getStringCellValue() == null || cell.getStringCellValue().isEmpty())
               )
           ) return false;
        
        ValueEval eval = unwrapEval(workbookEvaluator.evaluate(rule.getFormula1(), ConditionalFormattingEvaluator.getRef(cell), region));
        
        String f2 = rule.getFormula2();
        ValueEval eval2 = null;
        if (f2 != null && f2.length() > 0) {
            eval2 = unwrapEval(workbookEvaluator.evaluate(f2, ConditionalFormattingEvaluator.getRef(cell), region));
        }
        
        // we assume the cell has been evaluated, and the current formula value stored
        if (DataValidationEvaluator.isType(cell, CellType.BOOLEAN)) {
            if (eval instanceof BoolEval && (eval2 == null || eval2 instanceof BoolEval) ) {
                return operator.isValid(cell.getBooleanCellValue(), ((BoolEval) eval).getBooleanValue(), eval2 == null ? null : ((BoolEval) eval2).getBooleanValue());
            }
            return false; // wrong types
        }
        if (DataValidationEvaluator.isType(cell, CellType.NUMERIC)) {
            if (eval instanceof NumberEval && (eval2 == null || eval2 instanceof NumberEval) ) {
                return operator.isValid(cell.getNumericCellValue(), ((NumberEval) eval).getNumberValue(), eval2 == null ? null : ((NumberEval) eval2).getNumberValue());
            }
            return false; // wrong types
        }
        if (DataValidationEvaluator.isType(cell, CellType.STRING)) {
            if (eval instanceof StringEval && (eval2 == null || eval2 instanceof StringEval) ) {
                return operator.isValid(cell.getStringCellValue(), ((StringEval) eval).getStringValue(), eval2 == null ? null : ((StringEval) eval2).getStringValue());
            }
            return false; // wrong types
        }
        
        // should not get here, but in case...
        return false;
    }
    
    private ValueEval unwrapEval(ValueEval eval) {
        ValueEval comp = eval;
        
        while (comp instanceof RefEval) {
            RefEval ref = (RefEval) comp;
            comp = ref.getInnerValueEval(ref.getFirstSheetIndex());
        }
        return comp;
    }
    /**
     * @param cell needed for offsets from region anchor
     * @param region for adjusting relative formulas
     * @return true/false using the same rules as Data Validation evaluations
     */
    private boolean checkFormula(Cell cell, CellRangeAddress region) {
        ValueEval comp = unwrapEval(workbookEvaluator.evaluate(rule.getFormula1(), ConditionalFormattingEvaluator.getRef(cell), region));
        
        // Copied for now from DataValidationEvaluator.ValidationEnum.FORMULA#isValidValue()
        if (comp instanceof BlankEval) return true;
        if (comp instanceof ErrorEval) return false;
        if (comp instanceof BoolEval) {
            return ((BoolEval) comp).getBooleanValue();
        }
        // empirically tested in Excel - 0=false, any other number = true/valid
        // see test file DataValidationEvaluations.xlsx
        if (comp instanceof NumberEval) {
            return ((NumberEval) comp).getNumberValue() != 0;
        }
        return false; // anything else is false, such as text
    }
    
    private boolean checkFilter(Cell cell, CellRangeAddress region) {
        final ConditionFilterType filterType = rule.getConditionFilterType();
        if (filterType == null) return false;
        
        // TODO: this could/should be delegated to the Enum type, but that's in the usermodel package,
        // we may not want evaluation code there.  Of course, maybe the enum should go here in formula,
        // and not be returned by the SS model, but then we need the XSSF rule to expose the raw OOXML
        // type value, which isn't ideal either.
        switch (filterType) {
        case FILTER:
            return false; // we don't evaluate HSSF filters yet
        case TOP_10:
            // from testing, Excel only operates on numbers and dates (which are stored as numbers) in the range.
            // numbers stored as text are ignored, but numbers formatted as text are treated as numbers.
            
            final ValueAndFormat cv10 = getCellValue(cell);
            if (! cv10.isNumber()) return false;
            
            return getMeaningfulValues(region, false, new ValueFunction() {
                public Set<ValueAndFormat> evaluate(List<ValueAndFormat> allValues) {
                    List<ValueAndFormat> values = allValues;
                    final ConditionFilterData conf = rule.getFilterConfiguration();
                    
                    if (! conf.getBottom()) Collections.sort(values, Collections.reverseOrder());
                    else Collections.sort(values);
                    
                    int limit = (int) conf.getRank();
                    if (conf.getPercent()) limit = allValues.size() * limit / 100;
                    if (allValues.size() <= limit) return new HashSet<ValueAndFormat>(allValues);

                    return new HashSet<ValueAndFormat>(allValues.subList(0, limit));
                }
            }).contains(cv10);
        case UNIQUE_VALUES:
            // Per Excel help, "duplicate" means matching value AND format
            // https://support.office.com/en-us/article/Filter-for-unique-values-or-remove-duplicate-values-ccf664b0-81d6-449b-bbe1-8daaec1e83c2
            return getMeaningfulValues(region, true, new ValueFunction() {
                public Set<ValueAndFormat> evaluate(List<ValueAndFormat> allValues) {
                    List<ValueAndFormat> values = allValues;
                    Collections.sort(values);
                    
                    final Set<ValueAndFormat> unique = new HashSet<ValueAndFormat>();
                    
                    for (int i=0; i < values.size(); i++) {
                        final ValueAndFormat v = values.get(i);
                        // skip this if the current value matches the next one, or is the last one and matches the previous one
                        if ( (i < values.size()-1 && v.equals(values.get(i+1)) ) || ( i > 0 && i == values.size()-1 && v.equals(values.get(i-1)) ) ) {
                            // current value matches next value, skip both
                            i++;
                            continue;
                        }
                        unique.add(v);
                    }
                    
                    return unique;
                }
            }).contains(getCellValue(cell));
        case DUPLICATE_VALUES:
            // Per Excel help, "duplicate" means matching value AND format
            // https://support.office.com/en-us/article/Filter-for-unique-values-or-remove-duplicate-values-ccf664b0-81d6-449b-bbe1-8daaec1e83c2
            return getMeaningfulValues(region, true, new ValueFunction() {
                public Set<ValueAndFormat> evaluate(List<ValueAndFormat> allValues) {
                    List<ValueAndFormat> values = allValues;
                    Collections.sort(values);
                    
                    final Set<ValueAndFormat> dup = new HashSet<ValueAndFormat>();
                    
                    for (int i=0; i < values.size(); i++) {
                        final ValueAndFormat v = values.get(i);
                        // skip this if the current value matches the next one, or is the last one and matches the previous one
                        if ( (i < values.size()-1 && v.equals(values.get(i+1)) ) || ( i > 0 && i == values.size()-1 && v.equals(values.get(i-1)) ) ) {
                            // current value matches next value, add one
                            dup.add(v);
                            i++;
                        }
                    }
                    return dup;
                }
            }).contains(getCellValue(cell));
        case ABOVE_AVERAGE:
            // from testing, Excel only operates on numbers and dates (which are stored as numbers) in the range.
            // numbers stored as text are ignored, but numbers formatted as text are treated as numbers.
            
            final ConditionFilterData conf = rule.getFilterConfiguration();

            // actually ordered, so iteration order is predictable
            List<ValueAndFormat> values = new ArrayList<ValueAndFormat>(getMeaningfulValues(region, false, new ValueFunction() {
                public Set<ValueAndFormat> evaluate(List<ValueAndFormat> allValues) {
                    List<ValueAndFormat> values = allValues;
                    double total = 0;
                    ValueEval[] pop = new ValueEval[values.size()];
                    for (int i=0; i < values.size(); i++) {
                        ValueAndFormat v = values.get(i);
                        total += v.value.doubleValue();
                        pop[i] = new NumberEval(v.value.doubleValue());
                    }
                    
                    final Set<ValueAndFormat> avgSet = new LinkedHashSet<ValueAndFormat>(1);
                    avgSet.add(new ValueAndFormat(new Double(values.size() == 0 ? 0 : total / values.size()), null));
                    
                    final double stdDev = values.size() <= 1 ? 0 : ((NumberEval) AggregateFunction.STDEV.evaluate(pop, 0, 0)).getNumberValue();
                    avgSet.add(new ValueAndFormat(new Double(stdDev), null));
                    return avgSet;
                }
            }));
            
            final ValueAndFormat cv = getCellValue(cell);
            Double val = cv.isNumber() ? cv.getValue() : null;
            if (val == null) return false;
            
            double avg = values.get(0).value.doubleValue();
            double stdDev = values.get(1).value.doubleValue();
            
            /*
             * use StdDev, aboveAverage, equalAverage to find:
             * comparison value
             * operator type
             */
            
            Double comp = new Double(conf.getStdDev() > 0 ? (avg + (conf.getAboveAverage() ? 1 : -1) * stdDev * conf.getStdDev()) : avg) ;
            
            OperatorEnum op = null;
            if (conf.getAboveAverage()) {
                if (conf.getEqualAverage()) op = OperatorEnum.GREATER_OR_EQUAL;
                else op = OperatorEnum.GREATER_THAN;
            } else {
                if (conf.getEqualAverage()) op = OperatorEnum.LESS_OR_EQUAL;
                else op = OperatorEnum.LESS_THAN;
            }
            return op != null && op.isValid(val, comp, null);
        case CONTAINS_TEXT:
            // implemented both by a cfRule "text" attribute and a formula.  Use the formula.
            return checkFormula(cell, region);
        case NOT_CONTAINS_TEXT:
            // implemented both by a cfRule "text" attribute and a formula.  Use the formula.
            return checkFormula(cell, region);
        case BEGINS_WITH:
            // implemented both by a cfRule "text" attribute and a formula.  Use the formula.
            return checkFormula(cell, region);
        case ENDS_WITH:
            // implemented both by a cfRule "text" attribute and a formula.  Use the formula.
            return checkFormula(cell, region);
        case CONTAINS_BLANKS:
            try {
                String v = cell.getStringCellValue();
                // see TextFunction.TRIM for implementation
                return v == null || v.trim().length() == 0;
            } catch (Exception e) {
                // not a valid string value, and not a blank cell (that's checked earlier)
                return false;
            }
        case NOT_CONTAINS_BLANKS:
            try {
                String v = cell.getStringCellValue();
                // see TextFunction.TRIM for implementation
                return v != null && v.trim().length() > 0;
            } catch (Exception e) {
                // not a valid string value, but not blank
                return true;
            }
        case CONTAINS_ERRORS:
            return cell != null && DataValidationEvaluator.isType(cell, CellType.ERROR);
        case NOT_CONTAINS_ERRORS:
            return cell == null || ! DataValidationEvaluator.isType(cell, CellType.ERROR);
        case TIME_PERIOD:
            // implemented both by a cfRule "text" attribute and a formula.  Use the formula.
            return checkFormula(cell, region);
        default:
            return false;
        }
    }
    
    /**
     * from testing, Excel only operates on numbers and dates (which are stored as numbers) in the range.
     * numbers stored as text are ignored, but numbers formatted as text are treated as numbers.
     * 
     * @param region
     * @return the meaningful values in the range of cells specified
     */
    private Set<ValueAndFormat> getMeaningfulValues(CellRangeAddress region, boolean withText, ValueFunction func) {
        Set<ValueAndFormat> values = meaningfulRegionValues.get(region);
        if (values != null) return values;
        
        List<ValueAndFormat> allValues = new ArrayList<ValueAndFormat>((region.getLastColumn() - region.getFirstColumn()+1) * (region.getLastRow() - region.getFirstRow() + 1));
        
        for (int r=region.getFirstRow(); r <= region.getLastRow(); r++) {
            final Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                Cell cell = row.getCell(c);
                final ValueAndFormat cv = getCellValue(cell);
                if (cv != null && (withText || cv.isNumber()) ) allValues.add(cv);
            }
        }
        
        values = func.evaluate(allValues);
        meaningfulRegionValues.put(region, values);
        
        return values;
    }

    private ValueAndFormat getCellValue(Cell cell) {
        if (cell != null) {
            final CellType type = cell.getCellTypeEnum();
            if (type == CellType.NUMERIC || (type == CellType.FORMULA && cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC) ) {
                return new ValueAndFormat(new Double(cell.getNumericCellValue()), cell.getCellStyle().getDataFormatString());
            } else if (type == CellType.STRING || (type == CellType.FORMULA && cell.getCachedFormulaResultTypeEnum() == CellType.STRING) ) {
                return new ValueAndFormat(cell.getStringCellValue(), cell.getCellStyle().getDataFormatString());
            } else if (type == CellType.BOOLEAN || (type == CellType.FORMULA && cell.getCachedFormulaResultTypeEnum() == CellType.BOOLEAN) ) {
                return new ValueAndFormat(cell.getStringCellValue(), cell.getCellStyle().getDataFormatString());
            }
        }
        return null;
    }
    /**
     * instances evaluate the values for a region and return the positive matches for the function type.
     * TODO: when we get to use Java 8, this is obviously a Lambda Function.
     */
    protected interface ValueFunction {
        
        /**
         *
         * @param values
         * @return the desired values for the rules implemented by the current instance
         */
        Set<ValueAndFormat> evaluate(List<ValueAndFormat> values);
    }
    
    /**
     * Not calling it OperatorType to avoid confusion for now with other classes.
     * Definition order matches OOXML type ID indexes.
     * Note that this has NO_COMPARISON as the first item, unlike the similar 
     * DataValidation operator enum. Thanks, Microsoft.
     */
    public static enum OperatorEnum {
        NO_COMPARISON {
            /** always false/invalid */
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return false;
            }
        },
        BETWEEN {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) >= 0 && cellValue.compareTo(v2) <= 0;
            }
        },
        NOT_BETWEEN {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) < 0 || cellValue.compareTo(v2) > 0;
            }
        },
        EQUAL {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                // need to avoid instanceof, to work around a 1.6 compiler bug
                if (cellValue.getClass() == String.class) {
                    return cellValue.toString().compareToIgnoreCase(v1.toString()) == 0;
                }
                return cellValue.compareTo(v1) == 0;
            }
        },
        NOT_EQUAL {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                // need to avoid instanceof, to work around a 1.6 compiler bug
                if (cellValue.getClass() == String.class) {
                    return cellValue.toString().compareToIgnoreCase(v1.toString()) == 0;
                }
                return cellValue.compareTo(v1) != 0;
            }
        },
        GREATER_THAN {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) > 0;
            }
        },
        LESS_THAN {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) < 0;
            }
        },
        GREATER_OR_EQUAL {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) >= 0;
            }
        },
        LESS_OR_EQUAL {
            public <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
                return cellValue.compareTo(v1) <= 0;
            }
        },
        ;
        
        /**
         * Evaluates comparison using operator instance rules
         * @param cellValue won't be null, assumption is previous checks handled that
         * @param v1 if null, value assumed invalid, anything passes, per Excel behavior
         * @param v2 null if not needed.  If null when needed, assume anything passes, per Excel behavior
         * @return true if the comparison is valid
         */
        public abstract <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2);
    }
    
    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    protected class ValueAndFormat implements Comparable<ValueAndFormat> {
        
        private final Double value;
        private final String string;
        private final String format;
        
        public ValueAndFormat(Double value, String format) {
            this.value = value;
            this.format = format;
            string = null;
        }
        
        public ValueAndFormat(String value, String format) {
            this.value = null;
            this.format = format;
            string = value;
        }
        
        public boolean isNumber() {
            return value != null;
        }
        
        public Double getValue() {
            return value;
        }
        
        public boolean equals(Object obj) {
            ValueAndFormat o = (ValueAndFormat) obj;
            return ( value == o.value || value.equals(o.value))
                    && ( format == o.format || format.equals(o.format))
                    && (string == o.string || string.equals(o.string));
        }
        
        /**
         * Note: this class has a natural ordering that is inconsistent with equals.
         * @param o
         * @return value comparison
         */
        public int compareTo(ValueAndFormat o) {
            if (value == null && o.value != null) return 1;
            if (o.value == null && value != null) return -1;
            int cmp = value == null ? 0 : value.compareTo(o.value);
            if (cmp != 0) return cmp;
            
            if (string == null && o.string != null) return 1;
            if (o.string == null && string != null) return -1;
            
            return string == null ? 0 : string.compareTo(o.string);
        }
        
        public int hashCode() {
            return (string == null ? 0 : string.hashCode()) * 37 * 37 + 37 * (value == null ? 0 : value.hashCode()) + (format == null ? 0 : format.hashCode());
        }
    }
}
