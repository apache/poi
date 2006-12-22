
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
        

/*
 * TestCellStyle.java
 *
 * Created on December 11, 2001, 5:51 PM
 */
package org.apache.poi.hssf.usermodel;

import java.io.*;

import java.util.*;

import junit.framework.*;
import org.apache.poi.util.TempFile;

/**
 * Class to test cell styling functionality
 *
 * @author Andrew C. Oliver
 */

public class TestCellStyle
    extends TestCase
{

    /** Creates a new instance of TestCellStyle */

    public TestCellStyle(String name)
    {
        super(name);
    }

    /**
     * TEST NAME:  Test Write Sheet Font <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with fonts.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */

    public void testWriteSheetFont()
        throws IOException
    {
        File             file = TempFile.createTempFile("testWriteSheetFont",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();

        fnt.setColor(HSSFFont.COLOR_RED);
        fnt.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cs.setFont(fnt);
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
                c.setCellStyle(cs);
            }
        }
        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());

        // assert((s.getLastRowNum() == 99));
    }

    /**
     * Tests that is creating a file with a date or an calendar works correctly.
     */
    public void testDataStyle()
            throws Exception
    {
        File             file = TempFile.createTempFile("testWriteSheetStyleDate",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFRow row = s.createRow((short)0);

        // with Date:
        HSSFCell cell = row.createCell((short)1);
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        cell.setCellStyle(cs);
        cell.setCellValue(new Date());

        // with Calendar:
        cell = row.createCell((short)2);
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        cell.setCellStyle(cs);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cell.setCellValue(cal);

        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);

        assertEquals("LAST ROW ", 0, s.getLastRowNum());
        assertEquals("FIRST ROW ", 0, s.getFirstRowNum());

    }

    /**
     * TEST NAME:  Test Write Sheet Style <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with colors
     *             and borders.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */

    public void testWriteSheetStyle()
        throws IOException
    {
        File             file = TempFile.createTempFile("testWriteSheetStyle",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFCellStyle    cs2  = wb.createCellStyle();

        cs.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cs.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cs.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cs.setFillForegroundColor(( short ) 0xA);
        cs.setFillPattern(( short ) 1);
        fnt.setColor(( short ) 0xf);
        fnt.setItalic(true);
        cs2.setFillForegroundColor(( short ) 0x0);
        cs2.setFillPattern(( short ) 1);
        cs2.setFont(fnt);
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
                c.setCellStyle(cs);
                c = r.createCell(( short ) (cellnum + 1));
                c.setCellValue("TEST");
                c.setCellStyle(cs2);
            }
        }
        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());

        // assert((s.getLastRowNum() == 99));
    }

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.hssf.usermodel.HSSFCellStyle");
        junit.textui.TestRunner.run(TestCellStyle.class);
    }
}
