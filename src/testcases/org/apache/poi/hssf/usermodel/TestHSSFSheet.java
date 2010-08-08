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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.AssertionFailedError;

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.aggregates.WorksheetProtectionBlock;
import org.apache.poi.hssf.usermodel.RecordInspector.RecordCollector;
import org.apache.poi.ss.usermodel.BaseTestSheet;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.TempFile;

/**
 * Tests HSSFSheet.  This test case is very incomplete at the moment.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew C. Oliver (acoliver apache org)
 */
public final class TestHSSFSheet extends BaseTestSheet {

    public TestHSSFSheet() {
        super(HSSFITestDataProvider.instance);
    }

    public void testTestGetSetMargin() {
        baseTestGetSetMargin(new double[]{0.75, 0.75, 1.0, 1.0, 0.3, 0.3});
    }

    /**
     * Test the gridset field gets set as expected.
     */
    public void testBackupRecord() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        GridsetRecord gridsetRec = s.getSheet().getGridsetRecord();
		assertEquals(true, gridsetRec.getGridset());
        s.setGridsPrinted(true);
        assertEquals(false, gridsetRec.getGridset());
    }

    /**
     * Test vertically centered output.
     */
    public void testVerticallyCenter() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        VCenterRecord record = s.getSheet().getPageSettings().getVCenter();

        assertEquals(false, record.getVCenter());
        s.setVerticallyCenter(true);
        assertEquals(true, record.getVCenter());

        // wb.write(new FileOutputStream("c:\\test.xls"));
    }

    /**
     * Test horizontally centered output.
     */
    public void testHorizontallyCenter() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        HCenterRecord record = s.getSheet().getPageSettings().getHCenter();

        assertEquals(false, record.getHCenter());
        s.setHorizontallyCenter(true);
        assertEquals(true, record.getHCenter());
    }


    /**
     * Test WSBboolRecord fields get set in the user model.
     */
    public void testWSBool() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        WSBoolRecord record =
                (WSBoolRecord) s.getSheet().findFirstRecordBySid(WSBoolRecord.sid);

        // Check defaults
        assertEquals(true, record.getAlternateExpression());
        assertEquals(true, record.getAlternateFormula());
        assertEquals(false, record.getAutobreaks());
        assertEquals(false, record.getDialog());
        assertEquals(false, record.getDisplayGuts());
        assertEquals(true, record.getFitToPage());
        assertEquals(false, record.getRowSumsBelow());
        assertEquals(false, record.getRowSumsRight());

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
        assertEquals(true, record.getAlternateExpression()); //sheet.setRowSumsBelow alters this field too
        assertEquals(false, record.getAlternateFormula());
        assertEquals(true, record.getAutobreaks());
        assertEquals(true, record.getDialog());
        assertEquals(true, record.getDisplayGuts());
        assertEquals(false, record.getFitToPage());
        assertEquals(true, record.getRowSumsBelow());
        assertEquals(true, record.getRowSumsRight());
        assertEquals(true, s.getAlternateExpression());
        assertEquals(false, s.getAlternateFormula());
        assertEquals(true, s.getAutobreaks());
        assertEquals(true, s.getDialog());
        assertEquals(true, s.getDisplayGuts());
        assertEquals(false, s.getFitToPage());
        assertEquals(true, s.getRowSumsBelow());
        assertEquals(true, s.getRowSumsRight());
    }

    /**
     * Setting landscape and portrait stuff on existing sheets
     */
    public void testPrintSetupLandscapeExisting() {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls");

        assertEquals(3, workbook.getNumberOfSheets());

        HSSFSheet sheetL = workbook.getSheetAt(0);
        HSSFSheet sheetPM = workbook.getSheetAt(1);
        HSSFSheet sheetLS = workbook.getSheetAt(2);

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
        sheetPM.getPrintSetup().setCopies((short)3);

        // Check taken
        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetPM.getPrintSetup().getLandscape());
        assertTrue(sheetLS.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetPM.getPrintSetup().getCopies());
        assertEquals(1, sheetLS.getPrintSetup().getCopies());

        // Save and re-load, and check still there
        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetPM.getPrintSetup().getLandscape());
        assertTrue(sheetLS.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetPM.getPrintSetup().getCopies());
        assertEquals(1, sheetLS.getPrintSetup().getCopies());
    }

    public void testGroupRows() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet s = workbook.createSheet();
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

        s.groupRow(2,3);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(1, r3.getOutlineLevel());
        assertEquals(1, r4.getOutlineLevel());
        assertEquals(0, r5.getOutlineLevel());

        // Save and re-open
        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        s = workbook.getSheetAt(0);
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

    public void testGroupRowsExisting() {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("NoGutsRecords.xls");

        HSSFSheet s = workbook.getSheetAt(0);
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
        try {
            workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        } catch (OutOfMemoryError e) {
            throw new AssertionFailedError("Identified bug 39903");
        }

        s = workbook.getSheetAt(0);
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

    public void testCreateDrawings() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFPatriarch p1 = sheet.createDrawingPatriarch();
        HSSFPatriarch p2 = sheet.createDrawingPatriarch();
        assertSame(p1, p2);
    }

    public void testGetDrawings() {
        HSSFWorkbook wb1c = HSSFTestDataSamples.openSampleWorkbook("WithChart.xls");
        HSSFWorkbook wb2c = HSSFTestDataSamples.openSampleWorkbook("WithTwoCharts.xls");

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

    /**
     * Test that the ProtectRecord is included when creating or cloning a sheet
     */
    public void testCloneWithProtect() {
        String passwordA = "secrect";
        int expectedHashA = -6810;
        String passwordB = "admin";
        int expectedHashB = -14556;
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = workbook.createSheet();
        hssfSheet.protectSheet(passwordA);

        assertEquals(expectedHashA, hssfSheet.getSheet().getProtectionBlock().getPasswordHash());

        // Clone the sheet, and make sure the password hash is preserved
        HSSFSheet sheet2 = workbook.cloneSheet(0);
        assertEquals(expectedHashA, sheet2.getSheet().getProtectionBlock().getPasswordHash());

        // change the password on the first sheet
        hssfSheet.protectSheet(passwordB);
        assertEquals(expectedHashB, hssfSheet.getSheet().getProtectionBlock().getPasswordHash());
        // but the cloned sheet's password should remain unchanged
        assertEquals(expectedHashA, sheet2.getSheet().getProtectionBlock().getPasswordHash());
    }

    public void testProtectSheet() {
        int expectedHash = (short)0xfef1;
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        s.protectSheet("abcdefghij");
        WorksheetProtectionBlock pb = s.getSheet().getProtectionBlock();
        assertTrue("protection should be on", pb.isSheetProtected());
        assertTrue("object protection should be on",pb.isObjectProtected());
        assertTrue("scenario protection should be on",pb.isScenarioProtected());
        assertEquals("well known value for top secret hash should be "+Integer.toHexString(expectedHash).substring(4), expectedHash, pb.getPasswordHash());
    }

    /**
     * {@link PasswordRecord} belongs with the rest of the Worksheet Protection Block
     * (which should be before {@link DimensionsRecord}).
     */
    public void testProtectSheetRecordOrder_bug47363a() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        s.protectSheet("secret");
        RecordCollector rc = new RecordCollector();
        s.getSheet().visitContainedRecords(rc, 0);
        Record[] recs = rc.getRecords();
        int nRecs = recs.length;
        if (recs[nRecs-2] instanceof PasswordRecord && recs[nRecs-5] instanceof DimensionsRecord) {
           throw new AssertionFailedError("Identified bug 47363a - PASSWORD after DIMENSION");
        }
        // Check that protection block is together, and before DIMENSION
        confirmRecordClass(recs, nRecs-4, DimensionsRecord.class);
        confirmRecordClass(recs, nRecs-9, ProtectRecord.class);
        confirmRecordClass(recs, nRecs-8, ObjectProtectRecord.class);
        confirmRecordClass(recs, nRecs-7, ScenarioProtectRecord.class);
        confirmRecordClass(recs, nRecs-6, PasswordRecord.class);
    }

    private static void confirmRecordClass(Record[] recs, int index, Class<? extends Record> cls) {
        if (recs.length <= index) {
            throw new AssertionFailedError("Expected (" + cls.getName() + ") at index "
                    + index + " but array length is " + recs.length + ".");
        }
        assertEquals(cls, recs[index].getClass());
    }

    /**
     * There should be no problem with adding data validations after sheet protection
     */
    public void testDvProtectionOrder_bug47363b() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        sheet.protectSheet("secret");

        DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvc = dataValidationHelper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, "10", "100");
        CellRangeAddressList numericCellAddressList = new CellRangeAddressList(0, 0, 1, 1);
        DataValidation dv = dataValidationHelper.createValidation(dvc,numericCellAddressList);
        try {
            sheet.addValidationData(dv);
        } catch (IllegalStateException e) {
            String expMsg = "Unexpected (org.apache.poi.hssf.record.PasswordRecord) while looking for DV Table insert pos";
            if (expMsg.equals(e.getMessage())) {
                throw new AssertionFailedError("Identified bug 47363b");
            }
            throw e;
        }
        RecordCollector rc;
        rc = new RecordCollector();
        sheet.getSheet().visitContainedRecords(rc, 0);
        int nRecsWithProtection = rc.getRecords().length;

        sheet.protectSheet(null);
        rc = new RecordCollector();
        sheet.getSheet().visitContainedRecords(rc, 0);
        int nRecsWithoutProtection = rc.getRecords().length;

        assertEquals(4, nRecsWithProtection - nRecsWithoutProtection);
    }

    public void testZoom() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        assertEquals(-1, sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid));
        sheet.setZoom(3,4);
        assertTrue(sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid) > 0);
        SCLRecord sclRecord = (SCLRecord) sheet.getSheet().findFirstRecordBySid(SCLRecord.sid);
        assertEquals(3, sclRecord.getNumerator());
        assertEquals(4, sclRecord.getDenominator());

        int sclLoc = sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid);
        int window2Loc = sheet.getSheet().findFirstRecordLocBySid(WindowTwoRecord.sid);
        assertTrue(sclLoc == window2Loc + 1);
    }


    /**
     * When removing one merged region, it would break
     *
     */
    /**
     * Make sure the excel file loads work
     *
     */
    public void testPageBreakFiles() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        assertNotNull(sheet);

        assertEquals("1 row page break", 1, sheet.getRowBreaks().length);
        assertEquals("1 column page break", 1, sheet.getColumnBreaks().length);

        assertTrue("No row page break", sheet.isRowBroken(22));
        assertTrue("No column page break", sheet.isColumnBroken((short)4));

        sheet.setRowBreak(10);
        sheet.setColumnBreak((short)13);

        assertEquals("row breaks number", 2, sheet.getRowBreaks().length);
        assertEquals("column breaks number", 2, sheet.getColumnBreaks().length);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        assertTrue("No row page break", sheet.isRowBroken(22));
        assertTrue("No column page break", sheet.isColumnBroken((short)4));

        assertEquals("row breaks number", 2, sheet.getRowBreaks().length);
        assertEquals("column breaks number", 2, sheet.getColumnBreaks().length);
    }

    public void testDBCSName () {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("DBCSSheetName.xls");
        wb.getSheetAt(1);
        assertEquals ("DBCS Sheet Name 2", wb.getSheetName(1),"\u090f\u0915" );
        assertEquals("DBCS Sheet Name 1", wb.getSheetName(0),"\u091c\u093e");
    }

    /**
     * Testing newly added method that exposes the WINDOW2.toprow
     * parameter to allow setting the toprow in the visible view
     * of the sheet when it is first opened.
     */
    public void testTopRow() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPageBreaks.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        assertNotNull(sheet);

        short toprow = (short) 100;
        short leftcol = (short) 50;
        sheet.showInPane(toprow,leftcol);
        assertEquals("HSSFSheet.getTopRow()", toprow, sheet.getTopRow());
        assertEquals("HSSFSheet.getLeftCol()", leftcol, sheet.getLeftCol());
    }

    /** cell with formula becomes null on cloning a sheet*/
     public void test35084() {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet("Sheet1");
        HSSFRow r = s.createRow(0);
        r.createCell(0).setCellValue(1);
        r.createCell(1).setCellFormula("A1*2");
        HSSFSheet s1 = wb.cloneSheet(0);
        r = s1.getRow(0);
        assertEquals("double", r.getCell(0).getNumericCellValue(), 1, 0); // sanity check
        assertNotNull(r.getCell(1));
        assertEquals("formula", r.getCell(1).getCellFormula(), "A1*2");
    }

    /**
     *
     */
    public void testAddEmptyRow() {
        //try to add 5 empty rows to a new sheet
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        for (int i = 0; i < 5; i++) {
            sheet.createRow(i);
        }

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        //try adding empty rows in an existing worksheet
        workbook = HSSFTestDataSamples.openSampleWorkbook("Simple.xls");

        sheet = workbook.getSheetAt(0);
        for (int i = 3; i < 10; i++) sheet.createRow(i);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
    }

    public void testAutoSizeColumn() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("43902.xls");
        String sheetName = "my sheet";
        HSSFSheet sheet = wb.getSheet(sheetName);

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
        sheet.autoSizeColumn((short)0);
        assertTrue("Column autosized with only one row: wrong width", sheet.getColumnWidth(0) >= minWithRow1And2);
        assertTrue("Column autosized with only one row: wrong width", sheet.getColumnWidth(0) <= maxWithRow1And2);

        //create a region over the 2nd row and auto size the first column
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,1));
        sheet.autoSizeColumn((short)0);
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);

        // check that the autoSized column width has ignored the 2nd row
        // because it is included in a merged region (Excel like behavior)
        HSSFSheet sheet2 = wb2.getSheet(sheetName);
        assertTrue(sheet2.getColumnWidth(0) >= minWithRow1Only);
        assertTrue(sheet2.getColumnWidth(0) <= maxWithRow1Only);

        // remove the 2nd row merged region and check that the 2nd row value is used to the autoSizeColumn width
        sheet2.removeMergedRegion(1);
        sheet2.autoSizeColumn((short)0);
        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        HSSFSheet sheet3 = wb3.getSheet(sheetName);
        assertTrue(sheet3.getColumnWidth(0) >= minWithRow1And2);
        assertTrue(sheet3.getColumnWidth(0) <= maxWithRow1And2);
    }
    
    public void testAutoSizeDate() throws Exception {
       HSSFWorkbook wb = new HSSFWorkbook();
       HSSFSheet s = wb.createSheet("Sheet1");
       HSSFRow r = s.createRow(0);
       r.createCell(0).setCellValue(1);
       r.createCell(1).setCellValue(123456);
       
       // Will be sized fairly small
       s.autoSizeColumn((short)0);
       s.autoSizeColumn((short)1);
       
       // Size ranges due to different fonts on different machines
       assertTrue("Single number column too small: " + s.getColumnWidth(0), s.getColumnWidth(0) > 350); 
       assertTrue("Single number column too big: " + s.getColumnWidth(0),   s.getColumnWidth(0) < 550); 
       assertTrue("6 digit number column too small: " + s.getColumnWidth(1), s.getColumnWidth(1) > 1500); 
       assertTrue("6 digit number column too big: " + s.getColumnWidth(1),   s.getColumnWidth(1) < 2000);
       
       // Set a date format
       HSSFCellStyle cs = wb.createCellStyle();
       HSSFDataFormat f = wb.createDataFormat();
       cs.setDataFormat(f.getFormat("yyyy-mm-dd MMMM hh:mm:ss"));
       r.getCell(0).setCellStyle(cs);
       r.getCell(1).setCellStyle(cs);
       
       assertEquals(true, DateUtil.isCellDateFormatted(r.getCell(0)));
       assertEquals(true, DateUtil.isCellDateFormatted(r.getCell(1)));
       
       // Should get much bigger now
       s.autoSizeColumn((short)0);
       s.autoSizeColumn((short)1);

       assertTrue("Date column too small: " + s.getColumnWidth(0), s.getColumnWidth(0) > 4750); 
       assertTrue("Date column too small: " + s.getColumnWidth(1), s.getColumnWidth(1) > 4750); 
       assertTrue("Date column too big: " + s.getColumnWidth(0), s.getColumnWidth(0) < 6500); 
       assertTrue("Date column too big: " + s.getColumnWidth(0), s.getColumnWidth(0) < 6500); 
    }

    /**
     * Setting ForceFormulaRecalculation on sheets
     */
    public void testForceRecalculation() throws Exception {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("UncalcedRecord.xls");

        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFSheet sheet2 = workbook.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        row.createCell(0).setCellValue(5);
        row.createCell(1).setCellValue(8);
        assertFalse(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());

        // Save and manually verify that on column C we have 0, value in template
        File tempFile = TempFile.createTempFile("uncalced_err", ".xls" );
        FileOutputStream fout = new FileOutputStream( tempFile );
        workbook.write( fout );
        fout.close();
        sheet.setForceFormulaRecalculation(true);
        assertTrue(sheet.getForceFormulaRecalculation());

        // Save and manually verify that on column C we have now 13, calculated value
        tempFile = TempFile.createTempFile("uncalced_succ", ".xls");
        tempFile.delete();
        fout = new FileOutputStream( tempFile );
        workbook.write( fout );
        fout.close();

        // Try it can be opened
        HSSFWorkbook wb2 = new HSSFWorkbook(new FileInputStream(tempFile));

        // And check correct sheet settings found
        sheet = wb2.getSheetAt(0);
        sheet2 = wb2.getSheetAt(1);
        assertTrue(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());

        // Now turn if back off again
        sheet.setForceFormulaRecalculation(false);

        fout = new FileOutputStream( tempFile );
        wb2.write( fout );
        fout.close();
        wb2 = new HSSFWorkbook(new FileInputStream(tempFile));

        assertFalse(wb2.getSheetAt(0).getForceFormulaRecalculation());
        assertFalse(wb2.getSheetAt(1).getForceFormulaRecalculation());
        assertFalse(wb2.getSheetAt(2).getForceFormulaRecalculation());

        // Now add a new sheet, and check things work
        //  with old ones unset, new one set
        HSSFSheet s4 = wb2.createSheet();
        s4.setForceFormulaRecalculation(true);

        assertFalse(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());
        assertTrue(s4.getForceFormulaRecalculation());

        fout = new FileOutputStream( tempFile );
        wb2.write( fout );
        fout.close();

        HSSFWorkbook wb3 = new HSSFWorkbook(new FileInputStream(tempFile));
        assertFalse(wb3.getSheetAt(0).getForceFormulaRecalculation());
        assertFalse(wb3.getSheetAt(1).getForceFormulaRecalculation());
        assertFalse(wb3.getSheetAt(2).getForceFormulaRecalculation());
        assertTrue(wb3.getSheetAt(3).getForceFormulaRecalculation());
    }

    public void testColumnWidth() {
        //check we can correctly read column widths from a reference workbook
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("colwidth.xls");

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
            assertEquals(def_width*256, w);
        }

        //test new workbook
        wb = new HSSFWorkbook();
        sh = wb.createSheet();
        sh.setDefaultColumnWidth(10);
        assertEquals(10, sh.getDefaultColumnWidth());
        assertEquals(256*10, sh.getColumnWidth(0));
        assertEquals(256*10, sh.getColumnWidth(1));
        assertEquals(256*10, sh.getColumnWidth(2));
        for (char i = 'D'; i <= 'F'; i++) {
            short w = (256*12);
            sh.setColumnWidth(i, w);
            assertEquals(w, sh.getColumnWidth(i));
        }

        //serialize and read again
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        sh = wb.getSheetAt(0);
        assertEquals(10, sh.getDefaultColumnWidth());
        //columns A-C have default width
        assertEquals(256*10, sh.getColumnWidth(0));
        assertEquals(256*10, sh.getColumnWidth(1));
        assertEquals(256*10, sh.getColumnWidth(2));
        //columns D-F have custom width
        for (char i = 'D'; i <= 'F'; i++) {
            short w = (256*12);
            assertEquals(w, sh.getColumnWidth(i));
        }

        // check for 16-bit signed/unsigned error:
        sh.setColumnWidth(0, 40000);
        assertEquals(40000, sh.getColumnWidth(0));
    }

    /**
     * Some utilities write Excel files without the ROW records.
     * Excel, ooo, and google docs are OK with this.
     * Now POI is too.
     */
    public void testMissingRowRecords_bug41187() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex41187-19267.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        if(row == null) {
            throw new AssertionFailedError("Identified bug 41187 a");
        }
        if (row.getHeight() == 0) {
            throw new AssertionFailedError("Identified bug 41187 b");
        }
        assertEquals("Hi Excel!", row.getCell(0).getRichStringCellValue().getString());
        // check row height for 'default' flag
        assertEquals((short)0xFF, row.getHeight());

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    /**
     * If we clone a sheet containing drawings,
     * we must allocate a new ID of the drawing group and re-create shape IDs
     *
     * See bug #45720.
     */
    public void testCloneSheetWithDrawings() {
        HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("45720.xls");

        HSSFSheet sheet1 = wb1.getSheetAt(0);

        wb1.getWorkbook().findDrawingGroup();
        DrawingManager2 dm1 = wb1.getWorkbook().getDrawingManager();

        wb1.cloneSheet(0);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb2.getWorkbook().findDrawingGroup();
        DrawingManager2 dm2 = wb2.getWorkbook().getDrawingManager();

        //check EscherDggRecord - a workbook-level registry of drawing objects
        assertEquals(dm1.getDgg().getMaxDrawingGroupId() + 1, dm2.getDgg().getMaxDrawingGroupId());

        HSSFSheet sheet2 = wb2.getSheetAt(1);

        //check that id of the drawing group was updated
        EscherDgRecord dg1 = (EscherDgRecord)sheet1.getDrawingEscherAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
        EscherDgRecord dg2 = (EscherDgRecord)sheet2.getDrawingEscherAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
        int dg_id_1 = dg1.getOptions() >> 4;
        int dg_id_2 = dg2.getOptions() >> 4;
        assertEquals(dg_id_1 + 1, dg_id_2);

        //TODO: check shapeId in the cloned sheet
    }

    /**
     * POI now (Sep 2008) allows sheet names longer than 31 chars (for other apps besides Excel).
     * Since Excel silently truncates to 31, make sure that POI enforces uniqueness on the first
     * 31 chars.
     */
    public void testLongSheetNames() {
        HSSFWorkbook wb = new HSSFWorkbook();
        final String SAME_PREFIX = "A123456789B123456789C123456789"; // 30 chars

        wb.createSheet(SAME_PREFIX + "Dxxxx");
        try {
            wb.createSheet(SAME_PREFIX + "Dyyyy"); // identical up to the 32nd char
            throw new AssertionFailedError("Expected exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The workbook already contains a sheet of this name", e.getMessage());
        }
        wb.createSheet(SAME_PREFIX + "Exxxx"); // OK - differs in the 31st char
    }

    /**
     * Tests that we can read existing column styles
     */
    public void testReadColumnStyles() {
        HSSFWorkbook wbNone = HSSFTestDataSamples.openSampleWorkbook("ColumnStyleNone.xls");
        HSSFWorkbook wbSimple = HSSFTestDataSamples.openSampleWorkbook("ColumnStyle1dp.xls");
        HSSFWorkbook wbComplex = HSSFTestDataSamples.openSampleWorkbook("ColumnStyle1dpColoured.xls");

        // Presence / absence checks
        assertNull(wbNone.getSheetAt(0).getColumnStyle(0));
        assertNull(wbNone.getSheetAt(0).getColumnStyle(1));

        assertNull(wbSimple.getSheetAt(0).getColumnStyle(0));
        assertNotNull(wbSimple.getSheetAt(0).getColumnStyle(1));

        assertNull(wbComplex.getSheetAt(0).getColumnStyle(0));
        assertNotNull(wbComplex.getSheetAt(0).getColumnStyle(1));

        // Details checks
        HSSFCellStyle bs = wbSimple.getSheetAt(0).getColumnStyle(1);
        assertEquals(62, bs.getIndex());
        assertEquals("#,##0.0_ ;\\-#,##0.0\\ ", bs.getDataFormatString());
        assertEquals("Calibri", bs.getFont(wbSimple).getFontName());
        assertEquals(11*20, bs.getFont(wbSimple).getFontHeight());
        assertEquals(8, bs.getFont(wbSimple).getColor());
        assertFalse(bs.getFont(wbSimple).getItalic());
        assertEquals(HSSFFont.BOLDWEIGHT_NORMAL, bs.getFont(wbSimple).getBoldweight());


        HSSFCellStyle cs = wbComplex.getSheetAt(0).getColumnStyle(1);
        assertEquals(62, cs.getIndex());
        assertEquals("#,##0.0_ ;\\-#,##0.0\\ ", cs.getDataFormatString());
        assertEquals("Arial", cs.getFont(wbComplex).getFontName());
        assertEquals(8*20, cs.getFont(wbComplex).getFontHeight());
        assertEquals(10, cs.getFont(wbComplex).getColor());
        assertFalse(cs.getFont(wbComplex).getItalic());
        assertEquals(HSSFFont.BOLDWEIGHT_BOLD, cs.getFont(wbComplex).getBoldweight());
    }

    /**
     * Tests the arabic setting
     */
    public void testArabic() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();

        assertEquals(false, s.isRightToLeft());
        s.setRightToLeft(true);
        assertEquals(true, s.isRightToLeft());
    }

    public void testAutoFilter(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        InternalWorkbook iwb = wb.getWorkbook();
        InternalSheet ish = sh.getSheet();

        assertNull( iwb.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1) );
        assertNull( ish.findFirstRecordBySid(AutoFilterInfoRecord.sid) );

        CellRangeAddress range = CellRangeAddress.valueOf("A1:B10");
        sh.setAutoFilter(range);

        NameRecord name = iwb.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, 1);
        assertNotNull( name );

        // The built-in name for auto-filter must consist of a single Area3d Ptg.
        Ptg[] ptg = name.getNameDefinition();
        assertEquals("The built-in name for auto-filter must consist of a single Area3d Ptg", 1, ptg.length);
        assertTrue("The built-in name for auto-filter must consist of a single Area3d Ptg", ptg[0] instanceof Area3DPtg);

        Area3DPtg aref = (Area3DPtg)ptg[0];
        assertEquals(range.getFirstColumn(), aref.getFirstColumn());
        assertEquals(range.getFirstRow(), aref.getFirstRow());
        assertEquals(range.getLastColumn(), aref.getLastColumn());
        assertEquals(range.getLastRow(), aref.getLastRow());

        // verify  AutoFilterInfoRecord
        AutoFilterInfoRecord afilter = (AutoFilterInfoRecord)ish.findFirstRecordBySid(AutoFilterInfoRecord.sid);
        assertNotNull(afilter );
        assertEquals(2, afilter.getNumEntries()); //filter covers two columns

    }
}
