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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;

/**
 * Internal POI use only
 *
 * @author Josh Micich
 */
public final class HSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {

	private final HSSFWorkbook _uBook;
	private final Workbook _iBook;

	public static HSSFEvaluationWorkbook create(HSSFWorkbook book) {
		if (book == null) {
			return null;
		}
		return new HSSFEvaluationWorkbook(book);
	}

	private HSSFEvaluationWorkbook(HSSFWorkbook book) {
		_uBook = book;
		_iBook = book.getWorkbook();
	}

	public int getExternalSheetIndex(String sheetName) {
		int sheetIndex = _uBook.getSheetIndex(sheetName);
		return _iBook.checkExternSheet(sheetIndex);
	}
	public int getExternalSheetIndex(String workbookName, String sheetName) {
		return _iBook.getExternalSheetIndex(workbookName, sheetName);
	}

	public NameXPtg getNameXPtg(String name) {
		return _iBook.getNameXPtg(name);
	}

	/**
	 * Lookup a named range by its name.
	 *
	 * @param name the name to search
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means workbook-global names
	  */
	public EvaluationName getName(String name, int sheetIndex) {
		for(int i=0; i < _iBook.getNumNames(); i++) {
			NameRecord nr = _iBook.getNameRecord(i);
			if (nr.getSheetNumber() == sheetIndex+1 && name.equalsIgnoreCase(nr.getNameText())) {
				return new Name(nr, i);
			}
		}
		return sheetIndex == -1 ? null : getName(name, -1);
	}

	public int getSheetIndex(EvaluationSheet evalSheet) {
		HSSFSheet sheet = ((HSSFEvaluationSheet)evalSheet).getHSSFSheet();
		return _uBook.getSheetIndex(sheet);
	}
	public int getSheetIndex(String sheetName) {
		return _uBook.getSheetIndex(sheetName);
	}

	public String getSheetName(int sheetIndex) {
		return _uBook.getSheetName(sheetIndex);
	}

	public EvaluationSheet getSheet(int sheetIndex) {
		return new HSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex));
	}
	public int convertFromExternSheetIndex(int externSheetIndex) {
		return _iBook.getSheetIndexFromExternSheetIndex(externSheetIndex);
	}

	public ExternalSheet getExternalSheet(int externSheetIndex) {
		return _iBook.getExternalSheet(externSheetIndex);
	}

	public String resolveNameXText(NameXPtg n) {
		return _iBook.resolveNameXText(n.getSheetRefIndex(), n.getNameIndex());
	}

	public String getSheetNameByExternSheet(int externSheetIndex) {
		return _iBook.findSheetNameFromExternSheet(externSheetIndex);
	}
	public String getNameText(NamePtg namePtg) {
		return _iBook.getNameRecord(namePtg.getIndex()).getNameText();
	}
	public EvaluationName getName(NamePtg namePtg) {
		int ix = namePtg.getIndex();
		return new Name(_iBook.getNameRecord(ix), ix);
	}
	public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
		HSSFCell cell = ((HSSFEvaluationCell)evalCell).getHSSFCell();
		if (false) {
			// re-parsing the formula text also works, but is a waste of time
			// It is useful from time to time to run all unit tests with this code
			// to make sure that all formulas POI can evaluate can also be parsed.
			try {
				return HSSFFormulaParser.parse(cell.getCellFormula(), _uBook, FormulaType.CELL, _uBook.getSheetIndex(cell.getSheet()));
			} catch (FormulaParseException e) {
				// Note - as of Bugzilla 48036 (svn r828244, r828247) POI is capable of evaluating
				// IntesectionPtg.  However it is still not capable of parsing it.
				// So FormulaEvalTestData.xls now contains a few formulas that produce errors here.
				System.err.println(e.getMessage());
			}
		}
		FormulaRecordAggregate fra = (FormulaRecordAggregate) cell.getCellValueRecord();
		return fra.getFormulaTokens();
	}

	private static final class Name implements EvaluationName {

		private final NameRecord _nameRecord;
		private final int _index;

		public Name(NameRecord nameRecord, int index) {
			_nameRecord = nameRecord;
			_index = index;
		}
		public Ptg[] getNameDefinition() {
			return _nameRecord.getNameDefinition();
		}
		public String getNameText() {
			return _nameRecord.getNameText();
		}
		public boolean hasFormula() {
			return _nameRecord.hasFormula();
		}
		public boolean isFunctionName() {
			return _nameRecord.isFunctionName();
		}
		public boolean isRange() {
			return _nameRecord.hasFormula(); // TODO - is this right?
		}
		public NamePtg createPtg() {
			return new NamePtg(_index);
		}
	}

	public SpreadsheetVersion getSpreadsheetVersion(){
		return SpreadsheetVersion.EXCEL97;
	}
}
