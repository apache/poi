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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.ss.util.CellReference;

/**
 * Evaluates Conditional Formatting constraints.<p>
 *
 * For performance reasons, this class keeps a cache of all previously evaluated rules and cells.  
 * Be sure to call {@link #clearAllCachedFormats()} if any conditional formats are modified, added, or deleted,
 * and {@link #clearAllCachedValues()} whenever cell values change.
 * <p>
 * 
 */
public class ConditionalFormattingEvaluator {

    private final WorkbookEvaluator workbookEvaluator;
    private final Workbook workbook;
    
    /**
     * All the underlying structures, for both HSSF and XSSF, repeatedly go to the raw bytes/XML for the
     * different pieces used in the ConditionalFormatting* structures.  That's highly inefficient,
     * and can cause significant lag when checking formats for large workbooks.
     * <p>
     * Instead we need a cached version that is discarded when definitions change.
     * <p>
     * Sheets don't implement equals, and since its an interface, 
     * there's no guarantee instances won't be recreated on the fly by some implementation.
     * So we use sheet name.
     */
    private final Map<String, List<EvaluationConditionalFormatRule>> formats = new HashMap<>();
    
    /**
     * Evaluating rules for cells in their region(s) is expensive, so we want to cache them,
     * and empty/reevaluate the cache when values change.
     * <p>
     * Rule lists are in priority order, as evaluated by Excel (smallest priority # for XSSF, definition order for HSSF)
     * <p>
     * CellReference implements equals().
     */
    private final Map<CellReference, List<EvaluationConditionalFormatRule>> values = new HashMap<>();

    public ConditionalFormattingEvaluator(Workbook wb, WorkbookEvaluatorProvider provider) {
        this.workbook = wb;
        this.workbookEvaluator = provider._getWorkbookEvaluator();
    }
    
    protected WorkbookEvaluator getWorkbookEvaluator() {
        return workbookEvaluator;
    }
    
    /**
     * Call this whenever rules are added, reordered, or removed, or a rule formula is changed 
     * (not the formula inputs but the formula expression itself)
     */
    public void clearAllCachedFormats() {
        formats.clear();
    }
    
    /**
     * Call this whenever cell values change in the workbook, so condional formats are re-evaluated 
     * for all cells.
     * <p>
     * TODO: eventually this should work like {@link EvaluationCache#notifyUpdateCell(int, int, EvaluationCell)}
     * and only clear values that need recalculation based on the formula dependency tree.
     */
    public void clearAllCachedValues() {
        values.clear();
    }

    /**
     * lazy load by sheet since reading can be expensive
     * 
     * @param sheet The sheet to look at
     * @return unmodifiable list of rules
     */
    protected List<EvaluationConditionalFormatRule> getRules(Sheet sheet) {
        final String sheetName = sheet.getSheetName();
        List<EvaluationConditionalFormatRule> rules = formats.get(sheetName);
        if (rules == null) {
            if (formats.containsKey(sheetName)) {
                return Collections.emptyList();
            }
            final SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
            final int count = scf.getNumConditionalFormattings();
            rules = new ArrayList<>(count);
            formats.put(sheetName, rules);
            for (int i=0; i < count; i++) {
                ConditionalFormatting f = scf.getConditionalFormattingAt(i);
                //optimization, as this may be expensive for lots of ranges
                final CellRangeAddress[] regions = f.getFormattingRanges();
                for (int r=0; r < f.getNumberOfRules(); r++) {
                    ConditionalFormattingRule rule = f.getRule(r);
                    rules.add(new EvaluationConditionalFormatRule(workbookEvaluator, sheet, f, i, rule, r, regions));
                }
            }
            // need them in formatting and priority order so logic works right
            Collections.sort(rules);
        }
        return Collections.unmodifiableList(rules);
    }
    
    /**
     * This checks all applicable {@link ConditionalFormattingRule}s for the cell's sheet, 
     * in defined "priority" order, returning the matches if any.  This is a property currently
     * not exposed from <code>CTCfRule</code> in <code>XSSFConditionalFormattingRule</code>.  
     * <p>
     * Most cells will have zero or one applied rule, but it is possible to define multiple rules
     * that apply at the same time to the same cell, thus the List result.
     * <p>
     * Note that to properly apply conditional rules, care must be taken to offset the base 
     * formula by the relative position of the current cell, or the wrong value is checked.
     * This is handled by {@link WorkbookEvaluator#evaluate(String, CellReference, CellRangeAddressBase)}.
     * <p>
     * If the cell exists and is a formula cell, its cached value may be used for rule evaluation, so
     * make sure it is up to date.  If values have changed, it is best to call 
     * {@link FormulaEvaluator#evaluateFormulaCell(Cell)} or {@link FormulaEvaluator#evaluateAll()} first,
     * or the wrong conditional results may be returned. 
     * 
     * @param cellRef NOTE: if no sheet name is specified, this uses the workbook active sheet
     * @return Unmodifiable List of {@link EvaluationConditionalFormatRule}s that apply to the current cell value,
     *         in priority order, as evaluated by Excel (smallest priority # for XSSF, definition order for HSSF), 
     *         or null if none apply
     */
    public List<EvaluationConditionalFormatRule> getConditionalFormattingForCell(final CellReference cellRef) {
        List<EvaluationConditionalFormatRule> rules = values.get(cellRef);
        
        if (rules == null) {
            // compute and cache them
            rules = new ArrayList<>();
            
            final Sheet sheet;
            if (cellRef.getSheetName() != null) {
                sheet = workbook.getSheet(cellRef.getSheetName());
            } else {
                sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
            }
            
            /*
             * Per Excel help:
             * https://support.office.com/en-us/article/Manage-conditional-formatting-rule-precedence-e09711a3-48df-4bcb-b82c-9d8b8b22463d#__toc269129417
             * stopIfTrue is true for all rules from HSSF files, and an explicit value for XSSF files.
             * thus the explicit ordering of the rule lists in #getFormattingRulesForSheet(Sheet)
             */
            boolean stopIfTrue = false;
            for (EvaluationConditionalFormatRule rule : getRules(sheet)) {
                
                if (stopIfTrue) {
                    continue; // a previous rule matched and wants no more evaluations
                }

                if (rule.matches(cellRef)) {
                    rules.add(rule);
                    stopIfTrue = rule.getRule().getStopIfTrue();
                }
            }
            Collections.sort(rules);
            values.put(cellRef, rules);
        }
        
        return Collections.unmodifiableList(rules);
    }
    
