
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
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Avik Sengupta
 */

public class TestFormulas
extends TestCase {
    public TestFormulas(String s) {
        super(s);
    }
    
    /**
     * Add 1+1 -- WHoohoo!
     */
    
    public void testBasicAddIntegers()
    throws Exception {
        
        short            rownum = 0;
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)1);
        c = r.createCell((short)1);
        c.setCellFormula(1 + "+" + 1);
        
        wb.write(out);
        out.close();
        
        FileInputStream in = new FileInputStream(file);
        wb = new HSSFWorkbook(in);
        s  = wb.getSheetAt(0);
        r  = s.getRow((short)1);
        c  = r.getCell((short)1);
        
        assertTrue("Formula is as expected",("1+1".equals(c.getCellFormula())));
        in.close();
    }
    
    /**
     * Add various integers
     */
    
    public void testAddIntegers()
    throws Exception {
        binomialOperator("+");
    }
    
    /**
     * Multiply various integers
     */
    
    public void testMultplyIntegers()
    throws Exception {
        binomialOperator("*");
    }
    
    /**
     * Subtract various integers
     */
    public void testSubtractIntegers()
    throws Exception {
        binomialOperator("-");
    }
    
    /**
     * Subtract various integers
     */
    public void testDivideIntegers()
    throws Exception {
        binomialOperator("/");
    }
    
    /**
     * Exponentialize various integers;
     */
    public void testPowerIntegers()
    throws Exception {
        binomialOperator("^");
    }

    /**
     * Concatinate two numbers 1&2 = 12
     */
    public void testConcatIntegers() 
    throws Exception {
        binomialOperator("&");
    }
    
    /**
     * tests 1*2+3*4
     */
    public void testOrderOfOperationsMultiply() 
    throws Exception {
        orderTest("1*2+3*4");
    }
    
    /**
     * tests 1*2+3^4
     */
    public void testOrderOfOperationsPower() 
    throws Exception {
        orderTest("1*2+3^4");
    }
    
    /**
     * Tests that parenthesis are obeyed
     */
    public void testParenthesis() 
    throws Exception {
        orderTest("(1*3)+2+(1+2)*(3^4)^5");
    }
    
    public void testReferencesOpr() 
    throws Exception {
        String[] operation = new String[] {
                            "+", "-", "*", "/", "^", "&"
                           };
        for (int k = 0; k < operation.length; k++) {
            operationRefTest(operation[k]);
        }
    }
    
    /**
     * Tests creating a file with floating point in a formula.
     *
     */
    public void testFloat()
    throws Exception {
        floatTest("*");
        floatTest("/");
    }
    
    private void floatTest(String operator)
    throws Exception {
        short            rownum = 0;
        File file = File.createTempFile("testFormulaFloat",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        
        r = s.createRow((short)0);
        c = r.createCell((short)1);
        c.setCellFormula(""+Float.MIN_VALUE + operator + Float.MIN_VALUE);
 
       for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2) ) {
            r = s.createRow((short) x);
            
            for (short y = 1; y < 256 && y > 0; y= (short) (y +2)) {
                
                c = r.createCell((short) y);
                c.setCellFormula("" + x+"."+y + operator + y +"."+x);
                
                
            }
        }
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.createRow((short)0);
            c = r.createCell((short)0);
            c.setCellFormula("" + Float.MAX_VALUE + operator + Float.MAX_VALUE);
        }
        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());
        out=null;wb=null;  //otherwise we get out of memory error!
        floatVerify(operator,file);
        
    }
        
            private void floatVerify(String operator, File file)
    throws Exception {
        short            rownum = 0;
        
        FileInputStream  in     = new FileInputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook(in);
        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        // dont know how to check correct result .. for the moment, we just verify that the file can be read. 
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow((short) x);

            for (short y = 1; y < 256 && y > 0; y=(short)(y+2)) {

                c = r.getCell((short) y);
                assertTrue("got a formula",c.getCellFormula()!=null);
                
                assertTrue("loop Formula is as expected "+x+"."+y+operator+y+"."+x+"!="+c.getCellFormula(),(
                (""+x+"."+y+operator+y+"."+x).equals(c.getCellFormula()) ));
 
            }
        }
        
       in.close();
       assertTrue("file exists",file.exists());
    }
    
    public void testAreaSum() 
    throws Exception {
        areaFunctionTest("SUM");
    }
    
    public void testAreaAverage() 
    throws Exception {
        areaFunctionTest("AVERAGE");
    }
    
    public void testRefArraySum() 
    throws Exception {
        refArrayFunctionTest("SUM");
    }
    
    public void testAreaArraySum() 
    throws Exception {
        refAreaArrayFunctionTest("SUM");
    }

    
    
    private void operationRefTest(String operator) 
    throws Exception {
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)0);
        c = r.createCell((short)1);
        c.setCellFormula("A2" + operator + "A3");
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.createRow((short) x);

            for (short y = 1; y < 256 && y > 0; y++) {                
                
                String ref=null;
                String ref2=null;
                short refx1=0;
                short refy1=0;
                short refx2=0;
                short refy2=0;
                if (x +50 < Short.MAX_VALUE) {
                    refx1=(short)(x+50);
                    refx2=(short)(x+46);
                } else {
                    refx1=(short)(x-4);
                    refx2=(short)(x-3);
                }
                    
                if (y+50 < 255) {
                    refy1=(short)(y+50);
                    refy2=(short)(y+49);
                } else {
                    refy1=(short)(y-4);
                    refy2=(short)(y-3);
                }
                
                c = r.getCell((short) y);
                CellReference cr= new CellReference(refx1,refy1);
                ref=cr.toString();
                cr=new CellReference(refx2,refy2);
                ref2=cr.toString();

                c = r.createCell((short) y);
                c.setCellFormula("" + ref + operator + ref2);
                

                
            }
        }
        
        //make sure we do the maximum value of the Int operator
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.createRow((short)0);
            c = r.createCell((short)0);
            c.setCellFormula("" + "B1" + operator + "IV255");
        }
        
        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());
        operationalRefVerify(operator,file);
    }
    
    /**
     * Opens the sheet we wrote out by binomialOperator and makes sure the formulas
     * all match what we expect (x operator y)
     */
    private void operationalRefVerify(String operator, File file)
    throws Exception {
        short            rownum = 0;
        
        FileInputStream  in     = new FileInputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook(in);
        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.getRow((short)0);
        c = r.getCell((short)1);
        //get our minimum values
        assertTrue("minval Formula is as expected A2"+operator+"A3 != "+c.getCellFormula(),
        ( ("A2"+operator+"A3").equals(c.getCellFormula())
        ));

        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow((short) x);

            for (short y = 1; y < 256 && y > 0; y++) {                
                
                String ref=null;
                String ref2=null;
                short refx1=0;
                short refy1=0;
                short refx2=0;
                short refy2=0;
                if (x +50 < Short.MAX_VALUE) {
                    refx1=(short)(x+50);
                    refx2=(short)(x+46);
                } else {
                    refx1=(short)(x-4);
                    refx2=(short)(x-3);
                }
                    
                if (y+50 < 255) {
                    refy1=(short)(y+50);
                    refy2=(short)(y+49);
                } else {
                    refy1=(short)(y-4);
                    refy2=(short)(y-3);
                }

                c = r.getCell((short) y);
                CellReference cr= new CellReference(refx1,refy1);
                ref=cr.toString();
                cr=new CellReference(refx2,refy2);
                ref2=cr.toString();
                
                
                assertTrue("loop Formula is as expected "+ref+operator+ref2+"!="+c.getCellFormula(),(
                (""+ref+operator+ref2).equals(c.getCellFormula())
                                                         )
                );
                
                
            }
        }
        
        //test our maximum values
        r = s.getRow((short)0);
        c = r.getCell((short)0);
                
        assertTrue("maxval Formula is as expected",(
        ("B1"+operator+"IV255").equals(c.getCellFormula())
                                                   )
        );
        
        in.close();
        assertTrue("file exists",file.exists());
    }
    
    

    /**
     * tests order wrting out == order writing in for a given formula
     */    
    private void orderTest(String formula) 
    throws Exception {
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)0);
        c = r.createCell((short)1);
        c.setCellFormula(formula);
                       
        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());

        FileInputStream  in     = new FileInputStream(file);
        wb     = new HSSFWorkbook(in);
        s      = wb.getSheetAt(0);
        
        //get our minimum values
        r = s.getRow((short)0);
        c = r.getCell((short)1);
        assertTrue("minval Formula is as expected",
                   formula.equals(c.getCellFormula())
                  );
        
        in.close();
    }
    
    /**
     * All multi-binomial operator tests use this to create a worksheet with a
     * huge set of x operator y formulas.  Next we call binomialVerify and verify
     * that they are all how we expect.
     */
    private void binomialOperator(String operator)
    throws Exception {
        short            rownum = 0;
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)0);
        c = r.createCell((short)1);
        c.setCellFormula(1 + operator + 1);
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.createRow((short) x);

            for (short y = 1; y < 256 && y > 0; y++) {

                c = r.createCell((short) y);
                c.setCellFormula("" + x + operator + y);
                
            }
        }
        
        //make sure we do the maximum value of the Int operator
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.createRow((short)0);
            c = r.createCell((short)0);
            c.setCellFormula("" + Short.MAX_VALUE + operator + Short.MAX_VALUE);
        }
        
        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());
        
        binomialVerify(operator,file);
    }
    
    /**
     * Opens the sheet we wrote out by binomialOperator and makes sure the formulas
     * all match what we expect (x operator y)
     */
    private void binomialVerify(String operator, File file)
    throws Exception {
        short            rownum = 0;
        
        FileInputStream  in     = new FileInputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook(in);
        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.getRow((short)0);
        c = r.getCell((short)1);
        assertTrue("minval Formula is as expected 1"+operator+"1 != "+c.getCellFormula(),
        ( ("1"+operator+"1").equals(c.getCellFormula())
        ));
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow((short) x);

            for (short y = 1; y < 256 && y > 0; y++) {

                c = r.getCell((short) y);

                assertTrue("loop Formula is as expected "+x+operator+y+"!="+c.getCellFormula(),(
                (""+x+operator+y).equals(c.getCellFormula())
                                                         )
                );
                
                
            }
        }
        
        //test our maximum values
        r = s.getRow((short)0);
        c = r.getCell((short)0);
        
        
        assertTrue("maxval Formula is as expected",(
        (""+Short.MAX_VALUE+operator+Short.MAX_VALUE).equals(c.getCellFormula())
                                                   )
        );
        
        in.close();
        assertTrue("file exists",file.exists());
    }

 
    
    /**
     * Writes a function then tests to see if its correct
     *
     */
    public void areaFunctionTest(String function) 
    throws Exception {
            
            short            rownum = 0;
            File file = File.createTempFile("testFormulaAreaFunction"+function,".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet();
            HSSFRow          r      = null;
            HSSFCell         c      = null;


            r = s.createRow((short) 0);

            c = r.createCell((short) 0);
            c.setCellFormula(function+"(A2:A3)");


            wb.write(out);
            out.close();
            assertTrue("file exists",file.exists());
            
            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            
            assertTrue("function ="+function+"(A2:A3)",
                        ( (function+"(A2:A3)").equals((function+"(A2:A3)")) )
                      );
            in.close();
    }
    
    /**
     * Writes a function then tests to see if its correct
     *
     */
    public void refArrayFunctionTest(String function) 
    throws Exception {
            
            short            rownum = 0;
            File file = File.createTempFile("testFormulaArrayFunction"+function,".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet();
            HSSFRow          r      = null;
            HSSFCell         c      = null;


            r = s.createRow((short) 0);

            c = r.createCell((short) 0);
            c.setCellFormula(function+"(A2,A3)");


            wb.write(out);
            out.close();
            assertTrue("file exists",file.exists());
            
            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            
            assertTrue("function ="+function+"(A2,A3)",
                        ( (function+"(A2,A3)").equals(c.getCellFormula()) )
                      );
            in.close();
    }
    
    
    /**
     * Writes a function then tests to see if its correct
     *
     */
    public void refAreaArrayFunctionTest(String function) 
    throws Exception {
            
            short            rownum = 0;
            File file = File.createTempFile("testFormulaAreaArrayFunction"+function,".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet();
            HSSFRow          r      = null;
            HSSFCell         c      = null;


            r = s.createRow((short) 0);

            c = r.createCell((short) 0);
            c.setCellFormula(function+"(A2:A4,B2:B4)");
            c=r.createCell((short) 1);
            c.setCellFormula(function+"($A$2:$A4,B$2:B4)");

            wb.write(out);
            out.close();
            assertTrue("file exists",file.exists());
            
            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            
            assertTrue("function ="+function+"(A2:A4,B2:B4)",
                        ( (function+"(A2:A4,B2:B4)").equals(c.getCellFormula()) )
                      );
            
            c=r.getCell((short) 1);
             assertTrue("function ="+function+"($A$2:$A4,B$2:B4)",
                        ( (function+"($A$2:$A4,B$2:B4)").equals(c.getCellFormula()) )
                      );
            in.close();
    }
    
    
    
    public void testAbsRefs() throws Exception {
            File file = File.createTempFile("testFormulaAbsRef",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet();
            HSSFRow          r      = null;
            HSSFCell         c      = null;


            r = s.createRow((short) 0);

            c = r.createCell((short) 0);
            c.setCellFormula("A3+A2");
            c=r.createCell( (short) 1);
            c.setCellFormula("$A3+$A2");
            c=r.createCell( (short) 2);
            c.setCellFormula("A$3+A$2");
            c=r.createCell( (short) 3);
            c.setCellFormula("$A$3+$A$2");
            c=r.createCell( (short) 4);
            c.setCellFormula("SUM($A$3,$A$2)");
            
            wb.write(out);
            out.close();
            assertTrue("file exists",file.exists());
            
            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            assertTrue("A3+A2", ("A3+A2").equals(c.getCellFormula()));
             c = r.getCell((short)1);
            assertTrue("$A3+$A2", ("$A3+$A2").equals(c.getCellFormula()));
             c = r.getCell((short)2);
            assertTrue("A$3+A$2", ("A$3+A$2").equals(c.getCellFormula()));
             c = r.getCell((short)3);
            assertTrue("$A$3+$A$2", ("$A$3+$A$2").equals(c.getCellFormula()));
             c = r.getCell((short)4);
            assertTrue("SUM($A$3,$A$2)", ("SUM($A$3,$A$2)").equals(c.getCellFormula()));
            in.close();
    }
    
    public void testSheetFunctions()
        throws java.io.IOException
    {
        String filename = System.getProperty("HSSF.testdata.path");

            File file = File.createTempFile("testSheetFormula",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("A");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            r = s.createRow((short)0);
            c = r.createCell((short)0);c.setCellValue(1);
            c = r.createCell((short)1);c.setCellValue(2);
            
            s      = wb.createSheet("B");
            r = s.createRow((short)0);
            c=r.createCell((short)0); c.setCellFormula("AVERAGE(A!A1:B1)");
            c=r.createCell((short)1); c.setCellFormula("A!A1+A!B1");
            c=r.createCell((short)2); c.setCellFormula("A!$A$1+A!$B1");
            wb.write(out);
            out.close();
            
             assertTrue("file exists",file.exists());
            
            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheet("B");
            r = s.getRow(0);
            c = r.getCell((short)0);
            assertTrue("expected: AVERAGE(A!A1:B1) got: "+c.getCellFormula(), ("AVERAGE(A!A1:B1)").equals(c.getCellFormula()));
            c = r.getCell((short)1);
            assertTrue("expected: A!A1+A!B1 got: "+c.getCellFormula(), ("A!A1+A!B1").equals(c.getCellFormula()));
            in.close();
    }
    
    public void testRVAoperands() throws Exception {
         File file = File.createTempFile("testFormulaRVA",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet();
            HSSFRow          r      = null;
            HSSFCell         c      = null;


            r = s.createRow((short) 0);

            c = r.createCell((short) 0);
            c.setCellFormula("A3+A2");
            c=r.createCell( (short) 1);
            c.setCellFormula("AVERAGE(A3,A2)");
            c=r.createCell( (short) 2);
            c.setCellFormula("ROW(A3)");
            c=r.createCell( (short) 3);
            c.setCellFormula("AVERAGE(A2:A3)");
            c=r.createCell( (short) 4);
            c.setCellFormula("POWER(A2,A3)");
            c=r.createCell( (short) 5);
            c.setCellFormula("SIN(A2)");
            
            c=r.createCell( (short) 6);
            c.setCellFormula("SUM(A2:A3)");
            
            c=r.createCell( (short) 7);
            c.setCellFormula("SUM(A2,A3)");
            
            r = s.createRow((short) 1);c=r.createCell( (short) 0); c.setCellValue(2.0);
             r = s.createRow((short) 2);c=r.createCell( (short) 0); c.setCellValue(3.0);
            
            wb.write(out);
            out.close();
            assertTrue("file exists",file.exists());
    }
    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.usermodel.TestFormulas");
        junit.textui.TestRunner.run(TestFormulas.class);
    }
    
}
