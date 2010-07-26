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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.util.TempFile;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;

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
    public void testBasicAddIntegers() {

        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.createRow(1);
        c = r.createCell(1);
        c.setCellFormula(1 + "+" + 1);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s  = wb.getSheetAt(0);
        r  = s.getRow(1);
        c  = r.getCell(1);

        assertTrue("Formula is as expected",("1+1".equals(c.getCellFormula())));
    }

    /**
     * Add various integers
     */
    public void testAddIntegers() {
        binomialOperator("+");
    }

    /**
     * Multiply various integers
     */
    public void testMultplyIntegers() {
        binomialOperator("*");
    }

    /**
     * Subtract various integers
     */
    public void testSubtractIntegers() {
        binomialOperator("-");
    }

    /**
     * Subtract various integers
     */
    public void testDivideIntegers() {
        binomialOperator("/");
    }

    /**
     * Exponentialize various integers;
     */
    public void testPowerIntegers() {
        binomialOperator("^");
    }

    /**
     * Concatenate two numbers 1&2 = 12
     */
    public void testConcatIntegers() {
        binomialOperator("&");
    }

    /**
     * tests 1*2+3*4
     */
    public void testOrderOfOperationsMultiply() {
        orderTest("1*2+3*4");
    }

    /**
     * tests 1*2+3^4
     */
    public void testOrderOfOperationsPower() {
        orderTest("1*2+3^4");
    }

    /**
     * Tests that parenthesis are obeyed
     */
    public void testParenthesis() {
        orderTest("(1*3)+2+(1+2)*(3^4)^5");
    }

    public void testReferencesOpr() {
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
    public void testFloat() {
        floatTest("*");
        floatTest("/");
    }

    private static void floatTest(String operator) {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values

        r = s.createRow(0);
        c = r.createCell(1);
        c.setCellFormula(""+Float.MIN_VALUE + operator + Float.MIN_VALUE);

       for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2) ) {
            r = s.createRow(x);

            for (int y = 1; y < 256 && y > 0; y= (short) (y +2)) {

                c = r.createCell(y);
                c.setCellFormula("" + x+"."+y + operator + y +"."+x);


            }
        }
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.createRow(0);
            c = r.createCell(0);
            c.setCellFormula("" + Float.MAX_VALUE + operator + Float.MAX_VALUE);
        }
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        floatVerify(operator, wb);
    }

    private static void floatVerify(String operator, HSSFWorkbook wb) {

        HSSFSheet        s      = wb.getSheetAt(0);

        // don't know how to check correct result .. for the moment, we just verify that the file can be read.

        for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            HSSFRow r = s.getRow(x);

            for (int y = 1; y < 256 && y > 0; y=(short)(y+2)) {

                HSSFCell c = r.getCell(y);
                assertTrue("got a formula",c.getCellFormula()!=null);

                assertTrue("loop Formula is as expected "+x+"."+y+operator+y+"."+x+"!="+c.getCellFormula(),(
                (""+x+"."+y+operator+y+"."+x).equals(c.getCellFormula()) ));
            }
        }
    }

    public void testAreaSum() {
        areaFunctionTest("SUM");
    }

    public void testAreaAverage() {
        areaFunctionTest("AVERAGE");
    }

    public void testRefArraySum() {
        refArrayFunctionTest("SUM");
    }

    public void testAreaArraySum() {
        refAreaArrayFunctionTest("SUM");
    }

    private static void operationRefTest(String operator) {
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

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        operationalRefVerify(operator, wb);
    }

    /**
     * Opens the sheet we wrote out by binomialOperator and makes sure the formulas
     * all match what we expect (x operator y)
     */
    private static void operationalRefVerify(String operator, HSSFWorkbook wb) {

        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.getRow(0);
        c = r.getCell(1);
        //get our minimum values
        assertTrue("minval Formula is as expected A2"+operator+"A3 != "+c.getCellFormula(),
        ( ("A2"+operator+"A3").equals(c.getCellFormula())
        ));


        for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow(x);

            for (int y = 1; y < 256 && y > 0; y++) {

                int refx1;
                int refy1;
                int refx2;
                int refy2;
                if (x +50 < Short.MAX_VALUE) {
                    refx1=x+50;
                    refx2=x+46;
                } else {
                    refx1=x-4;
                    refx2=x-3;
                }

                if (y+50 < 255) {
                    refy1=y+50;
                    refy2=y+49;
                } else {
                    refy1=y-4;
                    refy2=y-3;
                }

                c = r.getCell(y);
                CellReference cr= new CellReference(refx1, refy1, false, false);
                String ref=cr.formatAsString();
                ref=cr.formatAsString();
                cr=new CellReference(refx2,refy2, false, false);
                String ref2=cr.formatAsString();


                assertTrue("loop Formula is as expected "+ref+operator+ref2+"!="+c.getCellFormula(),(
                (""+ref+operator+ref2).equals(c.getCellFormula())
                                                         )
                );
            }
        }

        //test our maximum values
        r = s.getRow(0);
        c = r.getCell(0);

        assertEquals("B1"+operator+"IV255", c.getCellFormula());
    }



    /**
     * tests order wrting out == order writing in for a given formula
     */
    private static void orderTest(String formula) {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.createRow(0);
        c = r.createCell(1);
        c.setCellFormula(formula);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s      = wb.getSheetAt(0);

        //get our minimum values
        r = s.getRow(0);
        c = r.getCell(1);
        assertTrue("minval Formula is as expected",
                   formula.equals(c.getCellFormula())
                  );
    }

    /**
     * All multi-binomial operator tests use this to create a worksheet with a
     * huge set of x operator y formulas.  Next we call binomialVerify and verify
     * that they are all how we expect.
     */
    private static void binomialOperator(String operator) {
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
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        binomialVerify(operator, wb);
    }

    /**
     * Opens the sheet we wrote out by binomialOperator and makes sure the formulas
     * all match what we expect (x operator y)
     */
    private static void binomialVerify(String operator, HSSFWorkbook wb) {
        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        //get our minimum values
        r = s.getRow(0);
        c = r.getCell(1);
        assertTrue("minval Formula is as expected 1"+operator+"1 != "+c.getCellFormula(),
        ( ("1"+operator+"1").equals(c.getCellFormula())
        ));

        for (int x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow(x);

            for (int y = 1; y < 256 && y > 0; y++) {

                c = r.getCell(y);

                assertTrue("loop Formula is as expected "+x+operator+y+"!="+c.getCellFormula(),(
                (""+x+operator+y).equals(c.getCellFormula())
                                                         )
                );
            }
        }

        //test our maximum values
        r = s.getRow(0);
        c = r.getCell(0);

        assertTrue("maxval Formula is as expected",(
        (""+Short.MAX_VALUE+operator+Short.MAX_VALUE).equals(c.getCellFormula())
                                                   )
        );
    }

    /**
     * Writes a function then tests to see if its correct
     */
    public static void areaFunctionTest(String function) {

        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;


        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellFormula(function+"(A2:A3)");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);

        assertTrue("function ="+function+"(A2:A3)",
                    ( (function+"(A2:A3)").equals((function+"(A2:A3)")) )
                  );
    }

    /**
     * Writes a function then tests to see if its correct
     */
    public void refArrayFunctionTest(String function) {

        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;


        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellFormula(function+"(A2,A3)");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);

        assertTrue("function ="+function+"(A2,A3)",
                    ( (function+"(A2,A3)").equals(c.getCellFormula()) )
                  );
    }


    /**
     * Writes a function then tests to see if its correct
     *
     */
    public void refAreaArrayFunctionTest(String function) {

        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;


        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellFormula(function+"(A2:A4,B2:B4)");
        c=r.createCell(1);
        c.setCellFormula(function+"($A$2:$A4,B$2:B4)");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);

        assertTrue("function ="+function+"(A2:A4,B2:B4)",
                    ( (function+"(A2:A4,B2:B4)").equals(c.getCellFormula()) )
                  );

        c=r.getCell(1);
        assertTrue("function ="+function+"($A$2:$A4,B$2:B4)",
                    ( (function+"($A$2:$A4,B$2:B4)").equals(c.getCellFormula()) )
                  );
    }



    public void testAbsRefs() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        HSSFRow r;
        HSSFCell c;

        r = s.createRow(0);
        c = r.createCell(0);
        c.setCellFormula("A3+A2");
        c=r.createCell(1);
        c.setCellFormula("$A3+$A2");
        c=r.createCell(2);
        c.setCellFormula("A$3+A$2");
        c=r.createCell(3);
        c.setCellFormula("$A$3+$A$2");
        c=r.createCell(4);
        c.setCellFormula("SUM($A$3,$A$2)");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);
        assertTrue("A3+A2", ("A3+A2").equals(c.getCellFormula()));
         c = r.getCell(1);
        assertTrue("$A3+$A2", ("$A3+$A2").equals(c.getCellFormula()));
         c = r.getCell(2);
        assertTrue("A$3+A$2", ("A$3+A$2").equals(c.getCellFormula()));
         c = r.getCell(3);
        assertTrue("$A$3+$A$2", ("$A$3+$A$2").equals(c.getCellFormula()));
         c = r.getCell(4);
        assertTrue("SUM($A$3,$A$2)", ("SUM($A$3,$A$2)").equals(c.getCellFormula()));
    }

    public void testSheetFunctions() {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet("A");
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        r = s.createRow(0);
        c = r.createCell(0);c.setCellValue(1);
        c = r.createCell(1);c.setCellValue(2);

        s      = wb.createSheet("B");
        r = s.createRow(0);
        c=r.createCell(0); c.setCellFormula("AVERAGE(A!A1:B1)");
        c=r.createCell(1); c.setCellFormula("A!A1+A!B1");
        c=r.createCell(2); c.setCellFormula("A!$A$1+A!$B1");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        s = wb.getSheet("B");
        r = s.getRow(0);
        c = r.getCell(0);
        assertTrue("expected: AVERAGE(A!A1:B1) got: "+c.getCellFormula(), ("AVERAGE(A!A1:B1)").equals(c.getCellFormula()));
        c = r.getCell(1);
        assertTrue("expected: A!A1+A!B1 got: "+c.getCellFormula(), ("A!A1+A!B1").equals(c.getCellFormula()));
    }

    public void testRVAoperands() throws Exception {
        File file = TempFile.createTempFile("testFormulaRVA",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;


        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellFormula("A3+A2");
        c=r.createCell(1);
        c.setCellFormula("AVERAGE(A3,A2)");
        c=r.createCell(2);
        c.setCellFormula("ROW(A3)");
        c=r.createCell(3);
        c.setCellFormula("AVERAGE(A2:A3)");
        c=r.createCell(4);
        c.setCellFormula("POWER(A2,A3)");
        c=r.createCell(5);
        c.setCellFormula("SIN(A2)");

        c=r.createCell(6);
        c.setCellFormula("SUM(A2:A3)");

        c=r.createCell(7);
        c.setCellFormula("SUM(A2,A3)");

        r = s.createRow(1);c=r.createCell(0); c.setCellValue(2.0);
         r = s.createRow(2);c=r.createCell(0); c.setCellValue(3.0);

        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());
    }

    public void testStringFormulas() {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet("A");
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        r = s.createRow(0);
        c=r.createCell(1); c.setCellFormula("UPPER(\"abc\")");
        c=r.createCell(2); c.setCellFormula("LOWER(\"ABC\")");
        c=r.createCell(3); c.setCellFormula("CONCATENATE(\" my \",\" name \")");

        HSSFTestDataSamples.writeOutAndReadBack(wb);

        wb = openSample("StringFormulas.xls");
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);
        assertEquals("UPPER(\"xyz\")", c.getCellFormula());
    }

    public void testLogicalFormulas() {

        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet("A");
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        r = s.createRow(0);
        c=r.createCell(1); c.setCellFormula("IF(A1<A2,B1,B2)");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(1);
        assertEquals("Formula in cell 1 ","IF(A1<A2,B1,B2)",c.getCellFormula());
    }

    public void testDateFormulas() {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet("testSheet1");
        HSSFRow          r      = null;
        HSSFCell         c      = null;

        r = s.createRow(0 );
        c = r.createCell(0 );

        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
        c.setCellValue(new Date());
        c.setCellStyle(cellStyle);

       // assertEquals("Checking hour = " + hour, date.getTime().getTime(),
       //              HSSFDateUtil.getJavaDate(excelDate).getTime());

        for (int k=1; k < 100; k++) {
          r=s.createRow(k);
          c=r.createCell(0);
          c.setCellFormula("A"+(k)+"+1");
          c.setCellStyle(cellStyle);
        }

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }


    public void testIfFormulas() {
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet("testSheet1");
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        r = s.createRow(0);
        c=r.createCell(1); c.setCellValue(1);
        c=r.createCell(2); c.setCellValue(2);
        c=r.createCell(3); c.setCellFormula("MAX(A1:B1)");
        c=r.createCell(4); c.setCellFormula("IF(A1=D1,\"A1\",\"B1\")");

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(4);

        assertTrue("expected: IF(A1=D1,\"A1\",\"B1\") got "+c.getCellFormula(), ("IF(A1=D1,\"A1\",\"B1\")").equals(c.getCellFormula()));

        wb = openSample("IfFormulaTest.xls");
        s = wb.getSheetAt(0);
        r = s.getRow(3);
        c = r.getCell(0);
        assertTrue("expected: IF(A3=A1,\"A1\",\"A2\") got "+c.getCellFormula(), ("IF(A3=A1,\"A1\",\"A2\")").equals(c.getCellFormula()));
        //c = r.getCell((short)1);
        //assertTrue("expected: A!A1+A!B1 got: "+c.getCellFormula(), ("A!A1+A!B1").equals(c.getCellFormula()));


        wb     = new HSSFWorkbook();
        s      = wb.createSheet("testSheet1");
        r      = null;
        c      = null;
        r = s.createRow(0);
        c=r.createCell(0); c.setCellFormula("IF(1=1,0,1)");

        HSSFTestDataSamples.writeOutAndReadBack(wb);

        wb     = new HSSFWorkbook();
        s      = wb.createSheet("testSheet1");
        r      = null;
        c      = null;
        r = s.createRow(0);
        c=r.createCell(0);
        c.setCellValue(1);

        c=r.createCell(1);
        c.setCellValue(3);


        HSSFCell formulaCell=r.createCell(3);

        r = s.createRow(1);
        c=r.createCell(0);
        c.setCellValue(3);

        c=r.createCell(1);
        c.setCellValue(7);

        formulaCell.setCellFormula("IF(A1=B1,AVERAGE(A1:B1),AVERAGE(A2:B2))");

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    public void testSumIf() {
        String function ="SUMIF(A1:A5,\">4000\",B1:B5)";

        HSSFWorkbook wb = openSample("sumifformula.xls");

        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = s.getRow(0);
        HSSFCell         c      = r.getCell(2);
        assertEquals(function, c.getCellFormula());


        wb     = new HSSFWorkbook();
        s      = wb.createSheet();

        r = s.createRow(0);
        c=r.createCell(0); c.setCellValue(1000);
        c=r.createCell(1); c.setCellValue(1);


        r = s.createRow(1);
        c=r.createCell(0); c.setCellValue(2000);
        c=r.createCell(1); c.setCellValue(2);

        r = s.createRow(2);
        c=r.createCell(0); c.setCellValue(3000);
        c=r.createCell(1); c.setCellValue(3);

        r = s.createRow(3);
        c=r.createCell(0); c.setCellValue(4000);
        c=r.createCell(1); c.setCellValue(4);

        r = s.createRow(4);
        c=r.createCell(0); c.setCellValue(5000);
        c=r.createCell(1); c.setCellValue(5);

        r = s.getRow(0);
        c=r.createCell(2); c.setCellFormula(function);

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    public void testSquareMacro() {
        HSSFWorkbook w = openSample("SquareMacro.xls");

        HSSFSheet s0 = w.getSheetAt(0);
        HSSFRow[] r = {s0.getRow(0), s0.getRow(1)};

        HSSFCell a1 = r[0].getCell(0);
        assertEquals("square(1)", a1.getCellFormula());
        assertEquals(1d, a1.getNumericCellValue(), 1e-9);

        HSSFCell a2 = r[1].getCell(0);
        assertEquals("square(2)", a2.getCellFormula());
        assertEquals(4d, a2.getNumericCellValue(), 1e-9);

        HSSFCell b1 = r[0].getCell(1);
        assertEquals("IF(TRUE,square(1))", b1.getCellFormula());
        assertEquals(1d, b1.getNumericCellValue(), 1e-9);

        HSSFCell b2 = r[1].getCell(1);
        assertEquals("IF(TRUE,square(2))", b2.getCellFormula());
        assertEquals(4d, b2.getNumericCellValue(), 1e-9);

        HSSFCell c1 = r[0].getCell(2);
        assertEquals("square(square(1))", c1.getCellFormula());
        assertEquals(1d, c1.getNumericCellValue(), 1e-9);

        HSSFCell c2 = r[1].getCell(2);
        assertEquals("square(square(2))", c2.getCellFormula());
        assertEquals(16d, c2.getNumericCellValue(), 1e-9);

        HSSFCell d1 = r[0].getCell(3);
        assertEquals("square(one())", d1.getCellFormula());
        assertEquals(1d, d1.getNumericCellValue(), 1e-9);

        HSSFCell d2 = r[1].getCell(3);
        assertEquals("square(two())", d2.getCellFormula());
        assertEquals(4d, d2.getNumericCellValue(), 1e-9);
    }

    public void testStringFormulaRead() {
        HSSFWorkbook w = openSample("StringFormulas.xls");
        HSSFCell c = w.getSheetAt(0).getRow(0).getCell(0);
        assertEquals("String Cell value","XYZ",c.getRichStringCellValue().getString());
    }

    /** test for bug 34021*/
    public void testComplexSheetRefs () throws IOException {
         HSSFWorkbook sb = new HSSFWorkbook();
         HSSFSheet s1 = sb.createSheet("Sheet a.1");
         HSSFSheet s2 = sb.createSheet("Sheet.A");
         s2.createRow(1).createCell(2).setCellFormula("'Sheet a.1'!A1");
         s1.createRow(1).createCell(2).setCellFormula("'Sheet.A'!A1");
         File file = TempFile.createTempFile("testComplexSheetRefs",".xls");
         sb.write(new FileOutputStream(file));
    }

    /** Unknown Ptg 3C*/
    public void test27272_1() throws Exception {
        HSSFWorkbook wb = openSample("27272_1.xls");
        wb.getSheetAt(0);
        assertEquals("Reference for named range ", "Compliance!#REF!",wb.getNameAt(0).getRefersToFormula());
        File outF = TempFile.createTempFile("bug27272_1",".xls");
        wb.write(new FileOutputStream(outF));
        System.out.println("Open "+outF.getAbsolutePath()+" in Excel");
    }
    /** Unknown Ptg 3D*/
    public void test27272_2() throws Exception {
        HSSFWorkbook wb = openSample("27272_2.xls");
        assertEquals("Reference for named range ", "LOAD.POD_HISTORIES!#REF!",wb.getNameAt(0).getRefersToFormula());
        File outF = TempFile.createTempFile("bug27272_2",".xls");
        wb.write(new FileOutputStream(outF));
        System.out.println("Open "+outF.getAbsolutePath()+" in Excel");
    }

    /** MissingArgPtg */
    public void testMissingArgPtg() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet("Sheet1").createRow(4).createCell(0);
        cell.setCellFormula("IF(A1=\"A\",1,)");
    }

    public void testSharedFormula() {
        HSSFWorkbook wb = openSample("SharedFormulaTest.xls");

        assertEquals("A$1*2", wb.getSheetAt(0).getRow(1).getCell(1).toString());
        assertEquals("$A11*2", wb.getSheetAt(0).getRow(11).getCell(1).toString());
        assertEquals("DZ2*2", wb.getSheetAt(0).getRow(1).getCell(128).toString());
        assertEquals("B32770*2", wb.getSheetAt(0).getRow(32768).getCell(1).toString());
    }

    /**
     * Test creation / evaluation of formulas with sheet-level names
     */
    public void testSheetLevelFormulas(){
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFRow row;
        HSSFSheet sh1 = wb.createSheet("Sheet1");
        HSSFName nm1 = wb.createName();
        nm1.setNameName("sales_1");
        nm1.setSheetIndex(0);
        nm1.setRefersToFormula("Sheet1!$A$1");
        row = sh1.createRow(0);
        row.createCell(0).setCellValue(3);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*2");


        HSSFSheet sh2 = wb.createSheet("Sheet2");
        HSSFName nm2 = wb.createName();
        nm2.setNameName("sales_1");
        nm2.setSheetIndex(1);
        nm2.setRefersToFormula("Sheet2!$A$1");

        row = sh2.createRow(0);
        row.createCell(0).setCellValue(5);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*3");

        //check that NamePtg refers to the correct NameRecord
        Ptg[] ptgs1 = HSSFFormulaParser.parse("sales_1", wb, FormulaType.CELL, 0);
        NamePtg nPtg1 = (NamePtg)ptgs1[0];
        assertSame(nm1, wb.getNameAt(nPtg1.getIndex()));

        Ptg[] ptgs2 = HSSFFormulaParser.parse("sales_1", wb, FormulaType.CELL, 1);
        NamePtg nPtg2 = (NamePtg)ptgs2[0];
        assertSame(nm2, wb.getNameAt(nPtg2.getIndex()));

        //check that the formula evaluator returns the correct result
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        assertEquals(3.0, evaluator.evaluate(sh1.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(6.0, evaluator.evaluate(sh1.getRow(0).getCell(2)).getNumberValue(), 0.0);

        assertEquals(5.0, evaluator.evaluate(sh2.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(15.0, evaluator.evaluate(sh2.getRow(0).getCell(2)).getNumberValue(), 0.0);
    }

    /**
     * Verify that FormulaParser handles defined names beginning with underscores,
     * see Bug #49640
     */
    public void testFormulasWithUnderscore(){
        HSSFWorkbook wb = new HSSFWorkbook();
        Name nm1 = wb.createName();
        nm1.setNameName("_score1");
        nm1.setRefersToFormula("A1");

        Name nm2 = wb.createName();
        nm2.setNameName("_score2");
        nm2.setRefersToFormula("A2");

        Sheet sheet = wb.createSheet();
        Cell cell = sheet.createRow(0).createCell(2);
        cell.setCellFormula("_score1*SUM(_score1+_score2)");
        assertEquals("_score1*SUM(_score1+_score2)", cell.getCellFormula());
    }
}
