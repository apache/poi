package org.apache.poi.xssf.usermodel;

import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Internal POI use only
 * 
 * @author Josh Micich
 */
public final class XSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {

	private final XSSFWorkbook _uBook;
	
	public static XSSFEvaluationWorkbook create(XSSFWorkbook book) {
		if (book == null) {
			return null;
		}
		return new XSSFEvaluationWorkbook(book);
	}

	private XSSFEvaluationWorkbook(XSSFWorkbook book) {
		_uBook = book;
	}

	private int convertFromExternalSheetIndex(int externSheetIndex) {
		return externSheetIndex;
	}
	/**
	 * @return the sheet index of the sheet with the given external index.
	 */
	public int convertFromExternSheetIndex(int externSheetIndex) {
		return externSheetIndex;
	}
	/**
	 * @return  the external sheet index of the sheet with the given internal
	 * index. Used by some of the more obscure formula and named range things. 
	 * Fairly easy on XSSF (we think...) since the internal and external 
	 * indicies are the same
	 */
	private int convertToExternalSheetIndex(int sheetIndex) {
		return sheetIndex;
	}

	public int getExternalSheetIndex(String sheetName) {
		int sheetIndex = _uBook.getSheetIndex(sheetName);
		return convertToExternalSheetIndex(sheetIndex);
	}

	public EvaluationName getName(int index) {
		return new Name(_uBook.getNameAt(index), index, this);
	}

	public EvaluationName getName(String name) {
		for(int i=0; i < _uBook.getNumberOfNames(); i++) {
			String nameText = _uBook.getNameName(i);
			if (name.equalsIgnoreCase(nameText)) {
				return new Name(_uBook.getNameAt(i), i, this);
			}
		}
		return null;
	}

	public int getSheetIndex(Sheet sheet) {
		return _uBook.getSheetIndex(sheet);
	}

	public String getSheetName(int sheetIndex) {
		return _uBook.getSheetName(sheetIndex);
	}

	public NameXPtg getNameXPtg(String name) {
		// may require to return null to make tests pass
		throw new RuntimeException("Not implemented yet");
	}

	public Sheet getSheet(int sheetIndex) {
		return _uBook.getSheetAt(sheetIndex);
	}

    /**
     * Doesn't do anything - returns the same index
     * TODO - figure out if this is a ole2 specific thing, or
     *  if we need to do something proper here too!
     */
	public Sheet getSheetByExternSheetIndex(int externSheetIndex) {
		int sheetIndex = convertFromExternalSheetIndex(externSheetIndex);
		return _uBook.getSheetAt(sheetIndex);
	}

	public Workbook getWorkbook() {
		return _uBook;
	}

    /**
     * TODO - figure out what the hell this methods does in
     *  HSSF...
     */
	public String resolveNameXText(NameXPtg n) {
		throw new RuntimeException("method not implemented yet");
	}

	public String getSheetNameByExternSheet(int externSheetIndex) {
		int sheetIndex = convertFromExternalSheetIndex(externSheetIndex);
		return _uBook.getSheetName(sheetIndex);
	}

	public String getNameText(NamePtg namePtg) {
		return _uBook.getNameAt(namePtg.getIndex()).getNameName();
	}
	public EvaluationName getName(NamePtg namePtg) {
		int ix = namePtg.getIndex();
		return new Name(_uBook.getNameAt(ix), ix, this);
	}
	public Ptg[] getFormulaTokens(Cell cell) {
		XSSFEvaluationWorkbook frBook = XSSFEvaluationWorkbook.create(_uBook);
		return FormulaParser.parse(cell.getCellFormula(), frBook);
	}

	private static final class Name implements EvaluationName {

		private final XSSFName _nameRecord;
		private final int _index;
		private final FormulaParsingWorkbook _fpBook;

		public Name(XSSFName name, int index, FormulaParsingWorkbook fpBook) {
			_nameRecord = name;
			_index = index;
			_fpBook = fpBook;
		}

		public Ptg[] getNameDefinition() {
			
			return FormulaParser.parse(_nameRecord.getReference(), _fpBook);
		}

		public String getNameText() {
			return _nameRecord.getNameName();
		}

		public boolean hasFormula() {
			// TODO - no idea if this is right
			CTDefinedName ctn = _nameRecord.getCTName();
			String strVal = ctn.getStringValue();
			return !ctn.getFunction() && strVal != null && strVal.length() > 0;
		}

		public boolean isFunctionName() {
			return _nameRecord.isFunctionName();
		}

		public boolean isRange() {
			return hasFormula(); // TODO - is this right?
		}
		public NamePtg createPtg() {
			return new NamePtg(_index);
		}
	}
}
