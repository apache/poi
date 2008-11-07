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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaErrPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ControlPtg;
import org.apache.poi.hssf.record.formula.DeletedArea3DPtg;
import org.apache.poi.hssf.record.formula.DeletedRef3DPtg;
import org.apache.poi.hssf.record.formula.ErrPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.MemErrPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefErrorPtg;
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
import org.apache.poi.hssf.record.formula.eval.MissingArgEval;
import org.apache.poi.hssf.record.formula.eval.NameEval;
import org.apache.poi.hssf.record.formula.eval.NameXEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.apache.poi.ss.usermodel.Cell;

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
 */
public final class WorkbookEvaluator {

	private final EvaluationWorkbook _workbook;
	private EvaluationCache _cache;
	private int _workbookIx;

	private final IEvaluationListener _evaluationListener;
	private final Map _sheetIndexesBySheet;
	private CollaboratingWorkbooksEnvironment _collaboratingWorkbookEnvironment;

	public WorkbookEvaluator(EvaluationWorkbook workbook) {
		this (workbook, null);
	}
	/* package */ WorkbookEvaluator(EvaluationWorkbook workbook, IEvaluationListener evaluationListener) {
		_workbook = workbook;
		_evaluationListener = evaluationListener;
		_cache = new EvaluationCache(evaluationListener);
		_sheetIndexesBySheet = new IdentityHashMap();
		_collaboratingWorkbookEnvironment = CollaboratingWorkbooksEnvironment.EMPTY;
		_workbookIx = 0;
	}

	/**
	 * also for debug use. Used in toString methods
	 */
	/* package */ String getSheetName(int sheetIndex) {
		return _workbook.getSheetName(sheetIndex);
	}

