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

import java.util.Stack;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Evaluates formula cells.<p/>
 *
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearCache()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public class WorkbookEvaluator {

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

	private final EvaluationWorkbook _workbook;
	private final EvaluationCache _cache;

	private Counter _evaluationCounter;

	public WorkbookEvaluator(EvaluationWorkbook workbook) {
		_workbook = workbook;
		_cache = new EvaluationCache();
		_evaluationCounter = new Counter();
	}

	/**
	 * for debug use. Used in toString methods
	 */
	/* package */ String getSheetName(Sheet sheet) {
		return getSheetName(getSheetIndex(sheet));
	}
	private String getSheetName(int sheetIndex) {
		return _workbook.getSheetName(sheetIndex);
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
	 * Should be called whenever there are changes to input cells in the evaluated workbook.
	 * Failure to call this method after changing cell values will cause incorrect behaviour
	 * of the evaluate~ methods of this class
	 */
	public void clearAllCachedResultValues() {
		_cache.clear();
	}

	public void clearCachedResultValue(Sheet sheet, int rowIndex, int columnIndex) {
		int sheetIndex = getSheetIndex(sheet);
		_cache.clearValue(sheetIndex, rowIndex, columnIndex);

	}
	private int getSheetIndex(Sheet sheet) {
		// TODO cache sheet indexes too
		return _workbook.getSheetIndex(sheet);
	}

	public ValueEval evaluate(Cell srcCell) {
		return internalEvaluate(srcCell, new EvaluationTracker(_cache));
	}

	/**
	 * Dev. Note: Internal evaluate must be passed only a formula cell
	 * else a runtime exception will be thrown somewhere inside the method.
	 * (Hence this is a private method.)
	 * @return never <code>null</code>, never {@link BlankEval}
	 */
	/* package */ ValueEval internalEvaluate(Cell srcCell, EvaluationTracker tracker) {
		int srcRowNum = srcCell.getRowIndex();
		int srcColNum = srcCell.getCellNum();

		ValueEval result;

		int sheetIndex = getSheetIndex(srcCell.getSheet());
		result = tracker.startEvaluate(sheetIndex, srcRowNum, srcColNum);
		if (result != null) {
			return result;
		}
		_evaluationCounter.value++;
		_evaluationCounter.depth++;

		try {
			Ptg[] ptgs = _workbook.getFormulaTokens(srcCell);
			result = evaluateCell(sheetIndex, srcRowNum, (short)srcColNum, ptgs, tracker);
		} finally {
			tracker.endEvaluate(sheetIndex, srcRowNum, srcColNum, result);
			_evaluationCounter.depth--;
		}
		if (isDebugLogEnabled()) {
			String sheetName = getSheetName(sheetIndex);
			CellReference cr = new CellReference(srcRowNum, srcColNum);
			logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + result.toString());
		}
		return result;
	}
	private ValueEval evaluateCell(int sheetIndex, int srcRowNum, short srcColNum, Ptg[] ptgs, EvaluationTracker tracker) {

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
//				logDebug("invoke " + operation + " (nAgs=" + numops + ")");
				opResult = invokeOperation(operation, ops, _workbook, sheetIndex, srcRowNum, srcColNum);
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
			EvaluationWorkbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {

		if(operation instanceof FunctionEval) {
			FunctionEval fe = (FunctionEval) operation;
			if(fe.isFreeRefFunction()) {
				return fe.getFreeRefFunction().evaluate(ops, workbook, sheetIndex, srcRowNum, srcColNum);
			}
		}
		return operation.evaluate(ops, srcRowNum, (short)srcColNum);
	}

	private Sheet getOtherSheet(int externSheetIndex) {
		return _workbook.getSheetByExternSheetIndex(externSheetIndex);
	}

	/**
	 * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
	 * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
	 * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
	 * passed here!
	 */
	private Eval getEvalForPtg(Ptg ptg, int sheetIndex, EvaluationTracker tracker) {
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
		CellEvaluator ce = new CellEvaluator(this, tracker);
		Sheet sheet = _workbook.getSheet(sheetIndex);
		if (ptg instanceof RefPtg) {
			return new LazyRefEval(((RefPtg) ptg), sheet, ce);
		}
		if (ptg instanceof AreaPtg) {
			return new LazyAreaEval(((AreaPtg) ptg), sheet, ce);
		}
		if (ptg instanceof Ref3DPtg) {
			Ref3DPtg refPtg = (Ref3DPtg) ptg;
			Sheet xsheet = getOtherSheet(refPtg.getExternSheetIndex());
			return new LazyRefEval(refPtg, xsheet, ce);
		}
		if (ptg instanceof Area3DPtg) {
			Area3DPtg a3dp = (Area3DPtg) ptg;
			Sheet xsheet = getOtherSheet(a3dp.getExternSheetIndex());
			return new LazyAreaEval(a3dp, xsheet, ce);
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
}
