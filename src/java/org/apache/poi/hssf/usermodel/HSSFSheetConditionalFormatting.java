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

import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.aggregates.CFRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ConditionalFormattingTable;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * The 'Conditional Formatting' facet of <tt>HSSFSheet</tt>
 */
public final class HSSFSheetConditionalFormatting implements SheetConditionalFormatting {
    private final HSSFSheet _sheet;
    private final ConditionalFormattingTable _conditionalFormattingTable;

    /* package */ HSSFSheetConditionalFormatting(HSSFSheet sheet) {
        _sheet = sheet;
        _conditionalFormattingTable = sheet.getSheet().getConditionalFormattingTable();
    }

    /**
     * A factory method allowing to create a conditional formatting rule
     * with a cell comparison operator<p>
     * TODO - formulas containing cell references are currently not parsed properly
     *
     * @param comparisonOperation - a constant value from
     *		 <tt>{@link org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator}</tt>: <p>
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
     * {@link org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator#BETWEEN}) and
     * {@link org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator#NOT_BETWEEN} operations)
     */
    public HSSFConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula1,
            String formula2) {
        CFRuleRecord rr = CFRuleRecord.create(_sheet, comparisonOperation, formula1, formula2);
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }

    public HSSFConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula1) {
        CFRuleRecord rr = CFRuleRecord.create(_sheet, comparisonOperation, formula1, null);
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }

    /**
     * A factory method allowing to create a conditional formatting rule with a formula.<br>
     *
     * The formatting rules are applied by Excel when the value of the formula not equal to 0.<p>
     * TODO - formulas containing cell references are currently not parsed properly
     * @param formula - formula for the valued, compared with the cell
     */
    public HSSFConditionalFormattingRule createConditionalFormattingRule(String formula) {
        CFRuleRecord rr = CFRuleRecord.create(_sheet, formula);
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }

    /**
     * A factory method allowing the creation of conditional formatting
     *  rules using an Icon Set / Multi-State formatting.
     * The thresholds for it will be created, but will be empty
     *  and require configuring with 
     *  {@link HSSFConditionalFormattingRule#getMultiStateFormatting()}
     *  then
     *  {@link HSSFIconMultiStateFormatting#getThresholds()}
     */
    public HSSFConditionalFormattingRule createConditionalFormattingRule(
            IconSet iconSet) {
        CFRule12Record rr = CFRule12Record.create(_sheet, iconSet);
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }

    /**
     * Create a Databar conditional formatting rule.
     * <p>The thresholds and colour for it will be created, but will be 
     *  empty and require configuring with 
     *  {@link HSSFConditionalFormattingRule#getDataBarFormatting()}
     *  then
     *  {@link HSSFDataBarFormatting#getMinThreshold()}
     *  and 
     *  {@link HSSFDataBarFormatting#getMaxThreshold()}
     */
    public HSSFConditionalFormattingRule createConditionalFormattingRule(HSSFExtendedColor color) {
        CFRule12Record rr = CFRule12Record.create(_sheet, color.getExtendedColor());
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }
    public HSSFConditionalFormattingRule createConditionalFormattingRule(ExtendedColor color) {
        return createConditionalFormattingRule((HSSFExtendedColor)color);
    }

    /**
     * Create a Color Scale / Color Gradient conditional formatting rule.
     * <p>The thresholds and colours for it will be created, but will be 
     *  empty and require configuring with 
     *  {@link HSSFConditionalFormattingRule#getColorScaleFormatting()}
     *  then
     *  {@link HSSFColorScaleFormatting#getThresholds()}
     *  and
     *  {@link HSSFColorScaleFormatting#getColors()}
     */
    public HSSFConditionalFormattingRule createConditionalFormattingColorScaleRule() {
        CFRule12Record rr = CFRule12Record.createColorScale(_sheet);
        return new HSSFConditionalFormattingRule(_sheet, rr);
    }
    
    /**
     * Adds a copy of HSSFConditionalFormatting object to the sheet
     * <p>This method could be used to copy HSSFConditionalFormatting object
     * from one sheet to another. For example:
     * <pre>
     * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
     * newSheet.addConditionalFormatting(cf);
     * </pre>
     *
     * @param cf HSSFConditionalFormatting object
     * @return index of the new Conditional Formatting object
     */
    public int addConditionalFormatting( HSSFConditionalFormatting cf ) {
        CFRecordsAggregate cfraClone = cf.getCFRecordsAggregate().cloneCFAggregate();

        return _conditionalFormattingTable.add(cfraClone);
    }

    public int addConditionalFormatting( ConditionalFormatting cf ) {
        return addConditionalFormatting((HSSFConditionalFormatting)cf);
    }

    /**
     * Allows to add a new Conditional Formatting set to the sheet.
     *
     * @param regions - list of rectangular regions to apply conditional formatting rules
     * @param cfRules - set of up to three conditional formatting rules
     *
     * @return index of the newly created Conditional Formatting object
     */
    public int addConditionalFormatting(CellRangeAddress[] regions, HSSFConditionalFormattingRule[] cfRules) {
        if (regions == null) {
            throw new IllegalArgumentException("regions must not be null");
        }
        for(CellRangeAddress range : regions) range.validate(SpreadsheetVersion.EXCEL97);

        if (cfRules == null) {
            throw new IllegalArgumentException("cfRules must not be null");
        }
        if (cfRules.length == 0) {
            throw new IllegalArgumentException("cfRules must not be empty");
        }
        if (cfRules.length > 3) {
            throw new IllegalArgumentException("Number of rules must not exceed 3");
        }

        CFRuleBase[] rules = new CFRuleBase[cfRules.length];
        for (int i = 0; i != cfRules.length; i++) {
            rules[i] = cfRules[i].getCfRuleRecord();
        }
        CFRecordsAggregate cfra = new CFRecordsAggregate(regions, rules);
        return _conditionalFormattingTable.add(cfra);
    }

    public int addConditionalFormatting(CellRangeAddress[] regions, ConditionalFormattingRule[] cfRules) {
        HSSFConditionalFormattingRule[] hfRules;
        if(cfRules instanceof HSSFConditionalFormattingRule[]) hfRules = (HSSFConditionalFormattingRule[])cfRules;
        else {
            hfRules = new HSSFConditionalFormattingRule[cfRules.length];
            System.arraycopy(cfRules, 0, hfRules, 0, hfRules.length);
        }
        return addConditionalFormatting(regions, hfRules);
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            HSSFConditionalFormattingRule rule1) {
        return addConditionalFormatting(regions, rule1 == null ? 
                    null : new HSSFConditionalFormattingRule[] { rule1 }
        );
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            ConditionalFormattingRule rule1) {
        return addConditionalFormatting(regions,  (HSSFConditionalFormattingRule)rule1);
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            HSSFConditionalFormattingRule rule1,
            HSSFConditionalFormattingRule rule2) {
        return addConditionalFormatting(regions,
                new HSSFConditionalFormattingRule[] { rule1, rule2 });
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            ConditionalFormattingRule rule1,
            ConditionalFormattingRule rule2) {
        return addConditionalFormatting(regions,
                (HSSFConditionalFormattingRule)rule1,
                (HSSFConditionalFormattingRule)rule2
        );
    }

    /**
     * gets Conditional Formatting object at a particular index
     *
     * @param index
     *			of the Conditional Formatting object to fetch
     * @return Conditional Formatting object
     */
    public HSSFConditionalFormatting getConditionalFormattingAt(int index) {
        CFRecordsAggregate cf = _conditionalFormattingTable.get(index);
        if (cf == null) {
            return null;
        }
        return new HSSFConditionalFormatting(_sheet, cf);
    }

    /**
     * @return number of Conditional Formatting objects of the sheet
     */
    public int getNumConditionalFormattings() {
        return _conditionalFormattingTable.size();
    }

    /**
     * removes a Conditional Formatting object by index
     * @param index of a Conditional Formatting object to remove
     */
    public void removeConditionalFormatting(int index) {
        _conditionalFormattingTable.remove(index);
    }
}
