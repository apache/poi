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
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Tests HSSFSheet.  This test case is very incomplete at the moment.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class TestHSSFSheet
        extends TestCase
{
    public TestHSSFSheet(String s)
    {
        super(s);
    }

    /**
     * Test the gridset field gets set as expected.
     */

    public void testBackupRecord()
            throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();

        assertEquals(true, sheet.getGridsetRecord().getGridset());
        s.setGridsPrinted(true);
        assertEquals(false, sheet.getGridsetRecord().getGridset());
    }

    /**
     * Test vertically centered output.
     */

    public void testVerticallyCenter()
            throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        VCenterRecord record =
                (VCenterRecord) sheet.findFirstRecordBySid(VCenterRecord.sid);

        assertEquals(false, record.getVCenter());
        s.setVerticallyCenter(true);
        assertEquals(true, record.getVCenter());

        // wb.write(new FileOutputStream("c:\\test.xls"));
    }

    /**
     * Test horizontally centered output.
     */

    public void testHorizontallyCenter()
            throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        HCenterRecord record =
                (HCenterRecord) sheet.findFirstRecordBySid(HCenterRecord.sid);

        assertEquals(false, record.getHCenter());
        s.setHorizontallyCenter(true);
        assertEquals(true, record.getHCenter());

    }    
    
    
    /**
     * Test WSBboolRecord fields get set in the user model.
     */

    public void testWSBool()
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

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
        assertEquals(false, record.getAlternateExpression());
        assertEquals(false, record.getAlternateFormula());
        assertEquals(true, record.getAutobreaks());
        assertEquals(true, record.getDialog());
        assertEquals(true, record.getDisplayGuts());
        assertEquals(false, record.getFitToPage());
        assertEquals(true, record.getRowSumsBelow());
        assertEquals(true, record.getRowSumsRight());
        assertEquals(false, s.getAlternateExpression());
        assertEquals(false, s.getAlternateFormula());
        assertEquals(true, s.getAutobreaks());
        assertEquals(true, s.getDialog());
        assertEquals(true, s.getDisplayGuts());
        assertEquals(false, s.getFitToPage());
        assertEquals(true, s.getRowSumsBelow());
        assertEquals(true, s.getRowSumsRight());
    }

    public void testReadBooleans()
            throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test boolean");
        HSSFRow row = sheet.createRow((short) 2);
        HSSFCell cell = row.createCell((short) 9);
        cell.setCellValue(true);
        cell = row.createCell((short) 11);
        cell.setCellValue(true);
        File tempFile = File.createTempFile("bool", "test.xls");
        FileOutputStream stream = new FileOutputStream(tempFile);
        workbook.write(stream);
        stream.close();

        FileInputStream readStream = new FileInputStream(tempFile);
        workbook = new HSSFWorkbook(readStream);
        sheet = workbook.getSheetAt(0);
        row = sheet.getRow(2);
        stream.close();
        tempFile.delete();
        assertNotNull(row);
        assertEquals(2, row.getPhysicalNumberOfCells());
    }

    public void testRemoveRow()
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test boolean");
        HSSFRow row = sheet.createRow((short) 2);
        sheet.removeRow(row);
    }

    public void testCloneSheet() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Clone");
        HSSFRow row = sheet.createRow((short) 0);
        HSSFCell cell = row.createCell((short) 0);
        cell.setCellValue("clone_test"); 
        HSSFSheet cloned = workbook.cloneSheet(0);
  
        //Check for a good clone
        assertEquals(cloned.getRow((short)0).getCell((short)0).getStringCellValue(), "clone_test");
        
        //Check that the cells are not somehow linked
        cell.setCellValue("Difference Check");
        assertEquals(cloned.getRow((short)0).getCell((short)0).getStringCellValue(), "clone_test");
    }

    /**
     * Tests the shiftRows function.  Does three different shifts.
     * After each shift, writes the workbook to file and reads back to
     * check.  This ensures that if some changes code that breaks
     * writing or what not, they realize it.
     *
     * Shawn Laubach (slaubach at apache dot org)
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

    public void testZoom()
            throws Exception
    {
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
}
