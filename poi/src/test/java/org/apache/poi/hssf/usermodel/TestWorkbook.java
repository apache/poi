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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Class to test Workbook functionality
 */
final class TestWorkbook {
    private static final String LAST_NAME_KEY        = "lastName";
    private static final String FIRST_NAME_KEY       = "firstName";
    private static final String SSN_KEY              = "ssn";
    private static final String REPLACE_ME           = "replaceMe";
    private static final String REPLACED             = "replaced";
    private static final String DO_NOT_REPLACE       = "doNotReplace";
    private static final String EMPLOYEE_INFORMATION = "Employee Info";
    private static final String LAST_NAME_VALUE      = "Bush";
    private static final String FIRST_NAME_VALUE     = "George";
    private static final String SSN_VALUE            = "555555555";
    private final SanityChecker sanityChecker = new SanityChecker();


    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    /**
     * TEST NAME:  Test Write Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    @Test
    void testWriteSheetSimple() throws IOException {
        try (HSSFWorkbook wb1  = new HSSFWorkbook()) {
            HSSFSheet s = wb1.createSheet();

            populateSheet(s);

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sanityChecker.checkHSSFWorkbook(wb1);
                assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
                assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");

                sanityChecker.checkHSSFWorkbook(wb2);
                s = wb2.getSheetAt(0);
                assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
                assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");
            }
        }
    }

    /**
     * TEST NAME:  Test Write/Modify Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values,
     *             remove some rows, yet still have a valid file/data.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (74,25).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    @Test
    void testWriteModifySheetSimple() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet s = wb1.createSheet();

        populateSheet(s);

        for (int rownum = 0; rownum < 25; rownum++) {
            HSSFRow r = s.getRow(rownum);
            s.removeRow(r);
        }
        for (int rownum = 75; rownum < 100; rownum++) {
            HSSFRow r = s.getRow(rownum);
            s.removeRow(r);
        }

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);

        sanityChecker.checkHSSFWorkbook(wb1);
        assertEquals(74, s.getLastRowNum(), "LAST ROW == 74");
        assertEquals(25, s.getFirstRowNum(), "FIRST ROW == 25");

        sanityChecker.checkHSSFWorkbook(wb2);
        s = wb2.getSheetAt(0);
        assertEquals(74, s.getLastRowNum(), "LAST ROW == 74");
        assertEquals(25, s.getFirstRowNum(), "FIRST ROW == 25");

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Read Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (Simple.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions.<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     */
    @Test
    void testReadSimple() throws IOException {
        HSSFWorkbook wb = openSample("Simple.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFCell cell = sheet.getRow(0).getCell(0);
        assertEquals(REPLACE_ME, cell .getRichStringCellValue().getString());
        wb.close();
    }

    /**
     * TEST NAME:  Test Read Simple w/ Data Format<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (SimpleWithDataFormat.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions and format is correct<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     */
    @Test
    void testReadSimpleWithDataFormat() throws IOException {
        HSSFWorkbook wb = openSample("SimpleWithDataFormat.xls");
        HSSFSheet       sheet    = wb.getSheetAt(0);
        HSSFDataFormat  format   = wb.createDataFormat();
        HSSFCell cell = sheet.getRow(0).getCell(0);

        assertEquals(1.25,cell.getNumericCellValue(), 1e-10);

        assertEquals(format.getFormat(cell.getCellStyle().getDataFormat()), "0.0");

        wb.close();
    }

/**
     * TEST NAME:  Test Read/Write Simple w/ Data Format<P>
     * OBJECTIVE:  Test that HSSF can write a sheet with custom data formats and then read it and get the proper formats.<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions and format is correct<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     */
    @Test
    void testWriteDataFormat() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet s1 = wb1.createSheet();
        HSSFDataFormat format = wb1.createDataFormat();
        HSSFCellStyle cs = wb1.createCellStyle();

        short df = format.getFormat("0.0");
        cs.setDataFormat(df);

        HSSFCell c1 = s1.createRow(0).createCell(0);
        c1.setCellStyle(cs);
        c1.setCellValue(1.25);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();

        HSSFSheet s2 = wb2.getSheetAt(0);
        HSSFCell c2 = s2.getRow(0).getCell(0);
        format = wb2.createDataFormat();

        assertEquals(1.25, c2.getNumericCellValue(), 1e-10);

        assertEquals(format.getFormat(df), "0.0");

        assertEquals(format, wb2.createDataFormat());

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Read Employee Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (Employee.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions.<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     */
    @Test
    void testReadEmployeeSimple() throws IOException {
        HSSFWorkbook wb = openSample("Employee.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getRichStringCellValue().getString());
        assertEquals(LAST_NAME_KEY, sheet.getRow(3).getCell(2).getRichStringCellValue().getString());
        assertEquals(FIRST_NAME_KEY, sheet.getRow(4).getCell(2).getRichStringCellValue().getString());
        assertEquals(SSN_KEY, sheet.getRow(5).getCell(2).getRichStringCellValue().getString());

        wb.close();
    }

    /**
     * TEST NAME:  Test Modify Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with a string value and replace
     *             it with another string value.<P>
     * SUCCESS:    HSSF reads a sheet.  HSSF replaces the cell value with another cell value. HSSF
     *             writes the sheet out to another file.  HSSF reads the result and ensures the value
     *             has been properly replaced.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF does not write the sheet or excepts.
     *             HSSF does not re-read the sheet or excepts.  Upon re-reading the sheet the value
     *             is incorrect or has not been replaced. <P>
     */
    @Test
    void testModifySimple() throws IOException {
        HSSFWorkbook wb1 = openSample("Simple.xls");
        HSSFSheet sheet = wb1.getSheetAt(0);
        HSSFCell cell = sheet.getRow(0).getCell(0);

        cell.setCellValue(new HSSFRichTextString(REPLACED));

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        sheet    = wb2.getSheetAt(0);
        cell     = sheet.getRow(0).getCell(0);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Modify Sheet Simple With Skipped cells<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with string values and replace
     *             them with other string values while not replacing other cells.<P>
     * SUCCESS:    HSSF reads a sheet.  HSSF replaces the cell value with another cell value. HSSF
     *             writes the sheet out to another file.  HSSF reads the result and ensures the value
     *             has been properly replaced and unreplaced values are still unreplaced.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF does not write the sheet or excepts.
     *             HSSF does not re-read the sheet or excepts.  Upon re-reading the sheet the value
     *             is incorrect or has not been replaced or the incorrect cell has its value replaced
     *             or is incorrect. <P>
     */
    @Test
    void testModifySimpleWithSkip() throws IOException {
        HSSFWorkbook wb1 = openSample("SimpleWithSkip.xls");
        HSSFSheet sheet = wb1.getSheetAt(0);
        HSSFCell cell = sheet.getRow(0).getCell(1);

        cell.setCellValue(new HSSFRichTextString(REPLACED));
        cell = sheet.getRow(1).getCell(0);
        cell.setCellValue(new HSSFRichTextString(REPLACED));

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);

        sheet    = wb2.getSheetAt(0);
        cell     = sheet.getRow(0).getCell(1);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(0).getCell(0);
        assertEquals(DO_NOT_REPLACE, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(1).getCell(0);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(1).getCell(1);
        assertEquals(DO_NOT_REPLACE, cell.getRichStringCellValue().getString());

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Modify Sheet With Styling<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with string values and replace
     *             them with other string values despite any styling.  In this release of HSSF styling will
     *             probably be lost and is NOT tested.<P>
     * SUCCESS:    HSSF reads a sheet.  HSSF replaces the cell values with other cell values. HSSF
     *             writes the sheet out to another file.  HSSF reads the result and ensures the value
     *             has been properly replaced.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF does not write the sheet or excepts.
     *             HSSF does not re-read the sheet or excepts.  Upon re-reading the sheet the value
     *             is incorrect or has not been replaced. <P>
     */
    @Test
    void testModifySimpleWithStyling() throws IOException {
        HSSFWorkbook wb1 = openSample("SimpleWithStyling.xls");
        HSSFSheet  sheet = wb1.getSheetAt(0);

        for (int k = 0; k < 4; k++) {
            HSSFCell cell = sheet.getRow(k).getCell(0);
            cell.setCellValue(new HSSFRichTextString(REPLACED));
        }

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        sheet    = wb2.getSheetAt(0);
        for (int k = 0; k < 4; k++) {
            HSSFCell cell = sheet.getRow(k).getCell(0);
            assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        }

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Modify Employee Sheet<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with string values and replace
     *             them with other string values despite any styling.  In this release of HSSF styling will
     *             probably be lost and is NOT tested.<P>
     * SUCCESS:    HSSF reads a sheet.  HSSF replaces the cell values with other cell values. HSSF
     *             writes the sheet out to another file.  HSSF reads the result and ensures the value
     *             has been properly replaced.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF does not write the sheet or excepts.
     *             HSSF does not re-read the sheet or excepts.  Upon re-reading the sheet the value
     *             is incorrect or has not been replaced. <P>
     */
    @Test
    void testModifyEmployee() throws IOException {
        HSSFWorkbook wb1 = openSample("Employee.xls");
        HSSFSheet  sheet = wb1.getSheetAt(0);
        HSSFCell    cell = sheet.getRow(3).getCell(2);

        cell.setCellValue(new HSSFRichTextString(LAST_NAME_VALUE));
        cell = sheet.getRow(4).getCell(2);
        cell.setCellValue(new HSSFRichTextString(FIRST_NAME_VALUE));
        cell = sheet.getRow(5).getCell(2);
        cell.setCellValue(new HSSFRichTextString(SSN_VALUE));

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        sheet = wb2.getSheetAt(0);
        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getRichStringCellValue().getString());
        assertEquals(LAST_NAME_VALUE, sheet.getRow(3).getCell(2).getRichStringCellValue().getString());
        assertEquals(FIRST_NAME_VALUE, sheet.getRow(4).getCell(2).getRichStringCellValue().getString());
        assertEquals(SSN_VALUE, sheet.getRow(5).getCell(2).getRichStringCellValue().getString());

        wb2.close();
        wb1.close();
    }

    /**
     * TEST NAME:  Test Read Sheet with an RK number<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with and RKRecord and correctly
     *             identify the cell as numeric and convert it to a NumberRecord.  <P>
     * SUCCESS:    HSSF reads a sheet.  HSSF returns that the cell is a numeric type cell.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF incorrectly identifies the cell<P>
     */
    @Test
    void testReadSheetWithRK() throws IOException {
        HSSFWorkbook wb = openSample("rk.xls");
        HSSFSheet    s  = wb.getSheetAt(0);
        HSSFCell     c  = s.getRow(0).getCell(0);

        assertEquals(CellType.NUMERIC, c.getCellType());

        wb.close();
    }

    /**
     * TEST NAME:  Test Write/Modify Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values,
     *             remove some rows, yet still have a valid file/data.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (74,25).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    @Test
    void testWriteModifySheetMerged() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet    s   = wb1.createSheet();

        populateSheet(s);

        assertEquals(0, s.addMergedRegion(new CellRangeAddress(0, 10, 0, 10)));
        assertEquals(1, s.addMergedRegion(new CellRangeAddress(30, 40, 5, 15)));
        sanityChecker.checkHSSFWorkbook(wb1);
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);

        s  = wb2.getSheetAt(0);
        CellRangeAddress r1 = s.getMergedRegion(0);
        CellRangeAddress r2 = s.getMergedRegion(1);

        confirmRegion(new CellRangeAddress(0, 10, 0, 10), r1);
        confirmRegion(new CellRangeAddress(30, 40,5, 15), r2);

        wb2.close();
        wb1.close();
    }

    private void populateSheet(Sheet s) {
        for (int rownum = 0; rownum < 100; rownum++) {
            Row r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                Cell c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                        + ((( double ) rownum / 1000)
                        + (( double ) cellnum / 10000)));
                c = r.createCell(cellnum + 1);
                c.setCellValue(new HSSFRichTextString("TEST"));
            }
        }
    }

    private static void confirmRegion(CellRangeAddress ra, CellRangeAddress rb) {
        assertEquals(ra.getFirstRow(), rb.getFirstRow());
        assertEquals(ra.getLastRow(), rb.getLastRow());
        assertEquals(ra.getFirstColumn(), rb.getFirstColumn());
        assertEquals(ra.getLastColumn(), rb.getLastColumn());
    }

    /**
     * Test the backup field gets set as expected.
     */
    @Test
    void testBackupRecord() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet();
		InternalWorkbook workbook = wb.getWorkbook();
        BackupRecord record   = workbook.getBackupRecord();

        assertEquals(0, record.getBackup());
        assertFalse(wb.getBackupFlag());
        wb.setBackupFlag(true);
        assertEquals(1, record.getBackup());
        assertTrue(wb.getBackupFlag());
        wb.setBackupFlag(false);
        assertEquals(0, record.getBackup());
        assertFalse(wb.getBackupFlag());

        wb.close();
    }

    private static final class RecordCounter implements RecordVisitor {
        private int _count;

        public RecordCounter() {
            _count=0;
        }
        public int getCount() {
            return _count;
        }
        @Override
        public void visitRecord(org.apache.poi.hssf.record.Record r) {
            if (r instanceof LabelSSTRecord) {
                _count++;
            }
        }
    }

    /**
     * This tests is for bug [ #506658 ] Repeating output.
     *
     * We need to make sure only one LabelSSTRecord is produced.
     */
    @Test
    void testRepeatingBug() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet    sheet    = workbook.createSheet("Design Variants");
        HSSFRow      row      = sheet.createRow(2);
        HSSFCell     cell     = row.createCell(1);

        cell.setCellValue(new HSSFRichTextString("Class"));
        cell = row.createCell(2);
        assertNotNull(cell);

        RecordCounter rc = new RecordCounter();
        sheet.getSheet().visitContainedRecords(rc, 0);
        assertEquals(1, rc.getCount());

        workbook.close();
    }


    /**
     * Test for row indexes beyond {@link Short#MAX_VALUE}.
     * This bug was first fixed in svn r352609.
     */
    @Test
    void testRowIndexesBeyond32768() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sheet = wb1.createSheet();
        HSSFRow row;
        HSSFCell cell;
        for (int i = 32700; i < 32771; i++) {
            row = sheet.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue(i);
        }
        sanityChecker.checkHSSFWorkbook(wb1);
        assertEquals(32770, sheet.getLastRowNum(), "LAST ROW == 32770");
        cell = sheet.getRow(32770).getCell(0);
        double lastVal = cell.getNumericCellValue();

        HSSFWorkbook    wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        HSSFSheet       s    = wb2.getSheetAt(0);
        row = s.getRow(32770);
        cell = row.getCell(0);
        assertEquals(lastVal, cell.getNumericCellValue(), 0, "Value from last row == 32770");
        assertEquals(32770, s.getLastRowNum(), "LAST ROW == 32770");

        wb2.close();
        wb1.close();
    }

    /**
     * Generate a file to verify repeating rows and cols made it
     */
    @Test
    void testRepeatingColsRows() throws IOException {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        HSSFSheet sheet = wb1.createSheet("Test Print Titles");

        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("hi"));

        CellRangeAddress cra = CellRangeAddress.valueOf("A1:B1");
        sheet.setRepeatingColumns(cra);
        sheet.setRepeatingRows(cra);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        sheet = wb2.getSheetAt(0);
        assertEquals("A:B", sheet.getRepeatingColumns().formatAsString());
        assertEquals("1:1", sheet.getRepeatingRows().formatAsString());

        wb2.close();
        wb1.close();
    }

    /**
     * Test setRepeatingRowsAndColumns when startRow and startColumn are -1.
     */
    @Test
    void testRepeatingColsRowsMinusOne() throws IOException
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Print Titles");

        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("hi"));
        CellRangeAddress cra = new CellRangeAddress(-1, 1, -1, 1);
        assertThrows(IllegalArgumentException.class, () -> sheet.setRepeatingColumns(cra), "invalid start index is ignored");
        assertThrows(IllegalArgumentException.class, () -> sheet.setRepeatingRows(cra), "invalid start index is ignored");

