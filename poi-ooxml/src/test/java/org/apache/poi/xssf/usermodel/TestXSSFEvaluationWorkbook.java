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

import java.io.IOException;

import org.apache.poi.ss.formula.NameIdentifier;
import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestXSSFEvaluationWorkbook {

    @Test
    void testRefToBlankCellInArrayFormula() {
        Workbook wb = new XSSFWorkbook();

        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        verifySheet(wb, formulaEvaluator);

        verifySheet(wb, formulaEvaluator);

        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
    }

    private void verifySheet(Workbook wb, FormulaEvaluator formulaEvaluator) {
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cellA1 = row.createCell(0);
        Cell cellB1 = row.createCell(1);
        Cell cellC1 = row.createCell(2);
        Row row2 = sheet.createRow(1);
        Cell cellA2 = row2.createCell(0);
        Cell cellB2 = row2.createCell(1);
        Cell cellC2 = row2.createCell(2);
        Row row3 = sheet.createRow(2);
        Cell cellA3 = row3.createCell(0);
        Cell cellB3 = row3.createCell(1);
        Cell cellC3 = row3.createCell(2);

        cellA1.setCellValue("1");
        // cell B1 intentionally left blank
        cellC1.setCellValue("3");

        cellA2.setCellFormula("A1");
        cellB2.setCellFormula("B1");
        cellC2.setCellFormula("C1");

        sheet.setArrayFormula("A1:C1", CellRangeAddress.valueOf("A3:C3"));

        formulaEvaluator.evaluateAll();

        assertEquals("1", cellA2.getStringCellValue());
        assertEquals(0,cellB2.getNumericCellValue(), 0.00001);
        assertEquals("3",cellC2.getStringCellValue());

        assertEquals("1", cellA3.getStringCellValue());
        assertEquals(0,cellB3.getNumericCellValue(), 0.00001);
        assertEquals("3",cellC3.getStringCellValue());
    }

    @Test
    void testResolveBookIndexWithBracketedName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx")) {
            XSSFEvaluationWorkbook ewb = XSSFEvaluationWorkbook.create(wb);
            SheetIdentifier sheet = new SheetIdentifier("[56737.xlsx]", new NameIdentifier("Uses", false));

            Ref3DPxg ptg = (Ref3DPxg) ewb.get3DReferencePtg(new CellReference("A1"), sheet);
            // 1 based results, 0 = current workbook
            assertEquals(1, ptg.getExternalWorkbookNumber());
        }
    }

    @Test
    void testResolveBookIndexForQuotedFileReference() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFEvaluationWorkbook ewb = XSSFEvaluationWorkbook.create(wb);
            SheetIdentifier sheet = new SheetIdentifier("'file:///C:/temp/Book1.xlsx'",
                    new NameIdentifier("Sheet1", false));

            // no link table for this file yet, so a placeholder one is added
            Ref3DPxg ptg = (Ref3DPxg) ewb.get3DReferencePtg(new CellReference("A1"), sheet);
            assertEquals(1, ptg.getExternalWorkbookNumber());
            assertEquals(1, wb.getExternalLinksTables().size());
            assertEquals("Book1.xlsx", wb.getExternalLinksTable(0).getLinkedFileName());

            // asking again finds the placeholder rather than adding a second one
            ptg = (Ref3DPxg) ewb.get3DReferencePtg(new CellReference("A1"), sheet);
            assertEquals(1, ptg.getExternalWorkbookNumber());
            assertEquals(1, wb.getExternalLinksTables().size());
        }
    }

    @Test
    void testResolveBookIndexForUnquotedFileReference() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFEvaluationWorkbook ewb = XSSFEvaluationWorkbook.create(wb);
            // the formula parser leaves the quotes in place, but we shouldn't depend on that
            SheetIdentifier sheet = new SheetIdentifier("file:///C:/temp/Book1.xlsx",
                    new NameIdentifier("Sheet1", false));

            Ref3DPxg ptg = (Ref3DPxg) ewb.get3DReferencePtg(new CellReference("A1"), sheet);
            assertEquals(1, ptg.getExternalWorkbookNumber());
            assertEquals("Book1.xlsx", wb.getExternalLinksTable(0).getLinkedFileName());
        }
    }

}