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

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.apache.poi.ss.formula.WorkbookEvaluator;

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

	private WorkbookEvaluator _bookEvaluator;

	/**
	 * @deprecated (Sep 2008) HSSFSheet parameter is ignored
	 */
	public HSSFFormulaEvaluator(HSSFSheet sheet, HSSFWorkbook workbook) {
		this(workbook);
		if (false) {
			sheet.toString(); // suppress unused parameter compiler warning
		}
	}
	public HSSFFormulaEvaluator(HSSFWorkbook workbook) {
		_bookEvaluator = new WorkbookEvaluator(HSSFEvaluationWorkbook.create(workbook));
	}
	
	/**
	 * Coordinates several formula evaluators together so that formulas that involve external
	 * references can be evaluated.
	 * @param workbookNames the simple file names used to identify the workbooks in formulas
	 * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
	 * @param evaluators all evaluators for the full set of workbooks required by the formulas. 
	 */
	public static void setupEnvironment(String[] workbookNames, HSSFFormulaEvaluator[] evaluators) {
		WorkbookEvaluator[] wbEvals = new WorkbookEvaluator[evaluators.length];
		for (int i = 0; i < wbEvals.length; i++) {
			wbEvals[i] = evaluators[i]._bookEvaluator;
		}
		CollaboratingWorkbooksEnvironment.setup(workbookNames, wbEvals);
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
	 * Should be called whenever there are major changes (e.g. moving sheets) to input cells
	 * in the evaluated workbook.
	 * Failure to call this method after changing cell values will cause incorrect behaviour
	 * of the evaluate~ methods of this class
	 */
	public void clearAllCachedResultValues() {
		_bookEvaluator.clearAllCachedResultValues();
	}
	/**
	 * Sets the cached value for a plain (non-formula) cell.
	 * Should be called whenever there are changes to individual input cells in the evaluated workbook.
	 * Failure to call this method after changing cell values will cause incorrect behaviour
	 * of the evaluate~ methods of this class
	 * @param never <code>null</code>. Use {@link BlankEval#INSTANCE} when the cell is being 
	 * cleared. Otherwise an instance of {@link NumberEval}, {@link StringEval}, {@link BoolEval}
	 * or {@link ErrorEval} to represent a plain cell value.
	 */
	public void setCachedPlainValue(HSSFSheet sheet, int rowIndex, int columnIndex, ValueEval value) {
		_bookEvaluator.setCachedPlainValue(sheet, rowIndex, columnIndex, value);
	}
	/**
	 * Should be called to tell the cell value cache that the specified cell has just become a
	 * formula cell, or the formula text has changed 
	 */
	public void notifySetFormula(HSSFSheet sheet, int rowIndex, int columnIndex) {
		_bookEvaluator.notifySetFormula(sheet, rowIndex, columnIndex);
	}

	/**
	 * If cell contains a formula, the formula is evaluated and returned,
	 * else the CellValue simply copies the appropriate cell value from
	 * the cell and also its cell type. This method should be preferred over
	 * evaluateInCell() when the call should not modify the contents of the
	 * original cell.
	 * 
	 * @param cell may be <code>null</code> signifying that the cell is not present (or blank)
	 * @return <code>null</code> if the supplied cell is <code>null</code> or blank
	 */
	public CellValue evaluate(HSSFCell cell) {
		if (cell == null) {
			return null;
		}

		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_BOOLEAN:
				return CellValue.valueOf(cell.getBooleanCellValue());
			case HSSFCell.CELL_TYPE_ERROR:
				return CellValue.getError(cell.getErrorCellValue());
			case HSSFCell.CELL_TYPE_FORMULA:
				return evaluateFormulaCellValue(cell);
			case HSSFCell.CELL_TYPE_NUMERIC:
				return new CellValue(cell.getNumericCellValue());
			case HSSFCell.CELL_TYPE_STRING:
				return new CellValue(cell.getRichStringCellValue().getString());
			case HSSFCell.CELL_TYPE_BLANK:
				return null;
		}
		throw new IllegalStateException("Bad cell type (" + cell.getCellType() + ")");
	}


	/**
	 * If cell contains formula, it evaluates the formula, and saves the result of the formula. The
	 * cell remains as a formula cell. If the cell does not contain formula, this method returns -1
	 * and leaves the cell unchanged.
	 * 
	 * Note that the type of the <em>formula result</em> is returned, so you know what kind of 
	 * cached formula result is also stored with  the formula.
	 * <pre>
	 * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
	 * </pre>
	 * Be aware that your cell will hold both the formula, and the result. If you want the cell 
	 * replaced with the result of the formula, use {@link #evaluateInCell(HSSFCell)}
	 * @param cell The cell to evaluate
	 * @return -1 for non-formula cells, or the type of the <em>formula result</em>
	 */
	public int evaluateFormulaCell(HSSFCell cell) {
		if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_FORMULA) {
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
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
			CellValue cv = evaluateFormulaCellValue(cell);
			setCellType(cell, cv); // cell will no longer be a formula cell
			setCellValue(cell, cv);
		}
		return cell;
	}
	private static void setCellType(HSSFCell cell, CellValue cv) {
		int cellType = cv.getCellType();
		switch (cellType) {
			case HSSFCell.CELL_TYPE_BOOLEAN:
			case HSSFCell.CELL_TYPE_ERROR:
			case HSSFCell.CELL_TYPE_NUMERIC:
			case HSSFCell.CELL_TYPE_STRING:
				cell.setCellType(cellType);
				return;
			case HSSFCell.CELL_TYPE_BLANK:
				// never happens - blanks eventually get translated to zero
			case HSSFCell.CELL_TYPE_FORMULA:
				// this will never happen, we have already evaluated the formula
		}
		throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
	}

	private static void setCellValue(HSSFCell cell, CellValue cv) {
		int cellType = cv.getCellType();
		switch (cellType) {
			case HSSFCell.CELL_TYPE_BOOLEAN:
				cell.setCellValue(cv.getBooleanValue());
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				cell.setCellErrorValue(cv.getErrorValue());
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				cell.setCellValue(cv.getNumberValue());
				break;
			case HSSFCell.CELL_TYPE_STRING:
				cell.setCellValue(cv.getRichTextStringValue());
				break;
			case HSSFCell.CELL_TYPE_BLANK:
				// never happens - blanks eventually get translated to zero
			case HSSFCell.CELL_TYPE_FORMULA:
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
	public static void evaluateAllFormulaCells(HSSFWorkbook wb) {
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
		for(int i=0; i<wb.getNumberOfSheets(); i++) {
			HSSFSheet sheet = wb.getSheetAt(i);

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
	private CellValue evaluateFormulaCellValue(HSSFCell cell) {
		ValueEval eval = _bookEvaluator.evaluate(cell);
		if (eval instanceof NumberEval) {
			NumberEval ne = (NumberEval) eval;
			return new CellValue(ne.getNumberValue());
		}
		if (eval instanceof BoolEval) {
			BoolEval be = (BoolEval) eval;
			return CellValue.valueOf(be.getBooleanValue());
		}
		if (eval instanceof StringEval) {
			StringEval ne = (StringEval) eval;
			return new CellValue(ne.getStringValue());
		}
		if (eval instanceof ErrorEval) {
			return CellValue.getError(((ErrorEval)eval).getErrorCode());
		}
		throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
	}

	/**
	 * Mimics the 'data view' of a cell. This allows formula evaluator
	 * to return a CellValue instead of precasting the value to String
	 * or Number or boolean type.
	 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
	 */
	public static final class CellValue {
		public static final CellValue TRUE = new CellValue(HSSFCell.CELL_TYPE_BOOLEAN, 0.0, true,  null, 0);
		public static final CellValue FALSE= new CellValue(HSSFCell.CELL_TYPE_BOOLEAN, 0.0, false, null, 0);

		private final int _cellType;
		private final double _numberValue;
		private final boolean _booleanValue;
		private final String _textValue;
		private final int _errorCode;

		private CellValue(int cellType, double numberValue, boolean booleanValue,
				String textValue, int errorCode) {
			_cellType = cellType;
			_numberValue = numberValue;
			_booleanValue = booleanValue;
			_textValue = textValue;
			_errorCode = errorCode;
		}


		/* package*/ CellValue(double numberValue) {
			this(HSSFCell.CELL_TYPE_NUMERIC, numberValue, false, null, 0);
		}
		/* package*/ static CellValue valueOf(boolean booleanValue) {
			return booleanValue ? TRUE : FALSE;
		}
		/* package*/ CellValue(String stringValue) {
			this(HSSFCell.CELL_TYPE_STRING, 0.0, false, stringValue, 0);
		}
		/* package*/ static CellValue getError(int errorCode) {
			return new CellValue(HSSFCell.CELL_TYPE_ERROR, 0.0, false, null, errorCode);
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
		public HSSFRichTextString getRichTextStringValue() {
			return new HSSFRichTextString(_textValue);
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
				case HSSFCell.CELL_TYPE_NUMERIC:
					return String.valueOf(_numberValue);
				case HSSFCell.CELL_TYPE_STRING:
					return '"' + _textValue + '"';
				case HSSFCell.CELL_TYPE_BOOLEAN:
					return _booleanValue ? "TRUE" : "FALSE";
				case HSSFCell.CELL_TYPE_ERROR:
					return ErrorEval.getText(_errorCode);
			}
			return "<error unexpected cell type " + _cellType + ">";
		}
	}
}
