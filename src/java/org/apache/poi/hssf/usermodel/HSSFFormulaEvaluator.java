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

import java.util.Iterator;
import java.util.Stack;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ControlPtg;
import org.apache.poi.hssf.record.formula.ErrPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.MemErrPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.record.formula.UnknownPtg;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.FunctionEval;
import org.apache.poi.hssf.record.formula.eval.LazyAreaEval;
import org.apache.poi.hssf.record.formula.eval.LazyRefEval;
import org.apache.poi.hssf.record.formula.eval.NameEval;
import org.apache.poi.hssf.record.formula.eval.NameXEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.util.CellReference;

/**
 * Evaluates formula cells.<p/>
 * 
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearCache()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 */
public class HSSFFormulaEvaluator {

	/**
	 * used to track the number of evaluations
	 */
    private static final class Counter {
        public int value;
        public Counter() {
            value = 0;
        }
    }

    private final HSSFSheet _sheet;
    private final HSSFWorkbook _workbook;
    private final EvaluationCache _cache;

    private Counter _evaluationCounter;

    public HSSFFormulaEvaluator(HSSFSheet sheet, HSSFWorkbook workbook) {
        this(sheet, workbook, new EvaluationCache(), new Counter());
    }

    private HSSFFormulaEvaluator(HSSFSheet sheet, HSSFWorkbook workbook, EvaluationCache cache, Counter evaluationCounter) {
        _sheet = sheet;
        _workbook = workbook;
        _cache = cache;
        _evaluationCounter = evaluationCounter;
    }

    /**
     * for debug use. Used in toString methods
     */
    public String getSheetName(HSSFSheet sheet) {
        return _workbook.getSheetName(_workbook.getSheetIndex(sheet));
    }
    /**
     * for debug/test use
     */
    /* package */ int getEvaluationCount() {
        return _evaluationCounter.value;
    }

    private static boolean isDebugLogEnabled() {
        return false;
    }
    private static void logDebug(String s) {
        if (isDebugLogEnabled()) {
            System.out.println(s);
        }
    }

    /**
     * Does nothing
     * @deprecated (Aug 2008) - not needed, since the current row can be derived from the cell
     */
    public void setCurrentRow(HSSFRow row) {
        // do nothing
        if (false) {
            row.getClass(); // suppress unused parameter compiler warning
        }
    }

    /**
     * Should be called whenever there are changes to input cells in the evaluated workbook.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    public void clearCache() {
        _cache.clear();
    }

    /**
     * Returns an underlying FormulaParser, for the specified
     *  Formula String and HSSFWorkbook.
     * This will allow you to generate the Ptgs yourself, if
     *  your needs are more complex than just having the
     *  formula evaluated.
     */
    public static FormulaParser getUnderlyingParser(HSSFWorkbook workbook, String formula) {
        return new FormulaParser(formula, workbook);
    }

    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * @param cell
     */
    public CellValue evaluate(HSSFCell cell) {
        CellValue retval = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:
                retval = new CellValue(HSSFCell.CELL_TYPE_BLANK);
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                retval = new CellValue(HSSFCell.CELL_TYPE_BOOLEAN);
                retval.setBooleanValue(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
                retval.setErrorValue(cell.getErrorCellValue());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                retval = getCellValueForEval(internalEvaluate(cell, _sheet));
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                retval = new CellValue(HSSFCell.CELL_TYPE_NUMERIC);
                retval.setNumberValue(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                retval = new CellValue(HSSFCell.CELL_TYPE_STRING);
                retval.setRichTextStringValue(cell.getRichStringCellValue());
                break;
            }
        }
        return retval;
    }