    /**
     * This checks all applicable {@link ConditionalFormattingRule}s for the cell's sheet, 
     * in defined "priority" order, returning the matches if any.  This is a property currently
     * not exposed from <code>CTCfRule</code> in <code>XSSFConditionalFormattingRule</code>.  
     * <p>
     * Most cells will have zero or one applied rule, but it is possible to define multiple rules
     * that apply at the same time to the same cell, thus the List result.
     * <p>
     * Note that to properly apply conditional rules, care must be taken to offset the base 
     * formula by the relative position of the current cell, or the wrong value is checked.
     * This is handled by {@link WorkbookEvaluator#evaluate(String, CellReference, CellRangeAddressBase)}.
     * <p>
     * If the cell exists and is a formula cell, its cached value may be used for rule evaluation, so
     * make sure it is up to date.  If values have changed, it is best to call 
     * {@link FormulaEvaluator#evaluateFormulaCell(Cell)} or {@link FormulaEvaluator#evaluateAll()} first,
     * or the wrong conditional results may be returned. 
     * 
     * @param cell The cell to look for
     * @return Unmodifiable List of {@link EvaluationConditionalFormatRule}s that apply to the current cell value,
     *         in priority order, as evaluated by Excel (smallest priority # for XSSF, definition order for HSSF), 
     *         or null if none apply
     */
    public List<EvaluationConditionalFormatRule> getConditionalFormattingForCell(Cell cell) {
        return getConditionalFormattingForCell(getRef(cell));
    }
    
    public static CellReference getRef(Cell cell) {
        return new CellReference(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), false, false);
    }
    
    /**
     * Retrieve all formatting rules for the sheet with the given name.
     *
     * @param sheetName The name of the sheet to look at
     * @return unmodifiable list of all Conditional format rules for the given sheet, if any
     */
    public List<EvaluationConditionalFormatRule> getFormatRulesForSheet(String sheetName) {
        return getFormatRulesForSheet(workbook.getSheet(sheetName));
    }
    
    /**
     * Retrieve all formatting rules for the given sheet.
     *
     * @param sheet The sheet to look at
     * @return unmodifiable list of all Conditional format rules for the given sheet, if any
     */
    public List<EvaluationConditionalFormatRule> getFormatRulesForSheet(Sheet sheet) {
        return getRules(sheet);
    }
    
    /**
     * Conditional formatting rules can apply only to cells in the sheet to which they are attached.
     * The POI data model does not have a back-reference to the owning sheet, so it must be passed in separately.
     * <p>
     * We could overload this with convenience methods taking a sheet name and sheet index as well.
     * <p>
     * @param sheet containing the rule
     * @param conditionalFormattingIndex of the {@link ConditionalFormatting} instance in the sheet's array
     * @param ruleIndex of the {@link ConditionalFormattingRule} instance within the {@link ConditionalFormatting}
     * @return unmodifiable List of all cells in the rule's region matching the rule's condition
     */
    public List<Cell> getMatchingCells(Sheet sheet, int conditionalFormattingIndex, int ruleIndex) {
        for (EvaluationConditionalFormatRule rule : getRules(sheet)) {
            if (rule.getSheet().equals(sheet) && rule.getFormattingIndex() == conditionalFormattingIndex && rule.getRuleIndex() == ruleIndex) {
                return getMatchingCells(rule);
            }
        }
        return Collections.emptyList();
    }
    
    /**
     * Retrieve all cells where the given formatting rule evaluates to true.
     *
     * @param rule The rule to look at
     * @return unmodifiable List of all cells in the rule's region matching the rule's condition
     */
    public List<Cell> getMatchingCells(EvaluationConditionalFormatRule rule) {
        final List<Cell> cells = new ArrayList<>();
        final Sheet sheet = rule.getSheet();
        
        for (CellRangeAddress region : rule.getRegions()) {
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                final Row row = sheet.getRow(r);
                if (row == null) {
                    continue; // no cells to check
                }
                for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                    final Cell cell = row.getCell(c);
                    if (cell == null) {
                        continue;
                    }
                    
                    List<EvaluationConditionalFormatRule> cellRules = getConditionalFormattingForCell(cell);
                    if (cellRules.contains(rule)) {
                        cells.add(cell);
                    }
                }
            }
        }
        return Collections.unmodifiableList(cells);
    }
}
