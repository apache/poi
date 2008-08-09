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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.util.TempFile;

/**
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Avik Sengupta
 */
public final class TestFormulas extends TestCase {

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    /**
     * Add 1+1 -- WHoohoo!
     */

    public void testBasicAddIntegers()
    throws Exception {

        File file = TempFile.createTempFile("testFormula",".xls");
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
        File file = TempFile.createTempFile("testFormulaFloat",".xls");
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
        File file = TempFile.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.createRow(0);
        c = r.createCell(1);
        c.setCellFormula("A2" + operator + "A3");

        for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.createRow(x);

            for (int y = 1; y < 256 && y > 0; y++) {

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

                c = r.getCell(y);
                CellReference cr= new CellReference(refx1,refy1, false, false);
                ref=cr.formatAsString();
                cr=new CellReference(refx2,refy2, false, false);
                ref2=cr.formatAsString();

                c = r.createCell(y);
                c.setCellFormula("" + ref + operator + ref2);



            }
        }

        //make sure we do the maximum value of the Int operator
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.getRow(0);
            c = r.createCell(0);
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
                CellReference cr= new CellReference(refx1, refy1, false, false);
                ref=cr.formatAsString();
                cr=new CellReference(refx2,refy2, false, false);
                ref2=cr.formatAsString();


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
        File file = TempFile.createTempFile("testFormula",".xls");
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
        File file = TempFile.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.createRow(0);
        c = r.createCell(1);
        c.setCellFormula(1 + operator + 1);

        for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.createRow(x);

