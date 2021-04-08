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

package org.apache.poi.hssf.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Locale;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.usermodel.FormulaExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFName;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.constant.ErrorConstant;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.junit.jupiter.api.Test;

/**
 * Test the low level formula parser functionality. High level tests are to
 * be done via usermodel/HSSFCell.setFormulaValue().
 */
final class TestFormulaParser {

    /**
     * @return parsed token array already confirmed not <code>null</code>
     */
    /* package */ static Ptg[] parseFormula(String formula) {
        Ptg[] result = HSSFFormulaParser.parse(formula, null);
        assertNotNull(result, "Ptg array should not be null");
        return result;
    }
    private static String toFormulaString(Ptg[] ptgs) {
        return HSSFFormulaParser.toFormulaString(null, ptgs);
    }

    @Test
    void testSimpleFormula() {
        confirmTokenClasses("2+2",IntPtg.class, IntPtg.class, AddPtg.class);
    }

    @Test
    void testFormulaWithSpace1() {
        confirmTokenClasses(" 2 + 2 ",IntPtg.class, IntPtg.class, AddPtg.class);
    }

    @Test
    void testFormulaWithSpace2() {
        Ptg[] ptgs = parseFormula("2+ sum( 3 , 4) ");
        assertEquals(5, ptgs.length);
    }

    @Test
    void testFormulaWithSpaceNRef() {
        Ptg[] ptgs = parseFormula("sum( A2:A3 )");
        assertEquals(2, ptgs.length);
    }

    @Test
    void testFormulaWithString() {
        Ptg[] ptgs = parseFormula("\"hello\" & \"world\" ");
        assertEquals(3, ptgs.length);
    }

    @Test
    void testTRUE() {
        Ptg[] ptgs = parseFormula("TRUE");
        assertEquals(1, ptgs.length);
        BoolPtg flag  = (BoolPtg) ptgs[0];
        assertTrue(flag.getValue());
    }

    @Test
    void testSumIf() {
        Ptg[] ptgs = parseFormula("SUMIF(A1:A5,\">4000\",B1:B5)");
        assertEquals(4, ptgs.length);
    }

    /**
     * Bug Reported by xt-jens.riis@nokia.com (Jens Riis)
     * Refers to Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=17582">#17582</a>
     *
     */
    @Test
    void testNonAlphaFormula() {
        Ptg[] ptgs = parseFormula("\"TOTAL[\"&F3&\"]\"");
        confirmTokenClasses(ptgs, StringPtg.class, RefPtg.class, ConcatPtg.class, StringPtg.class, ConcatPtg.class);
        assertEquals("TOTAL[", ((StringPtg)ptgs[0]).getValue());
    }

