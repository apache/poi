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
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.TempFile;

/**
 * Class to test Workbook functionality
 *
 * @author Andrew C. Oliver
 * @author Greg Merrill
 * @author Siggi Cherem
 */
public final class TestWorkbook extends TestCase {
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
    private SanityChecker sanityChecker = new SanityChecker();


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
     *
     */
    public void testWriteSheetSimple() throws IOException {
        File             file = TempFile.createTempFile("testWriteSheetSimple",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;

        for (int rownum = 0; rownum < 100; rownum++) {
            r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(cellnum + 1);
                c.setCellValue(new HSSFRichTextString("TEST"));
            }
        }
        wb.write(out);
        out.close();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());
    }

    /**
     * TEST NAME:  Test Write/Modify Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values,
     *             remove some rows, yet still have a valid file/data.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (74,25).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */

    public void testWriteModifySheetSimple()
        throws IOException
    {
        File             file = TempFile.createTempFile("testWriteSheetSimple",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;

        for (int rownum = 0; rownum < 100; rownum++) {
            r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(cellnum + 1);
                c.setCellValue(new HSSFRichTextString("TEST"));
            }
        }
        for (int rownum = 0; rownum < 25; rownum++) {
            r = s.getRow(rownum);
            s.removeRow(r);
        }
        for (int rownum = 75; rownum < 100; rownum++) {
            r = s.getRow(rownum);
            s.removeRow(r);
        }
        wb.write(out);
        out.close();

        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 74", 74, s.getLastRowNum());
        assertEquals("FIRST ROW == 25", 25, s.getFirstRowNum());
    }

    /**
     * TEST NAME:  Test Read Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (Simple.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions.<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     */

    public void testReadSimple() {
        HSSFWorkbook workbook = openSample("Simple.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);

        HSSFCell cell = sheet.getRow(0).getCell(0);
        assertEquals(REPLACE_ME, cell .getRichStringCellValue().getString());
    }

    /**
     * TEST NAME:  Test Read Simple w/ Data Format<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (SimpleWithDataFormat.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions and format is correct<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     */

    public void testReadSimpleWithDataFormat() {
        HSSFWorkbook workbook = openSample("SimpleWithDataFormat.xls");
        HSSFSheet       sheet    = workbook.getSheetAt(0);
        HSSFDataFormat  format   = workbook.createDataFormat();
        HSSFCell cell = sheet.getRow(0).getCell(0);

        assertEquals(1.25,cell.getNumericCellValue(), 1e-10);

        assertEquals(format.getFormat(cell.getCellStyle().getDataFormat()), "0.0");
    }

/**
     * TEST NAME:  Test Read/Write Simple w/ Data Format<P>
     * OBJECTIVE:  Test that HSSF can write a sheet with custom data formats and then read it and get the proper formats.<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions and format is correct<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     */

    public void testWriteDataFormat()
        throws IOException
    {
    File             file = TempFile.createTempFile("testWriteDataFormat",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;
    HSSFDataFormat format = wb.createDataFormat();
    HSSFCellStyle    cs   = wb.createCellStyle();

    short df = format.getFormat("0.0");
    cs.setDataFormat(df);

    r = s.createRow(0);
    c = r.createCell(0);
    c.setCellStyle(cs);
    c.setCellValue(1.25);

        wb.write(out);
        out.close();

        FileInputStream stream   = new FileInputStream(file);
        POIFSFileSystem fs       = new POIFSFileSystem(stream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fs);
        HSSFSheet       sheet    = workbook.getSheetAt(0);
    HSSFCell    cell     =
                     sheet.getRow(0).getCell(0);
    format = workbook.createDataFormat();

        assertEquals(1.25,cell.getNumericCellValue(), 1e-10);

    assertEquals(format.getFormat(df), "0.0");

    assertEquals(format, workbook.createDataFormat());

        stream.close();
    }

    /**
     * TEST NAME:  Test Read Employee Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (Employee.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions.<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     */

    public void testReadEmployeeSimple() {
        HSSFWorkbook workbook = openSample("Employee.xls");
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getRichStringCellValue().getString());
        assertEquals(LAST_NAME_KEY, sheet.getRow(3).getCell(2).getRichStringCellValue().getString());
        assertEquals(FIRST_NAME_KEY, sheet.getRow(4).getCell(2).getRichStringCellValue().getString());
        assertEquals(SSN_KEY, sheet.getRow(5).getCell(2).getRichStringCellValue().getString());
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
     *
     */

    public void testModifySimple() {
        HSSFWorkbook workbook = openSample("Simple.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFCell cell = sheet.getRow(0).getCell(0);

        cell.setCellValue(new HSSFRichTextString(REPLACED));

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet    = workbook.getSheetAt(0);
        cell     = sheet.getRow(0).getCell(0);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());
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
     *
     */
    public void testModifySimpleWithSkip() {
        HSSFWorkbook workbook = openSample("SimpleWithSkip.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFCell cell = sheet.getRow(0).getCell(1);

        cell.setCellValue(new HSSFRichTextString(REPLACED));
        cell = sheet.getRow(1).getCell(0);
        cell.setCellValue(new HSSFRichTextString(REPLACED));

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        sheet    = workbook.getSheetAt(0);
        cell     = sheet.getRow(0).getCell(1);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(0).getCell(0);
        assertEquals(DO_NOT_REPLACE, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(1).getCell(0);
        assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        cell = sheet.getRow(1).getCell(1);
        assertEquals(DO_NOT_REPLACE, cell.getRichStringCellValue().getString());
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
     *
     */
    public void testModifySimpleWithStyling() {
        HSSFWorkbook workbook = openSample("SimpleWithStyling.xls");
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        for (int k = 0; k < 4; k++)
        {
            HSSFCell cell = sheet.getRow(k).getCell(0);

            cell.setCellValue(new HSSFRichTextString(REPLACED));
        }


        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet    = workbook.getSheetAt(0);
        for (int k = 0; k < 4; k++)
        {
            HSSFCell cell = sheet.getRow(k).getCell(0);

            assertEquals(REPLACED, cell.getRichStringCellValue().getString());
        }
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
     *
     */
    public void testModifyEmployee() {
        HSSFWorkbook workbook = openSample("Employee.xls");
        HSSFSheet       sheet    = workbook.getSheetAt(0);
        HSSFCell        cell     = sheet.getRow(3).getCell(2);

        cell.setCellValue(new HSSFRichTextString(LAST_NAME_VALUE));
        cell = sheet.getRow(4).getCell(2);
        cell.setCellValue(new HSSFRichTextString(FIRST_NAME_VALUE));
        cell = sheet.getRow(5).getCell(2);
        cell.setCellValue(new HSSFRichTextString(SSN_VALUE));

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet    = workbook.getSheetAt(0);
        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getRichStringCellValue().getString());
        assertEquals(LAST_NAME_VALUE, sheet.getRow(3).getCell(2).getRichStringCellValue().getString());
        assertEquals(FIRST_NAME_VALUE, sheet.getRow(4).getCell(2).getRichStringCellValue().getString());
        assertEquals(SSN_VALUE, sheet.getRow(5).getCell(2).getRichStringCellValue().getString());
    }

    /**
     * TEST NAME:  Test Read Sheet with an RK number<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with and RKRecord and correctly
     *             identify the cell as numeric and convert it to a NumberRecord.  <P>
     * SUCCESS:    HSSF reads a sheet.  HSSF returns that the cell is a numeric type cell.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF incorrectly indentifies the cell<P>
     *
     */
    public void testReadSheetWithRK() {
        HSSFWorkbook h = openSample("rk.xls");
        HSSFSheet       s  = h.getSheetAt(0);
        HSSFCell        c  = s.getRow(0).getCell(0);
        int             a  = c.getCellType();

        assertEquals(a, c.CELL_TYPE_NUMERIC);
    }

    /**
     * TEST NAME:  Test Write/Modify Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values,
     *             remove some rows, yet still have a valid file/data.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (74,25).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */
    public void testWriteModifySheetMerged() {
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();

        for (int rownum = 0; rownum < 100; rownum++) {
            HSSFRow r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                HSSFCell c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(cellnum + 1);
                c.setCellValue(new HSSFRichTextString("TEST"));
            }
        }
        s.addMergedRegion(new CellRangeAddress(0, 10, 0, 10));
        s.addMergedRegion(new CellRangeAddress(30, 40, 5, 15));
        sanityChecker.checkHSSFWorkbook(wb);
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        s  = wb.getSheetAt(0);
        CellRangeAddress r1 = s.getMergedRegion(0);
        CellRangeAddress r2 = s.getMergedRegion(1);

        confirmRegion(new CellRangeAddress(0, 10, 0, 10), r1);
        confirmRegion(new CellRangeAddress(30, 40,5, 15), r2);
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
    public void testBackupRecord() {
        HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet();
		InternalWorkbook workbook = wb.getWorkbook();
        BackupRecord record   = workbook.getBackupRecord();

        assertEquals(0, record.getBackup());
        wb.setBackupFlag(true);
        assertEquals(1, record.getBackup());
    }

    private static final class RecordCounter implements RecordVisitor {
        private int _count;

        public RecordCounter() {
            _count=0;
        }
        public int getCount() {
            return _count;
        }
        public void visitRecord(Record r) {
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
    public void testRepeatingBug() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet    sheet    = workbook.createSheet("Design Variants");
        HSSFRow      row      = sheet.createRow(2);
        HSSFCell     cell     = row.createCell(1);

        cell.setCellValue(new HSSFRichTextString("Class"));
        cell = row.createCell(2);

        RecordCounter rc = new RecordCounter();
        sheet.getSheet().visitContainedRecords(rc, 0);
        assertEquals(1, rc.getCount());
    }


    /**
     * Test for row indexes beyond {@link Short#MAX_VALUE}.
     * This bug was first fixed in svn r352609.
     */
    public void testRowIndexesBeyond32768() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row;
        HSSFCell cell;
        for (int i = 32700; i < 32771; i++) {
            row = sheet.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue(i);
        }
        sanityChecker.checkHSSFWorkbook(workbook);
        assertEquals("LAST ROW == 32770", 32770, sheet.getLastRowNum());
        cell = sheet.getRow(32770).getCell(0);
        double lastVal = cell.getNumericCellValue();

        HSSFWorkbook    wb = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        HSSFSheet       s    = wb.getSheetAt(0);
        row = s.getRow(32770);
        cell = row.getCell(0);
        assertEquals("Value from last row == 32770", lastVal, cell.getNumericCellValue(), 0);
        assertEquals("LAST ROW == 32770", 32770, s.getLastRowNum());
    }

    /**
     * Generate a file to visually/programmatically verify repeating rows and cols made it
     */
    public void testRepeatingColsRows() throws IOException
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Print Titles");

        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("hi"));


        workbook.setRepeatingRowsAndColumns(0, 0, 1, 0, 0);

        File file = TempFile.createTempFile("testPrintTitles",".xls");

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();

        assertTrue("file exists",file.exists());
    }

}