            for (int y = 1; y < 256 && y > 0; y++) {

                c = r.createCell(y);
                c.setCellFormula("" + x + operator + y);

            }
        }

        //make sure we do the maximum value of the Int operator
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.getRow(0);
            c = r.createCell(0);
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

            File file = TempFile.createTempFile("testFormulaAreaFunction"+function,".xls");
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

            File file = TempFile.createTempFile("testFormulaArrayFunction"+function,".xls");
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

            File file = TempFile.createTempFile("testFormulaAreaArrayFunction"+function,".xls");
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
            File file = TempFile.createTempFile("testFormulaAbsRef",".xls");
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
        throws IOException
    {
            File file = TempFile.createTempFile("testSheetFormula",".xls");
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
         File file = TempFile.createTempFile("testFormulaRVA",".xls");
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

    public void testStringFormulas()
        throws IOException
    {
            File file = TempFile.createTempFile("testStringFormula",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("A");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            r = s.createRow((short)0);
            c=r.createCell((short)1); c.setCellFormula("UPPER(\"abc\")");
            c=r.createCell((short)2); c.setCellFormula("LOWER(\"ABC\")");
            c=r.createCell((short)3); c.setCellFormula("CONCATENATE(\" my \",\" name \")");

            wb.write(out);
            out.close();

            wb = openSample("StringFormulas.xls");
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)0);
            assertTrue("expected: UPPER(\"xyz\") got "+c.getCellFormula(), ("UPPER(\"xyz\")").equals(c.getCellFormula()));
            //c = r.getCell((short)1);
            //assertTrue("expected: A!A1+A!B1 got: "+c.getCellFormula(), ("A!A1+A!B1").equals(c.getCellFormula()));
    }



    public void testLogicalFormulas()
        throws IOException
    {

            File file = TempFile.createTempFile("testLogicalFormula",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("A");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            r = s.createRow((short)0);
            c=r.createCell((short)1); c.setCellFormula("IF(A1<A2,B1,B2)");


            wb.write(out);
            out.close();

             assertTrue("file exists",file.exists());

            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)1);
            assertEquals("Formula in cell 1 ","IF(A1<A2,B1,B2)",c.getCellFormula());
            in.close();
    }

    public void testDateFormulas()
        throws IOException
    {
            File file = TempFile.createTempFile("testDateFormula",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("testSheet1");
            HSSFRow          r      = null;
            HSSFCell         c      = null;

            r = s.createRow(  (short)0 );
            c = r.createCell( (short)0 );

            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
            c.setCellValue(new Date());
            c.setCellStyle(cellStyle);

           // assertEquals("Checking hour = " + hour, date.getTime().getTime(),
           //              HSSFDateUtil.getJavaDate(excelDate).getTime());

            for (int k=1; k < 100; k++) {
              r=s.createRow((short)k);
              c=r.createCell((short)0);
              c.setCellFormula("A"+(k)+"+1");
              c.setCellStyle(cellStyle);
            }

            wb.write(out);
            out.close();

            assertTrue("file exists",file.exists());

    }


    public void testIfFormulas()
        throws IOException
    {
            File file = TempFile.createTempFile("testIfFormula",".xls");
            FileOutputStream out    = new FileOutputStream(file);
            HSSFWorkbook     wb     = new HSSFWorkbook();
            HSSFSheet        s      = wb.createSheet("testSheet1");
            HSSFRow          r      = null;
            HSSFCell         c      = null;
            r = s.createRow((short)0);
            c=r.createCell((short)1); c.setCellValue(1);
            c=r.createCell((short)2); c.setCellValue(2);
            c=r.createCell((short)3); c.setCellFormula("MAX(A1:B1)");
            c=r.createCell((short)4); c.setCellFormula("IF(A1=D1,\"A1\",\"B1\")");

            wb.write(out);
            out.close();

            assertTrue("file exists",file.exists());

            FileInputStream in = new FileInputStream(file);
            wb = new HSSFWorkbook(in);
            s = wb.getSheetAt(0);
            r = s.getRow(0);
            c = r.getCell((short)4);

            assertTrue("expected: IF(A1=D1,\"A1\",\"B1\") got "+c.getCellFormula(), ("IF(A1=D1,\"A1\",\"B1\")").equals(c.getCellFormula()));
            in.close();

            wb = openSample("IfFormulaTest.xls");
            s = wb.getSheetAt(0);
            r = s.getRow(3);
            c = r.getCell((short)0);
            assertTrue("expected: IF(A3=A1,\"A1\",\"A2\") got "+c.getCellFormula(), ("IF(A3=A1,\"A1\",\"A2\")").equals(c.getCellFormula()));
            //c = r.getCell((short)1);
            //assertTrue("expected: A!A1+A!B1 got: "+c.getCellFormula(), ("A!A1+A!B1").equals(c.getCellFormula()));
            in.close();

        File simpleIf = TempFile.createTempFile("testSimpleIfFormulaWrite",".xls");
        out    = new FileOutputStream(simpleIf);
        wb     = new HSSFWorkbook();
        s      = wb.createSheet("testSheet1");
        r      = null;
        c      = null;
        r = s.createRow((short)0);
        c=r.createCell((short)0); c.setCellFormula("IF(1=1,0,1)");

        wb.write(out);
        out.close();
        assertTrue("file exists", simpleIf.exists());

        assertTrue("length of simpleIf file is zero", (simpleIf.length()>0));

        File nestedIf = TempFile.createTempFile("testNestedIfFormula",".xls");
        out    = new FileOutputStream(nestedIf);
        wb     = new HSSFWorkbook();
        s      = wb.createSheet("testSheet1");
        r      = null;
        c      = null;
        r = s.createRow((short)0);
        c=r.createCell((short)0);
        c.setCellValue(1);

        c=r.createCell((short)1);
        c.setCellValue(3);


        HSSFCell formulaCell=r.createCell((short)3);

        r = s.createRow((short)1);
        c=r.createCell((short)0);
        c.setCellValue(3);

        c=r.createCell((short)1);
        c.setCellValue(7);

        formulaCell.setCellFormula("IF(A1=B1,AVERAGE(A1:B1),AVERAGE(A2:B2))");


        wb.write(out);
        out.close();
        assertTrue("file exists", nestedIf.exists());

        assertTrue("length of nestedIf file is zero", (nestedIf.length()>0));
    }

    public void testSumIf()
        throws IOException
    {
        String function ="SUMIF(A1:A5,\">4000\",B1:B5)";

        HSSFWorkbook wb = openSample("sumifformula.xls");

        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = s.getRow(0);
        HSSFCell         c      = r.getCell((short)2);
        assertEquals(function, c.getCellFormula());


        File file = TempFile.createTempFile("testSumIfFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        wb     = new HSSFWorkbook();
        s      = wb.createSheet();

        r = s.createRow((short)0);
        c=r.createCell((short)0); c.setCellValue((double)1000);
        c=r.createCell((short)1); c.setCellValue((double)1);


        r = s.createRow((short)1);
        c=r.createCell((short)0); c.setCellValue((double)2000);
        c=r.createCell((short)1); c.setCellValue((double)2);

        r = s.createRow((short)2);
        c=r.createCell((short)0); c.setCellValue((double)3000);
        c=r.createCell((short)1); c.setCellValue((double)3);

        r = s.createRow((short)3);
        c=r.createCell((short)0); c.setCellValue((double)4000);
        c=r.createCell((short)1); c.setCellValue((double)4);

        r = s.createRow((short)4);
        c=r.createCell((short)0); c.setCellValue((double)5000);
        c=r.createCell((short)1); c.setCellValue((double)5);

        r = s.getRow(0);
        c=r.createCell((short)2); c.setCellFormula(function);

        wb.write(out);
        out.close();

        assertTrue("sumif file doesnt exists", (file.exists()));
        assertTrue("sumif == 0 bytes", file.length() > 0);
    }

    public void testSquareMacro() {
        HSSFWorkbook w = openSample("SquareMacro.xls");

        HSSFSheet s0 = w.getSheetAt(0);
        HSSFRow[] r = {s0.getRow(0), s0.getRow(1)};

        HSSFCell a1 = r[0].getCell((short) 0);
        assertEquals("square(1)", a1.getCellFormula());
        assertEquals(1d, a1.getNumericCellValue(), 1e-9);

        HSSFCell a2 = r[1].getCell((short) 0);
        assertEquals("square(2)", a2.getCellFormula());
        assertEquals(4d, a2.getNumericCellValue(), 1e-9);

        HSSFCell b1 = r[0].getCell((short) 1);
        assertEquals("IF(TRUE,square(1))", b1.getCellFormula());
        assertEquals(1d, b1.getNumericCellValue(), 1e-9);

        HSSFCell b2 = r[1].getCell((short) 1);
        assertEquals("IF(TRUE,square(2))", b2.getCellFormula());
        assertEquals(4d, b2.getNumericCellValue(), 1e-9);

        HSSFCell c1 = r[0].getCell((short) 2);
        assertEquals("square(square(1))", c1.getCellFormula());
        assertEquals(1d, c1.getNumericCellValue(), 1e-9);

        HSSFCell c2 = r[1].getCell((short) 2);
        assertEquals("square(square(2))", c2.getCellFormula());
        assertEquals(16d, c2.getNumericCellValue(), 1e-9);

        HSSFCell d1 = r[0].getCell((short) 3);
        assertEquals("square(one())", d1.getCellFormula());
        assertEquals(1d, d1.getNumericCellValue(), 1e-9);

        HSSFCell d2 = r[1].getCell((short) 3);
        assertEquals("square(two())", d2.getCellFormula());
        assertEquals(4d, d2.getNumericCellValue(), 1e-9);
    }

    public void testStringFormulaRead() {
        HSSFWorkbook w = openSample("StringFormulas.xls");
        HSSFCell c = w.getSheetAt(0).getRow(0).getCell((short)0);
        assertEquals("String Cell value","XYZ",c.getRichStringCellValue().getString());
    }

    /** test for bug 34021*/
    public void testComplexSheetRefs () throws IOException {
         HSSFWorkbook sb = new HSSFWorkbook();
         HSSFSheet s1 = sb.createSheet("Sheet a.1");
         HSSFSheet s2 = sb.createSheet("Sheet.A");
         s2.createRow(1).createCell((short) 2).setCellFormula("'Sheet a.1'!A1");
         s1.createRow(1).createCell((short) 2).setCellFormula("'Sheet.A'!A1");
         File file = TempFile.createTempFile("testComplexSheetRefs",".xls");
         sb.write(new FileOutputStream(file));
    }

    /*Unknown Ptg 3C*/
    public void test27272_1() throws Exception {
        HSSFWorkbook wb = openSample("27272_1.xls");
        wb.getSheetAt(0);
        assertEquals("Reference for named range ", "#REF!",wb.getNameAt(0).getReference());
        File outF = File.createTempFile("bug27272_1",".xls");
        wb.write(new FileOutputStream(outF));
        System.out.println("Open "+outF.getAbsolutePath()+" in Excel");
    }
    /*Unknown Ptg 3D*/
    public void test27272_2() throws Exception {
        HSSFWorkbook wb = openSample("27272_2.xls");
        assertEquals("Reference for named range ", "#REF!",wb.getNameAt(0).getReference());
        File outF = File.createTempFile("bug27272_2",".xls");
        wb.write(new FileOutputStream(outF));
        System.out.println("Open "+outF.getAbsolutePath()+" in Excel");
    }

    /* MissingArgPtg */
    public void testMissingArgPtg() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet("Sheet1").createRow(4).createCell((short) 0);
        cell.setCellFormula("IF(A1=\"A\",1,)");
    }

    public void testSharedFormula() {
        HSSFWorkbook wb = openSample("SharedFormulaTest.xls");

        assertEquals("A$1*2", wb.getSheetAt(0).getRow(1).getCell((short)1).toString());
        assertEquals("$A11*2", wb.getSheetAt(0).getRow(11).getCell((short)1).toString());
        assertEquals("DZ2*2", wb.getSheetAt(0).getRow(1).getCell((short)128).toString());
        assertEquals("B32770*2", wb.getSheetAt(0).getRow(32768).getCell((short)1).toString());
    }

    public static void main(String [] args) {
        junit.textui.TestRunner.run(TestFormulas.class);
    }
}
