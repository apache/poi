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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.util.Region;

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
	 * Test that the ProtectRecord is included when creating or cloning a sheet
	 */
	public void testProtect() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet hssfSheet = workbook.createSheet();
		Sheet sheet = hssfSheet.getSheet();
		ProtectRecord protect = sheet.getProtect();
   	
		assertFalse(protect.getProtect());

		// This will tell us that cloneSheet, and by extension,
		// the list forms of createSheet leave us with an accessible
		// ProtectRecord.
		hssfSheet.setProtect(true);
		Sheet cloned = sheet.cloneSheet();
		assertNotNull(cloned.getProtect());
		assertTrue(hssfSheet.getProtect());
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
    

	/**
	 * When removing one merged region, it would break
	 *
	 */    
	public void testRemoveMerged() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		Region region = new Region(0, (short)0, 1, (short)1);   	
		sheet.addMergedRegion(region);
		region = new Region(1, (short)0, 2, (short)1);
		sheet.addMergedRegion(region);
		
    	sheet.removeMergedRegion(0);
    	
    	region = sheet.getMergedRegionAt(0);
    	assertEquals("Left over region should be starting at row 1", 1, region.getRowFrom());
    	
    	sheet.removeMergedRegion(0);
    	
		assertEquals("there should be no merged regions left!", 0, sheet.getNumMergedRegions());
		
		//an, add, remove, get(0) would null pointer
		sheet.addMergedRegion(region);
		assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());
		sheet.removeMergedRegion(0);
		assertEquals("there should now be zero merged regions!", 0, sheet.getNumMergedRegions());
		//add it again!
		region.setRowTo(4);
    	
		sheet.addMergedRegion(region);
		assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());
		
		//should exist now!
		assertTrue("there isn't more than one merged region in there", 1 <= sheet.getNumMergedRegions());
		region = sheet.getMergedRegionAt(0);
		assertEquals("the merged row to doesnt match the one we put in ", 4, region.getRowTo());
    	
    }

	public void testShiftMerged() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
		cell.setCellValue("first row, first cell");
		
		row = sheet.createRow(1);
		cell = row.createCell((short)1);
		cell.setCellValue("second row, second cell");
		
		Region region = new Region(1, (short)0, 1, (short)1);   	
		sheet.addMergedRegion(region);
		
		sheet.shiftRows(1, 1, 1);
		
		region = sheet.getMergedRegionAt(0);
		assertEquals("Merged region not moved over to row 2", 2, region.getRowFrom());
		
	}

    /**
     * Tests the display of gridlines, formulas, and rowcolheadings.
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testDisplayOptions() throws Exception {
	HSSFWorkbook wb = new HSSFWorkbook();
	HSSFSheet sheet = wb.createSheet();
	
        File tempFile = File.createTempFile("display", "test.xls");
        FileOutputStream stream = new FileOutputStream(tempFile);
        wb.write(stream);
        stream.close();

        FileInputStream readStream = new FileInputStream(tempFile);
        wb = new HSSFWorkbook(readStream);
        sheet = wb.getSheetAt(0);
	readStream.close();

	assertEquals(sheet.isDisplayGridlines(), true);
	assertEquals(sheet.isDisplayRowColHeadings(), true);
	assertEquals(sheet.isDisplayFormulas(), false);

	sheet.setDisplayGridlines(false);
	sheet.setDisplayRowColHeadings(false);
	sheet.setDisplayFormulas(true);

        tempFile = File.createTempFile("display", "test.xls");
        stream = new FileOutputStream(tempFile);
        wb.write(stream);
        stream.close();

        readStream = new FileInputStream(tempFile);
        wb = new HSSFWorkbook(readStream);
        sheet = wb.getSheetAt(0);
	readStream.close();


	assertEquals(sheet.isDisplayGridlines(), false);
	assertEquals(sheet.isDisplayRowColHeadings(), false);
	assertEquals(sheet.isDisplayFormulas(), true);
    }

	public static void main(java.lang.String[] args) {
		 junit.textui.TestRunner.run(TestHSSFSheet.class);
	}    
}
