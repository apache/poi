
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

/*
 * TestCellStyle.java
 *
 * Created on December 11, 2001, 5:51 PM
 */
package org.apache.poi.hssf.usermodel;

import java.io.*;

import java.util.*;

import junit.framework.*;

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
        File             file = File.createTempFile("testWriteSheetFont",
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
        assertEquals("FILE LENGTH == 87040", file.length(), 87040);
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
        File             file = File.createTempFile("testWriteSheetStyleDate",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFRow row = s.createRow((short)0);

        // with Date:
        HSSFCell cell = row.createCell((short)1);
        cs.setDataFormat(HSSFDataFormat.getFormat("m/d/yy"));
        cell.setCellStyle(cs);
        cell.setCellValue(new Date());

        // with Calendar:
        cell = row.createCell((short)2);
        cs.setDataFormat(HSSFDataFormat.getFormat("m/d/yy"));
        cell.setCellStyle(cs);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cell.setCellValue(cal);

        wb.write(out);
        out.close();

        assertEquals("FILE LENGTH ", 5632, file.length());
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
        File             file = File.createTempFile("testWriteSheetStyle",
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
        assertEquals("FILE LENGTH == 87040", file.length(), 87040);
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
