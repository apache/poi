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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException;
import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.function.FunctionMetadataRegistry;
import org.apache.poi.ss.formula.functions.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
/**
 * Evaluates formula cells.<p/>
 *
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearAllCachedResultValues()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 * @author Thies Wellpott (debug output enhancements)
 */
@Internal
public final class WorkbookEvaluator {
    
    private static final POILogger LOG = POILogFactory.getLogger(WorkbookEvaluator.class);

    private final EvaluationWorkbook _workbook;
    private EvaluationCache _cache;
    /** part of cache entry key (useful when evaluating multiple workbooks) */
    private int _workbookIx;

    private final IEvaluationListener _evaluationListener;
    private final Map<EvaluationSheet, Integer> _sheetIndexesBySheet;
    private final Map<String, Integer> _sheetIndexesByName;
    private CollaboratingWorkbooksEnvironment _collaboratingWorkbookEnvironment;
    private final IStabilityClassifier _stabilityClassifier;
    private final AggregatingUDFFinder _udfFinder;

    private boolean _ignoreMissingWorkbooks;

    /**
     * whether print detailed messages about the next formula evaluation
     */
    private boolean dbgEvaluationOutputForNextEval;

    // special logger for formula evaluation output (because of possibly very large output)
    private final POILogger EVAL_LOG = POILogFactory.getLogger("POI.FormulaEval");
    // current indent level for evalution; negative value for no output
    private int dbgEvaluationOutputIndent = -1;

    /**
     * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
     */
    public WorkbookEvaluator(EvaluationWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        this (workbook, null, stabilityClassifier, udfFinder);
    }
    /* package */ WorkbookEvaluator(EvaluationWorkbook workbook, IEvaluationListener evaluationListener,
            IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        _workbook = workbook;
        _evaluationListener = evaluationListener;
        _cache = new EvaluationCache(evaluationListener);
        _sheetIndexesBySheet = new IdentityHashMap<>();
        _sheetIndexesByName = new IdentityHashMap<>();
        _collaboratingWorkbookEnvironment = CollaboratingWorkbooksEnvironment.EMPTY;
        _workbookIx = 0;
        _stabilityClassifier = stabilityClassifier;

        AggregatingUDFFinder defaultToolkit = // workbook can be null in unit tests
                workbook == null ? null : (AggregatingUDFFinder)workbook.getUDFFinder();
        if(defaultToolkit != null && udfFinder != null) {
            defaultToolkit.add(udfFinder);
        }
        _udfFinder = defaultToolkit;
    }

    /**
     * also for debug use. Used in toString methods
     */
    /* package */ String getSheetName(int sheetIndex) {
        return _workbook.getSheetName(sheetIndex);
    }

    /* package */ EvaluationSheet getSheet(int sheetIndex) {
        return _workbook.getSheet(sheetIndex);
    }
    
    /* package */ EvaluationWorkbook getWorkbook() {
        return _workbook;
    }

    /* package */ EvaluationName getName(String name, int sheetIndex) {
        return _workbook.getName(name, sheetIndex);
    }

    private static boolean isDebugLogEnabled() {
        return LOG.check(POILogger.DEBUG);
    }
    private static boolean isInfoLogEnabled() {
        return LOG.check(POILogger.INFO);
    }
    private static void logDebug(String s) {
        if (isDebugLogEnabled()) {
            LOG.log(POILogger.DEBUG, s);
        }
    }
    private static void logInfo(String s) {
        if (isInfoLogEnabled()) {
            LOG.log(POILogger.INFO, s);
        }
    }
    /* package */ void attachToEnvironment(CollaboratingWorkbooksEnvironment collaboratingWorkbooksEnvironment, EvaluationCache cache, int workbookIx) {
        _collaboratingWorkbookEnvironment = collaboratingWorkbooksEnvironment;
        _cache = cache;
        _workbookIx = workbookIx;
    }
    /* package */ CollaboratingWorkbooksEnvironment getEnvironment() {
        return _collaboratingWorkbookEnvironment;
    }

    /**
     * Discards the current workbook environment and attaches to the default 'empty' environment.
     * Also resets evaluation cache.
     */
    /* package */ void detachFromEnvironment() {
        _collaboratingWorkbookEnvironment = CollaboratingWorkbooksEnvironment.EMPTY;
        _cache = new EvaluationCache(_evaluationListener);
        _workbookIx = 0;
    }
    /**
     * @return the evaluator for another workbook which is part of the same {@link CollaboratingWorkbooksEnvironment}
     */
    /* package */ WorkbookEvaluator getOtherWorkbookEvaluator(String workbookName) throws WorkbookNotFoundException {
        return _collaboratingWorkbookEnvironment.getWorkbookEvaluator(workbookName);
    }

