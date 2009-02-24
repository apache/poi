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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventmodel.ERFListener;
import org.apache.poi.hssf.eventmodel.EventRecordFactory;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * Class for testing Excel's data validation mechanism
 *
 * @author Dragos Buleandra ( dragos.buleandra@trade2b.ro )
 */
public final class TestDataValidation extends TestCase {

	/** Convenient access to ERROR_STYLE constants */
	/*package*/ static final HSSFDataValidation.ErrorStyle ES = null;
	/** Convenient access to OPERATOR constants */
	/*package*/ static final DVConstraint.ValidationType VT = null;
	/** Convenient access to OPERATOR constants */
	/*package*/ static final DVConstraint.OperatorType OP = null;

	private static void log(String msg) {
		if (false) { // successful tests should be silent
			System.out.println(msg);
		}      
	}
  
	private static final class ValidationAdder {
	  
		private final HSSFCellStyle _style_1;
		private final HSSFCellStyle _style_2;
		private  final int _validationType;
		private final HSSFSheet _sheet;
		private int _currentRowIndex;
		private final HSSFCellStyle _cellStyle;

		public ValidationAdder(HSSFSheet fSheet, HSSFCellStyle style_1, HSSFCellStyle style_2,
				HSSFCellStyle cellStyle, int validationType) {
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

			DVConstraint dc = createConstraint(operatorType, firstFormula, secondFormula, explicitListValues);

			HSSFDataValidation dv = new HSSFDataValidation(new CellRangeAddressList(rowNum, rowNum, 0, 0), dc);
			
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
				HSSFRow row = _sheet.getRow(_sheet.getPhysicalNumberOfRows() - 1);
				HSSFCell cell = row.createCell(0);
				cell.setCellStyle(_cellStyle);
			}
			writeOtherSettings(_sheet, _style_1, promptDescr);
		}
		private DVConstraint createConstraint(int operatorType, String firstFormula,
				String secondFormula, String[] explicitListValues) {
			if (_validationType == VT.LIST) {
				if (explicitListValues != null) {
					return DVConstraint.createExplicitListConstraint(explicitListValues);
				}
				return DVConstraint.createFormulaListConstraint(firstFormula);
			}
			if (_validationType == VT.TIME) {
				return DVConstraint.createTimeConstraint(operatorType, firstFormula, secondFormula);
			}
			if (_validationType == VT.DATE) {
				return DVConstraint.createDateConstraint(operatorType, firstFormula, secondFormula, null);
			}
			if (_validationType == VT.FORMULA) {
				return DVConstraint.createCustomFormulaConstraint(firstFormula);
			}
			return DVConstraint.createNumericConstraint(_validationType, operatorType, firstFormula, secondFormula);
		}
		/**
		 * writes plain text values into cells in a tabular format to form comments readable from within 
		 * the spreadsheet.
		 */
		private static void writeDataValidationSettings(HSSFSheet sheet, HSSFCellStyle style_1,
				HSSFCellStyle style_2, String strCondition, boolean allowEmpty, boolean inputBox,
				boolean errorBox) {
			HSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			// condition's string
			HSSFCell cell = row.createCell(1);
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
		private static void writeOtherSettings(HSSFSheet sheet, HSSFCellStyle style,
				String strStettings) {
			HSSFRow row = sheet.getRow(sheet.getPhysicalNumberOfRows() - 1);
			HSSFCell cell = row.createCell(5);
			cell.setCellStyle(style);
			setCellValue(cell, strStettings);
		}
		public void addListValidation(String[] explicitListValues, String listFormula, String listValsDescr,
				boolean allowEmpty, boolean suppressDropDown) {
			String promptDescr = (allowEmpty ? "empty ok" : "not empty") 
					+ ", " + (suppressDropDown ? "no drop-down" : "drop-down"); 
			addValidationInternal(VT.LIST, listFormula, null, ES.STOP, listValsDescr, promptDescr, 
					allowEmpty, false, true, suppressDropDown, explicitListValues);
		}
	}