    @Test
    void testMacroFunction() throws IOException {
        // testNames.xls contains a VB function called 'myFunc'
        final String testFile = "testNames.xls";
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(testFile)) {
            HSSFEvaluationWorkbook book = HSSFEvaluationWorkbook.create(wb);

            //Expected ptg stack: [NamePtg(myFunc), StringPtg(arg), (additional operands go here...), FunctionPtg(myFunc)]
            Ptg[] ptg = FormulaParser.parse("myFunc(\"arg\")", book, FormulaType.CELL, -1);
            assertEquals(3, ptg.length);

            // the name gets encoded as the first operand on the stack
            NamePtg tname = (NamePtg) ptg[0];
            assertEquals("myFunc", tname.toFormulaString(book));

            // the function's arguments are pushed onto the stack from left-to-right as OperandPtgs
            StringPtg arg = (StringPtg) ptg[1];
            assertEquals("arg", arg.getValue());

            // The external FunctionPtg is the last Ptg added to the stack
            // During formula evaluation, this Ptg pops off the the appropriate number of
            // arguments (getNumberOfOperands()) and pushes the result on the stack
            AbstractFunctionPtg tfunc = (AbstractFunctionPtg) ptg[2]; //FuncVarPtg
            assertTrue(tfunc.isExternalFunction());

            // confirm formula parsing is case-insensitive
            FormulaParser.parse("mYfUnC(\"arg\")", book, FormulaType.CELL, -1);

            // confirm formula parsing doesn't care about argument count or type
            // this should only throw an error when evaluating the formula.
            FormulaParser.parse("myFunc()", book, FormulaType.CELL, -1);
            FormulaParser.parse("myFunc(\"arg\", 0, TRUE)", book, FormulaType.CELL, -1);

            // A completely unknown formula name (not saved in workbook) should still be parseable and renderable
            // but will throw an NotImplementedFunctionException or return a #NAME? error value if evaluated.
            FormulaParser.parse("yourFunc(\"arg\")", book, FormulaType.CELL, -1);

            // Verify that myFunc and yourFunc were successfully added to Workbook names
            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                // HSSFWorkbook/EXCEL97-specific side-effects user-defined function names must be added to Workbook's defined names in order to be saved.
                HSSFName myFunc = wb2.getName("myFunc");
                assertNotNull(myFunc);
                assertEqualsIgnoreCase("myFunc", myFunc.getNameName());
                HSSFName yourFunc = wb2.getName("yourFunc");
                assertNotNull(yourFunc);
                assertEqualsIgnoreCase("yourFunc", yourFunc.getNameName());

                // Manually check to make sure file isn't corrupted
                // TODO: develop a process for occasionally manually reviewing workbooks
                // to verify workbooks are not corrupted
                /*
                final File fileIn = HSSFTestDataSamples.getSampleFile(testFile);
                final File reSavedFile = new File(fileIn.getParentFile(), fileIn.getName().replace(".xls", "-saved.xls"));
                FileOutputStream fos = new FileOutputStream(reSavedFile);
                wb2.write(fos);
                fos.close();
                */
            }
        }
    }

    private static void assertEqualsIgnoreCase(String expected, String actual) {
        assertEquals(expected.toLowerCase(Locale.ROOT), actual.toLowerCase(Locale.ROOT));
    }

    @Test
    void testEmbeddedSlash() {
        confirmTokenClasses("HYPERLINK(\"http://www.jakarta.org\",\"Jakarta\")",
                        StringPtg.class, StringPtg.class, FuncVarPtg.class);
    }

    @Test
    void testConcatenate() {
        confirmTokenClasses("CONCATENATE(\"first\",\"second\")",
                StringPtg.class, StringPtg.class, FuncVarPtg.class);
    }

    @Test
    void testWorksheetReferences() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            HSSFSheet sheet1 = wb.createSheet("NoQuotesNeeded");
            sheet1.createRow(0).createCell(0).setCellValue("NoQuotesNeeded");
            HSSFSheet sheet2 = wb.createSheet("Quotes Needed Here &#$@");
            sheet2.createRow(0).createCell(0).setCellValue("Quotes Needed Here &#$@");

            HSSFSheet sheet = wb.createSheet("Test");
            HSSFRow row = sheet.createRow(0);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            HSSFCell cell;
            String act;

            cell = row.createCell(0);
            cell.setCellFormula("NoQuotesNeeded!A1");
            act = evaluator.evaluate(cell).getStringValue();
            assertEquals("NoQuotesNeeded", act);

            cell = row.createCell(1);
            cell.setCellFormula("'Quotes Needed Here &#$@'!A1");
            act = evaluator.evaluate(cell).getStringValue();
            assertEquals("Quotes Needed Here &#$@", act);
        }
    }

    @Test
    void testUnaryMinus() {
        confirmTokenClasses("-A1", RefPtg.class, UnaryMinusPtg.class);
    }

    @Test
    void testUnaryPlus() {
        confirmTokenClasses("+A1", RefPtg.class, UnaryPlusPtg.class);
    }

    /**
     * There may be multiple ways to encode an expression involving {@link UnaryPlusPtg}
     * or {@link UnaryMinusPtg}.  These may be perfectly equivalent from a formula
     * evaluation perspective, or formula rendering.  However, differences in the way
     * POI encodes formulas may cause unnecessary confusion.  These non-critical tests
     * check that POI follows the same encoding rules as Excel.
     */
    @Test
    void testExactEncodingOfUnaryPlusAndMinus() {
        // as tested in Excel:
        confirmUnary("-3", -3, NumberPtg.class);
        confirmUnary("--4", -4, NumberPtg.class, UnaryMinusPtg.class);
        confirmUnary("+++5", 5, IntPtg.class, UnaryPlusPtg.class, UnaryPlusPtg.class);
        confirmUnary("++-6", -6, NumberPtg.class, UnaryPlusPtg.class, UnaryPlusPtg.class);

        // Spaces muck things up a bit.  It would be clearer why the following cases are
        // reasonable if POI encoded tAttrSpace in the right places.
        // Otherwise these differences look capricious.
        confirmUnary("+ 12", 12, IntPtg.class, UnaryPlusPtg.class);
        confirmUnary("- 13", 13, IntPtg.class, UnaryMinusPtg.class);
    }

    private static void confirmUnary(String formulaText, double val, Class<?>...expectedTokenTypes) {
        Ptg[] ptgs = parseFormula(formulaText);
        confirmTokenClasses(ptgs, expectedTokenTypes);
        Ptg ptg0 = ptgs[0];
        if (ptg0 instanceof IntPtg) {
            IntPtg intPtg = (IntPtg) ptg0;
            assertEquals((int)val, intPtg.getValue());
        } else if (ptg0 instanceof NumberPtg) {
            NumberPtg numberPtg = (NumberPtg) ptg0;
            assertEquals(val, numberPtg.getValue(), 0.0);
        } else {
            fail("bad ptg0 " + ptg0);
        }
    }

    @Test
    void testLeadingSpaceInString() {
        String value = "  hi  ";
        Ptg[] ptgs = parseFormula("\"" + value + "\"");
        confirmTokenClasses(ptgs, StringPtg.class);
        assertEquals(((StringPtg) ptgs[0]).getValue(), value, "ptg0 contains exact value");
    }

    @Test
    void testLookupAndMatchFunctionArgs() {
        Ptg[] ptgs = parseFormula("lookup(A1, A3:A52, B3:B52)");
        confirmTokenClasses(ptgs, RefPtg.class, AreaPtg.class, AreaPtg.class, FuncVarPtg.class);
        assertEquals(ptgs[0].getPtgClass(), Ptg.CLASS_VALUE, "ptg0 has Value class");

        ptgs = parseFormula("match(A1, A3:A52)");
        confirmTokenClasses(ptgs, RefPtg.class, AreaPtg.class, FuncVarPtg.class);
        assertEquals(ptgs[0].getPtgClass(), Ptg.CLASS_VALUE, "ptg0 has Value class");
    }

    /** bug 33160*/
    @Test
    void testLargeInt() {
        confirmTokenClasses("40", IntPtg.class);
        confirmTokenClasses("40000", IntPtg.class);
    }

    /** bug 33160 */
    @Test
    void testSimpleLongFormula() {
        confirmTokenClasses("40000/2", IntPtg.class, IntPtg.class, DividePtg.class);
    }

    /** bug 35027, underscore in sheet name */
    @Test
    void testUnderscore() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet1 = wb.createSheet("Cash_Flow");
            sheet1.createRow(0).createCell(0).setCellValue("Cash_Flow");

            HSSFSheet sheet = wb.createSheet("Test");
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell;

            cell = row.createCell(0);
            cell.setCellFormula("Cash_Flow!A1");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            String act = evaluator.evaluate(cell).getStringValue();
            assertEquals("Cash_Flow", act);
        }
    }

    /** bug 49725, defined names with underscore */
    @Test
    void testNamesWithUnderscore() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook(); //or new XSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("NamesWithUnderscore");

        HSSFName nm;

        nm = wb.createName();
        nm.setNameName("DA6_LEO_WBS_Number");
        nm.setRefersToFormula("33");

        nm = wb.createName();
        nm.setNameName("DA6_LEO_WBS_Name");
        nm.setRefersToFormula("33");

        nm = wb.createName();
        nm.setNameName("A1_");
        nm.setRefersToFormula("22");

        nm = wb.createName();
        nm.setNameName("_A1");
        nm.setRefersToFormula("11");

        nm = wb.createName();
        nm.setNameName("A_1");
        nm.setRefersToFormula("44");

        nm = wb.createName();
        nm.setNameName("A_1_");
        nm.setRefersToFormula("44");

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);

        cell.setCellFormula("DA6_LEO_WBS_Number*2");
        assertEquals("DA6_LEO_WBS_Number*2", cell.getCellFormula());

        cell.setCellFormula("(A1_*_A1+A_1)/A_1_");
        assertEquals("(A1_*_A1+A_1)/A_1_", cell.getCellFormula());

        cell.setCellFormula("INDEX(DA6_LEO_WBS_Name,MATCH($A3,DA6_LEO_WBS_Number,0))");
        assertEquals("INDEX(DA6_LEO_WBS_Name,MATCH($A3,DA6_LEO_WBS_Number,0))", cell.getCellFormula());

        wb.close();
    }

    // bug 38396 : Formula with exponential numbers not parsed correctly.
    @Test
    void testExponentialParsing() {
        confirmTokenClasses("1.3E21/2",  NumberPtg.class, IntPtg.class, DividePtg.class);
        confirmTokenClasses("1322E21/2", NumberPtg.class, IntPtg.class, DividePtg.class);
        confirmTokenClasses("1.3E1/2",   NumberPtg.class, IntPtg.class, DividePtg.class);
    }

    @Test
    void testExponentialInSheet() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        wb.createSheet("Cash_Flow");

        HSSFSheet sheet = wb.createSheet("Test");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        String formula;

        cell.setCellFormula("1.3E21/3");
        formula = cell.getCellFormula();
        assertEquals("1.3E+21/3", formula);

        cell.setCellFormula("-1.3E21/3");
        formula = cell.getCellFormula();
        assertEquals("-1.3E+21/3", formula);

        cell.setCellFormula("1322E21/3");
        formula = cell.getCellFormula();
        assertEquals("1.322E+24/3", formula);

        cell.setCellFormula("-1322E21/3");
        formula = cell.getCellFormula();
        assertEquals("-1.322E+24/3", formula);

        cell.setCellFormula("1.3E1/3");
        formula = cell.getCellFormula();
        assertEquals("13/3", formula);

        cell.setCellFormula("-1.3E1/3");
        formula = cell.getCellFormula();
        assertEquals("-13/3", formula);

        cell.setCellFormula("1.3E-4/3");
        formula = cell.getCellFormula();
        assertEquals("0.00013/3", formula);

        cell.setCellFormula("-1.3E-4/3");
        formula = cell.getCellFormula();
        assertEquals("-0.00013/3", formula);

        cell.setCellFormula("13E-15/3");
        formula = cell.getCellFormula();
        assertEquals("0.000000000000013/3", formula);

        cell.setCellFormula("-13E-15/3");
        formula = cell.getCellFormula();
        assertEquals("-0.000000000000013/3", formula);

        cell.setCellFormula("1.3E3/3");
        formula = cell.getCellFormula();
        assertEquals("1300/3", formula);

        cell.setCellFormula("-1.3E3/3");
        formula = cell.getCellFormula();
        assertEquals("-1300/3", formula);

        cell.setCellFormula("1300000000000000/3");
        formula = cell.getCellFormula();
        assertEquals("1300000000000000/3", formula);

        cell.setCellFormula("-1300000000000000/3");
        formula = cell.getCellFormula();
        assertEquals("-1300000000000000/3", formula);

        cell.setCellFormula("-10E-1/3.1E2*4E3/3E4");
        formula = cell.getCellFormula();
        assertEquals("-1/310*4000/30000", formula);

        wb.close();
    }

    @Test
    void testNumbers() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        wb.createSheet("Cash_Flow");

        HSSFSheet sheet = wb.createSheet("Test");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        String formula;

        // starts from decimal point

        cell.setCellFormula(".1");
        formula = cell.getCellFormula();
        assertEquals("0.1", formula);

        cell.setCellFormula("+.1");
        formula = cell.getCellFormula();
        assertEquals("0.1", formula);

        cell.setCellFormula("-.1");
        formula = cell.getCellFormula();
        assertEquals("-0.1", formula);

        // has exponent

        cell.setCellFormula("10E1");
        formula = cell.getCellFormula();
        assertEquals("100", formula);

        cell.setCellFormula("10E+1");
        formula = cell.getCellFormula();
        assertEquals("100", formula);

        cell.setCellFormula("10E-1");
        formula = cell.getCellFormula();
        assertEquals("1", formula);

        wb.close();
    }

    @Test
    void testRanges() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        wb.createSheet("Cash_Flow");

        HSSFSheet sheet = wb.createSheet("Test");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        String formula;

        cell.setCellFormula("A1.A2");
        formula = cell.getCellFormula();
        assertEquals("A1:A2", formula);

        cell.setCellFormula("A1..A2");
        formula = cell.getCellFormula();
        assertEquals("A1:A2", formula);

        cell.setCellFormula("A1...A2");
        formula = cell.getCellFormula();
        assertEquals("A1:A2", formula);

        wb.close();
    }

    @Test
    void testMultiSheetReference() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        wb.createSheet("Cash_Flow");
        wb.createSheet("Test Sheet");

        HSSFSheet sheet = wb.createSheet("Test");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        String formula;


        // References to a single cell:

        // One sheet
        cell.setCellFormula("Cash_Flow!A1");
        formula = cell.getCellFormula();
        assertEquals("Cash_Flow!A1", formula);

        // Then the other
        cell.setCellFormula("'Test Sheet'!A1");
        formula = cell.getCellFormula();
        assertEquals("'Test Sheet'!A1", formula);

        // Now both
        cell.setCellFormula("Cash_Flow:'Test Sheet'!A1");
        formula = cell.getCellFormula();
        assertEquals("Cash_Flow:'Test Sheet'!A1", formula);


        // References to a range (area) of cells:

        // One sheet
        cell.setCellFormula("Cash_Flow!A1:B2");
        formula = cell.getCellFormula();
        assertEquals("Cash_Flow!A1:B2", formula);

        // Then the other
        cell.setCellFormula("'Test Sheet'!A1:B2");
        formula = cell.getCellFormula();
        assertEquals("'Test Sheet'!A1:B2", formula);

        // Now both
        cell.setCellFormula("Cash_Flow:'Test Sheet'!A1:B2");
        formula = cell.getCellFormula();
        assertEquals("Cash_Flow:'Test Sheet'!A1:B2", formula);

        wb.close();
    }

    /**
     * Test for bug observable at svn revision 618865 (5-Feb-2008)<br>
     * a formula consisting of a single no-arg function got rendered without the function braces
     */
    @Test
    void testToFormulaStringZeroArgFunction() throws IOException {
        HSSFWorkbook book = new HSSFWorkbook();

        Ptg[] ptgs = {
                FuncPtg.create(10),
        };
        assertEquals("NA()", HSSFFormulaParser.toFormulaString(book, ptgs));

        book.close();
    }

    @Test
    void testPercent() {

        confirmTokenClasses("5%", IntPtg.class, PercentPtg.class);
        // spaces OK
        confirmTokenClasses(" 250 % ", IntPtg.class, PercentPtg.class);
        // double percent OK
        confirmTokenClasses("12345.678%%", NumberPtg.class, PercentPtg.class, PercentPtg.class);

        // percent of a bracketed expression
        confirmTokenClasses("(A1+35)%*B1%", RefPtg.class, IntPtg.class, AddPtg.class, ParenthesisPtg.class,
                PercentPtg.class, RefPtg.class, PercentPtg.class, MultiplyPtg.class);

        // percent of a text quantity
        confirmTokenClasses("\"8.75\"%", StringPtg.class, PercentPtg.class);

        // percent to the power of
        confirmTokenClasses("50%^3", IntPtg.class, PercentPtg.class, IntPtg.class, PowerPtg.class);

        // things that parse OK but would *evaluate* to an error
        confirmTokenClasses("\"abc\"%", StringPtg.class, PercentPtg.class);
        confirmTokenClasses("#N/A%", ErrPtg.class, PercentPtg.class);
    }

    /**
     * Tests combinations of various operators in the absence of brackets
     */
    @Test
    void testPrecedenceAndAssociativity() {

        // TRUE=TRUE=2=2  evaluates to FALSE
        confirmTokenClasses("TRUE=TRUE=2=2", BoolPtg.class, BoolPtg.class, EqualPtg.class,
                IntPtg.class, EqualPtg.class, IntPtg.class, EqualPtg.class);

        //  2^3^2    evaluates to 64 not 512
        confirmTokenClasses("2^3^2", IntPtg.class, IntPtg.class, PowerPtg.class,
                IntPtg.class, PowerPtg.class);

        // "abc" & 2 + 3 & "def"   evaluates to "abc5def"
        confirmTokenClasses("\"abc\"&2+3&\"def\"", StringPtg.class, IntPtg.class, IntPtg.class,
                AddPtg.class, ConcatPtg.class, StringPtg.class, ConcatPtg.class);

        //  (1 / 2) - (3 * 4)
        confirmTokenClasses("1/2-3*4", IntPtg.class, IntPtg.class, DividePtg.class,
                IntPtg.class, IntPtg.class, MultiplyPtg.class, SubtractPtg.class);

        // 2 * (2^2)
        // NOT: (2 *2) ^ 2 -> int int multiply int power
        confirmTokenClasses("2*2^2", IntPtg.class, IntPtg.class, IntPtg.class, PowerPtg.class, MultiplyPtg.class);

        //  2^200% -> 2 not 1.6E58
        confirmTokenClasses("2^200%", IntPtg.class, IntPtg.class, PercentPtg.class, PowerPtg.class);
    }

    /* package */ static Ptg[] confirmTokenClasses(String formula, Class<?>...expectedClasses) {
        Ptg[] ptgs = parseFormula(formula);
        confirmTokenClasses(ptgs, expectedClasses);
        return ptgs;
    }

    private static void confirmTokenClasses(Ptg[] ptgs, Class<?>...expectedClasses) {
        assertEquals(expectedClasses.length, ptgs.length);
        for (int i = 0; i < expectedClasses.length; i++) {
            assertEquals(expectedClasses[i], ptgs[i].getClass(),
                "difference at token[" + i + "]: expected ("
                + expectedClasses[i].getName() + ") but got ("
                + ptgs[i].getClass().getName() + ")");
        }
    }

    @Test
    void testPower() {
        confirmTokenClasses("2^5", IntPtg.class, IntPtg.class, PowerPtg.class);
    }

    private static Ptg parseSingleToken(String formula, Class<? extends Ptg> ptgClass) {
        Ptg[] ptgs = parseFormula(formula);
        assertEquals(1, ptgs.length);
        Ptg result = ptgs[0];
        assertEquals(ptgClass, result.getClass());
        return result;
    }

    @Test
    void testParseNumber() {
        IntPtg ip;

        // bug 33160
        ip = (IntPtg) parseSingleToken("40", IntPtg.class);
        assertEquals(40, ip.getValue());
        ip = (IntPtg) parseSingleToken("40000", IntPtg.class);
        assertEquals(40000, ip.getValue());

        // check the upper edge of the IntPtg range:
        ip = (IntPtg) parseSingleToken("65535", IntPtg.class);
        assertEquals(65535, ip.getValue());
        NumberPtg np = (NumberPtg) parseSingleToken("65536", NumberPtg.class);
        assertEquals(65536, np.getValue(), 0);

        np = (NumberPtg) parseSingleToken("65534.6", NumberPtg.class);
        assertEquals(65534.6, np.getValue(), 0);
    }

    @Test
    void testMissingArgs() {
        confirmTokenClasses("if(A1, ,C1)",
                RefPtg.class,
                AttrPtg.class, // tAttrIf
                MissingArgPtg.class,
                AttrPtg.class, // tAttrSkip
                RefPtg.class,
                AttrPtg.class, // tAttrSkip
                FuncVarPtg.class
        );

        confirmTokenClasses("counta( , A1:B2, )", MissingArgPtg.class, AreaPtg.class, MissingArgPtg.class,
                FuncVarPtg.class);
    }

    @Test
    void testParseErrorLiterals() {

        confirmParseErrorLiteral(ErrPtg.NULL_INTERSECTION, "#NULL!");
        confirmParseErrorLiteral(ErrPtg.DIV_ZERO, "#DIV/0!");
        confirmParseErrorLiteral(ErrPtg.VALUE_INVALID, "#VALUE!");
        confirmParseErrorLiteral(ErrPtg.REF_INVALID, "#REF!");
        confirmParseErrorLiteral(ErrPtg.NAME_INVALID, "#NAME?");
        confirmParseErrorLiteral(ErrPtg.NUM_ERROR, "#NUM!");
        confirmParseErrorLiteral(ErrPtg.N_A, "#N/A");
        parseFormula("HLOOKUP(F7,#REF!,G7,#REF!)");
    }

    private static void confirmParseErrorLiteral(ErrPtg expectedToken, String formula) {
        assertEquals(expectedToken, parseSingleToken(formula, ErrPtg.class));
    }

    /**
     * To aid readability the parameters have been encoded with single quotes instead of double
     * quotes.  This method converts single quotes to double quotes before performing the parse
     * and result check.
     */
    private static void confirmStringParse(String singleQuotedValue) {
        // formula: internal quotes become double double, surround with double quotes
        String formula = '"' + singleQuotedValue.replaceAll("'", "\"\"") + '"';
        String expectedValue = singleQuotedValue.replace('\'', '"');

        StringPtg sp = (StringPtg) parseSingleToken(formula, StringPtg.class);
        assertEquals(expectedValue, sp.getValue());
    }

    @Test
    void testParseStringLiterals_bug28754() throws IOException {

        StringPtg sp;
        try {
            sp = (StringPtg) parseSingleToken("\"test\"\"ing\"", StringPtg.class);
        } catch (RuntimeException e) {
            if(e.getMessage().startsWith("Cannot Parse")) {
                fail("Identified bug 28754a");
            }
            throw e;
        }
        assertEquals("test\"ing", sp.getValue());

        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            wb.setSheetName(0, "Sheet1");

            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            cell.setCellFormula("right(\"test\"\"ing\", 3)");
            String actualCellFormula = cell.getCellFormula();
            assertNotEquals("RIGHT(\"test\"ing\",3)", actualCellFormula, "Identified bug 28754b");
            assertEquals("RIGHT(\"test\"\"ing\",3)", actualCellFormula);
        }
    }

    @Test
    void testParseStringLiterals() {
        confirmStringParse("goto considered harmful");

        confirmStringParse("goto 'considered' harmful");

        confirmStringParse("");
        confirmStringParse("'");
        confirmStringParse("''");
        confirmStringParse("' '");
        confirmStringParse(" ' ");
    }

    @Test
    void testParseSumIfSum() {
        String formulaString;
        Ptg[] ptgs;
        ptgs = parseFormula("sum(5, 2, if(3>2, sum(A1:A2), 6))");
        formulaString = toFormulaString(ptgs);
        assertEquals("SUM(5,2,IF(3>2,SUM(A1:A2),6))", formulaString);

        ptgs = parseFormula("if(1<2,sum(5, 2, if(3>2, sum(A1:A2), 6)),4)");
        formulaString = toFormulaString(ptgs);
        assertEquals("IF(1<2,SUM(5,2,IF(3>2,SUM(A1:A2),6)),4)", formulaString);
    }

    @Test
    void testParserErrors() {
        parseExpectedException(" 12 . 345  ");
        parseExpectedException("1 .23  ");

        parseExpectedException("sum(#NAME)");
        parseExpectedException("1 + #N / A * 2");
        parseExpectedException("#value?");
        parseExpectedException("#DIV/ 0+2");


        parseExpectedException("IF(TRUE)");
        parseExpectedException("countif(A1:B5, C1, D1)");

        parseExpectedException("(");
        parseExpectedException(")");
        parseExpectedException("+");
        parseExpectedException("42+");

        parseExpectedException("IF(");
    }

    private static void parseExpectedException(String formula) {
        FormulaParseException e = assertThrows(FormulaParseException.class, () -> parseFormula(formula));
        assertNotNull(e.getMessage());
    }

    @Test
    void testSetFormulaWithRowBeyond32768_Bug44539() throws IOException {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellFormula("SUM(A32769:A32770)");
        assertNotEquals("SUM(A-32767:A-32766)", cell.getCellFormula(), "Identified bug 44539");
        assertEquals("SUM(A32769:A32770)", cell.getCellFormula());

        wb.close();
    }

    @Test
    void testSpaceAtStartOfFormula() {
        // Simulating cell formula of "= 4" (note space)
        // The same Ptg array can be observed if an excel file is saved with that exact formula

        AttrPtg spacePtg = AttrPtg.createSpace(AttrPtg.SpaceType.SPACE_BEFORE, 1);
        Ptg[] ptgs = { spacePtg, new IntPtg(4), };
        String formulaString;
        try {
            formulaString = toFormulaString(ptgs);
        } catch (IllegalStateException e) {
            if(e.getMessage().equalsIgnoreCase("too much stuff left on the stack")) {
                fail("Identified bug 44609");
            }
            // else some unexpected error
            throw e;
        }
        // FormulaParser strips spaces anyway
        assertEquals("4", formulaString);

        ptgs = new Ptg[] { new IntPtg(3), spacePtg, new IntPtg(4), spacePtg, AddPtg.instance, };
        formulaString = toFormulaString(ptgs);
        assertEquals("3+4", formulaString);
    }

    /**
     * Checks some internal error detecting logic ('stack underflow error' in toFormulaString)
     */
    @Test
    void testTooFewOperandArgs() {
        // Simulating badly encoded cell formula of "=/1"
        // Not sure if Excel could ever produce this
        Ptg[] ptgs = {
                // Excel would probably have put tMissArg here
                new IntPtg(1),
                DividePtg.instance,
        };
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> toFormulaString(ptgs));
        assertTrue(e.getMessage().startsWith("Too few arguments supplied to operation"));
    }

    /**
     * Make sure that POI uses the right Func Ptg when encoding formulas.  Functions with variable
     * number of args should get FuncVarPtg, functions with fixed args should get FuncPtg.<p>
     *
     * Prior to the fix for bug 44675 POI would encode FuncVarPtg for all functions.  In many cases
     * Excel tolerates the wrong Ptg and evaluates the formula OK (e.g. SIN), but in some cases
     * (e.g. COUNTIF) Excel fails to evaluate the formula, giving '#VALUE!' instead.
     */
    @Test
    void testFuncPtgSelection() {

        Ptg[] ptgs = parseFormula("countif(A1:A2, 1)");
        assertEquals(3, ptgs.length);
        assertFalse(ptgs[2] instanceof FuncVarPtg, "Identified bug 44675");
        confirmTokenClasses(ptgs, AreaPtg.class, IntPtg.class, FuncPtg.class);

        confirmTokenClasses("sin(1)", IntPtg.class, FuncPtg.class);
    }

    @Test
    void testWrongNumberOfFunctionArgs() throws IOException {
        confirmArgCountMsg("sin()", "Too few arguments to function 'SIN'. Expected 1 but got 0.");
        confirmArgCountMsg("countif(1, 2, 3, 4)", "Too many arguments to function 'COUNTIF'. Expected 2 but got 4.");
        confirmArgCountMsg("index(1, 2, 3, 4, 5, 6)", "Too many arguments to function 'INDEX'. At most 4 were expected but got 6.");
        confirmArgCountMsg("vlookup(1, 2)", "Too few arguments to function 'VLOOKUP'. At least 3 were expected but got 2.");
    }

    private static void confirmArgCountMsg(String formula, String expectedMessage) throws IOException {
        try (HSSFWorkbook book = new HSSFWorkbook()) {
            FormulaParseException e = assertThrows(FormulaParseException.class, () -> HSSFFormulaParser.parse(formula, book));
            confirmParseException(e, expectedMessage);
        }
    }

    @Test
    void testParseErrorExpectedMsg() {
        FormulaParseException e;
        e = assertThrows(FormulaParseException.class, () -> parseFormula("round(3.14;2)"));
        confirmParseException(e, "Parse error near char 10 ';' in specified formula 'round(3.14;2)'. Expected ',' or ')'");

        e = assertThrows(FormulaParseException.class, () -> parseFormula(" =2+2"));
        confirmParseException(e, "The specified formula ' =2+2' starts with an equals sign which is not allowed.");
    }

    /**
     * this function name has a dot in it.
     */
    @Test
    void testParseErrorTypeFunction() {

        Ptg[] ptgs;
        try {
            ptgs = parseFormula("error.type(A1)");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Invalid Formula cell reference: 'error'")) {
                fail("Identified bug 45334");
            }
            throw e;
        }
        confirmTokenClasses(ptgs, RefPtg.class, FuncPtg.class);
        assertEquals("ERROR.TYPE", ((FuncPtg) ptgs[1]).getName());
    }

    @Test
    void testNamedRangeThatLooksLikeCell() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Sheet1");
            HSSFName name = wb.createName();
            name.setRefersToFormula("Sheet1!B1");
            name.setNameName("pfy1");

            Ptg[] ptgs;
            try {
                ptgs = HSSFFormulaParser.parse("count(pfy1)", wb);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().equals("Specified colIx (1012) is out of range")) {
                    fail("Identified bug 45354");
                }
                throw e;
            }
            confirmTokenClasses(ptgs, NamePtg.class, FuncVarPtg.class);

            HSSFCell cell = sheet.createRow(0).createCell(0);
            cell.setCellFormula("count(pfy1)");
            assertEquals("COUNT(pfy1)", cell.getCellFormula());
            FormulaParseException e = assertThrows(FormulaParseException.class, () -> cell.setCellFormula("count(pf1)"));
            confirmParseException(e, "Specified named range 'pf1' does not exist in the current workbook.");
            cell.setCellFormula("count(fp1)"); // plain cell ref, col is in range
        }
    }

    @Test
    void testParseAreaRefHighRow_bug45358() throws IOException {
        Ptg[] ptgs;
        AreaI aptg;

        HSSFWorkbook book = new HSSFWorkbook();
        book.createSheet("Sheet1");

        ptgs = HSSFFormulaParser.parse("Sheet1!A10:A40000", book);
        aptg = (AreaI) ptgs[0];
        assertNotEquals(-25537, aptg.getLastRow(), "Identified bug 45358");
        assertEquals(39999, aptg.getLastRow());

        ptgs = HSSFFormulaParser.parse("Sheet1!A10:A65536", book);
        aptg = (AreaI) ptgs[0];
        assertEquals(65535, aptg.getLastRow());

        // plain area refs should be ok too
        ptgs = parseFormula("A10:A65536");
        aptg = (AreaI) ptgs[0];
        assertEquals(65535, aptg.getLastRow());

        book.close();
    }

    @Test
    void testParseArray()  {
        Ptg[] ptgs;
        ptgs = parseFormula("mode({1,2,2,#REF!;FALSE,3,3,2})");
        confirmTokenClasses(ptgs, ArrayPtg.class, FuncVarPtg.class);
        assertEquals("{1,2,2,#REF!;FALSE,3,3,2}", ptgs[0].toFormulaString());

        ArrayPtg aptg = (ArrayPtg) ptgs[0];
        Object[][] values = aptg.getTokenArrayValues();
        assertEquals(ErrorConstant.valueOf(FormulaError.REF.getCode()), values[0][3]);
        assertEquals(Boolean.FALSE, values[1][0]);
    }

    @Test
    void testParseStringElementInArray() {
        Ptg[] ptgs;
        ptgs = parseFormula("MAX({\"5\"},3)");
        confirmTokenClasses(ptgs, ArrayPtg.class, IntPtg.class, FuncVarPtg.class);
        Object element = ((ArrayPtg)ptgs[0]).getTokenArrayValues()[0][0];
        // this would cause ClassCastException below
        assertFalse(element instanceof UnicodeString, "Wrong encoding of array element value");
        assertEquals(String.class, element.getClass());

        // make sure the formula encodes OK
        int encSize = Ptg.getEncodedSize(ptgs);
        byte[] data = new byte[encSize];
        Ptg.serializePtgs(ptgs, data, 0);
        byte[] expData = HexRead.readFromString(
                "20 00 00 00 00 00 00 00 " // tArray
                + "1E 03 00 "      // tInt(3)
                + "42 02 07 00 "   // tFuncVar(MAX) 2-arg
                + "00 00 00 "      // Array data: 1 col, 1 row
                + "02 01 00 00 35" // elem (type=string, len=1, "5")
        );
        assertArrayEquals(expData, data);
        int initSize = Ptg.getEncodedSizeWithoutArrayData(ptgs);
        Ptg[] ptgs2 = Ptg.readTokens(initSize, new LittleEndianByteArrayInputStream(data));
        confirmTokenClasses(ptgs2, ArrayPtg.class, IntPtg.class, FuncVarPtg.class);
    }

    @Test
    void testParseArrayNegativeElement() {
        Ptg[] ptgs;
        try {
            ptgs = parseFormula("{-42}");
        } catch (FormulaParseException e) {
            if (e.getMessage().equals("Parse error near char 1 '-' in specified formula '{-42}'. Expected Integer")) {
                fail("Identified bug - failed to parse negative array element.");
            }
            throw e;
        }
        confirmTokenClasses(ptgs, ArrayPtg.class);
        Object element = ((ArrayPtg)ptgs[0]).getTokenArrayValues()[0][0];

        assertEquals(-42.0, (Double) element, 0.0);

        // Should be able to handle whitespace between unary minus and digits (Excel
        // accepts this formula after presenting the user with a confirmation dialog).
        ptgs = parseFormula("{- 5}");
        element = ((ArrayPtg)ptgs[0]).getTokenArrayValues()[0][0];
        assertEquals(-5.0, (Double) element, 0.0);
    }

    @Test
    void testRangeOperator() throws IOException {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFCell cell = sheet.createRow(0).createCell(0);

        wb.setSheetName(0, "Sheet1");
        cell.setCellFormula("Sheet1!B$4:Sheet1!$C1"); // explicit range ':' operator
        assertEquals("Sheet1!B$4:Sheet1!$C1", cell.getCellFormula());

        cell.setCellFormula("Sheet1!B$4:$C1"); // plain area ref
        assertEquals("Sheet1!B1:$C$4", cell.getCellFormula()); // note - area ref is normalised

        cell.setCellFormula("Sheet1!$C1...B$4"); // different syntax for plain area ref
        assertEquals("Sheet1!B1:$C$4", cell.getCellFormula());

        // with funny sheet name
        wb.setSheetName(0, "A1...A2");
        cell.setCellFormula("A1...A2!B1");
        assertEquals("A1...A2!B1", cell.getCellFormula());

        wb.close();
    }

    @Test
    void testBooleanNamedSheet() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("true");
        HSSFCell cell = sheet.createRow(0).createCell(0);
        cell.setCellFormula("'true'!B2");

        assertEquals("'true'!B2", cell.getCellFormula());

        wb.close();
    }

    @Test
    void testParseExternalWorkbookReference() throws IOException {
        HSSFWorkbook wbA = HSSFTestDataSamples.openSampleWorkbook("multibookFormulaA.xls");
        HSSFCell cell = wbA.getSheetAt(0).getRow(0).getCell(0);

        // make sure formula in sample is as expected
        assertEquals("[multibookFormulaB.xls]BSheet1!B1", cell.getCellFormula());
        Ptg[] expectedPtgs = FormulaExtractor.getPtgs(cell);
        confirmSingle3DRef(expectedPtgs, 1);

        // now try (re-)parsing the formula
        Ptg[] actualPtgs = HSSFFormulaParser.parse("[multibookFormulaB.xls]BSheet1!B1", wbA);
        confirmSingle3DRef(actualPtgs, 1); // externalSheetIndex 1 -> BSheet1

        // try parsing a formula pointing to a different external sheet
        Ptg[] otherPtgs = HSSFFormulaParser.parse("[multibookFormulaB.xls]AnotherSheet!B1", wbA);
        confirmSingle3DRef(otherPtgs, 0); // externalSheetIndex 0 -> AnotherSheet

        // try setting the same formula in a cell
        cell.setCellFormula("[multibookFormulaB.xls]AnotherSheet!B1");
        assertEquals("[multibookFormulaB.xls]AnotherSheet!B1", cell.getCellFormula());

        wbA.close();
    }

    private static void confirmSingle3DRef(Ptg[] ptgs, int expectedExternSheetIndex) {
        assertEquals(1, ptgs.length);
        Ptg ptg0 = ptgs[0];
        assertTrue(ptg0 instanceof Ref3DPtg);
        assertEquals(expectedExternSheetIndex, ((Ref3DPtg)ptg0).getExternSheetIndex());
    }

    @Test
    void testUnion() throws IOException {
        String formula = "Sheet1!$B$2:$C$3,OFFSET(Sheet1!$E$2:$E$4,1,Sheet1!$A$1),Sheet1!$D$6";
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");
        Ptg[] ptgs = FormulaParser.parse(formula, HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1);

        confirmTokenClasses(ptgs,
                // TODO - AttrPtg.class, // Excel prepends this
                MemFuncPtg.class,
                Area3DPtg.class,
                Area3DPtg.class,
                IntPtg.class,
                Ref3DPtg.class,
                FuncVarPtg.class,
                UnionPtg.class,
                Ref3DPtg.class,
                UnionPtg.class
        );
        MemFuncPtg mf = (MemFuncPtg)ptgs[0];
        assertEquals(45, mf.getLenRefSubexpression());

        // We don't check the type of the operands.
        confirmTokenClasses("1,2", MemAreaPtg.class, IntPtg.class, IntPtg.class, UnionPtg.class);

        wb.close();
    }

    @Test
    void testIntersection() throws IOException {
       String formula = "Sheet1!$B$2:$C$3 OFFSET(Sheet1!$E$2:$E$4, 1,Sheet1!$A$1) Sheet1!$D$6";
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");
        Ptg[] ptgs = FormulaParser.parse(formula, HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1);

        confirmTokenClasses(ptgs,
                // TODO - AttrPtg.class, // Excel prepends this
                MemFuncPtg.class,
                Area3DPtg.class,
                Area3DPtg.class,
                IntPtg.class,
                Ref3DPtg.class,
                FuncVarPtg.class,
                IntersectionPtg.class,
                Ref3DPtg.class,
                IntersectionPtg.class
        );
        MemFuncPtg mf = (MemFuncPtg)ptgs[0];
        assertEquals(45, mf.getLenRefSubexpression());

        // This used to be an error but now parses.  Union has the same behaviour.
        confirmTokenClasses("1 2", MemAreaPtg.class, IntPtg.class, IntPtg.class, IntersectionPtg.class);

        wb.close();
    }

    @Test
    void testComparisonInParen() {
        confirmTokenClasses("(A1 > B2)",
            RefPtg.class,
            RefPtg.class,
            GreaterThanPtg.class,
            ParenthesisPtg.class
        );
    }

    @Test
    void testUnionInParen() {
        confirmTokenClasses("(A1:B2,B2:C3)",
          MemAreaPtg.class,
          AreaPtg.class,
          AreaPtg.class,
          UnionPtg.class,
          ParenthesisPtg.class
        );
    }

    @Test
    void testIntersectionInParen() {
        confirmTokenClasses("(A1:B2 B2:C3)",
            MemAreaPtg.class,
            AreaPtg.class,
            AreaPtg.class,
            IntersectionPtg.class,
            ParenthesisPtg.class
        );
    }

    // https://bz.apache.org/bugzilla/show_bug.cgi?id=60980
    @Test
    void testIntersectionInFunctionArgs() {
        confirmTokenClasses("SUM(A1:B2 B2:C3)",
                MemAreaPtg.class,
                AreaPtg.class,
                AreaPtg.class,
                IntersectionPtg.class,
                AttrPtg.class
        );
    }

    @Test
    void testIntersectionNamesInFunctionArgs() {
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFName name1 = wb.createName();
        name1.setNameName("foo1");
        name1.setRefersToFormula("A1:A3");

        HSSFName name2 = wb.createName();
        name2.setNameName("foo2");
        name2.setRefersToFormula("A1:B3");

        Ptg[] ptgs = FormulaParser.parse("SUM(foo1 foo2)", HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1);

        confirmTokenClasses(ptgs,
                MemFuncPtg.class,
                NamePtg.class,
                NamePtg.class,
                IntersectionPtg.class,
                AttrPtg.class
        );
    }

    @Test
    void testRange_bug46643() throws IOException {
        String formula = "Sheet1!A1:Sheet1!B3";
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");
        Ptg[] ptgs = FormulaParser.parse(formula, HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1, -1);

        if (ptgs.length == 3) {
            confirmTokenClasses(ptgs, Ref3DPtg.class, Ref3DPtg.class, RangePtg.class);
            fail("Identified bug 46643");
        }

        confirmTokenClasses(ptgs,
                MemFuncPtg.class,
                Ref3DPtg.class,
                Ref3DPtg.class,
                RangePtg.class
        );
        MemFuncPtg mf = (MemFuncPtg)ptgs[0];
        assertEquals(15, mf.getLenRefSubexpression());
        wb.close();
    }

    /** Named ranges with backslashes, e.g. 'POI\\2009' */
    @Test
    void testBackSlashInNames() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFName name = wb.createName();
        name.setNameName("POI\\2009");
        name.setRefersToFormula("Sheet1!$A$1");

        HSSFSheet sheet = wb.createSheet();
        HSSFRow row = sheet.createRow(0);

        HSSFCell cell_C1 =  row.createCell(2);
        cell_C1.setCellFormula("POI\\2009");
        assertEquals("POI\\2009", cell_C1.getCellFormula());

        HSSFCell cell_D1 = row.createCell(2);
        cell_D1.setCellFormula("NOT(POI\\2009=\"3.5-final\")");
        assertEquals("NOT(POI\\2009=\"3.5-final\")", cell_D1.getCellFormula());

        wb.close();
    }

    /**
     * See the related/similar test: {@link BaseTestBugzillaIssues#bug42448()}
     */
    @Test
    void testParseAbnormalSheetNamesAndRanges_bug42448() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("A");
        try {
            HSSFFormulaParser.parse("SUM(A!C7:A!C67)", wb);
        } catch (StringIndexOutOfBoundsException e) {
            fail("Identified bug 42448");
        }
        // the exact example from the bugzilla description:
        HSSFFormulaParser.parse("SUMPRODUCT(A!C7:A!C67, B8:B68) / B69", wb);

        wb.close();
    }

    @Test
    void testRangeFuncOperand_bug46951() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            Ptg[] ptgs;
            try {
                ptgs = HSSFFormulaParser.parse("SUM(C1:OFFSET(C1,0,B1))", wb);
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Specified named range 'OFFSET' does not exist in the current workbook.")) {
                    fail("Identified bug 46951");
                }
                throw e;
            }
            confirmTokenClasses(ptgs,
                MemFuncPtg.class, // [len=23]
                RefPtg.class, // [C1]
                RefPtg.class, // [C1]
                IntPtg.class, // [0]
                RefPtg.class, // [B1]
                FuncVarPtg.class, // [OFFSET nArgs=3]
                RangePtg.class, //
                AttrPtg.class // [sum ]
            );
        }
    }

    @Test
    void testUnionOfFullCollFullRowRef() throws IOException {
        parseFormula("3:4");
        Ptg[] ptgs = parseFormula("$Z:$AC");
        confirmTokenClasses(ptgs, AreaPtg.class);
        parseFormula("B:B");

        ptgs = parseFormula("$11:$13");
        confirmTokenClasses(ptgs, AreaPtg.class);

        ptgs = parseFormula("$A:$A,$1:$4");
        confirmTokenClasses(ptgs, MemAreaPtg.class,
                AreaPtg.class,
                AreaPtg.class,
                UnionPtg.class
        );

        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");
        ptgs = HSSFFormulaParser.parse("Sheet1!$A:$A,Sheet1!$1:$4", wb);
        confirmTokenClasses(ptgs, MemFuncPtg.class,
                Area3DPtg.class,
                Area3DPtg.class,
                UnionPtg.class
        );

        ptgs = HSSFFormulaParser.parse("'Sheet1'!$A:$A,'Sheet1'!$1:$4", wb);
        confirmTokenClasses(ptgs,
                MemFuncPtg.class,
                Area3DPtg.class,
                Area3DPtg.class,
                UnionPtg.class
        );

        wb.close();
    }

    @Test
    void testExplicitRangeWithTwoSheetNames() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");
        Ptg[] ptgs = HSSFFormulaParser.parse("Sheet1!F1:Sheet1!G2", wb);
        confirmTokenClasses(ptgs,
                MemFuncPtg.class,
                Ref3DPtg.class,
                Ref3DPtg.class,
                RangePtg.class
        );
        MemFuncPtg mf;
        mf = (MemFuncPtg)ptgs[0];
        assertEquals(15, mf.getLenRefSubexpression());
        wb.close();
    }

    /**
     * Checks that the area-ref and explicit range operators get the right associativity
     * and that the {@link MemFuncPtg} / {@link MemAreaPtg} is added correctly
     */
    @Test
    void testComplexExplicitRangeEncodings() {

        Ptg[] ptgs;
        ptgs = parseFormula("SUM(OFFSET(A1,0,0):B2:C3:D4:E5:OFFSET(F6,1,1):G7)");
        confirmTokenClasses(ptgs,
            // AttrPtg.class, // [volatile ] // POI doesn't do this yet (Apr 2009)
            MemFuncPtg.class, // len 57
            RefPtg.class, // [A1]
            IntPtg.class, // [0]
            IntPtg.class, // [0]
            FuncVarPtg.class, // [OFFSET nArgs=3]
            AreaPtg.class, // [B2:C3]
            RangePtg.class,
            AreaPtg.class, // [D4:E5]
            RangePtg.class,
            RefPtg.class, // [F6]
            IntPtg.class, // [1]
            IntPtg.class, // [1]
            FuncVarPtg.class, // [OFFSET nArgs=3]
            RangePtg.class,
            RefPtg.class, // [G7]
            RangePtg.class,
            AttrPtg.class // [sum ]
        );

        MemFuncPtg mf = (MemFuncPtg)ptgs[0];
        assertEquals(57, mf.getLenRefSubexpression());
        assertEquals("D4:E5", ptgs[7].toFormulaString());
        assertTrue(((AttrPtg)ptgs[16]).isSum());

        ptgs = parseFormula("SUM(A1:B2:C3:D4)");
        confirmTokenClasses(ptgs,
            // AttrPtg.class, // [volatile ] // POI doesn't do this yet (Apr 2009)
                MemAreaPtg.class, // len 19
                AreaPtg.class, // [A1:B2]
                AreaPtg.class, // [C3:D4]
                RangePtg.class,
                AttrPtg.class // [sum ]
        );
        MemAreaPtg ma = (MemAreaPtg)ptgs[0];
        assertEquals(19, ma.getLenRefSubexpression());
    }


    /**
     * Mostly confirming that erroneous conditions are detected.  Actual error message wording is not critical.
     *
     */
    @Test
    void testEdgeCaseParserErrors() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("Sheet1");

        confirmParseError(wb, "A1:ROUND(B1,1)", "The RHS of the range operator ':' at position 3 is not a proper reference.");

        confirmParseError(wb, "Sheet1!!!", "Parse error near char 7 '!' in specified formula 'Sheet1!!!'. Expected number, string, defined name, or data table");
        confirmParseError(wb, "Sheet1!.Name", "Parse error near char 7 '.' in specified formula 'Sheet1!.Name'. Expected number, string, defined name, or data table");
        confirmParseError(wb, "Sheet1!Sheet1", "Specified name 'Sheet1' for sheet Sheet1 not found");
        confirmParseError(wb, "Sheet1!F:Sheet1!G", "'Sheet1!F' is not a proper reference.");
        confirmParseError(wb, "Sheet1!F..foobar", "Complete area reference expected after sheet name at index 11.");
        confirmParseError(wb, "Sheet1!A .. B", "Dotted range (full row or column) expression 'A .. B' must not contain whitespace.");
        confirmParseError(wb, "Sheet1!A...B", "Dotted range (full row or column) expression 'A...B' must have exactly 2 dots.");
        confirmParseError(wb, "Sheet1!A foobar", "Second part of cell reference expected after sheet name at index 10.");

        confirmParseError(wb, "foobar", "Specified named range 'foobar' does not exist in the current workbook.");
        confirmParseError(wb, "A1:1", "The RHS of the range operator ':' at position 3 is not a proper reference.");
        wb.close();
    }

    private static void confirmParseError(HSSFWorkbook wb, String formula, String expectedMessage) {
        FormulaParseException e = assertThrows(FormulaParseException.class, () -> HSSFFormulaParser.parse(formula, wb));
        confirmParseException(e, expectedMessage);
    }

    /**
     * In bug 47078, POI had trouble evaluating a defined name flagged as 'complex'.
     * POI should also be able to parse such defined names.
     */
    @Test
    void testParseComplexName() throws IOException {

        // Mock up a spreadsheet to match the critical details of the sample
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            wb.createSheet("Sheet1");
            HSSFName definedName = wb.createName();
            definedName.setNameName("foo");
            definedName.setRefersToFormula("Sheet1!B2");

            // Set the complex flag - POI doesn't usually manipulate this flag
            NameRecord nameRec = TestHSSFName.getNameRecord(definedName);
            nameRec.setOptionFlag((short) 0x10); // 0x10 -> complex

            Ptg[] result;
            try {
                result = HSSFFormulaParser.parse("1+foo", wb);
            } catch (FormulaParseException e) {
                if (e.getMessage().equals("Specified name 'foo' is not a range as expected.")) {
                    fail("Identified bug 47078c");
                }
                throw e;
            }
            confirmTokenClasses(result, IntPtg.class, NamePtg.class, AddPtg.class);
        }
    }

    /**
     * Zero is not a valid row number so cell references like 'A0' are not valid.
     * Actually, they should be treated like defined names.
     * <br>
     * In addition, leading zeros (on the row component) should be removed from cell
     * references during parsing.
     */
    @Test
    void testZeroRowRefs() throws IOException {
        String badCellRef = "B0"; // bad because zero is not a valid row number
        String leadingZeroCellRef = "B000001"; // this should get parsed as "B1"
        HSSFWorkbook wb = new HSSFWorkbook();

        FormulaParseException e = assertThrows(FormulaParseException.class, () -> HSSFFormulaParser.parse(badCellRef, wb),
            "Identified bug 47312b - Shouldn't be able to parse cell ref '" + badCellRef + "'.");
        confirmParseException(e, "Specified named range '" + badCellRef + "' does not exist in the current workbook.");

        Ptg[] ptgs;
        try {
            ptgs = HSSFFormulaParser.parse(leadingZeroCellRef, wb);
            assertEquals("B1", ptgs[0].toFormulaString());
        } catch (FormulaParseException e2) {
            confirmParseException(e2, "Specified named range '" + leadingZeroCellRef + "' does not exist in the current workbook.");
            // close but no cigar
            fail("Identified bug 47312c - '" + leadingZeroCellRef + "' should parse as 'B1'.");
        }

        // create a defined name called 'B0' and try again
        Name n = wb.createName();
        n.setNameName("B0");
        n.setRefersToFormula("1+1");
        ptgs = HSSFFormulaParser.parse("B0", wb);
        confirmTokenClasses(ptgs, NamePtg.class);

        wb.close();
    }

    private static void confirmParseException(FormulaParseException e, String expMsg) {
        assertEquals(expMsg, e.getMessage());
    }

    @Test
    void test57196_Formula() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Ptg[] ptgs = HSSFFormulaParser.parse("DEC2HEX(HEX2DEC(O8)-O2+D2)", wb, FormulaType.CELL, -1);
        assertNotNull(ptgs, "Ptg array should not be null");

        confirmTokenClasses(ptgs,
            NameXPtg.class, // ??
            NameXPtg.class, // ??
            RefPtg.class, // O8
            FuncVarPtg.class, // HEX2DEC
            RefPtg.class, // O2
            SubtractPtg.class,
            RefPtg.class,   // D2
            AddPtg.class,
            FuncVarPtg.class // DEC2HEX
        );

        RefPtg o8 = (RefPtg) ptgs[2];
        FuncVarPtg hex2Dec = (FuncVarPtg) ptgs[3];
        RefPtg o2 = (RefPtg) ptgs[4];
        RefPtg d2 = (RefPtg) ptgs[6];
        FuncVarPtg dec2Hex = (FuncVarPtg) ptgs[8];

        assertEquals("O8", o8.toFormulaString());
        assertEquals(255, hex2Dec.getFunctionIndex());
        //assertEquals("", hex2Dec.toString());
        assertEquals("O2", o2.toFormulaString());
        assertEquals("D2", d2.toFormulaString());
        assertEquals(255, dec2Hex.getFunctionIndex());

        wb.close();
    }
}