    /* package */ IEvaluationListener getEvaluationListener() {
        return _evaluationListener;
    }

    /**
     * Should be called whenever there are changes to input cells in the evaluated workbook.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    public void clearAllCachedResultValues() {
        _cache.clear();
        _sheetIndexesBySheet.clear();
        _workbook.clearAllCachedResultValues();
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     */
    public void notifyUpdateCell(EvaluationCell cell) {
        int sheetIndex = getSheetIndex(cell.getSheet());
        _cache.notifyUpdateCell(_workbookIx, sheetIndex, cell);
    }
    /**
     * Should be called to tell the cell value cache that the specified cell has just been
     * deleted.
     */
    public void notifyDeleteCell(EvaluationCell cell) {
        int sheetIndex = getSheetIndex(cell.getSheet());
        _cache.notifyDeleteCell(_workbookIx, sheetIndex, cell);
    }
    
    private int getSheetIndex(EvaluationSheet sheet) {
        Integer result = _sheetIndexesBySheet.get(sheet);
        if (result == null) {
            int sheetIndex = _workbook.getSheetIndex(sheet);
            if (sheetIndex < 0) {
                throw new RuntimeException("Specified sheet from a different book");
            }
            result = Integer.valueOf(sheetIndex);
            _sheetIndexesBySheet.put(sheet, result);
        }
        return result.intValue();
    }

    public ValueEval evaluate(EvaluationCell srcCell) {
        int sheetIndex = getSheetIndex(srcCell.getSheet());
        return evaluateAny(srcCell, sheetIndex, srcCell.getRowIndex(), srcCell.getColumnIndex(), new EvaluationTracker(_cache));
    }

    /**
     * Case-insensitive.
     * @return -1 if sheet with specified name does not exist
     */
    /* package */ int getSheetIndex(String sheetName) {
        Integer result = _sheetIndexesByName.get(sheetName);
        if (result == null) {
            int sheetIndex = _workbook.getSheetIndex(sheetName);
            if (sheetIndex < 0) {
                return -1;
            }
            result = Integer.valueOf(sheetIndex);
            _sheetIndexesByName.put(sheetName, result);
        }
        return result.intValue();
    }
    
    /* package */ int getSheetIndexByExternIndex(int externSheetIndex) {
       return _workbook.convertFromExternSheetIndex(externSheetIndex);
    }


