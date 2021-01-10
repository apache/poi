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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.ss.usermodel.BaseTestDataValidation;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidation.ErrorStyle;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.junit.jupiter.api.Test;

/**
 * Class for testing Excel's data validation mechanism
 *
 * @author Dragos Buleandra ( dragos.buleandra@trade2b.ro )
 */
final class TestDataValidation extends BaseTestDataValidation {

    public TestDataValidation(){
        super(HSSFITestDataProvider.instance);
    }


	void assertDataValidation(Workbook wb) {

        byte[] generatedContent;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(22000)) {
			wb.write(baos);
            generatedContent = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		boolean isSame;
//		if (false) {
//			// TODO - add proof spreadsheet and compare
//			InputStream proofStream = HSSFTestDataSamples.openSampleFileStream("TestDataValidation.xls");
//			isSame = compareStreams(proofStream, generatedContent);
//		}
		isSame = true;

		if (isSame) {
			return;
		}
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File generatedFile = new File(tempDir, "GeneratedTestDataValidation.xls");
		try (FileOutputStream fileOut = new FileOutputStream(generatedFile)) {
			fileOut.write(generatedContent);
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
		ps.println();
		ps.println("One other possible (but less likely) cause of a failed test is a problem in the "
				+ "comparison logic used here. Perhaps some extra file regions need to be ignored.");
		ps.println("The generated file has been saved to '" + generatedFile.getAbsolutePath() + "' for manual inspection.");

		fail("Generated file differs from proof copy.  See sysout comments for details on how to fix.");

	}

    /* package */ static void setCellValue(HSSFCell cell, String text) {
	  cell.setCellValue(new HSSFRichTextString(text));

    }

	@Test
    void testAddToExistingSheet() throws Exception {

		// dvEmpty.xls is a simple one sheet workbook.  With a DataValidations header record but no
		// DataValidations.  It's important that the example has one SHEETPROTECTION record.
		// Such a workbook can be created in Excel (2007) by adding datavalidation for one cell
		// and then deleting the row that contains the cell.
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("dvEmpty.xls")) {
            int dvRow = 0;
            Sheet sheet = wb.getSheetAt(0);
            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint dc = dataValidationHelper.createIntegerConstraint(OperatorType.EQUAL, "42", null);
            DataValidation dv = dataValidationHelper.createValidation(dc, new CellRangeAddressList(dvRow, dvRow, 0, 0));

            dv.setEmptyCellAllowed(false);
            dv.setErrorStyle(ErrorStyle.STOP);
            dv.setShowPromptBox(true);
            dv.createErrorBox("Xxx", "Yyy");
            dv.setSuppressDropDownArrow(true);

            sheet.addValidationData(dv);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);

            byte[] wbData = baos.toByteArray();

//		if (false) { // TODO (Jul 2008) fix EventRecordFactory to process unknown records, (and DV records for that matter)
//
//			ERFListener erfListener = null; // new MyERFListener();
//			EventRecordFactory erf = new EventRecordFactory(erfListener, null);
//			try {
//				POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
//				erf.processRecords(fs.createDocumentInputStream("Workbook"));
//			} catch (RecordFormatException e) {
//				throw new RuntimeException(e);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
            // else verify record ordering by navigating the raw bytes

            byte[] dvHeaderRecStart = {(byte) 0xB2, 0x01, 0x12, 0x00,};
            int dvHeaderOffset = findIndex(wbData, dvHeaderRecStart);
            assertTrue(dvHeaderOffset > 0);
            int nextRecIndex = dvHeaderOffset + 22;
            int nextSid
                    = ((wbData[nextRecIndex + 0] << 0) & 0x00FF)
                    + ((wbData[nextRecIndex + 1] << 8) & 0xFF00);
            // nextSid should be for a DVRecord.  If anything comes between the DV header record
            // and the DV records, Excel will not be able to open the workbook without error.

            assertNotEquals(0x0867, nextSid, "Identified bug 45519");
            assertEquals(DVRecord.sid, nextSid);
        }
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

