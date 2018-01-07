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

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.DataValidation.ErrorStyle;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.Test;

/**
 * Class for testing Excel's data validation mechanism
 *
 * @author Dragos Buleandra ( dragos.buleandra@trade2b.ro )
 */
public abstract class BaseTestDataValidation {
    private final ITestDataProvider _testDataProvider;

    private static final POILogger log = POILogFactory.getLogger(BaseTestDataValidation.class);

    protected BaseTestDataValidation(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

	/** Convenient access to ERROR_STYLE constants */
	protected static final DataValidation.ErrorStyle ES = null;
	/** Convenient access to OPERATOR constants */
	protected static final DataValidationConstraint.ValidationType VT = null;
	/** Convenient access to OPERATOR constants */
	protected static final DataValidationConstraint.OperatorType OP = null;

	private static final class ValidationAdder {

		private final CellStyle _style_1;
		private final CellStyle _style_2;
		private  final int _validationType;
		private final Sheet _sheet;
		private int _currentRowIndex;
		private final CellStyle _cellStyle;

		public ValidationAdder(Sheet fSheet, CellStyle style_1, CellStyle style_2,
				CellStyle cellStyle, int validationType) {
			_sheet = fSheet;
			_style_1 = style_1;
			_style_2 = style_2;
			_cellStyle = cellStyle;
			_validationType = validationType;
			_currentRowIndex = fSheet.getPhysicalNumberOfRows();
		}
		public void addValidation(int operatorType, String firstFormula, String secondFormula,
				int errorStyle, String ruleDescr, String promptDescr,
				boolean allowEmpty, boolean inputBox, boolean errorBox) {
			String[] explicitListValues = null;

			addValidationInternal(operatorType, firstFormula, secondFormula, errorStyle, ruleDescr,
					promptDescr, allowEmpty, inputBox, errorBox, true,
					explicitListValues);
		}

		private void addValidationInternal(int operatorType, String firstFormula,
				String secondFormula, int errorStyle, String ruleDescr, String promptDescr,
				boolean allowEmpty, boolean inputBox, boolean errorBox, boolean suppressDropDown,
				String[] explicitListValues) {
			int rowNum = _currentRowIndex++;

			DataValidationHelper dataValidationHelper = _sheet.getDataValidationHelper();
			DataValidationConstraint dc = createConstraint(dataValidationHelper,operatorType, firstFormula, secondFormula, explicitListValues);

			DataValidation dv = dataValidationHelper.createValidation(dc,new CellRangeAddressList(rowNum, rowNum, 0, 0));

			dv.setEmptyCellAllowed(allowEmpty);
			dv.setErrorStyle(errorStyle);
			dv.createErrorBox("Invalid Input", "Something is wrong - check condition!");
			dv.createPromptBox("Validated Cell", "Allowable values have been restricted");

			dv.setShowPromptBox(inputBox);
			dv.setShowErrorBox(errorBox);
			dv.setSuppressDropDownArrow(suppressDropDown);


			_sheet.addValidationData(dv);
			writeDataValidationSettings(_sheet, _style_1, _style_2, ruleDescr, allowEmpty,
					inputBox, errorBox);
			if (_cellStyle != null) {
				Row row = _sheet.getRow(_sheet.getPhysicalNumberOfRows() - 1);
				Cell cell = row.createCell(0);
				cell.setCellStyle(_cellStyle);
			}
			writeOtherSettings(_sheet, _style_1, promptDescr);
		}
		private DataValidationConstraint createConstraint(DataValidationHelper dataValidationHelper,int operatorType, String firstFormula,
				String secondFormula, String[] explicitListValues) {
			if (_validationType == ValidationType.LIST) {
				if (explicitListValues != null) {
					return dataValidationHelper.createExplicitListConstraint(explicitListValues);
				}
				return dataValidationHelper.createFormulaListConstraint(firstFormula);
			}
			if (_validationType == ValidationType.TIME) {
				return dataValidationHelper.createTimeConstraint(operatorType, firstFormula, secondFormula);
			}
			if (_validationType == ValidationType.DATE) {
				return dataValidationHelper.createDateConstraint(operatorType, firstFormula, secondFormula, null);
			}
			if (_validationType == ValidationType.FORMULA) {
				return dataValidationHelper.createCustomConstraint(firstFormula);
			}

			if( _validationType == ValidationType.INTEGER) {
				return dataValidationHelper.createIntegerConstraint(operatorType, firstFormula, secondFormula);
			}
			if( _validationType == ValidationType.DECIMAL) {
				return dataValidationHelper.createDecimalConstraint(operatorType, firstFormula, secondFormula);
			}
			if( _validationType == ValidationType.TEXT_LENGTH) {
				return dataValidationHelper.createTextLengthConstraint(operatorType, firstFormula, secondFormula);
			}
			return null;
		}
		/**
		 * writes plain text values into cells in a tabular format to form comments readable from within
		 * the spreadsheet.
		 */
		private static void writeDataValidationSettings(Sheet sheet, CellStyle style_1,
				CellStyle style_2, String strCondition, boolean allowEmpty, boolean inputBox,
				boolean errorBox) {
			Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			// condition's string
			Cell cell = row.createCell(1);
			cell.setCellStyle(style_1);
			setCellValue(cell, strCondition);
			// allow empty cells
			cell = row.createCell(2);
			cell.setCellStyle(style_2);
			setCellValue(cell, ((allowEmpty) ? "yes" : "no"));
			// show input box
			cell = row.createCell(3);
			cell.setCellStyle(style_2);
			setCellValue(cell, ((inputBox) ? "yes" : "no"));
			// show error box
			cell = row.createCell(4);
			cell.setCellStyle(style_2);
			setCellValue(cell, ((errorBox) ? "yes" : "no"));
		}
		private static void writeOtherSettings(Sheet sheet, CellStyle style,
				String strStettings) {
			Row row = sheet.getRow(sheet.getPhysicalNumberOfRows() - 1);
			Cell cell = row.createCell(5);
			cell.setCellStyle(style);
			setCellValue(cell, strStettings);
		}
		public void addListValidation(String[] explicitListValues, String listFormula, String listValsDescr,
				boolean allowEmpty, boolean suppressDropDown) {
			String promptDescr = (allowEmpty ? "empty ok" : "not empty")
					+ ", " + (suppressDropDown ? "no drop-down" : "drop-down");
			addValidationInternal(ValidationType.LIST, listFormula, null, ErrorStyle.STOP, listValsDescr, promptDescr,
					allowEmpty, false, true, suppressDropDown, explicitListValues);
		}
	}

    private static void log(String msg) {
        log.log(POILogger.INFO, msg);
    }

	/**
	 * Manages the cell styles used for formatting the output spreadsheet
	 */
	private static final class WorkbookFormatter {

		private final Workbook _wb;
		private final CellStyle _style_1;
		private final CellStyle _style_2;
		private final CellStyle _style_3;
		private final CellStyle _style_4;
		private Sheet _currentSheet;

		public WorkbookFormatter(Workbook wb) {
			_wb = wb;
			_style_1 = createStyle( wb, HorizontalAlignment.LEFT );
			_style_2 = createStyle( wb, HorizontalAlignment.CENTER );
			_style_3 = createStyle( wb, HorizontalAlignment.CENTER, IndexedColors.GREY_25_PERCENT.getIndex(), true );
			_style_4 = createHeaderStyle(wb);
		}

		private static CellStyle createStyle(Workbook wb, HorizontalAlignment h_align, short color,
				boolean bold) {
			Font font = wb.createFont();
			if (bold) {
				font.setBold(true);
			}

			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFont(font);
			cellStyle.setFillForegroundColor(color);
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellStyle.setAlignment(h_align);
			cellStyle.setBorderLeft(BorderStyle.THIN);
			cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderTop(BorderStyle.THIN);
			cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderRight(BorderStyle.THIN);
			cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderBottom(BorderStyle.THIN);
			cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

			return cellStyle;
		}

		private static CellStyle createStyle(Workbook wb, HorizontalAlignment h_align) {
			return createStyle(wb, h_align, IndexedColors.WHITE.getIndex(), false);
		}
		private static CellStyle createHeaderStyle(Workbook wb) {
			Font font = wb.createFont();
			font.setColor( IndexedColors.WHITE.getIndex() );
			font.setBold(true);

			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyle.setAlignment(HorizontalAlignment.CENTER);
			cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellStyle.setBorderLeft(BorderStyle.THIN);
			cellStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
			cellStyle.setBorderTop(BorderStyle.THIN);
			cellStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
			cellStyle.setBorderRight(BorderStyle.THIN);
			cellStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());
			cellStyle.setBorderBottom(BorderStyle.THIN);
			cellStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
			cellStyle.setFont(font);
			return cellStyle;
		}


		public Sheet createSheet(String sheetName) {
			_currentSheet = _wb.createSheet(sheetName);
			return _currentSheet;
		}
		public void createDVTypeRow(String strTypeDescription) {
			Sheet sheet = _currentSheet;
			Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			sheet.addMergedRegion(new CellRangeAddress(sheet.getPhysicalNumberOfRows()-1, sheet.getPhysicalNumberOfRows()-1, 0, 5));
			Cell cell = row.createCell(0);
			setCellValue(cell, strTypeDescription);
			cell.setCellStyle(_style_3);
			row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		}

		public void createHeaderRow() {
			Sheet sheet = _currentSheet;
			Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			row.setHeight((short) 400);
			for (int i = 0; i < 6; i++) {
				row.createCell(i).setCellStyle(_style_4);
				if (i == 2 || i == 3 || i == 4) {
					sheet.setColumnWidth(i, 3500);
				} else if (i == 5) {
					sheet.setColumnWidth(i, 10000);
				} else {
					sheet.setColumnWidth(i, 8000);
				}
			}
			Cell cell = row.getCell(0);
			setCellValue(cell, "Data validation cells");
			cell = row.getCell(1);
			setCellValue(cell, "Condition");
			cell = row.getCell(2);
			setCellValue(cell, "Allow blank");
			cell = row.getCell(3);
			setCellValue(cell, "Prompt box");
			cell = row.getCell(4);
			setCellValue(cell, "Error box");
			cell = row.getCell(5);
			setCellValue(cell, "Other settings");
		}

		public ValidationAdder createValidationAdder(CellStyle cellStyle, int dataValidationType) {
			return new ValidationAdder(_currentSheet, _style_1, _style_2, cellStyle, dataValidationType);
		}

		public void createDVDescriptionRow(String strTypeDescription) {
			Sheet sheet = _currentSheet;
			Row row = sheet.getRow(sheet.getPhysicalNumberOfRows()-1);
			sheet.addMergedRegion(new CellRangeAddress(sheet.getPhysicalNumberOfRows()-1, sheet.getPhysicalNumberOfRows()-1, 0, 5));
			Cell cell = row.createCell(0);
			setCellValue(cell, strTypeDescription);
			cell.setCellStyle(_style_3);
			row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		}
	}


