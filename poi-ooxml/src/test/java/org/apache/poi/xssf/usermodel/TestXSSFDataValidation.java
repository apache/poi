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
package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.formula.DataValidationEvaluator;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.BaseTestDataValidation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestXSSFDataValidation extends BaseTestDataValidation {

    public TestXSSFDataValidation(){
        super(XSSFITestDataProvider.instance);
    }

    @Test
    void testAddValidations() throws Exception {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("DataValidations-49244.xlsx");
        XSSFSheet sheet = wb1.getSheetAt(0);
        List<XSSFDataValidation> dataValidations = sheet.getDataValidations();

        // For each validation type, there are two cells with the same validation. This tests
        // application of a single validation definition to multiple cells.
        //
        // For list ( 3 validations for explicit and 3 for formula )
        // 	- one validation that allows blank.
        // 	- one that does not allow blank.
        // 	- one that does not show the drop down arrow.
        // = 2
        //
        // For number validations ( integer/decimal and text length ) with 8 different types of operators.
        // = 50
        //
        // = 52 ( Total )

        assertEquals(52,dataValidations.size());

        DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
        int[] validationTypes = new int[]{ValidationType.INTEGER,ValidationType.DECIMAL,ValidationType.TEXT_LENGTH};

        int[] singleOperandOperatorTypes = new int[]{
                OperatorType.LESS_THAN,OperatorType.LESS_OR_EQUAL,
                OperatorType.GREATER_THAN,OperatorType.GREATER_OR_EQUAL,
                OperatorType.EQUAL,OperatorType.NOT_EQUAL
                } ;
        int[] doubleOperandOperatorTypes = new int[]{
                OperatorType.BETWEEN,OperatorType.NOT_BETWEEN
        };

        BigDecimal value  = new BigDecimal("10"),value2 = new BigDecimal("20");
        BigDecimal dvalue = new BigDecimal("10.001"),dvalue2 = new BigDecimal("19.999");
        final int lastRow = sheet.getLastRowNum();
        int offset = lastRow + 3;

        int lastKnownNumValidations = dataValidations.size();

        Row row = sheet.createRow(offset++);
        Cell cell = row.createCell(0);
        DataValidationConstraint explicitListValidation = dataValidationHelper.createExplicitListConstraint(new String[]{"MA","MI","CA"});
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList();
        cellRangeAddressList.addCellRangeAddress(cell.getRowIndex(), cell.getColumnIndex(), cell.getRowIndex(), cell.getColumnIndex());
        DataValidation dataValidation = dataValidationHelper.createValidation(explicitListValidation, cellRangeAddressList);
        setOtherValidationParameters(dataValidation);
        sheet.addValidationData(dataValidation);
        lastKnownNumValidations++;

        row = sheet.createRow(offset++);
        cell = row.createCell(0);

        cellRangeAddressList = new CellRangeAddressList();
        cellRangeAddressList.addCellRangeAddress(cell.getRowIndex(), cell.getColumnIndex(), cell.getRowIndex(), cell.getColumnIndex());

        Cell firstCell =  row.createCell(1);firstCell.setCellValue("UT");
        Cell secondCell = row.createCell(2);secondCell.setCellValue("MN");
        Cell thirdCell  = row.createCell(3);thirdCell.setCellValue("IL");

        int rowNum = row.getRowNum() + 1;
        String listFormula = "$B$" + rowNum + ":" + "$D$" + rowNum;
        DataValidationConstraint formulaListValidation = dataValidationHelper.createFormulaListConstraint(listFormula);

        dataValidation = dataValidationHelper.createValidation(formulaListValidation, cellRangeAddressList);
        setOtherValidationParameters(dataValidation);
        sheet.addValidationData(dataValidation);
        lastKnownNumValidations++;

        offset++;
        offset++;

        for (int i = 0; i < validationTypes.length; i++) {
            int validationType = validationTypes[i];
            offset = offset + 2;
            final Row row0 = sheet.createRow(offset++);
            Cell cell_10 = row0.createCell(0);
            cell_10.setCellValue(validationType==ValidationType.DECIMAL ? "Decimal " : validationType==ValidationType.INTEGER ? "Integer" : "Text Length");
            offset++;
            for (int j = 0; j < singleOperandOperatorTypes.length; j++) {
                int operatorType = singleOperandOperatorTypes[j];
                final Row row1 = sheet.createRow(offset++);

                //For Integer (> and >=) we add 1 extra cell for validations whose formulae reference other cells.
                final Row row2 = i==0 && j < 2 ? sheet.createRow(offset++) : null;

                cell_10 = row1.createCell(0);
                cell_10.setCellValue(XSSFDataValidation.operatorTypeMappings.get(operatorType).toString());
                Cell cell_11 = row1.createCell(1);
                Cell cell_21 = row1.createCell(2);
                Cell cell_22 = i==0 && j < 2 ? (row2 == null ? null : row2.createCell(2)) : null;

                Cell cell_13 = row1.createCell(3);
                cell_13.setCellValue(validationType==ValidationType.DECIMAL ? dvalue.doubleValue() : value.intValue());


                //First create value based validation;
                DataValidationConstraint constraint = dataValidationHelper.createNumericConstraint(validationType,operatorType, value.toString(), null);
                cellRangeAddressList = new CellRangeAddressList();
                cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_11.getRowIndex(),cell_11.getRowIndex(),cell_11.getColumnIndex(),cell_11.getColumnIndex()));
                DataValidation validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                setOtherValidationParameters(validation);
                sheet.addValidationData(validation);
                assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());

                //Now create real formula based validation.
                String formula1 = new CellReference(cell_13.getRowIndex(),cell_13.getColumnIndex()).formatAsString();
                constraint = dataValidationHelper.createNumericConstraint(validationType,operatorType, formula1, null);
                if (i==0 && j==0) {
                    cellRangeAddressList = new CellRangeAddressList();
                    cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_21.getRowIndex(), cell_21.getRowIndex(), cell_21.getColumnIndex(), cell_21.getColumnIndex()));
                    validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                    setOtherValidationParameters(validation);
                    sheet.addValidationData(validation);
                    assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());

                    cellRangeAddressList = new CellRangeAddressList();
                    if (cell_22 != null) {
                        cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_22.getRowIndex(), cell_22.getRowIndex(), cell_22.getColumnIndex(), cell_22.getColumnIndex()));
                    }
                    validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                    setOtherValidationParameters( validation);
                    sheet.addValidationData(validation);
                    assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());
                } else if(i==0 && j==1 ){
                    cellRangeAddressList = new CellRangeAddressList();
                    cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_21.getRowIndex(), cell_21.getRowIndex(), cell_21.getColumnIndex(), cell_21.getColumnIndex()));
                    if (cell_22 != null) {
                        cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_22.getRowIndex(), cell_22.getRowIndex(), cell_22.getColumnIndex(), cell_22.getColumnIndex()));
                    }
                    validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                    setOtherValidationParameters( validation);
                    sheet.addValidationData(validation);
                    assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());
                } else {
                    cellRangeAddressList = new CellRangeAddressList();
                    cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_21.getRowIndex(), cell_21.getRowIndex(), cell_21.getColumnIndex(), cell_21.getColumnIndex()));
                    validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                    setOtherValidationParameters(validation);
                    sheet.addValidationData(validation);
                    assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());
                }
            }

            for (int operatorType : doubleOperandOperatorTypes) {
                final Row row1 = sheet.createRow(offset++);

                cell_10 = row1.createCell(0);
                cell_10.setCellValue(XSSFDataValidation.operatorTypeMappings.get(operatorType).toString());

                Cell cell_11 = row1.createCell(1);
                Cell cell_21 = row1.createCell(2);

                Cell cell_13 = row1.createCell(3);
                Cell cell_14 = row1.createCell(4);


                String value1String = validationType==ValidationType.DECIMAL ? dvalue.toString() : value.toString();
                cell_13.setCellValue(validationType==ValidationType.DECIMAL ? dvalue.doubleValue() : value.intValue());

                String value2String = validationType==ValidationType.DECIMAL ? dvalue2.toString() : value2.toString();
                cell_14.setCellValue(validationType==ValidationType.DECIMAL ? dvalue2.doubleValue() : value2.intValue());


                //First create value based validation;
                DataValidationConstraint constraint = dataValidationHelper.createNumericConstraint(validationType,operatorType, value1String, value2String);
                cellRangeAddressList = new CellRangeAddressList();
                cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_11.getRowIndex(),cell_11.getRowIndex(),cell_11.getColumnIndex(),cell_11.getColumnIndex()));
                DataValidation validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);
                setOtherValidationParameters(validation);
                sheet.addValidationData(validation);
                assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());


                //Now create real formula based validation.
                String formula1 = new CellReference(cell_13.getRowIndex(),cell_13.getColumnIndex()).formatAsString();
                String formula2 = new CellReference(cell_14.getRowIndex(),cell_14.getColumnIndex()).formatAsString();
                constraint = dataValidationHelper.createNumericConstraint(validationType,operatorType, formula1, formula2);
                cellRangeAddressList = new CellRangeAddressList();
                cellRangeAddressList.addCellRangeAddress(new CellRangeAddress(cell_21.getRowIndex(),cell_21.getRowIndex(),cell_21.getColumnIndex(),cell_21.getColumnIndex()));
                validation = dataValidationHelper.createValidation(constraint, cellRangeAddressList);

                setOtherValidationParameters(validation);
                sheet.addValidationData(validation);
                assertEquals(++lastKnownNumValidations, sheet.getDataValidations().size());
            }
        }

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        XSSFSheet sheetAt = wb2.getSheetAt(0);
        assertEquals(lastKnownNumValidations, sheetAt.getDataValidations().size());
        wb2.close();
    }

    protected void setOtherValidationParameters(DataValidation validation) {
        boolean yesNo = true;
        validation.setEmptyCellAllowed(yesNo);
        validation.setShowErrorBox(yesNo);
        validation.setShowPromptBox(yesNo);
        validation.createErrorBox("Error Message Title", "Error Message");
        validation.createPromptBox("Prompt", "Enter some value");
        validation.setSuppressDropDownArrow(yesNo);
    }

    @Test
    void test53965() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            List<XSSFDataValidation> lst = sheet.getDataValidations();    //<-- works
            assertEquals(0, lst.size());

            //create the cell that will have the validation applied
            sheet.createRow(0).createCell(0);

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dataValidationHelper.createCustomConstraint("SUM($A$1:$A$1) <= 3500");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dataValidationHelper.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            // this line caused XmlValueOutOfRangeException , see Bugzilla 3965
            lst = sheet.getDataValidations();
            assertEquals(1, lst.size());
        }
    }

    @Test
    void testDefaultErrorStyle() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            final XSSFDataValidation validation = createValidation(sheet);
            sheet.addValidationData(validation);

            final List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
            assertEquals(DataValidation.ErrorStyle.STOP, dataValidations.get(0).getErrorStyle());
        }
    }

    @Test
    void testSetErrorStyles() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            XSSFDataValidation validation = createValidation(sheet);
            sheet.addValidationData(validation);

            // extract generated validation from sheet
            List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
            validation = dataValidations.get(0);

            // test INFO
            validation.setErrorStyle(DataValidation.ErrorStyle.INFO);
            assertEquals(DataValidation.ErrorStyle.INFO, dataValidations.get(0).getErrorStyle());

            // test WARNING
            validation.setErrorStyle(DataValidation.ErrorStyle.WARNING);
            assertEquals(DataValidation.ErrorStyle.WARNING, dataValidations.get(0).getErrorStyle());

            // test STOP
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            assertEquals(DataValidation.ErrorStyle.STOP, dataValidations.get(0).getErrorStyle());
        }
    }

    @Test
    void testDefaultAllowBlank() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            final XSSFDataValidation validation = createValidation(sheet);
            sheet.addValidationData(validation);

            final List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
            assertTrue(dataValidations.get(0).getCtDataValidation().getAllowBlank());
        }
    }

    @Test
    void testSetAllowBlankToFalse() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            final XSSFDataValidation validation = createValidation(sheet);
            validation.getCtDataValidation().setAllowBlank(false);

            sheet.addValidationData(validation);

            final List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
            assertFalse(dataValidations.get(0).getCtDataValidation().getAllowBlank());
        }
    }

    @Test
    void testSetAllowBlankToTrue() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            final XSSFDataValidation validation = createValidation(sheet);
            validation.getCtDataValidation().setAllowBlank(true);

            sheet.addValidationData(validation);

            final List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
            assertTrue(dataValidations.get(0).getCtDataValidation().getAllowBlank());
        }
    }

    private XSSFDataValidation createValidation(XSSFSheet sheet) {
        //create the cell that will have the validation applied
        final Row row = sheet.createRow(0);
        row.createCell(0);

        DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();

        DataValidationConstraint constraint = dataValidationHelper.createCustomConstraint("true");
        return (XSSFDataValidation) dataValidationHelper.createValidation(constraint, new CellRangeAddressList(0, 0, 0, 0));
    }

    @Test
    void testTableBasedValidationList() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("dataValidationTableRange.xlsx")) {
            XSSFFormulaEvaluator fEval = wb.getCreationHelper().createFormulaEvaluator();
            DataValidationEvaluator dve = new DataValidationEvaluator(wb, fEval);
            List<ValueEval> values = dve.getValidationValuesForCell(new CellReference("County Ranking", 8, 6, false, false));
            assertEquals(32, values.size(), "wrong # of valid values");
        }
    }
}
