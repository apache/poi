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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

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
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.StringUtil;

/**
 * Abstracted and cached version of a Conditional Format rule for use with a
 * {@link ConditionalFormattingEvaluator}. This references a rule, its owning
 * {@link ConditionalFormatting}, its priority order (lower index = higher priority in Excel),
 * and the information needed to evaluate the rule for a given cell.
 * <p>
 * Having this all combined and cached avoids repeated access calls to the
 * underlying structural objects, XSSF CT* objects and HSSF raw byte structures.
 * Those objects can be referenced from here. This object will be out of sync if
 * anything modifies the referenced structures' evaluation properties.
 * <p>
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

    private CellRangeAddress topLeftRegion;

    /**
     * Depending on the rule type, it may want to know about certain values in the region when evaluating {@link #matches(CellReference)},
     * such as top 10, unique, duplicate, average, etc.  This collection stores those if needed so they are not repeatedly calculated
     */
    private final Map<CellRangeAddress, Set<ValueAndFormat>> meaningfulRegionValues = new HashMap<>();

    private final int priority;
    private final int formattingIndex;
    private final int ruleIndex;
    private final String formula1;
    private final String formula2;
    private final String text;
    // cached for performance, used with cell text comparisons, which are case insensitive and need to be Locale aware (contains, starts with, etc.)
    private final String lowerText;

    private final OperatorEnum operator;
    private final ConditionType type;
    // cached for performance, to avoid reading the XMLBean every time a conditionally formatted cell is rendered
    private final ExcelNumberFormat numberFormat;
    // cached for performance, used to format numeric cells for string comparisons.  See Bug #61764 for explanation
    private final DecimalFormat decimalTextFormat;

    /**
     * @param formattingIndex for priority, zero based
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

        for (CellRangeAddress region : regions) {
            if (topLeftRegion == null) topLeftRegion = region;
            else if (region.getFirstColumn() < topLeftRegion.getFirstColumn()
                    || region.getFirstRow() < topLeftRegion.getFirstRow()) {
                topLeftRegion = region;
            }
        }
        formula1 = rule.getFormula1();
        formula2 = rule.getFormula2();

        text = rule.getText();
        lowerText = text == null ? null : text.toLowerCase(LocaleUtil.getUserLocale());

        numberFormat = rule.getNumberFormat();

        operator = OperatorEnum.values()[rule.getComparisonOperation()];
        type = rule.getConditionType();

//         Excel uses the stored text representation from the XML apparently, in tests done so far
        decimalTextFormat = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        decimalTextFormat.setMaximumFractionDigits(340); // DecimalFormat.DOUBLE_FRACTION_DIGITS, which is default scoped
    }

    /**
     * @return sheet
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * @return the formatting
     */
    public ConditionalFormatting getFormatting() {
        return formatting;
    }

    /**
     * @return conditional formatting index
     */
    public int getFormattingIndex() {
        return formattingIndex;
    }

    /**
     * @return Excel number format string to apply to matching cells, or null to keep the cell default
     */
    public ExcelNumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * @return the rule
     */
    public ConditionalFormattingRule getRule() {
        return rule;
    }

    /**
     * @return rule index
     */
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
     * @return condition text if any, or null
     */
    public String getText() {
        return text;
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
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (! obj.getClass().equals(this.getClass())) {
            return false;
        }
        final EvaluationConditionalFormatRule r = (EvaluationConditionalFormatRule) obj;
        return getSheet().getSheetName().equalsIgnoreCase(r.getSheet().getSheetName())
            && getFormattingIndex() == r.getFormattingIndex()
            && getRuleIndex() == r.getRuleIndex();
    }

    /**
     * Per Excel Help, XSSF rule priority is sheet-wide, not just within the owning ConditionalFormatting object.
     * This can be seen by creating 4 rules applying to two different ranges and examining the XML.
     * <p>
     * HSSF priority is based on definition/persistence order.
     *
     * @return comparison based on sheet name, formatting index, and rule priority
     */
    @Override
    public int compareTo(EvaluationConditionalFormatRule o) {
        int cmp = getSheet().getSheetName().compareToIgnoreCase(o.getSheet().getSheetName());
        if (cmp != 0) {
            return cmp;
        }

        final int x = getPriority();
        final int y = o.getPriority();
        // logic from Integer.compare()
        cmp = Integer.compare(x, y);
        if (cmp != 0) {
            return cmp;
        }

        cmp = Integer.compare(getFormattingIndex(), o.getFormattingIndex());
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(getRuleIndex(), o.getRuleIndex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheet.getSheetName(),formattingIndex,ruleIndex);
    }

    /**
     * @return true if this rule evaluates to true for the given cell
     */
    /* package */ boolean matches(CellReference ref) {
        // first check that it is in one of the regions defined for this format
        CellRangeAddress region = null;
        for (CellRangeAddress r : regions) {
            if (r.isInRange(ref)) {
                region = r;
                break;
            }
        }

        if (region == null) {
            // cell not in range of this rule
            return false;
        }

        final ConditionType ruleType = getRule().getConditionType();

        // these rules apply to all cells in a region. Specific condition criteria
        // may specify no special formatting for that value partition, but that's display logic
        if (ruleType.equals(ConditionType.COLOR_SCALE)
            || ruleType.equals(ConditionType.DATA_BAR)
            || ruleType.equals(ConditionType.ICON_SET)) {
           return true;
        }

        Cell cell = null;
        final Row row = sheet.getRow(ref.getRow());
        if (row != null) {
            cell = row.getCell(ref.getCol());
        }

        if (ruleType.equals(ConditionType.CELL_VALUE_IS)) {
            // undefined cells never match a VALUE_IS condition
            if (cell == null) return false;
            return checkValue(cell, topLeftRegion);
        }
        if (ruleType.equals(ConditionType.FORMULA)) {
            return checkFormula(ref, topLeftRegion);
        }
        if (ruleType.equals(ConditionType.FILTER)) {
            return checkFilter(cell, ref, topLeftRegion);
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
           ) {
            return false;
        }

        ValueEval eval = unwrapEval(workbookEvaluator.evaluate(rule.getFormula1(), ConditionalFormattingEvaluator.getRef(cell), region));

        String f2 = rule.getFormula2();
        ValueEval eval2 = BlankEval.instance;
        if (f2 != null && f2.length() > 0) {
            eval2 = unwrapEval(workbookEvaluator.evaluate(f2, ConditionalFormattingEvaluator.getRef(cell), region));
        }

        // we assume the cell has been evaluated, and the current formula value stored
        if (DataValidationEvaluator.isType(cell, CellType.BOOLEAN)
                && (eval == BlankEval.instance || eval instanceof BoolEval)
                && (eval2 == BlankEval.instance || eval2 instanceof BoolEval)
           ) {
            return operator.isValid(cell.getBooleanCellValue(), eval == BlankEval.instance ? null : ((BoolEval) eval).getBooleanValue(), eval2 == BlankEval.instance ? null : ((BoolEval) eval2).getBooleanValue());
        }
        if (DataValidationEvaluator.isType(cell, CellType.NUMERIC)
                && (eval == BlankEval.instance || eval instanceof NumberEval )
                && (eval2 == BlankEval.instance || eval2 instanceof NumberEval)
           ) {
            return operator.isValid(cell.getNumericCellValue(), eval == BlankEval.instance ? null : ((NumberEval) eval).getNumberValue(), eval2 == BlankEval.instance ? null : ((NumberEval) eval2).getNumberValue());
        }
        if (DataValidationEvaluator.isType(cell, CellType.STRING)
                && (eval == BlankEval.instance || eval instanceof StringEval )
                && (eval2 == BlankEval.instance || eval2 instanceof StringEval)
           ) {
                return operator.isValid(cell.getStringCellValue(), eval == BlankEval.instance ? null : ((StringEval) eval).getStringValue(), eval2 == BlankEval.instance ? null : ((StringEval) eval2).getStringValue());
        }

        return operator.isValidForIncompatibleTypes();
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
     * @param ref needed for offsets from region anchor - may be null!
     * @param region for adjusting relative formulas
     * @return true/false using the same rules as Data Validation evaluations
     */
    private boolean checkFormula(CellReference ref, CellRangeAddress region) {
        ValueEval comp = unwrapEval(workbookEvaluator.evaluate(rule.getFormula1(), ref, region));

        // Copied for now from DataValidationEvaluator.ValidationEnum.FORMULA#isValidValue()
        if (comp instanceof BlankEval) {
            return true;
        }
        if (comp instanceof ErrorEval) {
            return false;
        }
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

    private boolean checkFilter(Cell cell, CellReference ref, CellRangeAddress region) {
        final ConditionFilterType filterType = rule.getConditionFilterType();
        if (filterType == null) {
            return false;
        }

        final ValueAndFormat cv = getCellValue(cell);

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

            if (! cv.isNumber()) {
                return false;
            }

            return getMeaningfulValues(region, false, this::evaluateTop10).contains(cv);
        case UNIQUE_VALUES:
            // Per Excel help, "duplicate" means matching value AND format
            // https://support.office.com/en-us/article/Filter-for-unique-values-or-remove-duplicate-values-ccf664b0-81d6-449b-bbe1-8daaec1e83c2
            return getMeaningfulValues(region, true, this::evaluateUniqueValues).contains(cv);
        case DUPLICATE_VALUES:
            // Per Excel help, "duplicate" means matching value AND format
            // https://support.office.com/en-us/article/Filter-for-unique-values-or-remove-duplicate-values-ccf664b0-81d6-449b-bbe1-8daaec1e83c2
            return getMeaningfulValues(region, true, this::evaluateDuplicateValues).contains(cv);
        case ABOVE_AVERAGE:
            // from testing, Excel only operates on numbers and dates (which are stored as numbers) in the range.
            // numbers stored as text are ignored, but numbers formatted as text are treated as numbers.

            final ConditionFilterData conf = rule.getFilterConfiguration();

            // actually ordered, so iteration order is predictable
            List<ValueAndFormat> values = new ArrayList<>(getMeaningfulValues(region, false, this::evaluateAboveAverage));

            Double val = cv.isNumber() ? cv.getValue() : null;
            if (val == null) {
                return false;
            }

            double avg = values.get(0).value;
            double stdDev = values.get(1).value;

            /*
             * use StdDev, aboveAverage, equalAverage to find:
             * comparison value
             * operator type
             */

            Double comp = conf.getStdDev() > 0 ? (avg + (conf.getAboveAverage() ? 1 : -1) * stdDev * conf.getStdDev()) : avg;

            final OperatorEnum op;
            if (conf.getAboveAverage()) {
                if (conf.getEqualAverage()) {
                    op = OperatorEnum.GREATER_OR_EQUAL;
                } else {
                    op = OperatorEnum.GREATER_THAN;
                }
            } else {
                if (conf.getEqualAverage()) {
                    op = OperatorEnum.LESS_OR_EQUAL;
                } else {
                    op = OperatorEnum.LESS_THAN;
                }
            }
            return op.isValid(val, comp, null);
        case CONTAINS_TEXT:
            // implemented both by a cfRule "text" attribute and a formula.  Use the text.
            return text != null && cv.toString().toLowerCase(LocaleUtil.getUserLocale()).contains(lowerText);
        case NOT_CONTAINS_TEXT:
            // implemented both by a cfRule "text" attribute and a formula.  Use the text.
            return text == null || !cv.toString().toLowerCase(LocaleUtil.getUserLocale()).contains(lowerText);
        case BEGINS_WITH:
            // implemented both by a cfRule "text" attribute and a formula.  Use the text.
            return cv.toString().toLowerCase(LocaleUtil.getUserLocale()).startsWith(lowerText);
        case ENDS_WITH:
            // implemented both by a cfRule "text" attribute and a formula.  Use the text.
            return cv.toString().toLowerCase(LocaleUtil.getUserLocale()).endsWith(lowerText);
        case CONTAINS_BLANKS:
            try {
                String v = cv.getString();
                // see TextFunction.TRIM for implementation
                return StringUtil.isBlank(v);
            } catch (Exception e) {
                // not a valid string value, and not a blank cell (that's checked earlier)
                return false;
            }
        case NOT_CONTAINS_BLANKS:
            try {
                String v = cv.getString();
                // see TextFunction.TRIM for implementation
                return StringUtil.isNotBlank(v);
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
            return checkFormula(ref, region);
        default:
            return false;
        }
    }

    private Set<ValueAndFormat> evaluateTop10(List<ValueAndFormat> allValues) {
        final ConditionFilterData conf = rule.getFilterConfiguration();

        if (! conf.getBottom()) {
            allValues.sort(Collections.reverseOrder());
        } else {
            Collections.sort(allValues);
        }

        int limit = Math.toIntExact(conf.getRank());
        if (conf.getPercent()) {
            limit = allValues.size() * limit / 100;
        }
        if (allValues.size() <= limit) {
            return new HashSet<>(allValues);
        }

        return new HashSet<>(allValues.subList(0, limit));
    }

    private Set<ValueAndFormat> evaluateUniqueValues(List<ValueAndFormat> allValues) {
        Collections.sort(allValues);

        final Set<ValueAndFormat> unique = new HashSet<>();

        for (int i = 0; i < allValues.size(); i++) {
            final ValueAndFormat v = allValues.get(i);
            // skip this if the current value matches the next one, or is the last one and matches the previous one
            if ( (i < allValues.size()-1 && v.equals(allValues.get(i+1)) ) || ( i > 0 && i == allValues.size()-1 && v.equals(allValues.get(i-1)) ) ) {
                // current value matches next value, skip both
                i++;
                continue;
            }
            unique.add(v);
        }

        return unique;
    }

    public Set<ValueAndFormat> evaluateDuplicateValues(List<ValueAndFormat> allValues) {
        Collections.sort(allValues);

        final Set<ValueAndFormat> dup = new HashSet<>();

        for (int i = 0; i < allValues.size(); i++) {
            final ValueAndFormat v = allValues.get(i);
            // skip this if the current value matches the next one, or is the last one and matches the previous one
            if ( (i < allValues.size()-1 && v.equals(allValues.get(i+1)) ) || ( i > 0 && i == allValues.size()-1 && v.equals(allValues.get(i-1)) ) ) {
                // current value matches next value, add one
                dup.add(v);
                i++;
            }
        }
        return dup;
    }

    private Set<ValueAndFormat> evaluateAboveAverage(List<ValueAndFormat> allValues) {
        double total = 0;
        ValueEval[] pop = new ValueEval[allValues.size()];
        for (int i = 0; i < allValues.size(); i++) {
            ValueAndFormat v = allValues.get(i);
            total += v.value;
            pop[i] = new NumberEval(v.value);
        }

        final Set<ValueAndFormat> avgSet = new LinkedHashSet<>(1);
        avgSet.add(new ValueAndFormat(allValues.isEmpty() ? 0 : total / allValues.size(), null, decimalTextFormat));

        final double stdDev = allValues.size() <= 1 ? 0 : ((NumberEval) AggregateFunction.STDEV.evaluate(pop, 0, 0)).getNumberValue();
        avgSet.add(new ValueAndFormat(stdDev, null, decimalTextFormat));
        return avgSet;
    }

    /**
     * from testing, Excel only operates on numbers and dates (which are stored as numbers) in the range.
     * numbers stored as text are ignored, but numbers formatted as text are treated as numbers.
     *
     * @param func instances evaluate the values for a region and return the positive matches for the function type.
     *
     * @return the meaningful values in the range of cells specified
     */
    private Set<ValueAndFormat> getMeaningfulValues(CellRangeAddress region, boolean withText, Function<List<ValueAndFormat>,Set<ValueAndFormat>> func) {
        Set<ValueAndFormat> values = meaningfulRegionValues.get(region);
        if (values != null) {
            return values;
        }

        List<ValueAndFormat> allValues = new ArrayList<>((region.getLastColumn() - region.getFirstColumn() + 1) * (region.getLastRow() - region.getFirstRow() + 1));

        for (int r=region.getFirstRow(); r <= region.getLastRow(); r++) {
            final Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                Cell cell = row.getCell(c);
                final ValueAndFormat cv = getCellValue(cell);
                if (withText || cv.isNumber()) {
                    allValues.add(cv);
                }
            }
        }

        values = func.apply(allValues);
        meaningfulRegionValues.put(region, values);

        return values;
    }

    private ValueAndFormat getCellValue(Cell cell) {
        if (cell != null) {
            final String format = cell.getCellStyle().getDataFormatString();
            CellType type = cell.getCellType();
            if (type == CellType.FORMULA) {
                type = cell.getCachedFormulaResultType();
            }
            switch (type) {
                case NUMERIC:
                    return new ValueAndFormat(cell.getNumericCellValue(), format, decimalTextFormat);
                case STRING:
                case BOOLEAN:
                    return new ValueAndFormat(cell.getStringCellValue(), format);
                default:
                    break;
            }
        }
        return new ValueAndFormat("", "");
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    protected static class ValueAndFormat implements Comparable<ValueAndFormat> {

        private final Double value;
        private final String string;
        private final String format;
        private final DecimalFormat decimalTextFormat;

        public ValueAndFormat(Double value, String format, DecimalFormat df) {
            this.value = value;
            this.format = format;
            string = null;
            decimalTextFormat = df;
        }

        public ValueAndFormat(String value, String format) {
            this.value = null;
            this.format = format;
            string = value;
            decimalTextFormat = null;
        }

        public boolean isNumber() {
            return value != null;
        }

        public Double getValue() {
            return value;
        }

        public String getString() {
            return string;
        }

        public String toString() {
            if(isNumber()) {
                return decimalTextFormat.format(getValue().doubleValue());
            } else {
                return getString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ValueAndFormat)) {
                return false;
            }
            ValueAndFormat o = (ValueAndFormat) obj;
            return (Objects.equals(value, o.value)
                    && Objects.equals(format, o.format)
                    && Objects.equals(string, o.string));
        }

        /**
         * Note: this class has a natural ordering that is inconsistent with equals.
         * @return value comparison
         */
        @Override
        public int compareTo(ValueAndFormat o) {
            if (value == null && o.value != null) {
                return 1;
            }
            if (o.value == null && value != null) {
                return -1;
            }
            int cmp = value == null ? 0 : value.compareTo(o.value);
            if (cmp != 0) {
                return cmp;
            }

            if (string == null && o.string != null) {
                return 1;
            }
            if (o.string == null && string != null) {
                return -1;
            }

            return string == null ? 0 : string.compareTo(o.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(string,value,format);
        }
    }
}