    /**
     * @return never <code>null</code>, never {@link BlankEval}
     */
    private ValueEval evaluateAny(EvaluationCell srcCell, int sheetIndex,
                int rowIndex, int columnIndex, EvaluationTracker tracker) {

        // avoid tracking dependencies to cells that have constant definition
        boolean shouldCellDependencyBeRecorded = _stabilityClassifier == null ? true
                    : !_stabilityClassifier.isCellFinal(sheetIndex, rowIndex, columnIndex);
        if (srcCell == null || srcCell.getCellType() != CellType.FORMULA) {
            ValueEval result = getValueFromNonFormulaCell(srcCell);
            if (shouldCellDependencyBeRecorded) {
                tracker.acceptPlainValueDependency(_workbook, _workbookIx, sheetIndex, rowIndex, columnIndex, result);
            }
            return result;
        }

        FormulaCellCacheEntry cce = _cache.getOrCreateFormulaCellEntry(srcCell);
        if (shouldCellDependencyBeRecorded || cce.isInputSensitive()) {
            tracker.acceptFormulaDependency(cce);
        }
        IEvaluationListener evalListener = _evaluationListener;
        ValueEval result;
        if (cce.getValue() == null) {
            if (!tracker.startEvaluate(cce)) {
                return ErrorEval.CIRCULAR_REF_ERROR;
            }

            try {

                Ptg[] ptgs = _workbook.getFormulaTokens(srcCell);
                OperationEvaluationContext ec = new OperationEvaluationContext
                        (this, _workbook, sheetIndex, rowIndex, columnIndex, tracker);
                if (evalListener == null) {
                    result = evaluateFormula(ec, ptgs);
                } else {
                    evalListener.onStartEvaluate(srcCell, cce);
                    result = evaluateFormula(ec, ptgs);
                    evalListener.onEndEvaluate(cce, result);
                }

                tracker.updateCacheResult(result);
            }
             catch (NotImplementedException e) {
                throw addExceptionInfo(e, sheetIndex, rowIndex, columnIndex);
             } catch (RuntimeException re) {
                 if (re.getCause() instanceof WorkbookNotFoundException && _ignoreMissingWorkbooks) {
                     logInfo(re.getCause().getMessage() + " - Continuing with cached value!");
                     switch(srcCell.getCachedFormulaResultType()) {
                         case NUMERIC:
                             result = new NumberEval(srcCell.getNumericCellValue());
                             break;
                         case STRING:
                             result =  new StringEval(srcCell.getStringCellValue());
                             break;
                         case BLANK:
                             result = BlankEval.instance;
                             break;
                         case BOOLEAN:
                             result =  BoolEval.valueOf(srcCell.getBooleanCellValue());
                             break;
                         case ERROR:
                            result =  ErrorEval.valueOf(srcCell.getErrorCellValue());
                            break;
                         case FORMULA:
                        default:
                            throw new RuntimeException("Unexpected cell type '" + srcCell.getCellType()+"' found!");
                     }
                 } else {
                     throw re;
                 }
             } finally {
                tracker.endEvaluate(cce);
            }
        } else {
            if(evalListener != null) {
                evalListener.onCacheHit(sheetIndex, rowIndex, columnIndex, cce.getValue());
            }
            return cce.getValue();
        }
        if (isDebugLogEnabled()) {
            String sheetName = getSheetName(sheetIndex);
            CellReference cr = new CellReference(rowIndex, columnIndex);
            logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + result);
        }
        // Usually (result === cce.getValue())
        // But sometimes: (result==ErrorEval.CIRCULAR_REF_ERROR, cce.getValue()==null)
        // When circular references are detected, the cache entry is only updated for
        // the top evaluation frame
        return result;
    }

    /**
     * Adds the current cell reference to the exception for easier debugging.
     * Would be nice to get the formula text as well, but that seems to require
     * too much digging around and casting to get the FormulaRenderingWorkbook.
     */
    private NotImplementedException addExceptionInfo(NotImplementedException inner, int sheetIndex, int rowIndex, int columnIndex) {

        try {
            String sheetName = _workbook.getSheetName(sheetIndex);
            CellReference cr = new CellReference(sheetName, rowIndex, columnIndex, false, false);
            String msg =  "Error evaluating cell " + cr.formatAsString();
            return new NotImplementedException(msg, inner);
        } catch (Exception e) {
            // avoid bombing out during exception handling
            LOG.log(POILogger.ERROR, "Can't add exception info", e);
            return inner; // preserve original exception
        }
    }
    /**
     * Gets the value from a non-formula cell.
     * @param cell may be <code>null</code>
     * @return {@link BlankEval} if cell is <code>null</code> or blank, never <code>null</code>
     */
    /* package */ static ValueEval getValueFromNonFormulaCell(EvaluationCell cell) {
        if (cell == null) {
            return BlankEval.instance;
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case NUMERIC:
                return new NumberEval(cell.getNumericCellValue());
            case STRING:
                return new StringEval(cell.getStringCellValue());
            case BOOLEAN:
                return BoolEval.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return BlankEval.instance;
            case ERROR:
                return ErrorEval.valueOf(cell.getErrorCellValue());
            default:
                throw new RuntimeException("Unexpected cell type (" + cellType + ")");
        }
        
    }


    // visibility raised for testing
    @Internal
    /* package */ ValueEval evaluateFormula(OperationEvaluationContext ec, Ptg[] ptgs) {

        String dbgIndentStr = "";        // always init. to non-null just for defensive avoiding NPE
        if (dbgEvaluationOutputForNextEval) {
            // first evaluation call when ouput is desired, so iit. this evaluator instance
            dbgEvaluationOutputIndent = 1;
            dbgEvaluationOutputForNextEval = false;
        }
        if (dbgEvaluationOutputIndent > 0) {
            // init. indent string to needed spaces (create as substring vom very long space-only string;
            // limit indendation for deep recursions)
            dbgIndentStr = "                                                                                                    ";
            dbgIndentStr = dbgIndentStr.substring(0, Math.min(dbgIndentStr.length(), dbgEvaluationOutputIndent*2));
            EVAL_LOG.log(POILogger.WARN, dbgIndentStr
                               + "- evaluateFormula('" + ec.getRefEvaluatorForCurrentSheet().getSheetNameRange()
                               + "'/" + new CellReference(ec.getRowIndex(), ec.getColumnIndex()).formatAsString()
                               + "): " + Arrays.toString(ptgs).replaceAll("\\Qorg.apache.poi.ss.formula.ptg.\\E", ""));
            dbgEvaluationOutputIndent++;
        }

        EvaluationSheet evalSheet = ec.getWorkbook().getSheet(ec.getSheetIndex());
        EvaluationCell evalCell = evalSheet.getCell(ec.getRowIndex(), ec.getColumnIndex());

        Stack<ValueEval> stack = new Stack<>();
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            // since we don't know how to handle these yet :(
            Ptg ptg = ptgs[i];
            if (dbgEvaluationOutputIndent > 0) {
                EVAL_LOG.log(POILogger.INFO, dbgIndentStr + "  * ptg " + i + ": " + ptg + ", stack: " + stack);
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = (AttrPtg) ptg;
                if (attrPtg.isSum()) {
                    // Excel prefers to encode 'SUM()' as a tAttr token, but this evaluator
                    // expects the equivalent function token
                    ptg = FuncVarPtg.SUM;
                }
                if (attrPtg.isOptimizedChoose()) {
                    ValueEval arg0 = stack.pop();
                    int[] jumpTable = attrPtg.getJumpTable();
                    int dist;
                    int nChoices = jumpTable.length;
                    try {
                        int switchIndex = Choose.evaluateFirstArg(arg0, ec.getRowIndex(), ec.getColumnIndex());
                        if (switchIndex<1 || switchIndex > nChoices) {
                            stack.push(ErrorEval.VALUE_INVALID);
                            dist = attrPtg.getChooseFuncOffset() + 4; // +4 for tFuncFar(CHOOSE)
                        } else {
                            dist = jumpTable[switchIndex-1];
                        }
                    } catch (EvaluationException e) {
                        stack.push(e.getErrorEval());
                        dist = attrPtg.getChooseFuncOffset() + 4; // +4 for tFuncFar(CHOOSE)
                    }
                    // Encoded dist for tAttrChoose includes size of jump table, but
                    // countTokensToBeSkipped() does not (it counts whole tokens).
                    dist -= nChoices*2+2; // subtract jump table size
                    i+= countTokensToBeSkipped(ptgs, i, dist);
                    continue;
                }
                if (attrPtg.isOptimizedIf()) {
                    if(!evalCell.isPartOfArrayFormulaGroup()) {
                        ValueEval arg0 = stack.pop();
                        boolean evaluatedPredicate;

                        try {
                            evaluatedPredicate = IfFunc.evaluateFirstArg(arg0, ec.getRowIndex(), ec.getColumnIndex());
                        } catch (EvaluationException e) {
                            stack.push(e.getErrorEval());
                            int dist = attrPtg.getData();
                            i += countTokensToBeSkipped(ptgs, i, dist);
                            attrPtg = (AttrPtg) ptgs[i];
                            dist = attrPtg.getData() + 1;
                            i += countTokensToBeSkipped(ptgs, i, dist);
                            continue;
                        }
                        if (evaluatedPredicate) {
                            // nothing to skip - true param follows
                        } else {
                            int dist = attrPtg.getData();
                            i += countTokensToBeSkipped(ptgs, i, dist);
                            Ptg nextPtg = ptgs[i + 1];
                            if (ptgs[i] instanceof AttrPtg && nextPtg instanceof FuncVarPtg &&
                                    // in order to verify that there is no third param, we need to check
                                    // if we really have the IF next or some other FuncVarPtg as third param, e.g. ROW()/COLUMN()!
                                    ((FuncVarPtg) nextPtg).getFunctionIndex() == FunctionMetadataRegistry.FUNCTION_INDEX_IF) {
                                // this is an if statement without a false param (as opposed to MissingArgPtg as the false param)
                                //i++;
                                stack.push(arg0);
                                stack.push(BoolEval.FALSE);
                            }
                        }
                    }
                    continue;
                }
                if (attrPtg.isSkip() && !evalCell.isPartOfArrayFormulaGroup()) {
                    int dist = attrPtg.getData()+1;
                    i+= countTokensToBeSkipped(ptgs, i, dist);
                    if (stack.peek() == MissingArgEval.instance) {
                        stack.pop();
                        stack.push(BlankEval.instance);
                    }
                    continue;
                }
            }
            if (ptg instanceof ControlPtg) {
                // skip Parentheses, Attr, etc
                continue;
            }
            if (ptg instanceof MemFuncPtg || ptg instanceof MemAreaPtg) {
                // can ignore, rest of tokens for this expression are in OK RPN order
                continue;
            }
            if (ptg instanceof MemErrPtg) {
                continue;
            }

            if (ptg instanceof UnionPtg) {
                ValueEval v2 = stack.pop();
                ValueEval v1 = stack.pop();
                stack.push(new RefListEval(v1, v2));
                continue;
            }

            ValueEval opResult;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;

                int numops = optg.getNumberOfOperands();
                ValueEval[] ops = new ValueEval[numops];

                // storing the ops in reverse order since they are popping
                boolean areaArg = false; // whether one of the operands is an area
                for (int j = numops - 1; j >= 0; j--) {
                    ValueEval p = stack.pop();
                    ops[j] = p;
                    if(p instanceof AreaEval){
                        areaArg = true;
                    }
                }

                boolean arrayMode = false;
                if(areaArg) for (int ii = i; ii < iSize; ii++) {
                    if(ptgs[ii] instanceof FuncVarPtg){
                        FuncVarPtg f = (FuncVarPtg)ptgs[ii];
                        try {
                            Function func = FunctionEval.getBasicFunction(f.getFunctionIndex());
                            if (func != null && func instanceof ArrayMode) {
                                arrayMode = true;
                            }
                        } catch (NotImplementedException ne){
                            //FunctionEval.getBasicFunction can throw NotImplementedException
                            // if the fucntion is not yet supported.
                        }
                        break;
                    }
                }
                ec.setArrayMode(arrayMode);

//                logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                opResult = OperationEvaluatorFactory.evaluate(optg, ops, ec);

                ec.setArrayMode(false);

            } else {
                opResult = getEvalForPtg(ptg, ec);
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
//            logDebug("push " + opResult);
            stack.push(opResult);
            if (dbgEvaluationOutputIndent > 0) {
                EVAL_LOG.log(POILogger.INFO, dbgIndentStr + "    = " + opResult);
            }
        }

        ValueEval value = stack.pop();
        if (!stack.isEmpty()) {
            throw new IllegalStateException("evaluation stack not empty");
        }
        
        ValueEval result;
        
        if (ec.isSingleValue()) {
            result = dereferenceResult(value, ec);
        }
        else {
            result = value;
        }

        if (dbgEvaluationOutputIndent > 0) {
            EVAL_LOG.log(POILogger.INFO, dbgIndentStr + "finshed eval of "
                            + new CellReference(ec.getRowIndex(), ec.getColumnIndex()).formatAsString()
                            + ": " + result);
            dbgEvaluationOutputIndent--;
            if (dbgEvaluationOutputIndent == 1) {
                // this evaluation is done, reset indent to stop logging
                dbgEvaluationOutputIndent = -1;
            }
        } // if
        return result;

    }

    /**
     * Calculates the number of tokens that the evaluator should skip upon reaching a tAttrSkip.
     *
     * @return the number of tokens (starting from <tt>startIndex+1</tt>) that need to be skipped
     * to achieve the specified <tt>distInBytes</tt> skip distance.
     */
    private static int countTokensToBeSkipped(Ptg[] ptgs, int startIndex, int distInBytes) {
        int remBytes = distInBytes;
        int index = startIndex;
        while (remBytes != 0) {
            index++;
            remBytes -= ptgs[index].getSize();
            if (remBytes < 0) {
                throw new RuntimeException("Bad skip distance (wrong token size calculation).");
            }
            if (index >= ptgs.length) {
                throw new RuntimeException("Skip distance too far (ran out of formula tokens).");
            }
        }
        return index-startIndex;
    }
    
    /**
     * Dereferences a single value from any AreaEval or RefEval evaluation
     * result. If the supplied evaluationResult is just a plain value, it is
     * returned as-is.
     *
     * @return a {@link NumberEval}, {@link StringEval}, {@link BoolEval}, or
     *         {@link ErrorEval}. Never <code>null</code>. {@link BlankEval} is
     *         converted to {@link NumberEval#ZERO}
     */
    private static ValueEval dereferenceResult(ValueEval evaluationResult, OperationEvaluationContext ec) {
        ValueEval value;

        if (ec == null) {
            throw new IllegalArgumentException("OperationEvaluationContext ec is null");
        }
        if (ec.getWorkbook() == null) {
            throw new IllegalArgumentException("OperationEvaluationContext ec.getWorkbook() is null");
        }

        EvaluationSheet evalSheet = ec.getWorkbook().getSheet(ec.getSheetIndex());
        EvaluationCell evalCell = evalSheet.getCell(ec.getRowIndex(), ec.getColumnIndex());
 
        if (evalCell != null && evalCell.isPartOfArrayFormulaGroup() && evaluationResult instanceof AreaEval) {
            value = OperandResolver.getElementFromArray((AreaEval) evaluationResult, evalCell);
        }
        else {
            value = dereferenceResult(evaluationResult, ec.getRowIndex(), ec.getColumnIndex());
        }
        
        return value;
    }

    /**
     * Dereferences a single value from any AreaEval or RefEval evaluation
     * result. If the supplied evaluationResult is just a plain value, it is
     * returned as-is.
     *
     * @return a {@link NumberEval}, {@link StringEval}, {@link BoolEval}, or
     *         {@link ErrorEval}. Never <code>null</code>. {@link BlankEval} is
     *         converted to {@link NumberEval#ZERO}
     */
    public static ValueEval dereferenceResult(ValueEval evaluationResult, int srcRowNum, int srcColNum) {
        ValueEval value;
        try {
            value = OperandResolver.getSingleValue(evaluationResult, srcRowNum, srcColNum);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        if (value == BlankEval.instance) {
            // Note Excel behaviour here. A blank final final value is converted to zero.
            return NumberEval.ZERO;
            // Formulas _never_ evaluate to blank.  If a formula appears to have evaluated to
            // blank, the actual value is empty string. This can be verified with ISBLANK().
        }
        return value;
    }


    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
     * passed here!
     */
    private ValueEval getEvalForPtg(Ptg ptg, OperationEvaluationContext ec) {
        //  consider converting all these (ptg instanceof XxxPtg) expressions to (ptg.getClass() == XxxPtg.class)

        if (ptg instanceof NamePtg) {
            // Named ranges, macro functions
            NamePtg namePtg = (NamePtg) ptg;
            EvaluationName nameRecord = _workbook.getName(namePtg);
            return getEvalForNameRecord(nameRecord, ec);
        }
        if (ptg instanceof NameXPtg) {
            // Externally defined named ranges or macro functions
            return processNameEval(ec.getNameXEval((NameXPtg)ptg), ec);
        }
        if (ptg instanceof NameXPxg) {
            // Externally defined named ranges or macro functions
            return processNameEval(ec.getNameXEval((NameXPxg)ptg), ec);
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
        if (ptg instanceof MissingArgPtg) {
           return MissingArgEval.instance;
        }
        if (ptg instanceof AreaErrPtg ||ptg instanceof RefErrorPtg
               || ptg instanceof DeletedArea3DPtg || ptg instanceof DeletedRef3DPtg) {
           return ErrorEval.REF_INVALID;
        }
        if (ptg instanceof Ref3DPtg) {
           return ec.getRef3DEval((Ref3DPtg)ptg);
        }
        if (ptg instanceof Ref3DPxg) {
           return ec.getRef3DEval((Ref3DPxg)ptg);
        }
        if (ptg instanceof Area3DPtg) {
           return ec.getArea3DEval((Area3DPtg)ptg);
        }
        if (ptg instanceof Area3DPxg) {
           return ec.getArea3DEval((Area3DPxg)ptg);
        }
        if (ptg instanceof RefPtg) {
           RefPtg rptg = (RefPtg) ptg;
           return ec.getRefEval(rptg.getRow(), rptg.getColumn());
        }
        if (ptg instanceof AreaPtg) {
           AreaPtg aptg = (AreaPtg) ptg;
           return ec.getAreaEval(aptg.getFirstRow(), aptg.getFirstColumn(), aptg.getLastRow(), aptg.getLastColumn());
        }
        
        if (ptg instanceof ArrayPtg) {
           ArrayPtg aptg = (ArrayPtg) ptg;
           return ec.getAreaValueEval(0, 0, aptg.getRowCount() - 1, aptg.getColumnCount() - 1, aptg.getTokenArrayValues());
        }

        if (ptg instanceof UnknownPtg) {
            // POI uses UnknownPtg when the encoded Ptg array seems to be corrupted.
            // This seems to occur in very rare cases (e.g. unused name formulas in bug 44774, attachment 21790)
            // In any case, formulas are re-parsed before execution, so UnknownPtg should not get here
            throw new RuntimeException("UnknownPtg not allowed");
        }
        if (ptg instanceof ExpPtg) {
            // ExpPtg is used for array formulas and shared formulas.
            // it is currently unsupported, and may not even get implemented here
            throw new RuntimeException("ExpPtg currently not supported");
        }

        throw new RuntimeException("Unexpected ptg class (" + ptg.getClass().getName() + ")");
    }
   
    private ValueEval processNameEval(ValueEval eval, OperationEvaluationContext ec) {
        if (eval instanceof ExternalNameEval) {
            EvaluationName name = ((ExternalNameEval)eval).getName();
            return getEvalForNameRecord(name, ec);
        }
        return eval;
    }
    
    private ValueEval getEvalForNameRecord(EvaluationName nameRecord, OperationEvaluationContext ec) {
        if (nameRecord.isFunctionName()) {
            return new FunctionNameEval(nameRecord.getNameText());
        }
        if (nameRecord.hasFormula()) {
            return evaluateNameFormula(nameRecord.getNameDefinition(), ec);
        }

        throw new RuntimeException("Don't know how to evaluate name '" + nameRecord.getNameText() + "'");
    }
    
    /**
     * YK: Used by OperationEvaluationContext to resolve indirect names.
     */
    /*package*/ ValueEval evaluateNameFormula(Ptg[] ptgs, OperationEvaluationContext ec) {
      if (ptgs.length == 1 && !(ptgs[0] instanceof FuncVarPtg)) {
        return getEvalForPtg(ptgs[0], ec);
      }
        
      OperationEvaluationContext anyValueContext = new OperationEvaluationContext(this, ec.getWorkbook(), ec.getSheetIndex(), ec.getRowIndex(), ec.getColumnIndex(), new EvaluationTracker(_cache), false);
      return evaluateFormula(anyValueContext, ptgs);
    }

    /**
     * Used by the lazy ref evals whenever they need to get the value of a contained cell.
     */
    /* package */ ValueEval evaluateReference(
            EvaluationSheet sheet, int sheetIndex, int rowIndex,
            int columnIndex, EvaluationTracker tracker) {

        EvaluationCell cell = sheet.getCell(rowIndex, columnIndex);
        return evaluateAny(cell, sheetIndex, rowIndex, columnIndex, tracker);
    }
    public FreeRefFunction findUserDefinedFunction(String functionName) {
        return _udfFinder.findFunction(functionName);
    }

    /**
     * Evaluate a formula outside a cell value, e.g. conditional format rules or data validation expressions
     * 
     * @param formula to evaluate
     * @param ref defines the optional sheet and row/column base for the formula, if it is relative
     * @return value
     */
    public ValueEval evaluate(String formula, CellReference ref) {
        final String sheetName = ref == null ? null : ref.getSheetName();
        int sheetIndex;
        if (sheetName == null) {
            sheetIndex = -1; // workbook scope only
        } else {
            sheetIndex = getWorkbook().getSheetIndex(sheetName);
        }
        int rowIndex = ref == null ? -1 : ref.getRow();
        short colIndex = ref == null ? -1 : ref.getCol();
        final OperationEvaluationContext ec = new OperationEvaluationContext(
                this, 
                getWorkbook(), 
                sheetIndex, 
                rowIndex, 
                colIndex, 
                new EvaluationTracker(_cache)
            );
        Ptg[] ptgs = FormulaParser.parse(formula, (FormulaParsingWorkbook) getWorkbook(), FormulaType.CELL, sheetIndex, rowIndex);
        return evaluateNameFormula(ptgs, ec);
    }
    
    /**
     * Some expressions need to be evaluated in terms of an offset from the top left corner of a region,
     * such as some data validation and conditional format expressions, when those constraints apply
     * to contiguous cells.  When a relative formula is used, it must be evaluated by shifting by the target
     * offset position relative to the top left of the range.
     * <p>
     * Returns a single value e.g. a cell formula result or boolean value for conditional formatting.
     * 
     * @param formula The formula to evaluate
     * @param target cell context for the operation
     * @param region containing the cell
     * @return value
     * @throws IllegalArgumentException if target does not define a sheet name to evaluate the formula on.
     */
    public ValueEval evaluate(String formula, CellReference target, CellRangeAddressBase region) {
        return evaluate(formula, target, region, FormulaType.CELL);
    }
    
    /**
     * Some expressions need to be evaluated in terms of an offset from the top left corner of a region,
     * such as some data validation and conditional format expressions, when those constraints apply
     * to contiguous cells.  When a relative formula is used, it must be evaluated by shifting by the target
     * offset position relative to the top left of the range.
     * <p>
     * Returns a ValueEval that may be one or more values, such as the allowed values for a data validation constraint.
     *
     * @param formula The formula to evaluate
     * @param target cell context for the operation
     * @param region containing the cell
     * @return ValueEval for one or more values
     * @throws IllegalArgumentException if target does not define a sheet name to evaluate the formula on.
     */
    public ValueEval evaluateList(String formula, CellReference target, CellRangeAddressBase region) {
        return evaluate(formula, target, region, FormulaType.DATAVALIDATION_LIST);
    }
    
    private ValueEval evaluate(String formula, CellReference target, CellRangeAddressBase region, FormulaType formulaType) {
        final String sheetName = target == null ? null : target.getSheetName();
        if (sheetName == null) throw new IllegalArgumentException("Sheet name is required");
        
        final int sheetIndex = getWorkbook().getSheetIndex(sheetName);
        Ptg[] ptgs = FormulaParser.parse(formula, (FormulaParsingWorkbook) getWorkbook(), formulaType, sheetIndex, target.getRow());

        adjustRegionRelativeReference(ptgs, target, region);
        
        final OperationEvaluationContext ec = new OperationEvaluationContext(this, getWorkbook(), sheetIndex, target.getRow(), target.getCol(), new EvaluationTracker(_cache), formulaType.isSingleValue());
        return evaluateNameFormula(ptgs, ec);
    }
    
    /**
     * Adjust formula relative references by the offset between the start of the given region and the given target cell.
     * That is, treat the region top-left cell as "A1" for the purposes of evaluating relative reference components (row and/or column),
     * and further move references by the position of the target within the region.
     * <p><pre>formula ref + range top-left + current cell range offset </pre></p>
     * which simplifies to
     * <p><pre>formula ref + current cell ref</pre></p>
     * @param ptgs
     * @param target cell within the region to use.
     * @param region containing the cell, OR, for conditional format rules with multiple ranges, the region with the top-left-most cell
     * @return true if any Ptg references were shifted
     * @throws IndexOutOfBoundsException if the resulting shifted row/column indexes are over the document format limits
     * @throws IllegalArgumentException if target is not within region.
     */
    protected boolean adjustRegionRelativeReference(Ptg[] ptgs, CellReference target, CellRangeAddressBase region) {
        // region may not be the one that contains the target, if a conditional formatting rule applies to multiple regions
        
        int deltaRow = target.getRow() - region.getFirstRow();
        int deltaColumn = target.getCol() - region.getFirstColumn();
        
        boolean shifted = false;
        for (Ptg ptg : ptgs) {
            // base class for cell reference "things"
            if (ptg instanceof RefPtgBase) {
                RefPtgBase ref = (RefPtgBase) ptg;
                // re-calculate cell references
                final SpreadsheetVersion version = _workbook.getSpreadsheetVersion();
                if (ref.isRowRelative() && deltaRow > 0) {
                    final int rowIndex = ref.getRow() + deltaRow;
                    if (rowIndex > version.getMaxRows()) {
                        throw new IndexOutOfBoundsException(version.name() + " files can only have " + version.getMaxRows() + " rows, but row " + rowIndex + " was requested.");
                    }
                    ref.setRow(rowIndex);
                    shifted = true;
                }
                if (ref.isColRelative() && deltaColumn > 0) {
                    final int colIndex = ref.getColumn() + deltaColumn;
                    if (colIndex > version.getMaxColumns()) {
                        throw new IndexOutOfBoundsException(version.name() + " files can only have " + version.getMaxColumns() + " columns, but column " + colIndex + " was requested.");
                    }
                    ref.setColumn(colIndex);
                    shifted = true;
                }
            }
        }
        return shifted;
    }
    
    /**
     * Whether to ignore missing references to external workbooks and
     * use cached formula results in the main workbook instead.
     * <p>
     * In some cases exetrnal workbooks referenced by formulas in the main workbook are not avaiable.
     * With this method you can control how POI handles such missing references:
     * <ul>
     *     <li>by default ignoreMissingWorkbooks=false and POI throws {@link WorkbookNotFoundException}
     *     if an external reference cannot be resolved</li>
     *     <li>if ignoreMissingWorkbooks=true then POI uses cached formula result
     *     that already exists in the main workbook</li>
     * </ul>
     *
     * @param ignore whether to ignore missing references to external workbooks
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=52575">Bug 52575 for details</a>
     */
    public void setIgnoreMissingWorkbooks(boolean ignore){
        _ignoreMissingWorkbooks = ignore;
    }
    public boolean isIgnoreMissingWorkbooks(){
        return _ignoreMissingWorkbooks;
    }

    /**
     * Return a collection of functions that POI can evaluate
     *
     * @return names of functions supported by POI
     */
    public static Collection<String> getSupportedFunctionNames(){
        Collection<String> lst = new TreeSet<>();
        lst.addAll(FunctionEval.getSupportedFunctionNames());
        lst.addAll(AnalysisToolPak.getSupportedFunctionNames());
        return Collections.unmodifiableCollection(lst);
    }

    /**
     * Return a collection of functions that POI does not support
     *
     * @return names of functions NOT supported by POI
     */
    public static Collection<String> getNotSupportedFunctionNames(){
        Collection<String> lst = new TreeSet<>();
        lst.addAll(FunctionEval.getNotSupportedFunctionNames());
        lst.addAll(AnalysisToolPak.getNotSupportedFunctionNames());
        return Collections.unmodifiableCollection(lst);
    }

    /**
     * Register a ATP function in runtime.
     *
     * @param name  the function name
     * @param func  the functoin to register
     * @throws IllegalArgumentException if the function is unknown or already registered.
     * @since 3.8 beta6
     */
    public static void registerFunction(String name, FreeRefFunction func){
        AnalysisToolPak.registerFunction(name, func);
    }

    /**
     * Register a function in runtime.
     *
     * @param name  the function name
     * @param func  the functoin to register
     * @throws IllegalArgumentException if the function is unknown or already registered.
     * @since 3.8 beta6
     */
    public static void registerFunction(String name, Function func){
        FunctionEval.registerFunction(name, func);
    }

    public void setDebugEvaluationOutputForNextEval(boolean value){
        dbgEvaluationOutputForNextEval = value;
    }
    public boolean isDebugEvaluationOutputForNextEval(){
        return dbgEvaluationOutputForNextEval;
    }
}
