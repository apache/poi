
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

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.TempFile;

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
        File tempFile = TempFile.createTempFile( "shift", "test.xls" );
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
        tempFile = TempFile.createTempFile( "shift", "test.xls" );
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
        tempFile = TempFile.createTempFile( "shift", "test.xls" );
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

    /**
     * Tests when shifting the first row.
     *
     * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
     */
    public void testShiftRow0(){
   	HSSFWorkbook b = new HSSFWorkbook();
   	HSSFSheet s    = b.createSheet();
   	s.createRow(0).createCell((short)0).setCellValue("TEST1");
   	s.createRow(3).createCell((short)0).setCellValue("TEST2");
   	s.shiftRows(0,4,1);
    }
    
    /**
     * When shifting rows, the page breaks should go with it
     *
     */
    public void testShiftRowBreaks(){
      HSSFWorkbook b = new HSSFWorkbook();
      HSSFSheet s    = b.createSheet();
      HSSFRow row = s.createRow(4);
      row.createCell((short)0).setCellValue("test");
      s.setRowBreak(4);
      
      s.shiftRows(4, 4, 2);
      assertTrue("Row number 6 should have a pagebreak", s.isRowBroken(6));
      
    }
}

