/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Tests row shifting capabilities.
 *
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */

public class TestSheetShiftRows extends TestCase {

    /**
     * Constructor for TestSheetShiftRows.
     * @param arg0
     */
    public TestSheetShiftRows(String arg0) {
	super(arg0);
    }

    /**
     * Tests the shiftRows function.  Does three different shifts.
     * After each shift, writes the workbook to file and reads back to
     * check.  This ensures that if some changes code that breaks
     * writing or what not, they realize it.
     *
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testShiftRows() throws Exception
    {
        // Read initial file in
        String filename = System.getProperty( "HSSF.testdata.path" );
        filename = filename + "/SimpleMultiCell.xls";
        FileInputStream fin = new FileInputStream( filename );
        HSSFWorkbook wb = new HSSFWorkbook( fin );
        fin.close();
        HSSFSheet s = wb.getSheetAt( 0 );

        // Shift the second row down 1 and write to temp file
        s.shiftRows( 1, 1, 1 );
        File tempFile = File.createTempFile( "shift", "test.xls" );
        FileOutputStream fout = new FileOutputStream( tempFile );
        wb.write( fout );
        fout.close();

        // Read from temp file and check the number of cells in each
        // row (in original file each row was unique)
        fin = new FileInputStream( tempFile );
        wb = new HSSFWorkbook( fin );
        fin.close();
        s = wb.getSheetAt( 0 );

        assertEquals( s.getRow( 0 ).getPhysicalNumberOfCells(), 1 );
        assertTrue( s.getRow( 1 ) == null || s.getRow( 1 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 2 ).getPhysicalNumberOfCells(), 2 );
        assertEquals( s.getRow( 3 ).getPhysicalNumberOfCells(), 4 );
        assertEquals( s.getRow( 4 ).getPhysicalNumberOfCells(), 5 );

        // Shift rows 1-3 down 3 in the current one.  This tests when
        // 1 row is blank.  Write to a another temp file
        s.shiftRows( 0, 2, 3 );
        tempFile = File.createTempFile( "shift", "test.xls" );
        fout = new FileOutputStream( tempFile );
        wb.write( fout );
        fout.close();

        // Read and ensure things are where they should be
        fin = new FileInputStream( tempFile );
        wb = new HSSFWorkbook( fin );
        fin.close();
        s = wb.getSheetAt( 0 );
        assertTrue( s.getRow( 0 ) == null || s.getRow( 0 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 1 ) == null || s.getRow( 1 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 2 ) == null || s.getRow( 2 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 3 ).getPhysicalNumberOfCells(), 1 );
        assertTrue( s.getRow( 4 ) == null || s.getRow( 4 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 5 ).getPhysicalNumberOfCells(), 2 );

        // Read the first file again
        fin = new FileInputStream( filename );
        wb = new HSSFWorkbook( fin );
        fin.close();
        s = wb.getSheetAt( 0 );

        // Shift rows 3 and 4 up and write to temp file
        s.shiftRows( 2, 3, -2 );
        tempFile = File.createTempFile( "shift", "test.xls" );
        fout = new FileOutputStream( tempFile );
        wb.write( fout );
        fout.close();

        // Read file and test
        fin = new FileInputStream( tempFile );
        wb = new HSSFWorkbook( fin );
        fin.close();
        s = wb.getSheetAt( 0 );
        assertEquals( s.getRow( 0 ).getPhysicalNumberOfCells(), 3 );
        assertEquals( s.getRow( 1 ).getPhysicalNumberOfCells(), 4 );
        assertTrue( s.getRow( 2 ) == null || s.getRow( 2 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 3 ) == null || s.getRow( 3 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 4 ).getPhysicalNumberOfCells(), 5 );
    }

    /**
     * Tests when rows are null.
     *
     * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
     */
    public void testShiftRow(){
	HSSFWorkbook b = new HSSFWorkbook();
	HSSFSheet s    = b.createSheet();
	s.createRow(0).createCell((short)0).setCellValue("TEST1");
	s.createRow(3).createCell((short)0).setCellValue("TEST2");
	s.shiftRows(0,4,1);
    }
}

