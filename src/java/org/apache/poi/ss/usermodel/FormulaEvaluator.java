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

package org.apache.poi.ss.usermodel;

import java.util.Iterator;
import java.util.Stack;

import org.apache.poi.hssf.model.FormulaParser;
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
public class FormulaEvaluator {

    /**
     * used to track the number of evaluations
     */
    private static final class Counter {
        public int value;
        public int depth;
        public Counter() {
            value = 0;
        }
    }

    protected final Workbook _workbook;
    private final EvaluationCache _cache;

    private Counter _evaluationCounter;

    /**
     * @deprecated (Sep 2008) Sheet parameter is ignored
     */
    public FormulaEvaluator(Sheet sheet, Workbook workbook) {
        this(workbook);
        if (false) {
            sheet.toString(); // suppress unused parameter compiler warning
        }
    }
    public FormulaEvaluator(Workbook workbook) {
        this(workbook, new EvaluationCache(), new Counter());
    }

    private FormulaEvaluator(Workbook workbook, EvaluationCache cache, Counter evaluationCounter) {
        _workbook = workbook;
        _cache = cache;
        _evaluationCounter = evaluationCounter;
    }

    /**
     * for debug use. Used in toString methods
     */
    public String getSheetName(Sheet sheet) {
        return _workbook.getSheetName(_workbook.getSheetIndex(sheet));
    }
    /**
     * for debug/test use
     */
    public int getEvaluationCount() {
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
    public void setCurrentRow(Row row) {
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
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * @param cell
     */
    public CellValue evaluate(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return CellValue.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_ERROR:
                return CellValue.getError(cell.getErrorCellValue());
            case Cell.CELL_TYPE_FORMULA:
                return evaluateFormulaCellValue(cell);
            case Cell.CELL_TYPE_NUMERIC:
                return new CellValue(cell.getNumericCellValue(), _workbook.getCreationHelper());
            case Cell.CELL_TYPE_STRING:
                return new CellValue(cell.getRichStringCellValue().getString(), _workbook.getCreationHelper());
        }
        throw new IllegalStateException("Bad cell type (" + cell.getCellType() + ")");
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
     *  the result of the formula, use {@link #evaluateInCell(Cell)}
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as Cell.CELL_TYPE_FORMULA however)
     */
    public int evaluateFormulaCell(Cell cell) {
        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
            return -1;
        }
        CellValue cv = evaluateFormulaCellValue(cell);
        // cell remains a formula cell, but the cached value is changed
        setCellValue(cell, cv);
        return cv.getCellType();
    }

    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the same instance of Cell is returned to
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * Be aware that your cell value will be changed to hold the
     *  result of the formula. If you simply want the formula
     *  value computed for you, use {@link #evaluateFormulaCell(Cell)}
     * @param cell
     */
    public Cell evaluateInCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            CellValue cv = evaluateFormulaCellValue(cell);
            setCellType(cell, cv); // cell will no longer be a formula cell
            setCellValue(cell, cv);
        }
        return cell;
    }
    private static void setCellType(Cell cell, CellValue cv) {
        int cellType = cv.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BOOLEAN:
            case Cell.CELL_TYPE_ERROR:
            case Cell.CELL_TYPE_NUMERIC:
            case Cell.CELL_TYPE_STRING:
                cell.setCellType(cellType);
                return;
            case Cell.CELL_TYPE_BLANK:
                // never happens - blanks eventually get translated to zero
            case Cell.CELL_TYPE_FORMULA:
                // this will never happen, we have already evaluated the formula
        }
        throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
    }

    private static void setCellValue(Cell cell, CellValue cv) {
        int cellType = cv.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BOOLEAN:
                cell.setCellValue(cv.getBooleanValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                cell.setCellErrorValue(cv.getErrorValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellValue(cv.getNumberValue());
                break;
            case Cell.CELL_TYPE_STRING:
                cell.setCellValue(cv.getRichTextStringValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                // never happens - blanks eventually get translated to zero
            case Cell.CELL_TYPE_FORMULA:
                // this will never happen, we have already evaluated the formula
            default:
                throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
        }
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
    public static void evaluateAllFormulaCells(Workbook wb) {
        FormulaEvaluator evaluator = new FormulaEvaluator(wb);
        for(int i=0; i<wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);

            for (Iterator rit = sheet.rowIterator(); rit.hasNext();) {
                Row r = (Row)rit.next();

                for (Iterator cit = r.cellIterator(); cit.hasNext();) {
                    Cell c = (Cell)cit.next();
                    if (c.getCellType() == Cell.CELL_TYPE_FORMULA)
                        evaluator.evaluateFormulaCell(c);
                }
            }
        }
    }

    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param eval
     */
    private CellValue evaluateFormulaCellValue(Cell cell) {
        ValueEval eval = internalEvaluate(cell);
        if (eval instanceof NumberEval) {
            NumberEval ne = (NumberEval) eval;
            return new CellValue(ne.getNumberValue(), _workbook.getCreationHelper());
        }
        if (eval instanceof BoolEval) {
            BoolEval be = (BoolEval) eval;
            return CellValue.valueOf(be.getBooleanValue());
        }
        if (eval instanceof StringEval) {
            StringEval ne = (StringEval) eval;
            return new CellValue(ne.getStringValue(), _workbook.getCreationHelper());
        }
        if (eval instanceof ErrorEval) {
            return CellValue.getError(((ErrorEval)eval).getErrorCode());
        }
        throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
    }

    /**
     * Dev. Note: Internal evaluate must be passed only a formula cell
     * else a runtime exception will be thrown somewhere inside the method.
     * (Hence this is a private method.)
     * @return never <code>null</code>, never {@link BlankEval}
     */
    private ValueEval internalEvaluate(Cell srcCell) {
        int srcRowNum = srcCell.getRowIndex();
        int srcColNum = srcCell.getCellNum();

        ValueEval result;

        int sheetIndex = _workbook.getSheetIndex(srcCell.getSheet());
        result = _cache.getValue(sheetIndex, srcRowNum, srcColNum);
        if (result != null) {
            return result;
        }
        _evaluationCounter.value++;
        _evaluationCounter.depth++;

        EvaluationCycleDetector tracker = EvaluationCycleDetectorManager.getTracker();

        if(!tracker.startEvaluate(_workbook, sheetIndex, srcRowNum, srcColNum)) {
            return ErrorEval.CIRCULAR_REF_ERROR;
        }
        try {
            result = evaluateCell(sheetIndex, srcRowNum, (short)srcColNum, srcCell.getCellFormula());
        } finally {
            tracker.endEvaluate(_workbook, sheetIndex, srcRowNum, srcColNum);
            _cache.setValue(sheetIndex, srcRowNum, srcColNum, result);
            _evaluationCounter.depth--;
        }
        if (isDebugLogEnabled()) {
            String sheetName = _workbook.getSheetName(sheetIndex);
            CellReference cr = new CellReference(srcRowNum, srcColNum);
            logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + result.toString());
        }
        return result;
    }
    private ValueEval evaluateCell(int sheetIndex, int srcRowNum, short srcColNum, String cellFormulaText) {

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
//                logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                opResult = invokeOperation(operation, ops, _workbook, sheetIndex, srcRowNum, srcColNum);
            } else {
                opResult = getEvalForPtg(ptg, sheetIndex);
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
//            logDebug("push " + opResult);
            stack.push(opResult);
        }

        ValueEval value = ((ValueEval) stack.pop());
        if (!stack.isEmpty()) {
            throw new IllegalStateException("evaluation stack not empty");
        }
        value = dereferenceValue(value, srcRowNum, srcColNum);
        if (value == BlankEval.INSTANCE) {
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

    private static Eval invokeOperation(OperationEval operation, Eval[] ops, 
            Workbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {

        if(operation instanceof FunctionEval) {
            FunctionEval fe = (FunctionEval) operation;
            if(fe.isFreeRefFunction()) {
                return fe.getFreeRefFunction().evaluate(ops, workbook, sheetIndex, srcRowNum, srcColNum);
            }
        }
        return operation.evaluate(ops, srcRowNum, (short)srcColNum);
    }

    private Sheet getOtherSheet(int externSheetIndex) {
        return _workbook.getSheetAt(_workbook.getSheetIndexFromExternSheetIndex(externSheetIndex));
    }

    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
     * passed here!
     */
    private Eval getEvalForPtg(Ptg ptg, int sheetIndex) {
        if (ptg instanceof NamePtg) {
            // named ranges, macro functions
            NamePtg namePtg = (NamePtg) ptg;
            int numberOfNames = _workbook.getNumberOfNames();
            int nameIndex = namePtg.getIndex();
            if(nameIndex < 0 || nameIndex >= numberOfNames) {
                throw new RuntimeException("Bad name index (" + nameIndex
                        + "). Allowed range is (0.." + (numberOfNames-1) + ")");
            }
			if(_workbook instanceof org.apache.poi.hssf.usermodel.HSSFWorkbook) {
				NameRecord nameRecord = ((org.apache.poi.hssf.usermodel.HSSFWorkbook)_workbook).getNameRecord(nameIndex);
				if (nameRecord.isFunctionName()) {
					return new NameEval(nameRecord.getNameText());
				}
				if (nameRecord.hasFormula()) {
					return evaluateNameFormula(nameRecord.getNameDefinition(), sheetIndex);
				}

				throw new RuntimeException("Don't now how to evalate name '" + nameRecord.getNameText() + "'");
			}
			throw new RuntimeException("Don't now how to evalate name for XSSFWorkbook");
        }
        if (ptg instanceof NameXPtg) {
            NameXPtg nameXPtg = (NameXPtg) ptg;
            return new NameXEval(nameXPtg.getSheetRefIndex(), nameXPtg.getNameIndex());
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
        Sheet sheet = _workbook.getSheetAt(sheetIndex);
        if (ptg instanceof RefPtg) {
            return new LazyRefEval(((RefPtg) ptg), sheet, this);
        }
        if (ptg instanceof AreaPtg) {
            return new LazyAreaEval(((AreaPtg) ptg), sheet, this);
        }
        if (ptg instanceof Ref3DPtg) {
            Ref3DPtg refPtg = (Ref3DPtg) ptg;
            Sheet xsheet = getOtherSheet(refPtg.getExternSheetIndex());
            return new LazyRefEval(refPtg, xsheet, this);
        }
        if (ptg instanceof Area3DPtg) {
            Area3DPtg a3dp = (Area3DPtg) ptg;
            Sheet xsheet = getOtherSheet(a3dp.getExternSheetIndex());
            return new LazyAreaEval(a3dp, xsheet, this);
        }

        if (ptg instanceof UnknownPtg) {
            // POI uses UnknownPtg when the encoded Ptg array seems to be corrupted.
            // This seems to occur in very rare cases (e.g. unused name formulas in bug 44774, attachment 21790)
            // In any case, formulas are re-parsed before execution, so UnknownPtg should not get here 
            throw new RuntimeException("UnknownPtg not allowed");
        }
        
        throw new RuntimeException("Unexpected ptg class (" + ptg.getClass().getName() + ")");
    }
    private Eval evaluateNameFormula(Ptg[] ptgs, int sheetIndex) {
        if (ptgs.length > 1) {
            throw new RuntimeException("Complex name formulas not supported yet");
        }
        return getEvalForPtg(ptgs[0], sheetIndex);
    }

    /**
     * Given a cell, find its type and from that create an appropriate ValueEval
     * impl instance and return that. Since the cell could be an external
     * reference, we need the sheet that this belongs to.
     * Non existent cells are treated as empty.
     */
    public ValueEval getEvalForCell(Cell cell) {

        if (cell == null) {
            return BlankEval.INSTANCE;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return new NumberEval(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return new StringEval(cell.getRichStringCellValue().getString());
            case Cell.CELL_TYPE_FORMULA:
                return internalEvaluate(cell);
            case Cell.CELL_TYPE_BOOLEAN:
                return BoolEval.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_BLANK:
                return BlankEval.INSTANCE;
            case Cell.CELL_TYPE_ERROR:
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
        public static final CellValue TRUE = new CellValue(Cell.CELL_TYPE_BOOLEAN, 0.0, true,  null, 0, null);
        public static final CellValue FALSE= new CellValue(Cell.CELL_TYPE_BOOLEAN, 0.0, false, null, 0, null);
        
        private final int _cellType;
        private final double _numberValue;
        private final boolean _booleanValue;
        private final String _textValue;
        private final int _errorCode;
		private CreationHelper _creationHelper;

        private CellValue(int cellType, double numberValue, boolean booleanValue, 
                String textValue, int errorCode, CreationHelper creationHelper) {
            _cellType = cellType;
            _numberValue = numberValue;
            _booleanValue = booleanValue;
            _textValue = textValue;
            _errorCode = errorCode;
			_creationHelper = creationHelper;
        }
        
        
        /* package*/ CellValue(double numberValue, CreationHelper creationHelper) {
            this(Cell.CELL_TYPE_NUMERIC, numberValue, false, null, 0, creationHelper);
        }
        /* package*/ static CellValue valueOf(boolean booleanValue) {
            return booleanValue ? TRUE : FALSE;
        }
        /* package*/ CellValue(String stringValue, CreationHelper creationHelper) {
            this(Cell.CELL_TYPE_STRING, 0.0, false, stringValue, 0, creationHelper);
        }
        /* package*/ static CellValue getError(int errorCode) {
            return new CellValue(Cell.CELL_TYPE_ERROR, 0.0, false, null, errorCode, null);
        }
        
        
        /**
         * @return Returns the booleanValue.
         */
        public boolean getBooleanValue() {
            return _booleanValue;
        }
        /**
         * @return Returns the numberValue.
         */
        public double getNumberValue() {
            return _numberValue;
        }
        /**
         * @return Returns the stringValue.
         */
        public String getStringValue() {
            return _textValue;
        }
        /**
         * @return Returns the cellType.
         */
        public int getCellType() {
            return _cellType;
        }
        /**
         * @return Returns the errorValue.
         */
        public byte getErrorValue() {
            return (byte) _errorCode;
        }
        /**
         * @return Returns the richTextStringValue.
         * @deprecated (Sep 2008) Text formatting is lost during formula evaluation.  Use {@link #getStringValue()}  
         */
        public RichTextString getRichTextStringValue() {
            return _creationHelper.createRichTextString(_textValue);
        }
        public String toString() {
            StringBuffer sb = new StringBuffer(64);
            sb.append(getClass().getName()).append(" [");
            sb.append(formatAsString());
            sb.append("]");
            return sb.toString();
        }

        public String formatAsString() {
            switch (_cellType) {
                case Cell.CELL_TYPE_NUMERIC:
                    return String.valueOf(_numberValue);
                case Cell.CELL_TYPE_STRING:
                    return '"' + _textValue + '"';
                case Cell.CELL_TYPE_BOOLEAN:
                    return _booleanValue ? "TRUE" : "FALSE";
                case Cell.CELL_TYPE_ERROR:
                    return ErrorEval.getText(_errorCode);
            }
            return "<error unexpected cell type " + _cellType + ">";
        }
    }
}
