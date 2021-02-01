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

import static org.apache.poi.hssf.HSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.aggregates.WorksheetProtectionBlock;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.BaseTestSheet;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.junit.jupiter.api.Test;

/**
 * Tests HSSFSheet.  This test case is very incomplete at the moment.
 */
final class TestHSSFSheet extends BaseTestSheet {

    public TestHSSFSheet() {
        super(HSSFITestDataProvider.instance);
    }

    /**
     * Test for Bugzilla #29747.
     * Moved from TestHSSFWorkbook#testSetRepeatingRowsAndColumns().
     */
    @Test
    void setRepeatingRowsAndColumnsBug29747() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            wb.createSheet();
            wb.createSheet();
            HSSFSheet sheet2 = wb.createSheet();
            sheet2.setRepeatingRows(CellRangeAddress.valueOf("1:2"));
            NameRecord nameRecord = wb.getWorkbook().getNameRecord(0);
            assertEquals(3, nameRecord.getSheetNumber());
        }
    }

    /**
     * Test the gridset field gets set as expected.
     */
    @Test
    void backupRecord() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            GridsetRecord gridsetRec = s.getSheet().getGridsetRecord();
            assertTrue(gridsetRec.getGridset());
            s.setGridsPrinted(true);
            assertFalse(gridsetRec.getGridset());
        }
    }

    /**
     * Test vertically centered output.
     */
    @Test
    void verticallyCenter() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            VCenterRecord record = s.getSheet().getPageSettings().getVCenter();

            assertFalse(record.getVCenter());
            assertFalse(s.getVerticallyCenter());
            s.setVerticallyCenter(true);
            assertTrue(record.getVCenter());
            assertTrue(s.getVerticallyCenter());
        }
    }

    /**
     * Test horizontally centered output.
     */
    @Test
    void horizontallyCenter() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HCenterRecord record = s.getSheet().getPageSettings().getHCenter();

            assertFalse(record.getHCenter());
            assertFalse(s.getHorizontallyCenter());
            s.setHorizontallyCenter(true);
            assertTrue(record.getHCenter());
            assertTrue(s.getHorizontallyCenter());
        }
    }


    /**
     * Test WSBoolRecord fields get set in the user model.
     */
    @Test
    void wsBool() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            WSBoolRecord record = (WSBoolRecord) s.getSheet().findFirstRecordBySid(WSBoolRecord.sid);

            // Check defaults
            assertNotNull(record);
            assertTrue(record.getAlternateExpression());
            assertTrue(record.getAlternateFormula());
            assertFalse(record.getAutobreaks());
            assertFalse(record.getDialog());
            assertFalse(record.getDisplayGuts());
            assertTrue(record.getFitToPage());
            assertFalse(record.getRowSumsBelow());
            assertFalse(record.getRowSumsRight());

            // Alter
            s.setAlternativeExpression(false);
            s.setAlternativeFormula(false);
            s.setAutobreaks(true);
            s.setDialog(true);
            s.setDisplayGuts(true);
            s.setFitToPage(false);
            s.setRowSumsBelow(true);
            s.setRowSumsRight(true);

            // Check
            assertTrue(record.getAlternateExpression()); //sheet.setRowSumsBelow alters this field too
            assertFalse(record.getAlternateFormula());
            assertTrue(record.getAutobreaks());
            assertTrue(record.getDialog());
            assertTrue(record.getDisplayGuts());
            assertFalse(record.getFitToPage());
            assertTrue(record.getRowSumsBelow());
            assertTrue(record.getRowSumsRight());
            assertTrue(s.getAlternateExpression());
            assertFalse(s.getAlternateFormula());
            assertTrue(s.getAutobreaks());
            assertTrue(s.getDialog());
            assertTrue(s.getDisplayGuts());
            assertFalse(s.getFitToPage());
            assertTrue(s.getRowSumsBelow());
            assertTrue(s.getRowSumsRight());
        }
    }

    /**
     * Setting landscape and portrait stuff on existing sheets
     */
    @Test
    void printSetupLandscapeExisting() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls")) {

            assertEquals(3, wb1.getNumberOfSheets());

            HSSFSheet sheetL = wb1.getSheetAt(0);
            HSSFSheet sheetPM = wb1.getSheetAt(1);
            HSSFSheet sheetLS = wb1.getSheetAt(2);

            // Check two aspects of the print setup
            assertFalse(sheetL.getPrintSetup().getLandscape());
            assertTrue(sheetPM.getPrintSetup().getLandscape());
            assertTrue(sheetLS.getPrintSetup().getLandscape());
            assertEquals(1, sheetL.getPrintSetup().getCopies());
            assertEquals(1, sheetPM.getPrintSetup().getCopies());
            assertEquals(1, sheetLS.getPrintSetup().getCopies());

            // Change one on each
            sheetL.getPrintSetup().setLandscape(true);
            sheetPM.getPrintSetup().setLandscape(false);
            sheetPM.getPrintSetup().setCopies((short) 3);

            // Check taken
            assertTrue(sheetL.getPrintSetup().getLandscape());
            assertFalse(sheetPM.getPrintSetup().getLandscape());
            assertTrue(sheetLS.getPrintSetup().getLandscape());
            assertEquals(1, sheetL.getPrintSetup().getCopies());
            assertEquals(3, sheetPM.getPrintSetup().getCopies());
            assertEquals(1, sheetLS.getPrintSetup().getCopies());

            // Save and re-load, and check still there
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sheetL = wb2.getSheetAt(0);
                sheetPM = wb2.getSheetAt(1);
                sheetLS = wb2.getSheetAt(2);

                assertTrue(sheetL.getPrintSetup().getLandscape());
                assertFalse(sheetPM.getPrintSetup().getLandscape());
                assertTrue(sheetLS.getPrintSetup().getLandscape());
                assertEquals(1, sheetL.getPrintSetup().getCopies());
                assertEquals(3, sheetPM.getPrintSetup().getCopies());
                assertEquals(1, sheetLS.getPrintSetup().getCopies());
            }
        }
    }

    @Test
    void groupRows() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet s = wb1.createSheet();
            HSSFRow r1 = s.createRow(0);
            HSSFRow r2 = s.createRow(1);
            HSSFRow r3 = s.createRow(2);
            HSSFRow r4 = s.createRow(3);
            HSSFRow r5 = s.createRow(4);

            assertEquals(0, r1.getOutlineLevel());
            assertEquals(0, r2.getOutlineLevel());
            assertEquals(0, r3.getOutlineLevel());
            assertEquals(0, r4.getOutlineLevel());
            assertEquals(0, r5.getOutlineLevel());

            s.groupRow(2, 3);

            assertEquals(0, r1.getOutlineLevel());
            assertEquals(0, r2.getOutlineLevel());
            assertEquals(1, r3.getOutlineLevel());
            assertEquals(1, r4.getOutlineLevel());
            assertEquals(0, r5.getOutlineLevel());

            // Save and re-open
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                s = wb2.getSheetAt(0);
                r1 = s.getRow(0);
                r2 = s.getRow(1);
                r3 = s.getRow(2);
                r4 = s.getRow(3);
                r5 = s.getRow(4);

                assertEquals(0, r1.getOutlineLevel());
                assertEquals(0, r2.getOutlineLevel());
                assertEquals(1, r3.getOutlineLevel());
                assertEquals(1, r4.getOutlineLevel());
                assertEquals(0, r5.getOutlineLevel());
            }
        }
    }

    @Test
    void groupRowsExisting() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("NoGutsRecords.xls")) {

            HSSFSheet s = wb1.getSheetAt(0);
            HSSFRow r1 = s.getRow(0);
            HSSFRow r2 = s.getRow(1);
            HSSFRow r3 = s.getRow(2);
            HSSFRow r4 = s.getRow(3);
            HSSFRow r5 = s.getRow(4);
            HSSFRow r6 = s.getRow(5);

            assertEquals(0, r1.getOutlineLevel());
            assertEquals(0, r2.getOutlineLevel());
            assertEquals(0, r3.getOutlineLevel());
            assertEquals(0, r4.getOutlineLevel());
            assertEquals(0, r5.getOutlineLevel());
            assertEquals(0, r6.getOutlineLevel());

            // This used to complain about lacking guts records
            s.groupRow(2, 4);

            assertEquals(0, r1.getOutlineLevel());
            assertEquals(0, r2.getOutlineLevel());
            assertEquals(1, r3.getOutlineLevel());
            assertEquals(1, r4.getOutlineLevel());
            assertEquals(1, r5.getOutlineLevel());
            assertEquals(0, r6.getOutlineLevel());

            // Save and re-open
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                // OutOfMemoryError -> Identified bug 39903
                s = wb2.getSheetAt(0);
                r1 = s.getRow(0);
                r2 = s.getRow(1);
                r3 = s.getRow(2);
                r4 = s.getRow(3);
                r5 = s.getRow(4);
                r6 = s.getRow(5);

                assertEquals(0, r1.getOutlineLevel());
                assertEquals(0, r2.getOutlineLevel());
                assertEquals(1, r3.getOutlineLevel());
                assertEquals(1, r4.getOutlineLevel());
                assertEquals(1, r5.getOutlineLevel());
                assertEquals(0, r6.getOutlineLevel());
            }
        }
    }

    @Test
    void createDrawings() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet();
            HSSFPatriarch p1 = sheet.createDrawingPatriarch();
            HSSFPatriarch p2 = sheet.createDrawingPatriarch();
            assertSame(p1, p2);
        }
    }

    @Test
    void getDrawings() throws IOException {
        try (HSSFWorkbook wb1c = HSSFTestDataSamples.openSampleWorkbook("WithChart.xls");
            HSSFWorkbook wb2c = HSSFTestDataSamples.openSampleWorkbook("WithTwoCharts.xls")) {

            // 1 chart sheet -> data on 1st, chart on 2nd
            assertNotNull(wb1c.getSheetAt(0).getDrawingPatriarch());
            assertSame(wb1c.getSheetAt(0).getDrawingPatriarch(), wb1c.getSheetAt(0).getDrawingPatriarch());
            assertNotNull(wb1c.getSheetAt(1).getDrawingPatriarch());
            assertSame(wb1c.getSheetAt(1).getDrawingPatriarch(), wb1c.getSheetAt(1).getDrawingPatriarch());
            assertFalse(wb1c.getSheetAt(0).getDrawingPatriarch().containsChart());
            assertTrue(wb1c.getSheetAt(1).getDrawingPatriarch().containsChart());

            // 2 chart sheet -> data on 1st, chart on 2nd+3rd
            assertNotNull(wb2c.getSheetAt(0).getDrawingPatriarch());
            assertNotNull(wb2c.getSheetAt(1).getDrawingPatriarch());
            assertNotNull(wb2c.getSheetAt(2).getDrawingPatriarch());
            assertFalse(wb2c.getSheetAt(0).getDrawingPatriarch().containsChart());
            assertTrue(wb2c.getSheetAt(1).getDrawingPatriarch().containsChart());
            assertTrue(wb2c.getSheetAt(2).getDrawingPatriarch().containsChart());
        }
    }

    /**
     * Test that the ProtectRecord is included when creating or cloning a sheet
     */
    @Test
    void cloneWithProtect() throws IOException {
        String passwordA = "secrect";
        int expectedHashA = -6810;
        String passwordB = "admin";
        int expectedHashB = -14556;

        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet hssfSheet = workbook.createSheet();
            assertFalse(hssfSheet.getObjectProtect());
            hssfSheet.protectSheet(passwordA);
            assertTrue(hssfSheet.getObjectProtect());
            assertEquals(expectedHashA, hssfSheet.getPassword());

            assertEquals(expectedHashA, hssfSheet.getSheet().getProtectionBlock().getPasswordHash());

            // Clone the sheet, and make sure the password hash is preserved
            HSSFSheet sheet2 = workbook.cloneSheet(0);
            assertTrue(hssfSheet.getObjectProtect());
            assertEquals(expectedHashA, sheet2.getSheet().getProtectionBlock().getPasswordHash());

            // change the password on the first sheet
            hssfSheet.protectSheet(passwordB);
            assertTrue(hssfSheet.getObjectProtect());
            assertEquals(expectedHashB, hssfSheet.getSheet().getProtectionBlock().getPasswordHash());
            assertEquals(expectedHashB, hssfSheet.getPassword());
            // but the cloned sheet's password should remain unchanged
            assertEquals(expectedHashA, sheet2.getSheet().getProtectionBlock().getPasswordHash());
        }
    }

    @Test
    void protectSheetA() throws IOException {
        int expectedHash = (short)0xfef1;
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            s.protectSheet("abcdefghij");
            WorksheetProtectionBlock pb = s.getSheet().getProtectionBlock();
            assertTrue(pb.isSheetProtected(), "protection should be on");
            assertTrue(pb.isObjectProtected(), "object protection should be on");
            assertTrue(pb.isScenarioProtected(), "scenario protection should be on");
            assertEquals(expectedHash, pb.getPasswordHash(), "well known value for top secret hash should be " + Integer.toHexString(expectedHash).substring(4));
        }
    }

    /**
     * {@link PasswordRecord} belongs with the rest of the Worksheet Protection Block
     * (which should be before {@link DimensionsRecord}).
     */
    @Test
    void protectSheetRecordOrder_bug47363a() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            s.protectSheet("secret");
            List<org.apache.poi.hssf.record.Record> recs = new ArrayList<>();
            s.getSheet().visitContainedRecords(recs::add, 0);
            int nRecs = recs.size();

            // Check that protection block is together, and before DIMENSION
            // PASSWORD must be before DIMENSION
            Class<?>[] exp = {ProtectRecord.class, ObjectProtectRecord.class, ScenarioProtectRecord.class,
                    PasswordRecord.class, DefaultColWidthRecord.class, DimensionsRecord.class, WindowTwoRecord.class,
                    SelectionRecord.class, EOFRecord.class };
            Class<?>[] act = recs.subList(nRecs - 9, nRecs).stream().map(Object::getClass).toArray(Class[]::new);
            assertArrayEquals(exp, act);
        }
    }

    /**
     * There should be no problem with adding data validations after sheet protection
     */
    @Test
    void dvProtectionOrder_bug47363b() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Sheet1");
            sheet.protectSheet("secret");

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint dvc = dataValidationHelper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, "10", "100");
            CellRangeAddressList numericCellAddressList = new CellRangeAddressList(0, 0, 1, 1);
            DataValidation dv = dataValidationHelper.createValidation(dvc, numericCellAddressList);

            // bug 47363b: Unexpected (org.apache.poi.hssf.record.PasswordRecord) while looking for DV Table insert pos
            sheet.addValidationData(dv);

            int[] nRecsWithProtection = { 0 };
            sheet.getSheet().visitContainedRecords(r -> nRecsWithProtection[0]++, 0);

            sheet.protectSheet(null);
            int[] nRecsWithoutProtection = { 0 };
            sheet.getSheet().visitContainedRecords(r -> nRecsWithoutProtection[0]++, 0);

            assertEquals(4, nRecsWithProtection[0] - nRecsWithoutProtection[0]);
        }
    }

    @Test
    void zoom() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            assertEquals(-1, sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid));
            sheet.setZoom(75);
            assertTrue(sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid) > 0);
            SCLRecord sclRecord = (SCLRecord) sheet.getSheet().findFirstRecordBySid(SCLRecord.sid);
            assertNotNull(sclRecord);
            short numerator = sclRecord.getNumerator();
            assertEquals(75, 100 * numerator / sclRecord.getDenominator());

            int sclLoc = sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid);
            int window2Loc = sheet.getSheet().findFirstRecordLocBySid(WindowTwoRecord.sid);
            assertEquals(sclLoc, window2Loc + 1);

            // verify limits
            IllegalArgumentException e;
            e = assertThrows(IllegalArgumentException.class, () -> sheet.setZoom(0));
            assertEquals("Numerator must be greater than 0 and less than 65536", e.getMessage());

            e = assertThrows(IllegalArgumentException.class, () -> sheet.setZoom(65536));
            assertEquals("Numerator must be greater than 0 and less than 65536", e.getMessage());

            e = assertThrows(IllegalArgumentException.class, () -> sheet.setZoom(2, 0));
            assertEquals("Denominator must be greater than 0 and less than 65536", e.getMessage());

            e = assertThrows(IllegalArgumentException.class, () -> sheet.setZoom(2, 65536));
            assertEquals("Denominator must be greater than 0 and less than 65536", e.getMessage());
        }
    }


    /**
     * When removing one merged region, it would break
     * Make sure the excel file loads work
     */
    @Test
    void pageBreakFiles() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls")) {

            HSSFSheet sheet = wb1.getSheetAt(0);
            assertNotNull(sheet);

            assertEquals(1, sheet.getRowBreaks().length, "1 row page break");
            assertEquals(1, sheet.getColumnBreaks().length, "1 column page break");

            assertTrue(sheet.isRowBroken(22), "No row page break");
            assertTrue(sheet.isColumnBroken((short) 4), "No column page break");

            sheet.setRowBreak(10);
            sheet.setColumnBreak((short) 13);

            assertEquals(2, sheet.getRowBreaks().length, "row breaks number");
            assertEquals(2, sheet.getColumnBreaks().length, "column breaks number");

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);

                assertTrue(sheet.isRowBroken(22), "No row page break");
                assertTrue(sheet.isColumnBroken((short) 4), "No column page break");

                assertEquals(2, sheet.getRowBreaks().length, "row breaks number");
                assertEquals(2, sheet.getColumnBreaks().length, "column breaks number");
            }
        }
    }

    @Test
    void dbcsName () throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("DBCSSheetName.xls")) {
            wb.getSheetAt(1);
            assertEquals(wb.getSheetName(1), "\u090f\u0915", "DBCS Sheet Name 2");
            assertEquals(wb.getSheetName(0), "\u091c\u093e", "DBCS Sheet Name 1");
        }
    }

    /**
     * Testing newly added method that exposes the WINDOW2.toprow
     * parameter to allow setting the toprow in the visible view
     * of the sheet when it is first opened.
     */
    @Test
    void topRow() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            assertNotNull(sheet);

            short toprow = (short) 100;
            short leftcol = (short) 50;
            sheet.showInPane(toprow, leftcol);
            assertEquals(toprow, sheet.getTopRow(), "HSSFSheet.getTopRow()");
            assertEquals(leftcol, sheet.getLeftCol(), "HSSFSheet.getLeftCol()");
        }
    }

    @Test
    void addEmptyRow() throws IOException {
        //try to add 5 empty rows to a new sheet
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            for (int i = 0; i < 5; i++) {
                sheet.createRow(i);
            }

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                HSSFSheet sheet2 = wb2.getSheetAt(0);
                assertNotNull(sheet2.getRow(4));
            }
        }

            //try adding empty rows in an existing worksheet
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("Simple.xls")) {
            HSSFSheet sheet = wb1.getSheetAt(0);
            for (int i = 3; i < 10; i++) {
                sheet.createRow(i);
            }

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                HSSFSheet sheet2 = wb2.getSheetAt(0);
                assertNotNull(sheet2.getRow(4));
            }
        }
    }

    @Test
    void autoSizeColumn() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("43902.xls")) {
            String sheetName = "my sheet";
            HSSFSheet sheet = wb1.getSheet(sheetName);

            // Can't use literal numbers for column sizes, as
            //  will come out with different values on different
            //  machines based on the fonts available.
            // So, we use ranges, which are pretty large, but
            //  thankfully don't overlap!
            int minWithRow1And2 = 6400;
            int maxWithRow1And2 = 7800;
            int minWithRow1Only = 2750;
            int maxWithRow1Only = 3400;

            // autoSize the first column and check its size before the merged region (1,0,1,1) is set:
            // it has to be based on the 2nd row width
            sheet.autoSizeColumn((short) 0);
            assertTrue(sheet.getColumnWidth(0) >= minWithRow1And2, "Column autosized with only one row: wrong width");
            assertTrue(sheet.getColumnWidth(0) <= maxWithRow1And2, "Column autosized with only one row: wrong width");

            //create a region over the 2nd row and auto size the first column
            assertEquals(1, sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1)));
            assertNotNull(sheet.getMergedRegion(0));
            sheet.autoSizeColumn((short) 0);
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {

                // check that the autoSized column width has ignored the 2nd row
                // because it is included in a merged region (Excel like behavior)
                HSSFSheet sheet2 = wb2.getSheet(sheetName);
                assertTrue(sheet2.getColumnWidth(0) >= minWithRow1Only);
                assertTrue(sheet2.getColumnWidth(0) <= maxWithRow1Only);

                // remove the 2nd row merged region and check that the 2nd row value is used to the autoSizeColumn width
                sheet2.removeMergedRegion(1);
                sheet2.autoSizeColumn((short) 0);
                try (HSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    HSSFSheet sheet3 = wb3.getSheet(sheetName);
                    assertTrue(sheet3.getColumnWidth(0) >= minWithRow1And2);
                    assertTrue(sheet3.getColumnWidth(0) <= maxWithRow1And2);
                }
            }
        }
    }

    /**
     * Setting ForceFormulaRecalculation on sheets
     */
    @Test
    void forceRecalculation() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("UncalcedRecord.xls")) {

            HSSFSheet sheet = wb1.getSheetAt(0);
            HSSFSheet sheet2 = wb1.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            row.createCell(0).setCellValue(5);
            row.createCell(1).setCellValue(8);
            assertFalse(sheet.getForceFormulaRecalculation());
            assertFalse(sheet2.getForceFormulaRecalculation());

            // Save and manually verify that on column C we have 0, value in template
            writeOutAndReadBack(wb1).close();
            sheet.setForceFormulaRecalculation(true);
            assertTrue(sheet.getForceFormulaRecalculation());

            // Save and manually verify that on column C we have now 13, calculated value
            // Try it can be opened
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                // And check correct sheet settings found
                sheet = wb2.getSheetAt(0);
                sheet2 = wb2.getSheetAt(1);
                assertTrue(sheet.getForceFormulaRecalculation());
                assertFalse(sheet2.getForceFormulaRecalculation());

                // Now turn if back off again
                sheet.setForceFormulaRecalculation(false);

                try (HSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    assertFalse(wb3.getSheetAt(0).getForceFormulaRecalculation());
                    assertFalse(wb3.getSheetAt(1).getForceFormulaRecalculation());
                    assertFalse(wb3.getSheetAt(2).getForceFormulaRecalculation());

                    // Now add a new sheet, and check things work
                    //  with old ones unset, new one set
                    HSSFSheet s4 = wb3.createSheet();
                    s4.setForceFormulaRecalculation(true);

                    assertFalse(sheet.getForceFormulaRecalculation());
                    assertFalse(sheet2.getForceFormulaRecalculation());
                    assertTrue(s4.getForceFormulaRecalculation());

                    try (HSSFWorkbook wb4 = writeOutAndReadBack(wb3)) {
                        assertFalse(wb4.getSheetAt(0).getForceFormulaRecalculation());
                        assertFalse(wb4.getSheetAt(1).getForceFormulaRecalculation());
                        assertFalse(wb4.getSheetAt(2).getForceFormulaRecalculation());
                        assertTrue(wb4.getSheetAt(3).getForceFormulaRecalculation());
                    }
                }
            }
        }
    }

    @Test
    void columnWidthA() throws IOException {
        // check we can correctly read column widths from a reference workbook
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("colwidth.xls")) {

            //reference values
            int[] ref = {365, 548, 731, 914, 1097, 1280, 1462, 1645, 1828, 2011, 2194, 2377, 2560, 2742, 2925, 3108, 3291, 3474, 3657};

            HSSFSheet sh = wb.getSheetAt(0);
            for (char i = 'A'; i <= 'S'; i++) {
                int idx = i - 'A';
                int w = sh.getColumnWidth(idx);
                assertEquals(ref[idx], w);
            }

            //the second sheet doesn't have overridden column widths
            sh = wb.getSheetAt(1);
            int def_width = sh.getDefaultColumnWidth();
            for (char i = 'A'; i <= 'S'; i++) {
                int idx = i - 'A';
                int w = sh.getColumnWidth(idx);
                //getDefaultColumnWidth returns width measured in characters
                //getColumnWidth returns width measured in 1/256th units
                assertEquals(def_width * 256, w);
            }
        }

        // test new workbook
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            sh.setDefaultColumnWidth(10);
            assertEquals(10, sh.getDefaultColumnWidth());
            assertEquals(256 * 10, sh.getColumnWidth(0));
            assertEquals(256 * 10, sh.getColumnWidth(1));
            assertEquals(256 * 10, sh.getColumnWidth(2));
            for (char i = 'D'; i <= 'F'; i++) {
                short w = (256 * 12);
                sh.setColumnWidth(i, w);
                assertEquals(w, sh.getColumnWidth(i));
            }

            //serialize and read again
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                assertEquals(10, sh.getDefaultColumnWidth());
                //columns A-C have default width
                assertEquals(256 * 10, sh.getColumnWidth(0));
                assertEquals(256 * 10, sh.getColumnWidth(1));
                assertEquals(256 * 10, sh.getColumnWidth(2));
                //columns D-F have custom width
                for (char i = 'D'; i <= 'F'; i++) {
                    short w = (256 * 12);
                    assertEquals(w, sh.getColumnWidth(i));
                }

                // check for 16-bit signed/unsigned error:
                sh.setColumnWidth(0, 40000);
                assertEquals(40000, sh.getColumnWidth(0));
            }
        }
    }


    @Test
    void defaultColumnWidth() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook( "12843-1.xls" )) {
            HSSFSheet sheet = wb1.getSheetAt(7);
            // shall not be NPE
            assertEquals(8, sheet.getDefaultColumnWidth());
            assertEquals(8 * 256, sheet.getColumnWidth(0));

            assertEquals(0xFF, sheet.getDefaultRowHeight());
        }

        try (HSSFWorkbook wb2 = HSSFTestDataSamples.openSampleWorkbook( "34775.xls" )) {
            // second and third sheets miss DefaultColWidthRecord
            for (int i = 1; i <= 2; i++) {
                HSSFSheet sheet = wb2.getSheetAt(i);
                int dw = sheet.getDefaultColumnWidth();
                assertEquals(8, dw);
                int cw = sheet.getColumnWidth(0);
                assertEquals(8 * 256, cw);

                assertEquals(0xFF, sheet.getDefaultRowHeight());
            }
        }
    }

    /**
     * Some utilities write Excel files without the ROW records.
     * Excel, ooo, and google docs are OK with this.
     * Now POI is too.
     */
    @Test
    void missingRowRecords_bug41187() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex41187-19267.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            assertNotNull(row, "Identified bug 41187 a");

            assertNotEquals((short) 0, row.getHeight(), "Identified bug 41187 b");

            assertEquals(row.getCell(0).getRichStringCellValue().getString(), "Hi Excel!");
            // check row height for 'default' flag
            assertEquals((short) 0xFF, row.getHeight());

            writeOutAndReadBack(wb).close();
        }
    }

    /**
     * If we clone a sheet containing drawings,
     * we must allocate a new ID of the drawing group and re-create shape IDs
     *
     * See bug #45720.
     */
    @Test
    void cloneSheetWithDrawings() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("45720.xls")) {
            HSSFSheet sheet1 = wb1.getSheetAt(0);

            DrawingManager2 dm1 = wb1.getWorkbook().findDrawingGroup();
            int maxDrawingGroupId1 = dm1.getDgg().getMaxDrawingGroupId();
            wb1.cloneSheet(0);

            //check EscherDggRecord - a workbook-level registry of drawing objects
            assertEquals(maxDrawingGroupId1 + 1, dm1.getDgg().getMaxDrawingGroupId());

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                DrawingManager2 dm2 = wb2.getWorkbook().findDrawingGroup();
                assertEquals(maxDrawingGroupId1 + 1, dm2.getDgg().getMaxDrawingGroupId());

                HSSFSheet sheet2 = wb2.getSheetAt(1);

                //check that id of the drawing group was updated
                EscherDgRecord dg1 = (EscherDgRecord) sheet1.getDrawingPatriarch().getBoundAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
                EscherDgRecord dg2 = (EscherDgRecord) sheet2.getDrawingPatriarch().getBoundAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
                int dg_id_1 = dg1.getOptions() >> 4;
                int dg_id_2 = dg2.getOptions() >> 4;
                assertEquals(dg_id_1 + 1, dg_id_2);

                //TODO: check shapeId in the cloned sheet
            }
        }
    }

    /**
     * POI now (Sep 2008) allows sheet names longer than 31 chars (for other apps besides Excel).
     * Since Excel silently truncates to 31, make sure that POI enforces uniqueness on the first
     * 31 chars.
     */
    @Test
    void longSheetNames() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            final String SAME_PREFIX = "A123456789B123456789C123456789"; // 30 chars

            wb.createSheet(SAME_PREFIX + "Dxxxx");
            // identical up to the 32nd char
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> wb.createSheet(SAME_PREFIX + "Dyyyy"));
            assertEquals("The workbook already contains a sheet named 'A123456789B123456789C123456789Dyyyy'", e.getMessage());

            wb.createSheet(SAME_PREFIX + "Exxxx"); // OK - differs in the 31st char
        }
    }

    /**
     * Tests that we can read existing column styles
     */
    @Test
    void readColumnStyles() throws IOException {
        try (HSSFWorkbook wbNone = HSSFTestDataSamples.openSampleWorkbook("ColumnStyleNone.xls");
            HSSFWorkbook wbSimple = HSSFTestDataSamples.openSampleWorkbook("ColumnStyle1dp.xls");
            HSSFWorkbook wbComplex = HSSFTestDataSamples.openSampleWorkbook("ColumnStyle1dpColoured.xls")) {

            // Presence / absence checks
            assertNull(wbNone.getSheetAt(0).getColumnStyle(0));
            assertNull(wbNone.getSheetAt(0).getColumnStyle(1));

            assertNull(wbSimple.getSheetAt(0).getColumnStyle(0));
            assertNotNull(wbSimple.getSheetAt(0).getColumnStyle(1));

            assertNull(wbComplex.getSheetAt(0).getColumnStyle(0));
            assertNotNull(wbComplex.getSheetAt(0).getColumnStyle(1));

            // Details checks
            HSSFCellStyle bs = wbSimple.getSheetAt(0).getColumnStyle(1);
            assertNotNull(bs);
            assertEquals(62, bs.getIndex());
            assertEquals("#,##0.0_ ;\\-#,##0.0\\ ", bs.getDataFormatString());
            assertEquals("Calibri", bs.getFont(wbSimple).getFontName());
            assertEquals(11 * 20, bs.getFont(wbSimple).getFontHeight());
            assertEquals(8, bs.getFont(wbSimple).getColor());
            assertFalse(bs.getFont(wbSimple).getItalic());
            assertFalse(bs.getFont(wbSimple).getBold());


            HSSFCellStyle cs = wbComplex.getSheetAt(0).getColumnStyle(1);
            assertNotNull(cs);
            assertEquals(62, cs.getIndex());
            assertEquals("#,##0.0_ ;\\-#,##0.0\\ ", cs.getDataFormatString());
            assertEquals("Arial", cs.getFont(wbComplex).getFontName());
            assertEquals(8 * 20, cs.getFont(wbComplex).getFontHeight());
            assertEquals(10, cs.getFont(wbComplex).getColor());
            assertFalse(cs.getFont(wbComplex).getItalic());
            assertTrue(cs.getFont(wbComplex).getBold());
        }
    }

    /**
     * Tests the arabic setting
     */
    @Test
    void arabic() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();

            assertFalse(s.isRightToLeft());
            s.setRightToLeft(true);
            assertTrue(s.isRightToLeft());
        }
    }

    @Test
    void autoFilter() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            InternalWorkbook iwb = wb1.getWorkbook();
            InternalSheet ish = sh.getSheet();

            assertNull(iwb.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1));
            assertNull(ish.findFirstRecordBySid(AutoFilterInfoRecord.sid));

            CellRangeAddress range = CellRangeAddress.valueOf("A1:B10");
            sh.setAutoFilter(range);

            NameRecord name = iwb.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1);
            assertNotNull(name);

            // The built-in name for auto-filter must consist of a single Area3d Ptg.
            Ptg[] ptg = name.getNameDefinition();
            assertEquals(1, ptg.length, "The built-in name for auto-filter must consist of a single Area3d Ptg");
            assertTrue(ptg[0] instanceof Area3DPtg, "The built-in name for auto-filter must consist of a single Area3d Ptg");

            Area3DPtg aref = (Area3DPtg) ptg[0];
            assertEquals(range.getFirstColumn(), aref.getFirstColumn());
            assertEquals(range.getFirstRow(), aref.getFirstRow());
            assertEquals(range.getLastColumn(), aref.getLastColumn());
            assertEquals(range.getLastRow(), aref.getLastRow());

            // verify  AutoFilterInfoRecord
            AutoFilterInfoRecord afilter = (AutoFilterInfoRecord) ish.findFirstRecordBySid(AutoFilterInfoRecord.sid);
            assertNotNull(afilter);
            assertEquals(2, afilter.getNumEntries()); //filter covers two columns

            HSSFPatriarch dr = sh.getDrawingPatriarch();
            assertNotNull(dr);
            HSSFSimpleShape comboBoxShape = (HSSFSimpleShape) dr.getChildren().get(0);
            assertEquals(comboBoxShape.getShapeType(), HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX);

            assertNull(ish.findFirstRecordBySid(ObjRecord.sid)); // ObjRecord will appear after serializetion

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                ish = sh.getSheet();
                ObjRecord objRecord = (ObjRecord) ish.findFirstRecordBySid(ObjRecord.sid);
                assertNotNull(objRecord);
                List<SubRecord> subRecords = objRecord.getSubRecords();
                assertEquals(3, subRecords.size());
                assertTrue(subRecords.get(0) instanceof CommonObjectDataSubRecord);
                assertTrue(subRecords.get(1) instanceof FtCblsSubRecord); // must be present, see Bug 51481
                assertTrue(subRecords.get(2) instanceof LbsDataSubRecord);
            }
        }
    }

    @Test
    void getSetColumnHiddenShort() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");
            sheet.setColumnHidden((short) 2, true);
            assertTrue(sheet.isColumnHidden((short) 2));
        }
    }

    @Test
    void columnWidthShort() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            Sheet sheet = wb1.createSheet();

            //default column width measured in characters
            sheet.setDefaultColumnWidth((short) 10);
            assertEquals(10, sheet.getDefaultColumnWidth());
            //columns A-C have default width
            assertEquals(256 * 10, sheet.getColumnWidth((short) 0));
            assertEquals(256 * 10, sheet.getColumnWidth((short) 1));
            assertEquals(256 * 10, sheet.getColumnWidth((short) 2));

            //set custom width for D-F
            for (char i = 'D'; i <= 'F'; i++) {
                //Sheet#setColumnWidth accepts the width in units of 1/256th of a character width
                int w = 256 * 12;
                sheet.setColumnWidth((short) i, w);
                assertEquals(w, sheet.getColumnWidth((short) i));
            }
            //reset the default column width, columns A-C change, D-F still have custom width
            sheet.setDefaultColumnWidth((short) 20);
            assertEquals(20, sheet.getDefaultColumnWidth());
            assertEquals(256 * 20, sheet.getColumnWidth((short) 0));
            assertEquals(256 * 20, sheet.getColumnWidth((short) 1));
            assertEquals(256 * 20, sheet.getColumnWidth((short) 2));
            for (char i = 'D'; i <= 'F'; i++) {
                int w = 256 * 12;
                assertEquals(w, sheet.getColumnWidth((short) i));
            }

            // check for 16-bit signed/unsigned error:
            sheet.setColumnWidth((short) 10, 40000);
            assertEquals(40000, sheet.getColumnWidth((short) 10));

            //The maximum column width for an individual cell is 255 characters
            Sheet sheet2 = sheet;
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                sheet2.setColumnWidth((short) 9, 256 * 256));
            assertEquals("The maximum column width for an individual cell is 255 characters.", e.getMessage());

            //serialize and read again
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                assertEquals(20, sheet.getDefaultColumnWidth());
                //columns A-C have default width
                assertEquals(256 * 20, sheet.getColumnWidth((short) 0));
                assertEquals(256 * 20, sheet.getColumnWidth((short) 1));
                assertEquals(256 * 20, sheet.getColumnWidth((short) 2));
                //columns D-F have custom width
                for (char i = 'D'; i <= 'F'; i++) {
                    short w = (256 * 12);
                    assertEquals(w, sheet.getColumnWidth((short) i));
                }
                assertEquals(40000, sheet.getColumnWidth((short) 10));
            }
        }
    }

    @Test
    void showInPane() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            sheet.showInPane(2, 3);

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet.showInPane(Integer.MAX_VALUE, 3)
            );
            assertEquals("Maximum row number is 65535", ex.getMessage());
        }
    }

    @Test
    void drawingRecords() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();

            /* TODO: NPE?
            sheet.dumpDrawingRecords(false);
            sheet.dumpDrawingRecords(true);*/
            assertNull(sheet.getDrawingEscherAggregate());
        }
    }

    @Test
    void bug55723b() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();

            // stored with a special name
            assertNull(wb.getWorkbook().getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1));

            CellRangeAddress range = CellRangeAddress.valueOf("A:B");
            AutoFilter filter = sheet.setAutoFilter(range);
            assertNotNull(filter);

            // stored with a special name
            NameRecord record = wb.getWorkbook().getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1);
            assertNotNull(record);
        }
    }

    @Test
    void test58746() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            HSSFSheet first = wb.createSheet("first");
            first.createRow(0).createCell(0).setCellValue(1);

            HSSFSheet second = wb.createSheet("second");
            second.createRow(0).createCell(0).setCellValue(2);

            HSSFSheet third = wb.createSheet("third");
            HSSFRow row = third.createRow(0);
            row.createCell(0).setCellFormula("first!A1");
            row.createCell(1).setCellFormula("second!A1");

            // re-order for sheet "third"
            wb.setSheetOrder("third", 0);

            // verify results
            assertEquals("third", wb.getSheetAt(0).getSheetName());
            assertEquals("first", wb.getSheetAt(1).getSheetName());
            assertEquals("second", wb.getSheetAt(2).getSheetName());

            assertEquals("first!A1", wb.getSheetAt(0).getRow(0).getCell(0).getCellFormula());
            assertEquals("second!A1", wb.getSheetAt(0).getRow(0).getCell(1).getCellFormula());
        }
    }

    @Test
    void bug59135() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            wb1.createSheet().protectSheet("1111.2222.3333.1234");
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {

                assertEquals((short) 0xb86b, wb2.getSheetAt(0).getPassword());
            }
        }

        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            wb1.createSheet().protectSheet("1111.2222.3333.12345");
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {

                assertEquals((short) 0xbecc, wb2.getSheetAt(0).getPassword());
            }
        }
    }
}