    /**
     * If cell contains formula, it evaluates the formula,
     *  and saves the result of the formula. The cell
     *  remains as a formula cell.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the type of the formula result is returned,
     *  so you know what kind of value is also stored with
     *  the formula.
     * <pre>
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluateInCell(HSSFCell)}
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as HSSFCell.CELL_TYPE_FORMULA however)
     */
    public int evaluateFormulaCell(HSSFCell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_FORMULA:
                CellValue cv = getCellValueForEval(internalEvaluate(cell, _sheet));
                switch (cv.getCellType()) {
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cell.setCellValue(cv.getBooleanValue());
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    cell.setCellValue(cv.getErrorValue());
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    cell.setCellValue(cv.getNumberValue());
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    cell.setCellValue(cv.getRichTextStringValue());
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    break;
                case HSSFCell.CELL_TYPE_FORMULA: // this will never happen, we have already evaluated the formula
                    break;
                }
                return cv.getCellType();
            }
        }
        return -1;
    }

    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the same instance of HSSFCell is returned to
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * Be aware that your cell value will be changed to hold the
     *  result of the formula. If you simply want the formula
     *  value computed for you, use {@link #evaluateFormulaCell(HSSFCell)}
     * @param cell
     */
    public HSSFCell evaluateInCell(HSSFCell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_FORMULA:
                CellValue cv = getCellValueForEval(internalEvaluate(cell, _sheet));
                switch (cv.getCellType()) {
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
                    cell.setCellValue(cv.getBooleanValue());
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    cell.setCellErrorValue(cv.getErrorValue());
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(cv.getNumberValue());
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    cell.setCellValue(cv.getRichTextStringValue());
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    break;
                case HSSFCell.CELL_TYPE_FORMULA: // this will never happen, we have already evaluated the formula
                    break;
                }
            }
        }
        return cell;
    }

    /**
     * Loops over all cells in all sheets of the supplied
     *  workbook.
     * For cells that contain formulas, their formulas are
     *  evaluated, and the results are saved. These cells
     *  remain as formula cells.
     * For cells that do not contain formulas, no changes
     *  are made.
     * This is a helpful wrapper around looping over all
     *  cells, and calling evaluateFormulaCell on each one.
     */
    public static void evaluateAllFormulaCells(HSSFWorkbook wb) {
        for(int i=0; i<wb.getNumberOfSheets(); i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet, wb);

            for (Iterator rit = sheet.rowIterator(); rit.hasNext();) {
                HSSFRow r = (HSSFRow)rit.next();

                for (Iterator cit = r.cellIterator(); cit.hasNext();) {
                    HSSFCell c = (HSSFCell)cit.next();
                    if (c.getCellType() == HSSFCell.CELL_TYPE_FORMULA)
                        evaluator.evaluateFormulaCell(c);
                }
            }
        }
    }


    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param eval
     */
    private static CellValue getCellValueForEval(ValueEval eval) {
        CellValue retval = null;
        if (eval != null) {
            if (eval instanceof NumberEval) {
                NumberEval ne = (NumberEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_NUMERIC);
                retval.setNumberValue(ne.getNumberValue());
            }
            else if (eval instanceof BoolEval) {
                BoolEval be = (BoolEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_BOOLEAN);
                retval.setBooleanValue(be.getBooleanValue());
            }
            else if (eval instanceof StringEval) {
                StringEval ne = (StringEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_STRING);
                retval.setStringValue(ne.getStringValue());
            }
            else if (eval instanceof BlankEval) {
                retval = new CellValue(HSSFCell.CELL_TYPE_BLANK);
            }
            else if (eval instanceof ErrorEval) {
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
                retval.setErrorValue((byte)((ErrorEval)eval).getErrorCode());
//                retval.setRichTextStringValue(new HSSFRichTextString("#An error occurred. check cell.getErrorCode()"));
            }
            else {
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
            }
        }
        return retval;
    }

    /**
     * Dev. Note: Internal evaluate must be passed only a formula cell
     * else a runtime exception will be thrown somewhere inside the method.
     * (Hence this is a private method.)
     */
    private ValueEval internalEvaluate(HSSFCell srcCell, HSSFSheet sheet) {
        int srcRowNum = srcCell.getRowIndex();
        int srcColNum = srcCell.getCellNum();

        ValueEval result;

        int sheetIndex = _workbook.getSheetIndex(sheet);
        result = _cache.getValue(sheetIndex, srcRowNum, srcColNum);
        if (result != null) {
            return result;
        }
        _evaluationCounter.value++;

        EvaluationCycleDetector tracker = EvaluationCycleDetectorManager.getTracker();

        if(!tracker.startEvaluate(_workbook, sheet, srcRowNum, srcColNum)) {
            return ErrorEval.CIRCULAR_REF_ERROR;
        }
        try {
            result = evaluateCell(srcRowNum, (short)srcColNum, srcCell.getCellFormula());
        } finally {
            tracker.endEvaluate(_workbook, sheet, srcRowNum, srcColNum);
            _cache.setValue(sheetIndex, srcRowNum, srcColNum, result);
        }
        if (isDebugLogEnabled()) {
            String sheetName = _workbook.getSheetName(sheetIndex);
            CellReference cr = new CellReference(srcRowNum, srcColNum);
            logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + result.toString());
        }
        return result;
    }
    private ValueEval evaluateCell(int srcRowNum, short srcColNum, String cellFormulaText) {

        Ptg[] ptgs = FormulaParser.parse(cellFormulaText, _workbook);

        Stack stack = new Stack();
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {

            // since we don't know how to handle these yet :(
            Ptg ptg = ptgs[i];
            if (ptg instanceof ControlPtg) {
                // skip Parentheses, Attr, etc
                continue;
            }
            if (ptg instanceof MemErrPtg) { continue; }
            if (ptg instanceof MissingArgPtg) {
                // TODO - might need to push BlankEval or MissingArgEval
                continue;
            }
            Eval opResult;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;

                if (optg instanceof UnionPtg) { continue; }

                OperationEval operation = OperationEvaluatorFactory.create(optg);

                int numops = operation.getNumberOfOperands();
                Eval[] ops = new Eval[numops];

                // storing the ops in reverse order since they are popping
                for (int j = numops - 1; j >= 0; j--) {
                    Eval p = (Eval) stack.pop();
                    ops[j] = p;
                }
                logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                opResult = invokeOperation(operation, ops, srcRowNum, srcColNum, _workbook, _sheet);
            } else {
                opResult = getEvalForPtg(ptg, _sheet);
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
            logDebug("push " + opResult);
            stack.push(opResult);
        }

        ValueEval value = ((ValueEval) stack.pop());
        if (!stack.isEmpty()) {
            throw new IllegalStateException("evaluation stack not empty");
        }
        value = dereferenceValue(value, srcRowNum, srcColNum);
        if (value instanceof BlankEval) {
            // Note Excel behaviour here. A blank final final value is converted to zero.
            return NumberEval.ZERO;
            // Formulas _never_ evaluate to blank.  If a formula appears to have evaluated to
            // blank, the actual value is empty string. This can be verified with ISBLANK().
        }
        return value;
    }

    /**
     * Dereferences a single value from any AreaEval or RefEval evaluation result.
     * If the supplied evaluationResult is just a plain value, it is returned as-is.
     * @return a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt>,
     *  <tt>BlankEval</tt> or <tt>ErrorEval</tt>. Never <code>null</code>.
     */
    private static ValueEval dereferenceValue(ValueEval evaluationResult, int srcRowNum, short srcColNum) {
        if (evaluationResult instanceof RefEval) {
            RefEval rv = (RefEval) evaluationResult;
            return rv.getInnerValueEval();
        }
        if (evaluationResult instanceof AreaEval) {
            AreaEval ae = (AreaEval) evaluationResult;
            if (ae.isRow()) {
                if(ae.isColumn()) {
                    return ae.getRelativeValue(0, 0);
                }
                return ae.getValueAt(ae.getFirstRow(), srcColNum);
            }
            if (ae.isColumn()) {
                return ae.getValueAt(srcRowNum, ae.getFirstColumn());
            }
            return ErrorEval.VALUE_INVALID;
        }
        return evaluationResult;
    }

    private static Eval invokeOperation(OperationEval operation, Eval[] ops, int srcRowNum, short srcColNum,
            HSSFWorkbook workbook, HSSFSheet sheet) {

        if(operation instanceof FunctionEval) {
            FunctionEval fe = (FunctionEval) operation;
            if(fe.isFreeRefFunction()) {
                return fe.getFreeRefFunction().evaluate(ops, srcRowNum, srcColNum, workbook, sheet);
            }
        }
        return operation.evaluate(ops, srcRowNum, srcColNum);
    }

    private HSSFSheet getOtherSheet(int externSheetIndex) {
        Workbook wb = _workbook.getWorkbook();
        return _workbook.getSheetAt(wb.getSheetIndexFromExternSheetIndex(externSheetIndex));
    }
    private HSSFFormulaEvaluator createEvaluatorForAnotherSheet(HSSFSheet sheet) {
        return new HSSFFormulaEvaluator(sheet, _workbook, _cache, _evaluationCounter);
    }

    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
     * passed here!
     */
    private Eval getEvalForPtg(Ptg ptg, HSSFSheet sheet) {
        if (ptg instanceof NamePtg) {
            // named ranges, macro functions
            NamePtg namePtg = (NamePtg) ptg;
            int numberOfNames = _workbook.getNumberOfNames();
            int nameIndex = namePtg.getIndex();
            if(nameIndex < 0 || nameIndex >= numberOfNames) {
                throw new RuntimeException("Bad name index (" + nameIndex
                        + "). Allowed range is (0.." + (numberOfNames-1) + ")");
            }
            NameRecord nameRecord = _workbook.getWorkbook().getNameRecord(nameIndex);
            if (nameRecord.isFunctionName()) {
                return new NameEval(nameRecord.getNameText());
            }
            if (nameRecord.hasFormula()) {
                return evaluateNameFormula(nameRecord.getNameDefinition(), sheet);
            }

            throw new RuntimeException("Don't now how to evalate name '" + nameRecord.getNameText() + "'");
        }
        if (ptg instanceof NameXPtg) {
            NameXPtg nameXPtg = (NameXPtg) ptg;
            return new NameXEval(nameXPtg.getSheetRefIndex(), nameXPtg.getNameIndex());
        }
        if (ptg instanceof RefPtg) {
            return new LazyRefEval(((RefPtg) ptg), sheet, this);
        }
        if (ptg instanceof Ref3DPtg) {
            Ref3DPtg refPtg = (Ref3DPtg) ptg;
            HSSFSheet xsheet = getOtherSheet(refPtg.getExternSheetIndex());
            return new LazyRefEval(refPtg, xsheet, createEvaluatorForAnotherSheet(xsheet));
        }
        if (ptg instanceof AreaPtg) {
            return new LazyAreaEval(((AreaPtg) ptg), sheet, this);
        }
        if (ptg instanceof Area3DPtg) {
            Area3DPtg a3dp = (Area3DPtg) ptg;
            HSSFSheet xsheet = getOtherSheet(a3dp.getExternSheetIndex());
            return new LazyAreaEval(a3dp, xsheet, createEvaluatorForAnotherSheet(xsheet));
        }

        if (ptg instanceof IntPtg) {
            return new NumberEval(((IntPtg)ptg).getValue());
        }
        if (ptg instanceof NumberPtg) {
            return new NumberEval(((NumberPtg)ptg).getValue());
        }
        if (ptg instanceof StringPtg) {
            return new StringEval(((StringPtg) ptg).getValue());
        }
        if (ptg instanceof BoolPtg) {
            return BoolEval.valueOf(((BoolPtg) ptg).getValue());
        }
        if (ptg instanceof ErrPtg) {
            return ErrorEval.valueOf(((ErrPtg) ptg).getErrorCode());
        }
        if (ptg instanceof UnknownPtg) {
            // TODO - remove UnknownPtg
            throw new RuntimeException("UnknownPtg not allowed");
        }
        throw new RuntimeException("Unexpected ptg class (" + ptg.getClass().getName() + ")");
    }
    private Eval evaluateNameFormula(Ptg[] ptgs, HSSFSheet sheet) {
        if (ptgs.length > 1) {
            throw new RuntimeException("Complex name formulas not supported yet");
        }
        return getEvalForPtg(ptgs[0], sheet);
    }

    /**
     * Given a cell, find its type and from that create an appropriate ValueEval
     * impl instance and return that. Since the cell could be an external
     * reference, we need the sheet that this belongs to.
     * Non existent cells are treated as empty.
     * @param cell
     * @param sheet
     * @param workbook
     */
    public ValueEval getEvalForCell(HSSFCell cell, HSSFSheet sheet) {

        if (cell == null) {
            return BlankEval.INSTANCE;
        }
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return new NumberEval(cell.getNumericCellValue());
            case HSSFCell.CELL_TYPE_STRING:
                return new StringEval(cell.getRichStringCellValue().getString());
            case HSSFCell.CELL_TYPE_FORMULA:
                return internalEvaluate(cell, sheet);
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return BoolEval.valueOf(cell.getBooleanCellValue());
            case HSSFCell.CELL_TYPE_BLANK:
                return BlankEval.INSTANCE;
            case HSSFCell.CELL_TYPE_ERROR:
                return ErrorEval.valueOf(cell.getErrorCellValue());
        }
        throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ")");
    }

    /**
     * Mimics the 'data view' of a cell. This allows formula evaluator
     * to return a CellValue instead of precasting the value to String
     * or Number or boolean type.
     * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
     */
    public static final class CellValue {
        private int cellType;
        private HSSFRichTextString richTextStringValue;
        private double numberValue;
        private boolean booleanValue;
        private byte errorValue;

        /**
         * CellType should be one of the types defined in HSSFCell
         * @param cellType
         */
        public CellValue(int cellType) {
            super();
            this.cellType = cellType;
        }
        /**
         * @return Returns the booleanValue.
         */
        public boolean getBooleanValue() {
            return booleanValue;
        }
        /**
         * @param booleanValue The booleanValue to set.
         */
        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
        /**
         * @return Returns the numberValue.
         */
        public double getNumberValue() {
            return numberValue;
        }
        /**
         * @param numberValue The numberValue to set.
         */
        public void setNumberValue(double numberValue) {
            this.numberValue = numberValue;
        }
        /**
         * @return Returns the stringValue. This method is deprecated, use
         * getRichTextStringValue instead
         * @deprecated
         */
        public String getStringValue() {
            return richTextStringValue.getString();
        }
        /**
         * @param stringValue The stringValue to set. This method is deprecated, use
         * getRichTextStringValue instead.
         * @deprecated
         */
        public void setStringValue(String stringValue) {
            this.richTextStringValue = new HSSFRichTextString(stringValue);
        }
        /**
         * @return Returns the cellType.
         */
        public int getCellType() {
            return cellType;
        }
        /**
         * @return Returns the errorValue.
         */
        public byte getErrorValue() {
            return errorValue;
        }
        /**
         * @param errorValue The errorValue to set.
         */
        public void setErrorValue(byte errorValue) {
            this.errorValue = errorValue;
        }
        /**
         * @return Returns the richTextStringValue.
         */
        public HSSFRichTextString getRichTextStringValue() {
            return richTextStringValue;
        }
        /**
         * @param richTextStringValue The richTextStringValue to set.
         */
        public void setRichTextStringValue(HSSFRichTextString richTextStringValue) {
            this.richTextStringValue = richTextStringValue;
        }
    }

    /**
     * debug method
     */
    void inspectPtgs(String formula) {
        Ptg[] ptgs = FormulaParser.parse(formula, _workbook);
        System.out.println("<ptg-group>");
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            System.out.println("<ptg>");
            System.out.println(ptgs[i]);
            if (ptgs[i] instanceof OperationPtg) {
                System.out.println("numoperands: " + ((OperationPtg) ptgs[i]).getNumberOfOperands());
            }
            System.out.println("</ptg>");
        }
        System.out.println("</ptg-group>");
    }
}
