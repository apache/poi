
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

import org.apache.poi.ss.util.Region;
import org.apache.poi.util.TempFile;

import java.io.*;
import java.util.Iterator;



/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 * @author Avik Sengupta
 * @author Yegor Kozlov
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
        sheet.createRow(0).createCell((short)0).setCellValue(new HSSFRichTextString("Test"));
        book.write(out);
        
        book = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = book.getSheet("CLONE");
        HSSFRow row = sheet.getRow(0);
        HSSFCell cell = row.getCell((short)0);
        assertEquals("Test", cell.getRichStringCellValue().getString());
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

	public void test28031() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell((short)0);
        String formulaText =
			"IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))";
        cell.setCellFormula(formulaText);

        assertEquals(formulaText, cell.getCellFormula());
        if(false) {
            // this file can be inspected manually
            try {
                OutputStream os = new FileOutputStream("/tmp/output28031.xls");
                wb.write(os);
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
	
    protected String cwd = System.getProperty("HSSF.testdata.path");

    /**
     * Bug 25183: org.apache.poi.hssf.usermodel.HSSFSheet.setPropertiesFromSheet
     */
    public void test25183() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "25183.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 26100: 128-character message in IF statement cell causes HSSFWorkbook open failure
     */
    public void test26100() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "26100.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 27933: Unable to use a template (xls) file containing a wmf graphic
     */
    public void test27933() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "27933.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 29206:  	NPE on HSSFSheet.getRow for blank rows
     */
    public void test29206() throws Exception {
        //the first check with blank workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        for(int i = 1; i < 400; i++) {
            HSSFRow row = sheet.getRow(i);
            if(row != null) {
                HSSFCell cell = row.getCell((short)0);
            }
        }

        //now check on an existing xls file
        FileInputStream in = new FileInputStream(new File(cwd, "Simple.xls"));
        wb = new HSSFWorkbook(in);
        in.close();

        for(int i = 1; i < 400; i++) {
            HSSFRow row = sheet.getRow(i);
            if(row != null) {
                HSSFCell cell = row.getCell((short)0);
            }
        }

        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 29675: POI 2.5 final corrupts output when starting workbook has a graphic
     */
    public void test29675() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "29675.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 29942: Importing Excel files that have been created by Open Office on Linux
     */
    public void test29942() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "29942.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet sheet = wb.getSheetAt(0);
        int count = 0;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            HSSFRow row =  sheet.getRow(i);
            if (row != null) {
                HSSFCell cell = row .getCell((short)0);
                assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCellType());
                count++;
            }
        }
        assertEquals(85, count); //should read 85 rows
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 29982: Unable to read spreadsheet when dropdown list cell is selected -
     *  Unable to construct record instance
     */
    public void test29982() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "29982.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 30540: HSSFSheet.setRowBreak throws NullPointerException
     */
    public void test30540() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "30540.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet s = wb.getSheetAt(0);
        s.setRowBreak(1);
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 31749: {Need help urgently}[This is critical] workbook.write() corrupts the file......?
     */
    public void test31749() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "31749.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 31979: {urgent help needed .....}poi library does not support form objects properly.
     */
    public void test31979() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "31979.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        //wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 35564: HSSFCell.java: NullPtrExc in isGridsPrinted() and getProtect()
     *  when HSSFWorkbook is created from file
     */
    public void test35564() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "35564.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet sheet = wb.getSheetAt( 0 );
        assertEquals(false, sheet.isGridsPrinted());
        assertEquals(false, sheet.getProtect());

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 35565: HSSFCell.java: NullPtrExc in getColumnBreaks() when HSSFWorkbook is created from file
     */
    public void test35565() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "35565.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet sheet = wb.getSheetAt( 0 );
        assertNotNull(sheet);

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 37376: Cannot open the saved Excel file if checkbox controls exceed certain limit
     */
    public void test37376() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "37376.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 40285:  	CellIterator Skips First Column
     */
    public void test40285() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "40285.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet sheet = wb.getSheetAt( 0 );
        int rownum = 0;
        for (Iterator it = sheet.rowIterator(); it.hasNext(); rownum++) {
            HSSFRow row = (HSSFRow)it.next();
            assertEquals(rownum, row.getRowNum());
            int cellNum = 0;
            for (Iterator it2 = row.cellIterator(); it2.hasNext(); cellNum++) {
                HSSFCell cell = (HSSFCell)it2.next();
                assertEquals(cellNum, cell.getCellNum());
            }
        }
    }

    /**
     * Bug 40296:  	HSSFCell.setCellFormula throws
     *   ClassCastException if cell is created using HSSFRow.createCell(short column, int type)
     */
    public void test40296() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet workSheet = workBook.createSheet("Sheet1");
        HSSFCell cell;
        HSSFRow row;

        row = workSheet.createRow(0);
        cell = row.createCell((short)0, HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(1.0);
        cell = row.createCell((short)1, HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(2.0);
        cell = row.createCell((short)2, HSSFCell.CELL_TYPE_FORMULA);
        cell.setCellFormula("SUM(A1:B1)");

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Test bug 38266: NPE when adding a row break
     *
     * User's diagnosis:
     * 1. Manually (i.e., not using POI) create an Excel Workbook, making sure it
     * contains a sheet that doesn't have any row breaks
     * 2. Using POI, create a new HSSFWorkbook from the template in step #1
     * 3. Try adding a row break (via sheet.setRowBreak()) to the sheet mentioned in step #1
     * 4. Get a NullPointerException
     */
    public void test38266() throws Exception {
        String[] files = {"Simple.xls", "SimpleMultiCell.xls", "duprich1.xls"};
        for (int i = 0; i < files.length; i++) {
            FileInputStream in = new FileInputStream(new File(cwd, files[i]));
            HSSFWorkbook wb = new HSSFWorkbook(in);
            in.close();

            HSSFSheet sheet = wb.getSheetAt( 0 );
            int[] breaks = sheet.getRowBreaks();
            assertNull(breaks);

            //add 3 row breaks
            for (int j = 1; j <= 3; j++) {
                sheet.setRowBreak(j*20);
            }

            assertTrue("No Exceptions while adding row breaks in " + files[i], true);
        }
    }

    public void test40738() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "SimpleWithAutofilter.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 44200: Sheet not cloneable when Note added to excel cell
     */
    public void test44200() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "44200.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        wb.cloneSheet(0);
        assertTrue("No Exceptions while cloning sheet", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 44201: Sheet not cloneable when validation added to excel cell
     */
    public void test44201() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "44201.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        wb.cloneSheet(0);
        assertTrue("No Exceptions while cloning sheet", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 37684  : Unhandled Continue Record Error
     */
    public void test37684 () throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "37684-1.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No exceptions while reading workbook", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        assertTrue("No exceptions while saving workbook", true);

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No exceptions while reading saved stream", true);


        in = new FileInputStream(new File(cwd, "37684-2.xls"));
        wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No exceptions while reading workbook", true);

        //serialize and read again
        out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        assertTrue("No exceptions while saving workbook", true);

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No exceptions while reading saved stream", true);
    }

    /**
     * Bug 41139: Constructing HSSFWorkbook is failed,threw threw ArrayIndexOutOfBoundsException for creating UnknownRecord
     */
    public void test41139() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "41139.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 41546: Constructing HSSFWorkbook is failed,
     *  Unknown Ptg in Formula: 0x1a (26)
     */
    public void test41546() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "41546.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);
        assertEquals(1, wb.getNumberOfSheets());

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * Bug 42564: Some files from Access were giving a RecordFormatException
     *  when reading the BOFRecord
     */
    public void test42564() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "42564.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }
    
    /**
     * Bug 42564: Some files from Access also have issues
     *  with the NameRecord, once you get past the BOFRecord
     *  issue.
     * TODO - still broken
     */
    public void DISABLEDtest42564Alt() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "42564-2.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
    }

	/**
	 * Bug 42618: RecordFormatException reading a file containing
	 * 	=CHOOSE(2,A2,A3,A4)
	 * TODO - support getCellFormula too!
	 */
    public void test42618() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "SimpleWithChoose.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        assertTrue("No Exceptions while reading file", true);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertTrue("No Exceptions while reading file", true);
        
        // Check we detect the string properly too
        HSSFSheet s = wb.getSheetAt(0);
        
        // Textual value
        HSSFRow r1 = s.getRow(0);
        HSSFCell c1 = r1.getCell((short)1);
        assertEquals("=CHOOSE(2,A2,A3,A4)", c1.getRichStringCellValue().toString());
        
        // Formula Value
        HSSFRow r2 = s.getRow(1);
        HSSFCell c2 = r2.getCell((short)1);
        assertEquals(25, (int)c2.getNumericCellValue());
        
        // This will blow up with a 
        //  "EmptyStackException"
        //assertEquals("=CHOOSE(2,A2,A3,A4)", c2.getCellFormula());
    }
    
    /**
     * Something up with the FileSharingRecord
     */
    public void test43251() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "43251.xls"));
        
        // Used to blow up with an IllegalArgumentException
        //  when creating a FileSharingRecord
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        
        assertEquals(1, wb.getNumberOfSheets());
    }
    
    /**
     * Crystal reports generates files with short 
     *  StyleRecords, which is against the spec
     */
    public void test44471() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "OddStyleRecord.xls"));
        
        // Used to blow up with an ArrayIndexOutOfBounds
        //  when creating a StyleRecord
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        
        assertEquals(1, wb.getNumberOfSheets());
    }
    
    /**
     * Files with "read only recommended" were giving
     *  grief on the FileSharingRecord
     */
    public void test44536() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "ReadOnlyRecommended.xls"));
        
        // Used to blow up with an IllegalArgumentException
        //  when creating a FileSharingRecord
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        
        // Check read only advised
        assertEquals(3, wb.getNumberOfSheets());
        assertTrue(wb.isWriteProtected());
        
        // But also check that another wb isn't
        in = new FileInputStream(new File(cwd, "SimpleWithChoose.xls"));
        wb = new HSSFWorkbook(in);
        in.close();
        assertFalse(wb.isWriteProtected());
    }
    
    /**
     * Some files were having problems with the DVRecord,
     *  probably due to dropdowns
     */
    public void test44593() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "44593.xls"));
        
        // Used to blow up with an IllegalArgumentException
        //  when creating a DVRecord
        // Now won't, but no idea if this means we have
        //  rubbish in the DVRecord or not...
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        
        assertEquals(2, wb.getNumberOfSheets());
    }
    
    /**
     * Used to give problems due to trying to read a zero
     *  length string, but that's now properly handled
     */
    public void test44643() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "44643.xls"));
        
        // Used to blow up with an IllegalArgumentException
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();
        
        assertEquals(1, wb.getNumberOfSheets());
    }
    
    /**
     * User reported the wrong number of rows from the
     *  iterator, but we can't replicate that
     */
    public void test44693() throws Exception {
        FileInputStream in = new FileInputStream(new File(cwd, "44693.xls"));
        
        HSSFWorkbook wb = new HSSFWorkbook(in);
        HSSFSheet s = wb.getSheetAt(0);

        // Rows are 1 to 713
        assertEquals(0, s.getFirstRowNum());
        assertEquals(712, s.getLastRowNum());
        assertEquals(713, s.getPhysicalNumberOfRows());
        
        // Now check the iterator
        int rowsSeen = 0;
        for(Iterator i = s.rowIterator(); i.hasNext(); ) {
        	HSSFRow r = (HSSFRow)i.next();
        	rowsSeen++;
        }
        assertEquals(713, rowsSeen);
    }
}



