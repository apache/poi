/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2003 The Apache Software Foundation.  All rights
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
import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;



/**
 * @author Avik Sengupta
 */

public class TestBugs
extends TestCase {
    public TestBugs(String s) {
        super(s);
    }
    
    /** Test reading AND writing a complicated workbook
     *Test opening resulting sheet in excel*/
          public void test15228()
        throws java.io.IOException
    {
         String readFilename = System.getProperty("HSSF.testdata.path");
          FileInputStream in = new FileInputStream(readFilename+File.separator+"15228.xls");
          HSSFWorkbook wb = new HSSFWorkbook(in);
          HSSFSheet s = wb.getSheetAt(0);
          HSSFRow r = s.createRow(0);
          HSSFCell c = r.createCell((short)0);
          c.setCellValue(10);
          File file = File.createTempFile("test15228",".xls");
          FileOutputStream out    = new FileOutputStream(file);
          wb.write(out);
          assertTrue("No exception thrown", true); 
          assertTrue("File Should Exist", file.exists());
            
    }
          
                 public void test13796()
        throws java.io.IOException
    {
         String readFilename = System.getProperty("HSSF.testdata.path");
          FileInputStream in = new FileInputStream(readFilename+File.separator+"13796.xls");
          HSSFWorkbook wb = new HSSFWorkbook(in);
          HSSFSheet s = wb.getSheetAt(0);
          HSSFRow r = s.createRow(0);
          HSSFCell c = r.createCell((short)0);
          c.setCellValue(10);
          File file = File.createTempFile("test13796",".xls");
          FileOutputStream out    = new FileOutputStream(file);
          wb.write(out);
          assertTrue("No exception thrown", true); 
          assertTrue("File Should Exist", file.exists());
            
    }
     /**Test writing a hyperlink
      * Open resulting sheet in Excel and check that A1 contains a hyperlink*/            
     public void test23094() throws Exception {
         File file = File.createTempFile("test23094",".xls");
         FileOutputStream out    = new FileOutputStream(file);
         HSSFWorkbook     wb     = new HSSFWorkbook();
         HSSFSheet        s      = wb.createSheet();
         HSSFRow r = s.createRow(0);
         r.createCell((short)0).setCellFormula("HYPERLINK( \"http://jakarta.apache.org\", \"Jakarta\" )");
         assertTrue("No Exception expected",true);
         wb.write(out);
         out.close();
     }
      
     /* test hyperlinks
      * open resulting file in excel, and check that there is a link to Google
      **/
      public void test15353() throws Exception {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("My sheet");
            
            HSSFRow row = sheet.createRow( (short) 0 );
            HSSFCell cell = row.createCell( (short) 0 );
            cell.setCellFormula("HYPERLINK(\"http://google.com\",\"Google\")");
            
            // Write out the workbook
            File f = File.createTempFile("test15353",".xls");
            FileOutputStream fileOut = new FileOutputStream(f);
            wb.write(fileOut);
            fileOut.close();
        }
          
       /** test reading of a formula with a name and a cell ref in one
        **/
       public void test14460() throws Exception {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/14460.xls";
        FileInputStream in = new FileInputStream(filename);
            HSSFWorkbook wb = new HSSFWorkbook(in);
            HSSFSheet sheet = wb.getSheetAt(0);
            assertTrue("No exception throws", true);
     }
       
       public void test14330() throws Exception {
           String filedir = System.getProperty("HSSF.testdata.path");
           String filename=filedir+"/14330-1.xls";
           FileInputStream in = new FileInputStream(filename);
           HSSFWorkbook wb = new HSSFWorkbook(in);
           HSSFSheet sheet = wb.getSheetAt(0);
           
           filename=filedir+"/14330-2.xls";
           in = new FileInputStream(filename);
           wb = new HSSFWorkbook(in);
           sheet = wb.getSheetAt(0);
           assertTrue("No exception throws", true);
       }
    
       /** test rewriting a file with large number of unique strings
        *open resulting file in Excel to check results!*/
       public void test15375() {
           
           try {
               String filename = System.getProperty("HSSF.testdata.path");
               filename=filename+"/15375.xls";
               FileInputStream in = new FileInputStream(filename);
               HSSFWorkbook wb = new HSSFWorkbook(in);
               HSSFSheet sheet = wb.getSheetAt(0);
               
               HSSFRow row = sheet.getRow(5);
               HSSFCell cell = row.getCell((short)3);
               if (cell == null)
                   cell = row.createCell((short)3);
               
               // Write test
               cell.setCellType(HSSFCell.CELL_TYPE_STRING);
               cell.setCellValue("a test");
               
               // change existing numeric cell value
               
               HSSFRow oRow = sheet.getRow(14);
               HSSFCell oCell = oRow.getCell((short)4);
               oCell.setCellValue(75);
               oCell = oRow.getCell((short)5);
               oCell.setCellValue("0.3");
               
               // Write the output to a file
               File f = File.createTempFile("test15375",".xls");
               FileOutputStream fileOut = new FileOutputStream(f);
               wb.write(fileOut);
               fileOut.close();
           }
           catch (java.io.FileNotFoundException ex) {
               ex.printStackTrace();
           }
           catch (java.io.IOException ex) {
               ex.printStackTrace();
           }
           
       }
       
       /** test writing a file with large number of unique strings
        *open resulting file in Excel to check results!*/
       
       public void test15375_2() throws Exception{
           
           
           HSSFWorkbook wb = new HSSFWorkbook();
           HSSFSheet sheet = wb.createSheet();
           
           String tmp1 = null;
           String tmp2 = null;
           String tmp3 = null;
           
           for (int i = 0; i < 6000; i++) {
               tmp1 = "Test1" + i;
               tmp2 = "Test2" + i;
               tmp3 = "Test3" + i;
               
               HSSFRow row = sheet.createRow((short)i);
               
               HSSFCell cell = row.createCell((short)0);
               cell.setCellValue(tmp1);
               cell = row.createCell((short)1);
               cell.setCellValue(tmp2);
               cell = row.createCell((short)2);
               cell.setCellValue(tmp3);
           }
           File f = File.createTempFile("test15375-2",".xls");
           FileOutputStream fileOut = new FileOutputStream(f);
           wb.write(fileOut);
           fileOut.close();
       }
       /** another test for the number of unique strings issue
        *test opening the resulting file in Excel*/
       public void test22568() {
           int r=2000;int c=3;
           
           HSSFWorkbook wb = new HSSFWorkbook() ;
           HSSFSheet sheet = wb.createSheet("ExcelTest") ;
         
           int col_cnt=0, rw_cnt=0 ;
           
           col_cnt = c;
           rw_cnt = r;
           
           HSSFRow rw = null ;
           HSSFCell cell =null;
           rw = sheet.createRow((short)0) ;
           //Header row
           for(short j=0; j<col_cnt; j++){
               cell = rw.createCell((short)j) ;
               cell.setCellValue("Col " + (j+1)) ;
           }
           
           for(int i=1; i<rw_cnt; i++){
               rw = sheet.createRow((short)i) ;
               for(short j=0; j<col_cnt; j++){
                   cell = rw.createCell((short)j) ;
                   cell.setCellValue("Row:" + (i+1) + ",Column:" +
                   (j+1)) ;
               }
           }
           
           sheet.setDefaultColumnWidth((short) 18) ;
           
           try {
               File f = File.createTempFile("test22568",".xls");
               FileOutputStream out = new FileOutputStream(f) ;
               wb.write(out) ;
               
               out.close() ;
           }
           catch(java.io.IOException io_Excp) {
               System.out.println(io_Excp.getMessage()) ;
           }
       }
       
    /**Double byte strings*/
    public void test15556() throws java.io.IOException {

        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/15556.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(45);
        this.assertTrue("Read row fine!" , true);

    } 
    
    /*Double byte strings */
    public void test22742() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/22742.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);        this.assertTrue("Read workbook!" , true);

    }
    /*Double byte strings */
    public void test12561_1() throws java.io.IOException {

        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/12561-1.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        this.assertTrue("Read workbook!" , true);

    }
    /*Double byte strings */
    public void test12561_2() throws java.io.IOException {

        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/12561-2.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        this.assertTrue("Read workbook!" , true);

    }
    
    /** Reference to Name*/
    public void test13224() throws java.io.IOException {

        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/13224.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        this.assertTrue("Read workbook!" , true);

    }
}
    