	@Test
    void testGetDataValidationsAny() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createNumericConstraint(ValidationType.ANY, OperatorType.IGNORED, null, null);
            CellRangeAddressList addressList = new CellRangeAddressList(1, 2, 3, 4);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            validation.setEmptyCellAllowed(true);
            validation.createErrorBox("error-title", "error-text");
            validation.createPromptBox("prompt-title", "prompt-text");
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            {
                CellRangeAddressList regions = dv.getRegions();
                assertEquals(1, regions.countRanges());

                CellRangeAddress address = regions.getCellRangeAddress(0);
                assertEquals(1, address.getFirstRow());
                assertEquals(2, address.getLastRow());
                assertEquals(3, address.getFirstColumn());
                assertEquals(4, address.getLastColumn());
            }
            assertTrue(dv.getEmptyCellAllowed());
            assertFalse(dv.getSuppressDropDownArrow());
            assertTrue(dv.getShowErrorBox());
            assertEquals("error-title", dv.getErrorBoxTitle());
            assertEquals("error-text", dv.getErrorBoxText());
            assertTrue(dv.getShowPromptBox());
            assertEquals("prompt-title", dv.getPromptBoxTitle());
            assertEquals("prompt-text", dv.getPromptBoxText());

            DataValidationConstraint c = dv.getValidationConstraint();
            assertEquals(ValidationType.ANY, c.getValidationType());
            assertEquals(OperatorType.IGNORED, c.getOperator());
        }
    }

	@Test
    void testGetDataValidationsIntegerFormula() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createIntegerConstraint(OperatorType.BETWEEN, "=A2", "=A3");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.INTEGER, c.getValidationType());
            assertEquals(OperatorType.BETWEEN, c.getOperator());
            assertEquals("A2", c.getFormula1());
            assertEquals("A3", c.getFormula2());
            assertNull(c.getValue1());
            assertNull(c.getValue2());
        }
    }

	@Test
    void testGetDataValidationsIntegerValue() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createIntegerConstraint(OperatorType.BETWEEN, "100", "200");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.INTEGER, c.getValidationType());
            assertEquals(OperatorType.BETWEEN, c.getOperator());
            assertNull(c.getFormula1());
            assertNull(c.getFormula2());
            assertEquals(100d, c.getValue1(), 0);
            assertEquals(200d, c.getValue2(), 0);
        }
    }

	@Test
    void testGetDataValidationsDecimal() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createDecimalConstraint(OperatorType.BETWEEN, "=A2", "200");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.DECIMAL, c.getValidationType());
            assertEquals(OperatorType.BETWEEN, c.getOperator());
            assertEquals("A2", c.getFormula1());
            assertNull(c.getFormula2());
            assertNull(c.getValue1());
            assertEquals(200, c.getValue2(), 0);
        }
    }

	@Test
    void testGetDataValidationsDate() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createDateConstraint(OperatorType.EQUAL, "2014/10/25", null, null);
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.DATE, c.getValidationType());
            assertEquals(OperatorType.EQUAL, c.getOperator());
            assertNull(c.getFormula1());
            assertNull(c.getFormula2());
            assertEquals(DateUtil.getExcelDate(DateUtil.parseYYYYMMDDDate("2014/10/25")), c.getValue1(), 0);
            assertNull(c.getValue2());
        }
    }

	@Test
    void testGetDataValidationsListExplicit() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvh.createExplicitListConstraint(new String[]{"aaa", "bbb", "ccc"});
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dvh.createValidation(constraint, addressList);
            validation.setSuppressDropDownArrow(true);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            assertTrue(dv.getSuppressDropDownArrow());

            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.LIST, c.getValidationType());
            assertNull(c.getFormula1());
            assertNull(c.getFormula2());
            assertNull(c.getValue1());
            assertNull(c.getValue2());
            String[] values = c.getExplicitListValues();
            assertEquals(3, values.length);
            assertEquals("aaa", values[0]);
            assertEquals("bbb", values[1]);
            assertEquals("ccc", values[2]);
        }
    }

	@Test
    void testGetDataValidationsListFormula() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dataValidationHelper.createFormulaListConstraint("A2");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dataValidationHelper.createValidation(constraint, addressList);
            validation.setSuppressDropDownArrow(true);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            assertTrue(dv.getSuppressDropDownArrow());

            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.LIST, c.getValidationType());
            assertEquals("A2", c.getFormula1());
            assertNull(c.getFormula2());
            assertNull(c.getValue1());
            assertNull(c.getValue2());
        }
    }

	@Test
    void testGetDataValidationsFormula() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            List<HSSFDataValidation> list = sheet.getDataValidations();
            assertEquals(0, list.size());

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dataValidationHelper.createCustomConstraint("A2:A3");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dataValidationHelper.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            list = sheet.getDataValidations(); // <-- works
            assertEquals(1, list.size());

            HSSFDataValidation dv = list.get(0);
            DVConstraint c = dv.getConstraint();
            assertEquals(ValidationType.FORMULA, c.getValidationType());
            assertEquals("A2:A3", c.getFormula1());
            assertNull(c.getFormula2());
            assertNull(c.getValue1());
            assertNull(c.getValue2());
        }
    }
}