	/**
	 * Manages the cell styles used for formatting the output spreadsheet
	 */
	private static final class WorkbookFormatter {

		private final HSSFWorkbook _wb;
		private final HSSFCellStyle _style_1;
		private final HSSFCellStyle _style_2;
		private final HSSFCellStyle _style_3;
		private final HSSFCellStyle _style_4;
		private HSSFSheet _currentSheet;

		public WorkbookFormatter(HSSFWorkbook wb) {
			_wb = wb;
			_style_1 = createStyle( wb, HSSFCellStyle.ALIGN_LEFT );
			_style_2 = createStyle( wb, HSSFCellStyle.ALIGN_CENTER );
			_style_3 = createStyle( wb, HSSFCellStyle.ALIGN_CENTER, HSSFColor.GREY_25_PERCENT.index, true );
			_style_4 = createHeaderStyle(wb);
		}
		
		private static HSSFCellStyle createStyle(HSSFWorkbook wb, short h_align, short color,
				boolean bold) {
			HSSFFont font = wb.createFont();
			if (bold) {
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			}

			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFont(font);
			cellStyle.setFillForegroundColor(color);
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			cellStyle.setAlignment(h_align);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

			return cellStyle;
		}

		private static HSSFCellStyle createStyle(HSSFWorkbook wb, short h_align) {
			return createStyle(wb, h_align, HSSFColor.WHITE.index, false);
		}
		private static HSSFCellStyle createHeaderStyle(HSSFWorkbook wb) {
			HSSFFont font = wb.createFont();
			font.setColor( HSSFColor.WHITE.index );
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			cellStyle.setLeftBorderColor(HSSFColor.WHITE.index);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			cellStyle.setTopBorderColor(HSSFColor.WHITE.index);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			cellStyle.setRightBorderColor(HSSFColor.WHITE.index);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBottomBorderColor(HSSFColor.WHITE.index);
			cellStyle.setFont(font);
			return cellStyle;
		}
		

		public HSSFSheet createSheet(String sheetName) {
			_currentSheet = _wb.createSheet(sheetName);
			return _currentSheet;
		}
		public void createDVTypeRow(String strTypeDescription) {
			HSSFSheet sheet = _currentSheet;
			HSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			sheet.addMergedRegion(new CellRangeAddress(sheet.getPhysicalNumberOfRows()-1, sheet.getPhysicalNumberOfRows()-1, 0, 5));
			HSSFCell cell = row.createCell(0);
			setCellValue(cell, strTypeDescription);
			cell.setCellStyle(_style_3);
			row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		}
		
		public void createHeaderRow() {
			HSSFSheet sheet = _currentSheet;
			HSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
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
			HSSFCell cell = row.getCell(0);
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

		public ValidationAdder createValidationAdder(HSSFCellStyle cellStyle, int dataValidationType) {
			return new ValidationAdder(_currentSheet, _style_1, _style_2, cellStyle, dataValidationType);
		}

		public void createDVDescriptionRow(String strTypeDescription) {
			HSSFSheet sheet = _currentSheet;
			HSSFRow row = sheet.getRow(sheet.getPhysicalNumberOfRows()-1);
			sheet.addMergedRegion(new CellRangeAddress(sheet.getPhysicalNumberOfRows()-1, sheet.getPhysicalNumberOfRows()-1, 0, 5));
			HSSFCell cell = row.createCell(0);
			setCellValue(cell, strTypeDescription);
			cell.setCellStyle(_style_3);
			row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		}
	}
	
  
	private void addCustomValidations(WorkbookFormatter wf) {
		wf.createSheet("Custom");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, VT.FORMULA);
		va.addValidation(OP.BETWEEN, "ISNUMBER($A2)", null, ES.STOP, "ISNUMBER(A2)", "Error box type = STOP", true, true, true);
		va.addValidation(OP.BETWEEN, "IF(SUM(A2:A3)=5,TRUE,FALSE)", null, ES.WARNING, "IF(SUM(A2:A3)=5,TRUE,FALSE)", "Error box type = WARNING", false, false, true);
	}