	private static boolean isDebugLogEnabled() {
		return false;
	}
	private static void logDebug(String s) {
		if (isDebugLogEnabled()) {
			System.out.println(s);
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

	/* package */ void detachFromEnvironment() {
		_collaboratingWorkbookEnvironment = CollaboratingWorkbooksEnvironment.EMPTY;
		_cache = new EvaluationCache(_evaluationListener);
		_workbookIx = 0;
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
		Integer result = (Integer) _sheetIndexesBySheet.get(sheet);
		if (result == null) {
			int sheetIndex = _workbook.getSheetIndex(sheet);
			if (sheetIndex < 0) {
				throw new RuntimeException("Specified sheet from a different book");
			}
			result = new Integer(sheetIndex);
			_sheetIndexesBySheet.put(sheet, result);
		}
		return result.intValue();
	}

	public ValueEval evaluate(EvaluationCell srcCell) {
		int sheetIndex = getSheetIndex(srcCell.getSheet());
		return evaluateAny(srcCell, sheetIndex, srcCell.getRowIndex(), srcCell.getColumnIndex(), new EvaluationTracker(_cache));
	}


	/**
	 * @return never <code>null</code>, never {@link BlankEval}
	 */
	private ValueEval evaluateAny(EvaluationCell srcCell, int sheetIndex,
				int rowIndex, int columnIndex, EvaluationTracker tracker) {

		if (srcCell == null || srcCell.getCellType() != Cell.CELL_TYPE_FORMULA) {
			ValueEval result = getValueFromNonFormulaCell(srcCell);
			tracker.acceptPlainValueDependency(_workbookIx, sheetIndex, rowIndex, columnIndex, result);
			return result;
		}

		FormulaCellCacheEntry cce = _cache.getOrCreateFormulaCellEntry(srcCell);
		tracker.acceptFormulaDependency(cce);
		IEvaluationListener evalListener = _evaluationListener;
		if (cce.getValue() == null) {
			if (!tracker.startEvaluate(cce)) {
				return ErrorEval.CIRCULAR_REF_ERROR;
			}

			try {
				ValueEval result;

				Ptg[] ptgs = _workbook.getFormulaTokens(srcCell);
				if (evalListener == null) {
					result = evaluateFormula(sheetIndex, rowIndex, columnIndex, ptgs, tracker);
				} else {
					evalListener.onStartEvaluate(srcCell, cce, ptgs);
					result = evaluateFormula(sheetIndex, rowIndex, columnIndex, ptgs, tracker);
					evalListener.onEndEvaluate(cce, result);
				}

				tracker.updateCacheResult(result);
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
			logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + cce.getValue().toString());
		}
		return cce.getValue();
	}
	/**
	 * Gets the value from a non-formula cell.
	 * @param cell may be <code>null</code>
	 * @return {@link BlankEval} if cell is <code>null</code> or blank, never <code>null</code>
	 */
	/* package */ static ValueEval getValueFromNonFormulaCell(EvaluationCell cell) {
		if (cell == null) {
			return BlankEval.INSTANCE;
		}
		int cellType = cell.getCellType();
		switch (cellType) {
			case Cell.CELL_TYPE_NUMERIC:
				return new NumberEval(cell.getNumericCellValue());
			case Cell.CELL_TYPE_STRING:
				return new StringEval(cell.getStringCellValue());
			case Cell.CELL_TYPE_BOOLEAN:
				return BoolEval.valueOf(cell.getBooleanCellValue());
			case Cell.CELL_TYPE_BLANK:
				return BlankEval.INSTANCE;
			case Cell.CELL_TYPE_ERROR:
				return ErrorEval.valueOf(cell.getErrorCellValue());
		}
		throw new RuntimeException("Unexpected cell type (" + cellType + ")");
	}
	// visibility raised for testing
	/* package */ ValueEval evaluateFormula(int sheetIndex, int srcRowNum, int srcColNum, Ptg[] ptgs, EvaluationTracker tracker) {

		Stack stack = new Stack();
		for (int i = 0, iSize = ptgs.length; i < iSize; i++) {

			// since we don't know how to handle these yet :(
			Ptg ptg = ptgs[i];
			if (ptg instanceof AttrPtg) {
				AttrPtg attrPtg = (AttrPtg) ptg;
				if (attrPtg.isSum()) {
					// Excel prefers to encode 'SUM()' as a tAttr token, but this evaluator
					// expects the equivalent function token
					byte nArgs = 1;  // tAttrSum always has 1 parameter
					ptg = new FuncVarPtg("SUM", nArgs);
				}
			}
			if (ptg instanceof ControlPtg) {
				// skip Parentheses, Attr, etc
				continue;
			}
			if (ptg instanceof MemFuncPtg) {
				// can ignore, rest of tokens for this expression are in OK RPN order
				continue;
			}
			if (ptg instanceof MemErrPtg) { continue; }

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
//				logDebug("invoke " + operation + " (nAgs=" + numops + ")");
				opResult = invokeOperation(operation, ops, _workbook, sheetIndex, srcRowNum, srcColNum);
				if (opResult == MissingArgEval.instance) {
					opResult = BlankEval.INSTANCE;
				}
			} else {
				opResult = getEvalForPtg(ptg, sheetIndex, tracker);
			}
			if (opResult == null) {
				throw new RuntimeException("Evaluation result must not be null");
			}
//			logDebug("push " + opResult);
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
	private static ValueEval dereferenceValue(ValueEval evaluationResult, int srcRowNum, int srcColNum) {
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
			EvaluationWorkbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {

		if(operation instanceof FunctionEval) {
			FunctionEval fe = (FunctionEval) operation;
			if(fe.isFreeRefFunction()) {
				return fe.getFreeRefFunction().evaluate(ops, workbook, sheetIndex, srcRowNum, srcColNum);
			}
		}
		return operation.evaluate(ops, srcRowNum, (short)srcColNum);
	}
	private SheetRefEvaluator createExternSheetRefEvaluator(EvaluationTracker tracker,
			ExternSheetReferenceToken ptg) {
		int externSheetIndex = ptg.getExternSheetIndex();
		ExternalSheet externalSheet = _workbook.getExternalSheet(externSheetIndex);
		if (externalSheet != null) {
			WorkbookEvaluator otherEvaluator = _collaboratingWorkbookEnvironment.getWorkbookEvaluator(externalSheet.getWorkbookName());
			EvaluationWorkbook otherBook = otherEvaluator._workbook;
			int otherSheetIndex = otherBook.getSheetIndex(externalSheet.getSheetName());
			return new SheetRefEvaluator(otherEvaluator, tracker, otherBook, otherSheetIndex);
		}
		int otherSheetIndex = _workbook.convertFromExternSheetIndex(externSheetIndex);
		return new SheetRefEvaluator(this, tracker, _workbook, otherSheetIndex);

	}

	/**
	 * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
	 * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
	 * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
	 * passed here!
	 */
	private Eval getEvalForPtg(Ptg ptg, int sheetIndex, EvaluationTracker tracker) {
		//  consider converting all these (ptg instanceof XxxPtg) expressions to (ptg.getClass() == XxxPtg.class)

		if (ptg instanceof NamePtg) {
			// named ranges, macro functions
			NamePtg namePtg = (NamePtg) ptg;
			EvaluationName nameRecord = _workbook.getName(namePtg);
			if (nameRecord.isFunctionName()) {
				return new NameEval(nameRecord.getNameText());
			}
			if (nameRecord.hasFormula()) {
				return evaluateNameFormula(nameRecord.getNameDefinition(), sheetIndex, tracker);
			}

			throw new RuntimeException("Don't now how to evalate name '" + nameRecord.getNameText() + "'");
		}
		if (ptg instanceof NameXPtg) {
			return new NameXEval(((NameXPtg) ptg));
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
			Ref3DPtg refPtg = (Ref3DPtg) ptg;
			SheetRefEvaluator sre = createExternSheetRefEvaluator(tracker, refPtg);
			return new LazyRefEval(refPtg, sre);
		}
		if (ptg instanceof Area3DPtg) {
			Area3DPtg aptg = (Area3DPtg) ptg;
			SheetRefEvaluator sre = createExternSheetRefEvaluator(tracker, aptg);
			return new LazyAreaEval(aptg, sre);
		}
		SheetRefEvaluator sre = new SheetRefEvaluator(this, tracker, _workbook, sheetIndex);
		if (ptg instanceof RefPtg) {
			return new LazyRefEval(((RefPtg) ptg), sre);
		}
		if (ptg instanceof AreaPtg) {
			return new LazyAreaEval(((AreaPtg) ptg), sre);
		}

		if (ptg instanceof UnknownPtg) {
			// POI uses UnknownPtg when the encoded Ptg array seems to be corrupted.
			// This seems to occur in very rare cases (e.g. unused name formulas in bug 44774, attachment 21790)
			// In any case, formulas are re-parsed before execution, so UnknownPtg should not get here
			throw new RuntimeException("UnknownPtg not allowed");
		}

		throw new RuntimeException("Unexpected ptg class (" + ptg.getClass().getName() + ")");
	}
	private Eval evaluateNameFormula(Ptg[] ptgs, int sheetIndex, EvaluationTracker tracker) {
		if (ptgs.length > 1) {
			throw new RuntimeException("Complex name formulas not supported yet");
		}
		return getEvalForPtg(ptgs[0], sheetIndex, tracker);
	}

	/**
	 * Used by the lazy ref evals whenever they need to get the value of a contained cell.
	 */
	/* package */ ValueEval evaluateReference(EvaluationSheet sheet, int sheetIndex, int rowIndex,
			int columnIndex, EvaluationTracker tracker) {

		EvaluationCell cell = sheet.getCell(rowIndex, columnIndex);
		return evaluateAny(cell, sheetIndex, rowIndex, columnIndex, tracker);
	}
}
