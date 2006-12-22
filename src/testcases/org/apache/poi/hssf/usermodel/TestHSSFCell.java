
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

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.util.TempFile;

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

            File file = TempFile.createTempFile("testBoolErr",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("testSheet1");
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
            c.setCellErrorValue((byte)7);


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
            assertTrue("boolean value 0,2 = 7",c.getErrorCellValue() == 7);

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
    
    /**
     * Tests that the active cell can be correctly read and set
     */
    public void testActiveCell() throws Exception
    {
        //read in sample
        String dir = System.getProperty("HSSF.testdata.path");
        File sample = new File(dir + "/Simple.xls");
        assertTrue("Simple.xls exists and is readable", sample.canRead());
        FileInputStream fis = new FileInputStream(sample);
        HSSFWorkbook book = new HSSFWorkbook(fis);
        fis.close();
        
        //check initial position
        HSSFSheet umSheet = book.getSheetAt(0);
        Sheet s = umSheet.getSheet();
        assertEquals("Initial active cell should be in col 0",
            (short) 0, s.getActiveCellCol());
        assertEquals("Initial active cell should be on row 1",
            1, s.getActiveCellRow());
        
        //modify position through HSSFCell
        HSSFCell cell = umSheet.createRow(3).createCell((short) 2);
        cell.setAsActiveCell();
        assertEquals("After modify, active cell should be in col 2",
            (short) 2, s.getActiveCellCol());
        assertEquals("After modify, active cell should be on row 3",
            3, s.getActiveCellRow());
        
        //write book to temp file; read and verify that position is serialized
        File temp = TempFile.createTempFile("testActiveCell", ".xls");
        FileOutputStream fos = new FileOutputStream(temp);
        book.write(fos);
        fos.close();
        
        fis = new FileInputStream(temp);
        book = new HSSFWorkbook(fis);
        fis.close();
        umSheet = book.getSheetAt(0);
        s = umSheet.getSheet();
        
        assertEquals("After serialize, active cell should be in col 2",
            (short) 2, s.getActiveCellCol());
        assertEquals("After serialize, active cell should be on row 3",
            3, s.getActiveCellRow());
    }

    /**
     * test that Cell Styles being applied to formulas remain intact
     */
    public void testFormulaStyle()
            throws java.io.IOException {
        String readFilename = System.getProperty("HSSF.testdata.path");

            File file = TempFile.createTempFile("testFormulaStyle",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("testSheet1");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFFont f = wb.createFont();
            f.setFontHeightInPoints((short) 20);
            f.setColor((short) HSSFColor.RED.index);
            f.setBoldweight(f.BOLDWEIGHT_BOLD);
            f.setFontName("Arial Unicode MS");
            cs.setFillBackgroundColor((short)3);
            cs.setFont(f);
            cs.setBorderTop((short)1);
            cs.setBorderRight((short)1);
            cs.setBorderLeft((short)1);
            cs.setBorderBottom((short)1);
            
            r = s.createRow((short)0);
            c=r.createCell((short)0);
            c.setCellStyle(cs);
            c.setCellFormula("2*3");
            
            wb.write(out);
            out.close();

            assertTrue("file exists",file.exists());

            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            
            assertTrue("Formula Cell at 0,0", (c.getCellType()==c.CELL_TYPE_FORMULA));
            cs = c.getCellStyle();
            
            assertNotNull("Formula Cell Style", cs);
            assertTrue("Font Index Matches", (cs.getFontIndex() == f.getIndex()));
            assertTrue("Top Border", (cs.getBorderTop() == (short)1));
            assertTrue("Left Border", (cs.getBorderLeft() == (short)1));
            assertTrue("Right Border", (cs.getBorderRight() == (short)1));
            assertTrue("Bottom Border", (cs.getBorderBottom() == (short)1));
            
            in.close();
    }    
    
    /*tests the toString() method of HSSFCell*/
    public void testToString() throws Exception {
    	HSSFWorkbook wb = new HSSFWorkbook();
    	HSSFSheet s = wb.createSheet("Sheet1");
    	HSSFRow r = s.createRow(0);
    	HSSFCell c;
    	c=r.createCell((short) 0); c.setCellValue(true);
    	assertEquals("Boolean", "TRUE", c.toString());
    	c=r.createCell((short) 1); c.setCellValue(1.5);
    	assertEquals("Numeric", "1.5", c.toString());
    	c=r.createCell((short)(2)); c.setCellValue("Astring");
    	assertEquals("String", "Astring", c.toString());
    	c=r.createCell((short) 3); c.setCellErrorValue((byte) 7);
    	assertEquals("Error", "#ERR7", c.toString());
    	c=r.createCell((short)4); c.setCellFormula("A1+B1");
    	assertEquals("Formula", "A1+B1", c.toString());
    	
    	//Write out the file, read it in, and then check cell values
    	File f = File.createTempFile("testCellToString",".xls");
    	wb.write(new FileOutputStream(f));
    	wb = new HSSFWorkbook(new FileInputStream(f));
    	assertTrue("File exists and can be read", f.canRead());
    	
    	s = wb.getSheetAt(0);r=s.getRow(0);
    	c=r.getCell((short) 0);
    	assertEquals("Boolean", "TRUE", c.toString());
    	c=r.getCell((short) 1); 
    	assertEquals("Numeric", "1.5", c.toString());
    	c=r.getCell((short)(2)); 
    	assertEquals("String", "Astring", c.toString());
    	c=r.getCell((short) 3); 
    	assertEquals("Error", "#ERR7", c.toString());
    	c=r.getCell((short)4); 
    	assertEquals("Formula", "A1+B1", c.toString());
    }
    
    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.usermodel.TestHSSFCell");
        junit.textui.TestRunner.run(TestHSSFCell.class);
    }

}

