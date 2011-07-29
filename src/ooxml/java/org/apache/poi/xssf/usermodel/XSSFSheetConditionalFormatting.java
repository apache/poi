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

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCfType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTConditionalFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STConditionalFormattingOperator;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * @author Yegor Kozlov
 */
public class XSSFSheetConditionalFormatting implements SheetConditionalFormatting {
    private final XSSFSheet _sheet;

    /* package */ XSSFSheetConditionalFormatting(XSSFSheet sheet) {
        _sheet = sheet;
    }

    /**
     * A factory method allowing to create a conditional formatting rule
     * with a cell comparison operator<p/>
     * TODO - formulas containing cell references are currently not parsed properly
     *
     * @param comparisonOperation - a constant value from
     *		 <tt>{@link org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator}</tt>: <p>
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
     * {@link org.apache.poi.ss.usermodel.ComparisonOperator#BETWEEN}) and
     * {@link org.apache.poi.ss.usermodel.ComparisonOperator#NOT_BETWEEN} operations)
     */
    public XSSFConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula1,
            String formula2) {

        XSSFConditionalFormattingRule rule = new XSSFConditionalFormattingRule(_sheet);
        CTCfRule cfRule = rule.getCTCfRule();
        cfRule.addFormula(formula1);
        if(formula2 != null) cfRule.addFormula(formula2);
        cfRule.setType(STCfType.CELL_IS);
        STConditionalFormattingOperator.Enum operator;
        switch (comparisonOperation){
            case ComparisonOperator.BETWEEN: operator = STConditionalFormattingOperator.BETWEEN; break;
            case ComparisonOperator.NOT_BETWEEN: operator = STConditionalFormattingOperator.NOT_BETWEEN; break;
            case ComparisonOperator.LT: operator = STConditionalFormattingOperator.LESS_THAN; break;
            case ComparisonOperator.LE: operator = STConditionalFormattingOperator.LESS_THAN_OR_EQUAL; break;
            case ComparisonOperator.GT: operator = STConditionalFormattingOperator.GREATER_THAN; break;
            case ComparisonOperator.GE: operator = STConditionalFormattingOperator.GREATER_THAN_OR_EQUAL; break;
            case ComparisonOperator.EQUAL: operator = STConditionalFormattingOperator.EQUAL; break;
            case ComparisonOperator.NOT_EQUAL: operator = STConditionalFormattingOperator.NOT_EQUAL; break;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + comparisonOperation);
        }
        cfRule.setOperator(operator);

        return rule;
    }

    public XSSFConditionalFormattingRule createConditionalFormattingRule(
            byte comparisonOperation,
            String formula) {

        return createConditionalFormattingRule(comparisonOperation, formula, null);
    }

    /**
     * A factory method allowing to create a conditional formatting rule with a formula.<br>
     *
     * @param formula - formula for the valued, compared with the cell
     */
    public XSSFConditionalFormattingRule createConditionalFormattingRule(String formula) {
        XSSFConditionalFormattingRule rule = new XSSFConditionalFormattingRule(_sheet);
        CTCfRule cfRule = rule.getCTCfRule();
        cfRule.addFormula(formula);
        cfRule.setType(STCfType.EXPRESSION);
        return rule;
    }

    public int addConditionalFormatting(CellRangeAddress[] regions, ConditionalFormattingRule[] cfRules) {
        if (regions == null) {
            throw new IllegalArgumentException("regions must not be null");
        }
        for(CellRangeAddress range : regions) range.validate(SpreadsheetVersion.EXCEL2007);

        if (cfRules == null) {
            throw new IllegalArgumentException("cfRules must not be null");
        }
        if (cfRules.length == 0) {
            throw new IllegalArgumentException("cfRules must not be empty");
        }
        if (cfRules.length > 3) {
            throw new IllegalArgumentException("Number of rules must not exceed 3");
        }
        XSSFConditionalFormattingRule[] hfRules;
        if(cfRules instanceof XSSFConditionalFormattingRule[]) hfRules = (XSSFConditionalFormattingRule[])cfRules;
        else {
            hfRules = new XSSFConditionalFormattingRule[cfRules.length];
            System.arraycopy(cfRules, 0, hfRules, 0, hfRules.length);
        }
        CellRangeAddress[] mergeCellRanges = CellRangeUtil.mergeCellRanges(regions);
        CTConditionalFormatting cf = _sheet.getCTWorksheet().addNewConditionalFormatting();
        List<String> refs = new ArrayList<String>();
        for(CellRangeAddress a : mergeCellRanges) refs.add(a.formatAsString());
        cf.setSqref(refs);


        int priority = 1;
        for(CTConditionalFormatting c : _sheet.getCTWorksheet().getConditionalFormattingList()){
            priority += c.sizeOfCfRuleArray();
        }

        for(ConditionalFormattingRule rule : cfRules){
            XSSFConditionalFormattingRule xRule = (XSSFConditionalFormattingRule)rule;
            xRule.getCTCfRule().setPriority(priority++);
            cf.addNewCfRule().set(xRule.getCTCfRule());
        }
        return _sheet.getCTWorksheet().sizeOfConditionalFormattingArray() - 1;
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            ConditionalFormattingRule rule1)
    {
        return addConditionalFormatting(regions,
                rule1 == null ? null : new XSSFConditionalFormattingRule[] {
                (XSSFConditionalFormattingRule)rule1
        });
    }

    public int addConditionalFormatting(CellRangeAddress[] regions,
            ConditionalFormattingRule rule1, ConditionalFormattingRule rule2)
    {
        return addConditionalFormatting(regions,
                rule1 == null ? null : new XSSFConditionalFormattingRule[] {
                    (XSSFConditionalFormattingRule)rule1,
                    (XSSFConditionalFormattingRule)rule2
            });
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
    public int addConditionalFormatting( ConditionalFormatting cf ) {
        XSSFConditionalFormatting xcf = (XSSFConditionalFormatting)cf;
        CTWorksheet sh = _sheet.getCTWorksheet();
        sh.addNewConditionalFormatting().set(xcf.getCTConditionalFormatting().copy());
        return sh.sizeOfConditionalFormattingArray() - 1;
    }

    /**
    * gets Conditional Formatting object at a particular index
    *
    * @param index
    *			of the Conditional Formatting object to fetch
    * @return Conditional Formatting object
    */
    public XSSFConditionalFormatting getConditionalFormattingAt(int index) {
        checkIndex(index);
        CTConditionalFormatting cf = _sheet.getCTWorksheet().getConditionalFormattingArray(index);
        return new XSSFConditionalFormatting(_sheet, cf);
    }

    /**
    * @return number of Conditional Formatting objects of the sheet
    */
    public int getNumConditionalFormattings() {
        return _sheet.getCTWorksheet().sizeOfConditionalFormattingArray();
    }

    /**
    * removes a Conditional Formatting object by index
    * @param index of a Conditional Formatting object to remove
    */
    public void removeConditionalFormatting(int index) {
        checkIndex(index);
        _sheet.getCTWorksheet().getConditionalFormattingList().remove(index);
    }

    private void checkIndex(int index) {
        int cnt = getNumConditionalFormattings();
        if (index < 0 || index >= cnt) {
            throw new IllegalArgumentException("Specified CF index " + index
                    + " is outside the allowable range (0.." + (cnt - 1) + ")");
        }
    }

}
