
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.usermodel;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.util.*;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate;

/**
 * Class to test Workbook functionality
 *
 * @author Andrew C. Oliver
 */

public class TestWorkbook
    extends TestCase
{
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

    /**
     * Constructor TestWorkbook
     *
     * @param name
     */

    public TestWorkbook(String name)
    {
        super(name);
    }

    /**
     * TEST NAME:  Test Write Sheet Simple <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     * @author Andrew C. Oliver
     */

    public void testWriteSheetSimple()
        throws IOException
    {
        File             file = File.createTempFile("testWriteSheetSimple",
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
        assertEquals("FILE LENGTH == 87040", 87040,
                     file.length());   // changed because of new sheet behavior
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
     * @author Andrew C. Oliver
     */

    public void testWriteModifySheetSimple()
        throws IOException
    {
        File             file = File.createTempFile("testWriteSheetSimple",
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

        // System.out.println(file.length());
        // assertEquals("FILE LENGTH == 87552",file.length(), 87552);
        // System.out.println(s.getLastRowNum());
        assertEquals("FILE LENGTH == 45568", 45568,
                     file.length());   // changed due to new sheet behavior (<3)
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
     * @author Greg Merrill
     * @author Andrew C. Oliver
     */

    public void testReadSimple()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/Simple.xls";
        FileInputStream stream   = new FileInputStream(filename);
        POIFSFileSystem fs       = new POIFSFileSystem(stream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fs);
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        assertEquals(REPLACE_ME,
                     sheet.getRow(( short ) 0).getCell(( short ) 0)
                         .getStringCellValue());
        stream.close();
    }

    /**
     * TEST NAME:  Test Read Employee Simple <P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet (Employee.xls).<P>
     * SUCCESS:    HSSF reads the sheet.  Matches values in their particular positions.<P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF cannot identify values
     *             in the sheet in their known positions.<P>
     *
     * @author Greg Merrill
     * @author Andrew C. Oliver
     */

    public void testReadEmployeeSimple()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/Employee.xls";
        FileInputStream stream   = new FileInputStream(filename);
        POIFSFileSystem fs       = new POIFSFileSystem(stream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fs);
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        assertEquals(EMPLOYEE_INFORMATION,
                     sheet.getRow(1).getCell(( short ) 1)
                         .getStringCellValue());
        assertEquals(LAST_NAME_KEY,
                     sheet.getRow(3).getCell(( short ) 2)
                         .getStringCellValue());
        assertEquals(FIRST_NAME_KEY,
                     sheet.getRow(4).getCell(( short ) 2)
                         .getStringCellValue());
        assertEquals(SSN_KEY,
                     sheet.getRow(5).getCell(( short ) 2)
                         .getStringCellValue());
        stream.close();
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
     * @author Andrew C. Oliver
     * @author Greg Merrill
     */

    public void testModifySimple()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/Simple.xls";
        FileInputStream instream = new FileInputStream(filename);
        POIFSFileSystem fsin     = new POIFSFileSystem(instream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fsin);
        HSSFSheet       sheet    = workbook.getSheetAt(0);
        HSSFCell        cell     =
            sheet.getRow(( short ) 0).getCell(( short ) 0);

        cell.setCellValue(REPLACED);
        File             destination = File.createTempFile("SimpleResult",
                                           ".xls");
        FileOutputStream outstream   = new FileOutputStream(destination);

        workbook.write(outstream);
        instream.close();
        outstream.close();
        instream = new FileInputStream(destination);
        workbook = new HSSFWorkbook(new POIFSFileSystem(instream));
        sheet    = workbook.getSheetAt(0);
        cell     = sheet.getRow(( short ) 0).getCell(( short ) 0);
        assertEquals(REPLACED, cell.getStringCellValue());
        instream.close();
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
     * @author Andrew C. Oliver
     * @author Greg Merrill
     */

    public void testModifySimpleWithSkip()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/SimpleWithSkip.xls";
        FileInputStream instream = new FileInputStream(filename);
        POIFSFileSystem fsin     = new POIFSFileSystem(instream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fsin);
        HSSFSheet       sheet    = workbook.getSheetAt(0);
        HSSFCell        cell     =
            sheet.getRow(( short ) 0).getCell(( short ) 1);

        cell.setCellValue(REPLACED);
        cell = sheet.getRow(( short ) 1).getCell(( short ) 0);
        cell.setCellValue(REPLACED);
        File             destination =
            File.createTempFile("SimpleWithSkipResult", ".xls");
        FileOutputStream outstream   = new FileOutputStream(destination);

        workbook.write(outstream);
        instream.close();
        outstream.close();
        instream = new FileInputStream(destination);
        workbook = new HSSFWorkbook(new POIFSFileSystem(instream));
        sheet    = workbook.getSheetAt(0);
        cell     = sheet.getRow(( short ) 0).getCell(( short ) 1);
        assertEquals(REPLACED, cell.getStringCellValue());
        cell = sheet.getRow(( short ) 0).getCell(( short ) 0);
        assertEquals(DO_NOT_REPLACE, cell.getStringCellValue());
        cell = sheet.getRow(( short ) 1).getCell(( short ) 0);
        assertEquals(REPLACED, cell.getStringCellValue());
        cell = sheet.getRow(( short ) 1).getCell(( short ) 1);
        assertEquals(DO_NOT_REPLACE, cell.getStringCellValue());
        instream.close();
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
     * @author Andrew C. Oliver
     * @author Greg Merrill
     */

    public void testModifySimpleWithStyling()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/SimpleWithStyling.xls";
        FileInputStream instream = new FileInputStream(filename);
        POIFSFileSystem fsin     = new POIFSFileSystem(instream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fsin);
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        for (int k = 0; k < 4; k++)
        {
            HSSFCell cell = sheet.getRow(( short ) k).getCell(( short ) 0);

            cell.setCellValue(REPLACED);
        }
        File             destination =
            File.createTempFile("SimpleWithStylingResult", ".xls");
        FileOutputStream outstream   = new FileOutputStream(destination);

        workbook.write(outstream);
        instream.close();
        outstream.close();
        instream = new FileInputStream(destination);
        workbook = new HSSFWorkbook(new POIFSFileSystem(instream));
        sheet    = workbook.getSheetAt(0);
        for (int k = 0; k < 4; k++)
        {
            HSSFCell cell = sheet.getRow(( short ) k).getCell(( short ) 0);

            assertEquals(REPLACED, cell.getStringCellValue());
        }
        instream.close();
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
     * @author Andrew C. Oliver
     * @author Greg Merrill
     */

    public void testModifyEmployee()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/Employee.xls";
        FileInputStream instream = new FileInputStream(filename);
        POIFSFileSystem fsin     = new POIFSFileSystem(instream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fsin);
        HSSFSheet       sheet    = workbook.getSheetAt(0);
        HSSFCell        cell     =
            sheet.getRow(( short ) 3).getCell(( short ) 2);

        cell.setCellValue(LAST_NAME_VALUE);
        cell = sheet.getRow(( short ) 4).getCell(( short ) 2);
        cell.setCellValue(FIRST_NAME_VALUE);
        cell = sheet.getRow(( short ) 5).getCell(( short ) 2);
        cell.setCellValue(SSN_VALUE);
        File             destination = File.createTempFile("EmployeeResult",
                                           ".xls");
        FileOutputStream outstream   = new FileOutputStream(destination);

        workbook.write(outstream);
        instream.close();
        outstream.close();
        instream = new FileInputStream(destination);
        workbook = new HSSFWorkbook(new POIFSFileSystem(instream));
        sheet    = workbook.getSheetAt(0);
        assertEquals(EMPLOYEE_INFORMATION,
                     sheet.getRow(1).getCell(( short ) 1)
                         .getStringCellValue());
        assertEquals(LAST_NAME_VALUE,
                     sheet.getRow(3).getCell(( short ) 2)
                         .getStringCellValue());
        assertEquals(FIRST_NAME_VALUE,
                     sheet.getRow(4).getCell(( short ) 2)
                         .getStringCellValue());
        assertEquals(SSN_VALUE,
                     sheet.getRow(5).getCell(( short ) 2)
                         .getStringCellValue());
        instream.close();
    }

    /**
     * TEST NAME:  Test Read Sheet with an RK number<P>
     * OBJECTIVE:  Test that HSSF can read a simple spreadsheet with and RKRecord and correctly
     *             identify the cell as numeric and convert it to a NumberRecord.  <P>
     * SUCCESS:    HSSF reads a sheet.  HSSF returns that the cell is a numeric type cell.    <P>
     * FAILURE:    HSSF does not read a sheet or excepts.  HSSF incorrectly indentifies the cell<P>
     *
     * @author Siggi Cherem
     * @author Andrew C. Oliver (acoliver at apache dot org)
     */

    public void testReadSheetWithRK()
        throws IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/rk.xls";

        // a.xls has a value on position (0,0)
        FileInputStream in = new FileInputStream(filename);
        POIFSFileSystem fs = new POIFSFileSystem(in);
        HSSFWorkbook    h  = new HSSFWorkbook(fs);
        HSSFSheet       s  = h.getSheetAt(0);
        HSSFRow         r  = s.getRow(0);
        HSSFCell        c  = r.getCell(( short ) 0);
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
     * @author Andrew C. Oliver
     */

    public void testWriteModifySheetMerged()
        throws IOException
    {
        File             file = File.createTempFile("testWriteSheetMerged",
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
        s.addMergedRegion(new Region(( short ) 0, ( short ) 0, ( short ) 10,
                                     ( short ) 10));
        s.addMergedRegion(new Region(( short ) 30, ( short ) 5, ( short ) 40,
                                     ( short ) 15));
        wb.write(out);
        out.close();
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
        HSSFSheet    s        = wb.createSheet();
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

        cell.setCellValue("Class");
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

    public static void main(String [] ignored_args)
    {
        String filename = System.getProperty("HSSF.testdata.path");

        // assume andy is running this in the debugger
        if (filename == null)
        {
            System.setProperty(
                "HSSF.testdata.path",
                "/home/andy/npoi3/poi/production/testcases/net/sourceforge/poi/hssf/data");
        }
        System.out
            .println("Testing org.apache.poi.hssf.usermodel.HSSFWorkbook");
        junit.textui.TestRunner.run(TestWorkbook.class);
    }
}
