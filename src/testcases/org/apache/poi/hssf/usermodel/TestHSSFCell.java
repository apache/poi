
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

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Tests various functionity having to do with HSSFCell.  For instance support for
 * paticular datatypes, etc.
 * @author Andrew C. Oliver (andy at superlinksoftware dot com)
 * @author  Dan Sherman (dsherman at isisph.com)
 */

public class TestHSSFCell
extends TestCase {
    public TestHSSFCell(String s) {
        super(s);
    }

    /**
     * test that Boolean and Error types (BoolErrRecord) are supported properly.
     */
    public void testBoolErr()
            throws java.io.IOException {
        String readFilename = System.getProperty("HSSF.testdata.path");

            File file = File.createTempFile("testBoolErr",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("Sheet1");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            r = s.createRow((short)0);
            c=r.createCell((short)1);
            //c.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
            c.setCellValue(true);

            c=r.createCell((short)2);
            //c.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
            c.setCellValue(false);

            r = s.createRow((short)1);
            c=r.createCell((short)1);
            //c.setCellType(HSSFCell.CELL_TYPE_ERROR);
            c.setCellErrorValue((byte)0);

            c=r.createCell((short)2);
            //c.setCellType(HSSFCell.CELL_TYPE_ERROR);
            c.setCellErrorValue((byte)1);


            wb.write(out);
            out.close();

            assertTrue("file exists",file.exists());

            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)1);
            assertTrue("boolean value 0,1 = true",c.getBooleanCellValue());
            c = r.getCell((short)2);
            assertTrue("boolean value 0,2 = false",c.getBooleanCellValue()==false);
            r = s.getRow(1);
            c = r.getCell((short)1);
            assertTrue("boolean value 0,1 = 0",c.getErrorCellValue() == 0);
            c = r.getCell((short)2);
            assertTrue("boolean value 0,2 = 1",c.getErrorCellValue() == 1);

            in.close();
    }

    /**
    * Checks that the recognition of files using 1904 date windowing
    *  is working properly. Conversion of the date is also an issue,
    *  but there's a separate unit test for that.
    */
    public void testDateWindowing() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
        Date date = cal.getTime();
        String path = System.getProperty("HSSF.testdata.path");

        // first check a file with 1900 Date Windowing
        String filename = path + "/1900DateWindowing.xls";
        FileInputStream stream   = new FileInputStream(filename);
        POIFSFileSystem fs       = new POIFSFileSystem(stream);
        HSSFWorkbook    workbook = new HSSFWorkbook(fs);
        HSSFSheet       sheet    = workbook.getSheetAt(0);

        assertEquals("Date from file using 1900 Date Windowing",
                        date.getTime(),
                           sheet.getRow(0).getCell((short)0)
                              .getDateCellValue().getTime());
        stream.close();
        
        // now check a file with 1904 Date Windowing
        filename = path + "/1904DateWindowing.xls";
        stream   = new FileInputStream(filename);
        fs       = new POIFSFileSystem(stream);
        workbook = new HSSFWorkbook(fs);
        sheet    = workbook.getSheetAt(0);

        assertEquals("Date from file using 1904 Date Windowing",
                        date.getTime(),
                           sheet.getRow(0).getCell((short)0)
                              .getDateCellValue().getTime());
        stream.close();
    }

    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.usermodel.TestHSSFCell");
        junit.textui.TestRunner.run(TestHSSFCell.class);
    }

}