	private void addCustomValidations(WorkbookFormatter wf) {
		wf.createSheet("Custom");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, ValidationType.FORMULA);
		va.addValidation(OperatorType.BETWEEN, "ISNUMBER($A2)", null, ErrorStyle.STOP, "ISNUMBER(A2)", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.BETWEEN, "IF(SUM(A2:A3)=5,TRUE,FALSE)", null, ErrorStyle.WARNING, "IF(SUM(A2:A3)=5,TRUE,FALSE)", "Error box type = WARNING", false, false, true);
	}

	private static void addSimpleNumericValidations(WorkbookFormatter wf) {
		// data validation's number types
		wf.createSheet("Numbers");

		// "Whole number" validation type
		wf.createDVTypeRow("Whole number");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, ValidationType.INTEGER);
		va.addValidation(OperatorType.BETWEEN, "2", "6", ErrorStyle.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.NOT_BETWEEN, "2", "6", ErrorStyle.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OperatorType.EQUAL, "=3+2", null, ErrorStyle.WARNING, "Equal to (3+2)", "Error box type = WARNING", false, false, true);
		va.addValidation(OperatorType.NOT_EQUAL, "3", null, ErrorStyle.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OperatorType.GREATER_THAN, "3", null, ErrorStyle.WARNING, "Greater than 3", "-", true, false, false);
		va.addValidation(OperatorType.LESS_THAN, "3", null, ErrorStyle.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OperatorType.GREATER_OR_EQUAL, "4", null, ErrorStyle.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OperatorType.LESS_OR_EQUAL, "4", null, ErrorStyle.STOP, "Less than or equal to 4", "-", false, true, false);

		// "Decimal" validation type
		wf.createDVTypeRow("Decimal");
		wf.createHeaderRow();

		va = wf.createValidationAdder(null, ValidationType.DECIMAL);
		va.addValidation(OperatorType.BETWEEN, "2", "6", ErrorStyle.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.NOT_BETWEEN, "2", "6", ErrorStyle.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OperatorType.EQUAL, "3", null, ErrorStyle.WARNING, "Equal to 3", "Error box type = WARNING", false, false, true);
		va.addValidation(OperatorType.NOT_EQUAL, "3", null, ErrorStyle.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OperatorType.GREATER_THAN, "=12/6", null, ErrorStyle.WARNING, "Greater than (12/6)", "-", true, false, false);
		va.addValidation(OperatorType.LESS_THAN, "3", null, ErrorStyle.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OperatorType.GREATER_OR_EQUAL, "4", null, ErrorStyle.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OperatorType.LESS_OR_EQUAL, "4", null, ErrorStyle.STOP, "Less than or equal to 4", "-", false, true, false);
	}

	private static void addListValidations(WorkbookFormatter wf, Workbook wb) {
		final String cellStrValue
		 = "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 ";
		final String dataSheetName = "list_data";
		// "List" Data Validation type
		Sheet fSheet = wf.createSheet("Lists");
		Sheet dataSheet = wb.createSheet(dataSheetName);


		wf.createDVTypeRow("Explicit lists - list items are explicitly provided");
		wf.createDVDescriptionRow("Disadvantage - sum of item's length should be less than 255 characters");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, ValidationType.LIST);
		String listValsDescr = "POIFS,HSSF,HWPF,HPSF";
		String[] listVals = listValsDescr.split(",");
		va.addListValidation(listVals, null, listValsDescr, false, false);
		va.addListValidation(listVals, null, listValsDescr, false, true);
		va.addListValidation(listVals, null, listValsDescr, true, false);
		va.addListValidation(listVals, null, listValsDescr, true, true);



		wf.createDVTypeRow("Reference lists - list items are taken from others cells");
		wf.createDVDescriptionRow("Advantage - no restriction regarding the sum of item's length");
		wf.createHeaderRow();
		va = wf.createValidationAdder(null, ValidationType.LIST);
		String strFormula = "$A$30:$A$39";
		va.addListValidation(null, strFormula, strFormula, false, false);

		strFormula = dataSheetName + "!$A$1:$A$10";
		va.addListValidation(null, strFormula, strFormula, false, false);
		Name namedRange = wb.createName();
		namedRange.setNameName("myName");
		namedRange.setRefersToFormula(dataSheetName + "!$A$2:$A$7");
		strFormula = "myName";
		va.addListValidation(null, strFormula, strFormula, false, false);
		strFormula = "offset(myName, 2, 1, 4, 2)"; // Note about last param '2':
		// - Excel expects single row or single column when entered in UI, but process this OK otherwise
		va.addListValidation(null, strFormula, strFormula, false, false);

		// add list data on same sheet
		for (int i = 0; i < 10; i++) {
			Row currRow = fSheet.createRow(i + 29);
			setCellValue(currRow.createCell(0), cellStrValue);
		}
		// add list data on another sheet
		for (int i = 0; i < 10; i++) {
			Row currRow = dataSheet.createRow(i + 0);
			setCellValue(currRow.createCell(0), "Data a" + i);
			setCellValue(currRow.createCell(1), "Data b" + i);
			setCellValue(currRow.createCell(2), "Data c" + i);
		}
	}

	private static void addDateTimeValidations(WorkbookFormatter wf, Workbook wb) {
		wf.createSheet("Dates and Times");

		DataFormat dataFormat = wb.createDataFormat();
		short fmtDate = dataFormat.getFormat("m/d/yyyy");
		short fmtTime = dataFormat.getFormat("h:mm");
		CellStyle cellStyle_date = wb.createCellStyle();
		cellStyle_date.setDataFormat(fmtDate);
		CellStyle cellStyle_time = wb.createCellStyle();
		cellStyle_time.setDataFormat(fmtTime);

		wf.createDVTypeRow("Date ( cells are already formated as date - m/d/yyyy)");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(cellStyle_date, ValidationType.DATE);
		va.addValidation(OperatorType.BETWEEN,     "2004/01/02", "2004/01/06", ErrorStyle.STOP, "Between 1/2/2004 and 1/6/2004 ", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.NOT_BETWEEN, "2004/01/01", "2004/01/06", ErrorStyle.INFO, "Not between 1/2/2004 and 1/6/2004 ", "Error box type = INFO", false, true, true);
		va.addValidation(OperatorType.EQUAL,       "2004/03/02", null,       ErrorStyle.WARNING, "Equal to 3/2/2004", "Error box type = WARNING", false, false, true);
		va.addValidation(OperatorType.NOT_EQUAL,   "2004/03/02", null,       ErrorStyle.WARNING, "Not equal to 3/2/2004", "-", false, false, false);
		va.addValidation(OperatorType.GREATER_THAN,"=DATEVALUE(\"4-Jul-2001\")", null,       ErrorStyle.WARNING, "Greater than DATEVALUE('4-Jul-2001')", "-", true, false, false);
		va.addValidation(OperatorType.LESS_THAN,   "2004/03/02", null,       ErrorStyle.WARNING, "Less than 3/2/2004", "-", true, true, false);
		va.addValidation(OperatorType.GREATER_OR_EQUAL, "2004/03/02", null,       ErrorStyle.STOP, "Greater than or equal to 3/2/2004", "Error box type = STOP", true, false, true);
		va.addValidation(OperatorType.LESS_OR_EQUAL, "2004/03/04", null,       ErrorStyle.STOP, "Less than or equal to 3/4/2004", "-", false, true, false);

		// "Time" validation type
		wf.createDVTypeRow("Time ( cells are already formated as time - h:mm)");
		wf.createHeaderRow();

		va = wf.createValidationAdder(cellStyle_time, ValidationType.TIME);
		va.addValidation(OperatorType.BETWEEN,     "12:00", "16:00", ErrorStyle.STOP, "Between 12:00 and 16:00 ", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.NOT_BETWEEN, "12:00", "16:00", ErrorStyle.INFO, "Not between 12:00 and 16:00 ", "Error box type = INFO", false, true, true);
		va.addValidation(OperatorType.EQUAL,       "13:35", null,    ErrorStyle.WARNING, "Equal to 13:35", "Error box type = WARNING", false, false, true);
		va.addValidation(OperatorType.NOT_EQUAL,   "13:35", null,    ErrorStyle.WARNING, "Not equal to 13:35", "-", false, false, false);
		va.addValidation(OperatorType.GREATER_THAN,"12:00", null,    ErrorStyle.WARNING, "Greater than 12:00", "-", true, false, false);
		va.addValidation(OperatorType.LESS_THAN,   "=1/2", null,    ErrorStyle.WARNING, "Less than (1/2) -> 12:00", "-", true, true, false);
		va.addValidation(OperatorType.GREATER_OR_EQUAL, "14:00", null,    ErrorStyle.STOP, "Greater than or equal to 14:00", "Error box type = STOP", true, false, true);
		va.addValidation(OperatorType.LESS_OR_EQUAL,"14:00", null,    ErrorStyle.STOP, "Less than or equal to 14:00", "-", false, true, false);
	}

	private static void addTextLengthValidations(WorkbookFormatter wf) {
		wf.createSheet("Text lengths");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, ValidationType.TEXT_LENGTH);
		va.addValidation(OperatorType.BETWEEN, "2", "6", ErrorStyle.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OperatorType.NOT_BETWEEN, "2", "6", ErrorStyle.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OperatorType.EQUAL, "3", null, ErrorStyle.WARNING, "Equal to 3", "Error box type = WARNING", false, false, true);
		va.addValidation(OperatorType.NOT_EQUAL, "3", null, ErrorStyle.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OperatorType.GREATER_THAN, "3", null, ErrorStyle.WARNING, "Greater than 3", "-", true, false, false);
		va.addValidation(OperatorType.LESS_THAN, "3", null, ErrorStyle.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OperatorType.GREATER_OR_EQUAL, "4", null, ErrorStyle.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OperatorType.LESS_OR_EQUAL, "4", null, ErrorStyle.STOP, "Less than or equal to 4", "-", false, true, false);
	}

	@Test
	public void testDataValidation() throws Exception {
		log("\nTest no. 2 - Test Excel's Data validation mechanism");
		Workbook wb = _testDataProvider.createWorkbook();
		WorkbookFormatter wf = new WorkbookFormatter(wb);

		log("    Create sheet for Data Validation's number types ... ");
		addSimpleNumericValidations(wf);
		log("done !");

		log("    Create sheet for 'List' Data Validation type ... ");
		addListValidations(wf, wb);
		log("done !");

		log("    Create sheet for 'Date' and 'Time' Data Validation types ... ");
		addDateTimeValidations(wf, wb);
		log("done !");

		log("    Create sheet for 'Text length' Data Validation type... ");
		addTextLengthValidations(wf);
		log("done !");

		// Custom Validation type
		log("    Create sheet for 'Custom' Data Validation type ... ");
		addCustomValidations(wf);
		log("done !");

        _testDataProvider.writeOutAndReadBack(wb).close();
        
        wb.close();
	}



  /* package */ static void setCellValue(Cell cell, String text) {
	  cell.setCellValue(text);

      }

}