/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.ss.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * Test {@link FormulaParser}'s handling of row numbers at the edge of the
 * HSSF/XSSF ranges.
 * 
 * @author David North
 */
public class TestFormulaParser {

    @Test
    public void testHSSFFailsForOver65536() {
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:65537", workbook, FormulaType.CELL, 0);
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
            // expected here
        }
    }

    private static void checkHSSFFormula(String formula) {
        HSSFWorkbook wb = new HSSFWorkbook();
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(wb);
        FormulaParser.parse(formula, workbook, FormulaType.CELL, 0);
        IOUtils.closeQuietly(wb);
    } 
    private static void checkXSSFFormula(String formula) {
        XSSFWorkbook wb = new XSSFWorkbook();
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(wb);
        FormulaParser.parse(formula, workbook, FormulaType.CELL, 0);
        IOUtils.closeQuietly(wb);
    } 
    private static void checkFormula(String formula) {
        checkHSSFFormula(formula);
        checkXSSFFormula(formula);
    }

    @Test
    public void testHSSFPassCase() {
        checkHSSFFormula("Sheet1!1:65536");
    }

    @Test
    public void testXSSFWorksForOver65536() {
        checkXSSFFormula("Sheet1!1:65537");
    }

    @Test
    public void testXSSFFailCase() {
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(new XSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:1048577", workbook, FormulaType.CELL, 0); // one more than max rows.
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
            // expected here
        }
    }
    
    // copied from org.apache.poi.hssf.model.TestFormulaParser
    @Test
    public void testMacroFunction() throws Exception {
        // testNames.xlsm contains a VB function called 'myFunc'
        final String testFile = "testNames.xlsm";
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook(testFile);
        try {
            XSSFEvaluationWorkbook workbook = XSSFEvaluationWorkbook.create(wb);
    
            //Expected ptg stack: [NamePtg(myFunc), StringPtg(arg), (additional operands would go here...), FunctionPtg(myFunc)]
            Ptg[] ptg = FormulaParser.parse("myFunc(\"arg\")", workbook, FormulaType.CELL, -1);
            assertEquals(3, ptg.length);
    
            // the name gets encoded as the first operand on the stack
            NameXPxg tname = (NameXPxg) ptg[0];
            assertEquals("myFunc", tname.toFormulaString());
            
            // the function's arguments are pushed onto the stack from left-to-right as OperandPtgs
            StringPtg arg = (StringPtg) ptg[1];
            assertEquals("arg", arg.getValue());
    
            // The external FunctionPtg is the last Ptg added to the stack
            // During formula evaluation, this Ptg pops off the the appropriate number of
            // arguments (getNumberOfOperands()) and pushes the result on the stack 
            AbstractFunctionPtg tfunc = (AbstractFunctionPtg) ptg[2];
            assertTrue(tfunc.isExternalFunction());
            
            // confirm formula parsing is case-insensitive
            FormulaParser.parse("mYfUnC(\"arg\")", workbook, FormulaType.CELL, -1);
            
            // confirm formula parsing doesn't care about argument count or type
            // this should only throw an error when evaluating the formula.
            FormulaParser.parse("myFunc()", workbook, FormulaType.CELL, -1);
            FormulaParser.parse("myFunc(\"arg\", 0, TRUE)", workbook, FormulaType.CELL, -1);
            
            // A completely unknown formula name (not saved in workbook) should still be parseable and renderable
            // but will throw an NotImplementedFunctionException or return a #NAME? error value if evaluated.
            FormulaParser.parse("yourFunc(\"arg\")", workbook, FormulaType.CELL, -1);
            
            // Make sure workbook can be written and read
            XSSFTestDataSamples.writeOutAndReadBack(wb).close();
            
            // Manually check to make sure file isn't corrupted
            // TODO: develop a process for occasionally manually reviewing workbooks
            // to verify workbooks are not corrupted
            /*
            final File fileIn = XSSFTestDataSamples.getSampleFile(testFile);
            final File reSavedFile = new File(fileIn.getParentFile(), fileIn.getName().replace(".xlsm", "-saved.xlsm"));
            final FileOutputStream fos = new FileOutputStream(reSavedFile);
            wb.write(fos);
            fos.close();
            */
        } finally {
            wb.close();
        }
    }
    
    @Test
    public void testParserErrors() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("testNames.xlsm");
        try {
            XSSFEvaluationWorkbook workbook = XSSFEvaluationWorkbook.create(wb);
            
            parseExpectedException("(");
            parseExpectedException(")");
            parseExpectedException("+");
            parseExpectedException("42+");
            parseExpectedException("IF()");
            parseExpectedException("IF("); //no closing paren
            parseExpectedException("myFunc(", workbook); //no closing paren
        } finally {
            wb.close();
        }
    }
    
    private static void parseExpectedException(String formula) {
        parseExpectedException(formula, null);
    }
    
    /** confirm formula has invalid syntax and parsing the formula results in FormulaParseException
     */
    private static void parseExpectedException(String formula, FormulaParsingWorkbook wb) {
        try {
            FormulaParser.parse(formula, wb, FormulaType.CELL, -1);
            fail("Expected FormulaParseException: " + formula);
        } catch (final FormulaParseException e) {
            // expected during successful test
            assertNotNull(e.getMessage());
        }
    }
    
    // trivial case for bug 60219: FormulaParser can't parse external references when sheet name is quoted
    @Test
    public void testParseExternalReferencesWithUnquotedSheetName() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpwb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs = FormulaParser.parse("[1]Sheet1!A1", fpwb, FormulaType.CELL, -1);
        // org.apache.poi.ss.formula.ptg.Ref3DPxg [ [workbook=1] sheet=Sheet 1 ! A1]
        assertEquals("Ptgs length", 1, ptgs.length);
        assertTrue("Ptg class", ptgs[0] instanceof Ref3DPxg);
        Ref3DPxg pxg = (Ref3DPxg) ptgs[0];
        assertEquals("External workbook number", 1, pxg.getExternalWorkbookNumber());
        assertEquals("Sheet name", "Sheet1", pxg.getSheetName());
        assertEquals("Row", 0, pxg.getRow());
        assertEquals("Column", 0, pxg.getColumn());
        wb.close();
    }
    
    // bug 60219: FormulaParser can't parse external references when sheet name is quoted
    @Test
    public void testParseExternalReferencesWithQuotedSheetName() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpwb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs = FormulaParser.parse("'[1]Sheet 1'!A1", fpwb, FormulaType.CELL, -1);
        // org.apache.poi.ss.formula.ptg.Ref3DPxg [ [workbook=1] sheet=Sheet 1 ! A1]
        assertEquals("Ptgs length", 1, ptgs.length);
        assertTrue("Ptg class", ptgs[0] instanceof Ref3DPxg);
        Ref3DPxg pxg = (Ref3DPxg) ptgs[0];
        assertEquals("External workbook number", 1, pxg.getExternalWorkbookNumber());
        assertEquals("Sheet name", "Sheet 1", pxg.getSheetName());
        assertEquals("Row", 0, pxg.getRow());
        assertEquals("Column", 0, pxg.getColumn());
        wb.close();
    }

    // bug 60260
    @Test
    public void testUnicodeSheetName() {
        checkFormula("'Sheet\u30FB1'!A1:A6");
    }
}