	private static void addSimpleNumericValidations(WorkbookFormatter wf) {
		// data validation's number types
		wf.createSheet("Numbers");

		// "Whole number" validation type
		wf.createDVTypeRow("Whole number");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, VT.INTEGER);
		va.addValidation(OP.BETWEEN, "2", "6", ES.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OP.NOT_BETWEEN, "2", "6", ES.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OP.EQUAL, "=3+2", null, ES.WARNING, "Equal to (3+2)", "Error box type = WARNING", false, false, true);
		va.addValidation(OP.NOT_EQUAL, "3", null, ES.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OP.GREATER_THAN, "3", null, ES.WARNING, "Greater than 3", "-", true, false, false);
		va.addValidation(OP.LESS_THAN, "3", null, ES.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OP.GREATER_OR_EQUAL, "4", null, ES.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OP.LESS_OR_EQUAL, "4", null, ES.STOP, "Less than or equal to 4", "-", false, true, false);

		// "Decimal" validation type
		wf.createDVTypeRow("Decimal");
		wf.createHeaderRow();

		va = wf.createValidationAdder(null, VT.DECIMAL);
		va.addValidation(OP.BETWEEN, "2", "6", ES.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OP.NOT_BETWEEN, "2", "6", ES.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OP.EQUAL, "3", null, ES.WARNING, "Equal to 3", "Error box type = WARNING", false, false, true);
		va.addValidation(OP.NOT_EQUAL, "3", null, ES.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OP.GREATER_THAN, "=12/6", null, ES.WARNING, "Greater than (12/6)", "-", true, false, false);
		va.addValidation(OP.LESS_THAN, "3", null, ES.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OP.GREATER_OR_EQUAL, "4", null, ES.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OP.LESS_OR_EQUAL, "4", null, ES.STOP, "Less than or equal to 4", "-", false, true, false);
	}
	
	private static void addListValidations(WorkbookFormatter wf, HSSFWorkbook wb) {
		final String cellStrValue 
		 = "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "
		+ "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 ";
		final String dataSheetName = "list_data";
		// "List" Data Validation type
		HSSFSheet fSheet = wf.createSheet("Lists");
		HSSFSheet dataSheet = wb.createSheet(dataSheetName);


		wf.createDVTypeRow("Explicit lists - list items are explicitly provided");
		wf.createDVDescriptionRow("Disadvantage - sum of item's length should be less than 255 characters");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, VT.LIST);
		String listValsDescr = "POIFS,HSSF,HWPF,HPSF";
		String[] listVals = listValsDescr.split(",");
		va.addListValidation(listVals, null, listValsDescr, false, false);
		va.addListValidation(listVals, null, listValsDescr, false, true);
		va.addListValidation(listVals, null, listValsDescr, true, false);
		va.addListValidation(listVals, null, listValsDescr, true, true);
		
		
		
		wf.createDVTypeRow("Reference lists - list items are taken from others cells");
		wf.createDVDescriptionRow("Advantage - no restriction regarding the sum of item's length");
		wf.createHeaderRow();
		va = wf.createValidationAdder(null, VT.LIST);
		String strFormula = "$A$30:$A$39";
		va.addListValidation(null, strFormula, strFormula, false, false);
		
		strFormula = dataSheetName + "!$A$1:$A$10";
		va.addListValidation(null, strFormula, strFormula, false, false);
		HSSFName namedRange = wb.createName();
		namedRange.setNameName("myName");
		namedRange.setRefersToFormula(dataSheetName + "!$A$2:$A$7");
		strFormula = "myName";
		va.addListValidation(null, strFormula, strFormula, false, false);
		strFormula = "offset(myName, 2, 1, 4, 2)"; // Note about last param '2': 
		// - Excel expects single row or single column when entered in UI, but process this OK otherwise
		va.addListValidation(null, strFormula, strFormula, false, false);
		
		// add list data on same sheet
		for (int i = 0; i < 10; i++) {
			HSSFRow currRow = fSheet.createRow(i + 29);
			setCellValue(currRow.createCell(0), cellStrValue);
		}
		// add list data on another sheet
		for (int i = 0; i < 10; i++) {
			HSSFRow currRow = dataSheet.createRow(i + 0);
			setCellValue(currRow.createCell(0), "Data a" + i);
			setCellValue(currRow.createCell(1), "Data b" + i);
			setCellValue(currRow.createCell(2), "Data c" + i);
		}
	}

	private static void addDateTimeValidations(WorkbookFormatter wf, HSSFWorkbook wb) {
		wf.createSheet("Dates and Times");

		HSSFDataFormat dataFormat = wb.createDataFormat();
		short fmtDate = dataFormat.getFormat("m/d/yyyy");
		short fmtTime = dataFormat.getFormat("h:mm");
		HSSFCellStyle cellStyle_date = wb.createCellStyle();
		cellStyle_date.setDataFormat(fmtDate);
		HSSFCellStyle cellStyle_time = wb.createCellStyle();
		cellStyle_time.setDataFormat(fmtTime);

		wf.createDVTypeRow("Date ( cells are already formated as date - m/d/yyyy)");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(cellStyle_date, VT.DATE);
		va.addValidation(OP.BETWEEN,     "2004/01/02", "2004/01/06", ES.STOP, "Between 1/2/2004 and 1/6/2004 ", "Error box type = STOP", true, true, true);
		va.addValidation(OP.NOT_BETWEEN, "2004/01/01", "2004/01/06", ES.INFO, "Not between 1/2/2004 and 1/6/2004 ", "Error box type = INFO", false, true, true);
		va.addValidation(OP.EQUAL,       "2004/03/02", null,       ES.WARNING, "Equal to 3/2/2004", "Error box type = WARNING", false, false, true);
		va.addValidation(OP.NOT_EQUAL,   "2004/03/02", null,       ES.WARNING, "Not equal to 3/2/2004", "-", false, false, false);
		va.addValidation(OP.GREATER_THAN,"=DATEVALUE(\"4-Jul-2001\")", null,       ES.WARNING, "Greater than DATEVALUE('4-Jul-2001')", "-", true, false, false);
		va.addValidation(OP.LESS_THAN,   "2004/03/02", null,       ES.WARNING, "Less than 3/2/2004", "-", true, true, false);
		va.addValidation(OP.GREATER_OR_EQUAL, "2004/03/02", null,       ES.STOP, "Greater than or equal to 3/2/2004", "Error box type = STOP", true, false, true);
		va.addValidation(OP.LESS_OR_EQUAL, "2004/03/04", null,       ES.STOP, "Less than or equal to 3/4/2004", "-", false, true, false);

		// "Time" validation type
		wf.createDVTypeRow("Time ( cells are already formated as time - h:mm)");
		wf.createHeaderRow();

		va = wf.createValidationAdder(cellStyle_time, VT.TIME);
		va.addValidation(OP.BETWEEN,     "12:00", "16:00", ES.STOP, "Between 12:00 and 16:00 ", "Error box type = STOP", true, true, true);
		va.addValidation(OP.NOT_BETWEEN, "12:00", "16:00", ES.INFO, "Not between 12:00 and 16:00 ", "Error box type = INFO", false, true, true);
		va.addValidation(OP.EQUAL,       "13:35", null,    ES.WARNING, "Equal to 13:35", "Error box type = WARNING", false, false, true);
		va.addValidation(OP.NOT_EQUAL,   "13:35", null,    ES.WARNING, "Not equal to 13:35", "-", false, false, false);
		va.addValidation(OP.GREATER_THAN,"12:00", null,    ES.WARNING, "Greater than 12:00", "-", true, false, false);
		va.addValidation(OP.LESS_THAN,   "=1/2", null,    ES.WARNING, "Less than (1/2) -> 12:00", "-", true, true, false);
		va.addValidation(OP.GREATER_OR_EQUAL, "14:00", null,    ES.STOP, "Greater than or equal to 14:00", "Error box type = STOP", true, false, true);
		va.addValidation(OP.LESS_OR_EQUAL,"14:00", null,    ES.STOP, "Less than or equal to 14:00", "-", false, true, false);
	}

	private static void addTextLengthValidations(WorkbookFormatter wf) {
		wf.createSheet("Text lengths");
		wf.createHeaderRow();

		ValidationAdder va = wf.createValidationAdder(null, VT.TEXT_LENGTH);
		va.addValidation(OP.BETWEEN, "2", "6", ES.STOP, "Between 2 and 6 ", "Error box type = STOP", true, true, true);
		va.addValidation(OP.NOT_BETWEEN, "2", "6", ES.INFO, "Not between 2 and 6 ", "Error box type = INFO", false, true, true);
		va.addValidation(OP.EQUAL, "3", null, ES.WARNING, "Equal to 3", "Error box type = WARNING", false, false, true);
		va.addValidation(OP.NOT_EQUAL, "3", null, ES.WARNING, "Not equal to 3", "-", false, false, false);
		va.addValidation(OP.GREATER_THAN, "3", null, ES.WARNING, "Greater than 3", "-", true, false, false);
		va.addValidation(OP.LESS_THAN, "3", null, ES.WARNING, "Less than 3", "-", true, true, false);
		va.addValidation(OP.GREATER_OR_EQUAL, "4", null, ES.STOP, "Greater than or equal to 4", "Error box type = STOP", true, false, true);
		va.addValidation(OP.LESS_OR_EQUAL, "4", null, ES.STOP, "Less than or equal to 4", "-", false, true, false);
	}
	
	public void testDataValidation() {
		log("\nTest no. 2 - Test Excel's Data validation mechanism");
		HSSFWorkbook wb = new HSSFWorkbook();
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

		ByteArrayOutputStream baos = new ByteArrayOutputStream(22000);
		try {
			wb.write(baos);
			baos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] generatedContent = baos.toByteArray();
		boolean isSame;
		if (false) {
			// TODO - add proof spreadsheet and compare
			InputStream proofStream = HSSFTestDataSamples.openSampleFileStream("TestDataValidation.xls");
			isSame = compareStreams(proofStream, generatedContent);
		}
		isSame = true;
		
		if (isSame) {
			return;
		}
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File generatedFile = new File(tempDir, "GeneratedTestDataValidation.xls");
		try {
			FileOutputStream fileOut = new FileOutputStream(generatedFile);
			fileOut.write(generatedContent);
			fileOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		PrintStream ps = System.out;
	
		ps.println("This test case has failed because the generated file differs from proof copy '" 
				); // TODO+ proofFile.getAbsolutePath() + "'.");
		ps.println("The cause is usually a change to this test, or some common spreadsheet generation code.  "
				+ "The developer has to decide whether the changes were wanted or unwanted.");
		ps.println("If the changes to the generated version were unwanted, "
				+ "make the fix elsewhere (do not modify this test or the proof spreadsheet to get the test working).");
		ps.println("If the changes were wanted, make sure to open the newly generated file in Excel "
				+ "and verify it manually.  The new proof file should be submitted after it is verified to be correct.");
		ps.println("");
		ps.println("One other possible (but less likely) cause of a failed test is a problem in the "
				+ "comparison logic used here. Perhaps some extra file regions need to be ignored.");
		ps.println("The generated file has been saved to '" + generatedFile.getAbsolutePath() + "' for manual inspection.");
	
		fail("Generated file differs from proof copy.  See sysout comments for details on how to fix.");
		
	}
	
	private static boolean compareStreams(InputStream isA, byte[] generatedContent) {

		InputStream isB = new ByteArrayInputStream(generatedContent);

		// The allowable regions where the generated file can differ from the 
		// proof should be small (i.e. much less than 1K)
		int[] allowableDifferenceRegions = { 
				0x0228, 16,  // a region of the file containing the OS username
				0x506C, 8,   // See RootProperty (super fields _seconds_2 and _days_2)
		};
		int[] diffs = StreamUtility.diffStreams(isA, isB, allowableDifferenceRegions);
		if (diffs == null) {
			return true;
		}
		System.err.println("Diff from proof: ");
		for (int i = 0; i < diffs.length; i++) {
			System.err.println("diff at offset: 0x" + Integer.toHexString(diffs[i]));
		}
		return false;
	}
  




  /* package */ static void setCellValue(HSSFCell cell, String text) {
	  cell.setCellValue(new HSSFRichTextString(text));
	  
  }
  
	public void testAddToExistingSheet() {

		// dvEmpty.xls is a simple one sheet workbook.  With a DataValidations header record but no 
		// DataValidations.  It's important that the example has one SHEETPROTECTION record.
		// Such a workbook can be created in Excel (2007) by adding datavalidation for one cell
		// and then deleting the row that contains the cell.
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("dvEmpty.xls");  
		int dvRow = 0;
		HSSFSheet sheet = wb.getSheetAt(0);
		DVConstraint dc = DVConstraint.createNumericConstraint(VT.INTEGER, OP.EQUAL, "42", null);
		HSSFDataValidation dv = new HSSFDataValidation(new CellRangeAddressList(dvRow, dvRow, 0, 0), dc);
		
		dv.setEmptyCellAllowed(false);
		dv.setErrorStyle(ES.STOP);
		dv.setShowPromptBox(true);
		dv.createErrorBox("Xxx", "Yyy");
		dv.setSuppressDropDownArrow(true);

		sheet.addValidationData(dv);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		byte[] wbData = baos.toByteArray();
		
		if (false) { // TODO (Jul 2008) fix EventRecordFactory to process unknown records, (and DV records for that matter)

			ERFListener erfListener = null; // new MyERFListener();
			EventRecordFactory erf = new EventRecordFactory(erfListener, null);
			try {
				POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
				erf.processRecords(fs.createDocumentInputStream("Workbook"));
			} catch (RecordFormatException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		// else verify record ordering by navigating the raw bytes
		
		byte[] dvHeaderRecStart= { (byte)0xB2, 0x01, 0x12, 0x00, };
		int dvHeaderOffset = findIndex(wbData, dvHeaderRecStart);
		assertTrue(dvHeaderOffset > 0);
		int nextRecIndex = dvHeaderOffset + 22;
		int nextSid 
			= ((wbData[nextRecIndex + 0] << 0) & 0x00FF) 
			+ ((wbData[nextRecIndex + 1] << 8) & 0xFF00)
			;
		// nextSid should be for a DVRecord.  If anything comes between the DV header record 
		// and the DV records, Excel will not be able to open the workbook without error.
		
		if (nextSid == 0x0867) {
			throw new AssertionFailedError("Identified bug 45519");
		}
		assertEquals(DVRecord.sid, nextSid);
	}
	private int findIndex(byte[] largeData, byte[] searchPattern) {
		byte firstByte = searchPattern[0];
		for (int i = 0; i < largeData.length; i++) {
			if(largeData[i] != firstByte) {
				continue;
			}
			boolean match = true;
			for (int j = 1; j < searchPattern.length; j++) {
				if(searchPattern[j] != largeData[i+j]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}
}
