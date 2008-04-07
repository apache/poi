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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.util.TempFile;

/**
 * Tests various functionity having to do with HSSFCell.  For instance support for
 * paticular datatypes, etc.
 * @author Andrew C. Oliver (andy at superlinksoftware dot com)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author Alex Jacoby (ajacoby at gmail.com)
 */
public final class TestHSSFCell extends TestCase {

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }
    private static HSSFWorkbook writeOutAndReadBack(HSSFWorkbook original) {
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            original.write(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new HSSFWorkbook(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * test that Boolean and Error types (BoolErrRecord) are supported properly.
     */
    public void testBoolErr()
            throws java.io.IOException {

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
     public void testDateWindowingRead() throws Exception {
         GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
         Date date = cal.getTime();

         // first check a file with 1900 Date Windowing
         HSSFWorkbook    workbook = openSample("1900DateWindowing.xls");
         HSSFSheet       sheet    = workbook.getSheetAt(0);

         assertEquals("Date from file using 1900 Date Windowing",
                         date.getTime(),
                            sheet.getRow(0).getCell((short)0)
                               .getDateCellValue().getTime());
         
         // now check a file with 1904 Date Windowing
         workbook = openSample("1904DateWindowing.xls");
         sheet    = workbook.getSheetAt(0);

         assertEquals("Date from file using 1904 Date Windowing",
                         date.getTime(),
                            sheet.getRow(0).getCell((short)0)
                               .getDateCellValue().getTime());
     }

     /**
      * Checks that dates are properly written to both types of files:
      * those with 1900 and 1904 date windowing.  Note that if the
      * previous test ({@link #testDateWindowingRead}) fails, the
      * results of this test are meaningless.
      */
      public void testDateWindowingWrite() throws Exception {
          GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
          Date date = cal.getTime();

          // first check a file with 1900 Date Windowing
          HSSFWorkbook wb;
          wb = openSample("1900DateWindowing.xls");
          
          setCell(wb, 0, 1, date);
          wb = writeOutAndReadBack(wb);
          
          assertEquals("Date from file using 1900 Date Windowing",
                          date.getTime(),
                          readCell(wb, 0, 1).getTime());
          
          // now check a file with 1904 Date Windowing
            wb = openSample("1904DateWindowing.xls");
          setCell(wb, 0, 1, date);          
          wb = writeOutAndReadBack(wb);
          assertEquals("Date from file using 1900 Date Windowing",
                          date.getTime(),
                          readCell(wb, 0, 1).getTime());
      }

      private static void setCell(HSSFWorkbook workbook, int rowIdx, int colIdx, Date date) {
          HSSFSheet       sheet    = workbook.getSheetAt(0);
          HSSFRow         row      = sheet.getRow(rowIdx);
          HSSFCell        cell     = row.getCell(colIdx);
          
          if (cell == null) {
              cell = row.createCell((short)colIdx);
          }
          cell.setCellValue(date);
      }
      
      private static Date readCell(HSSFWorkbook workbook, int rowIdx, int colIdx) {
          HSSFSheet       sheet    = workbook.getSheetAt(0);
          HSSFRow         row      = sheet.getRow(rowIdx);
          HSSFCell        cell     = row.getCell(colIdx);
          return cell.getDateCellValue();
      }
      
    /**
     * Tests that the active cell can be correctly read and set
     */
    public void testActiveCell() throws Exception
    {
        //read in sample
        HSSFWorkbook book = openSample("Simple.xls");
        
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
        book = writeOutAndReadBack(book);

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
    public void testFormulaStyle() {

            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("testSheet1");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFFont f = wb.createFont();
            f.setFontHeightInPoints((short) 20);
            f.setColor(HSSFColor.RED.index);
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
            
            wb = writeOutAndReadBack(wb);
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
    }

    /**
     * Test reading hyperlinks
     */
    public void testWithHyperlink() {

        HSSFWorkbook wb = openSample("WithHyperlink.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFCell cell = sheet.getRow(4).getCell((short)0);
        HSSFHyperlink link = cell.getHyperlink();
        assertNotNull(link);

        assertEquals("Foo", link.getLabel());
        assertEquals("http://poi.apache.org/", link.getAddress());
        assertEquals(4, link.getFirstRow());
        assertEquals(0, link.getFirstColumn());
    }
    
    /**
     * Test reading hyperlinks
     */
    public void testWithTwoHyperlinks() throws Exception {

        HSSFWorkbook wb = openSample("WithTwoHyperLinks.xls");
        
        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFCell cell1 = sheet.getRow(4).getCell((short)0);
        HSSFHyperlink link1 = cell1.getHyperlink();
        assertNotNull(link1);
        assertEquals("Foo", link1.getLabel());
        assertEquals("http://poi.apache.org/", link1.getAddress());
        assertEquals(4, link1.getFirstRow());
        assertEquals(0, link1.getFirstColumn());

        HSSFCell cell2 = sheet.getRow(8).getCell((short)1);
        HSSFHyperlink link2 = cell2.getHyperlink();
        assertNotNull(link2);
        assertEquals("Bar", link2.getLabel());
        assertEquals("http://poi.apache.org/hssf/", link2.getAddress());
        assertEquals(8, link2.getFirstRow());
        assertEquals(1, link2.getFirstColumn());

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
        c=r.createCell((short)(2)); c.setCellValue(new HSSFRichTextString("Astring"));
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
    
    public void testSetStringInFormulaCell_bug44606() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet("Sheet1").createRow(0).createCell((short)0);
        cell.setCellFormula("B1&C1");
        try {
            cell.setCellValue(new HSSFRichTextString("hello"));
        } catch (ClassCastException e) {
            throw new AssertionFailedError("Identified bug 44606");
        }
    }
    
    public static void main(String [] args) {
        junit.textui.TestRunner.run(TestHSSFCell.class);
    }
}

