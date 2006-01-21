
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.util.TempFile;

import java.io.*;



/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
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
    throws java.io.IOException {
        String readFilename = System.getProperty("HSSF.testdata.path");
        FileInputStream in = new FileInputStream(readFilename+File.separator+"15228.xls");
        HSSFWorkbook wb = new HSSFWorkbook(in);
        HSSFSheet s = wb.getSheetAt(0);
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell((short)0);
        c.setCellValue(10);
        File file = TempFile.createTempFile("test15228",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        wb.write(out);
        assertTrue("No exception thrown", true);
        assertTrue("File Should Exist", file.exists());
        
    }
    
    public void test13796()
    throws java.io.IOException {
        String readFilename = System.getProperty("HSSF.testdata.path");
        FileInputStream in = new FileInputStream(readFilename+File.separator+"13796.xls");
        HSSFWorkbook wb = new HSSFWorkbook(in);
        HSSFSheet s = wb.getSheetAt(0);
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell((short)0);
        c.setCellValue(10);
        File file = TempFile.createTempFile("test13796",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        wb.write(out);
        assertTrue("No exception thrown", true);
        assertTrue("File Should Exist", file.exists());
        
    }
    /**Test writing a hyperlink
     * Open resulting sheet in Excel and check that A1 contains a hyperlink*/
    public void test23094() throws Exception {
        File file = TempFile.createTempFile("test23094",".xls");
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
        File f = TempFile.createTempFile("test15353",".xls");
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
            File f = TempFile.createTempFile("test15375",".xls");
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
        File f = TempFile.createTempFile("test15375-2",".xls");
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
            File f = TempFile.createTempFile("test22568",".xls");
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
    /**Double byte strings */
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
    /*Double byte strings
     File supplied by jubeson*/
    public void test12843_1() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/12843-1.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        this.assertTrue("Read workbook!" , true);
    }
    
    /*Double byte strings
     File supplied by Paul Chung*/
    public void test12843_2() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/12843-2.xls";
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
    
    /** Illegal argument exception - cannot store duplicate value in Map*/
    public void test19599() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        FileInputStream in = new FileInputStream(filename+"/19599-1.xls");
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in = new FileInputStream(filename+"/19599-2.xls");
        wb = new HSSFWorkbook(in);
        this.assertTrue("Read workbook, No exceptions" , true);
        
    }
    
    public void test24215() throws Exception {
        String filename = System.getProperty("HSSF.testdata.path");
        HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filename+"/24215.xls"));
        
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets();sheetIndex++) {
            HSSFSheet sheet = wb.getSheetAt(sheetIndex);
            int rows = sheet.getLastRowNum();
            
            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                HSSFRow row = sheet.getRow(rowIndex);
                int cells = row.getLastCellNum();
                
                for (short cellIndex = 0; cellIndex < cells; cellIndex++) {
                    HSSFCell cell  = row.getCell(cellIndex);
                }
            }
        }
        assertTrue("No Exceptions while reading file", true);
    }
    
     public void test18800() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HSSFWorkbook book = new HSSFWorkbook();
        book.createSheet("TEST");
        HSSFSheet sheet = book.cloneSheet(0);
        book.setSheetName(1,"CLONE");
        sheet.createRow(0).createCell((short)0).setCellValue("Test");
        book.write(out);
        
        book = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = book.getSheet("CLONE");
        HSSFRow row = sheet.getRow(0);
        HSSFCell cell = row.getCell((short)0);
        System.out.println(cell.getStringCellValue());
    }
    
    /**
     * Merged regions were being removed from the parent in cloned sheets
     * @throws Exception
     */
    public void test22720() throws Exception {
       HSSFWorkbook workBook = new HSSFWorkbook();
       workBook.createSheet("TEST");       
       HSSFSheet template = workBook.getSheetAt(0);
       
       template.addMergedRegion(new Region(0, (short)0, 1, (short)2));
       template.addMergedRegion(new Region(1, (short)0, 2, (short)2));
       
       HSSFSheet clone = workBook.cloneSheet(0);
       int originalMerged = template.getNumMergedRegions();
       assertEquals("2 merged regions", 2, originalMerged);

//        remove merged regions from clone
       for (int i=template.getNumMergedRegions()-1; i>=0; i--) {
         clone.removeMergedRegion(i);
       }

      assertEquals("Original Sheet's Merged Regions were removed", originalMerged, template.getNumMergedRegions());
//        check if template's merged regions are OK
       if (template.getNumMergedRegions()>0) {
          // fetch the first merged region...EXCEPTION OCCURS HERE
          template.getMergedRegionAt(0);
       }       
       //make sure we dont exception
       
    }
    
    /*Tests read and write of Unicode strings in formula results
     * bug and testcase submitted by Sompop Kumnoonsate
     * The file contains THAI unicode characters. 
     */
	public void testUnicodeStringFormulaRead() throws Exception {
		
		String filename = System.getProperty("HSSF.testdata.path");
		filename=filename+"/25695.xls";
		FileInputStream in = new FileInputStream(filename);
		HSSFWorkbook w;
		w = new HSSFWorkbook(in);
		in.close();

		HSSFCell a1 = w.getSheetAt(0).getRow(0).getCell((short) 0);
		HSSFCell a2 = w.getSheetAt(0).getRow(0).getCell((short) 1);
		HSSFCell b1 = w.getSheetAt(0).getRow(1).getCell((short) 0);
		HSSFCell b2 = w.getSheetAt(0).getRow(1).getCell((short) 1);
		HSSFCell c1 = w.getSheetAt(0).getRow(2).getCell((short) 0);
		HSSFCell c2 = w.getSheetAt(0).getRow(2).getCell((short) 1);
		HSSFCell d1 = w.getSheetAt(0).getRow(3).getCell((short) 0);
		HSSFCell d2 = w.getSheetAt(0).getRow(3).getCell((short) 1);
		
 /*     // THAI code page
        System.out.println("a1="+unicodeString(a1.getStringCellValue()));
        System.out.println("a2="+unicodeString(a2.getStringCellValue()));
        // US code page
        System.out.println("b1="+unicodeString(b1.getStringCellValue()));
        System.out.println("b2="+unicodeString(b2.getStringCellValue()));
        // THAI+US
        System.out.println("c1="+unicodeString(c1.getStringCellValue()));
        System.out.println("c2="+unicodeString(c2.getStringCellValue()));
        // US+THAI
        System.out.println("d1="+unicodeString(d1.getStringCellValue()));
        System.out.println("d2="+unicodeString(d2.getStringCellValue()));
*/
		assertEquals("String Cell value", a1.getStringCellValue(), a2.getStringCellValue());
		assertEquals("String Cell value", b1.getStringCellValue(), b2.getStringCellValue());
		assertEquals("String Cell value", c1.getStringCellValue(), c2.getStringCellValue());
		assertEquals("String Cell value", d1.getStringCellValue(), d2.getStringCellValue());

		File xls = TempFile.createTempFile("testFormulaUnicode", ".xls");
		FileOutputStream out = new FileOutputStream(xls);
		w.write(out);
		out.close();
		in = new FileInputStream(xls);

		HSSFWorkbook rw = new HSSFWorkbook(in);
		in.close();

		HSSFCell ra1 = rw.getSheetAt(0).getRow(0).getCell((short) 0);
		HSSFCell ra2 = rw.getSheetAt(0).getRow(0).getCell((short) 1);
		HSSFCell rb1 = rw.getSheetAt(0).getRow(1).getCell((short) 0);
		HSSFCell rb2 = rw.getSheetAt(0).getRow(1).getCell((short) 1);
		HSSFCell rc1 = rw.getSheetAt(0).getRow(2).getCell((short) 0);
		HSSFCell rc2 = rw.getSheetAt(0).getRow(2).getCell((short) 1);
		HSSFCell rd1 = rw.getSheetAt(0).getRow(3).getCell((short) 0);
		HSSFCell rd2 = rw.getSheetAt(0).getRow(3).getCell((short) 1);

		assertEquals("Re-Written String Cell value", a1.getStringCellValue(), ra1.getStringCellValue());
		assertEquals("Re-Written String Cell value", b1.getStringCellValue(), rb1.getStringCellValue());
		assertEquals("Re-Written String Cell value", c1.getStringCellValue(), rc1.getStringCellValue());
		assertEquals("Re-Written String Cell value", d1.getStringCellValue(), rd1.getStringCellValue());
		assertEquals("Re-Written Formula String Cell value", a1.getStringCellValue(), ra2.getStringCellValue());
		assertEquals("Re-Written Formula String Cell value", b1.getStringCellValue(), rb2.getStringCellValue());
		assertEquals("Re-Written Formula String Cell value", c1.getStringCellValue(), rc2.getStringCellValue());
		assertEquals("Re-Written Formula String Cell value", d1.getStringCellValue(), rd2.getStringCellValue());

	}
	
	private static String unicodeString(String ss) {
        char s[] = ss.toCharArray();
        java.lang.StringBuffer sb=new java.lang.StringBuffer();
        for (int x=0;x<s.length;x++) {
            sb.append("\\u").append(Integer.toHexString(s[x]));
        }
        return sb.toString();
    }
	
	/** Error in opening wb*/
	public void test32822() throws Exception{
    	String readFilename = System.getProperty("HSSF.testdata.path");
        FileInputStream in = new FileInputStream(readFilename+File.separator+"32822.xls");
        HSSFWorkbook wb = new HSSFWorkbook(in);
        assertTrue("No Exceptions while reading file", true);
    }
	/**fail to read wb with chart */
	public void test15573() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/15573.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        assertTrue("No Exceptions while reading file", true);
        
    }
	
	/**names and macros */
	public void test27852() throws java.io.IOException {
        String filename = System.getProperty("HSSF.testdata.path");
        filename=filename+"/27852.xls";
        FileInputStream in = new FileInputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook(in);
        assertTrue("No Exceptions while reading file", true);
        for(int i = 0 ; i < wb.getNumberOfNames() ; i++)
        {
          HSSFName name = wb.getNameAt(i);
          name.getNameName();
          name.getReference();
        }
        assertTrue("No Exceptions till here!", true);
    }
	
	public void test33082() throws java.io.IOException {
	       String filename = System.getProperty("HSSF.testdata.path");
	       filename=filename+"/33082.xls";
	       FileInputStream in = new FileInputStream(filename);
	       HSSFWorkbook wb = new HSSFWorkbook(in);
	       assertTrue("Read book fine!" , true);
	}
	
	/*NullPointerException on reading file*/
	public void test34775() throws java.io.IOException {
	       String filename = System.getProperty("HSSF.testdata.path");
	       filename=filename+"/34775.xls";
	       FileInputStream in = new FileInputStream(filename);
	       HSSFWorkbook wb = new HSSFWorkbook(in);
	       assertTrue("Read book fine!" , true);
	}
	
	/** Error when reading then writing ArrayValues in NameRecord's*/
	public void test37630() throws java.io.IOException {
	       String filename = System.getProperty("HSSF.testdata.path");
	       filename=filename+"/37630.xls";
	       FileInputStream in = new FileInputStream(filename);
	       HSSFWorkbook wb = new HSSFWorkbook(in);
           File file = TempFile.createTempFile("test37630",".xls");
	       FileOutputStream out    = new FileOutputStream(file);
	       wb.write(out);
	       
	       assertTrue("Read book fine!" , true);
	}
	
	
	

}



