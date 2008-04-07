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
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;

/**
 * Class to test Workbook functionality
 *
 * @author Andrew C. Oliver
 * @author Greg Merrill
 * @author Siggi Cherem
 */
public class TestWorkbook extends TestCase {
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

    public void testWriteSheetSimple()
        throws IOException
    {
        File             file = TempFile.createTempFile("testWriteSheetSimple",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;

        for (short rownum = ( short ) 0; rownum < 100; rownum++)
        {
            r = s.createRow(rownum);

            // r.setRowNum(( short ) rownum);
            for (short cellnum = ( short ) 0; cellnum < 50; cellnum += 2)
            {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(( short ) (cellnum + 1));
                c.setCellValue("TEST");
            }
        }
        wb.write(out);
        out.close();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());

        // assert((s.getLastRowNum() == 99));
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

        for (short rownum = ( short ) 0; rownum < 100; rownum++)
        {
            r = s.createRow(rownum);

            // r.setRowNum(( short ) rownum);
            for (short cellnum = ( short ) 0; cellnum < 50; cellnum += 2)
            {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(( short ) (cellnum + 1));
                c.setCellValue("TEST");
            }
        }
        for (short rownum = ( short ) 0; rownum < 25; rownum++)
        {
            r = s.getRow(rownum);
            s.removeRow(r);
        }
        for (short rownum = ( short ) 75; rownum < 100; rownum++)
        {
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

    r = s.createRow((short)0);
    c = r.createCell((short)0);
    c.setCellStyle(cs);
    c.setCellValue(1.25);

        wb.write(out);
        out.close();

        FileInputStream stream   = new FileInputStream(file);
        POIFSFileSystem fs       = new POIFSFileSystem(stream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fs);
        HSSFSheet       sheet    = workbook.getSheetAt(0);
    HSSFCell    cell     =
                     sheet.getRow(( short ) 0).getCell(( short ) 0);
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

        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getStringCellValue());
        assertEquals(LAST_NAME_KEY, sheet.getRow(3).getCell(2).getStringCellValue());
        assertEquals(FIRST_NAME_KEY, sheet.getRow(4).getCell(2).getStringCellValue());
        assertEquals(SSN_KEY, sheet.getRow(5).getCell(2).getStringCellValue());
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

        cell.setCellValue(REPLACED);
        cell = sheet.getRow(1).getCell(0);
        cell.setCellValue(REPLACED);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        sheet    = workbook.getSheetAt(0);
        cell     = sheet.getRow(0).getCell(1);
        assertEquals(REPLACED, cell.getStringCellValue());
        cell = sheet.getRow(0).getCell(0);
        assertEquals(DO_NOT_REPLACE, cell.getStringCellValue());
        cell = sheet.getRow(1).getCell(0);
        assertEquals(REPLACED, cell.getStringCellValue());
        cell = sheet.getRow(1).getCell(1);
        assertEquals(DO_NOT_REPLACE, cell.getStringCellValue());
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
            HSSFCell cell = sheet.getRow(( short ) k).getCell(( short ) 0);

            cell.setCellValue(new HSSFRichTextString(REPLACED));
        }


        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet    = workbook.getSheetAt(0);
        for (int k = 0; k < 4; k++)
        {
            HSSFCell cell = sheet.getRow(( short ) k).getCell(( short ) 0);

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

        cell.setCellValue(LAST_NAME_VALUE);
        cell = sheet.getRow(4).getCell(2);
        cell.setCellValue(FIRST_NAME_VALUE);
        cell = sheet.getRow(5).getCell(2);
        cell.setCellValue(SSN_VALUE);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet    = workbook.getSheetAt(0);
        assertEquals(EMPLOYEE_INFORMATION, sheet.getRow(1).getCell(1).getStringCellValue());
        assertEquals(LAST_NAME_VALUE, sheet.getRow(3).getCell(2).getStringCellValue());
        assertEquals(FIRST_NAME_VALUE, sheet.getRow(4).getCell(2).getStringCellValue());
        assertEquals(SSN_VALUE, sheet.getRow(5).getCell(2).getStringCellValue());
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

    public void testWriteModifySheetMerged()
        throws IOException
    {
        File             file = TempFile.createTempFile("testWriteSheetMerged",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        FileInputStream  in   = null;
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;

        for (short rownum = ( short ) 0; rownum < 100; rownum++)
        {
            r = s.createRow(rownum);

            for (short cellnum = ( short ) 0; cellnum < 50; cellnum += 2)
            {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(( short ) (cellnum + 1));
                c.setCellValue(new HSSFRichTextString("TEST"));
            }
        }
        s.addMergedRegion(new Region(( short ) 0, ( short ) 0, ( short ) 10,
                                     ( short ) 10));
        s.addMergedRegion(new Region(( short ) 30, ( short ) 5, ( short ) 40,
                                     ( short ) 15));
        wb.write(out);
        out.close();
        sanityChecker.checkHSSFWorkbook(wb);
        in = new FileInputStream(file);
        wb = new HSSFWorkbook(new POIFSFileSystem(in));
        s  = wb.getSheetAt(0);
        Region r1 = s.getMergedRegionAt(0);
        Region r2 = s.getMergedRegionAt(1);

        in.close();

        // System.out.println(file.length());
        // assertEquals("FILE LENGTH == 87552",file.length(), 87552);
        // System.out.println(s.getLastRowNum());
        assertEquals("REGION1 = 0,0,10,10", 0,
                     new Region(( short ) 0, ( short ) 0, ( short ) 10,
                                ( short ) 10).compareTo(r1));
        assertEquals("REGION2 == 30,5,40,15", 0,
                     new Region(( short ) 30, ( short ) 5, ( short ) 40,
                                ( short ) 15).compareTo(r2));
    }

    /**
     * Test the backup field gets set as expected.
     */

    public void testBackupRecord()
        throws Exception
    {
        HSSFWorkbook wb       = new HSSFWorkbook();
        wb.createSheet();
        Workbook     workbook = wb.getWorkbook();
        BackupRecord record   = workbook.getBackupRecord();

        assertEquals(0, record.getBackup());
        wb.setBackupFlag(true);
        assertEquals(1, record.getBackup());
    }

    /**
     * This tests is for bug [ #506658 ] Repeating output.
     *
     * We need to make sure only one LabelSSTRecord is produced.
     */

    public void testRepeatingBug()
        throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet    sheet    = workbook.createSheet("Design Variants");
        HSSFRow      row      = sheet.createRow(( short ) 2);
        HSSFCell     cell     = row.createCell(( short ) 1);

        cell.setCellValue(new HSSFRichTextString("Class"));
        cell = row.createCell(( short ) 2);

        // workbook.write(new FileOutputStream("/a2.xls"));
        ValueRecordsAggregate valueAggregate =
            ( ValueRecordsAggregate ) sheet.getSheet()
                .findFirstRecordBySid(ValueRecordsAggregate.sid);
        int                   sstRecords     = 0;
        Iterator              iterator       = valueAggregate.getIterator();

        while (iterator.hasNext())
        {
            if ((( Record ) iterator.next()).getSid() == LabelSSTRecord.sid)
            {
                sstRecords++;
            }
        }
        assertEquals(1, sstRecords);
    }


    public void testManyRows()
        throws Exception
    {
        String testName = "TestManyRows";
        File file = TempFile.createTempFile(testName, ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = null;
        HSSFCell cell = null;
        int i, j;
        for ( i = 0, j = 32771; j > 0; i++, j-- )
        {
            row = sheet.createRow(i);
            cell = row.createCell((short) 0);
            cell.setCellValue(i);
        }
        workbook.write(out);
        out.close();
        sanityChecker.checkHSSFWorkbook(workbook);
        assertEquals("LAST ROW == 32770", 32770, sheet.getLastRowNum());
        double lastVal = cell.getNumericCellValue();

        FileInputStream in      = new FileInputStream(file);
        POIFSFileSystem fs       = new POIFSFileSystem(in);
        HSSFWorkbook    wb = new HSSFWorkbook(fs);
        HSSFSheet       s    = wb.getSheetAt(0);
        row = s.getRow(32770);
        cell = row.getCell(( short ) 0);
        assertEquals("Value from last row == 32770", lastVal, cell.getNumericCellValue(), 0);
        assertEquals("LAST ROW == 32770", 32770, s.getLastRowNum());
        in.close();
        file.deleteOnExit();
    }

    /**
     * Generate a file to visually/programmatically verify repeating rows and cols made it
     */
    public void testRepeatingColsRows() throws IOException
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Print Titles");

        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell((short)1);
        cell.setCellValue(new HSSFRichTextString("hi"));


        workbook.setRepeatingRowsAndColumns(0, 0, 1, 0, 0);

        File file = TempFile.createTempFile("testPrintTitles",".xls");

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();

        assertTrue("file exists",file.exists());
    }


    public static void main(String [] ignored_args)
    {
        junit.textui.TestRunner.run(TestWorkbook.class);
    }
}