        sheet.setRepeatingColumns(null);
        sheet.setRepeatingRows(null);

        HSSFTestDataSamples.writeOutAndReadBack(workbook).close();

        workbook.close();
    }

    @Test
    void testBug58085RemoveSheetWithNames() throws Exception {
        HSSFWorkbook wb1 = new HSSFWorkbook();
        Sheet sheet1 = wb1.createSheet("sheet1");
        Sheet sheet2 = wb1.createSheet("sheet2");
        Sheet sheet3 = wb1.createSheet("sheet3");

        sheet1.createRow(0).createCell((short) 0).setCellValue("val1");
        sheet2.createRow(0).createCell((short) 0).setCellValue("val2");
        sheet3.createRow(0).createCell((short) 0).setCellValue("val3");

        Name namedCell1 = wb1.createName();
        namedCell1.setNameName("name1");
        String reference1 = "sheet1!$A$1";
        namedCell1.setRefersToFormula(reference1);

        Name namedCell2= wb1.createName();
        namedCell2.setNameName("name2");
        String reference2 = "sheet2!$A$1";
        namedCell2.setRefersToFormula(reference2);

        Name namedCell3 = wb1.createName();
        namedCell3.setNameName("name3");
        String reference3 = "sheet3!$A$1";
        namedCell3.setRefersToFormula(reference3);

        Workbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();

        Name nameCell = wb2.getName("name1");
        assertNotNull(nameCell);
        assertEquals("sheet1!$A$1", nameCell.getRefersToFormula());
        nameCell = wb2.getName("name2");
        assertNotNull(nameCell);
        assertEquals("sheet2!$A$1", nameCell.getRefersToFormula());
        nameCell = wb2.getName("name3");
        assertNotNull(nameCell);
        assertEquals("sheet3!$A$1", nameCell.getRefersToFormula());

        wb2.removeSheetAt(wb2.getSheetIndex("sheet1"));

        nameCell = wb2.getName("name1");
        assertNotNull(nameCell);
        assertEquals("#REF!$A$1", nameCell.getRefersToFormula());
        nameCell = wb2.getName("name2");
        assertNotNull(nameCell);
        assertEquals("sheet2!$A$1", nameCell.getRefersToFormula());
        nameCell = wb2.getName("name3");
        assertNotNull(nameCell);
        assertEquals("sheet3!$A$1", nameCell.getRefersToFormula());

        wb2.close();
    }
}
